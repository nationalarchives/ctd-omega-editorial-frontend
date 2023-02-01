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

import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatest.{ MustMatchers, WordSpec }
import uk.gov.nationalarchives.omega.editorial.support.DateParser.parse

import java.time.LocalDate
import java.time.Month.{ APRIL, DECEMBER, SEPTEMBER }

class DateParserSpec extends WordSpec with MustMatchers {

  "'parseDate' returns the expected the result" when {
    "the date should be considered valid" when {
      "before the switchover period" in {
        forAll(
          Table(
            ("Raw Date", "Expected Date"),
            ("1/9/1752", LocalDate.of(1752, SEPTEMBER, 1)),
            ("2/9/1752", LocalDate.of(1752, SEPTEMBER, 2))
          )
        )((rawDate: String, expectedDate: LocalDate) => parse(rawDate) mustBe Some(expectedDate))

      }
      "during the switchover period" in {

        forAll(
          Table(
            ("Raw Date", "Expected Date"),
            ("3/9/1752", LocalDate.of(1752, SEPTEMBER, 3)),
            ("4/9/1752", LocalDate.of(1752, SEPTEMBER, 4)),
            ("5/9/1752", LocalDate.of(1752, SEPTEMBER, 5)),
            ("6/9/1752", LocalDate.of(1752, SEPTEMBER, 6)),
            ("7/9/1752", LocalDate.of(1752, SEPTEMBER, 7)),
            ("8/9/1752", LocalDate.of(1752, SEPTEMBER, 8)),
            ("9/9/1752", LocalDate.of(1752, SEPTEMBER, 9)),
            ("10/9/1752", LocalDate.of(1752, SEPTEMBER, 10)),
            ("11/9/1752", LocalDate.of(1752, SEPTEMBER, 11)),
            ("12/9/1752", LocalDate.of(1752, SEPTEMBER, 12)),
            ("13/9/1752", LocalDate.of(1752, SEPTEMBER, 13))
          )
        )((rawDate: String, expectedDate: LocalDate) => parse(rawDate) mustBe Some(expectedDate))

      }
      "after the switchover period" in {
        forAll(
          Table(
            ("Raw Date", "Expected Date"),
            ("14/9/1752", LocalDate.of(1752, SEPTEMBER, 14)),
            ("15/9/1752", LocalDate.of(1752, SEPTEMBER, 15)),
            ("20/4/1984", LocalDate.of(1984, APRIL, 20)),
            ("26/12/2022", LocalDate.of(2022, DECEMBER, 26))
          )
        )((rawDate: String, expectedDate: LocalDate) => parse(rawDate) mustBe Some(expectedDate))
      }
      "day starting with a zero" in {
        parse("09/4/2022") mustBe Some(LocalDate.of(2022, APRIL, 9))
      }

      "month starting with a zero" in {
        parse("29/04/2022") mustBe Some(LocalDate.of(2022, APRIL, 29))
      }

      "year starting with a zero" in {
        parse("5/9/022") mustBe Some(LocalDate.of(22, SEPTEMBER, 5))
      }

      "year is 1, 2 or 3 digits" in {
        forAll(
          Table(
            ("Raw Date", "Expected Date"),
            ("1/12/1", LocalDate.of(1, DECEMBER, 1)),
            ("5/9/10", LocalDate.of(10, SEPTEMBER, 5)),
            ("5/9/100", LocalDate.of(100, SEPTEMBER, 5))
          )
        )((rawDate: String, expectedDate: LocalDate) => parse(rawDate) mustBe Some(expectedDate))
      }
    }

    "the date should be considered invalid" when {

      "malformed" in {
        Seq("14-9-2020", "9/2020", "2020", "1/14/9/2020")
          .foreach(rawDate => parse(rawDate) mustBe empty)
      }
      "non existent" in {
        Seq("29/2/2022", "30/2/2022", "31/2/2022", "42/10/2022", "10/14/2022")
          .foreach(rawDate => parse(rawDate) mustBe empty)
      }
      "given a negative year" in {
        parse("1/11/-10") mustBe empty
      }
      "given year is zero" in {
        parse("1/11/0") mustBe empty
      }
    }
  }

}
