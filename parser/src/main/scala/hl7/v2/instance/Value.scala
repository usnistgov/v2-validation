package hl7.v2.instance

import hl7.v2.profile.Primitive

import scala.util.Try

/**
  * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
  */

sealed trait Value {
  def asString: String

  /**
    * Returns true if the value contains unescaped field,
    * component, sub-component or repetition separators
    */
  def isUnescaped: Boolean

  /**
    * Compares this to v and returns :
    *   Success( 1 ) if this is greater that `v'
    *   Success(-1 ) if this is lower that `v'
    *   Success( 0 ) if this is equal to `v'
    *   Failure if `v' cannot be converted to the current Value class or if the comparison is not permitted.
    */
  //TODO: How should we handle comparison with formatted text?
  def compareTo(v: Value): Try[Int] = ??? //TODO To be implemented
}

//TODO: Correctly handle HL7 Null value

case class Number(asString: String, isUnescaped: Boolean = false) extends Value
case class Text(asString: String, isUnescaped: Boolean = false) extends Value
case class Date(asString: String, isUnescaped: Boolean = false) extends Value
case class Time(asString: String, isUnescaped: Boolean = false) extends Value
case class DateTime(asString: String, isUnescaped: Boolean = false) extends Value
case class FormattedText(asString: String, isUnescaped: Boolean = false) extends Value

object Value {

  /**
    * Create the value from string depending on the data type
    */
  def apply(datatype: Primitive, asString: String, isUnescaped: Boolean): Value =
    datatype.name match {
      case "NM"  => Number( asString, isUnescaped )
      case "DT"  => Date( asString, isUnescaped )
      case "TM"  => Time( asString, isUnescaped )
      case "DTM" => DateTime( asString, isUnescaped )
      case "FT"  => FormattedText( asString, isUnescaped )
      case _     => Text( asString, isUnescaped )
    }
}
