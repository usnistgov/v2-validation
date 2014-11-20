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
  def formatError: Option[String]

  /**
    * Compares this to v and returns :
    *   Success( 1 ) if this is greater that `v'
    *   Success(-1 ) if this is lower that `v'
    *   Success( 0 ) if this is equal to `v'
    *   Failure if `v' cannot be converted to the current
    *   Value class or if the comparison is not permitted.
    */
  def compareTo(v: Value): Try[Int]
}

/**
  * Object representing an HL7 Null
  */
case object Null extends Value {

  def raw = Value.NULL

  val formatError = None

  override def compareTo(v: Value): Try[Int] = v match {
    case Null => Success( 0 )
    case _    => Success( raw compareTo v.raw )
  }
}

/**
  * Class representing a number
  */
case class Number(raw: String) extends Value {

  lazy val formatError: Option[String] =
    if( raw matches Value.NMFormat) None
    else Some(s"$this is invalid according to HL7 NM data type format rules")

  override def compareTo(v: Value): Try[Int] = formatError match {
    case Some(m) => Failure( new Exception(m) )
    case None    =>
      v match {
        case x: Number =>
          x.formatError match {
            case None    => Success( raw.toDouble compareTo x.raw.toDouble )
            case Some(m) => Failure( new Exception(m) )
          }
        case x: Text          => convertAndCompare( v )
        case x: FormattedText => convertAndCompare( v )
        case _ => Failure( new Exception(s"Cannot compare $this with $v") )
      }
  }

  private def convertAndCompare(v: Value): Try[Int] =
    if( v.raw matches Value.NMFormat )
      Success(raw.toDouble compareTo v.raw.toDouble)
    else
      Failure( new Exception(s"Converting $v to a Number yielded an invalid result. The format is invalid") )
}

/**
  * Class representing a date
  */
case class Date(raw: String) extends Value {

  lazy val formatError: Option[String] =
    Value.checkDate(raw) map { x => s"$this is invalid. $x." }

  override def compareTo(v: Value): Try[Int] = formatError match {
    case Some(m) => Failure( new Exception(m) )
    case None    =>
      v match {
        case x: Date =>
          x.formatError match {
            case None    => Success( raw.toDouble compareTo x.raw.toDouble )
            case Some(m) => Failure( new Exception(m) )
          }
        case Null      => Failure( new Exception(s"Cannot compare $this with $v") )
        case x: Number => Failure( new Exception(s"Cannot compare $this with $v") ) //FIXME: The idea here is that 1.0 == 1 == 0001.000000
        case x: Time   => Failure( new Exception(s"Cannot compare $this with $v") )
        case _         => convertAndCompare( v )
      }
  }

  private def convertAndCompare(v: Value): Try[Int] = {
    //FIXME This is okay if we assume that DT and DTM have the same time zone
    val x = if ( v.isInstanceOf[DateTime] ) v.raw take 6 else v.raw
    Value.checkDate( x ) match {
      case None => Success( raw.toDouble compareTo x.toDouble )
      case Some(m) =>
        Failure( new Exception(s"Converting $v to a Date yielded an invalid result. $m") )
    }
  }
}

/**
  * Class representing a time
  */
case class Time(raw: String) extends Value {

  lazy val formatError: Option[String] = ??? //FIXME

  def compareTo(v: Value): Try[Int] = v match {
    case x: Time          => Success( raw compareTo v.raw ) //FIXME
    case x: Text          => Success( raw compareTo v.raw ) //FIXME check format
    case x: FormattedText => Success( raw compareTo v.raw ) //FIXME check format
    case _  => Failure( new Exception(s"Cannot compare $this with $v") ) //FIXME Is this Ok for Null?
  }
}

/**
  * Class representing a date time
  */
case class DateTime(raw: String) extends Value {

  lazy val formatError: Option[String] = ??? //FIXME

  def compareTo(v: Value): Try[Int] = v match {
    case x: DateTime => Success( raw compareTo v.raw ) //FIXME
    case x: Date     => x.compareTo( this ) map ( - _ ) //Forward and negate the result
    case x: Text     => Success( raw compareTo v.raw ) //Textual comparison
    case x: FormattedText => Success( raw compareTo v.raw ) //Textual comparison
    case _ => Failure( new Exception(s"Cannot compare $this with $v") ) //FIXME Is this Ok for Null?
  }
}

/**
  * Class representing a text
  */
case class Text(raw: String) extends Value {

  lazy val formatError = None

  def compareTo(v: Value): Try[Int] = Success( raw compareTo v.raw )
}

/**
  * Class representing a formatted text
  */
case class FormattedText(raw: String) extends Value {

  lazy val formatError = None

  def compareTo(v: Value): Try[Int] = Success( raw compareTo v.raw )
}

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
  val NMFormat = "(\\+|\\-)\\d+(\\.\\d*)?"

  //YYYY[MM[DD]]
  val DTFormat = """\d{4}((0[1-9]|1[0-2])(0[1-9]|[1-2][0-9]|3[0-1])?)?"""

  //HH[MM[SS[.S[S[S[S]]]]]][+/-ZZZZ]
  val TMFormat = ""

  // YYYY[MM[DD[HH[MM[SS[.S[S[S[S]]]]]]]]][+/-ZZZZ]
  val DTMFormat = ""


  def compareTo(v1: Value, v2: Value): Try[Int] = ??? //FIXME

  /**
    * Checks the date and returns the error message if any.
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

}
