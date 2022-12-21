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
import play.api.i18n.{ I18nSupport, Lang, Messages }
import play.api.mvc._
import uk.gov.nationalarchives.omega.editorial._
import uk.gov.nationalarchives.omega.editorial.controllers.authentication.Secured
import uk.gov.nationalarchives.omega.editorial.models.{ DateRange, EditSetRecord, User }
import uk.gov.nationalarchives.omega.editorial.services.CoveringDateCalculator.getStartAndEndDates
import uk.gov.nationalarchives.omega.editorial.services.CoveringDateError
import uk.gov.nationalarchives.omega.editorial.support.DateParser
import uk.gov.nationalarchives.omega.editorial.views.html.{ editSet, editSetRecordEdit, editSetRecordEditDiscard, editSetRecordEditSave }

import java.time.LocalDate
import java.time.temporal.ChronoField.{ DAY_OF_MONTH, MONTH_OF_YEAR, YEAR }
import javax.inject._

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

  private val logger: Logger = Logger(this.getClass)

  object FieldNames {
    val ccr = "ccr"
    val coveringDates = "coveringDates"
    val endDateDay = "endDateDay"
    val endDateMonth = "endDateMonth"
    val endDateYear = "endDateYear"
    val formerReferenceDepartment = "formerReferenceDepartment"
    val oci = "oci"
    val scopeAndContent = "scopeAndContent"
    val startDateDay = "startDateDay"
    val startDateMonth = "startDateMonth"
    val startDateYear = "startDateYear"
    val legalStatus = "legalStatus"
  }

  var editSetRecordForm: Form[EditSetRecord] = Form(
    mapping(
      FieldNames.ccr -> text,
      FieldNames.oci -> text,
      FieldNames.scopeAndContent -> text
        .verifying(
          resolvedMessage("edit-set.record.error.scope-and-content"),
          value => value.length <= 8000
        )
        .verifying(resolvedMessage("edit-set.record.missing.scope-and-content"), _.nonEmpty),
      FieldNames.coveringDates -> text
        .verifying(
          resolvedMessage("edit-set.record.missing.covering-dates"),
          _.trim.nonEmpty
        )
        .verifying(
          resolvedMessage("edit-set.record.error.covering-dates-too-long"),
          _.length <= 255
        )
        .verifying(
          resolvedMessage("edit-set.record.error.covering-dates"),
          value => getStartAndEndDates(value).isRight
        ),
      FieldNames.formerReferenceDepartment -> text.verifying(
        resolvedMessage("edit-set.record.error.former-reference-department"),
        value => value.length <= 255
      ),
      FieldNames.startDateDay   -> text,
      FieldNames.startDateMonth -> text,
      FieldNames.startDateYear  -> text,
      FieldNames.endDateDay     -> text,
      FieldNames.endDateMonth   -> text,
      FieldNames.endDateYear    -> text,
      FieldNames.legalStatus -> text.verifying(
        messagesApi("edit-set.record.error.legal-status")(Lang.apply("en")),
        value => !value.equalsIgnoreCase("ref.0")
      )
    )(EditSetRecord.apply)(EditSetRecord.unapply)
  )

  /** Create an Action for the edit set page.
    *
    * The configuration in the `routes` file means that this method will be called when the application receives a `GET`
    * request with a path of `/edit-set/{id}`.
    */
  def view(id: String): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    withUser { user =>
      logger.info(s"The edit set id is $id ")
      val editSetModels = editSets.getEditSet()
      val messages: Messages = request.messages
      val title: String = messages("edit-set.title")
      val heading: String = messages("edit-set.heading", editSetModels.name)
      Ok(editSet(user, title, heading, editSetModels))
    }
  }

  /** Create an Action for the edit set record edit page.
    *
    * The configuration in the `routes` file means that this method will be called when the application receives a `GET`
    * request with a path of `/edit-set/{id}/record/{recordId}/edit`.
    */
  def editRecord(id: String, recordId: String): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    withUser { user =>
      logger.info(s"The edit set id is $id for record id $recordId")
      editSetRecords.getEditSetRecordByOCI(recordId) match {
        case Some(record) =>
          val title: String = resolvedMessage("edit-set.record.edit.title")
          val recordForm = editSetRecordForm.fill(record)
          Ok(editSetRecordEdit(user, title, legalStatus.getLegalStatusData(), recordForm))
        case None => NotFound
      }
    }
  }

  def submit(id: String, recordId: String): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    withUser { user =>
      val title: String = resolvedMessage("edit-set.record.edit.title")
      logger.info(s"The edit set id is $id for record id $recordId")

      request.body.asFormUrlEncoded.get("action").headOption match {
        case Some("save") =>
          formToEither(performAdditionalValidation(editSetRecordForm.bindFromRequest())) match {
            case Left(formWithErrors) =>
              BadRequest(editSetRecordEdit(user, title, legalStatus.getLegalStatusData(), formWithErrors))
            case Right(editSetRecord) =>
              editSetRecords.saveEditSetRecord(editSetRecord)
              Redirect(controllers.routes.EditSetController.save(id, editSetRecord.oci))
          }

        case Some("discard") =>
          Redirect(controllers.routes.EditSetController.discard(id, recordId))

        case Some("calculateDates") => calculateDates(user, title)

        // TODO Below added to handle error flow which could be a redirect to an error page pending configuration
        case _ =>
          BadRequest("This action is not allowed")
      }

    }

  }

  def save(id: String, oci: String): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    withUser { user =>
      val title: String = resolvedMessage("edit-set.record.edit.title")
      editSetRecords.getEditSetRecordByOCI(oci).get
      val heading: String =
        resolvedMessage("edit-set.record.edit.heading", editSetRecords.getEditSetRecordByOCI(oci).get.ccr)
      val message: String = resolvedMessage("edit-set.record.save.text")
      logger.info(s"Save changes for record id $oci edit set id $id")

      Ok(editSetRecordEditSave(user, title, heading, oci, message))
    }
  }

  def discard(id: String, oci: String): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    withUser { user =>
      val title: String = resolvedMessage("edit-set.record.edit.title")
      editSetRecords.getEditSetRecordByOCI(oci).get
      val heading: String =
        resolvedMessage("edit-set.record.edit.heading", editSetRecords.getEditSetRecordByOCI(oci).get.ccr)
      val message: String = resolvedMessage("edit-set.record.discard.text")
      logger.info(s"Discard changes for record id $oci edit set id $id ")

      Ok(editSetRecordEditDiscard(user, title, heading, oci, message))
    }
  }

  private def formToEither[A](form: Form[A]): Either[Form[A], A] =
    form.fold(Left.apply, Right.apply)

  private def performAdditionalValidation(form: Form[EditSetRecord]): Form[EditSetRecord] =
    validateStartAndEndDates(form)

  private def validateStartAndEndDates(form: Form[EditSetRecord]): Form[EditSetRecord] = {
    val errorForInvalidStartDate =
      FormError("startDate", resolvedMessage("edit-set.record.error.start-date"))
    val errorForInvalidEndDate = FormError("endDate", resolvedMessage("edit-set.record.error.end-date"))
    val errorForEndDateBeforeStartDate =
      FormError("endDate", resolvedMessage("edit-set.record.error.end-date-before-start-date"))
    val formValues = form.data
    val additionErrors = (extractStartDate(formValues), extractEndDate(formValues)) match {
      case (Some(startDate), Some(endDate)) if endDate.isBefore(startDate) => Seq(errorForEndDateBeforeStartDate)
      case (Some(_), Some(_))                                              => Seq.empty
      case (Some(_), None)                                                 => Seq(errorForInvalidEndDate)
      case (None, Some(_))                                                 => Seq(errorForInvalidStartDate)
      case (None, None) => Seq(errorForInvalidStartDate, errorForInvalidEndDate)
    }
    form.copy(errors = form.errors ++ additionErrors)
  }

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

  private def calculateDates(user: User, title: String)(implicit request: Request[AnyContent]): Result = {
    val originalForm: Form[EditSetRecord] = editSetRecordForm.bindFromRequest()
    val errorsForCoveringDatesOnly = originalForm.errors(FieldNames.coveringDates)
    if (errorsForCoveringDatesOnly.isEmpty) {
      singleRange(originalForm.data.getOrElse(FieldNames.coveringDates, "")) match {
        case Right(singleDateRangeOpt) =>
          Ok(
            editSetRecordEdit(
              user,
              title,
              legalStatus.getLegalStatusData(),
              formWithUpdatedDateFields(originalForm, singleDateRangeOpt)
            )
          )
        case Left(_) =>
          BadRequest(
            editSetRecordEdit(
              user,
              title,
              legalStatus.getLegalStatusData(),
              formAfterCoveringDatesParseError(originalForm)
            )
          )
      }
    } else {
      BadRequest(
        editSetRecordEdit(
          user,
          title,
          legalStatus.getLegalStatusData(),
          originalForm.copy(errors = errorsForCoveringDatesOnly)
        )
      )
    }
  }

  private def singleRange(rawCoveringDates: String): Either[CoveringDateError, Option[DateRange]] =
    getStartAndEndDates(rawCoveringDates).map(DateRange.single)

  private def formWithUpdatedDateFields(
    originalForm: Form[EditSetRecord],
    dateRangeOpt: Option[DateRange]
  ): Form[EditSetRecord] =
    dateRangeOpt
      .map(dateRange =>
        originalForm.copy(
          data = originalForm.data ++ updatedDateFields(dateRange),
          errors = Seq.empty
        )
      )
      .getOrElse(originalForm)

  private def formAfterCoveringDatesParseError(
    originalForm: Form[EditSetRecord]
  ): Form[EditSetRecord] =
    originalForm.copy(errors =
      Seq(FormError(FieldNames.coveringDates, resolvedMessage("edit-set.record.error.covering-dates")))
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
