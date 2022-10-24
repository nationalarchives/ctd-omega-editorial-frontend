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
import play.api.mvc.{ AnyContentAsEmpty, DefaultActionBuilder, DefaultMessagesActionBuilderImpl, DefaultMessagesControllerComponents, MessagesControllerComponents }
import play.api.test._
import play.api.test.Helpers._
import play.i18n.MessagesApi
import uk.gov.nationalarchives.omega.editorial.controllers.HomeController
import uk.gov.nationalarchives.omega.editorial.models.session.Session

/** Add your spec here.
  * You can mock out a whole application including requests, plugins etc.
  *
  * For more information, see https://www.playframework.com/documentation/latest/ScalaTestingWithScalaTest
  */
class HomeControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {

  val validSessionToken = Session.generateToken("1234")
  val invalidSessionToken = Session.generateToken("invalid-user")
  val landingPagePath = "/edit-set/1"
  val loginPagePath = "/login"

  "HomeController GET" should {

    "render the index page from a new instance of controller" in {
      val messages: Map[String, Map[String, String]] = Map("en" -> Map("index.heading" -> "Welcome to the Catalogue"))
      val mockMessagesApi = stubMessagesApi(messages)
      val stub = stubControllerComponents()
      val controller = new HomeController(
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
      val home = controller
        .index()
        .apply(
          FakeRequest(GET, "/")
            .withSession("sessionToken" -> validSessionToken)
        )

      status(home) mustBe SEE_OTHER
      redirectLocation(home) mustBe Some(landingPagePath)
    }

    "render the index page from the application" in {
      val controller = inject[HomeController]
      val home = controller
        .index()
        .apply(
          FakeRequest(GET, "/")
            .withSession("sessionToken" -> validSessionToken)
        )

      status(home) mustBe SEE_OTHER
      redirectLocation(home) mustBe Some(landingPagePath)
    }

    "render the index page from the router" in {
      val request = FakeRequest(GET, "/").withSession("sessionToken" -> validSessionToken)
      val home = route(app, request).get

      status(home) mustBe SEE_OTHER
      redirectLocation(home) mustBe Some(landingPagePath)
    }
  }

  "redirect to the login page from the application when requested with invalid session token" in {
    val controller = inject[HomeController]
    val home = controller
      .index()
      .apply(
        FakeRequest(GET, "/")
          .withSession("sessionToken" -> invalidSessionToken)
      )

    status(home) mustBe SEE_OTHER
    redirectLocation(home) mustBe Some(loginPagePath)
  }

  "redirect to the login page from the router when requested with invalid session token" in {
    val request = FakeRequest(GET, "/").withSession("sessionToken" -> invalidSessionToken)
    val home = route(app, request).get

    status(home) mustBe SEE_OTHER
    redirectLocation(home) mustBe Some(loginPagePath)
  }

}
