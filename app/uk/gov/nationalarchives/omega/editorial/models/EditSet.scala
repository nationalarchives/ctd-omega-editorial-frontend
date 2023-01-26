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

import uk.gov.nationalarchives.omega.editorial.services.KeyToOrdering

import play.api.libs.json._

case class EditSetEntry(ccr: String, oci: String, scopeAndContent: String, coveringDates: String)

case class EditSet(name: String, id: String, entries: Seq[EditSetEntry])

case class EditSetRecord(
  ccr: String,
  oci: String,
  scopeAndContent: String,
  coveringDates: String,
  formerReferenceDepartment: String,
  formerReferencePro: String,
  startDateDay: String,
  startDateMonth: String,
  startDateYear: String,
  endDateDay: String,
  endDateMonth: String,
  endDateYear: String,
  legalStatusID: String,
  placeOfDepositID: String,
  note: String,
  background: String,
  custodialHistory: String,
  relatedMaterial: Seq[RelatedMaterial] = Seq.empty,
  separatedMaterial: Seq[SeparatedMaterial] = Seq.empty,
  creatorIDs: Seq[String] = Seq.empty
)

object EditSetRecord {
  implicit val editSetRecordReads: Reads[EditSetRecord] = Json.using[Json.WithDefaultValues].reads[EditSetRecord]
}

object EditSetEntry {
  implicit val editSetEntryReads: Reads[EditSetEntry] = Json.reads[EditSetEntry]

  implicit val hasOrdering: KeyToOrdering[EditSetEntry] = KeyToOrdering.byFunction {
    case "ccr"               => Ordering.by(_.ccr)
    case "oci"               => Ordering.by(_.oci)
    case "scopeAndContent"   => Ordering.by(_.scopeAndContent)
    case "scope-and-content" => Ordering.by(_.scopeAndContent)
    case "coveringDates"     => Ordering.by(_.coveringDates)
    case "covering-dates"    => Ordering.by(_.coveringDates)
  }

}

object EditSet {
  implicit val editSetReads: Reads[EditSet] = Json.reads[EditSet]

}
