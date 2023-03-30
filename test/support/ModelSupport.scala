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

package support

import uk.gov.nationalarchives.omega.editorial.models.UpdateEditSetRecord
import uk.gov.nationalarchives.omega.editorial.services.jms.StubData
import uk.gov.nationalarchives.omega.editorial.support.{ DateParser, TimeProvider }

trait ModelSupport extends StubData {

  def generateUpdateEditSetRecord(editSetId: String, recordOci: String)(implicit
    timeProvider: TimeProvider
  ): UpdateEditSetRecord = {

    def parseDate(day: String, month: String, year: String) = {
      val rawDateAsYearMonthDay = Seq(day, month, year).mkString("/")
      val localDateOpt = DateParser.parse(rawDateAsYearMonthDay)
      localDateOpt.getOrElse(throw new RuntimeException(s"Invalid date: [$rawDateAsYearMonthDay]"))
    }

    val editSetRecord = getExpectedEditSetRecord(recordOci)

    UpdateEditSetRecord(
      editSetOci = editSetId,
      recordOci = editSetRecord.oci,
      timestamp = timeProvider.now(),
      fields = UpdateEditSetRecord.Fields(
        description = editSetRecord.scopeAndContent,
        coveringDates = editSetRecord.coveringDates,
        formerReferenceDepartment = editSetRecord.formerReferenceDepartment,
        formerReferencePro = editSetRecord.formerReferencePro,
        startDate = parseDate(editSetRecord.startDateDay, editSetRecord.startDateMonth, editSetRecord.startDateYear),
        endDate = parseDate(editSetRecord.endDateDay, editSetRecord.endDateMonth, editSetRecord.endDateYear),
        legalStatusId = editSetRecord.legalStatusID,
        placeOfDepositID = editSetRecord.placeOfDepositID,
        note = editSetRecord.note,
        background = editSetRecord.background,
        custodialHistory = editSetRecord.custodialHistory,
        relatedMaterial = Seq(
          UpdateEditSetRecord.Fields.MaterialReference(
            linkHref = None,
            linkText = None,
            description = Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
          ),
          UpdateEditSetRecord.Fields.MaterialReference(
            linkHref = Some("#;"),
            linkText = Some("COAL 80/80/3"),
            description = None
          ),
          UpdateEditSetRecord.Fields.MaterialReference(
            linkHref = Some("#;"),
            linkText = Some("COAL 80/80/2"),
            description = Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
          )
        ),
        separatedMaterial = Seq(
          UpdateEditSetRecord.Fields.MaterialReference(
            linkHref = Some("#;"),
            linkText = Some("COAL 80/80/5"),
            description = None
          ),
          UpdateEditSetRecord.Fields.MaterialReference(
            linkHref = Some("#;"),
            linkText = Some("COAL 80/80/6"),
            description = None
          ),
          UpdateEditSetRecord.Fields.MaterialReference(
            linkHref = Some("#;"),
            linkText = Some("COAL 80/80/7"),
            description = None
          )
        ),
        creatorIDs = editSetRecord.creatorIDs.map(_.trim).filter(_.nonEmpty)
      )
    )
  }

}
