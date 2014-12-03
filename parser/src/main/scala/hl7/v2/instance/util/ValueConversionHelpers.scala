package hl7.v2.instance.util

import hl7.v2.instance.TimeZone

import scala.util.{Success, Failure, Try}

/**
  * Provides conversion functions for different value classes
  */
object ValueConversionHelpers {

  import hl7.v2.instance.util.ValueFormatCheckers._

  /**
    * Converts number of days to milliseconds
    * @param n - The number of days
    * @return The number of days in milliseconds
    */
  def daysToMilliSeconds(n: Int): Long = n * 86400000

  /**
    * Converts the time zone to milli seconds
    * @param s - The time zone as string
    * @return A Success or a Failure if the format is invalid
    */
  def timeZoneToMilliSeconds(s: String): Try[Long] =
    checkTimeZoneFormat( s ) map { tz =>
      val s = tz take 1
      val h = (tz drop 1 take 2).toLong
      val m = (tz drop 3       ).toLong
      val r = 1000 * ( 3600 * h + 60 * m )
      if( "-" == s ) -r else r
    }

  /**
    * Converts the time to milli seconds by using UTC when Time Zone is missing
    * @param s - The time as string
    * @return A Success or Failure if the time format is invalid
    */
  def timeToMilliSeconds(s: String): Try[Long] =
    checkTimeFormat(s) map { ts =>
      val(tm, tzs) = ts span ( c => c != '+' && c != '-' )
      val tz = if (tzs.isEmpty) 0 else timeZoneToMilliSeconds(tzs).get

      val hh = (tm take 2).toLong
      val mm = tm drop 2 take 2 match { case "" => 0 case x => x.toLong }
      val ss = tm drop 4 take 2 match { case "" => 0 case x => x.toLong }
      val ms = ( tm drop 7 padTo(4, '0') ).toLong
      val r  = ms + 1000 * (ss + 60 * mm + 3600 * hh)
      r + tz
    }

  /**
    * Converts the time to milli seconds by using dtz as the fallback
    * @param s - The time as string
    * @return A Success or Failure if the time format is invalid or
    *         if the time zone is not defined and no default is provide.
    */
  def timeToMilliSeconds(s: String, dtz: Option[TimeZone]): Try[Long] = {
    val(tm, tzs) = s span ( c => c != '+' && c != '-' )
    defaultTZ(tzs, dtz) flatMap { tz =>  timeToMilliSeconds(s"$tm$tz") }
  }

  private def defaultTZ(s: String, o: Option[TimeZone]): Try[String] =
    (s, o) match {
      case ("", None)    =>
        Failure(new Exception("Time Zone is missing and no default is set."))
      case ("", Some(x)) => Success(x.raw)
      case ( x, _ )      => Success(x)
    }
}
