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

import java.time._
import uk.gov.nationalarchives.omega.editorial.models.DateRange
import uk.gov.nationalarchives.omega.editorial.services.CoveringDateParser.ParseError
import uk.gov.nationalarchives.omega.editorial.services.{ CoveringDateNode => Node }

object CoveringDateCalculator {

  def getStartAndEndDates(coveringDateRaw: String): Either[ParseError, List[DateRange]] =
    CoveringDateParser
      .parseCoveringDates(sanitize(coveringDateRaw))
      .map(calculateDateRanges)

  def sanitize(input: String): String =
    input
      .replace("\u2013", "-") // En Dash
      .replace("\u201C", "\"")
      .replace("\u201D", "\"")

  def calculateDateRanges(node: CoveringDateNode): List[DateRange] =
    node match {
      case Node.Year(value)         => List(DateRange(startOfYear(value), endOfYear(value)))
      case Node.YearMonth(value)    => List(DateRange(startOfYear(value), endOfYear(value)))
      case Node.YearMonthDay(value) => List(DateRange(value, value))

      case Node.Single(value)   => calculateDateRanges(value)
      case Node.Range(from, to) =>
        // TODO assert from is before to
        calculateDateRanges(from)
          .zip(calculateDateRanges(to))
          .map { case (from, to) =>
            DateRange(from.start, to.end)
          }

      case Node.Approx(value) => calculateDateRanges(value)

      case Node.Derived(value) => calculateDateRanges(value)

      case Node.Gap(values) => values.flatMap(calculateDateRanges)

      case Node.Root(value) => calculateDateRanges(value)

      case Node.Undated => ???
    }

  private def startOfYear(year: Year): LocalDate =
    year.atMonthDay(MonthDay.of(Month.JANUARY, 1))

  private def endOfYear(year: Year): LocalDate =
    year.atMonthDay(MonthDay.of(Month.DECEMBER, 31))

  private def startOfYear(yearMonth: YearMonth): LocalDate =
    yearMonth.atDay(1)

  private def endOfYear(yearMonth: YearMonth): LocalDate =
    yearMonth.atEndOfMonth()

}
