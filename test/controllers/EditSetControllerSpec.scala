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

package controllers

import org.jsoup.nodes.Document
import org.scalatest.compatible.Assertion
import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._
import support.BaseSpec
import support.CommonMatchers._
import uk.gov.nationalarchives.omega.editorial.controllers.{ EditSetController, SessionKeys }
import uk.gov.nationalarchives.omega.editorial.editSetRecords.restoreOriginalRecords
import uk.gov.nationalarchives.omega.editorial.views.html.editSet

/** Add your spec here. You can mock out a whole application including requests, plugins etc.
  *
  * For more information, see https://www.playframework.com/documentation/latest/ScalaTestingWithScalaTest
  */
class EditSetControllerSpec extends BaseSpec {
  import EditSetControllerSpec._

  override def beforeEach(): Unit = {
    super.beforeEach()
    restoreOriginalRecords()
  }

  "EditSetController GET /edit-set/{id}" should {

    "render the edit set page from a new instance of controller" in {
      val messages: Map[String, Map[String, String]] =
        Map(
          "en" -> Map(
            "edit-set.heading"       -> "Edit set: COAL 80 Sample",
            "edit-set.table-caption" -> "Showing {0} - {1} of {2} records"
          )
        )
      val mockMessagesApi = stubMessagesApi(messages)
      val editSetInstance = inject[editSet]
      val stub = stubControllerComponents()
      val controller = new EditSetController(
        DefaultMessagesControllerComponents(
          new DefaultMessagesActionBuilderImpl(stubBodyParser(AnyContentAsEmpty), mockMessagesApi)(
            stub.executionContext
          ),
          DefaultActionBuilder(stub.actionBuilder.parser)(stub.executionContext),
          stub.parsers,
          mockMessagesApi,
          stub.langs,
          stub.fileMimeTypes,
          stub.executionContext
        ),
        editSetService,
        editSetInstance
      )
      val editSet = controller
        .view("1")
        .apply(
          CSRFTokenHelper.addCSRFToken(
            FakeRequest(GET, "/edit-set/1").withSession(SessionKeys.token -> validSessionToken)
          )
        )

      status(editSet) mustBe OK
      contentType(editSet) mustBe Some("text/html")
      val document = asDocument(editSet)
      document must haveCaption("Showing 1 - 10 of 12 records")
      document must haveHeader("Edit set: COAL 80 Sample")
    }

    "render the edit set page from the application" in {
      val controller = inject[EditSetController]
      val editSet = controller
        .view("1")
        .apply(
          CSRFTokenHelper.addCSRFToken(
            FakeRequest(GET, "/edit-set/1")
              .withSession(SessionKeys.token -> validSessionToken)
          )
        )

      status(editSet) mustBe OK
      contentType(editSet) mustBe Some("text/html")
      val document = asDocument(editSet)
      document must haveCaption("Showing 1 - 10 of 12 records")
      document must haveHeader("Edit set: COAL 80 Sample")
    }

    "render the edit set page from the router" in {
      val request = FakeRequest(GET, "/edit-set/1").withSession(SessionKeys.token -> validSessionToken)
      val editSet = route(app, request).get

      status(editSet) mustBe OK
      contentType(editSet) mustBe Some("text/html")
      val document = asDocument(editSet)
      document must haveCaption("Showing 1 - 10 of 12 records")
      document must haveHeader("Edit set: COAL 80 Sample")
    }

    "all ids in the document conform to w3c reccomendations" in {
      val request = FakeRequest(GET, "/edit-set/1").withSession(SessionKeys.token -> validSessionToken)
      val editRecordPage = route(app, request).get

      status(editRecordPage) mustBe OK
      asDocument(editRecordPage) must haveAllLowerCaseIds
    }

    "all class names in the document conform to w3c reccomendations" in {
      val request = FakeRequest(GET, "/edit-set/1").withSession(SessionKeys.token -> validSessionToken)
      val editRecordPage = route(app, request).get

      status(editRecordPage) mustBe OK
      asDocument(editRecordPage) must haveAllLowerCssClassNames
    }

    "redirect to the login page from the application when requested with invalid session token" in {
      val controller = inject[EditSetController]
      val editSet = controller
        .view("1")
        .apply(
          FakeRequest(GET, "/edit-set/1")
            .withSession(SessionKeys.token -> invalidSessionToken)
        )

      status(editSet) mustBe SEE_OTHER
      redirectLocation(editSet) mustBe Some("/login")
    }

    "redirect to the login page from the router when requested with invalid session token" in {
      val request = FakeRequest(GET, "/edit-set/1").withSession(SessionKeys.token -> invalidSessionToken)
      val editSet = route(app, request).get

      status(editSet) mustBe SEE_OTHER
      redirectLocation(editSet) mustBe Some("/login")
    }

    "order records" when {

      def orderingRequest(field: String, direction: String, offset: Int = 1) = {
        val request = FakeRequest(GET, s"/edit-set/1?field=$field&direction=$direction&offset=$offset")
          .withSession(SessionKeys.token -> validSessionToken)
        route(app, request).get
      }

      "CCR, ascending" in {
        val page = orderingRequest("ccr", "ascending")

        status(page) mustBe OK
        assertPageAsExpected(
          asDocument(page),
          ExpectedEditSetPage(
            title = "Browse Edit Set (Page 1 of 2)",
            caption = "Showing 1 - 10 of 12 records",
            ccrTableHeader = ExpectedTableHeader("ccr", "descending"),
            scopeAndContentTableHeader = ExpectedTableHeader("scope-and-content", "ascending"),
            coveringDateTableHeader = ExpectedTableHeader("covering-dates", "ascending"),
            header = "Edit set: COAL 80 Sample",
            expectedSummaryRows = Seq(
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/1",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (B)",
                coveringDates = "1962"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/10",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (J)",
                coveringDates = "1973"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/11",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (K)",
                coveringDates = "1975"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/12",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (L)",
                coveringDates = "1977"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/2",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (A)",
                coveringDates = "1966"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/3",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (C)",
                coveringDates = "1964"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/4",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (D)",
                coveringDates = "1961"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/5",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (E)",
                coveringDates = "1963"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/6",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (F)",
                coveringDates = "1965"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/7",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (G)",
                coveringDates = "1967"
              )
            ),
            numberOfPages = 2
          )
        )

      }

      "CCR, descending" in {

        val page = orderingRequest("ccr", "descending")

        status(page) mustBe OK
        assertPageAsExpected(
          asDocument(page),
          ExpectedEditSetPage(
            title = "Browse Edit Set (Page 1 of 2)",
            caption = "Showing 1 - 10 of 12 records",
            ccrTableHeader = ExpectedTableHeader("ccr", "ascending"),
            scopeAndContentTableHeader = ExpectedTableHeader("scope-and-content", "ascending"),
            coveringDateTableHeader = ExpectedTableHeader("covering-dates", "ascending"),
            header = "Edit set: COAL 80 Sample",
            expectedSummaryRows = Seq(
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/9",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (I)",
                coveringDates = "1971"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/8",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (H)",
                coveringDates = "1969"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/7",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (G)",
                coveringDates = "1967"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/6",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (F)",
                coveringDates = "1965"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/5",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (E)",
                coveringDates = "1963"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/4",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (D)",
                coveringDates = "1961"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/3",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (C)",
                coveringDates = "1964"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/2",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (A)",
                coveringDates = "1966"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/12",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (L)",
                coveringDates = "1977"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/11",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (K)",
                coveringDates = "1975"
              )
            ),
            numberOfPages = 2
          )
        )
      }

