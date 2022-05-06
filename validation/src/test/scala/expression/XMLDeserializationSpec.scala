package expression

import org.specs2.Specification
import XMLDeserializer.expression
import hl7.v2.instance.Number
import hl7.v2.instance.Text
import hl7.v2.profile.Range
/**
  * Expression XML Deserialization specification
  * 
  * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
  */

class XMLDeserializationSpec extends Specification { def is = s2"""
  Expression deserialization specification
    Deserialization of presence expression should work as expected        $pe1
    Deserialization of path value expression should work as expected      $pe2
    Deserialization of complex path value expression should work as expected      $pe18
    Deserialization of plain text expression should work as expected      $pe3
    Deserialization of format expression should work as expected          $pe4
    Deserialization of number list expression should work as expected     $pe5
    Deserialization of string list expression should work as expected     $pe6
    Deserialization of simple value expression should work as expected    $pe7
    Deserialization of and expression should work as expected             $pe8
    Deserialization of or expression should work as expected              $pe9
    Deserialization of not expression should work as expected             $pe10
    Deserialization of xor expression should work as expected             $pe11
    Deserialization of imply expression should work as expected           $pe12
    Deserialization of exist expression should work as expected           $pe13
    Deserialization of forall expression should work as expected          $pe14
    Deserialization of isNULL expression should work as expected          $pe15
    Deserialization of subContext expression should work as expected      $pe16
    Deserialization of string format expression should work as expected      $pe17
  """

  def pe1 = expression( <Presence Path="1[1]"/> ) === Presence( "1[1]" )

  def pe2 = pe21 and pe22 and pe23 and pe24 and pe25 and pe26 and pe27 and pe28 and pe29 and pe210 and pe211 and pe212
  def pe21 = expression( <PathValue Path1="1[1]" Operator="EQ" Path2="1[1]"/> ) === PathValue( "1[1]", Operator.EQ, "1[1]")
  def pe22 = expression( <PathValue Path1="1[1]" Operator="EQ" Path2="1[1]" NotPresentBehavior="FAIL"/> ) === PathValue( "1[1]", Operator.EQ, "1[1]", ComparisonMode(false, false), "FAIL", MultiCompareMode.All(), MultiCompareMode.All())
  def pe23 = expression( <PathValue Path1="1[1]" Operator="EQ" Path2="1[1]" NotPresentBehavior="INCONCLUSIVE"/> ) === PathValue( "1[1]", Operator.EQ, "1[1]",  ComparisonMode(false, false), "INCONCLUSIVE", MultiCompareMode.All(), MultiCompareMode.All())
  def pe24 = expression( <PathValue Path1="1[1]" Operator="EQ" Path2="1[1]" IdenticalEquality="true" NotPresentBehavior="FAIL"/> ) === PathValue( "1[1]", Operator.EQ, "1[1]", ComparisonMode(true, false), "FAIL", MultiCompareMode.All(), MultiCompareMode.All())
  def pe25 = expression( <PathValue Path1="1[1]" Operator="EQ" Path2="1[1]" Truncated="true" NotPresentBehavior="INCONCLUSIVE"/> ) === PathValue( "1[1]", Operator.EQ, "1[1]", ComparisonMode(false, true), "INCONCLUSIVE", MultiCompareMode.All(), MultiCompareMode.All())
  def pe26 = expression( <PathValue Path1="1[1]" Operator="EQ" Path2="1[1]" IdenticalEquality="true" Truncated="true" NotPresentBehavior="INCONCLUSIVE"/> ) === PathValue( "1[1]", Operator.EQ, "1[1]", ComparisonMode(true, true), "INCONCLUSIVE", MultiCompareMode.All(), MultiCompareMode.All())
  def pe27 = expression( <PathValue Path1="1[1]" Operator="EQ" Path2="1[1]" IdenticalEquality="true" Truncated="true" NotPresentBehavior="INCONCLUSIVE"/> ) === PathValue( "1[1]", Operator.EQ, "1[1]", ComparisonMode(true, true), "INCONCLUSIVE", MultiCompareMode.All(), MultiCompareMode.All())
  def pe28 = expression( <PathValue Path1="1[1]" Operator="EQ" Path2="1[1]" IdenticalEquality="true" Truncated="true" NotPresentBehavior="INCONCLUSIVE" Path1Mode="All" Path2Mode="AtLeastOne" /> ) === PathValue( "1[1]", Operator.EQ, "1[1]", ComparisonMode(true, true), "INCONCLUSIVE", MultiCompareMode.All(), MultiCompareMode.AtLeastOne())
  def pe29 = expression( <PathValue Path1="1[1]" Operator="EQ" Path2="1[1]" IdenticalEquality="true" Truncated="true" NotPresentBehavior="INCONCLUSIVE" Path1Mode="All" Path2Mode="2"/> ) === PathValue( "1[1]", Operator.EQ, "1[1]", ComparisonMode(true, true), "INCONCLUSIVE", MultiCompareMode.All(), MultiCompareMode.Count(2))
  def pe210 = expression( <PathValue Path1="1[1]" Operator="EQ" Path2="1[1]" IdenticalEquality="true" Truncated="true" NotPresentBehavior="INCONCLUSIVE" Path1Mode="AtLeastOne" Path2Mode="123243"/> ) === PathValue( "1[1]", Operator.EQ, "1[1]", ComparisonMode(true, true), "INCONCLUSIVE", MultiCompareMode.AtLeastOne(), MultiCompareMode.Count(123243))
  def pe211 = expression( <PathValue Path1="1[1]" Operator="EQ" Path2="1[1]" IdenticalEquality="true" Truncated="true" NotPresentBehavior="INCONCLUSIVE" Path1Mode="0" Path2Mode="smt"/> ) === PathValue( "1[1]", Operator.EQ, "1[1]", ComparisonMode(true, true), "INCONCLUSIVE") must throwA(new Error(s"[Error] value 'smt' is not a recognized path comparison mode"))
  def pe212 = expression( <PathValue Path1="1[1]" Operator="EQ" Path2="1[1]" IdenticalEquality="true" Truncated="true" NotPresentBehavior="INCONCLUSIVE" Path1Mode="-1" Path2Mode="All"/> ) === PathValue( "1[1]", Operator.EQ, "1[1]", ComparisonMode(true, true), "INCONCLUSIVE") must throwA(new Error(s"[Error] value '-1' is not a valid path comparison mode (must be >= 0)"))

