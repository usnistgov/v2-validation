package hl7.v2.validation.structure

import hl7.v2.instance.{Location, Text}
import hl7.v2.validation.report._
import hl7.v2.validation.structure.ValueSetValidation.checkValueSet
import hl7.v2.validation.vs.CodeUsage.{E, P}
import hl7.v2.validation.vs._
import org.specs2.Specification

class ValueSetValidationSpec extends Specification { def is = s2"""
  Value Set validation specification
    Value Set validation should:
      Abort if no value set is defined on the primitive element          $e1
      Return VSNotFound if the value set cannot be found in the library  $e2
      Return CodeNotFound if the code is not in the value set            $e3
      Return VSSpecError if more than one code is found in the value set $e4
      Returns EVS if the code is in the value set but the usage is E     $e5
      Returns PVS if the code is in the value set but the usage is P     $e6
  """

  val l = Location("", "", -1, -1)

  val bs            = Some(BindingStrength.R)
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

  def e1 = checkValueSet(l, Text(""), None) === Nil

  def e2 = checkValueSet(l, Text(""), Some("x#R")) === VSNotFound(l, "", "x", bs)::Nil

  def e3 = checkValueSet(l, Text("Z"), Some("01#R")) === CodeNotFound(l, "Z", vs1, bs)::Nil

  def e4 = checkValueSet(l, Text("A"), Some("03#R")) === List {
    val m = s"More than one code 'A' found in the value set '03'"
    VSSpecError(l, "03", m)
  }

  def e5 = checkValueSet(l, Text("A"), Some("02#R")) === EVS(l, "A", vs2, bs)::Nil

  def e6 = checkValueSet(l, Text("B"), Some("02#R")) === PVS(l, "B", vs2, bs)::Nil

}
