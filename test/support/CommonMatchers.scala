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

import org.jsoup.nodes.{ Document, Element }
import org.scalatest.matchers.{ MatchResult, Matcher }
import support.ExpectedValues.{ ExpectedRelatedMaterial, ExpectedSelectOption, ExpectedSeparatedMaterial, ExpectedSummaryErrorMessage }
import uk.gov.nationalarchives.omega.editorial.controllers.EditSetController._
import uk.gov.nationalarchives.omega.editorial.controllers.EditSetRecordController
import uk.gov.nationalarchives.omega.editorial.services.CoveringDateError

import scala.jdk.CollectionConverters._

object CommonMatchers {

  def haveHeaderTitle(expectedValue: String): Matcher[Document] = (document: Document) =>
    singleValueMatcher(
      label = "a header title",
      expectedValue = expectedValue,
      actualValue = document.select("div.govuk-header__content").text()
    )

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

  def haveLogoutLink: Matcher[Document] = (document: Document) =>
    singleValueMatcher(
      label = "the logout link",
      expectedValue = "/logout",
      actualValue = document.select("div.govuk-header__signout > a").attr("href")
    )

  def haveLogoutLinkLabel(expectedValue: String): Matcher[Document] = (document: Document) =>
    singleValueMatcher(
      label = "the logout link label",
      expectedValue = expectedValue,
      actualValue = document.select("div.govuk-header__signout > a").text()
    )

  def haveActionButtons(value: String, label: String, count: Int = 1): Matcher[Document] = (document: Document) => {
    val buttons =
      document
        .select(s"button[name=action][value=$value]")
        .textNodes()
        .asScala
        .map(_.getWholeText.trim)
        .filter(_ == label)
    val actualCount = buttons.size
    val errorMessageIfExpected =
      s"We expected $count action buttons with the value '$value' and label '$label', but there were '$actualCount'."
    val errorMessageIfNotExpected =
      s"We didn't expect $count action buttons with the value '$value' and label '$label', but there were."
    MatchResult(
      actualCount == count,
      errorMessageIfExpected,
      errorMessageIfNotExpected
    )
  }

  def haveLegend(expectedValue: String): Matcher[Document] = (document: Document) =>
    singleValueMatcher(
      label = "the legend",
      expectedValue = expectedValue,
      actualValue = document.select("legend").text()
    )

  def haveTitle(expectedValue: String): Matcher[Document] = (document: Document) =>
    singleValueMatcher(label = "a title", expectedValue = expectedValue, actualValue = document.title())

  def haveHeading(expectedValue: String): Matcher[Document] = (document: Document) =>
    singleValueMatcher(
      label = "a heading",
      expectedValue = expectedValue,
      actualValue = document.select("h1.govuk-heading-l").text()
    )

  def haveHeadingPacId(expectedValue: String): Matcher[Document] = (document: Document) =>
    singleValueMatcher(
      label = "a heading",
      expectedValue = expectedValue,
      actualValue = document.select("h2.govuk-heading-m").text()
    )

  def haveClassicCatalogueRef(expectedValue: String): Matcher[Document] = (document: Document) =>
    singleValueMatcher(
      label = "a classic catalogue ref",
      expectedValue = expectedValue,
      actualValue = document.select(s"#${FieldNames.ccr}").attr("value")
    )

  def haveOmegaCatalogueId(expectedValue: String): Matcher[Document] = (document: Document) =>
    singleValueMatcher(
      label = "an omega catalogue ref",
      expectedValue = expectedValue,
      actualValue = document.select(s"#${EditSetRecordController.FieldNames.oci}").attr("value")
    )

  def haveScopeAndContent(expectedValue: String): Matcher[Document] = (document: Document) =>
    singleValueMatcher(
      label = "a scope & content",
      expectedValue = expectedValue,
      actualValue = document.select(s"#${FieldNames.scopeAndContent}").text()
    )

