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

import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._

sealed abstract class RelatedMaterial

object RelatedMaterial {

  case class LinkAndDescription(linkHref: String, linkText: String, description: String) extends RelatedMaterial
  case class LinkOnly(linkHref: String, linkText: String) extends RelatedMaterial
  case class DescriptionOnly(description: String) extends RelatedMaterial

  val linkHrefPropertyReads: Reads[String] = (__ \ "linkHref").read[String]
  val linkTextPropertyReads: Reads[String] = (__ \ "linkText").read[String]
  val descriptionPropertyReads: Reads[String] = (__ \ "description").read[String]

  val linkAndDescriptionReads: Reads[RelatedMaterial] =
    (linkHrefPropertyReads and linkTextPropertyReads and descriptionPropertyReads)(LinkAndDescription.apply _)

  val linkReads: Reads[RelatedMaterial] =
    (linkHrefPropertyReads and linkTextPropertyReads)(LinkOnly.apply _)

  val descriptionReads: Reads[RelatedMaterial] =
    descriptionPropertyReads.map(DescriptionOnly.apply)

  implicit val relatedMaterialReads: Reads[RelatedMaterial] = linkAndDescriptionReads | linkReads | descriptionReads

}
