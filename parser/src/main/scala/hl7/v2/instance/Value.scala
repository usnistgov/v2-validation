package hl7.v2.instance

import hl7.v2.profile.Primitive

import scala.util.Try

/**
  * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
  */

sealed trait Value extends EscapeSeqHandler {

  /**
    * The raw string value as defined in the message
    */
  def raw: String

  /**
    * The raw value with escaped separators replaced with their values.
    */
  def unescaped(implicit s: Separators) = unescape( raw )

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

case class Number(raw: String) extends Value
case class Text(raw: String) extends Value
case class Date(raw: String) extends Value
case class Time(raw: String) extends Value
case class DateTime(raw: String) extends Value
case class FormattedText(raw: String) extends Value
case object Null extends Value { val raw = Value.NULL }

object Value {

  val NULL = ""

  /**
    * Create the value from string depending on the data type
    */
  def apply(datatype: Primitive, raw: String): Value =
    if( raw == NULL )
      Null
    else
      datatype.name match {
        case "NM"  => Number( raw )
        case "DT"  => Date( raw )
        case "TM"  => Time( raw )
        case "DTM" => DateTime( raw )
        case "FT"  => FormattedText( raw )
        case _     => Text( raw )
      }
}
