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
  def compareTo(v1: Value, v2: Value): Try[Int]
}

object Comparators {


  def compareTo(n: Number, v: Value): Try[Int] =
    checkNumberFormat( n.raw ) match {
      case Some(m) => invalidFormatFailure( m )
      case None =>
        v match {
          case x: Date     => comparisonFailure(s"$n cannot be compared with $v")
          case x: Time     => comparisonFailure(s"$n cannot be compared with $v")
          case x: DateTime => comparisonFailure(s"$n cannot be compared with $v")
          case NullDate    => comparisonFailure(s"$n cannot be compared with $v")
          case NullTime    => comparisonFailure(s"$n cannot be compared with $v")
          case NullDateTime => comparisonFailure(s"$n cannot be compared with $v")
          case _ =>
            checkNumberFormat( v.raw ) match {
              case Some(m) => invalidFormatFailure( m )
              case None    => Success(n.raw.toDouble compareTo v.raw.toDouble)
            }
        }
    }

  private def comparisonFailure(m: String) = Failure(new Exception(m))

  private def invalidFormatFailure(m: String) = Failure(new Exception(m))

  /*
    - v1 invalid abort
    - v2 same type
        * invalid then abort
        * valid then compare
    - v2 not same type
        * cannot convert abort
        * can convert
            + format not valid abort
            + format valid compare

  def milliSeconds(v: Time): Long = ???
  def milliSeconds(v: Date): Long = ???
  def milliSeconds(v: DateTime): Long = ???
   */

}