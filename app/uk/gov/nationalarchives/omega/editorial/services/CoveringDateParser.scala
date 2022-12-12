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
import java.time.format.DateTimeFormatter
import scala.util.parsing.combinator._
import scala.util.Try
import uk.gov.nationalarchives.omega.editorial
import uk.gov.nationalarchives.omega.editorial.models.{ CoveringDate, CoveringDates }

object CoveringDateParser extends JavaTokenParsers {

  private lazy val yearFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy")
  private lazy val yearMonthFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy MMM")
  private lazy val yearMonthDayFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy MMM d")

  private lazy val monthFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM")
  private lazy val validMonthNames: List[String] = Month.values().map(monthFormatter.format).toList

  def threeAlphaChar: Parser[String] = """[a-z][A-Z]{3}""".r
  def integerDigit: Parser[String] = """-?[0-9]\d*""".r

  def year: Parser[Year] = integerDigit ^? (attemptParseYear, yearErrorFormatter)

  def yearMonth: Parser[YearMonth] = integerDigit ~ threeAlphaChar ^? (attemptParseYearMonth, yearMonthErrorFormatter)

  def yearMonthDay: Parser[LocalDate] =
    threeAlphaChar ~ integerDigit ~ wholeNumber ^? (attemptParseLocalDate, yearMonthDayErrorFormatter)

  def attemptParseYear: PartialFunction[String, Year] =
    Function.unlift { yearRaw =>
      Try {
        Year.parse(yearRaw, yearFormatter)
      }.toOption
    }

  def attemptParseYearMonth: PartialFunction[String ~ String, YearMonth] =
    Function.unlift { case (yearRaw ~ monthRaw) =>
      Try {
        val normalizedMonth = monthRaw.toLowerCase.capitalize
        YearMonth.parse(s"$yearRaw $monthRaw", yearMonthFormatter)
      }.toOption
    }

  def attemptParseLocalDate: PartialFunction[String ~ String ~ String, LocalDate] =
    Function.unlift { case (yearRaw ~ monthRaw ~ dayRaw) =>
      Try {
        val normalizedMonth = monthRaw.toLowerCase.capitalize
        LocalDate.parse(s"$yearRaw $normalizedMonth $dayRaw", yearMonthDayFormatter)
      }.toOption
    }

  def yearErrorFormatter: String => String = badYear =>
    s"Expected a year represented by a positve or negative integer but got $badYear"

  def yearMonthErrorFormatter: String ~ String => String = { case (badYear ~ badMonth) =>
    s"""Expected a year followed by a month like: ${validMonthNames.mkString(", ")}; but got "$badYear $badMonth""""
  }

  def yearMonthDayErrorFormatter: String ~ String ~ String => String = { case (badYear ~ badMonth ~ badDay) =>
    s"""Expected a year followed by a month like: ${validMonthNames.mkString(
        ", "
      )} and a valid day of month; but got "$badYear $badMonth $badDay""""
  }

  sealed abstract class ParseError

  def parse2(input: String): Either[ParseError, editorial.models.CoveringDates] = {
    val dm: ParseResult[LocalDate] = parse(yearMonthDay, "1993 May 1")
    // val dm = parse(monthDay, "May 1").get
    pprint.pprintln(dm)
    pprint.pprintln(parse(year, "2993"))
    ???
  }

}
