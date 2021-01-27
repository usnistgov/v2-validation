package hl7.v2.instance
package util

import hl7.v2.instance.util.ValueConversionHelpers._
import hl7.v2.instance.util.ValueFormatCheckers._

import scala.util.{Failure, Success, Try}

object ValueComparator {

  type TZ = Option[TimeZone]

  /**
    * Compares the values v1 and v2 and returns :
    *   A Success( 1 ) if 'v1' is greater that 'v2'
    *   A Success(-1 ) if 'v1' is lower that 'v2'
    *   A Success( 0 ) if 'v1' is equal to 'v2'
    *   A Failure if :
    *     'v1' and 'v2' are not comparable
    *     'v1' or 'v2' is invalid according to the conversion target type
    */
  def compareTo(v1: Value, v2: Value, truncate: Boolean = false)(implicit dtz: TZ): Try[Int] = v1 match {
    case x: Text   => textComparison(x, v2, truncate)
    case x: Number => numberComparison(x, v2, truncate)
    case x: Date   => dateComparison(x, v2, truncate)
    case x: Time   => timeComparison(x, v2, truncate)
    case x: DateTime => dateTimeComparison(x, v2, truncate)
    case x: FText    => textComparison(x, v2, truncate)
  }

  /**
    * Compares the number v1 to the value v2
    */
  def numberComparison(v1: Number, v2: Value, truncate: Boolean = false): Try[Int] =
    checkAndCompare(v1, v2, numberComparisonGuard, numberComparator, truncate)

  /**
    * Compares the date v1 to the value v2
    */
  def dateComparison(v1: Date, v2: Value, truncate: Boolean = false): Try[Int] =
    checkAndCompare(v1, v2, dateComparisonGuard, dateComparator, truncate)

  /**
    * Compares the time v1 to the value v2
    */
  def timeComparison(v1: Time, v2: Value, truncate: Boolean = false)(implicit dtz: TZ): Try[Int] =
    checkAndCompare(v1, v2, timeComparisonGuard, timeComparator(dtz), truncate)

  /**
    * Compares the date time v1 to the value v2
    */
  def dateTimeComparison(v1: DateTime, v2: Value, truncate: Boolean = false)(implicit dtz: TZ): Try[Int] =
    checkAndCompare(v1, v2, dateTimeComparisonGuard, dateTimeComparator(dtz), truncate)

  /**
    * Compares the text v1 to the value v2
    */
  def textComparison(v1: Value, v2: Value, truncate: Boolean = false)(implicit dtz: TZ): Try[Int] =
    v2 match {
      case x: Number   =>   numberComparison(x, v1, truncate) map ( - _)
      case x: Date     =>     dateComparison(x, v1, truncate) map ( - _)
      case x: Time     =>     timeComparison(x, v1, truncate) map ( - _)
      case x: DateTime => dateTimeComparison(x, v1, truncate) map ( - _)
      case _           =>     textComparator(v1, v2, truncate) map ( - _)
    }

  /**
    * Compares v1 and v2 if they are comparable.
    * @param v1 - The first value
    * @param v2 - The second value
    * @param check   - The function used to check if v1 is comparable to v2
    * @param compare - The function used for comparison
    * @return
    *         - Success( 0 ) if v1 is same as v2
    *         - Success(-1 ) if v1 is lower than v2
    *         - Success( 1 ) if v1 is greater than v2
    *         - A Failure if :
    *             - v1 and v2 are not comparable
    *             - v1 or v2 is invalid according to the target type
    */
  private def checkAndCompare[T <: Value] (
      v1: T ,
      v2: Value,
      check: Value => Boolean,
      compare: (T, Value, Boolean) => Try[Int],
      truncate: Boolean  = false
    ): Try[Int] =

    check( v2 ) match {
      case false => Failure( new Exception(s"$v1 is not comparable to $v2.") )
      case true  => if( v1.isNull && v2.isNull ) Success(0) else compare(v1, v2, truncate)
    }

  /**
    * Returns true is v is comparable to a Number
    */
  private def numberComparisonGuard(v: Value): Boolean = v match {
    case x: Number => true
    case x: Text   => true
    case x: FText  => true
    case _         => false
  }

  /**
    * Returns true is v is comparable to a Date
    */
  private def dateComparisonGuard(v: Value): Boolean = v match {
    case x: Date  => true
    case x: Text  => true
    case x: FText => true
    case _        => false
  }

