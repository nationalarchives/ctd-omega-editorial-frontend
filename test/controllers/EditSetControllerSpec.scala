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
import play.api.test.Helpers._
import play.api.test._
import support.BaseSpec
import support.CustomMatchers._
import support.ExpectedValues._
import uk.gov.nationalarchives.omega.editorial.controllers.EditSetController._
import uk.gov.nationalarchives.omega.editorial.controllers.{ EditSetController, SessionKeys }
import uk.gov.nationalarchives.omega.editorial.editSetRecords.{ editSetRecordMap, restoreOriginalRecords }
import uk.gov.nationalarchives.omega.editorial.models.session.Session
import uk.gov.nationalarchives.omega.editorial.models.{ EditSetRecord, RelatedMaterial, SeparatedMaterial }
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

  override def beforeEach(): Unit = {
    super.beforeEach()
    restoreOriginalRecords()
  }

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
        testReferenceDataService,
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

      def requestPage(values: Map[String, String]): Future[Result] =
        route(
          app,
          CSRFTokenHelper.addCSRFToken(
            FakeRequest(POST, "/edit-set/1")
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
        testReferenceDataService,
        editSetInstance,
        editSetRecordEditInstance,
        editSetRecordEditDiscardInstance,
        editSetRecordEditSaveInstance
      )
      val editRecordPage = controller
        .viewEditRecordForm("1", "COAL.2022.V1RJW.P")
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
        .viewEditRecordForm("1", "COAL.2022.V1RJW.P")
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

      "All form sections appear in the correct order" in {
        val request =
          CSRFTokenHelper.addCSRFToken(
            FakeRequest(GET, "/edit-set/1/record/COAL.2022.V1RJW.P/edit").withSession(
              SessionKeys.token -> validSessionToken
            )
          )

        val editRecordPage = route(app, request).get

        status(editRecordPage) mustBe OK
        asDocument(editRecordPage) must haveSectionsInCorrectOrder(
          "Scope and content",
          "Creator",
          "Covering dates",
          "Start date",
          "End date",
          "Former reference (Department) (optional)",
          "Former reference (PRO) (optional)",
          "Legal Status",
          "Custodial History (optional)",
          "Held by",
          "Note (optional)",
          "Administrative / biographical background (optional)",
          "Related material",
          "Separated material"
        )
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
            formerReferencePro = "",
            startDate = ExpectedDate("1", "1", "1962"),
            endDate = ExpectedDate("31", "12", "1962"),
            legalStatusID = "ref.1",
            note = "A note about COAL.2022.V1RJW.P.",
            background = "Photo was taken by a daughter of one of the coal miners who used them.",
            optionsForPlaceOfDepositID = Seq(
              ExpectedSelectOption("", "Select where this record is held", disabled = true),
              ExpectedSelectOption("1", "The National Archives, Kew", selected = true),
              ExpectedSelectOption("2", "British Museum, Department of Libraries and Archives"),
              ExpectedSelectOption("3", "British Library, National Sound Archive")
            ),
            optionsForCreators = Seq(
              Seq(
                ExpectedSelectOption("", "Select creator", disabled = true),
                ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)", selected = true),
                ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                ExpectedSelectOption("8R6", "Queen Anne's Bounty")
              ),
              Seq(
                ExpectedSelectOption("", "Select creator", disabled = true),
                ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)", selected = true),
                ExpectedSelectOption("8R6", "Queen Anne's Bounty")
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
            formerReferencePro = "",
            startDate = ExpectedDate("1", "1", "1964"),
            endDate = ExpectedDate("31", "12", "1964"),
            legalStatusID = "",
            note = "",
            background = "Photo was taken by a son of one of the coal miners who used them.",
            optionsForPlaceOfDepositID = Seq(
              ExpectedSelectOption("", "Select where this record is held", selected = true, disabled = true),
              ExpectedSelectOption("1", "The National Archives, Kew"),
              ExpectedSelectOption("2", "British Museum, Department of Libraries and Archives"),
              ExpectedSelectOption("3", "British Library, National Sound Archive")
            ),
            optionsForCreators = Seq(
              Seq(
                ExpectedSelectOption("", "Select creator", disabled = true),
                ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                ExpectedSelectOption("8R6", "Queen Anne's Bounty", selected = true)
              ),
              Seq(
                ExpectedSelectOption("", "Select creator", disabled = true),
                ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)", selected = true),
                ExpectedSelectOption("8R6", "Queen Anne's Bounty")
              ),
              Seq(
                ExpectedSelectOption("", "Select creator", disabled = true),
                ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                ExpectedSelectOption("8R6", "Queen Anne's Bounty", selected = true)
              )
            ),
            custodialHistory = "",
            relatedMaterial = Seq.empty
          )
        )
      }
      "all data is valid, except that the one creator ID is unrecognised" in {
        val request =
          FakeRequest(GET, "/edit-set/1/record/COAL.2022.V10RJW.P/edit").withSession(
            SessionKeys.token -> validSessionToken
          )
        val editRecordPage = route(app, request).get

        status(editRecordPage) mustBe OK
        contentType(editRecordPage) mustBe Some("text/html")
        assertPageAsExpected(
          asDocument(editRecordPage),
          ExpectedEditRecordPage(
            title = "Edit record",
            heading = "TNA reference: COAL 80/80/10",
            legend = "Intellectual properties",
            classicCatalogueRef = "COAL 80/80/10",
            omegaCatalogueId = "COAL.2022.V10RJW.P",
            scopeAndContent = "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
            coveringDates = "1960",
            formerReferenceDepartment = "",
            formerReferencePro = "",
            startDate = ExpectedDate("1", "1", "1960"),
            endDate = ExpectedDate("31", "12", "1960"),
            legalStatusID = "ref.1",
            note = "",
            background = "",
            optionsForPlaceOfDepositID = Seq(
              ExpectedSelectOption("", "Select where this record is held", disabled = true),
              ExpectedSelectOption("1", "The National Archives, Kew", selected = true),
              ExpectedSelectOption("2", "British Museum, Department of Libraries and Archives"),
              ExpectedSelectOption("3", "British Library, National Sound Archive")
            ),
            optionsForCreators = Seq(
              Seq(
                ExpectedSelectOption("", "Select creator", disabled = true, selected = true),
                ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                ExpectedSelectOption("8R6", "Queen Anne's Bounty")
              )
            ),
            custodialHistory = "",
            relatedMaterial = Seq.empty,
            separatedMaterial = Seq(
              ExpectedSeparatedMaterial(description =
                Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
              )
            )
          )
        )
      }

      "all data is valid, except that no creator ID was previously selected" in {
        val request =
          FakeRequest(GET, "/edit-set/1/record/COAL.2022.V4RJW.P/edit").withSession(
            SessionKeys.token -> validSessionToken
          )
        val editRecordPage = route(app, request).get

        status(editRecordPage) mustBe OK
        contentType(editRecordPage) mustBe Some("text/html")
        assertPageAsExpected(
          asDocument(editRecordPage),
          ExpectedEditRecordPage(
            title = "Edit record",
            heading = "TNA reference: COAL 80/80/4",
            legend = "Intellectual properties",
            classicCatalogueRef = "COAL 80/80/4",
            omegaCatalogueId = "COAL.2022.V4RJW.P",
            scopeAndContent = "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
            coveringDates = "1960",
            formerReferenceDepartment = "",
            formerReferencePro = "",
            startDate = ExpectedDate("1", "1", "1960"),
            endDate = ExpectedDate("31", "12", "1960"),
            legalStatusID = "ref.1",
            note = "",
            background = "",
            optionsForPlaceOfDepositID = Seq(
              ExpectedSelectOption("", "Select where this record is held", disabled = true),
              ExpectedSelectOption("1", "The National Archives, Kew", selected = true),
              ExpectedSelectOption("2", "British Museum, Department of Libraries and Archives"),
              ExpectedSelectOption("3", "British Library, National Sound Archive")
            ),
            optionsForCreators = Seq(
              Seq(
                ExpectedSelectOption("", "Select creator", disabled = true, selected = true),
                ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                ExpectedSelectOption("8R6", "Queen Anne's Bounty")
              )
            ),
            custodialHistory = "",
            relatedMaterial = Seq.empty,
            separatedMaterial = Seq.empty
          )
        )
      }

    }

    "redirect to the login page from the application when requested with invalid session token" in {
      val controller = inject[EditSetController]
      val editRecordPage = controller
        .viewEditRecordForm("1", "COAL.2022.V1RJW.P")
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
    "when the action is to save the record" when {

      "fail" when {
        "and yet preserve the CCR" when {
          "there are errors" in {

            val blankScopeAndContentToFailValidation = ""
            val oci = "COAL.2022.V1RJW.P"
            val values = valuesFromRecord(oci) ++ Map(
              FieldNames.scopeAndContent -> blankScopeAndContentToFailValidation
            )

            val result = submitWhileLoggedIn("save", 1, oci, values)

            status(result) mustBe BAD_REQUEST
            assertPageAsExpected(
              asDocument(result),
              generateExpectedEditRecordPageFromRecord(oci)
                .copy(omegaCatalogueId = oci, scopeAndContent = blankScopeAndContentToFailValidation)
            )
          }
        }
        "start date" when {
          "is empty" in {

            val oci = "COAL.2022.V1RJW.P"
            val values =
              valuesFromRecord(oci) ++ Map(
                FieldNames.startDateDay   -> "",
                FieldNames.startDateMonth -> "",
                FieldNames.startDateYear  -> ""
              )

            val result = submitWhileLoggedIn("save", 1, oci, values)

            status(result) mustBe BAD_REQUEST
            assertPageAsExpected(
              asDocument(result),
              generateExpectedEditRecordPageFromRecord(oci).copy(
                startDate = ExpectedDate("", "", ""),
                summaryErrorMessages =
                  Seq(ExpectedSummaryErrorMessage("Start date is not a valid date", s"#${FieldNames.startDateDay}")),
                errorMessageForStartDate = Some("Start date is not a valid date")
              )
            )

          }
          "is of an invalid format" in {

            val oci = "COAL.2022.V1RJW.P"
            val values =
              valuesFromRecord(oci) ++ Map(
                FieldNames.startDateDay   -> "XX",
                FieldNames.startDateMonth -> "11",
                FieldNames.startDateYear  -> "1960"
              )

            val result = submitWhileLoggedIn("save", 1, "COAL.2022.V1RJW.P", values)

            status(result) mustBe BAD_REQUEST
            assertPageAsExpected(
              asDocument(result),
              generateExpectedEditRecordPageFromRecord(oci).copy(
                startDate = ExpectedDate("XX", "11", "1960"),
                summaryErrorMessages =
                  Seq(ExpectedSummaryErrorMessage("Start date is not a valid date", s"#${FieldNames.startDateDay}")),
                errorMessageForStartDate = Some("Start date is not a valid date")
              )
            )

          }
        }
        "doesn't exist" in {

          val oci = "COAL.2022.V1RJW.P"
          val values =
            valuesFromRecord(oci) ++ Map(
              FieldNames.startDateDay   -> "29",
              FieldNames.startDateMonth -> "2",
              FieldNames.startDateYear  -> "2022",
              FieldNames.endDateDay     -> "31",
              FieldNames.endDateMonth   -> "10",
              FieldNames.endDateYear    -> "2022"
            )

          val result = submitWhileLoggedIn("save", 1, oci, values)

          status(result) mustBe BAD_REQUEST
          assertPageAsExpected(
            asDocument(result),
            generateExpectedEditRecordPageFromRecord(oci).copy(
              startDate = ExpectedDate("29", "2", "2022"),
              endDate = ExpectedDate("31", "10", "2022"),
              summaryErrorMessages =
                Seq(ExpectedSummaryErrorMessage("Start date is not a valid date", s"#${FieldNames.startDateDay}")),
              errorMessageForStartDate = Some("Start date is not a valid date")
            )
          )

        }
        "end date" when {
          "is empty" in {

            val oci = "COAL.2022.V1RJW.P"
            val values =
              valuesFromRecord(oci) ++ Map(
                FieldNames.endDateDay   -> "",
                FieldNames.endDateMonth -> "",
                FieldNames.endDateYear  -> ""
              )

            val result = submitWhileLoggedIn("save", 1, oci, values)

            status(result) mustBe BAD_REQUEST
            assertPageAsExpected(
              asDocument(result),
              generateExpectedEditRecordPageFromRecord(oci).copy(
                endDate = ExpectedDate("", "", ""),
                summaryErrorMessages =
                  Seq(ExpectedSummaryErrorMessage("End date is not a valid date", s"#${FieldNames.endDateDay}")),
                errorMessageForEndDate = Some("End date is not a valid date")
              )
            )
          }
          "is of an invalid format" in {

            val oci = "COAL.2022.V1RJW.P"
            val values =
              valuesFromRecord(oci) ++ Map(
                FieldNames.endDateDay   -> "XX",
                FieldNames.endDateMonth -> "12",
                FieldNames.endDateYear  -> "2000"
              )

            val result = submitWhileLoggedIn("save", 1, oci, values)

            status(result) mustBe BAD_REQUEST
            assertPageAsExpected(
              asDocument(result),
              generateExpectedEditRecordPageFromRecord(oci).copy(
                endDate = ExpectedDate("XX", "12", "2000"),
                summaryErrorMessages =
                  Seq(ExpectedSummaryErrorMessage("End date is not a valid date", s"#${FieldNames.endDateDay}")),
                errorMessageForEndDate = Some("End date is not a valid date")
              )
            )

          }
          "doesn't exist" in {

            val oci = "COAL.2022.V1RJW.P"
            val values =
              valuesFromRecord(oci) ++ Map(
                FieldNames.startDateDay   -> "1",
                FieldNames.startDateMonth -> "2",
                FieldNames.startDateYear  -> "2022",
                FieldNames.endDateDay     -> "29",
                FieldNames.endDateMonth   -> "2",
                FieldNames.endDateYear    -> "2022"
              )

            val result = submitWhileLoggedIn("save", 1, oci, values)

            status(result) mustBe BAD_REQUEST
            assertPageAsExpected(
              asDocument(result),
              generateExpectedEditRecordPageFromRecord(oci).copy(
                startDate = ExpectedDate("1", "2", "2022"),
                endDate = ExpectedDate("29", "2", "2022"),
                summaryErrorMessages =
                  Seq(ExpectedSummaryErrorMessage("End date is not a valid date", s"#${FieldNames.endDateDay}")),
                errorMessageForEndDate = Some("End date is not a valid date")
              )
            )

          }
          "is before start date" in {

            val oci = "COAL.2022.V1RJW.P"
            val values =
              valuesFromRecord(oci) ++ Map(
                FieldNames.startDateDay   -> "12",
                FieldNames.startDateMonth -> "10",
                FieldNames.startDateYear  -> "2020",
                FieldNames.endDateDay     -> "11",
                FieldNames.endDateMonth   -> "10",
                FieldNames.endDateYear    -> "2020"
              )

            val result = submitWhileLoggedIn("save", 1, oci, values)

            status(result) mustBe BAD_REQUEST
            assertPageAsExpected(
              asDocument(result),
              generateExpectedEditRecordPageFromRecord(oci).copy(
                startDate = ExpectedDate("12", "10", "2020"),
                endDate = ExpectedDate("11", "10", "2020"),
                summaryErrorMessages =
                  Seq(ExpectedSummaryErrorMessage("End date cannot precede start date", s"#${FieldNames.endDateDay}")),
                errorMessageForEndDate = Some("End date cannot precede start date")
              )
            )

          }
        }
        "neither start date nor end date is valid" in {

          val oci = "COAL.2022.V1RJW.P"
          val values =
            valuesFromRecord(oci) ++ Map(
              FieldNames.startDateDay   -> "12",
              FieldNames.startDateMonth -> "14",
              FieldNames.startDateYear  -> "2020",
              FieldNames.endDateDay     -> "42",
              FieldNames.endDateMonth   -> "12",
              FieldNames.endDateYear    -> "2020"
            )

          val result = submitWhileLoggedIn("save", 1, oci, values)

          status(result) mustBe BAD_REQUEST
          assertPageAsExpected(
            asDocument(result),
            generateExpectedEditRecordPageFromRecord(oci).copy(
              startDate = ExpectedDate("12", "14", "2020"),
              endDate = ExpectedDate("42", "12", "2020"),
              summaryErrorMessages = Seq(
                ExpectedSummaryErrorMessage("Start date is not a valid date", s"#${FieldNames.startDateDay}"),
                ExpectedSummaryErrorMessage("End date is not a valid date", s"#${FieldNames.endDateDay}")
              ),
              errorMessageForStartDate = Some("Start date is not a valid date"),
              errorMessageForEndDate = Some("End date is not a valid date")
            )
          )

        }
        "covering date" when {
          "is invalid" in {

            val oci = "COAL.2022.V1RJW.P"
            val values =
              valuesFromRecord(oci) ++ Map(
                FieldNames.coveringDates -> "Oct 1 2004"
              )

            val result = submitWhileLoggedIn("save", 1, oci, values)

            status(result) mustBe BAD_REQUEST
            assertPageAsExpected(
              asDocument(result),
              generateExpectedEditRecordPageFromRecord(oci).copy(
                coveringDates = "Oct 1 2004",
                summaryErrorMessages = Seq(
                  ExpectedSummaryErrorMessage("Covering date format is not valid", s"#${FieldNames.coveringDates}")
                ),
                errorMessageForCoveringsDates = Some("Covering date format is not valid")
              )
            )

          }
          "is too long" in {

            val oci = "COAL.2022.V1RJW.P"
            val gapDateTooLong = (1 to 100).map(_ => "2004 Oct 1").mkString(";")
            val values =
              valuesFromRecord(oci) ++ Map(
                FieldNames.coveringDates -> gapDateTooLong
              )

            val result = submitWhileLoggedIn("save", 1, oci, values)

            status(result) mustBe BAD_REQUEST
            assertPageAsExpected(
              asDocument(result),
              generateExpectedEditRecordPageFromRecord(oci).copy(
                coveringDates = gapDateTooLong,
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

            val oci = "COAL.2022.V1RJW.P"
            val values =
              valuesFromRecord(oci) ++ Map(
                FieldNames.coveringDates -> "  "
              )

            val result = submitWhileLoggedIn("save", 1, oci, values)

            status(result) mustBe BAD_REQUEST
            assertPageAsExpected(
              asDocument(result),
              generateExpectedEditRecordPageFromRecord(oci).copy(
                coveringDates = "  ",
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

            val oci = "COAL.2022.V1RJW.P"
            val values =
              valuesFromRecord(oci) ++ Map(
                FieldNames.placeOfDepositID -> ""
              )

            val result = submitWhileLoggedIn("save", 1, oci, values)

            status(result) mustBe BAD_REQUEST
            assertPageAsExpected(
              asDocument(result),
              generateExpectedEditRecordPageFromRecord(oci).copy(
                optionsForPlaceOfDepositID = Seq(
                  ExpectedSelectOption("", "Select where this record is held", selected = true, disabled = true),
                  ExpectedSelectOption("1", "The National Archives, Kew"),
                  ExpectedSelectOption("2", "British Museum, Department of Libraries and Archives"),
                  ExpectedSelectOption("3", "British Library, National Sound Archive")
                ),
                summaryErrorMessages =
                  Seq(ExpectedSummaryErrorMessage("You must choose an option", s"#${FieldNames.placeOfDepositID}")),
                errorMessageForPlaceOfDeposit = Some("You must choose an option")
              )
            )

          }
          "is absent" in {

            val oci = "COAL.2022.V1RJW.P"
            val values = valuesFromRecord(oci) - FieldNames.placeOfDepositID

            val result = submitWhileLoggedIn("save", 1, oci, values)

            status(result) mustBe BAD_REQUEST
            assertPageAsExpected(
              asDocument(result),
              generateExpectedEditRecordPageFromRecord(oci).copy(
                optionsForPlaceOfDepositID = Seq(
                  ExpectedSelectOption("", "Select where this record is held", selected = true, disabled = true),
                  ExpectedSelectOption("1", "The National Archives, Kew"),
                  ExpectedSelectOption("2", "British Museum, Department of Libraries and Archives"),
                  ExpectedSelectOption("3", "British Library, National Sound Archive")
                ),
                summaryErrorMessages =
                  Seq(ExpectedSummaryErrorMessage("You must choose an option", s"#${FieldNames.placeOfDepositID}")),
                errorMessageForPlaceOfDeposit = Some("You must choose an option")
              )
            )

          }
          "isn't recognised" in {

            val oci = "COAL.2022.V1RJW.P"
            val values =
              valuesFromRecord(oci) ++ Map(FieldNames.placeOfDepositID -> "6")
            val result = submitWhileLoggedIn("save", 1, oci, values)

            status(result) mustBe BAD_REQUEST
            assertPageAsExpected(
              asDocument(result),
              generateExpectedEditRecordPageFromRecord(oci).copy(
                optionsForPlaceOfDepositID = Seq(
                  ExpectedSelectOption("", "Select where this record is held", selected = true, disabled = true),
                  ExpectedSelectOption("1", "The National Archives, Kew"),
                  ExpectedSelectOption("2", "British Museum, Department of Libraries and Archives"),
                  ExpectedSelectOption("3", "British Library, National Sound Archive")
                ),
                summaryErrorMessages =
                  Seq(ExpectedSummaryErrorMessage("You must choose an option", s"#${FieldNames.placeOfDepositID}")),
                errorMessageForPlaceOfDeposit = Some("You must choose an option")
              )
            )

          }
        }
        "legal status" when {
          "is not selected" in {

            val oci = "COAL.2022.V1RJW.P"
            val values =
              valuesFromRecord(oci) ++ Map(
                FieldNames.legalStatusID -> ""
              )

            val result = submitWhileLoggedIn("save", 1, oci, values)

            status(result) mustBe BAD_REQUEST
            assertPageAsExpected(
              asDocument(result),
              generateExpectedEditRecordPageFromRecord(oci).copy(
                legalStatusID = "",
                summaryErrorMessages =
                  Seq(ExpectedSummaryErrorMessage("You must choose an option", s"#${FieldNames.legalStatusID}")),
                errorMessageForLegalStatus = Some("Error: You must choose an option")
              )
            )

          }

          "value doesn't exist" in {

            val oci = "COAL.2022.V1RJW.P"
            val values =
              valuesFromRecord(oci) ++ Map(
                FieldNames.legalStatusID -> "ref.10"
              )

            val result = submitWhileLoggedIn("save", 1, oci, values)

            status(result) mustBe SEE_OTHER
            redirectLocation(result) mustBe Some("/edit-set/1/record/COAL.2022.V1RJW.P/edit/save")

          }
        }
        "note" when {
          "is too long" in {

            val oci = "COAL.2022.V1RJW.P"
            val excessivelyLongNote = "Something about something else." * 100
            val values =
              valuesFromRecord(oci) ++ Map(
                FieldNames.note -> excessivelyLongNote
              )

            val result = submitWhileLoggedIn("save", 1, oci, values)

            status(result) mustBe BAD_REQUEST
            assertPageAsExpected(
              asDocument(result),
              generateExpectedEditRecordPageFromRecord(oci).copy(
                note = excessivelyLongNote,
                summaryErrorMessages =
                  Seq(ExpectedSummaryErrorMessage("Note too long, maximum length 1000 characters", "#note")),
                errorMessageForNote = Some("Note too long, maximum length 1000 characters")
              )
            )

          }
        }
        "background" when {
          "is too long" in {

            val oci = "COAL.2022.V1RJW.P"
            val excessivelyLongBackground = "Something about one of the people." * 400
            val values =
              valuesFromRecord(oci) ++ Map(
                FieldNames.background -> excessivelyLongBackground
              )

            val result = submitWhileLoggedIn("save", 1, oci, values)

            status(result) mustBe BAD_REQUEST
            assertPageAsExpected(
              asDocument(result),
              generateExpectedEditRecordPageFromRecord(oci).copy(
                background = excessivelyLongBackground,
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

            val oci = "COAL.2022.V1RJW.P"
            val custodialHistoryTooLong =
              "Files originally created by successor or predecessor departments for COAL" * 100
            val values =
              valuesFromRecord(oci) ++ Map(
                FieldNames.custodialHistory -> custodialHistoryTooLong
              )

            val result = submitWhileLoggedIn("save", 1, oci, values)

            status(result) mustBe BAD_REQUEST
            assertPageAsExpected(
              asDocument(result),
              generateExpectedEditRecordPageFromRecord(oci).copy(
                custodialHistory = custodialHistoryTooLong,
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
        "no creator has been selected" in {

          val oci = "COAL.2022.V1RJW.P"
          val values =
            valuesFromRecord(oci) ++ Map(
              s"${FieldNames.creatorIDs}[0]" -> "",
              s"${FieldNames.creatorIDs}[1]" -> ""
            )

          val result = submitWhileLoggedIn("save", 1, oci, values)

          status(result) mustBe BAD_REQUEST
          assertPageAsExpected(
            asDocument(result),
            generateExpectedEditRecordPageFromRecord(oci).copy(
              optionsForCreators = Seq(
                Seq(
                  ExpectedSelectOption("", "Select creator", disabled = true, selected = true),
                  ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                  ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                  ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                  ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                ),
                Seq(
                  ExpectedSelectOption("", "Select creator", disabled = true, selected = true),
                  ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                  ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                  ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                  ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                )
              ),
              summaryErrorMessages = Seq(
                ExpectedSummaryErrorMessage(
                  "You must select at least one creator",
                  "#creator-id-0"
                )
              ),
              errorMessageForCreator = Some("You must select at least one creator")
            )
          )

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
            testReferenceDataService,
            editSetInstance,
            editSetRecordEditInstance,
            editSetRecordEditDiscardInstance,
            editSetRecordEditSaveInstance
          )

          val oci = "COAL.2022.V1RJW.P"
          val values =
            valuesFromRecord(oci) ++ Map(
              "action" -> "save"
            )

          val editRecordPage = controller
            .submit("1", oci)
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
          val oci = "COAL.2022.V1RJW.P"
          val values = valuesFromRecord(oci) ++ Map(
            "action" -> "save"
          )
          val editRecordPage = controller
            .submit("1", oci)
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

            val oci = "COAL.2022.V1RJW.P"
            val values = valuesFromRecord(oci)
            val editRecordPageResponse = submitWhileLoggedIn("save", 1, "COAL.2022.V1RJW.P", values)

            status(editRecordPageResponse) mustBe SEE_OTHER
            redirectLocation(editRecordPageResponse) mustBe Some("/edit-set/1/record/COAL.2022.V1RJW.P/edit/save")

            val getRecordResult = getRecordForEditingWhileLoggedIn(1, oci)
            assertPageAsExpected(asDocument(getRecordResult), generateExpectedEditRecordPageFromRecord(oci))

          }

          "the 'note' field is blank" in {

            val oci = "COAL.2022.V12RJW.P"
            val values =
              valuesFromRecord(oci) ++ Map(
                FieldNames.note -> ""
              )

            val editRecordPageResponse = submitWhileLoggedIn("save", 12, oci, values)

            status(editRecordPageResponse) mustBe SEE_OTHER
            redirectLocation(editRecordPageResponse) mustBe Some("/edit-set/12/record/COAL.2022.V12RJW.P/edit/save")

            val getRecordResult = getRecordForEditingWhileLoggedIn(12, oci)
            assertPageAsExpected(
              asDocument(getRecordResult),
              generateExpectedEditRecordPageFromRecord(oci).copy(
                note = ""
              )
            )

          }

          "the 'custodial history' field is blank" in {

            val oci = "COAL.2022.V1RJW.P"
            val values =
              valuesFromRecord(oci) ++ Map(
                FieldNames.custodialHistory -> ""
              )

            val editRecordPageResponse = submitWhileLoggedIn("save", 1, oci, values)

            status(editRecordPageResponse) mustBe SEE_OTHER
            redirectLocation(editRecordPageResponse) mustBe Some("/edit-set/1/record/COAL.2022.V1RJW.P/edit/save")

            val getRecordResult = getRecordForEditingWhileLoggedIn(1, oci)
            assertPageAsExpected(
              asDocument(getRecordResult),
              generateExpectedEditRecordPageFromRecord(oci).copy(
                custodialHistory = ""
              )
            )

          }
          "the 'background' field is blank" in {

            val oci = "COAL.2022.V1RJW.P"
            val values =
              valuesFromRecord(oci) ++ Map(
                FieldNames.background -> ""
              )

            val editRecordPageResponse = submitWhileLoggedIn("save", 1, oci, values)

            status(editRecordPageResponse) mustBe SEE_OTHER
            redirectLocation(editRecordPageResponse) mustBe Some("/edit-set/1/record/COAL.2022.V1RJW.P/edit/save")

            val getRecordResult = getRecordForEditingWhileLoggedIn(1, oci)
            assertPageAsExpected(
              asDocument(getRecordResult),
              generateExpectedEditRecordPageFromRecord(oci).copy(
                background = ""
              )
            )
          }
        }
        "multiple creators have been selected" in {

          val oci = "COAL.2022.V4RJW.P"
          val values = valuesFromRecord(oci) ++ Map(
            s"${FieldNames.creatorIDs}[0]" -> "48N",
            s"${FieldNames.creatorIDs}[1]" -> "46F"
          )

          val editRecordPageResponse = submitWhileLoggedIn("save", 1, oci, values)

          status(editRecordPageResponse) mustBe SEE_OTHER
          redirectLocation(editRecordPageResponse) mustBe Some(s"/edit-set/1/record/$oci/edit/save")

          val getRecordResult = getRecordForEditingWhileLoggedIn(1, oci)
          assertPageAsExpected(
            asDocument(getRecordResult),
            generateExpectedEditRecordPageFromRecord(oci).copy(
              optionsForCreators = Seq(
                Seq(
                  ExpectedSelectOption("", "Select creator", disabled = true),
                  ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)", selected = true),
                  ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                  ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                  ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                ),
                Seq(
                  ExpectedSelectOption("", "Select creator", disabled = true),
                  ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                  ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)", selected = true),
                  ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                  ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                )
              )
            )
          )
        }
        "multiple creators have been selected, as one empty selection" in {

          val oci = "COAL.2022.V4RJW.P"
          val values = valuesFromRecord(oci) ++ Map(
            s"${FieldNames.creatorIDs}[0]" -> "48N",
            s"${FieldNames.creatorIDs}[1]" -> "46F",
            s"${FieldNames.creatorIDs}[2]" -> ""
          )
          val editRecordPageResponse = submitWhileLoggedIn("save", 1, oci, values)

          status(editRecordPageResponse) mustBe SEE_OTHER
          redirectLocation(editRecordPageResponse) mustBe Some(s"/edit-set/1/record/$oci/edit/save")

          val getRecordResult = getRecordForEditingWhileLoggedIn(1, oci)
          assertPageAsExpected(
            asDocument(getRecordResult),
            generateExpectedEditRecordPageFromRecord(oci).copy(
              optionsForCreators = Seq(
                Seq(
                  ExpectedSelectOption("", "Select creator", disabled = true),
                  ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)", selected = true),
                  ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                  ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                  ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                ),
                Seq(
                  ExpectedSelectOption("", "Select creator", disabled = true),
                  ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                  ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)", selected = true),
                  ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                  ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                )
              )
            )
          )
        }
        "multiple creators have been selected, including duplicates" in {

          val oci = "COAL.2022.V4RJW.P"
          val values = valuesFromRecord(oci) ++ Map(
            s"${FieldNames.creatorIDs}[0]" -> "48N",
            s"${FieldNames.creatorIDs}[1]" -> "46F",
            s"${FieldNames.creatorIDs}[2]" -> "46F"
          )
          val editRecordPageResponse = submitWhileLoggedIn("save", 1, oci, values)

          status(editRecordPageResponse) mustBe SEE_OTHER
          redirectLocation(editRecordPageResponse) mustBe Some(s"/edit-set/1/record/$oci/edit/save")

          val getRecordResult = getRecordForEditingWhileLoggedIn(1, oci)
          assertPageAsExpected(
            asDocument(getRecordResult),
            generateExpectedEditRecordPageFromRecord(oci).copy(
              optionsForCreators = Seq(
                Seq(
                  ExpectedSelectOption("", "Select creator", disabled = true),
                  ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)", selected = true),
                  ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                  ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                  ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                ),
                Seq(
                  ExpectedSelectOption("", "Select creator", disabled = true),
                  ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                  ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)", selected = true),
                  ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                  ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                ),
                Seq(
                  ExpectedSelectOption("", "Select creator", disabled = true),
                  ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                  ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)", selected = true),
                  ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                  ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                )
              )
            )
          )

        }
      }
    }

    "when the action is to discard all changes" when {

      "successful" when {
        "even if the validation fails" in {

          val oci = "COAL.2022.V1RJW.P"
          val blankScopeAndContentToFailValidation = ""
          val values =
            valuesFromRecord(oci) ++ Map(
              "coveringDates" -> blankScopeAndContentToFailValidation
            )

          val result = submitWhileLoggedIn("discard", 1, oci, values)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some("/edit-set/1/record/COAL.2022.V1RJW.P/edit/discard")

        }
      }
    }

    "when the action is to calculate the start and end dates from the covering dates" when {

      "failure" when {
        "blank" in {

          val oci = "COAL.2022.V1RJW.P"
          val values =
            valuesFromRecord(oci) ++ Map(
              FieldNames.coveringDates -> "   "
            )

          val result = submitWhileLoggedIn("calculateDates", 1, oci, values)

          status(result) mustBe BAD_REQUEST
          assertPageAsExpected(
            asDocument(result),
            generateExpectedEditRecordPageFromRecord(oci).copy(
              coveringDates = "   ",
              summaryErrorMessages = Seq(
                ExpectedSummaryErrorMessage("Enter the covering dates", s"#${FieldNames.coveringDates}"),
                ExpectedSummaryErrorMessage("Covering date format is not valid", s"#${FieldNames.coveringDates}")
              ),
              errorMessageForCoveringsDates = Some("Enter the covering dates")
            )
          )

        }
        "invalid format" in {

          val oci = "COAL.2022.V1RJW.P"
          val values =
            valuesFromRecord(oci) ++ Map(
              FieldNames.coveringDates -> "1270s"
            )

          val result = submitWhileLoggedIn("calculateDates", 1, oci, values)

          status(result) mustBe BAD_REQUEST
          assertPageAsExpected(
            asDocument(result),
            generateExpectedEditRecordPageFromRecord(oci).copy(
              coveringDates = "1270s",
              summaryErrorMessages = Seq(
                ExpectedSummaryErrorMessage("Covering date format is not valid", s"#${FieldNames.coveringDates}")
              ),
              errorMessageForCoveringsDates = Some("Covering date format is not valid")
            )
          )

        }
        "contains a non-existent date" in {

          val oci = "COAL.2022.V1RJW.P"
          val values =
            valuesFromRecord(oci) ++ Map(
              FieldNames.coveringDates -> "2022 Feb 1-2022 Feb 29"
            )

          val result = submitWhileLoggedIn("calculateDates", 1, oci, values)

          status(result) mustBe BAD_REQUEST
          assertPageAsExpected(
            asDocument(result),
            generateExpectedEditRecordPageFromRecord(oci).copy(
              coveringDates = "2022 Feb 1-2022 Feb 29",
              summaryErrorMessages = Seq(
                ExpectedSummaryErrorMessage("Covering date format is not valid", s"#${FieldNames.coveringDates}")
              ),
              errorMessageForCoveringsDates = Some("Covering date format is not valid")
            )
          )

        }
      }
      "successful" when {
        "covers period of the switchover" in {

          val oci = "COAL.2022.V1RJW.P"
          val values =
            valuesFromRecord(oci) ++ Map(
              FieldNames.coveringDates -> "1752 Aug 1-1752 Sept 12"
            )

          val result = submitWhileLoggedIn("calculateDates", 1, oci, values)

          status(result) mustBe OK
          assertPageAsExpected(
            asDocument(result),
            generateExpectedEditRecordPageFromRecord(oci).copy(
              coveringDates = "1752 Aug 1-1752 Sept 12",
              startDate = ExpectedDate("1", "8", "1752"),
              endDate = ExpectedDate("12", "9", "1752")
            )
          )

        }
        "covers period after the switchover" in {

          val oci = "COAL.2022.V1RJW.P"
          val values =
            valuesFromRecord(oci) ++ Map(
              FieldNames.coveringDates -> "1984 Dec"
            )

          val result = submitWhileLoggedIn("calculateDates", 1, oci, values)

          status(result) mustBe OK
          assertPageAsExpected(
            asDocument(result),
            generateExpectedEditRecordPageFromRecord(oci).copy(
              coveringDates = "1984 Dec",
              startDate = ExpectedDate("1", "12", "1984"),
              endDate = ExpectedDate("31", "12", "1984")
            )
          )

        }
        "covers multiple ranges" in {

          val oci = "COAL.2022.V1RJW.P"
          val values =
            valuesFromRecord(oci) ++ Map(
              FieldNames.coveringDates -> "1868; 1890-1902; 1933"
            )

          val result = submitWhileLoggedIn("calculateDates", 1, oci, values)

          status(result) mustBe OK
          assertPageAsExpected(
            asDocument(result),
            generateExpectedEditRecordPageFromRecord(oci).copy(
              coveringDates = "1868; 1890-1902; 1933",
              startDate = ExpectedDate("1", "1", "1868"),
              endDate = ExpectedDate("31", "12", "1933")
            )
          )

        }
      }

    }

    "when the action is to add another selection 'slot' for a creator" when {
      "successful" when {
        "a single creator had been previously assigned and we" when {
          "keep that same selection" in {

            val oci = "COAL.2022.V11RJW.P"

            val getRecordResultBeforehand = getRecordForEditingWhileLoggedIn(1, oci)

            status(getRecordResultBeforehand) mustBe OK
            assertPageAsExpected(
              asDocument(getRecordResultBeforehand),
              generateExpectedEditRecordPageFromRecord(oci)
                .copy(optionsForCreators =
                  Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty", selected = true)
                    )
                  )
                )
            )

            val submissionValues = valuesFromRecord(oci) ++ Map(
              s"${FieldNames.creatorIDs}[0]" -> "8R6"
            )

            val submissionResult = submitWhileLoggedIn("addAnotherCreator", 1, oci, submissionValues)

            status(submissionResult) mustBe OK
            assertPageAsExpected(
              asDocument(submissionResult),
              generateExpectedEditRecordPageFromRecord(oci)
                .copy(optionsForCreators =
                  Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty", selected = true)
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true, selected = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    )
                  )
                )
            )

            val getRecordResultAfterwards = getRecordForEditingWhileLoggedIn(1, oci)

            status(getRecordResultAfterwards) mustBe OK
            assertPageAsExpected(
              asDocument(getRecordResultAfterwards),
              generateExpectedEditRecordPageFromRecord(oci)
                .copy(optionsForCreators =
                  Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty", selected = true)
                    )
                  )
                )
            )

          }
          "keep that same selection, but already have an empty slot" in {

            val oci = "COAL.2022.V11RJW.P"

            val getRecordResultBeforehand = getRecordForEditingWhileLoggedIn(1, oci)

            status(getRecordResultBeforehand) mustBe OK
            assertPageAsExpected(
              asDocument(getRecordResultBeforehand),
              generateExpectedEditRecordPageFromRecord(oci)
                .copy(optionsForCreators =
                  Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty", selected = true)
                    )
                  )
                )
            )

            val submissionValues = valuesFromRecord(oci) ++ Map(
              s"${FieldNames.creatorIDs}[0]" -> "8R6",
              s"${FieldNames.creatorIDs}[1]" -> ""
            )

            val submissionResult = submitWhileLoggedIn("addAnotherCreator", 1, oci, submissionValues)

            status(submissionResult) mustBe OK
            assertPageAsExpected(
              asDocument(submissionResult),
              generateExpectedEditRecordPageFromRecord(oci)
                .copy(optionsForCreators =
                  Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty", selected = true)
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true, selected = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    )
                  )
                )
            )

            val getRecordResultAfterwards = getRecordForEditingWhileLoggedIn(1, oci)

            status(getRecordResultAfterwards) mustBe OK
            assertPageAsExpected(
              asDocument(getRecordResultAfterwards),
              generateExpectedEditRecordPageFromRecord(oci)
                .copy(optionsForCreators =
                  Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty", selected = true)
                    )
                  )
                )
            )

          }
          "clear that selection" in {

            val oci = "COAL.2022.V11RJW.P"

            val getRecordResultBeforehand = getRecordForEditingWhileLoggedIn(1, oci)

            status(getRecordResultBeforehand) mustBe OK
            assertPageAsExpected(
              asDocument(getRecordResultBeforehand),
              generateExpectedEditRecordPageFromRecord(oci)
                .copy(optionsForCreators =
                  Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty", selected = true)
                    )
                  )
                )
            )

            val submissionValues = valuesFromRecord(oci) ++ Map(
              s"${FieldNames.creatorIDs}[0]" -> ""
            )

            val submissionResult = submitWhileLoggedIn("addAnotherCreator", 1, oci, submissionValues)

            status(submissionResult) mustBe OK
            assertPageAsExpected(
              asDocument(submissionResult),
              generateExpectedEditRecordPageFromRecord(oci)
                .copy(
                  optionsForCreators = Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true, selected = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    )
                  )
                )
            )

            val getRecordResultAfterwards = getRecordForEditingWhileLoggedIn(1, oci)

            status(getRecordResultAfterwards) mustBe OK
            assertPageAsExpected(
              asDocument(getRecordResultAfterwards),
              generateExpectedEditRecordPageFromRecord(oci)
                .copy(optionsForCreators =
                  Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty", selected = true)
                    )
                  )
                )
            )

          }
          "change that selection" in {

            val oci = "COAL.2022.V11RJW.P"

            val getRecordResultBeforehand = getRecordForEditingWhileLoggedIn(1, oci)

            status(getRecordResultBeforehand) mustBe OK
            assertPageAsExpected(
              asDocument(getRecordResultBeforehand),
              generateExpectedEditRecordPageFromRecord(oci)
                .copy(optionsForCreators =
                  Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty", selected = true)
                    )
                  )
                )
            )

            val submissionValues = valuesFromRecord(oci) ++ Map(
              s"${FieldNames.creatorIDs}[0]" -> "92W"
            )

            val submissionResult = submitWhileLoggedIn("addAnotherCreator", 1, oci, submissionValues)

            status(submissionResult) mustBe OK
            assertPageAsExpected(
              asDocument(submissionResult),
              generateExpectedEditRecordPageFromRecord(oci)
                .copy(
                  optionsForCreators = Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)", selected = true),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true, selected = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    )
                  )
                )
            )

            val getRecordResultAfterwards = getRecordForEditingWhileLoggedIn(1, oci)

            status(getRecordResultAfterwards) mustBe OK
            assertPageAsExpected(
              asDocument(getRecordResultAfterwards),
              generateExpectedEditRecordPageFromRecord(oci)
                .copy(optionsForCreators =
                  Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty", selected = true)
                    )
                  )
                )
            )

          }
        }
        "the record had multiple creators assigned" when {
          "and we keep those selections" in {

            val oci = "COAL.2022.V7RJW.P"

            val getRecordResultBeforehand = getRecordForEditingWhileLoggedIn(1, oci)

            status(getRecordResultBeforehand) mustBe OK
            assertPageAsExpected(
              asDocument(getRecordResultBeforehand),
              generateExpectedEditRecordPageFromRecord(oci)
                .copy(optionsForCreators =
                  Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(
                        "48N",
                        "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)",
                        selected = true
                      ),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)", selected = true),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    )
                  )
                )
            )

            val submissionValues = valuesFromRecord(oci) ++ Map(
              s"${FieldNames.creatorIDs}[0]" -> "48N",
              s"${FieldNames.creatorIDs}[1]" -> "92W"
            )

            val result = submitWhileLoggedIn("addAnotherCreator", 1, oci, submissionValues)

            status(result) mustBe OK
            assertPageAsExpected(
              asDocument(result),
              generateExpectedEditRecordPageFromRecord(oci)
                .copy(
                  optionsForCreators = Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(
                        "48N",
                        "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)",
                        selected = true
                      ),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)", selected = true),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true, selected = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    )
                  )
                )
            )

            val getRecordResultAfterwards = getRecordForEditingWhileLoggedIn(1, oci)

            status(getRecordResultAfterwards) mustBe OK
            assertPageAsExpected(
              asDocument(getRecordResultAfterwards),
              generateExpectedEditRecordPageFromRecord(oci)
                .copy(optionsForCreators =
                  Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(
                        "48N",
                        "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)",
                        selected = true
                      ),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)", selected = true),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    )
                  )
                )
            )

          }
          "(including duplicates) and keep those selections" in {

            val oci = "COAL.2022.V5RJW.P"

            val getRecordResultBeforehand = getRecordForEditingWhileLoggedIn(1, oci)

            status(getRecordResultBeforehand) mustBe OK
            assertPageAsExpected(
              asDocument(getRecordResultBeforehand),
              generateExpectedEditRecordPageFromRecord(oci)
                .copy(optionsForCreators =
                  Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)", selected = true),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(
                        "48N",
                        "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)",
                        selected = true
                      ),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)", selected = true),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    )
                  )
                )
            )

            val submissionValues = valuesFromRecord(oci) ++ Map(
              s"${FieldNames.creatorIDs}[0]" -> "46F",
              s"${FieldNames.creatorIDs}[1]" -> "48N",
              s"${FieldNames.creatorIDs}[2]" -> "46F"
            )

            val result = submitWhileLoggedIn("addAnotherCreator", 1, oci, submissionValues)

            status(result) mustBe OK
            assertPageAsExpected(
              asDocument(result),
              generateExpectedEditRecordPageFromRecord(oci)
                .copy(
                  optionsForCreators = Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)", selected = true),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(
                        "48N",
                        "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)",
                        selected = true
                      ),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)", selected = true),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true, selected = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    )
                  )
                )
            )

            val getRecordResultAfterwards = getRecordForEditingWhileLoggedIn(1, oci)

            status(getRecordResultAfterwards) mustBe OK
            assertPageAsExpected(
              asDocument(getRecordResultAfterwards),
              generateExpectedEditRecordPageFromRecord(oci)
                .copy(optionsForCreators =
                  Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)", selected = true),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(
                        "48N",
                        "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)",
                        selected = true
                      ),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)", selected = true),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    )
                  )
                )
            )

          }
          "but we change those selections" in {

            val oci = "COAL.2022.V5RJW.P"

            val getRecordResultBeforehand = getRecordForEditingWhileLoggedIn(1, oci)

            status(getRecordResultBeforehand) mustBe OK
            assertPageAsExpected(
              asDocument(getRecordResultBeforehand),
              generateExpectedEditRecordPageFromRecord(oci)
                .copy(optionsForCreators =
                  Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)", selected = true),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(
                        "48N",
                        "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)",
                        selected = true
                      ),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)", selected = true),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    )
                  )
                )
            )

            val submissionValues = valuesFromRecord(oci) ++ Map(
              s"${FieldNames.creatorIDs}[0]" -> "8R6",
              s"${FieldNames.creatorIDs}[1]" -> "92W",
              s"${FieldNames.creatorIDs}[2]" -> "48N"
            )

            val result = submitWhileLoggedIn("addAnotherCreator", 1, oci, submissionValues)

            status(result) mustBe OK
            assertPageAsExpected(
              asDocument(result),
              generateExpectedEditRecordPageFromRecord(oci)
                .copy(
                  optionsForCreators = Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty", selected = true)
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)", selected = true),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(
                        "48N",
                        "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)",
                        selected = true
                      ),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true, selected = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    )
                  )
                )
            )

            val getRecordResultAfterwards = getRecordForEditingWhileLoggedIn(1, oci)

            status(getRecordResultAfterwards) mustBe OK
            assertPageAsExpected(
              asDocument(getRecordResultAfterwards),
              generateExpectedEditRecordPageFromRecord(oci)
                .copy(optionsForCreators =
                  Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)", selected = true),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(
                        "48N",
                        "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)",
                        selected = true
                      ),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)", selected = true),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    )
                  )
                )
            )

          }

        }
      }
    }

    "when the action is to remove the last selection for a creator" when {

      "successful" when {
        "we have two selections and" when {
          "we leave the first selection unchanged" in {

            val oci = "COAL.2022.V1RJW.P"
            val values = valuesFromRecord(oci) ++ Map(
              s"${FieldNames.creatorIDs}[0]" -> "48N",
              s"${FieldNames.creatorIDs}[1]" -> "92W"
            )

            val result = submitWhileLoggedIn("removeLastCreator", 1, oci, values)

            status(result) mustBe OK
            assertPageAsExpected(
              asDocument(result),
              generateExpectedEditRecordPageFromRecord(oci)
                .copy(
                  optionsForCreators = Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(
                        "48N",
                        "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)",
                        selected = true
                      ),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    )
                  )
                )
            )

          }
          "we change the first selection" in {

            val oci = "COAL.2022.V1RJW.P"
            val values = valuesFromRecord(oci) ++ Map(
              s"${FieldNames.creatorIDs}[0]" -> "46F",
              s"${FieldNames.creatorIDs}[1]" -> "92W"
            )

            val result = submitWhileLoggedIn("removeLastCreator", 1, oci, values)

            status(result) mustBe OK
            assertPageAsExpected(
              asDocument(result),
              generateExpectedEditRecordPageFromRecord(oci)
                .copy(
                  optionsForCreators = Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)", selected = true),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    )
                  )
                )
            )

          }
          "we blank out the first" in {

            val oci = "COAL.2022.V1RJW.P"
            val values = valuesFromRecord(oci) ++ Map(
              s"${FieldNames.creatorIDs}[0]" -> "",
              s"${FieldNames.creatorIDs}[1]" -> "92W"
            )

            val result = submitWhileLoggedIn("removeLastCreator", 1, oci, values)

            status(result) mustBe OK
            assertPageAsExpected(
              asDocument(result),
              generateExpectedEditRecordPageFromRecord(oci)
                .copy(
                  optionsForCreators = Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true, selected = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    )
                  )
                )
            )

          }

        }
        "we have three selections and" when {
          "we leave the first two selection unchanged" in {

            val oci = "COAL.2022.V8RJW.P"
            val values = valuesFromRecord(oci) ++ Map(
              s"${FieldNames.creatorIDs}[0]" -> "48N",
              s"${FieldNames.creatorIDs}[1]" -> "46F",
              s"${FieldNames.creatorIDs}[2]" -> "8R6"
            )

            val result = submitWhileLoggedIn("removeLastCreator", 1, oci, values)

            status(result) mustBe OK
            assertPageAsExpected(
              asDocument(result),
              generateExpectedEditRecordPageFromRecord(oci)
                .copy(
                  optionsForCreators = Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(
                        "48N",
                        "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)",
                        selected = true
                      ),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)", selected = true),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    )
                  )
                )
            )

          }
          "we change the first two selections" in {

            val oci = "COAL.2022.V8RJW.P"
            val values = valuesFromRecord(oci) ++ Map(
              s"${FieldNames.creatorIDs}[0]" -> "92W",
              s"${FieldNames.creatorIDs}[1]" -> "48N",
              s"${FieldNames.creatorIDs}[2]" -> "8R6"
            )

            val result = submitWhileLoggedIn("removeLastCreator", 1, oci, values)

            status(result) mustBe OK
            assertPageAsExpected(
              asDocument(result),
              generateExpectedEditRecordPageFromRecord(oci)
                .copy(
                  optionsForCreators = Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)", selected = true),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(
                        "48N",
                        "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)",
                        selected = true
                      ),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    )
                  )
                )
            )

          }
          "we remove two in a row" in {

            val oci = "COAL.2022.V8RJW.P"
            val valuesForFirstRemoval = valuesFromRecord(oci) ++ Map(
              s"${FieldNames.creatorIDs}[0]" -> "92W",
              s"${FieldNames.creatorIDs}[1]" -> "48N",
              s"${FieldNames.creatorIDs}[2]" -> "8R6"
            )

            val resultAfterFirstRemoval = submitWhileLoggedIn("removeLastCreator", 1, oci, valuesForFirstRemoval)

            status(resultAfterFirstRemoval) mustBe OK
            assertPageAsExpected(
              asDocument(resultAfterFirstRemoval),
              generateExpectedEditRecordPageFromRecord(oci)
                .copy(
                  optionsForCreators = Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)", selected = true),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(
                        "48N",
                        "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)",
                        selected = true
                      ),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    )
                  )
                )
            )

            val valuesForSecondRemoval = (valuesFromRecord(oci) - s"${FieldNames.creatorIDs}[2]") ++ Map(
              s"${FieldNames.creatorIDs}[0]" -> "92W",
              s"${FieldNames.creatorIDs}[1]" -> "48N"
            )

            val resultAfterSecondRemoval = submitWhileLoggedIn("removeLastCreator", 1, oci, valuesForSecondRemoval)

            status(resultAfterSecondRemoval) mustBe OK
            assertPageAsExpected(
              asDocument(resultAfterSecondRemoval),
              generateExpectedEditRecordPageFromRecord(oci)
                .copy(
                  optionsForCreators = Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)", selected = true),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    )
                  )
                )
            )

          }
        }
      }
    }
  }

  private def submitWhileLoggedIn(
    action: String,
    editSetId: Int,
    recordId: String,
    values: Map[String, String]
  ): Future[Result] = {
    val request = CSRFTokenHelper.addCSRFToken(
      FakeRequest(POST, s"/edit-set/$editSetId/record/$recordId/edit")
        .withFormUrlEncodedBody((values ++ Map("action" -> action)).toSeq: _*)
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
    document must haveFormerReferencePro(expectedEditRecordPage.formerReferencePro)
    document must haveStartDateDay(expectedEditRecordPage.startDate.day)
    document must haveStartDateMonth(expectedEditRecordPage.startDate.month)
    document must haveStartDateYear(expectedEditRecordPage.startDate.year)
    document must haveEndDateDay(expectedEditRecordPage.endDate.day)
    document must haveEndDateMonth(expectedEditRecordPage.endDate.month)
    document must haveEndDateYear(expectedEditRecordPage.endDate.year)
    document must haveLegalStatus(expectedEditRecordPage.legalStatusID)
    document must haveSelectionForPlaceOfDeposit(expectedEditRecordPage.optionsForPlaceOfDepositID)

    document must haveNumberOfSelectionsForCreator(expectedEditRecordPage.optionsForCreators.size)
    expectedEditRecordPage.optionsForCreators.zipWithIndex.foreach { case (expectedSelectOptions, index) =>
      document must haveSelectionForCreator(index, expectedSelectOptions)
    }

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

    expectedEditRecordPage.errorMessageForCreator match {
      case Some(expectedErrorMessage) => document must haveErrorMessageForCreator(expectedErrorMessage)
      case None                       => document must haveNoErrorMessageForCreator
    }
  }

  private def assertPageAsExpected(document: Document, expectedEditRecordPage: ExpectedEditSetPage): Unit = {
    document must haveTitle(expectedEditRecordPage.title)
    document must haveCaption(expectedEditRecordPage.caption)
    document must haveActionButtons(expectedEditRecordPage.button.value, expectedEditRecordPage.button.label)
    document must haveSelectionForOrderingField(expectedEditRecordPage.expectedOptionsForField)
    document must haveSelectionForOrderingDirection(expectedEditRecordPage.expectedOptionsForDirection)
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

  }

  private def valuesFromRecord(oci: String): Map[String, String] = {
    val editSetRecord = getExpectedEditSetRecord(oci)
    val mapOfCreatorIDs = editSetRecord.creatorIDs.zipWithIndex.map { case (creatorId, index) =>
      val key = s"${FieldNames.creatorIDs}[$index]"
      (key, creatorId)
    }.toMap
    Map(
      FieldNames.background                -> editSetRecord.background,
      FieldNames.coveringDates             -> editSetRecord.coveringDates,
      FieldNames.custodialHistory          -> editSetRecord.custodialHistory,
      FieldNames.endDateDay                -> editSetRecord.endDateDay,
      FieldNames.endDateMonth              -> editSetRecord.endDateMonth,
      FieldNames.endDateYear               -> editSetRecord.endDateYear,
      FieldNames.formerReferenceDepartment -> editSetRecord.formerReferenceDepartment,
      FieldNames.formerReferencePro        -> editSetRecord.formerReferencePro,
      FieldNames.legalStatusID             -> editSetRecord.legalStatusID,
      FieldNames.note                      -> editSetRecord.note,
      FieldNames.oci                       -> editSetRecord.oci,
      FieldNames.placeOfDepositID          -> editSetRecord.placeOfDepositID,
      FieldNames.scopeAndContent           -> editSetRecord.scopeAndContent,
      FieldNames.startDateDay              -> editSetRecord.startDateDay,
      FieldNames.startDateMonth            -> editSetRecord.startDateMonth,
      FieldNames.startDateYear             -> editSetRecord.startDateYear
    ) ++ mapOfCreatorIDs

  }

  private def generateExpectedEditRecordPageFromRecord(oci: String): ExpectedEditRecordPage = {
    val editSetRecord = getExpectedEditSetRecord(oci)
    ExpectedEditRecordPage(
      title = "Edit record",
      heading = s"TNA reference: ${editSetRecord.ccr}",
      legend = "Intellectual properties",
      classicCatalogueRef = editSetRecord.ccr,
      omegaCatalogueId = editSetRecord.oci,
      scopeAndContent = editSetRecord.scopeAndContent,
      coveringDates = editSetRecord.coveringDates,
      formerReferenceDepartment = editSetRecord.formerReferenceDepartment,
      formerReferencePro = editSetRecord.formerReferencePro,
      startDate = ExpectedDate(editSetRecord.startDateDay, editSetRecord.startDateMonth, editSetRecord.startDateYear),
      endDate = ExpectedDate(editSetRecord.endDateDay, editSetRecord.endDateMonth, editSetRecord.endDateYear),
      legalStatusID = editSetRecord.legalStatusID,
      note = editSetRecord.note,
      background = editSetRecord.background,
      optionsForPlaceOfDepositID = Seq(
        ExpectedSelectOption("", "Select where this record is held", disabled = true),
        ExpectedSelectOption("1", "The National Archives, Kew", selected = true),
        ExpectedSelectOption("2", "British Museum, Department of Libraries and Archives"),
        ExpectedSelectOption("3", "British Library, National Sound Archive")
      ).map(expectedSelectedOption =>
        expectedSelectedOption.copy(selected = expectedSelectedOption.value == editSetRecord.placeOfDepositID)
      ),
      optionsForCreators = editSetRecord.creatorIDs
        .filter(creatorId => allCreators.exists(_.id == creatorId))
        .map(creatorId =>
          Seq(
            ExpectedSelectOption("", "Select creator", disabled = true),
            ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
            ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)", selected = true),
            ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
            ExpectedSelectOption("8R6", "Queen Anne's Bounty")
          ).map(expectedSelectedOption =>
            expectedSelectedOption.copy(selected = expectedSelectedOption.value == creatorId)
          )
        ),
      relatedMaterial = editSetRecord.relatedMaterial.map {
        case RelatedMaterial.LinkAndDescription(linkHref, linkText, description) =>
          ExpectedRelatedMaterial(linkHref = Some(linkHref), linkText = Some(linkText), description = Some(description))
        case RelatedMaterial.LinkOnly(linkHref, linkText) =>
          ExpectedRelatedMaterial(linkHref = Some(linkHref), linkText = Some(linkText))
        case RelatedMaterial.DescriptionOnly(description) => ExpectedRelatedMaterial(description = Some(description))
      },
      separatedMaterial = editSetRecord.separatedMaterial.map {
        case SeparatedMaterial.LinkAndDescription(linkHref, linkText, description) =>
          ExpectedSeparatedMaterial(
            linkHref = Some(linkHref),
            linkText = Some(linkText),
            description = Some(description)
          )
        case SeparatedMaterial.LinkOnly(linkHref, linkText) =>
          ExpectedSeparatedMaterial(linkHref = Some(linkHref), linkText = Some(linkText))
        case SeparatedMaterial.DescriptionOnly(description) =>
          ExpectedSeparatedMaterial(description = Some(description))
      },
      custodialHistory = editSetRecord.custodialHistory
    )

  }

  private def getExpectedEditSetRecord(oci: String): EditSetRecord =
    editSetRecordMap.getOrElse(oci, fail(s"Unable to get record for OCI [$oci]"))

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
    formerReferencePro: String,
    startDate: ExpectedDate,
    endDate: ExpectedDate,
    legalStatusID: String,
    note: String,
    background: String,
    custodialHistory: String,
    optionsForPlaceOfDepositID: Seq[ExpectedSelectOption],
    optionsForCreators: Seq[Seq[ExpectedSelectOption]],
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
    errorMessageForCustodialHistory: Option[String] = None,
    errorMessageForCreator: Option[String] = None
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
  )

  case class ExpectedEditSetSummaryRow(
    ccr: String,
    scopeAndContents: String,
    coveringDates: String
  )

}
