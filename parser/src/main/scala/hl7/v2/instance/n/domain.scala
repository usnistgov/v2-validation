package hl7.v2.instance.n

import gen.model.{QKind, Element, Complex, Simple, Value, QProps, Requirement}

/**
  * Query-able HL7 v2x kinds
  */
object Group    extends QKind //FIXME This will probably clash with Group companion object
object Segment  extends QKind
object Datatype extends QKind

/**
  * Trait representing a component
  */
sealed trait Component extends Element

/**
 * Class representing a simple component
 */
case class SComponent (
    value: Value,
    path: String,
    desc: String,
    line: Int,
    column: Int,
    qProps: QProps
) extends Component with Simple

/**
 * Trait representing a complex component
 */
case class CComponent (
    children: List[SComponent],
    path: String,
    desc: String,
    line: Int,
    column: Int,
    qProps: QProps,
    reqs: List[Requirement]
) extends Component with Complex

/**
 * Trait representing a field
 */
sealed trait Field extends Element

/**
 * Class representing a simple field
 */
case class SField(
    value: Value,
    path: String,
    desc: String,
    line: Int,
    column: Int,
    qProps: QProps
) extends Field with Simple

/**
 * Class representing a complex field
 */
case class CField(
    children: List[Component],
    path: String,
    desc: String,
    line: Int,
    column: Int,
    qProps: QProps,
    reqs: List[Requirement]
) extends Field with Complex

/**
 * Trait representing either a segment or a group
 */
sealed trait SegOrGroup extends Complex { val column = 1 }

/**
 * Class representing a segment
 */
case class Segment (
    children: List[Field],
    path: String,
    desc: String,
    line: Int,
    qProps: QProps,
    reqs: List[Requirement]
) extends SegOrGroup

/**
 * Class representing a group
 */
case class Group (
    children: List[SegOrGroup],
    path: String,
    desc: String,
    qProps: QProps,
    reqs: List[Requirement]
) extends SegOrGroup {

  // The group should contain an element with position = 1 and instance = 1
  require( children exists {c => c.qProps.position == 1 && c.qProps.instance == 1} )

  def head: Segment =
    children find { c => c.qProps.position == 1 && c.qProps.instance == 1 } match {
      case Some(s: Segment) => s
      case Some(g: Group)   => g.head
      case None => throw new Error(s"The group head is missing. $this")
    }

  def line = head.line
}

/**
  * Class representing a message
  */
case class Message (
    children: List[SegOrGroup],
    id: String,
    event: String,
    typ: String,
    desc: String,
    qProps: QProps,
    reqs: List[Requirement]
) {

  // The first element of a message must be MSH
  require( asGroup.head.path.take(3) == "MSH",
    s"The first element of a message must be the MSH segment. $this" )

  lazy val asGroup = Group(children, id, desc, qProps, reqs)
}
