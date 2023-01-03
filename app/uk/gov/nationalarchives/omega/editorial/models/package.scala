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
import uk.gov.nationalarchives.omega.editorial.models.{ EditSet, EditSetRecord }

package object editSetRecords {

  val editSetRecord1: EditSetRecord = Json
    .parse(
      """{
        |  "ccr" : "COAL 80/80/1",
        |  "oci" : "COAL.2022.V1RJW.P",
        |  "scopeAndContent" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
        |  "coveringDates" : "1960",
        |  "formerReferenceDepartment" : "",
        |  "startDateDay" : "1",
        |  "startDateMonth" : "1",
        |  "startDateYear" : "1960",
        |  "endDateDay" : "31",
        |  "endDateMonth" : "12",
        |  "endDateYear" : "1960"
        |} """.stripMargin
    )
    .validate[EditSetRecord]
    .get

  val editSetRecord2: EditSetRecord = Json
    .parse(
      """{
        |  "ccr" : "COAL 80/80/2",
        |  "oci" : "COAL.2022.V2RJW.P",
        |  "scopeAndContent" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
        |  "coveringDates" : "1960",
        |  "formerReferenceDepartment" : "",
        |  "startDateDay" : "1",
        |  "startDateMonth" : "1",
        |  "startDateYear" : "1960",
        |  "endDateDay" : "31",
        |  "endDateMonth" : "12",
        |  "endDateYear" : "1960"
        |} """.stripMargin
    )
    .validate[EditSetRecord]
    .get

  val editSetRecord3: EditSetRecord = Json
    .parse(
      """{
        |  "ccr" : "COAL 80/80/3",
        |  "oci" : "COAL.2022.V3RJW.P",
        |  "scopeAndContent" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
        |  "coveringDates" : "1960",
        |  "formerReferenceDepartment" : "",
        |  "startDateDay" : "1",
        |  "startDateMonth" : "1",
        |  "startDateYear" : "1960",
        |  "endDateDay" : "31",
        |  "endDateMonth" : "12",
        |  "endDateYear" : "1960"
        |} """.stripMargin
    )
    .validate[EditSetRecord]
    .get

  val editSetRecord4: EditSetRecord = Json
    .parse(
      """{
        |  "ccr" : "COAL 80/80/4",
        |  "oci" : "COAL.2022.V4RJW.P",
        |  "scopeAndContent" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
        |  "coveringDates" : "1960",
        |  "formerReferenceDepartment" : "",
        |  "startDateDay" : "1",
        |  "startDateMonth" : "1",
        |  "startDateYear" : "1960",
        |  "endDateDay" : "31",
        |  "endDateMonth" : "12",
        |  "endDateYear" : "1960"
        |} """.stripMargin
    )
    .validate[EditSetRecord]
    .get

  val editSetRecord5: EditSetRecord = Json
    .parse(
      """{
        |  "ccr" : "COAL 80/80/5",
        |  "oci" : "COAL.2022.V5RJW.P",
        |  "scopeAndContent" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
        |  "coveringDates" : "1960",
        |  "formerReferenceDepartment" : "",
        |  "startDateDay" : "1",
        |  "startDateMonth" : "1",
        |  "startDateYear" : "1960",
        |  "endDateDay" : "31",
        |  "endDateMonth" : "12",
        |  "endDateYear" : "1960"
        |} """.stripMargin
    )
    .validate[EditSetRecord]
    .get

  val editSetRecord6: EditSetRecord = Json
    .parse(
      """{
        |  "ccr" : "COAL 80/80/6",
        |  "oci" : "COAL.2022.V6RJW.P",
        |  "scopeAndContent" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
        |  "coveringDates" : "1960",
        |  "formerReferenceDepartment" : "",
        |  "startDateDay" : "1",
        |  "startDateMonth" : "1",
        |  "startDateYear" : "1960",
        |  "endDateDay" : "31",
        |  "endDateMonth" : "12",
        |  "endDateYear" : "1960"
        |} """.stripMargin
    )
    .validate[EditSetRecord]
    .get

  val editSetRecord7: EditSetRecord = Json
    .parse(
      """{
        |  "ccr" : "COAL 80/80/7",
        |  "oci" : "COAL.2022.V7RJW.P",
        |  "scopeAndContent" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
        |  "coveringDates" : "1960",
        |  "formerReferenceDepartment" : "",
        |  "startDateDay" : "1",
        |  "startDateMonth" : "1",
        |  "startDateYear" : "1960",
        |  "endDateDay" : "31",
        |  "endDateMonth" : "12",
        |  "endDateYear" : "1960"
        |} """.stripMargin
    )
    .validate[EditSetRecord]
    .get

  val editSetRecord8: EditSetRecord = Json
    .parse(
      """{
        |  "ccr" : "COAL 80/80/8",
        |  "oci" : "COAL.2022.V8RJW.P",
        |  "scopeAndContent" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
        |  "coveringDates" : "1960",
        |  "formerReferenceDepartment" : "",
        |  "startDateDay" : "1",
        |  "startDateMonth" : "1",
        |  "startDateYear" : "1960",
        |  "endDateDay" : "31",
        |  "endDateMonth" : "12",
        |  "endDateYear" : "1960"
        |} """.stripMargin
    )
    .validate[EditSetRecord]
    .get

  val editSetRecord9: EditSetRecord = Json
    .parse(
      """{
        |  "ccr" : "COAL 80/80/9",
        |  "oci" : "COAL.2022.V9RJW.P",
        |  "scopeAndContent" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
        |  "coveringDates" : "1960",
        |  "formerReferenceDepartment" : "",
        |  "startDateDay" : "1",
        |  "startDateMonth" : "1",
        |  "startDateYear" : "1960",
        |  "endDateDay" : "31",
        |  "endDateMonth" : "12",
        |  "endDateYear" : "1960"
        |} """.stripMargin
    )
    .validate[EditSetRecord]
    .get

  val editSetRecord10: EditSetRecord = Json
    .parse(
      """{
        |  "ccr" : "COAL 80/80/10",
        |  "oci" : "COAL.2022.V10RJW.P",
        |  "scopeAndContent" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
        |  "coveringDates" : "1960",
        |  "formerReferenceDepartment" : "",
        |  "startDateDay" : "1",
        |  "startDateMonth" : "1",
        |  "startDateYear" : "1960",
        |  "endDateDay" : "31",
        |  "endDateMonth" : "12",
        |  "endDateYear" : "1960"
        |} """.stripMargin
    )
    .validate[EditSetRecord]
    .get

  val editSetRecord11: EditSetRecord = Json
    .parse(
      """{
        |  "ccr" : "COAL 80/80/11",
        |  "oci" : "COAL.2022.V11RJW.P",
        |  "scopeAndContent" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
        |  "coveringDates" : "1960",
        |  "formerReferenceDepartment" : "",
        |  "startDateDay" : "1",
        |  "startDateMonth" : "1",
        |  "startDateYear" : "1960",
        |  "endDateDay" : "31",
        |  "endDateMonth" : "12",
        |  "endDateYear" : "1960"
        |} """.stripMargin
    )
    .validate[EditSetRecord]
    .get

  val editSetRecord12: EditSetRecord = Json
    .parse(
      """{
        |  "ccr" : "COAL 80/80/12",
        |  "oci" : "COAL.2022.V12RJW.P",
        |  "scopeAndContent" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
        |  "coveringDates" : "1960",
        |  "formerReferenceDepartment" : "",
        |  "startDateDay" : "1",
        |  "startDateMonth" : "1",
        |  "startDateYear" : "1960",
        |  "endDateDay" : "31",
        |  "endDateMonth" : "12",
        |  "endDateYear" : "1960"
        |} """.stripMargin
    )
    .validate[EditSetRecord]
    .get

  var editSetRecordMap = Map(
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
    "COAL.2022.V12RJW.P" -> editSetRecord12
  )
  def getEditSetRecordByOCI(oci: String): Option[EditSetRecord] =
    editSetRecordMap.get(oci)

  def saveEditSetRecord(editSetRecord: EditSetRecord) = {
    val editSetRecordOCI = editSetRecord.oci;
    editSetRecordMap -= editSetRecordOCI
    editSetRecordMap += (editSetRecordOCI -> editSetRecord)
  }

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
        |      "scopeAndContent" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
        |      "coveringDates" : "1960"
        |    },
        |    {
        |      "ccr" : "COAL 80/80/2",
        |      "oci" : "COAL.2022.V2RJW.P",
        |      "scopeAndContent" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
        |      "coveringDates" : "1960"
        |    },
        |    {
        |      "ccr" : "COAL 80/80/3",
        |      "oci" : "COAL.2022.V3RJW.P",
        |      "scopeAndContent" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
        |      "coveringDates" : "1960"
        |    },
        |    {
        |      "ccr" : "COAL 80/80/4",
        |      "oci" : "COAL.2022.V4RJW.P",
        |      "scopeAndContent" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
        |      "coveringDates" : "1960"
        |    },
        |    {
        |      "ccr" : "COAL 80/80/5",
        |      "oci" : "COAL.2022.V5RJW.P",
        |      "scopeAndContent" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
        |      "coveringDates" : "1960"
        |    },
        |    {
        |      "ccr" : "COAL 80/80/6",
        |      "oci" : "COAL.2022.V6RJW.P",
        |      "scopeAndContent" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
        |      "coveringDates" : "1960"
        |    },
        |    {
        |      "ccr" : "COAL 80/80/7",
        |      "oci" : "COAL.2022.V7RJW.P",
        |      "scopeAndContent" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
        |      "coveringDates" : "1960"
        |    },
        |    {
        |      "ccr" : "COAL 80/80/8",
        |      "oci" : "COAL.2022.V8RJW.P",
        |      "scopeAndContent" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
        |      "coveringDates" : "1960"
        |    },
        |    {
        |      "ccr" : "COAL 80/80/9",
        |      "oci" : "COAL.2022.V9RJW.P",
        |      "scopeAndContent" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
        |      "coveringDates" : "1960"
        |    },
        |    {
        |      "ccr" : "COAL 80/80/10",
        |      "oci" : "COAL.2022.V10RJW.P",
        |      "scopeAndContent" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
        |      "coveringDates" : "1960"
        |    },
        |    {
        |      "ccr" : "COAL 80/80/11",
        |      "oci" : "COAL.2022.V11RJW.P",
        |      "scopeAndContent" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
        |      "coveringDates" : "1960"
        |    },
        |    {
        |      "ccr" : "COAL 80/80/12",
        |      "oci" : "COAL.2022.V12RJW.P",
        |      "scopeAndContent" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
        |      "coveringDates" : "1960"
        |    }
        |  ]
        |}""".stripMargin
    )
    .validate[EditSet]
    .get

  def getEditSet(): EditSet =
    editSet1
}
