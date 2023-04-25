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

import cats.effect.implicits.genSpawnOps
import cats.effect.{Async, Resource}
import jms4s.JmsAcknowledgerConsumer.AckAction
import jms4s.config.QueueName
import jms4s.jms.{JmsMessage, MessageFactory}
import jms4s.sqs.simpleQueueService
import jms4s.sqs.simpleQueueService.{Credentials, DirectAddress, HTTP}
import jms4s.{JmsClient, JmsProducer}
import org.typelevel.log4cats.Logger
import uk.gov.nationalarchives.omega.editorial.config.{HostBrokerEndpoint, UsernamePasswordCredentials}
import uk.gov.nationalarchives.omega.editorial.connectors.JmsRequestReplyClient.ReplyMessageHandler
import uk.gov.nationalarchives.omega.editorial.connectors.messages.uk.gov.nationalarchives.omega.editorial.connectors.messages.ReplyMessage
import uk.gov.nationalarchives.omega.editorial.connectors.messages.{MessageProperties, RequestMessage}

import java.util.concurrent.ConcurrentHashMap
import scala.annotation.unused
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success, Try}

/** A JMS Request-Reply client.
  *
  * @param requestMap
  *   \- a map of the requests sent and their reply handlers
  * @param producer
  *   \- the JmsProducer used to send the message
  * @tparam F
  */

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

  private val defaultConsumerConcurrencyLevel = 1
  private val defaultConsumerPollingInterval = 50.millis
  private val defaultProducerConcurrencyLevel = 1
  private val signingRegion = "elasticmq"
  private val initialCapacityOfMap = 16
  private val loadFactorForMap = 0.75f

  /** Create a JMS Request-Reply Client for use with Amazon Simple Queue Service.
    *
    * @param endpoint
    *   \- details for the SQS Host
    * @param credentials
    *   \- the credentials for connecting to the ActiveMQ broker.
    * @param customClientId
    *   \- an optional Custom client ID to identify this client.
    * @param replyQueue
    *   \- the queue that replies should be consumed from.
    *
    * @return
    *   \- The resource for the JMS Request-Reply Client.
    */
  def createForSqs[F[_] : Async : Logger](
    endpoint: HostBrokerEndpoint,
    credentials: UsernamePasswordCredentials,
    customClientId: Option[F[String]] = None
  )(replyQueue: String): Resource[F, JmsRequestReplyClient[F]] = {
    val clientIdResource: Resource[F, String] =
      Resource.liftK[F](customClientId.getOrElse(RandomClientIdGen.randomClientId[F]))
    val jmsClientResource: Resource[F, JmsClient[F]] = clientIdResource.flatMap { clientId =>
      simpleQueueService.makeJmsClient[F](
        simpleQueueService.Config(
          endpoint =
            simpleQueueService.Endpoint(Some(DirectAddress(HTTP, endpoint.host, Some(endpoint.port))), signingRegion),
          credentials = Some(Credentials(credentials.username, credentials.password)),
          clientId = simpleQueueService.ClientId(clientId),
          numberOfMessagesToPrefetch = None
        )
      )
    }
    create[F](jmsClientResource)(replyQueue)
  }

  /** Create a JMS Request-Reply Client.
    *
    * @param jmsClientResource
    *   \- a jms4s Client resource.
    * @param replyQueue
    *   \- the queue that replies should be consumed from.
    * @return
    *   The resource for the JMS Request-Reply Client.
    */
  private def create[F[_] : Async : Logger](
    jmsClientResource: Resource[F, JmsClient[F]]
  )(replyQueue: String): Resource[F, JmsRequestReplyClient[F]] =
    for {
      requestMap <- Resource.pure(
                      new ConcurrentHashMap[String, ReplyMessage => F[Unit]](
                        initialCapacityOfMap,
                        loadFactorForMap,
                        defaultProducerConcurrencyLevel
                      )
                    )
      jmsClient <- jmsClientResource
      consumer <- jmsClient.createAcknowledgerConsumer(
                    inputDestinationName = QueueName(replyQueue),
                    concurrencyLevel = defaultConsumerConcurrencyLevel,
                    pollingInterval = defaultConsumerPollingInterval
                  )
      consumerHandlerResource = consumer.handle(jmsConsumerHandler[F](requestMap)(_, _)).background
      producerResource = jmsClient.createProducer(concurrencyLevel = defaultProducerConcurrencyLevel)
      consumerProducer <- Resource.both(consumerHandlerResource, producerResource)
    } yield new JmsRequestReplyClient[F](requestMap, consumerProducer._2)

  /** A jms4s consumer handler that consumes a reply message, finds the ReplyMessageHandler and dispatches the message
    * to it.
    */
  private def jmsConsumerHandler[F[_]](
    requestMap: ConcurrentHashMap[String, ReplyMessageHandler[F]]
  )(jmsMessage: JmsMessage, @unused messageFactory: MessageFactory[F])(implicit
    F: Async[F],
    L: Logger[F]
  ): F[AckAction[F]] = {
    val maybeReplyMessageHandler: F[Option[ReplyMessageHandler[F]]] =
      F.delay(jmsMessage.getJMSCorrelationId.flatMap(correlationId => Option(requestMap.remove(correlationId))))

    val maybeHandled: F[Unit] = F.flatMap(maybeReplyMessageHandler) {
      case Some(correlatedRequestHandler) =>
        correlatedRequestHandler(unpackReply(jmsMessage))
      case None =>
        L.error(s"No request found for response '${jmsMessage.attemptAsText.get}'")
    }

    F.*>(maybeHandled)(F.pure(AckAction.ack[F]))
  }

  private def unpackReply(jmsMessage: JmsMessage): ReplyMessage =
    ReplyMessage(
      jmsMessage.attemptAsText.getOrElse(""),
      jmsMessage.getJMSCorrelationId,
      jmsMessage.getStringProperty(MessageProperties.OMGMessageTypeID)
    )

}


