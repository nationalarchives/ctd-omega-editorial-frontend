import cats.effect.IO
import support.MessagingServiceMonitoring
import uk.gov.nationalarchives.omega.editorial.connectors.ApiConnector
import uk.gov.nationalarchives.omega.editorial.models._
import uk.gov.nationalarchives.omega.editorial.services.MessagingService

import javax.inject.{Inject, Singleton}

@Singleton
class MonitoredMessagingService @Inject()(
  apiConnector: ApiConnector
) extends MessagingService(apiConnector) with MessagingServiceMonitoring {

  override def getEditSet(getEditSetRequest: GetEditSet): IO[Option[EditSet]] = {
    record(getEditSetRequest)
    super.getEditSet(getEditSetRequest)
  }

  override def getEditSetRecord(getEditSetRecordRequest: GetEditSetRecord): IO[Option[EditSetRecord]] = {
    record(getEditSetRecordRequest)
    super.getEditSetRecord(getEditSetRecordRequest)
  }

  override def updateEditSetRecord(updateEditSetRecordRequest: UpdateEditSetRecord): IO[UpdateResponseStatus] = {
    record(updateEditSetRecordRequest)
    super.updateEditSetRecord(updateEditSetRecordRequest)
  }

}
