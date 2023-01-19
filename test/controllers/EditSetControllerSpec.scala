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
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test._
import org.scalatest.compatible.Assertion
import support.BaseSpec
import support.CustomMatchers._
import support.ExpectedValues._
import uk.gov.nationalarchives.omega.editorial.controllers.{ EditSetController, SessionKeys }
import uk.gov.nationalarchives.omega.editorial.controllers.EditSetController._
import uk.gov.nationalarchives.omega.editorial.models.session.Session
import uk.gov.nationalarchives.omega.editorial.views.html.{ editSet, editSetRecordEdit, editSetRecordEditDiscard, editSetRecordEditSave }

import scala.concurrent.Future

/** Add your spec here. You can mock out a whole application including requests, plugins etc.
  *
  * For more information, see https://www.playframework.com/documentation/latest/ScalaTestingWithScalaTest
  */
class EditSetControllerSpec extends BaseSpec {
  import EditSetControllerSpec._

  val validSessionToken: String = Session.generateToken("1234")
  val invalidSessionToken: String = Session.generateToken("invalid-user")

  "EditSetController GET /edit-set/{id}" should {

    "render the edit set page from a new instance of controller" in {
      val messages: Map[String, Map[String, String]] =
        Map("en" -> Map("edit-set.heading" -> "Edit set: COAL 80 Sample"))
      val mockMessagesApi = stubMessagesApi(messages)
      val editSetInstance = inject[editSet]
      val editSetRecordEditInstance = inject[editSetRecordEdit]
      val editSetRecordEditDiscardInstance = inject[editSetRecordEditDiscard]
      val editSetRecordEditSaveInstance = inject[editSetRecordEditSave]
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
        editSetInstance,
        editSetRecordEditInstance,
        editSetRecordEditDiscardInstance,
        editSetRecordEditSaveInstance
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
      document must haveCaption("Edit set: COAL 80 Sample")
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
      document must haveCaption("Edit set: COAL 80 Sample")
    }

    "render the edit set page from the router" in {
      val request = FakeRequest(GET, "/edit-set/1").withSession(SessionKeys.token -> validSessionToken)
      val editSet = route(app, request).get

      status(editSet) mustBe OK
      contentType(editSet) mustBe Some("text/html")
      val document = asDocument(editSet)
      document must haveCaption("Edit set: COAL 80 Sample")
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
  }

  "EditSetController POST /edit-set/{id}" should {
    "render the records in the expected order, when ordering by" when {

      def requestPage(values: Map[String, String], pageNumber: Int = 1): Future[Result] =
        route(
          app,
          CSRFTokenHelper.addCSRFToken(
            FakeRequest(POST, s"/edit-set/1?offset=$pageNumber")
              .withFormUrlEncodedBody(values.toSeq: _*)
              .withSession(SessionKeys.token -> validSessionToken)
          )
        ).get

      "CCR, ascending" in {

        val values = Map(fieldKey -> FieldNames.ccr, orderDirectionKey -> orderDirectionAscending)

        val page = requestPage(values)

        status(page) mustBe OK
        assertPageAsExpected(
          asDocument(page),
          ExpectedEditSetPage(
            title = "Edit set",
            caption = "Edit set: COAL 80 Sample",
            button = ExpectedActionButton("reorder", "Sort edit set"),
            expectedOptionsForField = Seq(
              ExpectedSelectOption(FieldNames.ccr, "CCR", selected = true),
              ExpectedSelectOption(FieldNames.scopeAndContent, "Scope and Content"),
              ExpectedSelectOption(FieldNames.coveringDates, "Covering Dates")
            ),
            expectedOptionsForDirection = Seq(
              ExpectedSelectOption(orderDirectionAscending, "Ascending", selected = true),
              ExpectedSelectOption(orderDirectionDescending, "Descending")
            ),
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
              )
            )
          )
        )

      }
      "CCR, descending" in {

        val values = Map("field" -> FieldNames.ccr, "direction" -> "descending")

        val page = requestPage(values)

        status(page) mustBe OK
        assertPageAsExpected(
          asDocument(page),
          ExpectedEditSetPage(
            title = "Edit set",
            caption = "Edit set: COAL 80 Sample",
            button = ExpectedActionButton("reorder", "Sort edit set"),
            expectedOptionsForField = Seq(
              ExpectedSelectOption(FieldNames.ccr, "CCR", selected = true),
              ExpectedSelectOption(FieldNames.scopeAndContent, "Scope and Content"),
              ExpectedSelectOption(FieldNames.coveringDates, "Covering Dates")
            ),
            expectedOptionsForDirection = Seq(
              ExpectedSelectOption(orderDirectionAscending, "Ascending"),
              ExpectedSelectOption(orderDirectionDescending, "Descending", selected = true)
            ),
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
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/10",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (J)",
                coveringDates = "1973"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/1",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (B)",
                coveringDates = "1962"
              )
            )
          )
        )

      }
      "Scope and Content, ascending" in {

        val values = Map(fieldKey -> FieldNames.scopeAndContent, orderDirectionKey -> orderDirectionAscending)

        val page = requestPage(values)

        status(page) mustBe OK
        assertPageAsExpected(
          asDocument(page),
          ExpectedEditSetPage(
            title = "Edit set",
            caption = "Edit set: COAL 80 Sample",
            button = ExpectedActionButton("reorder", "Sort edit set"),
            expectedOptionsForField = Seq(
              ExpectedSelectOption(FieldNames.ccr, "CCR"),
              ExpectedSelectOption(FieldNames.scopeAndContent, "Scope and Content", selected = true),
              ExpectedSelectOption(FieldNames.coveringDates, "Covering Dates")
            ),
            expectedOptionsForDirection = Seq(
              ExpectedSelectOption(orderDirectionAscending, "Ascending", selected = true),
              ExpectedSelectOption(orderDirectionDescending, "Descending")
            ),
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
              )
            )
          )
        )

      }
      "Scope and Content, descending" in {

        val values = Map(fieldKey -> FieldNames.scopeAndContent, orderDirectionKey -> orderDirectionDescending)

        val page = requestPage(values)

        status(page) mustBe OK
        assertPageAsExpected(
          asDocument(page),
          ExpectedEditSetPage(
            title = "Edit set",
            caption = "Edit set: COAL 80 Sample",
            button = ExpectedActionButton("reorder", "Sort edit set"),
            expectedOptionsForField = Seq(
              ExpectedSelectOption(FieldNames.ccr, "CCR"),
              ExpectedSelectOption(FieldNames.scopeAndContent, "Scope and Content", selected = true),
              ExpectedSelectOption(FieldNames.coveringDates, "Covering Dates")
            ),
            expectedOptionsForDirection = Seq(
              ExpectedSelectOption(orderDirectionAscending, "Ascending"),
              ExpectedSelectOption(orderDirectionDescending, "Descending", selected = true)
            ),
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
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/1",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (B)",
                coveringDates = "1962"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/2",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (A)",
                coveringDates = "1966"
              )
            )
          )
        )

      }
      "Covering dates, ascending" in {

        val values = Map(fieldKey -> FieldNames.coveringDates, orderDirectionKey -> orderDirectionAscending)

        val page = requestPage(values)

        status(page) mustBe OK
        assertPageAsExpected(
          asDocument(page),
          ExpectedEditSetPage(
            title = "Edit set",
            caption = "Edit set: COAL 80 Sample",
            button = ExpectedActionButton("reorder", "Sort edit set"),
            expectedOptionsForField = Seq(
              ExpectedSelectOption(FieldNames.ccr, "CCR"),
              ExpectedSelectOption(FieldNames.scopeAndContent, "Scope and Content"),
              ExpectedSelectOption(FieldNames.coveringDates, "Covering Dates", selected = true)
            ),
            expectedOptionsForDirection = Seq(
              ExpectedSelectOption(orderDirectionAscending, "Ascending", selected = true),
              ExpectedSelectOption(orderDirectionDescending, "Descending")
            ),
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
              )
            )
          )
        )

      }
      "Covering dates, descending" in {

        val values = Map(fieldKey -> FieldNames.coveringDates, orderDirectionKey -> orderDirectionDescending)

        val page = requestPage(values)

        status(page) mustBe OK
        assertPageAsExpected(
          asDocument(page),
          ExpectedEditSetPage(
            title = "Edit set",
            caption = "Edit set: COAL 80 Sample",
            button = ExpectedActionButton("reorder", "Sort edit set"),
            expectedOptionsForField = Seq(
              ExpectedSelectOption(FieldNames.ccr, "CCR"),
              ExpectedSelectOption(FieldNames.scopeAndContent, "Scope and Content"),
              ExpectedSelectOption(FieldNames.coveringDates, "Covering Dates", selected = true)
            ),
            expectedOptionsForDirection = Seq(
              ExpectedSelectOption(orderDirectionAscending, "Ascending"),
              ExpectedSelectOption(orderDirectionDescending, "Descending", selected = true)
            ),
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
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/1",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (B)",
                coveringDates = "1962"
              ),
              ExpectedEditSetSummaryRow(
                ccr = "COAL 80/80/4",
                scopeAndContents =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (D)",
                coveringDates = "1961"
              )
            )
          )
        )

      }
      "Unknown field and direction" in {

        val values = Map(fieldKey -> "height", orderDirectionKey -> "upwards")

        val page = requestPage(values)

        status(page) mustBe OK
        assertPageAsExpected(
          asDocument(page),
          ExpectedEditSetPage(
            title = "Edit set",
            caption = "Edit set: COAL 80 Sample",
            button = ExpectedActionButton("reorder", "Sort edit set"),
            expectedOptionsForField = Seq(
              ExpectedSelectOption(FieldNames.ccr, "CCR", selected = true),
              ExpectedSelectOption(FieldNames.scopeAndContent, "Scope and Content"),
              ExpectedSelectOption(FieldNames.coveringDates, "Covering Dates")
            ),
            expectedOptionsForDirection = Seq(
              ExpectedSelectOption(orderDirectionAscending, "Ascending", selected = true),
              ExpectedSelectOption(orderDirectionDescending, "Descending")
            ),
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
              )
            )
          )
        )

      }

      "Page 2 sorted by CCR, ascending" in {

        val values = Map(fieldKey -> FieldNames.ccr, orderDirectionKey -> orderDirectionAscending)

        val page = requestPage(values, pageNumber = 2)

        status(page) mustBe OK
        assertPageAsExpected(
          asDocument(page),
          ExpectedEditSetPage(
            title = "Edit set",
            caption = "Edit set: COAL 80 Sample",
            button = ExpectedActionButton("reorder", "Sort edit set"),
            expectedOptionsForField = Seq(
              ExpectedSelectOption(FieldNames.ccr, "CCR", selected = true),
              ExpectedSelectOption(FieldNames.scopeAndContent, "Scope and Content"),
              ExpectedSelectOption(FieldNames.coveringDates, "Covering Dates")
            ),
            expectedOptionsForDirection = Seq(
              ExpectedSelectOption(orderDirectionAscending, "Ascending", selected = true),
              ExpectedSelectOption(orderDirectionDescending, "Descending")
            ),
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
              )
            )
          ),
          pageNumber = 2
        )

      }
    }
  }

  "EditSetController GET /edit-set/{id}/record/{recordId}/edit" should {

    "render the edit set page from a new instance of controller" in {
      val defaultLang = play.api.i18n.Lang.defaultLang.code
      val messages: Map[String, Map[String, String]] =
        Map(defaultLang -> Map("edit-set.record.edit.heading" -> "TNA reference: COAL 80/80/1"))
      val mockMessagesApi = stubMessagesApi(messages)
      val editSetInstance = inject[editSet]
      val editSetRecordEditInstance = inject[editSetRecordEdit]
      val editSetRecordEditDiscardInstance = inject[editSetRecordEditDiscard]
      val editSetRecordEditSaveInstance = inject[editSetRecordEditSave]
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
        editSetInstance,
        editSetRecordEditInstance,
        editSetRecordEditDiscardInstance,
        editSetRecordEditSaveInstance
      )
      val editRecordPage = controller
        .editRecord("1", "COAL.2022.V1RJW.P")
        .apply(
          CSRFTokenHelper.addCSRFToken(
            FakeRequest(GET, "/edit-set/1/record/COAL.2022.V1RJW.P/edit")
              .withSession(SessionKeys.token -> validSessionToken)
          )
        )

      status(editRecordPage) mustBe OK
      contentType(editRecordPage) mustBe Some("text/html")
      val document = asDocument(editRecordPage)
      document must haveHeading("TNA reference: COAL 80/80/1")
    }

    "render the edit set page from the application" in {
      val controller = inject[EditSetController]
      val editRecordPage = controller
        .editRecord("1", "COAL.2022.V1RJW.P")
        .apply(
          CSRFTokenHelper.addCSRFToken(
            FakeRequest(GET, "/edit-set/1/record/COAL.2022.V1RJW.P/edit")
              .withSession(SessionKeys.token -> validSessionToken)
          )
        )

      status(editRecordPage) mustBe OK
      contentType(editRecordPage) mustBe Some("text/html")
      val document = asDocument(editRecordPage)
      document must haveHeading("TNA reference: COAL 80/80/1")
    }

    "render the edit set record page from the router" when {
      "all ids in the document conform to w3c reccomendations" in {
        val request =
          CSRFTokenHelper.addCSRFToken(
            FakeRequest(GET, "/edit-set/1/record/COAL.2022.V1RJW.P/edit").withSession(
              SessionKeys.token -> validSessionToken
            )
          )

        val editRecordPage = route(app, request).get

        status(editRecordPage) mustBe OK
        asDocument(editRecordPage) must haveAllLowerCaseIds
      }

      "all class names in the document conform to w3c reccomendations" in {
        val request =
          CSRFTokenHelper.addCSRFToken(
            FakeRequest(GET, "/edit-set/1/record/COAL.2022.V1RJW.P/edit").withSession(
              SessionKeys.token -> validSessionToken
            )
          )

        val editRecordPage = route(app, request).get

        status(editRecordPage) mustBe OK
        asDocument(editRecordPage) must haveAllLowerCssClassNames
      }

      "all data is valid" in {
        val request =
          CSRFTokenHelper.addCSRFToken(
            FakeRequest(GET, "/edit-set/1/record/COAL.2022.V1RJW.P/edit").withSession(
              SessionKeys.token -> validSessionToken
            )
          )

        val editRecordPage = route(app, request).get

        status(editRecordPage) mustBe OK
        assertPageAsExpected(
          asDocument(editRecordPage),
          ExpectedEditRecordPage(
            title = "Edit record",
            heading = "TNA reference: COAL 80/80/1",
            legend = "Intellectual properties",
            classicCatalogueRef = "COAL 80/80/1",
            omegaCatalogueId = "COAL.2022.V1RJW.P",
            scopeAndContent =
              "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (B)",
            coveringDates = "1962",
            formerReferenceDepartment = "",
            startDate = ExpectedDate("1", "1", "1962"),
            endDate = ExpectedDate("31", "12", "1962"),
            legalStatus = "ref.1",
            note = "A note about COAL.2022.V1RJW.P.",
            background = "Photo was taken by a daughter of one of the coal miners who used them.",
            optionsForPlaceOfDeposit = Seq(
              ExpectedSelectOption("", "Select where this record is held", disabled = true),
              ExpectedSelectOption("1", "The National Archives, Kew", selected = true),
              ExpectedSelectOption("2", "British Museum, Department of Libraries and Archives"),
              ExpectedSelectOption("3", "British Library, National Sound Archive")
            ),
            separatedMaterial = Seq(
              ExpectedSeparatedMaterial(
                linkHref = Some("#;"),
                linkText = Some("COAL 80/80/5")
              ),
              ExpectedSeparatedMaterial(
                linkHref = Some("#;"),
                linkText = Some("COAL 80/80/6")
              ),
              ExpectedSeparatedMaterial(
                linkHref = Some("#;"),
                linkText = Some("COAL 80/80/7")
              )
            ),
            relatedMaterial = Seq(
              ExpectedRelatedMaterial(
                description =
                  Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
              ),
              ExpectedRelatedMaterial(
                linkHref = Some("#;"),
                linkText = Some("COAL 80/80/3")
              ),
              ExpectedRelatedMaterial(
                linkHref = Some("#;"),
                linkText = Some("COAL 80/80/2"),
                description =
                  Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
              )
            ),
            custodialHistory = "Files originally created by successor or predecessor departments for COAL"
          )
        )
      }
      "all data is valid, except that the place of deposit is unrecognised" in {
        val request =
          FakeRequest(GET, "/edit-set/1/record/COAL.2022.V3RJW.P/edit").withSession(
            SessionKeys.token -> validSessionToken
          )
        val editRecordPage = route(app, request).get

        status(editRecordPage) mustBe OK
        contentType(editRecordPage) mustBe Some("text/html")
        assertPageAsExpected(
          asDocument(editRecordPage),
          ExpectedEditRecordPage(
            title = "Edit record",
            heading = "TNA reference: COAL 80/80/3",
            legend = "Intellectual properties",
            classicCatalogueRef = "COAL 80/80/3",
            omegaCatalogueId = "COAL.2022.V3RJW.P",
            scopeAndContent =
              "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (C)",
            coveringDates = "1964",
            formerReferenceDepartment = "",
            startDate = ExpectedDate("1", "1", "1964"),
            endDate = ExpectedDate("31", "12", "1964"),
            legalStatus = "",
            note = "",
            background = "Photo was taken by a son of one of the coal miners who used them.",
            optionsForPlaceOfDeposit = Seq(
              ExpectedSelectOption("", "Select where this record is held", selected = true, disabled = true),
              ExpectedSelectOption("1", "The National Archives, Kew"),
              ExpectedSelectOption("2", "British Museum, Department of Libraries and Archives"),
              ExpectedSelectOption("3", "British Library, National Sound Archive")
            ),
            custodialHistory = ""
          )
        )
      }
    }

    "redirect to the login page from the application when requested with invalid session token" in {
      val controller = inject[EditSetController]
      val editRecordPage = controller
        .editRecord("1", "COAL.2022.V1RJW.P")
        .apply(
          CSRFTokenHelper.addCSRFToken(
            FakeRequest(GET, "/edit-set/1/record/COAL.2022.V1RJW.P/edit")
              .withSession(SessionKeys.token -> invalidSessionToken)
          )
        )

      status(editRecordPage) mustBe SEE_OTHER
      redirectLocation(editRecordPage) mustBe Some("/login")
    }

    "redirect to the login page from the router when requested with invalid session token" in {
      val request =
        FakeRequest(GET, "/edit-set/1/record/COAL.2022.V1RJW.P/edit").withSession(
          SessionKeys.token -> invalidSessionToken
        )
      val editRecordPage = route(app, request).get

      status(editRecordPage) mustBe SEE_OTHER
      redirectLocation(editRecordPage) mustBe Some("/login")
    }
  }

  "EditSetController POST /edit-set/{id}/record/{recordId}/edit" should {

    val validValues: Map[String, String] =
      Map(
        FieldNames.scopeAndContent -> "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
        FieldNames.formerReferenceDepartment -> "1234",
        FieldNames.coveringDates             -> "2020 Oct",
        FieldNames.startDateDay              -> "1",
        FieldNames.startDateMonth            -> "10",
        FieldNames.startDateYear             -> "2020",
        FieldNames.endDateDay                -> "31",
        FieldNames.endDateMonth              -> "10",
        FieldNames.endDateYear               -> "2020",
        FieldNames.legalStatus               -> "ref.1",
        FieldNames.placeOfDeposit            -> "2",
        FieldNames.note                      -> "Need to check copyright info.",
        FieldNames.background       -> "Photo was taken by a daughter of one of the coal miners who used them.",
        FieldNames.custodialHistory -> "Files originally created by successor or predecessor departments for COAL"
      )

    "when the action is to save the record" when {

      val validValuesForSaving = validValues ++ Map("action" -> "save")

      "fail" when {
        "and yet preserve the CCR" when {
          "there are errors" in {
            val blankScopeAndContentToFailValidation = ""
            val values = validValuesForSaving ++ Map(
              FieldNames.scopeAndContent -> blankScopeAndContentToFailValidation
            )

            val result = submitWhileLoggedIn(1, "COAL.2022.V1RJW.P", values)

            status(result) mustBe BAD_REQUEST
            assertPageAsExpected(
              asDocument(result),
              ExpectedEditRecordPage(
                title = "Edit record",
                heading = "TNA reference: COAL 80/80/1",
                legend = "Intellectual properties",
                classicCatalogueRef = "COAL 80/80/1",
                omegaCatalogueId = "COAL.2022.V1RJW.P",
                scopeAndContent = "",
                coveringDates = "2020 Oct",
                formerReferenceDepartment = "1234",
                startDate = ExpectedDate("1", "10", "2020"),
                endDate = ExpectedDate("31", "10", "2020"),
                legalStatus = "ref.1",
                note = "Need to check copyright info.",
                background = "Photo was taken by a daughter of one of the coal miners who used them.",
                optionsForPlaceOfDeposit = Seq(
                  ExpectedSelectOption("", "Select where this record is held", disabled = true),
                  ExpectedSelectOption("1", "The National Archives, Kew"),
                  ExpectedSelectOption("2", "British Museum, Department of Libraries and Archives", selected = true),
                  ExpectedSelectOption("3", "British Library, National Sound Archive")
                ),
                separatedMaterial = Seq(
                  ExpectedSeparatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/5")
                  ),
                  ExpectedSeparatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/6")
                  ),
                  ExpectedSeparatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/7")
                  )
                ),
                relatedMaterial = Seq(
                  ExpectedRelatedMaterial(
                    description =
                      Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
                  ),
                  ExpectedRelatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/3")
                  ),
                  ExpectedRelatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/2"),
                    description =
                      Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
                  )
                ),
                custodialHistory = "Files originally created by successor or predecessor departments for COAL"
              )
            )
          }
        }
        "start date" when {
          "is empty" in {

            val values =
              validValuesForSaving ++ Map(
                FieldNames.startDateDay   -> "",
                FieldNames.startDateMonth -> "",
                FieldNames.startDateYear  -> ""
              )

            val result = submitWhileLoggedIn(1, "COAL.2022.V1RJW.P", values)

            status(result) mustBe BAD_REQUEST
            assertPageAsExpected(
              asDocument(result),
              ExpectedEditRecordPage(
                title = "Edit record",
                heading = "TNA reference: COAL 80/80/1",
                legend = "Intellectual properties",
                classicCatalogueRef = "COAL 80/80/1",
                omegaCatalogueId = "COAL.2022.V1RJW.P",
                scopeAndContent =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
                coveringDates = "2020 Oct",
                formerReferenceDepartment = "1234",
                startDate = ExpectedDate("", "", ""),
                endDate = ExpectedDate("31", "10", "2020"),
                legalStatus = "ref.1",
                note = "Need to check copyright info.",
                background = "Photo was taken by a daughter of one of the coal miners who used them.",
                optionsForPlaceOfDeposit = Seq(
                  ExpectedSelectOption("", "Select where this record is held", disabled = true),
                  ExpectedSelectOption("1", "The National Archives, Kew"),
                  ExpectedSelectOption("2", "British Museum, Department of Libraries and Archives", selected = true),
                  ExpectedSelectOption("3", "British Library, National Sound Archive")
                ),
                separatedMaterial = Seq(
                  ExpectedSeparatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/5")
                  ),
                  ExpectedSeparatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/6")
                  ),
                  ExpectedSeparatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/7")
                  )
                ),
                relatedMaterial = Seq(
                  ExpectedRelatedMaterial(
                    description =
                      Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
                  ),
                  ExpectedRelatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/3")
                  ),
                  ExpectedRelatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/2"),
                    description =
                      Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
                  )
                ),
                custodialHistory = "Files originally created by successor or predecessor departments for COAL",
                summaryErrorMessages =
                  Seq(ExpectedSummaryErrorMessage("Start date is not a valid date", s"#${FieldNames.startDateDay}")),
                errorMessageForStartDate = Some("Start date is not a valid date")
              )
            )

          }
          "is of an invalid format" in {

            val values =
              validValuesForSaving ++ Map(
                FieldNames.startDateDay   -> "XX",
                FieldNames.startDateMonth -> "11",
                FieldNames.startDateYear  -> "1960"
              )

            val result = submitWhileLoggedIn(1, "COAL.2022.V1RJW.P", values)

            status(result) mustBe BAD_REQUEST
            assertPageAsExpected(
              asDocument(result),
              ExpectedEditRecordPage(
                title = "Edit record",
                heading = "TNA reference: COAL 80/80/1",
                legend = "Intellectual properties",
                classicCatalogueRef = "COAL 80/80/1",
                omegaCatalogueId = "COAL.2022.V1RJW.P",
                scopeAndContent =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
                coveringDates = "2020 Oct",
                formerReferenceDepartment = "1234",
                startDate = ExpectedDate("XX", "11", "1960"),
                endDate = ExpectedDate("31", "10", "2020"),
                legalStatus = "ref.1",
                note = "Need to check copyright info.",
                background = "Photo was taken by a daughter of one of the coal miners who used them.",
                optionsForPlaceOfDeposit = Seq(
                  ExpectedSelectOption("", "Select where this record is held", disabled = true),
                  ExpectedSelectOption("1", "The National Archives, Kew"),
                  ExpectedSelectOption("2", "British Museum, Department of Libraries and Archives", selected = true),
                  ExpectedSelectOption("3", "British Library, National Sound Archive")
                ),
                separatedMaterial = Seq(
                  ExpectedSeparatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/5")
                  ),
                  ExpectedSeparatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/6")
                  ),
                  ExpectedSeparatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/7")
                  )
                ),
                relatedMaterial = Seq(
                  ExpectedRelatedMaterial(
                    description =
                      Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
                  ),
                  ExpectedRelatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/3")
                  ),
                  ExpectedRelatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/2"),
                    description =
                      Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
                  )
                ),
                custodialHistory = "Files originally created by successor or predecessor departments for COAL",
                summaryErrorMessages =
                  Seq(ExpectedSummaryErrorMessage("Start date is not a valid date", s"#${FieldNames.startDateDay}")),
                errorMessageForStartDate = Some("Start date is not a valid date")
              )
            )

          }
        }
        "doesn't exist" in {
          val values = validValuesForSaving ++ Map(
            FieldNames.startDateDay   -> "29",
            FieldNames.startDateMonth -> "2",
            FieldNames.startDateYear  -> "2022",
            FieldNames.endDateDay     -> "31",
            FieldNames.endDateMonth   -> "10",
            FieldNames.endDateYear    -> "2022"
          )

          val result = submitWhileLoggedIn(1, "COAL.2022.V1RJW.P", values)

          status(result) mustBe BAD_REQUEST
          val expectedPage = ExpectedEditRecordPage(
            title = "Edit record",
            heading = "TNA reference: COAL 80/80/1",
            legend = "Intellectual properties",
            classicCatalogueRef = "COAL 80/80/1",
            omegaCatalogueId = "COAL.2022.V1RJW.P",
            scopeAndContent = "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
            coveringDates = "2020 Oct",
            formerReferenceDepartment = "1234",
            startDate = ExpectedDate("29", "2", "2022"),
            endDate = ExpectedDate("31", "10", "2022"),
            legalStatus = "ref.1",
            note = "Need to check copyright info.",
            background = "Photo was taken by a daughter of one of the coal miners who used them.",
            optionsForPlaceOfDeposit = Seq(
              ExpectedSelectOption("", "Select where this record is held", disabled = true),
              ExpectedSelectOption("1", "The National Archives, Kew"),
              ExpectedSelectOption("2", "British Museum, Department of Libraries and Archives", selected = true),
              ExpectedSelectOption("3", "British Library, National Sound Archive")
            ),
            custodialHistory = "Files originally created by successor or predecessor departments for COAL",
            summaryErrorMessages =
              Seq(ExpectedSummaryErrorMessage("Start date is not a valid date", s"#${FieldNames.startDateDay}")),
            errorMessageForStartDate = Some("Start date is not a valid date"),
            separatedMaterial = Seq(
              ExpectedSeparatedMaterial(
                linkHref = Some("#;"),
                linkText = Some("COAL 80/80/5")
              ),
              ExpectedSeparatedMaterial(
                linkHref = Some("#;"),
                linkText = Some("COAL 80/80/6")
              ),
              ExpectedSeparatedMaterial(
                linkHref = Some("#;"),
                linkText = Some("COAL 80/80/7")
              )
            ),
            relatedMaterial = Seq(
              ExpectedRelatedMaterial(
                description =
                  Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
              ),
              ExpectedRelatedMaterial(
                linkHref = Some("#;"),
                linkText = Some("COAL 80/80/3")
              ),
              ExpectedRelatedMaterial(
                linkHref = Some("#;"),
                linkText = Some("COAL 80/80/2"),
                description =
                  Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
              )
            )
          )
          assertPageAsExpected(asDocument(result), expectedPage)
        }
        "end date" when {
          "is empty" in {

            val values = validValuesForSaving ++ Map(
              FieldNames.endDateDay   -> "",
              FieldNames.endDateMonth -> "",
              FieldNames.endDateYear  -> ""
            )

            val result = submitWhileLoggedIn(1, "COAL.2022.V1RJW.P", values)

            status(result) mustBe BAD_REQUEST
            val document = asDocument(result)
            assertPageAsExpected(
              document,
              ExpectedEditRecordPage(
                title = "Edit record",
                heading = "TNA reference: COAL 80/80/1",
                legend = "Intellectual properties",
                classicCatalogueRef = "COAL 80/80/1",
                omegaCatalogueId = "COAL.2022.V1RJW.P",
                scopeAndContent =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
                coveringDates = "2020 Oct",
                formerReferenceDepartment = "1234",
                startDate = ExpectedDate("1", "10", "2020"),
                endDate = ExpectedDate("", "", ""),
                legalStatus = "ref.1",
                note = "Need to check copyright info.",
                background = "Photo was taken by a daughter of one of the coal miners who used them.",
                optionsForPlaceOfDeposit = Seq(
                  ExpectedSelectOption("", "Select where this record is held", disabled = true),
                  ExpectedSelectOption("1", "The National Archives, Kew"),
                  ExpectedSelectOption("2", "British Museum, Department of Libraries and Archives", selected = true),
                  ExpectedSelectOption("3", "British Library, National Sound Archive")
                ),
                separatedMaterial = Seq(
                  ExpectedSeparatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/5")
                  ),
                  ExpectedSeparatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/6")
                  ),
                  ExpectedSeparatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/7")
                  )
                ),
                relatedMaterial = Seq(
                  ExpectedRelatedMaterial(
                    description =
                      Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
                  ),
                  ExpectedRelatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/3")
                  ),
                  ExpectedRelatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/2"),
                    description =
                      Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
                  )
                ),
                custodialHistory = "Files originally created by successor or predecessor departments for COAL",
                summaryErrorMessages =
                  Seq(ExpectedSummaryErrorMessage("End date is not a valid date", s"#${FieldNames.endDateDay}")),
                errorMessageForEndDate = Some("End date is not a valid date")
              )
            )

          }
          "is of an invalid format" in {

            val values =
              validValuesForSaving ++ Map(
                FieldNames.endDateDay   -> "XX",
                FieldNames.endDateMonth -> "12",
                FieldNames.endDateYear  -> "2000"
              )

            val result = submitWhileLoggedIn(1, "COAL.2022.V1RJW.P", values)

            status(result) mustBe BAD_REQUEST
            assertPageAsExpected(
              asDocument(result),
              ExpectedEditRecordPage(
                title = "Edit record",
                heading = "TNA reference: COAL 80/80/1",
                legend = "Intellectual properties",
                classicCatalogueRef = "COAL 80/80/1",
                omegaCatalogueId = "COAL.2022.V1RJW.P",
                scopeAndContent =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
                coveringDates = "2020 Oct",
                formerReferenceDepartment = "1234",
                startDate = ExpectedDate("1", "10", "2020"),
                endDate = ExpectedDate("XX", "12", "2000"),
                legalStatus = "ref.1",
                note = "Need to check copyright info.",
                background = "Photo was taken by a daughter of one of the coal miners who used them.",
                optionsForPlaceOfDeposit = Seq(
                  ExpectedSelectOption("", "Select where this record is held", disabled = true),
                  ExpectedSelectOption("1", "The National Archives, Kew"),
                  ExpectedSelectOption("2", "British Museum, Department of Libraries and Archives", selected = true),
                  ExpectedSelectOption("3", "British Library, National Sound Archive")
                ),
                separatedMaterial = Seq(
                  ExpectedSeparatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/5")
                  ),
                  ExpectedSeparatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/6")
                  ),
                  ExpectedSeparatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/7")
                  )
                ),
                relatedMaterial = Seq(
                  ExpectedRelatedMaterial(
                    description =
                      Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
                  ),
                  ExpectedRelatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/3")
                  ),
                  ExpectedRelatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/2"),
                    description =
                      Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
                  )
                ),
                custodialHistory = "Files originally created by successor or predecessor departments for COAL",
                summaryErrorMessages =
                  Seq(ExpectedSummaryErrorMessage("End date is not a valid date", s"#${FieldNames.endDateDay}")),
                errorMessageForEndDate = Some("End date is not a valid date")
              )
            )

          }
          "doesn't exist" in {

            val values = validValuesForSaving ++
              Map(
                FieldNames.startDateDay   -> "1",
                FieldNames.startDateMonth -> "2",
                FieldNames.startDateYear  -> "2022",
                FieldNames.endDateDay     -> "29",
                FieldNames.endDateMonth   -> "2",
                FieldNames.endDateYear    -> "2022"
              )
            val result = submitWhileLoggedIn(1, "COAL.2022.V1RJW.P", values)

            status(result) mustBe BAD_REQUEST
            assertPageAsExpected(
              asDocument(result),
              ExpectedEditRecordPage(
                title = "Edit record",
                heading = "TNA reference: COAL 80/80/1",
                legend = "Intellectual properties",
                classicCatalogueRef = "COAL 80/80/1",
                omegaCatalogueId = "COAL.2022.V1RJW.P",
                scopeAndContent =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
                coveringDates = "2020 Oct",
                formerReferenceDepartment = "1234",
                startDate = ExpectedDate("1", "2", "2022"),
                endDate = ExpectedDate("29", "2", "2022"),
                legalStatus = "ref.1",
                note = "Need to check copyright info.",
                background = "Photo was taken by a daughter of one of the coal miners who used them.",
                optionsForPlaceOfDeposit = Seq(
                  ExpectedSelectOption("", "Select where this record is held", disabled = true),
                  ExpectedSelectOption("1", "The National Archives, Kew"),
                  ExpectedSelectOption("2", "British Museum, Department of Libraries and Archives", selected = true),
                  ExpectedSelectOption("3", "British Library, National Sound Archive")
                ),
                separatedMaterial = Seq(
                  ExpectedSeparatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/5")
                  ),
                  ExpectedSeparatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/6")
                  ),
                  ExpectedSeparatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/7")
                  )
                ),
                relatedMaterial = Seq(
                  ExpectedRelatedMaterial(
                    description =
                      Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
                  ),
                  ExpectedRelatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/3")
                  ),
                  ExpectedRelatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/2"),
                    description =
                      Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
                  )
                ),
                custodialHistory = "Files originally created by successor or predecessor departments for COAL",
                summaryErrorMessages =
                  Seq(ExpectedSummaryErrorMessage("End date is not a valid date", s"#${FieldNames.endDateDay}")),
                errorMessageForEndDate = Some("End date is not a valid date")
              )
            )

          }
          "is before start date" in {

            val values = validValuesForSaving ++ Map(
              FieldNames.startDateDay   -> "12",
              FieldNames.startDateMonth -> "10",
              FieldNames.startDateYear  -> "2020",
              FieldNames.endDateDay     -> "11",
              FieldNames.endDateMonth   -> "10",
              FieldNames.endDateYear    -> "2020"
            )

            val result = submitWhileLoggedIn(1, "COAL.2022.V1RJW.P", values)

            status(result) mustBe BAD_REQUEST
            assertPageAsExpected(
              asDocument(result),
              ExpectedEditRecordPage(
                title = "Edit record",
                heading = "TNA reference: COAL 80/80/1",
                legend = "Intellectual properties",
                classicCatalogueRef = "COAL 80/80/1",
                omegaCatalogueId = "COAL.2022.V1RJW.P",
                scopeAndContent =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
                coveringDates = "2020 Oct",
                formerReferenceDepartment = "1234",
                startDate = ExpectedDate("12", "10", "2020"),
                endDate = ExpectedDate("11", "10", "2020"),
                legalStatus = "ref.1",
                note = "Need to check copyright info.",
                background = "Photo was taken by a daughter of one of the coal miners who used them.",
                optionsForPlaceOfDeposit = Seq(
                  ExpectedSelectOption("", "Select where this record is held", disabled = true),
                  ExpectedSelectOption("1", "The National Archives, Kew"),
                  ExpectedSelectOption("2", "British Museum, Department of Libraries and Archives", selected = true),
                  ExpectedSelectOption("3", "British Library, National Sound Archive")
                ),
                separatedMaterial = Seq(
                  ExpectedSeparatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/5")
                  ),
                  ExpectedSeparatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/6")
                  ),
                  ExpectedSeparatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/7")
                  )
                ),
                relatedMaterial = Seq(
                  ExpectedRelatedMaterial(
                    description =
                      Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
                  ),
                  ExpectedRelatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/3")
                  ),
                  ExpectedRelatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/2"),
                    description =
                      Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
                  )
                ),
                custodialHistory = "Files originally created by successor or predecessor departments for COAL",
                summaryErrorMessages =
                  Seq(ExpectedSummaryErrorMessage("End date cannot precede start date", s"#${FieldNames.endDateDay}")),
                errorMessageForEndDate = Some("End date cannot precede start date")
              )
            )

          }
        }
        "neither start date nor end date is valid" in {

          val values = validValuesForSaving ++ Map(
            FieldNames.startDateDay   -> "12",
            FieldNames.startDateMonth -> "14",
            FieldNames.startDateYear  -> "2020",
            FieldNames.endDateDay     -> "42",
            FieldNames.endDateMonth   -> "12",
            FieldNames.endDateYear    -> "2020"
          )

          val result = submitWhileLoggedIn(1, "COAL.2022.V1RJW.P", values)

          status(result) mustBe BAD_REQUEST
          assertPageAsExpected(
            asDocument(result),
            ExpectedEditRecordPage(
              title = "Edit record",
              heading = "TNA reference: COAL 80/80/1",
              legend = "Intellectual properties",
              classicCatalogueRef = "COAL 80/80/1",
              omegaCatalogueId = "COAL.2022.V1RJW.P",
              scopeAndContent =
                "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
              coveringDates = "2020 Oct",
              formerReferenceDepartment = "1234",
              startDate = ExpectedDate("12", "14", "2020"),
              endDate = ExpectedDate("42", "12", "2020"),
              legalStatus = "ref.1",
              note = "Need to check copyright info.",
              background = "Photo was taken by a daughter of one of the coal miners who used them.",
              optionsForPlaceOfDeposit = Seq(
                ExpectedSelectOption("", "Select where this record is held", disabled = true),
                ExpectedSelectOption("1", "The National Archives, Kew"),
                ExpectedSelectOption("2", "British Museum, Department of Libraries and Archives", selected = true),
                ExpectedSelectOption("3", "British Library, National Sound Archive")
              ),
              custodialHistory = "Files originally created by successor or predecessor departments for COAL",
              summaryErrorMessages = Seq(
                ExpectedSummaryErrorMessage("Start date is not a valid date", s"#${FieldNames.startDateDay}"),
                ExpectedSummaryErrorMessage("End date is not a valid date", s"#${FieldNames.endDateDay}")
              ),
              errorMessageForStartDate = Some("Start date is not a valid date"),
              errorMessageForEndDate = Some("End date is not a valid date"),
              separatedMaterial = Seq(
                ExpectedSeparatedMaterial(
                  linkHref = Some("#;"),
                  linkText = Some("COAL 80/80/5")
                ),
                ExpectedSeparatedMaterial(
                  linkHref = Some("#;"),
                  linkText = Some("COAL 80/80/6")
                ),
                ExpectedSeparatedMaterial(
                  linkHref = Some("#;"),
                  linkText = Some("COAL 80/80/7")
                )
              ),
              relatedMaterial = Seq(
                ExpectedRelatedMaterial(
                  description =
                    Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
                ),
                ExpectedRelatedMaterial(
                  linkHref = Some("#;"),
                  linkText = Some("COAL 80/80/3")
                ),
                ExpectedRelatedMaterial(
                  linkHref = Some("#;"),
                  linkText = Some("COAL 80/80/2"),
                  description =
                    Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
                )
              )
            )
          )

        }
        "covering date" when {
          "is invalid" in {

            val values = validValuesForSaving ++ Map(
              FieldNames.coveringDates -> "Oct 1 2004"
            )

            val result = submitWhileLoggedIn(1, "COAL.2022.V1RJW.P", values)

            status(result) mustBe BAD_REQUEST
            assertPageAsExpected(
              asDocument(result),
              ExpectedEditRecordPage(
                title = "Edit record",
                heading = "TNA reference: COAL 80/80/1",
                legend = "Intellectual properties",
                classicCatalogueRef = "COAL 80/80/1",
                omegaCatalogueId = "COAL.2022.V1RJW.P",
                scopeAndContent =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
                coveringDates = "Oct 1 2004",
                formerReferenceDepartment = "1234",
                startDate = ExpectedDate("1", "10", "2020"),
                endDate = ExpectedDate("31", "10", "2020"),
                legalStatus = "ref.1",
                note = "Need to check copyright info.",
                background = "Photo was taken by a daughter of one of the coal miners who used them.",
                optionsForPlaceOfDeposit = Seq(
                  ExpectedSelectOption("", "Select where this record is held", disabled = true),
                  ExpectedSelectOption("1", "The National Archives, Kew"),
                  ExpectedSelectOption("2", "British Museum, Department of Libraries and Archives", selected = true),
                  ExpectedSelectOption("3", "British Library, National Sound Archive")
                ),
                separatedMaterial = Seq(
                  ExpectedSeparatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/5")
                  ),
                  ExpectedSeparatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/6")
                  ),
                  ExpectedSeparatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/7")
                  )
                ),
                relatedMaterial = Seq(
                  ExpectedRelatedMaterial(
                    description =
                      Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
                  ),
                  ExpectedRelatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/3")
                  ),
                  ExpectedRelatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/2"),
                    description =
                      Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
                  )
                ),
                custodialHistory = "Files originally created by successor or predecessor departments for COAL",
                summaryErrorMessages = Seq(
                  ExpectedSummaryErrorMessage("Covering date format is not valid", s"#${FieldNames.coveringDates}")
                ),
                errorMessageForCoveringsDates = Some("Covering date format is not valid")
              )
            )

          }
          "is too long" in {
            val gapDateTooLong = (1 to 100).map(_ => "2004 Oct 1").mkString(";")
            val values = validValuesForSaving ++ Map(
              FieldNames.coveringDates -> gapDateTooLong
            )

            val result = submitWhileLoggedIn(1, "COAL.2022.V1RJW.P", values)

            status(result) mustBe BAD_REQUEST
            assertPageAsExpected(
              asDocument(result),
              ExpectedEditRecordPage(
                title = "Edit record",
                heading = "TNA reference: COAL 80/80/1",
                legend = "Intellectual properties",
                classicCatalogueRef = "COAL 80/80/1",
                omegaCatalogueId = "COAL.2022.V1RJW.P",
                scopeAndContent =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
                coveringDates = gapDateTooLong,
                formerReferenceDepartment = "1234",
                startDate = ExpectedDate("1", "10", "2020"),
                endDate = ExpectedDate("31", "10", "2020"),
                legalStatus = "ref.1",
                note = "Need to check copyright info.",
                background = "Photo was taken by a daughter of one of the coal miners who used them.",
                optionsForPlaceOfDeposit = Seq(
                  ExpectedSelectOption("", "Select where this record is held", disabled = true),
                  ExpectedSelectOption("1", "The National Archives, Kew"),
                  ExpectedSelectOption("2", "British Museum, Department of Libraries and Archives", selected = true),
                  ExpectedSelectOption("3", "British Library, National Sound Archive")
                ),
                separatedMaterial = Seq(
                  ExpectedSeparatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/5")
                  ),
                  ExpectedSeparatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/6")
                  ),
                  ExpectedSeparatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/7")
                  )
                ),
                relatedMaterial = Seq(
                  ExpectedRelatedMaterial(
                    description =
                      Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
                  ),
                  ExpectedRelatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/3")
                  ),
                  ExpectedRelatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/2"),
                    description =
                      Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
                  )
                ),
                custodialHistory = "Files originally created by successor or predecessor departments for COAL",
                summaryErrorMessages = Seq(
                  ExpectedSummaryErrorMessage(
                    "Covering date too long, maximum length 255 characters",
                    s"#${FieldNames.coveringDates}"
                  )
                ),
                errorMessageForCoveringsDates = Some("Covering date too long, maximum length 255 characters")
              )
            )

          }
          "is empty; showing error correctly" in {

            val values = validValuesForSaving ++ Map(
              FieldNames.coveringDates -> "  "
            )

            val result = submitWhileLoggedIn(1, "COAL.2022.V1RJW.P", values)

            status(result) mustBe BAD_REQUEST
            assertPageAsExpected(
              asDocument(result),
              ExpectedEditRecordPage(
                title = "Edit record",
                heading = "TNA reference: COAL 80/80/1",
                legend = "Intellectual properties",
                classicCatalogueRef = "COAL 80/80/1",
                omegaCatalogueId = "COAL.2022.V1RJW.P",
                scopeAndContent =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
                coveringDates = "  ",
                formerReferenceDepartment = "1234",
                startDate = ExpectedDate("1", "10", "2020"),
                endDate = ExpectedDate("31", "10", "2020"),
                legalStatus = "ref.1",
                note = "Need to check copyright info.",
                background = "Photo was taken by a daughter of one of the coal miners who used them.",
                optionsForPlaceOfDeposit = Seq(
                  ExpectedSelectOption("", "Select where this record is held", disabled = true),
                  ExpectedSelectOption("1", "The National Archives, Kew"),
                  ExpectedSelectOption("2", "British Museum, Department of Libraries and Archives", selected = true),
                  ExpectedSelectOption("3", "British Library, National Sound Archive")
                ),
                separatedMaterial = Seq(
                  ExpectedSeparatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/5")
                  ),
                  ExpectedSeparatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/6")
                  ),
                  ExpectedSeparatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/7")
                  )
                ),
                relatedMaterial = Seq(
                  ExpectedRelatedMaterial(
                    description =
                      Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
                  ),
                  ExpectedRelatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/3")
                  ),
                  ExpectedRelatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/2"),
                    description =
                      Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
                  )
                ),
                custodialHistory = "Files originally created by successor or predecessor departments for COAL",
                summaryErrorMessages = Seq(
                  ExpectedSummaryErrorMessage("Enter the covering dates", s"#${FieldNames.coveringDates}"),
                  ExpectedSummaryErrorMessage("Covering date format is not valid", s"#${FieldNames.coveringDates}")
                ),
                errorMessageForCoveringsDates = Some("Enter the covering dates")
              )
            )

          }
        }

        "place of deposit" when {
          "isn't selected" in {

            val values =
              validValuesForSaving ++ Map(FieldNames.placeOfDeposit -> "")

            val result = submitWhileLoggedIn(1, "COAL.2022.V1RJW.P", values)

            status(result) mustBe BAD_REQUEST
            assertPageAsExpected(
              asDocument(result),
              ExpectedEditRecordPage(
                title = "Edit record",
                heading = "TNA reference: COAL 80/80/1",
                legend = "Intellectual properties",
                classicCatalogueRef = "COAL 80/80/1",
                omegaCatalogueId = "COAL.2022.V1RJW.P",
                scopeAndContent =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
                coveringDates = "2020 Oct",
                formerReferenceDepartment = "1234",
                startDate = ExpectedDate("1", "10", "2020"),
                endDate = ExpectedDate("31", "10", "2020"),
                legalStatus = "ref.1",
                note = "Need to check copyright info.",
                background = "Photo was taken by a daughter of one of the coal miners who used them.",
                optionsForPlaceOfDeposit = Seq(
                  ExpectedSelectOption("", "Select where this record is held", selected = true, disabled = true),
                  ExpectedSelectOption("1", "The National Archives, Kew"),
                  ExpectedSelectOption("2", "British Museum, Department of Libraries and Archives"),
                  ExpectedSelectOption("3", "British Library, National Sound Archive")
                ),
                separatedMaterial = Seq(
                  ExpectedSeparatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/5")
                  ),
                  ExpectedSeparatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/6")
                  ),
                  ExpectedSeparatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/7")
                  )
                ),
                relatedMaterial = Seq(
                  ExpectedRelatedMaterial(
                    description =
                      Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
                  ),
                  ExpectedRelatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/3")
                  ),
                  ExpectedRelatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/2"),
                    description =
                      Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
                  )
                ),
                custodialHistory = "Files originally created by successor or predecessor departments for COAL",
                summaryErrorMessages =
                  Seq(ExpectedSummaryErrorMessage("You must choose an option", s"#${FieldNames.placeOfDeposit}")),
                errorMessageForPlaceOfDeposit = Some("You must choose an option")
              )
            )

          }
          "is absent" in {

            val values = validValuesForSaving.removed(FieldNames.placeOfDeposit)

            val result = submitWhileLoggedIn(1, "COAL.2022.V1RJW.P", values)

            status(result) mustBe BAD_REQUEST
            assertPageAsExpected(
              asDocument(result),
              ExpectedEditRecordPage(
                title = "Edit record",
                heading = "TNA reference: COAL 80/80/1",
                legend = "Intellectual properties",
                classicCatalogueRef = "COAL 80/80/1",
                omegaCatalogueId = "COAL.2022.V1RJW.P",
                scopeAndContent =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
                coveringDates = "2020 Oct",
                formerReferenceDepartment = "1234",
                startDate = ExpectedDate("1", "10", "2020"),
                endDate = ExpectedDate("31", "10", "2020"),
                legalStatus = "ref.1",
                note = "Need to check copyright info.",
                background = "Photo was taken by a daughter of one of the coal miners who used them.",
                optionsForPlaceOfDeposit = Seq(
                  ExpectedSelectOption("", "Select where this record is held", selected = true, disabled = true),
                  ExpectedSelectOption("1", "The National Archives, Kew"),
                  ExpectedSelectOption("2", "British Museum, Department of Libraries and Archives"),
                  ExpectedSelectOption("3", "British Library, National Sound Archive")
                ),
                separatedMaterial = Seq(
                  ExpectedSeparatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/5")
                  ),
                  ExpectedSeparatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/6")
                  ),
                  ExpectedSeparatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/7")
                  )
                ),
                relatedMaterial = Seq(
                  ExpectedRelatedMaterial(
                    description =
                      Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
                  ),
                  ExpectedRelatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/3")
                  ),
                  ExpectedRelatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/2"),
                    description =
                      Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
                  )
                ),
                custodialHistory = "Files originally created by successor or predecessor departments for COAL",
                summaryErrorMessages =
                  Seq(ExpectedSummaryErrorMessage("You must choose an option", s"#${FieldNames.placeOfDeposit}")),
                errorMessageForPlaceOfDeposit = Some("You must choose an option")
              )
            )

          }
          "isn't recognised" in {

            val values = validValuesForSaving ++ Map(
              FieldNames.placeOfDeposit -> "6"
            )

            val result = submitWhileLoggedIn(1, "COAL.2022.V1RJW.P", values)

            status(result) mustBe BAD_REQUEST
            assertPageAsExpected(
              asDocument(result),
              ExpectedEditRecordPage(
                title = "Edit record",
                heading = "TNA reference: COAL 80/80/1",
                legend = "Intellectual properties",
                classicCatalogueRef = "COAL 80/80/1",
                omegaCatalogueId = "COAL.2022.V1RJW.P",
                scopeAndContent =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
                coveringDates = "2020 Oct",
                formerReferenceDepartment = "1234",
                startDate = ExpectedDate("1", "10", "2020"),
                endDate = ExpectedDate("31", "10", "2020"),
                legalStatus = "ref.1",
                note = "Need to check copyright info.",
                background = "Photo was taken by a daughter of one of the coal miners who used them.",
                optionsForPlaceOfDeposit = Seq(
                  ExpectedSelectOption("", "Select where this record is held", selected = true, disabled = true),
                  ExpectedSelectOption("1", "The National Archives, Kew"),
                  ExpectedSelectOption("2", "British Museum, Department of Libraries and Archives"),
                  ExpectedSelectOption("3", "British Library, National Sound Archive")
                ),
                separatedMaterial = Seq(
                  ExpectedSeparatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/5")
                  ),
                  ExpectedSeparatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/6")
                  ),
                  ExpectedSeparatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/7")
                  )
                ),
                relatedMaterial = Seq(
                  ExpectedRelatedMaterial(
                    description =
                      Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
                  ),
                  ExpectedRelatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/3")
                  ),
                  ExpectedRelatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/2"),
                    description =
                      Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
                  )
                ),
                custodialHistory = "Files originally created by successor or predecessor departments for COAL",
                summaryErrorMessages =
                  Seq(ExpectedSummaryErrorMessage("You must choose an option", s"#${FieldNames.placeOfDeposit}")),
                errorMessageForPlaceOfDeposit = Some("You must choose an option")
              )
            )

          }
        }
        "legal status" when {
          "is not selected" in {

            val values =
              validValuesForSaving ++ Map(FieldNames.legalStatus -> "")

            val result = submitWhileLoggedIn(1, "COAL.2022.V1RJW.P", values)

            status(result) mustBe BAD_REQUEST
            assertPageAsExpected(
              asDocument(result),
              ExpectedEditRecordPage(
                title = "Edit record",
                heading = "TNA reference: COAL 80/80/1",
                legend = "Intellectual properties",
                classicCatalogueRef = "COAL 80/80/1",
                omegaCatalogueId = "COAL.2022.V1RJW.P",
                scopeAndContent =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
                coveringDates = "2020 Oct",
                formerReferenceDepartment = "1234",
                startDate = ExpectedDate("1", "10", "2020"),
                endDate = ExpectedDate("31", "10", "2020"),
                legalStatus = "",
                note = "Need to check copyright info.",
                background = "Photo was taken by a daughter of one of the coal miners who used them.",
                optionsForPlaceOfDeposit = Seq(
                  ExpectedSelectOption("", "Select where this record is held", disabled = true),
                  ExpectedSelectOption("1", "The National Archives, Kew"),
                  ExpectedSelectOption("2", "British Museum, Department of Libraries and Archives", selected = true),
                  ExpectedSelectOption("3", "British Library, National Sound Archive")
                ),
                separatedMaterial = Seq(
                  ExpectedSeparatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/5")
                  ),
                  ExpectedSeparatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/6")
                  ),
                  ExpectedSeparatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/7")
                  )
                ),
                relatedMaterial = Seq(
                  ExpectedRelatedMaterial(
                    description =
                      Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
                  ),
                  ExpectedRelatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/3")
                  ),
                  ExpectedRelatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/2"),
                    description =
                      Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
                  )
                ),
                custodialHistory = "Files originally created by successor or predecessor departments for COAL",
                summaryErrorMessages =
                  Seq(ExpectedSummaryErrorMessage("You must choose an option", s"#${FieldNames.legalStatus}")),
                errorMessageForLegalStatus = Some("Error: You must choose an option")
              )
            )

          }

          "value doesn't exist" in {

            val values =
              validValuesForSaving ++ Map(
                FieldNames.legalStatus -> "ref.10"
              )

            val result = submitWhileLoggedIn(1, "COAL.2022.V1RJW.P", values)

            status(result) mustBe SEE_OTHER

            redirectLocation(result) mustBe Some("/edit-set/1/record/COAL.2022.V1RJW.P/edit/save")

          }
        }
        "note" when {
          "is too long" in {

            val excessivelyLongNote = "Something about something else." * 100
            val values = validValuesForSaving ++ Map(
              FieldNames.note -> excessivelyLongNote
            )

            val result = submitWhileLoggedIn(1, "COAL.2022.V1RJW.P", values)

            status(result) mustBe BAD_REQUEST
            assertPageAsExpected(
              asDocument(result),
              ExpectedEditRecordPage(
                title = "Edit record",
                heading = "TNA reference: COAL 80/80/1",
                legend = "Intellectual properties",
                classicCatalogueRef = "COAL 80/80/1",
                omegaCatalogueId = "COAL.2022.V1RJW.P",
                scopeAndContent =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
                coveringDates = "2020 Oct",
                formerReferenceDepartment = "1234",
                startDate = ExpectedDate("1", "10", "2020"),
                endDate = ExpectedDate("31", "10", "2020"),
                legalStatus = "ref.1",
                note = excessivelyLongNote,
                background = "Photo was taken by a daughter of one of the coal miners who used them.",
                optionsForPlaceOfDeposit = Seq(
                  ExpectedSelectOption("", "Select where this record is held", disabled = true),
                  ExpectedSelectOption("1", "The National Archives, Kew"),
                  ExpectedSelectOption("2", "British Museum, Department of Libraries and Archives", selected = true),
                  ExpectedSelectOption("3", "British Library, National Sound Archive")
                ),
                separatedMaterial = Seq(
                  ExpectedSeparatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/5")
                  ),
                  ExpectedSeparatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/6")
                  ),
                  ExpectedSeparatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/7")
                  )
                ),
                relatedMaterial = Seq(
                  ExpectedRelatedMaterial(
                    description =
                      Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
                  ),
                  ExpectedRelatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/3")
                  ),
                  ExpectedRelatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/2"),
                    description =
                      Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
                  )
                ),
                custodialHistory = "Files originally created by successor or predecessor departments for COAL",
                summaryErrorMessages = Seq(
                  ExpectedSummaryErrorMessage("Note too long, maximum length 1000 characters", s"#${FieldNames.note}")
                ),
                errorMessageForNote = Some("Note too long, maximum length 1000 characters")
              )
            )

          }
        }
        "background" when {
          "is too long" in {

            val excessivelyLongBackground = "Something about one of the people." * 400
            val values = validValuesForSaving ++ Map(
              FieldNames.background -> excessivelyLongBackground
            )
            val result = submitWhileLoggedIn(1, "COAL.2022.V1RJW.P", values)

            status(result) mustBe BAD_REQUEST
            assertPageAsExpected(
              asDocument(result),
              ExpectedEditRecordPage(
                title = "Edit record",
                heading = "TNA reference: COAL 80/80/1",
                legend = "Intellectual properties",
                classicCatalogueRef = "COAL 80/80/1",
                omegaCatalogueId = "COAL.2022.V1RJW.P",
                scopeAndContent =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
                coveringDates = "2020 Oct",
                formerReferenceDepartment = "1234",
                startDate = ExpectedDate("1", "10", "2020"),
                endDate = ExpectedDate("31", "10", "2020"),
                legalStatus = "ref.1",
                note = "Need to check copyright info.",
                background = excessivelyLongBackground,
                optionsForPlaceOfDeposit = Seq(
                  ExpectedSelectOption("", "Select where this record is held", disabled = true),
                  ExpectedSelectOption("1", "The National Archives, Kew"),
                  ExpectedSelectOption("2", "British Museum, Department of Libraries and Archives", selected = true),
                  ExpectedSelectOption("3", "British Library, National Sound Archive")
                ),
                separatedMaterial = Seq(
                  ExpectedSeparatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/5")
                  ),
                  ExpectedSeparatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/6")
                  ),
                  ExpectedSeparatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/7")
                  )
                ),
                relatedMaterial = Seq(
                  ExpectedRelatedMaterial(
                    description =
                      Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
                  ),
                  ExpectedRelatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/3")
                  ),
                  ExpectedRelatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/2"),
                    description =
                      Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
                  )
                ),
                custodialHistory = "Files originally created by successor or predecessor departments for COAL",
                summaryErrorMessages = Seq(
                  ExpectedSummaryErrorMessage(
                    "Administrative / biographical background too long, maximum length 8000 characters",
                    s"#${FieldNames.background}"
                  )
                ),
                errorMessageForBackground =
                  Some("Administrative / biographical background too long, maximum length 8000 characters")
              )
            )
          }
        }

        "custodial history" when {
          "is too long" in {

            val custodialHistoryTooLong =
              "Files originally created by successor or predecessor departments for COAL" * 100
            val values = validValuesForSaving ++ Map(
              FieldNames.custodialHistory -> custodialHistoryTooLong
            )

            val result = submitWhileLoggedIn(1, "COAL.2022.V1RJW.P", values)

            status(result) mustBe BAD_REQUEST
            assertPageAsExpected(
              asDocument(result),
              ExpectedEditRecordPage(
                title = "Edit record",
                heading = "TNA reference: COAL 80/80/1",
                legend = "Intellectual properties",
                classicCatalogueRef = "COAL 80/80/1",
                omegaCatalogueId = "COAL.2022.V1RJW.P",
                scopeAndContent =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
                coveringDates = "2020 Oct",
                formerReferenceDepartment = "1234",
                startDate = ExpectedDate("1", "10", "2020"),
                endDate = ExpectedDate("31", "10", "2020"),
                legalStatus = "ref.1",
                note = "Need to check copyright info.",
                background = "Photo was taken by a daughter of one of the coal miners who used them.",
                optionsForPlaceOfDeposit = Seq(
                  ExpectedSelectOption("", "Select where this record is held", disabled = true),
                  ExpectedSelectOption("1", "The National Archives, Kew"),
                  ExpectedSelectOption("2", "British Museum, Department of Libraries and Archives", selected = true),
                  ExpectedSelectOption("3", "British Library, National Sound Archive")
                ),
                custodialHistory = custodialHistoryTooLong,
                relatedMaterial = Seq(
                  ExpectedRelatedMaterial(
                    description =
                      Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
                  ),
                  ExpectedRelatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/3")
                  ),
                  ExpectedRelatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/2"),
                    description =
                      Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
                  )
                ),
                separatedMaterial = Seq(
                  ExpectedSeparatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/5")
                  ),
                  ExpectedSeparatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/6")
                  ),
                  ExpectedSeparatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/7")
                  )
                ),
                summaryErrorMessages = Seq(
                  ExpectedSummaryErrorMessage(
                    "Custodial history too long, maximum length 1000 characters",
                    s"#${FieldNames.custodialHistory}"
                  )
                ),
                errorMessageForCustodialHistory = Some("Custodial history too long, maximum length 1000 characters")
              )
            )

          }
        }
      }
      "successful" when {
        "redirect to result page from a new instance of controller" in {
          val messages: Map[String, Map[String, String]] =
            Map("en" -> Map("edit-set.record.edit.heading" -> "TNA reference: COAL 80/80/1"))
          val mockMessagesApi = stubMessagesApi(messages)
          val editSetInstance = inject[editSet]
          val editSetRecordEditInstance = inject[editSetRecordEdit]
          val editSetRecordEditDiscardInstance = inject[editSetRecordEditDiscard]
          val editSetRecordEditSaveInstance = inject[editSetRecordEditSave]
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
            editSetInstance,
            editSetRecordEditInstance,
            editSetRecordEditDiscardInstance,
            editSetRecordEditSaveInstance
          )

          val values = validValuesForSaving

          val editRecordPage = controller
            .submit("1", "COAL.2022.V1RJW.P")
            .apply(
              CSRFTokenHelper
                .addCSRFToken(
                  FakeRequest(POST, "/edit-set/1/record/COAL.2022.V1RJW.P/edit")
                    .withFormUrlEncodedBody(values.toSeq: _*)
                    .withSession(SessionKeys.token -> validSessionToken)
                )
            )

          status(editRecordPage) mustBe SEE_OTHER
          redirectLocation(editRecordPage) mustBe Some("/edit-set/1/record/COAL.2022.V1RJW.P/edit/save")
        }

        "redirect to result page of the application" in {
          val controller = inject[EditSetController]
          val values = validValuesForSaving
          val editRecordPage = controller
            .submit("1", "COAL.2022.V1RJW.P")
            .apply(
              CSRFTokenHelper.addCSRFToken(
                FakeRequest(POST, "/edit-set/1/record/COAL.2022.V1RJW.P/edit")
                  .withFormUrlEncodedBody(values.toSeq: _*)
                  .withSession(SessionKeys.token -> validSessionToken)
              )
            )

          status(editRecordPage) mustBe SEE_OTHER
          redirectLocation(editRecordPage) mustBe Some("/edit-set/1/record/COAL.2022.V1RJW.P/edit/save")
        }

        "redirect to result page from the router" when {

          "all fields are provided" in {

            val values = validValuesForSaving
            val editRecordPageResponse = submitWhileLoggedIn(1, "COAL.2022.V1RJW.P", values)

            status(editRecordPageResponse) mustBe SEE_OTHER
            redirectLocation(editRecordPageResponse) mustBe Some("/edit-set/1/record/COAL.2022.V1RJW.P/edit/save")

            val getRecordResult = getRecordForEditingWhileLoggedIn(1, "COAL.2022.V1RJW.P")
            assertPageAsExpected(
              asDocument(getRecordResult),
              ExpectedEditRecordPage(
                title = "Edit record",
                heading = "TNA reference: COAL 80/80/1",
                legend = "Intellectual properties",
                classicCatalogueRef = "COAL 80/80/1",
                omegaCatalogueId = "COAL.2022.V1RJW.P",
                scopeAndContent =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
                coveringDates = "2020 Oct",
                formerReferenceDepartment = "1234",
                startDate = ExpectedDate("1", "10", "2020"),
                endDate = ExpectedDate("31", "10", "2020"),
                legalStatus = "ref.1",
                note = "Need to check copyright info.",
                background = "Photo was taken by a daughter of one of the coal miners who used them.",
                optionsForPlaceOfDeposit = Seq(
                  ExpectedSelectOption("", "Select where this record is held", disabled = true),
                  ExpectedSelectOption("1", "The National Archives, Kew"),
                  ExpectedSelectOption("2", "British Museum, Department of Libraries and Archives", selected = true),
                  ExpectedSelectOption("3", "British Library, National Sound Archive")
                ),
                separatedMaterial = Seq(
                  ExpectedSeparatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/5")
                  ),
                  ExpectedSeparatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/6")
                  ),
                  ExpectedSeparatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/7")
                  )
                ),
                relatedMaterial = Seq(
                  ExpectedRelatedMaterial(
                    description =
                      Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
                  ),
                  ExpectedRelatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/3")
                  ),
                  ExpectedRelatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/2"),
                    description =
                      Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
                  )
                ),
                custodialHistory = "Files originally created by successor or predecessor departments for COAL"
              )
            )

          }

          "the 'note' field is blank" in {

            val values =
              validValuesForSaving ++ Map(FieldNames.note -> "")

            val editRecordPageResponse = submitWhileLoggedIn(12, "COAL.2022.V12RJW.P", values)

            status(editRecordPageResponse) mustBe SEE_OTHER
            redirectLocation(editRecordPageResponse) mustBe Some("/edit-set/12/record/COAL.2022.V12RJW.P/edit/save")

            val getRecordResult = getRecordForEditingWhileLoggedIn(12, "COAL.2022.V12RJW.P")
            assertPageAsExpected(
              asDocument(getRecordResult),
              ExpectedEditRecordPage(
                title = "Edit record",
                heading = "TNA reference: COAL 80/80/12",
                legend = "Intellectual properties",
                classicCatalogueRef = "COAL 80/80/12",
                omegaCatalogueId = "COAL.2022.V12RJW.P",
                scopeAndContent =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
                coveringDates = "2020 Oct",
                formerReferenceDepartment = "1234",
                startDate = ExpectedDate("1", "10", "2020"),
                endDate = ExpectedDate("31", "10", "2020"),
                legalStatus = "ref.1",
                note = "",
                background = "Photo was taken by a daughter of one of the coal miners who used them.",
                optionsForPlaceOfDeposit = Seq(
                  ExpectedSelectOption("", "Select where this record is held", disabled = true),
                  ExpectedSelectOption("1", "The National Archives, Kew"),
                  ExpectedSelectOption("2", "British Museum, Department of Libraries and Archives", selected = true),
                  ExpectedSelectOption("3", "British Library, National Sound Archive")
                ),
                custodialHistory = "Files originally created by successor or predecessor departments for COAL",
                separatedMaterial = Seq(
                  ExpectedSeparatedMaterial(
                    description =
                      Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
                  ),
                  ExpectedSeparatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/5")
                  ),
                  ExpectedSeparatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/8"),
                    description =
                      Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
                  )
                )
              )
            )

          }

          "the 'custodial history' field is blank" in {

            val values = validValuesForSaving ++ Map(
              FieldNames.custodialHistory -> ""
            )

            val editRecordPageResponse = submitWhileLoggedIn(1, "COAL.2022.V1RJW.P", values)

            status(editRecordPageResponse) mustBe SEE_OTHER
            redirectLocation(editRecordPageResponse) mustBe Some("/edit-set/1/record/COAL.2022.V1RJW.P/edit/save")

            val getRecordResult = getRecordForEditingWhileLoggedIn(1, "COAL.2022.V1RJW.P")
            assertPageAsExpected(
              asDocument(getRecordResult),
              ExpectedEditRecordPage(
                title = "Edit record",
                heading = "TNA reference: COAL 80/80/1",
                legend = "Intellectual properties",
                classicCatalogueRef = "COAL 80/80/1",
                omegaCatalogueId = "COAL.2022.V1RJW.P",
                scopeAndContent =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
                coveringDates = "2020 Oct",
                formerReferenceDepartment = "1234",
                startDate = ExpectedDate("1", "10", "2020"),
                endDate = ExpectedDate("31", "10", "2020"),
                legalStatus = "ref.1",
                note = "Need to check copyright info.",
                background = "Photo was taken by a daughter of one of the coal miners who used them.",
                optionsForPlaceOfDeposit = Seq(
                  ExpectedSelectOption("", "Select where this record is held", disabled = true),
                  ExpectedSelectOption("1", "The National Archives, Kew"),
                  ExpectedSelectOption("2", "British Museum, Department of Libraries and Archives", selected = true),
                  ExpectedSelectOption("3", "British Library, National Sound Archive")
                ),
                relatedMaterial = Seq(
                  ExpectedRelatedMaterial(
                    description =
                      Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
                  ),
                  ExpectedRelatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/3")
                  ),
                  ExpectedRelatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/2"),
                    description =
                      Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
                  )
                ),
                separatedMaterial = Seq(
                  ExpectedSeparatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/5")
                  ),
                  ExpectedSeparatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/6")
                  ),
                  ExpectedSeparatedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/7")
                  )
                ),
                custodialHistory = ""
              )
            )

          }
          "the 'background' field is blank" in {

            val values =
              validValuesForSaving ++ Map(FieldNames.background -> "")

            val editRecordPageResponse = submitWhileLoggedIn(1, "COAL.2022.V5RJW.P", values)

            status(editRecordPageResponse) mustBe SEE_OTHER
            redirectLocation(editRecordPageResponse) mustBe Some("/edit-set/1/record/COAL.2022.V5RJW.P/edit/save")

            val getRecordResult = getRecordForEditingWhileLoggedIn(1, "COAL.2022.V5RJW.P")
            assertPageAsExpected(
              asDocument(getRecordResult),
              ExpectedEditRecordPage(
                title = "Edit record",
                heading = "TNA reference: COAL 80/80/5",
                legend = "Intellectual properties",
                classicCatalogueRef = "COAL 80/80/5",
                omegaCatalogueId = "COAL.2022.V5RJW.P",
                scopeAndContent =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
                coveringDates = "2020 Oct",
                formerReferenceDepartment = "1234",
                startDate = ExpectedDate("1", "10", "2020"),
                endDate = ExpectedDate("31", "10", "2020"),
                legalStatus = "ref.1",
                note = "Need to check copyright info.",
                background = "",
                optionsForPlaceOfDeposit = Seq(
                  ExpectedSelectOption("", "Select where this record is held", disabled = true),
                  ExpectedSelectOption("1", "The National Archives, Kew"),
                  ExpectedSelectOption("2", "British Museum, Department of Libraries and Archives", selected = true),
                  ExpectedSelectOption("3", "British Library, National Sound Archive")
                ),
                separatedMaterial = Seq.empty,
                relatedMaterial = Seq.empty,
                custodialHistory = "Files originally created by successor or predecessor departments for COAL"
              )
            )
          }
        }
      }
    }

    "when the action is to discard all changes" when {

      val validValuesForDiscarding = validValues ++ Map("action" -> "discard")

      "successful" when {
        "even if the validation fails" in {

          val blankScopeAndContentToFailValidation = ""
          val values = validValuesForDiscarding ++ Map("coveringDates" -> blankScopeAndContentToFailValidation)

          val result = submitWhileLoggedIn(1, "COAL.2022.V1RJW.P", values)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some("/edit-set/1/record/COAL.2022.V1RJW.P/edit/discard")

        }
      }
    }

    "when the action is to calculate the start and end dates from the covering dates" when {

      val validValuesForCalculatingDates = validValues ++ Map("action" -> "calculateDates")

      "failure" when {
        "blank" in {

          val values = validValuesForCalculatingDates ++ Map(
            FieldNames.coveringDates  -> "   ",
            FieldNames.startDateDay   -> "1",
            FieldNames.startDateMonth -> "10",
            FieldNames.startDateYear  -> "2020",
            FieldNames.endDateDay     -> "31",
            FieldNames.endDateMonth   -> "10",
            FieldNames.endDateYear    -> "2020"
          )

          val result = submitWhileLoggedIn(1, "COAL.2022.V1RJW.P", values)

          status(result) mustBe BAD_REQUEST
          assertPageAsExpected(
            asDocument(result),
            ExpectedEditRecordPage(
              title = "Edit record",
              heading = "TNA reference: COAL 80/80/1",
              legend = "Intellectual properties",
              classicCatalogueRef = "COAL 80/80/1",
              omegaCatalogueId = "COAL.2022.V1RJW.P",
              scopeAndContent =
                "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
              coveringDates = "   ",
              formerReferenceDepartment = "1234",
              startDate = ExpectedDate("1", "10", "2020"),
              endDate = ExpectedDate("31", "10", "2020"),
              legalStatus = "ref.1",
              note = "Need to check copyright info.",
              background = "Photo was taken by a daughter of one of the coal miners who used them.",
              optionsForPlaceOfDeposit = Seq(
                ExpectedSelectOption("", "Select where this record is held", disabled = true),
                ExpectedSelectOption("1", "The National Archives, Kew"),
                ExpectedSelectOption("2", "British Museum, Department of Libraries and Archives", selected = true),
                ExpectedSelectOption("3", "British Library, National Sound Archive")
              ),
              separatedMaterial = Seq(
                ExpectedSeparatedMaterial(
                  linkHref = Some("#;"),
                  linkText = Some("COAL 80/80/5")
                ),
                ExpectedSeparatedMaterial(
                  linkHref = Some("#;"),
                  linkText = Some("COAL 80/80/6")
                ),
                ExpectedSeparatedMaterial(
                  linkHref = Some("#;"),
                  linkText = Some("COAL 80/80/7")
                )
              ),
              relatedMaterial = Seq(
                ExpectedRelatedMaterial(
                  description =
                    Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
                ),
                ExpectedRelatedMaterial(
                  linkHref = Some("#;"),
                  linkText = Some("COAL 80/80/3")
                ),
                ExpectedRelatedMaterial(
                  linkHref = Some("#;"),
                  linkText = Some("COAL 80/80/2"),
                  description =
                    Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
                )
              ),
              custodialHistory = "Files originally created by successor or predecessor departments for COAL",
              summaryErrorMessages = Seq(
                ExpectedSummaryErrorMessage("Enter the covering dates", s"#${FieldNames.coveringDates}"),
                ExpectedSummaryErrorMessage("Covering date format is not valid", s"#${FieldNames.coveringDates}")
              ),
              errorMessageForCoveringsDates = Some("Enter the covering dates")
            )
          )

        }
        "invalid format" in {

          val values = validValuesForCalculatingDates ++ Map(
            FieldNames.coveringDates  -> "1270s",
            FieldNames.startDateDay   -> "1",
            FieldNames.startDateMonth -> "10",
            FieldNames.startDateYear  -> "2020",
            FieldNames.endDateDay     -> "31",
            FieldNames.endDateMonth   -> "10",
            FieldNames.endDateYear    -> "2020"
          )

          val result = submitWhileLoggedIn(1, "COAL.2022.V1RJW.P", values)

          status(result) mustBe BAD_REQUEST
          assertPageAsExpected(
            asDocument(result),
            ExpectedEditRecordPage(
              title = "Edit record",
              heading = "TNA reference: COAL 80/80/1",
              legend = "Intellectual properties",
              classicCatalogueRef = "COAL 80/80/1",
              omegaCatalogueId = "COAL.2022.V1RJW.P",
              scopeAndContent =
                "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
              coveringDates = "1270s",
              formerReferenceDepartment = "1234",
              startDate = ExpectedDate("1", "10", "2020"),
              endDate = ExpectedDate("31", "10", "2020"),
              legalStatus = "ref.1",
              note = "Need to check copyright info.",
              background = "Photo was taken by a daughter of one of the coal miners who used them.",
              optionsForPlaceOfDeposit = Seq(
                ExpectedSelectOption("", "Select where this record is held", disabled = true),
                ExpectedSelectOption("1", "The National Archives, Kew"),
                ExpectedSelectOption("2", "British Museum, Department of Libraries and Archives", selected = true),
                ExpectedSelectOption("3", "British Library, National Sound Archive")
              ),
              separatedMaterial = Seq(
                ExpectedSeparatedMaterial(
                  linkHref = Some("#;"),
                  linkText = Some("COAL 80/80/5")
                ),
                ExpectedSeparatedMaterial(
                  linkHref = Some("#;"),
                  linkText = Some("COAL 80/80/6")
                ),
                ExpectedSeparatedMaterial(
                  linkHref = Some("#;"),
                  linkText = Some("COAL 80/80/7")
                )
              ),
              relatedMaterial = Seq(
                ExpectedRelatedMaterial(
                  description =
                    Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
                ),
                ExpectedRelatedMaterial(
                  linkHref = Some("#;"),
                  linkText = Some("COAL 80/80/3")
                ),
                ExpectedRelatedMaterial(
                  linkHref = Some("#;"),
                  linkText = Some("COAL 80/80/2"),
                  description =
                    Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
                )
              ),
              custodialHistory = "Files originally created by successor or predecessor departments for COAL",
              summaryErrorMessages =
                Seq(ExpectedSummaryErrorMessage("Covering date format is not valid", s"#${FieldNames.coveringDates}")),
              errorMessageForCoveringsDates = Some("Covering date format is not valid")
            )
          )

        }
        "contains a non-existent date" in {

          val values = validValuesForCalculatingDates ++ Map(
            FieldNames.coveringDates  -> "2022 Feb 1-2022 Feb 29",
            FieldNames.startDateDay   -> "1",
            FieldNames.startDateMonth -> "10",
            FieldNames.startDateYear  -> "2020",
            FieldNames.endDateDay     -> "31",
            FieldNames.endDateMonth   -> "10",
            FieldNames.endDateYear    -> "2020"
          )

          val result = submitWhileLoggedIn(1, "COAL.2022.V1RJW.P", values)

          status(result) mustBe BAD_REQUEST
          assertPageAsExpected(
            asDocument(result),
            ExpectedEditRecordPage(
              title = "Edit record",
              heading = "TNA reference: COAL 80/80/1",
              legend = "Intellectual properties",
              classicCatalogueRef = "COAL 80/80/1",
              omegaCatalogueId = "COAL.2022.V1RJW.P",
              scopeAndContent =
                "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
              coveringDates = "2022 Feb 1-2022 Feb 29",
              formerReferenceDepartment = "1234",
              startDate = ExpectedDate("1", "10", "2020"),
              endDate = ExpectedDate("31", "10", "2020"),
              legalStatus = "ref.1",
              note = "Need to check copyright info.",
              background = "Photo was taken by a daughter of one of the coal miners who used them.",
              optionsForPlaceOfDeposit = Seq(
                ExpectedSelectOption("", "Select where this record is held", disabled = true),
                ExpectedSelectOption("1", "The National Archives, Kew"),
                ExpectedSelectOption("2", "British Museum, Department of Libraries and Archives", selected = true),
                ExpectedSelectOption("3", "British Library, National Sound Archive")
              ),
              separatedMaterial = Seq(
                ExpectedSeparatedMaterial(
                  linkHref = Some("#;"),
                  linkText = Some("COAL 80/80/5")
                ),
                ExpectedSeparatedMaterial(
                  linkHref = Some("#;"),
                  linkText = Some("COAL 80/80/6")
                ),
                ExpectedSeparatedMaterial(
                  linkHref = Some("#;"),
                  linkText = Some("COAL 80/80/7")
                )
              ),
              relatedMaterial = Seq(
                ExpectedRelatedMaterial(
                  description =
                    Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
                ),
                ExpectedRelatedMaterial(
                  linkHref = Some("#;"),
                  linkText = Some("COAL 80/80/3")
                ),
                ExpectedRelatedMaterial(
                  linkHref = Some("#;"),
                  linkText = Some("COAL 80/80/2"),
                  description =
                    Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
                )
              ),
              custodialHistory = "Files originally created by successor or predecessor departments for COAL",
              summaryErrorMessages =
                Seq(ExpectedSummaryErrorMessage("Covering date format is not valid", s"#${FieldNames.coveringDates}")),
              errorMessageForCoveringsDates = Some("Covering date format is not valid")
            )
          )

        }
      }
      "successful" when {
        "covers period of the switchover" in {

          val values = validValuesForCalculatingDates ++ Map(
            FieldNames.coveringDates -> "1752 Aug 1-1752 Sept 12"
          )

          val result = submitWhileLoggedIn(1, "COAL.2022.V1RJW.P", values)

          status(result) mustBe OK
          assertPageAsExpected(
            asDocument(result),
            ExpectedEditRecordPage(
              title = "Edit record",
              heading = "TNA reference: COAL 80/80/1",
              legend = "Intellectual properties",
              classicCatalogueRef = "COAL 80/80/1",
              omegaCatalogueId = "COAL.2022.V1RJW.P",
              scopeAndContent =
                "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
              coveringDates = "1752 Aug 1-1752 Sept 12",
              formerReferenceDepartment = "1234",
              startDate = ExpectedDate("1", "8", "1752"),
              endDate = ExpectedDate("12", "9", "1752"),
              legalStatus = "ref.1",
              note = "Need to check copyright info.",
              background = "Photo was taken by a daughter of one of the coal miners who used them.",
              optionsForPlaceOfDeposit = Seq(
                ExpectedSelectOption("", "Select where this record is held", disabled = true),
                ExpectedSelectOption("1", "The National Archives, Kew"),
                ExpectedSelectOption("2", "British Museum, Department of Libraries and Archives", selected = true),
                ExpectedSelectOption("3", "British Library, National Sound Archive")
              ),
              separatedMaterial = Seq(
                ExpectedSeparatedMaterial(
                  linkHref = Some("#;"),
                  linkText = Some("COAL 80/80/5")
                ),
                ExpectedSeparatedMaterial(
                  linkHref = Some("#;"),
                  linkText = Some("COAL 80/80/6")
                ),
                ExpectedSeparatedMaterial(
                  linkHref = Some("#;"),
                  linkText = Some("COAL 80/80/7")
                )
              ),
              relatedMaterial = Seq(
                ExpectedRelatedMaterial(
                  description =
                    Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
                ),
                ExpectedRelatedMaterial(
                  linkHref = Some("#;"),
                  linkText = Some("COAL 80/80/3")
                ),
                ExpectedRelatedMaterial(
                  linkHref = Some("#;"),
                  linkText = Some("COAL 80/80/2"),
                  description =
                    Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
                )
              ),
              custodialHistory = "Files originally created by successor or predecessor departments for COAL"
            )
          )

        }
        "covers period after the switchover" in {

          val values = validValuesForCalculatingDates ++ Map(
            FieldNames.coveringDates -> "1984 Dec"
          )

          val result = submitWhileLoggedIn(1, "COAL.2022.V1RJW.P", values)

          status(result) mustBe OK
          assertPageAsExpected(
            asDocument(result),
            ExpectedEditRecordPage(
              title = "Edit record",
              heading = "TNA reference: COAL 80/80/1",
              legend = "Intellectual properties",
              classicCatalogueRef = "COAL 80/80/1",
              omegaCatalogueId = "COAL.2022.V1RJW.P",
              scopeAndContent =
                "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
              coveringDates = "1984 Dec",
              formerReferenceDepartment = "1234",
              startDate = ExpectedDate("1", "12", "1984"),
              endDate = ExpectedDate("31", "12", "1984"),
              legalStatus = "ref.1",
              note = "Need to check copyright info.",
              background = "Photo was taken by a daughter of one of the coal miners who used them.",
              optionsForPlaceOfDeposit = Seq(
                ExpectedSelectOption("", "Select where this record is held", disabled = true),
                ExpectedSelectOption("1", "The National Archives, Kew"),
                ExpectedSelectOption("2", "British Museum, Department of Libraries and Archives", selected = true),
                ExpectedSelectOption("3", "British Library, National Sound Archive")
              ),
              separatedMaterial = Seq(
                ExpectedSeparatedMaterial(
                  linkHref = Some("#;"),
                  linkText = Some("COAL 80/80/5")
                ),
                ExpectedSeparatedMaterial(
                  linkHref = Some("#;"),
                  linkText = Some("COAL 80/80/6")
                ),
                ExpectedSeparatedMaterial(
                  linkHref = Some("#;"),
                  linkText = Some("COAL 80/80/7")
                )
              ),
              relatedMaterial = Seq(
                ExpectedRelatedMaterial(
                  description =
                    Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
                ),
                ExpectedRelatedMaterial(
                  linkHref = Some("#;"),
                  linkText = Some("COAL 80/80/3")
                ),
                ExpectedRelatedMaterial(
                  linkHref = Some("#;"),
                  linkText = Some("COAL 80/80/2"),
                  description =
                    Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
                )
              ),
              custodialHistory = "Files originally created by successor or predecessor departments for COAL"
            )
          )

        }
        "covers multiple ranges" in {

          val values = validValuesForCalculatingDates ++ Map(
            FieldNames.coveringDates -> "1868; 1890-1902; 1933"
          )

          val result = submitWhileLoggedIn(1, "COAL.2022.V1RJW.P", values)

          status(result) mustBe OK
          assertPageAsExpected(
            asDocument(result),
            ExpectedEditRecordPage(
              title = "Edit record",
              heading = "TNA reference: COAL 80/80/1",
              legend = "Intellectual properties",
              classicCatalogueRef = "COAL 80/80/1",
              omegaCatalogueId = "COAL.2022.V1RJW.P",
              scopeAndContent =
                "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
              coveringDates = "1868; 1890-1902; 1933",
              formerReferenceDepartment = "1234",
              startDate = ExpectedDate("1", "1", "1868"),
              endDate = ExpectedDate("31", "12", "1933"),
              legalStatus = "ref.1",
              note = "Need to check copyright info.",
              background = "Photo was taken by a daughter of one of the coal miners who used them.",
              optionsForPlaceOfDeposit = Seq(
                ExpectedSelectOption("", "Select where this record is held", disabled = true),
                ExpectedSelectOption("1", "The National Archives, Kew"),
                ExpectedSelectOption("2", "British Museum, Department of Libraries and Archives", selected = true),
                ExpectedSelectOption("3", "British Library, National Sound Archive")
              ),
              separatedMaterial = Seq(
                ExpectedSeparatedMaterial(
                  linkHref = Some("#;"),
                  linkText = Some("COAL 80/80/5")
                ),
                ExpectedSeparatedMaterial(
                  linkHref = Some("#;"),
                  linkText = Some("COAL 80/80/6")
                ),
                ExpectedSeparatedMaterial(
                  linkHref = Some("#;"),
                  linkText = Some("COAL 80/80/7")
                )
              ),
              relatedMaterial = Seq(
                ExpectedRelatedMaterial(
                  description =
                    Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
                ),
                ExpectedRelatedMaterial(
                  linkHref = Some("#;"),
                  linkText = Some("COAL 80/80/3")
                ),
                ExpectedRelatedMaterial(
                  linkHref = Some("#;"),
                  linkText = Some("COAL 80/80/2"),
                  description =
                    Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
                )
              ),
              custodialHistory = "Files originally created by successor or predecessor departments for COAL"
            )
          )

        }
      }

    }
  }

  private def submitWhileLoggedIn(editSetId: Int, recordId: String, values: Map[String, String]): Future[Result] = {
    val request = CSRFTokenHelper.addCSRFToken(
      FakeRequest(POST, s"/edit-set/$editSetId/record/$recordId/edit")
        .withFormUrlEncodedBody(values.toSeq: _*)
        .withSession(SessionKeys.token -> validSessionToken)
    )
    route(app, request).get
  }

  private def getRecordForEditingWhileLoggedIn(editSetId: Int, recordId: String): Future[Result] =
    getWhileLoggedIn(s"/edit-set/$editSetId/record/$recordId/edit")

  private def getWhileLoggedIn(location: String): Future[Result] = {
    val request =
      FakeRequest(GET, location).withSession(
        SessionKeys.token -> validSessionToken
      )
    route(app, request).get
  }

  private def assertPageAsExpected(document: Document, expectedEditRecordPage: ExpectedEditRecordPage): Assertion = {
    document must haveTitle(expectedEditRecordPage.title)
    document must haveHeading(expectedEditRecordPage.heading)
    document must haveLegend(expectedEditRecordPage.legend)
    document must haveClassicCatalogueRef(expectedEditRecordPage.classicCatalogueRef)
    document must haveOmegaCatalogueId(expectedEditRecordPage.omegaCatalogueId)
    document must haveScopeAndContent(expectedEditRecordPage.scopeAndContent)
    document must haveCoveringDates(expectedEditRecordPage.coveringDates)
    document must haveFormerReferenceDepartment(expectedEditRecordPage.formerReferenceDepartment)
    document must haveStartDateDay(expectedEditRecordPage.startDate.day)
    document must haveStartDateMonth(expectedEditRecordPage.startDate.month)
    document must haveStartDateYear(expectedEditRecordPage.startDate.year)
    document must haveEndDateDay(expectedEditRecordPage.endDate.day)
    document must haveEndDateMonth(expectedEditRecordPage.endDate.month)
    document must haveEndDateYear(expectedEditRecordPage.endDate.year)
    document must haveLegalStatus(expectedEditRecordPage.legalStatus)
    document must haveSelectionForPlaceOfDeposit(expectedEditRecordPage.optionsForPlaceOfDeposit)
    document must haveNote(expectedEditRecordPage.note)
    document must haveRelatedMaterial(expectedEditRecordPage.relatedMaterial: _*)
    document must haveSeparatedMaterial(expectedEditRecordPage.separatedMaterial: _*)
    document must haveBackground(expectedEditRecordPage.background)
    document must haveCustodialHistory(expectedEditRecordPage.custodialHistory)

    document must haveVisibleLogoutLink
    document must haveLogoutLinkLabel("Sign out")
    document must haveLogoutLink
    document must haveActionButtons("save", "Save changes", 2)
    document must haveActionButtons("discard", "Discard changes", 2)

    if (expectedEditRecordPage.summaryErrorMessages.nonEmpty) {
      document must haveSummaryErrorMessages(expectedEditRecordPage.summaryErrorMessages: _*)
    }

    expectedEditRecordPage.errorMessageForStartDate match {
      case Some(expectedErrorMessage) => document must haveErrorMessageForStartDate(expectedErrorMessage)
      case None                       => document must haveNoErrorMessageForStartDate
    }

    expectedEditRecordPage.errorMessageForEndDate match {
      case Some(expectedErrorMessage) => document must haveErrorMessageForEndDate(expectedErrorMessage)
      case None                       => document must haveNoErrorMessageForEndDate
    }

    expectedEditRecordPage.errorMessageForCoveringsDates match {
      case Some(expectedErrorMessage) => document must haveErrorMessageForCoveringDates(expectedErrorMessage)
      case None                       => document must haveNoErrorMessageForCoveringDates
    }

    expectedEditRecordPage.errorMessageForLegalStatus match {
      case Some(expectedErrorMessage) => document must haveErrorMessageForLegalStatus(expectedErrorMessage)
      case None                       => document must haveNoErrorMessageForLegalStatus
    }

    expectedEditRecordPage.errorMessageForPlaceOfDeposit match {
      case Some(expectedErrorMessage) => document must haveErrorMessageForPlaceOfDeposit(expectedErrorMessage)
      case None                       => document must haveNoErrorMessageForPlaceOfDeposit
    }

    expectedEditRecordPage.errorMessageForNote match {
      case Some(expectedErrorMessage) => document must haveErrorMessageForNote(expectedErrorMessage)
      case None                       => document must haveNoErrorMessageForNote
    }

    expectedEditRecordPage.errorMessageForBackground match {
      case Some(expectedErrorMessage) => document must haveErrorMessageForBackground(expectedErrorMessage)
      case None                       => document must haveNoErrorMessageForBackground
    }

    expectedEditRecordPage.errorMessageForCustodialHistory match {
      case Some(expectedErrorMessage) => document must haveErrorMessageForCustodialHistory(expectedErrorMessage)
      case None                       => document must haveNoErrorMessageForCustodialHistory
    }

  }

  private def assertPageAsExpected(
    document: Document,
    expectedEditRecordPage: ExpectedEditSetPage,
    pageNumber: Int = 1
  ): Assertion = {
    document must haveTitle(expectedEditRecordPage.title)
    document must haveCaption(expectedEditRecordPage.caption)
    document must haveActionButtons(expectedEditRecordPage.button.value, expectedEditRecordPage.button.label)
    document must haveSelectionForOrderingField(expectedEditRecordPage.expectedOptionsForField)
    document must haveSelectionForOrderingDirection(expectedEditRecordPage.expectedOptionsForDirection)

    val summaryRowsForPage = expectedEditRecordPage.summaryRowsForPage(pageNumber)
    document must haveSummaryRows(summaryRowsForPage.size)
    summaryRowsForPage.zipWithIndex.foreach { case (expectedEditSetSummaryRow, index) =>
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

  case class ExpectedEditRecordPage(
    title: String,
    heading: String,
    legend: String,
    classicCatalogueRef: String,
    omegaCatalogueId: String,
    scopeAndContent: String,
    coveringDates: String,
    formerReferenceDepartment: String,
    startDate: ExpectedDate,
    endDate: ExpectedDate,
    legalStatus: String,
    note: String,
    background: String,
    custodialHistory: String,
    optionsForPlaceOfDeposit: Seq[ExpectedSelectOption],
    relatedMaterial: Seq[ExpectedRelatedMaterial] = Seq.empty,
    separatedMaterial: Seq[ExpectedSeparatedMaterial] = Seq.empty,
    summaryErrorMessages: Seq[ExpectedSummaryErrorMessage] = Seq.empty,
    errorMessageForStartDate: Option[String] = None,
    errorMessageForEndDate: Option[String] = None,
    errorMessageForCoveringsDates: Option[String] = None,
    errorMessageForLegalStatus: Option[String] = None,
    errorMessageForPlaceOfDeposit: Option[String] = None,
    errorMessageForNote: Option[String] = None,
    errorMessageForBackground: Option[String] = None,
    errorMessageForCustodialHistory: Option[String] = None
  )

  case class ExpectedRelatedMaterial(
    linkHref: Option[String] = None,
    linkText: Option[String] = None,
    description: Option[String] = None
  )

  case class ExpectedSeparatedMaterial(
    linkHref: Option[String] = None,
    linkText: Option[String] = None,
    description: Option[String] = None
  )

  case class ExpectedDate(day: String, month: String, year: String)
  case class ExpectedEditSetPage(
    title: String,
    caption: String,
    button: ExpectedActionButton,
    expectedOptionsForField: Seq[ExpectedSelectOption],
    expectedOptionsForDirection: Seq[ExpectedSelectOption],
    expectedSummaryRows: Seq[ExpectedEditSetSummaryRow]
  ) {

    lazy val summaryRowsForPage: Map[Int, Seq[ExpectedEditSetSummaryRow]] =
      expectedSummaryRows
        .sliding(10, 10)
        .zipWithIndex
        .map { case (rowsForPage, i) =>
          i + 1 -> rowsForPage
        }
        .toMap

    lazy val numberOfPages = summaryRowsForPage.size

  }

  case class ExpectedEditSetSummaryRow(
    ccr: String,
    scopeAndContents: String,
    coveringDates: String
  )

}