  /**
    * Returns true is v is comparable to a Time
    */
  private def timeComparisonGuard(v: Value): Boolean = v match {
    case x: Time  => true
    case x: Text  => true
    case x: FText => true
    case _        => false
  }

  /**
    * Returns true is v is comparable to a DateTime
    */
  private def dateTimeComparisonGuard(v: Value): Boolean = v match {
    case x: DateTime  => true
    case x: Text  => true
    case x: FText => true
    case _        => false
  }

  private def preProcessString(v1: String, v2: String, truncate: Boolean = false): Try[(String, String)] = {
    if(truncate) truncateValue(v1, v2) else Success(v1, v2)
  }

  private def preProcessNumber(v1: String, v2: String, truncate: Boolean = false): Try[(String, String)] = {
    if(truncate) truncateNumber(v1, v2) else Success(v1, v2)
  }

  private def preProcessDateTime(v1: String, v2: String, truncate: Boolean = false): Try[(String, String)] = {
    if(truncate) truncateDateTime(v1, v2) else Success(v1, v2)
  }

  /**
    * Compares the values they both have to have a valid number format
    */
  private def textComparator(v1: Value, v2: Value, truncate: Boolean = false): Try[Int] =
    for {
      t1 <- Success(v1.raw.toLowerCase)
      t2 <- Success(v2.raw.toLowerCase)
      (va, vb) <- preProcessString(t1, t2, truncate)
    } yield va compareTo vb


  /**
    * Compares the values they both have to have a valid number format
    */
  private def numberComparator(v1: Number, v2: Value, truncate: Boolean = false): Try[Int] =
    for {
      n1 <- checkNumberFormat(v1.raw)
      n2 <- checkNumberFormat(v2.raw)
      (va, vb) <- preProcessNumber(n1, n2, truncate)
    } yield va.toDouble compareTo vb.toDouble

  /**
    * Compares the values they both have to have a valid date format
    */
  private def dateComparator(v1: Date, v2: Value, truncate: Boolean = false): Try[Int] =
    for {
      x1 <- checkDateFormat(v1.raw)
      x2 <- checkDateFormat(v2.raw)
      (va, vb) <- preProcessString(x1, x2, truncate)
    } yield va compareTo vb

  /**
    * Compares the values they both have to have a valid time format
    */
  private def timeComparator(dtz: TZ)(v1: Time, v2: Value, truncate: Boolean = false): Try[Int] =
    for {
      x1 <- checkTimeFormat(v1.raw)
      x2 <- checkTimeFormat(v2.raw)
      (va, vb) <- preProcessDateTime(x1, x2, truncate)
      (tm1, tzs1) = splitOnTZ(va)
      (tm2, tzs2) = splitOnTZ(vb)
      (tz1, tz2) <- checkTimeZoneCompare(dtz)(tzs1, tzs2)
      t1 <- timeToMilliSeconds(s"$tm1$tz1", dtz)
      t2 <- timeToMilliSeconds(s"$tm2$tz2", dtz)
    } yield t1 compareTo t2

  /**
  * Compares the values they both have to have a valid date time format
  */
  private def dateTimeComparator(dtz: TZ)(v1: DateTime, v2: Value, truncate: Boolean = false): Try[Int] =
    for {
      x1 <- checkDateTimeFormat(v1.raw)
      x2 <- checkDateTimeFormat(v2.raw)
      (va, vb) <- preProcessDateTime(x1, x2, truncate)
      (dtm1, tzs1) = splitOnTZ(va)
      (dtm2, tzs2) = splitOnTZ(vb)
      (tz1, tz2) <- checkTimeZoneCompare(dtz)(tzs1, tzs2)
      // If no default TZ
      // -> one of the dates doesn't have TZ then fail
      // -> both dates don't have TZ then use +0000 (consider in the same TZ)
    } yield {
      dtm1 take 6 compareTo ( dtm2 take 6 ) match {
        case 0 => // we need to check the day
          val d1 = dtm1 drop 6 take 2 match { case "" => 0 case x => x.toInt }
          val d2 = dtm2 drop 6 take 2 match { case "" => 0 case x => x.toInt }
          val t1 = dtm1 drop 8 match { case "" => s"00+0000" case x => s"$x$tz1" }
          val t2 = dtm2 drop 8 match { case "" => s"00+0000" case x => s"$x$tz2" }

          val r1 = daysToMilliSeconds(d1) + timeToMilliSeconds(t1).get
          val r2 = daysToMilliSeconds(d2) + timeToMilliSeconds(t2).get

          r1 compareTo r2
        case x => x
      }
    }

}
