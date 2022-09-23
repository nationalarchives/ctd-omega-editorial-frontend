package controllers

import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.mvc.{ AnyContentAsEmpty, DefaultActionBuilder, DefaultMessagesActionBuilderImpl, DefaultMessagesControllerComponents }
import play.api.test._
import play.api.test.Helpers._
import uk.gov.nationalarchives.omega.editorial.controllers.{ LoginController }

/** Add your spec here.
  * You can mock out a whole application including requests, plugins etc.
  *
  * For more information, see https://www.playframework.com/documentation/latest/ScalaTestingWithScalaTest
  */
class LoginControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {

  "LoginController GET" should {

    "render the login page from a new instance of controller" in {
      val messages: Map[String, Map[String, String]] =
        Map("en" -> Map("login.heading" -> "Sign in: Pan-Archival Catalogue"))
      val mockMessagesApi = stubMessagesApi(messages)
      val stub = stubControllerComponents()
      val controller = new LoginController(
        DefaultMessagesControllerComponents(
          new DefaultMessagesActionBuilderImpl(stubBodyParser(AnyContentAsEmpty), mockMessagesApi)(
            stub.executionContext
          ),
          DefaultActionBuilder(stub.actionBuilder.parser)(stub.executionContext),
          stub.parsers,
          mockMessagesApi,
          stub.langs,
          stub.fileMimeTypes,
          stub.executionContext
        )
      )
      val login = controller.login().apply(FakeRequest(GET, "/login"))

      status(login) mustBe OK
      contentType(login) mustBe Some("text/html")
      contentAsString(login) must include("Sign in: Pan-Archival Catalogue")
    }

    "render the login page from the application" in {
      val controller = inject[LoginController]
      val login = controller.login().apply(FakeRequest(GET, "/login"))

      status(login) mustBe OK
      contentType(login) mustBe Some("text/html")
      contentAsString(login) must include("Sign in: Pan-Archival Catalogue")
    }

    "render the login page from the router" in {
      val request = FakeRequest(GET, "/login")
      val login = route(app, request).get

      status(login) mustBe OK
      contentType(login) mustBe Some("text/html")
      contentAsString(login) must include("Sign in: Pan-Archival Catalogue")
    }
  }
}
