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

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

case class EditSetEntry(ccr: String, oci: String, scopeAndContent: String, coveringDates: String)
case class EditSet(name: String, id: String, entries: Seq[EditSetEntry])

case class EditSetRecord(
  ccr: String,
  oci: String,
  scopeAndContent: String,
  coveringDates: String,
  formerReferenceDepartment: String,
  startDate: String,
  endDate: String
)
object EditSetRecord {
  implicit val editSetRecordReads: Reads[EditSetRecord] =
    (
      (JsPath \ "ccr").read[String](minLength[String](5)) and
        (JsPath \ "oci").read[String](minLength[String](5)) and
        (JsPath \ "scopeAndContent").read[String](minLength[String](10)) and
        (JsPath \ "coveringDates").read[String](minLength[String](0)) and
        (JsPath \ "formerReferenceDepartment").read[String](minLength[String](0)) and
        (JsPath \ "startDate").read[String](minLength[String](0)) and
        (JsPath \ "endDate").read[String](minLength[String](0))
    )(EditSetRecord.apply _)
}

object EditSet {
  implicit val editSetReads: Reads[EditSet] =
    (
      (JsPath \ "name").read[String](minLength[String](5)) and
        (JsPath \ "id").read[String](minLength[String](1)) and
        (JsPath \ "entries").read[Seq[EditSetEntry]]
    )(EditSet.apply _)

}

object EditSetEntry {
  implicit val editSetEntryReads: Reads[EditSetEntry] =
    (
      (JsPath \ "ccr").read[String](minLength[String](5)) and
        (JsPath \ "oci").read[String](minLength[String](5)) and
        (JsPath \ "scopeAndContent").read[String](minLength[String](10)) and
        (JsPath \ "coveringDates").read[String](minLength[String](0))
    )(EditSetEntry.apply _)
}
