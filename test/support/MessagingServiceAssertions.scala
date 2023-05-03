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

import org.scalatest.Assertion
import org.scalatest.matchers.must.Matchers._
import uk.gov.nationalarchives.omega.editorial.models.{ GetEditSet, GetEditSetRecord, UpdateEditSetRecord }
import uk.gov.nationalarchives.omega.editorial.support.TimeProvider

trait MessagingServiceAssertions {

  def assertNoCallMadeToGetEditSet()(implicit messagingServiceMonitoring: MessagingServiceMonitoring): Assertion =
    messagingServiceMonitoring.getLastSentGetEditSetRequest mustBe empty

  def assertCallMadeToGetEditSet(
    id: String
  )(implicit messagingServiceMonitoring: MessagingServiceMonitoring, timeProvider: TimeProvider): Assertion =
    messagingServiceMonitoring.getLastSentGetEditSetRequest mustBe Option(generateGetEditSet(id))

  def assertNoCallMadeToGetEditSetRecord()(implicit messagingServiceMonitoring: MessagingServiceMonitoring): Assertion =
    messagingServiceMonitoring.getLastSentGetEditSetRecordRequest mustBe empty

  def assertCallMadeToGetEditSetRecord(editSetId: String, editSetRecordId: String)(implicit
    messagingServiceMonitoring: MessagingServiceMonitoring,
    timeProvider: TimeProvider
  ): Assertion =
    messagingServiceMonitoring.getLastSentGetEditSetRecordRequest mustBe Some(
      generateGetEditSetRecord(editSetId, editSetRecordId)
    )

  def assertCallMadeToUpdateEditSetRecord(expectedUpdateEditSetRecord: UpdateEditSetRecord)(implicit
    messagingServiceMonitoring: MessagingServiceMonitoring
  ): Assertion =
    messagingServiceMonitoring.getLastSentUpdateEditSetRecordRequest mustBe Option(expectedUpdateEditSetRecord)

  def assertNoCallMadeToUpdateEditSetRecord()(implicit
    messagingServiceMonitoring: MessagingServiceMonitoring
  ): Assertion =
    messagingServiceMonitoring.getLastSentUpdateEditSetRecordRequest mustBe empty

  private def generateGetEditSet(editSetId: String)(implicit timeProvider: TimeProvider): GetEditSet =
    GetEditSet(editSetId, timeProvider.now())

  private def generateGetEditSetRecord(editSetId: String, editSetRecordId: String)(implicit
    timeProvider: TimeProvider
  ): GetEditSetRecord =
    GetEditSetRecord(editSetId, editSetRecordId, timeProvider.now())

}
