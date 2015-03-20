package hl7.v2.profile

case class Req (
    position: Int,
    usage: Usage,
    cardinality: Option[Range],
    length: Option[Range],
    confLength: Option[String],
    vsSpec: List[ValueSetSpec]
)
