package views

import org.scalatestplus.play.PlaySpec
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.test.{ CSRFTokenHelper, FakeRequest, Helpers }
import play.api.test.Helpers.{ contentAsString, defaultAwaitTimeout, stubMessagesApi }
import play.test.WithApplication
import play.twirl.api.Html
import uk.gov.nationalarchives.omega.editorial.models.EditSetRecord
import uk.gov.nationalarchives.omega.editorial.views

class EditSetRecordEditSaveSpec extends PlaySpec {

  "Edit set record edit save Html" should {
    "render the given title and heading with save changes message" in new WithApplication {
      val messages: Map[String, Map[String, String]] = Map.empty
      implicit val messagesApi = stubMessagesApi(messages)
      val title = "EditRecordTitleTest"
      val heading = "EditRecordHeadingTest"
      val saveChanges = "Your changes have been saved."

      val confirmationEditSetRecordEditHtml: Html =
        views.html.editSetRecordEditSave(title, heading, saveChanges)

      contentAsString(confirmationEditSetRecordEditHtml) must include(title)
      contentAsString(confirmationEditSetRecordEditHtml) must include(heading)
      contentAsString(confirmationEditSetRecordEditHtml) must include(saveChanges)
    }

  }

}
