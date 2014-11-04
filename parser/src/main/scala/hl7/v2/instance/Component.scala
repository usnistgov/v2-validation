package hl7.v2.instance

import hl7.v2.profile.{QProps, Req, Datatype, Primitive, Composite}

/**
  * Trait representing a component
  */
sealed trait Component { val instance = 1 }

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

  private val empty = s"(?:\\s*\\Q$cs\\E*\\s*$ss*\\s*)*"

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

  private def apply(v: String, pm: Primitive, l: Location, p: Int)
           (implicit map: Map[String, Datatype]): Option[SimpleComponent] =
    if( v.matches(empty) ) None
    else Some(SimpleComponent(pm.qProps, l, p, value(pm, v)))

  private def apply(v: String, dt: Composite, l: Location, p: Int)
           (implicit map: Map[String, Datatype]): Option[ComplexComponent] =
    if( v.matches(empty) ) None
    else {
      val cml = dt.components
      // If the value is Null "" then all sub components will be Null
      val values = if( "\"\"" == v ) Array.fill(cml.size)( l.column -> "\"\"")
                   else split(ss, v, l.column)
      val hasExtra = values.size > cml.size
      val cs = children(cml, values, l)
      Some(ComplexComponent(dt.qProps, l, p, cs, dt.requirements, hasExtra))
    }

  type CM = hl7.v2.profile.Component

  private def children(cml: List[CM], vs: Array[(Int, String)], l: Location)
        (implicit map: Map[String, Datatype]): List[SimpleComponent] = {
    var cp = 0
    val r = cml zip vs map { t =>
      cp = cp + 1
      val (m, (col, s)) = t
      val loc = l.copy( desc=m.name, path=s"${l.path}.$cp[1]", column=col )
      apply(s, map(m.datatypeId).asInstanceOf[Primitive], loc, cp)
    }
    r.flatten
  }
}
