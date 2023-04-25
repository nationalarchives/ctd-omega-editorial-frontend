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
import uk.gov.nationalarchives.omega.editorial.models._
import uk.gov.nationalarchives.omega.editorial.support.TimeProvider

import javax.inject.{Inject, Singleton}

@Singleton
class ReferenceDataService @Inject() (messagingService: MessagingService, timeProvider: TimeProvider) {

  def getCreators: IO[Seq[Creator]] =
    for {
      persons         <- messagingService.getPersons(GetPersons(timestamp = timeProvider.now()))
      corporateBodies <- messagingService.getCorporateBodies(GetCorporateBodies(timestamp = timeProvider.now()))
    } yield persons.flatMap(Creator.from) ++ corporateBodies.flatMap(Creator.from)

  def getPlacesOfDeposit: IO[Seq[PlaceOfDeposit]] =
    messagingService.getPlacesOfDeposit(GetPlacesOfDeposit(timestamp = timeProvider.now()))

  def getLegalStatuses: IO[Seq[LegalStatus]] =
    messagingService.getLegalStatuses(GetLegalStatuses(timeProvider.now()))

}
