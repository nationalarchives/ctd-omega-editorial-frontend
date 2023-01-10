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
import play.api.test.{ CSRFTokenHelper, FakeRequest, Helpers }
import play.twirl.api.Html
import support.BaseSpec
import support.CustomMatchers._
import uk.gov.nationalarchives.omega.editorial.models.{ EditSetRecord, LegalStatus, RelatedMaterial }
import uk.gov.nationalarchives.omega.editorial.forms.EditSetRecordFormValues
import uk.gov.nationalarchives.omega.editorial.views.html.editSetRecordEdit

class EditRecordViewSpec extends BaseSpec {

  private val emptyForm: Form[EditSetRecordFormValues] = Form(
    mapping(
      "scopeAndContent"           -> text,
      "coveringDates"             -> text,
      "formerReferenceDepartment" -> text,
      "startDateDay"              -> text,
      "startDateMonth"            -> text,
      "startDateYear"             -> text,
      "endDateDay"                -> text,
      "endDateMonth"              -> text,
      "endDateYear"               -> text,
      "legalStatus"               -> text,
      "placeOfDeposit"            -> text,
      "note"                      -> text
    )(EditSetRecordFormValues.apply)(EditSetRecordFormValues.unapply)
  )

  val legalStatusReferenceData =
    Seq(
      LegalStatus("ref.1", "Public Record(s)"),
      LegalStatus("ref.2", "Not Public Records"),
      LegalStatus("ref.3", "Public Records unless otherwise Stated"),
      LegalStatus("ref.4", "Welsh Public Record(s)")
    )

  val editSetRecord = EditSetRecord(
    ccr = "COAL 80/80/1",
    oci = "COAL.2022.V1RJW.P",
    scopeAndContent = "",
    coveringDates = "",
    formerReferenceDepartment = "",
    startDateDay = "",
    startDateMonth = "",
    startDateYear = "",
    endDateDay = "",
    endDateMonth = "",
    endDateYear = "",
    legalStatus = "",
    placeOfDeposit = "",
    note = "",
    relatedMaterial = Seq(
      RelatedMaterial.DescriptionOnly(
        description = "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths."
      ),
      RelatedMaterial.LinkAndDescription(
        linkHref = "#;",
        linkText = "COAL 80/80/2",
        description = "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths."
      ),
      RelatedMaterial.LinkOnly(linkHref = "#;", linkText = "COAL 80/80/3")
    )
  )

  val emptyRecordValues: EditSetRecordFormValues = EditSetRecordFormValues.apply(
    scopeAndContent = "",
    coveringDates = "",
    formerReferenceDepartment = "",
    startDateDay = "",
    startDateMonth = "",
    startDateYear = "",
    endDateDay = "",
    endDateMonth = "",
    endDateYear = "",
    placeOfDeposit = "",
    legalStatus = "",
    note = ""
  )

