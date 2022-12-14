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

import java.time

sealed abstract class CoveringDateNode

object CoveringDateNode {

  trait Terminal

  final case class Year(value: time.Year) extends CoveringDateNode with Terminal
  final case class YearMonth(value: time.YearMonth) extends CoveringDateNode with Terminal
  final case class YearMonthDay(value: time.LocalDate) extends CoveringDateNode with Terminal

  trait Approximable
  trait Derivable
  trait Toplevel

  final case class Single(value: CoveringDateNode with Terminal)
      extends CoveringDateNode with Approximable with Derivable with Toplevel

  final case class Range(from: Single, to: Single)
      extends CoveringDateNode with Approximable with Derivable with Toplevel

  final case class Approx(value: CoveringDateNode with Approximable)
      extends CoveringDateNode with Derivable with Toplevel

  final case class Derived(value: CoveringDateNode with Derivable) extends CoveringDateNode with Toplevel

  final case object Undated extends CoveringDateNode with Toplevel

  final case class Gap(values: List[CoveringDateNode with Approximable]) extends CoveringDateNode with Toplevel

  final case class Root(value: CoveringDateNode with Toplevel) extends CoveringDateNode

}
