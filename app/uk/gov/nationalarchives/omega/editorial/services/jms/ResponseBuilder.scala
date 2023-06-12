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

package uk.gov.nationalarchives.omega.editorial.services.jms

import cats.MonadError
import cats.effect.IO
import cats.implicits._
import jms4s.jms.JmsMessage
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import play.api.libs.json.{ Json, Reads, Writes }
import uk.gov.nationalarchives.omega.editorial.connectors.MessageType
import uk.gov.nationalarchives.omega.editorial.connectors.messages.MessageProperties
import uk.gov.nationalarchives.omega.editorial.models._

import javax.inject.{ Inject, Singleton }

@Singleton
class ResponseBuilder @Inject() (stubData: StubData) {
  import ResponseBuilder._

  private val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]
  private val me = MonadError[IO, Throwable]

  def jmsMessageId(jmsMessage: JmsMessage): IO[String] =
    me.fromOption(
      jmsMessage.getJMSMessageId,
      ifEmpty = MissingMessageType
    )

  def createResponseText(jmsMessage: JmsMessage): IO[String] =
    jmsMessage.getStringProperty(MessageProperties.OMGMessageTypeID) match {
      case Some(messageType) if MessageType.GetEditSetType.matches(messageType) =>
        handleGetEditSet(jmsMessage)
      case Some(messageType) if MessageType.GetEditSetRecordType.matches(messageType) =>
        handleGetEditSetRecord(jmsMessage)
      case Some(messageType) if MessageType.UpdateEditSetRecordType.matches(messageType) =>
        handleUpdateEditSetRecord(jmsMessage)
      case Some(messageType) if MessageType.GetLegalStatusesType.matches(messageType) =>
        handleGetLegalStatuses(jmsMessage)
      case Some(messageType) if MessageType.GetAgentSummariesType.matches(messageType) =>
        handleGetAgentSummaries(jmsMessage)
      case Some(unknown) =>
        onUnhandledCase(s"Message type is unrecognised: [$unknown]")
      case None =>
        onUnhandledCase(s"No message type provided")
    }

  private def handleGetEditSet(jmsMessage: JmsMessage): IO[String] =
    parse[GetEditSet](jmsMessage).flatMap(getEditSetRequest =>
      stubData
        .getEditSet(getEditSetRequest.oci)
        .map(editSetRecord => asJsonString(editSetRecord))
        .getOrElse(onUnknownEditSet(getEditSetRequest.oci))
    )

  private def handleGetEditSetRecord(jmsMessage: JmsMessage): IO[String] =
    parse[GetEditSetRecord](jmsMessage).flatMap(getEditSetRecordRequest =>
      stubData
        .getEditSetRecord(getEditSetRecordRequest.recordOci)
        .map(editSetRecord => asJsonString(editSetRecord))
        .getOrElse(onUnknownEditSetRecord(getEditSetRecordRequest.editSetOci, getEditSetRecordRequest.recordOci))
    )

  private def handleUpdateEditSetRecord(jmsMessage: JmsMessage): IO[String] =
    parse[UpdateEditSetRecord](jmsMessage).flatMap(updateEditSetRecordRequest =>
      stubData
        .getEditSetRecord(updateEditSetRecordRequest.recordOci)
        .map(editSetRecord => asJsonString(updateEditSetRecord(editSetRecord, updateEditSetRecordRequest)))
        .getOrElse(onUnknownEditSetRecord(updateEditSetRecordRequest.editSetOci, updateEditSetRecordRequest.recordOci))
    )

  private def handleGetLegalStatuses(jmsMessage: JmsMessage): IO[String] =
    parse[GetLegalStatuses](jmsMessage)
      .flatMap(_ => asJsonString(stubData.getLegalStatuses()))

  private def handleGetAgentSummaries(jmsMessage: JmsMessage): IO[String] =
    parse[GetAgentSummaryList](jmsMessage)
      .flatMap(agentSummaryReq =>
        asJsonString(stubData.getAgentSummaries().filter(_.depository == agentSummaryReq.depository))
      )

  private def asJsonString[T : Writes](entity: T): IO[String] = me.pure(Json.toJson(entity).toString)

  /** Note that we do not actually update the record, anymore.
    */
  private def updateEditSetRecord(
    editSetRecord: EditSetRecord,
    updateEditSetRecord: UpdateEditSetRecord
  ): UpdateResponseStatus = {
    logger.info(s"Attempting to update Edit Set Record with request [$updateEditSetRecord] ...")
    UpdateResponseStatus("success", s"Successfully updated record with OCI [${editSetRecord.oci}]")
  }

  private def onUnknownEditSet(editSetOci: String): IO[String] =
    onUnhandledCase(
      s"Unable to find Edit Set with OCI [$editSetOci]"
    )

  private def onUnknownEditSetRecord(editSetOci: String, recordOci: String): IO[String] =
    onUnhandledCase(
      s"Unable to find record for Edit Set with OCI [$editSetOci] and Record OCI [$recordOci]"
    )

  private def onUnhandledCase(errorMessage: String): IO[String] =
    logger.error(errorMessage) *> me.raiseError(
      new NotImplementedError(s"We don't yet handle this case: $errorMessage")
    )

  private def parse[T : Reads](jmsMessage: JmsMessage): IO[T] =
    messageText(jmsMessage).flatMap(parse[T])

  private def messageText(jmsMessage: JmsMessage): IO[String] =
    jmsMessage.asTextF[IO].adaptError(err => NotATextMessage(err))

  private def parse[A : Reads](messageText: String): IO[A] =
    me
      .fromOption(
        Json.parse(messageText).validate[A].asOpt,
        ifEmpty = CannotParse(messageText)
      )

}

object ResponseBuilder {

  sealed abstract class StubServerError extends Throwable

  private final case object MissingMessageType extends StubServerError
  private final case class NotATextMessage(err: Throwable) extends StubServerError
  private final case class CannotParse(txt: String) extends StubServerError

}
