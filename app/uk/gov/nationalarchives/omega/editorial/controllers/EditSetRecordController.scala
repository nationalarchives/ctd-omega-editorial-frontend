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

package uk.gov.nationalarchives.omega.editorial.controllers

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import play.api.Logger
import play.api.data.{ Form, FormError }
import play.api.i18n.Messages
import play.api.mvc._
import play.twirl.api.HtmlFormat
import uk.gov.nationalarchives.omega.editorial.FrontendError.{ EditSetNotFound, EditSetRecordNotFound, InvalidAction, MissingAction, Outcome }
import uk.gov.nationalarchives.omega.editorial.{ CoveringDateError, FrontendError, controllers }
import uk.gov.nationalarchives.omega.editorial.controllers.EditSetRecordController._
import uk.gov.nationalarchives.omega.editorial.forms.EditSetRecordFormValues.populateForm
import uk.gov.nationalarchives.omega.editorial.forms.{ EditSetRecordFormValues, EditSetRecordFormValuesFormProvider }
import uk.gov.nationalarchives.omega.editorial.models._
import uk.gov.nationalarchives.omega.editorial.services.CoveringDateCalculator.getStartAndEndDates
import uk.gov.nationalarchives.omega.editorial.services.{ EditSetRecordService, EditSetService, ReferenceDataService }
import uk.gov.nationalarchives.omega.editorial.support.{ DateParser, FormSupport }
import uk.gov.nationalarchives.omega.editorial.views.html.{ editSetRecordEdit, editSetRecordEditDiscard, editSetRecordEditSave }

import java.time.LocalDate
import java.time.temporal.ChronoField.{ DAY_OF_MONTH, MONTH_OF_YEAR, YEAR }
import javax.inject.{ Inject, Singleton }

