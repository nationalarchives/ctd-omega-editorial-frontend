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

import cats.effect.IO
import play.api.i18n.{ I18nSupport, Lang }
import play.api.mvc.{ MessagesAbstractController, MessagesControllerComponents, Result }
import uk.gov.nationalarchives.omega.editorial.FrontendError.{ EditSetNotFound, EditSetRecordNotFound, Outcome }
import uk.gov.nationalarchives.omega.editorial.controllers.authentication.Secured
import uk.gov.nationalarchives.omega.editorial.models.{ EditSet, EditSetRecord }
import uk.gov.nationalarchives.omega.editorial.services.{ EditSetRecordService, EditSetService }
import uk.gov.nationalarchives.omega.editorial.support.{ FormSupport, MessageSupport }

abstract class BaseAppController(
  messagesControllerComponents: MessagesControllerComponents,
  editSetService: EditSetService,
  editSetRecordService: EditSetRecordService
) extends MessagesAbstractController(messagesControllerComponents) with I18nSupport with Secured with FormSupport
    with MessageSupport {

  implicit def resultToIOResult(result: Result): IO[Result] = IO.pure(result)

  implicit def outcomeToIOOutcome[T](outcome: Outcome[T]): IO[Outcome[T]] = IO.pure(outcome)

  def findEditSet(id: String): IO[Outcome[EditSet]] =
    editSetService
      .get(id)
      .map(_.toRight(EditSetNotFound(id)))

  def findEditSetRecord(editSetOci: String, recordOci: String): IO[Outcome[EditSetRecord]] =
    editSetRecordService
      .get(editSetOci, recordOci)
      .map(_.toRight(EditSetRecordNotFound(recordOci)))

  def resolvedMessage(key: String, args: String*): String = messagesApi(key, args: _*)(Lang("en"))

}
