/*
 * Copyright (c) 2023 The National Archives
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

import play.api.libs.json.Json
import uk.gov.nationalarchives.omega.editorial.connectors.{ ApiConnector, MessageType }
import uk.gov.nationalarchives.omega.editorial.models.GetEditSet

import java.time.{ LocalDateTime, Month }

class GetEditSetISpec extends BaseRequestReplyServiceISpec {

  override val messageType: String = MessageType.GetEditSetType.value

  "GetEditSet Client" - {

    "send a message and handle the reply" in { requestReplyHandler =>
      val editSetId = "1"
      val request =
        Json.stringify(Json.toJson(GetEditSet(oci = editSetId, LocalDateTime.of(2023, Month.FEBRUARY, 24, 8, 10))))
      val expected = Json.stringify(Json.toJson(stubData.getExpectedEditSet(editSetId)))

      val result = sendRequest(requestReplyHandler, request, ApiConnector.applicationId, messageType)
      result.asserting(_.messageText mustBe expected)
    }

    "send two messages and handle the replies" in { requestReplyHandler =>
      val editSetId = "1"
      val request =
        Json.stringify(Json.toJson(GetEditSet(oci = editSetId, LocalDateTime.of(2023, Month.FEBRUARY, 24, 8, 10))))
      val expected = Json.stringify(Json.toJson(stubData.getExpectedEditSet(editSetId)))

      val result1 = sendRequest(requestReplyHandler, request, ApiConnector.applicationId, messageType)
      val result2 = sendRequest(requestReplyHandler, request, ApiConnector.applicationId, messageType)

      result1.asserting(_.messageText mustBe expected) *>
        result2.asserting(_.messageText mustBe expected)
    }

    "send three messages and handle the replies" in { requestReplyHandler =>
      val editSetId = "1"
      val request =
        Json.stringify(Json.toJson(GetEditSet(oci = editSetId, LocalDateTime.of(2023, Month.FEBRUARY, 24, 8, 10))))
      val expected = Json.stringify(Json.toJson(stubData.getExpectedEditSet(editSetId)))

      val result1 = sendRequest(requestReplyHandler, request, ApiConnector.applicationId, messageType)
      val result2 = sendRequest(requestReplyHandler, request, ApiConnector.applicationId, messageType)
      val result3 = sendRequest(requestReplyHandler, request, ApiConnector.applicationId, messageType)

      result1.asserting(_.messageText mustBe expected) *>
        result2.asserting(_.messageText mustBe expected) *>
        result3.asserting(_.messageText mustBe expected)
    }

  }

}
