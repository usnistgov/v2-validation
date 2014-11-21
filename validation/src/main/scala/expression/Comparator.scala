package expression

import hl7.v2.instance.Value

import scala.util.Try

object ValueComparator {


  def compareTo[T1 <: Value, T2 <: Value](v1: T1, v2: T2)
                                         (implicit e: Comparator[T1, T2]): Try[Int] =
    e.compareTo(v1, v2)
}

trait Comparator[T1 <: Value, T2 <: Value] {

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
  def compareTo(v1: T1, v2: T2): Try[Int]
}

object Comparator {

  //TODO: Implements the different comparators.
}