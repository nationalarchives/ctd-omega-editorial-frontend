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
import play.api.data.Forms.{ mapping, seq, text }
import play.api.data.{ Form, FormError }
import play.api.test.{ CSRFTokenHelper, FakeRequest, Helpers }
import play.twirl.api.Html
import support.BaseViewSpec
import support.CommonMatchers._
import support.ExpectedValues.{ ExpectedSelectOption, ExpectedSummaryErrorMessage }
import uk.gov.hmrc.govukfrontend.views.html.components.{ GovukButton, GovukErrorMessage, GovukErrorSummary, GovukFieldset, GovukHint, GovukInput, GovukLabel, GovukSelect, GovukTextarea }
import uk.gov.nationalarchives.omega.editorial.controllers.EditSetRecordController.FieldNames
import uk.gov.nationalarchives.omega.editorial.forms.EditSetRecordFormValues
import uk.gov.nationalarchives.omega.editorial.models._
import uk.gov.nationalarchives.omega.editorial.views.html.editSetRecordEdit

class EditRecordViewSpec extends BaseViewSpec {

  private val emptyForm: Form[EditSetRecordFormValues] = Form(
    mapping(
      FieldNames.scopeAndContent           -> text,
      FieldNames.coveringDates             -> text,
      FieldNames.formerReferenceDepartment -> text,
      FieldNames.formerReferencePro        -> text,
      FieldNames.startDateDay              -> text,
      FieldNames.startDateMonth            -> text,
      FieldNames.startDateYear             -> text,
      FieldNames.endDateDay                -> text,
      FieldNames.endDateMonth              -> text,
      FieldNames.endDateYear               -> text,
      FieldNames.legalStatusID             -> text,
      FieldNames.placeOfDepositID          -> text,
      FieldNames.note                      -> text,
      FieldNames.background                -> text,
      FieldNames.custodialHistory          -> text,
      FieldNames.creatorIDs                -> seq(text)
    )(EditSetRecordFormValues.apply)(EditSetRecordFormValues.unapply)
  )

  private val legalStatusReferenceData =
    Seq(
      LegalStatus("ref.1", "Public Record(s)"),
      LegalStatus("ref.2", "Not Public Records"),
      LegalStatus("ref.3", "Public Records unless otherwise Stated"),
      LegalStatus("ref.4", "Welsh Public Record(s)")
    )

