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

package uk.gov.nationalarchives.omega.editorial.models

import uk.gov.nationalarchives.omega.editorial.models.MaterialReference.{ DescriptionOnly, LinkAndDescription, LinkOnly }

trait StubData {

  private val editSetRecordOne: EditSetRecord =
    EditSetRecord(
      ccr = "COAL 80/80/1",
      oci = "COAL.2022.V1RJW.P",
      scopeAndContent = "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (B)",
      coveringDates = "1962",
      formerReferenceDepartment = "MR 193 (9)",
      formerReferencePro = "MPS 4/1",
      startDateDay = "1",
      startDateMonth = "1",
      startDateYear = "1962",
      endDateDay = "31",
      endDateMonth = "12",
      endDateYear = "1962",
      legalStatusID = "ref.1",
      placeOfDepositID = "1",
      note = "A note about COAL.2022.V1RJW.P.",
      background = "Photo was taken by a daughter of one of the coal miners who used them.",
      custodialHistory = "Files originally created by successor or predecessor departments for COAL",
      relatedMaterial = Seq(
        DescriptionOnly("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths."),
        LinkOnly("#;", "COAL 80/80/3"),
        LinkAndDescription(
          "#;",
          "COAL 80/80/2",
          "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths."
        )
      ),
      separatedMaterial = Seq(
        LinkOnly("#;", "COAL 80/80/5"),
        LinkOnly("#;", "COAL 80/80/6"),
        LinkOnly("#;", "COAL 80/80/7")
      ),
      creatorIDs = Seq("48N", "92W")
    )

  private val editSetRecordTwo: EditSetRecord =
    EditSetRecord(
      ccr = "COAL 80/80/2",
      oci = "COAL.2022.V2RJW",
      scopeAndContent = "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (A)",
      coveringDates = "1966",
      formerReferenceDepartment = "",
      formerReferencePro = "",
      startDateDay = "1",
      startDateMonth = "1",
      startDateYear = "1966",
      endDateDay = "31",
      endDateMonth = "12",
      endDateYear = "1966",
      legalStatusID = "ref.2",
      placeOfDepositID = "",
      note = "A note about COAL.2022.V2RJW.",
      background = "",
      custodialHistory = "",
      relatedMaterial = Seq.empty,
      separatedMaterial = Seq.empty,
      creatorIDs = Seq("515", "XXX")
    )

  private val editSetRecordThree: EditSetRecord =
    EditSetRecord(
      ccr = "COAL 80/80/3",
      oci = "COAL.2022.V3RJW.P",
      scopeAndContent = "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (C)",
      coveringDates = "1964",
      formerReferenceDepartment = "",
      formerReferencePro = "",
      startDateDay = "1",
      startDateMonth = "1",
      startDateYear = "1964",
      endDateDay = "31",
      endDateMonth = "12",
      endDateYear = "1964",
      legalStatusID = "",
      placeOfDepositID = "6",
      note = "",
      background = "Photo was taken by a son of one of the coal miners who used them.",
      custodialHistory = "",
      relatedMaterial = Seq.empty,
      separatedMaterial = Seq.empty,
      creatorIDs = Seq("8R6", "92W", "8R6")
    )

  private val editSetRecordFour: EditSetRecord =
    EditSetRecord(
      ccr = "COAL 80/80/4",
      oci = "COAL.2022.V4RJW.P",
      scopeAndContent = "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (D)",
      coveringDates = "1961",
      formerReferenceDepartment = "",
      formerReferencePro = "CAB 172",
      startDateDay = "1",
      startDateMonth = "1",
      startDateYear = "1961",
      endDateDay = "31",
      endDateMonth = "12",
      endDateYear = "1961",
      legalStatusID = "ref.1",
      placeOfDepositID = "1",
      note = "",
      background = "",
      custodialHistory = "",
      relatedMaterial = Seq.empty,
      separatedMaterial = Seq.empty,
      creatorIDs = Seq.empty
    )

  private val editSetRecordFive: EditSetRecord =
    EditSetRecord(
      ccr = "COAL 80/80/5",
      oci = "COAL.2022.V5RJW.P",
      scopeAndContent = "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (E)",
      coveringDates = "1963",
      formerReferenceDepartment = "",
      formerReferencePro = "",
      startDateDay = "1",
      startDateMonth = "1",
      startDateYear = "1963",
      endDateDay = "31",
      endDateMonth = "12",
      endDateYear = "1963",
      legalStatusID = "ref.1",
      placeOfDepositID = "1",
      note = "Need to check copyright info.",
      background = "",
      custodialHistory = "",
      relatedMaterial = Seq.empty,
      separatedMaterial = Seq.empty,
      creatorIDs = Seq("46F", "48N", "46F")
    )

