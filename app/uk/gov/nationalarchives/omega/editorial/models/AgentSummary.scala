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

case class AgentSummary(
  agentType: AgentType,
  identifier: String,
  label: String,
  dateFrom: Option[String],
  dateTo: Option[String],
  depository: Option[Boolean] = None
) {

  val displayedName: String = {
    val dateDisplay = (agentType, dateFrom, dateTo) match {
      case (AgentType.CorporateBody, Some(dateFrom), Some(dateTo)) => s" ($dateFrom - $dateTo)"
      case (AgentType.CorporateBody, Some(dateFrom), None)         => s" ($dateFrom - )"
      case (AgentType.CorporateBody, None, Some(dateTo))           => s" ( - $dateTo)"
      case (AgentType.Person, Some(dateFrom), Some(dateTo))        => s" (b.$dateFrom - d.$dateTo)"
      case (AgentType.Person, Some(dateFrom), None)                => s" (b.$dateFrom - )"
      case (AgentType.Person, None, Some(dateTo))                  => s" ( - d.$dateTo)"
      case _                                                       => ""
    }
    s"$label$dateDisplay"
  }
}
object AgentSummary {

  implicit val agentSummaryFormat: Format[AgentSummary] = (
    (__ \ "type").format[AgentType] and
      (__ \ "identifier").format[String] and
      (__ \ "label").format[String] and
      (__ \ "date-from").formatNullable[String] and
      (__ \ "date-to").formatNullable[String] and
      (__ \ "depository").formatNullable[Boolean]
  )(AgentSummary.apply, unlift(AgentSummary.unapply))

}
