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
import jms4s.config.QueueName
import jms4s.jms.JmsMessage
import jms4s.jms.MessageFactory
import jms4s.JmsAcknowledgerConsumer.AckAction
import jms4s.JmsClient
import jms4s.sqs.simpleQueueService
import jms4s.sqs.simpleQueueService.{ ClientId, DirectAddress, Endpoint, HTTP, HTTPS }
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import uk.gov.nationalarchives.omega.editorial.config.{ Config, SqsJmsBrokerConfig, StubServerConfig }
import uk.gov.nationalarchives.omega.editorial.connectors.messages.MessageProperties

import scala.concurrent.duration.DurationInt
import javax.inject.{ Inject, Singleton }

@Singleton
class StubServer @Inject() (config: Config, responseBuilder: ResponseBuilder) {

  private val stubServerConfig = config.stubServer.getOrElse(StubServerConfig(config.sqsJmsBroker))

  private implicit val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]

  private val applicationId = "STUB001"
  private val requestQueueName = QueueName(s"${applicationId}_REQUEST001")
  private val replyQueueName = QueueName("PACE001_REPLY001")
  private val consumerConcurrencyLevel = 1
  private val pollingInterval = 50.millis

  private val jmsClient: Resource[IO, JmsClient[IO]] = {
    val maybeEndpointConfig = getEndpointConfigForSqs(stubServerConfig.sqsJmsBroker)

    simpleQueueService.makeJmsClient[IO](
      simpleQueueService.Config(
        stubServerConfig.sqsJmsBroker.awsRegion,
        endpoint = maybeEndpointConfig,
        clientId = ClientId("stub_server_1"),
        None
      )
    )
  }

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
               AckAction.send(message, replyQueueName)
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
      replyMessage     <- messageFactory.makeTextMessage(responseText)
      _ = replyMessage.setJMSCorrelationId(requestMessageId)
      _ = replyMessage.setStringProperty(MessageProperties.OMGApplicationID, applicationId)
      _ = replyMessage.setStringProperty(
            MessageProperties.OMGMessageTypeID,
            getReplyMessageType(jmsMessage.getStringProperty(MessageProperties.OMGMessageTypeID))
          )
      _ = replyMessage.setStringProperty(MessageProperties.OMGMessageFormat, "application/json")
      _ = replyMessage.setStringProperty(MessageProperties.OMGReplyAddress, replyQueueName.value)
      _ = replyMessage.setStringProperty(MessageProperties.OMGToken, "AbCdEf123456")
    } yield replyMessage

  private val messageTypeMap = Map[String, String]("OSLISALS001" -> "ODLISALS001", "OSLISAGT001" -> "ODLISAGT001")
  private def getReplyMessageType(maybeMessageType: Option[String]): String = {
    val messageType = for {
      requestMessageType <- maybeMessageType
      replyMessageType   <- messageTypeMap.get(requestMessageType)
    } yield replyMessageType
    messageType.getOrElse("NOT FOUND")
  }

  private def getEndpointConfigForSqs(sqsJmsBrokerConfig: SqsJmsBrokerConfig) =
    sqsJmsBrokerConfig.endpoint.flatMap { sqsJmsBrokerEndpoint =>
      val protocol = sqsJmsBrokerEndpoint.tls match {
        case true  => HTTPS
        case false => HTTP
      }
      val maybeDirectAddress: Option[DirectAddress] =
        sqsJmsBrokerEndpoint.host.map(host => DirectAddress(protocol, host, sqsJmsBrokerEndpoint.port))
      val maybeCredentials: Option[simpleQueueService.Credentials] =
        sqsJmsBrokerEndpoint.authentication.map(awsCredentialsAuthentication =>
          simpleQueueService.Credentials(awsCredentialsAuthentication.accessKey, awsCredentialsAuthentication.secretKey)
        )
      if (maybeDirectAddress.nonEmpty || maybeCredentials.nonEmpty) {
        Some(Endpoint(maybeDirectAddress, maybeCredentials))
      } else {
        None
      }
    }
}
