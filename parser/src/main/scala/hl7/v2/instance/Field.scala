package hl7.v2.instance

import hl7.v2.profile.{Datatype, Composite, Primitive}

/**
  * Trait representing a field
  */
sealed trait Field extends Element {
  def datatype: Datatype
  def location: Location
  def position: Int
  def instance: Int
}

/**
  * Class representing a simple field
  */
case class SimpleField(
    datatype: Primitive,
    location: Location,
    position: Int,
    instance: Int,
    value: Value
) extends Field with Simple

/**
  * Class representing a complex field
  */
case class ComplexField(
    datatype: Composite,
    location: Location,
    position: Int,
    instance: Int,
    children: List[Component],
    hasExtra: Boolean
) extends Field with Complex {

  def reqs = datatype.reqs
}
