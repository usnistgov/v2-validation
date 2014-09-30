package hl7.v2.profile

import org.specs2.Specification

class XMLDeserializerHelperSpec extends Specification  { def is =s2"""

    Deserialization of segment should work as expected $todo
    Deserialization of dynamic mappings should work as expected $todo
    Deserialization of field should work as expected            $field
    Deserialization of datatype should work as expected         $dt
    Deserialization of requirement should work as expected      $req
    Deserialization of cardinality should work as expected      $card
    Deserialization of length should work as expected           $len
"""

  def field = XMLDeserializerHelper.field( 1,
    <Field
      Name="X"
      Datatype="Y"
      Usage="R"
      Min="1"
      Max="*"
      MinLength="1"
      MaxLength="*"
      ConfLength="=2"
      Table="tt"
     />
  ) === {
    val r = Req(1, Usage.R, Some(Range(1,"*")), Some(Range(1,"*")), Some("=2"), Some("tt"))
    Field("X", "Y", r)
  }

  def dt = Seq (
    <Datatype ID="i1" Name="N1" Description="xx"/>
        -> Primitive("i1", "N1", "xx"),
    <Datatype ID="i2" Name="N2" Description="xx">{
      <Component
        Name="X"
        Datatype="Y"
        Usage="R"
        MinLength="1"
        MaxLength="*"
        ConfLength="=2"
        Table="tt"
      />
    }</Datatype>
      -> {
           val r = Req(1, Usage.R, None, Some(Range(1,"*")), Some("=2"), Some("tt"))
           Composite("i2", "N2", "xx", Component("X", "Y", r) :: Nil )
         }
  ) map ( t => XMLDeserializerHelper.datatype( t._1 ) === t._2 )


  def req = Seq (
    <E Usage="R"/>
        -> Req(1, Usage.R, None, None, None, None),
    <E Usage="R" Min="1" Max="2"/>
        -> Req(1, Usage.R, Some(Range(1, "2")), None, None, None),
    <E Usage="R" Min="1" Max="2" MinLength="1" MaxLength="*"/>
        -> Req(1, Usage.R, Some(Range(1, "2")), Some(Range(1, "*")), None, None),
    <E Usage="R" MinLength="1" MaxLength="*" Table="tt"/>
        -> Req(1, Usage.R, None, Some(Range(1, "*")), None, Some("tt")),
    <E Usage="R" MinLength="1" MaxLength="*" Table="tt" ConfLength="=2"/>
        -> Req(1, Usage.R, None, Some(Range(1, "*")), Some("=2"), Some("tt"))
    ) map ( t => XMLDeserializerHelper.requirement(1, t._1) === t._2 )

  def card = Seq (
      <E Min="1" Max="*"/> -> Some(Range(1, "*") ),
      <E/> -> None
  ) map( t => XMLDeserializerHelper.cardinality( t._1 ) === t._2 )

  def len = Seq (
    <E MinLength="1" MaxLength="*"/> -> Some(Range(1, "*") ),
    <E/> -> None
  ) map( t => XMLDeserializerHelper.length( t._1 ) === t._2 )

  //FIXME move this to nist.xml
  private implicit def toXOM( e: scala.xml.Node ): nu.xom.Element = {
    val r = new nu.xom.Element( e.label )
    // process the attributes
    e.attributes.asAttrMap foreach { t =>
      r.addAttribute( new nu.xom.Attribute(t._1, t._2) )
    }
    // process the children
    e.child foreach{ c => r.appendChild( toXOM( c ) ) }
    r
  }
}
