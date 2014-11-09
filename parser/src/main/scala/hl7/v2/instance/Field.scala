package hl7.v2.instance

import hl7.v2.profile.{Field => FM, Composite, Primitive}

/**
  * Trait representing a field
  */
sealed trait Field extends Element {
  def model: FM
  def location: Location
  def position = model.req.position
  def instance: Int
}

/**
  * Class representing a simple field
  */
case class SimpleField(
    model: FM,
    location: Location,
    instance: Int,
    value: Value
) extends Field with Simple {

  require( model.datatype.isInstanceOf[Primitive] )
}

/**
  * Class representing a complex field
  */
case class ComplexField(
    model: FM,
    location: Location,
    instance: Int,
    children: List[Component],
    hasExtra: Boolean
) extends Field with Complex {

  require( model.datatype.isInstanceOf[Composite] )

  def reqs = model.datatype.asInstanceOf[Composite].reqs
}
