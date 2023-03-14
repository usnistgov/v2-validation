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
  hide: Boolean,
  constantValue: Option[String])