@Singleton
class EditSetRecordController @Inject() (
  messagesControllerComponents: MessagesControllerComponents,
  referenceDataService: ReferenceDataService,
  editSetService: EditSetService,
  editRecordSetService: EditSetRecordService,
  editSetRecordEdit: editSetRecordEdit,
  editSetRecordEditDiscard: editSetRecordEditDiscard,
  editSetRecordEditSave: editSetRecordEditSave
) extends BaseAppController(messagesControllerComponents, editSetService, editRecordSetService) {

  private val logger: Logger = Logger(this.getClass)

  def viewEditRecordForm(editSetId: String, recordId: String): Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] =>
      logger.info(s"The edit set id is $editSetId for record id $recordId")
      generateResultForView(editSetId, recordId, view).unsafeToFuture()
  }

  def submit(editSetId: String, editSetRecordId: String): Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] =>
      logger.info(s"The edit set id is $editSetId for record id $editSetRecordId")
      generateResultForSubmission(editSetId, editSetRecordId, submit).unsafeToFuture()
  }

  def save(editSetId: String, editSetRecordId: String): Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] =>
      generateResultForView(editSetId, editSetRecordId, generateSaveView).unsafeToFuture()
  }

  def discard(editSetId: String, editSetRecordId: String): Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] =>
      generateResultForView(editSetId, editSetRecordId, generateDiscardView).unsafeToFuture()
  }

  private def updateEditSetRecord(
    editSetId: String,
    editSetRecord: EditSetRecord,
    editSetRecordFormValues: EditSetRecordFormValues
  ): IO[Result] =
    editRecordSetService
      .updateEditSetRecord(editSetId, editSetRecord.oci, editSetRecordFormValues)
      .map { updateResponseStatus =>
        logger
          .info(s"We are currently ignoring the returned status of the update: [$updateResponseStatus]")
        Redirect(controllers.routes.EditSetRecordController.save(editSetId, editSetRecord.oci))
      }

  private def view(user: User, editSet: EditSet, editSetRecord: EditSetRecord)(implicit
    request: Request[AnyContent]
  ): IO[Result] =
    for {
      placesOfDeposit <- referenceDataService.getPlacesOfDeposit
      creators        <- referenceDataService.getAgentSummaries
      legalStatuses   <- referenceDataService.getLegalStatuses
    } yield {
      val editSetRecordPreparedForDisplay = prepareForDisplay(editSetRecord, placesOfDeposit, creators)
      Ok(
        generateEditSetRecordEditView(
          user,
          editSet,
          editSetRecordPreparedForDisplay,
          placesOfDeposit,
          creators,
          legalStatuses,
          bindFormFromRecordForDisplay(editSetRecordPreparedForDisplay)
        )
      )
    }

  private def submit(user: User, editSet: EditSet, editSetRecord: EditSetRecord)(implicit
    request: Request[AnyContent]
  ): IO[Result] =
    getSubmitAction(editSetRecord).flatMap {
      case Right(Save(editSetRecord, values)) => updateEditSetRecord(editSet.id, editSetRecord, values)
      case Right(Discard) => Redirect(controllers.routes.EditSetRecordController.discard(editSet.id, editSetRecord.oci))
      case Right(CalculateDates(record))    => calculateDates(user, editSet, record)
      case Right(AddAnotherCreator(record)) => addAnotherCreator(user, editSet, record)
      case Right(RemoveLastCreator(record)) => removeLastCreator(user, editSet, record)
      case Left(FormValidationFailed(formWithErrors, record)) =>
        for {
          placesOfDeposit <- referenceDataService.getPlacesOfDeposit
          legalStatuses   <- referenceDataService.getLegalStatuses
          creators        <- referenceDataService.getAgentSummaries
        } yield BadRequest(
          generateEditSetRecordEditView(user, editSet, record, placesOfDeposit, creators, legalStatuses, formWithErrors)
        )
      case Left(EditSetNotFound(missingEditSetId)) =>
        BadRequest(s"Edit Set with ID [$missingEditSetId] not found")
      case Left(EditSetRecordNotFound(missingOci)) => BadRequest(s"Record with $missingOci not found")
      case Left(InvalidAction(badAction))          => BadRequest(s"$badAction is not allowed action")
      case Left(MissingAction)                     => BadRequest("This action is not allowed")
      case Left(_)                                 => BadRequest("Unknown action")
    }

  private def generateResult(
    editSetId: String,
    editSetRecordId: String,
    onSuccess: (User, EditSet, EditSetRecord) => IO[Result],
    onUnknownEditSet: String => IO[Result],
    onUnknownEditSetRecord: (String, String) => IO[Result]
  )(implicit request: Request[AnyContent]): IO[Result] =
    withUserAsync { user =>
      (for {
        editSetFindOutcome       <- findEditSet(editSetId)
        editSetRecordFindOutcome <- findEditSetRecord(editSetId, editSetRecordId)
      } yield (editSetFindOutcome, editSetRecordFindOutcome) match {
        case (Right(editSet), Right(editSetRecord)) => onSuccess(user, editSet, editSetRecord)
        case (Left(_), _)                           => onUnknownEditSet(editSetId)
        case (_, Left(_))                           => onUnknownEditSetRecord(editSetId, editSetRecordId)
      }).flatten
    }

  private def generateResultForView(
    editSetId: String,
    editSetRecordId: String,
    onSuccess: (User, EditSet, EditSetRecord) => IO[Result]
  )(implicit request: Request[AnyContent]): IO[Result] =
    generateResult(
      editSetId,
      editSetRecordId,
      onSuccess,
      notFoundDueToUnknownEditSet,
      notFoundDueToUnknownEditSetRecord
    )

  private def generateResultForSubmission(
    editSetId: String,
    editSetRecordId: String,
    onSuccess: (User, EditSet, EditSetRecord) => IO[Result]
  )(implicit request: Request[AnyContent]): IO[Result] =
    generateResult(
      editSetId,
      editSetRecordId,
      onSuccess,
      badRequestDueToUnknownEditSet,
      badRequestDueToUnknownEditSetRecord
    )

  private def notFoundDueToUnknownEditSet(editSetId: String): Result =
    NotFound(generateMessageForUnknownEditSet(editSetId))

  private def notFoundDueToUnknownEditSetRecord(editSetId: String, editSetRecordId: String): Result =
    NotFound(generateMessageForUnknownEditSetRecord(editSetId, editSetRecordId))

  private def badRequestDueToUnknownEditSet(editSetId: String): Result =
    NotFound(generateMessageForUnknownEditSet(editSetId))

  private def badRequestDueToUnknownEditSetRecord(editSetId: String, editSetRecordId: String): Result =
    NotFound(generateMessageForUnknownEditSetRecord(editSetId, editSetRecordId))

  private def generateMessageForUnknownEditSet(editSetId: String): String = s"Edit Set [$editSetId] was not found"

  private def generateMessageForUnknownEditSetRecord(editSetId: String, editSetRecordId: String): String =
    s"Edit Set Record [$editSetRecordId] for Edit Set [$editSetId] was not found"

  private def generateDiscardView(
    user: User,
    editSet: EditSet,
    editSetRecord: EditSetRecord
  )(implicit
    messages: Messages
  ): IO[Result] = IO.pure {
    val title = resolvedMessage(MessageKeys.title)
    val heading = resolvedMessage(MessageKeys.heading, editSetRecord.ccr)
    val message = resolvedMessage(MessageKeys.buttonDiscard)
    val recordType = editSetRecord.recordType
    logger.info(s"Discard changes for record id ${editSetRecord.oci} edit set id ${editSet.id} ")
    Ok(editSetRecordEditDiscard(user, editSet.name, title, heading, editSetRecord.oci, message, recordType))
  }

  private def generateSaveView(
    user: User,
    editSet: EditSet,
    editSetRecord: EditSetRecord
  )(implicit
    messages: Messages
  ): IO[Result] = IO.pure {
    val title = resolvedMessage(MessageKeys.title)
    val heading = resolvedMessage(MessageKeys.heading, editSetRecord.ccr)
    val message = resolvedMessage(MessageKeys.buttonSave)
    val recordType = editSetRecord.recordType
    logger.info(s"Save changes for record id ${editSetRecord.oci} edit set id ${editSet.id}")
    Ok(editSetRecordEditSave(user, editSet.name, title, heading, editSetRecord.oci, message, recordType))
  }

  private def addAnotherCreator(
    user: User,
    editSet: EditSet,
    editSetRecord: EditSetRecord
  )(implicit
    request: Request[AnyContent]
  ): IO[Result] =
    for {
      placesOfDeposit <- referenceDataService.getPlacesOfDeposit
      legalStatuses   <- referenceDataService.getLegalStatuses
      creators        <- referenceDataService.getAgentSummaries
    } yield {
      val selectedNonEmptyCreatorsFromRequest = filterRequestData { case (key, value) =>
        key.startsWith(FieldNames.creatorIDs) && value.trim.nonEmpty
      }
      val keyForNewCreator = s"${FieldNames.creatorIDs}[${selectedNonEmptyCreatorsFromRequest.size}]"
      val updatedCreatorRelatedData = selectedNonEmptyCreatorsFromRequest ++ Map(keyForNewCreator -> "")
      val formFromRecord = bindFormFromRecordForDisplay(editSetRecord)
      val updatedForm = formFromRecord.copy(data = formFromRecord.data ++ updatedCreatorRelatedData, errors = Seq.empty)
      Ok(
        generateEditSetRecordEditView(
          user,
          editSet,
          editSetRecord,
          placesOfDeposit,
          creators,
          legalStatuses,
          updatedForm
        )
      )
    }

  private def filterRequestData(f: (String, String) => Boolean)(implicit request: Request[AnyContent]) =
    bindFormFromRequestForDisplay.data.filter { case (key, value) => f(key, value) }

  private def bindFormFromRequestForDisplay(implicit request: Request[AnyContent]): Form[EditSetRecordFormValues] =
    EditSetRecordFormValuesFormProvider().bindFromRequest()

  private def removeLastCreator(
    user: User,
    editSet: EditSet,
    editSetRecord: EditSetRecord
  )(implicit
    request: Request[AnyContent]
  ): IO[Result] =
    for {
      placesOfDeposit <- referenceDataService.getPlacesOfDeposit
      legalStatuses   <- referenceDataService.getLegalStatuses
      creators        <- referenceDataService.getAgentSummaries
    } yield {
      val selectedCreatorsFromRequest = filterRequestData { case (key, _) =>
        key.startsWith(FieldNames.creatorIDs)
      }
      val keyToRemove = s"${FieldNames.creatorIDs}[${selectedCreatorsFromRequest.size - 1}]"
      val formFromRecord = bindFormFromRequestForDisplay
      val updatedForm =
        formFromRecord.copy(data = formFromRecord.data ++ selectedCreatorsFromRequest - keyToRemove, errors = Seq.empty)
      Ok(
        generateEditSetRecordEditView(
          user,
          editSet,
          editSetRecord,
          placesOfDeposit,
          creators,
          legalStatuses,
          updatedForm
        )
      )
    }

  private def calculateDates(user: User, editSet: EditSet, record: EditSetRecord)(implicit
    request: Request[AnyContent]
  ): IO[Result] =
    for {
      placesOfDeposit <- referenceDataService.getPlacesOfDeposit
      legalStatuses   <- referenceDataService.getLegalStatuses
      creators        <- referenceDataService.getAgentSummaries
    } yield {
      val originalForm: Form[EditSetRecordFormValues] = EditSetRecordFormValuesFormProvider().bindFromRequest()
      val errorsForCoveringDatesOnly = originalForm.errors(FieldNames.coveringDates)
      if (errorsForCoveringDatesOnly.isEmpty) {
        singleRange(originalForm.data.getOrElse(FieldNames.coveringDates, "")) match {
          case Right(singleDateRangeOpt) =>
            Ok(
              generateEditSetRecordEditView(
                user,
                editSet,
                record,
                placesOfDeposit,
                creators,
                legalStatuses,
                formWithUpdatedDateFields(originalForm, singleDateRangeOpt)
              )
            )
          case Left(_) =>
            BadRequest(
              generateEditSetRecordEditView(
                user,
                editSet,
                record,
                placesOfDeposit,
                creators,
                legalStatuses,
                formAfterCoveringDatesParseError(originalForm)
              )
            )
        }
      } else {
        BadRequest(
          generateEditSetRecordEditView(
            user,
            editSet,
            record,
            placesOfDeposit,
            creators,
            legalStatuses,
            originalForm.copy(errors = errorsForCoveringDatesOnly)
          )
        )
      }
    }

  private def formWithUpdatedDateFields(
    originalForm: Form[EditSetRecordFormValues],
    dateRangeOpt: Option[DateRange]
  ): Form[EditSetRecordFormValues] =
    dateRangeOpt
      .map(dateRange =>
        originalForm.copy(
          data = originalForm.data ++ updatedDateFields(dateRange),
          errors = Seq.empty
        )
      )
      .getOrElse(originalForm)

  private def updatedDateFields(dateRange: DateRange): Map[String, String] =
    Map(
      FieldNames.startDateDay   -> dateRange.start.get(DAY_OF_MONTH).toString,
      FieldNames.startDateMonth -> dateRange.start.get(MONTH_OF_YEAR).toString,
      FieldNames.startDateYear  -> dateRange.start.get(YEAR).toString,
      FieldNames.endDateDay     -> dateRange.end.get(DAY_OF_MONTH).toString,
      FieldNames.endDateMonth   -> dateRange.end.get(MONTH_OF_YEAR).toString,
      FieldNames.endDateYear    -> dateRange.end.get(YEAR).toString
    )

  private def formAfterCoveringDatesParseError(
    originalForm: Form[EditSetRecordFormValues]
  ): Form[EditSetRecordFormValues] =
    originalForm.copy(errors =
      Seq(FormError(FieldNames.coveringDates, resolvedMessage(MessageKeys.coveringDatesUnparseable)))
    )

  private def singleRange(rawCoveringDates: String): Either[CoveringDateError, Option[DateRange]] =
    getStartAndEndDates(rawCoveringDates).map(DateRange.single)

  private def getSubmitAction(
    editSetRecord: EditSetRecord
  )(implicit request: Request[AnyContent]): IO[Outcome[SubmitAction]] =
    request.body.asFormUrlEncoded.get("action").headOption match {
      case Some("save") =>
        for {
          placesOfDeposit <- referenceDataService.getPlacesOfDeposit
          validatedForm   <- validateForm(editSetRecord, placesOfDeposit)
        } yield Save(editSetRecord, validatedForm)
      case Some("discard")           => Right(Discard)
      case Some("calculateDates")    => Right(CalculateDates(editSetRecord))
      case Some("addAnotherCreator") => Right(AddAnotherCreator(editSetRecord))
      case Some("removeLastCreator") => Right(RemoveLastCreator(editSetRecord))
      case Some(badAction)           => Left(InvalidAction(badAction))
      case None                      => Left(MissingAction)
    }

  private def validateForm(record: EditSetRecord, placesOfDeposit: Seq[AgentSummary])(implicit
    request: Request[AnyContent]
  ): Outcome[EditSetRecordFormValues] =
    formToEither(bindFormFromRequestForSubmission(placesOfDeposit)).left.map { badForm =>
      FormValidationFailed(badForm, record)
    }

  private def bindFormFromRequestForSubmission(placesOfDeposit: Seq[AgentSummary])(implicit
    request: Request[AnyContent]
  ): Form[EditSetRecordFormValues] =
    validate(EditSetRecordFormValuesFormProvider().bindFromRequest(), placesOfDeposit)

  private def validate(
    form: Form[EditSetRecordFormValues],
    placesOfDeposit: Seq[AgentSummary]
  ): Form[EditSetRecordFormValues] =
    Seq[FormSupport.EditSetRecordFormValuesTransformer](
      validateStartAndEndDates,
      validatePlaceOfDeposit(placesOfDeposit),
      validateCreators
    ).foldLeft(form)((form, transformer) => transformer(form))

  private def prepareForDisplay(
    originalEditSetRecord: EditSetRecord,
    placesOfDeposit: Seq[AgentSummary],
    creators: Seq[AgentSummary]
  ): EditSetRecord =
    Seq[EditSetRecord.Transformer](
      record => prepareCreatorIDs(creators, record),
      preparePlaceOfDeposit(placesOfDeposit)
    ).foldLeft(originalEditSetRecord)((editSetRecord, transformer) => transformer(editSetRecord))

  private def bindFormFromRecordForDisplay(editSetRecord: EditSetRecord)(implicit
    request: Request[AnyContent]
  ): Form[EditSetRecordFormValues] =
    EditSetRecordFormValuesFormProvider().fill(populateForm(editSetRecord))

  private def validateCreators(
    form: Form[EditSetRecordFormValues]
  ): Form[EditSetRecordFormValues] = {
    val numberOfRecognisedSelectedCreatorIDs = form.data.count { case (key, value) =>
      key.startsWith(s"${FieldNames.creatorIDs}[") && value.trim.nonEmpty
    }
    val errorsExceptForCreatorRelated = form.errors.filterNot(error => error.key.startsWith(FieldNames.creatorIDs))
    val creatorRelatedErrors =
      if (numberOfRecognisedSelectedCreatorIDs == 0) {
        Seq(FormError("creator-id-0", resolvedMessage(MessageKeys.creatorMissing)))
      } else Seq.empty
    form.copy(errors = errorsExceptForCreatorRelated ++ creatorRelatedErrors)
  }

  private def generateEditSetRecordEditView(
    user: User,
    editSet: EditSet,
    editSetRecord: EditSetRecord,
    placesOfDeposit: Seq[AgentSummary],
    creators: Seq[AgentSummary],
    legalStatuses: Seq[LegalStatus],
    form: Form[EditSetRecordFormValues]
  )(implicit request: Request[AnyContent]): HtmlFormat.Appendable = {
    val title = resolvedMessage(MessageKeys.title)
    editSetRecordEdit(
      user,
      editSet.name,
      title,
      editSetRecord,
      legalStatuses,
      placesOfDeposit,
      creators,
      form
    )
  }

  private def validateStartAndEndDates(form: Form[EditSetRecordFormValues]): Form[EditSetRecordFormValues] = {
    val errorForInvalidStartDate =
      FormError(FieldNames.startDateDay, resolvedMessage(MessageKeys.startDateInvalid))
    val errorForInvalidEndDate = FormError(FieldNames.endDateDay, resolvedMessage(MessageKeys.endDateInvalid))
    val errorForEndDateBeforeStartDate =
      FormError(FieldNames.endDateDay, resolvedMessage(MessageKeys.endDateBeforeStartDate))
    val formValues = form.data
    val additionalErrors = (extractStartDate(formValues), extractEndDate(formValues)) match {
      case (Some(startDate), Some(endDate)) if endDate.isBefore(startDate) => Seq(errorForEndDateBeforeStartDate)
      case (Some(_), Some(_))                                              => Seq.empty
      case (Some(_), None)                                                 => Seq(errorForInvalidEndDate)
      case (None, Some(_))                                                 => Seq(errorForInvalidStartDate)
      case (None, None) => Seq(errorForInvalidStartDate, errorForInvalidEndDate)
    }
    form.copy(errors = form.errors ++ additionalErrors)
  }

  private def validatePlaceOfDeposit(
    placesOfDeposit: Seq[AgentSummary]
  )(form: Form[EditSetRecordFormValues]): Form[EditSetRecordFormValues] = {
    val formWhenValueAbsentOrUnrecognised =
      form.copy(
        data = form.data ++ Map(FieldNames.placeOfDepositID -> noSelectionForPlaceOfDeposit),
        errors = form.errors.filter(_.key != FieldNames.placeOfDepositID) ++ Seq(
          FormError(FieldNames.placeOfDepositID, resolvedMessage(MessageKeys.placeOfDepositMissingOrInvalid))
        )
      )
    form.data.get(FieldNames.placeOfDepositID) match {
      case Some(`noSelectionForPlaceOfDeposit`) => form
      case Some(placeOfDepositId) if !isPlaceOfDepositIdRecognised(placeOfDepositId, placesOfDeposit) =>
        formWhenValueAbsentOrUnrecognised
      case None => formWhenValueAbsentOrUnrecognised
      case _    => form
    }
  }

  private def extractStartDate(formValues: Map[String, String]): Option[LocalDate] =
    extractDate(formValues, FieldNames.startDateDay, FieldNames.startDateMonth, FieldNames.startDateYear)

  private def extractEndDate(formValues: Map[String, String]): Option[LocalDate] =
    extractDate(formValues, FieldNames.endDateDay, FieldNames.endDateMonth, FieldNames.endDateYear)

  private def extractDate(
    formValues: Map[String, String],
    fieldNameForDay: String,
    fieldNameForMonth: String,
    fieldNameForYear: String
  ): Option[LocalDate] =
    for {
      day   <- formValues.get(fieldNameForDay)
      month <- formValues.get(fieldNameForMonth)
      year  <- formValues.get(fieldNameForYear)
      date  <- DateParser.parse(List(day, month, year).mkString("/"))
    } yield date

  private def isPlaceOfDepositIdRecognised(placeOfDepositId: String, placesOfDeposit: Seq[AgentSummary]): Boolean =
    placesOfDeposit.map(_.identifier).contains(placeOfDepositId)

  private def preparePlaceOfDeposit(
    placesOfDeposit: Seq[AgentSummary]
  )(editSetRecord: EditSetRecord): EditSetRecord = {
    val correctedValue =
      if (isPlaceOfDepositIdRecognised(editSetRecord.placeOfDepositID, placesOfDeposit))
        editSetRecord.placeOfDepositID
      else ""
    editSetRecord.copy(placeOfDepositID = correctedValue)
  }

  private def prepareCreatorIDs(creators: Seq[AgentSummary], editSetRecord: EditSetRecord): EditSetRecord =
    editSetRecord.copy(creatorIDs = editSetRecord.creatorIDs.filter { id =>
      creators.exists(_.identifier == id)
    })
}

