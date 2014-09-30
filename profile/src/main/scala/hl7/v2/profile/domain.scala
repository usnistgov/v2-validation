package hl7.v2.profile

/**
  * Trait representing a data type
  */
sealed trait Datatype {
  def id: String
  def name: String
  def desc: String
  lazy val qProps = QProps(DT, id, name)
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

  lazy val qProps = QProps(SEG, id, name)

  lazy val requirements: List[Req] = fields map ( _.req )
}

case class Group(
    name: String,
    structure: List[(Req, Either[String, Group])]
) {

  lazy val qProps = QProps(GRP, "", name) //FIXME: Review the id
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
    id: String,
    messages : Map[String, Message],
    segments : Map[String, Segment],
    datatypes: Map[String, Datatype]
)
