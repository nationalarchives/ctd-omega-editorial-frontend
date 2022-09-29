package views

import org.scalatestplus.play.PlaySpec
import play.api.data.{ Form, FormError }
import play.api.test.{ CSRFTokenHelper, FakeRequest, Helpers }
import play.api.test.Helpers._
import play.test.WithApplication
import play.twirl.api.Html
import uk.gov.nationalarchives.omega.editorial._
import uk.gov.nationalarchives.omega.editorial.forms.CredentialsFormProvider
import uk.gov.nationalarchives.omega.editorial.models.Credentials

class LoginViewSpec extends PlaySpec {

  val messages: Map[String, Map[String, String]] = Map.empty
  implicit val messagesApi = stubMessagesApi(messages)

  "Login Html" should {
    "render the given title and heading" in new WithApplication {
      val title = "TitleTest"
      val heading = "HeadingTest"
      val credentialsForm: Form[Credentials] = CredentialsFormProvider()

      val loginHtml: Html =
        views.html
          .login(title, heading, credentialsForm)(Helpers.stubMessages(), CSRFTokenHelper.addCSRFToken(FakeRequest()))

      contentAsString(loginHtml) must include(title)
      contentAsString(loginHtml) must include(heading)
    }

    "render multiple errors when no username and password given" in new WithApplication {
      val title = "TitleTest"
      val heading = "HeadingTest"
      val userForm: Form[Credentials] = CredentialsFormProvider()
        .fill(Credentials.apply("", ""))
        .withError(FormError("username", "Enter a username"))
        .withError(FormError("password", "Enter a password"))

      val loginHtml: Html =
        views.html
          .login(title, heading, userForm)(Helpers.stubMessages(), CSRFTokenHelper.addCSRFToken(FakeRequest()))

      contentAsString(loginHtml) must include("There is a problem")
      contentAsString(loginHtml) must include("Enter a username")
      contentAsString(loginHtml) must include("Enter a password")
    }

    "render an error given an incorrect username and/or password " in new WithApplication {
      val title = "TitleTest"
      val heading = "HeadingTest"
      val credentialsForm: Form[Credentials] = CredentialsFormProvider()
        .fill(Credentials.apply("11", "22"))
        .withError(FormError("", "Username and/or password is incorrect."))

      val loginHtml: Html =
        views.html
          .login(title, heading, credentialsForm)(Helpers.stubMessages(), CSRFTokenHelper.addCSRFToken(FakeRequest()))

      contentAsString(loginHtml) must include("There is a problem")
      contentAsString(loginHtml) must include("Username and/or password is incorrect.")
    }

    "render an error when no username given" in new WithApplication {
      val title = "TitleTest"
      val heading = "HeadingTest"
      val credentialsForm: Form[Credentials] = CredentialsFormProvider()
        .fill(Credentials.apply("", ""))
        .withError(FormError("username", "Enter a username"))

      val loginHtml: Html =
        views.html
          .login(title, heading, credentialsForm)(Helpers.stubMessages(), CSRFTokenHelper.addCSRFToken(FakeRequest()))

      contentAsString(loginHtml) must include("There is a problem")
      contentAsString(loginHtml) must include("Enter a username")
    }

    "render an error when no password given" in new WithApplication {
      val title = "TitleTest"
      val heading = "HeadingTest"
      val credentialsForm: Form[Credentials] = CredentialsFormProvider()
        .fill(Credentials.apply("", ""))
        .withError(FormError("password", "Enter a password"))

      val loginHtml: Html =
        views.html
          .login(title, heading, credentialsForm)(Helpers.stubMessages(), CSRFTokenHelper.addCSRFToken(FakeRequest()))

      contentAsString(loginHtml) must include("There is a problem")
      contentAsString(loginHtml) must include("Enter a password")
    }
  }
}
