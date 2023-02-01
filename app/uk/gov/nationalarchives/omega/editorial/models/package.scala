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

package uk.gov.nationalarchives.omega.editorial

import play.api.libs.json._
import uk.gov.nationalarchives.omega.editorial.models._

package object editSetRecords {

  private val editSetRecord1: EditSetRecord = Json
    .parse(
      """{
        |  "ccr" : "COAL 80/80/1",
        |  "oci" : "COAL.2022.V1RJW.P",
        |  "scopeAndContent" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (B)",
        |  "coveringDates" : "1962",
        |  "formerReferenceDepartment" : "",
        |  "formerReferencePro" : "",
        |  "startDateDay" : "1",
        |  "startDateMonth" : "1",
        |  "startDateYear" : "1962",
        |  "endDateDay" : "31",
        |  "endDateMonth" : "12",
        |  "endDateYear" : "1962",
        |  "legalStatusID": "ref.1",
        |  "placeOfDepositID" : "1",
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
        |  "creatorIDs": ["48N", "92W"]
        |} """.stripMargin
    )
    .as[EditSetRecord]

  private val editSetRecord2: EditSetRecord = Json
    .parse(
      """{
        |  "ccr" : "COAL 80/80/2",
        |  "oci" : "COAL.2022.V2RJW.P",
        |  "scopeAndContent" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (A)",
        |  "coveringDates" : "1966",
        |  "formerReferenceDepartment" : "",
        |  "formerReferencePro" : "",
        |  "startDateDay" : "1",
        |  "startDateMonth" : "1",
        |  "startDateYear" : "1966",
        |  "endDateDay" : "31",
        |  "endDateMonth" : "12",
        |  "endDateYear" : "1966",
        |  "legalStatusID": "ref.2",
        |  "placeOfDepositID" : "",
        |  "note": "A note about COAL.2022.V2RJW.P.",
        |  "background": "",
        |  "custodialHistory" : "",
        |  "creatorIDs": ["515", "XXX"]
        |} """.stripMargin
    )
    .as[EditSetRecord]

  private val editSetRecord3: EditSetRecord = Json
    .parse(
      """{
        |  "ccr" : "COAL 80/80/3",
        |  "oci" : "COAL.2022.V3RJW.P",
        |  "scopeAndContent" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (C)",
        |  "coveringDates" : "1964",
        |  "formerReferenceDepartment" : "",
        |  "formerReferencePro" : "",
        |  "startDateDay" : "1",
        |  "startDateMonth" : "1",
        |  "startDateYear" : "1964",
        |  "endDateDay" : "31",
        |  "endDateMonth" : "12",
        |  "endDateYear" : "1964",
        |  "legalStatusID": "",
        |  "placeOfDepositID" : "6",
        |  "note": "",
        |  "background": "Photo was taken by a son of one of the coal miners who used them.",
        |  "custodialHistory" : "",
        |  "creatorIDs": ["8R6", "92W", "8R6"]
        |} """.stripMargin
    )
    .as[EditSetRecord]

  private val editSetRecord4: EditSetRecord = Json
    .parse(
      """{
        |  "ccr" : "COAL 80/80/4",
        |  "oci" : "COAL.2022.V4RJW.P",
        |  "scopeAndContent" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
        |  "coveringDates" : "1960",
        |  "formerReferenceDepartment" : "",
        |  "formerReferencePro" : "",
        |  "startDateDay" : "1",
        |  "startDateMonth" : "1",
        |  "startDateYear" : "1960",
        |  "endDateDay" : "31",
        |  "endDateMonth" : "12",
        |  "endDateYear" : "1960",
        |  "legalStatusID": "ref.1",
        |  "placeOfDepositID" : "1",
        |  "note": "",
        |  "background": "",
        |  "custodialHistory" : "",
        |  "creatorIDs": []
        |} """.stripMargin
    )
    .as[EditSetRecord]

  private val editSetRecord5: EditSetRecord = Json
    .parse(
      """{
        |  "ccr" : "COAL 80/80/5",
        |  "oci" : "COAL.2022.V5RJW.P",
        |  "scopeAndContent" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
        |  "coveringDates" : "1960",
        |  "formerReferenceDepartment" : "",
        |  "formerReferencePro" : "",
        |  "startDateDay" : "1",
        |  "startDateMonth" : "1",
        |  "startDateYear" : "1960",
        |  "endDateDay" : "31",
        |  "endDateMonth" : "12",
        |  "endDateYear" : "1960",
        |  "legalStatusID": "ref.1",
        |  "placeOfDepositID" : "1",
        |  "note": "Need to check copyright info.",
        |  "background": "",
        |  "custodialHistory" : "",
        |  "creatorIDs": ["46F","48N","46F"]
        |} """.stripMargin
    )
    .as[EditSetRecord]

  private val editSetRecord6: EditSetRecord = Json
    .parse(
      """{
        |  "ccr" : "COAL 80/80/6",
        |  "oci" : "COAL.2022.V6RJW.P",
        |  "scopeAndContent" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
        |  "coveringDates" : "1960",
        |  "formerReferenceDepartment" : "",
        |  "formerReferencePro" : "",
        |  "startDateDay" : "1",
        |  "startDateMonth" : "1",
        |  "startDateYear" : "1960",
        |  "endDateDay" : "31",
        |  "endDateMonth" : "12",
        |  "endDateYear" : "1960",
        |  "legalStatusID": "ref.1",
        |  "placeOfDepositID" : "1",
        |  "note": "",
        |  "background": "",
        |  "custodialHistory" : "",
        |  "creatorIDs": ["4", "FKS"]
        |} """.stripMargin
    )
    .as[EditSetRecord]

  private val editSetRecord7: EditSetRecord = Json
    .parse(
      """{
        |  "ccr" : "COAL 80/80/7",
        |  "oci" : "COAL.2022.V7RJW.P",
        |  "scopeAndContent" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
        |  "coveringDates" : "1960",
        |  "formerReferenceDepartment" : "",
        |  "formerReferencePro" : "",
        |  "startDateDay" : "1",
        |  "startDateMonth" : "1",
        |  "startDateYear" : "1960",
        |  "endDateDay" : "31",
        |  "endDateMonth" : "12",
        |  "endDateYear" : "1960",
        |  "legalStatusID": "ref.1",
        |  "placeOfDepositID" : "1",
        |  "note": "",
        |  "background": "",
        |  "custodialHistory" : "",
        |  "creatorIDs": ["48N", "92W"]
        |} """.stripMargin
    )
    .as[EditSetRecord]

  private val editSetRecord8: EditSetRecord = Json
    .parse(
      """{
        |  "ccr" : "COAL 80/80/8",
        |  "oci" : "COAL.2022.V8RJW.P",
        |  "scopeAndContent" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
        |  "coveringDates" : "1960",
        |  "formerReferenceDepartment" : "",
        |  "formerReferencePro" : "",
        |  "startDateDay" : "1",
        |  "startDateMonth" : "1",
        |  "startDateYear" : "1960",
        |  "endDateDay" : "31",
        |  "endDateMonth" : "12",
        |  "endDateYear" : "1960",
        |  "legalStatusID": "ref.1",
        |  "placeOfDepositID" : "1",
        |  "note": "",
        |  "background": "",
        |  "custodialHistory" : "",
        |  "creatorIDs": ["48N", "46F", "8R6"]
        |} """.stripMargin
    )
    .as[EditSetRecord]

  private val editSetRecord9: EditSetRecord = Json
    .parse(
      """{
        |  "ccr" : "COAL 80/80/9",
        |  "oci" : "COAL.2022.V9RJW.P",
        |  "scopeAndContent" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
        |  "coveringDates" : "1960",
        |  "formerReferenceDepartment" : "",
        |  "formerReferencePro" : "",
        |  "startDateDay" : "1",
        |  "startDateMonth" : "1",
        |  "startDateYear" : "1960",
        |  "endDateDay" : "31",
        |  "endDateMonth" : "12",
        |  "endDateYear" : "1960",
        |  "legalStatusID": "ref.1",
        |  "placeOfDepositID" : "1",
        |  "note": "",
        |  "background": "",
        |  "custodialHistory" : "",
        |  "creatorIDs": ["2TK", "39T", "ST"]
        |} """.stripMargin
    )
    .as[EditSetRecord]

  private val editSetRecord10: EditSetRecord = Json
    .parse(
      """{
        |  "ccr" : "COAL 80/80/10",
        |  "oci" : "COAL.2022.V10RJW.P",
        |  "scopeAndContent" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
        |  "coveringDates" : "1960",
        |  "formerReferenceDepartment" : "",
        |  "formerReferencePro" : "",
        |  "startDateDay" : "1",
        |  "startDateMonth" : "1",
        |  "startDateYear" : "1960",
        |  "endDateDay" : "31",
        |  "endDateMonth" : "12",
        |  "endDateYear" : "1960",
        |  "legalStatusID": "ref.1",
        |  "placeOfDepositID" : "1",
        |  "note": "",
        |  "background": "",
        |  "separatedMaterial" : [
        |    {
        |      "description" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths."
        |    }
        |  ],
        |  "custodialHistory" : "",
        |  "creatorIDs": ["XXX"]
        |} """.stripMargin
    )
    .as[EditSetRecord]

  private val editSetRecord11: EditSetRecord = Json
    .parse(
      """{
        |  "ccr" : "COAL 80/80/11",
        |  "oci" : "COAL.2022.V11RJW.P",
        |  "scopeAndContent" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
        |  "coveringDates" : "1960",
        |  "formerReferenceDepartment" : "",
        |  "formerReferencePro" : "",
        |  "startDateDay" : "1",
        |  "startDateMonth" : "1",
        |  "startDateYear" : "1960",
        |  "endDateDay" : "31",
        |  "endDateMonth" : "12",
        |  "endDateYear" : "1960",
        |  "legalStatusID": "ref.1",
        |  "placeOfDepositID" : "1",
        |  "note": "",
        |  "background": "",
        |  "separatedMaterial" : [
        |    {
        |      "description" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths."
        |    },
        |    {
        |      "linkHref" : "#;",
        |      "linkText" : "COAL 80/80/5"
        |    }
        |  ],
        |  "custodialHistory" : "",
        |  "creatorIDs": ["8R6"]
        |} """.stripMargin
    )
    .as[EditSetRecord]

  private val editSetRecord12: EditSetRecord = Json
    .parse(
      """{
        |  "ccr" : "COAL 80/80/12",
        |  "oci" : "COAL.2022.V12RJW.P",
        |  "scopeAndContent" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
        |  "coveringDates" : "1960",
        |  "formerReferenceDepartment" : "",
        |  "formerReferencePro" : "",
        |  "startDateDay" : "1",
        |  "startDateMonth" : "1",
        |  "startDateYear" : "1960",
        |  "endDateDay" : "31",
        |  "endDateMonth" : "12",
        |  "endDateYear" : "1960",
        |  "legalStatusID": "ref.1",
        |  "placeOfDepositID" : "1",
        |  "note": "",
        |  "background": "",
        |  "separatedMaterial" : [
        |    {
        |      "description" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths."
        |    },
        |    {
        |      "linkHref" : "#;",
        |      "linkText" : "COAL 80/80/5"
        |    },
        |    {
        |      "linkHref" : "#;",
        |      "linkText" : "COAL 80/80/8",
        |      "description" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths."
        |    }
        |  ],
        |  "custodialHistory" : "",
        |  "creatorIDs": ["8R6","92W", ""]
        |} """.stripMargin
    )
    .as[EditSetRecord]

  private val editSetRecord13: EditSetRecord = Json
    .parse(
      """{
        |  "ccr" : "COAL 20/80/13",
        |  "oci" : "COAL.2022.V13RJW",
        |  "scopeAndContent" : "Coal Mining Memorial Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
        |  "coveringDates" : "2000",
        |  "formerReferenceDepartment" : "",
        |  "formerReferencePro" : "",
        |  "startDateDay" : "1",
        |  "startDateMonth" : "1",
        |  "startDateYear" : "2000",
        |  "endDateDay" : "31",
        |  "endDateMonth" : "12",
        |  "endDateYear" : "2000",
        |  "legalStatusID": "ref.4",
        |  "placeOfDepositID" : "1",
        |  "note": "",
        |  "background": "",
        |  "custodialHistory" : "",
        |  "creatorIDs": ["8R6"]
        |} """.stripMargin
    )
    .as[EditSetRecord]

  private val originalEditSetRecordMap: Map[String, EditSetRecord] = Map(
    "COAL.2022.V1RJW.P"  -> editSetRecord1,
    "COAL.2022.V2RJW.P"  -> editSetRecord2,
    "COAL.2022.V3RJW.P"  -> editSetRecord3,
    "COAL.2022.V4RJW.P"  -> editSetRecord4,
    "COAL.2022.V5RJW.P"  -> editSetRecord5,
    "COAL.2022.V6RJW.P"  -> editSetRecord6,
    "COAL.2022.V7RJW.P"  -> editSetRecord7,
    "COAL.2022.V8RJW.P"  -> editSetRecord8,
    "COAL.2022.V9RJW.P"  -> editSetRecord9,
    "COAL.2022.V10RJW.P" -> editSetRecord10,
    "COAL.2022.V11RJW.P" -> editSetRecord11,
    "COAL.2022.V12RJW.P" -> editSetRecord12,
    "COAL.2022.V13RJW"   -> editSetRecord13
  )

  var editSetRecordMap: Map[String, EditSetRecord] = originalEditSetRecordMap

  def getEditSetRecordByOCI(oci: String): Option[EditSetRecord] =
    editSetRecordMap.get(oci)

  def saveEditSetRecord(editSetRecord: EditSetRecord) = {
    val editSetRecordOCI = editSetRecord.oci;
    editSetRecordMap -= editSetRecordOCI
    editSetRecordMap += (editSetRecordOCI -> editSetRecord)
  }

  def restoreOriginalRecords(): Unit = editSetRecordMap = originalEditSetRecordMap

}

