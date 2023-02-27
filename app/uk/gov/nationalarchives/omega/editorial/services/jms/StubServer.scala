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

package uk.gov.nationalarchives.omega.editorial.services.jms

import cats.effect._
import javax.inject.Singleton
import jms4s.config.QueueName
import jms4s.jms.JmsMessage
import jms4s.jms.MessageFactory
import jms4s.JmsAcknowledgerConsumer.AckAction
import jms4s.JmsClient
import jms4s.sqs.simpleQueueService
import jms4s.sqs.simpleQueueService._
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import scala.concurrent.duration.DurationInt

@Singleton
class StubServer {

  private implicit val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]

  private val responseBuilder = new ResponseBuilder[IO]

  private val requestQueueName = QueueName("request-general")
  private val responseQueryName = QueueName("omega-editorial-web-application-instance-1")
  private val consumerConcurrencyLevel = 1
  private val pollingInterval = 50.millis

  private val jmsClient: Resource[IO, JmsClient[IO]] = simpleQueueService.makeJmsClient[IO](
    Config(
      endpoint = Endpoint(Some(DirectAddress(HTTP, "localhost", Some(9324))), "elasticmq"),
      credentials = None,
      clientId = ClientId("stub_server_1"),
      None
    )
  )

  def start: IO[Unit] = {
    val consumerResource = for {
      _      <- Resource.eval(logger.info("Starting StubServer..."))
      client <- jmsClient
      consumer <- client.createAcknowledgerConsumer(
                    requestQueueName,
                    concurrencyLevel = consumerConcurrencyLevel,
                    pollingInterval = pollingInterval
                  )
      _ <- Resource.eval(consumer.handle { (jmsMessage, messageFactory) =>
             handleMessage(jmsMessage, messageFactory).map { message =>
               AckAction.send(message, responseQueryName)
             }
           })
    } yield consumer

    consumerResource.useForever
  }

  private def handleMessage(
    jmsMessage: JmsMessage,
    messageFactory: MessageFactory[IO]
  ): IO[JmsMessage.JmsTextMessage] =
    for {
      requestMessageId <- responseBuilder.jmsMessageId(jmsMessage)
      _                <- logger.info(s"got a message with ID $requestMessageId")
      responseText     <- responseBuilder.createResponseText(jmsMessage)
      responseMessage  <- messageFactory.makeTextMessage(responseText)
      _ = responseMessage.setJMSCorrelationId(requestMessageId)
    } yield responseMessage

}