  private val editSetRecordSix: EditSetRecord =
    EditSetRecord(
      ccr = "COAL 80/80/6",
      oci = "COAL.2022.V6RJW.P",
      scopeAndContent = "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (F)",
      coveringDates = "1965",
      formerReferenceDepartment = "",
      formerReferencePro = "",
      startDateDay = "1",
      startDateMonth = "1",
      startDateYear = "1965",
      endDateDay = "31",
      endDateMonth = "12",
      endDateYear = "1965",
      legalStatusID = "ref.1",
      placeOfDepositID = "1",
      note = "",
      background = "",
      custodialHistory = "",
      relatedMaterial = Seq.empty,
      separatedMaterial = Seq.empty,
      creatorIDs = Seq("4", "FKS")
    )

  private val editSetRecordSeven: EditSetRecord =
    EditSetRecord(
      ccr = "COAL 80/80/7",
      oci = "COAL.2022.V7RJW.P",
      scopeAndContent = "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (G)",
      coveringDates = "1967",
      formerReferenceDepartment = "",
      formerReferencePro = "",
      startDateDay = "1",
      startDateMonth = "1",
      startDateYear = "1967",
      endDateDay = "31",
      endDateMonth = "12",
      endDateYear = "1967",
      legalStatusID = "ref.1",
      placeOfDepositID = "1",
      note = "",
      background = "",
      custodialHistory = "",
      relatedMaterial = Seq.empty,
      separatedMaterial = Seq.empty,
      creatorIDs = Seq("48N", "92W")
    )

  private val editSetRecordEight: EditSetRecord =
    EditSetRecord(
      ccr = "COAL 80/80/8",
      oci = "COAL.2022.V8RJW.P",
      scopeAndContent = "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (H)",
      coveringDates = "1969",
      formerReferenceDepartment = "",
      formerReferencePro = "",
      startDateDay = "1",
      startDateMonth = "1",
      startDateYear = "1969",
      endDateDay = "31",
      endDateMonth = "12",
      endDateYear = "1969",
      legalStatusID = "ref.1",
      placeOfDepositID = "1",
      note = "",
      background = "",
      custodialHistory = "",
      relatedMaterial = Seq.empty,
      separatedMaterial = Seq.empty,
      creatorIDs = Seq("48N", "46F", "8R6")
    )

  private val editSetRecordNine: EditSetRecord =
    EditSetRecord(
      ccr = "COAL 80/80/9",
      oci = "COAL.2022.V9RJW.P",
      scopeAndContent = "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (I)",
      coveringDates = "1971",
      formerReferenceDepartment = "",
      formerReferencePro = "",
      startDateDay = "1",
      startDateMonth = "1",
      startDateYear = "1971",
      endDateDay = "31",
      endDateMonth = "12",
      endDateYear = "1971",
      legalStatusID = "ref.1",
      placeOfDepositID = "1",
      note = "",
      background = "",
      custodialHistory = "",
      relatedMaterial = Seq.empty,
      separatedMaterial = Seq.empty,
      creatorIDs = Seq("2TK", "39T", "ST")
    )

