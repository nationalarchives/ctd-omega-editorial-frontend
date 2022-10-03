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
import play.api.mvc.{ AnyContentAsEmpty, DefaultActionBuilder, DefaultMessagesActionBuilderImpl, DefaultMessagesControllerComponents }
import play.api.test._
import play.api.test.Helpers._
import uk.gov.nationalarchives.omega.editorial.controllers.LoginController

/** Add your spec here.
  * You can mock out a whole application including requests, plugins etc.
  *
  * For more information, see https://www.playframework.com/documentation/latest/ScalaTestingWithScalaTest
  */
class LoginControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {

  "LoginController GET" should {

    "render the login page from a new instance of controller" in {
      val messages: Map[String, Map[String, String]] =
        Map("en" -> Map("login.heading" -> "Sign in: Pan-Archival Catalogue"))
      val mockMessagesApi = stubMessagesApi(messages)
      val stub = stubControllerComponents()
      val controller = new LoginController(
        DefaultMessagesControllerComponents(
          new DefaultMessagesActionBuilderImpl(stubBodyParser(AnyContentAsEmpty), mockMessagesApi)(
            stub.executionContext
          ),
          DefaultActionBuilder(stub.actionBuilder.parser)(stub.executionContext),
          stub.parsers,
          mockMessagesApi,
          stub.langs,
          stub.fileMimeTypes,
          stub.executionContext
        )
      )
      val login = controller.view().apply(CSRFTokenHelper.addCSRFToken(FakeRequest(GET, "/login")))

      status(login) mustBe OK
      contentType(login) mustBe Some("text/html")
      contentAsString(login) must include("Sign in: Pan-Archival Catalogue")
    }

    "render the login page from the application" in {
      val controller = inject[LoginController]
      val login = controller.view().apply(CSRFTokenHelper.addCSRFToken(FakeRequest(GET, "/login")))

      status(login) mustBe OK
      contentType(login) mustBe Some("text/html")
      contentAsString(login) must include("Sign in: Pan-Archival Catalogue")
    }

    "render the login page from the router" in {
      val request = FakeRequest(GET, "/login")
      val login = route(app, request).get

      status(login) mustBe OK
      contentType(login) mustBe Some("text/html")
      contentAsString(login) must include("Sign in: Pan-Archival Catalogue")
    }
  }

  "LoginController POST" should {
    "redirect to result page from a new instance of controller" in {
      val messages: Map[String, Map[String, String]] =
        Map("en" -> Map("login.heading" -> "Sign in: Pan-Archival Catalogue"))
      val mockMessagesApi = stubMessagesApi(messages)
      val stub = stubControllerComponents()
      val controller = new LoginController(
        DefaultMessagesControllerComponents(
          new DefaultMessagesActionBuilderImpl(stubBodyParser(AnyContentAsEmpty), mockMessagesApi)(
            stub.executionContext
          ),
          DefaultActionBuilder(stub.actionBuilder.parser)(stub.executionContext),
          stub.parsers,
          mockMessagesApi,
          stub.langs,
          stub.fileMimeTypes,
          stub.executionContext
        )
      )

      val login = controller
        .submit()
        .apply(
          CSRFTokenHelper
            .addCSRFToken(
              FakeRequest(POST, "/login").withFormUrlEncodedBody("username" -> "1234", "password" -> "1234")
            )
        )
      status(login) mustBe SEE_OTHER
    }

    "redirect to result page of the application" in {
      val controller = inject[LoginController]
      val login = controller
        .submit()
        .apply(
          CSRFTokenHelper.addCSRFToken(
            FakeRequest(POST, "/login").withFormUrlEncodedBody("username" -> "1234", "password" -> "1234")
          )
        )

      status(login) mustBe SEE_OTHER
    }

    "redirect to result page from the router" in {
      val request = CSRFTokenHelper.addCSRFToken(
        FakeRequest(POST, "/login").withFormUrlEncodedBody("username" -> "1234", "password" -> "1234")
      )
      val login = route(app, request).get

      status(login) mustBe SEE_OTHER
    }
  }
}
