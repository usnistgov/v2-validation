package hl7.v2.instance
package value

import scala.util.{Success, Failure, Try}

trait Comparator[T1, T2] {

  /**
    * Compares t1 with t2 and returns :
    *   - Success( 0) if t1 is same as t2
    *   - Success(-1) if t1 is lower than t2
    *   - Success( 1) if t1 is greater than t2
    *   - A Failure if :
    *      - t1 and t2 are not comparable
    *      - t1 is invalid
    *      - t2 cannot be converted to t1
    * @param t1 - The first value
    * @param t2 - The second value
    * @return A Success or a Failure
    */
  def compareTo(t1: T1, t2: T2): Try[Int]
}

object Comparator extends FormatChecker {

  import Format._
  import ComparisonGuard._

  implicit object TextComparator extends Comparator[Text, Value] {

    def compareTo(t1: Text, t2: Value): Try[Int] =
      t2 match {
        case x: Number   =>   NumberComparator.compareTo(x, t1) map ( - _ )
        case x: Date     =>     DateComparator.compareTo(x, t1) map ( - _ )
        case x: Time     =>     TimeComparator.compareTo(x, t1) map ( - _ )
        case x: DateTime => DateTimeComparator.compareTo(x, t1) map ( - _ )
        case _ => Success( t1.raw compareTo t2.raw )
      }
  }

  implicit object FTextComparator extends Comparator[FText, Value] {

    def compareTo(t1: FText, t2: Value): Try[Int] =
      t2 match {
        case x: Number   =>   NumberComparator.compareTo(x, t1) map ( - _ )
        case x: Date     =>     DateComparator.compareTo(x, t1) map ( - _ )
        case x: Time     =>     TimeComparator.compareTo(x, t1) map ( - _ )
        case x: DateTime => DateTimeComparator.compareTo(x, t1) map ( - _ )
        case _ => Success( t1.raw compareTo t2.raw )
      }
  }

  implicit object NumberComparator extends Comparator[Number, Value] {

    def compareTo(t1: Number, t2: Value): Try[Int] =
      t1 comparableWith t2 match {
        case false => notComparableFailure(t1, t2)
        case true  =>
          if(t1.isNull && t2.isNull) Success(0)
          else
            for {
              d1 <- asRawNumber(t1)
              d2 <- asRawNumber(t2)
            } yield d1 compareTo d2
      }
  }

  implicit object DateComparator extends Comparator[Date, Value] {

    def compareTo(t1: Date, t2: Value): Try[Int] =
      t1 comparableWith t2 match {
        case false => notComparableFailure(t1, t2)
        case true  =>
          if(t1.isNull && t2.isNull) Success(0)
          else
            checkDateFormat( t1.raw ) match {
              case Some(m) => conversionFailure(m)
              case None    => extractDate( t2 ) map ( t1.raw compareTo _ )
            }
      }
  }

  implicit object TimeComparator extends Comparator[Time, Value] {

    def compareTo(t1: Time, t2: Value): Try[Int] =
      t1 comparableWith t2 match {
        case false => notComparableFailure(t1, t2)
        case true  =>
          if (t1.isNull && t2.isNull) Success(0)
          else {
            val dtz = t1.dtz
            for {
              d1 <- timeToMilliSeconds(t1)
              d2 <- timeToMilliSeconds(t2, dtz)
            } yield d1 compareTo d2
          }
      }
  }

  implicit object DateTimeComparator extends Comparator[DateTime, Value] {

    def compareTo(t1: DateTime, t2: Value): Try[Int] =
      t1 comparableWith t2 match {
        case false => notComparableFailure(t1, t2)
        case true  =>
          if (t1.isNull && t2.isNull) Success(0)
          else {
            val dtz = t1.dtz
            for {
              d1 <- dateTimeToMilliSeconds(t1)
              d2 <- dateTimeToMilliSeconds(t2, dtz)
            } yield d1 compareTo d2
          }
      }
  }

  // Helpers
  /**
    * Checks if 'v' has a valid Number format and convert it to Double
    */
  private def asRawNumber(v: Value): Try[Double] =
    checkNumberFormat( v.raw ) match {
      case None    => Success( v.raw.toDouble )
      case Some(m) => conversionFailure(m)
    }

