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

import play.api.libs.json.{ Format, Json }
import uk.gov.nationalarchives.omega.editorial.models.UpdateEditSetRecord.Fields

import java.time.{ LocalDate, LocalDateTime }

case class UpdateEditSetRecord(editSetOci: String, recordOci: String, timestamp: LocalDateTime, fields: Fields)

object UpdateEditSetRecord {

  implicit val format: Format[UpdateEditSetRecord] = Json.format[UpdateEditSetRecord]

  case class Fields(
    description: String,
    coveringDates: String,
    formerReferenceDepartment: String,
    formerReferencePro: String,
    startDate: LocalDate,
    endDate: LocalDate,
    legalStatusId: String,
    placeOfDepositID: String,
    note: String,
    background: String,
    custodialHistory: String,
    relatedMaterial: Seq[Fields.MaterialReference],
    separatedMaterial: Seq[Fields.MaterialReference],
    creatorIDs: Seq[String]
  )

  object Fields {

    implicit val format: Format[Fields] = Json.format[Fields]

    case class MaterialReference(linkHref: Option[String], linkText: Option[String], description: Option[String])

    object MaterialReference {

      implicit val format: Format[MaterialReference] = Json.format[MaterialReference]

    }
  }

}
