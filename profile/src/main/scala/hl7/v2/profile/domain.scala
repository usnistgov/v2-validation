package hl7.v2.profile

/**
  * Trait representing a data type
  */
sealed trait Datatype {
  def id: String
  def name: String
  def desc: String
}

/**
  * A primitive data type
  */
case class Primitive( id: String, name: String, desc: String ) extends Datatype

/**
  * A composite data type
  */
case class Composite(
    id: String,
    name: String,
    desc: String,
    components: List[Component]
) extends Datatype {

  lazy val reqs: List[Req] = components map ( _.req )
}

/**
  * A composite data type component
  */
case class Component( name: String, datatype: Datatype, req: Req )

/**
  * A segment field
  */
case class Field( name: String, datatype: Datatype, req: Req )

/**
  * Describes the mapping for dynamic data type
  * @param position  - The position of the element with dynamic data type
  * @param reference - The position which defines the data type name to be used
  * @param map       - The mapping ( data type name -> data type id )
  */
case class DynMapping( position: Int, reference: Int, map: Map[String, Datatype] )

/**
  * A segment
  */
case class Segment(
    id: String,
    name: String,
    desc: String,
    fields: List[Field],
    mappings: List[DynMapping]
) {

  lazy val reqs: List[Req] = fields map ( _.req )
}

/**
  * Trait representing either a segment reference or a group
  */
sealed trait SegRefOrGroup {
  def req: Req
  def reqs: List[Req]
}

/**
  * A segment reference
  */
case class SegmentRef( req: Req, ref: Segment ) extends SegRefOrGroup {
  def reqs = ref.reqs
}

/**
  * A group
  */
case class Group(
    name: String,
    structure: List[SegRefOrGroup],
    req: Req
) extends SegRefOrGroup {

  lazy val reqs: List[Req] = structure map ( _.req )
}

/**
  * A message
  */
case class Message (
    id: String,
    structId: String,
    event: String,
    typ  : String,
    desc : String,
    structure: List[SegRefOrGroup]
) {

  lazy val asGroup = Group(structId, structure,
        Req(1, structId, Usage.R, None, None, None, Nil))
}

/**
  * A profile
  */
case class Profile(
    id: String,
    messages : Map[String, Message],
    segments : Map[String, Segment],
    datatypes: Map[String, Datatype]
)
