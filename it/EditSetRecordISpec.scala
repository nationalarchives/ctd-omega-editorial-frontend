import EditSetRecordISpec.{ ExpectedEditRecordDiscardPage, ExpectedEditRecordEditPage, ExpectedEditRecordSavePage }
import org.scalatest.compatible.Assertion
import play.api.http.Status.{ BAD_REQUEST, OK }
import play.api.libs.ws.{ WSCookie, WSResponse }
import play.api.test.Helpers.{ await, defaultAwaitTimeout }
import support.CommonMatchers._
import support.ExpectedValues._
import uk.gov.nationalarchives.omega.editorial.models._

import java.time.{ LocalDate, Month }

class EditSetRecordISpec extends BaseISpec {

  "the page for the Edit Set Record" must {
    "behave as expected" when {
      "viewed" when {
        "not logged in" in {

          val response = getEditSetRecordEditPageWhileLoggedOut(idOfExistingEditSet, ociOfExistingRecord)

          assertRedirection(response, "/login")

          assertNoCallMadeToGetEditSet()
          assertNoCallMadeToGetEditSetRecord()
          assertNoCallMadeToUpdateEditSetRecord()

        }
        "session cookie isn't valid" in {

          val response = getEditSetRecordEditPage(idOfExistingEditSet, ociOfExistingRecord, Some(invalidSessionCookie))

          assertRedirection(response, "/login")

          assertNoCallMadeToGetEditSet()
          assertNoCallMadeToGetEditSetRecord()
          assertNoCallMadeToUpdateEditSetRecord()

        }
        "the record's data is completely valid, ensuring" that {
          "all ids conform to the w3c recommendations" in {

            val response = getEditSetRecordEditPageWhileLoggedIn(idOfExistingEditSet, ociOfExistingRecord)

            response.status mustBe OK
            asDocument(response) must haveAllLowerCaseIds

            assertCallMadeToGetEditSet(idOfExistingEditSet)
            assertCallMadeToGetEditSetRecord(idOfExistingEditSet, ociOfExistingRecord)
            assertNoCallMadeToUpdateEditSetRecord()

          }
          "all class names conform to the w3c recommendations" in {

            val response = getEditSetRecordEditPageWhileLoggedIn(idOfExistingEditSet, ociOfExistingRecord)

            response.status mustBe OK
            asDocument(response) must haveAllLowerCssClassNames

            assertCallMadeToGetEditSet(idOfExistingEditSet)
            assertCallMadeToGetEditSetRecord(idOfExistingEditSet, ociOfExistingRecord)
            assertNoCallMadeToUpdateEditSetRecord()

          }
          "all sections appear in the expected order" in {

            val response = getEditSetRecordEditPageWhileLoggedIn(idOfExistingEditSet, ociOfExistingRecord)

            response.status mustBe OK
            asDocument(response) must haveSectionsInCorrectOrder(
              "Scope and content",
              "Creator",
              "Covering dates",
              "Start date",
              "End date",
              "Former reference (Department) (optional)",
              "Former reference (PRO) (optional)",
              "Legal Status",
              "Custodial History (optional)",
              "Held by",
              "Note (optional)",
              "Administrative / biographical background (optional)",
              "Related material",
              "Separated material"
            )

            assertCallMadeToGetEditSet(idOfExistingEditSet)
            assertCallMadeToGetEditSetRecord(idOfExistingEditSet, ociOfExistingRecord)
            assertNoCallMadeToUpdateEditSetRecord()

          }
          "the form is filled out correctly" in {

            val response = getEditSetRecordEditPageWhileLoggedIn(idOfExistingEditSet, "COAL.2022.V1RJW.P")
            assertPageAsExpected(
              response,
              OK,
              ExpectedEditRecordEditPage(
                title = "Edit record",
                heading = "TNA reference: COAL 80/80/1",
                subHeading = "PAC-ID: COAL.2022.V1RJW.P Physical Record",
                legend = "Intellectual properties",
                classicCatalogueRef = "COAL 80/80/1",
                omegaCatalogueId = "COAL.2022.V1RJW.P",
                scopeAndContent =
                  "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (B)",
                coveringDates = "1962",
                formerReferenceDepartment = "MR 193 (9)",
                formerReferencePro = "MPS 4/1",
                startDate = ExpectedDate("1", "1", "1962"),
                endDate = ExpectedDate("31", "12", "1962"),
                legalStatusID = "http://cat.nationalarchives.gov.uk/public-record",
                note = "A note about COAL.2022.V1RJW.P.",
                background = "Photo was taken by a daughter of one of the coal miners who used them.",
                optionsForPlaceOfDepositID = Seq(
                  ExpectedSelectOption("", "Select where this record is held", disabled = true),
                  ExpectedSelectOption(tna, "The National Archives", selected = true),
                  ExpectedSelectOption(britishMuseum, "British Museum, Central Archive"),
                  ExpectedSelectOption(britishLibrary, "British Library, Sound Archive")
                ),
                optionsForCreators = Seq(
                  Seq(
                    ExpectedSelectOption("", "Select creator", disabled = true),
                    ExpectedSelectOption(
                      s"$baseUriAgent.3LG",
                      "Edwin Hill (1793 - 1876)",
                      selected = true
                    ),
                    ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)"),
                    ExpectedSelectOption(
                      s"$baseUriAgent.N6L",
                      "National Coal Board, Northumberland and Durham Division (1964 - 1967)"
                    ),
                    ExpectedSelectOption(s"$baseUriAgent.W91", "Queen's Printer for Scotland (1999 - )")
                  ),
                  Seq(
                    ExpectedSelectOption("", "Select creator", disabled = true),
                    ExpectedSelectOption(s"$baseUriAgent.3LG", "Edwin Hill (1793 - 1876)"),
                    ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)"),
                    ExpectedSelectOption(
                      s"$baseUriAgent.N6L",
                      "National Coal Board, Northumberland and Durham Division (1964 - 1967)",
                      selected = true
                    ),
                    ExpectedSelectOption(s"$baseUriAgent.W91", "Queen's Printer for Scotland (1999 - )")
                  )
                ),
                separatedMaterial = Seq(
                  ExpectedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/5")
                  ),
                  ExpectedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/6")
                  ),
                  ExpectedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/7")
                  )
                ),
                relatedMaterial = Seq(
                  ExpectedMaterial(
                    description =
                      Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
                  ),
                  ExpectedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/3")
                  ),
                  ExpectedMaterial(
                    linkHref = Some("#;"),
                    linkText = Some("COAL 80/80/2"),
                    description =
                      Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
                  )
                ),
                custodialHistory = "Files originally created by successor or predecessor departments for COAL"
              )
            )

            assertCallMadeToGetEditSet(idOfExistingEditSet)
            assertCallMadeToGetEditSetRecord(idOfExistingEditSet, ociOfExistingRecord)
            assertNoCallMadeToUpdateEditSetRecord()

          }
        }
        "the record's data is valid except" that {
          "the place of deposit is unrecognised" in {

            val editSetRecordOci = "COAL.2022.V3RJW.P"
            getExpectedEditSetRecord(editSetRecordOci).placeOfDepositID mustBe "6"

            val response = getEditSetRecordEditPageWhileLoggedIn(idOfExistingEditSet, editSetRecordOci)
            assertPageAsExpected(
              response,
              OK,
              generateExpectedEditRecordPageFromRecord(editSetRecordOci)
                .copy(
                  optionsForPlaceOfDepositID = Seq(
                    ExpectedSelectOption("", "Select where this record is held", selected = true, disabled = true),
                    ExpectedSelectOption(tna, "The National Archives"),
                    ExpectedSelectOption(britishMuseum, "British Museum, Central Archive"),
                    ExpectedSelectOption(britishLibrary, "British Library, Sound Archive")
                  )
                )
            )

            assertCallMadeToGetEditSet(idOfExistingEditSet)
            assertCallMadeToGetEditSetRecord(idOfExistingEditSet, editSetRecordOci)
            assertNoCallMadeToUpdateEditSetRecord()

          }
          "the single creator ID is unrecognised" in {

            val editSetRecordOci = "COAL.2022.V10RJW.P"
            getExpectedEditSetRecord(editSetRecordOci).creatorIDs mustBe Seq(s"$baseUriAgent.XXX")

            val response = getEditSetRecordEditPageWhileLoggedIn(idOfExistingEditSet, editSetRecordOci)
            assertPageAsExpected(
              response,
              OK,
              generateExpectedEditRecordPageFromRecord(editSetRecordOci)
                .copy(
                  optionsForCreators = Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true, selected = true),
                      ExpectedSelectOption(s"$baseUriAgent.3LG", "Edwin Hill (1793 - 1876)"),
                      ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)"),
                      ExpectedSelectOption(
                        s"$baseUriAgent.N6L",
                        "National Coal Board, Northumberland and Durham Division (1964 - 1967)"
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.W91", "Queen's Printer for Scotland (1999 - )")
                    )
                  )
                )
            )

            assertCallMadeToGetEditSet(idOfExistingEditSet)
            assertCallMadeToGetEditSetRecord(idOfExistingEditSet, editSetRecordOci)
            assertNoCallMadeToUpdateEditSetRecord()

          }
          "no creator ID was previously selected" in {

            val editSetRecordOci = "COAL.2022.V4RJW.P"
            getExpectedEditSetRecord(editSetRecordOci).creatorIDs mustBe empty

            val response = getEditSetRecordEditPageWhileLoggedIn(idOfExistingEditSet, editSetRecordOci)

            assertPageAsExpected(
              response,
              OK,
              generateExpectedEditRecordPageFromRecord(editSetRecordOci)
                .copy(
                  optionsForCreators = Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true, selected = true),
                      ExpectedSelectOption(s"$baseUriAgent.3LG", "Edwin Hill (1793 - 1876)"),
                      ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)"),
                      ExpectedSelectOption(
                        s"$baseUriAgent.N6L",
                        "National Coal Board, Northumberland and Durham Division (1964 - 1967)"
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.W91", "Queen's Printer for Scotland (1999 - )")
                    )
                  )
                )
            )

            assertCallMadeToGetEditSet(idOfExistingEditSet)
            assertCallMadeToGetEditSetRecord(idOfExistingEditSet, editSetRecordOci)
            assertNoCallMadeToUpdateEditSetRecord()

          }
        }
        "the Edit Set ID is not recognised" in {
          pending // TODO: Need to implement error response queue
        }
        "the Edit Set Record ID is not recognised" in {
          pending // TODO: Need to implement error response queue
        }
      }
      "saving changes" when {
        "the start date" when {
          "is empty" in {

            val editSetRecordOci = "COAL.2022.V1RJW.P"
            val values =
              valuesFromRecord(editSetRecordOci) ++
                Map(
                  "start-date-day"   -> "",
                  "start-date-month" -> "",
                  "start-date-year"  -> ""
                )

            val response = submitSavingChanges(idOfExistingEditSet, editSetRecordOci, values)

            assertPageAsExpected(
              response,
              BAD_REQUEST,
              generateExpectedEditRecordPageFromRecord(editSetRecordOci)
                .copy(
                  startDate = ExpectedDate("", "", ""),
                  summaryErrorMessages =
                    Seq(ExpectedSummaryErrorMessage("Start date is not a valid date", "start-date-day")),
                  errorMessageForStartDate = Some("Start date is not a valid date")
                )
            )

            assertCallMadeToGetEditSet(idOfExistingEditSet)
            assertCallMadeToGetEditSetRecord(idOfExistingEditSet, editSetRecordOci)
            assertNoCallMadeToUpdateEditSetRecord()

          }
          "is of an invalid format" in {

            val editSetRecordOci = "COAL.2022.V1RJW.P"
            val values =
              valuesFromRecord(editSetRecordOci) ++
                Map(
                  "start-date-day"   -> "XX",
                  "start-date-month" -> "11",
                  "start-date-year"  -> "1960"
                )

            val response = submitSavingChanges(idOfExistingEditSet, editSetRecordOci, values)

            assertPageAsExpected(
              response,
              BAD_REQUEST,
              generateExpectedEditRecordPageFromRecord(editSetRecordOci)
                .copy(
                  startDate = ExpectedDate("XX", "11", "1960"),
                  summaryErrorMessages =
                    Seq(ExpectedSummaryErrorMessage("Start date is not a valid date", "start-date-day")),
                  errorMessageForStartDate = Some("Start date is not a valid date")
                )
            )

            assertCallMadeToGetEditSet(idOfExistingEditSet)
            assertCallMadeToGetEditSetRecord(idOfExistingEditSet, editSetRecordOci)
            assertNoCallMadeToUpdateEditSetRecord()

          }
          "isn't a real date" in {

            val editSetRecordOci = "COAL.2022.V1RJW.P"
            val values =
              valuesFromRecord(editSetRecordOci) ++
                Map(
                  "start-date-day"   -> "29",
                  "start-date-month" -> "2",
                  "start-date-year"  -> "2022"
                )

            val response = submitSavingChanges(idOfExistingEditSet, editSetRecordOci, values)

            assertPageAsExpected(
              response,
              BAD_REQUEST,
              generateExpectedEditRecordPageFromRecord(editSetRecordOci)
                .copy(
                  startDate = ExpectedDate("29", "2", "2022"),
                  summaryErrorMessages =
                    Seq(ExpectedSummaryErrorMessage("Start date is not a valid date", "start-date-day")),
                  errorMessageForStartDate = Some("Start date is not a valid date")
                )
            )

            assertCallMadeToGetEditSet(idOfExistingEditSet)
            assertCallMadeToGetEditSetRecord(idOfExistingEditSet, editSetRecordOci)
            assertNoCallMadeToUpdateEditSetRecord()

          }
          "has a leading zero" when {
            "in day" in {

              val editSetRecordOci = "COAL.2022.V1RJW.P"
              val values =
                valuesFromRecord(editSetRecordOci) ++
                  Map(
                    "start-date-day"   -> "02",
                    "start-date-month" -> "1",
                    "start-date-year"  -> "1962"
                  )

              val submissionResponse = submitSavingChanges(idOfExistingEditSet, editSetRecordOci, values)

              assertRedirection(
                submissionResponse,
                s"/edit-set/$idOfExistingEditSet/record/$editSetRecordOci/edit/save"
              )

              assertCallMadeToGetEditSet(idOfExistingEditSet)
              assertCallMadeToGetEditSetRecord(idOfExistingEditSet, editSetRecordOci)
              val updateEditSetRecordForRecord = generateUpdateEditSetRecord(idOfExistingEditSet, editSetRecordOci)
              assertCallMadeToUpdateEditSetRecord(
                updateEditSetRecordForRecord.copy(fields =
                  updateEditSetRecordForRecord.fields.copy(startDate = LocalDate.of(1962, Month.JANUARY, 2))
                )
              )

            }
            "in month" in {

              val editSetRecordOci = "COAL.2022.V1RJW.P"
              val values =
                valuesFromRecord(editSetRecordOci) ++
                  Map(
                    "start-date-day"   -> "1",
                    "start-date-month" -> "02",
                    "start-date-year"  -> "1962"
                  )

              val submissionResponse = submitSavingChanges(idOfExistingEditSet, editSetRecordOci, values)

              assertRedirection(
                submissionResponse,
                s"/edit-set/$idOfExistingEditSet/record/$editSetRecordOci/edit/save"
              )

              assertCallMadeToGetEditSet(idOfExistingEditSet)
              assertCallMadeToGetEditSetRecord(idOfExistingEditSet, editSetRecordOci)
              val updateEditSetRecordForRecord = generateUpdateEditSetRecord(idOfExistingEditSet, editSetRecordOci)
              assertCallMadeToUpdateEditSetRecord(
                updateEditSetRecordForRecord.copy(fields =
                  updateEditSetRecordForRecord.fields.copy(startDate = LocalDate.of(1962, Month.FEBRUARY, 1))
                )
              )

            }
            "in year" in {

              val editSetRecordOci = "COAL.2022.V1RJW.P"
              val values =
                valuesFromRecord(editSetRecordOci) ++
                  Map(
                    "start-date-day"   -> "1",
                    "start-date-month" -> "1",
                    "start-date-year"  -> "0962"
                  )

              val submissionResponse = submitSavingChanges(idOfExistingEditSet, editSetRecordOci, values)

              assertRedirection(
                submissionResponse,
                s"/edit-set/$idOfExistingEditSet/record/$editSetRecordOci/edit/save"
              )

              assertCallMadeToGetEditSet(idOfExistingEditSet)
              assertCallMadeToGetEditSetRecord(idOfExistingEditSet, editSetRecordOci)
              val updateEditSetRecordForRecord = generateUpdateEditSetRecord(idOfExistingEditSet, editSetRecordOci)
              assertCallMadeToUpdateEditSetRecord(
                updateEditSetRecordForRecord.copy(fields =
                  updateEditSetRecordForRecord.fields.copy(startDate = LocalDate.of(962, Month.JANUARY, 1))
                )
              )

            }

          }
        }
        "the end date" when {
          "is empty" in {

            val editSetRecordOci = "COAL.2022.V1RJW.P"
            val values =
              valuesFromRecord(editSetRecordOci) ++
                Map(
                  "end-date-day"   -> "",
                  "end-date-month" -> "",
                  "end-date-year"  -> ""
                )

            val response = submitSavingChanges(idOfExistingEditSet, editSetRecordOci, values)

            assertPageAsExpected(
              response,
              BAD_REQUEST,
              generateExpectedEditRecordPageFromRecord(editSetRecordOci)
                .copy(
                  endDate = ExpectedDate("", "", ""),
                  summaryErrorMessages =
                    Seq(ExpectedSummaryErrorMessage("End date is not a valid date", "end-date-day")),
                  errorMessageForEndDate = Some("End date is not a valid date")
                )
            )

            assertCallMadeToGetEditSet(idOfExistingEditSet)
            assertCallMadeToGetEditSetRecord(idOfExistingEditSet, ociOfExistingRecord)
            assertNoCallMadeToUpdateEditSetRecord()

          }
          "is of an invalid format" in {

            val editSetRecordOci = "COAL.2022.V1RJW.P"
            val values =
              valuesFromRecord(editSetRecordOci) ++
                Map(
                  "end-date-day"   -> "XX",
                  "end-date-month" -> "12",
                  "end-date-year"  -> "2000"
                )

            val response = submitSavingChanges(idOfExistingEditSet, editSetRecordOci, values)

            assertPageAsExpected(
              response,
              BAD_REQUEST,
              generateExpectedEditRecordPageFromRecord(editSetRecordOci)
                .copy(
                  endDate = ExpectedDate("XX", "12", "2000"),
                  summaryErrorMessages =
                    Seq(ExpectedSummaryErrorMessage("End date is not a valid date", "end-date-day")),
                  errorMessageForEndDate = Some("End date is not a valid date")
                )
            )

            assertCallMadeToGetEditSet(idOfExistingEditSet)
            assertCallMadeToGetEditSetRecord(idOfExistingEditSet, ociOfExistingRecord)
            assertNoCallMadeToUpdateEditSetRecord()

          }
          "isn't a real date" in {

            val editSetRecordOci = "COAL.2022.V1RJW.P"
            val values =
              valuesFromRecord(editSetRecordOci) ++
                Map(
                  "end-date-day"   -> "29",
                  "end-date-month" -> "2",
                  "end-date-year"  -> "2022"
                )

            val response = submitSavingChanges(idOfExistingEditSet, editSetRecordOci, values)

            assertPageAsExpected(
              response,
              BAD_REQUEST,
              generateExpectedEditRecordPageFromRecord(editSetRecordOci)
                .copy(
                  endDate = ExpectedDate("29", "2", "2022"),
                  summaryErrorMessages =
                    Seq(ExpectedSummaryErrorMessage("End date is not a valid date", "end-date-day")),
                  errorMessageForEndDate = Some("End date is not a valid date")
                )
            )

            assertCallMadeToGetEditSet(idOfExistingEditSet)
            assertCallMadeToGetEditSetRecord(idOfExistingEditSet, ociOfExistingRecord)
            assertNoCallMadeToUpdateEditSetRecord()

          }
          "is before start date" in {

            val editSetRecordOci = "COAL.2022.V1RJW.P"
            val values =
              valuesFromRecord(editSetRecordOci) ++
                Map(
                  "start-date-day"   -> "12",
                  "start-date-month" -> "10",
                  "start-date-year"  -> "2020",
                  "end-date-day"     -> "11",
                  "end-date-month"   -> "10",
                  "end-date-year"    -> "2020"
                )

            val response = submitSavingChanges(idOfExistingEditSet, editSetRecordOci, values)

            assertPageAsExpected(
              response,
              BAD_REQUEST,
              generateExpectedEditRecordPageFromRecord(editSetRecordOci)
                .copy(
                  startDate = ExpectedDate("12", "10", "2020"),
                  endDate = ExpectedDate("11", "10", "2020"),
                  summaryErrorMessages =
                    Seq(ExpectedSummaryErrorMessage("End date cannot precede start date", "end-date-day")),
                  errorMessageForEndDate = Some("End date cannot precede start date")
                )
            )

            assertCallMadeToGetEditSet(idOfExistingEditSet)
            assertCallMadeToGetEditSetRecord(idOfExistingEditSet, ociOfExistingRecord)
            assertNoCallMadeToUpdateEditSetRecord()

          }
          "has a leading zero" when {
            "in day" in {

              val editSetRecordOci = "COAL.2022.V1RJW.P"
              val values =
                valuesFromRecord(editSetRecordOci) ++
                  Map(
                    "end-date-day"   -> "03",
                    "end-date-month" -> "12",
                    "end-date-year"  -> "1962"
                  )

              val submissionResponse = submitSavingChanges(idOfExistingEditSet, editSetRecordOci, values)

              assertRedirection(
                submissionResponse,
                s"/edit-set/$idOfExistingEditSet/record/$editSetRecordOci/edit/save"
              )

              assertCallMadeToGetEditSet(idOfExistingEditSet)
              assertCallMadeToGetEditSetRecord(idOfExistingEditSet, editSetRecordOci)
              val updateEditSetRecordForRecord = generateUpdateEditSetRecord(idOfExistingEditSet, editSetRecordOci)
              assertCallMadeToUpdateEditSetRecord(
                updateEditSetRecordForRecord.copy(fields =
                  updateEditSetRecordForRecord.fields.copy(endDate = LocalDate.of(1962, Month.DECEMBER, 3))
                )
              )

            }
            "in month" in {

              val editSetRecordOci = "COAL.2022.V1RJW.P"
              val values =
                valuesFromRecord(editSetRecordOci) ++
                  Map(
                    "end-date-day"   -> "1",
                    "end-date-month" -> "09",
                    "end-date-year"  -> "1962"
                  )

              val submissionResponse = submitSavingChanges(idOfExistingEditSet, editSetRecordOci, values)

              assertRedirection(
                submissionResponse,
                s"/edit-set/$idOfExistingEditSet/record/$editSetRecordOci/edit/save"
              )

              assertCallMadeToGetEditSet(idOfExistingEditSet)
              assertCallMadeToGetEditSetRecord(idOfExistingEditSet, editSetRecordOci)
              val updateEditSetRecordForRecord = generateUpdateEditSetRecord(idOfExistingEditSet, editSetRecordOci)
              assertCallMadeToUpdateEditSetRecord(
                updateEditSetRecordForRecord.copy(fields =
                  updateEditSetRecordForRecord.fields.copy(endDate = LocalDate.of(1962, Month.SEPTEMBER, 1))
                )
              )

            }
            "in year" in {

              val editSetRecordOci = "COAL.2022.V1RJW.P"
              val values =
                valuesFromRecord(editSetRecordOci) ++
                  Map(
                    "start-date-day"   -> "1",
                    "start-date-month" -> "1",
                    "start-date-year"  -> "962",
                    "end-date-day"     -> "31",
                    "end-date-month"   -> "12",
                    "end-date-year"    -> "0962"
                  )

              val submissionResponse = submitSavingChanges(idOfExistingEditSet, editSetRecordOci, values)

              assertRedirection(
                submissionResponse,
                s"/edit-set/$idOfExistingEditSet/record/$editSetRecordOci/edit/save"
              )

              assertCallMadeToGetEditSet(idOfExistingEditSet)
              assertCallMadeToGetEditSetRecord(idOfExistingEditSet, editSetRecordOci)
              val updateEditSetRecordForRecord = generateUpdateEditSetRecord(idOfExistingEditSet, editSetRecordOci)
              assertCallMadeToUpdateEditSetRecord(
                updateEditSetRecordForRecord.copy(fields =
                  updateEditSetRecordForRecord.fields.copy(
                    startDate = LocalDate.of(962, Month.JANUARY, 1),
                    endDate = LocalDate.of(962, Month.DECEMBER, 31)
                  )
                )
              )

            }
          }
        }
        "both the start and end dates are invalid" in {

          val editSetRecordOci = "COAL.2022.V1RJW.P"
          val values =
            valuesFromRecord(editSetRecordOci) ++
              Map(
                "start-date-day"   -> "12",
                "start-date-month" -> "14",
                "start-date-year"  -> "2020",
                "end-date-day"     -> "42",
                "end-date-month"   -> "12",
                "end-date-year"    -> "2020"
              )

          val submissionResponse = submitSavingChanges(idOfExistingEditSet, editSetRecordOci, values)

          assertPageAsExpected(
            submissionResponse,
            BAD_REQUEST,
            generateExpectedEditRecordPageFromRecord(editSetRecordOci)
              .copy(
                startDate = ExpectedDate("12", "14", "2020"),
                endDate = ExpectedDate("42", "12", "2020"),
                summaryErrorMessages = Seq(
                  ExpectedSummaryErrorMessage("Start date is not a valid date", "start-date-day"),
                  ExpectedSummaryErrorMessage("End date is not a valid date", "end-date-day")
                ),
                errorMessageForStartDate = Some("Start date is not a valid date"),
                errorMessageForEndDate = Some("End date is not a valid date")
              )
          )

          assertCallMadeToGetEditSet(idOfExistingEditSet)
          assertCallMadeToGetEditSetRecord(idOfExistingEditSet, ociOfExistingRecord)
          assertNoCallMadeToUpdateEditSetRecord()

        }
        "the covering dates" when {
          "are invalid" in {

            val editSetRecordOci = "COAL.2022.V1RJW.P"
            val values =
              valuesFromRecord(editSetRecordOci) ++
                Map(
                  "covering-dates" -> "Oct 1 2004"
                )

            val response = submitSavingChanges(idOfExistingEditSet, editSetRecordOci, values)

            assertPageAsExpected(
              response,
              BAD_REQUEST,
              generateExpectedEditRecordPageFromRecord(editSetRecordOci).copy(
                coveringDates = "Oct 1 2004",
                summaryErrorMessages = Seq(
                  ExpectedSummaryErrorMessage("Covering date format is not valid", "covering-dates")
                ),
                errorMessageForCoveringsDates = Some("Covering date format is not valid")
              )
            )

            assertCallMadeToGetEditSet(idOfExistingEditSet)
            assertCallMadeToGetEditSetRecord(idOfExistingEditSet, ociOfExistingRecord)
            assertNoCallMadeToUpdateEditSetRecord()

          }
          "are too long" in {

            val editSetRecordOci = "COAL.2022.V1RJW.P"
            val excessivelyLongValue = Seq.fill(100)("2004 Oct 1").mkString(";")
            val values =
              valuesFromRecord(editSetRecordOci) ++
                Map(
                  "covering-dates" -> excessivelyLongValue
                )

            val response = submitSavingChanges(idOfExistingEditSet, editSetRecordOci, values)

            assertPageAsExpected(
              response,
              BAD_REQUEST,
              generateExpectedEditRecordPageFromRecord(editSetRecordOci).copy(
                coveringDates = excessivelyLongValue,
                summaryErrorMessages = Seq(
                  ExpectedSummaryErrorMessage(
                    "Covering date too long, maximum length 255 characters",
                    "covering-dates"
                  )
                ),
                errorMessageForCoveringsDates = Some("Covering date too long, maximum length 255 characters")
              )
            )

            assertCallMadeToGetEditSet(idOfExistingEditSet)
            assertCallMadeToGetEditSetRecord(idOfExistingEditSet, ociOfExistingRecord)
            assertNoCallMadeToUpdateEditSetRecord()

          }
          "is empty" in {

            val editSetRecordOci = "COAL.2022.V1RJW.P"
            val values =
              valuesFromRecord(editSetRecordOci) ++
                Map(
                  "covering-dates" -> "  "
                )

            val response = submitSavingChanges(idOfExistingEditSet, editSetRecordOci, values)

            assertPageAsExpected(
              response,
              BAD_REQUEST,
              generateExpectedEditRecordPageFromRecord(editSetRecordOci)
                .copy(
                  coveringDates = "  ",
                  summaryErrorMessages = Seq(
                    ExpectedSummaryErrorMessage("Enter the covering dates", "covering-dates"),
                    ExpectedSummaryErrorMessage("Covering date format is not valid", "covering-dates")
                  ),
                  errorMessageForCoveringsDates = Some("Enter the covering dates")
                )
            )

            assertCallMadeToGetEditSet(idOfExistingEditSet)
            assertCallMadeToGetEditSetRecord(idOfExistingEditSet, ociOfExistingRecord)
            assertNoCallMadeToUpdateEditSetRecord()

          }
        }
        "the place of deposit" when {
          "isn't selected" in {

            val editSetRecordOci = "COAL.2022.V1RJW.P"
            val values =
              valuesFromRecord(editSetRecordOci) ++ Map(
                "place-of-deposit-id" -> ""
              )

            val response = submitSavingChanges(idOfExistingEditSet, editSetRecordOci, values)

            assertPageAsExpected(
              response,
              BAD_REQUEST,
              generateExpectedEditRecordPageFromRecord(editSetRecordOci).copy(
                optionsForPlaceOfDepositID = Seq(
                  ExpectedSelectOption("", "Select where this record is held", selected = true, disabled = true),
                  ExpectedSelectOption(tna, "The National Archives"),
                  ExpectedSelectOption(britishMuseum, "British Museum, Central Archive"),
                  ExpectedSelectOption(britishLibrary, "British Library, Sound Archive")
                ),
                summaryErrorMessages =
                  Seq(ExpectedSummaryErrorMessage("You must choose an option", "place-of-deposit-id")),
                errorMessageForPlaceOfDeposit = Some("You must choose an option")
              )
            )

            assertCallMadeToGetEditSet(idOfExistingEditSet)
            assertCallMadeToGetEditSetRecord(idOfExistingEditSet, ociOfExistingRecord)
            assertNoCallMadeToUpdateEditSetRecord()

          }
          "isn't recognised" in {

            val editSetRecordOci = "COAL.2022.V1RJW.P"
            val values =
              valuesFromRecord(editSetRecordOci) ++
                Map(
                  "place-of-deposit-id" -> "6"
                )

            val response = submitSavingChanges(idOfExistingEditSet, editSetRecordOci, values)

            assertPageAsExpected(
              response,
              BAD_REQUEST,
              generateExpectedEditRecordPageFromRecord(editSetRecordOci).copy(
                optionsForPlaceOfDepositID = Seq(
                  ExpectedSelectOption("", "Select where this record is held", selected = true, disabled = true),
                  ExpectedSelectOption(tna, "The National Archives"),
                  ExpectedSelectOption(britishMuseum, "British Museum, Central Archive"),
                  ExpectedSelectOption(britishLibrary, "British Library, Sound Archive")
                ),
                summaryErrorMessages =
                  Seq(ExpectedSummaryErrorMessage("You must choose an option", "place-of-deposit-id")),
                errorMessageForPlaceOfDeposit = Some("You must choose an option")
              )
            )

            assertCallMadeToGetEditSet(idOfExistingEditSet)
            assertCallMadeToGetEditSetRecord(idOfExistingEditSet, ociOfExistingRecord)
            assertNoCallMadeToUpdateEditSetRecord()

          }
        }
        "the legal status" when {
          "isn't selected" in {

            val editSetRecordOci = "COAL.2022.V1RJW.P"
            val values =
              valuesFromRecord(editSetRecordOci) ++
                Map(
                  "legal-status-id" -> ""
                )

            val response = submitSavingChanges(idOfExistingEditSet, editSetRecordOci, values)

            assertPageAsExpected(
              response,
              BAD_REQUEST,
              generateExpectedEditRecordPageFromRecord(editSetRecordOci).copy(
                legalStatusID = "",
                summaryErrorMessages = Seq(ExpectedSummaryErrorMessage("You must choose an option", "legal-status-id")),
                errorMessageForLegalStatus = Some("You must choose an option")
              )
            )

            assertCallMadeToGetEditSet(idOfExistingEditSet)
            assertCallMadeToGetEditSetRecord(idOfExistingEditSet, ociOfExistingRecord)
            assertNoCallMadeToUpdateEditSetRecord()

          }
          "is unrecognised" in {

            val editSetRecordOci = "COAL.2022.V1RJW.P"
            val values =
              valuesFromRecord(editSetRecordOci) ++
                Map(
                  "legal-status-id" -> "http://catalogue.nationalarchives.gov.uk/unrecognised"
                )

            val submissionResponse = submitSavingChanges(idOfExistingEditSet, editSetRecordOci, values)

            assertRedirection(submissionResponse, s"/edit-set/$idOfExistingEditSet/record/$editSetRecordOci/edit/save")

            assertCallMadeToGetEditSet(idOfExistingEditSet)
            assertCallMadeToGetEditSetRecord(idOfExistingEditSet, editSetRecordOci)
            val updateEditSetRecordForRecord = generateUpdateEditSetRecord(idOfExistingEditSet, editSetRecordOci)
            assertCallMadeToUpdateEditSetRecord(
              updateEditSetRecordForRecord.copy(fields =
                updateEditSetRecordForRecord.fields.copy(
                  legalStatusId = "http://catalogue.nationalarchives.gov.uk/unrecognised"
                )
              )
            )

          }
        }
        "the note" when {
          "is too long" in {

            val editSetRecordOci = "COAL.2022.V1RJW.P"
            val excessivelyLongValue = "Something about something else." * 100
            val values =
              valuesFromRecord(editSetRecordOci) ++
                Map(
                  "note" -> excessivelyLongValue
                )

            val response = submitSavingChanges(idOfExistingEditSet, editSetRecordOci, values)

            assertPageAsExpected(
              response,
              BAD_REQUEST,
              generateExpectedEditRecordPageFromRecord(editSetRecordOci).copy(
                note = excessivelyLongValue,
                summaryErrorMessages =
                  Seq(ExpectedSummaryErrorMessage("Note too long, maximum length 1000 characters", "note")),
                errorMessageForNote = Some("Note too long, maximum length 1000 characters")
              )
            )

            assertCallMadeToGetEditSet(idOfExistingEditSet)
            assertCallMadeToGetEditSetRecord(idOfExistingEditSet, ociOfExistingRecord)
            assertNoCallMadeToUpdateEditSetRecord()

          }
          "is blank" in {

            val editSetRecordOci = "COAL.2022.V1RJW.P"
            val values =
              valuesFromRecord(editSetRecordOci) ++
                Map(
                  "note" -> ""
                )

            val submissionResponse = submitSavingChanges(idOfExistingEditSet, editSetRecordOci, values)

            assertRedirection(submissionResponse, s"/edit-set/$idOfExistingEditSet/record/$editSetRecordOci/edit/save")

            assertCallMadeToGetEditSet(idOfExistingEditSet)
            assertCallMadeToGetEditSetRecord(idOfExistingEditSet, ociOfExistingRecord)
            val updateEditSetRecordForRecord = generateUpdateEditSetRecord(idOfExistingEditSet, editSetRecordOci)
            assertCallMadeToUpdateEditSetRecord(
              updateEditSetRecordForRecord.copy(fields =
                updateEditSetRecordForRecord.fields.copy(
                  note = ""
                )
              )
            )

          }
        }
        "the background" when {
          "is too long" in {

            val editSetRecordOci = "COAL.2022.V1RJW.P"
            val excessivelyLongValue = "X" * 8001
            val values =
              valuesFromRecord(editSetRecordOci) ++
                Map(
                  "background" -> excessivelyLongValue
                )

            val response = submitSavingChanges(idOfExistingEditSet, editSetRecordOci, values)

            assertPageAsExpected(
              response,
              BAD_REQUEST,
              generateExpectedEditRecordPageFromRecord(editSetRecordOci)
                .copy(
                  background = excessivelyLongValue,
                  summaryErrorMessages = Seq(
                    ExpectedSummaryErrorMessage(
                      "Administrative / biographical background too long, maximum length 8000 characters",
                      "background"
                    )
                  ),
                  errorMessageForBackground =
                    Some("Administrative / biographical background too long, maximum length 8000 characters")
                )
            )

            assertCallMadeToGetEditSet(idOfExistingEditSet)
            assertCallMadeToGetEditSetRecord(idOfExistingEditSet, ociOfExistingRecord)
            assertNoCallMadeToUpdateEditSetRecord()

          }
          "is blank" in {

            val editSetRecordOci = "COAL.2022.V1RJW.P"
            val values =
              valuesFromRecord(editSetRecordOci) ++
                Map(
                  "background" -> ""
                )

            val submissionResponse = submitSavingChanges(idOfExistingEditSet, editSetRecordOci, values)

            assertRedirection(submissionResponse, s"/edit-set/$idOfExistingEditSet/record/$editSetRecordOci/edit/save")

            assertCallMadeToGetEditSet(idOfExistingEditSet)
            assertCallMadeToGetEditSetRecord(idOfExistingEditSet, editSetRecordOci)
            val updateEditSetRecordForRecord = generateUpdateEditSetRecord(idOfExistingEditSet, editSetRecordOci)
            assertCallMadeToUpdateEditSetRecord(
              updateEditSetRecordForRecord.copy(fields =
                updateEditSetRecordForRecord.fields.copy(
                  background = ""
                )
              )
            )

          }
        }
        "the custodial history" when {
          "is too long" in {

            val editSetRecordOci = "COAL.2022.V1RJW.P"
            val excessivelyLongValue = "X" * 1001
            val values =
              valuesFromRecord(editSetRecordOci) ++
                Map(
                  "custodial-history" -> excessivelyLongValue
                )

            val response = submitSavingChanges(idOfExistingEditSet, editSetRecordOci, values)

            assertPageAsExpected(
              response,
              BAD_REQUEST,
              generateExpectedEditRecordPageFromRecord(editSetRecordOci).copy(
                custodialHistory = excessivelyLongValue,
                summaryErrorMessages = Seq(
                  ExpectedSummaryErrorMessage(
                    "Custodial history too long, maximum length 1000 characters",
                    "custodial-history"
                  )
                ),
                errorMessageForCustodialHistory = Some("Custodial history too long, maximum length 1000 characters")
              )
            )

            assertCallMadeToGetEditSet(idOfExistingEditSet)
            assertCallMadeToGetEditSetRecord(idOfExistingEditSet, ociOfExistingRecord)
            assertNoCallMadeToUpdateEditSetRecord()

          }
          "is blank" in {

            val editSetRecordOci = "COAL.2022.V1RJW.P"
            val values =
              valuesFromRecord(editSetRecordOci) ++
                Map(
                  "custodial-history" -> ""
                )

            val submissionResponse = submitSavingChanges(idOfExistingEditSet, editSetRecordOci, values)

            assertRedirection(submissionResponse, s"/edit-set/$idOfExistingEditSet/record/$editSetRecordOci/edit/save")

            assertCallMadeToGetEditSet(idOfExistingEditSet)
            assertCallMadeToGetEditSetRecord(idOfExistingEditSet, editSetRecordOci)
            val updateEditSetRecordForRecord = generateUpdateEditSetRecord(idOfExistingEditSet, editSetRecordOci)
            assertCallMadeToUpdateEditSetRecord(
              updateEditSetRecordForRecord.copy(fields =
                updateEditSetRecordForRecord.fields.copy(
                  custodialHistory = ""
                )
              )
            )

          }
          "all spaces" in {

            val editSetRecordOci = "COAL.2022.V1RJW.P"
            val values =
              valuesFromRecord(editSetRecordOci) ++
                Map(
                  "custodial-history" -> "   "
                )

            val submissionResponse = submitSavingChanges(idOfExistingEditSet, editSetRecordOci, values)

            assertRedirection(submissionResponse, s"/edit-set/$idOfExistingEditSet/record/$editSetRecordOci/edit/save")
            assertCallMadeToGetEditSet(idOfExistingEditSet)
            assertCallMadeToGetEditSetRecord(idOfExistingEditSet, editSetRecordOci)
            val updateEditSetRecordForRecord = generateUpdateEditSetRecord(idOfExistingEditSet, editSetRecordOci)
            assertCallMadeToUpdateEditSetRecord(
              updateEditSetRecordForRecord.copy(fields =
                updateEditSetRecordForRecord.fields.copy(
                  custodialHistory = "   "
                )
              )
            )

          }
        }
        "no creator has been selected" in {

          val editSetRecordOci = "COAL.2022.V1RJW.P"
          val values =
            valuesFromRecord(editSetRecordOci) ++
              Map(
                "creator-ids[0]" -> "",
                "creator-ids[1]" -> ""
              )

          val response = submitSavingChanges(idOfExistingEditSet, editSetRecordOci, values)

          assertPageAsExpected(
            response,
            BAD_REQUEST,
            generateExpectedEditRecordPageFromRecord(editSetRecordOci)
              .copy(
                optionsForCreators = Seq(
                  Seq(
                    ExpectedSelectOption("", "Select creator", disabled = true, selected = true),
                    ExpectedSelectOption(s"$baseUriAgent.3LG", "Edwin Hill (1793 - 1876)"),
                    ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)"),
                    ExpectedSelectOption(
                      s"$baseUriAgent.N6L",
                      "National Coal Board, Northumberland and Durham Division (1964 - 1967)"
                    ),
                    ExpectedSelectOption(s"$baseUriAgent.W91", "Queen's Printer for Scotland (1999 - )")
                  ),
                  Seq(
                    ExpectedSelectOption("", "Select creator", disabled = true, selected = true),
                    ExpectedSelectOption(s"$baseUriAgent.3LG", "Edwin Hill (1793 - 1876)"),
                    ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)"),
                    ExpectedSelectOption(
                      s"$baseUriAgent.N6L",
                      "National Coal Board, Northumberland and Durham Division (1964 - 1967)"
                    ),
                    ExpectedSelectOption(s"$baseUriAgent.W91", "Queen's Printer for Scotland (1999 - )")
                  )
                ),
                summaryErrorMessages = Seq(
                  ExpectedSummaryErrorMessage(
                    "You must select at least one creator",
                    "creator-id-0"
                  )
                ),
                errorMessageForCreator = Some("You must select at least one creator")
              )
          )

          assertCallMadeToGetEditSet(idOfExistingEditSet)
          assertCallMadeToGetEditSetRecord(idOfExistingEditSet, ociOfExistingRecord)
          assertNoCallMadeToUpdateEditSetRecord()

        }
        "multiple creators have been selected" in {

          val editSetRecordOci = "COAL.2022.V4RJW.P"
          val values =
            valuesFromRecord(editSetRecordOci) ++
              Map(
                "creator-ids[0]" -> s"$baseUriAgent.3LG",
                "creator-ids[1]" -> s"$baseUriAgent.2YK"
              )

          val submissionResponse = submitSavingChanges(idOfExistingEditSet, editSetRecordOci, values)

          assertRedirection(submissionResponse, s"/edit-set/$idOfExistingEditSet/record/$editSetRecordOci/edit/save")

          assertCallMadeToGetEditSet(idOfExistingEditSet)
          assertCallMadeToGetEditSetRecord(idOfExistingEditSet, editSetRecordOci)
          val updateEditSetRecordForRecord = generateUpdateEditSetRecord(idOfExistingEditSet, editSetRecordOci)
          assertCallMadeToUpdateEditSetRecord(
            updateEditSetRecordForRecord.copy(fields =
              updateEditSetRecordForRecord.fields.copy(
                creatorIDs = Seq(s"$baseUriAgent.3LG", s"$baseUriAgent.2YK")
              )
            )
          )

        }
        "multiple creators have been selected, as well as one empty selection" in {

          val editSetRecordOci = "COAL.2022.V4RJW.P"
          val values =
            valuesFromRecord(editSetRecordOci) ++
              Map(
                "creator-ids[0]" -> s"$baseUriAgent.3LG",
                "creator-ids[1]" -> s"$baseUriAgent.2YK",
                "creator-ids[2]" -> ""
              )

          val submissionResponse = submitSavingChanges(idOfExistingEditSet, editSetRecordOci, values)

          assertRedirection(submissionResponse, s"/edit-set/$idOfExistingEditSet/record/$editSetRecordOci/edit/save")

          assertCallMadeToGetEditSet(idOfExistingEditSet)
          assertCallMadeToGetEditSetRecord(idOfExistingEditSet, editSetRecordOci)
          val updateEditSetRecordForRecord = generateUpdateEditSetRecord(idOfExistingEditSet, editSetRecordOci)
          assertCallMadeToUpdateEditSetRecord(
            updateEditSetRecordForRecord.copy(fields =
              updateEditSetRecordForRecord.fields.copy(
                creatorIDs = Seq(s"$baseUriAgent.3LG", s"$baseUriAgent.2YK")
              )
            )
          )

        }
        "multiple creators have been selected, including duplicates" in {

          val editSetRecordOci = "COAL.2022.V4RJW.P"
          val values =
            valuesFromRecord(editSetRecordOci) ++
              Map(
                "creator-ids[0]" -> s"$baseUriAgent.3LG",
                "creator-ids[1]" -> s"$baseUriAgent.2YK",
                "creator-ids[2]" -> s"$baseUriAgent.2YK"
              )

          val submissionResponse = submitSavingChanges(idOfExistingEditSet, editSetRecordOci, values)

          assertRedirection(submissionResponse, s"/edit-set/$idOfExistingEditSet/record/$editSetRecordOci/edit/save")

          assertCallMadeToGetEditSet(idOfExistingEditSet)
          assertCallMadeToGetEditSetRecord(idOfExistingEditSet, editSetRecordOci)
          val updateEditSetRecordForRecord = generateUpdateEditSetRecord(idOfExistingEditSet, editSetRecordOci)
          assertCallMadeToUpdateEditSetRecord(
            updateEditSetRecordForRecord.copy(fields =
              updateEditSetRecordForRecord.fields.copy(
                creatorIDs = Seq(s"$baseUriAgent.3LG", s"$baseUriAgent.2YK", s"$baseUriAgent.2YK")
              )
            )
          )

        }
        "scope & content" when {
          "is blank" in {

            val editSetRecordOci = "COAL.2022.V1RJW.P"
            val values =
              valuesFromRecord(editSetRecordOci) ++
                Map(
                  "scope-and-content" -> ""
                )

            val response = submitSavingChanges(idOfExistingEditSet, editSetRecordOci, values)

            assertPageAsExpected(
              response,
              BAD_REQUEST,
              generateExpectedEditRecordPageFromRecord(editSetRecordOci).copy(
                scopeAndContent = "",
                summaryErrorMessages = Seq(
                  ExpectedSummaryErrorMessage(
                    "Enter the scope and content",
                    "scope-and-content"
                  )
                ),
                errorMessageForScopeAndContent = Some("Enter the scope and content")
              )
            )

            assertCallMadeToGetEditSet(idOfExistingEditSet)
            assertCallMadeToGetEditSetRecord(idOfExistingEditSet, ociOfExistingRecord)
            assertNoCallMadeToUpdateEditSetRecord()

          }
          "all spaces" in {

            val editSetRecordOci = "COAL.2022.V1RJW.P"
            val values =
              valuesFromRecord(editSetRecordOci) ++
                Map(
                  "scope-and-content" -> "   "
                )

            val submissionResponse = submitSavingChanges(idOfExistingEditSet, editSetRecordOci, values)

            assertRedirection(submissionResponse, s"/edit-set/$idOfExistingEditSet/record/$editSetRecordOci/edit/save")

            assertCallMadeToGetEditSet(idOfExistingEditSet)
            assertCallMadeToGetEditSetRecord(idOfExistingEditSet, editSetRecordOci)
            val updateEditSetRecordForRecord = generateUpdateEditSetRecord(idOfExistingEditSet, editSetRecordOci)
            assertCallMadeToUpdateEditSetRecord(
              updateEditSetRecordForRecord.copy(fields =
                updateEditSetRecordForRecord.fields.copy(
                  description = "   "
                )
              )
            )

          }
          "is too long" in {

            val editSetRecordOci = "COAL.2022.V1RJW.P"
            val excessivelyLongValue = "X" * 8001
            val values =
              valuesFromRecord(editSetRecordOci) ++
                Map(
                  "scope-and-content" -> excessivelyLongValue
                )

            val response = submitSavingChanges(idOfExistingEditSet, editSetRecordOci, values)

            assertPageAsExpected(
              response,
              BAD_REQUEST,
              generateExpectedEditRecordPageFromRecord(editSetRecordOci).copy(
                scopeAndContent = excessivelyLongValue,
                summaryErrorMessages = Seq(
                  ExpectedSummaryErrorMessage(
                    "Scope and content too long, maximum length 8000 characters",
                    "scope-and-content"
                  )
                ),
                errorMessageForScopeAndContent = Some("Scope and content too long, maximum length 8000 characters")
              )
            )

            assertCallMadeToGetEditSet(idOfExistingEditSet)
            assertCallMadeToGetEditSetRecord(idOfExistingEditSet, ociOfExistingRecord)
            assertNoCallMadeToUpdateEditSetRecord()

          }
        }
        "former reference department" when {
          "is blank" in {

            val editSetRecordOci = "COAL.2022.V1RJW.P"
            val values =
              valuesFromRecord(editSetRecordOci) ++
                Map(
                  "former-reference-department" -> ""
                )

            val submissionResponse = submitSavingChanges(idOfExistingEditSet, editSetRecordOci, values)

            assertRedirection(submissionResponse, s"/edit-set/$idOfExistingEditSet/record/$editSetRecordOci/edit/save")

            assertCallMadeToGetEditSet(idOfExistingEditSet)
            assertCallMadeToGetEditSetRecord(idOfExistingEditSet, editSetRecordOci)
            val updateEditSetRecordForRecord = generateUpdateEditSetRecord(idOfExistingEditSet, editSetRecordOci)
            assertCallMadeToUpdateEditSetRecord(
              updateEditSetRecordForRecord.copy(fields =
                updateEditSetRecordForRecord.fields.copy(
                  formerReferenceDepartment = ""
                )
              )
            )

          }
          "all spaces" in {

            val editSetRecordOci = "COAL.2022.V1RJW.P"
            val values =
              valuesFromRecord(editSetRecordOci) ++
                Map(
                  "former-reference-department" -> "   "
                )

            val submissionResponse = submitSavingChanges(idOfExistingEditSet, editSetRecordOci, values)

            assertRedirection(submissionResponse, s"/edit-set/$idOfExistingEditSet/record/$editSetRecordOci/edit/save")

            assertCallMadeToGetEditSet(idOfExistingEditSet)
            assertCallMadeToGetEditSetRecord(idOfExistingEditSet, editSetRecordOci)
            val updateEditSetRecordForRecord = generateUpdateEditSetRecord(idOfExistingEditSet, editSetRecordOci)
            assertCallMadeToUpdateEditSetRecord(
              updateEditSetRecordForRecord.copy(fields =
                updateEditSetRecordForRecord.fields.copy(
                  formerReferenceDepartment = "   "
                )
              )
            )

          }
          "is too long" in {

            val editSetRecordOci = "COAL.2022.V1RJW.P"
            val excessivelyLongValue = "X" * 256
            val values =
              valuesFromRecord(editSetRecordOci) ++
                Map(
                  "former-reference-department" -> excessivelyLongValue
                )

            val response = submitSavingChanges(idOfExistingEditSet, editSetRecordOci, values)

            assertPageAsExpected(
              response,
              BAD_REQUEST,
              generateExpectedEditRecordPageFromRecord(editSetRecordOci)
                .copy(
                  formerReferenceDepartment = excessivelyLongValue,
                  summaryErrorMessages = Seq(
                    ExpectedSummaryErrorMessage(
                      "Former reference - Department too long, maximum length 255 characters",
                      "former-reference-department"
                    )
                  ),
                  errorMessageForFormerReferenceDepartment =
                    Some("Former reference - Department too long, maximum length 255 characters")
                )
            )

            assertCallMadeToGetEditSet(idOfExistingEditSet)
            assertCallMadeToGetEditSetRecord(idOfExistingEditSet, ociOfExistingRecord)
            assertNoCallMadeToUpdateEditSetRecord()

          }
        }
        "former reference pro" when {
          "is blank" in {

            val editSetRecordOci = "COAL.2022.V1RJW.P"
            val values =
              valuesFromRecord(editSetRecordOci) ++
                Map(
                  "former-reference-pro" -> ""
                )

            val submissionResponse = submitSavingChanges(idOfExistingEditSet, editSetRecordOci, values)

            assertRedirection(submissionResponse, s"/edit-set/$idOfExistingEditSet/record/$editSetRecordOci/edit/save")

            assertCallMadeToGetEditSet(idOfExistingEditSet)
            assertCallMadeToGetEditSetRecord(idOfExistingEditSet, editSetRecordOci)
            val updateEditSetRecordForRecord = generateUpdateEditSetRecord(idOfExistingEditSet, editSetRecordOci)
            assertCallMadeToUpdateEditSetRecord(
              updateEditSetRecordForRecord.copy(fields =
                updateEditSetRecordForRecord.fields.copy(
                  formerReferencePro = ""
                )
              )
            )

          }
          "all spaces" in {

            val editSetRecordOci = "COAL.2022.V1RJW.P"
            val values =
              valuesFromRecord(editSetRecordOci) ++
                Map(
                  "former-reference-pro" -> "   "
                )

            val submissionResponse = submitSavingChanges(idOfExistingEditSet, editSetRecordOci, values)

            assertRedirection(submissionResponse, s"/edit-set/$idOfExistingEditSet/record/$editSetRecordOci/edit/save")

            assertCallMadeToGetEditSet(idOfExistingEditSet)
            assertCallMadeToGetEditSetRecord(idOfExistingEditSet, editSetRecordOci)
            val updateEditSetRecordForRecord = generateUpdateEditSetRecord(idOfExistingEditSet, editSetRecordOci)
            assertCallMadeToUpdateEditSetRecord(
              updateEditSetRecordForRecord.copy(fields =
                updateEditSetRecordForRecord.fields.copy(
                  formerReferencePro = "   "
                )
              )
            )

          }
          "is too long" in {

            val editSetRecordOci = "COAL.2022.V1RJW.P"
            val excessivelyLongValue = "X" * 256
            val values =
              valuesFromRecord(editSetRecordOci) ++
                Map(
                  "former-reference-pro" -> excessivelyLongValue
                )

            val response = submitSavingChanges(idOfExistingEditSet, editSetRecordOci, values)

            assertPageAsExpected(
              response,
              BAD_REQUEST,
              generateExpectedEditRecordPageFromRecord(editSetRecordOci)
                .copy(
                  formerReferencePro = excessivelyLongValue,
                  summaryErrorMessages = Seq(
                    ExpectedSummaryErrorMessage(
                      "Former reference (PRO) too long, maximum length 255 characters",
                      "former-reference-pro"
                    )
                  ),
                  errorMessageForFormerReferencePro =
                    Some("Former reference (PRO) too long, maximum length 255 characters")
                )
            )

            assertCallMadeToGetEditSet(idOfExistingEditSet)
            assertCallMadeToGetEditSetRecord(idOfExistingEditSet, ociOfExistingRecord)
            assertNoCallMadeToUpdateEditSetRecord()

          }
        }
        "all fields are provided and valid" in {

          val editSetRecordOci = "COAL.2022.V1RJW.P"
          val values =
            Map(
              "scope-and-content" -> "The Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (B)",
              "covering-dates"              -> "1960",
              "former-reference-department" -> "Photographs",
              "former-reference-pro"        -> "CAB 172",
              "start-date-day"              -> "2",
              "start-date-month"            -> "4",
              "start-date-year"             -> "1960",
              "end-date-day"                -> "26",
              "end-date-month"              -> "10",
              "end-date-year"               -> "1960",
              "legal-status-id"             -> "http://catalogue.nationalarchives.gov.uk/non-public-record",
              "note"                        -> "A brief note about COAL.2022.V1RJW.P.",
              "background"        -> "The photo was taken by a daughter of one of the coal miners who used them.",
              "custodial-history" -> "These files originally created by successor or predecessor departments for COAL",
              "place-of-deposit-id" -> britishLibrary,
              "creator-ids[0]"      -> s"$baseUriAgent.2YK",
              "creator-ids[1]"      -> s"$baseUriAgent.W91"
            )

          val submissionResponse = submitSavingChanges(idOfExistingEditSet, editSetRecordOci, values)

          assertRedirection(submissionResponse, "/edit-set/1/record/COAL.2022.V1RJW.P/edit/save")

          assertCallMadeToGetEditSet(idOfExistingEditSet)
          assertCallMadeToGetEditSetRecord(idOfExistingEditSet, editSetRecordOci)
          val updateEditSetRecordForRecord = generateUpdateEditSetRecord(idOfExistingEditSet, editSetRecordOci)
          assertCallMadeToUpdateEditSetRecord(
            updateEditSetRecordForRecord.copy(fields =
              updateEditSetRecordForRecord.fields.copy(
                description =
                  "The Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (B)",
                coveringDates = "1960",
                formerReferenceDepartment = "Photographs",
                formerReferencePro = "CAB 172",
                startDate = LocalDate.of(1960, Month.APRIL, 2),
                endDate = LocalDate.of(1960, Month.OCTOBER, 26),
                legalStatusId = "http://catalogue.nationalarchives.gov.uk/non-public-record",
                note = "A brief note about COAL.2022.V1RJW.P.",
                background = "The photo was taken by a daughter of one of the coal miners who used them.",
                custodialHistory = "These files originally created by successor or predecessor departments for COAL",
                placeOfDepositID = britishLibrary,
                creatorIDs = Seq(s"$baseUriAgent.2YK", s"$baseUriAgent.W91")
              )
            )
          )

          assertPageAsExpected(
            getEditSetRecordSavePage(idOfExistingEditSet, editSetRecordOci),
            OK,
            ExpectedEditRecordSavePage(
              title = "Edit record",
              bannerLineOne = "Changes to the following record have been saved:",
              bannerLineTwo = "TNA reference: COAL 80/80/1",
              bannerLineThree = s"PAC-ID: $editSetRecordOci Physical Record",
              bannerLineFour = "Series: National Coal Board and predecessors: Photographs",
              backLinkLabel = "Back to edit set (COAL 80 Sample)",
              backLinkHref = "/edit-set/1"
            )
          )

        }
        "fields are provided and valid without record type suffix" in {

          val editSetRecordOci = "COAL.2022.V2RJW"
          val values = valuesFromRecord(editSetRecordOci) ++ Map("place-of-deposit-id" -> britishLibrary)

          val submissionResponse = submitSavingChanges(idOfExistingEditSet, editSetRecordOci, values)

          assertRedirection(submissionResponse, s"/edit-set/1/record/$editSetRecordOci/edit/save")

          assertCallMadeToGetEditSet(idOfExistingEditSet)
          assertCallMadeToGetEditSetRecord(idOfExistingEditSet, editSetRecordOci)
          val updateEditSetRecordForRecord = generateUpdateEditSetRecord(idOfExistingEditSet, editSetRecordOci)
          assertCallMadeToUpdateEditSetRecord(
            updateEditSetRecordForRecord.copy(fields =
              updateEditSetRecordForRecord.fields.copy(
                placeOfDepositID = britishLibrary
              )
            )
          )

          assertPageAsExpected(
            getEditSetRecordSavePage(idOfExistingEditSet, editSetRecordOci),
            OK,
            ExpectedEditRecordSavePage(
              title = "Edit record",
              bannerLineOne = "Changes to the following record have been saved:",
              bannerLineTwo = "TNA reference: COAL 80/80/2",
              bannerLineThree = s"PAC-ID: $editSetRecordOci",
              bannerLineFour = "Series: National Coal Board and predecessors: Photographs",
              backLinkLabel = "Back to edit set (COAL 80 Sample)",
              backLinkHref = "/edit-set/1"
            )
          )

        }
      }
      "discarding changes" when {
        "and there is an invalid field" in {

          val editSetRecordOci = "COAL.2022.V1RJW.P"
          val values =
            valuesFromRecord(editSetRecordOci) ++ Map(
              "scope-and-content" -> ""
            )

          val submissionResponse = submitDiscardingChanges(idOfExistingEditSet, editSetRecordOci, values)

          assertRedirection(submissionResponse, "/edit-set/1/record/COAL.2022.V1RJW.P/edit/discard")

          assertPageAsExpected(
            getEditSetRecordDiscardPage(idOfExistingEditSet, editSetRecordOci),
            OK,
            ExpectedEditRecordDiscardPage(
              title = "Edit record",
              bannerLineOne = "Changes to the following record have been discarded:",
              bannerLineTwo = "TNA reference: COAL 80/80/1",
              bannerLineThree = s"PAC-ID: $editSetRecordOci Physical Record",
              bannerLineFour = "Series: National Coal Board and predecessors: Photographs",
              backLinkLabel = "Back to edit set (COAL 80 Sample)",
              backLinkHref = "/edit-set/1"
            )
          )

          assertCallMadeToGetEditSet(idOfExistingEditSet)
          assertCallMadeToGetEditSetRecord(idOfExistingEditSet, ociOfExistingRecord)
          assertNoCallMadeToUpdateEditSetRecord()

        }
        "all fields are unchanged" in {

          val editSetRecordOci = "COAL.2022.V1RJW.P"
          val values = valuesFromRecord(editSetRecordOci)

          val submissionResponse = submitDiscardingChanges(idOfExistingEditSet, editSetRecordOci, values)

          assertRedirection(submissionResponse, "/edit-set/1/record/COAL.2022.V1RJW.P/edit/discard")

          assertPageAsExpected(
            getEditSetRecordDiscardPage(idOfExistingEditSet, editSetRecordOci),
            OK,
            ExpectedEditRecordDiscardPage(
              title = "Edit record",
              bannerLineOne = "Changes to the following record have been discarded:",
              bannerLineTwo = "TNA reference: COAL 80/80/1",
              bannerLineThree = s"PAC-ID: $editSetRecordOci Physical Record",
              bannerLineFour = "Series: National Coal Board and predecessors: Photographs",
              backLinkLabel = "Back to edit set (COAL 80 Sample)",
              backLinkHref = "/edit-set/1"
            )
          )

          assertCallMadeToGetEditSet(idOfExistingEditSet)
          assertCallMadeToGetEditSetRecord(idOfExistingEditSet, ociOfExistingRecord)
          assertNoCallMadeToUpdateEditSetRecord()

        }

        "all fields are unchanged without a record type suffix" in {

          val editSetRecordOci = "COAL.2022.V2RJW"
          val values = valuesFromRecord(editSetRecordOci)

          val submissionResponse = submitDiscardingChanges(idOfExistingEditSet, editSetRecordOci, values)

          assertRedirection(submissionResponse, s"/edit-set/1/record/$editSetRecordOci/edit/discard")

          assertPageAsExpected(
            getEditSetRecordDiscardPage(idOfExistingEditSet, editSetRecordOci),
            OK,
            ExpectedEditRecordDiscardPage(
              title = "Edit record",
              bannerLineOne = "Changes to the following record have been discarded:",
              bannerLineTwo = "TNA reference: COAL 80/80/2",
              bannerLineThree = s"PAC-ID: $editSetRecordOci",
              bannerLineFour = "Series: National Coal Board and predecessors: Photographs",
              backLinkLabel = "Back to edit set (COAL 80 Sample)",
              backLinkHref = "/edit-set/1"
            )
          )

          assertCallMadeToGetEditSet(idOfExistingEditSet)
          assertCallMadeToGetEditSetRecord(idOfExistingEditSet, editSetRecordOci)
          assertNoCallMadeToUpdateEditSetRecord()

        }
      }
      "adding another creator" when {
        "a single creator had been previously assigned and we" when {
          "keep that same selection" in {

            val editSetRecordOci = "COAL.2022.V11RJW.P"
            assertPageAsExpected(
              getEditSetRecordEditPageWhileLoggedIn(idOfExistingEditSet, editSetRecordOci),
              OK,
              generateExpectedEditRecordPageFromRecord(editSetRecordOci)
                .copy(optionsForCreators =
                  Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(s"$baseUriAgent.3LG", "Edwin Hill (1793 - 1876)"),
                      ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)"),
                      ExpectedSelectOption(
                        s"$baseUriAgent.N6L",
                        "National Coal Board, Northumberland and Durham Division (1964 - 1967)"
                      ),
                      ExpectedSelectOption(
                        s"$baseUriAgent.W91",
                        "Queen's Printer for Scotland (1999 - )",
                        selected = true
                      )
                    )
                  )
                )
            )

            val values =
              valuesFromRecord(editSetRecordOci) ++
                Map(
                  "creator-ids[0]" -> s"$baseUriAgent.W91"
                )

            val response = submitAddingAnotherCreatorSlot(idOfExistingEditSet, editSetRecordOci, values)
            assertPageAsExpected(
              response,
              OK,
              generateExpectedEditRecordPageFromRecord(editSetRecordOci)
                .copy(optionsForCreators =
                  Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(s"$baseUriAgent.3LG", "Edwin Hill (1793 - 1876)"),
                      ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)"),
                      ExpectedSelectOption(
                        s"$baseUriAgent.N6L",
                        "National Coal Board, Northumberland and Durham Division (1964 - 1967)"
                      ),
                      ExpectedSelectOption(
                        s"$baseUriAgent.W91",
                        "Queen's Printer for Scotland (1999 - )",
                        selected = true
                      )
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true, selected = true),
                      ExpectedSelectOption(s"$baseUriAgent.3LG", "Edwin Hill (1793 - 1876)"),
                      ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)"),
                      ExpectedSelectOption(
                        s"$baseUriAgent.N6L",
                        "National Coal Board, Northumberland and Durham Division (1964 - 1967)"
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.W91", "Queen's Printer for Scotland (1999 - )")
                    )
                  )
                )
            )

            assertCallMadeToGetEditSet(idOfExistingEditSet)
            assertCallMadeToGetEditSetRecord(idOfExistingEditSet, editSetRecordOci)
            assertNoCallMadeToUpdateEditSetRecord()

          }
          "keep that same selection, but already have an empty slot" in {

            val editSetRecordOci = "COAL.2022.V11RJW.P"
            assertPageAsExpected(
              getEditSetRecordEditPageWhileLoggedIn(idOfExistingEditSet, editSetRecordOci),
              OK,
              generateExpectedEditRecordPageFromRecord(editSetRecordOci)
                .copy(optionsForCreators =
                  Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(s"$baseUriAgent.3LG", "Edwin Hill (1793 - 1876)"),
                      ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)"),
                      ExpectedSelectOption(
                        s"$baseUriAgent.N6L",
                        "National Coal Board, Northumberland and Durham Division (1964 - 1967)"
                      ),
                      ExpectedSelectOption(
                        s"$baseUriAgent.W91",
                        "Queen's Printer for Scotland (1999 - )",
                        selected = true
                      )
                    )
                  )
                )
            )

            val values =
              valuesFromRecord(editSetRecordOci) ++ Map(
                "creator-ids[0]" -> s"$baseUriAgent.W91",
                "creator-ids[1]" -> ""
              )

            val submissionResponse = submitAddingAnotherCreatorSlot(idOfExistingEditSet, editSetRecordOci, values)

            assertPageAsExpected(
              submissionResponse,
              OK,
              generateExpectedEditRecordPageFromRecord(editSetRecordOci)
                .copy(optionsForCreators =
                  Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(s"$baseUriAgent.3LG", "Edwin Hill (1793 - 1876)"),
                      ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)"),
                      ExpectedSelectOption(
                        s"$baseUriAgent.N6L",
                        "National Coal Board, Northumberland and Durham Division (1964 - 1967)"
                      ),
                      ExpectedSelectOption(
                        s"$baseUriAgent.W91",
                        "Queen's Printer for Scotland (1999 - )",
                        selected = true
                      )
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true, selected = true),
                      ExpectedSelectOption(s"$baseUriAgent.3LG", "Edwin Hill (1793 - 1876)"),
                      ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)"),
                      ExpectedSelectOption(
                        s"$baseUriAgent.N6L",
                        "National Coal Board, Northumberland and Durham Division (1964 - 1967)"
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.W91", "Queen's Printer for Scotland (1999 - )")
                    )
                  )
                )
            )

            assertCallMadeToGetEditSet(idOfExistingEditSet)
            assertCallMadeToGetEditSetRecord(idOfExistingEditSet, editSetRecordOci)
            assertNoCallMadeToUpdateEditSetRecord()

          }
          "clear that selection" in {

            val editSetRecordOci = "COAL.2022.V11RJW.P"
            assertPageAsExpected(
              getEditSetRecordEditPageWhileLoggedIn(idOfExistingEditSet, editSetRecordOci),
              OK,
              generateExpectedEditRecordPageFromRecord(editSetRecordOci)
                .copy(optionsForCreators =
                  Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(s"$baseUriAgent.3LG", "Edwin Hill (1793 - 1876)"),
                      ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)"),
                      ExpectedSelectOption(
                        s"$baseUriAgent.N6L",
                        "National Coal Board, Northumberland and Durham Division (1964 - 1967)"
                      ),
                      ExpectedSelectOption(
                        s"$baseUriAgent.W91",
                        "Queen's Printer for Scotland (1999 - )",
                        selected = true
                      )
                    )
                  )
                )
            )

            val values =
              valuesFromRecord(editSetRecordOci) ++ Map(
                "creator-ids[0]" -> ""
              )

            val submissionResponse = submitAddingAnotherCreatorSlot(idOfExistingEditSet, editSetRecordOci, values)

            assertPageAsExpected(
              submissionResponse,
              OK,
              generateExpectedEditRecordPageFromRecord(editSetRecordOci)
                .copy(optionsForCreators =
                  Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true, selected = true),
                      ExpectedSelectOption(s"$baseUriAgent.3LG", "Edwin Hill (1793 - 1876)"),
                      ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)"),
                      ExpectedSelectOption(
                        s"$baseUriAgent.N6L",
                        "National Coal Board, Northumberland and Durham Division (1964 - 1967)"
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.W91", "Queen's Printer for Scotland (1999 - )")
                    )
                  )
                )
            )

            assertCallMadeToGetEditSet(idOfExistingEditSet)
            assertCallMadeToGetEditSetRecord(idOfExistingEditSet, editSetRecordOci)
            assertNoCallMadeToUpdateEditSetRecord()

          }
          "change that selection" in {

            val editSetRecordOci = "COAL.2022.V11RJW.P"
            assertPageAsExpected(
              getEditSetRecordEditPageWhileLoggedIn(idOfExistingEditSet, editSetRecordOci),
              OK,
              generateExpectedEditRecordPageFromRecord(editSetRecordOci)
                .copy(optionsForCreators =
                  Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(s"$baseUriAgent.3LG", "Edwin Hill (1793 - 1876)"),
                      ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)"),
                      ExpectedSelectOption(
                        s"$baseUriAgent.N6L",
                        "National Coal Board, Northumberland and Durham Division (1964 - 1967)"
                      ),
                      ExpectedSelectOption(
                        s"$baseUriAgent.W91",
                        "Queen's Printer for Scotland (1999 - )",
                        selected = true
                      )
                    )
                  )
                )
            )

            val values =
              valuesFromRecord(editSetRecordOci) ++ Map(
                "creator-ids[0]" -> s"$baseUriAgent.N6L"
              )

            val submissionResponse = submitAddingAnotherCreatorSlot(idOfExistingEditSet, editSetRecordOci, values)
            assertPageAsExpected(
              submissionResponse,
              OK,
              generateExpectedEditRecordPageFromRecord(editSetRecordOci)
                .copy(optionsForCreators =
                  Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(s"$baseUriAgent.3LG", "Edwin Hill (1793 - 1876)"),
                      ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)"),
                      ExpectedSelectOption(
                        s"$baseUriAgent.N6L",
                        "National Coal Board, Northumberland and Durham Division (1964 - 1967)",
                        selected = true
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.W91", "Queen's Printer for Scotland (1999 - )")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true, selected = true),
                      ExpectedSelectOption(s"$baseUriAgent.3LG", "Edwin Hill (1793 - 1876)"),
                      ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)"),
                      ExpectedSelectOption(
                        s"$baseUriAgent.N6L",
                        "National Coal Board, Northumberland and Durham Division (1964 - 1967)"
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.W91", "Queen's Printer for Scotland (1999 - )")
                    )
                  )
                )
            )

            assertCallMadeToGetEditSet(idOfExistingEditSet)
            assertCallMadeToGetEditSetRecord(idOfExistingEditSet, editSetRecordOci)
            assertNoCallMadeToUpdateEditSetRecord()

          }
        }
        "multiple creators had been previously assigned" when {
          "and we keep those selections" in {

            val editSetRecordOci = "COAL.2022.V7RJW.P"
            assertPageAsExpected(
              getEditSetRecordEditPageWhileLoggedIn(idOfExistingEditSet, editSetRecordOci),
              OK,
              generateExpectedEditRecordPageFromRecord(editSetRecordOci)
                .copy(optionsForCreators =
                  Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(
                        s"$baseUriAgent.3LG",
                        "Edwin Hill (1793 - 1876)",
                        selected = true
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)"),
                      ExpectedSelectOption(
                        s"$baseUriAgent.N6L",
                        "National Coal Board, Northumberland and Durham Division (1964 - 1967)"
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.W91", "Queen's Printer for Scotland (1999 - )")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(s"$baseUriAgent.3LG", "Edwin Hill (1793 - 1876)"),
                      ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)"),
                      ExpectedSelectOption(
                        s"$baseUriAgent.N6L",
                        "National Coal Board, Northumberland and Durham Division (1964 - 1967)",
                        selected = true
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.W91", "Queen's Printer for Scotland (1999 - )")
                    )
                  )
                )
            )

            val values =
              valuesFromRecord(editSetRecordOci) ++
                Map(
                  "creator-ids[0]" -> s"$baseUriAgent.3LG",
                  "creator-ids[1]" -> s"$baseUriAgent.N6L"
                )

            val submissionResponse = submitAddingAnotherCreatorSlot(idOfExistingEditSet, editSetRecordOci, values)

            assertPageAsExpected(
              submissionResponse,
              OK,
              generateExpectedEditRecordPageFromRecord(editSetRecordOci)
                .copy(optionsForCreators =
                  Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(
                        s"$baseUriAgent.3LG",
                        "Edwin Hill (1793 - 1876)",
                        selected = true
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)"),
                      ExpectedSelectOption(
                        s"$baseUriAgent.N6L",
                        "National Coal Board, Northumberland and Durham Division (1964 - 1967)"
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.W91", "Queen's Printer for Scotland (1999 - )")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(s"$baseUriAgent.3LG", "Edwin Hill (1793 - 1876)"),
                      ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)"),
                      ExpectedSelectOption(
                        s"$baseUriAgent.N6L",
                        "National Coal Board, Northumberland and Durham Division (1964 - 1967)",
                        selected = true
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.W91", "Queen's Printer for Scotland (1999 - )")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true, selected = true),
                      ExpectedSelectOption(s"$baseUriAgent.3LG", "Edwin Hill (1793 - 1876)"),
                      ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)"),
                      ExpectedSelectOption(
                        s"$baseUriAgent.N6L",
                        "National Coal Board, Northumberland and Durham Division (1964 - 1967)"
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.W91", "Queen's Printer for Scotland (1999 - )")
                    )
                  )
                )
            )

            assertCallMadeToGetEditSet(idOfExistingEditSet)
            assertCallMadeToGetEditSetRecord(idOfExistingEditSet, editSetRecordOci)
            assertNoCallMadeToUpdateEditSetRecord()

          }
          "(including duplicates) and we keep those selections" in {

            val editSetRecordOci = "COAL.2022.V5RJW.P"
            assertPageAsExpected(
              getEditSetRecordEditPageWhileLoggedIn(idOfExistingEditSet, editSetRecordOci),
              OK,
              generateExpectedEditRecordPageFromRecord(editSetRecordOci)
                .copy(optionsForCreators =
                  Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(s"$baseUriAgent.3LG", "Edwin Hill (1793 - 1876)"),
                      ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)", selected = true),
                      ExpectedSelectOption(
                        s"$baseUriAgent.N6L",
                        "National Coal Board, Northumberland and Durham Division (1964 - 1967)"
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.W91", "Queen's Printer for Scotland (1999 - )")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(
                        s"$baseUriAgent.3LG",
                        "Edwin Hill (1793 - 1876)",
                        selected = true
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)"),
                      ExpectedSelectOption(
                        s"$baseUriAgent.N6L",
                        "National Coal Board, Northumberland and Durham Division (1964 - 1967)"
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.W91", "Queen's Printer for Scotland (1999 - )")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(s"$baseUriAgent.3LG", "Edwin Hill (1793 - 1876)"),
                      ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)", selected = true),
                      ExpectedSelectOption(
                        s"$baseUriAgent.N6L",
                        "National Coal Board, Northumberland and Durham Division (1964 - 1967)"
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.W91", "Queen's Printer for Scotland (1999 - )")
                    )
                  )
                )
            )

            val values =
              valuesFromRecord(editSetRecordOci) ++
                Map(
                  "creator-ids[0]" -> s"$baseUriAgent.2YK",
                  "creator-ids[1]" -> s"$baseUriAgent.3LG",
                  "creator-ids[2]" -> s"$baseUriAgent.2YK"
                )

            val response = submitAddingAnotherCreatorSlot(idOfExistingEditSet, editSetRecordOci, values)

            assertPageAsExpected(
              response,
              OK,
              generateExpectedEditRecordPageFromRecord(editSetRecordOci)
                .copy(
                  optionsForCreators = Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(s"$baseUriAgent.3LG", "Edwin Hill (1793 - 1876)"),
                      ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)", selected = true),
                      ExpectedSelectOption(
                        s"$baseUriAgent.N6L",
                        "National Coal Board, Northumberland and Durham Division (1964 - 1967)"
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.W91", "Queen's Printer for Scotland (1999 - )")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(
                        s"$baseUriAgent.3LG",
                        "Edwin Hill (1793 - 1876)",
                        selected = true
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)"),
                      ExpectedSelectOption(
                        s"$baseUriAgent.N6L",
                        "National Coal Board, Northumberland and Durham Division (1964 - 1967)"
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.W91", "Queen's Printer for Scotland (1999 - )")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(s"$baseUriAgent.3LG", "Edwin Hill (1793 - 1876)"),
                      ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)", selected = true),
                      ExpectedSelectOption(
                        s"$baseUriAgent.N6L",
                        "National Coal Board, Northumberland and Durham Division (1964 - 1967)"
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.W91", "Queen's Printer for Scotland (1999 - )")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true, selected = true),
                      ExpectedSelectOption(s"$baseUriAgent.3LG", "Edwin Hill (1793 - 1876)"),
                      ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)"),
                      ExpectedSelectOption(
                        s"$baseUriAgent.N6L",
                        "National Coal Board, Northumberland and Durham Division (1964 - 1967)"
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.W91", "Queen's Printer for Scotland (1999 - )")
                    )
                  )
                )
            )

            assertCallMadeToGetEditSet(idOfExistingEditSet)
            assertCallMadeToGetEditSetRecord(idOfExistingEditSet, editSetRecordOci)
            assertNoCallMadeToUpdateEditSetRecord()

          }
          "but we change those selections" in {

            val editSetRecordOci = "COAL.2022.V5RJW.P"
            assertPageAsExpected(
              getEditSetRecordEditPageWhileLoggedIn(idOfExistingEditSet, editSetRecordOci),
              OK,
              generateExpectedEditRecordPageFromRecord(editSetRecordOci)
                .copy(optionsForCreators =
                  Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(s"$baseUriAgent.3LG", "Edwin Hill (1793 - 1876)"),
                      ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)", selected = true),
                      ExpectedSelectOption(
                        s"$baseUriAgent.N6L",
                        "National Coal Board, Northumberland and Durham Division (1964 - 1967)"
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.W91", "Queen's Printer for Scotland (1999 - )")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(
                        s"$baseUriAgent.3LG",
                        "Edwin Hill (1793 - 1876)",
                        selected = true
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)"),
                      ExpectedSelectOption(
                        s"$baseUriAgent.N6L",
                        "National Coal Board, Northumberland and Durham Division (1964 - 1967)"
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.W91", "Queen's Printer for Scotland (1999 - )")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(s"$baseUriAgent.3LG", "Edwin Hill (1793 - 1876)"),
                      ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)", selected = true),
                      ExpectedSelectOption(
                        s"$baseUriAgent.N6L",
                        "National Coal Board, Northumberland and Durham Division (1964 - 1967)"
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.W91", "Queen's Printer for Scotland (1999 - )")
                    )
                  )
                )
            )

            val values =
              valuesFromRecord(editSetRecordOci) ++ Map(
                "creator-ids[0]" -> s"$baseUriAgent.W91",
                "creator-ids[1]" -> s"$baseUriAgent.N6L",
                "creator-ids[2]" -> s"$baseUriAgent.3LG"
              )

            val response = submitAddingAnotherCreatorSlot(idOfExistingEditSet, editSetRecordOci, values)

            assertPageAsExpected(
              response,
              OK,
              generateExpectedEditRecordPageFromRecord(editSetRecordOci)
                .copy(
                  optionsForCreators = Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(s"$baseUriAgent.3LG", "Edwin Hill (1793 - 1876)"),
                      ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)"),
                      ExpectedSelectOption(
                        s"$baseUriAgent.N6L",
                        "National Coal Board, Northumberland and Durham Division (1964 - 1967)"
                      ),
                      ExpectedSelectOption(
                        s"$baseUriAgent.W91",
                        "Queen's Printer for Scotland (1999 - )",
                        selected = true
                      )
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(s"$baseUriAgent.3LG", "Edwin Hill (1793 - 1876)"),
                      ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)"),
                      ExpectedSelectOption(
                        s"$baseUriAgent.N6L",
                        "National Coal Board, Northumberland and Durham Division (1964 - 1967)",
                        selected = true
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.W91", "Queen's Printer for Scotland (1999 - )")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(
                        s"$baseUriAgent.3LG",
                        "Edwin Hill (1793 - 1876)",
                        selected = true
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)"),
                      ExpectedSelectOption(
                        s"$baseUriAgent.N6L",
                        "National Coal Board, Northumberland and Durham Division (1964 - 1967)"
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.W91", "Queen's Printer for Scotland (1999 - )")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true, selected = true),
                      ExpectedSelectOption(s"$baseUriAgent.3LG", "Edwin Hill (1793 - 1876)"),
                      ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)"),
                      ExpectedSelectOption(
                        s"$baseUriAgent.N6L",
                        "National Coal Board, Northumberland and Durham Division (1964 - 1967)"
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.W91", "Queen's Printer for Scotland (1999 - )")
                    )
                  )
                )
            )

            assertCallMadeToGetEditSet(idOfExistingEditSet)
            assertCallMadeToGetEditSetRecord(idOfExistingEditSet, editSetRecordOci)
            assertNoCallMadeToUpdateEditSetRecord()

          }
        }
      }
      "removing the last creator" when {
        "we have two selections and" when {
          "we leave the first selection unchanged" in {

            val editSetRecordOci = "COAL.2022.V1RJW.P"
            assertPageAsExpected(
              getEditSetRecordEditPageWhileLoggedIn(idOfExistingEditSet, editSetRecordOci),
              OK,
              generateExpectedEditRecordPageFromRecord(editSetRecordOci)
                .copy(optionsForCreators =
                  Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(
                        s"$baseUriAgent.3LG",
                        "Edwin Hill (1793 - 1876)",
                        selected = true
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)"),
                      ExpectedSelectOption(
                        s"$baseUriAgent.N6L",
                        "National Coal Board, Northumberland and Durham Division (1964 - 1967)"
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.W91", "Queen's Printer for Scotland (1999 - )")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(s"$baseUriAgent.3LG", "Edwin Hill (1793 - 1876)"),
                      ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)"),
                      ExpectedSelectOption(
                        s"$baseUriAgent.N6L",
                        "National Coal Board, Northumberland and Durham Division (1964 - 1967)",
                        selected = true
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.W91", "Queen's Printer for Scotland (1999 - )")
                    )
                  )
                )
            )

            val values =
              valuesFromRecord(editSetRecordOci) ++
                Map(
                  "creator-ids[0]" -> s"$baseUriAgent.3LG",
                  "creator-ids[1]" -> s"$baseUriAgent.N6L"
                )

            val response = submitRemovingTheLastCreator(idOfExistingEditSet, editSetRecordOci, values)

            assertPageAsExpected(
              response,
              OK,
              generateExpectedEditRecordPageFromRecord(editSetRecordOci)
                .copy(optionsForCreators =
                  Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(
                        s"$baseUriAgent.3LG",
                        "Edwin Hill (1793 - 1876)",
                        selected = true
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)"),
                      ExpectedSelectOption(
                        s"$baseUriAgent.N6L",
                        "National Coal Board, Northumberland and Durham Division (1964 - 1967)"
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.W91", "Queen's Printer for Scotland (1999 - )")
                    )
                  )
                )
            )

            assertCallMadeToGetEditSet(idOfExistingEditSet)
            assertCallMadeToGetEditSetRecord(idOfExistingEditSet, ociOfExistingRecord)
            assertNoCallMadeToUpdateEditSetRecord()

          }
          "we change the first selection" in {

            val editSetRecordOci = "COAL.2022.V1RJW.P"
            assertPageAsExpected(
              getEditSetRecordEditPageWhileLoggedIn(idOfExistingEditSet, editSetRecordOci),
              OK,
              generateExpectedEditRecordPageFromRecord(editSetRecordOci)
                .copy(optionsForCreators =
                  Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(
                        s"$baseUriAgent.3LG",
                        "Edwin Hill (1793 - 1876)",
                        selected = true
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)"),
                      ExpectedSelectOption(
                        s"$baseUriAgent.N6L",
                        "National Coal Board, Northumberland and Durham Division (1964 - 1967)"
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.W91", "Queen's Printer for Scotland (1999 - )")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(s"$baseUriAgent.3LG", "Edwin Hill (1793 - 1876)"),
                      ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)"),
                      ExpectedSelectOption(
                        s"$baseUriAgent.N6L",
                        "National Coal Board, Northumberland and Durham Division (1964 - 1967)",
                        selected = true
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.W91", "Queen's Printer for Scotland (1999 - )")
                    )
                  )
                )
            )

            val values =
              valuesFromRecord(editSetRecordOci) ++
                Map(
                  "creator-ids[0]" -> s"$baseUriAgent.2YK",
                  "creator-ids[1]" -> s"$baseUriAgent.N6L"
                )

            val response = submitRemovingTheLastCreator(idOfExistingEditSet, editSetRecordOci, values)

            assertPageAsExpected(
              response,
              OK,
              generateExpectedEditRecordPageFromRecord(editSetRecordOci)
                .copy(optionsForCreators =
                  Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(s"$baseUriAgent.3LG", "Edwin Hill (1793 - 1876)"),
                      ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)", selected = true),
                      ExpectedSelectOption(
                        s"$baseUriAgent.N6L",
                        "National Coal Board, Northumberland and Durham Division (1964 - 1967)"
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.W91", "Queen's Printer for Scotland (1999 - )")
                    )
                  )
                )
            )

            assertCallMadeToGetEditSet(idOfExistingEditSet)
            assertCallMadeToGetEditSetRecord(idOfExistingEditSet, ociOfExistingRecord)
            assertNoCallMadeToUpdateEditSetRecord()

          }
          "we blank out the first" in {

            val editSetRecordOci = "COAL.2022.V1RJW.P"
            assertPageAsExpected(
              getEditSetRecordEditPageWhileLoggedIn(idOfExistingEditSet, editSetRecordOci),
              OK,
              generateExpectedEditRecordPageFromRecord(editSetRecordOci)
                .copy(optionsForCreators =
                  Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(
                        s"$baseUriAgent.3LG",
                        "Edwin Hill (1793 - 1876)",
                        selected = true
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)"),
                      ExpectedSelectOption(
                        s"$baseUriAgent.N6L",
                        "National Coal Board, Northumberland and Durham Division (1964 - 1967)"
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.W91", "Queen's Printer for Scotland (1999 - )")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(s"$baseUriAgent.3LG", "Edwin Hill (1793 - 1876)"),
                      ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)"),
                      ExpectedSelectOption(
                        s"$baseUriAgent.N6L",
                        "National Coal Board, Northumberland and Durham Division (1964 - 1967)",
                        selected = true
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.W91", "Queen's Printer for Scotland (1999 - )")
                    )
                  )
                )
            )

            val values =
              valuesFromRecord(editSetRecordOci) ++
                Map(
                  "creator-ids[0]" -> "",
                  "creator-ids[1]" -> "N6L"
                )

            val response = submitRemovingTheLastCreator(idOfExistingEditSet, editSetRecordOci, values)

            assertPageAsExpected(
              response,
              OK,
              generateExpectedEditRecordPageFromRecord(editSetRecordOci)
                .copy(optionsForCreators =
                  Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true, selected = true),
                      ExpectedSelectOption(s"$baseUriAgent.3LG", "Edwin Hill (1793 - 1876)"),
                      ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)"),
                      ExpectedSelectOption(
                        s"$baseUriAgent.N6L",
                        "National Coal Board, Northumberland and Durham Division (1964 - 1967)"
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.W91", "Queen's Printer for Scotland (1999 - )")
                    )
                  )
                )
            )

            assertCallMadeToGetEditSet(idOfExistingEditSet)
            assertCallMadeToGetEditSetRecord(idOfExistingEditSet, ociOfExistingRecord)
            assertNoCallMadeToUpdateEditSetRecord()

          }
        }
        "we have three selections and" when {
          "we leave the first two selections unchanged" in {

            val editSetRecordOci = "COAL.2022.V8RJW.P"
            assertPageAsExpected(
              getEditSetRecordEditPageWhileLoggedIn(idOfExistingEditSet, editSetRecordOci),
              OK,
              generateExpectedEditRecordPageFromRecord(editSetRecordOci)
                .copy(optionsForCreators =
                  Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(
                        s"$baseUriAgent.3LG",
                        "Edwin Hill (1793 - 1876)",
                        selected = true
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)"),
                      ExpectedSelectOption(
                        s"$baseUriAgent.N6L",
                        "National Coal Board, Northumberland and Durham Division (1964 - 1967)"
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.W91", "Queen's Printer for Scotland (1999 - )")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(s"$baseUriAgent.3LG", "Edwin Hill (1793 - 1876)"),
                      ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)", selected = true),
                      ExpectedSelectOption(
                        s"$baseUriAgent.N6L",
                        "National Coal Board, Northumberland and Durham Division (1964 - 1967)"
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.W91", "Queen's Printer for Scotland (1999 - )")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(s"$baseUriAgent.3LG", "Edwin Hill (1793 - 1876)"),
                      ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)"),
                      ExpectedSelectOption(
                        s"$baseUriAgent.N6L",
                        "National Coal Board, Northumberland and Durham Division (1964 - 1967)"
                      ),
                      ExpectedSelectOption(
                        s"$baseUriAgent.W91",
                        "Queen's Printer for Scotland (1999 - )",
                        selected = true
                      )
                    )
                  )
                )
            )

            val values =
              valuesFromRecord(editSetRecordOci) ++
                Map(
                  "creator-ids[0]" -> s"$baseUriAgent.3LG",
                  "creator-ids[1]" -> s"$baseUriAgent.2YK",
                  "creator-ids[2]" -> s"$baseUriAgent.W91"
                )

            val response = submitRemovingTheLastCreator(idOfExistingEditSet, editSetRecordOci, values)

            assertPageAsExpected(
              response,
              OK,
              generateExpectedEditRecordPageFromRecord(editSetRecordOci)
                .copy(optionsForCreators =
                  Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(
                        s"$baseUriAgent.3LG",
                        "Edwin Hill (1793 - 1876)",
                        selected = true
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)"),
                      ExpectedSelectOption(
                        s"$baseUriAgent.N6L",
                        "National Coal Board, Northumberland and Durham Division (1964 - 1967)"
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.W91", "Queen's Printer for Scotland (1999 - )")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(s"$baseUriAgent.3LG", "Edwin Hill (1793 - 1876)"),
                      ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)", selected = true),
                      ExpectedSelectOption(
                        s"$baseUriAgent.N6L",
                        "National Coal Board, Northumberland and Durham Division (1964 - 1967)"
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.W91", "Queen's Printer for Scotland (1999 - )")
                    )
                  )
                )
            )

            assertCallMadeToGetEditSet(idOfExistingEditSet)
            assertCallMadeToGetEditSetRecord(idOfExistingEditSet, editSetRecordOci)
            assertNoCallMadeToUpdateEditSetRecord()

          }
          "we change the first two selections" in {

            val editSetRecordOci = "COAL.2022.V8RJW.P"
            assertPageAsExpected(
              getEditSetRecordEditPageWhileLoggedIn(idOfExistingEditSet, editSetRecordOci),
              OK,
              generateExpectedEditRecordPageFromRecord(editSetRecordOci)
                .copy(optionsForCreators =
                  Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(
                        s"$baseUriAgent.3LG",
                        "Edwin Hill (1793 - 1876)",
                        selected = true
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)"),
                      ExpectedSelectOption(
                        s"$baseUriAgent.N6L",
                        "National Coal Board, Northumberland and Durham Division (1964 - 1967)"
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.W91", "Queen's Printer for Scotland (1999 - )")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(s"$baseUriAgent.3LG", "Edwin Hill (1793 - 1876)"),
                      ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)", selected = true),
                      ExpectedSelectOption(
                        s"$baseUriAgent.N6L",
                        "National Coal Board, Northumberland and Durham Division (1964 - 1967)"
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.W91", "Queen's Printer for Scotland (1999 - )")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(s"$baseUriAgent.3LG", "Edwin Hill (1793 - 1876)"),
                      ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)"),
                      ExpectedSelectOption(
                        s"$baseUriAgent.N6L",
                        "National Coal Board, Northumberland and Durham Division (1964 - 1967)"
                      ),
                      ExpectedSelectOption(
                        s"$baseUriAgent.W91",
                        "Queen's Printer for Scotland (1999 - )",
                        selected = true
                      )
                    )
                  )
                )
            )

            val values =
              valuesFromRecord(editSetRecordOci) ++
                Map(
                  "creator-ids[0]" -> s"$baseUriAgent.N6L",
                  "creator-ids[1]" -> s"$baseUriAgent.3LG",
                  "creator-ids[2]" -> s"$baseUriAgent.W91"
                )

            val response = submitRemovingTheLastCreator(idOfExistingEditSet, editSetRecordOci, values)

            assertPageAsExpected(
              response,
              OK,
              generateExpectedEditRecordPageFromRecord(editSetRecordOci)
                .copy(optionsForCreators =
                  Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(s"$baseUriAgent.3LG", "Edwin Hill (1793 - 1876)"),
                      ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)"),
                      ExpectedSelectOption(
                        s"$baseUriAgent.N6L",
                        "National Coal Board, Northumberland and Durham Division (1964 - 1967)",
                        selected = true
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.W91", "Queen's Printer for Scotland (1999 - )")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(
                        s"$baseUriAgent.3LG",
                        "Edwin Hill (1793 - 1876)",
                        selected = true
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)"),
                      ExpectedSelectOption(
                        s"$baseUriAgent.N6L",
                        "National Coal Board, Northumberland and Durham Division (1964 - 1967)"
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.W91", "Queen's Printer for Scotland (1999 - )")
                    )
                  )
                )
            )

            assertCallMadeToGetEditSet(idOfExistingEditSet)
            assertCallMadeToGetEditSetRecord(idOfExistingEditSet, editSetRecordOci)
            assertNoCallMadeToUpdateEditSetRecord()

          }
          "we remove two in a row" in {

            val editSetRecordOci = "COAL.2022.V8RJW.P"
            assertPageAsExpected(
              getEditSetRecordEditPageWhileLoggedIn(idOfExistingEditSet, editSetRecordOci),
              OK,
              generateExpectedEditRecordPageFromRecord(editSetRecordOci)
                .copy(optionsForCreators =
                  Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(
                        s"$baseUriAgent.3LG",
                        "Edwin Hill (1793 - 1876)",
                        selected = true
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)"),
                      ExpectedSelectOption(
                        s"$baseUriAgent.N6L",
                        "National Coal Board, Northumberland and Durham Division (1964 - 1967)"
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.W91", "Queen's Printer for Scotland (1999 - )")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(s"$baseUriAgent.3LG", "Edwin Hill (1793 - 1876)"),
                      ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)", selected = true),
                      ExpectedSelectOption(
                        s"$baseUriAgent.N6L",
                        "National Coal Board, Northumberland and Durham Division (1964 - 1967)"
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.W91", "Queen's Printer for Scotland (1999 - )")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(s"$baseUriAgent.3LG", "Edwin Hill (1793 - 1876)"),
                      ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)"),
                      ExpectedSelectOption(
                        s"$baseUriAgent.N6L",
                        "National Coal Board, Northumberland and Durham Division (1964 - 1967)"
                      ),
                      ExpectedSelectOption(
                        s"$baseUriAgent.W91",
                        "Queen's Printer for Scotland (1999 - )",
                        selected = true
                      )
                    )
                  )
                )
            )

            val valuesForFirstSubmission = valuesFromRecord(editSetRecordOci) ++
              Map(
                "creator-ids[0]" -> s"$baseUriAgent.N6L",
                "creator-ids[1]" -> s"$baseUriAgent.3LG",
                "creator-ids[2]" -> s"$baseUriAgent.W91"
              )

            val firstSubmissionResponse =
              submitRemovingTheLastCreator(idOfExistingEditSet, editSetRecordOci, valuesForFirstSubmission)
            assertPageAsExpected(
              firstSubmissionResponse,
              OK,
              generateExpectedEditRecordPageFromRecord(editSetRecordOci)
                .copy(optionsForCreators =
                  Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(s"$baseUriAgent.3LG", "Edwin Hill (1793 - 1876)"),
                      ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)"),
                      ExpectedSelectOption(
                        s"$baseUriAgent.N6L",
                        "National Coal Board, Northumberland and Durham Division (1964 - 1967)",
                        selected = true
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.W91", "Queen's Printer for Scotland (1999 - )")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(
                        s"$baseUriAgent.3LG",
                        "Edwin Hill (1793 - 1876)",
                        selected = true
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)"),
                      ExpectedSelectOption(
                        s"$baseUriAgent.N6L",
                        "National Coal Board, Northumberland and Durham Division (1964 - 1967)"
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.W91", "Queen's Printer for Scotland (1999 - )")
                    )
                  )
                )
            )

            val valuesForSecondSubmission = valuesFromRecord(editSetRecordOci) - "creator-ids[2]" ++
              Map(
                "creator-ids[0]" -> s"$baseUriAgent.N6L",
                "creator-ids[1]" -> s"$baseUriAgent.3LG"
              )

            val secondSubmissionResponse =
              submitRemovingTheLastCreator(idOfExistingEditSet, editSetRecordOci, valuesForSecondSubmission)
            assertPageAsExpected(
              secondSubmissionResponse,
              OK,
              generateExpectedEditRecordPageFromRecord(editSetRecordOci)
                .copy(optionsForCreators =
                  Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(s"$baseUriAgent.3LG", "Edwin Hill (1793 - 1876)"),
                      ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)"),
                      ExpectedSelectOption(
                        s"$baseUriAgent.N6L",
                        "National Coal Board, Northumberland and Durham Division (1964 - 1967)",
                        selected = true
                      ),
                      ExpectedSelectOption(s"$baseUriAgent.W91", "Queen's Printer for Scotland (1999 - )")
                    )
                  )
                )
            )

            assertCallMadeToGetEditSet(idOfExistingEditSet)
            assertCallMadeToGetEditSetRecord(idOfExistingEditSet, editSetRecordOci)
            assertNoCallMadeToUpdateEditSetRecord()

          }
        }
      }
      "calculating start and end dates from the covering dates" when {
        "blank" in {

          val editSetRecordOci = "COAL.2022.V1RJW.P"
          val values =
            valuesFromRecord(editSetRecordOci) ++
              Map(
                "covering-dates" -> ""
              )

          val response = submitCalculatingDates(idOfExistingEditSet, editSetRecordOci, values)

          assertPageAsExpected(
            response,
            BAD_REQUEST,
            generateExpectedEditRecordPageFromRecord(editSetRecordOci).copy(
              coveringDates = "",
              summaryErrorMessages = Seq(
                ExpectedSummaryErrorMessage("Enter the covering dates", "covering-dates"),
                ExpectedSummaryErrorMessage("Covering date format is not valid", "covering-dates")
              ),
              errorMessageForCoveringsDates = Some("Enter the covering dates")
            )
          )

          assertCallMadeToGetEditSet(idOfExistingEditSet)
          assertCallMadeToGetEditSetRecord(idOfExistingEditSet, ociOfExistingRecord)
          assertNoCallMadeToUpdateEditSetRecord()

        }
        "all spaces" in {

          val editSetRecordOci = "COAL.2022.V1RJW.P"
          val values =
            valuesFromRecord(editSetRecordOci) ++
              Map(
                "covering-dates" -> "   "
              )

          val response = submitCalculatingDates(idOfExistingEditSet, editSetRecordOci, values)
          assertPageAsExpected(
            response,
            BAD_REQUEST,
            generateExpectedEditRecordPageFromRecord(editSetRecordOci).copy(
              coveringDates = "   ",
              summaryErrorMessages = Seq(
                ExpectedSummaryErrorMessage("Enter the covering dates", "covering-dates"),
                ExpectedSummaryErrorMessage("Covering date format is not valid", "covering-dates")
              ),
              errorMessageForCoveringsDates = Some("Enter the covering dates")
            )
          )

          assertCallMadeToGetEditSet(idOfExistingEditSet)
          assertCallMadeToGetEditSetRecord(idOfExistingEditSet, ociOfExistingRecord)
          assertNoCallMadeToUpdateEditSetRecord()

        }
        "of an invalid format" in {

          val editSetRecordOci = "COAL.2022.V1RJW.P"
          val values =
            valuesFromRecord(editSetRecordOci) ++
              Map(
                "covering-dates" -> "1270s"
              )

          val response = submitCalculatingDates(idOfExistingEditSet, editSetRecordOci, values)

          assertPageAsExpected(
            response,
            BAD_REQUEST,
            generateExpectedEditRecordPageFromRecord(editSetRecordOci).copy(
              coveringDates = "1270s",
              summaryErrorMessages = Seq(
                ExpectedSummaryErrorMessage("Covering date format is not valid", "covering-dates")
              ),
              errorMessageForCoveringsDates = Some("Covering date format is not valid")
            )
          )

          assertCallMadeToGetEditSet(idOfExistingEditSet)
          assertCallMadeToGetEditSetRecord(idOfExistingEditSet, ociOfExistingRecord)
          assertNoCallMadeToUpdateEditSetRecord()

        }
        "containing a non-existent date" in {

          val editSetRecordOci = "COAL.2022.V1RJW.P"
          val values =
            valuesFromRecord(editSetRecordOci) ++
              Map(
                "covering-dates" -> "2022 Feb 1-2022 Feb 29"
              )

          val response = submitCalculatingDates(idOfExistingEditSet, editSetRecordOci, values)
          assertPageAsExpected(
            response,
            BAD_REQUEST,
            generateExpectedEditRecordPageFromRecord(editSetRecordOci).copy(
              coveringDates = "2022 Feb 1-2022 Feb 29",
              summaryErrorMessages = Seq(
                ExpectedSummaryErrorMessage("Covering date format is not valid", "covering-dates")
              ),
              errorMessageForCoveringsDates = Some("Covering date format is not valid")
            )
          )

          assertCallMadeToGetEditSet(idOfExistingEditSet)
          assertCallMadeToGetEditSetRecord(idOfExistingEditSet, ociOfExistingRecord)
          assertNoCallMadeToUpdateEditSetRecord()

        }
        "it covers period of the switchover" in {

          val editSetRecordOci = "COAL.2022.V1RJW.P"
          val values =
            valuesFromRecord(editSetRecordOci) ++
              Map(
                "covering-dates" -> "1752 Aug 1-1752 Sept 12"
              )

          val response = submitCalculatingDates(idOfExistingEditSet, editSetRecordOci, values)

          assertPageAsExpected(
            response,
            OK,
            generateExpectedEditRecordPageFromRecord(editSetRecordOci)
              .copy(
                coveringDates = "1752 Aug 1-1752 Sept 12",
                startDate = ExpectedDate("1", "8", "1752"),
                endDate = ExpectedDate("12", "9", "1752")
              )
          )

          assertCallMadeToGetEditSet(idOfExistingEditSet)
          assertCallMadeToGetEditSetRecord(idOfExistingEditSet, ociOfExistingRecord)
          assertNoCallMadeToUpdateEditSetRecord()

        }
        "covers period after the switchover" in {

          val editSetRecordOci = "COAL.2022.V1RJW.P"
          val values =
            valuesFromRecord(editSetRecordOci) ++
              Map(
                "covering-dates" -> "1984 Dec"
              )

          val response = submitCalculatingDates(idOfExistingEditSet, editSetRecordOci, values)

          assertPageAsExpected(
            response,
            OK,
            generateExpectedEditRecordPageFromRecord(editSetRecordOci).copy(
              coveringDates = "1984 Dec",
              startDate = ExpectedDate("1", "12", "1984"),
              endDate = ExpectedDate("31", "12", "1984")
            )
          )

          assertCallMadeToGetEditSet(idOfExistingEditSet)
          assertCallMadeToGetEditSetRecord(idOfExistingEditSet, ociOfExistingRecord)
          assertNoCallMadeToUpdateEditSetRecord()

        }
        "covers multiple ranges" in {

          val editSetRecordOci = "COAL.2022.V1RJW.P"
          val values =
            valuesFromRecord(editSetRecordOci) ++
              Map(
                "covering-dates" -> "1868; 1890-1902; 1933"
              )

          val response = submitCalculatingDates(idOfExistingEditSet, editSetRecordOci, values)

          assertPageAsExpected(
            response,
            OK,
            generateExpectedEditRecordPageFromRecord(editSetRecordOci)
              .copy(
                coveringDates = "1868; 1890-1902; 1933",
                startDate = ExpectedDate("1", "1", "1868"),
                endDate = ExpectedDate("31", "12", "1933")
              )
          )

          assertCallMadeToGetEditSet(idOfExistingEditSet)
          assertCallMadeToGetEditSetRecord(idOfExistingEditSet, ociOfExistingRecord)
          assertNoCallMadeToUpdateEditSetRecord()

        }

      }
    }
  }

  private def getEditSetRecordEditPageWhileLoggedOut(editSetId: String, editSetRecordOci: String): WSResponse =
    getEditSetRecordEditPage(editSetId, editSetRecordOci, None)

  private def getEditSetRecordEditPageWhileLoggedIn(editSetId: String, editSetRecordOci: String): WSResponse =
    getEditSetRecordEditPage(editSetId, editSetRecordOci, Some(loginForSessionCookie()))

  private def getEditSetRecordEditPage(
    editSetId: String,
    editSetRecordOci: String,
    sessionCookie: Option[WSCookie]
  ): WSResponse = {
    val rawUrl =
      s"/edit-set/$editSetId/record/$editSetRecordOci/edit"
    val baseUrl = wsUrl(rawUrl).withFollowRedirects(false)
    val url = sessionCookie.map(baseUrl.addCookies(_)).getOrElse(baseUrl)
    await(url.get())
  }

  private def getEditSetRecordDiscardPage(
    editSetId: String,
    editSetRecordOci: String
  ): WSResponse = {
    val rawUrl =
      s"/edit-set/$editSetId/record/$editSetRecordOci/edit/discard"
    val baseUrl = wsUrl(rawUrl).withFollowRedirects(false)
    val url = baseUrl.addCookies(loginForSessionCookie())
    await(url.get())
  }

  private def getEditSetRecordSavePage(
    editSetId: String,
    editSetRecordOci: String
  ): WSResponse = {
    val rawUrl =
      s"/edit-set/$editSetId/record/$editSetRecordOci/edit/save"
    val baseUrl = wsUrl(rawUrl).withFollowRedirects(false)
    val url = baseUrl.addCookies(loginForSessionCookie())
    await(url.get())
  }

  private def generateExpectedEditRecordPageFromRecord(oci: String): ExpectedEditRecordEditPage = {
    val editSetRecord = getExpectedEditSetRecord(oci)
    val messages: Map[String, String] = Map("edit-set.record.edit.type.physical" -> "Physical Record")
    val expectedEditRecordPage = ExpectedEditRecordEditPage(
      title = "Edit record",
      heading = s"TNA reference: ${editSetRecord.ccr}",
      subHeading = s"PAC-ID: ${editSetRecord.oci} ${editSetRecord.recordType match {
          case Some(PhysicalRecord) => messages("edit-set.record.edit.type.physical")
          case _                    => ""
        }}",
      legend = "Intellectual properties",
      classicCatalogueRef = editSetRecord.ccr,
      omegaCatalogueId = editSetRecord.oci,
      scopeAndContent = editSetRecord.scopeAndContent,
      coveringDates = editSetRecord.coveringDates,
      formerReferenceDepartment = editSetRecord.formerReferenceDepartment,
      formerReferencePro = editSetRecord.formerReferencePro,
      startDate = ExpectedDate(editSetRecord.startDateDay, editSetRecord.startDateMonth, editSetRecord.startDateYear),
      endDate = ExpectedDate(editSetRecord.endDateDay, editSetRecord.endDateMonth, editSetRecord.endDateYear),
      legalStatusID = editSetRecord.legalStatusID,
      note = editSetRecord.note,
      background = editSetRecord.background,
      optionsForPlaceOfDepositID = Seq(
        ExpectedSelectOption("", "Select where this record is held", disabled = true),
        ExpectedSelectOption(tna, "The National Archives", selected = true),
        ExpectedSelectOption(britishMuseum, "British Museum, Central Archive"),
        ExpectedSelectOption(britishLibrary, "British Library, Sound Archive")
      ).map(expectedSelectedOption =>
        expectedSelectedOption.copy(selected = expectedSelectedOption.value == editSetRecord.placeOfDepositID)
      ),
      optionsForCreators = {
        val recognisedCreatorIds =
          editSetRecord.creatorIDs.filter(creatorId => allCreators.exists(_.identifier == creatorId))
        val correctedCreatorIds = if (recognisedCreatorIds.nonEmpty) recognisedCreatorIds else Seq("")
        correctedCreatorIds
          .map(creatorId =>
            Seq(
              ExpectedSelectOption("", "Select creator", disabled = true),
              ExpectedSelectOption(s"$baseUriAgent.3LG", "Edwin Hill (1793 - 1876)"),
              ExpectedSelectOption(s"$baseUriAgent.2YK", "Edward Gibson (1837 - 1913)"),
              ExpectedSelectOption(
                s"$baseUriAgent.N6L",
                "National Coal Board, Northumberland and Durham Division (1964 - 1967)"
              ),
              ExpectedSelectOption(s"$baseUriAgent.W91", "Queen's Printer for Scotland (1999 - )")
            ).map(expectedSelectedOption =>
              expectedSelectedOption.copy(selected = expectedSelectedOption.value == creatorId)
            )
          )
      },
      relatedMaterial = editSetRecord.relatedMaterial.map {
        case MaterialReference.LinkAndDescription(linkHref, linkText, description) =>
          ExpectedMaterial(linkHref = Some(linkHref), linkText = Some(linkText), description = Some(description))
        case MaterialReference.LinkOnly(linkHref, linkText) =>
          ExpectedMaterial(linkHref = Some(linkHref), linkText = Some(linkText))
        case MaterialReference.DescriptionOnly(description) => ExpectedMaterial(description = Some(description))
      },
      separatedMaterial = editSetRecord.separatedMaterial.map {
        case MaterialReference.LinkAndDescription(linkHref, linkText, description) =>
          ExpectedMaterial(
            linkHref = Some(linkHref),
            linkText = Some(linkText),
            description = Some(description)
          )
        case MaterialReference.LinkOnly(linkHref, linkText) =>
          ExpectedMaterial(linkHref = Some(linkHref), linkText = Some(linkText))
        case MaterialReference.DescriptionOnly(description) =>
          ExpectedMaterial(description = Some(description))
      },
      custodialHistory = editSetRecord.custodialHistory
    )
    expectedEditRecordPage
  }

  private def assertPageAsExpected(
    response: WSResponse,
    expectedStatus: Int,
    expectedEditRecordPage: ExpectedEditRecordEditPage
  ): Assertion = {

    response.status mustBe expectedStatus
    val document = asDocument(response)
    document must haveTitle(expectedEditRecordPage.title)
    document must haveHeading(expectedEditRecordPage.heading)
    document must haveLegend(expectedEditRecordPage.legend)
    document must haveClassicCatalogueRef(expectedEditRecordPage.classicCatalogueRef)
    document must haveOmegaCatalogueId(expectedEditRecordPage.omegaCatalogueId)
    document must haveScopeAndContent(expectedEditRecordPage.scopeAndContent)
    document must haveCoveringDates(expectedEditRecordPage.coveringDates)
    document must haveFormerReferenceDepartment(expectedEditRecordPage.formerReferenceDepartment)
    document must haveFormerReferencePro(expectedEditRecordPage.formerReferencePro)
    document must haveStartDateDay(expectedEditRecordPage.startDate.day)
    document must haveStartDateMonth(expectedEditRecordPage.startDate.month)
    document must haveStartDateYear(expectedEditRecordPage.startDate.year)
    document must haveEndDateDay(expectedEditRecordPage.endDate.day)
    document must haveEndDateMonth(expectedEditRecordPage.endDate.month)
    document must haveEndDateYear(expectedEditRecordPage.endDate.year)
    document must haveLegalStatus(expectedEditRecordPage.legalStatusID)
    document must haveSelectionForPlaceOfDeposit(expectedEditRecordPage.optionsForPlaceOfDepositID)

    document must haveNumberOfSelectionsForCreator(expectedEditRecordPage.optionsForCreators.size)
    expectedEditRecordPage.optionsForCreators.zipWithIndex.foreach { case (expectedSelectOptions, index) =>
      document must haveSelectionForCreator(index, expectedSelectOptions)
    }

    document must haveNote(expectedEditRecordPage.note)
    document must haveRelatedMaterial(expectedEditRecordPage.relatedMaterial: _*)
    document must haveSeparatedMaterial(expectedEditRecordPage.separatedMaterial: _*)
    document must haveBackground(expectedEditRecordPage.background)
    document must haveCustodialHistory(expectedEditRecordPage.custodialHistory)

    document must haveVisibleLogoutLink
    document must haveLogoutLinkLabel("Sign out")
    document must haveLogoutLink
    document must haveActionButtons("save", "Save changes", 2)
    document must haveActionButtons("discard", "Discard changes", 2)

    if (expectedEditRecordPage.summaryErrorMessages.nonEmpty) {
      document must haveSummaryErrorMessages(expectedEditRecordPage.summaryErrorMessages: _*)
    } else {
      document must haveNoSummaryErrorMessages
    }

    expectedEditRecordPage.errorMessageForStartDate match {
      case Some(expectedErrorMessage) => document must haveErrorMessageForStartDate(expectedErrorMessage)
      case None                       => document must haveNoErrorMessageForStartDate
    }

    expectedEditRecordPage.errorMessageForEndDate match {
      case Some(expectedErrorMessage) => document must haveErrorMessageForEndDate(expectedErrorMessage)
      case None                       => document must haveNoErrorMessageForEndDate
    }

    expectedEditRecordPage.errorMessageForCoveringsDates match {
      case Some(expectedErrorMessage) => document must haveErrorMessageForCoveringDates(expectedErrorMessage)
      case None                       => document must haveNoErrorMessageForCoveringDates
    }

    expectedEditRecordPage.errorMessageForLegalStatus match {
      case Some(expectedErrorMessage) => document must haveErrorMessageForLegalStatus(expectedErrorMessage)
      case None                       => document must haveNoErrorMessageForLegalStatus
    }

    expectedEditRecordPage.errorMessageForPlaceOfDeposit match {
      case Some(expectedErrorMessage) => document must haveErrorMessageForPlaceOfDeposit(expectedErrorMessage)
      case None                       => document must haveNoErrorMessageForPlaceOfDeposit
    }

    expectedEditRecordPage.errorMessageForNote match {
      case Some(expectedErrorMessage) => document must haveErrorMessageForNote(expectedErrorMessage)
      case None                       => document must haveNoErrorMessageForNote
    }

    expectedEditRecordPage.errorMessageForBackground match {
      case Some(expectedErrorMessage) => document must haveErrorMessageForBackground(expectedErrorMessage)
      case None                       => document must haveNoErrorMessageForBackground
    }

    expectedEditRecordPage.errorMessageForCustodialHistory match {
      case Some(expectedErrorMessage) => document must haveErrorMessageForCustodialHistory(expectedErrorMessage)
      case None                       => document must haveNoErrorMessageForCustodialHistory
    }

    expectedEditRecordPage.errorMessageForCreator match {
      case Some(expectedErrorMessage) => document must haveErrorMessageForCreator(expectedErrorMessage)
      case None                       => document must haveNoErrorMessageForCreator
    }

    expectedEditRecordPage.errorMessageForScopeAndContent match {
      case Some(expectedErrorMessage) => document must haveErrorMessageForScopeAndContent(expectedErrorMessage)
      case None                       => document must haveNoErrorMessageForScopeAndContent
    }

    expectedEditRecordPage.errorMessageForFormerReferenceDepartment match {
      case Some(expectedErrorMessage) =>
        document must haveErrorMessageForFormerReferenceDepartment(expectedErrorMessage)
      case None => document must haveNoErrorMessageForFormerReferenceDepartment
    }

    expectedEditRecordPage.errorMessageForFormerReferencePro match {
      case Some(expectedErrorMessage) => document must haveErrorMessageForFormerReferencePro(expectedErrorMessage)
      case None                       => document must haveNoErrorMessageForFormerReferencePro
    }
  }

  private def assertPageAsExpected(
    response: WSResponse,
    expectedStatus: Int,
    expectedEditRecordDiscardPage: ExpectedEditRecordDiscardPage
  ): Assertion = {
    response.status mustBe expectedStatus

    val document = asDocument(response)

    document must haveTitle(expectedEditRecordDiscardPage.title)

    document must haveHeaderTitle("Pan-Archival Catalogue")
    document must haveVisibleLogoutLink
    document must haveLogoutLinkLabel("Sign out")
    document must haveLogoutLink

    document must haveNotificationBannerContents(
      Seq(
        expectedEditRecordDiscardPage.bannerLineOne,
        expectedEditRecordDiscardPage.bannerLineTwo,
        expectedEditRecordDiscardPage.bannerLineThree,
        expectedEditRecordDiscardPage.bannerLineFour
      )
    )
    document must haveBackLink(expectedEditRecordDiscardPage.backLinkHref, expectedEditRecordDiscardPage.backLinkLabel)

  }

  private def assertPageAsExpected(
    response: WSResponse,
    expectedStatus: Int,
    expectedPage: ExpectedEditRecordSavePage
  ): Assertion = {
    response.status mustBe expectedStatus

    val document = asDocument(response)

    document must haveTitle(expectedPage.title)
    document must haveHeaderTitle("Pan-Archival Catalogue")
    document must haveVisibleLogoutLink
    document must haveLogoutLinkLabel("Sign out")
    document must haveLogoutLink

    document must haveNotificationBannerContents(
      Seq(
        expectedPage.bannerLineOne,
        expectedPage.bannerLineTwo,
        expectedPage.bannerLineThree,
        expectedPage.bannerLineFour
      )
    )
    document must haveBackLink(expectedPage.backLinkHref, expectedPage.backLinkLabel)

  }

  private def submitSavingChanges(
    editSetId: String,
    editSetRecordOci: String,
    values: Map[String, String]
  ): WSResponse = {
    val valuesWithAction = values ++ Map("action" -> "save")
    val sessionCookie = loginForSessionCookie()
    val getPageResponse = getEditSetRecordEditPage(editSetId, editSetRecordOci, Some(sessionCookie))
    val csrfTokenFromLoginPage = getCsrfToken(getPageResponse)
    val rawUrl = s"/edit-set/$editSetId/record/$editSetRecordOci/edit"
    await(
      wsUrl(rawUrl)
        .withFollowRedirects(false)
        .addCookies(sessionCookie)
        .post(valuesWithAction ++ Map(csrfTokenName -> csrfTokenFromLoginPage))
    )
  }

  private def submitDiscardingChanges(
    editSetId: String,
    editSetRecordOci: String,
    values: Map[String, String]
  ): WSResponse = {
    val valuesWithAction = values ++ Map("action" -> "discard")
    val sessionCookie = loginForSessionCookie()
    val getPageResponse = getEditSetRecordEditPage(editSetId, editSetRecordOci, Some(sessionCookie))
    val csrfTokenFromLoginPage = getCsrfToken(getPageResponse)
    val rawUrl = s"/edit-set/$editSetId/record/$editSetRecordOci/edit"
    await(
      wsUrl(rawUrl)
        .withFollowRedirects(false)
        .addCookies(sessionCookie)
        .post(valuesWithAction ++ Map(csrfTokenName -> csrfTokenFromLoginPage))
    )
  }

  private def submitAddingAnotherCreatorSlot(
    editSetId: String,
    editSetRecordOci: String,
    values: Map[String, String]
  ): WSResponse = {
    val valuesWithAction = values ++ Map("action" -> "addAnotherCreator")
    val sessionCookie = loginForSessionCookie()
    val getPageResponse = getEditSetRecordEditPage(editSetId, editSetRecordOci, Some(sessionCookie))
    val csrfTokenFromLoginPage = getCsrfToken(getPageResponse)
    val rawUrl = s"/edit-set/$editSetId/record/$editSetRecordOci/edit"
    await(
      wsUrl(rawUrl)
        .withFollowRedirects(false)
        .addCookies(sessionCookie)
        .post(valuesWithAction ++ Map(csrfTokenName -> csrfTokenFromLoginPage))
    )
  }

  private def submitRemovingTheLastCreator(
    editSetId: String,
    editSetRecordOci: String,
    values: Map[String, String]
  ): WSResponse = {
    val valuesWithAction = values ++ Map("action" -> "removeLastCreator")
    val sessionCookie = loginForSessionCookie()
    val getPageResponse = getEditSetRecordEditPage(editSetId, editSetRecordOci, Some(sessionCookie))
    val csrfTokenFromLoginPage = getCsrfToken(getPageResponse)
    val rawUrl = s"/edit-set/$editSetId/record/$editSetRecordOci/edit"
    await(
      wsUrl(rawUrl)
        .withFollowRedirects(false)
        .addCookies(sessionCookie)
        .post(valuesWithAction ++ Map(csrfTokenName -> csrfTokenFromLoginPage))
    )
  }

  private def submitCalculatingDates(
    editSetId: String,
    editSetRecordOci: String,
    values: Map[String, String]
  ): WSResponse = {
    val valuesWithAction = values ++ Map("action" -> "calculateDates")
    val sessionCookie = loginForSessionCookie()
    val getPageResponse = getEditSetRecordEditPage(editSetId, editSetRecordOci, Some(sessionCookie))
    val csrfTokenFromLoginPage = getCsrfToken(getPageResponse)
    val rawUrl = s"/edit-set/$editSetId/record/$editSetRecordOci/edit"
    await(
      wsUrl(rawUrl)
        .withFollowRedirects(false)
        .addCookies(sessionCookie)
        .post(valuesWithAction ++ Map(csrfTokenName -> csrfTokenFromLoginPage))
    )
  }

  private def valuesFromRecord(oci: String): Map[String, String] = {
    val editSetRecord = getExpectedEditSetRecord(oci)
    val mapOfCreatorIDs = editSetRecord.creatorIDs.zipWithIndex.map { case (creatorId, index) =>
      val key = s"creator-ids[$index]"
      (key, creatorId)
    }.toMap
    Map(
      "background"                  -> editSetRecord.background,
      "covering-dates"              -> editSetRecord.coveringDates,
      "custodial-history"           -> editSetRecord.custodialHistory,
      "end-date-day"                -> editSetRecord.endDateDay,
      "end-date-month"              -> editSetRecord.endDateMonth,
      "end-date-year"               -> editSetRecord.endDateYear,
      "former-reference-department" -> editSetRecord.formerReferenceDepartment,
      "former-reference-pro"        -> editSetRecord.formerReferencePro,
      "legal-status-id"             -> editSetRecord.legalStatusID,
      "note"                        -> editSetRecord.note,
      "oci"                         -> editSetRecord.oci,
      "place-of-deposit-id"         -> editSetRecord.placeOfDepositID,
      "scope-and-content"           -> editSetRecord.scopeAndContent,
      "start-date-day"              -> editSetRecord.startDateDay,
      "start-date-month"            -> editSetRecord.startDateMonth,
      "start-date-year"             -> editSetRecord.startDateYear
    ) ++ mapOfCreatorIDs

  }

}

