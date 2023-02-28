import cats.effect.{ IO, Resource }
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.{ BeforeAndAfterAll, FutureOutcome }
import org.scalatest.freespec.FixtureAsyncFreeSpec
import org.scalatest.matchers.must.Matchers
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jFactory
import uk.gov.nationalarchives.omega.editorial.connectors.{ HostBrokerEndpoint, JmsRequestReplyClient, RequestMessage, RequestReplyHandler, UsernamePasswordCredentials }
import uk.gov.nationalarchives.omega.editorial.services.jms.StubServer

import scala.concurrent.duration.{ FiniteDuration, SECONDS }

abstract class BaseRequestReplyServiceISpec
    extends FixtureAsyncFreeSpec with AsyncIOSpec with Matchers with BeforeAndAfterAll {

  override type FixtureParam = RequestReplyHandler

  def serviceId: String

  implicit val logger: SelfAwareStructuredLogger[IO] = Slf4jFactory[IO].getLogger

  private val requestQueueName = "request-general"
  private val replyQueueName = "omega-editorial-web-application-instance-1"
  private val messagingServerHost = "localhost"
  private val messagingServerPort = 9324

  private val stubServer = new StubServer

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

  def sendRequest(requestReplyHandler: RequestReplyHandler, message: String): IO[String] =
    requestReplyHandler.handle(requestQueueName, RequestMessage(message, serviceId))

}
