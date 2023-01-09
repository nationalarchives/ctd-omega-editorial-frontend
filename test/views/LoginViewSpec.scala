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

import play.api.data.{ Form, FormError }
import play.api.i18n.MessagesApi
import play.api.test.Helpers._
import play.api.test.{ CSRFTokenHelper, FakeRequest, Helpers }
import support.BaseSpec
import support.CustomMatchers._
import support.ExpectedValues.ExpectedSummaryErrorMessage
import uk.gov.nationalarchives.omega.editorial.forms.CredentialsFormProvider
import uk.gov.nationalarchives.omega.editorial.models.Credentials
import uk.gov.nationalarchives.omega.editorial.views.html.login

class LoginViewSpec extends BaseSpec {

  private val errorSummaryTitle = "There is a problem"
  val defaultLang = play.api.i18n.Lang.defaultLang.code
  private val messages: Map[String, Map[String, String]] = Map(
    defaultLang -> Map(
      "error.summary.title" -> errorSummaryTitle
    )
  )
  implicit val messagesApi: MessagesApi = stubMessagesApi(messages)

  "Login Html" should {
    "render the given title and heading" in {
      val title = "TitleTest"
      val heading = "HeadingTest"
      val credentialsForm: Form[Credentials] = CredentialsFormProvider()
      val login = inject[login]
      val loginHtml =
        login(title, heading, credentialsForm)(
          Helpers.stubMessages(messagesApi),
          CSRFTokenHelper.addCSRFToken(FakeRequest())
        )

      val document = asDocument(loginHtml)
      document must haveTitle("TitleTest")
      document must haveLegend("HeadingTest")
    }

    "render the header" in {

      val login = inject[login]

      val page = login(title = "TitleTest", heading = "HeadingTest", credentialsForm = CredentialsFormProvider())(
        Helpers.stubMessages(messagesApi),
        CSRFTokenHelper.addCSRFToken(FakeRequest())
      )

      val document = asDocument(page)
      document must haveHeaderTitle("header.title")
      document must not(haveVisibleLogoutLink)

    }

    "render multiple errors when no username and password given" in {
      val title = "TitleTest"
      val heading = "HeadingTest"
      val userForm: Form[Credentials] = CredentialsFormProvider()
        .fill(Credentials.apply("", ""))
        .withError(FormError("username", "Enter a username"))
        .withError(FormError("password", "Enter a password"))
      val login = inject[login]
      val loginHtml =
        login(title, heading, userForm)(Helpers.stubMessages(messagesApi), CSRFTokenHelper.addCSRFToken(FakeRequest()))

      val document = asDocument(loginHtml)
      document must haveSummaryErrorMessages(
        ExpectedSummaryErrorMessage("Enter a username", "#username"),
        ExpectedSummaryErrorMessage("Enter a password", "#password")
      )
      document must haveErrorMessageForUsername("Enter a username")
      document must haveErrorMessageForPassword("Enter a password")
    }

    "render an error given an incorrect username and/or password " in {
      val title = "TitleTest"
      val heading = "HeadingTest"
      val credentialsForm: Form[Credentials] = CredentialsFormProvider()
        .fill(Credentials.apply("11", "22"))
        .withError(FormError("username", "Username and/or password is incorrect."))
      val login = inject[login]
      val loginHtml =
        login(title, heading, credentialsForm)(
          Helpers.stubMessages(messagesApi),
          CSRFTokenHelper.addCSRFToken(FakeRequest())
        )

      val document = asDocument(loginHtml)
      document must haveSummaryErrorTitle(errorSummaryTitle)
      document must haveSummaryErrorMessages(
        ExpectedSummaryErrorMessage("Username and/or password is incorrect.", "#username")
      )
    }

    "render an error when no username given" in {
      val title = "TitleTest"
      val heading = "HeadingTest"
      val credentialsForm: Form[Credentials] = CredentialsFormProvider()
        .fill(Credentials.apply("", ""))
        .withError(FormError("username", "Enter a username"))
      val login = inject[login]
      val loginHtml =
        login(title, heading, credentialsForm)(
          Helpers.stubMessages(messagesApi),
          CSRFTokenHelper.addCSRFToken(FakeRequest())
        )

      val document = asDocument(loginHtml)
      document must haveSummaryErrorTitle(errorSummaryTitle)
      document must haveSummaryErrorMessages(ExpectedSummaryErrorMessage("Enter a username", "#username"))
      document must haveErrorMessageForUsername("Enter a username")
    }

    "render an error when no password given" in {
      val title = "TitleTest"
      val heading = "HeadingTest"
      val credentialsForm: Form[Credentials] = CredentialsFormProvider()
        .fill(Credentials.apply("", ""))
        .withError(FormError("password", "Enter a password"))
      val login = inject[login]
      val loginHtml =
        login(title, heading, credentialsForm)(
          Helpers.stubMessages(messagesApi),
          CSRFTokenHelper.addCSRFToken(FakeRequest())
        )

      val document = asDocument(loginHtml)
      document must haveSummaryErrorTitle(errorSummaryTitle)
      document must haveSummaryErrorMessages(ExpectedSummaryErrorMessage("Enter a password", "#password"))
      document must haveErrorMessageForPassword("Enter a password")
    }
  }

}
