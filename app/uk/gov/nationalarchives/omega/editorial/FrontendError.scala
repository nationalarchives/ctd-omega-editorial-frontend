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

package uk.gov.nationalarchives.omega.editorial

import uk.gov.nationalarchives.omega.editorial.models.DateRange

import java.time.LocalDate

abstract class FrontendError extends Throwable

sealed abstract class CoveringDateError
sealed abstract class StubServerError extends FrontendError

case object MissingMessageType extends StubServerError
case class NotATextMessage(err: Throwable) extends StubServerError
case class CannotParse(txt: String) extends StubServerError
case class CannotParseReply(reply: String) extends Exception(
      s"""can't parse reply, got:
         |$reply
         |""".stripMargin
    )

object FrontendError {
  type Outcome[A] = Either[Error, A]

  type Result[A] = Either[CoveringDateError, A]

  abstract class Error extends FrontendError

  case object MissingAction extends Error
  case class InvalidAction(action: String) extends Error
  case class EditSetNotFound(id: String) extends Error
  case class EditSetRecordNotFound(id: String) extends Error

  final case object ParseError extends CoveringDateError
  final case class DateTooFarInFuture(date: LocalDate) extends CoveringDateError
  final case class DateTooFarInPast(date: LocalDate) extends CoveringDateError
  final case class InvalidRange(range: DateRange) extends CoveringDateError
  final case class MultipleErrors(errs: List[CoveringDateError]) extends CoveringDateError

}
