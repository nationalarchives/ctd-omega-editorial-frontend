import cats.effect.IO
import play.api.inject.ApplicationLifecycle
import support.ApiConnectorMonitoring
import uk.gov.nationalarchives.omega.editorial.config.Config
import uk.gov.nationalarchives.omega.editorial.connectors.ApiConnector
import uk.gov.nationalarchives.omega.editorial.models.{ EditSet, EditSetRecord, GetEditSet, GetEditSetRecord, UpdateEditSetRecord, UpdateResponseStatus }

import javax.inject.{ Inject, Singleton }

@Singleton
class MonitoredApiConnector @Inject() (
  config: Config,
  lifecycle: ApplicationLifecycle
) extends ApiConnector(config, lifecycle) with ApiConnectorMonitoring {

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
