package hl7.v2.instance

import hl7.v2.profile.{QProps, Req, Datatype, Primitive, Composite}

/**
  * Trait representing a component
  */
sealed trait Component extends Element { val instance = 1 }

/**
  * Class representing a simple component
  */
case class SimpleComponent (
    qProps: QProps,
    location: Location,
    position: Int,
    value: Value
) extends Component with Simple

/**
  * Class representing a complex component
  */
case class ComplexComponent (
    qProps: QProps,
    location: Location,
    position: Int,
    children: List[SimpleComponent],
    reqs: List[Req],
    hasExtra: Boolean
) extends Component with Complex

object Component {

  /**
    * Regular expression for matching empty components
    */
  private val empty = s"(?:\\s*\\s*$ss*\\s*)*"

  /**
    * Creates and returns a component object
    * @param v   - The component as string
    * @param dt  - The data type of the component
    * @param l   - The location of the component
    * @param p   - The position of the component
    * @param map - The data type map
    * @return The component object
    */
  def apply(v: String, dt: Datatype, l: Location, p: Int)
           (implicit map: Map[String, Datatype]): Option[Component] =
    dt match {
      case pm: Primitive => apply(v, pm, l, p)
      case cm: Composite => apply(v, cm, l, p)
    }

  /**
    * Creates and returns a simple component
    * @param v   - The value as string
    * @param dt  - The data type
    * @param l   - The location
    * @param p   - The position
    * @param map - The data type map
    * @return A simple component
    */
  private def apply(v: String, dt: Primitive, l: Location, p: Int)
                   (implicit map: Map[String, Datatype]): Option[SimpleComponent] =
    if( v.matches(empty) ) None
    else Some(SimpleComponent(dt.qProps, l, p, value(dt, v)))

  /**
    * Creates and returns a complex component
    * @param v   - The value as string
    * @param dt  - The data type
    * @param l   - The location
    * @param p   - The position
    * @param map - The data type map
    * @return A complex component
    */
  private def apply(v: String, dt: Composite, l: Location, p: Int)
                   (implicit map: Map[String, Datatype]): Option[ComplexComponent] =
    if( v.matches(empty) ) None
    else {
      val cml = dt.components
      val values = if( isNull(v) ) Array.fill(cml.size)( l.column -> "\"\"")
                   else split(ss, v, l.column)
      val hasExtra = values.size > cml.size
      val children = ( cml zip values map { t =>
        val (m, (col, s)) = t
        val cp = m.req.position
        val loc = l.copy( desc=m.name, path=s"${l.path}.$cp[1]", column=col )
        apply(s, map(m.datatypeId).asInstanceOf[Primitive], loc, cp)
      } ).flatten
      Some(ComplexComponent(dt.qProps, l, p, children, dt.requirements, hasExtra))
    }
}