  "Edit record Html" should {
    "render when all is valid" in {

      val document = generateDocument(
        title = "TNA reference: COAL 80/80/1",
        form = emptyForm.fill(
          emptyRecordValues.copy(
            scopeAndContent = "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
            coveringDates = "1960",
            formerReferenceDepartment = "TestFormerReferenceDepartment",
            startDateDay = "1",
            startDateMonth = "2",
            startDateYear = "1960",
            endDateDay = "31",
            endDateMonth = "12",
            endDateYear = "1960",
            legalStatus = "ref.1",
            placeOfDeposit = "3"
          )
        )
      )

      document must haveTitle("TNA reference: COAL 80/80/1")
      document must haveHeading("edit-set.record.edit.heading")
      document must haveClassicCatalogueRef("COAL 80/80/1")
      document must haveOmegaCatalogueId("COAL.2022.V1RJW.P")
      document must haveScopeAndContent(
        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths."
      )
      document must haveCoveringDates("1960")
      document must haveFormerReferenceDepartment("TestFormerReferenceDepartment")
      document must haveStartDateDay("1")
      document must haveStartDateMonth("2")
      document must haveStartDateYear("1960")
      document must haveEndDateDay("31")
      document must haveEndDateMonth("12")
      document must haveEndDateYear("1960")
      document must haveHeaderTitle("header.title")
      document must haveVisibleLogoutLink
      document must haveLogoutLinkLabel("header.logout")
      document must haveLogoutLink
      document must haveActionButtons("save", "edit-set.record.save.button", 2)
      document must haveActionButtons("discard", "edit-set.record.discard.button", 2)
      document must haveLegend("edit-set.record.edit.legend")
      document must haveLegalStatus("ref.1")

    }
    "render an error given no scope and content" in {
      val editSetRecordEditInstance = inject[editSetRecordEdit]
      val title = "EditRecordTitleTest"
      val editSetName = "COAL 80 Sample"
      val filledForm = emptyForm
        .fill(emptyRecordValues)
        .withError(FormError("scopeAndContent", "Enter the scope and content."))

      val editRecordHtml: Html =
        editSetRecordEditInstance(
          user,
          editSetName,
          title,
          editSetRecord,
          legalStatusReferenceData,
          allCorporateBodies,
          filledForm
        )(
          Helpers.stubMessages(),
          CSRFTokenHelper.addCSRFToken(FakeRequest())
        )

      val document = asDocument(editRecordHtml)
      document must haveTitle("EditRecordTitleTest")
      document must haveSummaryErrorTitle("error.summary.title")
      document must haveSummaryErrorMessages("Enter the scope and content.")
      document must haveErrorMessageForScopeAndContent("Enter the scope and content.")
      document must haveScopeAndContent("")
    }

    "render an error when given scope and content is more than 8000 characters" in {
      val editSetRecordEditInstance = inject[editSetRecordEdit]
      val title = "EditRecordTitleTest"
      val editSetName = "COAL 80 Sample"
      val filledForm = emptyForm
        .fill(
          emptyRecordValues.copy(
            scopeAndContent = "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths."
          )
        )
        .withError(FormError("scopeAndContent", "Scope and content too long, maximum length 8000 characters"))

      val editRecordHtml: Html =
        editSetRecordEditInstance(
          user,
          editSetName,
          title,
          editSetRecord,
          legalStatusReferenceData,
          allCorporateBodies,
          filledForm
        )(
          Helpers.stubMessages(),
          CSRFTokenHelper.addCSRFToken(FakeRequest())
        )

      val document = asDocument(editRecordHtml)
      document must haveTitle("EditRecordTitleTest")
      document must haveSummaryErrorTitle("error.summary.title")
      document must haveSummaryErrorMessages("Scope and content too long, maximum length 8000 characters")
      document must haveErrorMessageForScopeAndContent("Scope and content too long, maximum length 8000 characters")
      document must haveScopeAndContent(
        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths."
      )
    }

    "render an error when given former reference department is more than 255 characters" in {
      val editSetRecordEditInstance = inject[editSetRecordEdit]
      val title = "EditRecordTitleTest"
      val editSetName = "COAL 80 Sample"
      val filledForm = emptyForm
        .fill(
          emptyRecordValues.copy(
            scopeAndContent = "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
            formerReferenceDepartment =
              "Former reference - Department Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing"
          )
        )
        .withError(
          FormError(
            "formerReferenceDepartment",
            "Former reference - Department too long, maximum length 255 characters"
          )
        )

      val editRecordHtml: Html =
        editSetRecordEditInstance(
          user,
          editSetName,
          title,
          editSetRecord,
          legalStatusReferenceData,
          allCorporateBodies,
          filledForm
        )(
          Helpers.stubMessages(),
          CSRFTokenHelper.addCSRFToken(FakeRequest())
        )

      val document = asDocument(editRecordHtml)
      document must haveTitle("EditRecordTitleTest")
      document must haveSummaryErrorTitle("error.summary.title")
      document must haveSummaryErrorMessages("Former reference - Department too long, maximum length 255 characters")
      document must haveErrorMessageForFormerReferenceDepartment(
        "Former reference - Department too long, maximum length 255 characters"
      )
      document must haveFormerReferenceDepartment(
        "Former reference - Department Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing"
      )
    }
    "render an error when no legal status is selected" in {
      val editSetRecordEditInstance = inject[editSetRecordEdit]

      val title = "EditRecordTitleTest"
      val editSetName = "COAL 80 Sample"
      val filledForm = emptyForm
        .fill(emptyRecordValues)
        .withError(FormError("", "Select a valid legal status"))

      val editRecordHtml: Html =
        editSetRecordEditInstance(
          user,
          editSetName,
          title,
          editSetRecord,
          legalStatusReferenceData,
          allCorporateBodies,
          filledForm
        )(
          Helpers.stubMessages(),
          CSRFTokenHelper.addCSRFToken(FakeRequest())
        )

      val document = asDocument(editRecordHtml)
      document must haveSummaryErrorTitle("error.summary.title")
      document must haveSummaryErrorMessages("Select a valid legal status")
    }

    "render an error when given invalid covering dates" in {
      val editSetRecordEditInstance = inject[editSetRecordEdit]

      val title = "EditRecordTitleTest"
      val editSetName = "COAL 80 Sample"
      val inputData = EditSetRecordFormValues(
        scopeAndContent = "",
        coveringDates = "Mon 1 Oct 1330",
        formerReferenceDepartment = "",
        startDateDay = "1",
        startDateMonth = "2",
        startDateYear = "1960",
        endDateDay = "31",
        endDateMonth = "12",
        endDateYear = "1960",
        legalStatus = "ref.1",
        placeOfDeposit = "2",
        note = ""
      )

      val editSetRecordForm = emptyForm
        .fill(inputData)
        .withError("coveringDates", "covering date message string")

      val editRecordHtml =
        editSetRecordEditInstance(
          user,
          editSetName,
          title,
          editSetRecord,
          legalStatusReferenceData,
          allCorporateBodies,
          editSetRecordForm
        )(
          Helpers.stubMessages(),
          CSRFTokenHelper.addCSRFToken(FakeRequest())
        )

      val document = asDocument(editRecordHtml)
      document must haveSummaryErrorMessages("covering date message string")
    }
  }

