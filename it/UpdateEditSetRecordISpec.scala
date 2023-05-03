import play.api.libs.json.Json
import uk.gov.nationalarchives.omega.editorial.connectors.{ ApiConnector, MessageType }
import uk.gov.nationalarchives.omega.editorial.models.UpdateEditSetRecord

import java.time.{ LocalDate, LocalDateTime, Month }
class UpdateEditSetRecordISpec extends BaseRequestReplyServiceISpec {

  override val messageType: String = MessageType.UpdateEditSetRecordType.value

  "The service to get an Edit Set Record by OCI, will" - {
    "succeed, when we make" - {
      "a single request" - {
        "for a known Edit Set" - {
          "and a known Record" in { requestReplyHandler =>
            val request = generateRequestAsJsonString("1", "COAL.2022.V4RJW.P")

            val result = sendRequest(requestReplyHandler, request, ApiConnector.applicationId, messageType)

            result.asserting(
              _.messageText mustBe Json.stringify(
                Json.parse(
                  """{
                    |  "status":"success",
                    |  "message":"Successfully updated record with OCI [COAL.2022.V4RJW.P]"
                    |}""".stripMargin
                )
              )
            )
          }

        }
        "for an unknown Edit Set" - {
          "but a known Record" in { requestReplyHandler =>
            val request = generateRequestAsJsonString("88", "COAL.2022.V4RJW.P")

            val result = sendRequest(requestReplyHandler, request, ApiConnector.applicationId, messageType)

            result.asserting(
              _.messageText mustBe Json.stringify(
                Json.parse(
                  """{
                    |  "status":"success",
                    |  "message":"Successfully updated record with OCI [COAL.2022.V4RJW.P]"
                    |}""".stripMargin
                )
              )
            )
          }
        }
      }
    }
    "multiple requests, each for a known (but different) record" in { requestReplyHandler =>
      val request1 = generateRequestAsJsonString("1", "COAL.2022.V1RJW.P")
      val request2 = generateRequestAsJsonString("1", "COAL.2022.V4RJW.P")

      val result1 = sendRequest(requestReplyHandler, request1, ApiConnector.applicationId, messageType)
      val result2 = sendRequest(requestReplyHandler, request2, ApiConnector.applicationId, messageType)

      result1.asserting(
        _.messageText mustBe Json.stringify(
          Json.parse(
            """{
              |  "status":"success",
              |  "message":"Successfully updated record with OCI [COAL.2022.V1RJW.P]"
              |}""".stripMargin
          )
        )
      ) *>
        result2.asserting(
          _.messageText mustBe Json.stringify(
            Json.parse(
              """{
                |  "status":"success",
                |  "message":"Successfully updated record with OCI [COAL.2022.V4RJW.P]"
                |}""".stripMargin
            )
          )
        )

    }
  }

  /** Please note that it's not currently possible to have the failure case where no record exists for the requested
    * OCI; not only will it never return, it will block any further requests, however valid.
    *
    * This applies to any failure case, in fact.
    */

  private def generateRequestAsJsonString(editSetOci: String, recordOci: String): String =
    Json.stringify(
      Json.toJson(
        UpdateEditSetRecord(
          editSetOci = editSetOci,
          recordOci = recordOci,
          timestamp = LocalDateTime.now(),
          fields = UpdateEditSetRecord.Fields(
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
            creatorIDs = Seq("8R6", "92W")
          )
        )
      )
    )

}
