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

package support

import org.jsoup.nodes.Document
import org.scalatest.matchers.{ MatchResult, Matcher }
import uk.gov.nationalarchives.omega.editorial.services.CoveringDateParser

object CustomMatchers {

  def haveHeaderTitle: Matcher[Document] = (document: Document) => {
    val title = "header.title"
    val actualTitle = document.select("div.govuk-header__content").text()
    val errorMessageIfExpected =
      s"The page didn't have a header title of '$title'. The actual title was '$actualTitle'"
    val errorMessageIfNotExpected = s"The page did indeed have a header title of '$title', which was not expected."
    MatchResult(
      actualTitle == title,
      errorMessageIfExpected,
      errorMessageIfNotExpected
    )
  }

  def haveVisibleLogoutLink: Matcher[Document] = (document: Document) => {
    val isVisible = !document.select("div.govuk-header__signout").hasClass("hidden")
    val errorMessageIfExpected = "We expected the logout link to be visible but it was hidden."
    val errorMessageIfNotExpected = s"We expected the logout link to be hidden but it was visible."
    MatchResult(
      isVisible,
      errorMessageIfExpected,
      errorMessageIfNotExpected
    )
  }

  def haveLogoutLink: Matcher[Document] = (document: Document) => {
    val link = "/logout"
    val actualLink = document.select("div.govuk-header__signout > a").attr("href")
    val errorMessageIfExpected = s"We expected the sign out link to be '$link' but it was '$actualLink'"
    val errorMessageIfNotExpected = s"We didn't expect the sign out link to be '$link', but it was."
    MatchResult(
      link == actualLink,
      errorMessageIfExpected,
      errorMessageIfNotExpected
    )
  }

  def haveLogoutLinkLabel: Matcher[Document] = (document: Document) => {
    val label = "header.logout"
    val actualLabel = document.select("div.govuk-header__signout > a").text()
    val errorMessageIfExpected = s"We expected the logout link label to be '$label' but it was '$actualLabel'"
    val errorMessageIfNotExpected = s"We didn't expect the logout link label to be '$label', but it was."
    MatchResult(
      label == actualLabel,
      errorMessageIfExpected,
      errorMessageIfNotExpected
    )
  }

  def parseSuccessfullyAs[A](expected: A): Matcher[Either[CoveringDateParser.ParseError, A]] =
    (parseResult: Either[CoveringDateParser.ParseError, A]) =>
      parseResult match {
        case Right(ok) =>
          MatchResult(
            expected == ok,
            s"Successfully parsed $ok but expected $expected",
            s"Parsed $expected OK"
          )
        case Left(CoveringDateParser.ParseError(msg)) =>
          MatchResult(
            matches = false,
            s"Expected $expected, but failed to parse: $msg",
            ""
          )
      }

}
