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

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Result
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout}
import play.api.test.Injecting
import play.twirl.api.Content
import uk.gov.nationalarchives.omega.editorial.config.{AwsCredentialsAuthentication, Config, SqsJmsBrokerConfig, SqsJmsBrokerEndpointConfig}
import uk.gov.nationalarchives.omega.editorial.models._
import uk.gov.nationalarchives.omega.editorial.models.session.Session
import uk.gov.nationalarchives.omega.editorial.modules.StartupModule
import uk.gov.nationalarchives.omega.editorial.services.{EditSetRecordService, EditSetService, MessagingService}
import uk.gov.nationalarchives.omega.editorial.support.TimeProvider

import java.time.{LocalDateTime, Month}
import scala.concurrent.Future

class BaseSpec
    extends PlaySpec with GuiceOneAppPerSuite with Injecting with BeforeAndAfterEach with ModelSupport
    with MessagingServiceAssertions {

  val user: User = User("dummy user")
  val editSetRecordService: EditSetRecordService = app.injector.instanceOf[EditSetRecordService]
  val editSetService: EditSetService = app.injector.instanceOf[EditSetService]
  val validSessionToken: String = Session.generateToken("1234")
  val invalidSessionToken: String = Session.generateToken("invalid-user")
  implicit val messageServiceMonitoring: MessagingServiceMonitoring = TestMessagingService
  lazy implicit val testTimeProvider: TimeProvider = () => LocalDateTime.of(2023, Month.FEBRUARY, 28, 1, 1, 1)
  implicit val executionContext: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  private lazy val testConfig: Config = Config(
    sqsJmsBroker = SqsJmsBrokerConfig("not-a-real-region", Some(SqsJmsBrokerEndpointConfig(false, Some("not.a.real.host"), Some(0), Some(AwsCredentialsAuthentication("?", "?"))))),
    defaultRequestQueueName = "STUB001_REQUEST001"
  )

  override def beforeEach(): Unit = {
    super.beforeEach()
    TestMessagingService.reset()
  }

  override def fakeApplication(): Application =
    GuiceApplicationBuilder()
      .disable[StartupModule]
      .bindings(
        bind[MessagingService].toInstance(TestMessagingService),
        bind[TimeProvider].toInstance(testTimeProvider),
        bind[Config].toInstance(testConfig)
      )
      .build()

  def asDocument(content: Content): Document = asDocument(contentAsString(content))

  def asDocument(rawContent: String): Document = Jsoup.parse(rawContent)

  def asDocument(resultFuture: Future[Result]): Document = asDocument(contentAsString(resultFuture))

}
