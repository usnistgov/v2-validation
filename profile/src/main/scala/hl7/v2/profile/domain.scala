package hl7.v2.profile

/**
  * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
  */

/**
  * Generic element model
  */
trait EM {
  def usage: Usage
  def cardinality: Range
  def position: Int
}

/**
  * Data element model (either a field or a component model)
  */
trait DEM extends EM {
  def datatype: Datatype
  def length: Range
  def table: Option[String]
}

/**
  * Trait Representing a data type
  */
sealed trait Datatype {
  def id: String
  def name: String
  def description: String
  assert(id.trim.nonEmpty, s"The id cannot be blank # $this")
  assert(name.trim.nonEmpty, s"The name cannot be blank # $this")
}

/**
  * A primitive data type
  */
case class Primitive(id: String, name: String, description: String) extends Datatype

/**
  * A composite data type
  */
case class Composite(
    id: String,
    name: String,
    description: String,
    components: Seq[Component] 
  ) extends Datatype {
  assert( components.nonEmpty, s"Composite datatype must have at least one component # $this" )
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
  ) extends DEM {
  assert( isWellNested, s"""The component is not well nested # $this""" )

  //Checks for component nesting
  private def isWellNested = datatype match {
    case p: Primitive => true
    case c: Composite => c.components.forall( _.datatype.isInstanceOf[Primitive] )
  }

  lazy val cardinality = Range(1, "1")
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
) extends DEM

case class DynamicMapping(position: Int, reference: Int, map: Map[String, Datatype])

/**
  * A segment
  */
case class Segment(id: String, name: String, description: String, fields: Seq[Field], dynamicMapping: List[DynamicMapping] ) {
  assert(id.trim.nonEmpty, s"The id cannot be blank # $this")
  assert(name.trim.nonEmpty, s"The name cannot be blank # $this")
}

/**
  * A segment reference
  */
case class SegmentRef(position: Int, ref: Segment, usage: Usage, cardinality: Range) extends EM

/**
  * A group
  */
case class Group(
    position: Int,
    name: String,
    usage: Usage,
    cardinality: Range,
    children: Seq[Either[SegmentRef, Group]]
  ) extends EM {
  assert(name.trim.nonEmpty, s"The name cannot be blank # $this")
  assert( children.nonEmpty, s"A group cannot be empty # $this" )
}

/**
  * A message
  */
case class Message(name: String, description: String, children: Seq[Either[SegmentRef, Group]]) {
  assert(name.trim.nonEmpty, s"The name cannot be blank # $this")
  assert( children.nonEmpty, s"A message cannot be empty # $this" )
  assert( children.head match {case Left(x) => x.ref.name == "MSH" case _ => false},
      s"The first element of the message must be the MSH Segment # $this" )

  def asGroup = Group(1, name, Usage.R, Range(1,"*"), children)
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