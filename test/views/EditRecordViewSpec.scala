/*
 * Copyright (c) 2022 The National Archives
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package views

import org.jsoup.nodes.Document
import play.api.data.Forms.{ mapping, text }
import play.api.data.{ Form, FormError }
import play.api.test.Helpers.{ contentAsString, defaultAwaitTimeout }
import play.api.test.{ CSRFTokenHelper, FakeRequest, Helpers }
import play.twirl.api.Html
import support.BaseSpec
import support.CustomMatchers.{ haveHeaderTitle, haveSignOutLink, haveVisibleSignOutLink }
import uk.gov.nationalarchives.omega.editorial.models.EditSetRecord
import uk.gov.nationalarchives.omega.editorial.views.html.editSetRecordEdit

class EditRecordViewSpec extends BaseSpec {

  "Edit record Html" should {
    "render the given title and heading" in {
      val editSetRecordEditInstance = inject[editSetRecordEdit]
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

      val editSetData = new EditSetRecord(
        "TestCCR",
        "TestOCI",
        "TestScopeAndContent",
        "TestCoveringDates",
        "TestFormerReferenceDepartment",
        "TestStartDate",
        "TestEndDate"
      )

      val editRecordHtml: Html =
        editSetRecordEditInstance(user, title, heading, editSetRecordForm.fill(editSetData))(
          Helpers.stubMessages(),
          CSRFTokenHelper.addCSRFToken(FakeRequest())
        )

      contentAsString(editRecordHtml) must include(title)
      contentAsString(editRecordHtml) must include(heading)
      contentAsString(editRecordHtml) must include(editSetData.ccr)
      contentAsString(editRecordHtml) must include(editSetData.oci)
      contentAsString(editRecordHtml) must include(editSetData.scopeAndContent)
      contentAsString(editRecordHtml) must include(editSetData.coveringDates)
      contentAsString(editRecordHtml) must include(editSetData.formerReferenceDepartment)
      contentAsString(editRecordHtml) must include(editSetData.startDate)
      contentAsString(editRecordHtml) must include(editSetData.endDate)

    }

    "render the header" in {

      val document = generateDocument()

      document must haveHeaderTitle
      document must haveVisibleSignOutLink
      document must haveSignOutLink

    }

    "render an error given no scope and content" in {
      val editSetRecordEditInstance = inject[editSetRecordEdit]
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
        editSetRecordEditInstance(user, title, heading, editSetRecordForm)(
          Helpers.stubMessages(),
          CSRFTokenHelper.addCSRFToken(FakeRequest())
        )

      contentAsString(editRecordHtml) must include("There is a problem")
      contentAsString(editRecordHtml) must include("Enter the scope and content.")

    }

    "render an error when given scope and content is more than 8000 characters" in {
      val editSetRecordEditInstance = inject[editSetRecordEdit]
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
        editSetRecordEditInstance(user, title, heading, editSetRecordForm)(
          Helpers.stubMessages(),
          CSRFTokenHelper.addCSRFToken(FakeRequest())
        )

      contentAsString(editRecordHtml) must include("There is a problem")
      contentAsString(editRecordHtml) must include("Scope and content too long, maximum length 8000 characters")

    }

    "render an error when given former reference department is more than 255 characters" in {
      val editSetRecordEditInstance = inject[editSetRecordEdit]

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
        editSetRecordEditInstance(user, title, heading, editSetRecordForm)(
          Helpers.stubMessages(),
          CSRFTokenHelper.addCSRFToken(FakeRequest())
        )

      contentAsString(editRecordHtml) must include("There is a problem")
      contentAsString(editRecordHtml) must include(
        "Former reference - Department too long, maximum length 255 characters"
      )

    }

  }

  private def generateDocument(): Document = {
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

    val editSetRecordEditInstance: editSetRecordEdit = inject[editSetRecordEdit]
    asDocument(
      editSetRecordEditInstance(
        user,
        "EditRecordTitleTest",
        "EditRecordHeadingTest",
        editSetRecordForm.fill(
          new EditSetRecord(
            "TestCCR",
            "TestOCI",
            "TestScopeAndContent",
            "TestCoveringDates",
            "TestFormerReferenceDepartment",
            "TestStartDate",
            "TestEndDate"
          )
        )
      )(
        Helpers.stubMessages(),
        CSRFTokenHelper.addCSRFToken(FakeRequest())
      )
    )

  }
}
