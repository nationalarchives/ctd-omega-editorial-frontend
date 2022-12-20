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
import uk.gov.nationalarchives.omega.editorial.models.{ EditSet, EditSetEntry, EditSetRecord, LegalStatus }
import uk.gov.nationalarchives.omega.editorial.services.CoveringDateCalculator
import uk.gov.nationalarchives.omega.editorial.support.HistoricalDateParser
import uk.gov.nationalarchives.omega.editorial.views.html.{ editSet, editSetRecordEdit, editSetRecordEditDiscard, editSetRecordEditSave }

import java.util.Date
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

  val logger: Logger = Logger(this.getClass())
  val save = "save"
  val discard = "discard"

  var editSetRecordForm: Form[EditSetRecord] = Form(
    mapping(
      "ccr" -> text,
      "oci" -> text,
      "scopeAndContent" -> text
        .verifying(
          message("edit-set.record.error.scope-and-content"),
          value => value.length <= 8000
        )
        .verifying(messagesApi("edit-set.record.missing.scope-and-content")(Lang.apply("en")), _.nonEmpty),
      "coveringDates" -> text
        .verifying(
          messagesApi("edit-set.record.missing.covering-dates")(Lang.apply("en")),
          _.trim.nonEmpty
        )
        .verifying(
          messagesApi("edit-set.record.error.covering-dates-too-long")(Lang.apply("en")),
          _.length <= 255
        )
        .verifying(
          messagesApi("edit-set.record.error.covering-dates")(Lang.apply("en")),
          value => CoveringDateCalculator.getStartAndEndDates(value).isRight
        ),
      "formerReferenceDepartment" -> text.verifying(
        message("edit-set.record.error.former-reference-department"),
        value => value.length <= 255
      ),
      "startDateDay"   -> text,
      "startDateMonth" -> text,
      "startDateYear"  -> text,
      "endDateDay"     -> text,
      "endDateMonth"   -> text,
      "endDateYear"    -> text,
      "legalStatus" -> text
        .verifying(
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
  def view(id: String) = Action { implicit request: Request[AnyContent] =>
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
  def editRecord(id: String, recordId: String) = Action { implicit request: Request[AnyContent] =>
    withUser { user =>
      logger.info(s"The edit set id is $id for record id $recordId")
      editSetRecords.getEditSetRecordByOCI(recordId) match {
        case Some(record) =>
          val messages: Messages = request.messages
          val title: String = messages("edit-set.record.edit.title")
          val recordForm = editSetRecordForm.fill(record)
          Ok(editSetRecordEdit(user, title, legalStatus.getLegalStatusData(), recordForm))
        case None => NotFound
      }
    }
  }

  def submit(id: String, recordId: String) = Action { implicit request: Request[AnyContent] =>
    withUser { user =>
      val messages: Messages = messagesApi.preferred(request)
      val title: String = messages("edit-set.record.edit.title")
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

        // TODO Below added to handle error flow which could be a redirect to an error page pending configuration
        case _ =>
          BadRequest("This action is not allowed")
      }

    }
  }

  def save(id: String, oci: String) = Action { implicit request: Request[AnyContent] =>
    withUser { user =>
      val messages: Messages = messagesApi.preferred(request)
      val title: String = messages("edit-set.record.edit.title")
      editSetRecords.getEditSetRecordByOCI(oci).get
      val heading: String = messages("edit-set.record.edit.heading", editSetRecords.getEditSetRecordByOCI(oci).get.ccr)
      val message: String = messages("edit-set.record.save.text")
      logger.info(s"Save changes for record id $oci edit set id $id")

      Ok(editSetRecordEditSave(user, title, heading, oci, message))
    }
  }

  def discard(id: String, oci: String) = Action { implicit request: Request[AnyContent] =>
    withUser { user =>
      val messages: Messages = messagesApi.preferred(request)
      val title: String = messages("edit-set.record.edit.title")
      editSetRecords.getEditSetRecordByOCI(oci).get
      val heading: String = messages("edit-set.record.edit.heading", editSetRecords.getEditSetRecordByOCI(oci).get.ccr)
      val message: String = messages("edit-set.record.discard.text")
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
      FormError("startDate", message("edit-set.record.error.start-date"))
    val errorForInvalidEndDate = FormError("endDate", message("edit-set.record.error.end-date"))
    val errorForEndDateBeforeStartDate =
      FormError("endDate", message("edit-set.record.error.end-date-before-start-date"))
    val formValues = form.data
    val additionErrors = (extractStartDate(formValues), extractEndDate(formValues)) match {
      case (Some(startDate), Some(endDate)) if endDate.before(startDate) => Seq(errorForEndDateBeforeStartDate)
      case (Some(_), Some(_))                                            => Seq.empty
      case (Some(_), None)                                               => Seq(errorForInvalidEndDate)
      case (None, Some(_))                                               => Seq(errorForInvalidStartDate)
      case (None, None) => Seq(errorForInvalidStartDate, errorForInvalidEndDate)
    }
    form.copy(errors = form.errors ++ additionErrors)
  }

  private def message(key: String): String = messagesApi(key)(Lang("en"))

  private def extractStartDate(formValues: Map[String, String]): Option[Date] =
    extractDate(formValues, "startDateDay", "startDateMonth", "startDateYear")

  private def extractEndDate(formValues: Map[String, String]): Option[Date] =
    extractDate(formValues, "endDateDay", "endDateMonth", "endDateYear")

  private def extractDate(
    formValues: Map[String, String],
    fieldNameForDay: String,
    fieldNameForMonth: String,
    fieldNameForYear: String
  ): Option[Date] =
    for {
      day   <- formValues.get(fieldNameForDay)
      month <- formValues.get(fieldNameForMonth)
      year  <- formValues.get(fieldNameForYear)
      date  <- HistoricalDateParser.parse(List(day, month, year).mkString("/"))
    } yield date

}
