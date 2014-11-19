package hl7.v2.instance

import hl7.v2.profile.Primitive

import scala.util.{Failure, Success, Try}

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
  def compareTo(v: Value): Try[Int] = Value.compareTo(this, v)
}

case object Null extends Value { def raw = Value.NULL }
case class Number(raw: String) extends Value
case class Text(raw: String) extends Value
case class Date(raw: String) extends Value
case class Time(raw: String) extends Value
case class DateTime(raw: String) extends Value
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
    if( raw == NULL )
      Null
    else
      datatype.name match {
        case "NM"  => Number( raw )
        //FIXME case "DT"  => Date( raw )
        //FIXME case "TM"  => Time( raw )
        //FIXME case "DTM" => DateTime( raw )
        case "FT"  => FormattedText( raw )
        case _     => Text( raw )
      }

  /*
   A number represented as a series of ASCII numeric characters consisting
   of an optional leading sign (+ or -), the digits and an optional decimal
   point. In the absence of a sign, the number is assumed to be positive.
   If there is no decimal point the number is assumed to be an integer.

   Except for the optional leading sign (+ or -) and the optional decimal
   point (.), no non-numeric ASCII characters are allowed.
  */
  val NMRegex = "(\\+|\\-)\\d+(\\.\\d*)?".r

  def hasValidNMFormat(s: String) = NMRegex.pattern.matcher(s).matches

  def toNMasDouble(v: Value): Try[Double] =
    if( hasValidNMFormat(v.raw) )
      Success( v.raw.toDouble )
    else {
      val msg = v match {
        case Number(_) => s"$v is invalid according to HL7 NM format rules."
        case _ => s"Cannot convert $v to a Number. Its value has an invalid HL7 NM format."
      }
      Failure( new Exception(msg) )
    }


  def compareTo(v1: Value, v2: Value): Try[Int] = (v1, v2) match {
    case(n: Number, v) => toNMasDouble(n) flatMap { d => toNMasDouble(v) map (d compareTo _) }
    case(v, n: Number) => toNMasDouble(v) flatMap { d => toNMasDouble(v) map (d compareTo _) }
    case(x: Date, v)   => ???
    case(v, x: Date)   => ???
    case(x: Time, v)   => ???
    case(v, x: Time)   => ???
    case(x: DateTime, v) => ???
    case(v, x: DateTime) => ???
    case(x1, x2)         => Success( x1.raw compareTo x2.raw  )
  }

}