package object editSets {
  val editSet1: EditSet = Json
    .parse(
      """{
        |  "name" : "COAL 80 Sample",
        |  "id" : "1",
        |  "entries" : [
        |    {
        |      "ccr" : "COAL 80/80/1",
        |      "oci" : "COAL.2022.V1RJW.P",
        |      "scopeAndContent" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (B)",
        |      "coveringDates" : "1962"
        |    },
        |    {
        |      "ccr" : "COAL 80/80/2",
        |      "oci" : "COAL.2022.V2RJW.P",
        |      "scopeAndContent" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (A)",
        |      "coveringDates" : "1966"
        |    },
        |    {
        |      "ccr" : "COAL 80/80/3",
        |      "oci" : "COAL.2022.V3RJW.P",
        |      "scopeAndContent" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (C)",
        |      "coveringDates" : "1964"
        |    },
        |    {
        |      "ccr" : "COAL 80/80/4",
        |      "oci" : "COAL.2022.V4RJW.P",
        |      "scopeAndContent" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (D)",
        |      "coveringDates" : "1961"
        |    },
        |    {
        |      "ccr" : "COAL 80/80/5",
        |      "oci" : "COAL.2022.V5RJW.P",
        |      "scopeAndContent" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (E)",
        |      "coveringDates" : "1963"
        |    },
        |    {
        |      "ccr" : "COAL 80/80/6",
        |      "oci" : "COAL.2022.V6RJW.P",
        |      "scopeAndContent" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (F)",
        |      "coveringDates" : "1965"
        |    },
        |    {
        |      "ccr" : "COAL 80/80/7",
        |      "oci" : "COAL.2022.V7RJW.P",
        |      "scopeAndContent" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (G)",
        |      "coveringDates" : "1967"
        |    },
        |    {
        |      "ccr" : "COAL 80/80/8",
        |      "oci" : "COAL.2022.V8RJW.P",
        |      "scopeAndContent" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (H)",
        |      "coveringDates" : "1969"
        |    },
        |    {
        |      "ccr" : "COAL 80/80/9",
        |      "oci" : "COAL.2022.V9RJW.P",
        |      "scopeAndContent" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (I)",
        |      "coveringDates" : "1971"
        |    },
        |    {
        |      "ccr" : "COAL 80/80/10",
        |      "oci" : "COAL.2022.V10RJW.P",
        |      "scopeAndContent" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (J)",
        |      "coveringDates" : "1973"
        |    },
        |    {
        |      "ccr" : "COAL 80/80/11",
        |      "oci" : "COAL.2022.V11RJW.P",
        |      "scopeAndContent" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (K)",
        |      "coveringDates" : "1975"
        |    },
        |    {
        |      "ccr" : "COAL 80/80/12",
        |      "oci" : "COAL.2022.V12RJW.P",
        |      "scopeAndContent" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (L)",
        |      "coveringDates" : "1977"
        |    }
        |  ]
        |}""".stripMargin
    )
    .as[EditSet]

  def getEditSet(): EditSet =
    editSet1
}
