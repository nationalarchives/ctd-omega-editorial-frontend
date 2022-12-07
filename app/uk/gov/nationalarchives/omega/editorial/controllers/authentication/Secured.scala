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

package uk.gov.nationalarchives.omega.editorial.controllers.authentication

import play.api.mvc.Results.Redirect
import play.api.mvc._
import uk.gov.nationalarchives.omega.editorial.controllers.routes
import uk.gov.nationalarchives.omega.editorial.models.{ Credentials, User }
import uk.gov.nationalarchives.omega.editorial.models.session.Session

import java.time.{ LocalDateTime, ZoneOffset }

trait Secured {

  def withUser[T](block: User => Result)(implicit request: Request[AnyContent]): Result =
    extractUser(request)
      .map { credentials =>
        val user = User(credentials.username)
        block(user)
      }
      .getOrElse(Redirect(routes.LoginController.view()))

  private def extractUser(req: RequestHeader): Option[Credentials] =
    req.session
      .get("sessionToken")
      .flatMap(token => Session.getSession(token))
      .filter(_.expiration.isAfter(LocalDateTime.now(ZoneOffset.UTC)))
      .map(_.username)
      .flatMap(Credentials.getUser)
}
