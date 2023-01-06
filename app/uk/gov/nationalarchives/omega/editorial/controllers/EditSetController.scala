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

import play.api.Logger
import play.api.data.Forms.{ mapping, text }
import play.api.data.{ Form, FormError }
import play.api.i18n.{ I18nSupport, Lang }
import play.api.mvc._
import uk.gov.nationalarchives.omega.editorial._
import uk.gov.nationalarchives.omega.editorial.controllers.EditSetController.EditSetReorder
import uk.gov.nationalarchives.omega.editorial.controllers.authentication.Secured
import uk.gov.nationalarchives.omega.editorial.models.{ DateRange, EditSetEntry, EditSetRecord, User }
import uk.gov.nationalarchives.omega.editorial.services.CoveringDateCalculator.getStartAndEndDates
import uk.gov.nationalarchives.omega.editorial.services.CoveringDateError
import uk.gov.nationalarchives.omega.editorial.support.DateParser
import uk.gov.nationalarchives.omega.editorial.forms.EditSetRecordFormValues
import uk.gov.nationalarchives.omega.editorial.forms.EditSetRecordFormValues._
import uk.gov.nationalarchives.omega.editorial.views.html.{ editSet, editSetRecordEdit, editSetRecordEditDiscard, editSetRecordEditSave }

import java.time.LocalDate
import java.time.temporal.ChronoField.{ DAY_OF_MONTH, MONTH_OF_YEAR, YEAR }
import javax.inject._
import uk.gov.nationalarchives.omega.editorial.models.RelatedMaterial

/** This controller creates an `Action` to handle HTTP requests to the application's home page.
  */
