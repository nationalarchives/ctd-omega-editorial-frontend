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

import org.scalatest.prop.{ TableDrivenPropertyChecks, Tables }
import support.BaseSpec
import support.CommonMatchers.{ failToParseAs, parseSuccessfullyAs }
import uk.gov.nationalarchives.omega.editorial.models.DateRange
import uk.gov.nationalarchives.omega.editorial.services.CoveringDateCalculator
import uk.gov.nationalarchives.omega.editorial.services.CoveringDateError._

import java.time._
import java.time.format.DateTimeFormatter
import java.util.Locale

class CoveringDateCalculatorSpec extends BaseSpec with TableDrivenPropertyChecks {

  "CoveringDateCalculator" should {

    val validScenarioTestTable = Tables.Table(
      "date input"                     -> "parse result",
      "1305"                           -> defineTestCoveringDate("1305 Jan 1" -> "1305 Dec 31"),
      "1305 Apr"                       -> defineTestCoveringDate("1305 Apr 1" -> "1305 Apr 30"),
      "1305 Apr 1"                     -> defineTestCoveringDate("1305 Apr 1" -> "1305 Apr 1"),
      "1305-1306"                      -> defineTestCoveringDate("1305 Jan 1" -> "1306 Dec 31"),
      "1305 Apr-1305 Oct"              -> defineTestCoveringDate("1305 Apr 1" -> "1305 Oct 31"),
      "1305 Apr 1–1306 Apr 15"         -> defineTestCoveringDate("1305 Apr 1" -> "1306 Apr 15"),
      "Between 1305 Apr-1305 Oct"      -> defineTestCoveringDate("1305 Apr 1" -> "1305 Oct 31"),
      "Between 1305 Apr 1–1306 Apr 15" -> defineTestCoveringDate("1305 Apr 1" -> "1306 Apr 15"),
      "between 1305–1306"              -> defineTestCoveringDate("1305 Jan 1" -> "1306 Dec 31"),
      "[1914 - 1916]"                  -> defineTestCoveringDate("1914 Jan 1" -> "1916 Dec 31"),
      "[c 1915]"                       -> defineTestCoveringDate("1915 Jan 1" -> "1915 Dec 31"),
      "[c1915]"                        -> defineTestCoveringDate("1915 Jan 1" -> "1915 Dec 31"),
      "[?1915]"                        -> defineTestCoveringDate("1915 Jan 1" -> "1915 Dec 31"),
      "1868; 1890-1902; 1933" -> defineTestCoveringDate(
        "1868 Jan 1" -> "1868 Dec 31",
        "1890 Jan 1" -> "1902 Dec 31",
        "1933 Jan 1" -> "1933 Dec 31"
      ),
      "1582 Oct 11"               -> defineTestCoveringDate("1582 Oct 11" -> "1582 Oct 11"),
      "1582 Oct 11 - 1582 Nov 29" -> defineTestCoveringDate("1582 Oct 11" -> "1582 Nov 29"),
      // Dates related to the switchover from the Julian to the Gregorian calendar.
      "1752 Aug"                  -> defineTestCoveringDate("1752 Aug 1" -> "1752 Aug 31"),
      "1752 Aug 1–1752 Aug 2"     -> defineTestCoveringDate("1752 Aug 1" -> "1752 Aug 2"),
      "1752 Sept 1–1752 Sept 2"   -> defineTestCoveringDate("1752 Sep 1" -> "1752 Sep 2"),
      "1752 Sept 3–1752 Sept 13"  -> defineTestCoveringDate("1752 Sep 3" -> "1752 Sep 13"),
      "1752 Sept 14–1752 Sept 15" -> defineTestCoveringDate("1752 Sep 14" -> "1752 Sep 15"),
      "1752 Sept"                 -> defineTestCoveringDate("1752 Sep 1" -> "1752 Sep 30"),
      // End of switchover dates.
      "undated" -> List.empty
    )

    forAll(validScenarioTestTable) { (input, expectedResult) =>
      s"""calculate the date range successfully for: "$input"""" in {
        CoveringDateCalculator.getStartAndEndDates(input) must parseSuccessfullyAs(expectedResult)
      }
    }

    // TODO: Right now we don't check parse errors
    val invalidScenarioTestTable = Tables.Table(
      "date input"      -> "parse error",
      "1270s"           -> ParseError,
      "1305 Sept - Oct" -> ParseError,
      "Temp Edw I"      -> ParseError,
      "01 Oct 1305"     -> ParseError,
      "Mon 1 Oct 1330"  -> ParseError,
      "1999 - 1305"     -> InvalidRange(DateRange(LocalDate.of(1999, 1, 1), LocalDate.of(1305, 12, 31))),
      "1999 - 1305; 2000 Feb 3 - 1306 Nov 30" -> MultipleErrors(
        List(
          InvalidRange(DateRange(LocalDate.of(1999, 1, 1), LocalDate.of(1305, 12, 31))),
          InvalidRange(DateRange(LocalDate.of(2000, 2, 3), LocalDate.of(1306, 11, 30)))
        )
      )
    )

    forAll(invalidScenarioTestTable) { (input, expectedError) =>
      s"""fail to calculate: "$input"""" in {
        CoveringDateCalculator.getStartAndEndDates(input) must failToParseAs(expectedError)
      }
    }

  }

  lazy val basicCoveringDateFormatter = DateTimeFormatter.ofPattern("u MMM d").withLocale(Locale.UK)

  def defineTestCoveringDate(cds: (String, String)*): List[DateRange] =
    cds.map { case (start, end) =>
      DateRange(
        LocalDate.parse(start, basicCoveringDateFormatter),
        LocalDate.parse(end, basicCoveringDateFormatter)
      )
    }.toList

}
