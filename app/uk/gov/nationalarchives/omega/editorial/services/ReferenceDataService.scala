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

package uk.gov.nationalarchives.omega.editorial.services

import cats.effect.IO
import uk.gov.nationalarchives.omega.editorial.connectors.ApiConnector
import uk.gov.nationalarchives.omega.editorial.models._
import uk.gov.nationalarchives.omega.editorial.support.TimeProvider

import javax.inject.{ Inject, Singleton }

@Singleton
class ReferenceDataService @Inject() (apiConnector: ApiConnector, timeProvider: TimeProvider) {

  def getCorporateBodies: Seq[CorporateBody] = Seq(
    CorporateBody("RR6", "100th (Gordon Highlanders) Regiment of Foot", Some(1794), Some(1794)),
    CorporateBody("S34", "1st Regiment of Foot or Royal Scots", Some(1812), Some(1812)),
    CorporateBody("87K", "Abbotsbury Railway Company", Some(1877), Some(1877)),
    CorporateBody("VWG", "Accountant General in the Court of Chancery", Some(1726), Some(1726)),
    CorporateBody(
      "LWY",
      "Admiralty Administrative Whitley Council, General Purposes Committee",
      Some(1942),
      Some(1942)
    ),
    CorporateBody("VS6", "Advisory Committee on Animal Feedingstuffs", Some(1999), Some(1999)),
    CorporateBody("CC", "Bank of England", Some(1694), Some(1694)),
    CorporateBody("N9S", "Bank on Tickets of the Million Adventure", Some(1695), Some(1695)),
    CorporateBody("JS8", "BBC", None, None),
    CorporateBody("8WG", "Bee Husbandry Committee", Some(1959), Some(1959)),
    CorporateBody("6VQ", "Cabinet", Some(1919), Some(1919)),
    CorporateBody("SV", "Cabinet", Some(1945), Some(1945)),
    CorporateBody("5V4", "Cabinet, Committee for Control of Official Histories", Some(1946), Some(1946)),
    CorporateBody("GW5", "Cattle Emergency Committee", Some(1934), Some(1934)),
    CorporateBody("934", "Dairy Crest", Some(1981), Some(1981)),
    CorporateBody("9HC", "Dean of the Chapel Royal", None, None),
    CorporateBody(
      "WGL",
      "Department for Environment, Food and Rural Affairs, Water Quality Division",
      Some(2002),
      Some(2002)
    ),
    CorporateBody("WJ4", "Department for Exiting the European Union", Some(2016), Some(2016)),
    CorporateBody("9YJ", "East Grinstead, Groombridge and Tunbridge Wells Railway Company", Some(1862), Some(1862)),
    CorporateBody("HF4", "East India Company", Some(1600), Some(1600)),
    CorporateBody("WN3", "Education and Skills Funding Agency", Some(2017), Some(2017)),
    CorporateBody("WNL", "Education and Skills Funding Agency", Some(2017), Some(2017)),
    CorporateBody("Q1R", "Falkland Islands Company", Some(1899), Some(1899)),
    CorporateBody("SQ9", "Fish's Corps of Foot", Some(1782), Some(1782)),
    CorporateBody(
      "R6R",
      "Foreign and Commonwealth Office, Consulate, Dusseldorf, West Germany",
      Some(1968),
      Some(1968)
    ),
    CorporateBody("HKL", "Foreign Office, Consulate, Angora and Konieh, Ottoman Empire", Some(1895), Some(1895)),
    CorporateBody("KSC", "Gaming Board for Great Britain", Some(1968), Some(1968)),
    CorporateBody("73R", "GCHQ", None, None),
    CorporateBody("VR1", "Geffrye Museum", Some(1914), Some(1914)),
    CorporateBody(
      "QX5",
      "General Nursing Council for England and Wales, Registration and Enrolment Committee",
      Some(1970),
      Some(1970)
    ),
    CorporateBody("C1Y", "Halifax High Level and North and South Junction Railway Company", Some(1884), Some(1884)),
    CorporateBody("W2T", "Hansard Society", Some(1944), Some(1944)),
    CorporateBody(
      "F18",
      "Health and Safety Commission, Health and Safety Executive, Employment Medical Advisory Service",
      Some(1975),
      Some(1975)
    ),
    CorporateBody("8JK", "Her Majesty's Stationery Office", Some(1986), Some(1986)),
    CorporateBody("9FV", "Ideal Benefit Society", Some(1912), Some(1912)),
    CorporateBody("5YX", "Imperial War Museum: Churchill Museum and Cabinet War Rooms", Some(1939), Some(1939)),
    CorporateBody("W1Q", "Independent Expert Group on Mobile Phones", Some(1999), Some(1999)),
    CorporateBody("QLY", "Independent Expert Group on Mobile Phones", Some(1999), Some(1999)),
    CorporateBody("LS5", "Jodrell Bank Observatory, Cheshire", Some(1955), Some(1955)),
    CorporateBody("92W", "Joint Milk Quality Committee", Some(1948), Some(1948)),
    CorporateBody(
      "L3W",
      "Justices in Eyre, of Assize, of Gaol Delivery, of Oyer and Terminer, of the Peace, and of Nisi Prius",
      None,
      None
    ),
    CorporateBody("N8X", "Justices of the Forest", Some(1166), Some(1166)),
    CorporateBody("THY", "Kew Gardens Archive", None, None),
    CorporateBody("SGX", "King's Own Dragoons, 1751-1818", None, None),
    CorporateBody("CCR", "Knitting, Lace and Net Industry Training Board", Some(1966), Some(1966)),
    CorporateBody("TTT", "King's Volunteers Regiment of Foot, 1761-1763", None, None),
    CorporateBody("VR7", "Lady Lever Art Gallery", Some(1922), Some(1922)),
    CorporateBody("XQ", "Law Society", Some(1825), Some(1825)),
    CorporateBody("91W", "League of Mercy", Some(1898), Some(1898)),
    CorporateBody("VX", "Legal Aid Board", Some(1989), Some(1989)),
    CorporateBody("TXG", "Legal Aid Board, 1988-1989", None, None),
    CorporateBody("6LL", "Machinery of Government Committee", Some(1917), Some(1917)),
    CorporateBody("G6N", "Magnetic Department", Some(1839), Some(1839)),
    CorporateBody("71K", "Manpower Distribution Board", Some(1916), Some(1916)),
    CorporateBody("KN1", "Master of the Rolls Archives Committee", Some(1925), Some(1925)),
    CorporateBody(
      "J6X",
      "National Agricultural Advisory Service, Great House Experimental Husbandry Farm",
      Some(1951),
      Some(1951)
    ),
    CorporateBody(
      "K7N",
      "National Air Traffic Control Services, Director General Projects and Engineering, Directorate of Projects",
      Some(1963),
      Some(1963)
    ),
    CorporateBody("TSL", "National Archives, The", None, None),
    CorporateBody("LSN", "Navy Board, Transport Branch, Prisoner of War Department", Some(1817), Some(1817)),
    CorporateBody("W1S", "Office for Budget Responsibility", Some(2010), Some(2010)),
    CorporateBody("N4W", "Office of Population Censuses and Surveys, Computer Division", Some(1972), Some(1972)),
    CorporateBody(
      "QQC",
      "Office of Works, Directorate of Works, Maintenance Surveyors Division, Sanitary Engineers Section",
      Some(1928),
      Some(1928)
    ),
    CorporateBody(
      "QFY",
      "Office of the President of Social Security Appeal Tribunals, Medical Appeal Tribunals and Vaccine Damage Tribunals",
      Some(1984),
      Some(1984)
    ),
    CorporateBody(
      "VYJ",
      "Ordnance Survey of Great Britain, Directorate of Data Collection and Management",
      Some(2003),
      Some(2003)
    ),
    CorporateBody("8FX", "Overseas Development Administration, Information Department", Some(1970), Some(1970)),
    CorporateBody(
      "3C",
      "Overseas Finance, International Finance, IF1 International Financial Institutions and Debt Division",
      Some(1990),
      Some(1990)
    ),
    CorporateBody("988", "Oxford University Archives", Some(1634), Some(1634)),
    CorporateBody("TWX", "Oxford University Press", Some(1633), Some(1633)),
    CorporateBody("79L", "Palace Court", Some(1660), Some(1660)),
    CorporateBody("TX6", "Parker Inquiry", None, None),
    CorporateBody(
      "VY4",
      "Paymaster General of the Court of Chancery, Supreme Court Pay Office",
      Some(1884),
      Some(1884)
    ),
    CorporateBody("VX3", "Persona Associates Ltd", Some(1989), Some(1989)),
    CorporateBody("V36", "Petty Bag Office", None, None),
    CorporateBody("8R6", "Queen Anne's Bounty", None, None),
    CorporateBody("SH2", "Queen's Own Dragoons, 1788-1818", None, None),
    CorporateBody("79X", "Queens Prison", Some(1842), Some(1842)),
    CorporateBody("W91", "Queen's Printer for Scotland", Some(1999), Some(1999)),
    CorporateBody("F11", "Radioactive Substances Advisory Committee", Some(1948), Some(1948)),
    CorporateBody("CYY", "Railway Executive", Some(1947), Some(1947)),
    CorporateBody("CXY", "Railway Executive", Some(1914), Some(1914)),
    CorporateBody("CY1", "Railway Executive", Some(1939), Some(1939)),
    CorporateBody("TXH", "SaBRE", Some(2002), Some(2002)),
    CorporateBody("739", "Scaccarium Superius", None, None),
    CorporateBody("NWN", "School of Anti-Aircraft Artillery", Some(1942), Some(1942)),
    CorporateBody("SGS", "Scots Greys, 1877-1921", None, None),
    CorporateBody("VXR", "Takeover Panel", None, None),
    CorporateBody("QQR", "Tate Gallery", Some(1897), Some(1897)),
    CorporateBody("63K", "Tate Gallery Archive", Some(1970), Some(1970)),
    CorporateBody("G91", "Thalidomide Y List Inquiry", Some(1978), Some(1978)),
    CorporateBody("FKS", "The Buying Agency", Some(1991), Some(1991)),
    CorporateBody(
      "JLC",
      "The Crown Estate, Other Urban Estates, Foreshore and Seabed Branches",
      Some(1973),
      Some(1973)
    ),
    CorporateBody("SYL", "Uhlans Britanniques de Sainte-Domingue (Charmilly's), 1794-1795", None, None),
    CorporateBody("TXK", "UK Passport Service", Some(1991), Some(1991)),
    CorporateBody("V3H", "UK Web Archiving Consortium", None, None),
    CorporateBody(
      "CCX",
      "United Kingdom Atomic Energy Authority, Atomic Weapons Research Establishment, Directors Office",
      Some(1954),
      Some(1954)
    ),
    CorporateBody("VTY", "Valuation Office Agency", Some(1991), Some(19910)),
    CorporateBody("9HJ", "Venetian Republic", Some(727), Some(727)),
    CorporateBody("QYF", "Victoria and Albert Museum", Some(1857), Some(1857)),
    CorporateBody("61H", "Victoria & Albert Museum, Archive of Art and Design", Some(1992), Some(1992)),
    CorporateBody("W9K", "Wales Tourist Board", Some(1969), Some(1969)),
    CorporateBody("VRG", "Walker Art Gallery", Some(1873), Some(1873)),
    CorporateBody("61J", "Wallace Collection", Some(1897), Some(1897)),
    CorporateBody(
      "HXV",
      "War and Colonial Department, Commissioners for liquidating the Danish and Dutch loans for St Thomas and St John",
      Some(1808),
      Some(1808)
    ),
    CorporateBody("V2R", "Zahid Mubarek Inquiry", Some(2004), Some(2004)),
    CorporateBody("763", "Zambia Department, Commonwealth Office", Some(1967), Some(1967)),
    CorporateBody("765", "Zambia, Malawi and Southern Africa Department", Some(1968), Some(1968)),
    CorporateBody("G2Y", "Zuckerman Working Party", None, None),
    CorporateBody("63F", "British Museum Central Archive", Some(2001), Some(2001)),
    CorporateBody("614", "British Library, Sound Archive", Some(1983), Some(1983)),
    CorporateBody("S2", "The National Archives", Some(2003), None)
  )

