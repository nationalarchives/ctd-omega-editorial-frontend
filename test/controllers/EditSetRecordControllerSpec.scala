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

package controllers

import cats.effect.IO
import org.mockito.ArgumentMatchers
import org.mockito.captor.Captor._
import org.mockito.captor.{ ArgCaptor, Captor }
import org.mockito.stubbing.ScalaOngoingStubbing
import org.scalatest.Assertion
import play.api.data.{ Form, FormError }
import play.api.http.Status.OK
import play.api.i18n.Messages
import play.api.mvc._
import play.api.test.Helpers.{ status, _ }
import play.api.test.{ CSRFTokenHelper, FakeRequest, Helpers }
import play.twirl.api.HtmlFormat
import support.BaseControllerSpec
import uk.gov.nationalarchives.omega.editorial.controllers.EditSetRecordController.{ FieldNames, MessageKeys }
import uk.gov.nationalarchives.omega.editorial.controllers.{ EditSetRecordController, SessionKeys }
import uk.gov.nationalarchives.omega.editorial.forms.EditSetRecordFormValues
import uk.gov.nationalarchives.omega.editorial.models._
import uk.gov.nationalarchives.omega.editorial.services.{ EditSetRecordService, EditSetService, ReferenceDataService }
import uk.gov.nationalarchives.omega.editorial.views.html.{ editSetRecordEdit, editSetRecordEditDiscard, editSetRecordEditSave }

import scala.concurrent.Future

class EditSetRecordControllerSpec extends BaseControllerSpec {

  private val legalStatuses: Seq[LegalStatus] = Seq(
    LegalStatus("ref.1", "Public Record(s)"),
    LegalStatus("ref.2", "Not Public Records"),
    LegalStatus("ref.3", "Public Records unless otherwise Stated"),
    LegalStatus("ref.4", "Welsh Public Record(s)")
  )

  private val placesOfDeposit: Seq[PlaceOfDeposit] = Seq(
    PlaceOfDeposit("1", "The National Archives, Kew"),
    PlaceOfDeposit("2", "British Museum, Department of Libraries and Archives"),
    PlaceOfDeposit("3", "British Library, National Sound Archive")
  )

  /** As these mocks are within a fixture, they will all be managed; for instance, a check will be made against missed
    * or unnecessary stubbing. This will give a clearer picture of the usage of dependencies.
    */
  private trait Fixture {

    implicit val editSetService: EditSetService = mock[EditSetService]
    implicit val editSetRecordService: EditSetRecordService = mock[EditSetRecordService]
    implicit val referenceDataService: ReferenceDataService = mock[ReferenceDataService]
    implicit val editSetRecordEditView: editSetRecordEdit = mock[editSetRecordEdit]
    implicit val editSetRecordDiscardView: editSetRecordEditDiscard = mock[editSetRecordEditDiscard]
    implicit val editSetRecordSaveView: editSetRecordEditSave = mock[editSetRecordEditSave]

    implicit lazy val controller: EditSetRecordController =
      new EditSetRecordController(
        Helpers.stubMessagesControllerComponents(),
        referenceDataService,
        editSetService,
        editSetRecordService,
        editSetRecordEditView,
        editSetRecordDiscardView,
        editSetRecordSaveView
      )

  }

