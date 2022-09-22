package uk.gov.nationalarchives.omega.editorial.models

case class EditSetEntry(ccr: String, oci: String, scopeAndContent: String, coveringDates: String)
case class EditSet(name: String, id: String, entries: Seq[EditSetEntry])

case class EditSetRecord(
  ccr: String,
  oci: String,
  scopeAndContent: String,
  coveringDates: String,
  formerReferenceDepartment: String,
  startDate: String,
  endDate: String)
