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

package uk.gov.nationalarchives.omega.editorial.forms

import uk.gov.nationalarchives.omega.editorial.models.EditSetRecord

case class EditSetRecordFormValues(
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
  creatorIDs: Seq[String] = Seq.empty
)

object EditSetRecordFormValues {

  def modifyEditSetRecordWithFormValues(
    editSetRecord: EditSetRecord,
    formValues: EditSetRecordFormValues
  ): EditSetRecord =
    editSetRecord.copy(
      scopeAndContent = formValues.scopeAndContent,
      coveringDates = formValues.coveringDates,
      formerReferenceDepartment = formValues.formerReferenceDepartment,
      formerReferencePro = formValues.formerReferencePro,
      startDateDay = String.valueOf(formValues.startDateDay.toInt),
      startDateMonth = String.valueOf(formValues.startDateMonth.toInt),
      startDateYear = String.valueOf(formValues.startDateYear.toInt),
      endDateDay = String.valueOf(formValues.endDateDay.toInt),
      endDateMonth = String.valueOf(formValues.endDateMonth.toInt),
      endDateYear = String.valueOf(formValues.endDateYear.toInt),
      legalStatusID = formValues.legalStatusID,
      placeOfDepositID = formValues.placeOfDepositID,
      note = formValues.note,
      background = formValues.background,
      custodialHistory = formValues.custodialHistory,
      creatorIDs = formValues.creatorIDs
    )

  def populateForm(editSetRecord: EditSetRecord): EditSetRecordFormValues =
    EditSetRecordFormValues(
      scopeAndContent = editSetRecord.scopeAndContent,
      coveringDates = editSetRecord.coveringDates,
      formerReferenceDepartment = editSetRecord.formerReferenceDepartment,
      formerReferencePro = editSetRecord.formerReferencePro,
      startDateDay = editSetRecord.startDateDay,
      startDateMonth = editSetRecord.startDateMonth,
      startDateYear = editSetRecord.startDateYear,
      endDateDay = editSetRecord.endDateDay,
      endDateMonth = editSetRecord.endDateMonth,
      endDateYear = editSetRecord.endDateYear,
      legalStatusID = editSetRecord.legalStatusID,
      placeOfDepositID = editSetRecord.placeOfDepositID,
      note = editSetRecord.note,
      background = editSetRecord.background,
      custodialHistory = editSetRecord.custodialHistory,
      creatorIDs = editSetRecord.creatorIDs
    )

}
