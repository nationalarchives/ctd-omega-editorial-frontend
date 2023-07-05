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

import play.api.libs.functional.syntax.{ toFunctionalBuilderOps, unlift }
import play.api.libs.json.{ Format, __ }

case class AgentDescription(
  identifier: String,
  label: String,
  authorityFile: Option[Boolean],
  depository: Option[Boolean],
  versionTimestamp: String,
  dateFrom: Option[String],
  dateTo: Option[String],
  previousDescription: Option[String] = None
) {

  val displayedName: String = {
    val dateDisplay = (dateFrom, dateTo) match {
      case (Some(dateFrom), Some(dateTo)) => s" ($dateFrom - $dateTo)"
      case (Some(dateFrom), None)         => s" ($dateFrom - )"
      case (None, Some(dateTo))           => s" ( - $dateTo)"
      case _                              => ""
    }
    s"$label$dateDisplay"
  }
}

object AgentDescription {
  implicit val agentDescriptionFormat: Format[AgentDescription] = (
    (__ \ "identifier").format[String] and
      (__ \ "label").format[String] and
      (__ \ "authority-file").formatNullable[Boolean] and
      (__ \ "depository").formatNullable[Boolean] and
      (__ \ "version-timestamp").format[String] and
      (__ \ "date-from").formatNullable[String] and
      (__ \ "date-to").formatNullable[String] and
      (__ \ "previous-description").formatNullable[String]
  )(AgentDescription.apply, unlift(AgentDescription.unapply))

}
