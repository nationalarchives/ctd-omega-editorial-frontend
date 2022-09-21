package controllers

import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.mvc.{AnyContentAsEmpty, DefaultActionBuilder, DefaultMessagesActionBuilderImpl, DefaultMessagesControllerComponents, MessagesControllerComponents}
import play.api.test._
import play.api.test.Helpers._
import play.i18n.MessagesApi
import uk.gov.nationalarchives.omega.editorial.controllers.HomeController

/** Add your spec here.
  * You can mock out a whole application including requests, plugins etc.
  *
  * For more information, see https://www.playframework.com/documentation/latest/ScalaTestingWithScalaTest
  */
class HomeControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {

  "HomeController GET" should {

    "render the index page from a new instance of controller" in {
      val messages: Map[String, Map[String, String]] = Map( "en" -> Map("index.heading" -> "Welcome to the Catalogue"))
      val mockMessagesApi = stubMessagesApi(messages)
      val stub = stubControllerComponents()
      val controller = new HomeController(DefaultMessagesControllerComponents(
        new DefaultMessagesActionBuilderImpl(stubBodyParser(AnyContentAsEmpty), mockMessagesApi)(stub.executionContext),
        DefaultActionBuilder(stub.actionBuilder.parser)(stub.executionContext),
        stub.parsers,
        mockMessagesApi,
        stub.langs,
        stub.fileMimeTypes,
        stub.executionContext
      ))
      val home = controller.index().apply(FakeRequest(GET, "/"))

      status(home) mustBe OK
      contentType(home) mustBe Some("text/html")
      contentAsString(home) must include("Welcome to the Catalogue")
    }

    "render the index page from the application" in {
      val controller = inject[HomeController]
      val home = controller.index().apply(FakeRequest(GET, "/"))

      status(home) mustBe OK
      contentType(home) mustBe Some("text/html")
      contentAsString(home) must include("Welcome to the Catalogue")
    }

    "render the index page from the router" in {
      val request = FakeRequest(GET, "/")
      val home = route(app, request).get

      status(home) mustBe OK
      contentType(home) mustBe Some("text/html")
      contentAsString(home) must include("Welcome to the Catalogue")
    }
  }
}
