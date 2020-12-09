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

  implicit val dtMap = Map( "Y" -> Primitive("Y", "Y", "Y desc", "1.0"))

  def reqNoConstantValue(position: Int,
                         description: String,
                         usage: Usage,
                         cardinality: Option[Range],
                         length: Option[Range],
                         confLength: Option[String],
                         vsSpec: List[ValueSetSpec],
                         confRange : Option[Range],
                         hide: Boolean) = Req(position, description, usage, cardinality, length, confLength, vsSpec, confRange, hide, None)

  def reqNoHideNoConstantValue(position: Int,
          description: String,
          usage: Usage,
          cardinality: Option[Range],
          length: Option[Range],
          confLength: Option[String],
          vsSpec: List[ValueSetSpec],
          confRange : Option[Range]) = Req(position, description, usage, cardinality, length, confLength, vsSpec, confRange, false, None)

  def reqNoConfRangeNoHideNoConstantValue(position: Int,
                               description: String,
                               usage: Usage,
                               cardinality: Option[Range],
                               length: Option[Range],
                               confLength: Option[String],
                               vsSpec: List[ValueSetSpec]) = Req(position, description, usage, cardinality, length, confLength, vsSpec, None, false, None)

  def field = XMLDeserializerHelper.field( 1,
      <Field
      Name="X"
      Datatype="Y"
      Usage="R"
      Min="1"
      Max="*"
      MinLength="1"
      MaxLength="*"
      ConfLength="2="
      Binding="tt"
      />
  ) === {
    val vs = ValueSetSpec("tt", None, None) :: Nil
    val r = reqNoHideNoConstantValue(1, "X", Usage.R, Some(Range(1,"*")), Some(Range(1,"*")), Some("2="), vs, Some(Range(1,"2")))
    Field("X", "Y", r)
  }

  def dt = Seq (
      <Datatype ID="i1" Name="N1" Description="xx" Version="1.0"/>
      -> Primitive("i1", "N1", "xx", "1.0"),
    <Datatype ID="i2" Name="N2" Description="xx" Version="1.0">{
        <Component
        Name="X"
        Datatype="Y"
        Usage="R"
        MinLength="1"
        MaxLength="*"
        ConfLength="2="
        Binding="tt"
        />
      }</Datatype>
      -> {
      val vs = ValueSetSpec("tt", None, None) :: Nil
      val r  = reqNoHideNoConstantValue(1, "X", Usage.R, None, Some(Range(1,"*")), Some("2="), vs, Some(Range(1,"2")))
      Composite("i2", "N2", "xx", "1.0", Component("X", "Y", r) :: Nil )
    }
  ) map ( t => XMLDeserializerHelper.datatype( t._1 ) === t._2 )


  def req =
  {
    val vs = ValueSetSpec("tt", None, None) :: Nil
    val l = Seq (
        <E Usage="R"/>
        -> reqNoConfRangeNoHideNoConstantValue(1, "", Usage.R, None, None, None, Nil),
        <E Usage="R" Min="1" Max="2"/>
        -> reqNoConfRangeNoHideNoConstantValue(1, "", Usage.R, Some(Range(1, "2")), None, None, Nil),
        <E Usage="R" Min="1" Max="2" MinLength="1" MaxLength="*"/>
        -> reqNoHideNoConstantValue(1, "", Usage.R, Some(Range(1, "2")), Some(Range(1, "*")), None, Nil, None),
        <E Usage="R" MinLength="1" MaxLength="*" Binding="tt"/>
        -> reqNoHideNoConstantValue(1, "", Usage.R, None, Some(Range(1, "*")), None, vs, None),
        <E Usage="R" MinLength="1" MaxLength="*" Binding="tt" ConfLength="2="/>
        -> reqNoConstantValue(1, "", Usage.R, None, Some(Range(1, "*")), Some("2="), vs, Some(Range(1,"2")), false),
        <E Usage="R" MinLength="1" MaxLength="*" Binding="tt" ConfLength="2=" Hide="true" />
        -> reqNoConstantValue(1, "", Usage.R, None, Some(Range(1, "*")), Some("2="), vs, Some(Range(1,"2")), true),
        <E Usage="R" MinLength="1" MaxLength="*" Binding="tt" ConfLength="2=" Hide="TRUE" />
        -> reqNoConstantValue(1, "", Usage.R, None, Some(Range(1, "*")), Some("2="), vs, Some(Range(1,"2")), true),
        <E Usage="R" MinLength="1" MaxLength="*" Binding="tt" ConfLength="2=" Hide="false" />
        -> reqNoConstantValue(1, "", Usage.R, None, Some(Range(1, "*")), Some("2="), vs, Some(Range(1,"2")), false),
        <E Usage="R" MinLength="1" MaxLength="*" Binding="tt" ConfLength="2=" Hide="FALSE" />
        -> reqNoConstantValue(1, "", Usage.R, None, Some(Range(1, "*")), Some("2="), vs, Some(Range(1,"2")), false),
        <E Usage="R" MinLength="1" MaxLength="*" Binding="tt" ConfLength="2#" Hide="FALSE" />
        -> reqNoConstantValue(1, "", Usage.R, None, Some(Range(1, "*")), Some("2#"), vs, Some(Range(1,"2")), false),
        <E Usage="R" Binding="tt" ConfLength="2#" Hide="FALSE" />
        -> reqNoConstantValue(1, "", Usage.R, None, None, Some("2#"), vs, Some(Range(1,"2")), false),
        <E Usage="R" MinLength="1" MaxLength="*" Binding="tt" Hide="FALSE" />
        -> reqNoConstantValue(1, "", Usage.R, None, Some(Range(1, "*")), None, vs, None, false),
        <E Usage="R" MinLength="1" MaxLength="*" Binding="tt" ConfLength="NA" Hide="FALSE" />
        -> reqNoConstantValue(1, "", Usage.R, None, Some(Range(1, "*")), Some("NA"), vs, None, false),
        <E Usage="R" MinLength="NA" MaxLength="NA" Binding="tt" ConfLength="5" Hide="FALSE" />
        -> reqNoConstantValue(1, "", Usage.R, None, None, Some("5"), vs, Some(Range(1,"5")), false),
        <E Usage="R" MinLength="1" MaxLength="NA" Binding="tt" ConfLength="5" Hide="FALSE" />
        -> reqNoConstantValue(1, "", Usage.R, None, None, Some("5"), vs, Some(Range(1,"5")), false),
        <E Usage="R" MinLength="NA" MaxLength="*" Binding="tt" ConfLength="5" Hide="FALSE" />
        -> reqNoConstantValue(1, "", Usage.R, None, None, Some("5"), vs, Some(Range(1,"5")), false),
        <E Usage="R" MinLength="NA" MaxLength="*" Binding="tt" ConfLength="5" Hide="FALSE" ConstantValue="ABC" />
        -> Req(1, "", Usage.R, None, None, Some("5"), vs, Some(Range(1,"5")), false, Some("ABC"))
        
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
}