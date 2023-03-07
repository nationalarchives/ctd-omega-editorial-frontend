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

import cats.effect.IO
import org.mockito.ArgumentMatchers.anyString
import play.api.i18n.Messages
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test._
import play.twirl.api.Html
import support.BaseControllerSpec
import uk.gov.nationalarchives.omega.editorial.controllers.{ EditSetController, SessionKeys }
import uk.gov.nationalarchives.omega.editorial.editSetRecords.restoreOriginalRecords
import uk.gov.nationalarchives.omega.editorial.editSets.getEditSet
import uk.gov.nationalarchives.omega.editorial.models.User
import uk.gov.nationalarchives.omega.editorial.services.Direction.{ Ascending, Descending }
import uk.gov.nationalarchives.omega.editorial.services.EditSetEntryRowOrder.{ CCROrder, CoveringDatesOrder, ScopeAndContentOrder }
import uk.gov.nationalarchives.omega.editorial.services.EditSetPagination.EditSetPage
import uk.gov.nationalarchives.omega.editorial.services.{ EditSetEntryRowOrder, EditSetService }
import uk.gov.nationalarchives.omega.editorial.views.html.editSet

import scala.concurrent.ExecutionContext.Implicits.global

/** Add your spec here. You can mock out a whole application including requests, plugins etc.
  *
  * For more information, see https://www.playframework.com/documentation/latest/ScalaTestingWithScalaTest
  */
class EditSetControllerSpec extends BaseControllerSpec {
  override def beforeEach(): Unit = {
    super.beforeEach()
    restoreOriginalRecords()
  }

