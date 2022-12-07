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

import play.api.mvc.{ AnyContentAsEmpty, DefaultActionBuilder, DefaultMessagesActionBuilderImpl, DefaultMessagesControllerComponents }
import play.api.test.Helpers._
import play.api.test._
import support.BaseSpec
import uk.gov.nationalarchives.omega.editorial.controllers.EditSetController
import uk.gov.nationalarchives.omega.editorial.models.session.Session
import uk.gov.nationalarchives.omega.editorial.views.html.{ editSet, editSetRecordEdit, editSetRecordEditDiscard, editSetRecordEditSave }

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
        .apply(FakeRequest(GET, "/edit-set/1").withSession("sessionToken" -> validSessionToken))

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
            .withSession("sessionToken" -> validSessionToken)
        )

      status(editSet) mustBe OK
      contentType(editSet) mustBe Some("text/html")
      contentAsString(editSet) must include("Edit set: COAL 80 Sample")
    }

    "render the edit set page from the router" in {
      val request = FakeRequest(GET, "/edit-set/1").withSession("sessionToken" -> validSessionToken)
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
            .withSession("sessionToken" -> invalidSessionToken)
        )

      status(editSet) mustBe SEE_OTHER
      redirectLocation(editSet) mustBe Some("/login")
    }

    "redirect to the login page from the router when requested with invalid session token" in {
      val request = FakeRequest(GET, "/edit-set/1").withSession("sessionToken" -> invalidSessionToken)
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
              .withSession("sessionToken" -> validSessionToken)
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
              .withSession("sessionToken" -> validSessionToken)
          )
        )

      status(editRecordPage) mustBe OK
      contentType(editRecordPage) mustBe Some("text/html")
      contentAsString(editRecordPage) must include("TNA reference: COAL 80/80/1")
    }

    "render the edit set page from the router" in {
      val request =
        FakeRequest(GET, "/edit-set/1/record/COAL.2022.V5RJW.P/edit").withSession("sessionToken" -> validSessionToken)
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
              .withSession("sessionToken" -> invalidSessionToken)
          )
        )

      status(editRecordPage) mustBe SEE_OTHER
      redirectLocation(editRecordPage) mustBe Some("/login")
    }

    "redirect to the login page from the router when requested with invalid session token" in {
      val request =
        FakeRequest(GET, "/edit-set/1/record/COAL.2022.V5RJW.P/edit").withSession("sessionToken" -> invalidSessionToken)
      val editRecordPage = route(app, request).get

      status(editRecordPage) mustBe SEE_OTHER
      redirectLocation(editRecordPage) mustBe Some("/login")
    }
  }

  "EditSetController POST /edit-set/{id}/record/{recordId}/edit" should {
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
        .submit("COAL.2022.V5RJW.P", "COAL.2022.V5RJW.P")
        .apply(
          CSRFTokenHelper
            .addCSRFToken(
              FakeRequest(POST, "/edit-set/1/record/COAL.2022.V5RJW.P/edit").withFormUrlEncodedBody(
                "ccr"                       -> "1234",
                "oci"                       -> "1234",
                "scopeAndContent"           -> "1234",
                "formerReferenceDepartment" -> "1234",
                "coveringDates"             -> "1234",
                "startDate"                 -> "1234",
                "endDate"                   -> "1234",
                "action"                    -> "save"
              )
            )
        )
      status(editRecordPage) mustBe SEE_OTHER
    }

    "redirect to result page of the application" in {
      val controller = inject[EditSetController]
      val editRecordPage = controller
        .submit("COAL.2022.V5RJW.P", "COAL.2022.V5RJW.P")
        .apply(
          CSRFTokenHelper.addCSRFToken(
            FakeRequest(POST, "/edit-set/1/record/COAL.2022.V5RJW.P/edit").withFormUrlEncodedBody(
              "ccr"                       -> "1234",
              "oci"                       -> "1234",
              "scopeAndContent"           -> "1234",
              "formerReferenceDepartment" -> "1234",
              "coveringDates"             -> "1234",
              "startDate"                 -> "1234",
              "endDate"                   -> "1234",
              "action"                    -> "discard"
            )
          )
        )

      status(editRecordPage) mustBe SEE_OTHER
    }

    "redirect to result page from the router" in {
      val request = CSRFTokenHelper.addCSRFToken(
        FakeRequest(POST, "/edit-set/1/record/COAL.2022.V5RJW.P/edit").withFormUrlEncodedBody(
          "ccr"                       -> "1234",
          "oci"                       -> "1234",
          "scopeAndContent"           -> "1234",
          "formerReferenceDepartment" -> "1234",
          "coveringDates"             -> "1234",
          "startDate"                 -> "1234",
          "endDate"                   -> "1234",
          "action"                    -> "save"
        )
      )
      val editRecordPage = route(app, request).get

      status(editRecordPage) mustBe SEE_OTHER
    }
  }

}
