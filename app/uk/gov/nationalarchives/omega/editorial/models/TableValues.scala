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

package uk.gov.nationalarchives.omega.editorial.models

sealed abstract class CellOrdering
object CellOrdering {
  case class Ascending(value: String) extends CellOrdering
  case class Descending(value: String) extends CellOrdering
  case object None extends CellOrdering
}

case class TableValues(
  headers: List[String],
  rows: List[List[String]],
  orderedBy: CellOrdering = CellOrdering.None
) {

  def orderingNameForHeader(name: String): String =
    orderedBy match {
      case CellOrdering.Ascending(value) if name == value  => "ascending"
      case CellOrdering.Descending(value) if name == value => "descending"
      case _                                               => "none"
    }

  def changeOrdering(name: String): Option[TableValues] =
    headers.indexOf(name) match {
      case -1 => None
      case index =>
        Some(
          orderedBy match {
            case CellOrdering.Ascending(orderHeader) if orderHeader == name =>
              this.copy(
                rows = rows.sortBy(xs => xs(index)).reverse,
                orderedBy = CellOrdering.Descending(orderHeader)
              )
            case CellOrdering.Descending(orderHeader) if orderHeader == name =>
              this.copy(
                rows = rows.sortBy(xs => xs(index)),
                orderedBy = CellOrdering.Ascending(orderHeader)
              )
            case _ =>
              this.copy(
                rows = rows.sortBy(xs => xs(index)),
                orderedBy = CellOrdering.Ascending(name)
              )
          }
        )
    }

}
