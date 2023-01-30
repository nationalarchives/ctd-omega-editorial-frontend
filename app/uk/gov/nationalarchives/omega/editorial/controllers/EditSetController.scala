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

import play.api.data.Forms.{ mapping, seq, text }
import play.api.data.{ Form, FormError }
import play.api.i18n.{ I18nSupport, Lang }
import play.api.Logger
import play.api.mvc._
import play.twirl.api.HtmlFormat
import uk.gov.nationalarchives.omega.editorial._
import uk.gov.nationalarchives.omega.editorial.controllers.authentication.Secured
import uk.gov.nationalarchives.omega.editorial.forms.EditSetRecordFormValues
import uk.gov.nationalarchives.omega.editorial.forms.EditSetRecordFormValues._
import uk.gov.nationalarchives.omega.editorial.models._
import uk.gov.nationalarchives.omega.editorial.services._
import uk.gov.nationalarchives.omega.editorial.services.CoveringDateCalculator.getStartAndEndDates
import uk.gov.nationalarchives.omega.editorial.services.{ CoveringDateError, EditSetPagination, ReferenceDataService }
import uk.gov.nationalarchives.omega.editorial.support.{ DateParser, FormSupport }
import uk.gov.nationalarchives.omega.editorial.views.html.{ editSet, editSetRecordEdit, editSetRecordEditDiscard, editSetRecordEditSave }

import java.time.LocalDate
import java.time.temporal.ChronoField.{ DAY_OF_MONTH, MONTH_OF_YEAR, YEAR }
import javax.inject._

/** This controller creates an `Action` to handle HTTP requests to the application's home page.
  */
