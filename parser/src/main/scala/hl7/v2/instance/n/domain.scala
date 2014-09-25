package hl7.v2.instance.n

import gen.model.{QKind, Element, Complex, Simple, Value, QProps, Requirement}

/**
  * Query-able HL7 v2x kinds
  */
object GROUP    extends QKind
object SEGMENT  extends QKind
object DATATYPE extends QKind

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
sealed trait SegOrGroup extends Complex

/**
 * Class representing a segment
 */
case class Segment (
    children: List[Field],
    path: String,
    desc: String,
    line: Int,
    column: Int,
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
    line: Int,
    column: Int,
    qProps: QProps,
    reqs: List[Requirement]
) extends SegOrGroup

