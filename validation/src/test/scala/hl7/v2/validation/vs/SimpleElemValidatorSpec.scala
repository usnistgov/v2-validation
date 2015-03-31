package hl7.v2.validation.vs

import hl7.v2.instance.{Text, Simple, Location}
import hl7.v2.profile.{Usage, Req, ValueSetSpec, BindingStrength}
import hl7.v2.validation.report._
import hl7.v2.validation.vs.CodeUsage.{E, P}
import org.specs2.Specification

class SimpleElemValidatorSpec
    extends Specification
    with DefaultSimpleElemValidator { def is = s2"""

  Simple Element Value Set validation specification

    Value Set validation on a simple element should:
      Abort if the element value is Null                                 $e0
      Abort if no value set is defined on the primitive element          $e1
      Return VSNotFound if the value set cannot be found in the library  $e2
      Return EmptyVS if the value set is empty                           $e3
      Return CodeNotFound if the code is not in the value set            $e4
      Return EVS if the code is in the value set but the usage is E      $e5
      Return PVS if the code is in the value set but the usage is P      $e6
      Return VSSpecError if more than one code is found in the value set $e7
  """

  val l = Location("", "", -1, -1)

  val bs: Option[BindingStrength] = None
  val stability     = Some(Stability.Static)
  val extensibility = Some(Extensibility.Closed)

  val vs1 = ValueSet("01", extensibility, stability, Nil)
  val vs2 = ValueSet("02", extensibility, stability, codes = List(
    Code("A", "", E,"" ),
    Code("B", "", P,"" )
  ))
  val vs3 = ValueSet("03", extensibility, stability, codes = List(
    Code("A", "", E,"" ),
    Code("A", "", P,"" )
  ))

  implicit val library = Map[String, ValueSet](
    "01" -> vs1,
    "02" -> vs2,
    "03" -> vs3
  )

  def e0 = check( "\"\"", "04" ) === Nil
  def e1 = check( "", "" )    === Nil
  def e2 = check( "x", "04" ) === VSNotFound(l, "x", "04", bs) :: Nil
  def e3 = check( "x", "01" ) === EmptyVS(l, vs1, bs) :: Nil
  def e4 = check( "C", "02" ) === CodeNotFound(l, "C", vs2, bs) :: Nil
  def e5 = check( "A", "02" ) === EVS(l, "A", vs2, bs) :: Nil
  def e6 = check( "B", "02" ) === PVS(l, "B", vs2, bs) :: Nil
  def e7 = check( "A", "03" ) ===
    VSError (l, vs3, s"Multiple occurrences of the code 'A' found.") :: Nil

  def check(s: String, spec: String): List[VSEntry] = check(simple(s, spec), library)

  private def simple(v: String, s: String): Simple =
    new Simple {
      override val location = l
      override val value    = Text(v)
      override val position = -1
      override val instance = -1
      override val req = s match {
        case "" => Req(-1, Usage.O, None, None, None, Nil)
        case x  => Req(-1, Usage.O, None, None, None, ValueSetSpec(s).get::Nil)
      }
    }
}
