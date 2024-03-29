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

package uk.gov.nationalarchives.omega.editorial.forms

import play.api.data.Form
import play.api.data.Forms.{ mapping, text }
import play.api.i18n.MessagesApi
import play.api.mvc.{ AnyContent, Request }
import uk.gov.nationalarchives.omega.editorial.models.Credentials
import uk.gov.nationalarchives.omega.editorial.support.MessageSupport

object CredentialsFormProvider extends MessageSupport {

  import FieldNames._
  import MessageKeys._

  def apply()(implicit request: Request[AnyContent], messagesApi: MessagesApi): Form[Credentials] = Form(
    mapping(
      username -> text.verifying(resolveMessage(usernameMissing), _.nonEmpty),
      password -> text.verifying(resolveMessage(passwordMissing), _.nonEmpty)
    )(Credentials.apply)(Credentials.unapply)
      verifying (resolveMessage(authenticationError), isValidLogin _)
  )

  private def isValidLogin(credentials: Credentials): Boolean =
    Credentials.getUser(credentials.username).exists(_.password == credentials.password)

  object FieldNames {
    val password = "password"
    val username = "username"
  }

  object MessageKeys {
    val authenticationError = "login.authentication.error"
    val passwordMissing = "login.missing.password"
    val usernameMissing = "login.missing.username"
  }
}
