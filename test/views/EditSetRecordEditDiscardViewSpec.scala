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

import play.api.i18n.Messages
import play.api.test.Helpers
import support.BaseViewSpec
import support.CommonMatchers._
import uk.gov.hmrc.govukfrontend.views.html.components.GovukNotificationBanner
import uk.gov.nationalarchives.omega.editorial.models.PhysicalRecord
import uk.gov.nationalarchives.omega.editorial.views.html.editSetRecordEditDiscard

class EditSetRecordEditDiscardViewSpec extends BaseViewSpec {

  implicit lazy val messages: Messages = Helpers.stubMessages()
  lazy val banner = new GovukNotificationBanner
  lazy val editSetRecordEditDiscardInstance = new editSetRecordEditDiscard(banner)

  "Edit set record edit discard Html" should {

    "render the given title and heading with discard changes message" in {
      val confirmationEditSetRecordEditHtml = editSetRecordEditDiscardInstance(
        user = user,
        editSetName = "COAL 80 Sample",
        title = "EditRecordTitleTest",
        heading = "EditRecordHeadingTest",
        oci = "EditRecordOciTest",
        message = "Any changes have been discarded. Showing last saved version.",
        recordType = Some(PhysicalRecord)
      )
      val document = asDocument(confirmationEditSetRecordEditHtml)

      document must haveTitle("EditRecordTitleTest")
      document must haveNotificationBannerContents(
        Seq(
          "Any changes have been discarded. Showing last saved version.",
          "EditRecordHeadingTest",
          "edit-set.record.edit.id",
          "placeholder.series-national-coal-board"
        )
      )
      document must haveBackLink("/edit-set/1/record/EditRecordOciTest/edit", "edit-set.record.save.back")
    }

    "render the header" in {
      val confirmationEditSetRecordEditHtml = editSetRecordEditDiscardInstance(
        user = user,
        editSetName = "COAL 80 Sample",
        title = "EditRecordTitleTest",
        heading = "EditRecordHeadingTest",
        oci = "EditRecordOciTest",
        message = "Any changes have been discarded. Showing last saved version.",
        recordType = Some(PhysicalRecord)
      )
      val document = asDocument(confirmationEditSetRecordEditHtml)

      document must haveHeaderTitle("header.title")
      document must haveVisibleLogoutLink
      document must haveLogoutLinkLabel("header.logout")
      document must haveLogoutLink
    }

  }

}
