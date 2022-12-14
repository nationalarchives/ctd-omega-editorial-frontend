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

object CustomMatchers {

  def haveHeaderTitle(expectedValue: String): Matcher[Document] = (document: Document) => {
    val actualValue = document.select("div.govuk-header__content").text()
    val errorMessageIfExpected =
      s"The page didn't have a header title of '$expectedValue'. The actual value was '$actualValue'"
    val errorMessageIfNotExpected = s"The page did indeed have a header title of '$expectedValue', which was not expected."
    MatchResult(
      actualValue == expectedValue,
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
    val expectedValue = "/logout"
    val actualValue = document.select("div.govuk-header__signout > a").attr("href")
    val errorMessageIfExpected = s"We expected the sign out link to be '$expectedValue' but it was '$actualValue'"
    val errorMessageIfNotExpected = s"We didn't expect the sign out link to be '$expectedValue', but it was."
    MatchResult(
      expectedValue == actualValue,
      errorMessageIfExpected,
      errorMessageIfNotExpected
    )
  }

  def haveLogoutLinkLabel(expectedValue: String): Matcher[Document] = (document: Document) => {
    val actualValue = document.select("div.govuk-header__signout > a").text()
    val errorMessageIfExpected = s"We expected the logout link label to be '$expectedValue' but it was '$actualValue'"
    val errorMessageIfNotExpected = s"We didn't expect the logout link label to be '$expectedValue', but it was."
    MatchResult(
      expectedValue == actualValue,
      errorMessageIfExpected,
      errorMessageIfNotExpected
    )
  }

  def haveActionButtons(value: String, count: Int): Matcher[Document] = (document: Document) => {
    val buttons = document.select(s"button[name=action][value=$value]")
    val actualCount = buttons.size()
    val errorMessageIfExpected = s"We expected $count action buttons with the value '$value', but there were '$actualCount'."
    val errorMessageIfNotExpected = s"We didn't expect $count action buttons with the value '$value', but there were."
    MatchResult(
      actualCount == count,
      errorMessageIfExpected,
      errorMessageIfNotExpected
    )
  }

  def haveLegend(legend: String): Matcher[Document] = (document: Document) => {
    val actualLegend =  document.select("legend").text()
    val errorMessageIfExpected = s"We expected the legend to be '$legend' but it was actually '$actualLegend'."
    val errorMessageIfNotExpected = s"We didn't expect the legend to be '$legend' but it was."
    MatchResult(
      actualLegend == legend,
      errorMessageIfExpected,
      errorMessageIfNotExpected
    )
  }

  def haveTitle(title: String): Matcher[Document] = (document: Document) => {
    val actualTitle = document.title()
    val errorMessageIfExpected =
      s"The page didn't have a title of '$title'. The actual title was '$actualTitle'"
    val errorMessageIfNotExpected = s"The page did indeed have a title of '$title', which was not expected."
    MatchResult(
      actualTitle == title,
      errorMessageIfExpected,
      errorMessageIfNotExpected
    )
  }

  def haveHeading(heading: String): Matcher[Document] = (document: Document) => {
    val actualHeading = document.select("h1.govuk-heading-l").text()
    val errorMessageIfExpected =
      s"The page didn't have a heading of '$heading'. The actual heading was '$actualHeading'"
    val errorMessageIfNotExpected = s"The page did indeed have a heading of '$heading', which was not expected."
    MatchResult(
      actualHeading == heading,
      errorMessageIfExpected,
      errorMessageIfNotExpected
    )
  }

  def haveNotificationBannerWithSubheading(subheading: String): Matcher[Document] = (document: Document) => {
    val actualSubheading = document.select("h2.govuk-notification-banner-title").text()
    val errorMessageIfExpected =
      s"The page didn't have a heading of '$subheading'. The actual heading was '$actualSubheading'"
    val errorMessageIfNotExpected = s"The page did indeed have a heading of '$subheading', which was not expected."
    MatchResult(
      actualSubheading == subheading,
      errorMessageIfExpected,
      errorMessageIfNotExpected
    )
  }

  def haveClassicCatalogueRef(classicCatalogueRef: String): Matcher[Document] = (document: Document) => {
    val actualClassicCatalogueRef = document.select("#ccr").attr("value")
    val errorMessageIfExpected =
      s"The page didn't have a classic catalogue ref of '$classicCatalogueRef'. The actual value was '$actualClassicCatalogueRef'"
    val errorMessageIfNotExpected =
      s"The page did indeed have a classic catalogue ref of '$classicCatalogueRef', which was not expected."
    MatchResult(
      actualClassicCatalogueRef == classicCatalogueRef,
      errorMessageIfExpected,
      errorMessageIfNotExpected
    )
  }

  def haveOmegaCatalogueId(expectedValue: String): Matcher[Document] = (document: Document) => {
    val actualValue = document.select("#ocr").attr("value")
    val errorMessageIfExpected =
      s"The page didn't have an omega catalogue ref of '$expectedValue'. The actual value was '$actualValue'"
    val errorMessageIfNotExpected = s"The page did indeed have an omega catalogue ref of '$expectedValue', which was not expected."
    MatchResult(
      actualValue == expectedValue,
      errorMessageIfExpected,
      errorMessageIfNotExpected
    )
  }

  def haveScopeAndContent(expectedValue: String): Matcher[Document] = (document: Document) => {
    val actualValue = document.select("#scopeAndContent").text()
    val errorMessageIfExpected =
      s"The page didn't have a scope & content of '$expectedValue'. The actual value was '$actualValue'"
    val errorMessageIfNotExpected = s"The page did indeed have a scope & content of '$expectedValue', which was not expected."
    MatchResult(
      actualValue == expectedValue,
      errorMessageIfExpected,
      errorMessageIfNotExpected
    )
  }

  def haveCoveringDates(expectedValue: String): Matcher[Document] = (document: Document) => {
    val actualValue = document.select("#coveringDates").attr("value")
    val errorMessageIfExpected =
      s"The page didn't have covering dates of '$expectedValue'. The actual value was '$actualValue'"
    val errorMessageIfNotExpected = s"The page did indeed have covering dates of '$expectedValue', which was not expected."
    MatchResult(
      actualValue == expectedValue,
      errorMessageIfExpected,
      errorMessageIfNotExpected
    )
  }

  def haveFormerReferenceDepartment(expectedValue: String): Matcher[Document] = (document: Document) => {
    val actualValue = document.select("#formerReferenceDepartment").attr("value")
    val errorMessageIfExpected =
      s"The page didn't have a former reference department of '$expectedValue'. The actual value was '$actualValue'"
    val errorMessageIfNotExpected = s"The page did indeed have covering dates of '$expectedValue', which was not expected."
    MatchResult(
      actualValue == expectedValue,
      errorMessageIfExpected,
      errorMessageIfNotExpected
    )
  }

  def haveStartDate(expectedValue: String): Matcher[Document] = (document: Document) => {
    val actualValue = document.select("#startDate").attr("value")
    val errorMessageIfExpected =
      s"The page didn't have a start date of '$expectedValue'. The actual value was '$actualValue'"
    val errorMessageIfNotExpected = s"The page did indeed have a start date of '$expectedValue', which was not expected."
    MatchResult(
      actualValue == expectedValue,
      errorMessageIfExpected,
      errorMessageIfNotExpected
    )
  }

  def haveEndDate(expectedValue: String): Matcher[Document] = (document: Document) => {
    val actualValue = document.select("#endDate").attr("value")
    val errorMessageIfExpected =
      s"The page didn't have an end date of '$expectedValue'. The actual value was '$actualValue'"
    val errorMessageIfNotExpected = s"The page did indeed have a start date of '$expectedValue', which was not expected."
    MatchResult(
      actualValue == expectedValue,
      errorMessageIfExpected,
      errorMessageIfNotExpected
    )
  }
}
