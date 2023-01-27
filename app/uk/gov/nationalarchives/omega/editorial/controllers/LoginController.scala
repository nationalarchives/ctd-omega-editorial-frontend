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

import play.api.data._
import play.api.i18n._
import play.api.mvc._
import uk.gov.nationalarchives.omega.editorial.forms.CredentialsFormProvider
import uk.gov.nationalarchives.omega.editorial.forms.CredentialsFormProvider.FieldNames._
import uk.gov.nationalarchives.omega.editorial.forms.CredentialsFormProvider.MessageKeys._
import uk.gov.nationalarchives.omega.editorial.models.Credentials
import uk.gov.nationalarchives.omega.editorial.models.session.Session
import uk.gov.nationalarchives.omega.editorial.support.{ FormSupport, MessageSupport }
import uk.gov.nationalarchives.omega.editorial.views.html.login

import javax.inject._

/** This controller creates an `Action` to handle HTTP requests to the application's home page.
  */
@Singleton
class LoginController @Inject() (
  val messagesControllerComponents: MessagesControllerComponents,
  login: login
) extends MessagesAbstractController(messagesControllerComponents) with I18nSupport with MessageSupport
    with FormSupport {

  private val fixedEditSetId: String = "1"

  /** Create an Action for the login page.
    *
    * The configuration in the `routes` file means that this method will be called when the application receives a `GET`
    * request with a path of `/login`.
    */
  def view(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Ok(login(CredentialsFormProvider()))
  }

  def submit(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    formToEither(CredentialsFormProvider().bindFromRequest()) match {
      case Right(credentials) =>
        Redirect(routes.EditSetController.view(fixedEditSetId))
          .withSession(request.session + (SessionKeys.token -> Session.generateToken(credentials.username)))
      case Left(formWithErrors) => BadRequest(login(correct(formWithErrors)))
    }
  }

  private def correct(form: Form[Credentials])(implicit request: Request[AnyContent]): Form[Credentials] = {
    val hasAuthenticationError = form.hasGlobalErrors
    if (hasAuthenticationError)
      form.copy(
        data = form.data.removedAll(Seq(username, password)),
        errors = Seq(FormError(username, resolveMessage(authenticationError)))
      )
    else form.copy(data = form.data.removedAll(Seq(password)))
  }

}
