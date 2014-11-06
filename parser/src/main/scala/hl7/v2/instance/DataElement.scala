package hl7.v2.instance

import hl7.v2.profile.{Field => FM, Component => CM, Composite, Primitive}

object DataElement {

  /**
    * Creates and returns a field object
    * @param m - The field model
    * @param v - The value as string
    * @param l - The location
    * @param i - The instance (repetition) number
    * @return A field object
    */
  def apply(m: FM, v: String, l: Location, i: Int): Option[Field] =
    if( v matches emptyField ) None
    else Some {
      m.datatype match {
        case p: Primitive => SimpleField(p, l, m.req.position, i, value(p, v))
        case c: Composite =>
          val(hasExtra, components) = children(l, c.components, v, cs)
          ComplexField(c, l, m.req.position, i, components, hasExtra)
      }
    }

  /**
    * Creates and returns a component object
    * @param m - The component model
    * @param v - The value as string
    * @param l - The location
    * @return A component object
    */
  def apply(m: CM, v: String, l: Location): Option[Component] =
    if( v matches emptyComponent ) None
    else Some {
      m.datatype match {
        case p: Primitive => SimpleComponent(p, l, m.req.position, value(p, v))
        case c: Composite =>
          val(hasExtra, r) = children(l, c.components, v, ss)
          val components  = r.asInstanceOf[List[SimpleComponent]]
          ComplexComponent(c, l, m.req.position, components, hasExtra)
      }
    }

  /**
    * Creates and returns the list of components
    */
  private def children(l: Location, ml: List[CM], v: String, sep: Char):
                      (Boolean, List[Component]) = {
    val vs = if( isNull(v) ) Array.fill( ml.size )( l.column -> "\"\"")
             else split(sep, v, l.column)
    val hasExtra = vs.size > ml.size
    val _children = ml zip vs map { t =>
      val (m, (col, vv)) = t
      val pos = m.req.position
      val loc = l.copy( desc=m.name, path=s"${l.path}.$pos[1]", column=col )
      apply(m, vv, loc)
    }
    (hasExtra, _children.flatten)
  }

  /**
    * Creates and returns an HL7 hl7.v2.instance.Value
    * @param p - The data type
    * @param v - The values as String
    * @return An hl7.v2.instance.Value
    */
  private implicit def value(p: Primitive, v: String) = Value(p.name, v)

  /**
    * Returns if the value is Null i.e. ""
    */
  private def isNull(v: String) = "\"\"" == v

  /**
    * Regular expression to match an empty component
    */
  private val emptyComponent = s"(?:\\s*$ss*\\s*)*"

  /**
    * Regular expression to match an empty field
    */
  private val emptyField = s"(?:\\s*\\Q$cs\\E*\\s*$ss*\\s*)*"

}
