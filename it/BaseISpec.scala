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
import support._
import uk.gov.nationalarchives.omega.editorial.models._
import uk.gov.nationalarchives.omega.editorial.services.MessagingService
import uk.gov.nationalarchives.omega.editorial.services.jms._
import uk.gov.nationalarchives.omega.editorial.support.TimeProvider

import java.time.{ LocalDateTime, Month }

abstract class BaseISpec
    extends PlaySpec with GuiceOneServerPerSuite with BeforeAndAfterEach with ModelSupport
    with MessagingServiceAssertions {

  implicit val monitoredMessagingService: MonitoredMessagingService = app.injector.instanceOf[MonitoredMessagingService]
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
  private val stubData = app.injector.instanceOf[StubDataImpl]
  val allCreators: Seq[Creator] =
    stubData.getPersons().flatMap(Creator.from) ++ stubData.getCorporateBodies().flatMap(Creator.from)

  private val playSessionCookieName = "PLAY_SESSION"

  override def fakeApplication(): Application =
    GuiceApplicationBuilder()
      .bindings(inject.bind[MessagingService].to[MonitoredMessagingService])
      .bindings(inject.bind[StubData].to[TestStubData])
      .overrides(inject.bind[TimeProvider].toInstance(testTimeProvider))
      .build()

  override def beforeEach(): Unit = {
    super.beforeEach()
    resetMessageBus()
    monitoredMessagingService.reset()
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
    val getLoginPageResponse = getLoginPage
    val responseForLoginPageSubmission = await(
      wsUrl("/login")
        .withFollowRedirects(false)
        .addCookies(getSessionCookie(getLoginPageResponse))
        .post(values ++ Map(csrfTokenName -> getCsrfToken(getLoginPageResponse)))
    )
    responseForLoginPageSubmission.status mustBe SEE_OTHER
    getSessionCookie(responseForLoginPageSubmission)
  }

  def getLoginPage: WSResponse =
    await(
      wsUrl("/login")
        .withFollowRedirects(false)
        .get()
    )

  private def resetMessageBus(): Unit = {
    clearRequestQueue()
    clearReplyQueue()
    ()
  }

  private def clearRequestQueue(): Assertion = clearQueue("PACS001-request").status mustBe OK

  private def clearReplyQueue(): Assertion =
    clearQueue("PACE001-reply").status mustBe OK

  private def clearQueue(name: String): WSResponse =
    await {
      wsClient.url(s"http://localhost:9324/$name?Action=PurgeQueue").get()
    }

}