  def pe18 = pe181 and pe182 and pe183 and pe184 and pe185 and pe186
  def pe181 = expression( <ComplexPathValue Path1="1[1]" Operator="EQ" Path2="1[1]"/> ) === ComplexPathValue( "1[1]", Operator.EQ, "1[1]")
  def pe182 = expression( <ComplexPathValue Path1="1[1]" Operator="EQ" Path2="1[1]" NotPresentBehavior="FAIL"/> ) === ComplexPathValue( "1[1]", Operator.EQ, "1[1]", true, ComparisonMode(false, false), "FAIL")
  def pe183 = expression( <ComplexPathValue Path1="1[1]" Operator="EQ" Path2="1[1]" NotPresentBehavior="INCONCLUSIVE"/> ) === ComplexPathValue( "1[1]", Operator.EQ, "1[1]",  true, ComparisonMode(false, false), "INCONCLUSIVE")
  def pe184 = expression( <ComplexPathValue Path1="1[1]" Operator="EQ" Path2="1[1]" IdenticalEquality="true" NotPresentBehavior="FAIL"/> ) === ComplexPathValue( "1[1]", Operator.EQ, "1[1]", true, ComparisonMode(true, false), "FAIL")
  def pe185 = expression( <ComplexPathValue Path1="1[1]" Operator="EQ" Path2="1[1]" Truncated="true" NotPresentBehavior="INCONCLUSIVE"/> ) === ComplexPathValue( "1[1]", Operator.EQ, "1[1]", true, ComparisonMode(false, true), "INCONCLUSIVE")
  def pe186 = expression( <ComplexPathValue Path1="1[1]" Operator="EQ" Path2="1[1]" IdenticalEquality="true" Truncated="true" NotPresentBehavior="INCONCLUSIVE"/> ) === ComplexPathValue( "1[1]", Operator.EQ, "1[1]", true, ComparisonMode(true, true), "INCONCLUSIVE")


