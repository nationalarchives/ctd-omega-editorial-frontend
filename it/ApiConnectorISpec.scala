import cats.effect.unsafe.implicits.global
import cats.implicits._

import uk.gov.nationalarchives.omega.editorial.connectors.ApiConnector
import uk.gov.nationalarchives.omega.editorial.editSets

class ApiConnectorISpec extends BaseISpec {

  private val idOfExistingEditSet = "1"

  lazy val apiConnector: ApiConnector = app.injector.instanceOf[ApiConnector]

  "when a request for an edit set is made" must {
    s"get an edit set for id: $idOfExistingEditSet" in {
      val editSetResponse = apiConnector.getEditSet(idOfExistingEditSet).unsafeRunSync()

      editSetResponse mustBe editSets.editSet1
    }

    s"get the edit set 100 times for id: $idOfExistingEditSet" in {
      val action = (1 to 100).toList.traverse(_ => apiConnector.getEditSet(idOfExistingEditSet))

      action.unsafeRunSync().foreach(_ mustBe editSets.editSet1)
    }
  }

}
