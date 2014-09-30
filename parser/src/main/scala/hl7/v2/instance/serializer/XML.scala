/*package hl7.v2.instance
package serializer

import scala.xml.Elem

trait XML[A] {
  def xml( value: A ): scala.xml.Elem
}

object XML {

  implicit object componentXML extends XML[Component] {
    def xml(c: Component): Elem = 
      <Component 
          Name ={c.model.name}
          Usage={c.model.usage.toString}
          DT   ={c.model.datatype.name}
          LEN  ={c.model.length.toString}
          CLen ={c.model.confLength}
          Table={c.model.table.getOrElse("")} 
          Location={c.location.toString}
      >{
        c match {
          case s: SimpleComponent  => s.value
          case c: ComplexComponent => c.components.flatten.map( implicitly[XML[Component]].xml )
        }
      }</Component>
  }

  implicit object fieldXML extends XML[Field] {
    def xml(f: Field): Elem = 
      <Field
          Name ={f.model.name}
          Usage={f.model.usage.toString}
          DT   ={f.model.datatype.name}
          Card ={f.model.cardinality.toString}
          LEN  ={f.model.length.toString}
          CLen ={f.model.confLength}
          Table={f.model.table.getOrElse("")} 
          Location={f.location.toString}
      > { f match {
            case s: SimpleField => s.value
            case c: ComplexField => c.components.flatten.map( implicitly[XML[Component]].xml )
        }}
      </Field>
  }

  implicit object segXML extends XML[Segment] {
    def xml(s: Segment): Elem = 
      <Segment
          Name={s.model.ref.name}
          Usage={s.model.usage.toString}
          Card ={s.model.cardinality.toString}
          Location={s.location.toString}
      >
        { s.fields.flatten.map( implicitly[XML[Field]].xml ) }
      </Segment>
  }

  implicit object groupXML extends XML[Group] {
    def xml(g: Group): Elem = 
      <Group 
          Name ={g.model.name}
          Usage={g.model.usage.toString}
          Card ={g.model.cardinality.toString}
          Location={g.location.toString}
      >
        {  g.structure map {
            _ match {
              case Left (ls) => ls map implicitly[XML[Segment]].xml
              case Right(lg) => lg map implicitly[XML[Group]].xml
        }}}
      </Group>
  }

  implicit object messageXML extends XML[Message] {
    def xml(m: Message): Elem = 
      <Message ID={m.model.id} Type={m.model.typ} Event={m.model.event} StructID={m.model.structID}>
        {
          <Structure>
            {  m.structure map {
              _ match {
                case Left (ls) => ls map implicitly[XML[Segment]].xml
                case Right(lg) => lg map implicitly[XML[Group]].xml
            }}}
          </Structure>
          <Invalid>{ m.invalid map( l => <Line>{ l._1} # {l._2} </Line> ) }</Invalid>
          <Unexpected>{ m.unexpected map( l => <Line>{ l._1} # {l._2} </Line> ) }</Unexpected>
      }
      </Message>
  }
}
*/