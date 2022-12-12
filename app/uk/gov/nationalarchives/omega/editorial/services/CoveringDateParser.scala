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
import java.util.Locale
import scala.util.matching.Regex
import scala.util.parsing.combinator._
import scala.util.Try
import uk.gov.nationalarchives.omega.editorial
import uk.gov.nationalarchives.omega.editorial.models.{ CoveringDate, CoveringDates }
import uk.gov.nationalarchives.omega.editorial.services.{ CoveringDateNode => Node }

object CoveringDateParser extends JavaTokenParsers {

  override def skipWhitespace = true

  private lazy val yearFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("u")
  private lazy val yearMonthFormatter: DateTimeFormatter = DateTimeFormatter
    .ofPattern("u MMM")
    .withLocale(Locale.UK)
  private lazy val yearMonthDayFormatter: DateTimeFormatter = DateTimeFormatter
    .ofPattern("u MMM d")
    .withLocale(Locale.UK)

  private lazy val monthFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM")
  private lazy val validMonthNames: List[String] = Month.values().map(monthFormatter.format).toList

  def monthChars: Parser[String] = {
    val regexString = validMonthNames
      .map { name =>
        s"$name|${name.toLowerCase}"
      }
      .mkString("|")
    new Regex(regexString)
  }

  def integerDigit: Parser[String] = "-?[0-9]*".r

  def year: Parser[Node.Year] = integerDigit ^? (attemptParseYear, yearErrorFormatter)

  def yearMonth: Parser[Node.YearMonth] = integerDigit ~ monthChars ^? (attemptParseYearMonth, yearMonthErrorFormatter)

  def yearMonthDay: Parser[Node.YearMonthDay] =
    integerDigit ~ monthChars ~ wholeNumber ^? (attemptParseLocalDate, yearMonthDayErrorFormatter)

  def single: Parser[Node.Single] = (yearMonthDay | yearMonth | year) ^^ Node.Single.apply

  def range: Parser[Node.Range] = (single <~ "-") ~ single ^^ { case (l ~ r) => Node.Range(l, r) }

  def approx: Parser[Node.Approx] = "c|\\?".r ~> (range | single) ^^ Node.Approx.apply

  def derived: Parser[Node.Derived] = ("[" ~> (approx | range | single) <~ "]") ^^ Node.Derived.apply

  def undated: Parser[Node.Undated.type] = "undated" ^^^ Node.Undated

  def gap: Parser[Node.Gap] = rep1sep(range | single, ";") ^^ Node.Gap.apply

  def coveringDates: Parser[Node.Root] = (gap | derived | approx | range | single | undated) ^^ Node.Root.apply

  def attemptParseYear: PartialFunction[String, Node.Year] =
    Function.unlift { yearRaw =>
      Try {
        Node.Year(Year.parse(yearRaw, yearFormatter))
      }.toOption
    }

  def attemptParseYearMonth: PartialFunction[String ~ String, Node.YearMonth] =
    Function.unlift { case (yearRaw ~ monthRaw) =>
      Try {
        val normalizedMonth = monthRaw.toLowerCase.capitalize
        Node.YearMonth(YearMonth.parse(s"$yearRaw $normalizedMonth", yearMonthFormatter))
      }.toOption
    }

  def attemptParseLocalDate: PartialFunction[String ~ String ~ String, Node.YearMonthDay] =
    Function.unlift { case (yearRaw ~ monthRaw ~ dayRaw) =>
      Try {
        val normalizedMonth = monthRaw.toLowerCase.capitalize
        Node.YearMonthDay(LocalDate.parse(s"$yearRaw $normalizedMonth $dayRaw", yearMonthDayFormatter))
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

  sealed case class ParseError(val msg: String)

  def runParser[A](parser: Parser[A], input: String): Either[ParseError, A] =
    parse(parser, input) match {
      case Success(result, _) => Right(result)
      case Failure(msg, _)    => Left(new ParseError(msg))
      case Error(msg, _)      => Left(new ParseError(msg))
    }

  def parseCoveringDates(input: String): Either[ParseError, CoveringDateNode.Root] =
    runParser(coveringDates, input)

}
