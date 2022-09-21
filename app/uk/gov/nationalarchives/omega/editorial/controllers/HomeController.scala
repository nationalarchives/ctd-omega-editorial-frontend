package uk.gov.nationalarchives.omega.editorial.controllers

import javax.inject._
import play.api._
import play.api.i18n.I18nSupport.RequestWithMessagesApi
import play.api.i18n.Messages
import play.api.mvc._
import uk.gov.nationalarchives.omega.editorial._

/** This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class HomeController @Inject() (val messagesControllerComponents: MessagesControllerComponents)
    extends MessagesAbstractController(messagesControllerComponents) {

  /** Create an Action to render an HTML page.
    *
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/`.
    */
  def index() = Action { implicit request: Request[AnyContent] =>
    val messages: Messages = request.messages
    val title: String = messages("index.title")
    val heading: String = messages("index.heading")
    Ok(views.html.index(title, heading))
  }
}
