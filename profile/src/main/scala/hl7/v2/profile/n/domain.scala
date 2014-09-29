package hl7.v2.profile.n

import hl7.v2.profile.Range
import hl7.v2.profile.Usage

/**
  * Trait
  */
sealed trait QType
object DT  extends QType
object SEG extends QType
object GRP extends QType
object MSG extends QType

case class QProps( `type`: QType, id: String, name: String, desc: String ) {
  assert( id.nonEmpty,   s"The id is empty. $this" )
  assert( name.nonEmpty, s"The name is empty. $this" )
}

case class Req (
  position: Int,
  usage: Usage,
  cardinality: Option[Range],
  length: Option[Range],
  conLength: Option[String],
  table: Option[String]
)

/**
  * Trait representing a datatype
  */
sealed trait Datatype {
  def id: String
  def name: String
  def desc: String
  lazy val qProps = QProps(DT, id, name, desc)
}

case class Primitive(id: String, name: String, desc: String) extends Datatype

case class Composite (
    id: String,
    name: String,
    desc: String,
    components: List[Component]
) extends Datatype {

  lazy val requirements: List[Req] = components map ( _.req )
}

case class Component( name: String, datatypeId: String, req: Req )

case class Field( name: String, datatypeId: String, req: Req )

/**
  * Describes the mapping for dynamic data type
  * @param position  - The position of the element with dynamic data type
  * @param reference - The position which defines the data type name to be used
  * @param map       - The mapping ( data type name -> data type id )
  */
case class DynMapping( position: Int, reference: Int, map: Map[String, String] )

case class Segment (
    id: String,
    name: String,
    desc: String,
    fields: List[Field],
    mappings: List[DynMapping]
) {

  lazy val qProps = QProps( SEG, id, name, desc)

  lazy val requirements: List[Req] = fields map ( _.req )
}

case class Group(
    name: String,
    structure: List[(Req, Either[String, Group])]
) {

  lazy val qProps = QProps(GRP, "", name, "") //FIXME: Review the id
}

case class Message (
    id: String,
    structId: String,
    event: String,
    `type`: String,
    desc: String,
    root: Group
)

case class Profile(
    uuid: String,
    messages : Map[String, Message],
    segments : Map[String, Segment],
    datatypes: Map[String, Datatype]
)