object EditSetRecordController {

  private val noSelectionForPlaceOfDeposit = ""

  private sealed abstract class SubmitAction

  private case class Save(record: EditSetRecord, form: EditSetRecordFormValues) extends SubmitAction

  private case class CalculateDates(record: EditSetRecord) extends SubmitAction

  private case object Discard extends SubmitAction

  private case class AddAnotherCreator(editSetRecord: EditSetRecord) extends SubmitAction

  private case class RemoveLastCreator(editSetRecord: EditSetRecord) extends SubmitAction

  private case class FormValidationFailed(forWithErrors: Form[EditSetRecordFormValues], record: EditSetRecord)
      extends FrontendError.Error

  object FieldNames {
    val background = "background"
    val ccr = "ccr"
    val coveringDates = "covering-dates"
    val creatorIDs = "creator-ids"
    val custodialHistory = "custodial-history"
    val endDateDay = "end-date-day"
    val endDateMonth = "end-date-month"
    val endDateYear = "end-date-year"
    val endDateFieldError = "end-date-field-error"
    val formerReferenceDepartment = "former-reference-department"
    val formerReferencePro = "former-reference-pro"
    val legalStatusID = "legal-status-id"
    val note = "note"
    val oci = "oci"
    val placeOfDepositID = "place-of-deposit-id"
    val scopeAndContent = "scope-and-content"
    val startDateDay = "start-date-day"
    val startDateFieldError = "start-date-field-error"
    val startDateMonth = "start-date-month"
    val startDateYear = "start-date-year"
  }

