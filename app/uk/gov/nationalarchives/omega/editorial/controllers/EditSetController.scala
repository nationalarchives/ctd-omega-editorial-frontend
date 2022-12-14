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
import play.api.data.Forms.{mapping, text}
import play.api.data.{Form, FormError}
import play.api.i18n.{I18nSupport, Lang, Messages}
import play.api.mvc._
import uk.gov.nationalarchives.omega.editorial._
import javax.inject._
import play.api.data.Form
import play.api.data.Forms.{ mapping, text }
import play.api.i18n.{ I18nSupport, Lang, Messages }
import play.api.Logger
import play.api.mvc._
import uk.gov.nationalarchives.omega.editorial.controllers.authentication.Secured
import uk.gov.nationalarchives.omega.editorial.models.EditSetRecord
import uk.gov.nationalarchives.omega.editorial.views.html.{editSet, editSetRecordEdit, editSetRecordEditDiscard, editSetRecordEditSave}
import uk.gov.nationalarchives.omega.editorial.models.EditSetRecord
import uk.gov.nationalarchives.omega.editorial.services.CoveringDateCalculator
import uk.gov.nationalarchives.omega.editorial.views.html.{ editSet, editSetRecordEdit, editSetRecordEditDiscard, editSetRecordEditSave }
import uk.gov.nationalarchives.omega.editorial.{ editSetRecords, editSets, _ }

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject._
import scala.util.Try

/** This controller creates an `Action` to handle HTTP requests to the application's home page.
 */
@Singleton
class EditSetController @Inject()(
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
          messagesApi("edit-set.record.error.scope-and-content")(Lang.apply("en")),
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
        messagesApi("edit-set.record.error.former-reference-department")(Lang.apply("en")),
        value => value.length <= 255
      ),
      "startDateDay" -> text,
      "startDateMonth" -> text,
      "startDateYear" -> text,
      "endDateDay" -> text,
      "endDateMonth" -> text,
      "endDateYear" -> text
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
          Ok(editSetRecordEdit(user, title, recordForm))
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
          formToEither(editSetRecordForm.bindFromRequest()) match {
            case Left(formWithErrors) => BadRequest(editSetRecordEdit(user, title, formWithErrors))
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

  private def validateStartAndEndDates(form: Form[EditSetRecord]): Form[EditSetRecord] = {
    val errorForInvalidStartDate = FormError("startDate", messagesApi("edit-set.record.error.start-date")(Lang.apply("en")))
    val errorForInvalidEndDate = FormError("endDate", messagesApi("edit-set.record.error.end-date")(Lang.apply("en")))
    val errorForEndDateBeforeStartDate = FormError("endDate", messagesApi("edit-set.record.error.end-date-before-start-date")(Lang.apply("en")))
    val formValues = form.data
    val additionErrors = (extractStartDate(formValues), extractEndDate(formValues)) match {
      case (Some(startDate), Some(endDate)) if !endDate.isBefore(startDate) => Seq.empty
      case (Some(startDate), Some(endDate)) if endDate.isBefore(startDate) => Seq(errorForEndDateBeforeStartDate)
      case (Some(_), None) => Seq(errorForInvalidEndDate)
      case (None, Some(_)) => Seq(errorForInvalidStartDate)
      case (None, None) => Seq(errorForInvalidStartDate, errorForInvalidEndDate)
    }
    form.copy(errors = form.errors ++ additionErrors)
  }

  private def extractStartDate(formValues: Map[String, String]): Option[LocalDate] =
    extractDate(formValues.getOrElse("startDateDay", ""), formValues.getOrElse("startDateMonth", ""), formValues.getOrElse("startDateYear", ""))

  private def extractEndDate(formValues: Map[String, String]): Option[LocalDate] =
    extractDate(formValues.getOrElse("endDateDay", ""), formValues.getOrElse("endDateMonth", ""), formValues.getOrElse("endDateYear", ""))

  private def extractDate(day: String, month: String, year: String): Option[LocalDate] = {
    Try(
      LocalDate.parse(
        List(day, month, year).mkString("/"),
        DateTimeFormatter.ofPattern("d/M/yyyy")))
      .toOption
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

}
