package hl7.v2.instance
package util

import hl7.v2.instance.util.ValueConversionHelpers._
import hl7.v2.instance.util.ValueFormatCheckers._

import scala.util.{Failure, Success, Try}

object ValueComparator {

  /**
    * Compares the values v1 and v2 and returns :
    *   A Success( 1 ) if 'v1' is greater that 'v2'
    *   A Success(-1 ) if 'v1' is lower that 'v2'
    *   A Success( 0 ) if 'v1' is equal to 'v2'
    *   A Failure if :
    *     'v1' and 'v2' are not comparable
    *     'v1' or 'v2' is invalid according to the conversion target type
    */
  def compareTo(v1: Value, v2: Value): Try[Int] = v1 match {
    case x: Text   => textComparison(x, v2)
    case x: Number => numberComparison(x, v2)
    case x: Date   => dateComparison(x, v2)
    case x: Time   => timeComparison(x, v2)
    case x: DateTime => dateTimeComparison(x, v2)
    case x: FText    => textComparison(x, v2)
  }

  /**
    * Compares the number v1 to the value v2
    */
  def numberComparison(v1: Number, v2: Value): Try[Int] =
    checkAndCompare(v1, v2, numberComparisonGuard, numberComparator)

  /**
    * Compares the date v1 to the value v2
    */
  def dateComparison(v1: Date, v2: Value): Try[Int] =
    checkAndCompare(v1, v2, dateComparisonGuard, dateComparator)

  /**
    * Compares the time v1 to the value v2
    */
  def timeComparison(v1: Time, v2: Value): Try[Int] =
    checkAndCompare(v1, v2, timeComparisonGuard, timeComparator)

  /**
    * Compares the date time v1 to the value v2
    */
  def dateTimeComparison(v1: DateTime, v2: Value): Try[Int] =
    checkAndCompare(v1, v2, dateTimeComparisonGuard, dateTimeComparator)

  /**
    * Compares the text v1 to the value v2
    */
  def textComparison(v1: Value, v2: Value): Try[Int] = v2 match {
    case x: Number   =>   numberComparison(x, v1) map ( - _)
    case x: Date     =>     dateComparison(x, v1) map ( - _)
    case x: Time     =>     timeComparison(x, v1) map ( - _)
    case x: DateTime => dateTimeComparison(x, v1) map ( - _)
    case _           => Success( v1.raw compareTo v2.raw )
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
      compare: (T, Value) => Try[Int]
    ): Try[Int] =

    check( v2 ) match {
      case false => Failure( new Exception(s"$v1 is not comparable to $v2.") )
      case true  => if( v1.isNull && v2.isNull ) Success(0) else compare(v1, v2)
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

  /**
    * Compares the values they both have a valid number format
    */
  private def numberComparator(v1: Number, v2: Value): Try[Int] =
    for {
      n1 <- checkNumberFormat(v1.raw)
      n2 <- checkNumberFormat(v2.raw)
    } yield n1.toDouble compareTo n2.toDouble

  /**
    * Compares the values they both have a valid date format
    */
  private def dateComparator(v1: Date, v2: Value): Try[Int] =
    for {
      x1 <- checkDateFormat(v1.raw)
      x2 <- checkDateFormat(v2.raw)
    } yield x1 compareTo x2

  /**
    * Compares the values they both have a valid time format
    */
  private def timeComparator(v1: Time, v2: Value): Try[Int] =
    for {
      x1 <- checkTimeFormat(v1.raw)
      x2 <- checkTimeFormat(v2.raw)
      t1 <- timeToMilliSeconds(x1, v1.dtz)
      t2 <- timeToMilliSeconds(x2, v1.dtz)
    } yield t1 compareTo t2

  /**
    * Compares the values they both have a valid date time format
    */
  private def dateTimeComparator(v1: DateTime, v2: Value): Try[Int] =
    for {
      x1 <- checkDateTimeFormat(v1.raw)
      x2 <- checkDateTimeFormat(v2.raw)
      t1 <- dateTimeToMilliSeconds(x1, v1.dtz)
      t2 <- dateTimeToMilliSeconds(x2, v1.dtz)
    } yield t1 compareTo t2

}
