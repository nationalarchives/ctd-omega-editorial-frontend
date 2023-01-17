import org.scalatest.Assertion
import play.api.http.Status.{BAD_REQUEST, OK}
import play.api.libs.ws.{WSCookie, WSResponse}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import support.CustomMatchers._
import support.ExpectedValues.ExpectedSummaryErrorMessage

class LoginISpec extends BaseISpec {

  "when viewed" should {
    "have the expected contents" in {

      val response = getLoginPage

      assertPage(
        response,
        OK,
        ExpectedLoginPage(
          title = "Sign in",
          headerTitle = "Pan-Archival Catalogue",
          legend = "Sign in: Pan-Archival Catalogue",
          logoutLinkVisible = false
        )
      )

    }
  }
  "when submitted" should {

    val correctUsername = "1234"
    val correctPassword = "1234"

    "fail" when {
      "only username is blank" in {

        val values = Map("username" -> "", "password" -> correctPassword)

        val response = submitFromLoginPage(values)

        assertPage(
          response,
          BAD_REQUEST,
          ExpectedLoginPage(
            title = "Sign in",
            headerTitle = "Pan-Archival Catalogue",
            legend = "Sign in: Pan-Archival Catalogue",
            logoutLinkVisible = false,
            summaryErrorTitle = Some("There is a problem"),
            summaryErrorMessages = Seq(ExpectedSummaryErrorMessage("Enter a username", "username")),
            errorMessageForUsername = Some("Enter a username")
          )
        )

      }
      "only password is blank" in {

        val values = Map("username" -> correctUsername, "password" -> "")

        val response = submitFromLoginPage(values)

        assertPage(
          response,
          BAD_REQUEST,
          ExpectedLoginPage(
            title = "Sign in",
            headerTitle = "Pan-Archival Catalogue",
            legend = "Sign in: Pan-Archival Catalogue",
            logoutLinkVisible = false,
            summaryErrorTitle = Some("There is a problem"),
            summaryErrorMessages = Seq(ExpectedSummaryErrorMessage("Enter a password", "password")),
            errorMessageForPassword = Some("Enter a password")
          )
        )

      }
      "both username and password are blank" in {

        val values = Map("username" -> "", "password" -> "")

        val response = submitFromLoginPage(values)

        assertPage(
          response,
          BAD_REQUEST,
          ExpectedLoginPage(
            title = "Sign in",
            headerTitle = "Pan-Archival Catalogue",
            legend = "Sign in: Pan-Archival Catalogue",
            logoutLinkVisible = false,
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
      "username is incorrect" in {

        val values = Map("username" -> "1230", "password" -> correctPassword)

        val response = submitFromLoginPage(values)

        assertPage(
          response,
          BAD_REQUEST,
          ExpectedLoginPage(
            title = "Sign in",
            headerTitle = "Pan-Archival Catalogue",
            legend = "Sign in: Pan-Archival Catalogue",
            logoutLinkVisible = false,
            summaryErrorTitle = Some("There is a problem"),
            summaryErrorMessages = Seq(
              ExpectedSummaryErrorMessage("Username and/or password is incorrect.", "")
            )
          )
        )

      }
      "password is incorrect" in {

        val values = Map("username" -> correctUsername, "password" -> "1230")

        val response = submitFromLoginPage(values)

        assertPage(
          response,
          BAD_REQUEST,
          ExpectedLoginPage(
            title = "Sign in",
            headerTitle = "Pan-Archival Catalogue",
            legend = "Sign in: Pan-Archival Catalogue",
            logoutLinkVisible = false,
            summaryErrorTitle = Some("There is a problem"),
            summaryErrorMessages = Seq(
              ExpectedSummaryErrorMessage("Username and/or password is incorrect.", "")
            )
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
            title = "Edit set",
            logoutLinkVisible = true
          )
        )

      }
    }
  }

  private def submitFromLoginPage(values: Map[String, String]): WSResponse = {
    val securityInfo = getSecurityInfoFromLoginPage
    await(
      wsUrl("/login")
        .withFollowRedirects(false)
        .addCookies(securityInfo.sessionCookie)
        .post(values ++ Map("csrfToken" -> securityInfo.csrfToken))
    )
  }

  private def getEditSetPage(sessionCookie: WSCookie): WSResponse =
    await(
      wsUrl("/edit-set/1")
        .withFollowRedirects(false)
        .addCookies(sessionCookie)
        .get()
    )

  private def getLoginPage: WSResponse =
    await(
      wsUrl("/login")
        .withFollowRedirects(false)
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

  private def getSecurityInfoFromLoginPage: SecurityInfo = getSecurityInfoFromForm(getLoginPage)

  case class ExpectedLoginPage(
    title: String,
    headerTitle: String,
    legend: String,
    logoutLinkVisible: Boolean,
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
