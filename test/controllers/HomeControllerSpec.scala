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

import play.api.test._
import play.api.test.Helpers._
import uk.gov.nationalarchives.omega.editorial.controllers.{ HomeController, SessionKeys }
import support.BaseControllerSpec

/** Add your spec here. You can mock out a whole application including requests, plugins etc.
  *
  * For more information, see https://www.playframework.com/documentation/latest/ScalaTestingWithScalaTest
  */
class HomeControllerSpec extends BaseControllerSpec {

  val landingPagePath = "/edit-set/1"
  val loginPagePath = "/login"

  "HomeController GET" should {

      "render the index page from a new instance of controller" in {
        val controller = new HomeController(
          Helpers.stubMessagesControllerComponents()
        )
        val home = controller
          .index()
          .apply(
            FakeRequest(GET, "/")
              .withSession(SessionKeys.token -> validSessionToken)
          )

        status(home) mustBe SEE_OTHER
        redirectLocation(home) mustBe Some(landingPagePath)
      }

      "redirect to the login page from the application when requested with invalid session token" in {
        val controller = new HomeController(
          Helpers.stubMessagesControllerComponents()
        )
        val home = controller
          .index()
          .apply(
            FakeRequest(GET, "/")
              .withSession(SessionKeys.token -> invalidSessionToken)
          )

        status(home) mustBe SEE_OTHER
        redirectLocation(home) mustBe Some(loginPagePath)
      }

  }

}
