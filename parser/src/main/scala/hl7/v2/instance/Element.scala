package hl7.v2.instance

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
  def children: Seq[Element]
}

/**
  * A simple element
  */
trait Simple extends Element {
  def value: Value
}
