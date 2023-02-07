import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.{ Assertion, BeforeAndAfterEach }
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.{ Application, inject }
import play.api.http.Status.SEE_OTHER
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.{ DefaultWSCookie, WSClient, WSCookie, WSResponse }
import play.api.test.Helpers.{ await, defaultAwaitTimeout }
import support.TestReferenceDataService
import uk.gov.nationalarchives.omega.editorial.models.Creator
import uk.gov.nationalarchives.omega.editorial.services.ReferenceDataService

abstract class BaseISpec extends PlaySpec with GuiceOneServerPerSuite with BeforeAndAfterEach {

  implicit val wsClient: WSClient = app.injector.instanceOf[WSClient]

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
  val allCreators: Seq[Creator] = testReferenceDataService.getCreators

  private val playSessionCookieName = "PLAY_SESSION"

  override def fakeApplication(): Application =
    GuiceApplicationBuilder()
      .bindings(inject.bind[ReferenceDataService].to[TestReferenceDataService])
      .build()

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

}
