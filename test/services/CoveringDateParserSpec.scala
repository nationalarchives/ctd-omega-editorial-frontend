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

package services

import java.time.format.DateTimeFormatter
import java.time._
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.prop.Tables
import support.BaseSpec
import uk.gov.nationalarchives.omega.editorial.models.{ CoveringDate, CoveringDates }
import uk.gov.nationalarchives.omega.editorial.services.CoveringDateParser
import support.CustomMatchers.parseSuccessfullyAs
import org.scalatest.EitherValues

class CoveringDateParserSpec extends BaseSpec with TableDrivenPropertyChecks with EitherValues {

  lazy val testTable = Tables.Table(
    "date input" -> "parse result",
    "1305"       -> defineTestCoveringDate("1305 Jan 1" -> "1305 Dec 31")
//     "1305 Sept"                -> defineTestCoveringDate("1305 Sept 1" -> "1305 Sept 30"),
//     "1305 Sept 1"              -> defineTestCoveringDate("1305 Sept 1" -> "1305 Sept 1"),
//     "1305-1306"                -> defineTestCoveringDate("1305 Jan 1" -> "1306 Dec 31"),
//     "1305 Sept-1305 Oct"       -> defineTestCoveringDate("1305 Sept 1" -> "1306 Oct 31"),
//     "1305 Sept 1–1305 Sept 15" -> defineTestCoveringDate("1305 Sept 1" -> "1306 Sept 15"),
//     "[1914 - 1916]"            -> defineTestCoveringDate("1914 Jan 1" -> "1916 Dec 31"),
//     "[c 1915]"                 -> defineTestCoveringDate("1915 Jan 1" -> "1915 Dec 31"),
//     "[c1915]"                  -> defineTestCoveringDate("1915 Jan 1" -> "1915 Dec 31"),
//     "[?1915]"                  -> defineTestCoveringDate("1915 Jan 1" -> "1915 Dec 31"),
//     "1868; 1890-1902; 1933" -> defineTestCoveringDate(
//       "1868 Jan 1" -> "1868 Dec 31",
//       "1890 Jan 1" -> "1902 Dec 31",
//       "1933 Jan 1" -> "1933 Dec 31"
//     )
  )

  "CoveringDateParser" should {

    forAll(testTable) { (input, expectedResult) =>
      s"parse the covering date: $input" in {
        CoveringDateParser.parseCoveringDates(input) mustEqual expectedResult
      }

    }

  }

  "YearParser" should {

    lazy val testTable = Tables.Table(
      "year input" -> "parse result",
      "1993"       -> Year.of(1993),
      "423"        -> Year.of(423),
      "-123"       -> Year.of(-123),
      "2"          -> Year.of(2)
    )

    forAll(testTable) { (input, expectedResult) =>
      s"parse $input as a Year" in {
        val parseResult = CoveringDateParser.runParser(CoveringDateParser.year, input)
        parseResult must parseSuccessfullyAs(expectedResult)
      }
    }
  }

  "YearMonthParser" should {

    lazy val testTable = Tables.Table(
      "yearMonth input" -> "parse result",
      "1993 December"   -> YearMonth.of(1993, Month.DECEMBER),
      "1993 Jan"        -> YearMonth.of(1993, Month.JANUARY),
      "1993 jan"        -> YearMonth.of(1993, Month.JANUARY),
      "1993 Sept"       -> YearMonth.of(1993, Month.SEPTEMBER),
      "1993 Sep"        -> YearMonth.of(1993, Month.SEPTEMBER),
      "1993 July"       -> YearMonth.of(1993, Month.JULY),
      "1993 Jul"        -> YearMonth.of(1993, Month.JULY),
      "1 jan"           -> YearMonth.of(1, Month.JANUARY)
    )

    forAll(testTable) { (input, expectedResult) =>
      s"parse $input as a YearMonth" in {
        val parseResult = CoveringDateParser.runParser(CoveringDateParser.yearMonth, input)
        parseResult must parseSuccessfullyAs(expectedResult)
      }
    }
  }

  "YearMonthDayParser" should {

    lazy val testTable = Tables.Table(
      "yearMonthDay input" -> "parse result",
      "1993 Jan 1"         -> LocalDate.of(1993, 1, 1),
      "1993 Jan 01"        -> LocalDate.of(1993, 1, 1)
    )

    forAll(testTable) { (input, expectedResult) =>
      s"parse $input as a LocalDate" in {
        val parseResult = CoveringDateParser.runParser(CoveringDateParser.yearMonthDay, input)
        parseResult must parseSuccessfullyAs(expectedResult)
      }
    }
  }

  lazy val basicCoveringDateFormatter = DateTimeFormatter.ofPattern("u LLL d")

  def defineTestCoveringDate(cds: (String, String)*) = {
    val coveringDates: Seq[CoveringDate] = cds.map { case (start, end) =>
      CoveringDate(LocalDate.parse(start, basicCoveringDateFormatter), LocalDate.parse(end, basicCoveringDateFormatter))
    }
    Right(
      CoveringDates(
        coveringDates.toList
      )
    )
  }

}
