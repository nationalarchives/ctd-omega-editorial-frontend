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
import scala.util.parsing.combinator._
import scala.util.Try
import uk.gov.nationalarchives.omega.editorial.services.CoveringDateError._
import uk.gov.nationalarchives.omega.editorial.services.{ CoveringDateNode => Node }

object CoveringDateParser extends JavaTokenParsers {

  def parseCoveringDates(input: String): Result[Node.Root] =
    runParser(coveringDates, input)

  def runParser[A](parser: Parser[A], input: String): Result[A] =
    parseAll(parser, input) match {
      case Success(result, _) => Right(result)
      case Failure(_, _)      => Left(ParseError)
      case Error(_, _)        => Left(ParseError)
    }

  def coveringDates: Parser[Node.Root] =
    (gap | derived | approx | range | single | undated) ^^ Node.Root.apply

  def gap: Parser[Node.Gap] =
    ((range | single) <~ ";") ~ rep1sep(range | single, ";") ^^ { case (head ~ rest) =>
      Node.Gap(head :: rest)
    }

  def derived: Parser[Node.Derived] =
    ("[" ~> (approx | range | single) <~ "]") ^^ Node.Derived.apply

  def approx: Parser[Node.Approx] =
    "c|\\?".r ~> (range | single) ^^ Node.Approx.apply

  def range: Parser[Node.Range] =
    (single <~ "-") ~ single ^^ { case (l ~ r) => Node.Range(l, r) }

  def single: Parser[Node.Single] =
    (yearMonthDay | yearMonth | year) ^^ Node.Single.apply

  def yearMonthDay: Parser[Node.YearMonthDay] =
    integerDigit ~ month ~ wholeNumber ^? (attemptParseLocalDate, yearMonthDayErrorFormatter)

  def yearMonth: Parser[Node.YearMonth] =
    integerDigit ~ month ^? (attemptParseYearMonth, yearMonthErrorFormatter)

  def year: Parser[Node.Year] =
    integerDigit ^? (attemptParseYear, yearErrorFormatter)

  def month: Parser[Month] =
    ("Jan" ^^^ Month.JANUARY) | ("Feb" ^^^ Month.FEBRUARY) | ("Mar" ^^^ Month.MARCH) |
      ("Apr" ^^^ Month.APRIL) | ("May" ^^^ Month.MAY) | ("June" ^^^ Month.JUNE) |
      ("July" ^^^ Month.JULY) | ("Aug" ^^^ Month.AUGUST) | ("Sept" ^^^ Month.SEPTEMBER) |
      ("Oct" ^^^ Month.OCTOBER) | ("Nov" ^^^ Month.NOVEMBER) | ("Dec" ^^^ Month.DECEMBER)

  def integerDigit: Parser[String] =
    "-?[0-9]*".r

  def undated: Parser[Node.Undated.type] =
    "undated" ^^^ Node.Undated

  private def attemptParseYear: PartialFunction[String, Node.Year] =
    Function.unlift { yearRaw =>
      Try {
        Node.Year(Year.of(yearRaw.toInt))
      }.toOption
    }

  private def attemptParseYearMonth: PartialFunction[String ~ Month, Node.YearMonth] =
    Function.unlift { case (yearRaw ~ month) =>
      Try {
        Node.YearMonth(Year.of(yearRaw.toInt).atMonth(month))
      }.toOption
    }

  private def attemptParseLocalDate: PartialFunction[String ~ Month ~ String, Node.YearMonthDay] =
    Function.unlift { case (yearRaw ~ month ~ dayRaw) =>
      Try {
        Node.YearMonthDay(Year.of(yearRaw.toInt).atMonth(month).atDay(dayRaw.toInt))
      }.toOption
    }

  private def yearErrorFormatter: String => String = badYear =>
    s"Expected a year represented by a positve or negative integer but got $badYear"

  private def yearMonthErrorFormatter: String ~ Month => String = { case (badYear ~ badMonth) =>
    s"""Expected a year followed by a month but got "$badYear $badMonth""""
  }

  private def yearMonthDayErrorFormatter: String ~ Month ~ String => String = { case (badYear ~ badMonth ~ badDay) =>
    s"""Expected a year followed by a month and a valid day of month; but got "$badYear $badMonth $badDay""""
  }

}
