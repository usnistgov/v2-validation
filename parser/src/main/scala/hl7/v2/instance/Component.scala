package hl7.v2.instance

import hl7.v2.profile.{Component => CM, Primitive, Composite}

/**
  * Trait representing a component
  */
sealed trait Component extends Element {
  def model: CM
  def location: Location
  def position = model.req.position
  val instance = 1
}

/**
  * Class representing a simple component
  */
case class SimpleComponent(
    model: CM,
    location: Location,
    value: Value
) extends Component with Simple {

  require( model.datatype.isInstanceOf[Primitive] )
}

/**
  * Class representing a complex component
  */
case class ComplexComponent (
    model: CM,
    location: Location,
    children: List[SimpleComponent],
    hasExtra: Boolean
) extends Component with Complex {

  require( model.datatype.isInstanceOf[Composite] )

  def reqs = model.datatype.asInstanceOf[Composite].reqs
}