@Singleton
class EditSetController @Inject() (
  messagesControllerComponents: MessagesControllerComponents,
  referenceDataService: ReferenceDataService,
  editSet: editSet,
  editSetRecordEdit: editSetRecordEdit,
  editSetRecordEditDiscard: editSetRecordEditDiscard,
  editSetRecordEditSave: editSetRecordEditSave
) extends MessagesAbstractController(messagesControllerComponents) with I18nSupport with Secured with FormSupport {
  import EditSetController._

  private val logger: Logger = Logger(this.getClass)

  private lazy val creators: Seq[Creator] = referenceDataService.getCreators

  private lazy val placesOfDeposit: Seq[PlaceOfDeposit] = referenceDataService.getPlacesOfDeposit

  private lazy val legalStatuses: Seq[LegalStatus] = referenceDataService.getLegalStatuses

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

  type FormTransformer = Form[EditSetRecordFormValues] => Form[EditSetRecordFormValues]
  type RecordTransformer = EditSetRecord => EditSetRecord

  private val editSetRecordForm: Form[EditSetRecordFormValues] = Form(
    mapping(
      FieldNames.scopeAndContent -> text
        .verifying(
          resolvedMessage(MessageKeys.scopeAndContentInvalid),
          value => value.length <= 8000
        )
        .verifying(resolvedMessage(MessageKeys.scopeAndContentMissing), _.nonEmpty),
      FieldNames.coveringDates -> text
        .verifying(
          resolvedMessage(MessageKeys.coveringDatesMissing),
          _.trim.nonEmpty
        )
        .verifying(
          resolvedMessage(MessageKeys.coveringDatesTooLong),
          _.length <= 255
        )
        .verifying(
          resolvedMessage(MessageKeys.coveringDatesUnparseable),
          value => getStartAndEndDates(value).isRight
        ),
      FieldNames.formerReferenceDepartment -> text.verifying(
        resolvedMessage(MessageKeys.formerReferenceDepartmentInvalid),
        value => value.length <= 255
      ),
      FieldNames.formerReferencePro -> text.verifying(
        resolvedMessage(MessageKeys.formerReferenceProInvalid),
        value => value.length <= 255
      ),
      FieldNames.startDateDay   -> text,
      FieldNames.startDateMonth -> text,
      FieldNames.startDateYear  -> text,
      FieldNames.endDateDay     -> text,
      FieldNames.endDateMonth   -> text,
      FieldNames.endDateYear    -> text,
      FieldNames.legalStatusID -> text
        .verifying(
          resolvedMessage(MessageKeys.legalStatusMissing),
          _.trim.nonEmpty
        ),
      FieldNames.placeOfDepositID -> text
        .verifying(
          resolvedMessage(MessageKeys.placeOfDepositMissingOrInvalid),
          _.trim.nonEmpty
        ),
      FieldNames.note -> text
        .verifying(
          resolvedMessage(MessageKeys.noteTooLong),
          value => value.length <= 1000
        ),
      FieldNames.background -> text
        .verifying(
          resolvedMessage(MessageKeys.backgroundTooLong),
          value => value.length <= 8000
        ),
      FieldNames.custodialHistory -> text
        .verifying(
          resolvedMessage(MessageKeys.custodialHistoryTooLong),
          value => value.length <= 1000
        ),
      FieldNames.creatorIDs -> seq(text)
        .verifying(resolvedMessage(MessageKeys.creatorMissing), _.nonEmpty)
    )(EditSetRecordFormValues.apply)(EditSetRecordFormValues.unapply)
  )

  /** Create an Action for the edit set page.
    *
    * The configuration in the `routes` file means that this method will be called when the application receives a `GET`
    * request with a path of `/edit-set/{id}`.
    */
  def view(id: String): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    withUser { user =>
      val ordering = for {
        field     <- queryStringValue(request, fieldKey)
        direction <- queryStringValue(request, orderDirectionKey)
      } yield RowOrdering(field, direction)

      generateEditSetView(id, user, ordering.getOrElse(RowOrdering.defaultOrdering))
    }
  }

  /** Create an Action for the edit set record edit page.
    *
    * The configuration in the `routes` file means that this method will be called when the application receives a `GET`
    * request with a path of `/edit-set/{id}/record/{recordId}/edit`.
    */
  def viewEditRecordForm(id: String, recordId: String): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      withUser { user =>
        logger.info(s"The edit set id is $id for record id $recordId")
        val editSetName = editSets.getEditSet().name
        editSetRecords.getEditSetRecordByOCI(recordId) match {
          case Some(record) =>
            val recordPreparedForDisplay = prepareForDisplay(record)
            val recordForm = bindFormFromRecordForDisplay(recordPreparedForDisplay)
            Ok(generateEditSetRecordEditView(user, editSetName, recordPreparedForDisplay, recordForm))
          case None => NotFound
        }
      }
  }

  def submit(id: String, oci: String): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    withUser { user =>
      logger.info(s"The edit set id is $id for record id $oci")
      val editSetName = editSets.getEditSet().name

      val action = for {
        record <- findRecord(oci)
        action <- getSubmitAction(record)
      } yield action

      action match {

        case Right(Save(record, values)) =>
          val newRecord = modifyEditSetRecordWithFormValues(record, values)
          editSetRecords.saveEditSetRecord(newRecord)
          Redirect(controllers.routes.EditSetController.save(id, record.oci))

        case Right(Discard) => Redirect(controllers.routes.EditSetController.discard(id, oci))

        case Right(CalculateDates(record)) => calculateDates(user, record)

        case Right(AddAnotherCreator(record)) => addAnotherCreator(user, editSetName, record)

        case Right(RemoveLastCreator(record)) => removeLastCreator(user, editSetName, record)

        case Left(FormValidationFailed(formWithErrors, record)) =>
          BadRequest(generateEditSetRecordEditView(user, editSetName, record, formWithErrors))

        case Left(RecordNotFound(missingOci)) =>
          BadRequest(s"Record with $missingOci not found")

        case Left(InvalidAction(badAction)) =>
          BadRequest(s"$badAction is not allowed action")

        case Left(MissingAction) =>
          BadRequest("This action is not allowed")

      }

    }

  }

  def save(id: String, oci: String): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    withUser { user =>
      val title: String = resolvedMessage(MessageKeys.title)
      val editSetName = editSets.getEditSet().name
      editSetRecords.getEditSetRecordByOCI(oci).get
      val heading: String =
        resolvedMessage(MessageKeys.heading, editSetRecords.getEditSetRecordByOCI(oci).get.ccr)
      val message: String = resolvedMessage(MessageKeys.buttonSave)
      logger.info(s"Save changes for record id $oci edit set id $id")

      Ok(editSetRecordEditSave(user, editSetName, title, heading, oci, message))
    }
  }

  def discard(id: String, oci: String): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    withUser { user =>
      val title: String = resolvedMessage(MessageKeys.title)
      val editSetName = editSets.getEditSet().name
      editSetRecords.getEditSetRecordByOCI(oci).get
      val heading: String =
        resolvedMessage(MessageKeys.heading, editSetRecords.getEditSetRecordByOCI(oci).get.ccr)
      val message: String = resolvedMessage(MessageKeys.buttonDiscard)
      logger.info(s"Discard changes for record id $oci edit set id $id ")

      Ok(editSetRecordEditDiscard(user, editSetName, title, heading, oci, message))
    }
  }

  private def queryStringValue(request: Request[AnyContent], key: String): Option[String] =
    request.queryString.get(key).flatMap(_.headOption)

  private def prepareForDisplay(originalEditSetRecord: EditSetRecord): EditSetRecord =
    Seq[RecordTransformer](prepareCreatorIDs, preparePlaceOfDeposit).foldLeft(originalEditSetRecord)(
      (editSetRecord, transformer) => transformer(editSetRecord)
    )

  private def generateEditSetView(id: String, user: User, ordering: RowOrdering)(implicit
    request: Request[AnyContent]
  ): Result = {
    logger.info(s"The edit set id is $id ")
    val currentEditSet = editSets.getEditSet()

    val editSetEntries = currentEditSet.entries.sorted(ordering.ordering)
    val pageNumber = queryStringValue(request, offsetKey).map(_.toInt).getOrElse(1)

    val editSetPage = new EditSetPagination(
      id = id,
      ordering = ordering,
      nextText = resolvedMessage("edit-set.pagination.next"),
      previousText = resolvedMessage("edit-set.pagination.previous")
    ).makeEditSetPage(editSetEntries, pageNumber)
    val title = resolvedMessage("edit-set.title", editSetPage.pageNumber.toString, editSetPage.totalPages.toString)
    val heading: String = resolvedMessage(
      "edit-set.heading",
      currentEditSet.name,
      editSetPage.numberOfFirstEntry.toString,
      editSetPage.numberOfLastEntry.toString,
      editSetPage.totalNumberOfEntries.toString
    )
    Ok(editSet(user, title, heading, editSetPage, ordering))
  }

  private def prepareCreatorIDs(editSetRecord: EditSetRecord): EditSetRecord =
    editSetRecord.copy(creatorIDs = editSetRecord.creatorIDs.filter(isCreatorRecognised))

  private def preparePlaceOfDeposit(editSetRecord: EditSetRecord): EditSetRecord = {
    val correctedValue =
      if (isPlaceOfDepositRecognised(editSetRecord.placeOfDepositID)) editSetRecord.placeOfDepositID else ""
    editSetRecord.copy(placeOfDepositID = correctedValue)
  }

  /** For both start date and end date, if any of the parts are at fault (like the month), we assign the error to the
    * first field, the day. See: https://design-system.service.gov.uk/components/error-summary/
    * @param form
    * @return
    */
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

  private def validatePlaceOfDeposit(form: Form[EditSetRecordFormValues]): Form[EditSetRecordFormValues] = {
    val formWhenValueAbsentOrUnrecognised =
      form.copy(
        data = form.data ++ Map(FieldNames.placeOfDepositID -> noSelectionForPlaceOfDeposit),
        errors = form.errors.filter(_.key != FieldNames.placeOfDepositID) ++ Seq(
          FormError(FieldNames.placeOfDepositID, resolvedMessage(MessageKeys.placeOfDepositMissingOrInvalid))
        )
      )
    form.data.get(FieldNames.placeOfDepositID) match {
      case Some(`noSelectionForPlaceOfDeposit`)                                => form
      case Some(placeOfDeposit) if !isPlaceOfDepositRecognised(placeOfDeposit) => formWhenValueAbsentOrUnrecognised
      case None                                                                => formWhenValueAbsentOrUnrecognised
      case _                                                                   => form
    }
  }

  private def findRecord(oci: String): Outcome[EditSetRecord] =
    editSetRecords.getEditSetRecordByOCI(oci).toRight(RecordNotFound(oci))

  private def getSubmitAction(record: EditSetRecord)(implicit request: Request[AnyContent]): Outcome[SubmitAction] =
    request.body.asFormUrlEncoded.get("action").headOption match {
      case Some("save") =>
        validateForm(record).map { form =>
          Save(record, form)
        }
      case Some("discard")           => Right(Discard)
      case Some("calculateDates")    => Right(CalculateDates(record))
      case Some("addAnotherCreator") => Right(AddAnotherCreator(record))
      case Some("removeLastCreator") => Right(RemoveLastCreator(record))
      case Some(badAction)           => Left(InvalidAction(badAction))
      case None                      => Left(MissingAction)
    }

  private def validateForm(
    record: EditSetRecord
  )(implicit request: Request[AnyContent]): Outcome[EditSetRecordFormValues] =
    formToEither(bindFormFromRequestForSubmission).left.map { badForm =>
      FormValidationFailed(badForm, record)
    }

  private def bindFormFromRequestForDisplay(implicit request: Request[AnyContent]): Form[EditSetRecordFormValues] =
    editSetRecordForm.bindFromRequest()

  private def bindFormFromRequestForSubmission(implicit request: Request[AnyContent]): Form[EditSetRecordFormValues] =
    validate(editSetRecordForm.bindFromRequest())

  private def bindFormFromRecordForDisplay(editSetRecord: EditSetRecord): Form[EditSetRecordFormValues] =
    editSetRecordForm.fill(populateForm(editSetRecord))

  private def validate(form: Form[EditSetRecordFormValues]): Form[EditSetRecordFormValues] =
    Seq[FormTransformer](validateStartAndEndDates, validatePlaceOfDeposit, validateCreators).foldLeft(form)(
      (form, transformer) => transformer(form)
    )

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

  private def isPlaceOfDepositRecognised(placeOfDeposit: String): Boolean =
    placesOfDeposit.map(_.id).contains(placeOfDeposit)

  private def isCreatorRecognised(creatorID: String): Boolean =
    creatorID.trim.nonEmpty && creators.exists(_.id == creatorID)

  private def resolvedMessage(key: String, args: String*): String = messagesApi(key, args: _*)(Lang("en"))

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

  private def calculateDates(user: User, record: EditSetRecord)(implicit
    request: Request[AnyContent]
  ): Result = {
    val originalForm: Form[EditSetRecordFormValues] = editSetRecordForm.bindFromRequest()
    val errorsForCoveringDatesOnly = originalForm.errors(FieldNames.coveringDates)
    val editSetName = editSets.getEditSet().name
    if (errorsForCoveringDatesOnly.isEmpty) {
      singleRange(originalForm.data.getOrElse(FieldNames.coveringDates, "")) match {
        case Right(singleDateRangeOpt) =>
          Ok(
            generateEditSetRecordEditView(
              user,
              editSetName,
              record,
              formWithUpdatedDateFields(originalForm, singleDateRangeOpt)
            )
          )
        case Left(_) =>
          BadRequest(
            generateEditSetRecordEditView(
              user,
              editSetName,
              record,
              formAfterCoveringDatesParseError(originalForm)
            )
          )
      }
    } else {
      BadRequest(
        generateEditSetRecordEditView(
          user,
          editSetName,
          record,
          originalForm.copy(errors = errorsForCoveringDatesOnly)
        )
      )
    }
  }

  private def addAnotherCreator(user: User, editSetName: String, editSetRecord: EditSetRecord)(implicit
    request: Request[AnyContent]
  ): Result = {

    val selectedNonEmptyCreatorsFromRequest = filterRequestData { case (key, value) =>
      key.startsWith(FieldNames.creatorIDs) && value.trim.nonEmpty
    }
    val keyForNewCreator = s"${FieldNames.creatorIDs}[${selectedNonEmptyCreatorsFromRequest.size}]"
    val updatedCreatorRelatedData = selectedNonEmptyCreatorsFromRequest ++ Map(keyForNewCreator -> "")
    val formFromRecord = bindFormFromRecordForDisplay(editSetRecord)
    val updatedForm = formFromRecord.copy(data = formFromRecord.data ++ updatedCreatorRelatedData, errors = Seq.empty)
    Ok(generateEditSetRecordEditView(user, editSetName, editSetRecord, updatedForm))
  }

  private def removeLastCreator(user: User, editSetName: String, editSetRecord: EditSetRecord)(implicit
    request: Request[AnyContent]
  ): Result = {
    val selectedCreatorsFromRequest = filterRequestData { case (key, _) =>
      key.startsWith(FieldNames.creatorIDs)
    }
    val keyToRemove = s"${FieldNames.creatorIDs}[${selectedCreatorsFromRequest.size - 1}]"
    val formFromRecord = bindFormFromRequestForDisplay
    val updatedForm =
      formFromRecord.copy(data = formFromRecord.data ++ selectedCreatorsFromRequest - keyToRemove, errors = Seq.empty)
    Ok(generateEditSetRecordEditView(user, editSetName, editSetRecord, updatedForm))
  }

  private def generateEditSetRecordEditView(
    user: User,
    editSetName: String,
    editSetRecord: EditSetRecord,
    form: Form[EditSetRecordFormValues]
  )(implicit request: Request[AnyContent]): HtmlFormat.Appendable = {
    val title = resolvedMessage(MessageKeys.title)
    editSetRecordEdit(
      user,
      editSetName,
      title,
      editSetRecord,
      legalStatuses,
      placesOfDeposit,
      creators,
      form
    )
  }

  private def filterRequestData(f: (String, String) => Boolean)(implicit request: Request[AnyContent]) =
    bindFormFromRequestForDisplay.data.filter { case (key, value) => f(key, value) }

  private def singleRange(rawCoveringDates: String): Either[CoveringDateError, Option[DateRange]] =
    getStartAndEndDates(rawCoveringDates).map(DateRange.single)

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

  private def formAfterCoveringDatesParseError(
    originalForm: Form[EditSetRecordFormValues]
  ): Form[EditSetRecordFormValues] =
    originalForm.copy(errors =
      Seq(FormError(FieldNames.coveringDates, resolvedMessage(MessageKeys.coveringDatesUnparseable)))
    )

  private def updatedDateFields(dateRange: DateRange): Map[String, String] =
    Map(
      FieldNames.startDateDay   -> dateRange.start.get(DAY_OF_MONTH).toString,
      FieldNames.startDateMonth -> dateRange.start.get(MONTH_OF_YEAR).toString,
      FieldNames.startDateYear  -> dateRange.start.get(YEAR).toString,
      FieldNames.endDateDay     -> dateRange.end.get(DAY_OF_MONTH).toString,
      FieldNames.endDateMonth   -> dateRange.end.get(MONTH_OF_YEAR).toString,
      FieldNames.endDateYear    -> dateRange.end.get(YEAR).toString
    )

}

