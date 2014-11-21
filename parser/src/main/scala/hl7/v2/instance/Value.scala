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
    * Returns the format error if any
    */
  def formatError: Option[String] = None //No format error by default

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

case object NullNumber extends Null {

  override def compareTo(v: Value): Try[Int] =
    if( v == NullNumber ) Success(0) else Value.asRawNumber(v) map ( x => -1 )
}

case object NullDate extends Null {

  override def compareTo(v: Value): Try[Int] =
    if( v == NullDate ) Success(0) else Value.asRawDate(v) map ( x => -1 )
}

case object NullTime     extends Null
case object NullDateTime extends Null
case object NullText     extends Null

case class Number(raw: String) extends Value {

  override lazy val formatError: Option[String] =
    if( raw matches Value.NMFormat) None
    else Some(s"$this is invalid according to HL7 NM data type format rules")

  override def compareTo(v: Value): Try[Int] =
    formatError match {
      case None => Value.asRawNumber(v) map {raw.toDouble compareTo _.toDouble}
      case Some(m) => Failure( new Exception(m) )
    }
}

case class Date(raw: String) extends Value {

  override lazy val formatError: Option[String] =
    Value.checkDate(raw) map { x => s"$this is invalid. $x." }

  override def compareTo(v: Value): Try[Int] =
    formatError match {
      case None => Value.asRawDate(v) map {raw.toDouble compareTo _.toDouble}
      case Some(m) => Failure( new Exception(m) )
    }
}

case class Time(raw: String) extends Value //FIXME
case class DateTime(raw: String) extends Value //FIXME
case class Text(raw: String) extends Value
case class FormattedText(raw: String) extends Value


/**
  * Value companion object
  */
object Value {

  val NULL = "\"\""

  val NMFormat = "(\\+|\\-)\\d+(\\.\\d*)?"

  val DTFormat = """\d{4}((0[1-9]|1[0-2])(0[1-9]|[1-2][0-9]|3[0-1])?)?"""

  val TMFormat = "" //FIXME

  val DTMFormat = "" //FIXME

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

  /**
    * Checks the date format and allowed values
    * and returns the error message if any
    * @param s - The date as string
    * @return None if valid
    */
  def checkDate(s: String): Option[String] =
    if( s matches DTFormat ) {
      val y = s take 4
      val m = s drop 4 take 2
      val d = s drop 6 take 2
      if( "02" == m ) {
        if( d == "30" || d == "31" ) Some(s"February cannot have $d days")
        else if( d == "29" && y.toInt % 4 != 0 )
          Some(s"February cannot have 29 days since $y is not a leap year")
        else None
      }
      else if( d == "31" && ("04" == m || "06" == m || "09" == m || "11" == m) )
        Some(s"The month $m cannot have 31 days")
      else None
    } else Some("The format is invalid according to HL7 DT data type format")

  /**
    * Attempt to convert the value to a Number
    * @param v - The value to be converted
    * @return Success or a failure if the conversion
    *         is not allowed or the format is invalid
    */
  def asRawNumber(v: Value): Try[String] = v match {
    case x: Number    => Success( x.raw )
    case NullDate     => conversionNotAllowedFailure(v, "Number")
    case NullTime     => conversionNotAllowedFailure(v, "Number")
    case NullDateTime => conversionNotAllowedFailure(v, "Number")
    case x: Date      => conversionNotAllowedFailure(v, "Number")
    case x: Time      => conversionNotAllowedFailure(v, "Number")
    case x: DateTime  => conversionNotAllowedFailure(v, "Number")
    case _ =>
      if( v.raw matches NMFormat ) Success( v.raw )
      else conversionErrFailure(v, "Number", "The format is invalid accord to HL7 NM data type rules.")
  }

  /**
    * Attempt to convert the value to a Date
    * @param v - The value to be converted
    * @return Success or a failure if the conversion
    *         is not allowed or the format is invalid
    */
  def asRawDate(v: Value): Try[String] = v match {
    case x: Date      => Success( x.raw )
    case NullNumber   => conversionNotAllowedFailure(v, "Date")
    case NullTime     => conversionNotAllowedFailure(v, "Date")
    case NullDateTime => conversionNotAllowedFailure(v, "Date")
    case x: Number    => conversionNotAllowedFailure(v, "Date")
    case x: Time      => conversionNotAllowedFailure(v, "Date")
    case x: DateTime  => conversionNotAllowedFailure(v, "Date")
    case _ =>
      checkDate( v.raw ) match {
        case None    => Success( v.raw )
        case Some(m) => conversionErrFailure(v, "Date", m)
      }
  }

  private def conversionNotAllowedFailure(v: Value, t: String) =
    Failure( new Exception(s"$v cannot be converted to a $t") )

  private def conversionErrFailure(v: Value, t: String, m: String) =
    Failure(new Exception(s"Converting $v to a $t yielded an invalid result. $m"))

}
