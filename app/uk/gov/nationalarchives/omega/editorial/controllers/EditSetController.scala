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

import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import uk.gov.nationalarchives.omega.editorial.controllers.authentication.Secured
import uk.gov.nationalarchives.omega.editorial.forms.EditSetReorderFormProvider
import uk.gov.nationalarchives.omega.editorial.models._
import uk.gov.nationalarchives.omega.editorial.services.{ EditSetPagination, EditSetService }
import uk.gov.nationalarchives.omega.editorial.support.{ FormSupport, MessageSupport }
import uk.gov.nationalarchives.omega.editorial.views.html.editSet

import javax.inject._

/** This controller creates an `Action` to handle HTTP requests to the application's home page.
  */
@Singleton
class EditSetController @Inject() (
  messagesControllerComponents: MessagesControllerComponents,
  editSetService: EditSetService,
  editSet: editSet
) extends MessagesAbstractController(messagesControllerComponents) with I18nSupport with Secured with FormSupport
    with MessageSupport {
  import EditSetController._

  private val reorderForm: Form[EditSetReorder] = EditSetReorderFormProvider()
  private val fallbackEditSetReorder = EditSetReorder(FieldNames.ccr, orderDirectionAscending)

  /** Create an Action for the edit set page.
    *
    * The configuration in the `routes` file means that this method will be called when the application receives a `GET`
    * request with a path of `/edit-set/{id}`.
    */
  def view(id: String): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    withUser { user =>
      val ordering = for {
        field     <- queryStringValue(request, fieldKey)
        direction <- queryStringValue(request, orderDirectionKey)
      } yield EditSetReorder(field, direction)

      generateEditSetView(id, user, ordering.getOrElse(fallbackEditSetReorder))
    }
  }

  def viewAfterReordering(id: String): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    withUser { user =>
      generateEditSetView(
        id = id,
        user = user,
        editSetReorder = formToEither(reorderForm.bindFromRequest())
          .getOrElse(fallbackEditSetReorder)
      )
    }
  }

  private def queryStringValue(request: Request[AnyContent], key: String): Option[String] =
    request.queryString.get(key).flatMap(_.headOption)

  private def generateEditSetView(id: String, user: User, editSetReorder: EditSetReorder)(implicit
    request: Request[AnyContent]
  ): Result = {
    val currentEditSet = editSetService.getEditSet(id)

    val editSetEntries = currentEditSet.entries.sorted(getSorter(editSetReorder))
    val pageNumber = queryStringValue(request, offsetKey).map(_.toInt).getOrElse(1)

    val editSetPage = new EditSetPagination(
      id = id,
      ordering = editSetReorder,
      nextText = resolveMessage("edit-set.pagination.next"),
      previousText = resolveMessage("edit-set.pagination.previous")
    ).makeEditSetPage(editSetEntries, pageNumber)
    val title = resolveMessage("edit-set.title", editSetPage.pageNumber, editSetPage.totalPages)
    val heading: String = resolveMessage(
      "edit-set.heading",
      currentEditSet.name,
      editSetPage.numberOfFirstEntry.toString,
      editSetPage.numberOfLastEntry.toString,
      editSetPage.totalNumberOfEntries.toString
    )
    Ok(editSet(user, title, heading, editSetPage, reorderForm.fill(editSetReorder)))
  }

  private def getSorter(editSetReorder: EditSetReorder): Ordering[EditSetEntry] = {
    val fieldOrdering: Ordering[EditSetEntry] = editSetReorder.field match {
      case FieldNames.ccr             => Ordering.by(_.ccr)
      case FieldNames.scopeAndContent => Ordering.by(_.scopeAndContent)
      case FieldNames.coveringDates   => Ordering.by(_.coveringDates)
      case _                          => Ordering.by(_.ccr)
    }
    if (editSetReorder.direction == orderDirectionDescending) fieldOrdering.reverse else fieldOrdering
  }

}

object EditSetController {
  case class EditSetReorder(field: String, direction: String)

  object FieldNames {
    val ccr = "ccr"
    val coveringDates = "covering-dates"
    val orderDirection = "direction"
    val endDateFieldError = "end-date-field-error"
    val orderField = "field"
    val scopeAndContent = "scope-and-content"
  }

  val fieldKey = "field"
  val orderDirectionKey = "direction"
  val orderDirectionAscending = "ascending"
  val orderDirectionDescending = "descending"
  val offsetKey = "offset"

}
