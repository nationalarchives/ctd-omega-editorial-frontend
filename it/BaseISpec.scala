import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.Assertion
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.http.Status.{ OK, SEE_OTHER }
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.{ WSClient, WSCookie, WSResponse }

abstract class BaseISpec extends PlaySpec with GuiceOneServerPerSuite {

  implicit val wsClient: WSClient = app.injector.instanceOf[WSClient]

  override def fakeApplication(): Application =
    GuiceApplicationBuilder().configure(Map.empty[String, String]).build()

  def asDocument(response: WSResponse): Document = Jsoup.parse(response.body)

  def getSecurityInfoFromForm(response: WSResponse): SecurityInfo = {
    response.status mustBe OK
    SecurityInfo(
      getSessionCookie(response),
      asDocument(response).select("input[type=hidden][name=csrfToken]").attr("value")
    )
  }

  def getSessionCookie(response: WSResponse): WSCookie =
    response.cookie("PLAY_SESSION").getOrElse(fail("No cookie found 'PLAY_SESSION'"))

  def assertRedirection(response: WSResponse, expectedPath: String): Assertion = {
    response.status mustBe SEE_OTHER
    response.headers.get("Location") mustBe Some(Seq(expectedPath))
  }

  case class SecurityInfo(sessionCookie: WSCookie, csrfToken: String)
}
