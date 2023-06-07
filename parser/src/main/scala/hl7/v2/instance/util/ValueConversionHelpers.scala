package hl7.v2.instance.util

import hl7.v2.instance.TimeZone

import scala.util.{Success, Failure, Try}

/**
  * Provides conversion functions for different value classes
  */
object ValueConversionHelpers {

  import hl7.v2.instance.util.ValueFormatCheckers._

  def truncateValue(v1: String, v2: String): Try[(String, String)] = {
    val ln = Math.min(v1.length,v2.length)
    Success(v1.substring(0, ln), v2.substring(0, ln))
  }

  def truncateDateTime(v1: String, v2: String): Try[(String, String)] = {
    val (va, vtz1) = splitOnTZ(v1)
    val (vb, vtz2) = splitOnTZ(v2)

    val dateTime = truncateValue(va, vb).get
//    val timeZone = truncateValue(vtz1, vtz2).get

    Success(
      s"${dateTime._1}${vtz1}",
      s"${dateTime._2}${vtz2}"
    )
  }

  def truncateNumber(v1: String, v2: String): Try[(String, String)] = {
    val v1split = if(v1.contains(".")) v1.split('.') toList else List(v1, "")
    val v2split = if(v2.contains(".")) v2.split('.') toList else List(v2, "")

    val l1 = Math.min(v1split(1).length, v2split(1).length)

    Success(
      s"${v1split.head.toInt.toString}.${v1split(1).substring(0, l1)}",
      s"${v2split.head.toInt.toString}.${v2split(1).substring(0, l1)}"
    )
  }

  /**
    * Converts number of days to milliseconds
    * @param n - The number of days
    * @return The number of days in milliseconds
    */
  def daysToMilliSeconds(n: Int): Long = n.toLong * 86400000

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
      val(tm, tzs) = splitOnTZ(ts)
      val tz = if (tzs.isEmpty) 0 else timeZoneToMilliSeconds(tzs).get

      val hh = (tm take 2).toLong
      val mm = tm drop 2 take 2 match { case "" => 0 case x => x.toLong }
      val ss = tm drop 4 take 2 match { case "" => 0 case x => x.toLong }
      val ms = ( tm drop 7 padTo(4, '0') ).toLong
      val r  = ms + 1000 * (ss + 60 * mm + 3600 * hh)
      r - tz
    }

  /**
    * Converts the time to milli seconds by using dtz as the fallback
    * @param s - The time as string
    * @return A Success or Failure if the time format is invalid or
    *         if the time zone is not defined and no default is provide.
    */
  def timeToMilliSeconds(s: String, dtz: Option[TimeZone]): Try[Long] = {
    val(tm, tzs) = splitOnTZ(s)
    defaultTZ(tzs, dtz) flatMap { tz =>  timeToMilliSeconds(s"$tm$tz") }
  }

  def checkTimeZoneCompare(dtz: Option[TimeZone])(tz1: String, tz2: String): Try[(String, String)] = {
    (defaultTZ(tz1, dtz), defaultTZ(tz2, dtz)) match {
      case (Success(stz1), Success(stz2)) => Success(stz1, stz2)
      case (Failure(_), Failure(_)) => Success(("+0000", "+0000"))
      case (Success(_), Failure(ftz)) => Failure(ftz)
      case (Failure(ftz), Success(_)) => Failure(ftz)
    }
  }

  def splitOnTZ(s: String): (String, String) = {
    s span ( c => c != '+' && c != '-' )
  }

  def defaultTZ(s: String, o: Option[TimeZone]): Try[String] =
    (s, o) match {
      case ("", None)    =>
        Failure(new Exception("Time Zone is missing and no default is set."))
      case ("", Some(x)) => Success(x.raw)
      case ( x, _ )      => Success(x)
    }
}
