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

package uk.gov.nationalarchives.omega.editorial.support

import org.scalatest.matchers.{MatchResult, Matcher}
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import uk.gov.nationalarchives.omega.editorial.support.HistoricalDateParser.parse

import java.util._

class HistoricalDateParserSpec extends WordSpec with MustMatchers with OptionValues {

  "'parse' returns the expected the result" when {
    "the date should be considered valid" when {
      "before the switchover period" in {
        parse("1/9/1752").value must beSameGregorianDateAs(12, Calendar.SEPTEMBER, 1752)
        parse("2/9/1752").value must beSameGregorianDateAs(13, Calendar.SEPTEMBER, 1752)
      }
      "after the switchover period" in {
        parse("14/9/1752").value must beSameGregorianDateAs(14, Calendar.SEPTEMBER, 1752)
        parse("15/9/1752").value must beSameGregorianDateAs(15, Calendar.SEPTEMBER, 1752)
        parse("20/4/1984").value must beSameGregorianDateAs(20, Calendar.APRIL, 1984)
      }
    }
    "the date should be considered invalid" when {
      "during the switchover period" in {
        Seq("3/9/1752", "4/9/1752", "6/9/1752", "7/9/1752", "8/9/1752", "9/9/1752", "10/9/1752", "11/9/1752", "12/9/1752", "13/9/1752")
          .foreach(rawDate => parse(rawDate) mustBe empty)
      }
    }
  }

  private def beSameGregorianDateAs(expectedDay: Int, expectedMonth: Int, expectedYear: Int): Matcher[Date] = (date: Date) => {

    def formatForDisplay(day: Int, month: Int, year: Int): String = Seq(day, month, year).mkString("/")

    val calendar = new GregorianCalendar(TimeZone.getTimeZone("Europe/London"), Locale.UK)
    calendar.setTime(date)
    val actualDay = calendar.get(Calendar.DAY_OF_MONTH)
    val actualMonth = calendar.get(Calendar.MONTH)
    val actualYear = calendar.get(Calendar.YEAR)
    val expectedDateForDisplay = formatForDisplay(expectedDay, expectedMonth, expectedYear)
    val actualDateForDisplay = formatForDisplay(actualDay, actualMonth, actualYear)
    val errorMessageIfExpected =
      s"The page didn't have the expected date '$expectedDateForDisplay'. The actual value was '$actualDateForDisplay'"
    val errorMessageIfNotExpected = s"The page did indeed have an expected date '$expectedDateForDisplay', which was not expected."
    MatchResult(
      (actualDay == expectedDay) && (actualMonth == expectedMonth) && (actualYear == expectedYear),
      errorMessageIfExpected,
      errorMessageIfNotExpected
    )
  }

}