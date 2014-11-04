package hl7.v2.instance

import hl7.v2.profile._

/**
  * Trait representing a field
  */
sealed trait Field extends Element

/**
  * Class representing a simple field
  */
case class SimpleField(
    qProps: QProps,
    location: Location,
    position: Int,
    instance: Int,
    value: Value
) extends Field with Simple

/**
  * Class representing a complex field
  */
case class ComplexField(
    qProps: QProps,
    location: Location,
    position: Int,
    instance: Int,
    children: List[Component with Element], //FIXME: can we add any element here?
    reqs: List[Req],
    hasExtra: Boolean
) extends Field with Complex

object Field {

  /**
    * Regular expression for matching empty field
    */
  private val empty = s"(?:\\s*\\Q$cs\\E*\\s*$ss*\\s*)*"

  /**
    * Creates and returns a field
    * @param v   - The field as string
    * @param dt  - The data type of the field
    * @param l   - The location of the field
    * @param p   - The position of the field
    * @param map - The data type map
    * @return A field object
    */
  def apply(v: String, dt: Datatype, l: Location, p: Int, i: Int)
           (implicit map: Map[String, Datatype]): Option[Field] =
    dt match {
      case pm: Primitive => apply(v, pm, l, p, i)
      case cm: Composite => apply(v, cm, l, p, i)
    }

  /**
    * Creates and returns a simple field
    * @param v   - The value as string
    * @param dt  - The data type
    * @param l   - The location
    * @param p   - The position
    * @param map - The data type map
    * @return A simple field
    */
  private def apply(v: String, dt: Primitive, l: Location, p: Int, i: Int)
                   (implicit map: Map[String, Datatype]): Option[SimpleField] =
    if( v.matches(empty) ) None
    else Some( SimpleField(dt.qProps, l, p, i, value(dt, v)) )

  /**
    * Creates and returns a complex field
    * @param v   - The value as string
    * @param dt  - The data type
    * @param l   - The location
    * @param p   - The position
    * @param map - The data type map
    * @return A complex field
    */
  private def apply(v: String, dt: Composite, l: Location, p: Int, i: Int)
                   (implicit map: Map[String, Datatype]): Option[ComplexField] =
    if( v.matches(empty) ) None
    else {
      val cml = dt.components
      val values = if( isNull(v) ) Array.fill(cml.size)( l.column -> "\"\"")
                   else split(cs, v, l.column)
      val hasExtra = values.size > cml.size
      val children = ( cml zip values map { t =>
        val (m, (col, s)) = t
        val cp = m.req.position
        val loc = l.copy( desc=m.name, path=s"${l.path}.$cp[1]", column=col )
        Component(s, map(m.datatypeId).asInstanceOf[Primitive], loc, cp)
      } ).flatten
      Some( ComplexField(dt.qProps, l, p, i, children, dt.requirements, hasExtra))
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
