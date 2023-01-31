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

sealed abstract class Direction(val name: String)

object Direction {

  case object Ascending extends Direction("ascending")
  case object Descending extends Direction("descending")

  lazy val all = Set(Ascending, Descending)

}

sealed abstract class EditSetEntryRowOrder(
  val field: String,
  val direction: Direction,
  fieldOrdering: Ordering[EditSetEntry]
) {

  lazy val currentOrdering: Ordering[EditSetEntry] =
    if (direction == Direction.Descending) fieldOrdering.reverse
    else fieldOrdering

  def ariaSortValue(headerName: String): String =
    if (field == headerName) direction.name
    else "none"

  def orderingKey(headerName: String): String =
    if (field == headerName && direction == Direction.Ascending) Direction.Descending.name
    else Direction.Ascending.name

}

object EditSetEntryRowOrder {

  case class CCROrder(override val direction: Direction)
      extends EditSetEntryRowOrder("ccr", direction, Ordering.by(_.ccr))
  case class ScopeAndContentOrder(override val direction: Direction)
      extends EditSetEntryRowOrder("scope-and-content", direction, Ordering.by(_.scopeAndContent))
  case class CoveringDatesOrder(override val direction: Direction)
      extends EditSetEntryRowOrder("covering-dates", direction, Ordering.by(_.coveringDates))

  lazy val defaultOrder = CCROrder(Direction.Ascending)

  lazy val all = Direction.all.flatMap { direction =>
    Set(
      CCROrder(direction),
      ScopeAndContentOrder(direction),
      CoveringDatesOrder(direction)
    )
  }

  def fromNames(field: String, direction: String): EditSetEntryRowOrder =
    all
      .find { order =>
        order.field == field && order.direction.name == direction
      }
      .getOrElse {
        defaultOrder
      }

}
