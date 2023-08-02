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

import cats.data.NonEmptyList
import uk.gov.nationalarchives.omega.editorial.services.jms.StubData
import uk.gov.nationalarchives.omega.editorial.models.{ AgentSummary, _ }

import javax.inject.Inject

class TestStubData @Inject() extends StubData {

  override def getAgentSummaries: Seq[AgentSummary] = List(
    AgentSummary(
      AgentType.Person,
      s"$baseUriAgent.3LG",
      "current description",
      NonEmptyList.of(
        AgentDescription(
          s"$baseUriAgent.3LG",
          "Edwin Hill",
          Some(false),
          Some(false),
          "2022-06-22T02:00:00-0500",
          Some("1793"),
          Some("1876"),
          None
        )
      )
    ),
    AgentSummary(
      AgentType.Person,
      s"$baseUriAgent.2YK",
      "current description",
      NonEmptyList.of(
        AgentDescription(
          s"$baseUriAgent.2YK",
          "Edward Gibson",
          Some(false),
          Some(false),
          "2022-06-22T02:00:00-0500",
          Some("1837"),
          Some("1913")
        )
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.N6L",
      "current description",
      NonEmptyList.of(
        AgentDescription(
          s"$baseUriAgent.N6L",
          "National Coal Board, Northumberland and Durham Division",
          Some(false),
          Some(false),
          "2022-06-22T02:00:00-0500",
          Some("1964"),
          Some("1967")
        )
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      s"$baseUriAgent.W91",
      "current description",
      NonEmptyList.of(
        AgentDescription(
          s"$baseUriAgent.W91",
          "Queen's Printer for Scotland",
          Some(false),
          Some(false),
          "2022-06-22T02:00:00-0500",
          Some("1999"),
          None
        )
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      tna,
      "current description",
      NonEmptyList.of(
        AgentDescription(
          "S2",
          "The National Archives",
          Some(false),
          Some(true),
          "2022-06-22T02:00:00-0500",
          Some("2003"),
          None
        )
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      britishMuseum,
      "current description",
      NonEmptyList.of(
        AgentDescription(
          s"$baseUriAgent.63F",
          "British Museum, Central Archive",
          Some(false),
          Some(true),
          "2022-06-22T02:00:00-0500",
          Some("2001"),
          Some("2001")
        )
      )
    ),
    AgentSummary(
      AgentType.CorporateBody,
      britishLibrary,
      "current description",
      NonEmptyList.of(
        AgentDescription(
          s"$baseUriAgent.614",
          "British Library, Sound Archive",
          Some(false),
          Some(true),
          "2022-06-22T02:00:00-0500",
          Some("1983"),
          Some("1983")
        )
      )
    )
  )
}
