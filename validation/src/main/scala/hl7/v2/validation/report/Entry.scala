package hl7.v2.validation.report

import hl7.v2.profile.Range

sealed trait Entry
/**
  * Trait representing a structure check report entry
  */
trait SEntry extends Entry

object SEntry {

  type Location = Any

  case class RUsage(location: Location) extends SEntry

  case class XUsage(location: Location) extends SEntry

  case class WUsage(location: Location) extends SEntry

  case class MinCard(location: Location, instance: Int, range: Range) extends SEntry

  case class MaxCard(location: Location, instance: Int, range: Range) extends SEntry

  case class Length(location: Location, value: String, range: Range) extends SEntry

  case class Table(location: Location, value: String, table: String) extends SEntry

  case class TableNF(location: Location, value: String, table: String) extends SEntry

  case class Extra(location: Location) extends SEntry

  case class Const(location: Location, expected: String, found: String) extends SEntry

  case class Format(location: Location, pattern: String, value: String) extends SEntry
}

/**
  * Trait representing a content check report entry
  */
trait CEntry extends Entry

object CEntry {

  type Location = Any

  case class Success(location: Location, message: String) extends CEntry

  case class Failure(location: Location, message: String, reasons: Seq[String]) extends CEntry

  case class SpecError(location: Location, message: String, details: List[String]) extends CEntry
}