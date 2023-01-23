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

import uk.gov.nationalarchives.omega.editorial.models.Creator.{ CreatorType, CreatorTypeCorporateBody, CreatorTypePerson }

case class Creator(creatorType: CreatorType, id: String, name: String, startYear: Option[Int], endYear: Option[Int]) {

  val displayedName = {
    val dateDisplay = (creatorType, startYear, endYear) match {
      case (CreatorTypeCorporateBody, Some(startYear), Some(endYear)) => s" ($startYear - $endYear)"
      case (CreatorTypeCorporateBody, Some(startYear), None)          => s" ($startYear - )"
      case (CreatorTypeCorporateBody, None, Some(endYear))            => s" ( - $endYear)"
      case (CreatorTypePerson, Some(startYear), Some(endYear))        => s" (b.$startYear - d.$endYear)"
      case (CreatorTypePerson, Some(startYear), None)                 => s" (b.$startYear - )"
      case (CreatorTypePerson, None, Some(endYear))                   => s" ( - d.$endYear)"
      case (_, None, None)                                            => ""
    }
    s"$name$dateDisplay"
  }

}

object Creator {

  sealed trait CreatorType

  object CreatorTypeCorporateBody extends CreatorType
  object CreatorTypePerson extends CreatorType

  def from(person: Person): Option[Creator] =
    Option(Creator(CreatorTypePerson, person.id, person.name, person.yearOfBirth, person.yearOfDeath))
      .filter(hasMandatoryFields)
      .map { creator =>
        Creator(CreatorTypePerson, creator.id, getNameWithTitleIfPresent(person), creator.startYear, creator.endYear)
      }

  def from(corporateBody: CorporateBody): Option[Creator] =
    Option(
      Creator(
        CreatorTypeCorporateBody,
        corporateBody.id,
        corporateBody.name,
        corporateBody.startingYear,
        corporateBody.endingYear
      )
    )
      .filter(hasMandatoryFields)

  private def getNameWithTitleIfPresent(person: Person): String =
    person.title.map(title => s"${person.name}, $title").getOrElse(person.name)

  private def hasMandatoryFields(creator: Creator): Boolean =
    creator.id.trim.nonEmpty && creator.name.trim.nonEmpty

}
