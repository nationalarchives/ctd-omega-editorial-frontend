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
import play.twirl.api.Html
import support.BaseSpec
import support.CommonMatchers._
import uk.gov.nationalarchives.omega.editorial.views.html.editSetRecordEditSave
import uk.gov.nationalarchives.omega.editorial.models.PhysicalRecord

class EditSetRecordEditSaveSpec extends BaseSpec {

  "Edit set record edit save Html" should {
    "render the given title and heading with save changes message" in {
      implicit val messages: Messages = Helpers.stubMessages()

      val editSetRecordEditSaveInstance = inject[editSetRecordEditSave]
      val title = "EditRecordTitleTest"
      val editSetName = "COAL 80 Sample"
      val heading = "EditRecordHeadingTest"
      val saveChanges = "Your changes have been saved."
      val oci = "EditRecordOciTest"

      val confirmationEditSetRecordEditHtml: Html =
        editSetRecordEditSaveInstance(user, editSetName, title, heading, oci, saveChanges, Some(PhysicalRecord))

      val document = asDocument(confirmationEditSetRecordEditHtml)
      document must haveTitle("EditRecordTitleTest")
      document must haveNotificationBannerContents(
        Seq(
          "Your changes have been saved.",
          "EditRecordHeadingTest",
          "edit-set.record.edit.id",
          "placeholder.series-national-coal-board"
        )
      )
      document must haveBackLink("/edit-set/1/record/EditRecordOciTest/edit", "edit-set.record.save.back")

    }

    "render the header" in {

      val document = generateDocument()
      document must haveHeaderTitle("header.title")
      document must haveVisibleLogoutLink
      document must haveLogoutLinkLabel("header.logout")
      document must haveLogoutLink

    }

  }

  private def generateDocument(): Document = {
    implicit val messages: Messages = Helpers.stubMessages()
    val editSetRecordEditSaveInstance = inject[editSetRecordEditSave]
    asDocument(
      editSetRecordEditSaveInstance(
        user = user,
        editSetName = "COAL 80 Sample",
        title = "EditRecordTitleTest",
        heading = "EditRecordHeadingTest",
        oci = "EditRecordOciTest",
        message = "Your changes have been saved.",
        Some(PhysicalRecord)
      )
    )
  }

}
