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

  def getCreators(): IO[Seq[Creator]] =
    for {
      persons         <- apiConnector.getPersons(GetPersons(timestamp = timeProvider.now()))
      corporateBodies <- apiConnector.getCorporateBodies(GetCorporateBodies(timestamp = timeProvider.now()))
    } yield persons.flatMap(Creator.from) ++ corporateBodies.flatMap(Creator.from)

  def getPlacesOfDeposit(): IO[Seq[PlaceOfDeposit]] =
    apiConnector.getPlacesOfDeposit(GetPlacesOfDeposit(timestamp = timeProvider.now()))

  def getLegalStatuses: Seq[LegalStatus] = Seq(
    LegalStatus("ref.1", "Public Record(s)"),
    LegalStatus("ref.2", "Not Public Records"),
    LegalStatus("ref.3", "Public Records unless otherwise Stated"),
    LegalStatus("ref.4", "Welsh Public Record(s)")
  )

  def isCreatorRecognised(creators: Seq[Creator], creatorID: String): Boolean =
    creatorID.trim.nonEmpty && creators.exists(_.id == creatorID)
}
