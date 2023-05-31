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

import uk.gov.nationalarchives.omega.editorial.services.jms.StubData
import uk.gov.nationalarchives.omega.editorial.models.{ AgentSummary, _ }

import javax.inject.Inject

class TestStubData @Inject() extends StubData {

  override def getAgentSummaries(): Seq[AgentSummary] = List(
    AgentSummary(AgentType.Person, "48N", "Baden-Powell, Lady Olave St Clair", "1889", "1977"),
    AgentSummary(AgentType.Person, "46F", "Fawkes, Guy", "1570", "1606"),
    AgentSummary(AgentType.CorporateBody, "92W", "Joint Milk Quality Committee", "1948", "1948"),
    AgentSummary(AgentType.CorporateBody, "8R6", "Queen Anne's Bounty", "", "")
  )

}
