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

import java.text.SimpleDateFormat
import java.util._
import scala.util.Try

/** Please refer to https://blog.adamretter.org.uk/processing-historical-dates/ for background.
  */
object HistoricalDateParser {

  private final val dateFormat = initDateFormat

  def parse(rawDateAsYearMonthDay: String): Option[Date] = Try(dateFormat.parse(rawDateAsYearMonthDay)).toOption

  private def initDateFormat: SimpleDateFormat = {
    val dateFormat = new SimpleDateFormat("d/M/yyyy")
    dateFormat.setCalendar(initGregorianCalendar)
    dateFormat.setLenient(false)
    dateFormat
  }

  private def initGregorianCalendar: GregorianCalendar = {
    val locale = Locale.UK
    val timeZone = TimeZone.getTimeZone("Europe/London")
    val calendar = new GregorianCalendar(timeZone, locale)
    calendar.setGregorianChange(cutOverDate(locale, timeZone))
    calendar
  }

  private def cutOverDate(locale: Locale, timeZone: TimeZone) = {
    val cutOverDateFromJulianToGregorianCalendar = new GregorianCalendar(timeZone, locale)
    cutOverDateFromJulianToGregorianCalendar.set(1752, Calendar.SEPTEMBER, 14)
    cutOverDateFromJulianToGregorianCalendar.getTime
  }

}
