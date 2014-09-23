package hl7.v2.validation.structure

import hl7.v2.instance.Location
import hl7.v2.profile.Range

/**
  * Trait representing a structure check report entry
  */
trait Entry

case class RUsage(location: Location) extends Entry

case class XUsage(location: Location) extends Entry

case class WUsage(location: Location) extends Entry

case class MinCard(location: Location, instance: Int, range: Range) extends Entry

case class MaxCard(location: Location, instance: Int, range: Range) extends Entry

case class Length(location: Location, value: String, range: Range) extends Entry

case class UnexpectedLines( list: List[(Int, String)] ) extends Entry

case class InvalidLines( list: List[(Int, String)] ) extends Entry

//case class Extra(location: Location) extends Entry
