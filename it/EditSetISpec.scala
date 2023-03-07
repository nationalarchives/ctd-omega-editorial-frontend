import play.api.http.Status.OK
import play.api.libs.ws.{ WSCookie, WSResponse }
import play.api.test.Helpers.{ await, defaultAwaitTimeout }
import support.CommonMatchers._

class EditSetISpec extends BaseISpec {

  "when viewed, for a specified edit set ID" must {
    "when logged in" must {
      "have the expected contents" when {
        "not specifying the page" when {
          "nor the ordering" in {

            val editSetId = "1"
            val response = getEditSetPageWhileLoggedIn(editSetId, None, None, None)

            assertPageAsExpected(
              response,
              OK,
              ExpectedEditSetPage(
                title = "Browse Edit Set (Page 1 of 2)",
                header = "Edit set: COAL 80 Sample",
                caption = "Showing 1 - 10 of 12 records",
                ccrExpectedLinkDirection = "descending",
                scopeAndContentExpectedLinkDirection = "ascending",
                coveringDatesExpectedLinkDirection = "ascending",
                numberOfPageLinks = 2,
                expectedSummaryRows = Seq(
                  ExpectedEditSetSummaryRow(
                    ccr = "COAL 80/80/1",
                    scopeAndContents =
                      "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (B)",
                    coveringDates = "1962"
                  ),
                  ExpectedEditSetSummaryRow(
                    ccr = "COAL 80/80/10",
                    scopeAndContents =
                      "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (J)",
                    coveringDates = "1973"
                  ),
                  ExpectedEditSetSummaryRow(
                    ccr = "COAL 80/80/11",
                    scopeAndContents =
                      "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (K)",
                    coveringDates = "1975"
                  ),
                  ExpectedEditSetSummaryRow(
                    ccr = "COAL 80/80/12",
                    scopeAndContents =
                      "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (L)",
                    coveringDates = "1977"
                  ),
                  ExpectedEditSetSummaryRow(
                    ccr = "COAL 80/80/2",
                    scopeAndContents =
                      "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (A)",
                    coveringDates = "1966"
                  ),
                  ExpectedEditSetSummaryRow(
                    ccr = "COAL 80/80/3",
                    scopeAndContents =
                      "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (C)",
                    coveringDates = "1964"
                  ),
                  ExpectedEditSetSummaryRow(
                    ccr = "COAL 80/80/4",
                    scopeAndContents =
                      "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (D)",
                    coveringDates = "1961"
                  ),
                  ExpectedEditSetSummaryRow(
                    ccr = "COAL 80/80/5",
                    scopeAndContents =
                      "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (E)",
                    coveringDates = "1963"
                  ),
                  ExpectedEditSetSummaryRow(
                    ccr = "COAL 80/80/6",
                    scopeAndContents =
                      "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (F)",
                    coveringDates = "1965"
                  ),
                  ExpectedEditSetSummaryRow(
                    ccr = "COAL 80/80/7",
                    scopeAndContents =
                      "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (G)",
                    coveringDates = "1967"
                  )
                )
              )
            )

            assertCallMadeToGetEditSet(editSetId)

          }
          "specifically ordering by" when {

            val baseExpectedEditSetPage = ExpectedEditSetPage(
              title = "Browse Edit Set (Page 1 of 2)",
              header = "Edit set: COAL 80 Sample",
              caption = "Showing 1 - 10 of 12 records",
              ccrExpectedLinkDirection = "",
              scopeAndContentExpectedLinkDirection = "",
              coveringDatesExpectedLinkDirection = "",
              numberOfPageLinks = 2,
              expectedSummaryRows = Seq.empty
            )

            "CCR, ascending" in {

              val editSetId = "1"
              val response = getEditSetPageWhileLoggedIn(editSetId, None, Some("ccr"), Some("ascending"))

              assertPageAsExpected(
                response,
                OK,
                baseExpectedEditSetPage.copy(
                  ccrExpectedLinkDirection = "descending",
                  scopeAndContentExpectedLinkDirection = "ascending",
                  coveringDatesExpectedLinkDirection = "ascending",
                  expectedSummaryRows = Seq(
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/1",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (B)",
                      coveringDates = "1962"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/10",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (J)",
                      coveringDates = "1973"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/11",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (K)",
                      coveringDates = "1975"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/12",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (L)",
                      coveringDates = "1977"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/2",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (A)",
                      coveringDates = "1966"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/3",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (C)",
                      coveringDates = "1964"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/4",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (D)",
                      coveringDates = "1961"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/5",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (E)",
                      coveringDates = "1963"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/6",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (F)",
                      coveringDates = "1965"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/7",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (G)",
                      coveringDates = "1967"
                    )
                  )
                )
              )

              assertCallMadeToGetEditSet(editSetId)

            }
            "CCR, descending" in {

              val editSetId = "1"

              val response = getEditSetPageWhileLoggedIn(editSetId, None, Some("ccr"), Some("descending"))

              assertPageAsExpected(
                response,
                OK,
                baseExpectedEditSetPage.copy(
                  ccrExpectedLinkDirection = "ascending",
                  scopeAndContentExpectedLinkDirection = "ascending",
                  coveringDatesExpectedLinkDirection = "ascending",
                  expectedSummaryRows = Seq(
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/9",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (I)",
                      coveringDates = "1971"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/8",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (H)",
                      coveringDates = "1969"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/7",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (G)",
                      coveringDates = "1967"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/6",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (F)",
                      coveringDates = "1965"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/5",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (E)",
                      coveringDates = "1963"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/4",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (D)",
                      coveringDates = "1961"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/3",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (C)",
                      coveringDates = "1964"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/2",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (A)",
                      coveringDates = "1966"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/12",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (L)",
                      coveringDates = "1977"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/11",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (K)",
                      coveringDates = "1975"
                    )
                  )
                )
              )

              assertCallMadeToGetEditSet(editSetId)

            }
            "Scope and Content, ascending" in {

              val editSetId = "1"

              val response = getEditSetPageWhileLoggedIn(editSetId, None, Some("scope-and-content"), Some("ascending"))

              assertPageAsExpected(
                response,
                OK,
                baseExpectedEditSetPage.copy(
                  ccrExpectedLinkDirection = "ascending",
                  scopeAndContentExpectedLinkDirection = "descending",
                  coveringDatesExpectedLinkDirection = "ascending",
                  expectedSummaryRows = Seq(
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/2",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (A)",
                      coveringDates = "1966"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/1",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (B)",
                      coveringDates = "1962"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/3",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (C)",
                      coveringDates = "1964"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/4",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (D)",
                      coveringDates = "1961"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/5",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (E)",
                      coveringDates = "1963"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/6",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (F)",
                      coveringDates = "1965"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/7",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (G)",
                      coveringDates = "1967"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/8",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (H)",
                      coveringDates = "1969"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/9",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (I)",
                      coveringDates = "1971"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/10",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (J)",
                      coveringDates = "1973"
                    )
                  )
                )
              )

              assertCallMadeToGetEditSet(editSetId)

            }
            "Scope and Content, descending" in {

              val editSetId = "1"

              val response = getEditSetPageWhileLoggedIn(editSetId, None, Some("scope-and-content"), Some("descending"))

              assertPageAsExpected(
                response,
                OK,
                baseExpectedEditSetPage.copy(
                  ccrExpectedLinkDirection = "ascending",
                  scopeAndContentExpectedLinkDirection = "ascending",
                  coveringDatesExpectedLinkDirection = "ascending",
                  expectedSummaryRows = Seq(
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/12",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (L)",
                      coveringDates = "1977"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/11",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (K)",
                      coveringDates = "1975"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/10",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (J)",
                      coveringDates = "1973"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/9",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (I)",
                      coveringDates = "1971"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/8",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (H)",
                      coveringDates = "1969"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/7",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (G)",
                      coveringDates = "1967"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/6",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (F)",
                      coveringDates = "1965"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/5",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (E)",
                      coveringDates = "1963"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/4",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (D)",
                      coveringDates = "1961"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/3",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (C)",
                      coveringDates = "1964"
                    )
                  )
                )
              )

              assertCallMadeToGetEditSet(editSetId)

            }
            "Covering dates, ascending" in {

              val editSetId = "1"

              val response = getEditSetPageWhileLoggedIn(editSetId, None, Some("covering-dates"), Some("ascending"))

              assertPageAsExpected(
                response,
                OK,
                baseExpectedEditSetPage.copy(
                  ccrExpectedLinkDirection = "ascending",
                  scopeAndContentExpectedLinkDirection = "ascending",
                  coveringDatesExpectedLinkDirection = "descending",
                  expectedSummaryRows = Seq(
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/4",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (D)",
                      coveringDates = "1961"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/1",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (B)",
                      coveringDates = "1962"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/5",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (E)",
                      coveringDates = "1963"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/3",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (C)",
                      coveringDates = "1964"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/6",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (F)",
                      coveringDates = "1965"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/2",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (A)",
                      coveringDates = "1966"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/7",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (G)",
                      coveringDates = "1967"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/8",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (H)",
                      coveringDates = "1969"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/9",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (I)",
                      coveringDates = "1971"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/10",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (J)",
                      coveringDates = "1973"
                    )
                  )
                )
              )

              assertCallMadeToGetEditSet(editSetId)

            }
            "Covering dates, descending" in {

              val editSetId = "1"

              val response = getEditSetPageWhileLoggedIn(editSetId, None, Some("covering-dates"), Some("descending"))

              assertPageAsExpected(
                response,
                OK,
                baseExpectedEditSetPage.copy(
                  ccrExpectedLinkDirection = "ascending",
                  scopeAndContentExpectedLinkDirection = "ascending",
                  coveringDatesExpectedLinkDirection = "ascending",
                  expectedSummaryRows = Seq(
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/12",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (L)",
                      coveringDates = "1977"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/11",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (K)",
                      coveringDates = "1975"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/10",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (J)",
                      coveringDates = "1973"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/9",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (I)",
                      coveringDates = "1971"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/8",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (H)",
                      coveringDates = "1969"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/7",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (G)",
                      coveringDates = "1967"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/2",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (A)",
                      coveringDates = "1966"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/6",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (F)",
                      coveringDates = "1965"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/3",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (C)",
                      coveringDates = "1964"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/5",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (E)",
                      coveringDates = "1963"
                    )
                  )
                )
              )

