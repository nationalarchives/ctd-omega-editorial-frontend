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
import uk.gov.nationalarchives.omega.editorial.models.Creator
import uk.gov.nationalarchives.omega.editorial.models.Creator.{ CreatorTypeCorporateBody, CreatorTypePerson }

class DisplayedCreatorSpec extends WordSpec with MustMatchers {

  forAll(
    Tables.Table(
      "Creator" -> "Expected Display",
      Creator(CreatorTypeCorporateBody, "RR6", "100th (Gordon Highlanders) Regiment of Foot", Some(1794), Some(1894)) ->
        DisplayedCreator("RR6", "100th (Gordon Highlanders) Regiment of Foot (1794 - 1894)"),
      Creator(CreatorTypeCorporateBody, "JS8", "BBC", None, None) ->
        DisplayedCreator("JS8", "BBC"),
      Creator(CreatorTypeCorporateBody, "S2", "The National Archives", Some(2003), None) ->
        DisplayedCreator("S2", "The National Archives (2003 - )"),
      Creator(CreatorTypePerson, "3FH", "Dainton, Sir Frederick Sydney", Some(1914), Some(1997)) ->
        DisplayedCreator("3FH", "Dainton, Sir Frederick Sydney (b.1914 - d.1997)"),
      Creator(CreatorTypePerson, "39K", "Cannon, John Francis Michael", Some(1930), None) ->
        DisplayedCreator("39K", "Cannon, John Francis Michael (b.1930 - )"),
      Creator(CreatorTypePerson, "4VF", "Old Pretender, The", None, None) ->
        DisplayedCreator("4VF", "Old Pretender, The"),
      Creator(CreatorTypePerson, "XXY", "Jude, the Unborn", None, Some(1970)) ->
        DisplayedCreator("XXY", "Jude, the Unborn ( - d.1970)")
    )
  ) { (creator, expectedDisplayedCreator) =>
    s"convert creator '${creator.id}' to a displayed creator as expected" in {
      DisplayedCreator.fromCreator(creator) mustBe expectedDisplayedCreator
    }

  }
}
