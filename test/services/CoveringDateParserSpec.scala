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

import java.time._
import org.scalatest.prop.{ TableDrivenPropertyChecks, Tables }
import support.BaseSpec
import support.CustomMatchers.parseSuccessfullyAs
import uk.gov.nationalarchives.omega.editorial.models.DateRange
import uk.gov.nationalarchives.omega.editorial.services.{ CoveringDateNode => Node, CoveringDateParser }

class CoveringDateParserSpec extends BaseSpec with TableDrivenPropertyChecks {

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
        parseResult must parseSuccessfullyAs(Node.Year(expectedResult))
      }
    }
  }

  "YearMonthParser" should {

    lazy val testTable = Tables.Table(
      "yearMonth input" -> "parse result",
      "1993 Dec"        -> YearMonth.of(1993, Month.DECEMBER),
      "1993 Jan"        -> YearMonth.of(1993, Month.JANUARY),
      "1993 Sept"       -> YearMonth.of(1993, Month.SEPTEMBER),
      "1993 July"       -> YearMonth.of(1993, Month.JULY),
      "1 Jan"           -> YearMonth.of(1, Month.JANUARY)
    )

    forAll(testTable) { (input, expectedResult) =>
      s"parse $input as a YearMonth" in {
        val parseResult = CoveringDateParser.runParser(CoveringDateParser.yearMonth, input)
        parseResult must parseSuccessfullyAs(Node.YearMonth(expectedResult))
      }
    }
  }

  "YearMonthDayParser" should {

    lazy val testTable = Tables.Table(
      "yearMonthDay input" -> "parse result",
      "1993 Jan 1"         -> LocalDate.of(1993, 1, 1),
      "1305 Sept 1"        -> LocalDate.of(1305, 9, 1),
      "1993 Jan 01"        -> LocalDate.of(1993, 1, 1)
    )

    forAll(testTable) { (input, expectedResult) =>
      s"parse $input as a LocalDate" in {
        val parseResult = CoveringDateParser.runParser(CoveringDateParser.yearMonthDay, input)
        parseResult must parseSuccessfullyAs(Node.YearMonthDay(expectedResult))
      }
    }
  }

  "SingleParser" should {

    lazy val testTable = Tables.Table(
      "single input" -> "parse result",
      "1 Jan 1"      -> Node.Single(Node.YearMonthDay(LocalDate.of(1, 1, 1))),
      "1993 Jan 1"   -> Node.Single(Node.YearMonthDay(LocalDate.of(1993, 1, 1))),
      "1993 Dec"     -> Node.Single(Node.YearMonth(YearMonth.of(1993, Month.DECEMBER))),
      "1993"         -> Node.Single(Node.Year(Year.of(1993)))
    )

    forAll(testTable) { (input, expectedResult) =>
      s"parse $input as a Single" in {
        val parseResult = CoveringDateParser.runParser(CoveringDateParser.single, input)
        parseResult must parseSuccessfullyAs(expectedResult)
      }
    }
  }

  "RangeParser" should {

    lazy val testTable = Tables.Table(
      "Range input" -> "parse result",
      "1993 Jan 1 - 1993 Dec 31" -> Node.Range(
        Node.Single(Node.YearMonthDay(LocalDate.of(1993, 1, 1))),
        Node.Single(Node.YearMonthDay(LocalDate.of(1993, 12, 31)))
      ),
      "1993 Jan 1-1993 Dec 31" -> Node.Range(
        Node.Single(Node.YearMonthDay(LocalDate.of(1993, 1, 1))),
        Node.Single(Node.YearMonthDay(LocalDate.of(1993, 12, 31)))
      ),
      "1993 Jan - 1993 Dec" -> Node.Range(
        Node.Single(Node.YearMonth(YearMonth.of(1993, 1))),
        Node.Single(Node.YearMonth(YearMonth.of(1993, 12)))
      )
    )

    forAll(testTable) { (input, expectedResult) =>
      s"parse $input as a Range" in {
        val parseResult = CoveringDateParser.runParser(CoveringDateParser.range, input)
        parseResult must parseSuccessfullyAs(expectedResult)
      }
    }
  }

  "ApproxParser" should {

    lazy val testTable = Tables.Table(
      "Approx input" -> "parse result",
      "c 1 Jan 1"    -> Node.Approx(Node.Single(Node.YearMonthDay(LocalDate.of(1, 1, 1)))),
      "c 1993 Jan 1" -> Node.Approx(Node.Single(Node.YearMonthDay(LocalDate.of(1993, 1, 1)))),
      "c 1993 Dec"   -> Node.Approx(Node.Single(Node.YearMonth(YearMonth.of(1993, Month.DECEMBER)))),
      "c 1993"       -> Node.Approx(Node.Single(Node.Year(Year.of(1993)))),
      "c1 Jan 1"     -> Node.Approx(Node.Single(Node.YearMonthDay(LocalDate.of(1, 1, 1)))),
      "c1993 Jan 1"  -> Node.Approx(Node.Single(Node.YearMonthDay(LocalDate.of(1993, 1, 1)))),
      "c1993 Dec"    -> Node.Approx(Node.Single(Node.YearMonth(YearMonth.of(1993, Month.DECEMBER)))),
      "c1993"        -> Node.Approx(Node.Single(Node.Year(Year.of(1993)))),
      "? 1 Jan 1"    -> Node.Approx(Node.Single(Node.YearMonthDay(LocalDate.of(1, 1, 1)))),
      "? 1993 Jan 1" -> Node.Approx(Node.Single(Node.YearMonthDay(LocalDate.of(1993, 1, 1)))),
      "? 1993 Dec"   -> Node.Approx(Node.Single(Node.YearMonth(YearMonth.of(1993, Month.DECEMBER)))),
      "? 1993"       -> Node.Approx(Node.Single(Node.Year(Year.of(1993))))
    )

    forAll(testTable) { (input, expectedResult) =>
      s"parse $input as Approx" in {
        val parseResult = CoveringDateParser.runParser(CoveringDateParser.approx, input)
        parseResult must parseSuccessfullyAs(expectedResult)
      }
    }
  }

  "DerivedParser" should {

    lazy val testTable = Tables.Table(
      "Derived input" -> "parse result",
      "[c 1 Jan 1]"   -> Node.Derived(Node.Approx(Node.Single(Node.YearMonthDay(LocalDate.of(1, 1, 1))))),
      "[1993 Jan 1 - 2004 Dec 25]" -> Node.Derived(
        Node.Range(
          Node.Single(Node.YearMonthDay(LocalDate.of(1993, 1, 1))),
          Node.Single(Node.YearMonthDay(LocalDate.of(2004, 12, 25)))
        )
      )
    )

    forAll(testTable) { (input, expectedResult) =>
      s"parse $input as Derived" in {
        val parseResult = CoveringDateParser.runParser(CoveringDateParser.derived, input)
        parseResult must parseSuccessfullyAs(expectedResult)
      }
    }
  }

  "GapParser" should {

    lazy val testTable = Tables.Table(
      "Gap input" -> "parse result",
      "1868; 1890-1902; 1933" -> Node.Gap(
        List(
          Node.Single(Node.Year(Year.of(1868))),
          Node.Range(Node.Single(Node.Year(Year.of(1890))), Node.Single(Node.Year(Year.of(1902)))),
          Node.Single(Node.Year(Year.of(1933)))
        )
      )
    )

    forAll(testTable) { (input, expectedResult) =>
      s"parse $input as Gap" in {
        val parseResult = CoveringDateParser.runParser(CoveringDateParser.gap, input)
        parseResult must parseSuccessfullyAs(expectedResult)
      }
    }
  }

  "CoveringDateParser" should {

    lazy val testTable = Tables.Table(
      "Covering date input" -> "parse result",
      "1305 Apr 1-1306 Apr 15" -> Node.Root(
        Node.Range(
          Node.Single(Node.YearMonthDay(LocalDate.of(1305, 4, 1))),
          Node.Single(Node.YearMonthDay(LocalDate.of(1306, 4, 15)))
        )
      ),
      "1868; 1890-1902; 1933" -> Node.Root(
        Node.Gap(
          List(
            Node.Single(Node.Year(Year.of(1868))),
            Node.Range(Node.Single(Node.Year(Year.of(1890))), Node.Single(Node.Year(Year.of(1902)))),
            Node.Single(Node.Year(Year.of(1933)))
          )
        )
      ),
      "[1914 - 1916]" -> Node.Root(
        Node.Derived(
          Node.Range(
            Node.Single(Node.Year(Year.of(1914))),
            Node.Single(Node.Year(Year.of(1916)))
          )
        )
      )
    )

    forAll(testTable) { (input, expectedResult) =>
      s"""parse "$input"""" in {
        val parseResult = CoveringDateParser.runParser(CoveringDateParser.coveringDates, input)
        parseResult must parseSuccessfullyAs(expectedResult)
      }
    }
  }

}