  private val allPlacesOfDeposits = Seq(
    AgentSummary(
      AgentType.CorporateBody,
      "1",
      "current description",
      List(
        AgentDescription(
          "1",
          "The National Archives",
          Some(false),
          Some(false),
          "2022-06-22T02:00:00-0500",
          Some("2003"),
          None
        )
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      "2",
      "current description",
      List(
        AgentDescription(
          "2",
          "British Museum Central Archive",
          Some(false),
          Some(false),
          "2022-06-22T02:00:00-0500",
          Some("2001"),
          Some("2001")
        )
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      "3",
      "current description",
      List(
        AgentDescription(
          "3",
          "British Library, Sound Archive",
          Some(false),
          Some(false),
          "2022-06-22T02:00:00-0500",
          Some("1983"),
          Some("1983")
        )
      )
    )
  )

  private val allCreators: Seq[AgentSummary] = Seq.empty

  val editSetRecord: EditSetRecord = EditSetRecord(
    ccr = "COAL 80/80/1",
    oci = "COAL.2022.V1RJW.P",
    scopeAndContent = "",
    coveringDates = "",
    formerReferenceDepartment = "",
    formerReferencePro = "",
    startDateDay = "",
    startDateMonth = "",
    startDateYear = "",
    endDateDay = "",
    endDateMonth = "",
    endDateYear = "",
    legalStatusID = "",
    placeOfDepositID = "",
    background = "",
    note = "",
    custodialHistory = "",
    relatedMaterial = Seq(
      MaterialReference.DescriptionOnly(
        description = "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths."
      ),
      MaterialReference.LinkAndDescription(
        linkHref = "#;",
        linkText = "COAL 80/80/2",
        description = "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths."
      ),
      MaterialReference.LinkOnly(linkHref = "#;", linkText = "COAL 80/80/3")
    )
  )

  val emptyRecordValues: EditSetRecordFormValues = EditSetRecordFormValues.apply(
    scopeAndContent = "",
    coveringDates = "",
    formerReferenceDepartment = "",
    formerReferencePro = "",
    startDateDay = "",
    startDateMonth = "",
    startDateYear = "",
    endDateDay = "",
    endDateMonth = "",
    endDateYear = "",
    placeOfDepositID = "",
    legalStatusID = "",
    note = "",
    background = "",
    custodialHistory = "",
    creatorIDs = List.empty
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
            formerReferencePro = "TestFormerReferencePro",
            startDateDay = "1",
            startDateMonth = "2",
            startDateYear = "1960",
            endDateDay = "31",
            endDateMonth = "12",
            endDateYear = "1960",
            legalStatusID = "ref.1",
            placeOfDepositID = "3"
          )
        )
      )
      document must haveTitle("TNA reference: COAL 80/80/1")
      document must haveHeading("edit-set.record.edit.heading")
      document must haveSubHeading("edit-set.record.edit.id")
      document must haveClassicCatalogueRef("COAL 80/80/1")
      document must haveOmegaCatalogueId("COAL.2022.V1RJW.P")
      document must haveScopeAndContent(
        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths."
      )
      document must haveCoveringDates("1960")
      document must haveFormerReferenceDepartment("TestFormerReferenceDepartment")
      document must haveFormerReferencePro("TestFormerReferencePro")
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
      document must haveSelectionForPlaceOfDeposit(
        Seq(
          ExpectedSelectOption("", "edit-set.record.error.place-of-deposit-id", disabled = true),
          ExpectedSelectOption("1", "The National Archives"),
          ExpectedSelectOption("2", "British Museum Central Archive"),
          ExpectedSelectOption("3", "British Library, Sound Archive", selected = true)
        )
      )

    }
    "render an error given no scope and content" in {
      val editSetRecordEditInstance = getEditRecordInstance
      val title = "EditRecordTitleTest"
      val editSetName = "COAL 80 Sample"
      val filledForm = emptyForm
        .fill(emptyRecordValues)
        .withError(FormError(FieldNames.scopeAndContent, "Enter the scope and content."))

      val editRecordHtml: Html =
        editSetRecordEditInstance(
          user,
          editSetName,
          title,
          editSetRecord,
          legalStatusReferenceData,
          allPlacesOfDeposits,
          allCreators,
          filledForm
        )(
          Helpers.stubMessages(),
          CSRFTokenHelper.addCSRFToken(FakeRequest())
        )

      val document = asDocument(editRecordHtml)
      document must haveTitle("EditRecordTitleTest")
      document must haveSummaryErrorTitle("error.summary.title")
      document must haveSummaryErrorMessages(
        ExpectedSummaryErrorMessage("Enter the scope and content.", FieldNames.scopeAndContent)
      )
      document must haveErrorMessageForScopeAndContent("Enter the scope and content.")
      document must haveScopeAndContent("")
    }

    "render an error when given scope and content is more than 8000 characters" in {
      val editSetRecordEditInstance = getEditRecordInstance
      val title = "EditRecordTitleTest"
      val editSetName = "COAL 80 Sample"
      val filledForm = emptyForm
        .fill(
          emptyRecordValues.copy(
            scopeAndContent = "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths."
          )
        )
        .withError(FormError(FieldNames.scopeAndContent, "Scope and content too long, maximum length 8000 characters"))

      val editRecordHtml: Html =
        editSetRecordEditInstance(
          user,
          editSetName,
          title,
          editSetRecord,
          legalStatusReferenceData,
          allPlacesOfDeposits,
          allCreators,
          filledForm
        )(
          Helpers.stubMessages(),
          CSRFTokenHelper.addCSRFToken(FakeRequest())
        )

      val document = asDocument(editRecordHtml)
      document must haveTitle("EditRecordTitleTest")
      document must haveSummaryErrorTitle("error.summary.title")
      document must haveSummaryErrorMessages(
        ExpectedSummaryErrorMessage(
          "Scope and content too long, maximum length 8000 characters",
          FieldNames.scopeAndContent
        )
      )
      document must haveErrorMessageForScopeAndContent("Scope and content too long, maximum length 8000 characters")
      document must haveScopeAndContent(
        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths."
      )
    }

    "render an error when given former reference department is more than 255 characters" in {
      val editSetRecordEditInstance = getEditRecordInstance
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
            FieldNames.formerReferenceDepartment,
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
          allPlacesOfDeposits,
          allCreators,
          filledForm
        )(
          Helpers.stubMessages(),
          CSRFTokenHelper.addCSRFToken(FakeRequest())
        )

      val document = asDocument(editRecordHtml)
      document must haveTitle("EditRecordTitleTest")
      document must haveSummaryErrorTitle("error.summary.title")
      document must haveSummaryErrorMessages(
        ExpectedSummaryErrorMessage(
          "Former reference - Department too long, maximum length 255 characters",
          FieldNames.formerReferenceDepartment
        )
      )
      document must haveErrorMessageForFormerReferenceDepartment(
        "Former reference - Department too long, maximum length 255 characters"
      )
      document must haveFormerReferenceDepartment(
        "Former reference - Department Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing"
      )
    }
    "render an error when no legal status is selected" in {
      val editSetRecordEditInstance = getEditRecordInstance

      val title = "EditRecordTitleTest"
      val editSetName = "COAL 80 Sample"
      val filledForm = emptyForm
        .fill(emptyRecordValues)
        .withError(FormError(FieldNames.legalStatusID, "Select a valid legal status"))

      val editRecordHtml: Html =
        editSetRecordEditInstance(
          user,
          editSetName,
          title,
          editSetRecord,
          legalStatusReferenceData,
          allPlacesOfDeposits,
          allCreators,
          filledForm
        )(
          Helpers.stubMessages(),
          CSRFTokenHelper.addCSRFToken(FakeRequest())
        )

      val document = asDocument(editRecordHtml)
      document must haveSummaryErrorTitle("error.summary.title")
      document must haveSummaryErrorMessages(
        ExpectedSummaryErrorMessage("Select a valid legal status", FieldNames.legalStatusID)
      )
    }

    "render an error when given former reference pro is more than 255 characters" in {
      val editSetRecordEditInstance = getEditRecordInstance
      val title = "EditRecordTitleTest"
      val editSetName = "COAL 80 Sample"
      val filledForm = emptyForm
        .fill(
          emptyRecordValues.copy(
            scopeAndContent = "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
            formerReferencePro =
              "Former reference PRO Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing"
          )
        )
        .withError(
          FormError(
            FieldNames.formerReferencePro,
            "Former reference (PRO) too long, maximum length 255 characters"
          )
        )

      val editRecordHtml: Html =
        editSetRecordEditInstance(
          user,
          editSetName,
          title,
          editSetRecord,
          legalStatusReferenceData,
          allPlacesOfDeposits,
          allCreators,
          filledForm
        )(
          Helpers.stubMessages(),
          CSRFTokenHelper.addCSRFToken(FakeRequest())
        )

      val document = asDocument(editRecordHtml)
      document must haveTitle("EditRecordTitleTest")
      document must haveSummaryErrorTitle("error.summary.title")
      document must haveSummaryErrorMessages(
        ExpectedSummaryErrorMessage(
          "Former reference (PRO) too long, maximum length 255 characters",
          FieldNames.formerReferencePro
        )
      )
      document must haveErrorMessageForFormerReferencePro(
        "Former reference (PRO) too long, maximum length 255 characters"
      )
      document must haveFormerReferencePro(
        "Former reference PRO Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing Testing"
      )
    }

    "render an error when given invalid covering dates" in {
      val editSetRecordEditInstance = getEditRecordInstance

      val title = "EditRecordTitleTest"
      val editSetName = "COAL 80 Sample"
      val inputData = EditSetRecordFormValues(
        scopeAndContent = "",
        coveringDates = "Mon 1 Oct 1330",
        formerReferenceDepartment = "",
        formerReferencePro = "",
        startDateDay = "1",
        startDateMonth = "2",
        startDateYear = "1960",
        endDateDay = "31",
        endDateMonth = "12",
        endDateYear = "1960",
        legalStatusID = "ref.1",
        placeOfDepositID = "2",
        note = "",
        background = "",
        custodialHistory = "",
        creatorIDs = List.empty
      )

      val editSetRecordForm = emptyForm
        .fill(inputData)
        .withError(FieldNames.coveringDates, "covering date message string")

      val editRecordHtml =
        editSetRecordEditInstance(
          user,
          editSetName,
          title,
          editSetRecord,
          legalStatusReferenceData,
          allPlacesOfDeposits,
          allCreators,
          editSetRecordForm
        )(
          Helpers.stubMessages(),
          CSRFTokenHelper.addCSRFToken(FakeRequest())
        )

      val document = asDocument(editRecordHtml)
      document must haveSummaryErrorMessages(
        ExpectedSummaryErrorMessage("covering date message string", FieldNames.coveringDates)
      )
    }
  }

  "render expecting errors when" when {
    "only the start date is invalid" in {

      val document = generateDocument(
        form = emptyForm
          .fill(emptyRecordValues)
          .withError(FormError(FieldNames.startDateDay, "Start date is not a valid date"))
      )

      document must haveSummaryErrorMessages(
        ExpectedSummaryErrorMessage("Start date is not a valid date", FieldNames.startDateDay)
      )
      document must haveErrorMessageForStartDate("Start date is not a valid date")
      document must haveNoErrorMessageForEndDate

    }
    "only the end date is invalid" in {

      val document = generateDocument(
        form = emptyForm
          .fill(emptyRecordValues)
          .withError(FormError(FieldNames.endDateDay, "End date is not a valid date"))
      )

      document must haveSummaryErrorMessages(
        ExpectedSummaryErrorMessage("End date is not a valid date", FieldNames.endDateDay)
      )
      document must haveNoErrorMessageForStartDate
      document must haveErrorMessageForEndDate("End date is not a valid date")

    }
    "the end date precedes the start date" in {

      val document = generateDocument(
        form = emptyForm
          .fill(emptyRecordValues)
          .withError(FormError(FieldNames.endDateDay, "End date cannot precede start date"))
      )

      document must haveSummaryErrorMessages(
        ExpectedSummaryErrorMessage("End date cannot precede start date", FieldNames.endDateDay)
      )
      document must haveNoErrorMessageForStartDate
      document must haveErrorMessageForEndDate("End date cannot precede start date")

    }
  }

  private def generateDocument(title: String = "", form: Form[EditSetRecordFormValues]): Document = {
    val editSetRecordEditInstance: editSetRecordEdit = getEditRecordInstance
    asDocument(
      editSetRecordEditInstance(
        user = user,
        editSetName = "COAL 80 Sample",
        title = title,
        record = editSetRecord,
        legalStatusReferenceData,
        placesOfDeposit = allPlacesOfDeposits,
        creators = allCreators,
        editSetRecordForm = form
      )(
        Helpers.stubMessages(),
        CSRFTokenHelper.addCSRFToken(FakeRequest())
      )
    )
  }

  private def getEditRecordInstance: editSetRecordEdit =
    new editSetRecordEdit(
      new GovukButton,
      new GovukInput(new GovukErrorMessage, new GovukHint, new GovukLabel),
      new GovukTextarea(new GovukErrorMessage, new GovukHint, new GovukLabel),
      new GovukFieldset,
      new GovukErrorSummary,
      new GovukSelect(new GovukErrorMessage, new GovukHint, new GovukLabel),
      new GovukLabel
    )

}
