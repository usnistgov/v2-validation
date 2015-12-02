package hl7.v2.profile

import org.specs2.Specification

class XMLDeserializerHelperSpec extends Specification  {

 def is =s2"""
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
      Binding="tt"
      />
  ) === {
    val vs = ValueSetSpec("tt", None, None) :: Nil
    val r = Req(1, "X", Usage.R,
                Some(Range(1,"*")), Some(Range(1,"*")), Some("=2"), vs)
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
        Binding="tt"
        />
      }</Datatype>
      -> {
      val vs = ValueSetSpec("tt", None, None) :: Nil
      val r  = Req(1, "X", Usage.R, None, Some(Range(1,"*")), Some("=2"), vs)
      Composite("i2", "N2", "xx", Component("X", "Y", r) :: Nil )
    }
  ) map ( t => XMLDeserializerHelper.datatype( t._1 ) === t._2 )


  def req =
  {
    val vs = ValueSetSpec("tt", None, None) :: Nil
    val l = Seq (
        <E Usage="R"/>
        -> Req(1, "", Usage.R, None, None, None, Nil),
        <E Usage="R" Min="1" Max="2"/>
        -> Req(1, "", Usage.R, Some(Range(1, "2")), None, None, Nil),
        <E Usage="R" Min="1" Max="2" MinLength="1" MaxLength="*"/>
        -> Req(1, "", Usage.R, Some(Range(1, "2")), Some(Range(1, "*")), None, Nil),
        <E Usage="R" MinLength="1" MaxLength="*" Binding="tt"/>
        -> Req(1, "", Usage.R, None, Some(Range(1, "*")), None, vs),
        <E Usage="R" MinLength="1" MaxLength="*" Binding="tt" ConfLength="=2"/>
        -> Req(1, "", Usage.R, None, Some(Range(1, "*")), Some("=2"), vs,false),
        <E Usage="R" MinLength="1" MaxLength="*" Binding="tt" ConfLength="=2" Hide="true" />
        -> Req(1, "", Usage.R, None, Some(Range(1, "*")), Some("=2"), vs,true),
        <E Usage="R" MinLength="1" MaxLength="*" Binding="tt" ConfLength="=2" Hide="TRUE" />
        -> Req(1, "", Usage.R, None, Some(Range(1, "*")), Some("=2"), vs,true),
        <E Usage="R" MinLength="1" MaxLength="*" Binding="tt" ConfLength="=2" Hide="false" />
        -> Req(1, "", Usage.R, None, Some(Range(1, "*")), Some("=2"), vs,false),
        <E Usage="R" MinLength="1" MaxLength="*" Binding="tt" ConfLength="=2" Hide="FALSE" />
        -> Req(1, "", Usage.R, None, Some(Range(1, "*")), Some("=2"), vs,false)
    )

    l map ( t => XMLDeserializerHelper.requirement(1, "", t._1) === t._2 )
  }

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

  implicit val dtMap = Map( "Y" -> Primitive("Y", "Y", "Y desc") )

}