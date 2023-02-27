import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.{IO, Resource}
import com.dimafeng.testcontainers.GenericContainer
import com.dimafeng.testcontainers.GenericContainer.FileSystemBind
import com.dimafeng.testcontainers.scalatest.{TestContainerForAll, TestContainersForAll}
import org.scalatest.freespec.FixtureAsyncFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{BeforeAndAfterAll, FutureOutcome}
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.wait.strategy.Wait
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jFactory
import uk.gov.nationalarchives.omega.editorial.config.{HostBrokerEndpoint, UsernamePasswordCredentials}
import uk.gov.nationalarchives.omega.editorial.connectors.{JmsRequestReplyClient, RequestMessage, RequestReplyHandler}
import uk.gov.nationalarchives.omega.editorial.services.jms.{StubData, StubServer}

import scala.concurrent.duration.{FiniteDuration, SECONDS}

abstract class BaseRequestReplyServiceISpec
    extends FixtureAsyncFreeSpec with AsyncIOSpec with Matchers with BeforeAndAfterAll with StubData with TestContainerForAll {

  override type FixtureParam = RequestReplyHandler

  def serviceId: String

  implicit val logger: SelfAwareStructuredLogger[IO] = Slf4jFactory[IO].getLogger

  private val requestQueueName = "request-general"
  private val replyQueueName = "omega-editorial-web-application-instance-1"
  private val messagingServerHost = "localhost"
  private var messagingServerPort: Int = _
  private var stubServer: StubServer = _
  private val elasticMQContainerExportPort = 9324

  override def beforeAll(): Unit = {
    stubServer.start.unsafeToFuture()
    ()
  }

  override val containerDef: GenericContainer.Def[GenericContainer] = GenericContainer.Def(
    dockerImage = "softwaremill/elasticmq-native:latest",
    exposedPorts = Seq(elasticMQContainerExportPort),
    fileSystemBind = Seq(
      FileSystemBind("./elasticmq.conf", "/opt/elasticmq.conf", BindMode.READ_ONLY)
    ),
    waitStrategy = Wait.forListeningPort
  )

  override def afterContainersStart(container: Containers): Unit =
    container match {
      case genericContainer: GenericContainer =>
        messagingServerPort = genericContainer.mappedPort(elasticMQContainerExportPort)
        stubServer = new StubServer(messagingServerHost, messagingServerPort)
        stubServer.start.unsafeToFuture()
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
