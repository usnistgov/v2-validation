package hl7.v2.instance

import hl7.v2.profile.Primitive

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
}

case class Number(raw: String)   extends Value
case class Date(raw: String)     extends Value {

  lazy val formatError = Date.check(raw) match {
    case Success(_) => None
    case Failure(e) => Some(s"$this is invalid. $e.getMessage")
  }

}
case class Time(raw: String)     extends Value
case class DateTime(raw: String) extends Value
case class Text(raw: String)     extends Value
case class FText(raw: String)    extends Value

/**
  * Date companion object
  */
object Date {

  val YY = "\\d{4}"
  val MM = "(?:0[1-9]|1[0-2])"
  val DD = "(?:0[1-9]|[1-2][0-9]|3[0-1])"
  val DT = s"($YY)($MM)?($DD)?".r

  /**
    * Returns a Failure if the format or the
    * number of days in the month is invalid.
    * @param raw - The date as string
    * @return Success containing the date as string or a Failure
    */
  def check(raw: String): Try[String] =
    raw match {
      case DT(y, m, d) =>
        if ("02" == m) {
          if (d == "30" || d == "31")
            invalidDateFailure(s"February cannot have $d days")
          else if ( isLeap( y.toInt ) && d == "29")
            invalidDateFailure(s"February cannot have 29 days since $y is not a leap year")
          else Success(raw)
        }
        else if (d == "31" && ("04" == m || "06" == m || "09" == m || "11" == m))
          invalidDateFailure(s"The month $m cannot have 31 days")
        else Success(raw)
      case _ => invalidDateFailure("The format should be: YYYY[MM[DD]]")
    }

  /**
    * Returns true if 'y' is a leap year
    * @param y - The year as an integer
    * @return True if 'y' is a leap year
    */
  def isLeap(y: Int) = y % 4 == 0

  private def numberOfDaysInMonth(y: Int, m: String): Int =
    if( "02" == m ) if( isLeap(y) ) 29 else 28
    else if( "04" == m || "06" == m || "09" == m || "11" == m ) 30
    else 31

  private def numberOfDaysInYear(y: Int): Int = if( isLeap(y) ) 366 else 365

  private def invalidDateFailure(m: String) = Failure(new Exception(m))
}


object Time {

  val SS, MM = "[0-5][0-9]"
  val HH = "(?:[0-1][0-9]|2[0-3])"
  val TM = s"$HH($MM($SS(\\.\\d{1,4})?)?)?((\\+|\\-)?$HH$MM)?"

  /**
   * Checks the time format and returns the error message if any
   * @param s - The time as string
   * @return None if valid
   */
  def check(s: String): Try[String] = if(s matches TM) Success(s) else invalidTimeFailure

  def timeToMilliSeconds(v: String)(implicit dtz: Option[TimeZone]): Try[Double] =
    check( v ) flatMap { raw =>
      val(tm, tzs) = splitOnTZ(raw)
      val tzt = tzs match {
        case "" =>
          if( dtz.isEmpty ) undefinedTZFailure(v) else Success( dtz.get.raw )
        case x  => Success( x )
      }
      tzt flatMap { tz =>
        val hh = (tm take 2).toDouble
        val mm = tm drop 2 take 2 match { case "" => 0 case x => x.toDouble }
        val ss = tm drop 4 take 2 match { case "" => 0 case x => x.toDouble }
        val ms = tm drop 7        match { case "" => 0 case x => x.toDouble }
        val r  = ms + 1000 * (ss + 60 * mm + 3600 * hh)
        TimeZone.toMilliSeconds( tz ) map ( r + _ )
      }
    }

  def splitOnTZ(s: String): (String, String) = s span( c => c != '+' && c != '-' )

  private def invalidTimeFailure = Failure {
    new Exception("The format should be: HH[MM[SS[.S[S[S[S]]]]]][+/-ZZZZ]")
  }

  private def undefinedTZFailure(v: String) =
    Failure(new Exception(s"Time Zone is missing from $v and no default is set in MSH.7")) //FIXME make generic
}



/**
  * Class representing a time zone
  */
case class TimeZone(raw: String) extends AnyVal {

  /**
    * Attempts to convert the time zone to milli seconds.
    * Returns a Failure if the format is invalid
    */
  def toMilliSeconds: Try[Double] = TimeZone.toMilliSeconds(raw)
}

/**
  * Time Zone companion object
  */
object TimeZone {

  val TZ = s"(\\+|\\-)?(${Time.HH})(${Time.MM})".r

  /**
    * Attempts to convert the time zone to milli seconds
    * @param s - The value as string
    * @return Success or failure if the format is invalid
    */
  def toMilliSeconds(s: String): Try[Double] =
    s match {
      case TZ(s, hh, mm) =>
        val r = 1000 * ( 3600 * mm.toDouble + 60 * ss.toDouble )
        if( "-" == s ) Success(-r) else Success(r)
      case _ => Failure {
        new Exception(s"TimeZone($s) is invalid. The format should be: +/-HHMM")
      }
    }
}