  object MessageKeys {
    val backgroundTooLong = "edit-set.record.error.background-too-long"
    val buttonDiscard = "edit-set.record.discard.text"
    val buttonSave = "edit-set.record.save.text"
    val coveringDatesMissing = "edit-set.record.missing.covering-dates"
    val coveringDatesTooLong = "edit-set.record.error.covering-dates-too-long"
    val coveringDatesUnparseable = "edit-set.record.error.covering-dates"
    val custodialHistoryTooLong = "edit-set.record.error.custodial-history-long"
    val creatorMissing = "edit-set.record.error.minimum-creators"
    val endDateBeforeStartDate = "edit-set.record.error.end-date-before-start-date"
    val endDateInvalid = "edit-set.record.error.end-date"
    val formerReferenceDepartmentInvalid = "edit-set.record.error.former-reference-department"
    val formerReferenceProInvalid = "edit-set.record.error.former-reference-pro"
    val heading = "edit-set.record.edit.heading"
    val legalStatusMissing = "edit-set.record.error.choose-an-option"
    val noteTooLong = "edit-set.record.error.note-too-long"
    val placeOfDepositMissingOrInvalid = "edit-set.record.error.choose-an-option"
    val scopeAndContentInvalid = "edit-set.record.error.scope-and-content"
    val scopeAndContentMissing = "edit-set.record.missing.scope-and-content"
    val startDateInvalid = "edit-set.record.error.start-date"
    val title = "edit-set.record.edit.title"
  }
}
