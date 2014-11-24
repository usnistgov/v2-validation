package hl7.v2.instance

import hl7.v2.profile.Primitive
import Value._

import scala.util.{Success, Failure, Try}

/**
  * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
  */

sealed trait Value extends {

  /**
    * The raw string value as defined in the message
    */
  def raw: String

  /**
    * Returns true if the value is equal to HL7 Null ("")
    */
  def isNull: Boolean = raw == Value.NULL

}

case class Number(raw: String) extends Value
case class Date(raw: String) extends Value
case class Time(raw: String) extends Value
case class DateTime(raw: String) extends Value
case class Text(raw: String) extends Value
case class FText(raw: String) extends Value


case class TimeZone(raw: String)

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
      case "NM" => Number(raw)
      case "DT" => Date(raw)
      case "TM" => Time(raw)
      case "DTM"=> DateTime(raw)
      case "FT" => FText(raw)
      case _    => Text(raw)
    }

  def splitOnTZ(s: String): (String, String) = s span( c => c != '+' && c != '-' )
}
