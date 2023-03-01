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

package uk.gov.nationalarchives.omega.editorial.connectors

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import javax.inject.{ Inject, Singleton }
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import play.api.inject.ApplicationLifecycle
import play.api.libs.json.{ Json, Reads }

import uk.gov.nationalarchives.omega.editorial.models.{ EditSet, GetEditSet }
import uk.gov.nationalarchives.omega.editorial.support.TimeProvider
import uk.gov.nationalarchives.omega.editorial.config.{ Config, HostBrokerEndpoint, UsernamePasswordCredentials }

@Singleton
class ApiConnector @Inject() (
  config: Config,
  timeProvider: TimeProvider,
  lifecycle: ApplicationLifecycle
) {
  import ApiConnector._

  private implicit val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]

  private val getEditSetSID = "OSGEES001"

  private val requestQueueName = "request-general"
  private val replyQueueName = "omega-editorial-web-application-instance-1"

  private lazy val (client, closer): (JmsRequestReplyClient[IO], IO[Unit]) = createClientAndCloser.unsafeRunSync()
  private lazy val handler: RequestReplyHandler = RequestReplyHandler(client)

  lifecycle.addStopHook { () =>
    closer.unsafeToFuture()
  }

  def getEditSet(id: String): IO[EditSet] = {
    val now = timeProvider.now()
    val requestBody = Json.stringify(Json.toJson(GetEditSet(id, now)))

    logger.info(s"Requesting edit set $id...") *>
      handle(requestBody).flatMap(parse[EditSet])
  }

  private def createClientAndCloser: IO[(JmsRequestReplyClient[IO], IO[Unit])] =
    logger.info(s"Attempting to subscribe to $replyQueueName...") *>
      JmsRequestReplyClient.createForSqs[IO](config.broker, config.credentials)(replyQueueName).allocated

  private def handle(requestBody: String): IO[String] =
    handler.handle(
      requestQueueName,
      requestMessage = RequestMessage(requestBody, getEditSetSID)
    )

  private def parse[A : Reads](messageText: String): IO[A] =
    IO.fromOption(
      Json.parse(messageText).validate[A].asOpt
    )(CannotParseEditSetResponse(messageText))

}

object ApiConnector {

  case class CannotParseEditSetResponse(response: String) extends Exception(
        s"""can't parse edit set, got:
           |$response
           |""".stripMargin
      )

}