  "render expecting errors when" when {
    "only the start date is invalid" in {

      val document = generateDocument(
        form = emptyForm
          .fill(emptyRecordValues)
          .withError(FormError("startDate", "Start date is not a valid date"))
      )

      document must haveSummaryErrorMessages("Start date is not a valid date")
      document must haveErrorMessageForStartDate("Start date is not a valid date")
      document must haveNoErrorMessageForEndDate

    }
    "only the end date is invalid" in {

      val document = generateDocument(
        form = emptyForm
          .fill(emptyRecordValues)
          .withError(FormError("endDate", "End date is not a valid date"))
      )

      document must haveSummaryErrorMessages("End date is not a valid date")
      document must haveNoErrorMessageForStartDate
      document must haveErrorMessageForEndDate("End date is not a valid date")

    }
    "the end date precedes the start date" in {

      val document = generateDocument(
        form = emptyForm
          .fill(emptyRecordValues)
          .withError(FormError("endDate", "End date cannot precede start date"))
      )

      document must haveSummaryErrorMessages("End date cannot precede start date")
      document must haveNoErrorMessageForStartDate
      document must haveErrorMessageForEndDate("End date cannot precede start date")

    }
  }

  private def generateDocument(title: String = "", form: Form[EditSetRecordFormValues]): Document = {
    val editSetRecordEditInstance: editSetRecordEdit = inject[editSetRecordEdit]
    asDocument(
      editSetRecordEditInstance(
        user = user,
        editSetName = "COAL 80 Sample",
        title = title,
        record = editSetRecord,
        legalStatusReferenceData,
        corporateBodies = allCorporateBodies,
        editSetRecordForm = form
      )(
        Helpers.stubMessages(),
        CSRFTokenHelper.addCSRFToken(FakeRequest())
      )
    )
  }

}
