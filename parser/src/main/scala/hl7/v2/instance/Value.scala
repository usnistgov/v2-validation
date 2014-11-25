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
  def apply(datatype: Primitive, raw: String)
           (implicit dtz: Option[TimeZone]): Value =
    datatype.name match {
      case "NM" => Number(raw)
      case "DT" => Date(raw)
      case "TM" => Time(raw, dtz)
      case "DTM"=> DateTime(raw, dtz)
      case "FT" => FText(raw)
      case _    => Text(raw)
    }
}

//==============================================================================
/**
  * Class representing a text
  */
case class Text(raw: String) extends Value {

  def check: Try[String] = Success(raw)

  def compareTo(v: Value): Try[Int] = Success( raw compareTo v.raw )
}

//==============================================================================
/**
  * Class representing a formatted text
  */
case class FText(raw: String) extends Value {

  def check: Try[String] = Success(raw)

  def compareTo(v: Value): Try[Int] = Success( raw compareTo v.raw )
}


//==============================================================================
/**
  * Class representing a Number
  */
case class Number(raw: String) extends Value {

  def check: Try[String] = Number.check(raw)

  def compareTo(v: Value): Try[Int] =
    Number.isComparableWith(v) match {
      case false => Comparison.notComparableFailure(this, v)
      case true if isNull && v.isNull => Success(0)
      case _  =>
        for {
          d1 <- Number.check(raw)
          d2 <- Number.check(v.raw)
        } yield d1.toDouble compareTo d2.toDouble
    }
}

/**
  * Number companion object
  */
object Number {

  val NM = """(\+|\-)?\d+(\.\d*)?"""

  /**
    * Checks the number format and returns the error message if any
    * @param s - The number as string
    * @return None if valid
    */
  def check(s: String): Try[String] =
    if (s matches NM) Success(s)
    else {
      val m = s"Number($s) is invalid. The format should be: [+|-]digits[.digits]"
      Failure( new Exception(m) )
    }

  /**
    * Returns true if Number is comparable with v
    */
  def isComparableWith(v: Value): Boolean = v match {
    case x: Number => true
    case x: Text   => true
    case x: FText  => true
    case _         => false
  }
}

//==============================================================================
/**
  * Class representing a Date
  */
case class Date(raw: String) extends Value {

  def check: Try[String] = Date.check(raw)

  def compareTo(v: Value): Try[Int] =
    Date.isComparableWith(v) match {
      case false => Comparison.notComparableFailure(this, v)
      case true if isNull && v.isNull => Success(0)
      case _  =>
        val s = if( v.isInstanceOf[DateTime] ) v.raw take 6 else v.raw
        for {
          s1 <- Date.check(raw)
          s2 <- Date.check( s )
        } yield s1 compareTo s2
    }
}

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
   * @param s - The date as string
   * @return Success containing the date as string or a Failure
   */
  def check(s: String): Try[String] =
    s match {
      case DT(y, m, d) =>
        if ("02" == m) {
          if (d == "30" || d == "31")
            invalidDTFailure(s, s"February cannot have $d days")
          else if ( isLeap( y.toInt ) && d == "29")
            invalidDTFailure(s, s"February cannot have 29 days since $y is not a leap year")
          else Success(s)
        }
        else if (d == "31" && ("04" == m || "06" == m || "09" == m || "11" == m))
          invalidDTFailure(s, s"The month $m cannot have 31 days")
        else Success(s)
      case _ => invalidDTFailure(s, "The format should be: YYYY[MM[DD]]")
    }

  /**
    * Returns true if Number is comparable with v
    */
  def isComparableWith(v: Value): Boolean = v match {
    case x: Date     => true
    case x: DateTime => true
    case x: Text   => true
    case x: FText  => true
    case _         => false
  }

  /**
   * Returns true if 'y' is a leap year
   * @param y - The year as an integer
   * @return True if 'y' is a leap year
   */
  private def isLeap(y: Int) = y % 4 == 0

  private def numberOfDaysInMonth(y: Int, m: String): Int = //FIXME empty or invalid month will return a bad result
    if( "02" == m ) if( isLeap(y) ) 29 else 28
    else if( "04" == m || "06" == m || "09" == m || "11" == m ) 30
    else 31

  private def numberOfDaysInYear(y: Int): Int = if( isLeap(y) ) 366 else 365

  def toDays(s: String): Try[Double] = check(s) map { raw =>
    val DT(y, m ,d) = raw
    val year = y.toInt
    numberOfDaysInYear(year) + numberOfDaysInMonth(year, m) + d.toInt
  }

  private def invalidDTFailure(s: String, m: String) = Failure {
    new Exception(s"Date($s) is invalid. $m")
  }
}

