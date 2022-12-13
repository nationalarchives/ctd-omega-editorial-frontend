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
import org.scalatest.prop.{ TableDrivenPropertyChecks, Tables }
import support.BaseSpec
import support.CustomMatchers.parseSuccessfullyAs
import uk.gov.nationalarchives.omega.editorial.services.{ CoveringDateNode => Node, CoveringDateParser }
import uk.gov.nationalarchives.omega.editorial.models.DateRange

class CoveringDateCalculatorSpec extends BaseSpec with TableDrivenPropertyChecks {

  lazy val testTable = Tables.Table(
    "date input"             -> "parse result",
    "1305"                   -> defineTestCoveringDate("1305 Jan 1" -> "1305 Dec 31"),
    "1305 Apr"               -> defineTestCoveringDate("1305 Apr 1" -> "1305 Apr 30"),
    "1305 Apr 1"             -> defineTestCoveringDate("1305 Apr 1" -> "1305 Apr 1"),
    "1305-1306"              -> defineTestCoveringDate("1305 Jan 1" -> "1306 Dec 31"),
    "1305 Apr-1305 Oct"      -> defineTestCoveringDate("1305 Apr 1" -> "1305 Oct 31"),
    "1305 Apr 1–1306 Apr 15" -> defineTestCoveringDate("1305 Apr 1" -> "1306 Apr 15"),
    "[1914 - 1916]"          -> defineTestCoveringDate("1914 Jan 1" -> "1916 Dec 31"),
    "[c 1915]"               -> defineTestCoveringDate("1915 Jan 1" -> "1915 Dec 31"),
    "[c1915]"                -> defineTestCoveringDate("1915 Jan 1" -> "1915 Dec 31"),
    "[?1915]"                -> defineTestCoveringDate("1915 Jan 1" -> "1915 Dec 31"),
    "1868; 1890-1902; 1933" -> defineTestCoveringDate(
      "1868 Jan 1" -> "1868 Dec 31",
      "1890 Jan 1" -> "1902 Dec 31",
      "1933 Jan 1" -> "1933 Dec 31"
    )
  )

  "CoveringDateCalculator" should {

    forAll(testTable) { (input, expectedResult) =>
      s"""calculate the date range: "$input"""" in {
        CoveringDateCalculator.getStartAndEndDates(input) must parseSuccessfullyAs(expectedResult)
      }

    }

  }

  lazy val basicCoveringDateFormatter = DateTimeFormatter.ofPattern("u LLL d")

  def defineTestCoveringDate(cds: (String, String)*): List[DateRange] =
    cds.map { case (start, end) =>
      DateRange(
        LocalDate.parse(start, basicCoveringDateFormatter),
        LocalDate.parse(end, basicCoveringDateFormatter)
      )
    }.toList

}