  def pe3 = pe31 and pe32 and pe33 and pe34 and pe35 and pe36 and pe37 and pe38 and pe39 and pe310 and pe311 and pe312 and pe313
  def pe31 = expression( <PlainText Path="1[1]" Text="XX" IgnoreCase="true"/> )  === PlainText( "1[1]", "XX", true)
  def pe32 = expression( <PlainText Path="1[1]" Text="XX" IgnoreCase="1"/> )     === PlainText( "1[1]", "XX", true)
  def pe33 = expression( <PlainText Path="1[1]" Text="XX" IgnoreCase="false"/> ) === PlainText( "1[1]", "XX", false)
  def pe34 = expression( <PlainText Path="1[1]" Text="XX" IgnoreCase="0"/> )     === PlainText( "1[1]", "XX", false)
  def pe35 = expression( <PlainText Path="1[1]" Text="XX" IgnoreCase="0" AtLeastOnce="true"/> )     === PlainText( "1[1]", "XX", false, true)
  def pe36 = expression( <PlainText Path="1[1]" Text="XX" IgnoreCase="0" AtLeastOnce="false"/> )     === PlainText( "1[1]", "XX", false)
  def pe37 = expression( <PlainText Path="1[1]" Text="XX" IgnoreCase="0" AtLeastOnce="1"/> )     === PlainText( "1[1]", "XX", false, true)
  def pe38 = expression( <PlainText Path="1[1]" Text="XX" IgnoreCase="0" AtLeastOnce="0"/> )     === PlainText( "1[1]", "XX", false)
  def pe39 = expression( <PlainText Path="1[1]" Text="XX" IgnoreCase="true" NotPresentBehavior="FAIL" /> )  === PlainText( "1[1]", "XX", true, false, "FAIL")
  def pe310 = expression( <PlainText Path="1[1]" Text="XX" IgnoreCase="true" NotPresentBehavior="INCONCLUSIVE" /> )  === PlainText( "1[1]", "XX", true, false, "INCONCLUSIVE")
  def pe311 = expression( <PlainText Path="1[1]" Text="XX" IgnoreCase="true" NotPresentBehavior="INCONCLUSIVE" Min="0" Max="*" /> )  === PlainText( "1[1]", "XX", true, false, "INCONCLUSIVE", Some(Range(0, "*")))
  def pe312 = expression( <PlainText Path="1[1]" Text="XX" IgnoreCase="true" NotPresentBehavior="INCONCLUSIVE" AtLeastOnce="true" Min="0" Max="*" /> )  must throwA(new Error("AtLeastOnce and Min Max were defined, only one occurrences specification permitted, use eiter AtLeastOnce or (Min & Max)"))
  def pe313 = expression( <PlainText Path="1[1]" Text="XX" IgnoreCase="true" NotPresentBehavior="INCONCLUSIVE" AtLeastOnce="true" Min="0" /> )  must throwA(new AssertionError("assertion failed: Invalid Range, max is required # 0.."))

  def pe4 = pe41 and pe42 and pe43 and pe44 and pe45 and pe46 and pe47 and pe48
  def pe41 = expression( <Format Path="1[1]" Regex="XX"/> ) === Format( "1[1]", "XX")
  def pe42 = expression( <Format Path="1[1]" Regex="XX" AtLeastOnce="false" /> ) === Format( "1[1]", "XX")
  def pe43 = expression( <Format Path="1[1]" Regex="XX" AtLeastOnce="true" /> ) === Format( "1[1]", "XX", true)
  def pe44 = expression( <Format Path="1[1]" Regex="XX" AtLeastOnce="true" NotPresentBehavior="FAIL" /> ) === Format( "1[1]", "XX", true, "FAIL")
  def pe45 = expression( <Format Path="1[1]" Regex="XX" AtLeastOnce="true" NotPresentBehavior="INCONCLUSIVE" /> ) === Format( "1[1]", "XX", true, "INCONCLUSIVE")
  def pe46 = expression( <Format Path="1[1]" Regex="XX" NotPresentBehavior="INCONCLUSIVE" Min="0" Max="*"  /> ) === Format( "1[1]", "XX", false, "INCONCLUSIVE", Some(Range(0, "*")))
  def pe47 = expression( <Format Path="1[1]" Regex="XX" NotPresentBehavior="INCONCLUSIVE" AtLeastOnce="true" Min="0" Max="*"  /> ) must throwA(new Error("AtLeastOnce and Min Max were defined, only one occurrences specification permitted, use eiter AtLeastOnce or (Min & Max)"))
  def pe48 = expression( <Format Path="1[1]" Regex="XX" NotPresentBehavior="INCONCLUSIVE" AtLeastOnce="true" Min="0"  /> ) must throwA(new AssertionError("assertion failed: Invalid Range, max is required # 0.."))

