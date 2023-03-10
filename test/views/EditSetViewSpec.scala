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
import play.api.test.Helpers.{ contentType, defaultAwaitTimeout }
import play.api.test.{ CSRFTokenHelper, FakeRequest, Helpers }
import play.twirl.api.Html
import support.BaseViewSpec
import support.CommonMatchers._
import uk.gov.hmrc.govukfrontend.views.Aliases.Pagination
import uk.gov.hmrc.govukfrontend.views.html.components.{ GovukPagination, GovukTable }
import uk.gov.nationalarchives.omega.editorial.models.{ EditSet, EditSetEntry }
import uk.gov.nationalarchives.omega.editorial.services.EditSetEntryRowOrder
import uk.gov.nationalarchives.omega.editorial.services.EditSetPagination.EditSetPage
import uk.gov.nationalarchives.omega.editorial.views.html.editSet

class EditSetViewSpec extends BaseViewSpec {

  "Edit set Html" should {
    "render the given title and heading" in {
      val govukPagination = new GovukPagination
      val govukTable = new GovukTable
      val editSetInstance = new editSet(govukPagination, govukTable)
      val editSet: EditSet = getEditSetTest("1")
      val title = "EditSetTitleTest"
      val heading = editSet.name

      val editSetPage = EditSetPage(
        editSet.entries,
        Pagination(),
        1,
        editSet.entries.size,
        1
      )

      val editSetHtml: Html = editSetInstance(user, title, heading, editSetPage, EditSetEntryRowOrder.defaultOrder)(
        Helpers.stubMessages(),
        CSRFTokenHelper.addCSRFToken(FakeRequest())
      )

      contentType(editSetHtml) mustBe "text/html"
      val document = asDocument(Helpers.contentAsString(editSetHtml))
      document must haveTitle(title)
      document must haveHeader("COAL 80 Sample")
      document must haveCaption("edit-set.table-caption")
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
    val govukPagination = new GovukPagination
    val govukTable = new GovukTable
    val editSetInstance = new editSet(govukPagination, govukTable)
    val editSet: EditSet = getEditSetTest("1")
    val editSetPage = EditSetPage(
      editSet.entries,
      Pagination(),
      1,
      editSet.entries.size,
      1
    )
    asDocument(
      Helpers.contentAsString(
        editSetInstance(
          user = user,
          title = "EditSetTitleTest",
          heading = editSet.name,
          page = editSetPage,
          rowOrder = EditSetEntryRowOrder.defaultOrder
        )(
          Helpers.stubMessages(),
          CSRFTokenHelper.addCSRFToken(FakeRequest())
        )
      )
    )
  }

}
