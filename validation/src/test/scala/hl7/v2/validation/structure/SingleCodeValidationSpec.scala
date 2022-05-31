package hl7.v2.validation.structure

import gov.nist.validation.report.Entry
import hl7.v2.instance.{Complex, Element, Location, Simple, Text, Value}
import hl7.v2.parser.impl.DefaultParser
import hl7.v2.profile.{Req, Usage}
import hl7.v2.validation.vs.{BindingLocation, EmptyValueSetSpecification, SingleCodeBinding, VSValidationCode, ValueSetLibraryImpl, ValueSetSpecification}
import org.specs2.Specification
import org.specs2.matcher.MatchResult

trait SingleCodeValidationSpec extends Specification
  with hl7.v2.validation.vs.DefaultValueSetValidator
  with Helpers
  with DefaultParser {def is = s2"""

     Single Code XOR
        Code 'A', CodeSystem 'LN' bound to 1 or 4, was found in both 1 and 4 should return a Single Code XOR detection $xor1
        Code 'A', CodeSystem 'LN' bound to 1 or 4 or 10, was found in 1 and 10 should return a Single Code XOR detection $xor2
     Single Code Invalid Code System
        Code 'A', CodeSystem 'LN' bound to 1, was found in location 1 with CodeSystem LX should return a Single Code Invalid Code System $wrongCs1
        Code 'A', CodeSystem 'LN' bound to 1 or 4, was found in location 1 with CodeSystem LY, was found in location 4 with CodeSystem LX should return 2 x Single Code Invalid Code System $wrongCs2
     Single Code Code System Not Found
        Code 'A', CodeSystem 'LN' bound to 1, was found in location 1 with CodeSystem empty should return a Single Code Code System Not Found $emptyCs1
        Code 'A', CodeSystem 'LN' bound to 1 or 4, was found in location 1 with CodeSystem empty, was found in location 4 with CodeSystem empty should return 2 x Single Code Code System Not Found $emptyCs2
     Single Code Not Found
        Code 'A', CodeSystem 'LN' bound to 1, was not found in location 1 should return a Single Code Not Found $nf1
        Code 'A', CodeSystem 'LN' bound to 1 or 4 or 10, was not found in 1 and 4 and 10 should return 3 x Single Code Not Found $nf2
     Failure For Different Reasons By Location
        Code 'A', CodeSystem 'LN' bound to 1 or 4 or 10, was not found in 1, was found with CodeSystem LX in 4, was found with CodeSystem empty in 10 should return a Single Code Not Found and Single Code Invalid Code System and Single Code Code System Not Found $mix
     Single Code Success
        Code 'A', CodeSystem 'LN' bound to 1 was found in location 1 should return a Single Code Success $succ1
        Code 'A', CodeSystem 'LN' bound to 1 or 4, was found in location 1 and not location 4 should return a Single Code Success $succ2
        Code 'A', CodeSystem 'LN' bound to 1 or 4, was found in location 1 and CodeSystem LX in location 4 should return a Single Code Success $succ3
        Code 'A', CodeSystem 'LN' bound to 1 or 4, was found in location 1 and CodeSystem empty in location 4 should return a Single Code Success $succ4
  """
  val vsSpecification: ValueSetSpecification = EmptyValueSetSpecification
  val valueSetLibrary = new ValueSetLibraryImpl(List(), Map())

  val xor1: MatchResult[List[Entry]] = {
    val s11 = S(1, 1, Text("A"))
    val s41 = S(4, 1, Text("A"))
    val CWE_2_7_XOR: C = C(0, 1, List(
      s11,
      S(3, 1, Text("LN")),

      s41,
      S(6, 1, Text("LN")),

      S(10, 1, Text("C")),
      S(12, 1, Text("SYS"))
    ))
    val binding = SingleCodeBinding(".", "A", "LN",
      List(
        BindingLocation("1[*]", Some("3[*]")),
        BindingLocation("4[*]", Some("6[*]"))
      )
    )
    check(CWE_2_7_XOR, binding) must containTheSameElementsAs(List(
      createEntry(CWE_2_7_XOR, VSValidationCode.SingleCodeMultiLocationXOR(CWE_2_7_XOR, List(s11, s41), binding), None)
    ))
  }

  val xor2: MatchResult[List[Entry]] = {
    val s11 = S(1, 1, Text("A"))
    val s41 = S(4, 1, Text("A"))
    val s101 = S(10, 1, Text("A"))
    val CWE_2_7_XOR: C = C(0, 1, List(
      s11,
      S(3, 1, Text("LN")),

      s41,
      S(6, 1, Text("LN")),

      s101,
      S(12, 1, Text("LN"))
    ))
    val binding = SingleCodeBinding(".", "A", "LN",
      List(
        BindingLocation("1[*]", Some("3[*]")),
        BindingLocation("4[*]", Some("6[*]")),
        BindingLocation("10[*]", Some("12[*]"))
      )
    )
    check(CWE_2_7_XOR, binding) must containTheSameElementsAs(List(
      createEntry(CWE_2_7_XOR, VSValidationCode.SingleCodeMultiLocationXOR(CWE_2_7_XOR, List(s11, s41, s101), binding), None)
    ))
  }

  val wrongCs1: MatchResult[List[Entry]] = {
    val s11 = S(1, 1, Text("A"))
    val s31 = S(3, 1, Text("LX"))
    val s41 = S(4, 1, Text("B"))
    val s101 = S(10, 1, Text("C"))
    val CWE_2_7_XOR: C = C(0, 1, List(
      s11,
      s31,

      s41,
      S(6, 1, Text("LN")),

      s101,
      S(12, 1, Text("LN"))
    ))
    val binding = SingleCodeBinding(".", "A", "LN",
      List(
        BindingLocation("1[*]", Some("3[*]")),
      )
    )
    check(CWE_2_7_XOR, binding) must containTheSameElementsAs(List(
      createEntry(CWE_2_7_XOR, VSValidationCode.SingleCodeInvalidCodeSystem(CWE_2_7_XOR, s11, s31, binding), None)
    ))
  }

  val wrongCs2: MatchResult[List[Entry]] = {
    val s11 = S(1, 1, Text("A"))
    val s31 = S(3, 1, Text("LX"))
    val s41 = S(4, 1, Text("A"))
    val s61 = S(6, 1, Text("LY"))
    val s101 = S(10, 1, Text("C"))
    val CWE_2_7_XOR: C = C(0, 1, List(
      s11,
      s31,

      s41,
      s61,

      s101,
      S(12, 1, Text("LN"))
    ))

    val binding = SingleCodeBinding(".", "A", "LN",
      List(
        BindingLocation("1[*]", Some("3[*]")),
        BindingLocation("4[*]", Some("6[*]"))
      )
    )
    check(CWE_2_7_XOR, binding) must containTheSameElementsAs(List(
      createEntry(CWE_2_7_XOR, VSValidationCode.SingleCodeInvalidCodeSystem(CWE_2_7_XOR, s11, s31, binding), None),
      createEntry(CWE_2_7_XOR, VSValidationCode.SingleCodeInvalidCodeSystem(CWE_2_7_XOR, s41, s61, binding), None)
    ))
  }

  val emptyCs1: MatchResult[List[Entry]] = {
    val s11 = S(1, 1, Text("A"))
    val s41 = S(4, 1, Text("B"))
    val s101 = S(10, 1, Text("C"))
    val EC1: C = C(0, 1, List(
      s11,

      s41,
      S(6, 1, Text("LN")),

      s101,
      S(12, 1, Text("LN"))
    ))
    val binding = SingleCodeBinding(".", "A", "LN",
      List(
        BindingLocation("1[*]", Some("3[*]")),
      )
    )
    check(EC1, binding) must containTheSameElementsAs(List(
      createEntry(EC1, VSValidationCode.SingleCodeCodeSystemNotFound(EC1, s11, "3[*]", binding), None)
    ))
  }

  val emptyCs2: MatchResult[List[Entry]] = {
    val s11 = S(1, 1, Text("A"))
    val s41 = S(4, 1, Text("A"))
    val s101 = S(10, 1, Text("C"))
    val EC2: C = C(0, 1, List(
      s11,

      s41,

      s101,
      S(12, 1, Text("LN"))
    ))

    val binding = SingleCodeBinding(".", "A", "LN",
      List(
        BindingLocation("1[*]", Some("3[*]")),
        BindingLocation("4[*]", Some("6[*]"))
      )
    )
    check(EC2, binding) must containTheSameElementsAs(List(
      createEntry(EC2, VSValidationCode.SingleCodeCodeSystemNotFound(EC2, s11, "3[*]", binding), None),
      createEntry(EC2, VSValidationCode.SingleCodeCodeSystemNotFound(EC2, s41, "6[*]", binding), None)
    ))
  }

  val nf1: MatchResult[List[Entry]] = {
    val s11 = S(1, 1, Text("X"))
    val CWE_2_7_XOR: C = C(0, 1, List(
      s11,
      S(3, 1, Text("LN"))
    ))
    val binding = SingleCodeBinding(".", "A", "LN",
      List(
        BindingLocation("1[*]", Some("3[*]"))
      )
    )
    check(CWE_2_7_XOR, binding) must containTheSameElementsAs(List(
      createEntry(CWE_2_7_XOR, VSValidationCode.SingleCodeNotFound(s11, "X", binding), None)
    ))
  }

  val nf2: MatchResult[List[Entry]] = {
    val s11 = S(1, 1, Text("X"))
    val s41 = S(4, 1, Text("Y"))
    val s101 = S(10, 1, Text("Z"))
    val CWE_2_7_XOR: C = C(0, 1, List(
      s11,
      S(3, 1, Text("LN")),

      s41,
      S(6, 1, Text("LN")),

      s101,
      S(12, 1, Text("SYS"))
    ))
    val binding = SingleCodeBinding(".", "A", "LN",
      List(
        BindingLocation("1[*]", Some("3[*]")),
        BindingLocation("4[*]", Some("6[*]")),
        BindingLocation("10[*]", Some("12[*]"))
      )
    )
    check(CWE_2_7_XOR, binding) must containTheSameElementsAs(List(
      createEntry(CWE_2_7_XOR, VSValidationCode.SingleCodeNotFound(s11, "X", binding), None),
      createEntry(CWE_2_7_XOR, VSValidationCode.SingleCodeNotFound(s41, "Y", binding), None),
      createEntry(CWE_2_7_XOR, VSValidationCode.SingleCodeNotFound(s101, "Z", binding), None)
    ))
  }

  val mix: MatchResult[List[Entry]] = {
    val s11 = S(1, 1, Text("A"))
    val s31 = S(3, 1, Text("LX"))
    val s41 = S(4, 1, Text("A"))
    val s101 = S(10, 1, Text("X"))
    val CWE_2_7_XOR: C = C(0, 1, List(
      s11,
      s31,

      s41,

      s101,
      S(12, 1, Text("SYS"))
    ))
    val binding = SingleCodeBinding(".", "A", "LN",
      List(
        BindingLocation("1[*]", Some("3[*]")),
        BindingLocation("4[*]", Some("6[*]")),
        BindingLocation("10[*]", Some("12[*]"))
      )
    )
    check(CWE_2_7_XOR, binding) must containTheSameElementsAs(List(
      createEntry(CWE_2_7_XOR, VSValidationCode.SingleCodeCodeSystemNotFound(CWE_2_7_XOR, s41, "6[*]", binding), None),
      createEntry(CWE_2_7_XOR, VSValidationCode.SingleCodeInvalidCodeSystem(CWE_2_7_XOR, s11, s31, binding), None),
      createEntry(CWE_2_7_XOR, VSValidationCode.SingleCodeNotFound(s101, "X", binding), None)
    ))
  }

  val succ1 : MatchResult[List[Entry]] = {
    val s11 = S(1, 1, Text("A"))
    val s31 = S(3, 1, Text("LN"))
    val CWE_2_7_XOR: C = C(0, 1, List(
      s11,
      s31,
    ))
    val binding = SingleCodeBinding(".", "A", "LN",
      List(
        BindingLocation("1[*]", Some("3[*]")),
      )
    )
    check(CWE_2_7_XOR, binding) must containTheSameElementsAs(List(
      createEntry(CWE_2_7_XOR, VSValidationCode.SingleCodeSuccess(CWE_2_7_XOR, s11, binding), None)
    ))
  }

  val succ2 : MatchResult[List[Entry]] = {
    val s11 = S(1, 1, Text("A"))
    val s31 = S(3, 1, Text("LN"))
    val s41 = S(4, 1, Text("X"))
    val s61 = S(6, 1, Text("LN"))
    val CWE_2_7_XOR: C = C(0, 1, List(
      s11,
      s31,
      s41,
      s61
    ))
    val binding = SingleCodeBinding(".", "A", "LN",
      List(
        BindingLocation("1[*]", Some("3[*]")),
        BindingLocation("4[*]", Some("6[*]")),
      )
    )
    check(CWE_2_7_XOR, binding) must containTheSameElementsAs(List(
      createEntry(CWE_2_7_XOR, VSValidationCode.SingleCodeSuccess(CWE_2_7_XOR, s11, binding), None),
    ))
  }

  val succ3 : MatchResult[List[Entry]] = {
    val s11 = S(1, 1, Text("A"))
    val s31 = S(3, 1, Text("LN"))
    val s41 = S(4, 1, Text("A"))
    val s61 = S(6, 1, Text("LX"))
    val CWE_2_7_XOR: C = C(0, 1, List(
      s11,
      s31,
      s41,
      s61
    ))
    val binding = SingleCodeBinding(".", "A", "LN",
      List(
        BindingLocation("1[*]", Some("3[*]")),
        BindingLocation("4[*]", Some("6[*]")),
      )
    )
    check(CWE_2_7_XOR, binding) must containTheSameElementsAs(List(
      createEntry(CWE_2_7_XOR, VSValidationCode.SingleCodeSuccess(CWE_2_7_XOR, s11, binding), None),
    ))
  }

  val succ4 : MatchResult[List[Entry]] = {
    val s11 = S(1, 1, Text("A"))
    val s31 = S(3, 1, Text("LN"))
    val s41 = S(4, 1, Text("A"))
    val CWE_2_7_XOR: C = C(0, 1, List(
      s11,
      s31,
      s41,
    ))
    val binding = SingleCodeBinding(".", "A", "LN",
      List(
        BindingLocation("1[*]", Some("3[*]")),
        BindingLocation("4[*]", Some("6[*]")),
      )
    )
    check(CWE_2_7_XOR, binding) must containTheSameElementsAs(List(
      createEntry(CWE_2_7_XOR, VSValidationCode.SingleCodeSuccess(CWE_2_7_XOR, s11, binding), None),
    ))
  }


  trait Default {
    val reqs: List[Req] = List[Req]()
    val location: Location = Location(null, "desc ...", "Path", -1, -1)
    val hasExtra = false
    val req: Req = Req(-1, "", Usage.O, None, None, None, Nil, None, hide = false, None)
    val rawMessageValue = ""
  }

  case class S(override val position: Int, instance: Int, value: Value, override val req: Req = Req(-1, "", Usage.O, None, None, None, Nil, None, hide = false, None))
    extends Simple  with Default

  case class C(override val position: Int, instance: Int, children: List[Element], override val req: Req = Req(-1, "", Usage.O, None, None, None, Nil, None, hide = false, None))
    extends Complex with Default

}