//==============================================================================

case class Time(raw: String, dtz: Option[TimeZone]) extends Value {

  def check: Try[String] = Time.check(raw)

  def compareTo(v: Value): Try[Int] =
    Time.isComparableWith(v) match {
      case false => Comparison.notComparableFailure(this, v)
      case true if isNull && v.isNull => Success(0)
      case _  =>
        for {
          s1 <- Time.timeToMilliSeconds(raw, dtz)
          s2 <- Time.timeToMilliSeconds(v.raw, dtz)
        } yield s1 compareTo s2
    }
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
  def check(s: String): Try[String] = if(s matches TM) Success(s) else invalidTMFailure(s)


  /**
    * Returns true if Number is comparable with v
    */
  def isComparableWith(v: Value): Boolean = v match {
    case x: Time  => true
    case x: Text  => true
    case x: FText => true
    case _        => false
  }

  /**
    * Attempts to convert the time to milli seconds.
    * Returns a Failure if the time format is invalid
    * @param v   - The time as string
    * @param dtz - The default time zone
    * @return A Success or Failure if the time is invalid
    */
  def timeToMilliSeconds(v: String, dtz: Option[TimeZone]): Try[Double] =
    check( v ) flatMap { raw =>
      val(tm, tzs) = raw span ( c => c != '+' && c != '-' )
      val tzt = tzs match {
        case "" =>
          if( dtz.isEmpty ) dtzFailure(v) else Success( dtz.get.raw )
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

  private def invalidTMFailure(s: String) = Failure {
    new Exception(s"Time($s) is invalid. The format should be: HH[MM[SS[.S[S[S[S]]]]]][+/-ZZZZ]")
  }

  private def dtzFailure(v: String) = Failure {
    new Exception(s"Time Zone is missing from Time($v) and no default is set.")
  }
}

//==============================================================================

case class DateTime(raw: String, dtz: Option[TimeZone]) extends Value

object DateTime {

  val DTM = s"(\\d{4,8})([^\\+\\-]*)((?:\\+|\\-)?.*)".r

  /**
    * Checks the date-time format and returns the error message if any
    * @param s - The date-time as string
    * @return None if valid
    */
  def check(s: String): Try[String] = s match {
    case DTM(ds, ts, tzs) =>
      val r = for {
        r1 <- Date.check(ds)
        r2 <- Date.check(s"$ts$tzs")
      } yield s
      r recoverWith { case e: Throwable =>
        Failure( new Exception(s"DateTime($s) is invalid. ${e.getMessage}") )
      }
    case _ => formatFailure(s)
  }

  def toMilliSeconds(v: String, dtz: Option[TimeZone]): Try[Double] =
    v match {
      case DTM(d, t, tz) =>
        for {
          dd <- Date.toDays(d)
          td <- Time.timeToMilliSeconds(s"$t$tz", dtz)
        } yield  86400000 * dd + td
      case _ => formatFailure(v)
    }

  private def formatFailure(s: String) =
    Failure( new Exception(s"DateTime($s) is invalid. The format should " +
      s"be: YYYY[MM[DD[HH[MM[SS[.S[S[S[S]]]]]]]][+/-ZZZZ]"))
}


//==============================================================================

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
      case TZ(n, hh, mm) =>
        val r = 1000 * ( 3600 * hh.toDouble + 60 * mm.toDouble )
        Success { if( "-" == n ) -r else r }
      case _ => Failure {
        new Exception(s"TimeZone($s) is invalid. The format should be: +/-HHMM")
      }
  }
}

object Comparison {

  def notComparableFailure(v1: Value, v2: Value) =
    Failure( new Exception(s"$v1 is not comparable with $v2") )
}
