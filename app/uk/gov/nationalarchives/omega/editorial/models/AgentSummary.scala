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

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{ Format, Json, Reads, __ }

case class AgentSummary(
  agentType: AgentType,
  identifier: String,
  label: String,
  dateFrom: String,
  dateTo: String
) {

  val displayedName: String = {
    val dateDisplay = (agentType, dateFrom, dateTo) match {
      case (AgentType.CorporateBody, dateFrom, dateTo) => s" ($dateFrom - $dateTo)"
      case (AgentType.CorporateBody, dateFrom, "")     => s" ($dateFrom - )"
      case (AgentType.CorporateBody, "", dateTo)       => s" ( - $dateTo)"
      case (AgentType.Person, dateFrom, dateTo)        => s" (b.$dateFrom - d.$dateTo)"
      case (AgentType.Person, dateFrom, "")            => s" (b.$dateFrom - )"
      case (AgentType.Person, "", dateTo)              => s" ( - d.$dateTo)"
      case _                                           => ""
    }
    s"$label$dateDisplay"
  }
}
object AgentSummary {
  implicit val format: Format[AgentSummary] = Json.format[AgentSummary]

  implicit val agentSummaryReads: Reads[AgentSummary] = (
    (__ \ "type").read[AgentType] and
      (__ \ "identifier").read[String] and
      (__ \ "label").read[String] and
      (__ \ "date-from").read[String] and
      (__ \ "date-to").read[String]
  ) { (agentType, identifier, label, dateFrom, dateTo) =>
    AgentSummary(agentType, identifier, label, dateFrom, dateTo)
  }

}