              assertCallMadeToGetEditSet(editSetId)

            }
            "Unknown field and direction" in {

              val editSetId = "1"

              val response = getEditSetPageWhileLoggedIn(editSetId, None, Some("age"), Some("upwards"))

              assertPageAsExpected(
                response,
                OK,
                baseExpectedEditSetPage.copy(
                  ccrExpectedLinkDirection = "descending",
                  scopeAndContentExpectedLinkDirection = "ascending",
                  coveringDatesExpectedLinkDirection = "ascending",
                  expectedSummaryRows = Seq(
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/1",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (B)",
                      coveringDates = "1962"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/10",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (J)",
                      coveringDates = "1973"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/11",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (K)",
                      coveringDates = "1975"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/12",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (L)",
                      coveringDates = "1977"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/2",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (A)",
                      coveringDates = "1966"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/3",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (C)",
                      coveringDates = "1964"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/4",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (D)",
                      coveringDates = "1961"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/5",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (E)",
                      coveringDates = "1963"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/6",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (F)",
                      coveringDates = "1965"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/7",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (G)",
                      coveringDates = "1967"
                    )
                  )
                )
              )

              assertCallMadeToGetEditSet(editSetId)

            }
          }
        }
        "specifying an existing page" when {
          "but not the ordering" in {

            val editSetId = "1"

            val response = getEditSetPageWhileLoggedIn(editSetId, Some(2), None, None)

            assertPageAsExpected(
              response,
              OK,
              ExpectedEditSetPage(
                title = "Browse Edit Set (Page 2 of 2)",
                header = "Edit set: COAL 80 Sample",
                caption = "Showing 11 - 12 of 12 records",
                ccrExpectedLinkDirection = "descending",
                scopeAndContentExpectedLinkDirection = "ascending",
                coveringDatesExpectedLinkDirection = "ascending",
                numberOfPageLinks = 2,
                expectedSummaryRows = Seq(
                  ExpectedEditSetSummaryRow(
                    ccr = "COAL 80/80/8",
                    scopeAndContents =
                      "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (H)",
                    coveringDates = "1969"
                  ),
                  ExpectedEditSetSummaryRow(
                    ccr = "COAL 80/80/9",
                    scopeAndContents =
                      "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (I)",
                    coveringDates = "1971"
                  )
                )
              )
            )

            assertCallMadeToGetEditSet(editSetId)

          }
          "specifically ordering by" when {

            val baseExpectedEditSetPage = ExpectedEditSetPage(
              title = "Browse Edit Set (Page 2 of 2)",
              header = "Edit set: COAL 80 Sample",
              caption = "Showing 11 - 12 of 12 records",
              ccrExpectedLinkDirection = "",
              scopeAndContentExpectedLinkDirection = "",
              coveringDatesExpectedLinkDirection = "",
              numberOfPageLinks = 2,
              expectedSummaryRows = Seq.empty
            )

            "CCR, ascending" in {

              val editSetId = "1"

              val response = getEditSetPageWhileLoggedIn(editSetId, Some(2), Some("ccr"), Some("ascending"))

              assertPageAsExpected(
                response,
                OK,
                baseExpectedEditSetPage.copy(
                  ccrExpectedLinkDirection = "descending",
                  scopeAndContentExpectedLinkDirection = "ascending",
                  coveringDatesExpectedLinkDirection = "ascending",
                  expectedSummaryRows = Seq(
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/8",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (H)",
                      coveringDates = "1969"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/9",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (I)",
                      coveringDates = "1971"
                    )
                  )
                )
              )

              assertCallMadeToGetEditSet(editSetId)

            }
            "CCR, descending" in {

              val editSetId = "1"

              val response = getEditSetPageWhileLoggedIn(editSetId, Some(2), Some("ccr"), Some("descending"))

              assertPageAsExpected(
                response,
                OK,
                baseExpectedEditSetPage.copy(
                  ccrExpectedLinkDirection = "ascending",
                  scopeAndContentExpectedLinkDirection = "ascending",
                  coveringDatesExpectedLinkDirection = "ascending",
                  expectedSummaryRows = Seq(
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/10",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (J)",
                      coveringDates = "1973"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/1",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (B)",
                      coveringDates = "1962"
                    )
                  )
                )
              )

              assertCallMadeToGetEditSet(editSetId)

            }
            "Scope and Content, ascending" in {

              val editSetId = "1"

              val response =
                getEditSetPageWhileLoggedIn(editSetId, Some(2), Some("scope-and-content"), Some("ascending"))

              assertPageAsExpected(
                response,
                OK,
                baseExpectedEditSetPage.copy(
                  ccrExpectedLinkDirection = "ascending",
                  scopeAndContentExpectedLinkDirection = "descending",
                  coveringDatesExpectedLinkDirection = "ascending",
                  expectedSummaryRows = Seq(
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/11",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (K)",
                      coveringDates = "1975"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/12",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (L)",
                      coveringDates = "1977"
                    )
                  )
                )
              )

              assertCallMadeToGetEditSet(editSetId)

            }
            "Scope and Content, descending" in {

              val editSetId = "1"

              val response =
                getEditSetPageWhileLoggedIn(editSetId, Some(2), Some("scope-and-content"), Some("descending"))

              assertPageAsExpected(
                response,
                OK,
                baseExpectedEditSetPage.copy(
                  ccrExpectedLinkDirection = "ascending",
                  scopeAndContentExpectedLinkDirection = "ascending",
                  coveringDatesExpectedLinkDirection = "ascending",
                  expectedSummaryRows = Seq(
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/1",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (B)",
                      coveringDates = "1962"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/2",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (A)",
                      coveringDates = "1966"
                    )
                  )
                )
              )

              assertCallMadeToGetEditSet(editSetId)

            }
            "Covering dates, ascending" in {

              val editSetId = "1"

              val response = getEditSetPageWhileLoggedIn(editSetId, Some(2), Some("covering-dates"), Some("ascending"))

              assertPageAsExpected(
                response,
                OK,
                baseExpectedEditSetPage.copy(
                  ccrExpectedLinkDirection = "ascending",
                  scopeAndContentExpectedLinkDirection = "ascending",
                  coveringDatesExpectedLinkDirection = "descending",
                  expectedSummaryRows = Seq(
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/11",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (K)",
                      coveringDates = "1975"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/12",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (L)",
                      coveringDates = "1977"
                    )
                  )
                )
              )

              assertCallMadeToGetEditSet(editSetId)

            }
            "Covering dates, descending" in {

              val editSetId = "1"

              val response = getEditSetPageWhileLoggedIn(editSetId, Some(2), Some("covering-dates"), Some("descending"))

              assertPageAsExpected(
                response,
                OK,
                baseExpectedEditSetPage.copy(
                  ccrExpectedLinkDirection = "ascending",
                  scopeAndContentExpectedLinkDirection = "ascending",
                  coveringDatesExpectedLinkDirection = "ascending",
                  expectedSummaryRows = Seq(
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/1",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (B)",
                      coveringDates = "1962"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/4",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (D)",
                      coveringDates = "1961"
                    )
                  )
                )
              )

              assertCallMadeToGetEditSet(editSetId)

            }
            "Unknown field and direction" in {

              val editSetId = "1"

              val response = getEditSetPageWhileLoggedIn(editSetId, Some(2), Some("age"), Some("upwards"))

              assertPageAsExpected(
                response,
                OK,
                baseExpectedEditSetPage.copy(
                  ccrExpectedLinkDirection = "descending",
                  scopeAndContentExpectedLinkDirection = "ascending",
                  coveringDatesExpectedLinkDirection = "ascending",
                  expectedSummaryRows = Seq(
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/8",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (H)",
                      coveringDates = "1969"
                    ),
                    ExpectedEditSetSummaryRow(
                      ccr = "COAL 80/80/9",
                      scopeAndContents =
                        "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (I)",
                      coveringDates = "1971"
                    )
                  )
                )
              )

              assertCallMadeToGetEditSet(editSetId)

            }
          }

        }
        "specifying a page which doesn't exist" when {

          val requestedPageNumber = 6
          val baseExpectedEditSetPage = ExpectedEditSetPage(
            title = "Browse Edit Set (Page 6 of 2)",
            header = "Edit set: COAL 80 Sample",
            caption = "Showing 51 - 50 of 12 records",
            ccrExpectedLinkDirection = "",
            scopeAndContentExpectedLinkDirection = "",
            coveringDatesExpectedLinkDirection = "",
            numberOfPageLinks = 2,
            expectedSummaryRows = Seq.empty
          )

          "but not the ordering" in {

            val editSetId = "1"

            val response = getEditSetPageWhileLoggedIn(editSetId, Some(requestedPageNumber), None, None)

            assertPageAsExpected(
              response,
              OK,
              baseExpectedEditSetPage.copy(
                ccrExpectedLinkDirection = "descending",
                scopeAndContentExpectedLinkDirection = "ascending",
                coveringDatesExpectedLinkDirection = "ascending",
                numberOfPageLinks = 3,
                expectedSummaryRows = Seq.empty
              )
            )

            assertCallMadeToGetEditSet(editSetId)
          }
          "specifically ordering by" when {
            "CCR, ascending" in {

              val editSetId = "1"

              val response =
                getEditSetPageWhileLoggedIn(editSetId, Some(requestedPageNumber), Some("ccr"), Some("ascending"))

              assertPageAsExpected(
                response,
                OK,
                baseExpectedEditSetPage.copy(
                  ccrExpectedLinkDirection = "descending",
                  scopeAndContentExpectedLinkDirection = "ascending",
                  coveringDatesExpectedLinkDirection = "ascending",
                  numberOfPageLinks = 3,
                  expectedSummaryRows = Seq.empty
                )
              )

              assertCallMadeToGetEditSet(editSetId)

            }

          }
        }
      }
      "only have elements with w3c style" when {
        "ids" in {

          val response = getEditSetPageWhileLoggedIn("1", None, None, None)

          response.status mustBe OK
          val document = asDocument(response)
          document must haveAllLowerCaseIds

        }
        "class names" in {

          val response = getEditSetPageWhileLoggedIn("1", None, None, None)

          response.status mustBe OK
          val document = asDocument(response)
          document must haveAllLowerCssClassNames

        }
      }
    }
    "when not logged in" must {
      "redirect to the login page" in {

        val response = getEditSetPageWhileLoggedOut("1", None, None, None)

        assertRedirection(response, "/login")

        assertNoCallMadeToGetEditSet()
      }
    }
    "when session cookie isn't valid" must {
      "redirect to the login page" in {

        val response = getEditSetPage(
          "1",
          None,
          None,
          None,
          Option(invalidSessionCookie)
        )

        assertRedirection(response, "/login")

        assertNoCallMadeToGetEditSet()

      }
    }

  }

  private def getEditSetPageWhileLoggedIn(
    id: String,
    pageNumber: Option[Int],
    orderingField: Option[String],
    orderingDirection: Option[String]
  ): WSResponse =
    getEditSetPage(id, pageNumber, orderingField, orderingDirection, Some(loginForSessionCookie()))

  private def getEditSetPageWhileLoggedOut(
    id: String,
    pageNumber: Option[Int],
    orderingField: Option[String],
    orderingDirection: Option[String]
  ): WSResponse =
    getEditSetPage(id, pageNumber, orderingField, orderingDirection, None)

  private def getEditSetPage(
    id: String,
    pageNumber: Option[Int],
    orderingField: Option[String],
    orderingDirection: Option[String],
    sessionCookie: Option[WSCookie]
  ): WSResponse = {
    val rawUrl =
      s"/edit-set/$id?" +
        pageNumber.map(page => s"offset=$page&").getOrElse("") +
        orderingField.map(field => s"field=$field&").getOrElse("") +
        orderingDirection.map(direction => s"direction=$direction").getOrElse("")
    val baseUrl = wsUrl(rawUrl).withFollowRedirects(false)
    val url = sessionCookie.map(baseUrl.addCookies(_)).getOrElse(baseUrl)
    await(url.get())
  }

  private def assertPageAsExpected(
    response: WSResponse,
    expectedStatus: Int,
    expectedEditRecordPage: ExpectedEditSetPage
  ): Unit = {

    response.status mustBe expectedStatus

    val document = asDocument(response)
    document must haveTitle(expectedEditRecordPage.title)
    document must haveHeader(expectedEditRecordPage.header)
    document must haveCaption(expectedEditRecordPage.caption)
    document must haveSummaryRows(expectedEditRecordPage.expectedSummaryRows.size)

    document must haveFieldInTableHeader("CCR", "ccr")
    document must haveDirectionInTableHeader("CCR", expectedEditRecordPage.ccrExpectedLinkDirection)

    document must haveFieldInTableHeader("Scope and content", "scope-and-content")
    document must haveDirectionInTableHeader(
      "Scope and content",
      expectedEditRecordPage.scopeAndContentExpectedLinkDirection
    )

    document must haveFieldInTableHeader("Covering dates", "covering-dates")
    document must haveDirectionInTableHeader(
      "Covering dates",
      expectedEditRecordPage.coveringDatesExpectedLinkDirection
    )

    document must haveNumberOfPages(expectedEditRecordPage.numberOfPageLinks)

    expectedEditRecordPage.expectedSummaryRows.zipWithIndex.foreach { case (expectedEditSetSummaryRow, index) =>
      document must haveSummaryRowContents(
        index + 1,
        Seq(
          expectedEditSetSummaryRow.ccr,
          expectedEditSetSummaryRow.scopeAndContents,
          expectedEditSetSummaryRow.coveringDates
        )
      )
    }

  }

  case class ExpectedEditSetPage(
    title: String,
    caption: String,
    header: String,
    ccrExpectedLinkDirection: String,
    scopeAndContentExpectedLinkDirection: String,
    coveringDatesExpectedLinkDirection: String,
    numberOfPageLinks: Int,
    expectedSummaryRows: Seq[ExpectedEditSetSummaryRow]
  )

  case class ExpectedTableHeader(sortField: String, sortOrder: String)

  case class ExpectedEditSetSummaryRow(
    ccr: String,
    scopeAndContents: String,
    coveringDates: String
  )

}
