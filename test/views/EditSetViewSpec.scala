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
import uk.gov.nationalarchives.omega.editorial.models.{ EditSet, EditSetEntry }
import uk.gov.nationalarchives.omega.editorial.views.html.editSet

class EditSetViewSpec extends BaseSpec {

  "Edit set Html" should {
    implicit val messages: Messages = Helpers.stubMessages()
    "render the given title and heading" in {

      val editSetInstance = inject[editSet]
      val editSet: EditSet = getEditSetTest("1")
      val title = "EditSetTitleTest"
      val heading = editSet.name

      val editSetHtml: Html = editSetInstance(user, title, heading, editSet)
      contentAsString(editSetHtml) must include(title)
      contentAsString(editSetHtml) must include(heading)
      contentAsString(editSetHtml) must include(editSet.name)
      for (entry <- editSet.entries) {
        contentAsString(editSetHtml) must include(entry.ccr)
        contentAsString(editSetHtml) must include(entry.scopeAndContent)
        contentAsString(editSetHtml) must include(entry.coveringDates)

      }

    }

    "render the header" in {

      val document: Document = generateDocument()

      document must haveHeaderTitle
      document must haveVisibleSignOutLink

    }

  }

  private def getEditSetTest(id: String): EditSet = {

    val scopeAndContent = "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths."
    val editSetEntry1 =
      EditSetEntry("COAL 80/80/1", id, scopeAndContent, "1960")
    val editSetEntry2 =
      EditSetEntry("COAL 80/80/2", id, scopeAndContent, "1960")
    val editSetEntry3 =
      EditSetEntry("COAL 80/80/3", id, scopeAndContent, "1960")
    val entries = Seq(editSetEntry1, editSetEntry2, editSetEntry3)
    val editSetName = "COAL 80 Sample"
    val editSet = EditSet(editSetName, id: String, entries)

    editSet
  }

  private def generateDocument(): Document = {
    implicit val messages: Messages = Helpers.stubMessages()
    val editSetInstance = inject[editSet]
    val editSet: EditSet = getEditSetTest("1")
    asDocument(editSetInstance(user, "EditSetTitleTest", editSet.name, editSet))
  }
}
