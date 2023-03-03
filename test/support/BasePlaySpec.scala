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

///*
// * Copyright (c) 2022 The National Archives
// *
// * Permission is hereby granted, free of charge, to any person obtaining a copy of
// * this software and associated documentation files (the "Software"), to deal in
// * the Software without restriction, including without limitation the rights to
// * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
// * the Software, and to permit persons to whom the Software is furnished to do so,
// * subject to the following conditions:
// *
// * The above copyright notice and this permission notice shall be included in all
// * copies or substantial portions of the Software.
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
// * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
// * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
// * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
// * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
// */

package support

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.scalatest.MockitoSugar
import org.scalatest.{ BeforeAndAfterAll, BeforeAndAfterEach }
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.play.PlaySpec
import play.api.mvc.Results
import play.api.test.Helpers.{ contentAsString, defaultAwaitTimeout }
import play.twirl.api.Content
import uk.gov.nationalarchives.omega.editorial.models.{ EditSet, User }
import uk.gov.nationalarchives.omega.editorial.models.session.Session

class BasePlaySpec
    extends PlaySpec with Results with Matchers with BeforeAndAfterAll with BeforeAndAfterEach with MockitoSugar {

  val messages: Map[String, Map[String, String]] =
    Map(
      "en" -> Map(
        "edit-set.heading"       -> "Edit set: COAL 80 Sample",
        "edit-set.table-caption" -> "Showing {0} - {1} of {2} records"
      )
    )

  val validSessionToken: String = Session.generateToken("1234")
  val mockEditSetView: EditSet = mock[EditSet]
  val user: User = User("dummy user")
  val invalidSessionToken: String = Session.generateToken("invalid-user")

}
