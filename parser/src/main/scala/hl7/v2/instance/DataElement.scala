package hl7.v2.instance

import hl7.v2.profile.{Field => FM, Component => CM, Composite, Primitive, Datatype}


object DataElement {

  def apply(m: FM, v: String, l: Location, ins: Int)
           (implicit map: Map[String, Datatype]): Option[Field] = {
    val pos = m.req.position

    ???
  }

  def apply(m: CM, v: String, l: Location)
           (implicit map: Map[String, Datatype]): Option[Component] = {
    val pos = m.req.position
    val dt  = map( m.datatypeId )

    if( v matches emptyComponent )
      None
    else dt match {
      case p: Primitive => Some(SimpleComponent(dt.qProps, l, pos, value(p, v)))
      case c: Composite => ???
    }


    ???
  }


  private
  def children(l: Location, ml: List[CM], vv: String, sep: Char)
              (implicit map: Map[String, Datatype]): (Boolean, List[Option[Component]]) = {
    val vs = createArray( vv, sep, l.column, ml.size )
    val hasExtra = vs.size > ml.size
    val _children = ml zip vs map { t =>
      val (m, (col, v)) = t
      val pos = m.req.position
      val loc = l.copy( desc=m.name, path=s"${l.path}.$pos[1]", column=col )
      apply(m, v, loc)
    }
    (hasExtra, _children)
  }

  private def createArray(v: String, sep: Char, col: Int, max: Int) =
    if( isNull(v) ) Array.fill(max)( col -> "\"\"") else split(sep, v, col)

  private val emptyComponent = s"(?:\\s*\\Q${cs}\\E*\\s*${ss}*\\s*)*"

  private val emptyField = s"(?:\\s*\\Q${cs}\\E*\\s*${ss}*\\s*)*"

}
