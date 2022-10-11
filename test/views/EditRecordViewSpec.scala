package views

import org.scalatestplus.play.PlaySpec
import play.api.data.{ Form, FormError }
import play.api.data.Forms.{ mapping, text }
import play.api.i18n.Lang
import play.api.test.{ CSRFTokenHelper, FakeRequest, Helpers }
import play.api.test.Helpers.{ contentAsString, defaultAwaitTimeout, stubMessagesApi }
import play.test.WithApplication
import play.twirl.api.Html
import uk.gov.nationalarchives.omega.editorial.models.{ Credentials, EditSetRecord }
import uk.gov.nationalarchives.omega.editorial.views

class EditRecordViewSpec extends PlaySpec {

  "Edit record Html" should {
    "render the given title and heading" in new WithApplication {
      val messages: Map[String, Map[String, String]] = Map.empty
      implicit val messagesApi = stubMessagesApi(messages)
      val title = "EditRecordTitleTest"
      val heading = "EditRecordHeadingTest"
      val editSetRecordForm: Form[EditSetRecord] = Form(
        mapping(
          "ccr"                       -> text,
          "oci"                       -> text,
          "scopeAndContent"           -> text,
          "coveringDates"             -> text,
          "formerReferenceDepartment" -> text,
          "startDate"                 -> text,
          "endDate"                   -> text
        )(EditSetRecord.apply)(EditSetRecord.unapply)
      )

      val editRecordHtml: Html =
        views.html.editSetRecordEdit(title, heading, editSetRecordForm)(
          Helpers.stubMessages(),
          CSRFTokenHelper.addCSRFToken(FakeRequest())
        )

      contentAsString(editRecordHtml) must include(title)
      contentAsString(editRecordHtml) must include(heading)
    }

    "render an error given no scope and content" in new WithApplication {
      val messages: Map[String, Map[String, String]] = Map.empty
      implicit val messagesApi = stubMessagesApi(messages)
      val title = "EditRecordTitleTest"
      val heading = "EditRecordHeadingTest"
      val editSetRecordForm: Form[EditSetRecord] = Form(
        mapping(
          "ccr"                       -> text,
          "oci"                       -> text,
          "scopeAndContent"           -> text,
          "coveringDates"             -> text,
          "formerReferenceDepartment" -> text,
          "startDate"                 -> text,
          "endDate"                   -> text
        )(EditSetRecord.apply)(EditSetRecord.unapply)
      ).fill(EditSetRecord.apply("", "", "", "", "", "", ""))
        .withError(FormError("", "Enter the scope and content."))

      val editRecordHtml: Html =
        views.html.editSetRecordEdit(title, heading, editSetRecordForm)(
          Helpers.stubMessages(),
          CSRFTokenHelper.addCSRFToken(FakeRequest())
        )

      contentAsString(editRecordHtml) must include("There is a problem")
      contentAsString(editRecordHtml) must include("Enter the scope and content.")

    }

    "render an error when given scope and content is more than 8000 characters" in new WithApplication {
      val messages: Map[String, Map[String, String]] = Map.empty
      implicit val messagesApi = stubMessagesApi(messages)
      val title = "EditRecordTitleTest"
      val heading = "EditRecordHeadingTest"
      val editSetRecordForm: Form[EditSetRecord] = Form(
        mapping(
          "ccr"                       -> text,
          "oci"                       -> text,
          "scopeAndContent"           -> text,
          "coveringDates"             -> text,
          "formerReferenceDepartment" -> text,
          "startDate"                 -> text,
          "endDate"                   -> text
        )(EditSetRecord.apply)(EditSetRecord.unapply)
      ).fill(
        EditSetRecord.apply(
          "",
          "",
          "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
          "",
          "",
          "",
          ""
        )
      ).withError(FormError("", "Scope and content too long, maximum length 8000 characters"))

      val editRecordHtml: Html =
        views.html.editSetRecordEdit(title, heading, editSetRecordForm)(
          Helpers.stubMessages(),
          CSRFTokenHelper.addCSRFToken(FakeRequest())
        )

      contentAsString(editRecordHtml) must include("There is a problem")
      contentAsString(editRecordHtml) must include("Scope and content too long, maximum length 8000 characters")

    }

    "render an error when given former reference department is more than 255 characters" in new WithApplication {
      val messages: Map[String, Map[String, String]] = Map.empty
      implicit val messagesApi = stubMessagesApi(messages)
      val title = "EditRecordTitleTest"
      val heading = "EditRecordHeadingTest"
      val editSetRecordForm: Form[EditSetRecord] = Form(
        mapping(
          "ccr"                       -> text,
          "oci"                       -> text,
          "scopeAndContent"           -> text,
          "coveringDates"             -> text,
          "formerReferenceDepartment" -> text,
          "startDate"                 -> text,
          "endDate"                   -> text
        )(EditSetRecord.apply)(EditSetRecord.unapply)
      ).fill(
        EditSetRecord.apply(
          "",
          "",
          "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
          "Former reference - Department Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing",
          "",
          "",
          ""
        )
      ).withError(FormError("", "Former reference - Department too long, maximum length 255 characters"))

      val editRecordHtml: Html =
        views.html.editSetRecordEdit(title, heading, editSetRecordForm)(
          Helpers.stubMessages(),
          CSRFTokenHelper.addCSRFToken(FakeRequest())
        )

      contentAsString(editRecordHtml) must include("There is a problem")
      contentAsString(editRecordHtml) must include(
        "Former reference - Department too long, maximum length 255 characters"
      )

    }

  }
}
