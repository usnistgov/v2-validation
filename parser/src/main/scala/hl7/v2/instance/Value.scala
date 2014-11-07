package hl7.v2.instance

import hl7.v2.profile.Primitive

import scala.util.Try

/**
  * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
  */

sealed trait Value {
  def asString: String

  /**
    * Compares this to v and returns :
    *   Success( 1 ) if this is greater that `v'
    *   Success(-1 ) if this is lower that `v'
    *   Success( 0 ) if this is equal to `v'
    *   Failure if `v' cannot be converted to the current Value class or if the comparison is not permitted.
    */
  def compareTo(v: Value): Try[Int] = ??? //FIXME
}

case class Number(asString: String)   extends Value
case class Text(asString: String)     extends Value
case class Date(asString: String)     extends Value
case class Time(asString: String)     extends Value
case class DateTime(asString: String) extends Value

object Value {

  /**
    * Create the value from string depending on the data type
    */
  def apply(datatype: Primitive, asString: String): Value = datatype.name match {
    case "NM"  => Number(asString)
    case "DT"  => Date(asString)
    case "TM"  => Time(asString)
    case "DTM" => DateTime(asString)
    case _     => Text(asString)
  }
}
