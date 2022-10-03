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

package views

import org.scalatestplus.play.PlaySpec
import play.api.data.{ Form, FormError }
import play.api.test.{ CSRFTokenHelper, FakeRequest, Helpers }
import play.api.test.Helpers._
import play.test.WithApplication
import play.twirl.api.Html
import uk.gov.nationalarchives.omega.editorial._
import uk.gov.nationalarchives.omega.editorial.forms.CredentialsFormProvider
import uk.gov.nationalarchives.omega.editorial.models.Credentials

class LoginViewSpec extends PlaySpec {

  val messages: Map[String, Map[String, String]] = Map.empty
  implicit val messagesApi = stubMessagesApi(messages)

  "Login Html" should {
    "render the given title and heading" in new WithApplication {
      val title = "TitleTest"
      val heading = "HeadingTest"
      val credentialsForm: Form[Credentials] = CredentialsFormProvider()

      val loginHtml: Html =
        views.html
          .login(title, heading, credentialsForm)(Helpers.stubMessages(), CSRFTokenHelper.addCSRFToken(FakeRequest()))

      contentAsString(loginHtml) must include(title)
      contentAsString(loginHtml) must include(heading)
    }

    "render multiple errors when no username and password given" in new WithApplication {
      val title = "TitleTest"
      val heading = "HeadingTest"
      val userForm: Form[Credentials] = CredentialsFormProvider()
        .fill(Credentials.apply("", ""))
        .withError(FormError("username", "Enter a username"))
        .withError(FormError("password", "Enter a password"))

      val loginHtml: Html =
        views.html
          .login(title, heading, userForm)(Helpers.stubMessages(), CSRFTokenHelper.addCSRFToken(FakeRequest()))

      contentAsString(loginHtml) must include("There is a problem")
      contentAsString(loginHtml) must include("Enter a username")
      contentAsString(loginHtml) must include("Enter a password")
    }

    "render an error given an incorrect username and/or password " in new WithApplication {
      val title = "TitleTest"
      val heading = "HeadingTest"
      val credentialsForm: Form[Credentials] = CredentialsFormProvider()
        .fill(Credentials.apply("11", "22"))
        .withError(FormError("", "Username and/or password is incorrect."))

      val loginHtml: Html =
        views.html
          .login(title, heading, credentialsForm)(Helpers.stubMessages(), CSRFTokenHelper.addCSRFToken(FakeRequest()))

      contentAsString(loginHtml) must include("There is a problem")
      contentAsString(loginHtml) must include("Username and/or password is incorrect.")
    }

    "render an error when no username given" in new WithApplication {
      val title = "TitleTest"
      val heading = "HeadingTest"
      val credentialsForm: Form[Credentials] = CredentialsFormProvider()
        .fill(Credentials.apply("", ""))
        .withError(FormError("username", "Enter a username"))

      val loginHtml: Html =
        views.html
          .login(title, heading, credentialsForm)(Helpers.stubMessages(), CSRFTokenHelper.addCSRFToken(FakeRequest()))

      contentAsString(loginHtml) must include("There is a problem")
      contentAsString(loginHtml) must include("Enter a username")
    }

    "render an error when no password given" in new WithApplication {
      val title = "TitleTest"
      val heading = "HeadingTest"
      val credentialsForm: Form[Credentials] = CredentialsFormProvider()
        .fill(Credentials.apply("", ""))
        .withError(FormError("password", "Enter a password"))

      val loginHtml: Html =
        views.html
          .login(title, heading, credentialsForm)(Helpers.stubMessages(), CSRFTokenHelper.addCSRFToken(FakeRequest()))

      contentAsString(loginHtml) must include("There is a problem")
      contentAsString(loginHtml) must include("Enter a password")
    }
  }
}
