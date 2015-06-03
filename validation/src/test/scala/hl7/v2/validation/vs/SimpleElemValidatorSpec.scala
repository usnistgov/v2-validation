package hl7.v2.validation.vs

import hl7.v2.instance.{Text, Simple, Location}
import hl7.v2.profile.{Usage, Req, ValueSetSpec, BindingStrength}
import hl7.v2.validation.report._
import hl7.v2.validation.vs.CodeUsage.{R, E, P}
import org.specs2.Specification

import gov.nist.validation.report.Entry

class SimpleElemValidatorSpec
    extends Specification
    with DefaultSimpleElemValidator { def is = s2"""

  Simple Element Value Set validation specification

    Value Set validation on a simple element should:
      Abort if the element value is Null                                     $e0
      Abort if no value set is defined on the primitive element              $e1
      Return VSNotFound if the value set cannot be found in the library      $e2
      Return EmptyVS if the value set is empty                               $e3
      Return CodeNotFound if the code is not in the value set                $e4
      Return EVS if the code is in the value set but the usage is E          $e5
      Return PVS if the code is in the value set but the usage is P          $e6
      Return VSSpecError if more than one code is found in the value set     $e7
      Return no detection if the value is in the value set and the usage is R$e8
      Return no detection if the value match HL7nnn or 99ZZZ and vs is 0396  $e9
      Return no error if the value set is excluded from the validation       $e10
  """

  val l = Location(null, "", "", -1, -1)

  val bs: Option[BindingStrength] = None
  val stability     = Some(Stability.Static)
  val extensibility = Some(Extensibility.Closed)

  val vs1 = ValueSet("01", extensibility, stability, Nil)
  val vs2 = ValueSet("02", extensibility, stability, codes = List(
    Code("A", "", E,"" ),
    Code("B", "", P,"" ),
    Code("X", "", R,"" )
  ))
  val vs3 = ValueSet("03", extensibility, stability, codes = List(
    Code("A", "", E,"" ),
    Code("A", "", P,"" )
  ))
  val vs4 = ValueSet("0396", extensibility, stability, codes = List(
    Code("x", "", E,"" ),
    Code("x", "", P,"" )
  ))
  val vs5 = ValueSet("HL70396", extensibility, stability, codes = List(
    Code("x", "", E,"" ),
    Code("x", "", P,"" )
  ))
  val vs6 = ValueSet("06", extensibility, stability, Nil)

  val noValidation = Seq("06")

  implicit val library = ValueSetLibrary(
    noValidation,
    Map[String, ValueSet](
      "01" -> vs1,
      "02" -> vs2,
      "03" -> vs3,
      "0396" -> vs4,
      "HL70396" -> vs5,
      "06" -> vs6
    )
  )

  def e0 = check( "\"\"", "04" ) === Nil
  def e1 = check( "", "" )    === Nil
  def e2 = check( "x", "04" ) === Detections.vsNotFound(l, "x", "04") :: Nil
  def e3 = check( "x", "01" ) === Detections.emptyVS(l, vs1, "01") :: Nil
  def e4 = check( "C", "02" ) === Detections.codeNotFound(l, "C", vs2, "02") :: Nil
  def e5 = check( "A", "02" ) === Detections.evs(l, "A", vs2, "02") :: Nil
  def e6 = check( "B", "02" ) === Detections.pvs(l, "B", vs2, "02") :: Nil
  def e7 = check( "A", "03" ) ===
    Detections.vsError(l, s"Multiple occurrences of the code 'A' found.", vs3, "03") :: Nil

  def e8 = check( "X", "02" ) === Nil

  def e9 = Seq("0396", "HL70396") map { vs =>
    check("HL70001", vs) === Nil and check("99ZZZ", vs) === Nil
  }

  def e10 = check( "x", "06" ) === Detections.vsNoVal(l, "06") :: Nil

  def check(s: String, spec: String): List[Entry] = check(simple(s, spec), library)

  private def simple(v: String, s: String): Simple =
    new Simple {
      override val location = l
      override val value    = Text(v)
      override val position = -1
      override val instance = -1
      override val req = s match {
        case "" => Req(-1, "", Usage.O, None, None, None, Nil)
        case x  => Req(-1, "", Usage.O, None, None, None, ValueSetSpec(s).get::Nil)
      }
    }

  implicit private def vsSpec(s: String): ValueSetSpec = ValueSetSpec(s).get
}
