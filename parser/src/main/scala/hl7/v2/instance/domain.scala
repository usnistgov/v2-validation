package hl7.v2.instance

import hl7.v2.profile.{DT, QProps, Req}

/**
  * Trait representing a component
  */
sealed trait Component /*extends Element*/ { val instance = 1 }

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
    children: List[SComponent],
    reqs: List[Req],
    hasExtra: Boolean
) extends Component with Complex

/**
  * Trait representing a field
  */
sealed trait Field /*extends Element*/

object NField extends Field {
  val qProps: QProps = QProps(DT, "###", "###")
  val location: Location = Location("", "", -1, -1)
  val position: Int = -1
  val instance: Int = -1
}

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
    children: List[Component with Element], //FIXME: can we add any element here?
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
    children: List[Field with Element], //FIXME: can we add any element here?
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

/*

"HL7 does not care how systems actually store data within an application. When fields are transmitted, they are sent as character strings. Except where noted, HL7 data fields may take on the null value. Sending the null value, which is transmitted as two double quote marks (""), is different from omitting an optional data field. The difference appears when the contents of a message will be used to update a record in a database rather than create a new one. If no value is sent, (i.e., it is omitted) the old value should remain unchanged. If the null value is sent, the old value should be changed to null. For further details, see Section 2.6, "Message construction rules".

The above is little obscure.

Here is what I think. I believe that the data type of the file should be taken into account.
So for primitive field there is no problem the will be "" and will keep its semantic.

For complex and depending on the data type Null ("") means every primitive component/sub component should be null.
"" <=> ""^""^""^""^""^ <=> ""^""&""&""^""   ( <=> means equivalent)
In term of validation

Usage:
     "" and R => OK
      "" and X or W => Error or Warning

Cardinality

""~""....

 */