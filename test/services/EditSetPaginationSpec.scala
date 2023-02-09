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

import org.scalatest.matchers.{ MatchResult, Matcher }
import support.BaseSpec
import uk.gov.nationalarchives.omega.editorial.models.EditSetEntry
import uk.gov.nationalarchives.omega.editorial.services.{ EditSetEntryRowOrder, EditSetPagination }

class EditSetPaginationSpec extends BaseSpec {
  import EditSetPaginationSpec._

  "EditSetPagination" should {

    "calculate the number of pagination items when there is 1 item" in {
      val entries = Seq(sampleEntry)
      val page = new EditSetPagination("1", sampleOrdering, "Next", "Previous").makeEditSetPage(entries, pageNumber = 1)

      page.entries mustBe entries
      page.pagination.items.size mustBe 1
      page.pagination.next mustBe None
      page.pagination.previous mustBe None
      page mustNot haveBothEllipsisItems
    }

    "calculate the number of pagination items when there are 11 items" in {
      val entries = Seq.fill(11)(sampleEntry)
      val page = new EditSetPagination("1", sampleOrdering, "Next", "Previous").makeEditSetPage(entries, pageNumber = 1)

      page.entries mustBe entries.take(10)
      page.pagination.items.get.size mustBe 2

      page must havePaginationNextLink
      page mustNot havePaginationPreviousLink
      page mustNot haveBothEllipsisItems
    }

    "don't have a next link when on the last page" in {
      val entries = Seq.fill(11)(sampleEntry)
      val page = new EditSetPagination("1", sampleOrdering, "Next", "Previous").makeEditSetPage(entries, pageNumber = 2)

      page.entries.size mustBe 1
      page.pagination.items.get.size mustBe 2

      page mustNot havePaginationNextLink
      page must havePaginationPreviousLink
      page mustNot haveBothEllipsisItems
    }

    "only show 5 items when there are 100 entries" in {
      val entries = Seq.fill(100)(sampleEntry)
      val page = new EditSetPagination("1", sampleOrdering, "Next", "Previous").makeEditSetPage(entries, pageNumber = 5)

      page.entries.size mustBe 10
      page.pagination.items.get.size mustBe 7

      page must havePaginationNextLink
      page must havePaginationPreviousLink
      page must haveBothEllipsisItems
    }

    "get the number for the first and last displayed entry on the first page" in {
      val entries = Seq.fill(20)(sampleEntry)
      val page = new EditSetPagination("1", sampleOrdering, "Next", "Previous").makeEditSetPage(entries, pageNumber = 1)

      page.numberOfFirstEntry mustBe 1
      page.numberOfLastEntry mustBe 10
      page.totalNumberOfEntries mustBe 20
    }

    "get the number for the first and last displayed entry on the last page" in {
      val entries = Seq.fill(12)(sampleEntry)
      val page = new EditSetPagination("1", sampleOrdering, "Next", "Previous").makeEditSetPage(entries, pageNumber = 2)

      page.numberOfFirstEntry mustBe 11
      page.numberOfLastEntry mustBe 12
      page.totalNumberOfEntries mustBe 12
    }

    "get the number for the first and last displayed entry on a page in the middle" in {
      val entries = Seq.fill(22)(sampleEntry)
      val page = new EditSetPagination("1", sampleOrdering, "Next", "Previous").makeEditSetPage(entries, pageNumber = 2)

      page.numberOfFirstEntry mustBe 11
      page.numberOfLastEntry mustBe 20
      page.totalNumberOfEntries mustBe 22
    }

    "format next href correctly" in {
      val entries = Seq.fill(22)(sampleEntry)
      val page = new EditSetPagination("1", sampleOrdering, "Next", "Previous").makeEditSetPage(entries, pageNumber = 1)

      page must haveNextHref("/edit-set/1?offset=3&field=ccr&direction=ascending")
    }

    "format page 2 href correctly" in {
      val entries = Seq.fill(22)(sampleEntry)
      val page = new EditSetPagination("1", sampleOrdering, "Next", "Previous").makeEditSetPage(entries, pageNumber = 1)

      page must havePageNumberLink(2, "/edit-set/1?offset=2&field=ccr&direction=ascending")
    }

    "format page 3 href correctly" in {
      val entries = Seq.fill(32)(sampleEntry)
      val page = new EditSetPagination("1", sampleOrdering, "Next", "Previous").makeEditSetPage(entries, pageNumber = 2)

      page must havePageNumberLink(3, "/edit-set/1?offset=3&field=ccr&direction=ascending")
    }

  }

}

