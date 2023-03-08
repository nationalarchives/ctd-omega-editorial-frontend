import play.api.libs.json.Json
import uk.gov.nationalarchives.omega.editorial.connectors.ApiConnector.SID
import uk.gov.nationalarchives.omega.editorial.models.UpdateEditSetRecord

import java.time.LocalDateTime

class UpdateEditSetRecordISpec extends BaseRequestReplyServiceISpec {

  override val serviceId: String = SID.UpdateEditSetRecord.value

  "The service to get an Edit Set Record by OCI, will" - {
    "succeed, when we make" - {
      "a single request" - {
        "for a known Edit Set" - {
          "and a known Record" in { requestReplyHandler =>
            val request = generateRequestAsJsonString("1", "COAL.2022.V4RJW.P")

            val result = sendRequest(requestReplyHandler, request)

            result.asserting(
              _ mustBe Json.stringify(
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

            val result = sendRequest(requestReplyHandler, request)

            result.asserting(
              _ mustBe Json.stringify(
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

      val result1 = sendRequest(requestReplyHandler, request1)
      val result2 = sendRequest(requestReplyHandler, request2)

      result1.asserting(
        _ mustBe Json.stringify(
          Json.parse(
            """{
              |  "status":"success",
              |  "message":"Successfully updated record with OCI [COAL.2022.V1RJW.P]"
              |}""".stripMargin
          )
        )
      ) *>
        result2.asserting(
          _ mustBe Json.stringify(
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
          timestamp = LocalDateTime.now()
        )
      )
    )

}
