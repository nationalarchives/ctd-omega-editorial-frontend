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

import cats.effect.Async
import jms4s.JmsProducer
import jms4s.config.QueueName
import jms4s.jms.JmsMessage
import org.typelevel.log4cats.Logger
import uk.gov.nationalarchives.omega.editorial.connectors.JmsRequestReplyClient.ReplyMessageHandler
import uk.gov.nationalarchives.omega.editorial.connectors.messages.{ MessageProperties, ReplyMessage, RequestMessage }

import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import scala.util.{ Failure, Success, Try }

/** A JMS Request-Reply client.
  *
  * @param requestMap
  *   \- a map of the requests sent and their reply handlers
  * @param producer
  *   \- the JmsProducer used to send the message
  * @tparam F
  */

@Inject()
class JmsRequestReplyClient[F[_] : Async](
  requestMap: ConcurrentHashMap[String, ReplyMessageHandler[F]],
  producer: JmsProducer[F]
) {

  /** @param requestQueue
    *   \- the queue to send the message to
    * @param jmsRequest
    *   \- the request message
    * @param replyMessageHandler
    *   \- the reply handler
    * @return
    */
  def request(requestQueue: String, jmsRequest: RequestMessage, replyMessageHandler: ReplyMessageHandler[F])(implicit
    L: Logger[F]
  ): F[Unit] = {

    val sender: F[Option[String]] = producer.send { messageFactory =>
      val jmsMessage: F[JmsMessage.JmsTextMessage] = messageFactory.makeTextMessage(jmsRequest.body)
      Async[F].map(jmsMessage)(jmsMessage =>
        setProperties(jmsMessage, jmsRequest) match {
          case Success(_) => (jmsMessage, QueueName(requestQueue))
          case Failure(e) =>
            L.error(s"Failed to set SID due to ${e.getMessage}")
            (jmsMessage, QueueName(requestQueue))
        }
      )
    }

    Async[F].flatMap(sender) {
      case Some(messageId) =>
        Async[F].delay {
          val _ = requestMap.put(messageId, replyMessageHandler)
        }
      case None =>
        Async[F].raiseError(
          new IllegalStateException("No messageId obtainable from JMS but application requires messageId support")
        )
    }
  }

  private def setProperties(jmsRequest: JmsMessage, requestMessage: RequestMessage): Try[JmsMessage] =
    for {
      _ <- jmsRequest.setStringProperty(MessageProperties.OMGApplicationID, requestMessage.omgApplicationId)
      _ <- jmsRequest.setStringProperty(MessageProperties.OMGMessageTypeID, requestMessage.omgMessageTypeId)
      _ <- jmsRequest.setStringProperty(MessageProperties.OMGMessageFormat, "application/json")
      _ <- jmsRequest.setStringProperty(MessageProperties.OMGReplyAddress, "PACS001.request")
      _ <- jmsRequest.setStringProperty(MessageProperties.OMGToken, "AbCdEf123456")
    } yield jmsRequest
}
object JmsRequestReplyClient {
  type ReplyMessageHandler[F[_]] = ReplyMessage => F[Unit]

}