  /**
    * Extract the Date from the value if possible
    */
  private def extractDate(v: Value): Try[String] = v match {
    case Date(s) => Success(s)
    case Text(s) => Success(s)
    case FText(s) => Success(s)
    case DateTime(s, _) => val (dtm, _) = splitOnTZ(s); Success(dtm take 6)
    case _ => Failure( new Exception(s"Cannot extract the Date from $v") )
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
    * Convert the Date to Days
    */
  private def dateToDays(s: String): Try[Double] =
    checkDateFormat(s) match {
      case Some(m) => conversionFailure(m)
      case None    =>
        val y = (s take 4).toInt
        val m = s drop 4 take 2 match { case "" => 0 case x => x.toInt }
        val d = s drop 6        match { case "" => 0 case x => x.toInt }
        Success( numberOfDaysInYear(y) + numberOfDaysInMonth(y, m) + d )
    }

  /**
    * Convert the time to milli seconds
    */
  private def timeToMilliSeconds(t: Time): Try[Double] =
    timeToMilliSeconds(t.raw, t.dtz)

  /**
    * Convert the value to milli seconds by using dtz as the default TimeZone
    */
  private def timeToMilliSeconds(v: Value, dtz: Option[TimeZone]): Try[Double] =
    v match {
      case t: Time => timeToMilliSeconds( t.raw, t.dtz )
      case _       => timeToMilliSeconds(v.raw, dtz) //FIXME do this only for text based
    }

  /**
    * Attempts to convert the time to milli seconds.
    * Returns a Failure if the time format is invalid
    * @param v   - The time as string
    * @param dtz - The default time zone
    * @return A Success or Failure if the time is invalid
    */
  private def timeToMilliSeconds(v: String, dtz: Option[TimeZone]): Try[Double] =
    checkTimeFormat( v ) match {
      case Some(m) => conversionFailure(m)
      case None =>
        val(tm, tzs) = splitOnTZ(v)
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
          timeZoneToMilliSeconds( tz ) map ( r + _ )
        }
    }

  /**
    * Attempts to convert the time zone to milli seconds
    * @param s - The time zone as string
    * @return Success or failure if the format is invalid
    */
  private def timeZoneToMilliSeconds(s: String): Try[Double] =
    s match {
      case TZ(n, hh, mm) =>
        val r = 1000 * ( 3600 * hh.toDouble + 60 * mm.toDouble )
        Success { if( "-" == n ) -r else r }
      case _ => Failure {
        new Exception(s"TimeZone($s) is invalid. The format should be: +/-HHMM")
      }
    }

  private def dateTimeToMilliSeconds(v: String, dtz: Option[TimeZone]): Try[Double] =
    v match {
      case DTM(d, t, tz) =>
        for {
          dd <- dateToDays(d)
          tm = if( t.isEmpty ) s"00$tz" else s"$t$tz"
          td <- timeToMilliSeconds(tm, dtz)
        } yield  86400000 * dd + td
      case _ => Failure( new Exception("$v is not a valid DateTime(DTM)") )
    }

  /**
    * Convert the DateTime to milli seconds
    */
  private def dateTimeToMilliSeconds(t: DateTime): Try[Double] =
    dateTimeToMilliSeconds(t.raw, t.dtz)

  /**
    * Convert the value to milli seconds by using dtz as the default TimeZone
    */
  private def dateTimeToMilliSeconds(v: Value, dtz: Option[TimeZone]): Try[Double] =
    v match {
      case t: DateTime => timeToMilliSeconds( t.raw, t.dtz )
      case _           => timeToMilliSeconds(v.raw, dtz) //FIXME do this only for text based
    }

  private def splitOnTZ(s: String) = s span ( c => c != '+' && c != '-' )

  private def notComparableFailure(v1: Value, v2: Value) =
    Failure( new Exception(s"$v1 cannot be compared with $v2") )

  private def conversionFailure(m: String) = Failure(new Exception(m))

  private def dtzFailure(v: String) = Failure {
    new Exception(s"Time Zone is missing from Time($v) and no default is set.")
  }

}

/**
  * Provides extension method which allow
  * to check if two values can be compared
  */
object ComparisonGuard { //FIXME TO BE Replaced with extension.ValueComparisonGuards

  implicit class TextComparisonGuard(val o: Text) extends AnyVal {
    def comparableWith(v: Value) = true
  }

  implicit class FTextComparisonGuard(val o: FText) extends AnyVal {
    def comparableWith(v: Value) = true
  }

  implicit class NumberComparisonGuard(val o: Number) extends AnyVal {
    def comparableWith(v: Value) = v match {
      case x: Number => true
      case x: Text   => true
      case x: FText  => true
      case _         => false
    }
  }

  implicit class DateComparisonGuard(val o: Date) extends AnyVal {
    def comparableWith(v: Value) = v match {
      case x: Date  => true
      case x: Text  => true
      case x: FText => true
      case _        => false
    }
  }

  implicit class TimeComparisonGuard(val o: Time) extends AnyVal {
    def comparableWith(v: Value) = v match {
      case x: Time  => true
      case x: Text  => true
      case x: FText => true
      case _        => false
    }
  }

  implicit class DateTimeComparisonGuard(val o: DateTime) extends AnyVal {
    def comparableWith(v: Value) = v match {
      case x: DateTime => true
      case x: Text     => true
      case x: FText    => true
      case _           => false
    }
  }
}
