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

package uk.gov.nationalarchives.omega.editorial.connectors

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import play.api.inject.ApplicationLifecycle
import play.api.libs.json.{ Json, Reads }
import uk.gov.nationalarchives.omega.editorial.config.Config
import uk.gov.nationalarchives.omega.editorial.models._

import javax.inject.{ Inject, Singleton }

@Singleton
class ApiConnector @Inject() (
  config: Config,
  lifecycle: ApplicationLifecycle
) {
  import ApiConnector._

  private implicit val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]
  private val requestQueueName = "request-general"
  private val replyQueueName = "omega-editorial-web-application-instance-1"
  private lazy val (client, closer): (JmsRequestReplyClient[IO], IO[Unit]) = createClientAndCloser.unsafeRunSync()
  private lazy val handler: RequestReplyHandler = RequestReplyHandler(client)

  def getEditSet(getEditSet: GetEditSet): IO[Option[EditSet]] = {
    val requestBody = Json.stringify(Json.toJson(getEditSet))
    logger.info(s"Requesting edit set ${getEditSet.oci}...") *>
      handle(SID.GetEditSet, requestBody)
        .flatMap(parse[EditSet])
        .redeem(_ => None, Some.apply)
  }

  def getEditSetRecord(getEditSetRecord: GetEditSetRecord): IO[Option[EditSetRecord]] = {
    val requestBody = Json.stringify(Json.toJson(getEditSetRecord))
    logger.info(s"Requesting record ${getEditSetRecord.recordOci} from edit set ${getEditSetRecord.editSetOci}") *>
      handle(SID.GetEditSetRecord, requestBody)
        .flatMap(parse[EditSetRecord])
        .redeem(_ => None, Some.apply)
  }

  def updateEditSetRecord(updateEditSetRecord: UpdateEditSetRecord): IO[UpdateResponseStatus] = {
    val requestBody = Json.stringify(Json.toJson(updateEditSetRecord))
    logger.info(
      s"Requesting update of edit set record ${updateEditSetRecord.recordOci}: \n${pprint.apply(updateEditSetRecord)}\n"
    ) *>
      handle(SID.UpdateEditSetRecord, requestBody).flatMap(parse[UpdateResponseStatus])
  }

  def getPlacesOfDeposit(getPlacesOfDeposit: GetPlacesOfDeposit): IO[Seq[PlaceOfDeposit]] =
    logger.info(s"Requesting all of the places of deposit") *>
      handle(SID.GetPlacesOfDeposit, Json.stringify(Json.toJson(getPlacesOfDeposit)))
        .flatMap(parse[Seq[PlaceOfDeposit]])

  def getPersons(getPersons: GetPersons): IO[Seq[Person]] =
    logger.info(s"Requesting all of the persons") *>
      handle(SID.GetPersons, Json.stringify(Json.toJson(getPersons)))
        .flatMap(parse[Seq[Person]])

  def getCorporateBodies(getCorporateBodies: GetCorporateBodies): IO[Seq[CorporateBody]] =
    logger.info(s"Requesting all of the corporate bodies") *>
      handle(SID.GetCorporateBodies, Json.stringify(Json.toJson(getCorporateBodies)))
        .flatMap(parse[Seq[CorporateBody]])

  def getLegalStatuses(getLegalStatuses: GetLegalStatuses): IO[Seq[LegalStatus]] =
    logger.info(s"Requesting all of the legal status summary") *>
      handle(SID.GetLegalStatuses, Json.stringify(Json.toJson(getLegalStatuses)))
        .flatMap(parse[Seq[LegalStatus]])

  private def createClientAndCloser: IO[(JmsRequestReplyClient[IO], IO[Unit])] =
    registerStopHook() *>
      logger.info(s"Attempting to subscribe to $replyQueueName...") *>
      JmsRequestReplyClient.createForSqs[IO](config.broker, config.credentials)(replyQueueName).allocated

  private def registerStopHook(): IO[Unit] = IO.delay {
    lifecycle.addStopHook { () =>
      closer.unsafeToFuture()
    }
  }

  private def handle(sid: SID, requestBody: String): IO[String] =
    handler.handle(
      requestQueueName,
      requestMessage = RequestMessage(requestBody, sid.value)
    )

  private def parse[A : Reads](messageText: String): IO[A] =
    IO.fromOption(
      Json.parse(messageText).validate[A].asOpt
    )(CannotParseEditSetResponse(messageText))

}

object ApiConnector {

  sealed abstract class SID(val value: String) {

    def matches(sid: String): Boolean =
      sid.trim.equalsIgnoreCase(this.value)

  }

  object SID {

    case object GetEditSet extends SID("OSGEES001")
    case object GetEditSetRecord extends SID("OSGESR001")
    case object UpdateEditSetRecord extends SID("OSUESR001")
    case object GetLegalStatuses extends SID("OSLISALS001")
    // TODO: The real SID will be provided by Adam once he figures out the schema.
    case object GetPlacesOfDeposit extends SID("OSGPOD001")
    case object GetPersons extends SID("OSGPER001")
    case object GetCorporateBodies extends SID("OSGCBY001")

  }

  private case class CannotParseEditSetResponse(response: String) extends Exception(
        s"""can't parse edit set, got:
           |$response
           |""".stripMargin
      )

}
