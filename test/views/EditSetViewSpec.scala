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
import play.api.test.Helpers.{ contentAsString, defaultAwaitTimeout }
import play.test.WithApplication
import play.twirl.api.Html
import uk.gov.nationalarchives.omega.editorial._

class EditSetViewSpec extends PlaySpec {

  "Edit set Html" should {
    "render the given title and heading" in new WithApplication {
      val title = "EditSetTitleTest"
      val heading = "EditSetHeadingTest"

      val editSetHtml: Html = views.html.editSet(title, heading)

      contentAsString(editSetHtml) must include(title)
      contentAsString(editSetHtml) must include(heading)
    }

  }
}
