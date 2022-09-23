package views

import org.scalatestplus.play.PlaySpec
import play.api.test.Helpers.{ contentAsString, defaultAwaitTimeout }
import play.test.WithApplication
import play.twirl.api.Html
import uk.gov.nationalarchives.omega.editorial._

class EditSetViewSpec extends PlaySpec {

  "Edit set Html" should {
    "render the given title and heading" in new WithApplication {
      val title = "EditSetTitleTest"
      val heading = "EditSetHeadingTest"

      val editSetHtml: Html = views.html.editSet(title, heading)

      contentAsString(editSetHtml) must include(title)
      contentAsString(editSetHtml) must include(heading)
    }

  }
}
