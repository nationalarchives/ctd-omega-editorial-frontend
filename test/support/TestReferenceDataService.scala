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

package support

import uk.gov.nationalarchives.omega.editorial.models.{ CorporateBody, LegalStatus, Person, PlaceOfDeposit }
import uk.gov.nationalarchives.omega.editorial.services.ReferenceDataService

class TestReferenceDataService extends ReferenceDataService {

  override def getCorporateBodies: Seq[CorporateBody] =
    Seq(
      CorporateBody("92W", "Joint Milk Quality Committee", Some(1948), Some(1948)),
      CorporateBody("8R6", "Queen Anne's Bounty", None, None)
    )

  override def getPersons: Seq[Person] = Seq(
    Person("46F", "Fawkes, Guy", None, Some(1570), Some(1606)),
    Person("48N", "Baden-Powell, Lady Olave St Clair", None, Some(1889), Some(1977))
  )

  override def getPlacesOfDeposit: Seq[PlaceOfDeposit] =
    Seq(
      PlaceOfDeposit("1", "The National Archives, Kew"),
      PlaceOfDeposit("2", "British Museum, Department of Libraries and Archives"),
      PlaceOfDeposit("3", "British Library, National Sound Archive")
    )

  override def getLegalStatuses: Seq[LegalStatus] = Seq(
    LegalStatus("ref.1", "Public Record(s)"),
    LegalStatus("ref.2", "Not Public Records"),
    LegalStatus("ref.3", "Public Records unless otherwise Stated"),
    LegalStatus("ref.4", "Welsh Public Record(s)")
  )
}
