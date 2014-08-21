package hl7.v2.validation.content

import hl7.v2.instance.Location

/**
  * Trait representing a content check report entry
  */
trait Entry

case class Success(location: Location, message: String) extends Entry

case class Failure(location: Location, message: String, reasons: Seq[String]) extends Entry

case class SpecError(location: Location, message: String, details: List[String]) extends Entry
