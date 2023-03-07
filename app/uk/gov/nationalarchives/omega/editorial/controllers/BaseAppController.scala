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
import play.api.i18n.{ I18nSupport, Lang }
import play.api.mvc.{ MessagesAbstractController, MessagesControllerComponents, Result }
import uk.gov.nationalarchives.omega.editorial.controllers.authentication.Secured
import uk.gov.nationalarchives.omega.editorial.models.{ EditSet, EditSetRecord }
import uk.gov.nationalarchives.omega.editorial.services.{ EditSetRecordService, EditSetService }
import uk.gov.nationalarchives.omega.editorial.support.{ FormSupport, MessageSupport }

import scala.concurrent.{ ExecutionContext, Future }

abstract class BaseAppController(
  messagesControllerComponents: MessagesControllerComponents,
  editSetService: EditSetService,
  editSetRecordService: EditSetRecordService
)(implicit
  ec: ExecutionContext
) extends MessagesAbstractController(messagesControllerComponents) with I18nSupport with Secured with FormSupport
    with MessageSupport {

  import BaseAppController._

  implicit def resultToFutureResult(result: Result): Future[Result] = Future(result)

  def findEditSet(id: String): Future[Outcome[EditSet]] =
    editSetService
      .get(id)
      .unsafeToFuture()
      .map(_.toRight(EditSetNotFound(id)))

  def findEditSetRecord(editSetOci: String, recordOci: String): Future[Outcome[EditSetRecord]] =
    editSetRecordService
      .get(editSetOci, recordOci)
      .unsafeToFuture()
      .map(_.toRight(EditSetRecordNotFound(recordOci)))

  def resolvedMessage(key: String, args: String*): String = messagesApi(key, args: _*)(Lang("en"))

}

object BaseAppController {

  type Outcome[A] = Either[Error, A]

  abstract class Error

  case object MissingAction extends Error

  case class InvalidAction(action: String) extends Error

  case class EditSetNotFound(id: String) extends Error

  case class EditSetRecordNotFound(id: String) extends Error

}
