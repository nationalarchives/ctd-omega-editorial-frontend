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

sealed abstract class RowOrdering(val field: String, val direction: String) {

  def toOrdering[A](implicit orderFinder: KeyToOrdering[A]): Option[Ordering[A]] =
    this match {
      case RowOrdering.Ascending(value)  => orderFinder.orderingFor(value)
      case RowOrdering.Descending(value) => orderFinder.orderingFor(value).map(_.reverse)
      case RowOrdering.NoOrder           => None
    }

  def headerOrderingName(header: String): String =
    this match {
      case RowOrdering.Ascending(`header`)  => "ascending"
      case RowOrdering.Descending(`header`) => "descending"
      case _                                => "none"
    }

  def sort[A](items: Seq[A])(implicit orderFinder: KeyToOrdering[A]): Seq[A] =
    toOrdering[A] match {
      case Some(ordering) => items.sorted(ordering)
      case None           => items
    }

}

object RowOrdering {

  case class Ascending(value: String) extends RowOrdering(value, "ascending")
  case class Descending(value: String) extends RowOrdering(value, "descending")
  case object NoOrder extends RowOrdering("none", "none")

  def fromNames(field: String, direction: String): RowOrdering =
    direction.trim.toLowerCase match {
      case "ascending"  => Ascending(field)
      case "descending" => Descending(field)
      case _            => NoOrder
    }

}

trait KeyToOrdering[A] {
  def orderingFor(key: String): Option[Ordering[A]]
}

object KeyToOrdering {
  def byFunction[A](f: PartialFunction[String, Ordering[A]]): KeyToOrdering[A] =
    (key: String) => f.lift(key)

}