  "When viewing" when {
    "when requested with an invalid session token" in new Fixture {

      val editSetId = "1"
      val editSetRecordId = "COAL.2022.V1RJW.P"

      val result: Future[Result] = viewWhileLoggedOut(editSetId, editSetRecordId)

      assertRedirectionToLoginPage(result)

    }
    "when requested with a valid session token" in new Fixture {

      val editSetId = "1"
      val editSetRecordId = "COAL.2022.V1RJW.P"
      val returnedEditSet: EditSet = getExpectedEditSet(editSetId)
      givenEditSetExists(editSetId, returnedEditSet)
      val returnedEditSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
      givenEditSetRecordExists(editSetId, editSetRecordId, returnedEditSetRecord)
      givenCreatorIdsArePrepared(returnedEditSetRecord)
      givenLegalStatusesExist()
      givenPlacesOfDepositsExist()
      givenCreatorsExist()
      givenEditViewIsGenerated(returnedEditSetRecord)

      val result: Future[Result] = viewWhileLoggedIn(editSetId, editSetRecordId)

      status(result) mustBe OK
      assertEditViewForm(editSetRecordFormValuesFromRecord(editSetRecordId), returnedEditSetRecord, Seq.empty)

    }

  }
  "When submitting" when {
    "intending to save the record" should {
      "fail" when {
        "start date" when {
          "is empty" in new Fixture {

            val editSetId = "1"
            val editSetRecordId = "COAL.2022.V1RJW.P"
            val values: Map[String, String] =
              valuesFromRecord(editSetRecordId) ++ Map(
                FieldNames.startDateDay   -> "",
                FieldNames.startDateMonth -> "",
                FieldNames.startDateYear  -> ""
              )
            val returnedEditSet: EditSet = getExpectedEditSet(editSetId)
            givenEditSetExists(editSetId, returnedEditSet)
            val returnedEditSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
            givenEditSetRecordExists(editSetId, editSetRecordId, returnedEditSetRecord)
            givenLegalStatusesExist()
            givenPlacesOfDepositsExist()
            givenCreatorsExist()
            givenEditViewIsGenerated(returnedEditSetRecord)

            val result: Future[Result] = submitToSaveWhileLoggedIn(editSetId, editSetRecordId, values)

            status(result) mustBe BAD_REQUEST
            assertEditViewForm(
              editSetRecordFormValuesFromRecord(editSetRecordId)
                .copy(startDateDay = "", startDateMonth = "", startDateYear = ""),
              returnedEditSetRecord,
              Seq(FormError(FieldNames.startDateDay, MessageKeys.startDateInvalid))
            )

          }
          "is of an invalid format" in new Fixture {

            val editSetId = "1"
            val editSetRecordId = "COAL.2022.V1RJW.P"
            val values: Map[String, String] =
              valuesFromRecord(editSetRecordId) ++ Map(
                FieldNames.startDateDay   -> "XX",
                FieldNames.startDateMonth -> "11",
                FieldNames.startDateYear  -> "1960"
              )
            val returnedEditSet: EditSet = getExpectedEditSet(editSetId)
            givenEditSetExists(editSetId, returnedEditSet)
            val returnedEditSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
            givenEditSetRecordExists(editSetId, editSetRecordId, returnedEditSetRecord)
            givenLegalStatusesExist()
            givenPlacesOfDepositsExist()
            givenCreatorsExist()
            givenEditViewIsGenerated(returnedEditSetRecord)

            val result: Future[Result] = submitToSaveWhileLoggedIn(editSetId, editSetRecordId, values)

            status(result) mustBe BAD_REQUEST
            assertEditViewForm(
              editSetRecordFormValuesFromRecord(editSetRecordId)
                .copy(startDateDay = "XX", startDateMonth = "11", startDateYear = "1960"),
              returnedEditSetRecord,
              Seq(FormError(FieldNames.startDateDay, MessageKeys.startDateInvalid))
            )

          }
          "doesn't exist" in new Fixture {

            val editSetId = "1"
            val editSetRecordId = "COAL.2022.V1RJW.P"
            val values: Map[String, String] =
              valuesFromRecord(editSetRecordId) ++ Map(
                FieldNames.startDateDay   -> "29",
                FieldNames.startDateMonth -> "2",
                FieldNames.startDateYear  -> "2022",
                FieldNames.endDateDay     -> "31",
                FieldNames.endDateMonth   -> "10",
                FieldNames.endDateYear    -> "2022"
              )
            val returnedEditSet: EditSet = getExpectedEditSet(editSetId)
            givenEditSetExists(editSetId, returnedEditSet)
            val returnedEditSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
            givenEditSetRecordExists(editSetId, editSetRecordId, returnedEditSetRecord)
            givenLegalStatusesExist()
            givenPlacesOfDepositsExist()
            givenCreatorsExist()
            givenEditViewIsGenerated(returnedEditSetRecord)

            val result: Future[Result] = submitToSaveWhileLoggedIn(editSetId, editSetRecordId, values)

            status(result) mustBe BAD_REQUEST
            assertEditViewForm(
              editSetRecordFormValuesFromRecord(editSetRecordId)
                .copy(
                  startDateDay = "29",
                  startDateMonth = "2",
                  startDateYear = "2022",
                  endDateDay = "31",
                  endDateMonth = "10",
                  endDateYear = "2022"
                ),
              returnedEditSetRecord,
              Seq(FormError(FieldNames.startDateDay, MessageKeys.startDateInvalid))
            )

          }
        }

        "end date" when {
          "is empty" in new Fixture {

            val editSetId = "1"
            val editSetRecordId = "COAL.2022.V1RJW.P"
            val values: Map[String, String] =
              valuesFromRecord(editSetRecordId) ++ Map(
                FieldNames.endDateDay   -> "",
                FieldNames.endDateMonth -> "",
                FieldNames.endDateYear  -> ""
              )
            val returnedEditSet: EditSet = getExpectedEditSet(editSetId)
            givenEditSetExists(editSetId, returnedEditSet)
            val returnedEditSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
            givenEditSetRecordExists(editSetId, editSetRecordId, returnedEditSetRecord)
            givenLegalStatusesExist()
            givenPlacesOfDepositsExist()
            givenCreatorsExist()
            givenEditViewIsGenerated(returnedEditSetRecord)

            val result: Future[Result] = submitToSaveWhileLoggedIn(editSetId, editSetRecordId, values)

            status(result) mustBe BAD_REQUEST
            assertEditViewForm(
              editSetRecordFormValuesFromRecord(editSetRecordId)
                .copy(endDateDay = "", endDateMonth = "", endDateYear = ""),
              returnedEditSetRecord,
              Seq(FormError(FieldNames.endDateDay, MessageKeys.endDateInvalid))
            )

          }
          "is of an invalid format" in new Fixture {

            val editSetId = "1"
            val editSetRecordId = "COAL.2022.V1RJW.P"
            val values: Map[String, String] =
              valuesFromRecord(editSetRecordId) ++ Map(
                FieldNames.endDateDay   -> "XX",
                FieldNames.endDateMonth -> "12",
                FieldNames.endDateYear  -> "2000"
              )
            val returnedEditSet: EditSet = getExpectedEditSet(editSetId)
            givenEditSetExists(editSetId, returnedEditSet)
            val returnedEditSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
            givenEditSetRecordExists(editSetId, editSetRecordId, returnedEditSetRecord)
            givenLegalStatusesExist()
            givenPlacesOfDepositsExist()
            givenCreatorsExist()
            givenEditViewIsGenerated(returnedEditSetRecord)

            val result: Future[Result] = submitToSaveWhileLoggedIn(editSetId, editSetRecordId, values)

            status(result) mustBe BAD_REQUEST
            assertEditViewForm(
              editSetRecordFormValuesFromRecord(editSetRecordId)
                .copy(endDateDay = "XX", endDateMonth = "12", endDateYear = "2000"),
              returnedEditSetRecord,
              Seq(FormError(FieldNames.endDateDay, MessageKeys.endDateInvalid))
            )

          }
          "doesn't exist" in new Fixture {

            val editSetId = "1"
            val editSetRecordId = "COAL.2022.V1RJW.P"
            val values: Map[String, String] =
              valuesFromRecord(editSetRecordId) ++ Map(
                FieldNames.startDateDay   -> "1",
                FieldNames.startDateMonth -> "2",
                FieldNames.startDateYear  -> "2022",
                FieldNames.endDateDay     -> "29",
                FieldNames.endDateMonth   -> "2",
                FieldNames.endDateYear    -> "2022"
              )
            val returnedEditSet: EditSet = getExpectedEditSet(editSetId)
            givenEditSetExists(editSetId, returnedEditSet)
            val returnedEditSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
            givenEditSetRecordExists(editSetId, editSetRecordId, returnedEditSetRecord)
            givenLegalStatusesExist()
            givenPlacesOfDepositsExist()
            givenCreatorsExist()
            givenEditViewIsGenerated(returnedEditSetRecord)

            val result: Future[Result] = submitToSaveWhileLoggedIn(editSetId, editSetRecordId, values)

            status(result) mustBe BAD_REQUEST
            assertEditViewForm(
              editSetRecordFormValuesFromRecord(editSetRecordId)
                .copy(
                  startDateDay = "1",
                  startDateMonth = "2",
                  startDateYear = "2022",
                  endDateDay = "29",
                  endDateMonth = "2",
                  endDateYear = "2022"
                ),
              returnedEditSetRecord,
              Seq(FormError(FieldNames.endDateDay, MessageKeys.endDateInvalid))
            )

          }
          "is before start date" in new Fixture {

            val editSetId = "1"
            val editSetRecordId = "COAL.2022.V1RJW.P"
            val values: Map[String, String] =
              valuesFromRecord(editSetRecordId) ++ Map(
                FieldNames.startDateDay   -> "12",
                FieldNames.startDateMonth -> "10",
                FieldNames.startDateYear  -> "2020",
                FieldNames.endDateDay     -> "11",
                FieldNames.endDateMonth   -> "10",
                FieldNames.endDateYear    -> "2020"
              )
            val returnedEditSet: EditSet = getExpectedEditSet(editSetId)
            givenEditSetExists(editSetId, returnedEditSet)
            val returnedEditSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
            givenEditSetRecordExists(editSetId, editSetRecordId, returnedEditSetRecord)
            givenLegalStatusesExist()
            givenPlacesOfDepositsExist()
            givenCreatorsExist()
            givenEditViewIsGenerated(returnedEditSetRecord)

            val result: Future[Result] = submitToSaveWhileLoggedIn(editSetId, editSetRecordId, values)

            status(result) mustBe BAD_REQUEST
            assertEditViewForm(
              editSetRecordFormValuesFromRecord(editSetRecordId)
                .copy(
                  startDateDay = "12",
                  startDateMonth = "10",
                  startDateYear = "2020",
                  endDateDay = "11",
                  endDateMonth = "10",
                  endDateYear = "2020"
                ),
              returnedEditSetRecord,
              Seq(FormError(FieldNames.endDateDay, MessageKeys.endDateBeforeStartDate))
            )

          }
        }
        "neither start date nor end date is valid" in new Fixture {

          val editSetId = "1"
          val editSetRecordId = "COAL.2022.V1RJW.P"
          val values: Map[String, String] =
            valuesFromRecord(editSetRecordId) ++ Map(
              FieldNames.startDateDay   -> "12",
              FieldNames.startDateMonth -> "14",
              FieldNames.startDateYear  -> "2020",
              FieldNames.endDateDay     -> "42",
              FieldNames.endDateMonth   -> "12",
              FieldNames.endDateYear    -> "2020"
            )
          val returnedEditSet: EditSet = getExpectedEditSet(editSetId)
          givenEditSetExists(editSetId, returnedEditSet)
          val returnedEditSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
          givenEditSetRecordExists(editSetId, editSetRecordId, returnedEditSetRecord)
          givenLegalStatusesExist()
          givenPlacesOfDepositsExist()
          givenCreatorsExist()
          givenEditViewIsGenerated(returnedEditSetRecord)

          val result: Future[Result] = submitToSaveWhileLoggedIn(editSetId, editSetRecordId, values)

          status(result) mustBe BAD_REQUEST
          assertEditViewForm(
            editSetRecordFormValuesFromRecord(editSetRecordId)
              .copy(
                startDateDay = "12",
                startDateMonth = "14",
                startDateYear = "2020",
                endDateDay = "42",
                endDateMonth = "12",
                endDateYear = "2020"
              ),
            returnedEditSetRecord,
            Seq(
              FormError(FieldNames.startDateDay, MessageKeys.startDateInvalid),
              FormError(FieldNames.endDateDay, MessageKeys.endDateInvalid)
            )
          )

        }
        "covering date" when {
          "is invalid" in new Fixture {

            val editSetId = "1"
            val editSetRecordId = "COAL.2022.V1RJW.P"
            val values: Map[String, String] =
              valuesFromRecord(editSetRecordId) ++ Map(FieldNames.coveringDates -> "Oct 1 2004")
            val returnedEditSet: EditSet = getExpectedEditSet(editSetId)
            givenEditSetExists(editSetId, returnedEditSet)
            val returnedEditSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
            givenEditSetRecordExists(editSetId, editSetRecordId, returnedEditSetRecord)
            givenLegalStatusesExist()
            givenPlacesOfDepositsExist()
            givenCreatorsExist()
            givenEditViewIsGenerated(returnedEditSetRecord)

            val result: Future[Result] = submitToSaveWhileLoggedIn(editSetId, editSetRecordId, values)

            status(result) mustBe BAD_REQUEST
            assertEditViewForm(
              returnedEditSetRecord,
              Seq(FormError(FieldNames.coveringDates, MessageKeys.coveringDatesUnparseable))
            )
          }
          "is too long" in new Fixture {

            val editSetId = "1"
            val editSetRecordId = "COAL.2022.V1RJW.P"
            val gapDateTooLong: String = (1 to 100).map(_ => "2004 Oct 1").mkString(";")
            val values: Map[String, String] =
              valuesFromRecord(editSetRecordId) ++ Map(FieldNames.coveringDates -> gapDateTooLong)
            val returnedEditSet: EditSet = getExpectedEditSet(editSetId)
            givenEditSetExists(editSetId, returnedEditSet)
            val returnedEditSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
            givenEditSetRecordExists(editSetId, editSetRecordId, returnedEditSetRecord)
            givenLegalStatusesExist()
            givenPlacesOfDepositsExist()
            givenCreatorsExist()
            givenEditViewIsGenerated(returnedEditSetRecord)

            val result: Future[Result] = submitToSaveWhileLoggedIn(editSetId, editSetRecordId, values)

            status(result) mustBe BAD_REQUEST
            assertEditViewForm(
              returnedEditSetRecord,
              Seq(FormError(FieldNames.coveringDates, MessageKeys.coveringDatesTooLong))
            )
          }
          "is empty" in new Fixture {

            val editSetId = "1"
            val editSetRecordId = "COAL.2022.V1RJW.P"
            val values: Map[String, String] =
              valuesFromRecord(editSetRecordId) ++ Map(FieldNames.coveringDates -> "  ")
            val returnedEditSet: EditSet = getExpectedEditSet(editSetId)
            givenEditSetExists(editSetId, returnedEditSet)
            val returnedEditSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
            givenEditSetRecordExists(editSetId, editSetRecordId, returnedEditSetRecord)
            givenLegalStatusesExist()
            givenPlacesOfDepositsExist()
            givenCreatorsExist()
            givenEditViewIsGenerated(returnedEditSetRecord)

            val result: Future[Result] = submitToSaveWhileLoggedIn(editSetId, editSetRecordId, values)

            status(result) mustBe BAD_REQUEST
            assertEditViewForm(
              returnedEditSetRecord,
              Seq(
                FormError(FieldNames.coveringDates, List(MessageKeys.coveringDatesMissing)),
                FormError(FieldNames.coveringDates, List(MessageKeys.coveringDatesUnparseable))
              )
            )

          }
        }

        "place of deposit" when {
          "isn't selected" in new Fixture {

            val editSetId = "1"
            val editSetRecordId = "COAL.2022.V1RJW.P"
            val values: Map[String, String] =
              valuesFromRecord(editSetRecordId) ++ Map(FieldNames.placeOfDepositID -> "")
            val returnedEditSet: EditSet = getExpectedEditSet(editSetId)
            givenEditSetExists(editSetId, returnedEditSet)
            val returnedEditSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
            givenEditSetRecordExists(editSetId, editSetRecordId, returnedEditSetRecord)
            givenLegalStatusesExist()
            givenPlacesOfDepositsExist()
            givenCreatorsExist()
            givenEditViewIsGenerated(returnedEditSetRecord)

            val result: Future[Result] = submitToSaveWhileLoggedIn(editSetId, editSetRecordId, values)

            status(result) mustBe BAD_REQUEST
            assertEditViewForm(
              returnedEditSetRecord,
              Seq(FormError(FieldNames.placeOfDepositID, List(MessageKeys.placeOfDepositMissingOrInvalid)))
            )

          }
          "is absent" in new Fixture {

            val editSetId = "1"
            val editSetRecordId = "COAL.2022.V1RJW.P"
            val values: Map[String, String] = valuesFromRecord(editSetRecordId) - FieldNames.placeOfDepositID
            val returnedEditSet: EditSet = getExpectedEditSet(editSetId)
            givenEditSetExists(editSetId, returnedEditSet)
            val returnedEditSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
            givenEditSetRecordExists(editSetId, editSetRecordId, returnedEditSetRecord)
            givenLegalStatusesExist()
            givenPlacesOfDepositsExist()
            givenCreatorsExist()
            givenEditViewIsGenerated(returnedEditSetRecord)

            val result: Future[Result] = submitToSaveWhileLoggedIn(editSetId, editSetRecordId, values)

            status(result) mustBe BAD_REQUEST
            assertEditViewForm(
              returnedEditSetRecord,
              Seq(FormError(FieldNames.placeOfDepositID, List(MessageKeys.placeOfDepositMissingOrInvalid)))
            )

          }
          "isn't recognised" in new Fixture {

            val editSetId = "1"
            val editSetRecordId = "COAL.2022.V1RJW.P"
            val values: Map[String, String] =
              valuesFromRecord(editSetRecordId) ++ Map(FieldNames.placeOfDepositID -> "6")
            val returnedEditSet: EditSet = getExpectedEditSet(editSetId)
            givenEditSetExists(editSetId, returnedEditSet)
            val returnedEditSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
            givenEditSetRecordExists(editSetId, editSetRecordId, returnedEditSetRecord)
            givenLegalStatusesExist()
            givenPlacesOfDepositsExist()
            givenCreatorsExist()
            givenEditViewIsGenerated(returnedEditSetRecord)

            val result: Future[Result] = submitToSaveWhileLoggedIn(editSetId, editSetRecordId, values)

            status(result) mustBe BAD_REQUEST
            assertEditViewForm(
              editSetRecordFormValuesFromRecord(editSetRecordId).copy(placeOfDepositID = "6"),
              returnedEditSetRecord,
              Seq(FormError(FieldNames.placeOfDepositID, List(MessageKeys.placeOfDepositMissingOrInvalid)))
            )

          }
        }
        "legal status" when {
          "is not selected" in new Fixture {

            val editSetId = "1"
            val editSetRecordId = "COAL.2022.V1RJW.P"
            val values: Map[String, String] =
              valuesFromRecord(editSetRecordId) ++ Map(FieldNames.legalStatusID -> "")
            val returnedEditSet: EditSet = getExpectedEditSet(editSetId)
            givenEditSetExists(editSetId, returnedEditSet)
            val returnedEditSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
            givenEditSetRecordExists(editSetId, editSetRecordId, returnedEditSetRecord)
            givenLegalStatusesExist()
            givenPlacesOfDepositsExist()
            givenCreatorsExist()
            givenEditViewIsGenerated(returnedEditSetRecord)

            val result: Future[Result] = submitToSaveWhileLoggedIn(editSetId, editSetRecordId, values)

            status(result) mustBe BAD_REQUEST
            assertEditViewForm(
              returnedEditSetRecord,
              Seq(FormError(FieldNames.legalStatusID, List(MessageKeys.legalStatusMissing)))
            )

          }

        }
        "note" when {
          "is too long" in new Fixture {

            val editSetId = "1"
            val editSetRecordId = "COAL.2022.V1RJW.P"
            val excessivelyLongNote: String = "Something about something else." * 100
            val values: Map[String, String] =
              valuesFromRecord(editSetRecordId) ++ Map(FieldNames.note -> excessivelyLongNote)
            val returnedEditSet: EditSet = getExpectedEditSet(editSetId)
            givenEditSetExists(editSetId, returnedEditSet)
            val returnedEditSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
            givenEditSetRecordExists(editSetId, editSetRecordId, returnedEditSetRecord)
            givenLegalStatusesExist()
            givenPlacesOfDepositsExist()
            givenCreatorsExist()
            givenEditViewIsGenerated(returnedEditSetRecord)

            val result: Future[Result] = submitToSaveWhileLoggedIn(editSetId, editSetRecordId, values)

            status(result) mustBe BAD_REQUEST
            assertEditViewForm(returnedEditSetRecord, Seq(FormError(FieldNames.note, List(MessageKeys.noteTooLong))))

          }
        }
        "background" when {
          "is too long" in new Fixture {

            val editSetId = "1"
            val editSetRecordId = "COAL.2022.V1RJW.P"
            val excessivelyLongBackground: String = "Something about one of the people." * 400
            val values: Map[String, String] =
              valuesFromRecord(editSetRecordId) ++ Map(FieldNames.background -> excessivelyLongBackground)
            val returnedEditSet: EditSet = getExpectedEditSet(editSetId)
            givenEditSetExists(editSetId, returnedEditSet)
            val returnedEditSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
            givenEditSetRecordExists(editSetId, editSetRecordId, returnedEditSetRecord)
            givenLegalStatusesExist()
            givenPlacesOfDepositsExist()
            givenCreatorsExist()
            givenEditViewIsGenerated(returnedEditSetRecord)

            val result: Future[Result] = submitToSaveWhileLoggedIn(editSetId, editSetRecordId, values)

            status(result) mustBe BAD_REQUEST
            assertEditViewForm(
              returnedEditSetRecord,
              Seq(FormError(FieldNames.background, List(MessageKeys.backgroundTooLong)))
            )
          }
        }

        "custodial history" when {
          "is too long" in new Fixture {

            val editSetId = "1"
            val editSetRecordId = "COAL.2022.V1RJW.P"
            val custodialHistoryTooLong: String =
              "Files originally created by successor or predecessor departments for COAL" * 100
            val values: Map[String, String] =
              valuesFromRecord(editSetRecordId) ++ Map(FieldNames.custodialHistory -> custodialHistoryTooLong)
            val returnedEditSet: EditSet = getExpectedEditSet(editSetId)
            givenEditSetExists(editSetId, returnedEditSet)
            val returnedEditSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
            givenEditSetRecordExists(editSetId, editSetRecordId, returnedEditSetRecord)
            givenLegalStatusesExist()
            givenPlacesOfDepositsExist()
            givenCreatorsExist()
            givenEditViewIsGenerated(returnedEditSetRecord)

            val result: Future[Result] = submitToSaveWhileLoggedIn(editSetId, editSetRecordId, values)

            status(result) mustBe BAD_REQUEST
            assertEditViewForm(
              returnedEditSetRecord,
              Seq(FormError(FieldNames.custodialHistory, List(MessageKeys.custodialHistoryTooLong)))
            )

          }
        }
        "no creator has been selected" in new Fixture {

          val editSetId = "1"
          val editSetRecordId = "COAL.2022.V1RJW.P"
          val values: Map[String, String] =
            valuesFromRecord(editSetRecordId) ++ Map(
              s"${FieldNames.creatorIDs}[0]" -> "",
              s"${FieldNames.creatorIDs}[1]" -> ""
            )
          val returnedEditSet: EditSet = getExpectedEditSet(editSetId)
          givenEditSetExists(editSetId, returnedEditSet)
          val returnedEditSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
          givenEditSetRecordExists(editSetId, editSetRecordId, returnedEditSetRecord)
          givenLegalStatusesExist()
          givenPlacesOfDepositsExist()
          givenCreatorsExist()
          givenEditViewIsGenerated(returnedEditSetRecord)

          val result: Future[Result] = submitToSaveWhileLoggedIn(editSetId, editSetRecordId, values)

          status(result) mustBe BAD_REQUEST
          assertEditViewForm(
            editSetRecordFormValuesFromRecord(editSetRecordId).copy(creatorIDs = Seq("", "")),
            returnedEditSetRecord,
            Seq(FormError("creator-id-0", List(MessageKeys.creatorMissing)))
          )

        }
      }
      "succeed" when {
        "all is valid" in new Fixture {

          val editSetId = "1"
          val editSetRecordId = "COAL.2022.V1RJW.P"
          val values: Map[String, String] = valuesFromRecord(editSetRecordId)
          val returnedEditSet: EditSet = getExpectedEditSet(editSetId)
          givenEditSetExists(editSetId, returnedEditSet)
          val returnedEditSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
          givenEditSetRecordExists(editSetId, editSetRecordId, returnedEditSetRecord)
          givenPlacesOfDepositsExist()
          givenEditSetRecordIsSuccessfullyUpdated(
            editSetId,
            editSetRecordId,
            editSetRecordFormValuesFromRecord(editSetRecordId)
          )

          val result: Future[Result] = submitToSaveWhileLoggedIn(editSetId, editSetRecordId, values)

          assertRedirectionToSavePage(result, editSetId, editSetRecordId)

        }

        "redirect to the save page" when {
          "the 'note' field is blank" in new Fixture {

            val editSetId = "1"
            val editSetRecordId = "COAL.2022.V1RJW.P"
            val values: Map[String, String] =
              valuesFromRecord(editSetRecordId) ++ Map(FieldNames.note -> "")
            val returnedEditSet: EditSet = getExpectedEditSet(editSetId)
            givenEditSetExists(editSetId, returnedEditSet)
            val returnedEditSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
            givenEditSetRecordExists(editSetId, editSetRecordId, returnedEditSetRecord)
            givenPlacesOfDepositsExist()
            givenEditSetRecordIsSuccessfullyUpdated(
              editSetId,
              editSetRecordId,
              editSetRecordFormValuesFromRecord(editSetRecordId)
                .copy(note = "")
            )

            val result: Future[Result] = submitToSaveWhileLoggedIn(editSetId, editSetRecordId, values)

            assertRedirectionToSavePage(result, editSetId, editSetRecordId)

          }

          "the 'custodial history' field is blank" in new Fixture {

            val editSetId = "1"
            val editSetRecordId = "COAL.2022.V1RJW.P"
            val values: Map[String, String] =
              valuesFromRecord(editSetRecordId) ++ Map(FieldNames.custodialHistory -> "")
            val returnedEditSet: EditSet = getExpectedEditSet(editSetId)
            givenEditSetExists(editSetId, returnedEditSet)
            val returnedEditSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
            givenEditSetRecordExists(editSetId, editSetRecordId, returnedEditSetRecord)
            givenPlacesOfDepositsExist()
            givenEditSetRecordIsSuccessfullyUpdated(
              editSetId,
              editSetRecordId,
              editSetRecordFormValuesFromRecord(editSetRecordId)
                .copy(custodialHistory = "")
            )

            val result: Future[Result] = submitToSaveWhileLoggedIn(editSetId, editSetRecordId, values)

            assertRedirectionToSavePage(result, editSetId, editSetRecordId)

          }
          "the 'background' field is blank" in new Fixture {

            val editSetId = "1"
            val editSetRecordId = "COAL.2022.V1RJW.P"
            val values: Map[String, String] =
              valuesFromRecord(editSetRecordId) ++ Map(FieldNames.background -> "")
            val returnedEditSet: EditSet = getExpectedEditSet(editSetId)
            givenEditSetExists(editSetId, returnedEditSet)
            val returnedEditSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
            givenEditSetRecordExists(editSetId, editSetRecordId, returnedEditSetRecord)
            givenPlacesOfDepositsExist()
            givenEditSetRecordIsSuccessfullyUpdated(
              editSetId,
              editSetRecordId,
              editSetRecordFormValuesFromRecord(editSetRecordId)
                .copy(background = "")
            )

            val result: Future[Result] = submitToSaveWhileLoggedIn(editSetId, editSetRecordId, values)

            assertRedirectionToSavePage(result, editSetId, editSetRecordId)

          }
        }
        "multiple creators have been selected" in new Fixture {

          val editSetId = "1"
          val editSetRecordId = "COAL.2022.V1RJW.P"
          val values: Map[String, String] = valuesFromRecord(editSetRecordId) ++ Map(
            s"${FieldNames.creatorIDs}[0]" -> "48N",
            s"${FieldNames.creatorIDs}[1]" -> "46F"
          )
          val returnedEditSet: EditSet = getExpectedEditSet(editSetId)
          givenEditSetExists(editSetId, returnedEditSet)
          val returnedEditSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
          givenEditSetRecordExists(editSetId, editSetRecordId, returnedEditSetRecord)
          givenPlacesOfDepositsExist()
          givenEditSetRecordIsSuccessfullyUpdated(
            editSetId,
            editSetRecordId,
            editSetRecordFormValuesFromRecord(editSetRecordId)
              .copy(creatorIDs = Seq("48N", "46F"))
          )

          val result: Future[Result] = submitToSaveWhileLoggedIn(editSetId, editSetRecordId, values)

          assertRedirectionToSavePage(result, editSetId, editSetRecordId)

        }
        "multiple creators have been selected, as one empty selection" in new Fixture {

          val editSetId = "1"
          val editSetRecordId = "COAL.2022.V1RJW.P"
          val values: Map[String, String] = valuesFromRecord(editSetRecordId) ++ Map(
            s"${FieldNames.creatorIDs}[0]" -> "48N",
            s"${FieldNames.creatorIDs}[1]" -> "46F",
            s"${FieldNames.creatorIDs}[2]" -> ""
          )
          val returnedEditSet: EditSet = getExpectedEditSet(editSetId)
          givenEditSetExists(editSetId, returnedEditSet)
          val returnedEditSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
          givenEditSetRecordExists(editSetId, editSetRecordId, returnedEditSetRecord)
          givenPlacesOfDepositsExist()
          givenEditSetRecordIsSuccessfullyUpdated(
            editSetId,
            editSetRecordId,
            editSetRecordFormValuesFromRecord(editSetRecordId)
              .copy(creatorIDs = Seq("48N", "46F", ""))
          )

          val result: Future[Result] = submitToSaveWhileLoggedIn(editSetId, editSetRecordId, values)

          assertRedirectionToSavePage(result, editSetId, editSetRecordId)

        }
        "multiple creators have been selected, including duplicates" in new Fixture {

          val editSetId = "1"
          val editSetRecordId = "COAL.2022.V1RJW.P"
          val values: Map[String, String] = valuesFromRecord(editSetRecordId) ++ Map(
            s"${FieldNames.creatorIDs}[0]" -> "48N",
            s"${FieldNames.creatorIDs}[1]" -> "46F",
            s"${FieldNames.creatorIDs}[2]" -> "46F"
          )
          val returnedEditSet: EditSet = getExpectedEditSet(editSetId)
          givenEditSetExists(editSetId, returnedEditSet)
          val returnedEditSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
          givenEditSetRecordExists(editSetId, editSetRecordId, returnedEditSetRecord)
          givenPlacesOfDepositsExist()
          givenEditSetRecordIsSuccessfullyUpdated(
            editSetId,
            editSetRecordId,
            editSetRecordFormValuesFromRecord(editSetRecordId)
              .copy(creatorIDs = Seq("48N", "46F", "46F"))
          )

          val result: Future[Result] = submitToSaveWhileLoggedIn(editSetId, editSetRecordId, values)

          assertRedirectionToSavePage(result, editSetId, editSetRecordId)

        }

        "start date has a leading zero" when {
          "in day" in new Fixture {

            val editSetId = "1"
            val editSetRecordId = "COAL.2022.V1RJW.P"
            val values: Map[String, String] =
              valuesFromRecord(editSetRecordId) ++ Map(FieldNames.startDateDay -> "01")
            val returnedEditSet: EditSet = getExpectedEditSet(editSetId)
            givenEditSetExists(editSetId, returnedEditSet)
            val returnedEditSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
            givenEditSetRecordExists(editSetId, editSetRecordId, returnedEditSetRecord)
            givenPlacesOfDepositsExist()
            givenEditSetRecordIsSuccessfullyUpdated(
              editSetId,
              editSetRecordId,
              editSetRecordFormValuesFromRecord(editSetRecordId)
                .copy(startDateDay = "01")
            )

            val result: Future[Result] = submitToSaveWhileLoggedIn(editSetId, editSetRecordId, values)

            assertRedirectionToSavePage(result, editSetId, editSetRecordId)

          }

          "in month" in new Fixture {

            val editSetId = "1"
            val editSetRecordId = "COAL.2022.V1RJW.P"
            val values: Map[String, String] =
              valuesFromRecord(editSetRecordId) ++ Map(FieldNames.startDateMonth -> "01")
            val returnedEditSet: EditSet = getExpectedEditSet(editSetId)
            givenEditSetExists(editSetId, returnedEditSet)
            val returnedEditSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
            givenEditSetRecordExists(editSetId, editSetRecordId, returnedEditSetRecord)
            givenPlacesOfDepositsExist()
            givenEditSetRecordIsSuccessfullyUpdated(
              editSetId,
              editSetRecordId,
              editSetRecordFormValuesFromRecord(editSetRecordId)
                .copy(startDateMonth = "01")
            )

            val result: Future[Result] = submitToSaveWhileLoggedIn(editSetId, editSetRecordId, values)

            assertRedirectionToSavePage(result, editSetId, editSetRecordId)

          }

          "in year" in new Fixture {

            val editSetId = "1"
            val editSetRecordId = "COAL.2022.V1RJW.P"
            val values: Map[String, String] =
              valuesFromRecord(editSetRecordId) ++ Map(FieldNames.startDateYear -> "0962")
            val returnedEditSet: EditSet = getExpectedEditSet(editSetId)
            givenEditSetExists(editSetId, returnedEditSet)
            val returnedEditSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
            givenEditSetRecordExists(editSetId, editSetRecordId, returnedEditSetRecord)
            givenPlacesOfDepositsExist()
            givenEditSetRecordIsSuccessfullyUpdated(
              editSetId,
              editSetRecordId,
              editSetRecordFormValuesFromRecord(editSetRecordId)
                .copy(startDateYear = "0962")
            )

            val result: Future[Result] = submitToSaveWhileLoggedIn(editSetId, editSetRecordId, values)

            assertRedirectionToSavePage(result, editSetId, editSetRecordId)

          }
        }
        "end date has a leading zero" when {
          "in day" in new Fixture {

            val editSetId = "1"
            val editSetRecordId = "COAL.2022.V1RJW.P"
            val values: Map[String, String] =
              valuesFromRecord(editSetRecordId) ++ Map(FieldNames.endDateDay -> "03")
            val returnedEditSet: EditSet = getExpectedEditSet(editSetId)
            givenEditSetExists(editSetId, returnedEditSet)
            val returnedEditSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
            givenEditSetRecordExists(editSetId, editSetRecordId, returnedEditSetRecord)
            givenPlacesOfDepositsExist()
            givenEditSetRecordIsSuccessfullyUpdated(
              editSetId,
              editSetRecordId,
              editSetRecordFormValuesFromRecord(editSetRecordId)
                .copy(endDateDay = "03")
            )

            val result: Future[Result] = submitToSaveWhileLoggedIn(editSetId, editSetRecordId, values)

            assertRedirectionToSavePage(result, editSetId, editSetRecordId)

          }

          "in month" in new Fixture {

            val editSetId = "1"
            val editSetRecordId = "COAL.2022.V1RJW.P"
            val values: Map[String, String] =
              valuesFromRecord(editSetRecordId) ++ Map(FieldNames.endDateMonth -> "01")
            val returnedEditSet: EditSet = getExpectedEditSet(editSetId)
            givenEditSetExists(editSetId, returnedEditSet)
            val returnedEditSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
            givenEditSetRecordExists(editSetId, editSetRecordId, returnedEditSetRecord)
            givenPlacesOfDepositsExist()
            givenEditSetRecordIsSuccessfullyUpdated(
              editSetId,
              editSetRecordId,
              editSetRecordFormValuesFromRecord(editSetRecordId)
                .copy(endDateMonth = "01")
            )

            val result: Future[Result] = submitToSaveWhileLoggedIn(editSetId, editSetRecordId, values)

            assertRedirectionToSavePage(result, editSetId, editSetRecordId)

          }

          "in year" in new Fixture {

            val editSetId = "1"
            val editSetRecordId = "COAL.2022.V1RJW.P"
            val values: Map[String, String] =
              valuesFromRecord(editSetRecordId) ++ Map(
                FieldNames.startDateYear -> "962",
                FieldNames.endDateYear   -> "0962"
              )
            val returnedEditSet: EditSet = getExpectedEditSet(editSetId)
            givenEditSetExists(editSetId, returnedEditSet)
            val returnedEditSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
            givenEditSetRecordExists(editSetId, editSetRecordId, returnedEditSetRecord)
            givenPlacesOfDepositsExist()
            givenEditSetRecordIsSuccessfullyUpdated(
              editSetId,
              editSetRecordId,
              editSetRecordFormValuesFromRecord(editSetRecordId)
                .copy(startDateYear = "962", endDateYear = "0962")
            )

            val result: Future[Result] = submitToSaveWhileLoggedIn(editSetId, editSetRecordId, values)

            assertRedirectionToSavePage(result, editSetId, editSetRecordId)

          }
        }

        "legal status doesn't exist" in new Fixture {

          // TODO: Should this pass? It's consistent with other cases of an unrecognised reference, like place of deposit.

          val editSetId = "1"
          val editSetRecordId = "COAL.2022.V1RJW.P"
          val values: Map[String, String] =
            valuesFromRecord(editSetRecordId) ++ Map(FieldNames.legalStatusID -> "ref.10")
          val returnedEditSet: EditSet = getExpectedEditSet(editSetId)
          givenEditSetExists(editSetId, returnedEditSet)
          val returnedEditSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
          givenEditSetRecordExists(editSetId, editSetRecordId, returnedEditSetRecord)
          givenPlacesOfDepositsExist()
          givenEditSetRecordIsSuccessfullyUpdated(
            editSetId,
            editSetRecordId,
            editSetRecordFormValuesFromRecord(editSetRecordId)
              .copy(legalStatusID = "ref.10")
          )

          val result: Future[Result] = submitToSaveWhileLoggedIn(editSetId, editSetRecordId, values)

          assertRedirectionToSavePage(result, editSetId, editSetRecordId)

        }

      }
    }

    "when the action is to discard all changes" when {

      "successful" when {
        "even if the validation fails" in new Fixture {

          val editSetId = "1"
          val editSetRecordId = "COAL.2022.V1RJW.P"
          val blankCoveringDatesToFailValidation = ""
          val values: Map[String, String] =
            valuesFromRecord(editSetRecordId) ++ Map("coveringDates" -> blankCoveringDatesToFailValidation)
          val returnedEditSet: EditSet = getExpectedEditSet(editSetId)
          givenEditSetExists(editSetId, returnedEditSet)
          val returnedEditSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
          givenEditSetRecordExists(editSetId, editSetRecordId, returnedEditSetRecord)

          val result: Future[Result] = submitToDiscardWhileLoggedIn(editSetId, editSetRecordId, values)

          assertRedirectionToDiscardPage(result, editSetId, editSetRecordId)

        }
      }
    }

    "when the action is to calculate the start and end dates from the covering dates" when {

      "failure" when {
        "blank" in new Fixture {

          val editSetId = "1"
          val editSetRecordId = "COAL.2022.V1RJW.P"
          val values: Map[String, String] =
            valuesFromRecord(editSetRecordId) ++ Map(FieldNames.coveringDates -> "   ")
          val returnedEditSet: EditSet = getExpectedEditSet(editSetId)
          givenEditSetExists(editSetId, returnedEditSet)
          val returnedEditSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
          givenEditSetRecordExists(editSetId, editSetRecordId, returnedEditSetRecord)
          givenLegalStatusesExist()
          givenPlacesOfDepositsExist()
          givenCreatorsExist()
          givenEditViewIsGenerated(returnedEditSetRecord)

          val result: Future[Result] = submitToCalculateDatesWhileLoggedIn(editSetId, editSetRecordId, values)

          status(result) mustBe BAD_REQUEST
          assertEditViewForm(
            returnedEditSetRecord,
            Seq(
              FormError(FieldNames.coveringDates, List(MessageKeys.coveringDatesMissing)),
              FormError(FieldNames.coveringDates, List(MessageKeys.coveringDatesUnparseable))
            )
          )

        }
        "invalid format" in new Fixture {

          val editSetId = "1"
          val editSetRecordId = "COAL.2022.V1RJW.P"
          val values: Map[String, String] =
            valuesFromRecord(editSetRecordId) ++ Map(FieldNames.coveringDates -> "1270s")
          val returnedEditSet: EditSet = getExpectedEditSet(editSetId)
          givenEditSetExists(editSetId, returnedEditSet)
          val returnedEditSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
          givenEditSetRecordExists(editSetId, editSetRecordId, returnedEditSetRecord)
          givenLegalStatusesExist()
          givenPlacesOfDepositsExist()
          givenCreatorsExist()
          givenEditViewIsGenerated(returnedEditSetRecord)

          val result: Future[Result] = submitToCalculateDatesWhileLoggedIn(editSetId, editSetRecordId, values)

          status(result) mustBe BAD_REQUEST
          assertEditViewForm(
            returnedEditSetRecord,
            Seq(FormError(FieldNames.coveringDates, List(MessageKeys.coveringDatesUnparseable)))
          )

        }
        "contains a non-existent date" in new Fixture {

          val editSetId = "1"
          val editSetRecordId = "COAL.2022.V1RJW.P"
          val values: Map[String, String] =
            valuesFromRecord(editSetRecordId) ++ Map(FieldNames.coveringDates -> "2022 Feb 1-2022 Feb 29")
          val returnedEditSet: EditSet = getExpectedEditSet(editSetId)
          givenEditSetExists(editSetId, returnedEditSet)
          val returnedEditSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
          givenEditSetRecordExists(editSetId, editSetRecordId, returnedEditSetRecord)
          givenLegalStatusesExist()
          givenPlacesOfDepositsExist()
          givenCreatorsExist()
          givenEditViewIsGenerated(returnedEditSetRecord)

          val result: Future[Result] = submitToCalculateDatesWhileLoggedIn(editSetId, editSetRecordId, values)

          status(result) mustBe BAD_REQUEST
          assertEditViewForm(
            returnedEditSetRecord,
            Seq(FormError(FieldNames.coveringDates, List(MessageKeys.coveringDatesUnparseable)))
          )
        }
      }
      "successful" when {
        "covers period of the switchover" in new Fixture {

          val editSetId = "1"
          val editSetRecordId = "COAL.2022.V1RJW.P"
          val values: Map[String, String] =
            valuesFromRecord(editSetRecordId) ++ Map(FieldNames.coveringDates -> "1752 Aug 1-1752 Sept 12")
          val returnedEditSet: EditSet = getExpectedEditSet(editSetId)
          givenEditSetExists(editSetId, returnedEditSet)
          val returnedEditSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
          givenEditSetRecordExists(editSetId, editSetRecordId, returnedEditSetRecord)
          givenLegalStatusesExist()
          givenPlacesOfDepositsExist()
          givenCreatorsExist()
          givenEditViewIsGenerated(returnedEditSetRecord)

          val result: Future[Result] = submitToCalculateDatesWhileLoggedIn(editSetId, editSetRecordId, values)

          status(result) mustBe OK
          assertEditViewForm(
            editSetRecordFormValuesFromRecord(editSetRecordId).copy(coveringDates = "1752 Aug 1-1752 Sept 12"),
            returnedEditSetRecord,
            Seq.empty
          )

        }
        "covers period after the switchover" in new Fixture {

          val editSetId = "1"
          val editSetRecordId = "COAL.2022.V1RJW.P"
          val values: Map[String, String] =
            valuesFromRecord(editSetRecordId) ++ Map(FieldNames.coveringDates -> "1984 Dec")
          val returnedEditSet: EditSet = getExpectedEditSet(editSetId)
          givenEditSetExists(editSetId, returnedEditSet)
          val returnedEditSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
          givenEditSetRecordExists(editSetId, editSetRecordId, returnedEditSetRecord)
          givenLegalStatusesExist()
          givenPlacesOfDepositsExist()
          givenCreatorsExist()
          givenEditViewIsGenerated(returnedEditSetRecord)

          val result: Future[Result] = submitToCalculateDatesWhileLoggedIn(editSetId, editSetRecordId, values)

          status(result) mustBe OK
          assertEditViewForm(
            editSetRecordFormValuesFromRecord(editSetRecordId).copy(coveringDates = "1984 Dec"),
            returnedEditSetRecord,
            Seq.empty
          )

        }
        "covers multiple ranges" in new Fixture {

          val editSetId = "1"
          val editSetRecordId = "COAL.2022.V1RJW.P"
          val values: Map[String, String] =
            valuesFromRecord(editSetRecordId) ++ Map(FieldNames.coveringDates -> "1868; 1890-1902; 1933")
          val returnedEditSet: EditSet = getExpectedEditSet(editSetId)
          givenEditSetExists(editSetId, returnedEditSet)
          val returnedEditSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
          givenEditSetRecordExists(editSetId, editSetRecordId, returnedEditSetRecord)
          givenLegalStatusesExist()
          givenPlacesOfDepositsExist()
          givenCreatorsExist()
          givenEditViewIsGenerated(returnedEditSetRecord)

          val result: Future[Result] = submitToCalculateDatesWhileLoggedIn(editSetId, editSetRecordId, values)

          status(result) mustBe OK
          assertEditViewForm(
            editSetRecordFormValuesFromRecord(editSetRecordId).copy(coveringDates = "1868; 1890-1902; 1933"),
            returnedEditSetRecord,
            Seq.empty
          )

        }
      }

    }

    "when the action is to add another selection 'slot' for a creator" when {
      "successful" when {
        "a single creator had been previously assigned and we" when {
          "keep that same selection" in new Fixture {

            val editSetId = "1"
            val editSetRecordId = "COAL.2022.V11RJW.P"
            val editSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
            editSetRecord.creatorIDs mustBe Seq("8R6")
            val values: Map[String, String] =
              valuesFromRecord(editSetRecordId) ++ Map(s"${FieldNames.creatorIDs}[0]" -> "8R6")
            val returnedEditSet: EditSet = getExpectedEditSet(editSetId)
            givenEditSetExists(editSetId, returnedEditSet)
            val returnedEditSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
            givenEditSetRecordExists(editSetId, editSetRecordId, returnedEditSetRecord)
            givenLegalStatusesExist()
            givenPlacesOfDepositsExist()
            givenCreatorsExist()
            givenEditViewIsGenerated(returnedEditSetRecord)

            val result: Future[Result] = submitToAddAnotherCreatorWhileLoggedIn(editSetId, editSetRecordId, values)

            status(result) mustBe OK
            assertEditViewForm(
              editSetRecordFormValuesFromRecord(editSetRecordId).copy(creatorIDs = Seq("8R6")),
              returnedEditSetRecord,
              Seq.empty
            )

          }
          "keep that same selection, but already have an empty slot" in new Fixture {

            val editSetId = "1"
            val editSetRecordId = "COAL.2022.V11RJW.P"
            val editSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
            editSetRecord.creatorIDs mustBe Seq("8R6")
            val values: Map[String, String] = valuesFromRecord(editSetRecordId) ++ Map(
              s"${FieldNames.creatorIDs}[0]" -> "8R6",
              s"${FieldNames.creatorIDs}[1]" -> ""
            )
            val returnedEditSet: EditSet = getExpectedEditSet(editSetId)
            givenEditSetExists(editSetId, returnedEditSet)
            val returnedEditSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
            givenEditSetRecordExists(editSetId, editSetRecordId, returnedEditSetRecord)
            givenLegalStatusesExist()
            givenPlacesOfDepositsExist()
            givenCreatorsExist()
            givenEditViewIsGenerated(returnedEditSetRecord)

            val result: Future[Result] = submitToAddAnotherCreatorWhileLoggedIn(editSetId, editSetRecordId, values)

            status(result) mustBe OK
            assertEditViewForm(
              editSetRecordFormValuesFromRecord(editSetRecordId).copy(creatorIDs = Seq("8R6")),
              returnedEditSetRecord,
              Seq.empty
            )

          }
          "clear that selection" in new Fixture {

            val editSetId = "1"
            val editSetRecordId = "COAL.2022.V11RJW.P"
            val editSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
            editSetRecord.creatorIDs mustBe Seq("8R6")
            val values: Map[String, String] =
              valuesFromRecord(editSetRecordId) ++ Map(s"${FieldNames.creatorIDs}[0]" -> "")
            val returnedEditSet: EditSet = getExpectedEditSet(editSetId)
            givenEditSetExists(editSetId, returnedEditSet)
            val returnedEditSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
            givenEditSetRecordExists(editSetId, editSetRecordId, returnedEditSetRecord)
            givenLegalStatusesExist()
            givenPlacesOfDepositsExist()
            givenCreatorsExist()
            givenEditViewIsGenerated(returnedEditSetRecord)

            val result: Future[Result] = submitToAddAnotherCreatorWhileLoggedIn(editSetId, editSetRecordId, values)

            status(result) mustBe OK
            assertEditViewForm(
              editSetRecordFormValuesFromRecord(editSetRecordId).copy(creatorIDs = Seq("8R6")),
              returnedEditSetRecord,
              Seq.empty
            )

          }
          "change that selection" in new Fixture {

            val editSetId = "1"
            val editSetRecordId = "COAL.2022.V11RJW.P"
            val editSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
            editSetRecord.creatorIDs mustBe Seq("8R6")
            val values: Map[String, String] =
              valuesFromRecord(editSetRecordId) ++ Map(s"${FieldNames.creatorIDs}[0]" -> "92W")
            val returnedEditSet: EditSet = getExpectedEditSet(editSetId)
            givenEditSetExists(editSetId, returnedEditSet)
            val returnedEditSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
            givenEditSetRecordExists(editSetId, editSetRecordId, returnedEditSetRecord)
            givenLegalStatusesExist()
            givenPlacesOfDepositsExist()
            givenCreatorsExist()
            givenEditViewIsGenerated(returnedEditSetRecord)

            val result: Future[Result] = submitToAddAnotherCreatorWhileLoggedIn(editSetId, editSetRecordId, values)

            status(result) mustBe OK
            assertEditViewForm(
              editSetRecordFormValuesFromRecord(editSetRecordId).copy(creatorIDs = Seq("8R6")),
              returnedEditSetRecord,
              Seq.empty
            )

          }
        }
        "the record had multiple creators assigned" when {
          "and we keep those selections" in new Fixture {

            val editSetId = "1"
            val editSetRecordId = "COAL.2022.V7RJW.P"
            val editSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
            editSetRecord.creatorIDs mustBe Seq("48N", "92W")
            val values: Map[String, String] = valuesFromRecord(editSetRecordId) ++ Map(
              s"${FieldNames.creatorIDs}[0]" -> "48N",
              s"${FieldNames.creatorIDs}[1]" -> "92W"
            )
            val returnedEditSet: EditSet = getExpectedEditSet(editSetId)
            givenEditSetExists(editSetId, returnedEditSet)
            val returnedEditSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
            givenEditSetRecordExists(editSetId, editSetRecordId, returnedEditSetRecord)
            givenLegalStatusesExist()
            givenPlacesOfDepositsExist()
            givenCreatorsExist()
            givenEditViewIsGenerated(returnedEditSetRecord)

            val result: Future[Result] = submitToAddAnotherCreatorWhileLoggedIn(editSetId, editSetRecordId, values)

            status(result) mustBe OK
            assertEditViewForm(
              editSetRecordFormValuesFromRecord(editSetRecordId).copy(creatorIDs = Seq("48N", "92W")),
              returnedEditSetRecord,
              Seq.empty
            )

          }
          "(including duplicates) and keep those selections" in new Fixture {

            val editSetId = "1"
            val editSetRecordId = "COAL.2022.V5RJW.P"
            val editSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
            editSetRecord.creatorIDs mustBe Seq("46F", "48N", "46F")
            val values: Map[String, String] = valuesFromRecord(editSetRecordId) ++ Map(
              s"${FieldNames.creatorIDs}[0]" -> "46F",
              s"${FieldNames.creatorIDs}[1]" -> "48N",
              s"${FieldNames.creatorIDs}[2]" -> "46F"
            )
            val returnedEditSet: EditSet = getExpectedEditSet(editSetId)
            givenEditSetExists(editSetId, returnedEditSet)
            val returnedEditSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
            givenEditSetRecordExists(editSetId, editSetRecordId, returnedEditSetRecord)
            givenLegalStatusesExist()
            givenPlacesOfDepositsExist()
            givenCreatorsExist()
            givenEditViewIsGenerated(returnedEditSetRecord)

            val result: Future[Result] = submitToAddAnotherCreatorWhileLoggedIn(editSetId, editSetRecordId, values)

            status(result) mustBe OK
            assertEditViewForm(
              editSetRecordFormValuesFromRecord(editSetRecordId).copy(creatorIDs = Seq("46F", "48N", "46F")),
              returnedEditSetRecord,
              Seq.empty
            )

          }
          "but we change those selections" in new Fixture {

            val editSetId = "1"
            val editSetRecordId = "COAL.2022.V5RJW.P"
            val editSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
            editSetRecord.creatorIDs mustBe Seq("46F", "48N", "46F")
            val values: Map[String, String] = valuesFromRecord(editSetRecordId) ++ Map(
              s"${FieldNames.creatorIDs}[0]" -> "8R6",
              s"${FieldNames.creatorIDs}[1]" -> "92W",
              s"${FieldNames.creatorIDs}[2]" -> "48N"
            )
            val returnedEditSet: EditSet = getExpectedEditSet(editSetId)
            givenEditSetExists(editSetId, returnedEditSet)
            val returnedEditSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
            givenEditSetRecordExists(editSetId, editSetRecordId, returnedEditSetRecord)
            givenLegalStatusesExist()
            givenPlacesOfDepositsExist()
            givenCreatorsExist()
            givenEditViewIsGenerated(returnedEditSetRecord)

            val result: Future[Result] = submitToAddAnotherCreatorWhileLoggedIn(editSetId, editSetRecordId, values)

            status(result) mustBe OK
            assertEditViewForm(
              editSetRecordFormValuesFromRecord(editSetRecordId).copy(creatorIDs = Seq("46F", "48N", "46F")),
              returnedEditSetRecord,
              Seq.empty
            )

          }

        }
      }
    }

    "when the action is to remove the last selection for a creator" when {

      "successful" when {
        "we have two selections and" when {
          "we leave the first selection unchanged" in new Fixture {

            val editSetId = "1"
            val editSetRecordId = "COAL.2022.V1RJW.P"
            val editSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
            editSetRecord.creatorIDs mustBe Seq("48N", "92W")
            val values: Map[String, String] = valuesFromRecord(editSetRecordId) ++ Map(
              s"${FieldNames.creatorIDs}[0]" -> "48N",
              s"${FieldNames.creatorIDs}[1]" -> "8R6"
            )
            val returnedEditSet: EditSet = getExpectedEditSet(editSetId)
            givenEditSetExists(editSetId, returnedEditSet)
            val returnedEditSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
            givenEditSetRecordExists(editSetId, editSetRecordId, returnedEditSetRecord)
            givenLegalStatusesExist()
            givenPlacesOfDepositsExist()
            givenCreatorsExist()
            givenEditViewIsGenerated(returnedEditSetRecord)

            val result: Future[Result] = submitToAddAnotherCreatorWhileLoggedIn(editSetId, editSetRecordId, values)

            status(result) mustBe OK
            assertEditViewForm(
              editSetRecordFormValuesFromRecord(editSetRecordId).copy(creatorIDs = Seq("48N", "92W")),
              returnedEditSetRecord,
              Seq.empty
            )

          }
          "we change the first selection" in new Fixture {

            val editSetId = "1"
            val editSetRecordId = "COAL.2022.V1RJW.P"
            val editSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
            editSetRecord.creatorIDs mustBe Seq("48N", "92W")
            val values: Map[String, String] = valuesFromRecord(editSetRecordId) ++ Map(
              s"${FieldNames.creatorIDs}[0]" -> "46F",
              s"${FieldNames.creatorIDs}[1]" -> "92W"
            )
            val returnedEditSet: EditSet = getExpectedEditSet(editSetId)
            givenEditSetExists(editSetId, returnedEditSet)
            val returnedEditSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
            givenEditSetRecordExists(editSetId, editSetRecordId, returnedEditSetRecord)
            givenLegalStatusesExist()
            givenPlacesOfDepositsExist()
            givenCreatorsExist()
            givenEditViewIsGenerated(returnedEditSetRecord)

            val result: Future[Result] = submitToAddAnotherCreatorWhileLoggedIn(editSetId, editSetRecordId, values)

            status(result) mustBe OK
            assertEditViewForm(
              editSetRecordFormValuesFromRecord(editSetRecordId).copy(creatorIDs = Seq("48N", "92W")),
              returnedEditSetRecord,
              Seq.empty
            )

          }
          "we blank out the first" in new Fixture {

            val editSetId = "1"
            val editSetRecordId = "COAL.2022.V1RJW.P"
            val editSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
            editSetRecord.creatorIDs mustBe Seq("48N", "92W")
            val values: Map[String, String] = valuesFromRecord(editSetRecordId) ++ Map(
              s"${FieldNames.creatorIDs}[0]" -> "",
              s"${FieldNames.creatorIDs}[1]" -> "92W"
            )
            val returnedEditSet: EditSet = getExpectedEditSet(editSetId)
            givenEditSetExists(editSetId, returnedEditSet)
            val returnedEditSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
            givenEditSetRecordExists(editSetId, editSetRecordId, returnedEditSetRecord)
            givenLegalStatusesExist()
            givenPlacesOfDepositsExist()
            givenCreatorsExist()
            givenEditViewIsGenerated(returnedEditSetRecord)

            val result: Future[Result] = submitToRemoveLastCreatorWhileLoggedIn(editSetId, editSetRecordId, values)

            status(result) mustBe OK
            assertEditViewForm(
              editSetRecordFormValuesFromRecord(editSetRecordId).copy(creatorIDs = Seq("", "92W")),
              returnedEditSetRecord,
              Seq.empty
            )

          }

        }
        "we have three selections and" when {
          "we leave the first two selection unchanged" in new Fixture {

            val editSetId = "1"
            val editSetRecordId = "COAL.2022.V8RJW.P"
            val editSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
            editSetRecord.creatorIDs mustBe Seq("48N", "46F", "8R6")
            val values: Map[String, String] = valuesFromRecord(editSetRecordId) ++ Map(
              s"${FieldNames.creatorIDs}[0]" -> "48N",
              s"${FieldNames.creatorIDs}[1]" -> "46F",
              s"${FieldNames.creatorIDs}[2]" -> "92W"
            )
            val returnedEditSet: EditSet = getExpectedEditSet(editSetId)
            givenEditSetExists(editSetId, returnedEditSet)
            val returnedEditSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
            givenEditSetRecordExists(editSetId, editSetRecordId, returnedEditSetRecord)
            givenLegalStatusesExist()
            givenPlacesOfDepositsExist()
            givenCreatorsExist()
            givenEditViewIsGenerated(returnedEditSetRecord)

            val result: Future[Result] = submitToRemoveLastCreatorWhileLoggedIn(editSetId, editSetRecordId, values)

            status(result) mustBe OK
            assertEditViewForm(
              editSetRecordFormValuesFromRecord(editSetRecordId).copy(creatorIDs = Seq("48N", "46F", "92W")),
              returnedEditSetRecord,
              Seq.empty
            )

          }
          "we change the first two selections" in new Fixture {

            val editSetId = "1"
            val editSetRecordId = "COAL.2022.V8RJW.P"
            val editSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
            editSetRecord.creatorIDs mustBe Seq("48N", "46F", "8R6")
            val values: Map[String, String] = valuesFromRecord(editSetRecordId) ++ Map(
              s"${FieldNames.creatorIDs}[0]" -> "92W",
              s"${FieldNames.creatorIDs}[1]" -> "48N",
              s"${FieldNames.creatorIDs}[2]" -> "8R6"
            )
            val returnedEditSet: EditSet = getExpectedEditSet(editSetId)
            givenEditSetExists(editSetId, returnedEditSet)
            val returnedEditSetRecord: EditSetRecord = getExpectedEditSetRecord(editSetRecordId)
            givenEditSetRecordExists(editSetId, editSetRecordId, returnedEditSetRecord)
            givenLegalStatusesExist()
            givenPlacesOfDepositsExist()
            givenCreatorsExist()
            givenEditViewIsGenerated(returnedEditSetRecord)

            val result: Future[Result] = submitToRemoveLastCreatorWhileLoggedIn(editSetId, editSetRecordId, values)

            status(result) mustBe OK
            assertEditViewForm(
              editSetRecordFormValuesFromRecord(editSetRecordId).copy(creatorIDs = Seq("92W", "48N", "8R6")),
              returnedEditSetRecord,
              Seq.empty
            )

          }
        }
      }
    }
  }

