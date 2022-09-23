package uk.gov.nationalarchives.omega.editorial.controllers

import akka.http.scaladsl.model.headers.LinkParams.title

import javax.inject._
import play.api.i18n._
import play.api.mvc._
import uk.gov.nationalarchives.omega.editorial._
import play.api.data._
import play.api.data.Forms._
import uk.gov.nationalarchives.omega.editorial.models.Credentials

/** This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class LoginController @Inject() (val messagesControllerComponents: MessagesControllerComponents, langs: Langs)
    extends MessagesAbstractController(messagesControllerComponents) with play.api.i18n.I18nSupport {

  val credentialsForm: Form[Credentials] = Form(
    mapping(
      "username" -> nonEmptyText,
      "password" -> nonEmptyText
    )(Credentials.apply)(Credentials.unapply)
  )

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

    credentialsForm.bindFromRequest
      .fold(
        formWithErrors => BadRequest(views.html.login(title, heading, formWithErrors)),
        credentials => {
          val username = credentials.username
          val password = credentials.password

          if (username != "1234" || password != "1234") {
            BadRequest(views.html.login(title, heading, credentialsForm))
          } else {
            Redirect(controllers.routes.EditSetController.view("1"))
          }

        }
      )
  }
}
