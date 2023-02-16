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


import cats.MonadError
import cats.effect._
import cats.implicits._
import cats.instances.either._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.monadError._
import javax.inject.Singleton
import jms4s.JmsAcknowledgerConsumer.AckAction
import jms4s.JmsClient
import jms4s.config.QueueName
import jms4s.jms.JmsMessage
import jms4s.sqs.simpleQueueService
import jms4s.sqs.simpleQueueService._
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jFactory
import play.api.libs.json.{ Reads, Json }
import scala.concurrent.duration.DurationInt
import uk.gov.nationalarchives.omega.editorial.editSets
import uk.gov.nationalarchives.omega.editorial.models.EditSet
import uk.gov.nationalarchives.omega.editorial.models.GetEditSet

// Copied (more or less) from https://github.com/nationalarchives/jms4s-request-reply-stub
@Singleton
class EchoServer {
  import EchoServer._

  private implicit val logger: SelfAwareStructuredLogger[IO] = Slf4jFactory[IO].getLogger
  private val requestQueueName = QueueName("request-general")
  private val responseQueryName = QueueName("omega-editorial-web-application-instance-1")
  private val consumerConcurrencyLevel = 1

  private val jmsClient: Resource[IO, JmsClient[IO]] = simpleQueueService.makeJmsClient[IO](
    Config(
      endpoint = Endpoint(Some(DirectAddress(HTTP, "localhost", Some(9324))), "elasticmq"),
      credentials = None,
      clientId = ClientId("echo_server_1"),
      None
    )
  )

  def startEchoServer: IO[Unit] = {
    val consumerResource = for {
       _ <- Resource.eval(logger.info("Starting EchoServer..."))
      client <- jmsClient
      consumer <- client.createAcknowledgerConsumer(
                    requestQueueName,
                    concurrencyLevel = consumerConcurrencyLevel,
                    pollingInterval = 50.millis
                  )
    } yield consumer

    consumerResource
      .use(_.handle { (jmsMessage, messageFactory) =>
        for {
          // requestMessageId <- jmsMessageId(jmsMessage)
          // responseText <- createResponse(jmsMessage)
          responseMessage <- messageFactory.makeTextMessage("blaf")
          // _ = responseMessage.setJMSCorrelationId(requestMessageId)
          // requestText <- jmsMessage.asTextF[IO]
          // _ <- logger.info(s"Echo Server received message: $requestText")
          // // sid <- getSIDHeader(jmsMessage).left.map(x => throw new Exception(x.toString))
          // responseText = s"Echo Server: $requestText"
          // responseMessage <- messageFactory.makeTextMessage(responseText)
          // requestMessageId = jmsMessage.getJMSMessageId.get
          // _ = responseMessage.setJMSCorrelationId(requestMessageId)
          // _ <- logger.info(s"Echo Server sending response message: $responseText with correlationId: $requestMessageId")
        } yield AckAction.send(???, responseQueryName)
      })

  }
}

object EchoServer {

  sealed abstract class EchoServerError extends Exception

  final case object MissingSID extends EchoServerError
  final case object MissingJMSID extends EchoServerError
  final case class SidNotFound(badSid: String) extends EchoServerError
  final case class NotATextMessage(err: Throwable) extends EchoServerError
  final case class CannotParse(txt: String) extends EchoServerError

  type Outcome[A] = Either[EchoServerError, A]

  val sidHeaderKey = "sid"
  val sid1 = "OSGEES001"

  def jmsMessageId[F[_]](jmsMessage: JmsMessage)(implicit me: MonadError[F, EchoServerError]): F[String] =
    me.fromOption(jmsMessage.getJMSMessageId, { MissingJMSID })

  def createResponse(jmsMessage: JmsMessage): Outcome[String] =
    jmsMessage.getStringProperty(sidHeaderKey) match {
      case Some(sidValue) if sidValue == sid1 => 
        getEditSet(jmsMessage).map { editSet =>
          Json.toJson(editSet).toString
        }

      case Some(sidValue) =>
        Left(SidNotFound(sidValue))

      case None =>
        Left(MissingSID)
    }

  def echoMessage(jmsMessage: JmsMessage): Outcome[String] =
    messageText(jmsMessage).map { requestText =>
      s"Echo Server: $requestText"
    }

  def parse[A](messageText: String)(implicit reads: Reads[A]): Outcome[A] =
    Json.parse(messageText).validate[A].asEither.left.map { _ =>
      CannotParse(messageText)
    }

  def messageText(jmsMessage: JmsMessage): Outcome[String] = ???
  // def messageText[F[_]](jmsMessage: JmsMessage)(implicit me: MonadError[F, EchoServerError]): F[String] =
  //   jmsMessage.asTextF.adaptError { ex => NotATextMessage(ex) }

  def getEditSet(jmsMessage: JmsMessage): Outcome[EditSet] =
    for {
      messageText <- messageText(jmsMessage)
      // TODO We'll need to get the OCI from the GetEditSet eventuly
      _ <- parse[GetEditSet](messageText)
    } yield editSets.editSet1


}
