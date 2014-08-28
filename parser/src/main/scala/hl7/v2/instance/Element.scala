package hl7.v2.instance

import scala.util.Try

/**
  * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
  */

case class Location(path: String, line: Int, column: Int)

/**
  * A generic message element
  */
trait Element {
  def position: Int
  def instance: Int
  def location: Location
}

/**
  * A complex element
  */
trait Complex extends Element {
  def get(position: Int): Seq[Element]
  def get(position: Int, instance: Int): Seq[Element]
}

/**
  * A simple element
  */
trait Simple extends Element {
  def value: Value
}
