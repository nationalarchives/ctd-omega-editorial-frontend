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

package uk.gov.nationalarchives.omega.editorial.models

import play.api.libs.functional.syntax._
import play.api.libs.json._

sealed abstract class MaterialReference

object MaterialReference {

  case class LinkAndDescription(linkHref: String, linkText: String, description: String) extends MaterialReference

  case class LinkOnly(linkHref: String, linkText: String) extends MaterialReference

  case class DescriptionOnly(description: String) extends MaterialReference

  implicit val format: Format[MaterialReference] = new Format[MaterialReference] {
    override def writes(material: MaterialReference): JsValue = material match {
      case linkAndDescription: LinkAndDescription => Json.toJson(linkAndDescription)(Json.writes[LinkAndDescription])
      case linkOnly: LinkOnly                     => Json.toJson(linkOnly)(Json.writes[LinkOnly])
      case descriptionOnly: DescriptionOnly       => Json.toJson(descriptionOnly)(Json.writes[DescriptionOnly])
    }

    override def reads(json: JsValue): JsResult[MaterialReference] =
      linkAndDescriptionFormat.reads(json) | linkOnlyFormat.reads(json) | descriptionOnlyFormat.reads(json)
  }

  private val linkAndDescriptionFormat: Format[LinkAndDescription] = Json.format[LinkAndDescription]

  private val linkOnlyFormat: Format[LinkOnly] = Json.format[LinkOnly]

  private val descriptionOnlyFormat: Format[DescriptionOnly] = Json.format[DescriptionOnly]

}
