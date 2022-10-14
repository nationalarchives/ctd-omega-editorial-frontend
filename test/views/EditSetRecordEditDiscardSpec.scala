package views

import org.scalatestplus.play.PlaySpec
import play.api.test.Helpers.{ contentAsString, defaultAwaitTimeout, stubMessagesApi }
import play.test.WithApplication
import play.twirl.api.Html
import uk.gov.nationalarchives.omega.editorial.views

class EditSetRecordEditDiscardSpec extends PlaySpec {

  "Edit set record edit discard Html" should {

    "render the given title and heading with discard changes message" in new WithApplication {
      val messages: Map[String, Map[String, String]] = Map.empty
      implicit val messagesApi = stubMessagesApi(messages)
      val title = "EditRecordTitleTest"
      val heading = "EditRecordHeadingTest"
      val discardChanges = "Any changes have been discarded. Showing last saved version."

      val confirmationEditSetRecordEditHtml: Html =
        views.html.editSetRecordEditDiscard(title, heading, discardChanges)

      contentAsString(confirmationEditSetRecordEditHtml) must include(title)
      contentAsString(confirmationEditSetRecordEditHtml) must include(heading)
      contentAsString(confirmationEditSetRecordEditHtml) must include(discardChanges)
    }
  }

}