object EditSetController {
  case class EditSetReorder(field: String, direction: String)

  sealed abstract class SubmitAction
  case class Save(record: EditSetRecord, form: EditSetRecordFormValues) extends SubmitAction
  case class CalculateDates(record: EditSetRecord) extends SubmitAction
  case object Discard extends SubmitAction
  case class AddAnotherCreator(editSetRecord: EditSetRecord) extends SubmitAction
  case class RemoveLastCreator(editSetRecord: EditSetRecord) extends SubmitAction

  type Outcome[A] = Either[Error, A]

  sealed abstract class Error
  case class InvalidAction(action: String) extends Error
  case object MissingAction extends Error
  case class RecordNotFound(oci: String) extends Error
  case class FormValidationFailed(forWithErrors: Form[EditSetRecordFormValues], record: EditSetRecord) extends Error

  object FieldNames {
    val background = "background"
    val ccr = "ccr"
    val coveringDates = "covering-dates"
    val creatorIDs = "creator-ids"
    val custodialHistory = "custodial-history"
    val orderDirection = "direction"
    val endDateDay = "end-date-day"
    val endDateMonth = "end-date-month"
    val endDateYear = "end-date-year"
    val endDateFieldError = "end-date-field-error"
    val orderField = "field"
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

  val noSelectionForPlaceOfDeposit = ""
  val fieldKey = "field"
  val orderDirectionKey = "direction"
  val orderDirectionAscending = "ascending"
  val orderDirectionDescending = "descending"
  val offsetKey = "offset"

}
