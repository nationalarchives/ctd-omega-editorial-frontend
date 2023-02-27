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

package uk.gov.nationalarchives.omega.editorial.models

import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatest.wordspec.AnyWordSpec

import java.time.LocalDate
import java.time.Month.{ DECEMBER, FEBRUARY, JANUARY, OCTOBER }

class DateRangeSpec extends AnyWordSpec {

  private val october4th2020: LocalDate = LocalDate.of(2020, OCTOBER, 4)
  private val october14th2020 = LocalDate.of(2020, OCTOBER, 14)
  private val january10th2021: LocalDate = LocalDate.of(2021, JANUARY, 10)
  private val february14th2021 = LocalDate.of(2021, FEBRUARY, 14)
  private val december1st2022 = LocalDate.of(2022, DECEMBER, 1)
  private val december31st2022 = LocalDate.of(2022, DECEMBER, 31)

  "extracting a single date range" when {
    "there are none" in {
      DateRange.single(Seq.empty) mustBe Matchers.empty
    }
    "there is only one" in {
      val dateRange = DateRange(october4th2020, october14th2020)
      DateRange.single(Seq(dateRange)) mustBe Some(dateRange)
    }
    "there are several" when {
      "in chronological order" in {
        DateRange.single(
          Seq(
            DateRange(october4th2020, october14th2020),
            DateRange(january10th2021, february14th2021),
            DateRange(december1st2022, december31st2022)
          )
        ) mustBe Some(DateRange(october4th2020, december31st2022))
      }
      "not in chronological order" in {
        DateRange.single(
          Seq(
            DateRange(december1st2022, december31st2022),
            DateRange(january10th2021, february14th2021),
            DateRange(october4th2020, october14th2020)
          )
        ) mustBe Some(DateRange(october4th2020, december31st2022))
      }
    }
  }

}
