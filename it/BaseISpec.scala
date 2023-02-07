import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.Assertion
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.http.Status.SEE_OTHER
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.{ WSClient, WSCookie, WSResponse }
import play.api.test.Helpers.{ await, defaultAwaitTimeout }

abstract class BaseISpec extends PlaySpec with GuiceOneServerPerSuite {

  implicit val wsClient: WSClient = app.injector.instanceOf[WSClient]

  val csrfTokenName = "csrfToken"

  private val playSessionCookieName = "PLAY_SESSION"

  override def fakeApplication(): Application =
    GuiceApplicationBuilder().configure(Map.empty[String, String]).build()

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