  def pe5 = pe51 and pe52 and pe53 and pe54 and pe55 and pe56 and pe57 and pe58
  def pe51 = expression( <NumberList Path="1[1]" CSV=" 1 , 2.0 , 3 "/> ) === NumberList( "1[1]", List(1, 2.0, 3))
  def pe52 = expression( <NumberList Path="1[1]" CSV=" 1 , 2.0 , 3 " AtLeastOnce="false" /> ) === NumberList( "1[1]", List(1, 2.0, 3))
  def pe53 = expression( <NumberList Path="1[1]" CSV=" 1 , 2.0 , 3 " AtLeastOnce="true" /> ) === NumberList( "1[1]", List(1, 2.0, 3), true)
  def pe54 = expression( <NumberList Path="1[1]" CSV=" 1 , 2.0 , 3 " AtLeastOnce="true" NotPresentBehavior="FAIL" /> ) === NumberList( "1[1]", List(1, 2.0, 3), true, "FAIL")
  def pe55 = expression( <NumberList Path="1[1]" CSV=" 1 , 2.0 , 3 " AtLeastOnce="true" NotPresentBehavior="INCONCLUSIVE" /> ) === NumberList( "1[1]", List(1, 2.0, 3), true, "INCONCLUSIVE")
  def pe56 = expression( <NumberList Path="1[1]" CSV=" 1 , 2.0 , 3 " NotPresentBehavior="INCONCLUSIVE" Min="0" Max="*" /> ) === NumberList( "1[1]", List(1, 2.0, 3), false, "INCONCLUSIVE", Some(Range(0, "*")))
  def pe57 = expression( <NumberList Path="1[1]" CSV=" 1 , 2.0 , 3 " AtLeastOnce="true" NotPresentBehavior="INCONCLUSIVE" Min="0" Max="*" /> ) must throwA(new Error("AtLeastOnce and Min Max were defined, only one occurrences specification permitted, use eiter AtLeastOnce or (Min & Max)"))
  def pe58 = expression( <NumberList Path="1[1]" CSV=" 1 , 2.0 , 3 " AtLeastOnce="true" NotPresentBehavior="INCONCLUSIVE" Min="0"/> )  must throwA(new AssertionError("assertion failed: Invalid Range, max is required # 0.."))

  def pe6 = pe61 and pe62 and pe63 and pe64 and pe65 and pe66 and pe67 and pe68
  def pe61 = expression( <StringList Path="1[1]" CSV="1,2"/> ) === StringList( "1[1]", List("1", "2"))
  def pe62 = expression( <StringList Path="1[1]" CSV="1,2" AtLeastOnce="false" /> ) === StringList( "1[1]", List("1", "2"))
  def pe63 = expression( <StringList Path="1[1]" CSV="1,2" AtLeastOnce="true" /> ) === StringList( "1[1]", List("1", "2"), true)
  def pe64 = expression( <StringList Path="1[1]" CSV="1,2" AtLeastOnce="true" NotPresentBehavior="FAIL" /> ) === StringList( "1[1]", List("1", "2"), true, "FAIL")
  def pe65 = expression( <StringList Path="1[1]" CSV="1,2" AtLeastOnce="true" NotPresentBehavior="INCONCLUSIVE" /> ) === StringList( "1[1]", List("1", "2"), true, "INCONCLUSIVE")
  def pe66 = expression( <StringList Path="1[1]" CSV="1,2" NotPresentBehavior="INCONCLUSIVE" Min="0" Max="*" /> ) === StringList( "1[1]", List("1", "2"), false, "INCONCLUSIVE", Some(Range(0, "*")))
  def pe67 = expression( <StringList Path="1[1]" CSV="1,2" AtLeastOnce="true" NotPresentBehavior="INCONCLUSIVE" Min="0" Max="*" /> ) must throwA(new Error("AtLeastOnce and Min Max were defined, only one occurrences specification permitted, use eiter AtLeastOnce or (Min & Max)"))
  def pe68 = expression( <StringList Path="1[1]" CSV="1,2" AtLeastOnce="true" NotPresentBehavior="INCONCLUSIVE" Min="0" /> ) must throwA(new AssertionError("assertion failed: Invalid Range, max is required # 0.."))

