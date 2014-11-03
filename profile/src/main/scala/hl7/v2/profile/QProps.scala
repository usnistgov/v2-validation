package hl7.v2.profile

sealed trait QType

object QType {
  object DT extends QType
  object SEG extends QType
  object GRP extends QType
  object MSG extends QType
}

case class QProps( `type`: QType, id: String, name: String ) {
  assert( id.nonEmpty,   s"The id is empty. $this" )
  assert( name.nonEmpty, s"The name is empty. $this" )
}