@Singleton
class EditSetController @Inject() (
  val messagesControllerComponents: MessagesControllerComponents,
  editSet: editSet,
  editSetRecordEdit: editSetRecordEdit,
  editSetRecordEditDiscard: editSetRecordEditDiscard,
  editSetRecordEditSave: editSetRecordEditSave
) extends MessagesAbstractController(messagesControllerComponents) with I18nSupport with Secured {
  import EditSetController._

  private val logger: Logger = Logger(this.getClass)

  object FieldNames {
    val ccr = "ccr"
    val coveringDates = "coveringDates"
    val orderDirection = "direction"
    val endDate = "endDate"
    val endDateDay = "endDateDay"
    val endDateMonth = "endDateMonth"
    val endDateYear = "endDateYear"
    val orderField = "field"
    val formerReferenceDepartment = "formerReferenceDepartment"
    val legalStatus = "legalStatus"
    val note = "note"
    val oci = "oci"
    val placeOfDeposit = "placeOfDeposit"
    val relatedMaterial = "relatedMaterial"
    val scopeAndContent = "scopeAndContent"
    val startDate = "startDate"
    val startDateDay = "startDateDay"
    val startDateMonth = "startDateMonth"
    val startDateYear = "startDateYear"
  }

  object MessageKeys {
    val buttonDiscard = "edit-set.record.discard.text"
    val buttonSave = "edit-set.record.save.text"
    val coveringDatesMissing = "edit-set.record.missing.covering-dates"
    val coveringDatesTooLong = "edit-set.record.error.covering-dates-too-long"
    val coveringDatesUnparseable = "edit-set.record.error.covering-dates"
    val endDateBeforeStartDate = "edit-set.record.error.end-date-before-start-date"
    val endDateInvalid = "edit-set.record.error.end-date"
    val formerReferenceDepartmentInvalid = "edit-set.record.error.former-reference-department"
    val heading = "edit-set.record.edit.heading"
    val legalStatusMissing = "edit-set.record.error.choose-an-option"
    val noteTooLong = "edit-set.record.error.note-too-long"
    val placeOfDepositMissingOrInvalid = "edit-set.record.error.choose-an-option"
    val scopeAndContentInvalid = "edit-set.record.error.scope-and-content"
    val scopeAndContentMissing = "edit-set.record.missing.scope-and-content"
    val startDateInvalid = "edit-set.record.error.start-date"
    val title = "edit-set.record.edit.title"
  }

  private val noSelectionForPlaceOfDeposit = ""
  private val orderDirectionAscending = "ascending"
  private val orderDirectionDescending = "descending"

  type FormTransformer = Form[EditSetRecordFormValues] => Form[EditSetRecordFormValues]

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
      FieldNames.startDateDay   -> text,
      FieldNames.startDateMonth -> text,
      FieldNames.startDateYear  -> text,
      FieldNames.endDateDay     -> text,
      FieldNames.endDateMonth   -> text,
      FieldNames.endDateYear    -> text,
      FieldNames.legalStatus -> text
        .verifying(
          resolvedMessage(MessageKeys.legalStatusMissing),
          _.trim.nonEmpty
        ),
      FieldNames.placeOfDeposit -> text
        .verifying(
          resolvedMessage(MessageKeys.placeOfDepositMissingOrInvalid),
          _.trim.nonEmpty
        ),
      FieldNames.note -> text
        .verifying(
          resolvedMessage(MessageKeys.noteTooLong),
          value => value.length <= 1000
        )
    )(EditSetRecordFormValues.apply)(EditSetRecordFormValues.unapply)
  )

  private val reorderForm: Form[EditSetReorder] = Form(
    mapping(
      FieldNames.orderField -> text
        .verifying(proposed =>
          Seq(FieldNames.ccr, FieldNames.scopeAndContent, FieldNames.coveringDates).contains(proposed)
        ),
      FieldNames.orderDirection -> text
        .verifying(proposed => Seq(orderDirectionAscending, orderDirectionDescending).contains(proposed))
    )(EditSetReorder.apply)(EditSetReorder.unapply)
  )

  private val fallbackEditSetReorder = EditSetReorder(FieldNames.ccr, orderDirectionAscending)

  /** Create an Action for the edit set page.
    *
    * The configuration in the `routes` file means that this method will be called when the application receives a `GET`
    * request with a path of `/edit-set/{id}`.
    */
  def view(id: String): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    withUser { user =>
      generateEditSetView(id, user, fallbackEditSetReorder)
    }
  }

  def viewAfterReordering(id: String): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    withUser { user =>
      generateEditSetView(id, user, formToEither(reorderForm.bindFromRequest()).getOrElse(fallbackEditSetReorder))
    }
  }

  private def generateEditSetView(id: String, user: User, editSetReorder: EditSetReorder)(implicit
    request: Request[AnyContent]
  ): Result = {
    logger.info(s"The edit set id is $id ")
    val currentEditSet = editSets.getEditSet()
    val title = resolvedMessage("edit-set.title")
    val heading: String = resolvedMessage("edit-set.heading", currentEditSet.name)
    val editSetEntries = currentEditSet.entries.sorted(getSorter(editSetReorder))
    Ok(editSet(user, title, heading, editSetEntries, reorderForm.fill(editSetReorder)))
  }

  private def getSorter(editSetReorder: EditSetReorder): Ordering[EditSetEntry] = {
    val fieldOrdering: Ordering[EditSetEntry] = editSetReorder.field match {
      case FieldNames.ccr             => Ordering.by(_.ccr)
      case FieldNames.scopeAndContent => Ordering.by(_.scopeAndContent)
      case FieldNames.coveringDates   => Ordering.by(_.coveringDates)
      case _                          => Ordering.by(_.ccr)
    }
    if (editSetReorder.direction == orderDirectionDescending) fieldOrdering.reverse else fieldOrdering
  }

  /** Create an Action for the edit set record edit page.
    *
    * The configuration in the `routes` file means that this method will be called when the application receives a `GET`
    * request with a path of `/edit-set/{id}/record/{recordId}/edit`.
    */
  def editRecord(id: String, recordId: String): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    withUser { user =>
      logger.info(s"The edit set id is $id for record id $recordId")
      val editSetName = editSets.getEditSet().name
      editSetRecords.getEditSetRecordByOCI(recordId) match {
        case Some(record) =>
          val title: String = resolvedMessage(MessageKeys.title)
          val recordForm = correctBeforePresenting(editSetRecordForm.fill(populateForm(record)))
          Ok(
            editSetRecordEdit(
              user,
              editSetName,
              title,
              record,
              legalStatus.getLegalStatusReferenceData(),
              corporateBodies.all,
              recordForm
            )
          )
        case None => NotFound
      }
    }
  }

  def submit(id: String, oci: String): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    withUser { user =>
      val title: String = resolvedMessage(MessageKeys.title)
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

        case Right(Discard) =>
          Redirect(controllers.routes.EditSetController.discard(id, oci))

        case Right(CalculateDates(record)) =>
          calculateDates(user, title, record)

        case Left(FormValidationFailed(formWithErrors, record)) =>
          BadRequest(
            editSetRecordEdit(
              user,
              editSetName,
              title,
              record,
              legalStatus.getLegalStatusReferenceData(),
              corporateBodies.all,
              formWithErrors
            )
          )

        case Left(RecordNotFound(missingOci)) =>
          BadRequest(s"Record with $missingOci not found")

        case Left(InvalidAction(_)) =>
          BadRequest("This action is not allowed")

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

  private def formToEither[A](form: Form[A]): Either[Form[A], A] =
    form.fold(Left.apply, Right.apply)

  private def performAdditionalValidation(originalForm: Form[EditSetRecordFormValues]): Form[EditSetRecordFormValues] =
    Seq[FormTransformer](validateStartAndEndDates, validatePlaceOfDeposit).foldLeft(originalForm)((form, transformer) =>
      transformer(form)
    )

  private def validateStartAndEndDates(form: Form[EditSetRecordFormValues]): Form[EditSetRecordFormValues] = {
    val errorForInvalidStartDate =
      FormError(FieldNames.startDate, resolvedMessage(MessageKeys.startDateInvalid))
    val errorForInvalidEndDate = FormError(FieldNames.endDate, resolvedMessage(MessageKeys.endDateInvalid))
    val errorForEndDateBeforeStartDate =
      FormError(FieldNames.endDate, resolvedMessage(MessageKeys.endDateBeforeStartDate))
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
        data = form.data ++ Map(FieldNames.placeOfDeposit -> noSelectionForPlaceOfDeposit),
        errors = form.errors.filter(_.key != FieldNames.placeOfDeposit) ++ Seq(
          FormError(FieldNames.placeOfDeposit, resolvedMessage(MessageKeys.placeOfDepositMissingOrInvalid))
        )
      )
    form.data.get(FieldNames.placeOfDeposit) match {
      case Some(`noSelectionForPlaceOfDeposit`)                                => form
      case Some(placeOfDeposit) if !isPlaceOfDepositRecognised(placeOfDeposit) => formWhenValueAbsentOrUnrecognised
      case None                                                                => formWhenValueAbsentOrUnrecognised
      case _                                                                   => form
    }
  }

  private def findRecord(oci: String): Outcome[EditSetRecord] =
    editSetRecords.getEditSetRecordByOCI(oci).toRight(RecordNotFound(oci))

  private def getSubmitAction(
    record: EditSetRecord
  )(implicit request: Request[AnyContent]): Outcome[SubmitAction] =
    request.body.asFormUrlEncoded.get("action").headOption match {
      case Some("save") =>
        validateForm(record).map { form =>
          Save(record, form)
        }
      case Some("discard")        => Right(Discard)
      case Some("calculateDates") => Right(CalculateDates(record))
      case Some(badAction)        => Left(InvalidAction(badAction))
      case None                   => Left(MissingAction)
    }

  private def validateForm(
    record: EditSetRecord
  )(implicit request: Request[AnyContent]): Outcome[EditSetRecordFormValues] =
    formToEither(performAdditionalValidation(editSetRecordForm.bindFromRequest())).left.map { badForm =>
      FormValidationFailed(badForm, record)
    }

  private def isPlaceOfDepositRecognised(placeOfDeposit: String): Boolean =
    corporateBodies.all.map(_.id).contains(placeOfDeposit)

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

  private def calculateDates(user: User, title: String, record: EditSetRecord)(implicit
    request: Request[AnyContent]
  ): Result = {
    val originalForm: Form[EditSetRecordFormValues] = editSetRecordForm.bindFromRequest()
    val errorsForCoveringDatesOnly = originalForm.errors(FieldNames.coveringDates)
    val editSetName = editSets.getEditSet().name
    if (errorsForCoveringDatesOnly.isEmpty) {
      singleRange(originalForm.data.getOrElse(FieldNames.coveringDates, "")) match {
        case Right(singleDateRangeOpt) =>
          Ok(
            editSetRecordEdit(
              user,
              editSetName,
              title,
              record,
              legalStatus.getLegalStatusReferenceData(),
              corporateBodies.all,
              formWithUpdatedDateFields(originalForm, singleDateRangeOpt)
            )
          )
        case Left(_) =>
          BadRequest(
            editSetRecordEdit(
              user,
              editSetName,
              title,
              record,
              legalStatus.getLegalStatusReferenceData(),
              corporateBodies.all,
              formAfterCoveringDatesParseError(originalForm)
            )
          )
      }
    } else {
      BadRequest(
        editSetRecordEdit(
          user,
          editSetName,
          title,
          record,
          legalStatus.getLegalStatusReferenceData(),
          corporateBodies.all,
          originalForm.copy(errors = errorsForCoveringDatesOnly)
        )
      )
    }
  }

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

  private def correctBeforePresenting(form: Form[EditSetRecordFormValues]): Form[EditSetRecordFormValues] = {
    val correctedPlaceOfDeposit =
      form.data.get(FieldNames.placeOfDeposit).filter(isPlaceOfDepositRecognised).getOrElse("")
    form.copy(data = form.data ++ Map(FieldNames.placeOfDeposit -> correctedPlaceOfDeposit))
  }

}

object EditSetController {
  case class EditSetReorder(field: String, direction: String)

  sealed abstract class SubmitAction
  case class Save(record: EditSetRecord, form: EditSetRecordFormValues) extends SubmitAction
  case class CalculateDates(record: EditSetRecord) extends SubmitAction
  case object Discard extends SubmitAction

  type Outcome[A] = Either[Error, A]

  sealed abstract class Error
  case class InvalidAction(action: String) extends Error
  case object MissingAction extends Error
  case class RecordNotFound(oci: String) extends Error
  case class FormValidationFailed(forWithErrors: Form[EditSetRecordFormValues], record: EditSetRecord) extends Error

}
