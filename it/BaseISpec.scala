import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.{ Assertion, BeforeAndAfterEach }
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.http.Status.{ OK, SEE_OTHER }
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.{ DefaultWSCookie, WSClient, WSCookie, WSResponse }
import play.api.test.Helpers.{ await, defaultAwaitTimeout }
import play.api.{ Application, inject }
import support.{ ApiConnectorAssertions, ModelSupport, TestReferenceDataService }
import uk.gov.nationalarchives.omega.editorial.connectors.ApiConnector
import uk.gov.nationalarchives.omega.editorial.models._
import uk.gov.nationalarchives.omega.editorial.services.ReferenceDataService
import uk.gov.nationalarchives.omega.editorial.support.TimeProvider

import java.time.{ LocalDateTime, Month }

abstract class BaseISpec
    extends PlaySpec with GuiceOneServerPerSuite with BeforeAndAfterEach with ModelSupport with ApiConnectorAssertions {

  implicit val monitoredApiConnector: MonitoredApiConnector = app.injector.instanceOf[MonitoredApiConnector]
  lazy implicit val testTimeProvider: TimeProvider = () => LocalDateTime.of(2023, Month.FEBRUARY, 28, 1, 1, 1)
  implicit val wsClient: WSClient = app.injector.instanceOf[WSClient]

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
  val testReferenceDataService: TestReferenceDataService = app.injector.instanceOf[TestReferenceDataService]
  val allCreators: Seq[Creator] = Seq(
    CorporateBody("RR6", "100th (Gordon Highlanders) Regiment of Foot", Some(1794), Some(1794)),
    CorporateBody("S34", "1st Regiment of Foot or Royal Scots", Some(1812), Some(1812)),
    CorporateBody("87K", "Abbotsbury Railway Company", Some(1877), Some(1877))
  ).flatMap(Creator.from) ++
    Seq(
      Person("3RX", "Abbot, Charles", Some("2nd Baron Colchester"), Some(1798), Some(1867)),
      Person("48N", "Baden-Powell, Lady Olave St Clair", None, Some(1889), Some(1977)),
      Person("39K", "Cannon, John Francis Michael", None, Some(1930), None)
    ).flatMap(Creator.from)

  private val playSessionCookieName = "PLAY_SESSION"

  override def fakeApplication(): Application =
    GuiceApplicationBuilder()
      .bindings(inject.bind[ReferenceDataService].to[TestReferenceDataService])
      .bindings(inject.bind[ApiConnector].to[MonitoredApiConnector])
      .overrides(inject.bind[TimeProvider].toInstance(testTimeProvider))
      .build()

  override def beforeEach(): Unit = {
    super.beforeEach()
    resetMessageBus()
    monitoredApiConnector.reset()
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
