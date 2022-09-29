package uk.gov.nationalarchives.omega.editorial.controllers

import javax.inject._
import play.api.i18n._
import play.api.mvc._
import uk.gov.nationalarchives.omega.editorial._
import play.api.data._
import uk.gov.nationalarchives.omega.editorial.forms.CredentialsFormProvider
import uk.gov.nationalarchives.omega.editorial.models.Credentials

/** This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class LoginController @Inject() (val messagesControllerComponents: MessagesControllerComponents)
    extends MessagesAbstractController(messagesControllerComponents) with I18nSupport {

  val credentialsForm: Form[Credentials] = CredentialsFormProvider()

  /** Create an Action for the login page.
    *
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/login`.
    */
  def view() = Action { implicit request: Request[AnyContent] =>
    val messages: Messages = messagesApi.preferred(request)
    val title: String = messages("login.title")
    val heading: String = messages("login.heading")
    Ok(views.html.login(title, heading, credentialsForm))
  }

  def submit() = Action { implicit request: Request[AnyContent] =>
    val messages: Messages = messagesApi.preferred(request)
    val title: String = messages("login.title")
    val heading: String = messages("login.heading")

    credentialsForm
      .bindFromRequest()
      .fold(
        formWithErrors => BadRequest(views.html.login(title, heading, formWithErrors)),
        _ => Redirect(controllers.routes.EditSetController.view("1"))
      )
  }
}
