package hl7.v2.instance

import hl7.v2.profile.Primitive

import scala.util.{Failure, Success, Try}

/**
  * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
  */

sealed trait Value extends {

  /**
    * The raw string value as defined in the message
    */
  def raw: String

  /**
    * Compares this to v and returns :
    *   Success( 1 ) if this is greater that `v'
    *   Success(-1 ) if this is lower that `v'
    *   Success( 0 ) if this is equal to `v'
    *   Failure if `v' cannot be converted to the current
    *   Value class or if the comparison is not permitted.
    */
  def compareTo(v: Value): Try[Int] = Success( raw compareTo v.raw ) // Textual comparison by default
}

sealed trait Null extends Value { val raw = Value.NULL }
case object NullNumber   extends Null
case object NullDate     extends Null
case object NullTime     extends Null
case object NullDateTime extends Null
case object NullText     extends Null

case class Number(raw: String)        extends Value
case class Date(raw: String)          extends Value
case class Time(raw: String)          extends Value
case class DateTime(raw: String)      extends Value
case class Text(raw: String)          extends Value
case class FormattedText(raw: String) extends Value

/**
  * Value companion object
  */
object Value {

  val NULL = "\"\""

  /**
    * Create the value from string depending on the data type
    */
  def apply(datatype: Primitive, raw: String): Value =
    datatype.name match {
      case "NM"  => if( raw == NULL ) NullNumber   else Number( raw )
      case "DT"  => if( raw == NULL ) NullDate     else Date( raw )
      case "TM"  => if( raw == NULL ) NullTime     else Time( raw )
      case "DTM" => if( raw == NULL ) NullDateTime else DateTime( raw )
      case _ if raw == NULL => NullText
      case "FT"  => FormattedText( raw )
      case _     => Text( raw )
    }
}
