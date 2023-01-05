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
import play.api.data.Form
import play.api.data.Forms.{ mapping, text }
import play.api.test.{ CSRFTokenHelper, FakeRequest, Helpers }
import play.twirl.api.Html
import support.BaseSpec
import support.CustomMatchers._
import uk.gov.nationalarchives.omega.editorial.controllers.EditSetController.EditSetReorder
import uk.gov.nationalarchives.omega.editorial.models.{ EditSet, EditSetEntry }
import uk.gov.nationalarchives.omega.editorial.views.html.editSet

class EditSetViewSpec extends BaseSpec {

  private val reorderForm: Form[EditSetReorder] = Form(
    mapping(
      "field"     -> text,
      "direction" -> text
    )(EditSetReorder.apply)(EditSetReorder.unapply)
  )

  "Edit set Html" should {
    "render the given title and heading" in {

      val editSetInstance = inject[editSet]
      val editSet: EditSet = getEditSetTest("1")
      val title = "EditSetTitleTest"
      val heading = editSet.name

      val editSetHtml: Html = editSetInstance(user, title, heading, editSet.entries, reorderForm)(
        Helpers.stubMessages(),
        CSRFTokenHelper.addCSRFToken(FakeRequest())
      )

      val document = asDocument(editSetHtml)
      document must haveTitle(title)
      document must haveCaption(heading)
      document must haveSummaryRows(3)
      document must haveSummaryRowContents(
        1,
        Seq(
          "COAL 80/80/1",
          "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
          "1960"
        )
      )
      document must haveSummaryRowContents(
        2,
        Seq(
          "COAL 80/80/2",
          "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of factory.",
          "1962"
        )
      )
      document must haveSummaryRowContents(
        3,
        Seq(
          "COAL 80/80/3",
          "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of workers.",
          "1964"
        )
      )

    }

    "render the header" in {

      val document: Document = generateDocument()

      document must haveHeaderTitle("header.title")
      document must haveVisibleLogoutLink
      document must haveLogoutLinkLabel("header.logout")
      document must haveLogoutLink

    }

  }

  private def getEditSetTest(id: String): EditSet =
    EditSet(
      "COAL 80 Sample",
      id,
      Seq(
        EditSetEntry(
          "COAL 80/80/1",
          id,
          "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
          "1960"
        ),
        EditSetEntry(
          "COAL 80/80/2",
          id,
          "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of factory.",
          "1962"
        ),
        EditSetEntry(
          "COAL 80/80/3",
          id,
          "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of workers.",
          "1964"
        )
      )
    )

  private def generateDocument(): Document = {
    val editSetInstance = inject[editSet]
    val editSet: EditSet = getEditSetTest("1")
    asDocument(
      editSetInstance(
        user = user,
        title = "EditSetTitleTest",
        heading = editSet.name,
        editSetEntries = editSet.entries,
        editSetReorderForm = reorderForm
      )(
        Helpers.stubMessages(),
        CSRFTokenHelper.addCSRFToken(FakeRequest())
      )
    )
  }
}
