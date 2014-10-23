package hl7.v2.instance

import hl7.v2.profile.{QProps, Req}

/**
  * Trait representing a field
  */
sealed trait Field /*extends Element*/

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
