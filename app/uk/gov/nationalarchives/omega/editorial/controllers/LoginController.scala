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
import play.api.i18n._
import play.api.mvc._
import play.api.data._
import uk.gov.nationalarchives.omega.editorial.forms.CredentialsFormProvider
import uk.gov.nationalarchives.omega.editorial.models.Credentials
import uk.gov.nationalarchives.omega.editorial.models.dao.SessionDAO
import uk.gov.nationalarchives.omega.editorial.views.html.login

/** This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class LoginController @Inject() (
  val messagesControllerComponents: MessagesControllerComponents,
  login: login
) extends MessagesAbstractController(messagesControllerComponents) with I18nSupport {

  val credentialsForm: Form[Credentials] = CredentialsFormProvider()

  /** Create an Action for the login page.
    *
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/login`.
    */
  def view(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    val messages: Messages = messagesApi.preferred(request)
    val title: String = messages("login.title")
    val heading: String = messages("login.heading")
    Ok(login(title, heading, credentialsForm))
  }

  def submit(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    val messages: Messages = messagesApi.preferred(request)
    val title: String = messages("login.title")
    val heading: String = messages("login.heading")

    credentialsForm
      .bindFromRequest()
      .fold(
        formWithErrors => BadRequest(login(title, heading, formWithErrors)),
        credentials => {
          val token = SessionDAO.generateToken(credentials.username)
          Redirect(routes.EditSetController.view("1")).withSession(request.session + ("sessionToken" -> token))
        }
      )
  }
}
