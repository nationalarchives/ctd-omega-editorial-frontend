package uk.gov.nationalarchives.omega.editorial.forms

import play.api.data.Form
import play.api.data.Forms.{ mapping, text }
import play.api.i18n.{ Lang, MessagesApi }
import uk.gov.nationalarchives.omega.editorial.models.Credentials

object CredentialsFormProvider {

  private val editorialUsername = scala.util.Properties.envOrElse("CTD_EDITORIAL_USERNAME", "1234")
  private val editorialPassword = scala.util.Properties.envOrElse("CTD_EDITORIAL_PASSWORD", "1234")

  def apply()(implicit messagesApi: MessagesApi): Form[Credentials] = Form(
    mapping(
      "username" -> text.verifying(messagesApi("login.missing.username")(Lang.apply("en")), _.nonEmpty),
      "password" -> text.verifying(messagesApi("login.missing.password")(Lang.apply("en")), _.nonEmpty)
    )(Credentials.apply)(Credentials.unapply)
      verifying (messagesApi("login.authentication.error")(Lang.apply("en")), credentials =>
        credentials.username == editorialUsername && credentials.password == editorialPassword)
  )
}
