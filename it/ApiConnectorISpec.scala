import cats.effect.unsafe.implicits.global
import cats.implicits._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{ Application, inject }

import java.time.{ LocalDateTime, Month }

import support.TestReferenceDataService
import uk.gov.nationalarchives.omega.editorial.connectors.ApiConnector
import uk.gov.nationalarchives.omega.editorial.editSets
import uk.gov.nationalarchives.omega.editorial.services.ReferenceDataService
import uk.gov.nationalarchives.omega.editorial.support.TimeProvider

class ApiConnectorISpec extends BaseISpec {

  private lazy val testTimeProvider: TimeProvider = () => LocalDateTime.of(2023, Month.FEBRUARY, 28, 1, 1, 1)
  private val idOfExistingEditSet = "1"

  override def fakeApplication(): Application =
    GuiceApplicationBuilder()
      .bindings(inject.bind[ReferenceDataService].to[TestReferenceDataService])
      .overrides(inject.bind[TimeProvider].toInstance(testTimeProvider))
      .build()

  lazy val apiConnector: ApiConnector = app.injector.instanceOf[ApiConnector]

  "when a request for an edit set is made" must {
    s"get an edit set for id: $idOfExistingEditSet" in {
      val editSetResponse = apiConnector.getEditSet(idOfExistingEditSet).unsafeRunSync()

      editSetResponse mustBe editSets.editSet1
    }

    s"get the edit set twice for id: $idOfExistingEditSet" in {
      val action = for {
        editSetResponse1 <- apiConnector.getEditSet(idOfExistingEditSet)
        editSetResponse2 <- apiConnector.getEditSet(idOfExistingEditSet)
      } yield (editSetResponse1, editSetResponse2)

      val (editSetResponse1, editSetResponse2) = action.unsafeRunSync()

      editSetResponse1 mustBe editSets.editSet1
      editSetResponse2 mustBe editSets.editSet1
    }

    s"get the edit set 100 times for id: $idOfExistingEditSet" in {
      val action = (1 to 100).toList.traverse(_ => apiConnector.getEditSet(idOfExistingEditSet))

      action.unsafeRunSync().foreach(_ mustBe editSets.editSet1)
    }
  }

}
