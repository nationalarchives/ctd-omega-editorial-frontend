import cats.effect.unsafe.implicits.global
import org.scalatest.prop.TableDrivenPropertyChecks.forAll
import org.scalatest.prop.Tables.Table
import uk.gov.nationalarchives.omega.editorial.models
import uk.gov.nationalarchives.omega.editorial.models.{ GetEditSet, GetEditSetRecord, UpdateEditSetRecord, UpdateResponseStatus }
import uk.gov.nationalarchives.omega.editorial.services.MessagingService

import java.time.{ LocalDate, LocalDateTime, Month }

class ApiConnectorISpec extends BaseISpec {

  lazy val messagingService: MessagingService = app.injector.instanceOf[MessagingService]

  "when a request for an edit set is made" must {

    val getEditSetRequest = GetEditSet(idOfExistingEditSet, testTimeProvider.now())

    s"get an edit set for id: $idOfExistingEditSet" in {
      val editSetResponse =
        messagingService.getEditSet(getEditSetRequest).unsafeRunSync()

      editSetResponse mustBe getEditSet(idOfExistingEditSet)
    }
  }

  "when a request for an editSetRecord is made" must {
    val ocis = Seq(
      "COAL.2022.V1RJW.P",
      "COAL.2022.V2RJW",
      "COAL.2022.V3RJW.P",
      "COAL.2022.V4RJW.P",
      "COAL.2022.V5RJW.P",
      "COAL.2022.V6RJW.P",
      "COAL.2022.V7RJW.P",
      "COAL.2022.V8RJW.P",
      "COAL.2022.V9RJW.P",
      "COAL.2022.V10RJW.P",
      "COAL.2022.V11RJW.P",
      "COAL.2022.V12RJW.P"
    )
    val editSetRecordTable = Table(
      "record oci" -> "record",
      ocis.map { oci =>
        oci -> getEditSetRecord(oci)
      }: _*
    )

    forAll(editSetRecordTable) { (oci, expectedResult) =>
      s"get an edit set record for oci: $oci" in {
        messagingService
          .getEditSetRecord(GetEditSetRecord(idOfExistingEditSet, oci, testTimeProvider.now()))
          .unsafeRunSync() mustBe expectedResult
      }
    }

  }
  "when a request to update an Edit Set Record is made" in {

    val response = messagingService.updateEditSetRecord(
      UpdateEditSetRecord(
        editSetOci = "1",
        recordOci = "COAL.2022.V1RJW.P",
        timestamp = LocalDateTime.now(),
        fields = models.UpdateEditSetRecord.Fields(
          description = "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (B)",
          coveringDates = "1962",
          formerReferenceDepartment = "MR 193 (9)",
          formerReferencePro = "MPS 4/1",
          startDate = LocalDate.of(1962, Month.JANUARY, 1),
          endDate = LocalDate.of(1962, Month.DECEMBER, 31),
          legalStatusId = "ref.1",
          placeOfDepositID = "1",
          note = "A note about COAL.2022.V1RJW.P.",
          background = "Photo was taken by a daughter of one of the coal miners who used them.",
          custodialHistory = "Files originally created by successor or predecessor departments for COAL",
          relatedMaterial = Seq(
            UpdateEditSetRecord.Fields.MaterialReference(
              linkHref = None,
              linkText = None,
              description =
                Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
            ),
            UpdateEditSetRecord.Fields.MaterialReference(
              linkHref = Some("#;"),
              linkText = Some("COAL 80/80/3"),
              description = None
            ),
            UpdateEditSetRecord.Fields.MaterialReference(
              linkHref = Some("#;"),
              linkText = Some("COAL 80/80/2"),
              description =
                Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
            )
          ),
          separatedMaterial = Seq(
            UpdateEditSetRecord.Fields.MaterialReference(
              linkHref = Some("#;"),
              linkText = Some("COAL 80/80/5"),
              description = None
            ),
            UpdateEditSetRecord.Fields.MaterialReference(
              linkHref = Some("#;"),
              linkText = Some("COAL 80/80/6"),
              description = None
            ),
            UpdateEditSetRecord.Fields.MaterialReference(
              linkHref = Some("#;"),
              linkText = Some("COAL 80/80/7"),
              description = None
            )
          ),
          creatorIDs = Seq.empty
        )
      )
    )

    response.unsafeRunSync() mustBe UpdateResponseStatus(
      "success",
      "Successfully updated record with OCI [COAL.2022.V1RJW.P]"
    )

  }
}
