package hl7.v2.validation.report

import hl7.v2.instance.Location
import hl7.v2.profile.Range

case class RUsage(location: Location) extends SEntry

case class XUsage(location: Location) extends SEntry

case class WUsage(location: Location) extends SEntry

case class MinCard(location: Location, instance: Int, range: Range) extends SEntry

case class MaxCard(location: Location, instance: Int, range: Range) extends SEntry

case class Length(location: Location, value: String, range: Range) extends SEntry

case class UnexpectedLines( list: List[(Int, String)] ) extends SEntry

case class InvalidLines( list: List[(Int, String)] ) extends SEntry

case class Extra( location: Location ) extends SEntry

case class UnescapedSeparators( location: Location ) extends SEntry

case class Format(location: Location, details: String) extends SEntry