object EditSetRecordISpec {

  case class ExpectedEditRecordEditPage(
    title: String,
    heading: String,
    subHeading: String,
    legend: String,
    classicCatalogueRef: String,
    omegaCatalogueId: String,
    scopeAndContent: String,
    coveringDates: String,
    formerReferenceDepartment: String,
    formerReferencePro: String,
    startDate: ExpectedDate,
    endDate: ExpectedDate,
    legalStatusID: String,
    note: String,
    background: String,
    custodialHistory: String,
    optionsForPlaceOfDepositID: Seq[ExpectedSelectOption],
    optionsForCreators: Seq[Seq[ExpectedSelectOption]],
    relatedMaterial: Seq[ExpectedMaterial] = Seq.empty,
    separatedMaterial: Seq[ExpectedMaterial] = Seq.empty,
    summaryErrorMessages: Seq[ExpectedSummaryErrorMessage] = Seq.empty,
    errorMessageForStartDate: Option[String] = None,
    errorMessageForEndDate: Option[String] = None,
    errorMessageForCoveringsDates: Option[String] = None,
    errorMessageForLegalStatus: Option[String] = None,
    errorMessageForPlaceOfDeposit: Option[String] = None,
    errorMessageForNote: Option[String] = None,
    errorMessageForBackground: Option[String] = None,
    errorMessageForCustodialHistory: Option[String] = None,
    errorMessageForCreator: Option[String] = None,
    errorMessageForScopeAndContent: Option[String] = None,
    errorMessageForFormerReferenceDepartment: Option[String] = None,
    errorMessageForFormerReferencePro: Option[String] = None
  )

  case class ExpectedEditRecordDiscardPage(
    title: String,
    bannerLineOne: String,
    bannerLineTwo: String,
    bannerLineThree: String,
    bannerLineFour: String,
    backLinkLabel: String,
    backLinkHref: String
  )

  case class ExpectedEditRecordSavePage(
    title: String,
    bannerLineOne: String,
    bannerLineTwo: String,
    bannerLineThree: String,
    bannerLineFour: String,
    backLinkLabel: String,
    backLinkHref: String
  )

}