  def pe7 = {
    val e1 = expression( <SimpleValue Path="1[1]" Operator="NE" Value="XX"/> )
    val e2 = expression( <SimpleValue Path="1[1]" Operator="NE" Value="XX" Type="Number"/> )
    val e21 = expression( <SimpleValue Path="1[1]" Operator="NE" Value="XX" AtLeastOnce="true" Type="Number"/> )
    val e22 = expression( <SimpleValue Path="1[1]" Operator="NE" Value="XX" AtLeastOnce="false" Type="Number"/> )
    val e3 = expression( <SimpleValue Path="1[1]" Operator="NE" Value="XX" Type="Number" NotPresentBehavior="FAIL" /> )
    val e4 = expression( <SimpleValue Path="1[1]" Operator="NE" Value="XX" Type="Number" NotPresentBehavior="INCONCLUSIVE" /> )
    val e41 = expression( <SimpleValue Path="1[1]" Operator="NE" Value="XX" Type="Number" IdenticalEquality="true" Truncated="true" NotPresentBehavior="INCONCLUSIVE" /> )
    val e42 = expression( <SimpleValue Path="1[1]" Operator="NE" Value="XX" Type="Number" IdenticalEquality="true" NotPresentBehavior="INCONCLUSIVE" /> )
    val e43 = expression( <SimpleValue Path="1[1]" Operator="NE" Value="XX" Type="Number" Truncated="true" NotPresentBehavior="INCONCLUSIVE" /> )
    val e5 = expression( <SimpleValue Path="1[1]" Operator="NE" Value="XX" Type="Number" NotPresentBehavior="INCONCLUSIVE"  Min="0" Max="*"   /> )
    def e6 = expression( <SimpleValue Path="1[1]" Operator="NE" Value="XX" Type="Number" Truncated="true" NotPresentBehavior="INCONCLUSIVE" AtLeastOnce="true"  Min="0" Max="*"  /> ) must throwA(new Error("AtLeastOnce and Min Max were defined, only one occurrences specification permitted, use eiter AtLeastOnce or (Min & Max)"))
    def e7 = expression( <SimpleValue Path="1[1]" Operator="NE" Value="XX" Type="Number" Truncated="true" NotPresentBehavior="INCONCLUSIVE"  Min="0"  /> )  must throwA(new AssertionError("assertion failed: Invalid Range, max is required # 0.."))


    e1 === SimpleValue( "1[1]", Operator.NE, Text("XX") ) and e2 === SimpleValue( "1[1]", Operator.NE, Number("XX") ) and
    e3 === SimpleValue( "1[1]", Operator.NE, Number("XX"), ComparisonMode(false, false), false, "FAIL") and e4 === SimpleValue( "1[1]", Operator.NE, Number("XX"), ComparisonMode(false, false), false, "INCONCLUSIVE") and
    e21 === SimpleValue( "1[1]", Operator.NE, Number("XX"), ComparisonMode(false, false), true ) and e22 === SimpleValue( "1[1]", Operator.NE, Number("XX"), ComparisonMode(false, false), false ) and
      e41 === SimpleValue( "1[1]", Operator.NE, Number("XX"), new ComparisonMode(true, true), false, "INCONCLUSIVE") and
      e42 === SimpleValue( "1[1]", Operator.NE, Number("XX"), new ComparisonMode(true, false), false, "INCONCLUSIVE") and
      e43 === SimpleValue( "1[1]", Operator.NE, Number("XX"), new ComparisonMode(false, true), false, "INCONCLUSIVE") and
      e5 === SimpleValue( "1[1]", Operator.NE, Number("XX"), ComparisonMode(false, false), false, "INCONCLUSIVE", Some(Range(0, "*"))) and
      e6 and e7
  }

  def pe8  = expression( <AND><Presence Path="1[1]"/><Presence Path="2[2]"/></AND> ) === AND( Presence("1[1]"), Presence("2[2]") )

  def pe9  = expression( <OR><Presence Path="1[1]"/><Presence Path="2[2]"/></OR> ) === OR( Presence("1[1]"), Presence("2[2]") )

  def pe10 = expression( <NOT><Presence Path="1[1]"/></NOT> ) === NOT( Presence("1[1]") )

  def pe11 = expression( <XOR><Presence Path="1[1]"/><Presence Path="2[2]"/></XOR> ) === XOR( Presence("1[1]"), Presence("2[2]") )

