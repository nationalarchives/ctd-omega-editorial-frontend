import cats.effect.unsafe.implicits.global
import org.scalatest.prop.TableDrivenPropertyChecks.forAll
import org.scalatest.prop.Tables.Table

import uk.gov.nationalarchives.omega.editorial.connectors.ApiConnector
import uk.gov.nationalarchives.omega.editorial.{ editSetRecords, editSets }

class ApiConnectorISpec extends BaseISpec {

  private val idOfExistingEditSet = "1"

  lazy val apiConnector: ApiConnector = app.injector.instanceOf[ApiConnector]

  "when a request for an edit set is made" must {
    s"get an edit set for id: $idOfExistingEditSet" in {
      val editSetResponse = apiConnector.getEditSet(idOfExistingEditSet).unsafeRunSync()

      editSetResponse mustBe editSets.editSet1
    }
  }

  "when a request for an editSetRecord is made" must {
    lazy val editSetRecordTable = Table(
      "record oci"         -> "record",
      "COAL.2022.V1RJW.P"  -> editSetRecords.getEditSetRecordByOCI("COAL.2022.V1RJW.P"),
      "COAL.2022.V2RJW"    -> editSetRecords.getEditSetRecordByOCI("COAL.2022.V2RJW"),
      "COAL.2022.V3RJW.P"  -> editSetRecords.getEditSetRecordByOCI("COAL.2022.V3RJW.P"),
      "COAL.2022.V4RJW.P"  -> editSetRecords.getEditSetRecordByOCI("COAL.2022.V4RJW.P"),
      "COAL.2022.V5RJW.P"  -> editSetRecords.getEditSetRecordByOCI("COAL.2022.V5RJW.P"),
      "COAL.2022.V6RJW.P"  -> editSetRecords.getEditSetRecordByOCI("COAL.2022.V6RJW.P"),
      "COAL.2022.V7RJW.P"  -> editSetRecords.getEditSetRecordByOCI("COAL.2022.V7RJW.P"),
      "COAL.2022.V8RJW.P"  -> editSetRecords.getEditSetRecordByOCI("COAL.2022.V8RJW.P"),
      "COAL.2022.V9RJW.P"  -> editSetRecords.getEditSetRecordByOCI("COAL.2022.V9RJW.P"),
      "COAL.2022.V10RJW.P" -> editSetRecords.getEditSetRecordByOCI("COAL.2022.V10RJW.P"),
      "COAL.2022.V11RJW.P" -> editSetRecords.getEditSetRecordByOCI("COAL.2022.V11RJW.P"),
      "COAL.2022.V12RJW.P" -> editSetRecords.getEditSetRecordByOCI("COAL.2022.V12RJW.P")
    )

    forAll(editSetRecordTable) { (oci, expectedResult) =>
      s"get an edit set record for oci: $oci" in {
        val editSetRecordRespose =
          apiConnector.getEditSetRecord(idOfExistingEditSet, oci).unsafeRunSync()

        editSetRecordRespose mustBe expectedResult
      }
    }
  }

}