  private def valuesFromRecord(editSetRecordId: String): Map[String, String] = {
    val editSetRecord = getExpectedEditSetRecord(editSetRecordId)
    val mapOfCreatorIDs = editSetRecord.creatorIDs.zipWithIndex.map { case (creatorId, index) =>
      val key = s"${FieldNames.creatorIDs}[$index]"
      (key, creatorId)
    }.toMap
    Map(
      FieldNames.background                -> editSetRecord.background,
      FieldNames.coveringDates             -> editSetRecord.coveringDates,
      FieldNames.custodialHistory          -> editSetRecord.custodialHistory,
      FieldNames.endDateDay                -> editSetRecord.endDateDay,
      FieldNames.endDateMonth              -> editSetRecord.endDateMonth,
      FieldNames.endDateYear               -> editSetRecord.endDateYear,
      FieldNames.formerReferenceDepartment -> editSetRecord.formerReferenceDepartment,
      FieldNames.formerReferencePro        -> editSetRecord.formerReferencePro,
      FieldNames.legalStatusID             -> editSetRecord.legalStatusID,
      FieldNames.note                      -> editSetRecord.note,
      FieldNames.oci                       -> editSetRecord.oci,
      FieldNames.placeOfDepositID          -> editSetRecord.placeOfDepositID,
      FieldNames.scopeAndContent           -> editSetRecord.scopeAndContent,
      FieldNames.startDateDay              -> editSetRecord.startDateDay,
      FieldNames.startDateMonth            -> editSetRecord.startDateMonth,
      FieldNames.startDateYear             -> editSetRecord.startDateYear
    ) ++ mapOfCreatorIDs

  }