  "EditSetController GET /edit-set/{id}" should {

    "render the edit set page from a new instance of controller" in {
      val mockEditSet = mock[editSet]
      val mockEditSetService = mock[EditSetService]
      val controller = new EditSetController(
        Helpers.stubMessagesControllerComponents(),
        mockEditSetService,
        mockEditSet
      )
      when(mockEditSetService.get("1")).thenReturn(IO.pure(getEditSet()))
      when(
        mockEditSet(any[User], anyString(), anyString(), any[EditSetPage], any[EditSetEntryRowOrder])(
          any[Messages],
          any[Request[AnyContent]]
        )
      ).thenReturn(getEditSetDisplay)
      val editSet = controller
        .view("1")
        .apply(
          CSRFTokenHelper.addCSRFToken(
            FakeRequest(GET, "/edit-set/1").withSession(SessionKeys.token -> validSessionToken)
          )
        )

      status(editSet) mustBe OK
    }

    "redirect to the login page from the application when requested with invalid session token" in {
      val mockEditSet = mock[editSet]
      val mockEditSetService = mock[EditSetService]
      val controller = new EditSetController(
        Helpers.stubMessagesControllerComponents(),
        mockEditSetService,
        mockEditSet
      )
      val editSet = controller
        .view("1")
        .apply(
          CSRFTokenHelper.addCSRFToken(
            FakeRequest(GET, "/edit-set/1").withSession(SessionKeys.token -> invalidSessionToken)
          )
        )

      status(editSet) mustBe SEE_OTHER
    }

    "order records" when {

      def orderingRequest(mockEditSet: editSet, field: String, direction: String, offset: Int = 1) = {
        val request = FakeRequest(GET, s"/edit-set/1?field=$field&direction=$direction&offset=$offset")
          .withSession(SessionKeys.token -> validSessionToken)
        val mockEditSetService = mock[EditSetService]
        val controller = new EditSetController(
          Helpers.stubMessagesControllerComponents(),
          mockEditSetService,
          mockEditSet
        )
        when(mockEditSetService.get("1")).thenReturn(IO.pure(getEditSet()))
        when(
          mockEditSet(any[User], anyString, anyString, any[EditSetPage], any[EditSetEntryRowOrder])(
            any[Messages],
            any[Request[AnyContent]]
          )
        ).thenReturn(getEditSetDisplay)

        val editSet = controller
          .view("1")
          .apply(CSRFTokenHelper.addCSRFToken(request))

        verify(mockEditSetService, times(1)).get(anyString())

        editSet
      }

      "CCR, ascending" in {
        val mockEditSet = mock[editSet]
        val page = orderingRequest(mockEditSet, "ccr", "ascending")
        status(page) mustBe OK
        verify(mockEditSet).apply(any[User], anyString, anyString, any[EditSetPage], eqTo(CCROrder(Ascending)))(
          any[Messages],
          any[Request[AnyContent]]
        )

      }

      "CCR, descending" in {
        val mockEditSet = mock[editSet]
        val page = orderingRequest(mockEditSet, "ccr", "descending")
        status(page) mustBe OK
        verify(mockEditSet).apply(any[User], anyString, anyString, any[EditSetPage], eqTo(CCROrder(Descending)))(
          any[Messages],
          any[Request[AnyContent]]
        )
      }

      "Scope and Content, ascending" in {
        val mockEditSet = mock[editSet]
        val page = orderingRequest(mockEditSet, "scope-and-content", "ascending")
        status(page) mustBe OK
        verify(mockEditSet)
          .apply(any[User], anyString, anyString, any[EditSetPage], eqTo(ScopeAndContentOrder(Ascending)))(
            any[Messages],
            any[Request[AnyContent]]
          )
      }

      "Scope and Content, descending" in {
        val mockEditSet = mock[editSet]
        val page = orderingRequest(mockEditSet, "scope-and-content", "descending")
        status(page) mustBe OK
        verify(mockEditSet)
          .apply(any[User], anyString, anyString, any[EditSetPage], eqTo(ScopeAndContentOrder(Descending)))(
            any[Messages],
            any[Request[AnyContent]]
          )
      }

      "Covering dates, ascending" in {
        val mockEditSet = mock[editSet]
        val page = orderingRequest(mockEditSet, "covering-dates", "ascending")
        status(page) mustBe OK
        verify(mockEditSet)
          .apply(any[User], anyString, anyString, any[EditSetPage], eqTo(CoveringDatesOrder(Ascending)))(
            any[Messages],
            any[Request[AnyContent]]
          )
      }

      "Covering dates, descending" in {
        val mockEditSet = mock[editSet]
        val page = orderingRequest(mockEditSet, "covering-dates", "descending")
        status(page) mustBe OK
        verify(mockEditSet)
          .apply(any[User], anyString, anyString, any[EditSetPage], eqTo(CoveringDatesOrder(Descending)))(
            any[Messages],
            any[Request[AnyContent]]
          )
      }

      "Covering dates, normalizing direction names" in {
        val mockEditSet = mock[editSet]
        val page = orderingRequest(mockEditSet, "covering-dates", "Descending")
        status(page) mustBe OK
        verify(mockEditSet)
          .apply(any[User], anyString, anyString, any[EditSetPage], eqTo(CoveringDatesOrder(Descending)))(
            any[Messages],
            any[Request[AnyContent]]
          )
      }

      "Unknown field and direction" in {
        val mockEditSet = mock[editSet]
        val page = orderingRequest(mockEditSet, "height", "upwards")
        status(page) mustBe OK
        verify(mockEditSet)
          .apply(any[User], anyString, anyString, any[EditSetPage], eqTo(CCROrder(Ascending)))(
            any[Messages],
            any[Request[AnyContent]]
          )
      }

      "Page 2 sorted by CCR, ascending" in {
        val mockEditSet = mock[editSet]
        val page = orderingRequest(mockEditSet, "ccr", "ascending", offset = 2)
        status(page) mustBe OK
        verify(mockEditSet)
          .apply(any[User], anyString, anyString, any[EditSetPage], eqTo(CCROrder(Ascending)))(
            any[Messages],
            any[Request[AnyContent]]
          )
      }
    }

  }
  private def getEditSetDisplay: Html = new Html(
    """
      |<html>
      | <head>
      |   <title>Test Page</title>
      |   <body>
      |     <input type='button' name='b' value='Click Me' onclick='document.title="scalatest"' />
      |   </body>
      | </head>
      |</html>
            """.stripMargin
  )

}
