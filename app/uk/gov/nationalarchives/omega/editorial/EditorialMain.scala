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

package uk.gov.nationalarchives.omega.editorial

import cats.effect.{IO, Resource}
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jFactory
import play.api.inject.ApplicationLifecycle
import uk.gov.nationalarchives.omega.jms.{HostBrokerEndpoint, JmsRRClient, UsernamePasswordCredentials}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

@Singleton
class EditorialMain @Inject()(lifeCycle: ApplicationLifecycle)(implicit ec: ExecutionContext) {

  private val logging = Slf4jFactory[IO]
  implicit val logger: SelfAwareStructuredLogger[IO] = logging.getLogger

  private val replyQueue = "omega-editorial-web-application-instance-1"

  private val clientRes: Resource[IO, JmsRRClient[IO]] = JmsRRClient.createForSqs[IO](
    HostBrokerEndpoint("localhost", 9324),
    UsernamePasswordCredentials("x", "x"),
    None
  )(replyQueue)

  // setup for cats-effect
  import cats.effect.unsafe.implicits._
  val (jmsRrClient, closer) = clientRes.allocated.unsafeRunSync()

  lifeCycle.addStopHook { () =>
    Future(closer.unsafeRunSync())

  }
}