  private def editSetRecordFormValuesFromRecord(editSetRecordId: String): EditSetRecordFormValues = {
    val editSetRecord = getExpectedEditSetRecord(editSetRecordId)
    EditSetRecordFormValues(
      scopeAndContent = editSetRecord.scopeAndContent,
      coveringDates = editSetRecord.coveringDates,
      formerReferenceDepartment = editSetRecord.formerReferenceDepartment,
      formerReferencePro = editSetRecord.formerReferencePro,
      startDateDay = editSetRecord.startDateDay,
      startDateMonth = editSetRecord.startDateMonth,
      startDateYear = editSetRecord.startDateYear,
      endDateDay = editSetRecord.endDateDay,
      endDateMonth = editSetRecord.endDateMonth,
      endDateYear = editSetRecord.endDateYear,
      legalStatusID = editSetRecord.legalStatusID,
      placeOfDepositID = editSetRecord.placeOfDepositID,
      note = editSetRecord.note,
      background = editSetRecord.background,
      custodialHistory = editSetRecord.custodialHistory,
      creatorIDs = editSetRecord.creatorIDs
    )
  }

  private def viewWhileLoggedIn(editSetId: String, editSetRecordId: String)(implicit
    editSetRecordController: EditSetRecordController
  ): Future[Result] =
    editSetRecordController
      .viewEditRecordForm(editSetId, editSetRecordId)
      .apply(
        CSRFTokenHelper.addCSRFToken(
          FakeRequest()
            .withSession(SessionKeys.token -> validSessionToken)
        )
      )