  def haveCoveringDates(expectedValue: String): Matcher[Document] = (document: Document) =>
    singleValueMatcher(
      label = "covering dates",
      expectedValue = expectedValue,
      actualValue = document.select(s"#${FieldNames.coveringDates}").attr("value")
    )

  def haveFormerReferenceDepartment(expectedValue: String): Matcher[Document] = (document: Document) =>
    singleValueMatcher(
      label = "a former reference department",
      expectedValue = expectedValue,
      actualValue = document.select(s"#${EditSetRecordController.FieldNames.formerReferenceDepartment}").attr("value")
    )

  def haveFormerReferencePro(expectedValue: String): Matcher[Document] = (document: Document) =>
    singleValueMatcher(
      label = "a former reference pro",
      expectedValue = expectedValue,
      actualValue = document.select(s"#${EditSetRecordController.FieldNames.formerReferencePro}").attr("value")
    )

  def haveStartDateDay(expectedValue: String): Matcher[Document] = (document: Document) =>
    singleValueMatcher(
      label = "a start date day",
      expectedValue = expectedValue,
      actualValue = document.select(s"#${EditSetRecordController.FieldNames.startDateDay}").attr("value")
    )

  def haveStartDateMonth(expectedValue: String): Matcher[Document] = (document: Document) =>
    singleValueMatcher(
      label = "a start date month",
      expectedValue = expectedValue,
      actualValue = document.select(s"#${EditSetRecordController.FieldNames.startDateMonth}").attr("value")
    )

  def haveStartDateYear(expectedValue: String): Matcher[Document] = (document: Document) =>
    singleValueMatcher(
      label = "a start date year",
      expectedValue = expectedValue,
      actualValue = document.select(s"#${EditSetRecordController.FieldNames.startDateYear}").attr("value")
    )

  def haveEndDateDay(expectedValue: String): Matcher[Document] = (document: Document) =>
    singleValueMatcher(
      label = "an end date day",
      expectedValue = expectedValue,
      actualValue = document.select(s"#${EditSetRecordController.FieldNames.endDateDay}").attr("value")
    )

  def haveEndDateMonth(expectedValue: String): Matcher[Document] = (document: Document) =>
    singleValueMatcher(
      label = "an end date month",
      expectedValue = expectedValue,
      actualValue = document.select(s"#${EditSetRecordController.FieldNames.endDateMonth}").attr("value")
    )

  def haveEndDateYear(expectedValue: String): Matcher[Document] = (document: Document) =>
    singleValueMatcher(
      label = "an end date year",
      expectedValue = expectedValue,
      actualValue = document.select(s"#${EditSetRecordController.FieldNames.endDateYear}").attr("value")
    )

  def haveRelatedMaterial(relatedMaterials: ExpectedRelatedMaterial*): Matcher[Document] = (document: Document) =>
    if (relatedMaterials.isEmpty)
      singleValueMatcher(
        label = "a single list item for related material with the text None",
        expectedValue = "None",
        actualValue = document.select("#related-material > li").text()
      )
    else
      singleValueMatcher(
        label = "a list of related material",
        expectedValue = relatedMaterials.toSeq,
        actualValue = getRelatedMaterialItems(document)
      )

  def haveSeparatedMaterial(separatedMaterials: ExpectedSeparatedMaterial*): Matcher[Document] = (document: Document) =>
    if (separatedMaterials.isEmpty)
      singleValueMatcher(
        label = "a single list item for separate material with the text None",
        expectedValue = "None",
        actualValue = document.select("#separated-material > li").text()
      )
    else
      singleValueMatcher(
        label = "a list of separated material",
        expectedValue = separatedMaterials.toSeq,
        actualValue = getSeperatedMaterialItems(document)
      )

  def haveSummaryErrorTitle(expectedValue: String): Matcher[Document] = (document: Document) =>
    singleValueMatcher(
      label = "an error summary title",
      expectedValue = expectedValue,
      actualValue = document.select(".govuk-error-summary__title").text()
    )

