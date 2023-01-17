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

package uk.gov.nationalarchives.omega.editorial.support

import org.scalatest.prop.TableDrivenPropertyChecks.forAll
import org.scalatest.prop.Tables
import org.scalatest.{ MustMatchers, WordSpec }
import uk.gov.nationalarchives.omega.editorial.models.Creator.{ CreatorTypeCorporateBody, CreatorTypePerson }
import uk.gov.nationalarchives.omega.editorial.models.{ CorporateBody, Creator, Person }

class CreatorSpec extends WordSpec with MustMatchers {

  "attempting to generate a creator from a corporate body" should {
    "succeed" when {
      "all fields are present" in {
        Creator.from(CorporateBody("8WG", "Bee Husbandry Committee", Some(1959), Some(1964))) mustBe Some(
          Creator(
            CreatorTypeCorporateBody,
            "8WG",
            "Bee Husbandry Committee",
            Some(1959),
            Some(1964)
          )
        )

      }
      "only the mandatory fields are present" in {
        Creator.from(CorporateBody("9HC", "Dean of the Chapel Royal", None, None)) mustBe Some(
          Creator(
            CreatorTypeCorporateBody,
            "9HC",
            "Dean of the Chapel Royal",
            None,
            None
          )
        )
      }
    }
    "fail" when {
      "id is empty" in {
        Creator.from(CorporateBody("", "Cattle Emergency Committee", Some(1934), Some(1942))) mustBe empty
      }
      "id is empty (with padding)" in {
        Creator.from(CorporateBody("   ", "Cattle Emergency Committee", Some(1934), Some(1942))) mustBe empty
      }
      "name is empty" in {
        Creator.from(CorporateBody("GW5", "", Some(1934), Some(1942))) mustBe empty
      }
      "name is empty (with padding)" in {
        Creator.from(CorporateBody("GW5", "   ", Some(1934), Some(1942))) mustBe empty
      }
      "both id and name are empty" in {
        Creator.from(CorporateBody("", "", Some(1934), Some(1942))) mustBe empty
      }
    }
  }

  "attempting to generate a creator from a person" should {
    "succeed" when {
      "all fields are present" in {
        Creator.from(
          Person("2QX", "Edward VII", Some("King of Great Britain and Ireland"), Some(1841), Some(1910))
        ) mustBe
          Some(
            Creator(CreatorTypePerson, "2QX", "Edward VII, King of Great Britain and Ireland", Some(1841), Some(1910))
          )
      }
      "all fields are present except a title" in {
        Creator.from(Person("46F", "Fawkes, Guy", None, Some(1570), Some(1606))) mustBe
          Some(Creator(CreatorTypePerson, "46F", "Fawkes, Guy", Some(1570), Some(1606)))
      }
      "only the mandatory fields are present" in {
        Creator.from(Person("4VF", "Old Pretender, The", None, None, None)) mustBe
          Some(Creator(CreatorTypePerson, "4VF", "Old Pretender, The", None, None))
      }
    }
    "fail" when {
      "id is empty" in {
        Creator.from(Person("", "Tate, Sir Henry", Some("1st Baronet"), Some(1819), Some(1899))) mustBe empty
      }
      "id is empty (with padding)" in {
        Creator.from(Person("  ", "Tate, Sir Henry", Some("1st Baronet"), Some(1819), Some(1899))) mustBe empty
      }
      "name is empty" in {
        Creator.from(Person("3QL", "", Some("1st Baronet"), Some(1819), Some(1899))) mustBe empty
      }
      "name is empty (with padding)" in {
        Creator.from(Person("3QL", "  ", Some("1st Baronet"), Some(1819), Some(1899))) mustBe empty
      }
      "both id and name are empty" in {
        Creator.from(Person("", "", Some("1st Baronet"), Some(1819), Some(1899))) mustBe empty
      }
    }
  }

  forAll(
    Tables.Table(
      "Creator" -> "Expected Displayed Name",
      Creator(
        CreatorTypeCorporateBody,
        "RR6",
        "100th (Gordon Highlanders) Regiment of Foot",
        Some(1794),
        Some(1894)
      ) -> "100th (Gordon Highlanders) Regiment of Foot (1794 - 1894)",
      Creator(CreatorTypeCorporateBody, "JS8", "BBC", None, None) -> "BBC",
      Creator(
        CreatorTypeCorporateBody,
        "S2",
        "The National Archives",
        Some(2003),
        None
      ) -> "The National Archives (2003 - )",
      Creator(
        CreatorTypePerson,
        "3FH",
        "Dainton, Sir Frederick Sydney",
        Some(1914),
        Some(1997)
      ) -> "Dainton, Sir Frederick Sydney (b.1914 - d.1997)",
      Creator(
        CreatorTypePerson,
        "39K",
        "Cannon, John Francis Michael",
        Some(1930),
        None
      )                                                                   -> "Cannon, John Francis Michael (b.1930 - )",
      Creator(CreatorTypePerson, "4VF", "Old Pretender, The", None, None) -> "Old Pretender, The",
      Creator(CreatorTypePerson, "XXY", "Jude, the Unborn", None, Some(1970)) -> "Jude, the Unborn ( - d.1970)"
    )
  ) { (creator, expectedDisplayedName) =>
    s"display name for creator '${creator.id}' should be as expected" in {
      creator.displayedName mustBe expectedDisplayedName
    }
  }

}
