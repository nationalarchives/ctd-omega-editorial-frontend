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

package uk.gov.nationalarchives.omega.editorial.services.jms

import uk.gov.nationalarchives.omega.editorial.models.MaterialReference.{ DescriptionOnly, LinkAndDescription, LinkOnly }
import uk.gov.nationalarchives.omega.editorial.models.{ LegalStatus, _ }
import com.google.inject.ImplementedBy

import javax.inject.{ Inject, Singleton }

@Singleton
class StubDataImpl @Inject() extends StubData

@ImplementedBy(classOf[StubDataImpl])
trait StubData {

  val baseUri: String = "http://cat.nationalarchives.gov.uk"
  val baseUriAgent: String = s"$baseUri/agent"

  val tna: String = s"$baseUriAgent.S2"
  val britishMuseum: String = s"$baseUriAgent.63F"
  val britishLibrary: String = s"$baseUriAgent.614"

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
      legalStatusID = s"$baseUri/public-record",
      placeOfDepositID = tna,
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
      creatorIDs = Seq(s"$baseUriAgent.48N", s"$baseUriAgent.92W")
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
      legalStatusID = s"$baseUri/non-public-record",
      placeOfDepositID = "",
      note = "A note about COAL.2022.V2RJW.",
      background = "",
      custodialHistory = "",
      relatedMaterial = Seq.empty,
      separatedMaterial = Seq.empty,
      creatorIDs = Seq(s"$baseUriAgent.515", s"$baseUriAgent.XXX")
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
      creatorIDs = Seq(s"$baseUriAgent.8R6", s"$baseUriAgent.92W", s"$baseUriAgent.8R6")
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
      legalStatusID = s"$baseUri/public-record",
      placeOfDepositID = tna,
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
      legalStatusID = s"$baseUri/public-record",
      placeOfDepositID = tna,
      note = "Need to check copyright info.",
      background = "",
      custodialHistory = "",
      relatedMaterial = Seq.empty,
      separatedMaterial = Seq.empty,
      creatorIDs = Seq(s"$baseUriAgent.46F", s"$baseUriAgent.48N", s"$baseUriAgent.46F")
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
      legalStatusID = s"$baseUri/public-record",
      placeOfDepositID = tna,
      note = "",
      background = "",
      custodialHistory = "",
      relatedMaterial = Seq.empty,
      separatedMaterial = Seq.empty,
      creatorIDs = Seq(s"$baseUriAgent.4", s"$baseUriAgent.FKS", s"$baseUriAgent.4WV")
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
      legalStatusID = s"$baseUri/public-record",
      placeOfDepositID = tna,
      note = "",
      background = "",
      custodialHistory = "",
      relatedMaterial = Seq.empty,
      separatedMaterial = Seq.empty,
      creatorIDs = Seq(s"$baseUriAgent.48N", s"$baseUriAgent.92W")
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
      legalStatusID = s"$baseUri/public-record",
      placeOfDepositID = tna,
      note = "",
      background = "",
      custodialHistory = "",
      relatedMaterial = Seq.empty,
      separatedMaterial = Seq.empty,
      creatorIDs = Seq(s"$baseUriAgent.48N", s"$baseUriAgent.46F", s"$baseUriAgent.8R6")
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
      legalStatusID = s"$baseUri/public-record",
      placeOfDepositID = tna,
      note = "",
      background = "",
      custodialHistory = "",
      relatedMaterial = Seq.empty,
      separatedMaterial = Seq.empty,
      creatorIDs = Seq(s"$baseUriAgent.2TK", s"$baseUriAgent.39T", s"$baseUriAgent.ST", s"$baseUriAgent.331")
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
      legalStatusID = s"$baseUri/public-record",
      placeOfDepositID = tna,
      note = "",
      background = "",
      custodialHistory = "",
      relatedMaterial = Seq.empty,
      separatedMaterial = Seq(
        DescriptionOnly("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
      ),
      creatorIDs = Seq(s"$baseUriAgent.XXX")
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
      legalStatusID = s"$baseUri/public-record",
      placeOfDepositID = tna,
      note = "",
      background = "",
      custodialHistory = "",
      separatedMaterial = Seq(
        DescriptionOnly("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths."),
        LinkOnly("#;", "COAL 80/80/5")
      ),
      creatorIDs = Seq(s"$baseUriAgent.8R6")
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
      legalStatusID = s"$baseUri/public-record",
      placeOfDepositID = tna,
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
      creatorIDs = Seq(s"$baseUriAgent.8R6", s"$baseUriAgent.92W", "")
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

  def getLegalStatuses: Seq[LegalStatus] = Seq(
    LegalStatus(s"$baseUri/public-record", "Public Record"),
    LegalStatus(s"$baseUri/non-public-record", "Non-Public Record"),
    LegalStatus(
      s"$baseUri/public-record-unless-otherwise-stated",
      "Public Record (unless otherwise stated)"
    ),
    LegalStatus(s"$baseUri/welsh-public-record", "Welsh Public Record"),
    LegalStatus(s"$baseUri/non-record-material", "Non-Record Material")
  )

  def getAgentSummaries: Seq[AgentSummary] = List(
    AgentSummary(
      AgentType.Person,
      s"$baseUriAgent.3RX",
      "current description",
      AgentDescription(
        s"$baseUriAgent.3RX",
        "Abbot, Charles",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1798"),
        Some("1867")
      )
    ),
    AgentSummary(
      AgentType.Person,
      s"$baseUriAgent.48N",
      "current description",
      AgentDescription(
        s"$baseUriAgent.48N",
        "Baden-Powell, Lady Olave St Clair",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1889"),
        Some("1977"),
        None
      )
    ),
    AgentSummary(
      AgentType.Person,
      s"$baseUriAgent.39K",
      "current description",
      AgentDescription(
        s"$baseUriAgent.39K",
        "Cannon, John Francis Michael",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1930"),
        None,
        None
      )
    ),
    AgentSummary(
      AgentType.Person,
      s"$baseUriAgent.3FH",
      "current description",
      AgentDescription(
        s"$baseUriAgent.3FH",
        "Dainton, Sir Frederick Sydney",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1914"),
        Some("1997"),
        None
      )
    ),
    AgentSummary(
      AgentType.Person,
      s"$baseUriAgent.54J",
      "current description",
      AgentDescription(
        s"$baseUriAgent.54J",
        "Edward, ",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1330"),
        Some("1376")
      )
    ),
    AgentSummary(
      AgentType.Person,
      s"$baseUriAgent.2QX",
      "current description",
      AgentDescription(
        s"$baseUriAgent.2QX",
        "Edward VII",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1841"),
        Some("1910")
      )
    ),
    AgentSummary(
      AgentType.Person,
      s"$baseUriAgent.561",
      "current description",
      AgentDescription(
        s"$baseUriAgent.561",
        "Fanshawe, Baron, of Richmond, ",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        None,
        None
      )
    ),
    AgentSummary(
      AgentType.Person,
      s"$baseUriAgent.46F",
      "current description",
      AgentDescription(
        s"$baseUriAgent.46F",
        "Fawkes, Guy",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1570"),
        Some("1606")
      )
    ),
    AgentSummary(
      AgentType.Person,
      s"$baseUriAgent.2JN",
      "current description",
      AgentDescription(
        s"$baseUriAgent.2JN",
        "George, David Lloyd",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1863"),
        Some("1945")
      )
    ),
    AgentSummary(
      AgentType.Person,
      s"$baseUriAgent.34X",
      "current description",
      AgentDescription(
        s"$baseUriAgent.34X",
        "Halley, Edmund",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1656"),
        Some("1742")
      )
    ),
    AgentSummary(
      AgentType.Person,
      s"$baseUriAgent.2TK",
      "current description",
      AgentDescription(
        s"$baseUriAgent.2TK",
        "Halifax, ",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        None,
        None
      )
    ),
    AgentSummary(
      AgentType.Person,
      s"$baseUriAgent.39T",
      "current description",
      AgentDescription(
        s"$baseUriAgent.39T",
        "Irvine, Linda Mary",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1928"),
        None
      )
    ),
    AgentSummary(
      AgentType.Person,
      s"$baseUriAgent.4",
      "current description",
      AgentDescription(
        s"$baseUriAgent.4",
        "Jack the Ripper, ",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1888"),
        Some("1888")
      )
    ),
    AgentSummary(
      AgentType.Person,
      s"$baseUriAgent.4FF",
      "current description",
      AgentDescription(
        s"$baseUriAgent.4FF",
        "Keay, Sir Lancelot Herman",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1883"),
        Some("1974")
      )
    ),
    AgentSummary(
      AgentType.Person,
      s"$baseUriAgent.ST",
      "current description",
      AgentDescription(
        s"$baseUriAgent.ST",
        "Lawson, Nigel",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1932"),
        None
      )
    ),
    AgentSummary(
      AgentType.Person,
      s"$baseUriAgent.51X",
      "current description",
      AgentDescription(
        s"$baseUriAgent.51X",
        "Macpherson, Sir William (Alan)",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1926"),
        None
      )
    ),
    AgentSummary(
      AgentType.Person,
      s"$baseUriAgent.515",
      "current description",
      AgentDescription(
        s"$baseUriAgent.515",
        "Newcastle, 1st Duke of, ",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        None,
        None
      )
    ),
    AgentSummary(
      AgentType.Person,
      s"$baseUriAgent.4VF",
      "current description",
      AgentDescription(
        s"$baseUriAgent.4VF",
        "Old Pretender, The",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        None,
        None
      )
    ),
    AgentSummary(
      AgentType.Person,
      s"$baseUriAgent.4H3",
      "current description",
      AgentDescription(
        s"$baseUriAgent.4H3",
        "Oliphant, Sir Mark Marcus Laurence Elwin",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1901"),
        Some("2000")
      )
    ),
    AgentSummary(
      AgentType.Person,
      s"$baseUriAgent.46W",
      "current description",
      AgentDescription(
        s"$baseUriAgent.46W",
        "Paine, Thomas",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1737"),
        Some("1809")
      )
    ),
    AgentSummary(
      AgentType.Person,
      s"$baseUriAgent.3SH",
      "current description",
      AgentDescription(
        s"$baseUriAgent.3SH",
        "Reade, Hubert Granville Revell",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1859"),
        Some("1938")
      )
    ),
    AgentSummary(
      AgentType.Person,
      s"$baseUriAgent.2TF",
      "current description",
      AgentDescription(
        s"$baseUriAgent.2TF",
        "Reading, ",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        None,
        None
      )
    ),
    AgentSummary(
      AgentType.Person,
      s"$baseUriAgent.53T",
      "current description",
      AgentDescription(
        s"$baseUriAgent.53T",
        "Salisbury, Sir Edward James",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1886"),
        Some("1978")
      )
    ),
    AgentSummary(
      AgentType.Person,
      s"$baseUriAgent.3QL",
      "current description",
      AgentDescription(
        s"$baseUriAgent.3QL",
        "Tate, Sir Henry",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1819"),
        Some("1899")
      )
    ),
    AgentSummary(
      AgentType.Person,
      s"$baseUriAgent.37K",
      "current description",
      AgentDescription(
        s"$baseUriAgent.37K",
        "Uvarov, Sir Boris Petrovitch",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1889"),
        Some("1970")
      )
    ),
    AgentSummary(
      AgentType.Person,
      s"$baseUriAgent.2T1",
      "current description",
      AgentDescription(
        s"$baseUriAgent.2T1",
        "Vane-Tempest-Stewart, Charles Stewart",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1852"),
        Some("1915")
      )
    ),
    AgentSummary(
      AgentType.Person,
      s"$baseUriAgent.4RW",
      "current description",
      AgentDescription(
        s"$baseUriAgent.4RW",
        "Victor Amadeus, ",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1666"),
        Some("1732")
      )
    ),
    AgentSummary(
      AgentType.Person,
      s"$baseUriAgent.3GY",
      "current description",
      AgentDescription(
        s"$baseUriAgent.3GY",
        "Victoria, ",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1819"),
        Some("1901")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.RR6",
      "current description",
      AgentDescription(
        s"$baseUriAgent.RR6",
        "100th (Gordon Highlanders) Regiment of Foot",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1794"),
        Some("1794")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.S34",
      "current description",
      AgentDescription(
        s"$baseUriAgent.S34",
        "1st Regiment of Foot or Royal Scots",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1812"),
        Some("1812")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.87K",
      "current description",
      AgentDescription(
        s"$baseUriAgent.87K",
        "Abbotsbury Railway Company",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1877"),
        Some("1877")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.VWG",
      "current description",
      AgentDescription(
        s"$baseUriAgent.VWG",
        "Accountant General in the Court of Chancery",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1726"),
        Some("1726")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.LWY",
      "current description",
      AgentDescription(
        s"$baseUriAgent.LWY",
        "Admiralty Administrative Whitley Council, General Purposes Committee",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1942"),
        Some("1942")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.VS6",
      "current description",
      AgentDescription(
        s"$baseUriAgent.VS6",
        "Advisory Committee on Animal Feedingstuffs",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1999"),
        Some("1999")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.CC",
      "current description",
      AgentDescription(
        s"$baseUriAgent.CC",
        "Bank of England",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1694"),
        Some("1694")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.N9S",
      "current description",
      AgentDescription(
        s"$baseUriAgent.N9S",
        "Bank on Tickets of the Million Adventure",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1695"),
        Some("1695")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.JS8",
      "current description",
      AgentDescription(s"$baseUriAgent.JS8", "BBC", Some(false), Some(false), "2022-06-22T02:00:00-0500", None, None)
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.8WG",
      "current description",
      AgentDescription(
        s"$baseUriAgent.8WG",
        "Bee Husbandry Committee",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1959"),
        Some("1959")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.6VQ",
      "current description",
      AgentDescription(
        s"$baseUriAgent.6VQ",
        "Cabinet",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1919"),
        Some("1919")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.SV",
      "current description",
      AgentDescription(
        s"$baseUriAgent.SV",
        "Cabinet",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1945"),
        Some("1945")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.5V4",
      "current description",
      AgentDescription(
        s"$baseUriAgent.5V4",
        "Cabinet, Committee for Control of Official Histories",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1946"),
        Some("1946")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.GW5",
      "current description",
      AgentDescription(
        s"$baseUriAgent.GW5",
        "Cattle Emergency Committee",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1934"),
        Some("1934")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.934",
      "current description",
      AgentDescription(
        s"$baseUriAgent.934",
        "Dairy Crest",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1981"),
        Some("1981")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.9HC",
      "current description",
      AgentDescription(
        s"$baseUriAgent.9HC",
        "Dean of the Chapel Royal",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        None,
        None
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.WGL",
      "current description",
      AgentDescription(
        s"$baseUriAgent.WGL",
        "Department for Environment, Food and Rural Affairs, Water Quality Division",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("2002"),
        Some("2002")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.WJ4",
      "current description",
      AgentDescription(
        s"$baseUriAgent.WJ4",
        "Department for Exiting the European Union",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("2016"),
        Some("2016")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.9YJ",
      "current description",
      AgentDescription(
        s"$baseUriAgent.9YJ",
        "East Grinstead, Groombridge and Tunbridge Wells Railway Company",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1862"),
        Some("1862")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.HF4",
      "current description",
      AgentDescription(
        s"$baseUriAgent.HF4",
        "East India Company",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1600"),
        Some("1600")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.WN3",
      "current description",
      AgentDescription(
        s"$baseUriAgent.WN3",
        "Education and Skills Funding Agency",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("2017"),
        Some("2017")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.WNL",
      "current description",
      AgentDescription(
        s"$baseUriAgent.WNL",
        "Education and Skills Funding Agency",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("2017"),
        Some("2017")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.Q1R",
      "current description",
      AgentDescription(
        s"$baseUriAgent.Q1R",
        "Falkland Islands Company",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1899"),
        Some("1899")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.SQ9",
      "current description",
      AgentDescription(
        s"$baseUriAgent.SQ9",
        "Fish's Corps of Foot",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1782"),
        Some("1782")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.R6R",
      "current description",
      AgentDescription(
        s"$baseUriAgent.R6R",
        "Foreign and Commonwealth Office, Consulate, Dusseldorf, West Germany",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1968"),
        Some("1968")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.HKL",
      "current description",
      AgentDescription(
        s"$baseUriAgent.HKL",
        "Foreign Office, Consulate, Angora and Konieh, Ottoman Empire",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1895"),
        Some("1895")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.KSC",
      "current description",
      AgentDescription(
        s"$baseUriAgent.KSC",
        "Gaming Board for Great Britain",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1968"),
        Some("1968")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.73R",
      "current description",
      AgentDescription("73R", "GCHQ", Some(false), Some(false), "2022-06-22T02:00:00-0500", None, None)
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.VR1",
      "current description",
      AgentDescription(
        "VR1",
        "Geffrye Museum",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1914"),
        Some("1914")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.QX5",
      "current description",
      AgentDescription(
        s"$baseUriAgent.QX5",
        "General Nursing Council for England and Wales, Registration and Enrolment Committee",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1970"),
        Some("1970")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.C1Y",
      "current description",
      AgentDescription(
        s"$baseUriAgent.C1Y",
        "Halifax High Level and North and South Junction Railway Company",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1884"),
        Some("1884")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.W2T",
      "current description",
      AgentDescription(
        s"$baseUriAgent.W2T",
        "Hansard Society",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1944"),
        Some("1944")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.F18",
      "current description",
      AgentDescription(
        s"$baseUriAgent.F18",
        "Health and Safety Commission, Health and Safety Executive, Employment Medical Advisory Service",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1975"),
        Some("1975")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.8JK",
      "current description",
      AgentDescription(
        s"$baseUriAgent.8JK",
        "Her Majesty's Stationery Office",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1986"),
        Some("1986")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.9FV",
      "current description",
      AgentDescription(
        s"$baseUriAgent.9FV",
        "Ideal Benefit Society",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1912"),
        Some("1912")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.5YX",
      "current description",
      AgentDescription(
        s"$baseUriAgent.5YX",
        "Imperial War Museum: Churchill Museum and Cabinet War Rooms",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1939"),
        Some("1939")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.W1Q",
      "current description",
      AgentDescription(
        s"$baseUriAgent.W1Q",
        "Independent Expert Group on Mobile Phones",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1999"),
        Some("1999")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.QLY",
      "current description",
      AgentDescription(
        s"$baseUriAgent.QLY",
        "Independent Expert Group on Mobile Phones",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1999"),
        Some("1999")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.LS5",
      "current description",
      AgentDescription(
        s"$baseUriAgent.LS5",
        "Jodrell Bank Observatory, Cheshire",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1955"),
        Some("1955")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.92W",
      "current description",
      AgentDescription(
        s"$baseUriAgent.92W",
        "Joint Milk Quality Committee",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1948"),
        Some("1948")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.L3W",
      "current description",
      AgentDescription(
        s"$baseUriAgent.L3W",
        "Justices in Eyre, of Assize, of Gaol Delivery, of Oyer and Terminer, of the Peace, and of Nisi Prius",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        None,
        None
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.N8X",
      "current description",
      AgentDescription(
        s"$baseUriAgent.N8X",
        "Justices of the Forest",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1166"),
        Some("1166")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.THY",
      "current description",
      AgentDescription(
        s"$baseUriAgent.THY",
        "Kew Gardens Archive",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        None,
        None
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.SGX",
      "current description",
      AgentDescription(
        s"$baseUriAgent.SGX",
        "King's Own Dragoons, 1751-1818",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        None,
        None
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.CCR",
      "current description",
      AgentDescription(
        s"$baseUriAgent.CCR",
        "Knitting, Lace and Net Industry Training Board",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1966"),
        Some("1966")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.TTT",
      "current description",
      AgentDescription(
        s"$baseUriAgent.TTT",
        "King's Volunteers Regiment of Foot, 1761-1763",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        None,
        None
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.VR7",
      "current description",
      AgentDescription(
        s"$baseUriAgent.VR7",
        "Lady Lever Art Gallery",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1922"),
        Some("1922")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.XQ",
      "current description",
      AgentDescription(
        s"$baseUriAgent.XQ",
        "Law Society",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1825"),
        Some("1825")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.91W",
      "current description",
      AgentDescription(
        s"$baseUriAgent.91W",
        "League of Mercy",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1898"),
        Some("1898")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.VX",
      "current description",
      AgentDescription(
        s"$baseUriAgent.VX",
        "Legal Aid Board",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1989"),
        Some("1989")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.TXG",
      "current description",
      AgentDescription(
        s"$baseUriAgent.TXG",
        "Legal Aid Board, 1988-1989",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        None,
        None
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.6LL",
      "current description",
      AgentDescription(
        s"$baseUriAgent.6LL",
        "Machinery of Government Committee",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1917"),
        Some("1917")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.G6N",
      "current description",
      AgentDescription(
        s"$baseUriAgent.G6N",
        "Magnetic Department",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1839"),
        Some("1839")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.71K",
      "current description",
      AgentDescription(
        s"$baseUriAgent.71K",
        "Manpower Distribution Board",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1916"),
        Some("1916")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.KN1",
      "current description",
      AgentDescription(
        s"$baseUriAgent.KN1",
        "Master of the Rolls Archives Committee",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1925"),
        Some("1925")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.J6X",
      "current description",
      AgentDescription(
        s"$baseUriAgent.J6X",
        "National Agricultural Advisory Service, Great House Experimental Husbandry Farm",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1951"),
        Some("1951")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.K7N",
      "current description",
      AgentDescription(
        s"$baseUriAgent.K7N",
        "National Air Traffic Control Services, Director General Projects and Engineering, Directorate of Projects",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1963"),
        Some("1963")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.TSL",
      "current description",
      AgentDescription(
        s"$baseUriAgent.TSL",
        "National Archives, The",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        None,
        None
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.LSN",
      "current description",
      AgentDescription(
        s"$baseUriAgent.LSN",
        "Navy Board, Transport Branch, Prisoner of War Department",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1817"),
        Some("1817")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.W1S",
      "current description",
      AgentDescription(
        s"$baseUriAgent.W1S",
        "Office for Budget Responsibility",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("2010"),
        Some("2010")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.N4W",
      "current description",
      AgentDescription(
        s"$baseUriAgent.N4W",
        "Office of Population Censuses and Surveys, Computer Division",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1972"),
        Some("1972")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.QQC",
      "current description",
      AgentDescription(
        s"$baseUriAgent.QQC",
        "Office of Works, Directorate of Works, Maintenance Surveyors Division, Sanitary Engineers Section",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1928"),
        Some("1928")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.QFY",
      "current description",
      AgentDescription(
        s"$baseUriAgent.QFY",
        "Office of the President of Social Security Appeal Tribunals, Medical Appeal Tribunals and Vaccine Damage Tribunals",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1984"),
        Some("1984")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.VYJ",
      "current description",
      AgentDescription(
        s"$baseUriAgent.VYJ",
        "Ordnance Survey of Great Britain, Directorate of Data Collection and Management",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("2003"),
        Some("2003")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.8FX",
      "current description",
      AgentDescription(
        s"$baseUriAgent.8FX",
        "Overseas Development Administration, Information Department",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1970"),
        Some("1970")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.3C",
      "current description",
      AgentDescription(
        s"$baseUriAgent.3C",
        "Overseas Finance, International Finance, IF1 International Financial Institutions and Debt Division",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1990"),
        Some("1990")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.988",
      "current description",
      AgentDescription(
        s"$baseUriAgent.988",
        "Oxford University Archives",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1634"),
        Some("1634")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.TWX",
      "current description",
      AgentDescription(
        s"$baseUriAgent.TWX",
        "Oxford University Press",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1633"),
        Some("1633")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.79L",
      "current description",
      AgentDescription(
        s"$baseUriAgent.79L",
        "Palace Court",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1660"),
        Some("1660")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.TX6",
      "current description",
      AgentDescription(
        s"$baseUriAgent.TX6",
        "Parker Inquiry",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        None,
        None
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.VY4",
      "current description",
      AgentDescription(
        s"$baseUriAgent.VY4",
        "Paymaster General of the Court of Chancery, Supreme Court Pay Office",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1884"),
        Some("1884")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.VX3",
      "current description",
      AgentDescription(
        s"$baseUriAgent.VX3",
        "Persona Associates Ltd",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1989"),
        Some("1989")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.V36",
      "current description",
      AgentDescription(
        s"$baseUriAgent.V36",
        "Petty Bag Office",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        None,
        None
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.8R6",
      "current description",
      AgentDescription(
        s"$baseUriAgent.8R6",
        "Queen Anne's Bounty",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        None,
        None
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.SH2",
      "current description",
      AgentDescription(
        s"$baseUriAgent.SH2",
        "Queen's Own Dragoons, 1788-1818",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        None,
        None
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.79X",
      "current description",
      AgentDescription(
        s"$baseUriAgent.79X",
        "Queens Prison",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1842"),
        Some("1842")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.W91",
      "current description",
      AgentDescription(
        s"$baseUriAgent.W91",
        "Queen's Printer for Scotland",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1999"),
        Some("1999")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.F11",
      "current description",
      AgentDescription(
        s"$baseUriAgent.F11",
        "Radioactive Substances Advisory Committee",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1948"),
        Some("1948")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.CYY",
      "current description",
      AgentDescription(
        s"$baseUriAgent.CYY",
        "Railway Executive",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1947"),
        Some("1947")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.CXY",
      "current description",
      AgentDescription(
        s"$baseUriAgent.CXY",
        "Railway Executive",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1914"),
        Some("1914")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.CY1",
      "current description",
      AgentDescription(
        s"$baseUriAgent.CY1",
        "Railway Executive",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1939"),
        Some("1939")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.TXH",
      "current description",
      AgentDescription(
        s"$baseUriAgent.TXH",
        "SaBRE",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("2002"),
        Some("2002")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.739",
      "current description",
      AgentDescription(
        s"$baseUriAgent.739",
        "Scaccarium Superius",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        None,
        None
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.NWN",
      "current description",
      AgentDescription(
        s"$baseUriAgent.NWN",
        "School of Anti-Aircraft Artillery",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1942"),
        Some("1942")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.SGS",
      "current description",
      AgentDescription(
        s"$baseUriAgent.SGS",
        "Scots Greys, 1877-1921",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        None,
        None
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.VXR",
      "current description",
      AgentDescription(
        s"$baseUriAgent.VXR",
        "Takeover Panel",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        None,
        None
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.QQR",
      "current description",
      AgentDescription(
        s"$baseUriAgent.QQR",
        "Tate Gallery",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1897"),
        Some("1897")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.63K",
      "current description",
      AgentDescription(
        s"$baseUriAgent.63K",
        "Tate Gallery Archive",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1970"),
        Some("1970")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.G91",
      "current description",
      AgentDescription(
        s"$baseUriAgent.G91",
        "Thalidomide Y List Inquiry",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1978"),
        Some("1978")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.FKS",
      "current description",
      AgentDescription(
        s"$baseUriAgent.FKS",
        "The Buying Agency",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1991"),
        Some("1991")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.JLC",
      "current description",
      AgentDescription(
        s"$baseUriAgent.JLC",
        "The Crown Estate, Other Urban Estates, Foreshore and Seabed Branches",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1973"),
        Some("1973")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.SYL",
      "current description",
      AgentDescription(
        s"$baseUriAgent.SYL",
        "Uhlans Britanniques de Sainte-Domingue (Charmilly's), 1794-1795",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        None,
        None
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.TXK",
      "current description",
      AgentDescription(
        s"$baseUriAgent.TXK",
        "UK Passport Service",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1991"),
        Some("1991")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.V3H",
      "current description",
      AgentDescription(
        s"$baseUriAgent.V3H",
        "UK Web Archiving Consortium",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        None,
        None
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.CCX",
      "current description",
      AgentDescription(
        s"$baseUriAgent.CCX",
        "United Kingdom Atomic Energy Authority, Atomic Weapons Research Establishment, Directors Office",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1954"),
        Some("1954")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.VTY",
      "current description",
      AgentDescription(
        s"$baseUriAgent.VTY",
        "Valuation Office Agency",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1991"),
        Some("19910")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.9HJ",
      "current description",
      AgentDescription(
        s"$baseUriAgent.9HJ",
        "Venetian Republic",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("727"),
        Some("727")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.QYF",
      "current description",
      AgentDescription(
        s"$baseUriAgent.QYF",
        "Victoria and Albert Museum",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1857"),
        Some("1857")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.61H",
      "current description",
      AgentDescription(
        s"$baseUriAgent.61H",
        "Victoria & Albert Museum, Archive of Art and Design",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1992"),
        Some("1992")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.W9K",
      "current description",
      AgentDescription(
        s"$baseUriAgent.W9K",
        "Wales Tourist Board",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1969"),
        Some("1969")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.VRG",
      "current description",
      AgentDescription(
        s"$baseUriAgent.VRG",
        "Walker Art Gallery",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1873"),
        Some("1873")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.61J",
      "current description",
      AgentDescription(
        s"$baseUriAgent.61J",
        "Wallace Collection",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1897"),
        Some("1897")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.HXV",
      "current description",
      AgentDescription(
        s"$baseUriAgent.HXV",
        "War and Colonial Department, Commissioners for liquidating the Danish and Dutch loans for St Thomas and St John",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1808"),
        Some("1808")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.V2R",
      "current description",
      AgentDescription(
        s"$baseUriAgent.V2R",
        "Zahid Mubarek Inquiry",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("2004"),
        Some("2004")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.763",
      "current description",
      AgentDescription(
        s"$baseUriAgent.763",
        "Zambia Department, Commonwealth Office",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1967"),
        Some("1967")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.765",
      "current description",
      AgentDescription(
        s"$baseUriAgent.765",
        "Zambia, Malawi and Southern Africa Department",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        Some("1968"),
        Some("1968")
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.G2Y",
      "current description",
      AgentDescription(
        s"$baseUriAgent.G2Y",
        "Zuckerman Working Party",
        Some(false),
        Some(false),
        "2022-06-22T02:00:00-0500",
        None,
        None
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.63F",
      "current description",
      AgentDescription(
        s"$baseUriAgent.63F",
        "British Museum, Central Archive",
        Some(false),
        Some(true),
        "2022-06-22T02:00:00-0500",
        Some("2001"),
        Some("2001"),
        None
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.614",
      "current description",
      AgentDescription(
        s"$baseUriAgent.614",
        "British Library, Sound Archive",
        Some(false),
        Some(true),
        "2022-06-22T02:00:00-0500",
        Some("1983"),
        Some("1983"),
        None
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.S2",
      "current description",
      AgentDescription(
        s"$baseUriAgent.S2",
        "The National Archives",
        Some(false),
        Some(true),
        "2022-06-22T02:00:00-0500",
        Some("2003"),
        None,
        None
      )
    )
  )
}