  def haveUsername(expectedValue: String): Matcher[Document] = (document: Document) =>
    singleValueMatcher(
      label = "a username",
      expectedValue = expectedValue,
      actualValue = document.select("#username").attr("value")
    )

  def havePassword(expectedValue: String): Matcher[Document] = (document: Document) =>
    singleValueMatcher(
      label = "a password",
      expectedValue = expectedValue,
      actualValue = document.select("#password").attr("value")
    )

  def haveAPasswordTypeField(fieldName: String): Matcher[Document] = (document: Document) => {
    val expectedInputType = "password"
    val actualInputType = document.select(s"#$fieldName").attr("type")
    val errorMessageIfExpected =
      s"The page was expected have an input field '$fieldName' with a type of '$expectedInputType'. It was actually '$actualInputType'"
    val errorMessageIfNotExpected =
      s"The page actually had an input field '$fieldName' with a type of '$expectedInputType', which was not expected."
    MatchResult(
      expectedInputType == actualInputType,
      errorMessageIfExpected,
      errorMessageIfNotExpected
    )
  }

  def haveNoSummaryErrorTitle: Matcher[Document] = haveSummaryErrorTitle("")

  def haveSummaryErrorMessages(expectedSummaryErrorMessages: ExpectedSummaryErrorMessage*): Matcher[Document] =
    (document: Document) => {

      def display(summaryErrorMessages: Seq[ExpectedSummaryErrorMessage]) =
        summaryErrorMessages
          .map(summaryErrorMessage => s"[${summaryErrorMessage.message}] / [${summaryErrorMessage.fieldName}]")
          .mkString(",")

      val actualAnchors = document.select("ul.govuk-error-summary__list > li > a").asScala.toSeq
      val actualSummaryErrorMessages = actualAnchors.map(asExpectedSummaryErrorMessage)
      val actualValuesForDisplay = display(actualSummaryErrorMessages)
      val expectedValuesForDisplay = display(expectedSummaryErrorMessages)
      val errorMessageIfExpected =
        s"The page didn't have summary error messages of ('$expectedValuesForDisplay'). The actual messages were ('$actualValuesForDisplay')"
      val errorMessageIfNotExpected =
        s"The page did indeed have a summary error messages of ('$expectedValuesForDisplay'), which was not expected."
      MatchResult(
        actualSummaryErrorMessages == expectedSummaryErrorMessages,
        errorMessageIfExpected,
        errorMessageIfNotExpected
      )
    }

  def haveNoSummaryErrorMessages: Matcher[Document] = haveSummaryErrorMessages()

  def haveErrorMessageForStartDate(expectedValue: String): Matcher[Document] =
    haveErrorMessageForField("start date", s"#${EditSetRecordController.FieldNames.startDateFieldError}", expectedValue)

  def haveNoErrorMessageForStartDate: Matcher[Document] = haveErrorMessageForStartDate("")

  def haveErrorMessageForEndDate(expectedValue: String): Matcher[Document] =
    haveErrorMessageForField("end date", s"#${FieldNames.endDateFieldError}", expectedValue)

  def haveNoErrorMessageForEndDate: Matcher[Document] = haveErrorMessageForEndDate("")

  def haveErrorMessageForCoveringDates(expectedValue: String): Matcher[Document] =
    haveErrorMessageForField("covering dates", s"#${FieldNames.coveringDates}-error", expectedValue)

  def haveNoErrorMessageForCoveringDates: Matcher[Document] = haveErrorMessageForCoveringDates("")

  def haveErrorMessageForLegalStatus(expectedValue: String): Matcher[Document] =
    haveErrorMessageForField(
      "legal status",
      s"#${EditSetRecordController.FieldNames.legalStatusID}-error",
      expectedValue
    )

  def haveNoErrorMessageForLegalStatus: Matcher[Document] = haveErrorMessageForLegalStatus("")

  def haveLegalStatus(expectedValue: String): Matcher[Document] = (document: Document) =>
    singleValueMatcher(
      "a legal status",
      expectedValue,
      document.select(s"#${EditSetRecordController.FieldNames.legalStatusID} option[selected]").attr("value")
    )

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

