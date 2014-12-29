package hl7.v2.instance

import hl7.v2.profile.{Datatype, Req, Composite, Primitive}

/**
  * Trait representing a field
  */
sealed trait Field extends Element {
  def datatype: Datatype
  def req: Req
  def location: Location
  def instance: Int
}

/**
  * Class representing a simple field
  */
case class SimpleField(
    datatype: Primitive,
    req: Req,
    location: Location,
    instance: Int,
    value: Value
) extends Field with Simple

/**
  * Class representing a complex field
  */
case class ComplexField(
    datatype: Composite,
    req: Req,
    location: Location,
    instance: Int,
    children: List[Component],
    hasExtra: Boolean
) extends Field with Complex {

  def reqs = datatype.reqs
}
