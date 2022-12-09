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

package controllers

import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.test.Helpers._
import play.api.test._
import uk.gov.nationalarchives.omega.editorial.controllers.{ LogoutController, SessionKeys }
import uk.gov.nationalarchives.omega.editorial.models.session.Session

class LogoutControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {

  val validSessionToken: String = Session.generateToken("1234")
  val loginPagePath: String = "/login"

  "LogoutController GET" should {
    "redirect to the login page" when {
      "when logged in" in {

        val controller = inject[LogoutController]
        val result = controller
          .logout()
          .apply(FakeRequest().withSession(SessionKeys.token -> validSessionToken))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(loginPagePath)
        session(result).get(SessionKeys.token) mustBe empty

      }
      "when already logged out" in {

        val controller: LogoutController = inject[LogoutController]
        val result = controller
          .logout()
          .apply(FakeRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(loginPagePath)
        session(result).get(SessionKeys.token) mustBe empty

      }

    }

  }

}
