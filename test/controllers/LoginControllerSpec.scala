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

import play.api.http.Status.{ BAD_REQUEST, OK, SEE_OTHER }
import play.api.test.Helpers.{ defaultAwaitTimeout, redirectLocation, session, status }
import play.api.test.{ CSRFTokenHelper, FakeRequest, Helpers }
import support.BaseControllerSpec
import uk.gov.hmrc.govukfrontend.views.html.components.{ GovukButton, GovukErrorSummary, GovukFieldset }
import uk.gov.nationalarchives.omega.editorial.controllers.{ LoginController, SessionKeys }
import uk.gov.nationalarchives.omega.editorial.views.html.login

class LoginControllerSpec extends BaseControllerSpec {

  "LoginController GET" should {

    "render the login page from a new instance of controller" in {

      val response = getLoginController.view().apply(CSRFTokenHelper.addCSRFToken(FakeRequest()))

      status(response) mustBe OK
    }

  }

  "LoginController POST" should {
    "redirect to result page given valid username and password from a new instance of controller" in {

      val response = getLoginController
        .submit()
        .apply(
          CSRFTokenHelper
            .addCSRFToken(
              FakeRequest().withFormUrlEncodedBody("username" -> "1234", "password" -> "1234")
            )
        )
      status(response) mustBe SEE_OTHER
      redirectLocation(response) mustBe Some(landingPagePath)
      session(response).get(SessionKeys.token) must not be empty
    }

    "render the login page given valid username and invalid password from a new instance of controller" in {

      val response = getLoginController
        .submit()
        .apply(
          CSRFTokenHelper
            .addCSRFToken(
              FakeRequest().withFormUrlEncodedBody("username" -> "1234", "password" -> "12345")
            )
        )
      status(response) mustBe BAD_REQUEST
      session(response).get(SessionKeys.token) mustBe empty
    }

    "render the login page given an invalid username and valid password from a new instance of controller" in {

      val response = getLoginController
        .submit()
        .apply(
          CSRFTokenHelper
            .addCSRFToken(
              FakeRequest().withFormUrlEncodedBody("username" -> "123", "password" -> "1234")
            )
        )
      status(response) mustBe BAD_REQUEST
      session(response).get(SessionKeys.token) mustBe empty
    }

    "render the login page given an invalid username and password from a new instance of controller" in {

      val response = getLoginController
        .submit()
        .apply(
          CSRFTokenHelper
            .addCSRFToken(
              FakeRequest().withFormUrlEncodedBody("username" -> "", "password" -> "1233")
            )
        )
      status(response) mustBe BAD_REQUEST
      session(response).get(SessionKeys.token) mustBe empty
    }

  }

  def getLoginController: LoginController = {
    val login = new login(new GovukFieldset, new GovukButton, new GovukErrorSummary)
    new LoginController(
      Helpers.stubMessagesControllerComponents(),
      login
    )
  }
}
