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

import java.time.LocalDate
import java.time.format.{ DateTimeFormatter, ResolverStyle }
import java.util._
import scala.util.Try

/** This is an "historically sensitive" date parser.
  *
  * The switch from the Julian to the Gregorian calendar happened on 14th September, 1752 - at least in the UK.
  *
  * Fortunately, java.time.LocalDate handles this switchover gracefully, so that even dates falling in the gap between
  * the two calendars are resolved, for instance: 13th September, 1752.
  *
  * Note, this does require that the ResolverStyle be set to SMART; incidentally, this also adds tolerance for a day
  * that doesn't exist in that particular month, for example 29th February, 2022. However, it does not allow a month of
  * 13 or higher, which LENIENT would have done.
  *
  * However, we have removed this tolerance for an invalid day of month.
  *
  * Please refer to https://blog.adamretter.org.uk/processing-historical-dates/ for background.
  */
object DateParser {

  private val partDelimiter = "/"

  val dateTimeFormatter =
    DateTimeFormatter
      .ofPattern(s"d${partDelimiter}M${partDelimiter}y")
      .withLocale(Locale.UK)
      .withResolverStyle(ResolverStyle.SMART)

  def parse(rawDateAsYearMonthDay: String): Option[LocalDate] =
    Try(LocalDate.parse(rawDateAsYearMonthDay, dateTimeFormatter)).toOption
      .filter(dateIsSameAsOriginal(_, rawDateAsYearMonthDay))

  private def dateIsSameAsOriginal(date: LocalDate, rawDateAsYearMonthDay: String): Boolean =
    dateTimeFormatter.format(date) == normaliseFormat(rawDateAsYearMonthDay)

  private def normaliseFormat(originalRawDate: String): String = {
    val partsOfOriginalRawDateWithoutLeadingZeros =
      originalRawDate
        .split(partDelimiter)
        .map(part => String.valueOf(part.toInt))
        .toSeq
    partsOfOriginalRawDateWithoutLeadingZeros.mkString(partDelimiter)
  }

}
