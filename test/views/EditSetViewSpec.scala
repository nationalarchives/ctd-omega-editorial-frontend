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
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.Table
import uk.gov.nationalarchives.omega.editorial.models.{ EditSet, EditSetEntry, User }
import uk.gov.nationalarchives.omega.editorial.services.EditSetEntryRowOrder
import uk.gov.nationalarchives.omega.editorial.services.EditSetPagination.EditSetPage
import uk.gov.nationalarchives.omega.editorial.views.html.editSet

class EditSetViewSpec extends BaseViewSpec {

  "Edit set Html" should {
    "render the given title and heading" in {
      val mockGovukPagination = mock[GovukPagination]
      val mockGovukTable = mock[GovukTable]
      val editSetInstance = new editSet(mockGovukPagination, mockGovukTable)
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

      when(mockGovukTable.apply(Table(anyList))).thenReturn(getEditSetTable())
      when(mockGovukPagination.apply(Pagination())).thenReturn(any)
      val editSetHtml: Html = editSetInstance(user, title, heading, editSetPage, EditSetEntryRowOrder.defaultOrder)(
        Helpers.stubMessages(),
        CSRFTokenHelper.addCSRFToken(FakeRequest())
      )

      contentType(editSetHtml) mustBe "text/html"
      println(editSetHtml)
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
    val mockGovukPagination = mock[GovukPagination]
    val mockGovukTable = mock[GovukTable]
    val editSetInstance = new editSet(mockGovukPagination, mockGovukTable)
    val editSet: EditSet = getEditSetTest("1")
    val editSetPage = EditSetPage(
      editSet.entries,
      Pagination(),
      1,
      editSet.entries.size,
      1
    )
    when(mockGovukTable.apply(Table(anyList))).thenReturn(any)
    when(mockGovukPagination.apply(Pagination())).thenReturn(any)
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

  private def getEditSetTable(): Html = new Html(
    """
      |  <table class="govuk-table">
      |  
      |    <caption class="govuk-table__caption govuk-table__caption--m">edit-set.table-caption</caption>

      |    <thead class="govuk-table__head">
      |     <tr class="govuk-table__row">
      |       
      |        <th scope="col" class="govuk-table__header"   aria-sort="ascending">
      |  
      |    <a class="govuk-link govuk-link--no-visited-state" href="/edit-set/1?field=ccr&amp;direction=descending">edit-set.record.edit.ccr</a>
      |  
      |       </th>
      |       
      |         <th scope="col" class="govuk-table__header"  aria-sort="none">
      |  
      |    <a class="govuk-link govuk-link--no-visited-state" href="/edit-set/1?field=scope-and-content&amp;direction=ascending">edit-set.record.edit.scope-and-content</a>
      |  
      |       </th>
      |       
      |        <th scope="col" class="govuk-table__header" aria-sort="none">
      |  
      |    <a class="govuk-link govuk-link--no-visited-state" href="/edit-set/1?field=covering-dates&amp;direction=ascending">edit-set.record.edit.covering-dates</a>
      |  
      |       </th>
      |       
      |     </tr>
      |    </thead>
      |  
      |  <tbody class="govuk-table__body">
      |    
      |      <tr class="govuk-table__row">
      |        
      |          
      |         <th scope="row" class="govuk-table__header">
      |    
      |        <a class="govuk-link govuk-link--no-visited-state" href=/edit-set/1/record/1/edit>COAL 80/80/1</a>
      |    
      |         </th>
      |            <td class="govuk-table__cell">Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.</td>
      |            <td class="govuk-table__cell">1960</td>
      |      </tr>
      |    
      |      <tr class="govuk-table__row">
      |        
      |          
      |          <th scope="row" class="govuk-table__header">
      |    
      |        <a class="govuk-link govuk-link--no-visited-state" href=/edit-set/1/record/1/edit>COAL 80/80/2</a>
      |    
      |         </th>
      |            <td class="govuk-table__cell">Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of factory.</td>
      |            <td class="govuk-table__cell">1962</td>
      |      </tr>
      |    
      |      <tr class="govuk-table__row">
      |         <th scope="row" class="govuk-table__header">
      |    
      |        <a class="govuk-link govuk-link--no-visited-state" href=/edit-set/1/record/1/edit>COAL 80/80/3</a>
      |    
      |         </th>
      |            <td class="govuk-table__cell">Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of workers.</td>
      |            <td class="govuk-table__cell">1964</td>
      |      </tr>
      |    
      |  </tbody>
      |</table>
      |
      |""".stripMargin
  )
}
