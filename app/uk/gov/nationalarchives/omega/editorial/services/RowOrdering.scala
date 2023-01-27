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

import uk.gov.nationalarchives.omega.editorial.models.EditSetEntry
import uk.gov.nationalarchives.omega.editorial.controllers.EditSetController.FieldNames

case class RowOrdering(field: String, direction: String) {
  import RowOrdering._

  lazy val ordering: Ordering[EditSetEntry] = {
    val ordering: Ordering[EditSetEntry] = field match {
      case FieldNames.ccr             => Ordering.by(_.ccr)
      case FieldNames.oci             => Ordering.by(_.oci)
      case FieldNames.scopeAndContent => Ordering.by(_.scopeAndContent)
      case FieldNames.coveringDates   => Ordering.by(_.coveringDates)
      case _                          => Ordering.by(_.ccr)
    }

    if (direction == orderDirectionDescending) ordering.reverse else ordering
  }

  def ariaSortValue(headerValue: String): String =
    if (field == headerValue) direction
    else "none"

}

object RowOrdering {

  lazy val defaultOrdering = RowOrdering(FieldNames.ccr, orderDirectionAscending)

  val fieldKey = "field"
  val directionKey = "direction"
  val orderDirectionDescending = "descending"
  val orderDirectionAscending = "ascending"

}