  def haveCorrectClassesOnAllLabels: Matcher[Document] = (document: Document) =>
    singleValueMatcher(
      "no labels without the 'govuk-label--s' class",
      expectedValue = Seq.empty,
      actualValue = document
        .select(".govuk-fieldset > div > label")
        .asScala
        .toSeq
        .filterNot(_.classNames.contains("govuk-label--s"))
    )

  def haveErrorMessageForUsername(expectedValue: String): Matcher[Document] =
    haveErrorMessageForField("a username", "#username-error", expectedValue)

  def haveNoErrorMessageForUsername: Matcher[Document] = haveErrorMessageForUsername("")

  def haveErrorMessageForPassword(expectedValue: String): Matcher[Document] =
    haveErrorMessageForField("a password", "#password-error", expectedValue)

  def haveNoErrorMessageForPassword: Matcher[Document] = haveErrorMessageForPassword("")

  def haveErrorMessageForScopeAndContent(expectedValue: String): Matcher[Document] =
    haveErrorMessageForField("scope and content", s"#${FieldNames.scopeAndContent}-error", expectedValue)

  def haveErrorMessageForFormerReferenceDepartment(expectedValue: String): Matcher[Document] =
    haveErrorMessageForField(
      "former reference department",
      s"#${EditSetRecordController.FieldNames.formerReferenceDepartment}-error",
      expectedValue
    )

  def haveErrorMessageForFormerReferencePro(expectedValue: String): Matcher[Document] =
    haveErrorMessageForField(
      "former reference pro",
      s"#${EditSetRecordController.FieldNames.formerReferencePro}-error",
      expectedValue
    )

  def haveErrorMessageForPlaceOfDeposit(expectedValue: String): Matcher[Document] =
    haveErrorMessageForField(
      "place of deposit",
      s"#${EditSetRecordController.FieldNames.placeOfDepositID}-error",
      expectedValue
    )

  def haveNoErrorMessageForPlaceOfDeposit: Matcher[Document] = haveErrorMessageForPlaceOfDeposit("")

  def haveHeader(expectedValue: String): Matcher[Document] = (document: Document) =>
    singleValueMatcher("a top level header", expectedValue, document.select("h1").text())

  def haveCaption(expectedValue: String): Matcher[Document] = (document: Document) =>
    singleValueMatcher("a caption", expectedValue, document.select("caption").text())

  def haveSummaryRows(expectedCount: Int): Matcher[Document] = (document: Document) => {
    val actualCount = document.select(s"tbody > tr.govuk-table__row").size()
    val errorMessageIfExpected =
      s"We expected $expectedCount summary rows but there were actually '$actualCount'."
    val errorMessageIfNotExpected = s"We didn't expect $expectedCount summary rows, but there were."
    MatchResult(
      actualCount == expectedCount,
      errorMessageIfExpected,
      errorMessageIfNotExpected
    )
  }

  def haveSummaryRowContents(rowNumber: Int, expectedColumnContents: Seq[String]): Matcher[Document] =
    (document: Document) => {
      val summaryRow = document.select(s"tbody > tr.govuk-table__row").get(rowNumber - 1)
      val columns = summaryRow.select("tr > *")
      val actualColumnContents = columns.eachText().asScala.toList
      val errorMessageIfExpected =
        s"We expected the contents of summary row $rowNumber to be [$expectedColumnContents] but it was actually [$actualColumnContents]."
      val errorMessageIfNotExpected =
        s"We didn't expect the contents of summary row $rowNumber to be [$expectedColumnContents], but it was."
      MatchResult(
        actualColumnContents == expectedColumnContents,
        errorMessageIfExpected,
        errorMessageIfNotExpected
      )
    }