  def pe12 = expression( <IMPLY><Presence Path="1[1]"/><Presence Path="2[2]"/></IMPLY> ) === IMPLY( Presence("1[1]"), Presence("2[2]") )

  def pe13 = expression( <EXIST><Presence Path="1[1]"/><Presence Path="2[2]"/></EXIST> ) === EXIST( Presence("1[1]"), Presence("2[2]") )

  def pe14 = expression( <FORALL><Presence Path="1[1]"/><Presence Path="2[2]"/></FORALL> ) === FORALL( Presence("1[1]"), Presence("2[2]") )

  def pe15 = expression( <isNULL  Path="1[1]"/> ) === isNULL("1[1]")

  def pe16 = pe161 and pe162 and pe163 and pe164
  def pe161 = expression( <SubContext Path="1[*]" MinOccurrence="1" MaxOccurrence="*"><PlainText Path="1[1]" Text="XX" IgnoreCase="true"/></SubContext> ) ===  SubContext(PlainText( "1[1]", "XX", true), "1[*]", Some(Range(1, "*")), None)
  def pe162 = expression( <SubContext Path="1[*]" AtLeastOnce="true"><PlainText Path="1[1]" Text="XX" IgnoreCase="true"/></SubContext> ) ===  SubContext(PlainText( "1[1]", "XX", true), "1[*]", None, Some(true))
  def pe163 = expression( <SubContext Path="1[*]" AtLeastOnce="true" NotPresentBehavior="FAIL"><PlainText Path="1[1]" Text="XX" IgnoreCase="true"/></SubContext> ) ===  SubContext(PlainText( "1[1]", "XX", true), "1[*]", None, Some(true), "FAIL")
  def pe164 = expression( <SubContext Path="1[*]" AtLeastOnce="true" NotPresentBehavior="INCONCLUSIVE"><PlainText Path="1[1]" Text="XX" IgnoreCase="true"/></SubContext> ) ===  SubContext(PlainText( "1[1]", "XX", true), "1[*]", None, Some(true), "INCONCLUSIVE")

  def pe17 = pe171 and pe172 and pe173 and pe174 and pe175 and pe176 and pe177 and pe178
  def pe171 = expression(<StringFormat Path="1[*]" Format="LOINC" />) === StringFormat("1[*]", "LOINC")
  def pe172 = expression(<StringFormat Path="1[*]" Format="LOINC" AtLeastOnce="false" />) === StringFormat("1[*]", "LOINC")
  def pe173 = expression(<StringFormat Path="1[*]" Format="LOINC" AtLeastOnce="true" />) === StringFormat("1[*]", "LOINC", true)
  def pe174 = expression(<StringFormat Path="1[*]" Format="LOINC" AtLeastOnce="true" NotPresentBehavior="FAIL" />) === StringFormat("1[*]", "LOINC", true, "FAIL")
  def pe175 = expression(<StringFormat Path="1[*]" Format="LOINC" AtLeastOnce="true" NotPresentBehavior="INCONCLUSIVE" />) === StringFormat("1[*]", "LOINC", true, "INCONCLUSIVE")
  def pe176 = expression(<StringFormat Path="1[*]" Format="LOINC" NotPresentBehavior="INCONCLUSIVE" Min="0" Max="*" />) === StringFormat("1[*]", "LOINC", false, "INCONCLUSIVE", Some(Range(0, "*")))
  def pe177 = expression(<StringFormat Path="1[*]" Format="LOINC" AtLeastOnce="true" NotPresentBehavior="INCONCLUSIVE" Min="0" Max="*" />) must throwA(new Error("AtLeastOnce and Min Max were defined, only one occurrences specification permitted, use eiter AtLeastOnce or (Min & Max)"))
  def pe178 = expression(<StringFormat Path="1[*]" Format="LOINC" AtLeastOnce="true" NotPresentBehavior="INCONCLUSIVE" Min="0"/>) must throwA(new AssertionError("assertion failed: Invalid Range, max is required # 0.."))

  private implicit def toXOM( e: scala.xml.Node ): nu.xom.Element = {
    val r = new nu.xom.Element( e.label )
    // process the attributes
    e.attributes.asAttrMap foreach { t => r.addAttribute( new nu.xom.Attribute(t._1, t._2) ) }
    // process the children
    e.child foreach{ c => r.appendChild( toXOM( c ) ) }
    r
  }
}