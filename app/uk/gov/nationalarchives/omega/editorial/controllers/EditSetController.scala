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

import javax.inject._
import play.api.data.Form
import play.api.data.Forms.{ mapping, text }
import play.api.i18n.{ I18nSupport, Lang, Messages }
import play.api.Logger
import play.api.mvc._
import uk.gov.nationalarchives.omega.editorial.controllers.authentication.Secured
import uk.gov.nationalarchives.omega.editorial.models.EditSetRecord
import uk.gov.nationalarchives.omega.editorial.services.CoveringDateCalculator
import uk.gov.nationalarchives.omega.editorial.views.html.{ editSet, editSetRecordEdit, editSetRecordEditDiscard, editSetRecordEditSave }
import uk.gov.nationalarchives.omega.editorial.{ editSetRecords, editSets, _ }

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
      "startDate" -> text,
      "endDate"   -> text
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
          val heading: String = messages("edit-set.record.edit.heading", record.ccr)
          val recordForm = editSetRecordForm.fill(record)
          Ok(editSetRecordEdit(user, title, heading, recordForm))
        case None => NotFound
      }
    }
  }

  def submit(id: String, recordId: String) = Action { implicit request: Request[AnyContent] =>
    withUser { user =>
      val messages: Messages = messagesApi.preferred(request)
      val title: String = messages("edit-set.record.edit.title")
      val heading: String = messages("edit-set.record.edit.heading")
      logger.info(s"The edit set id is $id for record id $recordId")

      editSetRecordForm
        .bindFromRequest()
        .fold(
          formWithErrors => BadRequest(editSetRecordEdit(user, title, heading, formWithErrors)),
          editSetRecord =>
            request.body.asFormUrlEncoded.get("action").headOption match {
              case Some("save") =>
                editSetRecords.saveEditSetRecord(editSetRecord)
                Redirect(controllers.routes.EditSetController.save(id, editSetRecord.oci))
              case Some("discard") => Redirect(controllers.routes.EditSetController.discard(id, recordId))
              // TODO Below added to handle error flow which could be a redirect to an error page pending configuration
              case _ => BadRequest("This action is not allowed")
            }
        )
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

}
