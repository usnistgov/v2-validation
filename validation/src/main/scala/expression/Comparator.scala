package expression

import hl7.v2.instance.FormatChecker._
import hl7.v2.instance._

import scala.util.{Failure, Success, Try}


trait Comparator {

  /**
    * Compares the values v1 and v2 and returns :
    *   A Success( 1 ) if 'v1' is greater that 'v2'
    *   A Success(-1 ) if 'v1' is lower that 'v2'
    *   A Success( 0 ) if 'v1' is equal to 'v2'
    *   A Failure if :
    *     'v1' format is invalid
    *     'v2' cannot be converted to the type of 'v1'
    *     'v2' conversion to the type of 'v1' yielded an invalid result
    */
  def compareTo(v1: Value, v2: Value)(implicit tz: TimeZone): Try[Int]
}

object Comparators {

  def compareTo(v1: Value, v2: Value)(implicit tz: TimeZone): Try[Int] =
    v1 match {
      case x: Text   => textualComparison(x, v2)
      case x: FText  => textualComparison(x, v2)
      case x: Number =>  numberComparison(x, v2)
      case x: Date   => ???
      case x: Time   => ???
      case x: DateTime => ???
    }

  def numberComparison(v1: Number, v2: Value): Try[Int] =
    v2 match {
      case x: Date     => comparisonFailure(v1, v2)
      case x: Time     => comparisonFailure(v1, v2)
      case x: DateTime => comparisonFailure(v1, v2)
      case _           =>
        if(v1.isNull && v2.isNull) Success(0)
        else
          for {
            d1 <- numberAsDoubleIfValid( v1.raw )
            d2 <- numberAsDoubleIfValid( v2.raw )
          } yield d1 compareTo d2
    }

  def dateComparison(v1: Date, v2: Value): Try[Int] =
    v2 match {
      case x: Number   => comparisonFailure(v1, v2)
      case x: Time     => comparisonFailure(v1, v2)
      case x: DateTime => comparisonFailure(v1, v2) //FIXME: Should we allow ?
      case _           =>
        if(v1.isNull && v2.isNull) Success(0)
        else
          for {
            s1 <- dateAsValidIfValid(v1.raw)
            s2 <- dateAsValidIfValid(v2.raw)
          } yield s1 compareTo s2
    }

  def timeComparison(v1: Time, v2: Value)(implicit d: Option[TimeZone]): Try[Int] =
    v2 match {
      case x: Number   => comparisonFailure(v1, v2)
      case x: Date     => comparisonFailure(v1, v2)
      case x: DateTime => comparisonFailure(v1, v2) //FIXME: Should we allow ?
      case _           =>
        if(v1.isNull && v2.isNull) Success(0)
        else
          for {
            d1 <- timeToMilliSeconds( v1.raw )
            d2 <- timeToMilliSeconds( v2.raw )
          } yield d1 compareTo d2
    }

  private def numberAsDoubleIfValid(v: String): Try[Double] =
    checkNumberFormat( v ) match {
      case Some(m) => invalidFormatFailure( m )
      case None    => Success( v.toDouble )
    }

  private def dateAsValidIfValid(v: String): Try[String] =
    checkDateFormat( v ) match {
      case Some(m) => invalidFormatFailure( m )
      case None    => Success( v )
    }

  def timeToMilliSeconds(v: String)(implicit dtz: Option[TimeZone]): Try[Double] =
    checkTimeFormat(v) match {
      case Some(m) => Failure( new Exception(m) )
      case None    =>
        val(tm, tzs) = splitOnTZ(v)
        val tz = tzs match {
          case "" =>
            if(dtz.isEmpty) undefinedTZFailure(v) else Success(dtz.get.raw)
          case x  => Success( x )
        }
        tz flatMap { y =>
          val hh = (tm take 2).toDouble
          val mm = tm drop 2 take 2 match { case "" => 0 case x => x.toDouble }
          val ss = tm drop 4 take 2 match { case "" => 0 case x => x.toDouble }
          val ms = tm drop 7        match { case "" => 0 case x => x.toDouble }
          val r  = ms + 1000 * (ss + 60 * mm + 3600 * hh)
          tzToMilliSeconds( y ) map ( r + _ )
        }
    }

  def tzToMilliSeconds(tz: String): Try[Double] =
    checkTimeZoneFormat(tz) match {
      case Some(m) => Failure( new Exception(m) )
      case None    =>
        val TZFormat(s, mm, ss) = tz
        val r = 1000 * ( 3600 * mm.toDouble + 60 * ss.toDouble )
        if( "-" == s ) Success(-r) else Success(r)
    }

  private def textualComparison(v1: Value, v2: Value): Try[Int] =
    Success(v1.raw compareTo v2.raw)

  private def comparisonFailure(v1: Value, v2: Value) =
    Failure(new Exception(s"$v1 cannot be compared with $v2"))

  private def invalidFormatFailure(m: String) = Failure(new Exception(m))

  private def undefinedTZFailure(v: String) =
    Failure(new Exception(s"Time Zone is missing from $v and no default is set in MSH.7"))

}