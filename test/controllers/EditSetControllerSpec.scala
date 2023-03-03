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
import play.api.test._
import play.api.test.Helpers._

import scala.concurrent.ExecutionContext.Implicits.global
import uk.gov.nationalarchives.omega.editorial.controllers.{ EditSetController, SessionKeys }
import play.twirl.api.Html
import support.BasePlaySpec
import uk.gov.nationalarchives.omega.editorial.editSets.getEditSet
import uk.gov.nationalarchives.omega.editorial.models.User
import uk.gov.nationalarchives.omega.editorial.services.EditSetPagination.EditSetPage
import uk.gov.nationalarchives.omega.editorial.services.{ EditSetEntryRowOrder, EditSetService }
import uk.gov.nationalarchives.omega.editorial.editSetRecords.restoreOriginalRecords
import uk.gov.nationalarchives.omega.editorial.views.html.editSet

/** Add your spec here. You can mock out a whole application including requests, plugins etc.
  *
  * For more information, see https://www.playframework.com/documentation/latest/ScalaTestingWithScalaTest
  */
class EditSetControllerSpec extends BasePlaySpec {
  override def beforeEach(): Unit = {
    super.beforeEach()
    restoreOriginalRecords()
  }

  "EditSetController GET /edit-set/{id}" should {

    "render the edit set page from a new instance of controller" in {
      val mockMessagesApi = stubMessagesApi(messages)
      val mockEditSet = mock[editSet]
      val mockEditSetService = mock[EditSetService]
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
      contentType(editSet) mustBe Some("text/html")
    }

    "order records" when {

      def orderingRequest(field: String, direction: String, offset: Int = 1) = {
        val request = FakeRequest(GET, s"/edit-set/1?field=$field&direction=$direction&offset=$offset")
          .withSession(SessionKeys.token -> validSessionToken)
        val mockMessagesApi = stubMessagesApi(messages)
        val mockEditSet = mock[editSet]
        val mockEditSetService = mock[EditSetService]
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
          .apply(CSRFTokenHelper.addCSRFToken(request))

        verify(mockEditSetService).get(anyString())

        editSet
      }

      "with request parameters" in {
        val page = orderingRequest("ccr", "ascending")

        status(page) mustBe OK
        contentType(page) mustBe Some("text/html")
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
