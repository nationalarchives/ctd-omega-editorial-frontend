package uk.gov.nationalarchives.omega.editorial.controllers

import javax.inject._
import play.api.i18n.I18nSupport.RequestWithMessagesApi
import play.api.i18n.Messages
import play.api.mvc._
import play.api.Logger
import uk.gov.nationalarchives.omega.editorial._

/** This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class EditSetController @Inject() (val messagesControllerComponents: MessagesControllerComponents)
    extends MessagesAbstractController(messagesControllerComponents) {

  val logger: Logger = Logger(this.getClass())

  /** Create an Action for the edit set page.
    *
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/edit-set/{id}`.
    */
  def view(id: String) = Action { implicit request: Request[AnyContent] =>
    logger.info(s"The edit set id is $id ")
    val messages: Messages = request.messages
    val title: String = messages("edit-set.title")
    val heading: String = messages("edit-set.heading")
    Ok(views.html.editSet(title, heading))
  }

  /** Create an Action for the edit set record edit page.
    *
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/edit-set/{id}/record/{recordId}/edit`.
    */
  def editRecord(id: String, recordId: String) = Action { implicit request: Request[AnyContent] =>
    logger.info(s"The edit set id is $id for record id $recordId")
    val messages: Messages = request.messages
    val title: String = messages("edit-set.record.edit.title")
    val heading: String = messages("edit-set.record.edit.heading")
    Ok(views.html.editSetRecordEdit(title, heading))
  }

}
