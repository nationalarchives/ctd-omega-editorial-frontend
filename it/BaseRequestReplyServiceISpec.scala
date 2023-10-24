import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.{IO, Resource}
import org.scalatest.freespec.FixtureAsyncFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{BeforeAndAfterAll, FutureOutcome}
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jFactory
import support.TestStubData
import uk.gov.nationalarchives.omega.editorial.config.{AwsCredentialsAuthentication, Config, SqsJmsBrokerConfig, SqsJmsBrokerEndpointConfig, StubServerConfig}
import uk.gov.nationalarchives.omega.editorial.connectors.messages.{ReplyMessage, RequestMessage}
import uk.gov.nationalarchives.omega.editorial.connectors.{JmsRequestReplyClient, RequestReplyHandler}
import uk.gov.nationalarchives.omega.editorial.services.jms._

import scala.concurrent.duration.{FiniteDuration, SECONDS}

abstract class BaseRequestReplyServiceISpec
    extends FixtureAsyncFreeSpec with AsyncIOSpec with Matchers with BeforeAndAfterAll {

  override type FixtureParam = RequestReplyHandler

  def messageType: String

  implicit val logger: SelfAwareStructuredLogger[IO] = Slf4jFactory[IO].getLogger

  protected val stubData = new TestStubData
  private val requestQueueName = "STUB001_REQUEST001"
  private val replyQueueName = "PACE001_REPLY001"
  private val messagingServerHost = "localhost"
  private val messagingServerPort = 9324
  private val sqsJmsBrokerConfig = SqsJmsBrokerConfig("elasticmq", Some(SqsJmsBrokerEndpointConfig(false, Some(messagingServerHost), Some(messagingServerPort), Some(AwsCredentialsAuthentication("?", "?")))))
  private val stubServer = new StubServer(Config(SqsJmsBrokerConfig("elasticmq", None), Some(StubServerConfig(sqsJmsBrokerConfig)),requestQueueName), new ResponseBuilder(stubData))

  override def beforeAll(): Unit = {
    stubServer.start.unsafeToFuture()
    ()
  }

  override def withFixture(test: OneArgAsyncTest): FutureOutcome = {
    val clientResource: Resource[IO, JmsRequestReplyClient[IO]] = JmsRequestReplyClient.createForSqs[IO](
      sqsJmsBrokerConfig = sqsJmsBrokerConfig,
      customClientId = None
    )(replyQueueName)
    val (client, closer) = clientResource.allocated.unsafeRunSync()
    complete {
      super.withFixture(test.toNoArgAsyncTest(RequestReplyHandler(client)))
    } lastly {
      val _ = closer.unsafeRunTimed(FiniteDuration(1, SECONDS))
      ()
    }
  }

  def sendRequest(
    requestReplyHandler: RequestReplyHandler,
    message: String,
    applicationId: String,
    messageType: String
  ): IO[ReplyMessage] =
    requestReplyHandler.handle(requestQueueName, RequestMessage(message, applicationId, messageType))

}
