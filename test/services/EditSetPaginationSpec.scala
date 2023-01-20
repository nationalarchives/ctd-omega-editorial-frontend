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

package services

import support.BaseSpec
import support.CustomMatchers._
import uk.gov.nationalarchives.omega.editorial.services.EditSetPagination
import uk.gov.nationalarchives.omega.editorial.models.EditSetEntry
import uk.gov.nationalarchives.omega.editorial.controllers.EditSetController.{ EditSetReorder, FieldNames, orderDirectionAscending }

class EditSetPaginationSpec extends BaseSpec {

  "EditSetPagination" should {

    "calculate the number of pagination items when there is 1 item" in {
      val entries = Seq(sampleEntry)
      val page = new EditSetPagination("1", sampleOrdering).makeEditSetPage(entries, pageNumber = 1)

      page.entries mustEqual entries
      page.pagination.items.size mustBe 1
      page.pagination.next mustBe None
      page.pagination.previous mustBe None
    }

    "calculate the number of pagination items when there are 11 items" in {
      val entries = Seq.fill(11)(sampleEntry)
      val page = new EditSetPagination("1", sampleOrdering).makeEditSetPage(entries, pageNumber = 1)

      page.entries mustEqual entries.take(10)
      page.pagination.items.get.size mustBe 2

      page must havePaginationNextLink
      page mustNot havePaginationPreviousLink
    }

    "don't have a next link when on the last page" in {
      val entries = Seq.fill(11)(sampleEntry)
      val page = new EditSetPagination("1", sampleOrdering).makeEditSetPage(entries, pageNumber = 2)

      page.entries.size mustEqual 1
      page.pagination.items.get.size mustBe 2

      page mustNot havePaginationNextLink
      page must havePaginationPreviousLink
      page must haveBothEllipsisItems
    }

  }

  lazy val sampleOrdering = EditSetReorder(FieldNames.ccr, orderDirectionAscending)

  lazy val sampleEntry =
    EditSetEntry(
      ccr = "COAL 80/80/1",
      oci = "COAL.2022.V1RJW.P",
      scopeAndContent = "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
      coveringDates = "1960"
    )

}