object EditSetPaginationSpec {
  import EditSetPagination._

  private lazy val sampleOrdering = EditSetEntryRowOrder.defaultOrder

  private lazy val sampleEntry =
    EditSetEntry(
      ccr = "COAL 80/80/1",
      oci = "COAL.2022.V1RJW.P",
      scopeAndContent = "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
      coveringDates = "1960"
    )

  def havePaginationNextLink: Matcher[EditSetPage] = (page: EditSetPage) => {
    val expectedPageNumber = page.pageNumber + 1
    val errorMessageIfExpected =
      s"The page didn't have a pagination next link to page $expectedPageNumber"
    val errorMessageIfNotExpected =
      "The page did indeed have a pagination next link, which was not expected"
    MatchResult(
      page.pagination.next.exists(_.href.contains(s"offset=$expectedPageNumber")),
      errorMessageIfExpected,
      errorMessageIfNotExpected
    )
  }

  def havePaginationPreviousLink: Matcher[EditSetPage] = (page: EditSetPage) => {
    val expectedPageNumber = page.pageNumber - 1
    val errorMessageIfExpected =
      s"The page didn't have a pagination previous link to page $expectedPageNumber"
    val errorMessageIfNotExpected =
      "The page did indeed have a pagination previous link, which was not expected"
    MatchResult(
      page.pagination.previous.exists(_.href.contains(s"offset=$expectedPageNumber")),
      errorMessageIfExpected,
      errorMessageIfNotExpected
    )
  }

  def haveBothEllipsisItems: Matcher[EditSetPage] = (page: EditSetPage) => {
    val errorMessageIfExpected =
      s"The page didn't have both ellipsis items"
    val errorMessageIfNotExpected =
      "The page did have ellipsis items, which was not expected"
    MatchResult(
      page.pagination.items.exists(_.count(_.ellipsis.contains(true)) == 2),
      errorMessageIfExpected,
      errorMessageIfNotExpected
    )
  }

  def haveNextHref(expectedHref: String): Matcher[EditSetPage] = (page: EditSetPage) => {
    val errorMessageIfExpected = page.pagination.next.map(_.href) match {
      case Some(href) =>
        s"""The hrefs did not match:
           | expected: $expectedHref
           | found:    $href
          """.stripMargin
      case None =>
        s"""The page next link did not have an href"""
    }
    val errorMessageIfNotExpected =
      "The page did have an href, which was not expected"
    MatchResult(
      page.pagination.next.map(_.href) == Some(expectedHref),
      errorMessageIfExpected,
      errorMessageIfNotExpected
    )
  }

  def havePageNumberLink(pageNumber: Int, expectedHref: String): Matcher[EditSetPage] = (page: EditSetPage) => {
    val link = for {
      items <- page.pagination.items
      item  <- items.toList.lift(pageNumber - 1)
    } yield item.href
    val errorMessageIfExpected = link match {
      case Some(href) =>
        s"""The hrefs did not match:
           | expected: $expectedHref
           | found:    $href
          """.stripMargin
      case None =>
        s"""The page next link did not have an href"""
    }
    val errorMessageIfNotExpected =
      "The page did have an href, which was not expected"
    MatchResult(
      link == Some(expectedHref),
      errorMessageIfExpected,
      errorMessageIfNotExpected
    )
  }

}
