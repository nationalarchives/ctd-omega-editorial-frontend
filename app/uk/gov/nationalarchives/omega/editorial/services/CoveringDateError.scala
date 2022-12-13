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

package uk.gov.nationalarchives.omega.editorial.services

import uk.gov.nationalarchives.omega.editorial.services.{ CoveringDateNode => Node }
import uk.gov.nationalarchives.omega.editorial.models.DateRange

sealed abstract class CoveringDateError(val message: String)

object CoveringDateError {

  type Result[A] = Either[CoveringDateError, A]

  final case class ParseError(msg: String) extends CoveringDateError(msg)
  final case class InvalidRange(range: DateRange) extends CoveringDateError(
        s"Invalid date range; ${range.start} is not before or equal to ${range.end}"
      )
  final case class MultipleErrors(errs: List[CoveringDateError]) extends CoveringDateError(
        s"Multiple errors:\n ${errs.map(_.message).mkString("\n")}"
      )

}
