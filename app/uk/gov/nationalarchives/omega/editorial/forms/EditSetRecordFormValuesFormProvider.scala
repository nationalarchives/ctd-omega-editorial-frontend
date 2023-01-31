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

import play.api.data.Form
import play.api.data.Forms.{ mapping, seq, text }
import play.api.i18n.MessagesApi
import play.api.mvc.{ AnyContent, Request }
import uk.gov.nationalarchives.omega.editorial.controllers.EditSetRecordController.{ FieldNames, MessageKeys }
import uk.gov.nationalarchives.omega.editorial.services.CoveringDateCalculator.getStartAndEndDates
import uk.gov.nationalarchives.omega.editorial.support.MessageSupport

object EditSetRecordFormValuesFormProvider extends MessageSupport {

  def apply()(implicit request: Request[AnyContent], messagesApi: MessagesApi): Form[EditSetRecordFormValues] = Form(
    mapping(
      FieldNames.scopeAndContent -> text
        .verifying(
          resolveMessage(MessageKeys.scopeAndContentInvalid),
          value => value.length <= 8000
        )
        .verifying(resolveMessage(MessageKeys.scopeAndContentMissing), _.nonEmpty),
      FieldNames.coveringDates -> text
        .verifying(
          resolveMessage(MessageKeys.coveringDatesMissing),
          _.trim.nonEmpty
        )
        .verifying(
          resolveMessage(MessageKeys.coveringDatesTooLong),
          _.length <= 255
        )
        .verifying(
          resolveMessage(MessageKeys.coveringDatesUnparseable),
          value => getStartAndEndDates(value).isRight
        ),
      FieldNames.formerReferenceDepartment -> text.verifying(
        resolveMessage(MessageKeys.formerReferenceDepartmentInvalid),
        value => value.length <= 255
      ),
      FieldNames.formerReferencePro -> text.verifying(
        resolveMessage(MessageKeys.formerReferenceProInvalid),
        value => value.length <= 255
      ),
      FieldNames.startDateDay   -> text,
      FieldNames.startDateMonth -> text,
      FieldNames.startDateYear  -> text,
      FieldNames.endDateDay     -> text,
      FieldNames.endDateMonth   -> text,
      FieldNames.endDateYear    -> text,
      FieldNames.legalStatusID -> text
        .verifying(
          resolveMessage(MessageKeys.legalStatusMissing),
          _.trim.nonEmpty
        ),
      FieldNames.placeOfDepositID -> text
        .verifying(
          resolveMessage(MessageKeys.placeOfDepositMissingOrInvalid),
          _.trim.nonEmpty
        ),
      FieldNames.note -> text
        .verifying(
          resolveMessage(MessageKeys.noteTooLong),
          value => value.length <= 1000
        ),
      FieldNames.background -> text
        .verifying(
          resolveMessage(MessageKeys.backgroundTooLong),
          value => value.length <= 8000
        ),
      FieldNames.custodialHistory -> text
        .verifying(
          resolveMessage(MessageKeys.custodialHistoryTooLong),
          value => value.length <= 1000
        ),
      FieldNames.creatorIDs -> seq(text)
        .verifying(resolveMessage(MessageKeys.creatorMissing), _.nonEmpty)
    )(EditSetRecordFormValues.apply)(EditSetRecordFormValues.unapply)
  )

}
