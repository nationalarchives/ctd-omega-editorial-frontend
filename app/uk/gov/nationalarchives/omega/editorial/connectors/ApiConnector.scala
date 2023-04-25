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
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import play.api.inject.ApplicationLifecycle
import uk.gov.nationalarchives.omega.editorial.config.Config
import uk.gov.nationalarchives.omega.editorial.connectors.messages.RequestMessage
import uk.gov.nationalarchives.omega.editorial.connectors.messages.uk.gov.nationalarchives.omega.editorial.connectors.messages.ReplyMessage

import javax.inject.{Inject, Singleton}

@Singleton
class ApiConnector @Inject()(
  config: Config,
  lifecycle: ApplicationLifecycle
) {

  private implicit val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]
  private val requestQueueName = "request-general"
  private val replyQueueName = "omega-editorial-web-application-instance-1"
  private lazy val (client, closer): (JmsRequestReplyClient[IO], IO[Unit]) = createClientAndCloser.unsafeRunSync()
  private lazy val handler: RequestReplyHandler = RequestReplyHandler(client)


  private def createClientAndCloser: IO[(JmsRequestReplyClient[IO], IO[Unit])] =
    registerStopHook() *>
      logger.info(s"Attempting to subscribe to $replyQueueName...") *>
      JmsRequestReplyClient.createForSqs[IO](config.broker, config.credentials)(replyQueueName).allocated

  private def registerStopHook(): IO[Unit] = IO.delay {
    lifecycle.addStopHook { () =>
      closer.unsafeToFuture()
    }
  }

  def handle(messageType: MessageType, requestBody: String): IO[ReplyMessage] =
    handler.handle(
      requestQueueName,
      requestMessage = RequestMessage(requestBody, ApiConnector.applicationId, messageType.value)
    )

}

object ApiConnector {

  val applicationId = "PACE001"

}
