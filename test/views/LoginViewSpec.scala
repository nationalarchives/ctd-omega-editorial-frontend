package views

import org.scalatestplus.play.PlaySpec
import play.api.data.{ Form, FormError }
import play.api.data.Forms.{ mapping, text }
import play.api.test.{ CSRFTokenHelper, FakeRequest, Helpers }
import play.api.test.Helpers.{ contentAsString, defaultAwaitTimeout }
import play.test.WithApplication
import play.twirl.api.Html
import uk.gov.nationalarchives.omega.editorial._
import uk.gov.nationalarchives.omega.editorial.models.Credentials

class LoginViewSpec extends PlaySpec {

  "Login Html" should {
    "render the given title and heading" in new WithApplication {
      val title = "TitleTest"
      val heading = "HeadingTest"
      val credentialsForm: Form[Credentials] = Form(
        mapping(
          "username" -> text,
          "password" -> text
        )(Credentials.apply)(Credentials.unapply)
      )

      val loginHtml: Html =
        views.html
          .login(title, heading, credentialsForm)(Helpers.stubMessages(), CSRFTokenHelper.addCSRFToken(FakeRequest()))

      contentAsString(loginHtml) must include(title)
      contentAsString(loginHtml) must include(heading)
    }

    "render multiple errors when no username and password given" in new WithApplication {
      val title = "TitleTest"
      val heading = "HeadingTest"
      val credentialsForm: Form[Credentials] = Form(
        mapping(
          "username" -> text,
          "password" -> text
        )(Credentials.apply)(Credentials.unapply),
        data = Map.empty,
        errors = Seq(FormError("username", "Enter a username"), FormError("password", "Enter a password")),
        value = None
      )

      val loginHtml: Html =
        views.html
          .login(title, heading, credentialsForm)(Helpers.stubMessages(), CSRFTokenHelper.addCSRFToken(FakeRequest()))

      contentAsString(loginHtml) must include("There is a problem")
      contentAsString(loginHtml) must include("Enter a username")
      contentAsString(loginHtml) must include("Enter a password")
    }

    "render an error given an incorrect username and/or password " in new WithApplication {
      val title = "TitleTest"
      val heading = "HeadingTest"
      val credentialsForm: Form[Credentials] = Form(
        mapping(
          "username" -> text,
          "password" -> text
        )(Credentials.apply)(Credentials.unapply),
        data = Map("username" -> "11", "password" -> "22"),
        errors = Seq(FormError("", "Username and/or password is incorrect.")),
        value = None
      )

      val loginHtml: Html =
        views.html
          .login(title, heading, credentialsForm)(Helpers.stubMessages(), CSRFTokenHelper.addCSRFToken(FakeRequest()))

      contentAsString(loginHtml) must include("There is a problem")
      contentAsString(loginHtml) must include("Username and/or password is incorrect.")
    }

    "render an error when no username given" in new WithApplication {
      val title = "TitleTest"
      val heading = "HeadingTest"
      val credentialsForm: Form[Credentials] = Form(
        mapping(
          "username" -> text,
          "password" -> text
        )(Credentials.apply)(Credentials.unapply),
        data = Map.empty,
        errors = Seq(FormError("username", "Enter a username")),
        value = None
      )

      val loginHtml: Html =
        views.html
          .login(title, heading, credentialsForm)(Helpers.stubMessages(), CSRFTokenHelper.addCSRFToken(FakeRequest()))

      contentAsString(loginHtml) must include("There is a problem")
      contentAsString(loginHtml) must include("Enter a username")
    }

    "render an error when no password given" in new WithApplication {
      val title = "TitleTest"
      val heading = "HeadingTest"
      val credentialsForm: Form[Credentials] = Form(
        mapping(
          "username" -> text,
          "password" -> text
        )(Credentials.apply)(Credentials.unapply),
        data = Map.empty,
        errors = Seq(FormError("username", "Enter a password")),
        value = None
      )

      val loginHtml: Html =
        views.html
          .login(title, heading, credentialsForm)(Helpers.stubMessages(), CSRFTokenHelper.addCSRFToken(FakeRequest()))

      contentAsString(loginHtml) must include("There is a problem")
      contentAsString(loginHtml) must include("Enter a password")
    }
  }
}
