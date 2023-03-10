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

package support

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.test.Helpers.{ contentAsString, defaultAwaitTimeout }
import play.twirl.api.Content
import uk.gov.hmrc.govukfrontend.views.html.components.{ GovukButton, GovukErrorMessage, GovukErrorSummary, GovukFieldset, GovukHint, GovukInput, GovukLabel, GovukSelect, GovukTextarea }
import uk.gov.nationalarchives.omega.editorial.models.User
import uk.gov.nationalarchives.omega.editorial.views.html.editSetRecordEdit

class BaseViewSpec extends UnitTest {

  val user: User = User("dummy user")
  def asDocument(html: String): Document = Jsoup.parse(html)
  def asDocument(content: Content): Document = asDocument(contentAsString(content))

  def getEditRecordInstance(): editSetRecordEdit =
    new editSetRecordEdit(
      new GovukButton,
      new GovukInput(new GovukErrorMessage, new GovukHint, new GovukLabel),
      new GovukTextarea(new GovukErrorMessage, new GovukHint, new GovukLabel),
      new GovukFieldset,
      new GovukErrorSummary,
      new GovukSelect(new GovukErrorMessage, new GovukHint, new GovukLabel),
      new GovukLabel
    )

}
