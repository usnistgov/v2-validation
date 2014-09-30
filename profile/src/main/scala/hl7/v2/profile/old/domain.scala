package hl7.v2.profile.old

import hl7.v2.profile.Range
import hl7.v2.profile.Usage

/**
  * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
  */

/**
  * A data type
  */
case class Datatype(
    id: String,
    name: String,
    description: String,
    components: List[Component] 
  ) {
  assert(id.trim.nonEmpty, s"The id cannot be blank # $this")
  assert(name.trim.nonEmpty, s"The name cannot be blank # $this")
}

/**
  * A composite data type component
  */
case class Component(
    position: Int,
    name: String,
    datatype: Datatype,
    usage: Usage,
    length: Range,
    confLength: String,
    table: Option[String]
  ) {
  /*assert( isWellNested, s"""The component is not well nested # $this""" )

  //Checks for component nesting
  private def isWellNested = datatype match {
    case p: Primitive => true
    case c: Composite => c.components.forall( _.datatype.isInstanceOf[Primitive] )
  }*/

  //lazy val cardinality = Range(1, "1")
}

/**
  * A field
  */
case class Field(
    position: Int,
    name: String,
    datatype: Datatype,
    usage: Usage,
    cardinality: Range,
    length: Range,
    confLength: String,
    table: Option[String]
)

case class DynamicMapping(position: Int, reference: Int, map: Map[String, Datatype])

/**
  * A segment
  */
case class Segment(id: String, name: String, description: String, fields: List[Field], dynamicMapping: List[DynamicMapping] ) {
  assert(id.trim.nonEmpty, s"The id cannot be blank # $this")
  assert(name.trim.nonEmpty, s"The name cannot be blank # $this")
}

/**
  * A segment reference
  */
case class SegmentRef(position: Int, ref: Segment, usage: Usage, cardinality: Range)

/**
  * A group
  */
case class Group(
    position: Int,
    name: String,
    usage: Usage,
    cardinality: Range,
    children: List[Either[SegmentRef, Group]]
  ) {
  assert(name.trim.nonEmpty, s"The name cannot be blank # $this")
  assert( children.nonEmpty, s"A group cannot be empty # $this" )
}

/**
  * A message
  */
case class Message(
    id      : String,
    typ     : String,
    event   : String,
    structID: String, 
    description: String, 
    children: List[Either[SegmentRef, Group]]
  ) {
  assert(id.trim.nonEmpty, s"The id cannot be blank # $this")
  assert( children.nonEmpty, s"A message cannot be empty # $this" )
  assert( children.head match {case Left(x) => x.ref.name == "MSH" case _ => false},
      s"The first element of the message must be the MSH Segment # $this" )

  def asGroup = Group(1, id, Usage.R, Range(1,"1"), children)
}

/**
  * The profile
  */
case class Profile(
    id           : String,
    typ          : String,
    hl7Version   : String,
    schemaVersion: String,
    messages     : Map[String, Message],
    segments     : Map[String, Segment],
    datatypes    : Map[String, Datatype]
)