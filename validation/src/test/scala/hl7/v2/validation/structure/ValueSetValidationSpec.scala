package hl7.v2.validation.structure

import org.specs2.Specification
import hl7.v2.parser.impl.DefaultParser
import hl7.v2.validation.vs.{BindingLocation, Code, CodeHolder, CodeUsage, EmptyValueSetSpecification, Extensibility, Stability, VSValidationCode, InternalValueSet => ValueSet, ValueSetBinding, ValueSetLibraryImpl, ValueSetSpecification}
import hl7.v2.profile.{BindingStrength, Req, Usage}
import hl7.v2.instance.Location
import hl7.v2.instance.Value
import hl7.v2.instance.Simple
import hl7.v2.instance.Element
import hl7.v2.instance.Complex
import hl7.v2.instance.Text
import org.specs2.specification.SpecPart


trait ValueSetValidationSpec extends Specification
       with hl7.v2.validation.vs.DefaultValueSetValidator
       with Helpers
       with DefaultParser {def is = s2"""

  ValueSet Specification
    Specification Errors
      Validation should detect an empty VS (Primitive/Single, Complex/Single, Complex/Multiple) $emptyVs
      Validation should detect an VS excluded from validation (Primitive/Single, Complex/Single, Complex/Multiple) $excludedVs
      Validation should detect a VS Not Found (Primitive/Single, Complex/Single, Complex/Multiple) $vsNotFound
      Validation should detect an Undefined Binding Strength, stop the validation and return a UBS detection (Primitive/Single, Complex/Single, Complex/Multiple) $ubs
      Validation should detect a binding on a simple element with multiple value sets, stop the validation $multiVsOnSimple
      Validation should detect a used code that is Permitted inside a Closed value set (Primitive/Single, Complex/Single, Complex/Multiple) $PinsideClosed
      Validation should detect a dynamic value set in the case of a PVS code (Primitive/Single, Complex/Single, Complex/Multiple) $PVSDynamic
      Validation should detect a dynamic value set in the case of a Code Not Found code (Primitive/Single, Complex/Single, Complex/Multiple) $NotFoundDynamic
      Complex element with unresolvable binding location should return a spec error $unresolvableBL
      Complex element with unresolvable code system binding location should return a spec error $unresolvableCsBL


    Primitive
      Simple element with R code present in the value set should return an RVS detection $checkSimpleBindingR
      Simple element with P code present in the value set should return an PVS detection $checkSimpleBindingP
      Simple element with E code present in the value set should return an EVS detection $checkSimpleBindingE
      Simple element with NoUsage code present in the value set should return an NoUsage detection $checkSimpleBindingNoUsage
      Simple element with code not present in the value set should return a CodeNotFound detection $codeNotFound
      Simple element with code present multiple times in the value set should return a duplicate code detection and a found detection for each match with the correct found detection for usage $simpleSingleVsDuplicate

    Complex
      Single Binding Location
        Single Value Set
          Complex element with R code present in the value set should return an RVS detection $checkComplexBindingR
          Complex element with P code present in the value set should return an PVS detection $checkComplexBindingP
          Complex element with E code present in the value set should return an EVS detection $checkComplexBindingE
          Complex element with NoUsage code present in the value set should return a NoUsage detection $checkComplexBindingNoUsage
          Complex element with code not present in the value set should return a CodeNotFound detection $checkComplexBindingNotFound
          Complex element with code present with different code system in the value set should return a Invalid Code System and a Code Not Found detection $checkComplexBindingInvalidCodeSystem
          Complex element with code present multiple times in the value set should return a duplicate code detection and a found detection for each match with the correct usage $checkComplexBindingDuplicateInVs
        Multiple Value Set
          Complex element with R code present in one of the value sets should return an RVS detection $checkComplexBindingMultiVsR
          Complex element with P code present in one of the value sets should return an PVS detection $checkComplexBindingMultiVsP
          Complex element with E code present in one of the value sets should return an EVS detection $checkComplexBindingMultiVsE
          Complex element with NoUsage code present in one of the value sets should return a NoUsage detection $checkComplexBindingMultiVsNoUsage
          Complex element with code not present in any of the value sets should return a CodeNotFound detection $checkComplexBindingMultiVsNotFound
          Complex element with a code present in multiple value sets with the wrong code system should return an Invalid Code System from each VS / Match and a code not found detection  $checkComplexBindingMultiVsInvalidCS
          Complex element with a code present in one value set with the right code system and another with the wrong code system should return an RVS detection of the right code found $checkComplexBindingMultiVsValidAndInvalidCS
          Complex element with code present in multiple value sets should return a duplicate code detection $checkComplexBindingMultiDuplicate
      Multiple Binding Location
        Single Value Set
          Complex element with multiple (2) binding locations with different codes found in value set (RVS and PVS) should return an RVS and PVS detection and an XOR detection $checkComplexBindingMultiBLRP
          Complex element with multiple (2) binding locations with different codes one found (E) in value set and one not found should return an EVS and not return a code not found detection for the second location $checkComplexBindingMultiBLRNotFound
          Complex element with multiple (2) binding locations with different codes both not present in value set should return multiple (2) code not found detection $checkComplexBindingMultiBLBothNotFound
          Complex element with multiple (2) binding locations with different codes both with wrong code system in value set should return invalid code system and code not found detection for each location $checkComplexBindingMultiBLBothInvalidCs
        Multiple Value Set
          Complex element with multiple (2) binding locations with different codes found in same value set (RVS and PVS) should return an RVS and PVS detection and an XOR detection $checkComplexBindingMultiBLMultiVsRP_1
          Complex element with multiple (2) binding locations with different codes each found in a different value set (RVS and PVS) should return an RVS and PVS detection $checkComplexBindingMultiBLMultiVsRP_2
          Complex element with multiple (2) binding locations with different codes one found (E) in value set and one not found should return an EVS and not return a code not found detection for the second location $checkComplexBindingMultiBLMultiVsRNotFound
          Complex element with multiple (2) binding locations with different codes both not present in any value set should return multiple (2) code not found detection $checkComplexBindingMultiBLMultiVsBothNotFound
          Complex element with multiple (2) binding locations with different codes with each present in one value set with the right code system (R) and wrong code system in the other (P) should return RVS for each location $checkComplexBindingMultiBLMultiVsOneInvalidCsOtherPass


  """

  val vsSpecification: ValueSetSpecification = EmptyValueSetSpecification

  // ============================================ MOCK ===========================================
  // ---------------------------------------- DATA ELEMENTS --------------------------------------
  val HD_1: C = C(0, 1, List(
    S(1, 1, Text("A")),
    S(2, 1, Text("B")),
    S(3, 1, Text("C")),
  ))

  val HD_2: C = C(0, 1, List(
    S(1, 1, Text("D")),
    S(2, 1, Text("E")),
    S(3, 1, Text("F")),
  ))

  val CWE_2_7: C = C(0, 1, List(
    S(1, 1, Text("A")),
    S(3, 1, Text("SYS")),

    S(4, 1, Text("B")),
    S(6, 1, Text("SYS")),

    S(10, 1, Text("C")),
    S(12, 1, Text("SYS")),
  ))

  val CWE_2_7_a: C = C(0, 1, List(
    S(1, 1, Text("D")),
    S(3, 1, Text("SYS")),

    S(4, 1, Text("B")),
    S(6, 1, Text("SYS")),

    S(10, 1, Text("C")),
    S(12, 1, Text("SYS")),
  ))

  val CWE_2_7_b: C = C(0, 1, List(
    S(1, 1, Text("M")),
    S(3, 1, Text("SYS")),

    S(4, 1, Text("N")),
    S(6, 1, Text("SYS")),
  ))

  // ---------------------------------------- VALUE SETS --------------------------------------------

  val vs1: ValueSet = ValueSet("vs1", None, None, List(Code("A", "des", CodeUsage.R, "SYS"),Code("B", "des", CodeUsage.P, "SYS"),Code("C", "des", CodeUsage.E, "SYS")))
  val vs1_no_usage: ValueSet = ValueSet("vs1_no_usage", None, None, List(Code("A", "des", null, "SYS"),Code("B", "des", CodeUsage.P, "SYS"),Code("C", "des", CodeUsage.E, "SYS")))
  val vs1_closed: ValueSet = ValueSet("vs1_closed", Some(Extensibility.Closed), None, List(Code("A", "des", CodeUsage.P, "SYS"),Code("B", "des", CodeUsage.P, "SYS"),Code("C", "des", CodeUsage.P, "SYS")))
  val vs1_dynamic: ValueSet = ValueSet("vs1_dynamic", None, Some(Stability.Dynamic), List(Code("A", "des", CodeUsage.P, "SYS"),Code("H", "des", CodeUsage.P, "SYS"),Code("G", "des", CodeUsage.P, "SYS")))

  val vs1_cs: ValueSet = ValueSet("vs1_cs", None, None, List(Code("A", "des", CodeUsage.R, "SYS1"),Code("B", "des", CodeUsage.P, "SYS1"),Code("C", "des", CodeUsage.E, "SYS1")))
  val vs1_cs_1: ValueSet = ValueSet("vs1_cs_1", None, None, List(Code("A", "des", CodeUsage.R, "SYS2"),Code("B", "des", CodeUsage.P, "SYS2"),Code("C", "des", CodeUsage.E, "SYS2")))

  val vs1_1: ValueSet = ValueSet("vs1_1", None, None, List(Code("D", "des", CodeUsage.R, "SYS"),Code("E", "des", CodeUsage.P, "SYS"),Code("F", "des", CodeUsage.E, "SYS")))
  val vs1_1_cs: ValueSet = ValueSet("vs1_1_cs", None, None, List(Code("D", "des", CodeUsage.R, "SYS1"),Code("E", "des", CodeUsage.P, "SYS1"),Code("F", "des", CodeUsage.E, "SYS1")))

  val vs2: ValueSet = ValueSet("vs2", None, None, List(Code("D", "des", CodeUsage.R, "SYS"),Code("D", "des", CodeUsage.P, "SYS"),Code("C", "des", CodeUsage.E, "SYS")))
  val vs2_2: ValueSet = ValueSet("vs2_2", None, None, List(Code("X", "des", CodeUsage.R, "SYS"),Code("Y", "des", CodeUsage.P, "SYS"),Code("Z", "des", CodeUsage.E, "SYS")))
  val vs2_2_2: ValueSet = ValueSet("vs2_2_2", None, None, List(Code("U", "des", CodeUsage.R, "SYS"),Code("R", "des", CodeUsage.P, "SYS"),Code("S", "des", CodeUsage.E, "SYS")))

  val vs3: ValueSet = ValueSet("vs3", None, None, List(Code("A", "des", CodeUsage.P, "SYS1"),Code("B", "des", CodeUsage.P, "SYS1"),Code("C", "des", CodeUsage.E, "SYS1")))
  val vs3_1: ValueSet = ValueSet("vs3_1", None, None, List(Code("A", "des", CodeUsage.P, "SYS"),Code("B", "des", CodeUsage.P, "SYS"),Code("C", "des", CodeUsage.E, "SYS")))

  val vs4: ValueSet = ValueSet("vs4", None, None, List(Code("M", "des", CodeUsage.R, "SYS"),Code("N", "des", CodeUsage.P, "SYS1")))
  val vs4_1: ValueSet = ValueSet("vs4_1", None, None, List(Code("M", "des", CodeUsage.P, "SYS1"),Code("N", "des", CodeUsage.R, "SYS")))

  val empty: ValueSet = ValueSet("empty", None, None, List())
  val valueSetLibrary = new ValueSetLibraryImpl(List("excluded"), (vs1::vs2::vs3::vs1_1::vs2_2::vs1_cs::vs1_cs_1::vs1_1_cs::vs3_1::vs2_2_2::vs4::vs4_1::empty::vs1_no_usage::vs1_closed::vs1_dynamic::Nil).map(x => { (x.id, x) }).toMap)

  // ============================================ TEST CASES ===========================================


  val emptyVs: SpecPart = {
    // Simple
    val sBindings = ValueSetBinding("1[*]", Some(BindingStrength.R), List("empty"), List(BindingLocation(".", None)))::Nil

    // Complex
    val cBindings = ValueSetBinding(".", Some(BindingStrength.R), List("empty"), List(BindingLocation("1[1]", Some("3[1]")))) :: ValueSetBinding(".", Some(BindingStrength.R), List("vs1", "empty"), List(BindingLocation("1[1]", Some("3[1]")))) :: Nil

    val parts: Seq[(Element, ValueSetBinding)] = sBindings.map(b => (HD_1, b)) ::: cBindings.map(b => (CWE_2_7, b))
    parts.map(c => check(c._1, c._2) must contain(createEntry(c._1, VSValidationCode.EmptyVS(empty), Some(BindingStrength.R))))
  }

  val PinsideClosed: SpecPart = {
    // Simple
    val sBindings = ValueSetBinding("1[*]", Some(BindingStrength.R), List("vs1_closed"), List(BindingLocation(".", None)))::Nil

    // Complex
    val cBindings = ValueSetBinding(".", Some(BindingStrength.R), List("vs1_closed"), List(BindingLocation("1[1]", Some("3[1]")))) :: ValueSetBinding(".", Some(BindingStrength.R), List("vs2_2", "vs1_closed"), List(BindingLocation("1[1]", Some("3[1]")))) :: Nil

    val parts: Seq[(Element, ValueSetBinding)] = sBindings.map(b => (HD_1, b)) ::: cBindings.map(b => (CWE_2_7, b))
    parts.map(c => check(c._1, c._2) must containTheSameElementsAs(List(
      createEntry(c._1, VSValidationCode.UsageAndExtensibilityNotCompatible(Code("A", "des", CodeUsage.P, "SYS"), vs1_closed), Some(BindingStrength.R)),
      createEntry(c._1, VSValidationCode.PVS(Code("A", "des", CodeUsage.P, "SYS"), vs1_closed), Some(BindingStrength.R)),
    )))
  }

  val PVSDynamic: SpecPart = {
    // Simple
    val sBindings = ValueSetBinding("1[*]", Some(BindingStrength.R), List("vs1_dynamic"), List(BindingLocation(".", None)))::Nil

    // Complex
    val cBindings = ValueSetBinding(".", Some(BindingStrength.R), List("vs1_dynamic"), List(BindingLocation("1[1]", Some("3[1]")))) :: ValueSetBinding(".", Some(BindingStrength.R), List("vs2_2", "vs1_dynamic"), List(BindingLocation("1[1]", Some("3[1]")))) :: Nil

    val parts: Seq[(Element, ValueSetBinding)] = sBindings.map(b => (HD_1, b)) ::: cBindings.map(b => (CWE_2_7, b))
    parts.map(c => check(c._1, c._2) must containTheSameElementsAs(List(
      createEntry(c._1, VSValidationCode.DynamicValueSet(vs1_dynamic), Some(BindingStrength.R)),
      createEntry(c._1, VSValidationCode.PVS(Code("A", "des", CodeUsage.P, "SYS"), vs1_dynamic), Some(BindingStrength.R)),
    )))
  }

  val NotFoundDynamic: SpecPart = {
    // Simple
    val sBindings = ValueSetBinding("2[*]", Some(BindingStrength.R), List("vs1_dynamic"), List(BindingLocation(".", None)))::Nil

    // Complex
    val cBindings = ValueSetBinding(".", Some(BindingStrength.R), List("vs1_dynamic"), List(BindingLocation("4[1]", Some("6[1]")))) :: ValueSetBinding(".", Some(BindingStrength.R), List("vs2_2", "vs1_dynamic"), List(BindingLocation("4[1]", Some("6[1]")))) :: Nil

    val parts: Seq[(Element, ValueSetBinding)] = sBindings.map(b => (HD_1, b)) ::: cBindings.map(b => (CWE_2_7, b))
    parts.map(c => check(c._1, c._2) must containTheSameElementsAs(List(
      createEntry(c._1, VSValidationCode.DynamicValueSet(vs1_dynamic), Some(BindingStrength.R)),
      createEntry(c._1, VSValidationCode.CodeNotFound("B", Some("SYS"), if(c._2.bindings.size === 1) List(vs1_dynamic) else List(vs2_2, vs1_dynamic)), Some(BindingStrength.R)),
    )))
  }

  val excludedVs: SpecPart = {
    // Simple
    val sBindingsA = ValueSetBinding("1[*]", Some(BindingStrength.R), List("excluded"), List(BindingLocation(".", None)))::Nil

    // Complex
    val cBindingsB = ValueSetBinding(".", Some(BindingStrength.R), List("excluded"), List(BindingLocation("1[1]", Some("3[1]")))) :: ValueSetBinding(".", Some(BindingStrength.R), List("vs1", "excluded"), List(BindingLocation("1[1]", Some("3[1]")))) :: Nil

    val parts: Seq[(Element, ValueSetBinding)] = sBindingsA.map(b => (HD_1, b)) ::: cBindingsB.map(b => (CWE_2_7, b))
    parts.map(c => check(c._1, c._2) must contain(createEntry(c._1, VSValidationCode.ExcludedVS("excluded"), Some(BindingStrength.R))))
  }

  val vsNotFound: SpecPart = {
    // Simple
    val sBindingsA = ValueSetBinding("1[*]", Some(BindingStrength.R), List("not_found"), List(BindingLocation(".", None)))::Nil

    // Complex
    val cBindingsB = ValueSetBinding(".", Some(BindingStrength.R), List("not_found"), List(BindingLocation("1[1]", Some("3[1]")))) :: ValueSetBinding(".", Some(BindingStrength.R), List("vs1", "not_found"), List(BindingLocation("1[1]", Some("3[1]")))) :: Nil

    val parts: Seq[(Element, ValueSetBinding)] = sBindingsA.map(b => (HD_1, b)) ::: cBindingsB.map(b => (CWE_2_7, b))
    parts.map(c => check(c._1, c._2) must contain(createEntry(c._1, VSValidationCode.VSNotFound("not_found"), Some(BindingStrength.R))))
  }

  val ubs = {
    // Simple
    val sBindingsA = ValueSetBinding("1[*]", Some(BindingStrength.U), List("vs1_1"), List(BindingLocation(".", None)))::Nil

    // Complex
    val cBindingsB = ValueSetBinding(".", Some(BindingStrength.U), List("vs1_1"), List(BindingLocation("1[1]", Some("3[1]")))) :: ValueSetBinding(".", Some(BindingStrength.U), List("vs2", "vs4"), List(BindingLocation("1[1]", Some("3[1]")))) :: Nil

    val parts: Seq[(Element, ValueSetBinding)] = sBindingsA.map(b => (HD_1, b)) ::: cBindingsB.map(b => (CWE_2_7, b))
    parts.map(c => check(c._1, c._2) must containTheSameElementsAs(List(createEntry(c._1, VSValidationCode.UBS(c._2), Some(BindingStrength.R)))))
  }

  val multiVsOnSimple: SpecPart  = {
    // Simple
    val binding = ValueSetBinding("1[*]", Some(BindingStrength.R), List("vs1_1", "vsx"), List(BindingLocation(".", None)))

    check(HD_1, binding) must containTheSameElementsAs(List(
      createEntry(HD_1, VSValidationCode.MultipleVSForPrimitive(binding), Some(BindingStrength.R))
    ))
  }

  val unresolvableBL: SpecPart = {
    val binding = ValueSetBinding("1[*]", Some(BindingStrength.R), List("vs1_1"), List(BindingLocation("1[1]", None)))

    check(HD_1, binding) must containTheSameElementsAs(List(
      createEntry(HD_1, VSValidationCode.InvalidCodeBindingLocation("1[1]", false), Some(BindingStrength.R))
    ))
  }

  val unresolvableCsBL: SpecPart = {
    val binding = ValueSetBinding("1[*]", Some(BindingStrength.R), List("vs1_1"), List(BindingLocation(".", Some("1[1]"))))

    check(HD_1, binding) must containTheSameElementsAs(List(
      createEntry(HD_1, VSValidationCode.InvalidCodeSystemBindingLocation("1[1]", false), Some(BindingStrength.R))
    ))
  }


  val checkSimpleBindingR: SpecPart = {
    check(HD_1, ValueSetBinding("1[*]", Some(BindingStrength.R), List("vs1"), List(BindingLocation(".", None)))) must containTheSameElementsAs(List(
      createEntry(HD_1, VSValidationCode.RVS(Code("A", "des", CodeUsage.R, "SYS"), vs1), Some(BindingStrength.R))
    ))
  }

  val checkSimpleBindingNoUsage: SpecPart = {
    check(HD_1, ValueSetBinding("1[*]", Some(BindingStrength.R), List("vs1_no_usage"), List(BindingLocation(".", None)))) must containTheSameElementsAs(List(
      createEntry(HD_1, VSValidationCode.NoUsage(Code("A", "des", null, "SYS"), vs1_no_usage), Some(BindingStrength.R))
    ))
  }

  val checkSimpleBindingP: SpecPart = {
    check(HD_1, ValueSetBinding("2[*]", Some(BindingStrength.R), List("vs1"), List(BindingLocation(".", None)))) must containTheSameElementsAs(List(
      createEntry(HD_1, VSValidationCode.PVS(Code("B", "des", CodeUsage.P, "SYS"), vs1), Some(BindingStrength.R))
    ))
  }

  val checkSimpleBindingE: SpecPart = {
    check(HD_1, ValueSetBinding("3[*]", Some(BindingStrength.R), List("vs1"), List(BindingLocation(".", None)))) must containTheSameElementsAs(List(
      createEntry(HD_1, VSValidationCode.EVS(Code("C", "des", CodeUsage.P, "SYS"), vs1), Some(BindingStrength.R))
    ))
  }

  val codeNotFound: SpecPart = {
    check(HD_2, ValueSetBinding("1[*]", Some(BindingStrength.R), List("vs1"), List(BindingLocation(".", None)))) must containTheSameElementsAs(List(
      createEntry(HD_2, VSValidationCode.CodeNotFound("D", None, List(vs1)), Some(BindingStrength.R))
    ))
  }

  val simpleSingleVsDuplicate: SpecPart = {
    check(HD_2, ValueSetBinding("1[*]", Some(BindingStrength.R), List("vs2"), List(BindingLocation(".", None)))) must containTheSameElementsAs(List(
      createEntry(HD_2, VSValidationCode.RVS(Code("D", "des", CodeUsage.P, "SYS"), vs2), Some(BindingStrength.R)),
      createEntry(HD_2, VSValidationCode.PVS(Code("D", "des", CodeUsage.P, "SYS"), vs2), Some(BindingStrength.R)),
      createEntry(HD_2, VSValidationCode.MultipleCodesFoundInValueSet("D",vs2, List(Code("D", "des", CodeUsage.R, "SYS"),Code("D", "des", CodeUsage.P, "SYS"))), Some(BindingStrength.R))
    ))
  }

  val checkComplexBindingR: SpecPart = {
    check(CWE_2_7, ValueSetBinding(".", Some(BindingStrength.R), List("vs1"), List(BindingLocation("1[1]", Some("3[1]"))))) must containTheSameElementsAs(List(
      createEntry(CWE_2_7, VSValidationCode.RVS(Code("A", "des", CodeUsage.R, "SYS"), vs1), Some(BindingStrength.R))
    ))
  }

  val checkComplexBindingNoUsage: SpecPart = {
    check(CWE_2_7, ValueSetBinding(".", Some(BindingStrength.R), List("vs1_no_usage"), List(BindingLocation("1[1]", Some("3[1]"))))) must containTheSameElementsAs(List(
      createEntry(CWE_2_7, VSValidationCode.NoUsage(Code("A", "des", null, "SYS"), vs1_no_usage), Some(BindingStrength.R))
    ))
  }

  val checkComplexBindingP: SpecPart = {
    check(CWE_2_7, ValueSetBinding(".", Some(BindingStrength.R), List("vs1"), List(BindingLocation("4[1]", Some("6[1]"))))) must containTheSameElementsAs(List(
      createEntry(CWE_2_7, VSValidationCode.PVS(Code("B", "des", CodeUsage.P, "SYS"), vs1), Some(BindingStrength.R))
    ))
  }

  val checkComplexBindingE: SpecPart = {
    check(CWE_2_7, ValueSetBinding(".", Some(BindingStrength.R), List("vs1"), List(BindingLocation("10[1]", Some("12[1]"))))) must containTheSameElementsAs(List(
      createEntry(CWE_2_7, VSValidationCode.EVS(Code("C", "des", CodeUsage.E, "SYS"), vs1), Some(BindingStrength.R))
    ))
  }

  val checkComplexBindingNotFound: SpecPart = {
    check(CWE_2_7, ValueSetBinding(".", Some(BindingStrength.R), List("vs2"), List(BindingLocation("1[1]", Some("3[1]"))))) must containTheSameElementsAs(List(
      createEntry(CWE_2_7, VSValidationCode.CodeNotFound("A", Some("SYS"), List(vs2)), Some(BindingStrength.R))
    ))
  }

  val checkComplexBindingInvalidCodeSystem: SpecPart = {
    check(CWE_2_7, ValueSetBinding(".", Some(BindingStrength.R), List("vs3"), List(BindingLocation("1[1]", Some("3[1]"))))) must containTheSameElementsAs(List(
      createEntry(CWE_2_7, VSValidationCode.CodeNotFound("A", Some("SYS"), List(vs3)), Some(BindingStrength.R)),
      createEntry(CWE_2_7, VSValidationCode.SimpleCodeFoundInvalidCodeSystem("A", "SYS1", "SYS", vs3), Some(BindingStrength.R))
    ))
  }

  val checkComplexBindingDuplicateInVs: SpecPart = {
    check(CWE_2_7_a, ValueSetBinding(".", Some(BindingStrength.R), List("vs2"), List(BindingLocation("1[1]", Some("3[1]"))))) must containTheSameElementsAs(List(
      createEntry(CWE_2_7_a, VSValidationCode.RVS(Code("D", "des", CodeUsage.P, "SYS"), vs2), Some(BindingStrength.R)),
      createEntry(CWE_2_7_a, VSValidationCode.PVS(Code("D", "des", CodeUsage.P, "SYS"), vs2), Some(BindingStrength.R)),
      createEntry(CWE_2_7_a, VSValidationCode.MultipleCodeAndCodeSystemFound("D", "SYS", vs2, List(Code("D", "des", CodeUsage.R, "SYS"),Code("D", "des", CodeUsage.P, "SYS"))), Some(BindingStrength.R))
    ))
  }

  val checkComplexBindingMultiVsNoUsage: SpecPart = {
    check(CWE_2_7, ValueSetBinding(".", Some(BindingStrength.R), List("vs1_no_usage", "vs1_1"), List(BindingLocation("1[1]", Some("3[1]"))))) must containTheSameElementsAs(List(
      createEntry(CWE_2_7, VSValidationCode.NoUsage(Code("A", "des", null, "SYS"), vs1_no_usage), Some(BindingStrength.R))
    ))
  }

  val checkComplexBindingMultiVsR: SpecPart = {
    check(CWE_2_7, ValueSetBinding(".", Some(BindingStrength.R), List("vs1", "vs1_1"), List(BindingLocation("1[1]", Some("3[1]"))))) must containTheSameElementsAs(List(
      createEntry(CWE_2_7, VSValidationCode.RVS(Code("A", "des", CodeUsage.R, "SYS"), vs1), Some(BindingStrength.R))
    ))
  }

  val checkComplexBindingMultiVsE: SpecPart = {
    check(CWE_2_7, ValueSetBinding(".", Some(BindingStrength.R), List("vs1", "vs1_1"), List(BindingLocation("4[1]", Some("6[1]"))))) must containTheSameElementsAs(List(
      createEntry(CWE_2_7, VSValidationCode.PVS(Code("B", "des", CodeUsage.P, "SYS"), vs1), Some(BindingStrength.R))
    ))
  }

  val checkComplexBindingMultiVsP: SpecPart = {
    check(CWE_2_7, ValueSetBinding(".", Some(BindingStrength.R), List("vs1", "vs1_1"), List(BindingLocation("10[1]", Some("12[1]"))))) must containTheSameElementsAs(List(
      createEntry(CWE_2_7, VSValidationCode.EVS(Code("C", "des", CodeUsage.E, "SYS"), vs1), Some(BindingStrength.R))
    ))
  }

  val checkComplexBindingMultiVsNotFound: SpecPart = {
    check(CWE_2_7, ValueSetBinding(".", Some(BindingStrength.R), List("vs2_2", "vs1_1"), List(BindingLocation("1[1]", Some("3[1]"))))) must containTheSameElementsAs(List(
      createEntry(CWE_2_7, VSValidationCode.CodeNotFound("A", Some("SYS"), List(vs2_2, vs1_1)), Some(BindingStrength.R))
    ))
  }

  val checkComplexBindingMultiVsInvalidCS: SpecPart = {
    check(CWE_2_7, ValueSetBinding(".", Some(BindingStrength.R), List("vs1_cs", "vs1_cs_1"), List(BindingLocation("1[1]", Some("3[1]"))))) must containTheSameElementsAs(List(
      createEntry(CWE_2_7, VSValidationCode.CodeNotFound("A", Some("SYS"), List(vs1_cs, vs1_cs_1)), Some(BindingStrength.R)),
      createEntry(CWE_2_7, VSValidationCode.SimpleCodeFoundInvalidCodeSystem("A", "SYS1", "SYS", vs1_cs), Some(BindingStrength.R)),
      createEntry(CWE_2_7, VSValidationCode.SimpleCodeFoundInvalidCodeSystem("A", "SYS2", "SYS", vs1_cs_1), Some(BindingStrength.R))
    ))
  }

  val checkComplexBindingMultiVsValidAndInvalidCS: SpecPart = {
    check(CWE_2_7, ValueSetBinding(".", Some(BindingStrength.R), List("vs1", "vs1_cs_1"), List(BindingLocation("1[1]", Some("3[1]"))))) must containTheSameElementsAs(List(
      createEntry(CWE_2_7, VSValidationCode.RVS(Code("A", "des", CodeUsage.R, "SYS"), vs1), Some(BindingStrength.R))
    ))
  }

  val checkComplexBindingMultiDuplicate: SpecPart = {
    check(CWE_2_7, ValueSetBinding(".", Some(BindingStrength.R), List("vs3_1", "vs1"), List(BindingLocation("1[1]", Some("3[1]"))))) must containTheSameElementsAs(List(
      createEntry(CWE_2_7, VSValidationCode.RVS(Code("A", "des", CodeUsage.R, "SYS"), vs1), Some(BindingStrength.R)),
      createEntry(CWE_2_7, VSValidationCode.PVS(Code("A", "des", CodeUsage.P, "SYS"), vs3_1), Some(BindingStrength.R)),
      createEntry(CWE_2_7, VSValidationCode.FoundInMultipleValueSets("A", Some("SYS"), List(vs3_1,vs1)), Some(BindingStrength.R))
    ))
  }

  val checkComplexBindingMultiBLRP: SpecPart = {
    check(CWE_2_7, ValueSetBinding(".", Some(BindingStrength.R), List("vs1"), List(BindingLocation("1[1]", Some("3[1]")), BindingLocation("4[1]", Some("6[1]"))))) must containTheSameElementsAs(List(
      createEntry(CWE_2_7, VSValidationCode.CodedElementXOR(List(
        CodeHolder(S(1, 1, Text("A")), Some(S(3, 1, Text("SYS"))), BindingLocation("1[1]", Some("3[1]"))),
        CodeHolder(S(4, 1, Text("B")), Some(S(6, 1, Text("SYS"))), BindingLocation("4[1]", Some("6[1]")))
      ), vs1), Some(BindingStrength.R)),
      createEntry(CWE_2_7, VSValidationCode.RVS(Code("A", "des", CodeUsage.R, "SYS"), vs1), Some(BindingStrength.R)),
      createEntry(CWE_2_7, VSValidationCode.PVS(Code("B", "des", CodeUsage.P, "SYS"), vs1), Some(BindingStrength.R)),
    ))
  }

  val checkComplexBindingMultiBLRNotFound: SpecPart = {
    check(CWE_2_7_a, ValueSetBinding(".", Some(BindingStrength.R), List("vs1"), List(BindingLocation("10[1]", Some("12[1]")), BindingLocation("1[1]", Some("3[1]"))))) must containTheSameElementsAs(List(
      createEntry(CWE_2_7_a, VSValidationCode.EVS(Code("C", "des", CodeUsage.E, "SYS"), vs1), Some(BindingStrength.R)),
    ))
  }

  val checkComplexBindingMultiBLBothNotFound: SpecPart = {
    check(CWE_2_7_a, ValueSetBinding(".", Some(BindingStrength.R), List("vs2_2"), List(BindingLocation("10[1]", Some("12[1]")), BindingLocation("1[1]", Some("3[1]"))))) must containTheSameElementsAs(List(
      createEntry(CWE_2_7_a, VSValidationCode.CodeNotFound("C", Some("SYS"), List(vs2_2)), Some(BindingStrength.R)),
      createEntry(CWE_2_7_a, VSValidationCode.CodeNotFound("D", Some("SYS"), List(vs2_2)), Some(BindingStrength.R))
    ))
  }

  val checkComplexBindingMultiBLBothInvalidCs: SpecPart = {
    check(CWE_2_7, ValueSetBinding(".", Some(BindingStrength.R), List("vs1_cs"), List(BindingLocation("1[1]", Some("3[1]")), BindingLocation("4[1]", Some("6[1]"))))) must containTheSameElementsAs(List(
      createEntry(CWE_2_7, VSValidationCode.CodeNotFound("A", Some("SYS"), List(vs1_cs)), Some(BindingStrength.R)),
      createEntry(CWE_2_7, VSValidationCode.CodeNotFound("B", Some("SYS"), List(vs1_cs)), Some(BindingStrength.R)),
      createEntry(CWE_2_7, VSValidationCode.SimpleCodeFoundInvalidCodeSystem("A", "SYS1", "SYS", vs1_cs), Some(BindingStrength.R)),
      createEntry(CWE_2_7, VSValidationCode.SimpleCodeFoundInvalidCodeSystem("B", "SYS1", "SYS", vs1_cs), Some(BindingStrength.R))
    ))
  }

  val checkComplexBindingMultiBLMultiVsRP_1: SpecPart = {
    check(CWE_2_7, ValueSetBinding(".", Some(BindingStrength.R), List("vs1", "vs2_2"), List(BindingLocation("1[1]", Some("3[1]")), BindingLocation("4[1]", Some("6[1]"))))) must containTheSameElementsAs(List(
      createEntry(CWE_2_7, VSValidationCode.CodedElementXOR(List(
        CodeHolder(S(1, 1, Text("A")), Some(S(3, 1, Text("SYS"))), BindingLocation("1[1]", Some("3[1]"))),
        CodeHolder(S(4, 1, Text("B")), Some(S(6, 1, Text("SYS"))), BindingLocation("4[1]", Some("6[1]")))
      ), vs1), Some(BindingStrength.R)),
      createEntry(CWE_2_7, VSValidationCode.RVS(Code("A", "des", CodeUsage.R, "SYS"), vs1), Some(BindingStrength.R)),
      createEntry(CWE_2_7, VSValidationCode.PVS(Code("B", "des", CodeUsage.P, "SYS"), vs1), Some(BindingStrength.R)),
    ))
  }

  val checkComplexBindingMultiBLMultiVsRP_2: SpecPart = {
    check(CWE_2_7_a, ValueSetBinding(".", Some(BindingStrength.R), List("vs1", "vs1_1"), List(BindingLocation("1[1]", Some("3[1]")), BindingLocation("4[1]", Some("6[1]"))))) must containTheSameElementsAs(List(
      createEntry(CWE_2_7, VSValidationCode.RVS(Code("D", "des", CodeUsage.R, "SYS"), vs1_1), Some(BindingStrength.R)),
      createEntry(CWE_2_7, VSValidationCode.PVS(Code("B", "des", CodeUsage.P, "SYS"), vs1), Some(BindingStrength.R)),
    ))
  }

  val checkComplexBindingMultiBLMultiVsRNotFound: SpecPart = {
    check(CWE_2_7_a, ValueSetBinding(".", Some(BindingStrength.R), List("vs1", "vs2_2"), List(BindingLocation("10[1]", Some("12[1]")), BindingLocation("1[1]", Some("3[1]"))))) must containTheSameElementsAs(List(
      createEntry(CWE_2_7_a, VSValidationCode.EVS(Code("C", "des", CodeUsage.E, "SYS"), vs1), Some(BindingStrength.R)),
    ))
  }

  val checkComplexBindingMultiBLMultiVsBothNotFound: SpecPart = {
    check(CWE_2_7_a, ValueSetBinding(".", Some(BindingStrength.R), List("vs2_2", "vs2_2_2"), List(BindingLocation("10[1]", Some("12[1]")), BindingLocation("1[1]", Some("3[1]"))))) must containTheSameElementsAs(List(
      createEntry(CWE_2_7_a, VSValidationCode.CodeNotFound("C", Some("SYS"), List(vs2_2, vs2_2_2)), Some(BindingStrength.R)),
      createEntry(CWE_2_7_a, VSValidationCode.CodeNotFound("D", Some("SYS"), List(vs2_2, vs2_2_2)), Some(BindingStrength.R))
    ))
  }

  val checkComplexBindingMultiBLMultiVsOneInvalidCsOtherPass: SpecPart = {
    check(CWE_2_7_b, ValueSetBinding(".", Some(BindingStrength.R), List("vs4", "vs4_1"), List(BindingLocation("1[1]", Some("3[1]")), BindingLocation("4[1]", Some("6[1]"))))) must containTheSameElementsAs(List(
      createEntry(CWE_2_7_b, VSValidationCode.RVS(Code("M", "des", CodeUsage.R, "SYS"), vs4), Some(BindingStrength.R)),
      createEntry(CWE_2_7_b, VSValidationCode.RVS(Code("N", "des", CodeUsage.R, "SYS"), vs4_1), Some(BindingStrength.R)),
    ))
  }


  // ============================================ HELPERS ===========================================

  trait Default {
    val reqs: List[Req] = List[Req]()
    val location: Location = Location(null, "desc ...", "Path", -1, -1)
    val hasExtra = false
    val req: Req = Req(-1, "", Usage.O, None, None, None, Nil)
  }

  case class S(override val position: Int, instance: Int, value: Value)
    extends Simple  with Default

  case class C(override val position: Int, instance: Int, children: List[Element])
    extends Complex with Default
}