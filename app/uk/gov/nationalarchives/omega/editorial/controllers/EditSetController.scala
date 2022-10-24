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
import play.api.i18n.I18nSupport.RequestWithMessagesApi
import play.api.i18n.{ I18nSupport, Lang, Messages }
import play.api.mvc._
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms.{ mapping, nonEmptyText, text }
import uk.gov.nationalarchives.omega.editorial._
import uk.gov.nationalarchives.omega.editorial.controllers.authentication.Secured
import uk.gov.nationalarchives.omega.editorial.models.{ EditSet, EditSetEntry, EditSetRecord }
import uk.gov.nationalarchives.omega.editorial.views.html.{ editSetRecordEdit, editSetRecordEditDiscard, editSetRecordEditSave }

/** This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class EditSetController @Inject() (
  val messagesControllerComponents: MessagesControllerComponents,
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
      "coveringDates" -> text,
      "formerReferenceDepartment" -> text.verifying(
        messagesApi("edit-set.record.error.former-reference-department")(Lang.apply("en")),
        value => value.length <= 255
      ),
      "startDate" -> text,
      "endDate"   -> text
    )(EditSetRecord.apply)(EditSetRecord.unapply)
  )
  //Set default values
  editSetRecordForm = editSetRecordForm.fill(
    new EditSetRecord(
      "COAL 80/80/1",
      "COAL.2022.V5RJW.P Physical Record",
      "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
      "1960",
      "",
      "",
      ""
    )
  );

  /** Create an Action for the edit set page.
    *
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/edit-set/{id}`.
    */
  def view(id: String) = Action { implicit request: Request[AnyContent] =>
    withUser { _ =>
      logger.info(s"The edit set id is $id ")
      val editSet = getEditSet(id)
      val messages: Messages = request.messages
      val title: String = messages("edit-set.title")
      val heading: String = messages("edit-set.heading", editSet.name)
      Ok(views.html.editSet(title, heading, editSet))
    }
  }

  def getEditSet(id: String): EditSet = {

    val scopeAndContent = "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths."
    val editSetEntry1 =
      EditSetEntry("COAL 80/80/1", id, scopeAndContent, "1960")
    val editSetEntry2 =
      EditSetEntry("COAL 80/80/2", id, scopeAndContent, "1960")
    val editSetEntry3 =
      EditSetEntry("COAL 80/80/3", id, scopeAndContent, "1960")
    val entries = Seq(editSetEntry1, editSetEntry2, editSetEntry3)
    val editSetName = "COAL 80 Sample"
    EditSet(editSetName, id: String, entries)
  }

  /** Create an Action for the edit set record edit page.
    *
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/edit-set/{id}/record/{recordId}/edit`.
    */
  def editRecord(id: String, recordId: String) = Action { implicit request: Request[AnyContent] =>
    withUser { _ =>
      logger.info(s"The edit set id is $id for record id $recordId")
      val messages: Messages = request.messages
      val title: String = messages("edit-set.record.edit.title")
      val heading: String = messages("edit-set.record.edit.heading")
      Ok(editSetRecordEdit(title, heading, editSetRecordForm))
    }
  }

  def submit(id: String, recordId: String) = Action { implicit request: Request[AnyContent] =>
    withUser { _ =>
      val messages: Messages = messagesApi.preferred(request)
      val title: String = messages("edit-set.record.edit.title")
      val heading: String = messages("edit-set.record.edit.heading")
      logger.info(s"The edit set id is $id for record id $recordId")

      editSetRecordForm
        .bindFromRequest()
        .fold(
          formWithErrors => BadRequest(editSetRecordEdit(title, heading, formWithErrors)),
          _ =>
            request.body.asFormUrlEncoded.get("action").headOption match {
              case Some("save")    => Redirect(controllers.routes.EditSetController.save(id, recordId))
              case Some("discard") => Redirect(controllers.routes.EditSetController.discard(id, recordId))
              //TODO Below added to handle error flow which could be a redirect to an error page pending configuration
              case _ => BadRequest("This action is not allowed")
            }
        )
    }
  }

  def save(id: String, recordId: String) = Action { implicit request: Request[AnyContent] =>
    withUser { _ =>
      val messages: Messages = messagesApi.preferred(request)
      val title: String = messages("edit-set.record.edit.title")
      val heading: String = messages("edit-set.record.edit.heading")
      val message: String = messages("edit-set.record.save.text")
      logger.info(s"Save changes for record id $recordId edit set id $id")

      Ok(editSetRecordEditSave(title, heading, message))
    }
  }

  def discard(id: String, recordId: String) = Action { implicit request: Request[AnyContent] =>
    withUser { _ =>
      val messages: Messages = messagesApi.preferred(request)
      val title: String = messages("edit-set.record.edit.title")
      val heading: String = messages("edit-set.record.edit.heading")
      val message: String = messages("edit-set.record.discard.text")
      logger.info(s"Discard changes for record id $recordId edit set id $id ")

      Ok(editSetRecordEditDiscard(title, heading, message))
    }
  }

}
