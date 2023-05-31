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
      legalStatusID = "http://catalogue.nationalarchives.gov.uk/public-record",
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
      legalStatusID = "http://catalogue.nationalarchives.gov.uk/public-record",
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
      legalStatusID = "http://catalogue.nationalarchives.gov.uk/public-record",
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
      legalStatusID = "http://catalogue.nationalarchives.gov.uk/public-record",
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
      legalStatusID = "http://catalogue.nationalarchives.gov.uk/public-record",
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
      legalStatusID = "http://catalogue.nationalarchives.gov.uk/public-record",
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
      legalStatusID = "http://catalogue.nationalarchives.gov.uk/public-record",
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
      legalStatusID = "http://catalogue.nationalarchives.gov.uk/public-record",
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

  def getPlacesOfDeposit(): Seq[PlaceOfDeposit] = Seq(
    PlaceOfDeposit("1", "The National Archives, Kew"),
    PlaceOfDeposit("2", "British Museum, Department of Libraries and Archives"),
    PlaceOfDeposit("3", "British Library, National Sound Archive")
  )

  def getAgentSummaries(): Seq[AgentSummary] = List(
    AgentSummary(AgentType.Person, "3RX", "Abbot, Charles", "1798", "1867"),
    AgentSummary(AgentType.Person, "48N", "Baden-Powell, Lady Olave St Clair", "1889", "1977"),
    AgentSummary(AgentType.Person, "39K", "Cannon, John Francis Michael", "1930", ""),
    AgentSummary(AgentType.Person, "3FH", "Dainton, Sir Frederick Sydney", "1914", "1997"),
    AgentSummary(AgentType.Person, "54J", "Edward, ", "1330", "1376"),
    AgentSummary(AgentType.Person, "2QX", "Edward VII", "1841", "1910"),
    AgentSummary(AgentType.Person, "561", "Fanshawe, Baron, of Richmond, ", "", ""),
    AgentSummary(AgentType.Person, "46F", "Fawkes, Guy", "1570", "1606"),
    AgentSummary(AgentType.Person, "2JN", "George, David Lloyd", "1863", "1945"),
    AgentSummary(AgentType.Person, "34X", "Halley, Edmund", "1656", "1742"),
    AgentSummary(AgentType.Person, "2TK", "Halifax, ", "", ""),
    AgentSummary(AgentType.Person, "39T", "Irvine, Linda Mary", "1928", ""),
    AgentSummary(AgentType.Person, "4", "Jack the Ripper, ", "1888", "1888"),
    AgentSummary(AgentType.Person, "4FF", "Keay, Sir Lancelot Herman", "1883", "1974"),
    AgentSummary(AgentType.Person, "ST", "Lawson, Nigel", "1932", ""),
    AgentSummary(AgentType.Person, "51X", "Macpherson, Sir William (Alan)", "1926", ""),
    AgentSummary(AgentType.Person, "515", "Newcastle, 1st Duke of, ", "", ""),
    AgentSummary(AgentType.Person, "4VF", "Old Pretender, The", "", ""),
    AgentSummary(AgentType.Person, "4H3", "Oliphant, Sir Mark Marcus Laurence Elwin", "1901", "2000"),
    AgentSummary(AgentType.Person, "46W", "Paine, Thomas", "1737", "1809"),
    AgentSummary(AgentType.Person, "3SH", "Reade, Hubert Granville Revell", "1859", "1938"),
    AgentSummary(AgentType.Person, "2TF", "Reading, ", "", ""),
    AgentSummary(AgentType.Person, "53T", "Salisbury, Sir Edward James", "1886", "1978"),
    AgentSummary(AgentType.Person, "3QL", "Tate, Sir Henry", "1819", "1899"),
    AgentSummary(AgentType.Person, "37K", "Uvarov, Sir Boris Petrovitch", "1889", "1970"),
    AgentSummary(AgentType.Person, "2T1", "Vane-Tempest-Stewart, Charles Stewart", "1852", "1915"),
    AgentSummary(AgentType.Person, "4RW", "Victor Amadeus, ", "1666", "1732"),
    AgentSummary(AgentType.Person, "3GY", "Victoria, ", "1819", "1901"),
    AgentSummary(AgentType.CorporateBody, "RR6", "100th (Gordon Highlanders) Regiment of Foot", "1794", "1794"),
    AgentSummary(AgentType.CorporateBody, "S34", "1st Regiment of Foot or Royal Scots", "1812", "1812"),
    AgentSummary(AgentType.CorporateBody, "87K", "Abbotsbury Railway Company", "1877", "1877"),
    AgentSummary(AgentType.CorporateBody, "VWG", "Accountant General in the Court of Chancery", "1726", "1726"),
    AgentSummary(
      AgentType.CorporateBody,
      "LWY",
      "Admiralty Administrative Whitley Council, General Purposes Committee",
      "1942",
      "1942"
    ),
    AgentSummary(AgentType.CorporateBody, "VS6", "Advisory Committee on Animal Feedingstuffs", "1999", "1999"),
    AgentSummary(AgentType.CorporateBody, "CC", "Bank of England", "1694", "1694"),
    AgentSummary(AgentType.CorporateBody, "N9S", "Bank on Tickets of the Million Adventure", "1695", "1695"),
    AgentSummary(AgentType.CorporateBody, "JS8", "BBC", "", ""),
    AgentSummary(AgentType.CorporateBody, "8WG", "Bee Husbandry Committee", "1959", "1959"),
    AgentSummary(AgentType.CorporateBody, "6VQ", "Cabinet", "1919", "1919"),
    AgentSummary(AgentType.CorporateBody, "SV", "Cabinet", "1945", "1945"),
    AgentSummary(
      AgentType.CorporateBody,
      "5V4",
      "Cabinet, Committee for Control of Official Histories",
      "1946",
      "1946"
    ),
    AgentSummary(AgentType.CorporateBody, "GW5", "Cattle Emergency Committee", "1934", "1934"),
    AgentSummary(AgentType.CorporateBody, "934", "Dairy Crest", "1981", "1981"),
    AgentSummary(AgentType.CorporateBody, "9HC", "Dean of the Chapel Royal", "", ""),
    AgentSummary(
      AgentType.CorporateBody,
      "WGL",
      "Department for Environment, Food and Rural Affairs, Water Quality Division",
      "2002",
      "2002"
    ),
    AgentSummary(AgentType.CorporateBody, "WJ4", "Department for Exiting the European Union", "2016", "2016"),
    AgentSummary(
      AgentType.CorporateBody,
      "9YJ",
      "East Grinstead, Groombridge and Tunbridge Wells Railway Company",
      "1862",
      "1862"
    ),
    AgentSummary(AgentType.CorporateBody, "HF4", "East India Company", "1600", "1600"),
    AgentSummary(AgentType.CorporateBody, "WN3", "Education and Skills Funding Agency", "2017", "2017"),
    AgentSummary(AgentType.CorporateBody, "WNL", "Education and Skills Funding Agency", "2017", "2017"),
    AgentSummary(AgentType.CorporateBody, "Q1R", "Falkland Islands Company", "1899", "1899"),
    AgentSummary(AgentType.CorporateBody, "SQ9", "Fish's Corps of Foot", "1782", "1782"),
    AgentSummary(
      AgentType.CorporateBody,
      "R6R",
      "Foreign and Commonwealth Office, Consulate, Dusseldorf, West Germany",
      "1968",
      "1968"
    ),
    AgentSummary(
      AgentType.CorporateBody,
      "HKL",
      "Foreign Office, Consulate, Angora and Konieh, Ottoman Empire",
      "1895",
      "1895"
    ),
    AgentSummary(AgentType.CorporateBody, "KSC", "Gaming Board for Great Britain", "1968", "1968"),
    AgentSummary(AgentType.CorporateBody, "73R", "GCHQ", "", ""),
    AgentSummary(AgentType.CorporateBody, "VR1", "Geffrye Museum", "1914", "1914"),
    AgentSummary(
      AgentType.CorporateBody,
      "QX5",
      "General Nursing Council for England and Wales, Registration and Enrolment Committee",
      "1970",
      "1970"
    ),
    AgentSummary(
      AgentType.CorporateBody,
      "C1Y",
      "Halifax High Level and North and South Junction Railway Company",
      "1884",
      "1884"
    ),
    AgentSummary(AgentType.CorporateBody, "W2T", "Hansard Society", "1944", "1944"),
    AgentSummary(
      AgentType.CorporateBody,
      "F18",
      "Health and Safety Commission, Health and Safety Executive, Employment Medical Advisory Service",
      "1975",
      "1975"
    ),
    AgentSummary(AgentType.CorporateBody, "8JK", "Her Majesty's Stationery Office", "1986", "1986"),
    AgentSummary(AgentType.CorporateBody, "9FV", "Ideal Benefit Society", "1912", "1912"),
    AgentSummary(
      AgentType.CorporateBody,
      "5YX",
      "Imperial War Museum: Churchill Museum and Cabinet War Rooms",
      "1939",
      "1939"
    ),
    AgentSummary(AgentType.CorporateBody, "W1Q", "Independent Expert Group on Mobile Phones", "1999", "1999"),
    AgentSummary(AgentType.CorporateBody, "QLY", "Independent Expert Group on Mobile Phones", "1999", "1999"),
    AgentSummary(AgentType.CorporateBody, "LS5", "Jodrell Bank Observatory, Cheshire", "1955", "1955"),
    AgentSummary(AgentType.CorporateBody, "92W", "Joint Milk Quality Committee", "1948", "1948"),
    AgentSummary(
      AgentType.CorporateBody,
      "L3W",
      "Justices in Eyre, of Assize, of Gaol Delivery, of Oyer and Terminer, of the Peace, and of Nisi Prius",
      "",
      ""
    ),
    AgentSummary(AgentType.CorporateBody, "N8X", "Justices of the Forest", "1166", "1166"),
    AgentSummary(AgentType.CorporateBody, "THY", "Kew Gardens Archive", "", ""),
    AgentSummary(AgentType.CorporateBody, "SGX", "King's Own Dragoons, 1751-1818", "", ""),
    AgentSummary(
      AgentType.CorporateBody,
      "CCR",
      "Knitting, Lace and Net Industry Training Board",
      "1966",
      "1966"
    ),
    AgentSummary(AgentType.CorporateBody, "TTT", "King's Volunteers Regiment of Foot, 1761-1763", "", ""),
    AgentSummary(AgentType.CorporateBody, "VR7", "Lady Lever Art Gallery", "1922", "1922"),
    AgentSummary(AgentType.CorporateBody, "XQ", "Law Society", "1825", "1825"),
    AgentSummary(AgentType.CorporateBody, "91W", "League of Mercy", "1898", "1898"),
    AgentSummary(AgentType.CorporateBody, "VX", "Legal Aid Board", "1989", "1989"),
    AgentSummary(AgentType.CorporateBody, "TXG", "Legal Aid Board, 1988-1989", "", ""),
    AgentSummary(AgentType.CorporateBody, "6LL", "Machinery of Government Committee", "1917", "1917"),
    AgentSummary(AgentType.CorporateBody, "G6N", "Magnetic Department", "1839", "1839"),
    AgentSummary(AgentType.CorporateBody, "71K", "Manpower Distribution Board", "1916", "1916"),
    AgentSummary(AgentType.CorporateBody, "KN1", "Master of the Rolls Archives Committee", "1925", "1925"),
    AgentSummary(
      AgentType.CorporateBody,
      "J6X",
      "National Agricultural Advisory Service, Great House Experimental Husbandry Farm",
      "1951",
      "1951"
    ),
    AgentSummary(
      AgentType.CorporateBody,
      "K7N",
      "National Air Traffic Control Services, Director General Projects and Engineering, Directorate of Projects",
      "1963",
      "1963"
    ),
    AgentSummary(AgentType.CorporateBody, "TSL", "National Archives, The", "", ""),
    AgentSummary(
      AgentType.CorporateBody,
      "LSN",
      "Navy Board, Transport Branch, Prisoner of War Department",
      "1817",
      "1817"
    ),
    AgentSummary(AgentType.CorporateBody, "W1S", "Office for Budget Responsibility", "2010", "2010"),
    AgentSummary(
      AgentType.CorporateBody,
      "N4W",
      "Office of Population Censuses and Surveys, Computer Division",
      "1972",
      "1972"
    ),
    AgentSummary(
      AgentType.CorporateBody,
      "QQC",
      "Office of Works, Directorate of Works, Maintenance Surveyors Division, Sanitary Engineers Section",
      "1928",
      "1928"
    ),
    AgentSummary(
      AgentType.CorporateBody,
      "QFY",
      "Office of the President of Social Security Appeal Tribunals, Medical Appeal Tribunals and Vaccine Damage Tribunals",
      "1984",
      "1984"
    ),
    AgentSummary(
      AgentType.CorporateBody,
      "VYJ",
      "Ordnance Survey of Great Britain, Directorate of Data Collection and Management",
      "2003",
      "2003"
    ),
    AgentSummary(
      AgentType.CorporateBody,
      "8FX",
      "Overseas Development Administration, Information Department",
      "1970",
      "1970"
    ),
    AgentSummary(
      AgentType.CorporateBody,
      "3C",
      "Overseas Finance, International Finance, IF1 International Financial Institutions and Debt Division",
      "1990",
      "1990"
    ),
    AgentSummary(AgentType.CorporateBody, "988", "Oxford University Archives", "1634", "1634"),
    AgentSummary(AgentType.CorporateBody, "TWX", "Oxford University Press", "1633", "1633"),
    AgentSummary(AgentType.CorporateBody, "79L", "Palace Court", "1660", "1660"),
    AgentSummary(AgentType.CorporateBody, "TX6", "Parker Inquiry", "", ""),
    AgentSummary(
      AgentType.CorporateBody,
      "VY4",
      "Paymaster General of the Court of Chancery, Supreme Court Pay Office",
      "1884",
      "1884"
    ),
    AgentSummary(AgentType.CorporateBody, "VX3", "Persona Associates Ltd", "1989", "1989"),
    AgentSummary(AgentType.CorporateBody, "V36", "Petty Bag Office", "", ""),
    AgentSummary(AgentType.CorporateBody, "8R6", "Queen Anne's Bounty", "", ""),
    AgentSummary(AgentType.CorporateBody, "SH2", "Queen's Own Dragoons, 1788-1818", "", ""),
    AgentSummary(AgentType.CorporateBody, "79X", "Queens Prison", "1842", "1842"),
    AgentSummary(AgentType.CorporateBody, "W91", "Queen's Printer for Scotland", "1999", "1999"),
    AgentSummary(AgentType.CorporateBody, "F11", "Radioactive Substances Advisory Committee", "1948", "1948"),
    AgentSummary(AgentType.CorporateBody, "CYY", "Railway Executive", "1947", "1947"),
    AgentSummary(AgentType.CorporateBody, "CXY", "Railway Executive", "1914", "1914"),
    AgentSummary(AgentType.CorporateBody, "CY1", "Railway Executive", "1939", "1939"),
    AgentSummary(AgentType.CorporateBody, "TXH", "SaBRE", "2002", "2002"),
    AgentSummary(AgentType.CorporateBody, "739", "Scaccarium Superius", "", ""),
    AgentSummary(AgentType.CorporateBody, "NWN", "School of Anti-Aircraft Artillery", "1942", "1942"),
    AgentSummary(AgentType.CorporateBody, "SGS", "Scots Greys, 1877-1921", "", ""),
    AgentSummary(AgentType.CorporateBody, "VXR", "Takeover Panel", "", ""),
    AgentSummary(AgentType.CorporateBody, "QQR", "Tate Gallery", "1897", "1897"),
    AgentSummary(AgentType.CorporateBody, "63K", "Tate Gallery Archive", "1970", "1970"),
    AgentSummary(AgentType.CorporateBody, "G91", "Thalidomide Y List Inquiry", "1978", "1978"),
    AgentSummary(AgentType.CorporateBody, "FKS", "The Buying Agency", "1991", "1991"),
    AgentSummary(
      AgentType.CorporateBody,
      "JLC",
      "The Crown Estate, Other Urban Estates, Foreshore and Seabed Branches",
      "1973",
      "1973"
    ),
    AgentSummary(
      AgentType.CorporateBody,
      "SYL",
      "Uhlans Britanniques de Sainte-Domingue (Charmilly's), 1794-1795",
      "",
      ""
    ),
    AgentSummary(AgentType.CorporateBody, "TXK", "UK Passport Service", "1991", "1991"),
    AgentSummary(AgentType.CorporateBody, "V3H", "UK Web Archiving Consortium", "", ""),
    AgentSummary(
      AgentType.CorporateBody,
      "CCX",
      "United Kingdom Atomic Energy Authority, Atomic Weapons Research Establishment, Directors Office",
      "1954",
      "1954"
    ),
    AgentSummary(AgentType.CorporateBody, "VTY", "Valuation Office Agency", "1991", "19910"),
    AgentSummary(AgentType.CorporateBody, "9HJ", "Venetian Republic", "727", "727"),
    AgentSummary(AgentType.CorporateBody, "QYF", "Victoria and Albert Museum", "1857", "1857"),
    AgentSummary(
      AgentType.CorporateBody,
      "61H",
      "Victoria & Albert Museum, Archive of Art and Design",
      "1992",
      "1992"
    ),
    AgentSummary(AgentType.CorporateBody, "W9K", "Wales Tourist Board", "1969", "1969"),
    AgentSummary(AgentType.CorporateBody, "VRG", "Walker Art Gallery", "1873", "1873"),
    AgentSummary(AgentType.CorporateBody, "61J", "Wallace Collection", "1897", "1897"),
    AgentSummary(
      AgentType.CorporateBody,
      "HXV",
      "War and Colonial Department, Commissioners for liquidating the Danish and Dutch loans for St Thomas and St John",
      "1808",
      "1808"
    ),
    AgentSummary(AgentType.CorporateBody, "V2R", "Zahid Mubarek Inquiry", "2004", "2004"),
    AgentSummary(AgentType.CorporateBody, "763", "Zambia Department, Commonwealth Office", "1967", "1967"),
    AgentSummary(
      AgentType.CorporateBody,
      "765",
      "Zambia, Malawi and Southern Africa Department",
      "1968",
      "1968"
    ),
    AgentSummary(AgentType.CorporateBody, "G2Y", "Zuckerman Working Party", "", ""),
    AgentSummary(AgentType.CorporateBody, "63F", "British Museum Central Archive", "2001", "2001"),
    AgentSummary(AgentType.CorporateBody, "614", "British Library, Sound Archive", "1983", "1983"),
    AgentSummary(AgentType.CorporateBody, "S2", "The National Archives", "2003", "")
  )
}
