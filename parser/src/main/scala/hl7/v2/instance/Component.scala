package hl7.v2.instance

import hl7.v2.profile.{Datatype, Primitive, Composite}

/**
  * Trait representing a component
  */
sealed trait Component extends Element {
  def datatype: Datatype
  def location: Location
  def position: Int
  val instance = 1
}

/**
  * Class representing a simple component
  */
case class SimpleComponent(
    datatype: Primitive,
    location: Location,
    position: Int,
    value: Value
) extends Component with Simple

/**
  * Class representing a complex component
  */
case class ComplexComponent (
    datatype: Composite,
    location: Location,
    position: Int,
    children: List[SimpleComponent],
    hasExtra: Boolean
) extends Component with Complex {

  def reqs = datatype.reqs
}
