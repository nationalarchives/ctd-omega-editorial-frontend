package views

import org.scalatestplus.play.PlaySpec
import play.api.test.Helpers.{ contentAsString, defaultAwaitTimeout }
import play.test.WithApplication
import play.twirl.api.Html
import uk.gov.nationalarchives.omega.editorial._

class LoginViewSpec extends PlaySpec {

  "Login Html" should {
    "render the given title and heading" in new WithApplication {
      val title = "TitleTest"
      val heading = "HeadingTest"

      val loginHtml: Html = views.html.login(title, heading)

      contentAsString(loginHtml) must include(title)
      contentAsString(loginHtml) must include(heading)
    }

  }
}
