package hl7.v2.validation.report

import hl7.v2.instance.Location
import hl7.v2.profile.Range

sealed trait Entry
/**
  * Trait representing a structure check report entry
  */
trait SEntry extends Entry

case class RUsage(location: Location) extends SEntry

case class XUsage(location: Location) extends SEntry

case class WUsage(location: Location) extends SEntry

case class MinCard(location: Location, instance: Int, range: Range) extends SEntry

case class MaxCard(location: Location, instance: Int, range: Range) extends SEntry

case class Length(location: Location, value: String, range: Range) extends SEntry

case class Table(location: Location, value: String, table: String) extends SEntry

case class TableNF(location: Location, value: String, table: String) extends SEntry

//case class Extra(location: Location) extends SEntry

/**
  * Trait representing a content check report entry
  */
trait CEntry extends Entry

case class Success(location: Location, message: String) extends CEntry

case class Failure(location: Location, message: String, reasons: Seq[String]) extends CEntry

case class SpecError(location: Location, message: String, details: List[String]) extends CEntry