  private def viewWhileLoggedOut(editSetId: String, editSetRecordId: String)(implicit
    editSetRecordController: EditSetRecordController
  ): Future[Result] =
    editSetRecordController
      .viewEditRecordForm(editSetId, editSetRecordId)
      .apply(FakeRequest())

  private def submitToSaveWhileLoggedIn(
    editSetId: String,
    editSetRecordId: String,
    values: Map[String, String]
  )(implicit editSetRecordController: EditSetRecordController): Future[Result] =
    submitWhileLoggedIn("save", editSetId, editSetRecordId, values)

  private def submitToDiscardWhileLoggedIn(
    editSetId: String,
    editSetRecordId: String,
    values: Map[String, String]
  )(implicit editSetRecordController: EditSetRecordController): Future[Result] =
    submitWhileLoggedIn("discard", editSetId, editSetRecordId, values)

  private def submitToAddAnotherCreatorWhileLoggedIn(
    editSetId: String,
    editSetRecordId: String,
    values: Map[String, String]
  )(implicit editSetRecordController: EditSetRecordController): Future[Result] =
    submitWhileLoggedIn("addAnotherCreator", editSetId, editSetRecordId, values)

  private def submitToRemoveLastCreatorWhileLoggedIn(
    editSetId: String,
    editSetRecordId: String,
    values: Map[String, String]
  )(implicit editSetRecordController: EditSetRecordController): Future[Result] =
    submitWhileLoggedIn("removeLastCreator", editSetId, editSetRecordId, values)

