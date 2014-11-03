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
      val values = split(ss, v, l.column)
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

/*

"HL7 does not care how systems actually store data within an application. When fields are transmitted, they are sent as character strings. Except where noted, HL7 data fields may take on the null value. Sending the null value, which is transmitted as two double quote marks (""), is different from omitting an optional data field. The difference appears when the contents of a message will be used to update a record in a database rather than create a new one. If no value is sent, (i.e., it is omitted) the old value should remain unchanged. If the null value is sent, the old value should be changed to null. For further details, see Section 2.6, "Message construction rules".

The above is little obscure.

Here is what I think. I believe that the data type of the file should be taken into account.
So for primitive field there is no problem the will be "" and will keep its semantic.

For complex and depending on the data type Null ("") means every primitive component/sub component should be null.
"" <=> ""^""^""^""^""^ <=> ""^""&""&""^""   ( <=> means equivalent)
In term of validation

Usage:
     "" and R => OK
      "" and X or W => Error or Warning

Cardinality

""~""....

 */