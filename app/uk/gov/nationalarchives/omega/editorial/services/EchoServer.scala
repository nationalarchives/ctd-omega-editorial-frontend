/*
 * Copyright (c) 2022 The National Archives
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package uk.gov.nationalarchives.omega.editorial.services

import cats.effect._
import jms4s.JmsClient
import jms4s.config.QueueName
import jms4s.sqs.simpleQueueService
import jms4s.sqs.simpleQueueService._
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jFactory
import jms4s.JmsAcknowledgerConsumer.AckAction

import javax.inject.Singleton
import scala.concurrent.duration.DurationInt

// Copied (more or less) from https://github.com/nationalarchives/jms4s-request-reply-stub
@Singleton
class EchoServer extends IOApp {

  private implicit val logger: SelfAwareStructuredLogger[IO] = Slf4jFactory[IO].getLogger
  private val requestQueueName = QueueName("request-general")
  private val responseQueryName = QueueName("omega-editorial-web-application-instance-1")
  private val consumerConcurrencyLevel = 1

  private val jmsClient: Resource[IO, JmsClient[IO]] = simpleQueueService.makeJmsClient[IO](
    Config(
      endpoint = Endpoint(Some(DirectAddress(HTTP, "localhost", Some(9324))), "elasticmq"),
      credentials = Some(Credentials("x", "x")),
      clientId = ClientId("echo_server_1"),
      None
    )
  )

  override def run(args: List[String]): IO[ExitCode] = {

    val consumerResource = for {
      _      <- Resource.liftK(IO.println("Starting EchoServer..."))
      client <- jmsClient
      consumer <- client.createAcknowledgerConsumer(
                    requestQueueName,
                    concurrencyLevel = consumerConcurrencyLevel,
                    pollingInterval = 50.millis
                  )
    } yield consumer

    consumerResource
      .use(_.handle { (jmsMessage, mf) =>
        for {
          requestText     <- jmsMessage.asTextF[IO]
          _               <- IO.println(s"Echo Server received message: $requestText")
          responseText    <- IO.pure(s"Echo Server: $requestText")
          responseMessage <- mf.makeTextMessage(responseText)
          requestMessageId = jmsMessage.getJMSMessageId.get
          _ = responseMessage.setJMSCorrelationId(requestMessageId)
          _ <- IO.println(s"Echo Server sending response message: $responseText with correlationId: $requestMessageId")
        } yield AckAction.send(responseMessage, responseQueryName)
      })
      .as(ExitCode.Success)

  }
}