  private def submitToCalculateDatesWhileLoggedIn(
    editSetId: String,
    editSetRecordId: String,
    values: Map[String, String]
  )(implicit editSetRecordController: EditSetRecordController): Future[Result] =
    submitWhileLoggedIn("calculateDates", editSetId, editSetRecordId, values)

  private def submitWhileLoggedIn(
    action: String,
    editSetId: String,
    recordId: String,
    values: Map[String, String]
  )(implicit editSetRecordController: EditSetRecordController): Future[Result] =
    editSetRecordController
      .submit(editSetId, recordId)
      .apply(
        CSRFTokenHelper.addCSRFToken(
          FakeRequest()
            .withFormUrlEncodedBody((values ++ Map("action" -> action)).toSeq: _*)
            .withSession(SessionKeys.token -> validSessionToken)
        )
      )

  private def givenEditSetExists(
    editSetId: String,
    returnedEditSet: EditSet
  )(implicit editSetService: EditSetService): ScalaOngoingStubbing[IO[Option[EditSet]]] =
    when(editSetService.get(editSetId))
      .thenReturn(IO.pure(Option(returnedEditSet)))

  private def givenEditSetRecordExists(
    editSetId: String,
    editSetRecordId: String,
    returnedEditSetRecord: EditSetRecord
  )(implicit editSetRecordService: EditSetRecordService): ScalaOngoingStubbing[IO[Option[EditSetRecord]]] =
    when(editSetRecordService.get(editSetId, editSetRecordId))
      .thenReturn(IO.pure(Option(returnedEditSetRecord)))

