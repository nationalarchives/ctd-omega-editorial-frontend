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

import support.BaseSpec
import uk.gov.nationalarchives.omega.editorial.services.{ Direction, EditSetEntryRowOrder }

class EditSetEntryRowOrderSpec extends BaseSpec {

  "EditSetEntryRowOrder" should {

    "fromNames" should {

      "parse a valid object from string names" in {
        EditSetEntryRowOrder.fromNames("ccr", "ascending") mustBe EditSetEntryRowOrder.CCROrder(Direction.Ascending)
      }

      "default to ccr ascending when given incorrect direction name" in {
        EditSetEntryRowOrder.fromNames("ccr", "Upwards") mustBe EditSetEntryRowOrder.CCROrder(Direction.Ascending)
      }

      "default to ccr ascending when given incorrect field and direction name" in {
        EditSetEntryRowOrder.fromNames("cccr", "Upwards") mustBe EditSetEntryRowOrder.CCROrder(Direction.Ascending)
      }

      "treat field name and direction name case insensitively" in {
        EditSetEntryRowOrder.fromNames("ccr", "Descending") mustBe EditSetEntryRowOrder.CCROrder(Direction.Descending)
      }

    }

  }

}
