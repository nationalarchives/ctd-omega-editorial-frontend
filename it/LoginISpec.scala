import org.scalatest.Assertion
import play.api.http.Status.{ BAD_REQUEST, OK }
import play.api.libs.ws.{ WSCookie, WSResponse }
import play.api.test.Helpers.{ await, defaultAwaitTimeout }
import support.CommonMatchers._
import support.ExpectedValues.ExpectedSummaryErrorMessage

class LoginISpec extends BaseISpec {

  "when viewed" must {
    "have the expected contents" in {

      val response = getLoginPage

      assertPage(
        response,
        OK,
        ExpectedLoginPage(
          title = "Sign in",
          headerTitle = "Pan-Archival Catalogue",
          legend = "Sign in: Pan-Archival Catalogue",
          logoutLinkVisible = false,
          username = "",
          password = ""
        )
      )

    }
  }
  "when submitted" must {

    val correctUsername = "1234"
    val correctPassword = "1234"

    val baseExpectedLoginPage =
      ExpectedLoginPage(
        title = "Sign in",
        headerTitle = "Pan-Archival Catalogue",
        legend = "Sign in: Pan-Archival Catalogue",
        logoutLinkVisible = false,
        username = "",
        password = ""
      )

    "fail" when {
      "only username is blank" in {

        val values = Map("username" -> "", "password" -> correctPassword)

        val response = submitFromLoginPage(values)

        assertPage(
          response,
          BAD_REQUEST,
          baseExpectedLoginPage.copy(
            username = "",
            password = "",
            summaryErrorTitle = Some("There is a problem"),
            summaryErrorMessages = Seq(ExpectedSummaryErrorMessage("Enter a username", "username")),
            errorMessageForUsername = Some("Enter a username")
          )
        )

      }
      "only username is blank (padded spaces)" in {

        val values = Map("username" -> "    ", "password" -> correctPassword)

        val response = submitFromLoginPage(values)

        assertPage(
          response,
          BAD_REQUEST,
          baseExpectedLoginPage.copy(
            username = "",
            password = "",
            summaryErrorTitle = Some("There is a problem"),
            summaryErrorMessages = Seq(ExpectedSummaryErrorMessage("Enter a valid username and password", "username")),
            errorMessageForUsername = Some("Enter a valid username and password")
          )
        )

      }
      "only password is blank" in {

        val values = Map("username" -> correctUsername, "password" -> "")

        val response = submitFromLoginPage(values)

        assertPage(
          response,
          BAD_REQUEST,
          baseExpectedLoginPage.copy(
            username = correctUsername,
            password = "",
            summaryErrorTitle = Some("There is a problem"),
            summaryErrorMessages = Seq(ExpectedSummaryErrorMessage("Enter a password", "password")),
            errorMessageForPassword = Some("Enter a password")
          )
        )

      }
      "only password is blank (padded spaces)" in {

        val values = Map("username" -> correctUsername, "password" -> "     ")

        val response = submitFromLoginPage(values)

        assertPage(
          response,
          BAD_REQUEST,
          baseExpectedLoginPage.copy(
            username = "",
            password = "",
            summaryErrorTitle = Some("There is a problem"),
            summaryErrorMessages = Seq(ExpectedSummaryErrorMessage("Enter a valid username and password", "username")),
            errorMessageForUsername = Some("Enter a valid username and password")
          )
        )

      }
      "both username and password are blank" in {

        val values = Map("username" -> "", "password" -> "")

        val response = submitFromLoginPage(values)

        assertPage(
          response,
          BAD_REQUEST,
          baseExpectedLoginPage.copy(
            username = "",
            password = "",
            summaryErrorTitle = Some("There is a problem"),
            summaryErrorMessages = Seq(
              ExpectedSummaryErrorMessage("Enter a username", "username"),
              ExpectedSummaryErrorMessage("Enter a password", "password")
            ),
            errorMessageForUsername = Some("Enter a username"),
            errorMessageForPassword = Some("Enter a password")
          )
        )

      }
      "both username and password are blank (padded spaces)" in {

        val values = Map("username" -> "   ", "password" -> "   ")

        val response = submitFromLoginPage(values)

        assertPage(
          response,
          BAD_REQUEST,
          baseExpectedLoginPage.copy(
            username = "",
            password = "",
            summaryErrorTitle = Some("There is a problem"),
            summaryErrorMessages = Seq(ExpectedSummaryErrorMessage("Enter a valid username and password", "username")),
            errorMessageForUsername = Some("Enter a valid username and password")
          )
        )

      }
      "username is incorrect" in {

        val values = Map("username" -> "1230", "password" -> correctPassword)

        val response = submitFromLoginPage(values)

        assertPage(
          response,
          BAD_REQUEST,
          baseExpectedLoginPage.copy(
            username = "",
            password = "",
            summaryErrorTitle = Some("There is a problem"),
            summaryErrorMessages = Seq(ExpectedSummaryErrorMessage("Enter a valid username and password", "username")),
            errorMessageForUsername = Some("Enter a valid username and password")
          )
        )

      }
      "username is correct - except for a leading space" in {

        val values = Map("username" -> s" $correctUsername", "password" -> correctPassword)

        val response = submitFromLoginPage(values)

        assertPage(
          response,
          BAD_REQUEST,
          baseExpectedLoginPage.copy(
            username = "",
            password = "",
            summaryErrorTitle = Some("There is a problem"),
            summaryErrorMessages = Seq(ExpectedSummaryErrorMessage("Enter a valid username and password", "username")),
            errorMessageForUsername = Some("Enter a valid username and password")
          )
        )

      }
      "username is correct - except for a trailing space" in {

        val values = Map("username" -> s"$correctUsername ", "password" -> correctPassword)

        val response = submitFromLoginPage(values)

        assertPage(
          response,
          BAD_REQUEST,
          baseExpectedLoginPage.copy(
            username = "",
            password = "",
            summaryErrorTitle = Some("There is a problem"),
            summaryErrorMessages = Seq(ExpectedSummaryErrorMessage("Enter a valid username and password", "username")),
            errorMessageForUsername = Some("Enter a valid username and password")
          )
        )

      }
      "password is incorrect" in {

        val values = Map("username" -> correctUsername, "password" -> "1230")

        val response = submitFromLoginPage(values)

        assertPage(
          response,
          BAD_REQUEST,
          baseExpectedLoginPage.copy(
            username = "",
            password = "",
            summaryErrorTitle = Some("There is a problem"),
            summaryErrorMessages = Seq(ExpectedSummaryErrorMessage("Enter a valid username and password", "username")),
            errorMessageForUsername = Some("Enter a valid username and password")
          )
        )

      }
      "password is correct - except for a leading space" in {

        val values = Map("username" -> correctUsername, "password" -> s" $correctPassword")

        val response = submitFromLoginPage(values)

        assertPage(
          response,
          BAD_REQUEST,
          baseExpectedLoginPage.copy(
            username = "",
            password = "",
            summaryErrorTitle = Some("There is a problem"),
            summaryErrorMessages = Seq(ExpectedSummaryErrorMessage("Enter a valid username and password", "username")),
            errorMessageForUsername = Some("Enter a valid username and password")
          )
        )

      }
      "password is correct - except for a trailing space" in {

        val values = Map("username" -> correctUsername, "password" -> s"$correctPassword ")

        val response = submitFromLoginPage(values)

        assertPage(
          response,
          BAD_REQUEST,
          baseExpectedLoginPage.copy(
            username = "",
            password = "",
            summaryErrorTitle = Some("There is a problem"),
            summaryErrorMessages = Seq(ExpectedSummaryErrorMessage("Enter a valid username and password", "username")),
            errorMessageForUsername = Some("Enter a valid username and password")
          )
        )

      }
    }

    "succeed" when {
      "username and password are correct" in {

        val values = Map("username" -> correctUsername, "password" -> correctPassword)

        val responseForLoginSubmission = submitFromLoginPage(values)

        assertRedirection(responseForLoginSubmission, "/edit-set/1")

        val responseForEditSetPage = getEditSetPage(getSessionCookie(responseForLoginSubmission))

        assertPage(
          responseForEditSetPage,
          OK,
          ExpectedEditSetPage(
            title = "Browse Edit Set (Page 1 of 2)",
            logoutLinkVisible = true
          )
        )

      }
    }
  }

