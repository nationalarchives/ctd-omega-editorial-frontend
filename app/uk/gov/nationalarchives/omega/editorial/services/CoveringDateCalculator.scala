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

import uk.gov.nationalarchives.omega.editorial.FrontendError.{ DateTooFarInFuture, DateTooFarInPast, InvalidRange, MultipleErrors, Result }
import java.time._
import uk.gov.nationalarchives.omega.editorial.models.DateRange
import uk.gov.nationalarchives.omega.editorial.services.{ CoveringDateNode => Node }

object CoveringDateCalculator {

  val unicodeEnDash = "\u2013"

  def getStartAndEndDates(coveringDateRaw: String): Result[List[DateRange]] =
    CoveringDateParser
      .parseCoveringDates(sanitize(coveringDateRaw))
      .map(calculateDateRanges)
      .flatMap(validateDateRanges)

  private def sanitize(input: String): String =
    input
      .replace(unicodeEnDash, "-")

  private def calculateDateRanges(node: CoveringDateNode): List[DateRange] =
    node match {
      case Node.Year(value)         => List(DateRange(startOfYear(value), endOfYear(value)))
      case Node.YearMonth(value)    => List(DateRange(startOfYearMonth(value), endOfYearMonth(value)))
      case Node.YearMonthDay(value) => List(DateRange(value, value))

      case Node.Single(value) => calculateDateRanges(value)
      case Node.Range(from, to) =>
        calculateDateRanges(from)
          .zip(calculateDateRanges(to))
          .map { case (from, to) =>
            DateRange(from.start, to.end)
          }

      case Node.Approx(value) => calculateDateRanges(value)

      case Node.Derived(value) => calculateDateRanges(value)

      case Node.Gap(values) => values.flatMap(calculateDateRanges)

      case Node.Root(value) => calculateDateRanges(value)

      case Node.Undated => List.empty
    }

  private def startOfYear(year: Year): LocalDate =
    year.atMonthDay(MonthDay.of(Month.JANUARY, 1))

  private def endOfYear(year: Year): LocalDate =
    year.atMonthDay(MonthDay.of(Month.DECEMBER, 31))

  private def startOfYearMonth(yearMonth: YearMonth): LocalDate =
    yearMonth.atDay(1)

  private def endOfYearMonth(yearMonth: YearMonth): LocalDate =
    yearMonth.atEndOfMonth()

  private def validateDateRanges(ranges: List[DateRange]): Result[List[DateRange]] =
    ranges.partitionMap { range =>
      checkDatesNotInFutureOrPast(range).flatMap(checkStartDateBeforeEndDate)
    } match {
      case (Nil, results) => Right(results)
      case (List(err), _) => Left(err)
      case (errs, _)      => Left(MultipleErrors(errs))
    }

  private def checkStartDateBeforeEndDate(range: DateRange): Result[DateRange] =
    if (range.start.isBefore(range.end) || range.start == range.end)
      Right(range)
    else
      Left(InvalidRange(range))

  private def checkDatesNotInFutureOrPast(range: DateRange): Result[DateRange] =
    for {
      validStart <- checkDate(range.start)
      validEnd   <- checkDate(range.end)
    } yield DateRange(validStart, validEnd)

  private def checkDate(date: LocalDate): Result[LocalDate] =
    date match {
      case date if date.getYear <= 0                      => Left(DateTooFarInPast(date))
      case date if date.getYear > LocalDate.now().getYear => Left(DateTooFarInFuture(date))
      case _                                              => Right(date)
    }

}
