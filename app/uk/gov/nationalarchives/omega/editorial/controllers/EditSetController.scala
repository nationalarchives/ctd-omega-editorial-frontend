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

package uk.gov.nationalarchives.omega.editorial.controllers

import cats.effect.unsafe.implicits.global
import play.api.i18n.I18nSupport
import play.api.mvc._
import uk.gov.nationalarchives.omega.editorial.controllers.authentication.Secured
import uk.gov.nationalarchives.omega.editorial.models._
import uk.gov.nationalarchives.omega.editorial.services.{ EditSetEntryRowOrder, EditSetPagination, EditSetService }
import uk.gov.nationalarchives.omega.editorial.support.{ FormSupport, MessageSupport }
import uk.gov.nationalarchives.omega.editorial.views.html.editSet

import javax.inject._
import scala.concurrent.{ ExecutionContext, Future }

/** This controller creates an `Action` to handle HTTP requests to the application's home page.
  */
@Singleton
class EditSetController @Inject() (
  messagesControllerComponents: MessagesControllerComponents,
  editSetService: EditSetService,
  editSet: editSet
)(implicit ec: ExecutionContext)
    extends MessagesAbstractController(messagesControllerComponents) with I18nSupport with Secured with FormSupport
    with MessageSupport {
  import EditSetController._

  /** Create an Action for the edit set page.
    *
    * The configuration in the `routes` file means that this method will be called when the application receives a `GET`
    * request with a path of `/edit-set/{id}`.
    */
  def view(id: String): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    withUserAsync { user =>
      val ordering = for {
        field     <- queryStringValue(request, fieldKey)
        direction <- queryStringValue(request, orderDirectionKey)
      } yield EditSetEntryRowOrder.fromNames(field, direction)

      generateEditSetView(id, user, ordering.getOrElse(EditSetEntryRowOrder.defaultOrder))
    }
  }

  private def queryStringValue(request: Request[AnyContent], key: String): Option[String] =
    request.queryString.get(key).flatMap(_.headOption)

  private def generateEditSetView(id: String, user: User, editSetEntryRowOrder: EditSetEntryRowOrder)(implicit
    request: Request[AnyContent]
  ): Future[Result] =
    editSetService.get(id).unsafeToFuture().map { currentEditSet =>
      val pageNumber = queryStringValue(request, offsetKey).map(_.toInt).getOrElse(1)
      val sortedEntries = currentEditSet.entries.sorted(editSetEntryRowOrder.currentOrdering)

      val editSetPage = new EditSetPagination(
        id = id,
        rowOrder = editSetEntryRowOrder,
        nextText = resolveMessage("edit-set.pagination.next"),
        previousText = resolveMessage("edit-set.pagination.previous")
      ).makeEditSetPage(sortedEntries, pageNumber)

      val title = resolveMessage("edit-set.title", editSetPage.pageNumber, editSetPage.totalPages)
      val heading: String = resolveMessage(
        "edit-set.heading",
        currentEditSet.name,
        editSetPage.numberOfFirstEntry.toString,
        editSetPage.numberOfLastEntry.toString,
        editSetPage.totalNumberOfEntries.toString
      )
      Ok(editSet(user, title, heading, editSetPage, editSetEntryRowOrder))
    }
}

object EditSetController {

  object FieldNames {
    val ccr = "ccr"
    val coveringDates = "covering-dates"
    val endDateFieldError = "end-date-field-error"
    val scopeAndContent = "scope-and-content"
  }

  val fieldKey = "field"
  val orderDirectionKey = "direction"
  val offsetKey = "offset"

}
