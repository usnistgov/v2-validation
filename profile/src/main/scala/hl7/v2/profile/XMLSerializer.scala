package hl7.v2.profile

/**
  * Module to serialize a profile to XML
  * 
  * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
  */

object XMLSerializer {

  def serialize( p: Profile ) =
    <ConformanceProfile ID={p.id} Type={p.typ} HL7Version={p.hl7Version} SchemaVersion={p.schemaVersion}>
      {
        <Messages>{ p.messages.values  map message }</Messages>
        <Segments>{ p.segments.values  map segment }</Segments>
        <Datatypes>{ p.datatypes.values map datatype }</Datatypes>
      }
    </ConformanceProfile>

  def message( m: Message ) = 
    <Message ID={m.name} StructID={m.name} Description={m.description}>
      { m.children map ( _ match { case Left(sr) => segmentRef(sr) case Right(g) => group(g) } ) }
    </Message>

  def group( g: Group ): scala.xml.Elem = 
    <Group Name={g.name} Usage={g.usage.toString} Min={g.cardinality.min.toString} Max={g.cardinality.max}>
      { g.children map ( _ match { case Left(sr) => segmentRef(sr) case Right(g) => group(g) } ) }
    </Group>

  def segmentRef( sr: SegmentRef ) = 
    <Segment Ref={sr.ref.name} Usage={sr.usage.toString} Min={sr.cardinality.min.toString} Max={sr.cardinality.max}/>

  def segment( s: Segment ) = 
    <Segment ID={s.id} Name={s.name} Description={s.description}>
      { s.dynamicMapping map dynamicMapping }
      { s.fields map field }
    </Segment>

  def dynamicMapping( dm: DynamicMapping ) = 
    <DynamicMapping>
      <Mapping Position={dm.position.toString} Reference={dm.reference.toString}>
        { dm.map map mapping }
      </Mapping>
    </DynamicMapping>

  def mapping( t: (String, Datatype) ) = <Case Value={t._1} Datatype={t._2.id}/>

  def field(f: Field) =
    <Field 
        Name={f.name}
        Datatype={f.datatype.name}
        Usage={f.usage.toString()}
        Min={f.cardinality.min.toString}
        Max={f.cardinality.max}
        MinLength={f.length.min.toString}
        MaxLength={f.length.max}
        ConfLength={f.confLength match{ case "" => null; case s => s }}
        Table={f.table.getOrElse(null)}
    />

  def datatype(d: Datatype) = 
    <Datatype ID={d.id}  Name={d.name} Description={d.description}>
      { d.components map component }
    </Datatype>

  def component(c: Component) =
    <Component 
        Name={c.name}
        Datatype={c.datatype.name}
        Usage={c.usage.toString()}
        MinLength={c.length.min.toString}
        MaxLength={c.length.max}
        ConfLength={c.confLength match{ case "" => null; case s => s }}
        Table={c.table.getOrElse(null)}
     />
}
