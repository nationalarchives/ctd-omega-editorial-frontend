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

package services

import cats.effect.testing.scalatest.AsyncIOSpec
import org.mockito.cats.MockitoCats.whenF
import org.mockito.{ ArgumentMatchersSugar, MockitoSugar }
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.must.Matchers
import uk.gov.nationalarchives.omega.editorial.connectors.ApiConnector
import uk.gov.nationalarchives.omega.editorial.connectors.MessageType.{ GetAgentSummariesType, GetLegalStatusesType }
import uk.gov.nationalarchives.omega.editorial.connectors.messages.ReplyMessage
import uk.gov.nationalarchives.omega.editorial.models._
import uk.gov.nationalarchives.omega.editorial.services.MessagingService

import java.time.LocalDateTime

class MessagingServiceSpec
    extends AsyncFreeSpec with AsyncIOSpec with Matchers with MockitoSugar with ArgumentMatchersSugar {

  "When the messaging service receives a request" - {
    " for legal statuses" in {
      val mockApiConnector = mock[ApiConnector]
      val expectedLegalStatus = LegalStatus("ABC", "Public record")
      val messagingService = new MessagingService(mockApiConnector)
      whenF(mockApiConnector.handle(eqTo(GetLegalStatusesType), any[String]))
        .thenReturn(ReplyMessage(getExpectedLegalStatusJson(expectedLegalStatus), Some(""), Some("")))
      val result = messagingService.getLegalStatuses(GetLegalStatuses(LocalDateTime.now()))
      result.asserting(_ mustEqual Seq(expectedLegalStatus))
    }

    " for agent summaries" in {
      val mockApiConnector = mock[ApiConnector]
      val expectedAgentSummaries = List(
        AgentSummary(
          AgentType.CorporateBody,
          "W2T",
          "current description",
          List(
            AgentDescription(
              "W2T",
              "Hansard Society",
              Some(false),
              Some(false),
              "2022-06-22T02:00:00-0500",
              Some("1944"),
              Some("1944")
            )
          )
        )
      )
      val messagingService = new MessagingService(mockApiConnector)
      whenF(mockApiConnector.handle(eqTo(GetAgentSummariesType), any[String]))
        .thenReturn(ReplyMessage(getExpectedAgentSummariesJson(expectedAgentSummaries), Some(""), Some("")))
      val result = messagingService.getAgentSummaries(
        GetAgentSummaryList(List(AgentType.CorporateBody), None, Some(false), Some(false))
      )
      result.asserting(_ mustEqual expectedAgentSummaries)
    }

    " for place of deposit" in {
      val mockApiConnector = mock[ApiConnector]
      val expectedAgentSummaries =
        List(
          AgentSummary(
            AgentType.CorporateBody,
            "614",
            "current description",
            List(
              AgentDescription(
                "614",
                "The National Archives",
                Some(false),
                Some(false),
                "2022-06-22T02:00:00-0500",
                Some("2003"),
                None
              )
            )
          )
        )
      val messagingService = new MessagingService(mockApiConnector)
      whenF(mockApiConnector.handle(eqTo(GetAgentSummariesType), any[String]))
        .thenReturn(ReplyMessage(getExpectedPlacesOfDepositJson(expectedAgentSummaries), Some(""), Some("")))
      val result = messagingService.getPlacesOfDeposit(
        GetAgentSummaryList(List(AgentType.CorporateBody), None, Some(true), Some(false))
      )
      result.asserting(_ mustEqual expectedAgentSummaries)
    }
  }

  private def getExpectedLegalStatusJson(legalStatus: LegalStatus): String =
    s"""
       |[
       |  {
       |    "identifier": "${legalStatus.identifier}",
       |    "label": "${legalStatus.label}"
       |  }
       |]
       |""".stripMargin

  private def getExpectedAgentSummariesJson(agentSummaryList: Seq[AgentSummary]): String =
    s"""
       |[
       |  {
       |    "type" : "${agentSummaryList.head.agentType.entryName}",
       |    "identifier" : "${agentSummaryList.head.identifier}",
       |    "current-description" : "${agentSummaryList.head.currentDescription}",
       |    "description" : [
       |      {
       |      "identifier": "${agentSummaryList.head.identifier}",
       |      "label": "${agentSummaryList.head.description.head.label}",
       |      "authority-file" : ${agentSummaryList.head.description.head.authorityFile.getOrElse("")},
       |      "depository" : ${agentSummaryList.head.description.head.depository.getOrElse("")},
       |      "version-timestamp" : "${agentSummaryList.head.description.head.versionTimestamp}",
       |      "date-from": "${agentSummaryList.head.description.head.dateFrom.getOrElse("")}",
       |      "date-to": "${agentSummaryList.head.description.head.dateTo.getOrElse("")}"
       |      }
       |    ]
       |   }
       |]
       |""".stripMargin

  private def getExpectedPlacesOfDepositJson(agentSummaryList: Seq[AgentSummary]): String =
    s"""
       |[
       |  {
       |    "type" : "${agentSummaryList.head.agentType.entryName}",
       |    "identifier" : "${agentSummaryList.head.identifier}",
       |    "current-description" : "${agentSummaryList.head.currentDescription}",
       |    "description" : [
       |      {
       |      "identifier": "${agentSummaryList.head.identifier}",
       |      "label": "${agentSummaryList.head.description.head.label}",
       |      "authority-file" : ${agentSummaryList.head.description.head.authorityFile.getOrElse("")},
       |      "depository" : ${agentSummaryList.head.description.head.depository.getOrElse("")},
       |      "version-timestamp" : "${agentSummaryList.head.description.head.versionTimestamp}",
       |      "date-from": "${agentSummaryList.head.description.head.dateFrom.getOrElse("")}"
       |      }
       |    ]
       |   }
       |]
       |""".stripMargin
}
