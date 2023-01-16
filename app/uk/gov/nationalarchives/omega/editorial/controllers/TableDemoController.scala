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

package uk.gov.nationalarchives.omega.editorial.controllers

import javax.inject._
import play.api.i18n._
import play.api.mvc._
import play.api.data._
import uk.gov.nationalarchives.omega.editorial.views.html.tableDemo
import uk.gov.nationalarchives.omega.editorial.models.TableValues

/** This controller creates an `Action` to handle HTTP requests to the application's home page.
  */
@Singleton
class TableDemoController @Inject() (
  val messagesControllerComponents: MessagesControllerComponents,
  tableDemo: tableDemo
) extends MessagesAbstractController(messagesControllerComponents) with I18nSupport {

  var tableValues = TableValues(
    headers = List("Name", "Elevation", "Continent", "First Summit"),
    rows = List(
      List("Test2", "6,962 meters", "South America", "1897"),
      List("Aconcagua", "6,961 meters", "South America", "1897"),
      List("Denali", "6,194 meters", "North America", "1913"),
      List("Elbrus", "5,642 meters", "Europe", "1874"),
      List("Everest", "8,850 meters", "Asia", "1953"),
      List("Kilimanjaro", "5,895 meters", "Africa", "1889"),
      List("Puncak Jaya", "4,884 meters", "Australia", "1962"),
      List("Vinson", "4,897 meters", "Antarctica", "1966")
    )
  )

  def table(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Ok(tableDemo(tableValues))
  }

  def sortedTable(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    println(request.body.asFormUrlEncoded)
    val headerName = request.body.asFormUrlEncoded.get("action").head
    val sortedTableValues = tableValues.changeOrdering(headerName).get
    tableValues = sortedTableValues
    Ok(tableDemo(sortedTableValues))
  }

}

