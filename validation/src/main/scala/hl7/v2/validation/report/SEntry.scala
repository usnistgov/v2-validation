/*package hl7.v2.validation.report

import hl7.v2.instance.{EType, Location}
import hl7.v2.profile.Range
import hl7.v2.validation.report.Configurations.{Categories, Classifications, Templates}

/**
  * Trait defining a structure report entry
  *
  * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
  */
trait SEntry extends Entry

sealed trait UsageEntry extends SEntry { def location: Location; def msg: String }

case class RUsage(location: Location) extends UsageEntry {
  override lazy val category: String = Categories.RUsage
  override lazy val classification: String = Classifications.RUsage
  override lazy val msg = String.format(Templates.RUsage, location.prettyString)
}

case class XUsage(location: Location) extends UsageEntry {
  override lazy val category: String = Categories.XUsage
  override lazy val classification: String = Classifications.XUsage
  override lazy val msg = String.format(Templates.XUsage, location.prettyString)
}

case class WUsage(location: Location) extends UsageEntry {
  override lazy val category: String = Categories.WUsage
  override lazy val classification: String = Classifications.WUsage
  override lazy val msg = String.format(Templates.WUsage, location.prettyString)
}

case class REUsage(location: Location) extends SEntry {
  override lazy val category: String = Categories.REUsage
  override lazy val classification: String = Classifications.REUsage
  override lazy val msg = String.format(Templates.REUsage, location.prettyString)
}

case class MinCard(location: Location, instance: Int, range: Range) extends SEntry {
  override lazy val category: String = Categories.MinCard
  override lazy val classification: String = Classifications.MinCard
  override lazy val msg = java.lang.String.format(Templates.MinCard,
    location.prettyString,range.min.toString, range.max, instance.toString)
}

case class MaxCard(location: Location, instance: Int, range: Range) extends SEntry {
  override lazy val category: String = Categories.MaxCard
  override lazy val classification: String = Classifications.MaxCard
  override lazy val msg = String.format(Templates.MaxCard,
    location.prettyString,range.min.toString, range.max, instance.toString)
}

case class Length(location: Location, value: String, range: Range) extends SEntry {
  override lazy val category: String = Categories.Length
  override lazy val classification: String = Classifications.Length
  override lazy val msg = String.format(Templates.Length,
    location.prettyString,range.min.toString, range.max, value)
}

case class Format(location: Location, details: String) extends SEntry {
  override lazy val category: String = Categories.Format
  override lazy val classification: String = Classifications.Format
  override lazy val msg = String.format(Templates.Format, details)
}

case class UnescapedSeparators( location: Location ) extends SEntry {
  override lazy val category: String = Categories.Unescaped
  override lazy val classification: String = Classifications.Unescaped
  override lazy val msg = String.format(Templates.Unescaped, location.prettyString)
}

case class Extra( location: Location ) extends SEntry {
  override lazy val category: String = Categories.Extra
  override lazy val classification: String = Classifications.Extra
  override lazy val msg = String.format(Templates.Extra, location.prettyString)
}

case class UnexpectedLine(override val line: Int, content: String) extends SEntry  {
  val location = Location(EType.Segment, "", content take 3, line, 1)
  override lazy val category: String = Categories.Unexpected
  override lazy val classification: String = Classifications.Unescaped
  override lazy val msg = String.format(Templates.Unexpected, content)
}

case class InvalidLine(override val line: Int, content: String) extends SEntry  {
  val location = Location(EType.Segment, "", "", line, 1)
  override lazy val category: String = Categories.Invalid
  override lazy val classification: String = Classifications.Invalid
  override lazy val msg = String.format(Templates.Invalid, content)
}
*/