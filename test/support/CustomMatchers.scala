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
import org.scalatest.matchers.{MatchResult, Matcher}
import uk.gov.nationalarchives.omega.editorial.services.CoveringDateError

import scala.jdk.CollectionConverters._

object CustomMatchers {

  def haveHeaderTitle(expectedValue: String): Matcher[Document] = (document: Document) => {
    singleValueMatcher(
      label = "a header title",
      expectedValue = expectedValue,
      actualValue = document.select("div.govuk-header__content").text())
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
    singleValueMatcher(
      label = "the logout link",
      expectedValue = "/logout",
      actualValue = document.select("div.govuk-header__signout > a").attr("href"))
  }

  def haveLogoutLinkLabel(expectedValue: String): Matcher[Document] = (document: Document) => {
    singleValueMatcher(
      label = "the logout link label",
      expectedValue = expectedValue,
      actualValue = document.select("div.govuk-header__signout > a").text())
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

  def haveLegend(expectedValue: String): Matcher[Document] = (document: Document) => {
    singleValueMatcher(
      label = "the legend",
      expectedValue = expectedValue,
      actualValue = document.select("legend").text())
  }

  def haveTitle(expectedValue: String): Matcher[Document] = (document: Document) => {
    singleValueMatcher(
      label = "a title",
      expectedValue = expectedValue,
      actualValue = document.title())
  }

  def haveHeading(expectedValue: String): Matcher[Document] = (document: Document) => {
    singleValueMatcher(
      label = "a heading",
      expectedValue = expectedValue,
      actualValue = document.select("h1.govuk-heading-l").text())
  }

  def haveClassicCatalogueRef(expectedValue: String): Matcher[Document] = (document: Document) => {
    singleValueMatcher(
      label = "a classic catalogue ref",
      expectedValue = expectedValue,
      actualValue = document.select("#ccr").attr("value"))
  }

  def haveOmegaCatalogueId(expectedValue: String): Matcher[Document] = (document: Document) => {
    singleValueMatcher(
      label = "n omega catalogue ref",
      expectedValue = expectedValue,
      actualValue = document.select("#ocr").attr("value"))
  }

  def haveScopeAndContent(expectedValue: String): Matcher[Document] = (document: Document) => {
    singleValueMatcher(
      label = "a scope & content",
      expectedValue = expectedValue,
      actualValue = document.select("#scopeAndContent").text())
  }

  def haveCoveringDates(expectedValue: String): Matcher[Document] = (document: Document) => {
    singleValueMatcher(
      label = "covering dates",
      expectedValue = expectedValue,
      actualValue = document.select("#coveringDates").attr("value"))
  }

  def haveFormerReferenceDepartment(expectedValue: String): Matcher[Document] = (document: Document) => {
    singleValueMatcher(
      label = "a former reference department",
      expectedValue = expectedValue,
      actualValue = document.select("#formerReferenceDepartment").attr("value"))
  }

  def haveStartDateDay(expectedValue: String): Matcher[Document] = (document: Document) => {
    singleValueMatcher(
      label = "a start date day",
      expectedValue = expectedValue,
      actualValue = document.select("#startDateDay").attr("value"))
  }

  def haveStartDateMonth(expectedValue: String): Matcher[Document] = (document: Document) => {
    singleValueMatcher(
      label = "a start date month",
      expectedValue = expectedValue,
      actualValue = document.select("#startDateMonth").attr("value"))
  }

  def haveStartDateYear(expectedValue: String): Matcher[Document] = (document: Document) => {
    singleValueMatcher(
      label = "a start date year",
      expectedValue = expectedValue,
      actualValue = document.select("#startDateYear").attr("value"))
  }

  def haveEndDateDay(expectedValue: String): Matcher[Document] = (document: Document) => {
    singleValueMatcher(
      label = "an end date day",
      expectedValue = expectedValue,
      actualValue = document.select("#endDateDay").attr("value"))
  }

  def haveEndDateMonth(expectedValue: String): Matcher[Document] = (document: Document) => {
    singleValueMatcher(
      label = "an end date month",
      expectedValue = expectedValue,
      actualValue = document.select("#endDateMonth").attr("value"))
  }

  def haveEndDateYear(expectedValue: String): Matcher[Document] = (document: Document) => {
    singleValueMatcher(
      label = "an end date year",
      expectedValue = expectedValue,
      actualValue = document.select("#endDateYear").attr("value"))
  }

  def haveSummaryErrorMessages(expectedValues: Set[String]): Matcher[Document] = (document: Document) => {
    val actualValues = document.select("ul.govuk-error-summary__list > li").eachText().asScala.toSet
    val actualValuesForDisplay = actualValues.mkString(",")
    val expectedValuesForDisplay = expectedValues.mkString(",")
    val errorMessageIfExpected =
      s"The page didn't have summary error messages of ('$expectedValuesForDisplay'). The actual messages were ('$actualValuesForDisplay')"
    val errorMessageIfNotExpected = s"The page did indeed have a summary error messages of ('$expectedValuesForDisplay'), which was not expected."
    MatchResult(
      actualValues == expectedValues,
      errorMessageIfExpected,
      errorMessageIfNotExpected
    )
  }

  def haveErrorMessageForStartDate(expectedValue: String): Matcher[Document] = (document: Document) => {
    val actualValue = document.select("#startDateFieldError").text()
    val errorMessageIfExpected =
      s"The page didn't have an error message of start date of '$expectedValue'. The actual messages were '$actualValue'"
    val errorMessageIfNotExpected = s"The page did indeed have an error message of start date of '$expectedValue', which was not expected."
    MatchResult(
      actualValue == expectedValue,
      errorMessageIfExpected,
      errorMessageIfNotExpected
    )
  }

  def haveNoErrorMessageForStartDate: Matcher[Document] = haveErrorMessageForStartDate("")

  def haveErrorMessageForEndDate(expectedValue: String): Matcher[Document] = (document: Document) => {
    val actualValue = document.select("#endDateFieldError").text()
    val errorMessageIfExpected =
      s"The page didn't have an error message of end date of '$expectedValue'. The actual messages were '$actualValue'"
    val errorMessageIfNotExpected = s"The page did indeed have an error message of end date of '$expectedValue', which was not expected."
    MatchResult(
      actualValue == expectedValue,
      errorMessageIfExpected,
      errorMessageIfNotExpected
    )
  }

  def haveNoErrorMessageForEndDate: Matcher[Document] = haveErrorMessageForEndDate("")

  private def singleValueMatcher[T](label: String, expectedValue: T, actualValue: T) = {
    val errorMessageIfExpected =
      s"The page didn't have $label of '$expectedValue'. The actual value was '$actualValue'"
    val errorMessageIfNotExpected = s"The page did indeed have $label of '$expectedValue', which was not expected."
    MatchResult(
      actualValue == expectedValue,
      errorMessageIfExpected,
      errorMessageIfNotExpected
    )
  }

  def parseSuccessfullyAs[A](expected: A): Matcher[CoveringDateError.Result[A]] = {
    case Right(ok) =>
      MatchResult(
        expected == ok,
        s"""Parsed OK but no match:
           |got:      $ok
           |expected: $expected""".stripMargin,
        s"Parsed $expected OK"
      )
    case Left(err) =>
      MatchResult(
        matches = false,
        s"Expected $expected, but got error: $err",
        ""
      )
  }

  def failToParseAs(expectedError: CoveringDateError): Matcher[CoveringDateError.Result[_]] = {
    case Right(ok) =>
      MatchResult(
        matches = false,
        s"Expected to fail as $expectedError, but passed instead with: $ok",
        ""
      )
    case Left(err) =>
      MatchResult(
        expectedError == err,
        s"""Failed but no match:
           |got:      $err
           |expected: $expectedError""".stripMargin,
        s"Failed with $expectedError as expected"
      )
  }

}
