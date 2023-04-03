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
import cats.implicits._
import jms4s.jms.JmsMessage
import org.typelevel.log4cats.Logger
import play.api.libs.json.{ Json, Reads, Writes }
import uk.gov.nationalarchives.omega.editorial.connectors.ApiConnector.SID
import uk.gov.nationalarchives.omega.editorial.models._
import uk.gov.nationalarchives.omega.editorial.services.jms.ResponseBuilder.ME

class ResponseBuilder[F[_] : ME : Logger] extends StubData {
  import ResponseBuilder._

  private val me = MonadError[F, Throwable]
  private val logger = Logger[F]

  def jmsMessageId(jmsMessage: JmsMessage): F[String] =
    me.fromOption(
      jmsMessage.getJMSMessageId,
      ifEmpty = MissingJMSID
    )

  def createResponseText(jmsMessage: JmsMessage): F[String] =
    jmsMessage.getStringProperty(sidHeaderKey) match {
      case Some(sidValue) if SID.GetEditSet.matches(sidValue) =>
        handleGetEditSet(jmsMessage)
      case Some(sidValue) if SID.GetEditSetRecord.matches(sidValue) =>
        handleGetEditSetRecord(jmsMessage)
      case Some(sidValue) if SID.UpdateEditSetRecord.matches(sidValue) =>
        handleUpdateEditSetRecord(jmsMessage)
      case Some(sidValue) if SID.GetPlacesOfDeposit.matches(sidValue) =>
        handleGetPlacesOfDeposit(jmsMessage)
      case Some(sidValue) if SID.GetCreators.matches(sidValue) =>
        handleGetCreator(jmsMessage)
      case Some(unknown) =>
        onUnhandledCase(s"SID is unrecognised: [$unknown]")
      case None =>
        onUnhandledCase(s"No SID provided")
    }

  private def handleGetEditSet(jmsMessage: JmsMessage): F[String] =
    parse[GetEditSet](jmsMessage).flatMap(getEditSetRequest =>
      getEditSet(getEditSetRequest.oci)
        .map(editSetRecord => asJsonString(editSetRecord))
        .getOrElse(onUnknownEditSet(getEditSetRequest.oci))
    )

  private def handleGetEditSetRecord(jmsMessage: JmsMessage): F[String] =
    parse[GetEditSetRecord](jmsMessage).flatMap(getEditSetRecordRequest =>
      getEditSetRecord(getEditSetRecordRequest.recordOci)
        .map(editSetRecord => asJsonString(editSetRecord))
        .getOrElse(onUnknownEditSetRecord(getEditSetRecordRequest.editSetOci, getEditSetRecordRequest.recordOci))
    )

  private def handleUpdateEditSetRecord(jmsMessage: JmsMessage): F[String] =
    parse[UpdateEditSetRecord](jmsMessage).flatMap(updateEditSetRecordRequest =>
      getEditSetRecord(updateEditSetRecordRequest.recordOci)
        .map(editSetRecord => asJsonString(updateEditSetRecord(editSetRecord, updateEditSetRecordRequest)))
        .getOrElse(onUnknownEditSetRecord(updateEditSetRecordRequest.editSetOci, updateEditSetRecordRequest.recordOci))
    )

  private def handleGetPlacesOfDeposit(jmsMessage: JmsMessage): F[String] =
    parse[GetPlacesOfDeposit](jmsMessage)
      .flatMap(_ => asJsonString(getPlacesOfDeposit()))

  private def handleGetCreator(jmsMessage: JmsMessage): F[String] =
    parse[GetCreators](jmsMessage)
      .flatMap(_ => asJsonString(getCreators()))

  private def asJsonString[T : Writes](entity: T): F[String] = me.pure(Json.toJson(entity).toString)

  /** Note that we do not actually update the record, anymore.
    */
  private def updateEditSetRecord(
    editSetRecord: EditSetRecord,
    updateEditSetRecord: UpdateEditSetRecord
  ): UpdateResponseStatus = {
    logger.info(s"Attempting to update Edit Set Record with request [$updateEditSetRecord] ...")
    UpdateResponseStatus("success", s"Successfully updated record with OCI [${editSetRecord.oci}]")
  }

  private def onUnknownEditSet(editSetOci: String): F[String] =
    onUnhandledCase(
      s"Unable to find Edit Set with OCI [$editSetOci]"
    )

  private def onUnknownEditSetRecord(editSetOci: String, recordOci: String): F[String] =
    onUnhandledCase(
      s"Unable to find record for Edit Set with OCI [$editSetOci] and Record OCI [$recordOci]"
    )

  private def onUnhandledCase(errorMessage: String): F[String] =
    logger.error(errorMessage) *> me.raiseError(
      new NotImplementedError(s"We don't yet handle this case: $errorMessage")
    )

  private def parse[T : Reads](jmsMessage: JmsMessage): F[T] =
    messageText(jmsMessage).flatMap(parse[T])

  private def messageText(jmsMessage: JmsMessage): F[String] =
    jmsMessage.asTextF[F].adaptError(err => NotATextMessage(err))

  private def parse[A : Reads](messageText: String): F[A] =
    me.fromOption(
      Json.parse(messageText).validate[A].asOpt,
      ifEmpty = CannotParse(messageText)
    )

}

object ResponseBuilder {

  type ME[F[_]] = MonadError[F, Throwable]

  sealed abstract class StubServerError extends Throwable

  private final case object MissingJMSID extends StubServerError
  private final case class NotATextMessage(err: Throwable) extends StubServerError
  private final case class CannotParse(txt: String) extends StubServerError

  private val sidHeaderKey = "sid"

}
