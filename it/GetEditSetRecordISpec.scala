/*
 * Copyright (c) 2023 The National Archives
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

import play.api.libs.json.Json
import uk.gov.nationalarchives.omega.editorial.connectors.{ ApiConnector, MessageType }
import uk.gov.nationalarchives.omega.editorial.models.GetEditSetRecord

import java.time.LocalDateTime

class GetEditSetRecordISpec extends BaseRequestReplyServiceISpec {

  override val messageType: String = MessageType.GetEditSetRecordType.value

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
                    |  "ccr" : "COAL 80/80/4",
                    |  "oci" : "COAL.2022.V4RJW.P",
                    |  "scopeAndContent" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (D)",
                    |  "coveringDates" : "1961",
                    |  "formerReferenceDepartment" : "",
                    |  "formerReferencePro" : "CAB 172",
                    |  "startDateDay" : "1",
                    |  "startDateMonth" : "1",
                    |  "startDateYear" : "1961",
                    |  "endDateDay" : "31",
                    |  "endDateMonth" : "12",
                    |  "endDateYear" : "1961",
                    |  "legalStatusID": "http://cat.nationalarchives.gov.uk/public-record",
                    |  "placeOfDepositID" : "http://cat.nationalarchives.gov.uk/agent.S7",
                    |  "note": "",
                    |  "background": "",
                    |  "custodialHistory" : "",
                    |  "relatedMaterial":[],
                    |  "separatedMaterial":[],
                    |  "creatorIDs": []
                    |} """.stripMargin
                )
              )
            )
          }

        }
        "for an unknown Edit Set" - {
          "but a known Record" in { requestReplyHandler =>
            val request = generateRequestAsJsonString("88", "COAL.2022.V1RJW.P")

            val result = sendRequest(requestReplyHandler, request, ApiConnector.applicationId, messageType)

            result.asserting(_.messageText mustBe Json.stringify(Json.parse("""{
              "ccr" : "COAL 80/80/1",
              "oci" : "COAL.2022.V1RJW.P",
              "scopeAndContent" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (B)",
              "coveringDates" : "1962",
              "formerReferenceDepartment" : "MR 193 (9)",
              "formerReferencePro" : "MPS 4/1",
              "startDateDay" : "1",
              "startDateMonth" : "1",
              "startDateYear" : "1962",
              "endDateDay" : "31",
              "endDateMonth" : "12",
              "endDateYear" : "1962",
              "legalStatusID": "http://cat.nationalarchives.gov.uk/public-record",
              "placeOfDepositID" : "http://cat.nationalarchives.gov.uk/agent.S7",
              "note": "A note about COAL.2022.V1RJW.P.",
              "background": "Photo was taken by a daughter of one of the coal miners who used them.",
              "custodialHistory" : "Files originally created by successor or predecessor departments for COAL",
              "relatedMaterial" : [
                {
                  "description" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths."
                },
                {
                  "linkHref" : "#;",
                  "linkText" : "COAL 80/80/3"
                },
                {
                  "linkHref" : "#;",
                  "linkText" : "COAL 80/80/2",
                  "description" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths."
                }
              ],
              "separatedMaterial" : [
                {
                  "linkHref" : "#;",
                  "linkText" : "COAL 80/80/5"
                },
                {
                  "linkHref" : "#;",
                  "linkText" : "COAL 80/80/6"
                },
                {
                  "linkHref" : "#;",
                  "linkText" : "COAL 80/80/7"
                }
              ],
              "background": "Photo was taken by a daughter of one of the coal miners who used them.",
              "creatorIDs": ["http://cat.nationalarchives.gov.uk/agent.3LG", "http://cat.nationalarchives.gov.uk/agent.N6L"]
            } """.stripMargin)))
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
                |  "ccr" : "COAL 80/80/1",
                |  "oci" : "COAL.2022.V1RJW.P",
                |  "scopeAndContent" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (B)",
                |  "coveringDates" : "1962",
                |  "formerReferenceDepartment" : "MR 193 (9)",
                |  "formerReferencePro" : "MPS 4/1",
                |  "startDateDay" : "1",
                |  "startDateMonth" : "1",
                |  "startDateYear" : "1962",
                |  "endDateDay" : "31",
                |  "endDateMonth" : "12",
                |  "endDateYear" : "1962",
                |  "legalStatusID": "http://cat.nationalarchives.gov.uk/public-record",
                |  "placeOfDepositID" : "http://cat.nationalarchives.gov.uk/agent.S7",
                |  "note": "A note about COAL.2022.V1RJW.P.",
                |  "background": "Photo was taken by a daughter of one of the coal miners who used them.",
                |  "custodialHistory" : "Files originally created by successor or predecessor departments for COAL",
                |  "relatedMaterial" : [
                |    {
                |      "description" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths."
                |    },
                |    {
                |      "linkHref" : "#;",
                |      "linkText" : "COAL 80/80/3"
                |    },
                |    {
                |      "linkHref" : "#;",
                |      "linkText" : "COAL 80/80/2",
                |      "description" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths."
                |    }
                |  ],
                |  "separatedMaterial" : [
                |    {
                |      "linkHref" : "#;",
                |      "linkText" : "COAL 80/80/5"
                |    },
                |    {
                |      "linkHref" : "#;",
                |      "linkText" : "COAL 80/80/6"
                |    },
                |    {
                |      "linkHref" : "#;",
                |      "linkText" : "COAL 80/80/7"
                |    }
                |  ],
                |  "background": "Photo was taken by a daughter of one of the coal miners who used them.",
                |  "creatorIDs": ["http://cat.nationalarchives.gov.uk/agent.3LG", "http://cat.nationalarchives.gov.uk/agent.N6L"]
                |} """.stripMargin
            )
          )
        ) *>
          result2.asserting(
            _.messageText mustBe Json.stringify(
              Json.parse(
                """{
                  |  "ccr" : "COAL 80/80/4",
                  |  "oci" : "COAL.2022.V4RJW.P",
                  |  "scopeAndContent" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (D)",
                  |  "coveringDates" : "1961",
                  |  "formerReferenceDepartment" : "",
                  |  "formerReferencePro" : "CAB 172",
                  |  "startDateDay" : "1",
                  |  "startDateMonth" : "1",
                  |  "startDateYear" : "1961",
                  |  "endDateDay" : "31",
                  |  "endDateMonth" : "12",
                  |  "endDateYear" : "1961",
                  |  "legalStatusID": "http://cat.nationalarchives.gov.uk/public-record",
                  |  "placeOfDepositID" : "http://cat.nationalarchives.gov.uk/agent.S7",
                  |  "note": "",
                  |  "background": "",
                  |  "custodialHistory" : "",
                  |  "relatedMaterial":[],
                  |  "separatedMaterial":[],
                  |  "creatorIDs": []
                  |} """.stripMargin
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

  }

  private def generateRequestAsJsonString(editSetOci: String, recordOci: String): String =
    Json.stringify(
      Json.toJson(
        GetEditSetRecord(
          editSetOci = editSetOci,
          recordOci = recordOci,
          timestamp = LocalDateTime.now()
        )
      )
    )

}
