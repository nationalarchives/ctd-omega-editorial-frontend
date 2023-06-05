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
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.{ Json, Reads }
import uk.gov.nationalarchives.omega.editorial.connectors.{ ApiConnector, MessageType }
import uk.gov.nationalarchives.omega.editorial.models.{ AgentSummary, EditSet, EditSetRecord, GetAgentSummaryList, GetEditSet, GetEditSetRecord, GetLegalStatuses, GetPlacesOfDeposit, LegalStatus, PlaceOfDeposit, UpdateEditSetRecord, UpdateResponseStatus }

import javax.inject.{ Inject, Singleton }

@Singleton
class MessagingService @Inject() (apiConnector: ApiConnector) {

  private implicit val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]

  def getEditSet(getEditSet: GetEditSet): IO[Option[EditSet]] = {
    val requestBody = Json.stringify(Json.toJson(getEditSet))
    logger.info(s"Requesting edit set ${getEditSet.oci}...") *>
      apiConnector
        .handle(MessageType.GetEditSetType, requestBody)
        .flatMap(replyMessage => parse[EditSet](replyMessage.messageText))
        .redeem(_ => None, Some.apply)
  }

  def getEditSetRecord(getEditSetRecord: GetEditSetRecord): IO[Option[EditSetRecord]] = {
    val requestBody = Json.stringify(Json.toJson(getEditSetRecord))
    logger.info(s"Requesting record ${getEditSetRecord.recordOci} from edit set ${getEditSetRecord.editSetOci}") *>
      apiConnector
        .handle(MessageType.GetEditSetRecordType, requestBody)
        .flatMap(replyMessage => parse[EditSetRecord](replyMessage.messageText))
        .redeem(_ => None, Some.apply)
  }

  def updateEditSetRecord(updateEditSetRecord: UpdateEditSetRecord): IO[UpdateResponseStatus] = {
    val requestBody = Json.stringify(Json.toJson(updateEditSetRecord))
    logger.info(
      s"Requesting update of edit set record ${updateEditSetRecord.recordOci}: \n${pprint.apply(updateEditSetRecord)}\n"
    ) *>
      apiConnector
        .handle(MessageType.UpdateEditSetRecordType, requestBody)
        .flatMap(replyMessage => parse[UpdateResponseStatus](replyMessage.messageText))
  }

  def getPlacesOfDeposit(getPlacesOfDeposit: GetPlacesOfDeposit): IO[Seq[PlaceOfDeposit]] =
    logger.info(s"Requesting all of the places of deposit") *>
      apiConnector
        .handle(MessageType.GetPlacesOfDepositType, Json.stringify(Json.toJson(getPlacesOfDeposit)))
        .flatMap(replyMessage => parse[Seq[PlaceOfDeposit]](replyMessage.messageText))

  def getAgentSummaries(getAgentSummaryList: GetAgentSummaryList): IO[Seq[AgentSummary]] =
    logger.info(s"Requesting all of the agent summaries") *>
      apiConnector
        .handle(MessageType.GetAgentSummariesType, Json.stringify(Json.toJson(getAgentSummaryList)))
        .flatMap(replyMessage => parse[Seq[AgentSummary]](replyMessage.messageText))

  def getLegalStatuses(getLegalStatuses: GetLegalStatuses): IO[Seq[LegalStatus]] =
    logger.info(s"Requesting all of the legal status summary") *>
      apiConnector
        .handle(MessageType.GetLegalStatusesType, Json.stringify(Json.toJson(getLegalStatuses)))
        .flatMap(replyMessage => parse[Seq[LegalStatus]](replyMessage.messageText))

  private def parse[A : Reads](messageText: String): IO[A] =
    IO.fromOption(
      Json.parse(messageText).validate[A].asOpt
    )(CannotParseReply(messageText))

  private case class CannotParseReply(reply: String) extends Exception(
        s"""can't parse reply, got:
           |$reply
           |""".stripMargin
      )

}
