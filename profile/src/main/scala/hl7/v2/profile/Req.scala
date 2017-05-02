package hl7.v2.profile

case class Req(
  position: Int,
  description: String,
  usage: Usage,
  cardinality: Option[Range],
  length: Option[Range],
  confLength: Option[String],
  vsSpec: List[ValueSetSpec],
  confRange : Option[Range],
  hide: Boolean)

object Req {
  def apply(
    position: Int,
    description: String,
    usage: Usage,
    cardinality: Option[Range],
    length: Option[Range],
    confLength: Option[String],
    vsSpec: List[ValueSetSpec]): Req = {
    Req(position, description, usage, cardinality, length, confLength, vsSpec, None, false)
  }
  def apply(
    position: Int,
    description: String,
    usage: Usage,
    cardinality: Option[Range],
    length: Option[Range],
    confLength: Option[String],
    vsSpec: List[ValueSetSpec],
    confRange : Option[Range]): Req = {
    Req(position, description, usage, cardinality, length, confLength, vsSpec, confRange, false)
  }
  def apply(
    position: Int,
    description: String,
    usage: Usage,
    cardinality: Option[Range],
    length: Option[Range],
    confLength: Option[String],
    vsSpec: List[ValueSetSpec],
    hide : Boolean): Req = {
    Req(position, description, usage, cardinality, length, confLength, vsSpec, None, hide)
  }
}