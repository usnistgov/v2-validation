package hl7.v2.validation.vs

import gov.nist.validation.report.Entry
import hl7.v2.instance.{Location, Simple, Text}
import hl7.v2.profile.{BindingStrength, Req, Usage, ValueSetSpec}
import CodeUsage.{E, P, R}
import org.specs2.Specification
import hl7.v2.validation.report.ConfigurableDetections
import com.typesafe.config.ConfigFactory

class SimpleElementValidatorSpec
  extends Specification  { def is = s2"""

  Simple Element Value Set validation specification

    Value Set validation on a simple element should:
      Abort if the element value is Null                                     $e0
      Abort if no value set is defined on the primitive element              $e1
      Return VSNotFound if the value set cannot be found in the library      $e2
      Return EmptyVS if the value set is empty                               $e3
      Return CodeNotFound if the code is not in the value set                $e4
      Return VSSpecError if more than one code is found in the value set     $e7
      Return no detection if the value is in the value set and the usage is R$e8
      Return no detection if the value match HL7nnn or 99ZZZ and vs is 0396  $e9
      Return no error if the value set is excluded from the validation       $e10
      Return EVS if the code is in the value set but the usage is E          $e5
      Return PVS if the code is in the value set but the usage is P          $e6
  """

  implicit val Detections: ConfigurableDetections = new ConfigurableDetections(ConfigFactory.load());
  implicit val vsValidation: SimpleElementValidator = new SimpleElementValidator(Detections)

  val l = Location(null, "", "", -1, -1)

  val bs: Option[BindingStrength] = None
  val stability     = Some(Stability.Static)
  val extensibility = Some(Extensibility.Closed)

  val vs1 =  InternalValueSet("01", extensibility, stability, Nil)
  val vs2 =  InternalValueSet("02", extensibility, stability, codes = List(
    Code("A", "", E,"" ),
    Code("B", "", P,"" ),
    Code("X", "", R,"" )
  ))
  val vs3 =  InternalValueSet("03", extensibility, stability, codes = List(
    Code("A", "", E,"" ),
    Code("A", "", P,"" )
  ))
  val vs4 =  InternalValueSet("0396", extensibility, stability, codes = List(
    Code("x", "", E,"" ),
    Code("x", "", P,"" )
  ))
  val vs5 =  InternalValueSet("HL70396", extensibility, stability, codes = List(
    Code("x", "", E,"" ),
    Code("x", "", P,"" )
  ))
  val vs6 =  InternalValueSet("06", extensibility, stability, Nil)

  val noValidation = Seq("06")

  implicit val library = ValueSetLibraryImpl(
    noValidation,
    Map[String,  InternalValueSet](
      "01" -> vs1,
      "02" -> vs2,
      "03" -> vs3,
      "0396" -> vs4,
      "HL70396" -> vs5,
      "06" -> vs6
    )
  )

  def e0 = check( "\"\"", "04" ) === null
  def e1 = check( "", "" )    === null
  def e2 = check( "x", "04" ) === Detections.vsNotFound(l, "x", vsSpec("04"))
  def e3 = check( "x", "01" ) === Detections.emptyVS(l, vs1, vsSpec("01"))
  def e4 = check( "C", "02" ) === Detections.codeNotFound(l, "C", "02", null, null, null)
  def e5 = check( "A", "02" ) === new EnhancedEntry(Detections.vsCodeFound("evs", l, "A", "02", null, null, null),false)
  def e6 = check( "B", "02" ) === new EnhancedEntry(Detections.vsCodeFound("pvs", l, "B", "02", null, null, null),true)
  def e7 = check( "A", "03" ) === Detections.vsError(l, "Multiple occurrences of the code 'A' found.", vs3, vsSpec("03"))

  def e8 = check( "X", "02" ) === new EnhancedEntry(Detections.vsCodeFound("rvs", l, "X", "02", null, null, null),true)

  def e9 = Seq("0396", "HL70396") map { vs =>
    check("HL70001", vs) === null and check("99ZZZ", vs) === null
  }

  def e10 = check( "x", "06" ) === Detections.vsNoVal(l, "06")

  def check(s: String, spec: String): Entry = {
    val x = simple(s, spec)
    val y = x.req.vsSpec match { case Nil => null case z::zs => z }
    vsValidation.check(x, y, library)
  }

  private def simple(v: String, vsid: String): Simple =
    new Simple {
      override val location = l
      override val value    = Text(v)
      override val position = -1
      override val instance = -1
      override val req = vsid match {
        case "" => Req(-1, "", Usage.O, None, None, None, Nil, None, false, None)
        case x  => Req(-1, "", Usage.O, None, None, None, ValueSetSpec(vsid, None, None)::Nil, None, false, None)
      }
    }

  implicit private def vsSpec(vsid: String): ValueSetSpec = ValueSetSpec(vsid, None, None)
}