  private def givenCreatorIdsArePrepared(expectedEditSetRecord: EditSetRecord)(implicit
    editSetRecordService: EditSetRecordService
  ): ScalaOngoingStubbing[EditSetRecord] =
    when(editSetRecordService.prepareCreatorIDs(expectedEditSetRecord)).thenReturn(expectedEditSetRecord)

  /** The actual list has no relevance to these tests, at least until we validate the legal status ID upon submission.
    */
  private def givenLegalStatusesExist()(implicit
    referenceDataService: ReferenceDataService
  ): ScalaOngoingStubbing[IO[Seq[LegalStatus]]] =
    when(referenceDataService.getLegalStatuses).thenReturn(IO.pure(legalStatuses))

  private def givenPlacesOfDepositsExist()(implicit
    referenceDataService: ReferenceDataService
  ): ScalaOngoingStubbing[IO[Seq[PlaceOfDeposit]]] =
    when(referenceDataService.getPlacesOfDeposit()).thenReturn(IO.pure(placesOfDeposit))

  /** The actual list has no relevance to these tests.
    */
  private def givenCreatorsExist(returnedCreators: Seq[Creator] = Seq.empty)(implicit
    referenceDataService: ReferenceDataService
  ): ScalaOngoingStubbing[Seq[Creator]] =
    when(referenceDataService.getCreators).thenReturn(returnedCreators)

