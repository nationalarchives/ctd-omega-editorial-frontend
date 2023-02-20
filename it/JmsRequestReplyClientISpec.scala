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

import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.{ IO, Resource }
import org.scalatest.freespec.FixtureAsyncFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{ BeforeAndAfterAll, FutureOutcome }
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jFactory
import uk.gov.nationalarchives.omega.editorial.connectors._
import uk.gov.nationalarchives.omega.editorial.services.EchoServer

import scala.concurrent.duration.{ FiniteDuration, SECONDS }
import scala.util.{ Failure, Success }

class JmsRequestReplyClientISpec extends FixtureAsyncFreeSpec with AsyncIOSpec with Matchers with BeforeAndAfterAll {

  override type FixtureParam = RequestReplyHandler

  implicit val logger: SelfAwareStructuredLogger[IO] = Slf4jFactory[IO].getLogger

  private val serviceId = "1234"
  private val requestQueueName = "request-general"
  private val replyQueueName = "omega-editorial-web-application-instance-1"
  private val messagingServerHost = "localhost"
  private val messagingServerPort = 9324

  private val echoServer = new EchoServer

  override def beforeAll(): Unit =
    echoServer.startEchoServer.unsafeToFuture().onComplete {
      case Success(_)         =>
      case Failure(exception) => fail(s"Failed to start Echo Server", exception)
    }

  override def withFixture(test: OneArgAsyncTest): FutureOutcome = {
    val clientResource: Resource[IO, JmsRequestReplyClient[IO]] = JmsRequestReplyClient.createForSqs[IO](
      endpoint = HostBrokerEndpoint(messagingServerHost, messagingServerPort),
      credentials = UsernamePasswordCredentials("?", "?"),
      customClientId = None
    )(replyQueueName)
    val (client, closer) = clientResource.allocated.unsafeRunSync()
    complete {
      super.withFixture(test.toNoArgAsyncTest(RequestReplyHandler(client)))
    } lastly {
      val _ = closer.unsafeRunTimed(FiniteDuration(1, SECONDS))
      ()
    }
  }

  "SQS Client" - {
    "send a message and handle the reply" in { requestReplyHandler =>
      val result = sendRequest(requestReplyHandler, "hello 1234")

      result.asserting(_ mustBe "Echo Server: hello 1234")
    }
    "send two messages and handle the replies" in { requestReplyHandler =>
      val result1 = sendRequest(requestReplyHandler, "hello 1234")
      val result2 = sendRequest(requestReplyHandler, "hello 5678")

      result1.asserting(_ mustBe "Echo Server: hello 1234") *>
        result2.asserting(_ mustBe "Echo Server: hello 5678")
    }
    "send three messages and handle the replies" in { requestReplyHandler =>
      val result1 = sendRequest(requestReplyHandler, "hello 1234")
      val result2 = sendRequest(requestReplyHandler, "hello 5678")
      val result3 = sendRequest(requestReplyHandler, "hello 9000")

      result1.asserting(_ mustBe "Echo Server: hello 1234") *>
        result2.asserting(_ mustBe "Echo Server: hello 5678") *>
        result3.asserting(_ mustBe "Echo Server: hello 9000")
    }
  }

  private def sendRequest(requestReplyHandler: RequestReplyHandler, message: String): IO[String] =
    requestReplyHandler.handle(requestQueueName, RequestMessage(message, serviceId))

}