  def haveNumberOfPages(numberOfPages: Int): Matcher[Document] =
    (document: Document) => {
      val numberOfLinks = document.select("li > .govuk-pagination__link").asScala.toSeq.size
      val expectedNumberOfPageLinks = math.min(numberOfPages, 5)
      singleValueMatcher(
        "the number of page links",
        expectedValue = expectedNumberOfPageLinks,
        actualValue = numberOfLinks
      )
    }

  def haveNotificationBannerContents(expectedValues: Seq[String]): Matcher[Document] = (document: Document) => {
    val bannerItems = document.select(s"div.govuk-notification-banner__content > *")
    val actualValues = bannerItems.eachText().asScala.toList
    val errorMessageIfExpected =
      s"We expected the contents of the banner to be [$expectedValues] but it was actually [$actualValues]."
    val errorMessageIfNotExpected = s"We didn't expect the contents of the banner to be [$expectedValues], but it was."
    MatchResult(
      actualValues == expectedValues,
      errorMessageIfExpected,
      errorMessageIfNotExpected
    )
  }

  def haveBackLink(expectedPath: String, expectedLabel: String): Matcher[Document] = (document: Document) => {
    val actualLabel = document.select(s"a.govuk-back-link[href=$expectedPath]").text()
    val errorMessageIfExpected =
      s"We expected a back link with path of '$expectedPath' and label of '$expectedLabel', but there wasn't one."
    val errorMessageIfNotExpected =
      s"We didn't expect a back link with path of '$expectedPath' and label of '$expectedLabel', but there one."
    MatchResult(
      actualLabel == expectedLabel,
      errorMessageIfExpected,
      errorMessageIfNotExpected
    )
  }

  def haveSelectionForPlaceOfDeposit(expectedSelectOptions: Seq[ExpectedSelectOption]): Matcher[Document] =
    haveSelectionOptions(EditSetRecordController.FieldNames.placeOfDepositID, "place of deposit", expectedSelectOptions)

  def haveSelectionForOrderingField(expectedSelectOptions: Seq[ExpectedSelectOption]): Matcher[Document] =
    haveSelectionOptions(fieldKey, "ordering field", expectedSelectOptions)

  def haveSelectionForOrderingDirection(expectedSelectOptions: Seq[ExpectedSelectOption]): Matcher[Document] =
    haveSelectionOptions(orderDirectionKey, "ordering direction", expectedSelectOptions)

  def haveSelectionForCreator(index: Int, expectedSelectOptions: Seq[ExpectedSelectOption]): Matcher[Document] =
    haveSelectionOptions(s"creator-id-$index", s"creator (at index [$index])", expectedSelectOptions)

  def haveNumberOfSelectionsForCreator(expectedNumber: Int): Matcher[Document] = (document: Document) => {
    val actualNumber = document.select("select").asScala.count(_.attr("id").startsWith("creator-id-"))
    val errorMessageIfExpected =
      s"We expected $expectedNumber creator selections, but there were actually $actualNumber."
    val errorMessageIfNotExpected =
      s"We didn't expect $expectedNumber creator selections, but that's what we found"
    MatchResult(
      actualNumber == expectedNumber,
      errorMessageIfExpected,
      errorMessageIfNotExpected
    )
  }

  def haveNote(expectedValue: String): Matcher[Document] = (document: Document) =>
    singleValueMatcher(
      label = "a note",
      expectedValue = expectedValue,
      actualValue = document.select(s"#${EditSetRecordController.FieldNames.note}").text()
    )

  def haveErrorMessageForNote(expectedValue: String): Matcher[Document] =
    haveErrorMessageForField(
      s"${EditSetRecordController.FieldNames.note}",
      s"#${EditSetRecordController.FieldNames.note}-error",
      expectedValue
    )

  def haveNoErrorMessageForNote: Matcher[Document] = haveErrorMessageForNote("")

  def haveBackground(expectedValue: String): Matcher[Document] = (document: Document) =>
    singleValueMatcher(
      label = "a background",
      expectedValue = expectedValue,
      actualValue = document.select(s"#${EditSetRecordController.FieldNames.background}").text()
    )

