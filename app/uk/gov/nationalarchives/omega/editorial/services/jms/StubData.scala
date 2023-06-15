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

  val baseUri: String = "http://catalogue.nationalarchives.gov.uk"

  val tna: String = s"""$baseUri/agent.S2"""
  val britishMuseum: String = s"""$baseUri/agent.63F"""
  val britishLibrary: String = s"""$baseUri/agent.614"""

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
      legalStatusID = "http://catalogue.nationalarchives.gov.uk/public-record",
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
      legalStatusID = "http://catalogue.nationalarchives.gov.uk/non-public-record",
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
      legalStatusID = "http://catalogue.nationalarchives.gov.uk/public-record",
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
      legalStatusID = "http://catalogue.nationalarchives.gov.uk/public-record",
      placeOfDepositID = tna,
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
      legalStatusID = "http://catalogue.nationalarchives.gov.uk/public-record",
      placeOfDepositID = tna,
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
      legalStatusID = "http://catalogue.nationalarchives.gov.uk/public-record",
      placeOfDepositID = tna,
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
      legalStatusID = "http://catalogue.nationalarchives.gov.uk/public-record",
      placeOfDepositID = tna,
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
      legalStatusID = "http://catalogue.nationalarchives.gov.uk/public-record",
      placeOfDepositID = tna,
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
      legalStatusID = "http://catalogue.nationalarchives.gov.uk/public-record",
      placeOfDepositID = tna,
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
      legalStatusID = "http://catalogue.nationalarchives.gov.uk/public-record",
      placeOfDepositID = tna,
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
      legalStatusID = "http://catalogue.nationalarchives.gov.uk/public-record",
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

  def getLegalStatuses(): Seq[LegalStatus] = Seq(
    LegalStatus("http://catalogue.nationalarchives.gov.uk/public-record", "Public Record"),
    LegalStatus("http://catalogue.nationalarchives.gov.uk/non-public-record", "Non-Public Record"),
    LegalStatus(
      "http://catalogue.nationalarchives.gov.uk/public-record-unless-otherwise-stated",
      "Public Record (unless otherwise stated)"
    ),
    LegalStatus("http://catalogue.nationalarchives.gov.uk/welsh-public-record", "Welsh Public Record"),
    LegalStatus("http://catalogue.nationalarchives.gov.uk/non-record-material", "Non-Record Material")
  )

  def getAgentSummaries(): Seq[AgentSummary] = List(
    AgentSummary(AgentType.Person, "3RX", "Abbot, Charles", Some("1798"), Some("1867")),
    AgentSummary(AgentType.Person, "48N", "Baden-Powell, Lady Olave St Clair", Some("1889"), Some("1977")),
    AgentSummary(AgentType.Person, "39K", "Cannon, John Francis Michael", Some("1930"), None),
    AgentSummary(AgentType.Person, "3FH", "Dainton, Sir Frederick Sydney", Some("1914"), Some("1997")),
    AgentSummary(AgentType.Person, "54J", "Edward, ", Some("1330"), Some("1376")),
    AgentSummary(AgentType.Person, "2QX", "Edward VII", Some("1841"), Some("1910")),
    AgentSummary(AgentType.Person, "561", "Fanshawe, Baron, of Richmond, ", None, None),
    AgentSummary(AgentType.Person, "46F", "Fawkes, Guy", Some("1570"), Some("1606")),
    AgentSummary(AgentType.Person, "2JN", "George, David Lloyd", Some("1863"), Some("1945")),
    AgentSummary(AgentType.Person, "34X", "Halley, Edmund", Some("1656"), Some("1742")),
    AgentSummary(AgentType.Person, "2TK", "Halifax, ", None, None),
    AgentSummary(AgentType.Person, "39T", "Irvine, Linda Mary", Some("1928"), None),
    AgentSummary(AgentType.Person, "4", "Jack the Ripper, ", Some("1888"), Some("1888")),
    AgentSummary(AgentType.Person, "4FF", "Keay, Sir Lancelot Herman", Some("1883"), Some("1974")),
    AgentSummary(AgentType.Person, "ST", "Lawson, Nigel", Some("1932"), None),
    AgentSummary(AgentType.Person, "51X", "Macpherson, Sir William (Alan)", Some("1926"), None),
    AgentSummary(AgentType.Person, "515", "Newcastle, 1st Duke of, ", None, None),
    AgentSummary(AgentType.Person, "4VF", "Old Pretender, The", None, None),
    AgentSummary(AgentType.Person, "4H3", "Oliphant, Sir Mark Marcus Laurence Elwin", Some("1901"), Some("2000")),
    AgentSummary(AgentType.Person, "46W", "Paine, Thomas", Some("1737"), Some("1809")),
    AgentSummary(AgentType.Person, "3SH", "Reade, Hubert Granville Revell", Some("1859"), Some("1938")),
    AgentSummary(AgentType.Person, "2TF", "Reading, ", None, None),
    AgentSummary(AgentType.Person, "53T", "Salisbury, Sir Edward James", Some("1886"), Some("1978")),
    AgentSummary(AgentType.Person, "3QL", "Tate, Sir Henry", Some("1819"), Some("1899")),
    AgentSummary(AgentType.Person, "37K", "Uvarov, Sir Boris Petrovitch", Some("1889"), Some("1970")),
    AgentSummary(AgentType.Person, "2T1", "Vane-Tempest-Stewart, Charles Stewart", Some("1852"), Some("1915")),
    AgentSummary(AgentType.Person, "4RW", "Victor Amadeus, ", Some("1666"), Some("1732")),
    AgentSummary(AgentType.Person, "3GY", "Victoria, ", Some("1819"), Some("1901")),
    AgentSummary(
      AgentType.CorporateBody,
      "RR6",
      "100th (Gordon Highlanders) Regiment of Foot",
      Some("1794"),
      Some("1794")
    ),
    AgentSummary(AgentType.CorporateBody, "S34", "1st Regiment of Foot or Royal Scots", Some("1812"), Some("1812")),
    AgentSummary(AgentType.CorporateBody, "87K", "Abbotsbury Railway Company", Some("1877"), Some("1877")),
    AgentSummary(
      AgentType.CorporateBody,
      "VWG",
      "Accountant General in the Court of Chancery",
      Some("1726"),
      Some("1726")
    ),
    AgentSummary(
      AgentType.CorporateBody,
      "LWY",
      "Admiralty Administrative Whitley Council, General Purposes Committee",
      Some("1942"),
      Some("1942")
    ),
    AgentSummary(
      AgentType.CorporateBody,
      "VS6",
      "Advisory Committee on Animal Feedingstuffs",
      Some("1999"),
      Some("1999")
    ),
    AgentSummary(AgentType.CorporateBody, "CC", "Bank of England", Some("1694"), Some("1694")),
    AgentSummary(
      AgentType.CorporateBody,
      "N9S",
      "Bank on Tickets of the Million Adventure",
      Some("1695"),
      Some("1695")
    ),
    AgentSummary(AgentType.CorporateBody, "JS8", "BBC", None, None),
    AgentSummary(AgentType.CorporateBody, "8WG", "Bee Husbandry Committee", Some("1959"), Some("1959")),
    AgentSummary(AgentType.CorporateBody, "6VQ", "Cabinet", Some("1919"), Some("1919")),
    AgentSummary(AgentType.CorporateBody, "SV", "Cabinet", Some("1945"), Some("1945")),
    AgentSummary(
      AgentType.CorporateBody,
      "5V4",
      "Cabinet, Committee for Control of Official Histories",
      Some("1946"),
      Some("1946")
    ),
    AgentSummary(AgentType.CorporateBody, "GW5", "Cattle Emergency Committee", Some("1934"), Some("1934")),
    AgentSummary(AgentType.CorporateBody, "934", "Dairy Crest", Some("1981"), Some("1981")),
    AgentSummary(AgentType.CorporateBody, "9HC", "Dean of the Chapel Royal", None, None),
    AgentSummary(
      AgentType.CorporateBody,
      "WGL",
      "Department for Environment, Food and Rural Affairs, Water Quality Division",
      Some("2002"),
      Some("2002")
    ),
    AgentSummary(
      AgentType.CorporateBody,
      "WJ4",
      "Department for Exiting the European Union",
      Some("2016"),
      Some("2016")
    ),
    AgentSummary(
      AgentType.CorporateBody,
      "9YJ",
      "East Grinstead, Groombridge and Tunbridge Wells Railway Company",
      Some("1862"),
      Some("1862")
    ),
    AgentSummary(AgentType.CorporateBody, "HF4", "East India Company", Some("1600"), Some("1600")),
    AgentSummary(AgentType.CorporateBody, "WN3", "Education and Skills Funding Agency", Some("2017"), Some("2017")),
    AgentSummary(AgentType.CorporateBody, "WNL", "Education and Skills Funding Agency", Some("2017"), Some("2017")),
    AgentSummary(AgentType.CorporateBody, "Q1R", "Falkland Islands Company", Some("1899"), Some("1899")),
    AgentSummary(AgentType.CorporateBody, "SQ9", "Fish's Corps of Foot", Some("1782"), Some("1782")),
    AgentSummary(
      AgentType.CorporateBody,
      "R6R",
      "Foreign and Commonwealth Office, Consulate, Dusseldorf, West Germany",
      Some("1968"),
      Some("1968")
    ),
    AgentSummary(
      AgentType.CorporateBody,
      "HKL",
      "Foreign Office, Consulate, Angora and Konieh, Ottoman Empire",
      Some("1895"),
      Some("1895")
    ),
    AgentSummary(AgentType.CorporateBody, "KSC", "Gaming Board for Great Britain", Some("1968"), Some("1968")),
    AgentSummary(AgentType.CorporateBody, "73R", "GCHQ", None, None),
    AgentSummary(AgentType.CorporateBody, "VR1", "Geffrye Museum", Some("1914"), Some("1914")),
    AgentSummary(
      AgentType.CorporateBody,
      "QX5",
      "General Nursing Council for England and Wales, Registration and Enrolment Committee",
      Some("1970"),
      Some("1970")
    ),
    AgentSummary(
      AgentType.CorporateBody,
      "C1Y",
      "Halifax High Level and North and South Junction Railway Company",
      Some("1884"),
      Some("1884")
    ),
    AgentSummary(AgentType.CorporateBody, "W2T", "Hansard Society", Some("1944"), Some("1944")),
    AgentSummary(
      AgentType.CorporateBody,
      "F18",
      "Health and Safety Commission, Health and Safety Executive, Employment Medical Advisory Service",
      Some("1975"),
      Some("1975")
    ),
    AgentSummary(AgentType.CorporateBody, "8JK", "Her Majesty's Stationery Office", Some("1986"), Some("1986")),
    AgentSummary(AgentType.CorporateBody, "9FV", "Ideal Benefit Society", Some("1912"), Some("1912")),
    AgentSummary(
      AgentType.CorporateBody,
      "5YX",
      "Imperial War Museum: Churchill Museum and Cabinet War Rooms",
      Some("1939"),
      Some("1939")
    ),
    AgentSummary(
      AgentType.CorporateBody,
      "W1Q",
      "Independent Expert Group on Mobile Phones",
      Some("1999"),
      Some("1999")
    ),
    AgentSummary(
      AgentType.CorporateBody,
      "QLY",
      "Independent Expert Group on Mobile Phones",
      Some("1999"),
      Some("1999")
    ),
    AgentSummary(AgentType.CorporateBody, "LS5", "Jodrell Bank Observatory, Cheshire", Some("1955"), Some("1955")),
    AgentSummary(AgentType.CorporateBody, "92W", "Joint Milk Quality Committee", Some("1948"), Some("1948")),
    AgentSummary(
      AgentType.CorporateBody,
      "L3W",
      "Justices in Eyre, of Assize, of Gaol Delivery, of Oyer and Terminer, of the Peace, and of Nisi Prius",
      None,
      None
    ),
    AgentSummary(AgentType.CorporateBody, "N8X", "Justices of the Forest", Some("1166"), Some("1166")),
    AgentSummary(AgentType.CorporateBody, "THY", "Kew Gardens Archive", None, None),
    AgentSummary(AgentType.CorporateBody, "SGX", "King's Own Dragoons, 1751-1818", None, None),
    AgentSummary(
      AgentType.CorporateBody,
      "CCR",
      "Knitting, Lace and Net Industry Training Board",
      Some("1966"),
      Some("1966")
    ),
    AgentSummary(AgentType.CorporateBody, "TTT", "King's Volunteers Regiment of Foot, 1761-1763", None, None),
    AgentSummary(AgentType.CorporateBody, "VR7", "Lady Lever Art Gallery", Some("1922"), Some("1922")),
    AgentSummary(AgentType.CorporateBody, "XQ", "Law Society", Some("1825"), Some("1825")),
    AgentSummary(AgentType.CorporateBody, "91W", "League of Mercy", Some("1898"), Some("1898")),
    AgentSummary(AgentType.CorporateBody, "VX", "Legal Aid Board", Some("1989"), Some("1989")),
    AgentSummary(AgentType.CorporateBody, "TXG", "Legal Aid Board, 1988-1989", None, None),
    AgentSummary(AgentType.CorporateBody, "6LL", "Machinery of Government Committee", Some("1917"), Some("1917")),
    AgentSummary(AgentType.CorporateBody, "G6N", "Magnetic Department", Some("1839"), Some("1839")),
    AgentSummary(AgentType.CorporateBody, "71K", "Manpower Distribution Board", Some("1916"), Some("1916")),
    AgentSummary(AgentType.CorporateBody, "KN1", "Master of the Rolls Archives Committee", Some("1925"), Some("1925")),
    AgentSummary(
      AgentType.CorporateBody,
      "J6X",
      "National Agricultural Advisory Service, Great House Experimental Husbandry Farm",
      Some("1951"),
      Some("1951")
    ),
    AgentSummary(
      AgentType.CorporateBody,
      "K7N",
      "National Air Traffic Control Services, Director General Projects and Engineering, Directorate of Projects",
      Some("1963"),
      Some("1963")
    ),
    AgentSummary(AgentType.CorporateBody, "TSL", "National Archives, The", None, None),
    AgentSummary(
      AgentType.CorporateBody,
      "LSN",
      "Navy Board, Transport Branch, Prisoner of War Department",
      Some("1817"),
      Some("1817")
    ),
    AgentSummary(AgentType.CorporateBody, "W1S", "Office for Budget Responsibility", Some("2010"), Some("2010")),
    AgentSummary(
      AgentType.CorporateBody,
      "N4W",
      "Office of Population Censuses and Surveys, Computer Division",
      Some("1972"),
      Some("1972")
    ),
    AgentSummary(
      AgentType.CorporateBody,
      "QQC",
      "Office of Works, Directorate of Works, Maintenance Surveyors Division, Sanitary Engineers Section",
      Some("1928"),
      Some("1928")
    ),
    AgentSummary(
      AgentType.CorporateBody,
      "QFY",
      "Office of the President of Social Security Appeal Tribunals, Medical Appeal Tribunals and Vaccine Damage Tribunals",
      Some("1984"),
      Some("1984")
    ),
    AgentSummary(
      AgentType.CorporateBody,
      "VYJ",
      "Ordnance Survey of Great Britain, Directorate of Data Collection and Management",
      Some("2003"),
      Some("2003")
    ),
    AgentSummary(
      AgentType.CorporateBody,
      "8FX",
      "Overseas Development Administration, Information Department",
      Some("1970"),
      Some("1970")
    ),
    AgentSummary(
      AgentType.CorporateBody,
      "3C",
      "Overseas Finance, International Finance, IF1 International Financial Institutions and Debt Division",
      Some("1990"),
      Some("1990")
    ),
    AgentSummary(AgentType.CorporateBody, "988", "Oxford University Archives", Some("1634"), Some("1634")),
    AgentSummary(AgentType.CorporateBody, "TWX", "Oxford University Press", Some("1633"), Some("1633")),
    AgentSummary(AgentType.CorporateBody, "79L", "Palace Court", Some("1660"), Some("1660")),
    AgentSummary(AgentType.CorporateBody, "TX6", "Parker Inquiry", None, None),
    AgentSummary(
      AgentType.CorporateBody,
      "VY4",
      "Paymaster General of the Court of Chancery, Supreme Court Pay Office",
      Some("1884"),
      Some("1884")
    ),
    AgentSummary(AgentType.CorporateBody, "VX3", "Persona Associates Ltd", Some("1989"), Some("1989")),
    AgentSummary(AgentType.CorporateBody, "V36", "Petty Bag Office", None, None),
    AgentSummary(AgentType.CorporateBody, "8R6", "Queen Anne's Bounty", None, None),
    AgentSummary(AgentType.CorporateBody, "SH2", "Queen's Own Dragoons, 1788-1818", None, None),
    AgentSummary(AgentType.CorporateBody, "79X", "Queens Prison", Some("1842"), Some("1842")),
    AgentSummary(AgentType.CorporateBody, "W91", "Queen's Printer for Scotland", Some("1999"), Some("1999")),
    AgentSummary(
      AgentType.CorporateBody,
      "F11",
      "Radioactive Substances Advisory Committee",
      Some("1948"),
      Some("1948")
    ),
    AgentSummary(AgentType.CorporateBody, "CYY", "Railway Executive", Some("1947"), Some("1947")),
    AgentSummary(AgentType.CorporateBody, "CXY", "Railway Executive", Some("1914"), Some("1914")),
    AgentSummary(AgentType.CorporateBody, "CY1", "Railway Executive", Some("1939"), Some("1939")),
    AgentSummary(AgentType.CorporateBody, "TXH", "SaBRE", Some("2002"), Some("2002")),
    AgentSummary(AgentType.CorporateBody, "739", "Scaccarium Superius", None, None),
    AgentSummary(AgentType.CorporateBody, "NWN", "School of Anti-Aircraft Artillery", Some("1942"), Some("1942")),
    AgentSummary(AgentType.CorporateBody, "SGS", "Scots Greys, 1877-1921", None, None),
    AgentSummary(AgentType.CorporateBody, "VXR", "Takeover Panel", None, None),
    AgentSummary(AgentType.CorporateBody, "QQR", "Tate Gallery", Some("1897"), Some("1897")),
    AgentSummary(AgentType.CorporateBody, "63K", "Tate Gallery Archive", Some("1970"), Some("1970")),
    AgentSummary(AgentType.CorporateBody, "G91", "Thalidomide Y List Inquiry", Some("1978"), Some("1978")),
    AgentSummary(AgentType.CorporateBody, "FKS", "The Buying Agency", Some("1991"), Some("1991")),
    AgentSummary(
      AgentType.CorporateBody,
      "JLC",
      "The Crown Estate, Other Urban Estates, Foreshore and Seabed Branches",
      Some("1973"),
      Some("1973")
    ),
    AgentSummary(
      AgentType.CorporateBody,
      "SYL",
      "Uhlans Britanniques de Sainte-Domingue (Charmilly's), 1794-1795",
      None,
      None
    ),
    AgentSummary(AgentType.CorporateBody, "TXK", "UK Passport Service", Some("1991"), Some("1991")),
    AgentSummary(AgentType.CorporateBody, "V3H", "UK Web Archiving Consortium", None, None),
    AgentSummary(
      AgentType.CorporateBody,
      "CCX",
      "United Kingdom Atomic Energy Authority, Atomic Weapons Research Establishment, Directors Office",
      Some("1954"),
      Some("1954")
    ),
    AgentSummary(AgentType.CorporateBody, "VTY", "Valuation Office Agency", Some("1991"), Some("19910")),
    AgentSummary(AgentType.CorporateBody, "9HJ", "Venetian Republic", Some("727"), Some("727")),
    AgentSummary(AgentType.CorporateBody, "QYF", "Victoria and Albert Museum", Some("1857"), Some("1857")),
    AgentSummary(
      AgentType.CorporateBody,
      "61H",
      "Victoria & Albert Museum, Archive of Art and Design",
      Some("1992"),
      Some("1992")
    ),
    AgentSummary(AgentType.CorporateBody, "W9K", "Wales Tourist Board", Some("1969"), Some("1969")),
    AgentSummary(AgentType.CorporateBody, "VRG", "Walker Art Gallery", Some("1873"), Some("1873")),
    AgentSummary(AgentType.CorporateBody, "61J", "Wallace Collection", Some("1897"), Some("1897")),
    AgentSummary(
      AgentType.CorporateBody,
      "HXV",
      "War and Colonial Department, Commissioners for liquidating the Danish and Dutch loans for St Thomas and St John",
      Some("1808"),
      Some("1808")
    ),
    AgentSummary(AgentType.CorporateBody, "V2R", "Zahid Mubarek Inquiry", Some("2004"), Some("2004")),
    AgentSummary(AgentType.CorporateBody, "763", "Zambia Department, Commonwealth Office", Some("1967"), Some("1967")),
    AgentSummary(
      AgentType.CorporateBody,
      "765",
      "Zambia, Malawi and Southern Africa Department",
      Some("1968"),
      Some("1968")
    ),
    AgentSummary(AgentType.CorporateBody, "G2Y", "Zuckerman Working Party", None, None),
    AgentSummary(
      AgentType.CorporateBody,
      britishMuseum,
      "British Museum, Department of Libraries and Archives",
      Some("2001"),
      Some("2001"),
      Some(true)
    ),
    AgentSummary(
      AgentType.CorporateBody,
      britishLibrary,
      "British Library, National Sound Archive",
      Some("1983"),
      Some("1983"),
      Some(true)
    ),
    AgentSummary(
      AgentType.CorporateBody,
      tna,
      "The National Archives, Kew",
      Some("2003"),
      None,
      Some(true)
    )
  )
}
