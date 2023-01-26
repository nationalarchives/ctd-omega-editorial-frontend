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

package services

import org.scalatest.OptionValues
import org.scalatest.prop.{ TableDrivenPropertyChecks, Tables }
import support.BaseSpec
import uk.gov.nationalarchives.omega.editorial.services.{ KeyToOrdering, RowOrdering }

class RowOrderingSpec extends BaseSpec with TableDrivenPropertyChecks with OptionValues {
  import RowOrderingSpec._

  "RowOrdering" should {

    val validTestTable = Tables.Table(
      "field name and order direction" -> "expected result",
      ("value1", "Ascending")          -> exampleRows.sortBy(_.value1),
      ("value2", "Ascending")          -> exampleRows.sortBy(_.value2),
      ("value3", "Ascending")          -> exampleRows.sortBy(_.value3),
      ("value1", "Descending")         -> exampleRows.sortWith(_.value1 > _.value1),
      ("value2", "Descending")         -> exampleRows.sortWith(_.value2 > _.value2),
      ("value3", "Descending")         -> exampleRows.sortBy(_.value3)(Ordering[Option[String]].reverse),
      ("value1", "DESCENDING")         -> exampleRows.sortWith(_.value1 > _.value1)
    )

    val invalidTestTable = Tables.Table(
      "field name and order direction" -> "expected result",
      ("value1", "scending")           -> None,
      ("value4", "Ascending")          -> None,
      ("", "Ascending")                -> None
    )

    forAll(validTestTable) { case ((field, direction), result) =>
      s"""sort by "$field", "$direction"""" in {
        val ordering = RowOrdering.fromNames(field, direction).flatMap(_.toOrdering[ExampleTableRow]).value

        exampleRows.sorted(ordering) mustBe result
      }
    }

    forAll(invalidTestTable) { case ((field, direction), result) =>
      s"""fail to sort by "$field", "$direction"""" in {
        val failedOrdering = RowOrdering.fromNames(field, direction).flatMap(_.toOrdering[ExampleTableRow])

        failedOrdering mustBe result
      }
    }

  }

}

object RowOrderingSpec {

  case class ExampleTableRow(value1: String, value2: Int, value3: Option[String])

  object ExampleTableRow {
    implicit val hasOrdering: KeyToOrdering[ExampleTableRow] = KeyToOrdering.byFunction {
      case "value1" => Ordering.by(_.value1)
      case "value2" => Ordering.by(_.value2)
      case "value3" => Ordering.by(_.value3)
    }
  }

  lazy val exampleRows = Seq(
    ExampleTableRow("one", 1, Some("one")),
    ExampleTableRow("two", 2, Some("two")),
    ExampleTableRow("three", 3, Some("three")),
    ExampleTableRow("four", 4, Some("four")),
    ExampleTableRow("five", 5, Some("five")),
    ExampleTableRow("five", 6, Some("five"))
  )

}