  def getPersons: Seq[Person] = Seq(
    Person("3RX", "Abbot, Charles", Some("2nd Baron Colchester"), Some(1798), Some(1867)),
    Person("48N", "Baden-Powell, Lady Olave St Clair", None, Some(1889), Some(1977)),
    Person("39K", "Cannon, John Francis Michael", None, Some(1930), None),
    Person(
      "3FH",
      "Dainton, Sir Frederick Sydney",
      Some("Baron Dainton of Hallam Moors in South Yorkshire"),
      Some(1914),
      Some(1997)
    ),
    Person("54J", "Edward, ", Some("Prince of Wales"), Some(1330), Some(1376)),
    Person("2QX", "Edward VII", Some("King of Great Britain and Ireland"), Some(1841), Some(1910)),
    Person("561", "Fanshawe, Baron, of Richmond, ", None, None, None),
    Person("46F", "Fawkes, Guy", None, Some(1570), Some(1606)),
    Person("2JN", "George, David Lloyd", Some("1st Earl Lloyd George of Dwyfor"), Some(1863), Some(1945)),
    Person("34X", "Halley, Edmund", None, Some(1656), Some(1742)),
    Person("2TK", "Halifax, ", Some(""), None, None),
    Person("39T", "Irvine, Linda Mary", Some("nee Newton"), Some(1928), None),
    Person("4", "Jack the Ripper, ", None, Some(1888), Some(1888)),
    Person("4FF", "Keay, Sir Lancelot Herman", Some("Knight"), Some(1883), Some(1974)),
    Person("ST", "Lawson, Nigel", Some("Baron Lawson of Blaby"), Some(1932), None),
    Person(
      "51X",
      "Macpherson, Sir William (Alan)",
      Some("Sir William Macpherson of Cluny (and Blairgowrie)"),
      Some(1926),
      None
    ),
    Person("515", "Newcastle, 1st Duke of, ", None, None, None),
    Person("4VF", "Old Pretender, The", None, None, None),
    Person("4H3", "Oliphant, Sir Mark Marcus Laurence Elwin", Some("Knight"), Some(1901), Some(2000)),
    Person("46W", "Paine, Thomas", None, Some(1737), Some(1809)),
    Person("3SH", "Reade, Hubert Granville Revell", None, Some(1859), Some(1938)),
    Person("2TF", "Reading, ", None, None, None),
    Person("53T", "Salisbury, Sir Edward James", Some("Professor"), Some(1886), Some(1978)),
    Person("3QL", "Tate, Sir Henry", Some("1st Baronet"), Some(1819), Some(1899)),
    Person("37K", "Uvarov, Sir Boris Petrovitch", Some("Knight"), Some(1889), Some(1970)),
    Person("2T1", "Vane-Tempest-Stewart, Charles Stewart", Some("6th Marquess of Londonderry"), Some(1852), Some(1915)),
    Person("4RW", "Victor Amadeus, ", Some("Duke of Savoy; King of Sardinia; King of Sicily"), Some(1666), Some(1732)),
    Person("3GY", "Victoria, ", Some("Queen of Great Britain and Ireland"), Some(1819), Some(1901))
  )

  def getCreators: Seq[Creator] =
    (getCorporateBodies.flatMap(Creator.from) ++ getPersons.flatMap(Creator.from)).sortBy(_.name)

  def getPlacesOfDeposit(): IO[Seq[PlaceOfDeposit]] =
    apiConnector.getPlacesOfDeposit(GetPlacesOfDeposit(timestamp = timeProvider.now()))

  def getLegalStatuses: IO[Seq[LegalStatus]] =
    apiConnector.getLegalStatusesSummary(GetLegalStatuses(timeProvider.now()))

  def isCreatorRecognised(creatorID: String): Boolean =
    creatorID.trim.nonEmpty && getCreators.exists(_.id == creatorID)
}