  def haveErrorMessageForBackground(expectedValue: String): Matcher[Document] =
    haveErrorMessageForField(
      EditSetRecordController.FieldNames.background,
      s"#${EditSetRecordController.FieldNames.background}-error",
      expectedValue
    )

  def haveNoErrorMessageForBackground: Matcher[Document] = haveErrorMessageForBackground("")

  def haveCustodialHistory(expectedValue: String): Matcher[Document] = (document: Document) =>
    singleValueMatcher(
      label = "custodial history",
      expectedValue = expectedValue,
      actualValue = document.select(s"#${EditSetRecordController.FieldNames.custodialHistory}").text()
    )

  def haveErrorMessageForCustodialHistory(expectedValue: String): Matcher[Document] =
    haveErrorMessageForField(
      EditSetRecordController.FieldNames.custodialHistory,
      s"#${EditSetRecordController.FieldNames.custodialHistory}-error",
      expectedValue
    )

  def haveNoErrorMessageForCustodialHistory: Matcher[Document] = haveErrorMessageForCustodialHistory("")

  def haveErrorMessageForCreator(expectedValue: String): Matcher[Document] =
    haveErrorMessageForField(EditSetRecordController.FieldNames.creatorIDs, "#creator-id-0-error", expectedValue)

  def haveNoErrorMessageForCreator: Matcher[Document] = haveErrorMessageForCreator("")

  def haveAllLowerCaseIds: Matcher[Document] = (document: Document) =>
    singleValueMatcher(
      label = "an empty list of invalid ids",
      expectedValue = Seq.empty,
      actualValue = getAllIds(document).filterNot(validW3CIdentifier.matches)
    )

  def haveAllLowerCssClassNames: Matcher[Document] = (document: Document) =>
    singleValueMatcher(
      label = "an empty list of invalid class names",
      expectedValue = Seq.empty,
      actualValue = getAllClassNames(document).filterNot(validW3CIdentifier.matches)
    )

  def haveSectionsInCorrectOrder(sectionTitles: String*): Matcher[Document] = (document: Document) => {
    val inputFields = document.select(".govuk-fieldset > * > label, .govuk-fieldset > label").asScala.toSeq
    val displayFields = document.select(".govuk-fieldset > h3").asScala.toSeq
    singleValueMatcher(
      label = "a list of input sections in the correct order",
      expectedValue = sectionTitles.toSeq,
      actualValue = (inputFields ++ displayFields).map(_.text)
    )
  }

  def haveTableHeaderLink(headerText: String, queryStringKey: String, queryStringValue: String): Matcher[Document] =
    (document: Document) => {
      val queryParameters = document
        .select("th > .govuk-link")
        .asScala
        .toSeq
        .find(_.text == headerText)
        .map { elem =>
          getQueryParameters(elem.attr("href"))
        }
        .getOrElse(Map.empty)
      singleValueMatcher(
        label = s"a header cell for $headerText with a link with a query string $queryStringKey -> $queryStringValue",
        expectedValue = Some(queryStringValue),
        actualValue = queryParameters.get(queryStringKey)
      )
    }

  def haveDirectionInTableHeader(headerText: String, direction: String): Matcher[Document] =
    haveTableHeaderLink(headerText, "direction", direction)

  def haveFieldInTableHeader(headerText: String, field: String): Matcher[Document] =
    haveTableHeaderLink(headerText, "field", field)

  private def asExpectedSummaryErrorMessage(element: Element): ExpectedSummaryErrorMessage =
    ExpectedSummaryErrorMessage(element.text(), element.attr("href").replace("#", ""))

