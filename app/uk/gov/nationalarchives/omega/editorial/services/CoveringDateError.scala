package uk.gov.nationalarchives.omega.editorial.services

import uk.gov.nationalarchives.omega.editorial.services.{ CoveringDateNode => Node }
import uk.gov.nationalarchives.omega.editorial.models.DateRange

sealed abstract class CoveringDateError

object CoveringDateError {

  type Result[A] = Either[CoveringDateError, A]

  final case class ParseError(msg: String) extends CoveringDateError
  final case class InvalidRange(range: DateRange) extends CoveringDateError
  final case class MultipleErrors(errs: List[CoveringDateError]) extends CoveringDateError

}
