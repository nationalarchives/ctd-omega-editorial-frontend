package controllers

import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.mvc.{ AnyContentAsEmpty, DefaultActionBuilder, DefaultMessagesActionBuilderImpl, DefaultMessagesControllerComponents }
import play.api.test._
import play.api.test.Helpers._
import uk.gov.nationalarchives.omega.editorial.controllers.{ EditSetController }

/** Add your spec here.
  * You can mock out a whole application including requests, plugins etc.
  *
  * For more information, see https://www.playframework.com/documentation/latest/ScalaTestingWithScalaTest
  */
class EditSetControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {

  "EditSetController GET /edit-set/{id}" should {

    "render the edit set page from a new instance of controller" in {
      val messages: Map[String, Map[String, String]] =
        Map("en" -> Map("edit-set.heading" -> "Edit set: COAL 80 Sample"))
      val mockMessagesApi = stubMessagesApi(messages)
      val stub = stubControllerComponents()
      val controller = new EditSetController(
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
      val login = controller.view("1").apply(FakeRequest(GET, "/edit-set/1"))

      status(login) mustBe OK
      contentType(login) mustBe Some("text/html")
      contentAsString(login) must include("Edit set: COAL 80 Sample")
    }

    "render the edit set page from the application" in {
      val controller = inject[EditSetController]
      val login = controller.view("1").apply(FakeRequest(GET, "/edit-set/1"))

      status(login) mustBe OK
      contentType(login) mustBe Some("text/html")
      contentAsString(login) must include("Edit set: COAL 80 Sample")
    }

    "render the edit set page from the router" in {
      val request = FakeRequest(GET, "/edit-set/1")
      val login = route(app, request).get

      status(login) mustBe OK
      contentType(login) mustBe Some("text/html")
      contentAsString(login) must include("Edit set: COAL 80 Sample")
    }
  }

  "EditSetController GET /edit-set/{id}/record/{recordId}/edit" should {

    "render the edit set page from a new instance of controller" in {
      val messages: Map[String, Map[String, String]] =
        Map("en" -> Map("edit-set.record.edit.heading" -> "TNA reference: COAL 80/80/1"))
      val mockMessagesApi = stubMessagesApi(messages)
      val stub = stubControllerComponents()
      val controller = new EditSetController(
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
      val editRecordPage = controller.editRecord("1", "1").apply(FakeRequest(GET, "/edit-set/1/record/1/edit"))

      status(editRecordPage) mustBe OK
      contentType(editRecordPage) mustBe Some("text/html")
      contentAsString(editRecordPage) must include("TNA reference: COAL 80/80/1")
    }

    "render the edit set page from the application" in {
      val controller = inject[EditSetController]
      val editRecordPage = controller.editRecord("1", "1").apply(FakeRequest(GET, "/edit-set/1/record/1/edit"))

      status(editRecordPage) mustBe OK
      contentType(editRecordPage) mustBe Some("text/html")
      contentAsString(editRecordPage) must include("TNA reference: COAL 80/80/1")
    }

    "render the edit set page from the router" in {
      val request = FakeRequest(GET, "/edit-set/1/record/1/edit")
      val editRecordPage = route(app, request).get

      status(editRecordPage) mustBe OK
      contentType(editRecordPage) mustBe Some("text/html")
      contentAsString(editRecordPage) must include("TNA reference: COAL 80/80/1")
    }
  }

}