  private def submitFromLoginPage(values: Map[String, String]): WSResponse = {
    val getLoginPageResponse = getLoginPage
    val sessionCookie = getSessionCookie(getLoginPageResponse)
    val csrfTokenFromLoginPage = getCsrfToken(getLoginPageResponse)
    await(
      wsUrl("/login")
        .withFollowRedirects(false)
        .addCookies(sessionCookie)
        .post(values ++ Map(csrfTokenName -> csrfTokenFromLoginPage))
    )
  }

  private def getEditSetPage(sessionCookie: WSCookie): WSResponse =
    await(
      wsUrl("/edit-set/1")
        .withFollowRedirects(false)
        .addCookies(sessionCookie)
        .get()
    )

  private def assertPage(response: WSResponse, expectedStatus: Int, expectedPage: ExpectedLoginPage): Assertion = {
    response.status mustBe expectedStatus

    val document = asDocument(response)
    document must haveTitle(expectedPage.title)
    document must haveLegend(expectedPage.legend)
    document must haveHeaderTitle(expectedPage.headerTitle)

    if (expectedPage.logoutLinkVisible) {
      document must haveVisibleLogoutLink
    } else {
      document must not(haveVisibleLogoutLink)
    }

    document must haveUsername(expectedPage.username)
    document must haveAPasswordTypeField("password")
    document must havePassword(expectedPage.password)

    expectedPage.summaryErrorTitle match {
      case Some(summaryErrorTitle) => document must haveSummaryErrorTitle(summaryErrorTitle)
      case None                    => document must haveNoSummaryErrorMessages
    }

    if (expectedPage.summaryErrorMessages.nonEmpty) {
      document must haveSummaryErrorMessages(expectedPage.summaryErrorMessages: _*)
    } else {
      document must haveNoSummaryErrorMessages
    }

    expectedPage.errorMessageForUsername match {
      case Some(errorMessage) => document must haveErrorMessageForUsername(errorMessage)
      case None               => document must haveNoErrorMessageForUsername
    }

    expectedPage.errorMessageForPassword match {
      case Some(errorMessage) => document must haveErrorMessageForPassword(errorMessage)
      case None               => document must haveNoErrorMessageForPassword
    }

  }

  private def assertPage(response: WSResponse, expectedStatus: Int, expectedPage: ExpectedEditSetPage): Assertion = {
    response.status mustBe expectedStatus

    val document = asDocument(response)
    document must haveTitle(expectedPage.title)

    if (expectedPage.logoutLinkVisible) {
      document must haveVisibleLogoutLink
    } else {
      document must not(haveVisibleLogoutLink)
    }
  }

  case class ExpectedLoginPage(
    title: String,
    headerTitle: String,
    legend: String,
    logoutLinkVisible: Boolean,
    username: String,
    password: String,
    summaryErrorTitle: Option[String] = None,
    summaryErrorMessages: Seq[ExpectedSummaryErrorMessage] = Seq.empty,
    errorMessageForUsername: Option[String] = None,
    errorMessageForPassword: Option[String] = None
  )

  case class ExpectedEditSetPage(
    title: String,
    logoutLinkVisible: Boolean
  )

}