  private def haveSelectionOptions(
    id: String,
    label: String,
    expectedSelectOptions: Seq[ExpectedSelectOption]
  ): Matcher[Document] =
    (document: Document) => {
      val actualSelectOptions = getActualSelectOptions(document, id)
      val expectedSelectOptionsPresented = formatForDisplay(expectedSelectOptions)
      val actualSelectOptionsPresented = formatForDisplay(actualSelectOptions)
      val errorMessageIfExpected =
        s"We expected the '$label' selection to have options of [$expectedSelectOptionsPresented], but they were [$actualSelectOptionsPresented]."
      val errorMessageIfNotExpected =
        s"We didn't expect the '$label' selection to have options of [$expectedSelectOptionsPresented], but they were."
      MatchResult(
        actualSelectOptions == expectedSelectOptions,
        errorMessageIfExpected,
        errorMessageIfNotExpected
      )
    }

  private def formatForDisplay(expectedSelectOptions: Seq[ExpectedSelectOption]): String =
    expectedSelectOptions
      .map(option => s"'${option.value}' -> '${option.label}' (selected: ${option.selected})")
      .mkString(",")

  private def getActualSelectOptions(document: Document, selectionId: String): Seq[ExpectedSelectOption] =
    document
      .select(s"#$selectionId > option")
      .asScala
      .map(option =>
        ExpectedSelectOption(
          option.attr("value"),
          option.text(),
          option.hasAttr("selected"),
          option.hasAttr("disabled")
        )
      )
      .toSeq

  private def singleValueMatcher[T](label: String, expectedValue: T, actualValue: T) = {
    val errorMessageIfExpected =
      s"The page didn't have $label of:\n'$expectedValue'.\nThe actual value was\n'$actualValue'"
    val errorMessageIfNotExpected = s"The page did indeed have $label of '$expectedValue', which was not expected."
    MatchResult(
      actualValue == expectedValue,
      errorMessageIfExpected,
      errorMessageIfNotExpected
    )
  }

  private def getRelatedMaterialItems(document: Document): Seq[ExpectedRelatedMaterial] =
    getMaterialListItems(document, listId = "related-material")
      .map { listItem =>
        ExpectedRelatedMaterial(
          linkHref = noneIfEmpty(listItem.select("a").attr("href")),
          linkText = noneIfEmpty(listItem.select("a").text()),
          description = noneIfEmpty(listItem.select("span").text())
        )
      }

  private def getSeperatedMaterialItems(document: Document): Seq[ExpectedSeparatedMaterial] =
    getMaterialListItems(document, listId = "separated-material")
      .map { listItem =>
        ExpectedSeparatedMaterial(
          linkHref = noneIfEmpty(listItem.select("a").attr("href")),
          linkText = noneIfEmpty(listItem.select("a").text()),
          description = noneIfEmpty(listItem.select("span").text())
        )
      }

  private def getMaterialListItems(document: Document, listId: String): Seq[Element] =
    document
      .select(s"#$listId > li")
      .asScala
      .toSeq

  private def removeInvisibleErrorMessagePrefix(original: String) =
    original
      .replace("Error: ", "")
      .replace("login.hidden.error ", "") // TODO: Remove if LoginViewSpec is removed.

  private def haveErrorMessageForField(
    fieldName: String,
    valueSelector: String,
    expectedValue: String
  ): Matcher[Document] = (document: Document) =>
    singleValueMatcher(
      s"an error message for $fieldName",
      expectedValue,
      removeInvisibleErrorMessagePrefix(document.select(valueSelector).text().replace("Error: ", ""))
    )

  private def noneIfEmpty(input: String): Option[String] =
    if (input.trim.isEmpty)
      None
    else
      Some(input)

  private val validW3CIdentifier = """^[a-z][a-z0-9_!:\.-]*""".r

  private def getAllClassNames(document: Document): Seq[String] =
    document.select("*").asScala.toSeq.flatMap(_.classNames.asScala.toSeq).filter(_.nonEmpty)

  private def getAllIds(document: Document): Seq[String] =
    document.select("*").asScala.toSeq.map(_.id).filter(_.nonEmpty)

  private def getQueryParameters(url: String): Map[String, String] =
    url.split('?') match {
      case Array(_, queryPart) =>
        queryPart
          .split('&')
          .collect { case s"$key=$value" =>
            key -> value
          }
          .toMap
      case _ => Map.empty
    }

}
