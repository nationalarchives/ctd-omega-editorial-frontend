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

import play.api.mvc._
import play.api.test.Helpers._
import play.api.test._
import support.BaseSpec
import support.CustomMatchers._
import uk.gov.nationalarchives.omega.editorial.controllers.{ EditSetController, SessionKeys }
import uk.gov.nationalarchives.omega.editorial.models.session.Session
import uk.gov.nationalarchives.omega.editorial.views.html.{ editSet, editSetRecordEdit, editSetRecordEditDiscard, editSetRecordEditSave }

import scala.concurrent.Future

/** Add your spec here. You can mock out a whole application including requests, plugins etc.
  *
  * For more information, see https://www.playframework.com/documentation/latest/ScalaTestingWithScalaTest
  */
class EditSetControllerSpec extends BaseSpec {

  val validSessionToken: String = Session.generateToken("1234")
  val invalidSessionToken: String = Session.generateToken("invalid-user")

  "EditSetController GET /edit-set/{id}" should {

    "render the edit set page from a new instance of controller" in {
      val defaultLang = play.api.i18n.Lang.defaultLang.code
      val messages: Map[String, Map[String, String]] =
        Map(defaultLang -> Map("edit-set.heading" -> "Edit set: COAL 80 Sample"))
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
        .view("COAL.2022.V5RJW.P")
        .apply(FakeRequest(GET, "/edit-set/1").withSession(SessionKeys.token -> validSessionToken))

      status(editSet) mustBe OK
      contentType(editSet) mustBe Some("text/html")
      contentAsString(editSet) must include("Edit set: COAL 80 Sample")
    }

    "render the edit set page from the application" in {
      val controller = inject[EditSetController]
      val editSet = controller
        .view("COAL.2022.V5RJW.P")
        .apply(
          FakeRequest(GET, "/edit-set/1")
            .withSession(SessionKeys.token -> validSessionToken)
        )

      status(editSet) mustBe OK
      contentType(editSet) mustBe Some("text/html")
      contentAsString(editSet) must include("Edit set: COAL 80 Sample")
    }

    "render the edit set page from the router" in {
      val request = FakeRequest(GET, "/edit-set/1").withSession(SessionKeys.token -> validSessionToken)
      val editSet = route(app, request).get

      status(editSet) mustBe OK
      contentType(editSet) mustBe Some("text/html")
      contentAsString(editSet) must include("Edit set: COAL 80 Sample")
    }

    "redirect to the login page from the application when requested with invalid session token" in {
      val controller = inject[EditSetController]
      val editSet = controller
        .view("COAL.2022.V5RJW.P")
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
        .editRecord("COAL.2022.V5RJW.P", "COAL.2022.V5RJW.P")
        .apply(
          CSRFTokenHelper.addCSRFToken(
            FakeRequest(GET, "/edit-set/1/record/COAL.2022.V5RJW.P/edit")
              .withSession(SessionKeys.token -> validSessionToken)
          )
        )

      status(editRecordPage) mustBe OK
      contentType(editRecordPage) mustBe Some("text/html")
      contentAsString(editRecordPage) must include("TNA reference: COAL 80/80/1")
    }

    "render the edit set page from the application" in {
      val controller = inject[EditSetController]
      val editRecordPage = controller
        .editRecord("COAL.2022.V5RJW.P", "COAL.2022.V5RJW.P")
        .apply(
          CSRFTokenHelper.addCSRFToken(
            FakeRequest(GET, "/edit-set/1/record/COAL.2022.V5RJW.P/edit")
              .withSession(SessionKeys.token -> validSessionToken)
          )
        )

      status(editRecordPage) mustBe OK
      contentType(editRecordPage) mustBe Some("text/html")
      contentAsString(editRecordPage) must include("TNA reference: COAL 80/80/1")
    }

    "render the edit set page from the router" in {
      val request =
        FakeRequest(GET, "/edit-set/1/record/COAL.2022.V5RJW.P/edit").withSession(
          SessionKeys.token -> validSessionToken
        )
      val editRecordPage = route(app, request).get

      status(editRecordPage) mustBe OK
      contentType(editRecordPage) mustBe Some("text/html")
      contentAsString(editRecordPage) must include("TNA reference: COAL 80/80/1")
    }

    "redirect to the login page from the application when requested with invalid session token" in {
      val controller = inject[EditSetController]
      val editRecordPage = controller
        .editRecord("COAL.2022.V5RJW.P", "COAL.2022.V5RJW.P")
        .apply(
          CSRFTokenHelper.addCSRFToken(
            FakeRequest(GET, "/edit-set/1/record/COAL.2022.V5RJW.P/edit")
              .withSession(SessionKeys.token -> invalidSessionToken)
          )
        )

      status(editRecordPage) mustBe SEE_OTHER
      redirectLocation(editRecordPage) mustBe Some("/login")
    }

    "redirect to the login page from the router when requested with invalid session token" in {
      val request =
        FakeRequest(GET, "/edit-set/1/record/COAL.2022.V5RJW.P/edit").withSession(
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
        "ccr"             -> "COAL 80/80/1",
        "oci"             -> "COAL.2022.V5RJW.P",
        "scopeAndContent" -> "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
        "formerReferenceDepartment" -> "1234",
        "coveringDates"             -> "2020 Oct",
        "startDateDay"              -> "1",
        "startDateMonth"            -> "10",
        "startDateYear"             -> "2020",
        "endDateDay"                -> "31",
        "endDateMonth"              -> "10",
        "endDateYear"               -> "2020",
        "action"                    -> "save"
      )

    "when the action is to save the record" when {
      "fail" when {
        "and yet preserve the CCR" when {
          "there are errors" in {
            val ccrToAssert = "COAL 80/80/1"
            val blankScopeAndContentToFailValidation = ""
            val request = CSRFTokenHelper.addCSRFToken(
              FakeRequest(POST, "/edit-set/1/record/COAL.2022.V5RJW.P/edit")
                .withFormUrlEncodedBody(
                  "ccr"                       -> ccrToAssert,
                  "oci"                       -> "1234",
                  "scopeAndContent"           -> blankScopeAndContentToFailValidation,
                  "formerReferenceDepartment" -> "1234",
                  "coveringDates"             -> "1234",
                  "startDate"                 -> "1234",
                  "endDate"                   -> "1234",
                  "action"                    -> "save"
                )
                .withSession(SessionKeys.token -> validSessionToken)
            )
            val editRecordPage = route(app, request).get

            status(editRecordPage) mustBe BAD_REQUEST

            val document = asDocument(contentAsString(editRecordPage))
            document must haveHeading(s"TNA reference: $ccrToAssert")
          }
        }
        "start date" when {
          "is empty" in {

            val values = validValues ++ Map("startDateDay" -> "", "startDateMonth" -> "", "startDateYear" -> "")

            val result = submitWhileLoggedIn(1, "COAL.2022.V5RJW.P", values)

            status(result) mustBe BAD_REQUEST
            val document = asDocument(contentAsString(result))
            document must haveSummaryErrorMessages(Set("Start date is not a valid date"))
            document must haveErrorMessageForStartDate("Start date is not a valid date")
            document must haveStartDateDay("")
            document must haveStartDateMonth("")
            document must haveStartDateYear("")

          }
          "is of an invalid format" in {

            val values = validValues ++ Map("startDateDay" -> "XX", "startDateMonth" -> "11", "startDateYear" -> "1960")

            val result = submitWhileLoggedIn(1, "COAL.2022.V5RJW.P", values)

            status(result) mustBe BAD_REQUEST
            val document = asDocument(contentAsString(result))
            document must haveSummaryErrorMessages(Set("Start date is not a valid date"))
            document must haveErrorMessageForStartDate("Start date is not a valid date")
            document must haveStartDateDay("XX")
            document must haveStartDateMonth("11")
            document must haveStartDateYear("1960")

          }
          "doesn't exist" in {

            val values = validValues ++
              Map(
                "action"         -> "save",
                "startDateDay"   -> "29",
                "startDateMonth" -> "2",
                "startDateYear"  -> "2022",
                "endDateDay"     -> "31",
                "endDateMonth"   -> "10",
                "endDateYear"    -> "2022"
              )

            val result = submitWhileLoggedIn(2, "COAL.2022.V5RJW.R", values)

            status(result) mustBe BAD_REQUEST
            val document = asDocument(contentAsString(result))

            document must haveSummaryErrorMessages(Set("Start date is not a valid date"))
            document must haveErrorMessageForStartDate("Start date is not a valid date")
            document must haveStartDateDay("29")
            document must haveStartDateMonth("2")
            document must haveStartDateYear("2022")

          }
        }
        "end date" when {
          "is empty" in {

            val values = validValues ++ Map("endDateDay" -> "", "endDateMonth" -> "", "endDateYear" -> "")

            val result = submitWhileLoggedIn(1, "COAL.2022.V5RJW.P", values)

            status(result) mustBe BAD_REQUEST

            val document = asDocument(contentAsString(result))
            document must haveSummaryErrorMessages(Set("End date is not a valid date"))
            document must haveErrorMessageForEndDate("End date is not a valid date")
            document must haveEndDateDay("")
            document must haveEndDateMonth("")
            document must haveEndDateYear("")

          }
          "is of an invalid format" in {

            val values = validValues ++ Map("endDateDay" -> "XX", "endDateMonth" -> "12", "endDateYear" -> "2000")
            val result = submitWhileLoggedIn(1, "COAL.2022.V5RJW.P", values)

            status(result) mustBe BAD_REQUEST
            val document = asDocument(contentAsString(result))

            document must haveSummaryErrorMessages(Set("End date is not a valid date"))
            document must haveErrorMessageForEndDate("End date is not a valid date")
            document must haveEndDateDay("XX")
            document must haveEndDateMonth("12")
            document must haveEndDateYear("2000")

          }
          "doesn't exist" in {

            val values = validValues ++
              Map(
                "action"         -> "save",
                "startDateDay"   -> "1",
                "startDateMonth" -> "2",
                "startDateYear"  -> "2022",
                "endDateDay"     -> "29",
                "endDateMonth"   -> "2",
                "endDateYear"    -> "2022"
              )
            val result = submitWhileLoggedIn(1, "COAL.2022.V5RJW.P", values)

            status(result) mustBe BAD_REQUEST
            val document = asDocument(contentAsString(result))
            document must haveSummaryErrorMessages(Set("End date is not a valid date"))
            document must haveErrorMessageForEndDate("End date is not a valid date")
            document must haveEndDateDay("29")
            document must haveEndDateMonth("2")
            document must haveEndDateYear("2022")

          }
          "is before start date" in {

            val values = validValues ++ Map(
              "startDateDay"   -> "12",
              "startDateMonth" -> "10",
              "startDateYear"  -> "2020",
              "endDateDay"     -> "11",
              "endDateMonth"   -> "10",
              "endDateYear"    -> "2020"
            )

            val result = submitWhileLoggedIn(1, "COAL.2022.V5RJW.P", values)

            status(result) mustBe BAD_REQUEST
            val document = asDocument(contentAsString(result))
            document must haveSummaryErrorMessages(Set("End date cannot precede start date"))
            document must haveErrorMessageForEndDate("End date cannot precede start date")
            document must haveNoErrorMessageForStartDate
            document must haveStartDateDay("12")
            document must haveStartDateMonth("10")
            document must haveStartDateYear("2020")
            document must haveEndDateDay("11")
            document must haveEndDateMonth("10")
            document must haveEndDateYear("2020")

          }
        }
        "neither start date nor end date is valid" in {

          val values = validValues ++ Map(
            "startDateDay"   -> "12",
            "startDateMonth" -> "14",
            "startDateYear"  -> "2020",
            "endDateDay"     -> "42",
            "endDateMonth"   -> "12",
            "endDateYear"    -> "2020"
          )

          val result = submitWhileLoggedIn(1, "COAL.2022.V5RJW.P", values)

          status(result) mustBe BAD_REQUEST
          val document = asDocument(contentAsString(result))
          document must haveSummaryErrorMessages(Set("Start date is not a valid date", "End date is not a valid date"))
          document must haveErrorMessageForStartDate("Start date is not a valid date")
          document must haveStartDateDay("12")
          document must haveStartDateMonth("14")
          document must haveStartDateYear("2020")
          document must haveErrorMessageForEndDate("End date is not a valid date")
          document must haveEndDateDay("42")
          document must haveEndDateMonth("12")
          document must haveEndDateYear("2020")

        }
        "covering date" when {
          "is invalid" in {
            val request = CSRFTokenHelper.addCSRFToken(
              FakeRequest(POST, "/edit-set/1/record/COAL.2022.V5RJW.P/edit")
                .withFormUrlEncodedBody(
                  "ccr"                       -> "1234",
                  "oci"                       -> "1234",
                  "scopeAndContent"           -> "1234",
                  "formerReferenceDepartment" -> "1234",
                  "coveringDates"             -> "Oct 1 2004",
                  "startDate"                 -> "1234",
                  "endDate"                   -> "1234",
                  "action"                    -> "save"
                )
                .withSession(
                  SessionKeys.token -> validSessionToken
                )
            )
            val editRecordPage = route(app, request).get

            status(editRecordPage) mustBe BAD_REQUEST
          }
          "is too long" in {
            val gapDateTooLong = (1 to 100).map(_ => "2004 Oct 1").mkString(";")
            val request = CSRFTokenHelper.addCSRFToken(
              FakeRequest(POST, "/edit-set/1/record/COAL.2022.V5RJW.P/edit")
                .withFormUrlEncodedBody(
                  "ccr"                       -> "1234",
                  "oci"                       -> "1234",
                  "scopeAndContent"           -> "1234",
                  "formerReferenceDepartment" -> "1234",
                  "coveringDates"             -> gapDateTooLong,
                  "startDateDay"              -> "26",
                  "startDateMonth"            -> "12",
                  "startDateYear"             -> "2020",
                  "endDateDay"                -> "31",
                  "endDateMonth"              -> "12",
                  "endDateYear"               -> "2020",
                  "action"                    -> "save"
                )
                .withSession(
                  SessionKeys.token -> validSessionToken
                )
            )
            val editRecordPage = route(app, request).get

            status(editRecordPage) mustBe BAD_REQUEST

            val document = asDocument(contentAsString(editRecordPage))
            document must haveSummaryErrorMessages(Set("Covering date too long, maximum length 255 characters"))

          }
          "is empty; showing error correctly" in {
            val request = CSRFTokenHelper.addCSRFToken(
              FakeRequest(POST, "/edit-set/1/record/COAL.2022.V5RJW.P/edit")
                .withFormUrlEncodedBody(
                  "ccr"                       -> "1234",
                  "oci"                       -> "1234",
                  "scopeAndContent"           -> "1234",
                  "formerReferenceDepartment" -> "1234",
                  "coveringDates"             -> "  ",
                  "startDateDay"              -> "26",
                  "startDateMonth"            -> "12",
                  "startDateYear"             -> "2020",
                  "endDateDay"                -> "31",
                  "endDateMonth"              -> "12",
                  "endDateYear"               -> "2020",
                  "action"                    -> "save"
                )
                .withSession(
                  SessionKeys.token -> validSessionToken
                )
            )
            val editRecordPage = route(app, request).get

            status(editRecordPage) mustBe BAD_REQUEST

            val document = asDocument(contentAsString(editRecordPage))
            document must haveSummaryErrorMessages(Set("Enter the covering dates", "Covering date format is not valid"))

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

          val editRecordPage = controller
            .submit("1", "COAL.2022.V5RJW.P")
            .apply(
              CSRFTokenHelper
                .addCSRFToken(
                  FakeRequest(POST, "/edit-set/1/record/COAL.2022.V5RJW.P/edit")
                    .withFormUrlEncodedBody(
                      "ccr"                       -> "1234",
                      "oci"                       -> "1234",
                      "scopeAndContent"           -> "1234",
                      "formerReferenceDepartment" -> "1234",
                      "coveringDates"             -> "1234",
                      "startDateDay"              -> "26",
                      "startDateMonth"            -> "12",
                      "startDateYear"             -> "2020",
                      "endDateDay"                -> "31",
                      "endDateMonth"              -> "12",
                      "endDateYear"               -> "2020",
                      "legalStatus"               -> "1234",
                      "action"                    -> "save"
                    )
                    .withSession(SessionKeys.token -> validSessionToken)
                )
            )

          status(editRecordPage) mustBe SEE_OTHER
          redirectLocation(editRecordPage) mustBe Some("/edit-set/1/record/1234/edit/save")
        }

        "redirect to result page of the application" in {
          val controller = inject[EditSetController]
          val editRecordPage = controller
            .submit("1", "COAL.2022.V5RJW.P")
            .apply(
              CSRFTokenHelper.addCSRFToken(
                FakeRequest(POST, "/edit-set/1/record/COAL.2022.V5RJW.P/edit")
                  .withFormUrlEncodedBody(
                    "ccr"                       -> "1234",
                    "oci"                       -> "1234",
                    "scopeAndContent"           -> "1234",
                    "formerReferenceDepartment" -> "1234",
                    "coveringDates"             -> "1234",
                    "startDateDay"              -> "26",
                    "startDateMonth"            -> "12",
                    "startDateYear"             -> "2020",
                    "endDateDay"                -> "31",
                    "endDateMonth"              -> "12",
                    "endDateYear"               -> "2020",
                    "legalStatus"               -> "1234",
                    "action"                    -> "save"
                  )
                  .withSession(SessionKeys.token -> validSessionToken)
              )
            )

          status(editRecordPage) mustBe SEE_OTHER
          redirectLocation(editRecordPage) mustBe Some("/edit-set/1/record/1234/edit/save")

        }

        "redirect to result page from the router" in {
          val request = CSRFTokenHelper.addCSRFToken(
            FakeRequest(POST, "/edit-set/1/record/COAL.2022.V5RJW.P/edit")
              .withFormUrlEncodedBody(
                "ccr"                       -> "1234",
                "oci"                       -> "1234",
                "scopeAndContent"           -> "1234",
                "formerReferenceDepartment" -> "1234",
                "coveringDates"             -> "1234",
                "startDateDay"              -> "26",
                "startDateMonth"            -> "12",
                "startDateYear"             -> "2020",
                "endDateDay"                -> "31",
                "endDateMonth"              -> "12",
                "endDateYear"               -> "2020",
                "legalStatus"               -> "1234",
                "action"                    -> "save"
              )
              .withSession(SessionKeys.token -> validSessionToken)
          )
          val editRecordPage = route(app, request).get

          status(editRecordPage) mustBe SEE_OTHER
          redirectLocation(editRecordPage) mustBe Some("/edit-set/1/record/1234/edit/save")

          val getRecordResult = getRecordForEditingWhileLoggedIn(1, "1234")
          val document = asDocument(contentAsString(getRecordResult))
          document must haveStartDateDay("26")
          document must haveStartDateMonth("12")
          document must haveStartDateYear("2020")
          document must haveEndDateDay("31")
          document must haveEndDateMonth("12")
          document must haveEndDateYear("2020")

        }

      }

    }

    "when the action is to discard all changes" when {
      "successful" when {
        "even if the validation fails" in {
          val blankScopeAndContentToFailValidation = ""
          val request = CSRFTokenHelper.addCSRFToken(
            FakeRequest(POST, "/edit-set/1/record/COAL.2022.V5RJW.P/edit")
              .withFormUrlEncodedBody(
                "ccr"                       -> "1234",
                "oci"                       -> "1234",
                "scopeAndContent"           -> blankScopeAndContentToFailValidation,
                "formerReferenceDepartment" -> "1234",
                "coveringDates"             -> "1234",
                "startDateDay"              -> "26",
                "startDateMonth"            -> "12",
                "startDateYear"             -> "2020",
                "endDateDay"                -> "31",
                "endDateMonth"              -> "12",
                "endDateYear"               -> "2020",
                "legalStatus"               -> "1234",
                "action"                    -> "discard"
              )
              .withSession(SessionKeys.token -> validSessionToken)
          )
          val editRecordPage = route(app, request).get

          status(editRecordPage) mustBe SEE_OTHER
          redirectLocation(editRecordPage) mustBe Some("/edit-set/1/record/COAL.2022.V5RJW.P/edit/discard")

        }
      }
    }

    "when the action is to calculate the start and end dates from the covering dates" when {
      "failure" when {
        "blank" in {

          val values = validValues ++ Map(
            "action"         -> "calculateDates",
            "coveringDates"  -> "   ",
            "startDateDay"   -> "1",
            "startDateMonth" -> "10",
            "startDateYear"  -> "2020",
            "endDateDay"     -> "31",
            "endDateMonth"   -> "10",
            "endDateYear"    -> "2020"
          )

          val result = submitWhileLoggedIn(1, "COAL.2022.V5RJW.P", values)

          status(result) mustBe BAD_REQUEST
          val document = asDocument(contentAsString(result))
          document must haveSummaryErrorMessages(Set("Enter the covering dates", "Covering date format is not valid"))
          document must haveErrorMessageForCoveringDate("Enter the covering dates")
          document must haveNoErrorMessageForStartDate
          document must haveStartDateDay("1")
          document must haveStartDateMonth("10")
          document must haveStartDateYear("2020")
          document must haveNoErrorMessageForEndDate
          document must haveEndDateDay("31")
          document must haveEndDateMonth("10")
          document must haveEndDateYear("2020")

        }
        "invalid format" in {

          val values = validValues ++ Map(
            "action"         -> "calculateDates",
            "coveringDates"  -> "1270s",
            "startDateDay"   -> "1",
            "startDateMonth" -> "10",
            "startDateYear"  -> "2020",
            "endDateDay"     -> "31",
            "endDateMonth"   -> "10",
            "endDateYear"    -> "2020"
          )

          val result = submitWhileLoggedIn(1, "COAL.2022.V5RJW.P", values)

          status(result) mustBe BAD_REQUEST
          val document = asDocument(contentAsString(result))
          document must haveSummaryErrorMessages(Set("Covering date format is not valid"))
          document must haveErrorMessageForCoveringDate("Covering date format is not valid")
          document must haveNoErrorMessageForStartDate
          document must haveStartDateDay("1")
          document must haveStartDateMonth("10")
          document must haveStartDateYear("2020")
          document must haveNoErrorMessageForEndDate
          document must haveEndDateDay("31")
          document must haveEndDateMonth("10")
          document must haveEndDateYear("2020")

        }
        "contains a non-existent date" in {

          val values = validValues ++ Map(
            "action"         -> "calculateDates",
            "coveringDates"  -> "2022 Feb 1-2022 Feb 29",
            "startDateDay"   -> "1",
            "startDateMonth" -> "10",
            "startDateYear"  -> "2020",
            "endDateDay"     -> "31",
            "endDateMonth"   -> "10",
            "endDateYear"    -> "2020"
          )

          val result = submitWhileLoggedIn(1, "COAL.2022.V5RJW.P", values)

          status(result) mustBe BAD_REQUEST
          val document = asDocument(contentAsString(result))
          document must haveSummaryErrorMessages(Set("Covering date format is not valid"))
          document must haveErrorMessageForCoveringDate("Covering date format is not valid")
          document must haveNoErrorMessageForStartDate
          document must haveStartDateDay("1")
          document must haveStartDateMonth("10")
          document must haveStartDateYear("2020")
          document must haveNoErrorMessageForEndDate
          document must haveEndDateDay("31")
          document must haveEndDateMonth("10")
          document must haveEndDateYear("2020")

        }
      }
      "successful" when {
        "covers period of the switchover" in {

          val values = validValues ++ Map(
            "action"        -> "calculateDates",
            "coveringDates" -> "1752 Aug 1-1752 Sept 12"
          )

          val result = submitWhileLoggedIn(1, "COAL.2022.V5RJW.P", values)

          status(result) mustBe OK
          val document = asDocument(contentAsString(result))
          document must haveNoSummaryErrorMessages
          document must haveNoErrorMessageForCoveringDate
          document must haveNoErrorMessageForStartDate
          document must haveStartDateDay("1")
          document must haveStartDateMonth("8")
          document must haveStartDateYear("1752")
          document must haveNoErrorMessageForEndDate
          document must haveEndDateDay("12")
          document must haveEndDateMonth("9")
          document must haveEndDateYear("1752")

        }
        "covers period after the switchover" in {

          val values = validValues ++ Map(
            "action"        -> "calculateDates",
            "coveringDates" -> "1984 Dec"
          )

          val result = submitWhileLoggedIn(1, "COAL.2022.V5RJW.P", values)

          status(result) mustBe OK
          val document = asDocument(contentAsString(result))
          document must haveNoSummaryErrorMessages
          document must haveNoErrorMessageForCoveringDate
          document must haveNoErrorMessageForStartDate
          document must haveStartDateDay("1")
          document must haveStartDateMonth("12")
          document must haveStartDateYear("1984")
          document must haveNoErrorMessageForEndDate
          document must haveEndDateDay("31")
          document must haveEndDateMonth("12")
          document must haveEndDateYear("1984")

        }
        "covers multiple ranges" in {

          val values = validValues ++ Map(
            "action"        -> "calculateDates",
            "coveringDates" -> "1868; 1890-1902; 1933"
          )

          val result = submitWhileLoggedIn(1, "COAL.2022.V5RJW.P", values)

          status(result) mustBe OK
          val document = asDocument(contentAsString(result))
          document must haveNoSummaryErrorMessages
          document must haveNoErrorMessageForCoveringDate
          document must haveNoErrorMessageForStartDate
          document must haveStartDateDay("1")
          document must haveStartDateMonth("1")
          document must haveStartDateYear("1868")
          document must haveNoErrorMessageForEndDate
          document must haveEndDateDay("31")
          document must haveEndDateMonth("12")
          document must haveEndDateYear("1933")

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
}
