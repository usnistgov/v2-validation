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
        Type={m.`type`}
        Event={m.event}
        StructID={m.structId}
        Description={m.desc}
    >
      {
        m.root.structure map { s =>
          s match {
            case (r, Left(id))   => segmentRef(id, r)
            case (r, Right(gg)) => group(gg, r)
          }
        }
      }
    </Message>

  def group( g: Group, r: Req ): Elem =
    <Group Name={g.name}
           Usage={r.usage.toString}
           Min={r.cardinality.get.min.toString}
           Max={r.cardinality.get.min.toString}
    >
      {
        g.structure map { s =>
          s match {
            case (r, Left(id))   => segmentRef(id, r)
            case (r, Right(gg)) => group(gg, r)
          }
        }
      }
    </Group>

  def segmentRef( id: String, r: Req ) =
    <Segment
        Ref={id}
        Usage={r.usage.toString}
        Min={r.cardinality.get.min.toString}
        Max={r.cardinality.get.min.toString}
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
        { dm.map map ( t => <Case Value={t._1} Datatype={t._2}/> ) }
      </Mapping>
    </DynamicMapping>

  def field(f: Field) =
      <Field
        Name={f.name}
        Datatype={f.datatypeId}
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
        Datatype={c.datatypeId}
        Usage={c.req.usage.toString}
        MinLength={c.req.length.get.min.toString}
        MaxLength={c.req.length.get.max}
        ConfLength={c.req.confLength.orNull}
        Table={c.req.table.orNull}
      />
}
