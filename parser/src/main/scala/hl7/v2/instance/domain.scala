package hl7.v2.instance

import hl7.v2.profile.{QProps, Req}

/**
  * Trait representing a component
  */
sealed trait Component extends Element { val instance = 1 }

/**
  * Class representing a simple component
  */
case class SComponent (
    qProps: QProps,
    location: Location,
    position: Int,
    value: Value
) extends Component with Simple

/**
  * Trait representing a complex component
  */
case class CComponent (
    qProps: QProps,
    location: Location,
    position: Int,
    children: List[Component],
    reqs: List[Req],
    hasExtra: Boolean
) extends Component with Complex

/**
  * Trait representing a field
  */
sealed trait Field extends Element

/**
  * Class representing a simple field
  */
case class SField(
    qProps: QProps,
    location: Location,
    position: Int,
    instance: Int,
    value: Value
) extends Field with Simple

/**
  * Class representing a complex field
  */
case class CField(
    qProps: QProps,
    location: Location,
    position: Int,
    instance: Int,
    children: List[Component],
    reqs: List[Req],
    hasExtra: Boolean
) extends Field with Complex

/**
  * Trait representing either a segment or a group
  */
sealed trait SegOrGroup extends Complex { val column = 1 }

/**
  * Class representing a segment
  */
case class Segment (
    qProps: QProps,
    location: Location,
    position: Int,
    instance: Int,
    children: List[Field],
    reqs: List[Req],
    hasExtra: Boolean
) extends SegOrGroup

/**
  * Class representing a group
  */
case class Group (
    qProps: QProps,
    position: Int,
    instance: Int,
    children: List[SegOrGroup],
    reqs: List[Req]
  ) extends SegOrGroup {

  // The group should contain an element with position = 1 and instance = 1
  require( children exists {c => c.position == 1 && c.instance == 1} )

  val hasExtra = false //FIXME: A group cannot have extra unless it is the root right ?

  lazy val head: Segment =
    children find { c => c.position == 1 && c.instance == 1 } match {
      case Some(s: Segment) => s
      case Some(g: Group)   => g.head
      case None => throw new Error(s"The group head is missing. $this")
    }

  lazy val location =
    head.location.copy( desc="", path = s"${qProps.name}[$instance]" )
}

/**
  * Class representing a message
  */
case class Message (
    id: String,
    structId: String,
    event: String,
    typ: String,
    desc: String,
    children: List[SegOrGroup],
    reqs: List[Req]
) {

  private lazy val qProps = QProps(hl7.v2.profile.MSG, id, structId)

  lazy val asGroup = Group(qProps, -1, 1, children, reqs)

  lazy val location = asGroup.location
}
