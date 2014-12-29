package hl7.v2.instance

import hl7.v2.profile.{Datatype, Req, Primitive, Composite}

/**
  * Trait representing a component
  */
sealed trait Component extends Element {
  def datatype: Datatype
  def req: Req
  def location: Location
  val instance = 1
}

/**
  * Class representing a simple component
  */
case class SimpleComponent(
    datatype: Primitive,
    req: Req,
    location: Location,
    value: Value
) extends Component with Simple

/**
  * Class representing a complex component
  */
case class ComplexComponent (
    datatype: Composite,
    req: Req,
    location: Location,
    children: List[SimpleComponent],
    hasExtra: Boolean
) extends Component with Complex {

  def reqs = datatype.reqs
}
