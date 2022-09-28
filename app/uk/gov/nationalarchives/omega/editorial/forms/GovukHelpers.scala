package uk.gov.nationalarchives.omega.editorial.forms
import views.html.helper.FieldConstructor

object GovukHelpers {
  implicit val myFields: FieldConstructor = FieldConstructor(
    uk.gov.nationalarchives.omega.editorial.views.html.govukInput.f
  )
}
