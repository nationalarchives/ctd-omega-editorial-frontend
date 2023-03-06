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
import play.api.libs.json.{ Json, Reads }
import uk.gov.nationalarchives.omega.editorial.editSetRecords.getEditSetRecordByOCI
import uk.gov.nationalarchives.omega.editorial.editSets
import uk.gov.nationalarchives.omega.editorial.models._
import uk.gov.nationalarchives.omega.editorial.services.jms.ResponseBuilder.ME

class ResponseBuilder[F[_] : ME : Logger] {
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
      case Some(sidValue) if sidValue == SID.GetEditSet =>
        handleGetEditSet(jmsMessage, sidValue)
      case Some(sidValue) if sidValue == SID.GetEditSetRecord =>
        handleGetEditSetRecord(jmsMessage)
      case Some(sidValue) if sidValue == SID.UpdateEditSetRecord =>
        handleUpdateEditSetRecord(jmsMessage)
      case unknown =>
        onUnhandledCase(s"SID is unrecognised: [$unknown]")
    }

  private def handleGetEditSet(jmsMessage: JmsMessage, sidValue: String): F[String] =
    logger.info(s"got a message with sid header '$sidValue', trying to parse payload") *>
      getEditSet(jmsMessage).flatMap { editSet =>
        logger.info("parsed payload, creating edit set response") *>
          me.pure(Json.toJson(editSet).toString)
      }

  private def handleGetEditSetRecord(jmsMessage: JmsMessage): F[String] =
    asGetEditSetRecordRequest(jmsMessage).flatMap(getEditSetRecordRequest =>
      getEditSetRecordByOCI(getEditSetRecordRequest.recordOci)
        .map(editSetRecord => me.pure(Json.toJson(editSetRecord).toString))
        getOrElse onUnknownEditSetRecord(getEditSetRecordRequest.editSetOci, getEditSetRecordRequest.recordOci)
    )

  private def handleUpdateEditSetRecord(jmsMessage: JmsMessage): F[String] =
    asUpdateEditSetRecordRequest(jmsMessage).flatMap(updateEditSetRecordRequest =>
      getEditSetRecordByOCI(updateEditSetRecordRequest.recordOci)
        .map(editSetRecord =>
          me.pure(Json.toJson(updateEditSetRecord(editSetRecord, updateEditSetRecordRequest)).toString)
        )
        .getOrElse(onUnknownEditSetRecord(updateEditSetRecordRequest.editSetOci, updateEditSetRecordRequest.recordOci))
    )

  private def updateEditSetRecord(
    editSetRecord: EditSetRecord,
    updateEditSetRecord: UpdateEditSetRecord
  ): UpdateResponseStatus = {
    logger.info(s"Attempting to update Edit Set Record with request [$updateEditSetRecord] ...")
    UpdateResponseStatus("success", s"Successfully updated record with OCI [${editSetRecord.oci}]")
  }

  private def onUnknownEditSetRecord(editSetOci: String, recordOci: String): F[String] =
    onUnhandledCase(
      s"Unable to find record for Edit Set with OCI [$editSetOci] and Record OCI [$recordOci]"
    )

  private def onUnhandledCase(errorMessage: String): F[String] =
    logger.error(errorMessage) *> me.raiseError(
      new NotImplementedError(s"We don't yet handle this case: $errorMessage")
    )

  private def getEditSet(jmsMessage: JmsMessage): F[EditSet] =
    for {
      messageText <- messageText(jmsMessage)
      // TODO We'll need to get the OCI from the GetEditSet eventuly
      _ <- parse[GetEditSet](messageText)
    } yield editSets.editSet1

  private def asGetEditSetRecordRequest(jmsMessage: JmsMessage): F[GetEditSetRecord] =
    messageText(jmsMessage).flatMap(parse[GetEditSetRecord])

  private def asUpdateEditSetRecordRequest(jmsMessage: JmsMessage): F[UpdateEditSetRecord] =
    messageText(jmsMessage).flatMap(parse[UpdateEditSetRecord])

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

  final case object MissingJMSID extends StubServerError
  private final case class NotATextMessage(err: Throwable) extends StubServerError
  private final case class CannotParse(txt: String) extends StubServerError

  private val sidHeaderKey = "sid"

  object SID {
    val GetEditSet = "OSGEES001"
    val GetEditSetRecord = "OSGESR001"
    val UpdateEditSetRecord = "OSUESR001"
  }

}
