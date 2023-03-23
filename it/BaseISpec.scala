import cats.effect.unsafe.implicits.global
import com.dimafeng.testcontainers.GenericContainer
import com.dimafeng.testcontainers.GenericContainer.FileSystemBind
import com.dimafeng.testcontainers.scalatest.TestContainerForAll
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.{Assertion, BeforeAndAfterEach, Outcome, TestData}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.{GuiceOneServerPerSuite, GuiceOneServerPerTest}
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.wait.strategy.Wait
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.{DefaultWSCookie, WSClient, WSCookie, WSResponse}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import play.api.{Application, inject}
import support.{ApiConnectorAssertions, ModelSupport, TestReferenceDataService}
import uk.gov.nationalarchives.omega.editorial.config.{Config, HostBrokerEndpoint, UsernamePasswordCredentials}
import uk.gov.nationalarchives.omega.editorial.connectors.ApiConnector
import uk.gov.nationalarchives.omega.editorial.models.Creator
import uk.gov.nationalarchives.omega.editorial.services.ReferenceDataService
import uk.gov.nationalarchives.omega.editorial.services.jms.StubServer
import uk.gov.nationalarchives.omega.editorial.support.TimeProvider

import java.time.{LocalDateTime, Month}

abstract class BaseISpec
    extends PlaySpec with GuiceOneServerPerTest with BeforeAndAfterEach with ModelSupport with ApiConnectorAssertions with TestContainerForAll {

  implicit var monitoredApiConnector: MonitoredApiConnector = _
  lazy implicit val testTimeProvider: TimeProvider = () => LocalDateTime.of(2023, Month.FEBRUARY, 28, 1, 1, 1)
  implicit var wsClient: WSClient = _

  val idOfExistingEditSet = "1" // We only support one, for the moment.
  val ociOfExistingRecord: String = "COAL.2022.V1RJW.P"

  val csrfTokenName = "csrfToken"
  lazy val invalidSessionCookie: DefaultWSCookie = DefaultWSCookie(
    name = playSessionCookieName,
    value = "whatever",
    domain = None,
    path = None,
    maxAge = None,
    secure = false,
    httpOnly = false
  )
  var testReferenceDataService: TestReferenceDataService = _
  var allCreators: Seq[Creator] = _
  private val messagingServerHost = "localhost"
  private var messagingServerPort: Int = _
  private var stubServer: StubServer = _
  private val elasticMQContainerExportPort = 9324

  private val playSessionCookieName = "PLAY_SESSION"

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

  override def newAppForTest(testData: TestData): Application =  {

    val config = Config(HostBrokerEndpoint(messagingServerHost, messagingServerPort), UsernamePasswordCredentials("?","?"))

    val application = GuiceApplicationBuilder()
      .bindings(inject.bind[ReferenceDataService].to[TestReferenceDataService])
      .bindings(inject.bind[ApiConnector].to[MonitoredApiConnector])
      .overrides(inject.bind[TimeProvider].toInstance(testTimeProvider))
      .overrides(inject.bind[Config].toInstance(config))
      .build()

    monitoredApiConnector = application.injector.instanceOf[MonitoredApiConnector]
    wsClient = application.injector.instanceOf[WSClient]
    testReferenceDataService = application.injector.instanceOf[TestReferenceDataService]
    allCreators = testReferenceDataService.getCreators

    application
  }



//  override def fakeApplication(): Application =
//    GuiceApplicationBuilder()
//      .bindings(inject.bind[ReferenceDataService].to[TestReferenceDataService])
//      .bindings(inject.bind[ApiConnector].to[MonitoredApiConnector])
//      .overrides(inject.bind[TimeProvider].toInstance(testTimeProvider))
//      .build()

  override def beforeEach(): Unit = {
    super.beforeEach()
//    resetMessageBus()
//    monitoredApiConnector.reset()
  }

  def asDocument(response: WSResponse): Document = Jsoup.parse(response.body)

  def getCsrfToken(response: WSResponse): String =
    asDocument(response).select(s"input[type=hidden][name=$csrfTokenName]").attr("value")

  def getSessionCookie(response: WSResponse): WSCookie =
    response.cookie(playSessionCookieName).getOrElse(fail(s"No cookie found '$playSessionCookieName'"))

  def assertRedirection(response: WSResponse, expectedPath: String): Assertion = {
    response.status mustBe SEE_OTHER
    response.headers.get("Location") mustBe Some(Seq(expectedPath))
  }

  def loginForSessionCookie(): WSCookie = {
    val values = Map("username" -> "1234", "password" -> "1234")
    val getLoginPageResponse = getLoginPage()
    val responseForLoginPageSubmission = await(
      wsUrl("/login")
        .withFollowRedirects(false)
        .addCookies(getSessionCookie(getLoginPageResponse))
        .post(values ++ Map(csrfTokenName -> getCsrfToken(getLoginPageResponse)))
    )
    responseForLoginPageSubmission.status mustBe SEE_OTHER
    getSessionCookie(responseForLoginPageSubmission)
  }

  def getLoginPage(): WSResponse =
    await(
      wsUrl("/login")
        .withFollowRedirects(false)
        .get()
    )

  private def resetMessageBus(): Unit = {
    clearRequestQueue()
    clearResponseQueue()
    ()
  }

  private def clearRequestQueue(): Assertion = clearQueue("request-general").status mustBe OK

  private def clearResponseQueue(): Assertion =
    clearQueue("omega-editorial-web-application-instance-1").status mustBe OK

  private def clearQueue(name: String): WSResponse =
    await {
      wsClient.url(s"http://localhost:9324/$name?Action=PurgeQueue").get()
    }

}