      "Scope and Content, ascending" in {

        val page = orderingRequest("scope-and-content", "ascending")

        status(page) mustBe OK
        assertPageAsExpected(
          asDocument(page),
          ExpectedEditSetPage(
            title = "Browse Edit Set (Page 1 of 2)",
            caption = "Showing 1 - 10 of 12 records",
            ccrTableHeader = ExpectedTableHeader("ccr", "ascending"),
            scopeAndContentTableHeader = ExpectedTableHeader("scope-and-content", "descending"),
            coveringDateTableHeader = ExpectedTableHeader("covering-dates", "ascending"),
            header = "Edit set: COAL 80 Sample",
            expectedSummaryRows = Seq(
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/2",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (A)",
                coveringDates = "1966"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/1",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (B)",
                coveringDates = "1962"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/3",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (C)",
                coveringDates = "1964"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/4",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (D)",
                coveringDates = "1961"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/5",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (E)",
                coveringDates = "1963"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/6",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (F)",
                coveringDates = "1965"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/7",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (G)",
                coveringDates = "1967"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/8",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (H)",
                coveringDates = "1969"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/9",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (I)",
                coveringDates = "1971"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/10",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (J)",
                coveringDates = "1973"
              )
            ),
            numberOfPages = 2
          )
        )

      }

      "Scope and Content, descending" in {

        val page = orderingRequest("scope-and-content", "descending")

        status(page) mustBe OK
        assertPageAsExpected(
          asDocument(page),
          ExpectedEditSetPage(
            title = "Browse Edit Set (Page 1 of 2)",
            caption = "Showing 1 - 10 of 12 records",
            header = "Edit set: COAL 80 Sample",
            ccrTableHeader = ExpectedTableHeader("ccr", "ascending"),
            scopeAndContentTableHeader = ExpectedTableHeader("scope-and-content", "ascending"),
            coveringDateTableHeader = ExpectedTableHeader("covering-dates", "ascending"),
            expectedSummaryRows = Seq(
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/12",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (L)",
                coveringDates = "1977"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/11",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (K)",
                coveringDates = "1975"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/10",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (J)",
                coveringDates = "1973"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/9",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (I)",
                coveringDates = "1971"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/8",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (H)",
                coveringDates = "1969"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/7",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (G)",
                coveringDates = "1967"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/6",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (F)",
                coveringDates = "1965"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/5",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (E)",
                coveringDates = "1963"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/4",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (D)",
                coveringDates = "1961"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/3",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (C)",
                coveringDates = "1964"
              )
            ),
            numberOfPages = 2
          )
        )

      }

      "Covering dates, ascending" in {

        val page = orderingRequest("covering-dates", "ascending")

        status(page) mustBe OK
        assertPageAsExpected(
          asDocument(page),
          ExpectedEditSetPage(
            title = "Browse Edit Set (Page 1 of 2)",
            caption = "Showing 1 - 10 of 12 records",
            ccrTableHeader = ExpectedTableHeader("ccr", "ascending"),
            scopeAndContentTableHeader = ExpectedTableHeader("scope-and-content", "ascending"),
            coveringDateTableHeader = ExpectedTableHeader("covering-dates", "descending"),
            header = "Edit set: COAL 80 Sample",
            expectedSummaryRows = Seq(
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/4",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (D)",
                coveringDates = "1961"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/1",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (B)",
                coveringDates = "1962"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/5",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (E)",
                coveringDates = "1963"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/3",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (C)",
                coveringDates = "1964"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/6",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (F)",
                coveringDates = "1965"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/2",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (A)",
                coveringDates = "1966"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/7",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (G)",
                coveringDates = "1967"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/8",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (H)",
                coveringDates = "1969"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/9",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (I)",
                coveringDates = "1971"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/10",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (J)",
                coveringDates = "1973"
              )
            ),
            numberOfPages = 2
          )
        )

      }

      "Covering dates, descending" in {

        val page = orderingRequest("covering-dates", "descending")

        status(page) mustBe OK
        assertPageAsExpected(
          asDocument(page),
          ExpectedEditSetPage(
            title = "Browse Edit Set (Page 1 of 2)",
            caption = "Showing 1 - 10 of 12 records",
            header = "Edit set: COAL 80 Sample",
            ccrTableHeader = ExpectedTableHeader("ccr", "ascending"),
            scopeAndContentTableHeader = ExpectedTableHeader("scope-and-content", "ascending"),
            coveringDateTableHeader = ExpectedTableHeader("covering-dates", "ascending"),
            expectedSummaryRows = Seq(
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/12",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (L)",
                coveringDates = "1977"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/11",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (K)",
                coveringDates = "1975"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/10",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (J)",
                coveringDates = "1973"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/9",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (I)",
                coveringDates = "1971"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/8",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (H)",
                coveringDates = "1969"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/7",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (G)",
                coveringDates = "1967"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/2",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (A)",
                coveringDates = "1966"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/6",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (F)",
                coveringDates = "1965"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/3",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (C)",
                coveringDates = "1964"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/5",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (E)",
                coveringDates = "1963"
              )
            ),
            numberOfPages = 2
          )
        )

      }

      "Unknown field and direction" in {

        val page = orderingRequest("height", "upwards")

        status(page) mustBe OK
        assertPageAsExpected(
          asDocument(page),
          ExpectedEditSetPage(
            title = "Browse Edit Set (Page 1 of 2)",
            caption = "Showing 1 - 10 of 12 records",
            header = "Edit set: COAL 80 Sample",
            ccrTableHeader = ExpectedTableHeader("ccr", "descending"),
            scopeAndContentTableHeader = ExpectedTableHeader("scope-and-content", "ascending"),
            coveringDateTableHeader = ExpectedTableHeader("covering-dates", "ascending"),
            expectedSummaryRows = Seq(
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/1",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (B)",
                coveringDates = "1962"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/10",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (J)",
                coveringDates = "1973"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/11",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (K)",
                coveringDates = "1975"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/12",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (L)",
                coveringDates = "1977"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/2",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (A)",
                coveringDates = "1966"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/3",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (C)",
                coveringDates = "1964"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/4",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (D)",
                coveringDates = "1961"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/5",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (E)",
                coveringDates = "1963"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/6",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (F)",
                coveringDates = "1965"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/7",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (G)",
                coveringDates = "1967"
              )
            ),
            numberOfPages = 2
          )
        )

      }

      "Page 2 sorted by CCR, ascending" in {
        val page = orderingRequest("ccr", "ascending", offset = 2)

        status(page) mustBe OK
        assertPageAsExpected(
          asDocument(page),
          ExpectedEditSetPage(
            title = "Browse Edit Set (Page 2 of 2)",
            caption = "Showing 11 - 12 of 12 records",
            header = "Edit set: COAL 80 Sample",
            ccrTableHeader = ExpectedTableHeader("ccr", "descending"),
            scopeAndContentTableHeader = ExpectedTableHeader("scope-and-content", "ascending"),
            coveringDateTableHeader = ExpectedTableHeader("covering-dates", "ascending"),
            expectedSummaryRows = Seq(
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/8",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (H)",
                coveringDates = "1969"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/9",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (I)",
                coveringDates = "1971"
              )
            ),
            numberOfPages = 2
          )
        )

      }

    }

  }

  private def assertPageAsExpected(
    document: Document,
    expectedEditRecordPage: ExpectedEditSetPage
  ): Assertion = {
    document must haveTitle(expectedEditRecordPage.title)
    document must haveCaption(expectedEditRecordPage.caption)

    document must haveDirectionInTableHeader("CCR", expectedEditRecordPage.ccrTableHeader.sortOrder)
    document must haveFieldInTableHeader("CCR", expectedEditRecordPage.ccrTableHeader.sortField)

    document must haveDirectionInTableHeader(
      "Scope and content",
      expectedEditRecordPage.scopeAndContentTableHeader.sortOrder
    )
    document must haveFieldInTableHeader(
      "Scope and content",
      expectedEditRecordPage.scopeAndContentTableHeader.sortField
    )

    document must haveDirectionInTableHeader("Covering dates", expectedEditRecordPage.coveringDateTableHeader.sortOrder)
    document must haveFieldInTableHeader("Covering dates", expectedEditRecordPage.coveringDateTableHeader.sortField)

    document must haveSummaryRows(expectedEditRecordPage.expectedSummaryRows.size)
    expectedEditRecordPage.expectedSummaryRows.zipWithIndex.foreach { case (expectedEditSetSummaryRow, index) =>
      document must haveSummaryRowContents(
        index + 1,
        Seq(
          expectedEditSetSummaryRow.ccr,
          expectedEditSetSummaryRow.scopeAndContents,
          expectedEditSetSummaryRow.coveringDates
        )
      )
    }
    document must haveNumberOfPages(expectedEditRecordPage.numberOfPages)
  }

}

object EditSetControllerSpec {

  case class ExpectedEditSetPage(
    title: String,
    header: String,
    caption: String,
    ccrTableHeader: ExpectedTableHeader,
    scopeAndContentTableHeader: ExpectedTableHeader,
    coveringDateTableHeader: ExpectedTableHeader,
    expectedSummaryRows: Seq[ExpectedEditSetSummaryRow],
    numberOfPages: Int
  )

  case class ExpectedTableHeader(sortField: String, sortOrder: String)

  case class ExpectedEditSetSummaryRow(
    ccr: String,
    scopeAndContents: String,
    coveringDates: String
  )

}
