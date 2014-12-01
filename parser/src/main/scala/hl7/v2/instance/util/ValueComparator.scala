package hl7.v2.instance
package util

import hl7.v2.instance.value.Format._

import scala.util.{Success, Failure, Try}

object ValueComparator {

  import ValueFormatCheckers._

  def compareTo(v1: Number, v2: Value): Try[Double] =
    for {
      n1 <- asRawNumber(v1)
      n2 <- asRawNumber(v2)
    } yield if( n1 == "" && n2 == "" ) 0 else n1.toDouble compareTo n2.toDouble

  def compareTo(v1: Date, v2: Value): Try[Double] =
    for {
      d1 <- asRawDate(v1)
      d2 <- asRawDate(v2)
    } yield if( d1 == "" && d2 == "" ) 0 else d1 compareTo d2

  def compareTo(v1: Time, v2: Value): Try[Double] =
    for {
      t1 <- asTimeInMilliSeconds(v1, v1.dtz)
      t2 <- asTimeInMilliSeconds(v2, v1.dtz)
    } yield t1 compareTo t2

  def compareTo(v1: DateTime, v2: Value): Try[Double] = ??? //FIXME

  def compareTo(v1: Text, v2: Value): Try[Double] = v2 match {
    case x: Number   => compareTo(x, v1) map ( - _)
    case x: Date     => compareTo(x, v1) map ( - _)
    case x: Time     => compareTo(x, v1) map ( - _)
    case x: DateTime => compareTo(x, v1) map ( - _)
    case _           => Success( v1.raw compareTo v2.raw )
  }

  def compareTo(v1: FText, v2: Value): Try[Double] = v2 match {
    case x: Number   => compareTo(x, v1) map ( - _)
    case x: Date     => compareTo(x, v1) map ( - _)
    case x: Time     => compareTo(x, v1) map ( - _)
    case x: DateTime => compareTo(x, v1) map ( - _)
    case _           => Success( v1.raw compareTo v2.raw )
  }


  /**
    * Attempts to convert the value v as number and returns the raw string
    * @param v - The value to be converted
    * @return The raw string or A Failure if:
    *         - v is a number but its format is invalid
    *         - v cannot be converted to a number
    *         - converting v to a number yielded an invalid result
    */
  def asRawNumber(v: Value): Try[String] = v match {
    case x: Number => check(x, checkNumber )
    case x: Text   => convert( x, checkNumber )
    case x: FText  => convert( x, checkNumber )
    case _ => Failure( new Exception(s"$v cannot be converted to a Number.") )
  }

  def asRawDate(v: Value): Try[String] = v match {
    case x: Date   =>    check(x, checkDate )
    case x: Text   => convert( x, checkDate )
    case x: FText  => convert( x, checkDate )
    case x: DateTime =>
      val(y, _) = splitOnTZ(x.raw)
      check( Date(y take 8), checkDate)
    case _ => Failure( new Exception(s"$v cannot be converted to a Date.") )
  }

  def asTimeInMilliSeconds(v: Value, dtz: Option[TimeZone]) = v match {
    case x: Time =>  timeToMilliSeconds(x.raw, dtz)
    case Text(x) =>  timeToMilliSeconds(x, dtz)
    case FText(x) => timeToMilliSeconds(x, dtz)
    case _ => Failure( new Exception(s"$v cannot be converted to a Time.") )
  }

  /**
   * Convert the Date to Days
   */
  private def dateToDays(s: String): Try[Double] =
    checkDateFormat(s) match {
      case Some(m) => Failure( new Exception(m) )
      case None    =>
        val y = (s take 4).toInt
        val m = s drop 4 take 2 match { case "" => 0 case x => x.toInt }
        val d = s drop 6        match { case "" => 0 case x => x.toInt }
        Success( numberOfDaysInYear(y) + numberOfDaysInMonth(y, m) + d )
    }

  private def dateTimeToMilliSeconds(v: String, dtz: Option[TimeZone]): Try[Double] =
    checkDateTime(v) match {
      case Some(m) => Failure( new Exception(m) )
      case None    =>
        val(dtm, tz) = splitOnTZ(v)
        val d = dtm take 8
        val t = dtm drop 8
        for {
          dd <- dateToDays(d)
          tm = if( t.isEmpty ) s"00$tz" else s"$t$tz" //FIXME Is 00 Ok for time ?
          td <- timeToMilliSeconds(tm, dtz)
        } yield  86400000 * dd + td
    }

  /**
    * Attempts to convert the time to milli seconds.
    * Returns a Failure if the time format is invalid
    * @param v   - The time as string
    * @param dtz - The default time zone
    * @return A Success or Failure if the time is invalid
    */
  private def timeToMilliSeconds(v: String, dtz: Option[TimeZone]): Try[Double] =
    checkTime( v ) match {
      case Some(m) => Failure( new Exception(m) )
      case None    =>
        val(tm, tzs) = splitOnTZ(v)
        val tzt = tzs match {
          case "" => if( dtz.isEmpty ) dtzFailure(v) else Success( dtz.get.raw )
          case x  => Success( x )
        }
        tzt flatMap { tz =>
          val hh = (tm take 2).toDouble
          val mm = tm drop 2 take 2 match { case "" => 0 case x => x.toDouble }
          val ss = tm drop 4 take 2 match { case "" => 0 case x => x.toDouble }
          val ms = tm drop 7        match { case "" => 0 case x => x.toDouble }
          val r  = ms + 1000 * (ss + 60 * mm + 3600 * hh)
          timeZoneToMilliSeconds( tz ) map ( r + _ )
        }
    }

  /**
    * Convert the time zone to milliseconds
    */
  private def timeZoneToMilliSeconds(tz: String): Try[Double] =
    checkTimeZone( tz ) match {
      case Some(m) => Failure( new Exception(m) )
      case None    =>
        val s = tz take 1
        val h = tz drop 1 take 2 match { case "" => 0 case x => x.toInt }
        val m = tz drop 3        match { case "" => 0 case x => x.toInt }
       val r = 1000 * ( 3600 * h + 60 * m )
       if( "-" == s ) Success(-r) else Success(r)
    }

  /**
    * Returns true if 'y' is a leap year
    */
  private def isLeapYear(y: Int) = y % 4 == 0

  /**
    * Returns the number of days in the month
    */
  private def numberOfDaysInMonth(y: Int, m: Int): Int =
    if( m == 0 ) 0
    else if( 2 == m ) if( isLeapYear(y) ) 29 else 28
    else if( 4 == m || 6 == m || 9 == m || 11 == m ) 30
    else 31

  /**
    * Returns the number of days in the year
    */
  private def numberOfDaysInYear(y: Int): Int = if( isLeapYear(y) ) 366 else 365

  /**
    * Split the string on the time zone
    */
  private def splitOnTZ(s: String) = s span ( c => c != '+' && c != '-' )

  private def dtzFailure(v: String) = Failure {
    new Exception(s"Time Zone is missing from Time($v) and no default is set.")
  }

  /**
   * Check the value v with the specified checker and returns the result
   */
  private def check(v: Value, checker: String => Option[String]): Try[String] =
    checker(v.raw) match {
      case None    => Success(v.raw)
      case Some(m) => Failure( new Exception(m) )
    }

  private def convert(v: Value, checker: String => Option[String]): Try[String] =
    checker(v.raw) match {
      case None    => Success(v.raw)
      case Some(m) => Failure( new Exception(s"Converting $v failed. $m") )
    }

}
