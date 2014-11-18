package hl7.v2.profile

import scala.xml.Elem

/**
 * Module to serialize a profile to XML
 *
 * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
 */

object XMLSerializer {

  //FIXME @Type, @HL7Version and @SchemaVersion are required
  def serialize( p: Profile ): Elem =
    <ConformanceProfile ID={p.id}
        Type="Constrainable" HL7Version="2.5.1" SchemaVersion="2.5"
    >
      {
        <Messages>{  p.messages.values  map message  }</Messages>
        <Segments>{  p.segments.values  map segment  }</Segments>
        <Datatypes>{ p.datatypes.values map datatype }</Datatypes>
      }
    </ConformanceProfile>

  def message( m: Message ) =
    <Message
        ID={m.id}
        Type={m.typ}
        Event={m.event}
        StructID={m.structId}
        Description={m.desc}
    >
      {
        m.structure map {
          case s: SegmentRef  => segmentRef(s)
          case g: Group => group(g)
        }
      }
    </Message>

  def group( g: Group): Elem =
    <Group Name={g.name}
           Usage={g.req.usage.toString}
           Min={g.req.cardinality.get.min.toString}
           Max={g.req.cardinality.get.min.toString}
    >
      {
        g.structure map {
          case s: SegmentRef  => segmentRef(s)
          case g: Group => group(g)
        }
      }
    </Group>

  def segmentRef( s: SegmentRef ) =
    <Segment
        Ref={s.ref.name}
        Usage={s.req.usage.toString}
        Min={s.req.cardinality.get.min.toString}
        Max={s.req.cardinality.get.min.toString}
    />

  def segment( s: Segment ) =
    <Segment ID={s.id} Name={s.name} Description={s.desc}>
      { s.mappings map dynamicMapping }
      { s.fields map field }
    </Segment>

  def dynamicMapping( dm: DynMapping ) =
    <DynamicMapping>
      <Mapping
          Position={dm.position.toString}
          Reference={dm.reference.toString}
      >
        { dm.map map ( t => <Case Value={t._1} Datatype={t._2.name}/> ) }
      </Mapping>
    </DynamicMapping>

  def field(f: Field) =
      <Field
        Name={f.name}
        Datatype={f.datatype.name}
        Usage={f.req.usage.toString}
        MinLength={f.req.length.get.min.toString}
        MaxLength={f.req.length.get.max}
        ConfLength={f.req.confLength.orNull}
        Table={f.req.table.orNull}
        Min={f.req.cardinality.get.min.toString}
        Max={f.req.cardinality.get.max}
      />

  def datatype(d: Datatype) =
    <Datatype ID={d.id}  Name={d.name} Description={d.desc}>
      {
        d match {
          case Primitive(_, _, _)     =>
          case Composite(_, _, _, xs) => xs map component
        }
      }
    </Datatype>

  def component(c: Component) =
      <Component
        Name={c.name}
        Datatype={c.datatype.name}
        Usage={c.req.usage.toString}
        MinLength={c.req.length.get.min.toString}
        MaxLength={c.req.length.get.max}
        ConfLength={c.req.confLength.orNull}
        Table={c.req.table.orNull}
      />
}
