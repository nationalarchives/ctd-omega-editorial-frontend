import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.{ IO, Resource }
import org.scalatest.freespec.FixtureAsyncFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{ BeforeAndAfterAll, FutureOutcome }
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jFactory
import support.TestStubData
import uk.gov.nationalarchives.omega.editorial.config.{ HostBrokerEndpoint, UsernamePasswordCredentials }
import uk.gov.nationalarchives.omega.editorial.connectors.messages.{ ReplyMessage, RequestMessage }
import uk.gov.nationalarchives.omega.editorial.connectors.{ JmsRequestReplyClient, RequestReplyHandler }
import uk.gov.nationalarchives.omega.editorial.services.jms._

import scala.concurrent.duration.{ FiniteDuration, SECONDS }

abstract class BaseRequestReplyServiceISpec
    extends FixtureAsyncFreeSpec with AsyncIOSpec with Matchers with BeforeAndAfterAll {

  override type FixtureParam = RequestReplyHandler

  def messageType: String

  implicit val logger: SelfAwareStructuredLogger[IO] = Slf4jFactory[IO].getLogger

  protected val stubData = new TestStubData
  private val requestQueueName = "PACS001-request"
  private val replyQueueName = "PACE001-reply"
  private val messagingServerHost = "localhost"
  private val messagingServerPort = 9324
  private val stubServer = new StubServer(new ResponseBuilder(stubData))

  override def beforeAll(): Unit = {
    stubServer.start.unsafeToFuture()
    ()
  }

  override def withFixture(test: OneArgAsyncTest): FutureOutcome = {
    val clientResource: Resource[IO, JmsRequestReplyClient[IO]] = JmsRequestReplyClient.createForSqs[IO](
      endpoint = HostBrokerEndpoint(messagingServerHost, messagingServerPort),
      credentials = UsernamePasswordCredentials("?", "?"),
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
