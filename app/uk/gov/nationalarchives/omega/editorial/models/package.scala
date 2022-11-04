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
import uk.gov.nationalarchives.omega.editorial.editSetRecords.editSetRecordMap
import uk.gov.nationalarchives.omega.editorial.models.{ EditSet, EditSetRecord }

package object editSetRecords {

  val editSetRecord1: EditSetRecord = Json
    .parse(
      """{
        |  "ccr" : "COAL 80/80/1",
        |  "oci" : "COAL.2022.V5RJW.P",
        |  "scopeAndContent" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
        |  "coveringDates" : "1960",
        |  "formerReferenceDepartment" : "",
        |  "startDate" : "1960",
        |  "endDate" : ""
        |} """.stripMargin
    )
    .validate[EditSetRecord]
    .get

  val editSetRecord2: EditSetRecord = Json
    .parse(
      """{
        |  "ccr" : "COAL 80/80/1",
        |  "oci" : "COAL.2022.V4RJW.P",
        |  "scopeAndContent" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
        |  "coveringDates" : "1960",
        |  "formerReferenceDepartment" : "",
        |  "startDate" : "1960",
        |  "endDate" : ""
        |} """.stripMargin
    )
    .validate[EditSetRecord]
    .get

  val editSetRecord3: EditSetRecord = Json
    .parse(
      """{
        |  "ccr" : "COAL 80/80/1",
        |  "oci" : "COAL.2022.V3RJW.P",
        |  "scopeAndContent" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
        |  "coveringDates" : "1960",
        |  "formerReferenceDepartment" : "",
        |  "startDate" : "1960",
        |  "endDate" : ""
        |} """.stripMargin
    )
    .validate[EditSetRecord]
    .get

  var editSetRecordMap = Map(
    "COAL.2022.V5RJW.P" -> editSetRecord1,
    "COAL.2022.V4RJW.P" -> editSetRecord2,
    "COAL.2022.V3RJW.P" -> editSetRecord3
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
        |      "oci" : "COAL.2022.V5RJW.P",
        |      "scopeAndContent" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
        |      "coveringDates" : "1960"
        |    },
        |    {
        |      "ccr" : "COAL 80/80/2",
        |      "oci" : "COAL.2022.V4RJW.P",
        |      "scopeAndContent" : "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.",
        |      "coveringDates" : "1960"
        |    },
        |    {
        |      "ccr" : "COAL 80/80/3",
        |      "oci" : "COAL.2022.V3RJW.P",
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
