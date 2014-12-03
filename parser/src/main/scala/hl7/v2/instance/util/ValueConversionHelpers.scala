package hl7.v2.instance.util

import hl7.v2.instance.TimeZone

import scala.concurrent.duration.Duration
import scala.util.{Failure, Try}

/**
  * Provides conversion functions for different value classes
  */
object ValueConversionHelpers {

  import hl7.v2.instance.util.ValueFormatCheckers._

  /**
    * Converts the data to days
    * @param s - The date as string
    * @return A Success or a Failure if the date is invalid
    */
  def dateToDays(s: String): Try[Long] =
    checkDateFormat(s) map { dt =>
      val y = (s take 4).toInt
      val m = s drop 4 take 2 match { case "" => 0 case x => x.toInt }
      val d = s drop 6        match { case "" => 0 case x => x.toInt }
      numberOfDaysInYear(y) + numberOfDaysInMonth(y, m) + d
    }

  /**
    * Converts the time zone to milli seconds
    * @param s - The time zone as string
    * @return A Success or a Failure if the format is invalid
    */
  def timeZoneToMilliSeconds(s: String): Try[Long] =
    checkTimeZoneFormat( s ) map { tz =>
      val s = tz take 1
      val h = tz drop 1 take 2 match { case "" => 0 case x => x.toLong }
      val m = tz drop 3        match { case "" => 0 case x => x.toLong }
      val r = 1000 * ( 3600 * h + 60 * m )
      if( "-" == s ) -r else r
    }

  /**
    * Converts the time to milli seconds
    * @param s   - The time as string
    * @param dtz - The default time zone
    * @return A Success or Failure if the time format is invalid
    */
  //FIXME give dtz as string
  def timeToMilliSeconds(s: String, dtz: Option[TimeZone]): Try[Long] =
    checkTimeFormat(s) flatMap { ts =>
      val(tm, tzs) = splitOnTZ(ts)
      val tzInMilliSeconds =
        if( tzs.nonEmpty ) timeZoneToMilliSeconds(tzs)
        else dtz match {
          case None    => undefinedTimeZoneFailure(s)
          case Some(x) => timeZoneToMilliSeconds( x.raw )
        }
      tzInMilliSeconds map { tz =>
        val hh = (tm take 2).toLong
        val mm = tm drop 2 take 2 match { case "" => 0 case x => x.toLong }
        val ss = tm drop 4 take 2 match { case "" => 0 case x => x.toLong }
        val ms = ( tm drop 7 padTo(4, '0') ).toLong
        val r  = ms + 1000 * (ss + 60 * mm + 3600 * hh)
        r + tz
      }
    }

  /**
    * Converts the date time to milli seconds
    * @param s   - The date time as string
    * @param dtz - The default time zone
    * @return A Success or a Failure if the date time is invalid
    */
  //FIXME give dtz as string
  def dateTimeToMilliSeconds(s: String, dtz: Option[TimeZone]): Try[Long] =
    checkDateTimeFormat(s) flatMap { _ =>
      val(dtm, tz) = splitOnTZ(s)
      val d = dtm take 8
      val t = dtm drop 8
      for {
        dd <- dateToDays(d)
        tm = if( t.isEmpty ) s"00$tz" else s"$t$tz"
        td <- timeToMilliSeconds(tm, dtz)
      } yield  86400000 * dd + td
    }

  /**
    * Returns true if 'y' is a leap year
    */
  private def isLeapYear(y: Int) = y % 4 == 0

  /**
    * Returns the number of days in the month
    * @param y - The year
    * @param m - The month
    * @return The number of days in the month or 0 is the if the month is invalid
    */
  private def numberOfDaysInMonth(y: Int, m: Int): Int =
    if( 2 == m ) if( isLeapYear(y) ) 29 else 28
    else if( 4 == m || 6 == m || 9 == m || 11 == m ) 30
    else if( 1 == m || 3 == m || 5 == m || 7 == m || 8 == m || 10 == m || 12 == m ) 31
    else 0

  /**
    * Returns the number of days in the year
    */
  private def numberOfDaysInYear(y: Int): Int =
    if( y == 0) 0 else if( isLeapYear(y) ) 366 else 365

  /**
    * Split the string on the time zone
    */
  private def splitOnTZ(s: String) = s span ( c => c != '+' && c != '-' )

  /**
    * Create the failure for undefined default time zone
    */
  private def undefinedTimeZoneFailure(v: String) = Failure {
    new Exception(s"Time Zone is missing from Time($v) and no default is set.")
  }

}