  def givenEditViewIsGenerated(editSetRecord: EditSetRecord)(implicit
    editSetRecordEditView: editSetRecordEdit
  ): ScalaOngoingStubbing[HtmlFormat.Appendable] =
    when(
      editSetRecordEditView.apply(
        user = any[User],
        editSetName = any[String],
        title = any[String],
        record = ArgumentMatchers.eq(editSetRecord),
        legalStatusReferenceData = ArgumentMatchers.eq(legalStatuses),
        placesOfDeposit = ArgumentMatchers.eq(placesOfDeposit),
        creators = ArgumentMatchers.eq(Seq.empty),
        editSetRecordForm = any[Form[EditSetRecordFormValues]]
      )(any[Messages], any[Request[AnyContent]])
    ).thenReturn(HtmlFormat.raw(""))

  def verifyEditViewGeneration(editSetRecord: EditSetRecord)(implicit
    editSetRecordEditView: editSetRecordEdit
  ): Form[EditSetRecordFormValues] = {
    val formCaptor: Captor[Form[EditSetRecordFormValues]] = ArgCaptor[Form[EditSetRecordFormValues]]
    verify(editSetRecordEditView).apply(
      user = any[User],
      editSetName = any[String],
      title = any[String],
      record = ArgumentMatchers.eq(editSetRecord),
      legalStatusReferenceData = ArgumentMatchers.eq(legalStatuses),
      placesOfDeposit = ArgumentMatchers.eq(placesOfDeposit),
      creators = ArgumentMatchers.eq(Seq.empty),
      editSetRecordForm = formCaptor.capture
    )(any[Messages], any[Request[AnyContent]])
    formCaptor.value
  }

  def givenEditSetRecordIsSuccessfullyUpdated(
    editSetId: String,
    editSetRecordId: String,
    editSetRecordFormValues: EditSetRecordFormValues
  )(implicit editSetRecordService: EditSetRecordService): ScalaOngoingStubbing[IO[UpdateResponseStatus]] =
    when(editSetRecordService.updateEditSetRecord(editSetId, editSetRecordId, editSetRecordFormValues))
      .thenReturn(IO.pure(UpdateResponseStatus("success", "")))

  private def assertRedirectionToSavePage(
    result: Future[Result],
    editSetId: String,
    editSetRecordId: String
  ): Assertion = {
    status(result) mustBe SEE_OTHER
    redirectLocation(result) mustBe Some(s"/edit-set/$editSetId/record/$editSetRecordId/edit/save")
  }

  private def assertRedirectionToDiscardPage(
    result: Future[Result],
    editSetId: String,
    editSetRecordId: String
  ): Assertion = {
    status(result) mustBe SEE_OTHER
    redirectLocation(result) mustBe Some(s"/edit-set/$editSetId/record/$editSetRecordId/edit/discard")
  }

  private def assertRedirectionToLoginPage(result: Future[Result]): Assertion = {
    status(result) mustBe SEE_OTHER
    redirectLocation(result) mustBe Some("/login")
  }

  private def assertEditViewForm(
    editSetRecordFormValues: EditSetRecordFormValues,
    editSetRecord: EditSetRecord,
    errors: Seq[FormError]
  )(implicit
    editSetRecordEditView: editSetRecordEdit
  ): Assertion = {
    val viewForm: Form[EditSetRecordFormValues] = verifyEditViewGeneration(editSetRecord)
    viewForm.value mustBe Option(editSetRecordFormValues)
    viewForm.errors mustBe errors
  }

  private def assertEditViewForm(
    editSetRecord: EditSetRecord,
    errors: Seq[FormError]
  )(implicit
    editSetRecordEditView: editSetRecordEdit
  ): Assertion = {
    val viewForm: Form[EditSetRecordFormValues] = verifyEditViewGeneration(editSetRecord)
    viewForm.value mustBe empty
    viewForm.errors mustBe errors
  }

}
