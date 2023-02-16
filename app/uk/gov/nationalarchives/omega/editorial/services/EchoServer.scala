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
import cats.implicits._
import cats.instances.either._
import javax.inject.Singleton
import jms4s.config.QueueName
import jms4s.jms.JmsMessage
import jms4s.jms.MessageFactory
import jms4s.JmsAcknowledgerConsumer.AckAction
import jms4s.JmsClient
import jms4s.sqs.simpleQueueService
import jms4s.sqs.simpleQueueService._
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jFactory
import org.typelevel.log4cats.slf4j.Slf4jLogger
import play.api.libs.json.{ Reads, Json }

import scala.concurrent.duration.DurationInt

import uk.gov.nationalarchives.omega.editorial.editSets
import uk.gov.nationalarchives.omega.editorial.models.EditSet
import uk.gov.nationalarchives.omega.editorial.models.GetEditSet
import cats.MonadError
import org.typelevel.log4cats.Logger

// Copied (more or less) from https://github.com/nationalarchives/jms4s-request-reply-stub
@Singleton
class EchoServer {
  import EchoServer._

  // private implicit val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]

  private val requestQueueName = QueueName("request-general")
  private val responseQueryName = QueueName("omega-editorial-web-application-instance-1")
  private val consumerConcurrencyLevel = 1

  // private val jmsClient: Resource[IO, JmsClient[IO]] = simpleQueueService.makeJmsClient[IO](
  //   Config(
  //     endpoint = Endpoint(Some(DirectAddress(HTTP, "localhost", Some(9324))), "elasticmq"),
  //     credentials = None,
  //     clientId = ClientId("echo_server_1"),
  //     None
  //   )
  // )

  private val jmsClient: Resource[IO, JmsClient[IO]] = ???

  def eitherToIO[F[_], A](x: Either[EchoServerError, A])(implicit me: MonadError[F, Throwable]): F[A] =
    x match {
      case Right(ok) => me.pure(ok)
      case Left(err) => me.raiseError(err)
    }

  def handleMessage[F[_]](
    jmsMessage: JmsMessage,
    messageFactory: MessageFactory[F]
  )(implicit me: MonadError[F, Throwable], l: Logger[F]): F[JmsMessage.JmsTextMessage] =
    for {
      requestMessageId <- eitherToIO[F, String](jmsMessageId(jmsMessage))
      _ <- Logger[F].info("")
      responseText <- eitherToIO[F, String](createResponse(jmsMessage))
      responseMessage <- messageFactory.makeTextMessage(responseText)
      _ = responseMessage.setJMSCorrelationId(requestMessageId)
    } yield responseMessage

  def startEchoServer: IO[Unit] = {
    val consumerResource = for {
       // _ <- Resource.eval(logger.info("Starting EchoServer..."))
      client <- jmsClient
      consumer <- client.createAcknowledgerConsumer(
                    requestQueueName,
                    concurrencyLevel = consumerConcurrencyLevel,
                    pollingInterval = 50.millis
                  )
    } yield consumer

    ???
    // consumerResource
    //   .use(_.handle { (jmsMessage, messageFactory) =>
    //     handleMessage[IO](jmsMessage, messageFactory).map { message =>
    //       AckAction.send(message, responseQueryName)
    //     }
    //   })

  }
}

object EchoServer {

  sealed abstract class EchoServerError extends Throwable

  final case object MissingSID extends EchoServerError
  final case object MissingJMSID extends EchoServerError
  final case class SidNotFound(badSid: String) extends EchoServerError
  final case class NotATextMessage(err: Throwable) extends EchoServerError
  final case class CannotParse(txt: String) extends EchoServerError

  type Outcome[A] = Either[EchoServerError, A]

  val sidHeaderKey = "sid"
  val sid1 = "OSGEES001"

  def jmsMessageId(jmsMessage: JmsMessage): Outcome[String] =
    jmsMessage.getJMSMessageId.toRight { MissingJMSID }

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

  def messageText(jmsMessage: JmsMessage): Outcome[String] =
    jmsMessage.asTextF.toEither.leftMap { NotATextMessage }

  def getEditSet(jmsMessage: JmsMessage): Outcome[EditSet] =
    for {
      messageText <- messageText(jmsMessage)
      // TODO We'll need to get the OCI from the GetEditSet eventuly
      _ <- parse[GetEditSet](messageText)
    } yield editSets.editSet1


}