  private val editSetRecordTen: EditSetRecord =
    EditSetRecord(
      ccr = "COAL 80/80/10",
      oci = "COAL.2022.V10RJW.P",
      scopeAndContent = "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (J)",
      coveringDates = "1973",
      formerReferenceDepartment = "",
      formerReferencePro = "",
      startDateDay = "1",
      startDateMonth = "1",
      startDateYear = "1973",
      endDateDay = "31",
      endDateMonth = "12",
      endDateYear = "1973",
      legalStatusID = "ref.1",
      placeOfDepositID = "1",
      note = "",
      background = "",
      custodialHistory = "",
      relatedMaterial = Seq.empty,
      separatedMaterial = Seq(
        DescriptionOnly("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
      ),
      creatorIDs = Seq("XXX")
    )

  private val editSetRecordEleven: EditSetRecord =
    EditSetRecord(
      ccr = "COAL 80/80/11",
      oci = "COAL.2022.V11RJW.P",
      scopeAndContent = "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (K)",
      coveringDates = "1975",
      formerReferenceDepartment = "",
      formerReferencePro = "",
      startDateDay = "1",
      startDateMonth = "1",
      startDateYear = "1975",
      endDateDay = "31",
      endDateMonth = "12",
      endDateYear = "1975",
      legalStatusID = "ref.1",
      placeOfDepositID = "1",
      note = "",
      background = "",
      custodialHistory = "",
      separatedMaterial = Seq(
        DescriptionOnly("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths."),
        LinkOnly("#;", "COAL 80/80/5")
      ),
      creatorIDs = Seq("8R6")
    )

  private val editSetRecordTwelve: EditSetRecord =
    EditSetRecord(
      ccr = "COAL 80/80/12",
      oci = "COAL.2022.V12RJW.P",
      scopeAndContent = "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (L)",
      coveringDates = "1977",
      formerReferenceDepartment = "",
      formerReferencePro = "",
      startDateDay = "1",
      startDateMonth = "1",
      startDateYear = "1977",
      endDateDay = "31",
      endDateMonth = "12",
      endDateYear = "1977",
      legalStatusID = "ref.1",
      placeOfDepositID = "1",
      note = "Quality of photos is only fair.",
      background = "",
      custodialHistory = "",
      relatedMaterial = Seq.empty,
      separatedMaterial = Seq(
        DescriptionOnly("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths."),
        LinkOnly("#;", "COAL 80/80/5"),
        LinkAndDescription(
          "#;",
          "COAL 80/80/8",
          "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths."
        )
      ),
      creatorIDs = Seq("8R6", "92W", "")
    )

  private val editSetRecordMap: Map[String, EditSetRecord] =
    Seq(
      editSetRecordOne,
      editSetRecordTwo,
      editSetRecordThree,
      editSetRecordFour,
      editSetRecordFive,
      editSetRecordSix,
      editSetRecordSeven,
      editSetRecordEight,
      editSetRecordNine,
      editSetRecordTen,
      editSetRecordEleven,
      editSetRecordTwelve
    )
      .map(editSetRecord => editSetRecord.oci -> editSetRecord)
      .toMap

  private val editSetOne = EditSet(
    name = "COAL 80 Sample",
    id = "1",
    entries = Seq(
      EditSetEntry(
        ccr = editSetRecordOne.ccr,
        oci = editSetRecordOne.oci,
        scopeAndContent = editSetRecordOne.scopeAndContent,
        coveringDates = editSetRecordOne.coveringDates
      ),
      EditSetEntry(
        ccr = editSetRecordTwo.ccr,
        oci = editSetRecordTwo.oci,
        scopeAndContent = editSetRecordTwo.scopeAndContent,
        coveringDates = editSetRecordTwo.coveringDates
      ),
      EditSetEntry(
        ccr = editSetRecordThree.ccr,
        oci = editSetRecordThree.oci,
        scopeAndContent = editSetRecordThree.scopeAndContent,
        coveringDates = editSetRecordThree.coveringDates
      ),
      EditSetEntry(
        ccr = editSetRecordFour.ccr,
        oci = editSetRecordFour.oci,
        scopeAndContent = editSetRecordFour.scopeAndContent,
        coveringDates = editSetRecordFour.coveringDates
      ),
      EditSetEntry(
        ccr = editSetRecordFive.ccr,
        oci = editSetRecordFive.oci,
        scopeAndContent = editSetRecordFive.scopeAndContent,
        coveringDates = editSetRecordFive.coveringDates
      ),
      EditSetEntry(
        ccr = editSetRecordSix.ccr,
        oci = editSetRecordSix.oci,
        scopeAndContent = editSetRecordSix.scopeAndContent,
        coveringDates = editSetRecordSix.coveringDates
      ),
      EditSetEntry(
        ccr = editSetRecordSeven.ccr,
        oci = editSetRecordSeven.oci,
        scopeAndContent = editSetRecordSeven.scopeAndContent,
        coveringDates = editSetRecordSeven.coveringDates
      ),
      EditSetEntry(
        ccr = editSetRecordEight.ccr,
        oci = editSetRecordEight.oci,
        scopeAndContent = editSetRecordEight.scopeAndContent,
        coveringDates = editSetRecordEight.coveringDates
      ),
      EditSetEntry(
        ccr = editSetRecordNine.ccr,
        oci = editSetRecordNine.oci,
        scopeAndContent = editSetRecordNine.scopeAndContent,
        coveringDates = editSetRecordNine.coveringDates
      ),
      EditSetEntry(
        ccr = editSetRecordTen.ccr,
        oci = editSetRecordTen.oci,
        scopeAndContent = editSetRecordTen.scopeAndContent,
        coveringDates = editSetRecordTen.coveringDates
      ),
      EditSetEntry(
        ccr = editSetRecordEleven.ccr,
        oci = editSetRecordEleven.oci,
        scopeAndContent = editSetRecordEleven.scopeAndContent,
        coveringDates = editSetRecordEleven.coveringDates
      ),
      EditSetEntry(
        ccr = editSetRecordTwelve.ccr,
        oci = editSetRecordTwelve.oci,
        scopeAndContent = editSetRecordTwelve.scopeAndContent,
        coveringDates = editSetRecordTwelve.coveringDates
      )
    )
  )

  private val editSetMap =
    Seq(editSetOne)
      .map(editSet => editSet.id -> editSet)
      .toMap

  def getEditSet(id: String): Option[EditSet] = editSetMap.get(id)

  def getExpectedEditSet(id: String): EditSet =
    getEditSet(id)
      .getOrElse(throw new RuntimeException(s"Unknown Edit Set [$id]"))

  def getEditSetRecord(id: String): Option[EditSetRecord] =
    editSetRecordMap.get(id)

  def getExpectedEditSetRecord(id: String): EditSetRecord =
    getEditSetRecord(id)
      .getOrElse(throw new RuntimeException(s"Unknown Edit Set Record [$id]"))

}
