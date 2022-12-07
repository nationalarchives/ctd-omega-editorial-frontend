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

import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.test.Helpers
import play.api.test.Helpers.{ contentAsString, defaultAwaitTimeout }
import play.twirl.api.Html
import support.BaseSpec
import support.CustomMatchers.{ haveHeaderTitle, haveVisibleSignOutLink }
import uk.gov.nationalarchives.omega.editorial.views.html.editSetRecordEditDiscard

class EditSetRecordEditDiscardSpec extends BaseSpec {

  "Edit set record edit discard Html" should {
    "render the given title and heading with discard changes message" in {
      implicit val messages: Messages = Helpers.stubMessages()

      val editSetRecordEditDiscardInstance = inject[editSetRecordEditDiscard]
      val title = "EditRecordTitleTest"
      val heading = "EditRecordHeadingTest"
      val discardChanges = "Any changes have been discarded. Showing last saved version."
      val oci = "EditRecordOciTest"

      val confirmationEditSetRecordEditHtml: Html =
        editSetRecordEditDiscardInstance(user, title, heading, oci, discardChanges)

      contentAsString(confirmationEditSetRecordEditHtml) must include(title)
      contentAsString(confirmationEditSetRecordEditHtml) must include(heading)
      contentAsString(confirmationEditSetRecordEditHtml) must include(discardChanges)
    }

    "render the header" in {

      val document = generateDocument()

      document must haveHeaderTitle
      document must haveVisibleSignOutLink

    }
  }

  private def generateDocument(): Document = {
    implicit val messages: Messages = Helpers.stubMessages()
    val editSetRecordEditDiscardInstance = inject[editSetRecordEditDiscard]
    asDocument(
      editSetRecordEditDiscardInstance(
        user,
        "EditRecordTitleTest",
        "EditRecordHeadingTest",
        "EditRecordOciTest",
        "Any changes have been discarded. Showing last saved version."
      )
    )
  }

}
