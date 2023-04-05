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
import play.api.test.Helpers.{ contentAsString, defaultAwaitTimeout }
import play.api.test.Injecting
import play.twirl.api.Content
import uk.gov.nationalarchives.omega.editorial.config.{ Config, HostBrokerEndpoint, UsernamePasswordCredentials }
import uk.gov.nationalarchives.omega.editorial.connectors.ApiConnector
import uk.gov.nationalarchives.omega.editorial.models._
import uk.gov.nationalarchives.omega.editorial.models.Creator.CreatorType
import uk.gov.nationalarchives.omega.editorial.models.session.Session
import uk.gov.nationalarchives.omega.editorial.modules.StartupModule
import uk.gov.nationalarchives.omega.editorial.services.{ EditSetRecordService, EditSetService }
import uk.gov.nationalarchives.omega.editorial.support.TimeProvider

import java.time.{ LocalDateTime, Month }
import scala.concurrent.Future

class BaseSpec
    extends PlaySpec with GuiceOneAppPerSuite with Injecting with BeforeAndAfterEach with ModelSupport
    with ApiConnectorAssertions {

  val user: User = User("dummy user")
  val editSetRecordService: EditSetRecordService = app.injector.instanceOf[EditSetRecordService]
  val editSetService: EditSetService = app.injector.instanceOf[EditSetService]
  val legalStatuses: Seq[LegalStatus] = Seq(
    LegalStatus("ref.1", "Public Record(s)"),
    LegalStatus("ref.2", "Not Public Records"),
    LegalStatus("ref.3", "Public Records unless otherwise Stated"),
    LegalStatus("ref.4", "Welsh Public Record(s)")
  )
  val allPlacesOfDeposit: Seq[PlaceOfDeposit] = Seq(
    PlaceOfDeposit("1", "The National Archives, Kew"),
    PlaceOfDeposit("2", "British Museum, Department of Libraries and Archives"),
    PlaceOfDeposit("3", "British Library, National Sound Archive")
  )
  val allCreators: Seq[Creator] = Seq(
    Creator(CreatorType.CorporateBody, "RR6", "100th (Gordon Highlanders) Regiment of Foot", Some(1794), Some(1794)),
    Creator(CreatorType.CorporateBody, "S34", "1st Regiment of Foot or Royal Scots", Some(1812), Some(1812)),
    Creator(CreatorType.CorporateBody, "87K", "Abbotsbury Railway Company", Some(1877), Some(1877)),
    Creator(CreatorType.Person, "3RX", "Abbot, Charles, 2nd Baron Colchester", Some(1798), Some(1867)),
    Creator(CreatorType.Person, "48N", "Baden-Powell, Lady Olave St Clair", Some(1889), Some(1977)),
    Creator(CreatorType.Person, "39K", "Cannon, John Francis Michael", Some(1930), None)
  )
  val validSessionToken: String = Session.generateToken("1234")
  val invalidSessionToken: String = Session.generateToken("invalid-user")
  implicit val apiConnectorMonitoring: ApiConnectorMonitoring = TestApiConnector
  lazy implicit val testTimeProvider: TimeProvider = () => LocalDateTime.of(2023, Month.FEBRUARY, 28, 1, 1, 1)
  implicit val executionContext: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  private lazy val testConfig: Config = Config(
    broker = HostBrokerEndpoint("not.a.real.host", 0),
    credentials = UsernamePasswordCredentials("?", "?")
  )

  override def beforeEach(): Unit = {
    super.beforeEach()
    TestApiConnector.reset()
  }

  override def fakeApplication(): Application =
    GuiceApplicationBuilder()
      .disable[StartupModule]
      .bindings(
        bind[ApiConnector].toInstance(TestApiConnector),
        bind[TimeProvider].toInstance(testTimeProvider),
        bind[Config].toInstance(testConfig)
      )
      .build()

  def asDocument(content: Content): Document = asDocument(contentAsString(content))

  def asDocument(rawContent: String): Document = Jsoup.parse(rawContent)

  def asDocument(resultFuture: Future[Result]): Document = asDocument(contentAsString(resultFuture))

}
