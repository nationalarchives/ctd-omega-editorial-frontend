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

package uk.gov.nationalarchives.omega.editorial.services

import cats.effect.IO
import uk.gov.nationalarchives.omega.editorial.connectors.ApiConnector
import uk.gov.nationalarchives.omega.editorial.forms.EditSetRecordFormValues
import uk.gov.nationalarchives.omega.editorial.models.UpdateEditSetRecord.Fields.MaterialReference
import uk.gov.nationalarchives.omega.editorial.models._
import uk.gov.nationalarchives.omega.editorial.support.{ DateParser, TimeProvider }

import java.time.LocalDate
import javax.inject.{ Inject, Singleton }
import scala.annotation.unused

@Singleton
class EditSetRecordService @Inject() (
  apiConnector: ApiConnector,
  referenceDataService: ReferenceDataService,
  timeProvider: TimeProvider
) {

  def get(editSetOci: String, recordOci: String): IO[Option[EditSetRecord]] =
    apiConnector.getEditSetRecord(GetEditSetRecord(editSetOci, recordOci, timeProvider.now()))

  def prepareCreatorIDs(creators: Seq[Creator], editSetRecord: EditSetRecord): EditSetRecord =
    editSetRecord.copy(creatorIDs = editSetRecord.creatorIDs.filter { id =>
      referenceDataService.isCreatorRecognised(creators, id)
    })

  def updateEditSetRecord(
    editSetId: String,
    recordId: String,
    values: EditSetRecordFormValues
  ): IO[UpdateResponseStatus] =
    apiConnector.updateEditSetRecord(asUpdateEditSetRecord(editSetId, recordId, values))

  private def asUpdateEditSetRecord(
    editSetId: String,
    recordId: String,
    values: EditSetRecordFormValues
  ): UpdateEditSetRecord = UpdateEditSetRecord(
    editSetOci = editSetId,
    recordOci = recordId,
    timestamp = timeProvider.now(),
    fields = UpdateEditSetRecord.Fields(
      description = values.scopeAndContent,
      coveringDates = values.coveringDates,
      formerReferenceDepartment = values.formerReferenceDepartment,
      formerReferencePro = values.formerReferencePro,
      startDate = extractRawStartDate(values),
      endDate = extractRawEndDate(values),
      legalStatusId = values.legalStatusID,
      placeOfDepositID = values.placeOfDepositID,
      note = values.note,
      background = values.background,
      custodialHistory = values.custodialHistory,
      relatedMaterial = extractRelatedMaterials(values),
      separatedMaterial = extractSeparatedMaterials(values),
      creatorIDs = extractCreatorIds(values)
    )
  )

  private def extractCreatorIds(values: EditSetRecordFormValues): Seq[String] =
    values.creatorIDs.map(_.trim).filter(_.nonEmpty)

  private def extractRawStartDate(values: EditSetRecordFormValues): LocalDate =
    parseDate(values.startDateDay, values.startDateMonth, values.startDateYear)

  private def extractRawEndDate(values: EditSetRecordFormValues): LocalDate =
    parseDate(values.endDateDay, values.endDateMonth, values.endDateYear)

  private def parseDate(rawDay: String, rawMonth: String, rawYear: String): LocalDate = {
    val rawDateAsYearMonthDay = Seq(rawDay, rawMonth, rawYear).mkString("/")
    DateParser
      .parse(rawDateAsYearMonthDay)
      .getOrElse(throw new RuntimeException(s"Invalid date: [$rawDateAsYearMonthDay]"))
  }

  // TODO: As we don't currently support modification of this field, we're sending hardcoded values
  private def extractRelatedMaterials(@unused values: EditSetRecordFormValues): Seq[MaterialReference] =
    Seq(
      MaterialReference(
        linkHref = None,
        linkText = None,
        description = Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
      ),
      MaterialReference(
        linkHref = Some("#;"),
        linkText = Some("COAL 80/80/3"),
        description = None
      ),
      MaterialReference(
        linkHref = Some("#;"),
        linkText = Some("COAL 80/80/2"),
        description = Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
      )
    )

  // TODO: As we don't currently support modification of this field, we're sending hardcoded values
  private def extractSeparatedMaterials(@unused values: EditSetRecordFormValues): Seq[MaterialReference] =
    Seq(
      MaterialReference(
        linkHref = Some("#;"),
        linkText = Some("COAL 80/80/5"),
        description = None
      ),
      MaterialReference(
        linkHref = Some("#;"),
        linkText = Some("COAL 80/80/6"),
        description = None
      ),
      MaterialReference(
        linkHref = Some("#;"),
        linkText = Some("COAL 80/80/7"),
        description = None
      )
    )

}
