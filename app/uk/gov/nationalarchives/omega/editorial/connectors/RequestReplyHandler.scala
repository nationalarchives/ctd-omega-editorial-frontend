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
import cats.effect.std.Queue
import org.typelevel.log4cats.Logger
import uk.gov.nationalarchives.omega.editorial.connectors.JmsRequestReplyClient.ReplyMessageHandler
import uk.gov.nationalarchives.omega.editorial.connectors.messages.{ ReplyMessage, RequestMessage }

case class RequestReplyHandler(client: JmsRequestReplyClient[IO]) {

  /** Convenience method for binding a request and its reply
    * @param requestQueue
    *   the JMS queue to send the message to
    * @param requestMessage
    *   the JMS message
    * @return
    */
  def handle(requestQueue: String, requestMessage: RequestMessage)(implicit L: Logger[IO]): IO[ReplyMessage] =
    Queue.bounded[IO, ReplyMessage](1).flatMap { queue =>
      val replyHandler: ReplyMessageHandler[IO] = replyMessage => queue.offer(replyMessage)
      client.request(requestQueue, requestMessage, replyHandler) flatMap { _ =>
        queue.take
      }
    }

}
