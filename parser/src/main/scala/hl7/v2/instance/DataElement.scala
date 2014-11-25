package hl7.v2.instance

import hl7.v2.profile.{ Field => FM, Component => CM, Composite, Primitive }

object DataElement {

  implicit val dtz: Option[TimeZone] = None //FIXME Get the default from MSH

  /**
    * Creates and returns a field object
    * @param m - The field model
    * @param v - The value as string
    * @param l - The location
    * @param i - The instance (repetition) number
    * @return A field object
    */
  def apply(m: FM, v: String, l: Location, i: Int)
           (implicit s: Separators): Option[Field] =
    if( v matches emptyField( cs, ss) ) None
    else Some {
      m.datatype match {
        case p: Primitive => SimpleField(m, l, i, Value(p, v))
        case c: Composite =>
          val(hasExtra, components) = children(l, c.components, v, s.cs)
          ComplexField(m, l, i, components, hasExtra)
      }
    }

  /**
    * Creates and returns a component object
    * @param m - The component model
    * @param v - The value as string
    * @param l - The location
    * @return A component object
    */
  def apply(m: CM, v: String, l: Location)
           (implicit s: Separators): Option[Component] =
    if( v matches emptyComponent( s.ss ) ) None
    else Some {
      m.datatype match {
        case p: Primitive => SimpleComponent(m, l, Value(p, v))
        case c: Composite =>
          val(hasExtra, r) = children(l, c.components, v, s.ss)
          val components  = r.asInstanceOf[List[SimpleComponent]]
          ComplexComponent(m, l, components, hasExtra)
      }
    }

  /**
    * Creates and returns the list of components
    */
  private def children(l: Location, ml: List[CM], v: String, sep: Char)
                      (implicit s: Separators): (Boolean, List[Component]) = {
    val max = ml.size
    val vs = if( isNull(v) ) Array.fill(max)(l.column -> "\"\"") else split(sep, v, l.column)
    val hasExtra = vs.size > max
    val _children = ml zip vs map { t =>
      val (m, (col, vv)) = t
      val pos = m.req.position
      val loc = l.copy( desc=m.name, path=s"${l.path}.$pos[1]", column=col )
      apply(m, vv, loc)
    }
    (hasExtra, _children.flatten)
  }

  /**
    * Returns if the value is Null i.e. ""
    */
  private def isNull(v: String) = v == Value.NULL

  /**
    * Regular expression to match an empty component
    */
  private def emptyComponent(ss: Char) = s"(?:\\s*$ss*\\s*)*"

  /**
    * Regular expression to match an empty field
    */
  private def emptyField(cs: Char, ss: Char) = s"(?:\\s*\\Q$cs\\E*\\s*$ss*\\s*)*"

}
