package hl7.v2.validation.structure

import gov.nist.validation.report.Entry
import org.specs2.Specification
import hl7.v2.parser.impl.DefaultParser
import hl7.v2.validation.vs.{BindingLocation, Code, CodeHolder, CodeUsage, DefaultValueSetSpecification, EmptyValueSetSpecification, Extensibility, Stability, VSValidationCode, ValueSetBinding, ValueSetLibraryImpl, ValueSetSpecification, InternalValueSet => ValueSet}
import hl7.v2.profile.{BindingStrength, Composite, Primitive, Req, Usage}
import hl7.v2.instance.{Complex, ComplexComponent, Element, Location, Simple, SimpleComponent, Text, Value}
import hl7.v2.validation.FeatureFlags
import org.specs2.matcher.MatchResult

import scala.collection.immutable.List


trait ValueSetValidationSpec extends Specification
       with hl7.v2.validation.vs.DefaultValueSetValidator
       with Helpers
       with DefaultParser {def is =
  s2"""

  ValueSet Specification
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

    Legacy 0396 Check

      If ValueSet Library contains HL7nnnn in 0396 without a pattern containsLegacy0396Codes should be TRUE $vsLibHL7nnnnNoPattern
      If ValueSet Library contains 99zzz in 0396 without a pattern containsLegacy0396Codes should be TRUE $vsLib99zzzNoPattern
      If ValueSet Library contains HL7nnnn in 0396 with a pattern containsLegacy0396Codes should be FALSE $vsLibHL7nnnnPattern
      If ValueSet Library contains 99zzz in 0396 with a pattern containsLegacy0396Codes should be FALSE $vsLib99zzzPattern
      If ValueSet Library doesn't contain 0396 containsLegacy0396Codes should be FALSE $vsLibNo396
      If ValueSet Library contains 0396 without HL7nnnn or 99zzz containsLegacy0396Codes should be FALSE $vsLib396NoCode

      If Legacy0396 Feature Flag is ENABLED
        If binding is 0396 and code is HL70125
          If HL7nnnn is in 0396 with code usage R => RVS Detection $checkLegacy396EnabledHL7nnnnR
          If HL7nnnn is in 0396 with code usage P => PVS Detection $checkLegacy396EnabledHL7nnnnP
          If HL7nnnn is not in 0396 => Code not found Detection $checkLegacy396EnabledHL7nnnnNF
          If HL7nnnn is in 0396 with usage R and HL70125 is in 0396 with usage P => Duplicate Code Detection and HL7nnnn RVS and HL70125 PVS $checkLegacy396EnabledHL7nnnnBoth
        If binding is 0396 and code is 99001
          If 99zzz is in 0396 with code usage R  => RVS Detection $checkLegacy396Enabled99zzzR
          If 99zzz is in 0396 with code usage P => PVS Detection $checkLegacy396Enabled99zzzP
          If 99zzz is not in 0396 => Code not found Detection $checkLegacy396Enabled99zzzNF
          If 99zzz is in 0396 with code usage R and 99001 is in 0396 with usage P => Duplicate Code Detection and 99zzz RVS and 99001 PVS $checkLegacy396Enabled99zzzBoth
        If binding is 0396 and code is HL7nnnn and HL7nnnn is in 0396 => Code not found Detection (Because it's considered as a pattern) $checkLegacy396EnabledHL7nnnnExactMatch
        If binding is 0396 and code is 99zzz and 99zzz is in 0396 => RVS $checkLegacy396Enabled99zzzExactMatch
      If Legacy0396 Feature Flag is DISABLED
        If binding is 0396 and code is HL70125
          If HL7nnnn is in 0396 with code usage R => Code not found Detection $checkLegacy396DisabledHL7nnnnR
          If HL7nnnn is in 0396 with code usage P => Code not found Detection $checkLegacy396DisabledHL7nnnnP
          If HL7nnnn is not in 0396 => Code not found Detection $checkLegacy396DisabledHL7nnnnNF
          If HL7nnnn is in 0396 with usage R and HL70125 is in 0396 with usage P => HL70125 PVS Detection $checkLegacy396DisabledHL7nnnnBOTH
        If binding is 0396 and code is 99001
          If 99zzz is in 0396 with code usage R => Code not found Detection $checkLegacy396Disabled99zzzR
          If 99zzz is in 0396 with code usage P => Code not found Detection $checkLegacy396Disabled99zzzP
          If 99zzz is not in 0396 => Code not found Detection $checkLegacy396Disabled99zzzNF
          If 99zzz is in 0396 with usage R and 99001 is in 0396 with usage P => 99001 PVS Detection $checkLegacy396Disabled99zzzBOTH
        If binding is 0396 and code is HL7nnnn and HL7nnnn is in 0396 with usage R => RVS $checkLegacy396DisabledHL7nnnnExactMatch
        If binding is 0396 and code is 99zzz and 99zzz is in 0396 with usage R => RVS $checkLegacy396Disabled99zzzExactMatch

    Only check relevant elements
      Skip checking Binding Targets when usage is X or W $skipTargets
      Skip checking Binding when context element usage is X or W $skipContext
      Skip checking children of element when element usage is X or W $skipContextChildren

    Code Pattern Matching
      Simple element with value that matches a code pattern present in the value set should return an RVS detection $simpleCodePatternMatch
      Simple element with value that matches the VALUE but not the pattern of a code present in the value set should return a CodeNotFound detection $simpleCodePatternMatchValueNotRegex
      Simple element with value that matches a code pattern and a simple code should return a duplicate code detection and a found detection for each match with the correct found detection for usage $simpleCodePatternMatchCodeAndPattern
      Complex element with value that matches a code pattern present in the value set should return an RVS detection $complexCodePatternMatch
      Complex element with value that matches the VALUE but not the pattern of an R code present in the value set should return a CodeNotFound detection $complexCodePatternMatchValueNotRegex
      Complex element with value that matches a code pattern and a simple code should return a duplicate code detection and a found detection for each match with the correct found detection for usage $complexCodePatternMatchCodeAndPattern

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
          Complex element with code present in the value set and code system not populated should return a Code System Not Populated and a Code Not Found detection $checkComplexBindingCodeSystemNp
          Complex element with code present multiple times in the value set should return a duplicate code detection and a found detection for each match with the correct usage $checkComplexBindingDuplicateInVs
        Multiple Value Set
          Complex element with R code present in one of the value sets should return an RVS detection $checkComplexBindingMultiVsR
          Complex element with P code present in one of the value sets should return an PVS detection $checkComplexBindingMultiVsP
          Complex element with E code present in one of the value sets should return an EVS detection $checkComplexBindingMultiVsE
          Complex element with NoUsage code present in one of the value sets should return a NoUsage detection $checkComplexBindingMultiVsNoUsage
          Complex element with code not present in any of the value sets should return a CodeNotFound detection $checkComplexBindingMultiVsNotFound
          Complex element with a code present in multiple value sets with code system not populated should return a Code System Not Populated from each VS / Match and a code not found detection  $checkComplexBindingMultiVsCSNP
          Complex element with a code present in multiple value sets with the wrong code system should return an Invalid Code System from each VS / Match and a code not found detection  $checkComplexBindingMultiVsInvalidCS
          Complex element with a code present in one value set with the right code system and another with the wrong code system should return an RVS detection of the right code found $checkComplexBindingMultiVsValidAndInvalidCS
          Complex element with code present in multiple value sets should return a duplicate code detection $checkComplexBindingMultiDuplicate
      Multiple Binding Location
        Single Value Set
          Complex element with multiple (2) binding locations with different codes found in value set (RVS and PVS) should return an RVS and PVS detection and an XOR detection $checkComplexBindingMultiBLRP
          Complex element with multiple (2) binding locations with different codes one found (E) in value set and one not found should return an EVS and not return a code not found detection for the second location $checkComplexBindingMultiBLRNotFound
          Complex element with multiple (2) binding locations with different codes both not present in value set should return multiple (2) code not found detection $checkComplexBindingMultiBLBothNotFound
          Complex element with multiple (2) binding locations with different codes both with wrong code system in value set should return invalid code system and code not found detection for each location $checkComplexBindingMultiBLBothInvalidCs
          Complex element with multiple (2) binding locations with different codes both in value set but code system is not populated should return code system not populated and code not found detection for each location $checkComplexBindingMultiBLBothCSNP
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
    S(3, 1, Text("C"))
  ))

  val HD_2: C = C(0, 1, List(
    S(1, 1, Text("D")),
    S(2, 1, Text("E")),
    S(3, 1, Text("F"))
  ))

  val HD_CP: C = C(0, 1, List(
    S(1, 1, Text("A1")),
    S(2, 1, Text("A5")),
    S(3, 1, Text("PAT"))
  ))

  val HD_0396: C = C(0, 1, List(
    S(1, 1, Text("HL7nnnn")),
    S(2, 1, Text("99zzz")),
    S(3, 1, Text("HL70125")),
    S(4, 1, Text("99abc"))
  ))

  val CWE_2_7: C = C(0, 1, List(
    S(1, 1, Text("A")),
    S(3, 1, Text("SYS")),

    S(4, 1, Text("B")),
    S(6, 1, Text("SYS")),

    S(10, 1, Text("C")),
    S(12, 1, Text("SYS"))
  ))

  val CWE_2_7_CSNP: C = C(0, 1, List(
    S(1, 1, Text("A")),

    S(4, 1, Text("B")),

    S(10, 1, Text("C")),
    S(12, 1, Text("SYS"))
  ))

  val CWE_2_7_CP: C = C(0, 1, List(
    S(1, 1, Text("A1")),
    S(3, 1, Text("SYS")),

    S(4, 1, Text("A5")),
    S(6, 1, Text("SYS")),

    S(10, 1, Text("PAT")),
    S(12, 1, Text("SYS"))
  ))

  val CWE_2_7_a: C = C(0, 1, List(
    S(1, 1, Text("D")),
    S(3, 1, Text("SYS")),

    S(4, 1, Text("B")),
    S(6, 1, Text("SYS")),

    S(10, 1, Text("C")),
    S(12, 1, Text("SYS"))
  ))

  val CWE_2_7_b: C = C(0, 1, List(
    S(1, 1, Text("M")),
    S(3, 1, Text("SYS")),

    S(4, 1, Text("N")),
    S(6, 1, Text("SYS"))
  ))

  // ---------------------------------------- VALUE SETS --------------------------------------------

  val vs1: ValueSet = ValueSet("vs1", None, None, List(Code("A", "des", CodeUsage.R, "SYS"),Code("B", "des", CodeUsage.P, "SYS"),Code("C", "des", CodeUsage.E, "SYS")))
  val vs1_no_usage: ValueSet = ValueSet("vs1_no_usage", None, None, List(Code("A", "des", null, "SYS"),Code("B", "des", CodeUsage.P, "SYS"),Code("C", "des", CodeUsage.E, "SYS")))
  val vs1_closed: ValueSet = ValueSet("vs1_closed", Some(Extensibility.Closed), None, List(Code("A", "des", CodeUsage.P, "SYS"),Code("B", "des", CodeUsage.P, "SYS"),Code("C", "des", CodeUsage.P, "SYS")))
  val vs1_dynamic: ValueSet = ValueSet("vs1_dynamic", None, Some(Stability.Dynamic), List(Code("A", "des", CodeUsage.P, "SYS"),Code("H", "des", CodeUsage.P, "SYS"),Code("G", "des", CodeUsage.P, "SYS")))

  val vsCodePattern: ValueSet = ValueSet("vsCodePattern", None, None, List(Code("PAT", "pattern", CodeUsage.R, "SYS", Some("[A-Z][0-9]")),Code("A5", "duplicate", CodeUsage.E, "SYS")))

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

  val vs396_R: ValueSet = ValueSet("0396_R", None, None, List(Code("HL7nnnn", "des", CodeUsage.R, "SYS"),Code("99zzz", "des", CodeUsage.R, "SYS")))
  val vs396_P: ValueSet = ValueSet("0396_P", None, None, List(Code("HL7nnnn", "des", CodeUsage.P, "SYS"),Code("99zzz", "des", CodeUsage.P, "SYS")))
  val vs396_NF: ValueSet = ValueSet("0396_NF", None, None, List(Code("A", "des", CodeUsage.R, "SYS"),Code("B", "des", CodeUsage.R, "SYS")))
  val vs396_BOTH: ValueSet = ValueSet("0396_BOTH", None, None, List(Code("HL7nnnn", "des", CodeUsage.R, "SYS"),Code("HL70125", "des", CodeUsage.P, "SYS"),Code("99zzz", "des", CodeUsage.R, "SYS"),Code("99abc", "des", CodeUsage.P, "SYS")))


  val empty: ValueSet = ValueSet("empty", None, None, List())
  val valueSetLibrary = new ValueSetLibraryImpl(List("excluded"), (vsCodePattern::vs1::vs2::vs3::vs1_1::vs2_2::vs1_cs::vs1_cs_1::vs1_1_cs::vs3_1::vs2_2_2::vs4::vs4_1::empty::vs1_no_usage::vs1_closed::vs1_dynamic::vs396_R::vs396_P::vs396_NF::vs396_BOTH::Nil).map(x => { (x.id, x) }).toMap)

  // ============================================ TEST CASES ===========================================

  val simpleCodePatternMatch: MatchResult[List[Entry]] = {
    check(HD_CP, ValueSetBinding("1[*]", Some(BindingStrength.R), List("vsCodePattern"), List(BindingLocation(".", None)))) must containTheSameElementsAs(List(
      createEntry(HD_CP, VSValidationCode.RVS(Code("PAT", "pattern", CodeUsage.R, "SYS", Some("[A-Z][0-9]")), "A1", vsCodePattern), Some(BindingStrength.R))
    ))
  }

  val simpleCodePatternMatchValueNotRegex: MatchResult[List[Entry]] = check(HD_CP, ValueSetBinding("3[*]", Some(BindingStrength.R), List("vsCodePattern"), List(BindingLocation(".", None)))) must containTheSameElementsAs(List(
    createEntry(HD_CP, VSValidationCode.CodeNotFound("PAT", None, List(vsCodePattern)), Some(BindingStrength.R))
  ))

  val simpleCodePatternMatchCodeAndPattern: MatchResult[List[Entry]] = check(HD_CP, ValueSetBinding("2[*]", Some(BindingStrength.R), List("vsCodePattern"), List(BindingLocation(".", None))))  must containTheSameElementsAs(List(
    createEntry(HD_CP, VSValidationCode.RVS(Code("PAT", "pattern", CodeUsage.R, "SYS", Some("[A-Z][0-9]")), "A5", vsCodePattern), Some(BindingStrength.R)),
    createEntry(HD_CP, VSValidationCode.EVS(Code("A5", "duplicate", CodeUsage.E, "SYS"), "A5", vsCodePattern), Some(BindingStrength.R)),
    createEntry(HD_CP, VSValidationCode.MultipleCodesFoundInValueSet("A5",vsCodePattern, List(Code("PAT", "pattern", CodeUsage.R, "SYS", Some("[A-Z][0-9]")),Code("A5", "duplicate", CodeUsage.E, "SYS"))), Some(BindingStrength.R))
  ))

  val complexCodePatternMatch: MatchResult[List[Entry]] = {
    check(CWE_2_7_CP, ValueSetBinding(".", Some(BindingStrength.R), List("vsCodePattern"), List(BindingLocation("1[*]", Some("3[*]"))))) must containTheSameElementsAs(List(
      createEntry(CWE_2_7_CP, VSValidationCode.RVS(Code("PAT", "pattern", CodeUsage.R, "SYS", Some("[A-Z][0-9]")), "A1", vsCodePattern), Some(BindingStrength.R))
    ))
  }

  val complexCodePatternMatchValueNotRegex: MatchResult[List[Entry]] = check(CWE_2_7_CP, ValueSetBinding(".", Some(BindingStrength.R), List("vsCodePattern"), List(BindingLocation("10[*]", Some("12[*]"))))) must containTheSameElementsAs(List(
    createEntry(CWE_2_7_CP, VSValidationCode.CodeNotFound("PAT", Some("SYS"), List(vsCodePattern)), Some(BindingStrength.R))
  ))

  val complexCodePatternMatchCodeAndPattern: MatchResult[List[Entry]] = check(CWE_2_7_CP, ValueSetBinding(".", Some(BindingStrength.R), List("vsCodePattern"), List(BindingLocation("4[*]", Some("6[*]")))))  must containTheSameElementsAs(List(
    createEntry(CWE_2_7_CP, VSValidationCode.RVS(Code("PAT", "pattern", CodeUsage.R, "SYS", Some("[A-Z][0-9]")), "A5", vsCodePattern), Some(BindingStrength.R)),
    createEntry(CWE_2_7_CP, VSValidationCode.EVS(Code("A5", "duplicate", CodeUsage.E, "SYS"), "A5", vsCodePattern), Some(BindingStrength.R)),
    createEntry(CWE_2_7_CP, VSValidationCode.MultipleCodeAndCodeSystemFound("A5", "SYS", vsCodePattern, List(Code("PAT", "pattern", CodeUsage.R, "SYS", Some("[A-Z][0-9]")),Code("A5", "duplicate", CodeUsage.E, "SYS"))), Some(BindingStrength.R))
  ))

  val skipTargets : Seq[MatchResult[List[Entry]]] = {
    Seq(Usage.X, Usage.W) map(u => {
      val CWE_2_7_X = CWE_2_7.copy(req = CWE_2_7.req.copy(usage = u))
      check(CWE_2_7_X, ValueSetBinding(".", Some(BindingStrength.R), List("vs1"), List(BindingLocation("1[*]", Some("3[*]")))))  must containTheSameElementsAs(Nil)
    })
  }

  val skipContext: Seq[MatchResult[List[Entry]]] = {
    Seq(Usage.X, Usage.W) map(u => {
      val CWE_2_7_X = CWE_2_7.copy(req = CWE_2_7.req.copy(usage = u))
      val complexComponent = makeComponent(CWE_2_7_X, "CWE", Map(1 -> "ST", 3 -> "ST", 4 -> "ST", 6 -> "ST", 10 -> "ST", 12 -> "ST"))
      val valueSetSpecification: ValueSetSpecification = new DefaultValueSetSpecification(
        Map("CWE"-> List(ValueSetBinding(".", Some(BindingStrength.R), List("vs1"), List(BindingLocation("1[*]", Some("3[*]")))))),
        Map(),
        Map(),
        Map(),
        Map(),
        Map()
      )

      checkMsgElement(complexComponent, Some(valueSetSpecification)) must containTheSameElementsAs(Nil)
    })
  }

  val skipContextChildren: Seq[MatchResult[List[Entry]]] = {
    Seq(Usage.X, Usage.W) map(u => {
      val CWE_2_7_X = CWE_2_7.copy(req = CWE_2_7.req.copy(usage = u))
      val complexComponent = makeComponent(CWE_2_7_X, "CWE", Map(1 -> "ST", 3 -> "ST", 4 -> "ST", 6 -> "ST", 10 -> "ST", 12 -> "ST"))
      val valueSetSpecification: ValueSetSpecification = new DefaultValueSetSpecification(
        Map("ST"-> List(ValueSetBinding(".", Some(BindingStrength.R), List("vs1"), List(BindingLocation(".", None))))),
        Map(),
        Map(),
        Map(),
        Map(),
        Map()
      )

      checkMsgElement(complexComponent, Some(valueSetSpecification)) must containTheSameElementsAs(Nil)
    })
  }


  val emptyVs: Seq[MatchResult[List[Entry]]] = {
    // Simple
    val sBindings = ValueSetBinding("1[*]", Some(BindingStrength.R), List("empty"), List(BindingLocation(".", None)))::Nil

    // Complex
    val cBindings = ValueSetBinding(".", Some(BindingStrength.R), List("empty"), List(BindingLocation("1[1]", Some("3[1]")))) :: ValueSetBinding(".", Some(BindingStrength.R), List("vs1", "empty"), List(BindingLocation("1[1]", Some("3[1]")))) :: Nil

    val parts: Seq[(Element, ValueSetBinding)] = sBindings.map(b => (HD_1, b)) ::: cBindings.map(b => (CWE_2_7, b))
    parts.map(c => check(c._1, c._2) must contain(createEntry(c._1, VSValidationCode.EmptyVS(empty), Some(BindingStrength.R))))
  }

  val PinsideClosed: Seq[MatchResult[List[Entry]]] = {
    // Simple
    val sBindings = ValueSetBinding("1[*]", Some(BindingStrength.R), List("vs1_closed"), List(BindingLocation(".", None)))::Nil

    // Complex
    val cBindings = ValueSetBinding(".", Some(BindingStrength.R), List("vs1_closed"), List(BindingLocation("1[1]", Some("3[1]")))) :: ValueSetBinding(".", Some(BindingStrength.R), List("vs2_2", "vs1_closed"), List(BindingLocation("1[1]", Some("3[1]")))) :: Nil

    val parts: Seq[(Element, ValueSetBinding)] = sBindings.map(b => (HD_1, b)) ::: cBindings.map(b => (CWE_2_7, b))
    parts.map(c => check(c._1, c._2) must containTheSameElementsAs(List(
      createEntry(c._1, VSValidationCode.UsageAndExtensibilityNotCompatible(Code("A", "des", CodeUsage.P, "SYS"), vs1_closed), Some(BindingStrength.R)),
      createEntry(c._1, VSValidationCode.PVS(Code("A", "des", CodeUsage.P, "SYS"), "A", vs1_closed), Some(BindingStrength.R))
    )))
  }

  val PVSDynamic: Seq[MatchResult[List[Entry]]] = {
    // Simple
    val sBindings = ValueSetBinding("1[*]", Some(BindingStrength.R), List("vs1_dynamic"), List(BindingLocation(".", None)))::Nil

    // Complex
    val cBindings = ValueSetBinding(".", Some(BindingStrength.R), List("vs1_dynamic"), List(BindingLocation("1[1]", Some("3[1]")))) :: ValueSetBinding(".", Some(BindingStrength.R), List("vs2_2", "vs1_dynamic"), List(BindingLocation("1[1]", Some("3[1]")))) :: Nil

    val parts: Seq[(Element, ValueSetBinding)] = sBindings.map(b => (HD_1, b)) ::: cBindings.map(b => (CWE_2_7, b))
    parts.map(c => check(c._1, c._2) must containTheSameElementsAs(List(
      createEntry(c._1, VSValidationCode.DynamicValueSet(vs1_dynamic), Some(BindingStrength.R)),
      createEntry(c._1, VSValidationCode.PVS(Code("A", "des", CodeUsage.P, "SYS"), "A", vs1_dynamic), Some(BindingStrength.R))
    )))
  }

  val NotFoundDynamic: Seq[MatchResult[List[Entry]]] = {
    // Simple
    val sBindings = ValueSetBinding("2[*]", Some(BindingStrength.R), List("vs1_dynamic"), List(BindingLocation(".", None)))::Nil

    // Complex
    val cBindings = ValueSetBinding(".", Some(BindingStrength.R), List("vs1_dynamic"), List(BindingLocation("4[1]", Some("6[1]")))) :: ValueSetBinding(".", Some(BindingStrength.R), List("vs2_2", "vs1_dynamic"), List(BindingLocation("4[1]", Some("6[1]")))) :: Nil

    val parts: Seq[(Element, ValueSetBinding)] = sBindings.map(b => (HD_1, b)) ::: cBindings.map(b => (CWE_2_7, b))
    parts.map(c => check(c._1, c._2) must containTheSameElementsAs(List(
      createEntry(c._1, VSValidationCode.DynamicValueSet(vs1_dynamic), Some(BindingStrength.R)),
      createEntry(c._1, VSValidationCode.CodeNotFound("B", if(c._2.bindingLocations.head.codeSystemLocation.isDefined) Some("SYS") else None, if(c._2.bindings.size === 1) List(vs1_dynamic) else List(vs2_2, vs1_dynamic)), Some(BindingStrength.R))
    )))
  }

  val excludedVs: Seq[MatchResult[List[Entry]]] = {
    // Simple
    val sBindingsA = ValueSetBinding("1[*]", Some(BindingStrength.R), List("excluded"), List(BindingLocation(".", None)))::Nil

    // Complex
    val cBindingsB = ValueSetBinding(".", Some(BindingStrength.R), List("excluded"), List(BindingLocation("1[1]", Some("3[1]")))) :: ValueSetBinding(".", Some(BindingStrength.R), List("vs1", "excluded"), List(BindingLocation("1[1]", Some("3[1]")))) :: Nil

    val parts: Seq[(Element, ValueSetBinding)] = sBindingsA.map(b => (HD_1, b)) ::: cBindingsB.map(b => (CWE_2_7, b))
    parts.map(c => check(c._1, c._2) must contain(createEntry(c._1, VSValidationCode.ExcludedVS("excluded"), Some(BindingStrength.R))))
  }

  val vsNotFound: Seq[MatchResult[List[Entry]]] = {
    // Simple
    val sBindingsA = ValueSetBinding("1[*]", Some(BindingStrength.R), List("not_found"), List(BindingLocation(".", None)))::Nil

    // Complex
    val cBindingsB = ValueSetBinding(".", Some(BindingStrength.R), List("not_found"), List(BindingLocation("1[1]", Some("3[1]")))) :: ValueSetBinding(".", Some(BindingStrength.R), List("vs1", "not_found"), List(BindingLocation("1[1]", Some("3[1]")))) :: Nil

    val parts: Seq[(Element, ValueSetBinding)] = sBindingsA.map(b => (HD_1, b)) ::: cBindingsB.map(b => (CWE_2_7, b))
    parts.map(c => check(c._1, c._2) must contain(createEntry(c._1, VSValidationCode.VSNotFound("not_found"), Some(BindingStrength.R))))
  }

  val ubs: Seq[MatchResult[List[Entry]]] = {
    // Simple
    val sBindingsA = ValueSetBinding("1[*]", Some(BindingStrength.U), List("vs1_1"), List(BindingLocation(".", None)))::Nil

    // Complex
    val cBindingsB = ValueSetBinding(".", Some(BindingStrength.U), List("vs1_1"), List(BindingLocation("1[1]", Some("3[1]")))) :: ValueSetBinding(".", Some(BindingStrength.U), List("vs2", "vs4"), List(BindingLocation("1[1]", Some("3[1]")))) :: Nil

    val parts: Seq[(Element, ValueSetBinding)] = sBindingsA.map(b => (HD_1, b)) ::: cBindingsB.map(b => (CWE_2_7, b))
    parts.map(c => check(c._1, c._2) must containTheSameElementsAs(List(createEntry(c._1, VSValidationCode.UBS(c._2), Some(BindingStrength.R)))))
  }

  val multiVsOnSimple: MatchResult[List[Entry]] = {
    // Simple
    val binding = ValueSetBinding("1[*]", Some(BindingStrength.R), List("vs1_1", "vsx"), List(BindingLocation(".", None)))

    check(HD_1, binding) must containTheSameElementsAs(List(
      createEntry(HD_1, VSValidationCode.MultipleVSForPrimitive(binding), Some(BindingStrength.R))
    ))
  }

  val unresolvableBL: MatchResult[List[Entry]] = {
    val binding = ValueSetBinding("1[*]", Some(BindingStrength.R), List("vs1_1"), List(BindingLocation("1[1]", None)))

    check(HD_1, binding) must containTheSameElementsAs(List(
      createEntry(HD_1, VSValidationCode.InvalidCodeBindingLocation("1[1]", multiple = false), Some(BindingStrength.R))
    ))
  }

  val unresolvableCsBL: MatchResult[List[Entry]] = {
    val binding = ValueSetBinding("1[*]", Some(BindingStrength.R), List("vs1_1"), List(BindingLocation(".", Some("1[1]"))))

    check(HD_1, binding) must containTheSameElementsAs(List(
      createEntry(HD_1, VSValidationCode.InvalidCodeSystemBindingLocation("1[1]", multiple = false), Some(BindingStrength.R))
    ))
  }


  val checkSimpleBindingR: MatchResult[List[Entry]] = {
    check(HD_1, ValueSetBinding("1[*]", Some(BindingStrength.R), List("vs1"), List(BindingLocation(".", None)))) must containTheSameElementsAs(List(
      createEntry(HD_1, VSValidationCode.RVS(Code("A", "des", CodeUsage.R, "SYS"), "A", vs1), Some(BindingStrength.R))
    ))
  }

  val checkSimpleBindingNoUsage: MatchResult[List[Entry]] = {
    check(HD_1, ValueSetBinding("1[*]", Some(BindingStrength.R), List("vs1_no_usage"), List(BindingLocation(".", None)))) must containTheSameElementsAs(List(
      createEntry(HD_1, VSValidationCode.NoUsage(Code("A", "des", null, "SYS"), "A", vs1_no_usage), Some(BindingStrength.R))
    ))
  }

  val checkSimpleBindingP: MatchResult[List[Entry]] = {
    check(HD_1, ValueSetBinding("2[*]", Some(BindingStrength.R), List("vs1"), List(BindingLocation(".", None)))) must containTheSameElementsAs(List(
      createEntry(HD_1, VSValidationCode.PVS(Code("B", "des", CodeUsage.P, "SYS"), "B", vs1), Some(BindingStrength.R))
    ))
  }

  val checkSimpleBindingE: MatchResult[List[Entry]] = {
    check(HD_1, ValueSetBinding("3[*]", Some(BindingStrength.R), List("vs1"), List(BindingLocation(".", None)))) must containTheSameElementsAs(List(
      createEntry(HD_1, VSValidationCode.EVS(Code("C", "des", CodeUsage.P, "SYS"), "C", vs1), Some(BindingStrength.R))
    ))
  }

  val codeNotFound: MatchResult[List[Entry]] = {
    check(HD_2, ValueSetBinding("1[*]", Some(BindingStrength.R), List("vs1"), List(BindingLocation(".", None)))) must containTheSameElementsAs(List(
      createEntry(HD_2, VSValidationCode.CodeNotFound("D", None, List(vs1)), Some(BindingStrength.R))
    ))
  }

  val simpleSingleVsDuplicate: MatchResult[List[Entry]] = {
    check(HD_2, ValueSetBinding("1[*]", Some(BindingStrength.R), List("vs2"), List(BindingLocation(".", None)))) must containTheSameElementsAs(List(
      createEntry(HD_2, VSValidationCode.RVS(Code("D", "des", CodeUsage.P, "SYS"), "D", vs2), Some(BindingStrength.R)),
      createEntry(HD_2, VSValidationCode.PVS(Code("D", "des", CodeUsage.P, "SYS"), "D", vs2), Some(BindingStrength.R)),
      createEntry(HD_2, VSValidationCode.MultipleCodesFoundInValueSet("D",vs2, List(Code("D", "des", CodeUsage.R, "SYS"),Code("D", "des", CodeUsage.P, "SYS"))), Some(BindingStrength.R))
    ))
  }

  val checkComplexBindingR: MatchResult[List[Entry]] = {
    check(CWE_2_7, ValueSetBinding(".", Some(BindingStrength.R), List("vs1"), List(BindingLocation("1[1]", Some("3[1]"))))) must containTheSameElementsAs(List(
      createEntry(CWE_2_7, VSValidationCode.RVS(Code("A", "des", CodeUsage.R, "SYS"), "A", vs1), Some(BindingStrength.R))
    ))
  }

  val checkComplexBindingNoUsage: MatchResult[List[Entry]] = {
    check(CWE_2_7, ValueSetBinding(".", Some(BindingStrength.R), List("vs1_no_usage"), List(BindingLocation("1[1]", Some("3[1]"))))) must containTheSameElementsAs(List(
      createEntry(CWE_2_7, VSValidationCode.NoUsage(Code("A", "des", null, "SYS"), "A", vs1_no_usage), Some(BindingStrength.R))
    ))
  }

  val checkComplexBindingP: MatchResult[List[Entry]] = {
    check(CWE_2_7, ValueSetBinding(".", Some(BindingStrength.R), List("vs1"), List(BindingLocation("4[1]", Some("6[1]"))))) must containTheSameElementsAs(List(
      createEntry(CWE_2_7, VSValidationCode.PVS(Code("B", "des", CodeUsage.P, "SYS"), "B", vs1), Some(BindingStrength.R))
    ))
  }

  val checkComplexBindingE: MatchResult[List[Entry]] = {
    check(CWE_2_7, ValueSetBinding(".", Some(BindingStrength.R), List("vs1"), List(BindingLocation("10[1]", Some("12[1]"))))) must containTheSameElementsAs(List(
      createEntry(CWE_2_7, VSValidationCode.EVS(Code("C", "des", CodeUsage.E, "SYS"), "C", vs1), Some(BindingStrength.R))
    ))
  }

  val checkComplexBindingNotFound: MatchResult[List[Entry]] = {
    check(CWE_2_7, ValueSetBinding(".", Some(BindingStrength.R), List("vs2"), List(BindingLocation("1[1]", Some("3[1]"))))) must containTheSameElementsAs(List(
      createEntry(CWE_2_7, VSValidationCode.CodeNotFound("A", Some("SYS"), List(vs2)), Some(BindingStrength.R))
    ))
  }

  val checkComplexBindingInvalidCodeSystem: MatchResult[List[Entry]] = {
    check(CWE_2_7, ValueSetBinding(".", Some(BindingStrength.R), List("vs3"), List(BindingLocation("1[1]", Some("3[1]"))))) must containTheSameElementsAs(List(
      createEntry(CWE_2_7, VSValidationCode.CodeNotFound("A", Some("SYS"), List(vs3)), Some(BindingStrength.R)),
      createEntry(CWE_2_7, VSValidationCode.SimpleCodeFoundInvalidCodeSystem("A", "SYS1", "SYS", vs3), Some(BindingStrength.R))
    ))
  }

  val checkComplexBindingCodeSystemNp: MatchResult[List[Entry]] = {
    check(CWE_2_7_CSNP, ValueSetBinding(".", Some(BindingStrength.R), List("vs3"), List(BindingLocation("1[1]", Some("3[1]"))))) must containTheSameElementsAs(List(
//      createEntry(CWE_2_7_CSNP, VSValidationCode.CodeNotFound("A", None, List(vs3)), Some(BindingStrength.R)),
      createEntry(CWE_2_7_CSNP, VSValidationCode.SimpleCodeFoundCodeSystemNotPopulated("A", "SYS1", "3[1]", vs3), Some(BindingStrength.R))
    ))
  }

  val checkComplexBindingDuplicateInVs: MatchResult[List[Entry]] = {
    check(CWE_2_7_a, ValueSetBinding(".", Some(BindingStrength.R), List("vs2"), List(BindingLocation("1[1]", Some("3[1]"))))) must containTheSameElementsAs(List(
      createEntry(CWE_2_7_a, VSValidationCode.RVS(Code("D", "des", CodeUsage.P, "SYS"), "D", vs2), Some(BindingStrength.R)),
      createEntry(CWE_2_7_a, VSValidationCode.PVS(Code("D", "des", CodeUsage.P, "SYS"), "D", vs2), Some(BindingStrength.R)),
      createEntry(CWE_2_7_a, VSValidationCode.MultipleCodeAndCodeSystemFound("D", "SYS", vs2, List(Code("D", "des", CodeUsage.R, "SYS"),Code("D", "des", CodeUsage.P, "SYS"))), Some(BindingStrength.R))
    ))
  }

  val checkComplexBindingMultiVsNoUsage: MatchResult[List[Entry]] = {
    check(CWE_2_7, ValueSetBinding(".", Some(BindingStrength.R), List("vs1_no_usage", "vs1_1"), List(BindingLocation("1[1]", Some("3[1]"))))) must containTheSameElementsAs(List(
      createEntry(CWE_2_7, VSValidationCode.NoUsage(Code("A", "des", null, "SYS"), "A", vs1_no_usage), Some(BindingStrength.R))
    ))
  }

  val checkComplexBindingMultiVsR: MatchResult[List[Entry]] = {
    check(CWE_2_7, ValueSetBinding(".", Some(BindingStrength.R), List("vs1", "vs1_1"), List(BindingLocation("1[1]", Some("3[1]"))))) must containTheSameElementsAs(List(
      createEntry(CWE_2_7, VSValidationCode.RVS(Code("A", "des", CodeUsage.R, "SYS"), "A", vs1), Some(BindingStrength.R))
    ))
  }

  val checkComplexBindingMultiVsE: MatchResult[List[Entry]] = {
    check(CWE_2_7, ValueSetBinding(".", Some(BindingStrength.R), List("vs1", "vs1_1"), List(BindingLocation("4[1]", Some("6[1]"))))) must containTheSameElementsAs(List(
      createEntry(CWE_2_7, VSValidationCode.PVS(Code("B", "des", CodeUsage.P, "SYS"), "B", vs1), Some(BindingStrength.R))
    ))
  }

  val checkComplexBindingMultiVsP: MatchResult[List[Entry]] = {
    check(CWE_2_7, ValueSetBinding(".", Some(BindingStrength.R), List("vs1", "vs1_1"), List(BindingLocation("10[1]", Some("12[1]"))))) must containTheSameElementsAs(List(
      createEntry(CWE_2_7, VSValidationCode.EVS(Code("C", "des", CodeUsage.E, "SYS"), "C", vs1), Some(BindingStrength.R))
    ))
  }

  val checkComplexBindingMultiVsNotFound: MatchResult[List[Entry]] = {
    check(CWE_2_7, ValueSetBinding(".", Some(BindingStrength.R), List("vs2_2", "vs1_1"), List(BindingLocation("1[1]", Some("3[1]"))))) must containTheSameElementsAs(List(
      createEntry(CWE_2_7, VSValidationCode.CodeNotFound("A", Some("SYS"), List(vs2_2, vs1_1)), Some(BindingStrength.R))
    ))
  }

  val checkComplexBindingMultiVsInvalidCS: MatchResult[List[Entry]] = {
    check(CWE_2_7, ValueSetBinding(".", Some(BindingStrength.R), List("vs1_cs", "vs1_cs_1"), List(BindingLocation("1[1]", Some("3[1]"))))) must containTheSameElementsAs(List(
      createEntry(CWE_2_7, VSValidationCode.CodeNotFound("A", Some("SYS"), List(vs1_cs, vs1_cs_1)), Some(BindingStrength.R)),
      createEntry(CWE_2_7, VSValidationCode.SimpleCodeFoundInvalidCodeSystem("A", "SYS1", "SYS", vs1_cs), Some(BindingStrength.R)),
      createEntry(CWE_2_7, VSValidationCode.SimpleCodeFoundInvalidCodeSystem("A", "SYS2", "SYS", vs1_cs_1), Some(BindingStrength.R))
    ))
  }

  val checkComplexBindingMultiVsCSNP: MatchResult[List[Entry]] = {
    check(CWE_2_7_CSNP, ValueSetBinding(".", Some(BindingStrength.R), List("vs1_cs", "vs1_cs_1"), List(BindingLocation("1[1]", Some("3[1]"))))) must containTheSameElementsAs(List(
//      createEntry(CWE_2_7_CSNP, VSValidationCode.CodeNotFound("A", None, List(vs1_cs, vs1_cs_1)), Some(BindingStrength.R)),
      createEntry(CWE_2_7_CSNP, VSValidationCode.SimpleCodeFoundCodeSystemNotPopulated("A", "SYS1", "3[1]", vs1_cs), Some(BindingStrength.R)),
      createEntry(CWE_2_7_CSNP, VSValidationCode.SimpleCodeFoundCodeSystemNotPopulated("A", "SYS2", "3[1]", vs1_cs_1), Some(BindingStrength.R))
    ))
  }



  val checkComplexBindingMultiVsValidAndInvalidCS: MatchResult[List[Entry]] = {
    check(CWE_2_7, ValueSetBinding(".", Some(BindingStrength.R), List("vs1", "vs1_cs_1"), List(BindingLocation("1[1]", Some("3[1]"))))) must containTheSameElementsAs(List(
      createEntry(CWE_2_7, VSValidationCode.RVS(Code("A", "des", CodeUsage.R, "SYS"), "A", vs1), Some(BindingStrength.R))
    ))
  }

  val checkComplexBindingMultiDuplicate: MatchResult[List[Entry]] = {
    check(CWE_2_7, ValueSetBinding(".", Some(BindingStrength.R), List("vs3_1", "vs1"), List(BindingLocation("1[1]", Some("3[1]"))))) must containTheSameElementsAs(List(
      createEntry(CWE_2_7, VSValidationCode.RVS(Code("A", "des", CodeUsage.R, "SYS"), "A", vs1), Some(BindingStrength.R)),
      createEntry(CWE_2_7, VSValidationCode.PVS(Code("A", "des", CodeUsage.P, "SYS"), "A", vs3_1), Some(BindingStrength.R)),
      createEntry(CWE_2_7, VSValidationCode.FoundInMultipleValueSets("A", Some("SYS"), List(vs3_1,vs1)), Some(BindingStrength.R))
    ))
  }

  val checkComplexBindingMultiBLRP: MatchResult[List[Entry]] = {
    check(CWE_2_7, ValueSetBinding(".", Some(BindingStrength.R), List("vs1"), List(BindingLocation("1[1]", Some("3[1]")), BindingLocation("4[1]", Some("6[1]"))))) must containTheSameElementsAs(List(
      createEntry(CWE_2_7, VSValidationCode.CodedElementXOR(List(
        CodeHolder(S(1, 1, Text("A")), Some(S(3, 1, Text("SYS"))), BindingLocation("1[1]", Some("3[1]"))),
        CodeHolder(S(4, 1, Text("B")), Some(S(6, 1, Text("SYS"))), BindingLocation("4[1]", Some("6[1]")))
      ), vs1), Some(BindingStrength.R)),
      createEntry(CWE_2_7, VSValidationCode.RVS(Code("A", "des", CodeUsage.R, "SYS"), "A", vs1), Some(BindingStrength.R)),
      createEntry(CWE_2_7, VSValidationCode.PVS(Code("B", "des", CodeUsage.P, "SYS"), "B", vs1), Some(BindingStrength.R))
    ))
  }

  val checkComplexBindingMultiBLRNotFound: MatchResult[List[Entry]] = {
    check(CWE_2_7_a, ValueSetBinding(".", Some(BindingStrength.R), List("vs1"), List(BindingLocation("10[1]", Some("12[1]")), BindingLocation("1[1]", Some("3[1]"))))) must containTheSameElementsAs(List(
      createEntry(CWE_2_7_a, VSValidationCode.EVS(Code("C", "des", CodeUsage.E, "SYS"), "C", vs1), Some(BindingStrength.R))
    ))
  }

  val checkComplexBindingMultiBLBothNotFound: MatchResult[List[Entry]] = {
    check(CWE_2_7_a, ValueSetBinding(".", Some(BindingStrength.R), List("vs2_2"), List(BindingLocation("10[1]", Some("12[1]")), BindingLocation("1[1]", Some("3[1]"))))) must containTheSameElementsAs(List(
      createEntry(CWE_2_7_a, VSValidationCode.CodeNotFound("C", Some("SYS"), List(vs2_2)), Some(BindingStrength.R)),
      createEntry(CWE_2_7_a, VSValidationCode.CodeNotFound("D", Some("SYS"), List(vs2_2)), Some(BindingStrength.R))
    ))
  }

  val checkComplexBindingMultiBLBothInvalidCs: MatchResult[List[Entry]] = {
    check(CWE_2_7, ValueSetBinding(".", Some(BindingStrength.R), List("vs1_cs"), List(BindingLocation("1[1]", Some("3[1]")), BindingLocation("4[1]", Some("6[1]"))))) must containTheSameElementsAs(List(
      createEntry(CWE_2_7, VSValidationCode.CodeNotFound("A", Some("SYS"), List(vs1_cs)), Some(BindingStrength.R)),
      createEntry(CWE_2_7, VSValidationCode.CodeNotFound("B", Some("SYS"), List(vs1_cs)), Some(BindingStrength.R)),
      createEntry(CWE_2_7, VSValidationCode.SimpleCodeFoundInvalidCodeSystem("A", "SYS1", "SYS", vs1_cs), Some(BindingStrength.R)),
      createEntry(CWE_2_7, VSValidationCode.SimpleCodeFoundInvalidCodeSystem("B", "SYS1", "SYS", vs1_cs), Some(BindingStrength.R))
    ))
  }

  val checkComplexBindingMultiBLBothCSNP: MatchResult[List[Entry]] = {
    check(CWE_2_7_CSNP, ValueSetBinding(".", Some(BindingStrength.R), List("vs1_cs"), List(BindingLocation("1[1]", Some("3[1]")), BindingLocation("4[1]", Some("6[1]"))))) must containTheSameElementsAs(List(
//      createEntry(CWE_2_7_CSNP, VSValidationCode.CodeNotFound("A", None, List(vs1_cs)), Some(BindingStrength.R)),
//      createEntry(CWE_2_7_CSNP, VSValidationCode.CodeNotFound("B", None, List(vs1_cs)), Some(BindingStrength.R)),
      createEntry(CWE_2_7_CSNP, VSValidationCode.SimpleCodeFoundCodeSystemNotPopulated("A", "SYS1", "3[1]", vs1_cs), Some(BindingStrength.R)),
      createEntry(CWE_2_7_CSNP, VSValidationCode.SimpleCodeFoundCodeSystemNotPopulated("B", "SYS1", "6[1]", vs1_cs), Some(BindingStrength.R))
    ))
  }



  val checkComplexBindingMultiBLMultiVsRP_1: MatchResult[List[Entry]] = {
    check(CWE_2_7, ValueSetBinding(".", Some(BindingStrength.R), List("vs1", "vs2_2"), List(BindingLocation("1[1]", Some("3[1]")), BindingLocation("4[1]", Some("6[1]"))))) must containTheSameElementsAs(List(
      createEntry(CWE_2_7, VSValidationCode.CodedElementXOR(List(
        CodeHolder(S(1, 1, Text("A")), Some(S(3, 1, Text("SYS"))), BindingLocation("1[1]", Some("3[1]"))),
        CodeHolder(S(4, 1, Text("B")), Some(S(6, 1, Text("SYS"))), BindingLocation("4[1]", Some("6[1]")))
      ), vs1), Some(BindingStrength.R)),
      createEntry(CWE_2_7, VSValidationCode.RVS(Code("A", "des", CodeUsage.R, "SYS"), "A", vs1), Some(BindingStrength.R)),
      createEntry(CWE_2_7, VSValidationCode.PVS(Code("B", "des", CodeUsage.P, "SYS"), "B", vs1), Some(BindingStrength.R))
    ))
  }

  val checkComplexBindingMultiBLMultiVsRP_2: MatchResult[List[Entry]] = {
    check(CWE_2_7_a, ValueSetBinding(".", Some(BindingStrength.R), List("vs1", "vs1_1"), List(BindingLocation("1[1]", Some("3[1]")), BindingLocation("4[1]", Some("6[1]"))))) must containTheSameElementsAs(List(
      createEntry(CWE_2_7, VSValidationCode.RVS(Code("D", "des", CodeUsage.R, "SYS"), "D", vs1_1), Some(BindingStrength.R)),
      createEntry(CWE_2_7, VSValidationCode.PVS(Code("B", "des", CodeUsage.P, "SYS"), "B", vs1), Some(BindingStrength.R))
    ))
  }

  val checkComplexBindingMultiBLMultiVsRNotFound: MatchResult[List[Entry]] = {
    check(CWE_2_7_a, ValueSetBinding(".", Some(BindingStrength.R), List("vs1", "vs2_2"), List(BindingLocation("10[1]", Some("12[1]")), BindingLocation("1[1]", Some("3[1]"))))) must containTheSameElementsAs(List(
      createEntry(CWE_2_7_a, VSValidationCode.EVS(Code("C", "des", CodeUsage.E, "SYS"), "C", vs1), Some(BindingStrength.R))
    ))
  }

  val checkComplexBindingMultiBLMultiVsBothNotFound: MatchResult[List[Entry]] = {
    check(CWE_2_7_a, ValueSetBinding(".", Some(BindingStrength.R), List("vs2_2", "vs2_2_2"), List(BindingLocation("10[1]", Some("12[1]")), BindingLocation("1[1]", Some("3[1]"))))) must containTheSameElementsAs(List(
      createEntry(CWE_2_7_a, VSValidationCode.CodeNotFound("C", Some("SYS"), List(vs2_2, vs2_2_2)), Some(BindingStrength.R)),
      createEntry(CWE_2_7_a, VSValidationCode.CodeNotFound("D", Some("SYS"), List(vs2_2, vs2_2_2)), Some(BindingStrength.R))
    ))
  }

  val checkComplexBindingMultiBLMultiVsOneInvalidCsOtherPass: MatchResult[List[Entry]] = {
    check(CWE_2_7_b, ValueSetBinding(".", Some(BindingStrength.R), List("vs4", "vs4_1"), List(BindingLocation("1[1]", Some("3[1]")), BindingLocation("4[1]", Some("6[1]"))))) must containTheSameElementsAs(List(
      createEntry(CWE_2_7_b, VSValidationCode.RVS(Code("M", "des", CodeUsage.R, "SYS"), "M", vs4), Some(BindingStrength.R)),
      createEntry(CWE_2_7_b, VSValidationCode.RVS(Code("N", "des", CodeUsage.R, "SYS"), "N", vs4_1), Some(BindingStrength.R))
    ))
  }

  val checkLegacy396EnabledHL7nnnnR: MatchResult[List[Entry]] = {
    implicit val featureFlags: FeatureFlags = FeatureFlags(legacy0396 = true)
    check(HD_0396, ValueSetBinding(".", Some(BindingStrength.R), List("0396_R"), List(BindingLocation("3[1]", None)))) must containTheSameElementsAs(List(
      createEntry(HD_0396, VSValidationCode.RVS(Code("HL7nnnn", "des", CodeUsage.R, "SYS"), "HL70125", vs396_R), Some(BindingStrength.R)),
    ))
  }

  val checkLegacy396EnabledHL7nnnnP: MatchResult[List[Entry]] = {
    implicit val featureFlags: FeatureFlags = FeatureFlags(legacy0396 = true)
    check(HD_0396, ValueSetBinding(".", Some(BindingStrength.R), List("0396_P"), List(BindingLocation("3[1]", None)))) must containTheSameElementsAs(List(
      createEntry(HD_0396, VSValidationCode.PVS(Code("HL7nnnn", "des", CodeUsage.P, "SYS"), "HL70125", vs396_P), Some(BindingStrength.R)),
    ))
  }

  val checkLegacy396EnabledHL7nnnnNF: MatchResult[List[Entry]] = {
    implicit val featureFlags: FeatureFlags = FeatureFlags(legacy0396 = true)
    check(HD_0396, ValueSetBinding(".", Some(BindingStrength.R), List("0396_NF"), List(BindingLocation("3[1]", None)))) must containTheSameElementsAs(List(
      createEntry(HD_0396, VSValidationCode.CodeNotFound("HL70125", None, List(vs396_NF)), Some(BindingStrength.R)),
    ))
  }

  val checkLegacy396EnabledHL7nnnnBoth: MatchResult[List[Entry]] = {
    implicit val featureFlags: FeatureFlags = FeatureFlags(legacy0396 = true)
    check(HD_0396, ValueSetBinding(".", Some(BindingStrength.R), List("0396_BOTH"), List(BindingLocation("3[1]", None)))) must containTheSameElementsAs(List(
      createEntry(HD_0396, VSValidationCode.RVS(Code("HL7nnnn", "des", CodeUsage.R, "SYS"), "HL70125", vs396_BOTH), Some(BindingStrength.R)),
      createEntry(HD_0396, VSValidationCode.PVS(Code("HL70125", "des", CodeUsage.P, "SYS"), "HL70125", vs396_BOTH), Some(BindingStrength.R)),
      createEntry(HD_0396, VSValidationCode.MultipleCodesFoundInValueSet("HL70125", vs396_BOTH, List(Code("HL7nnnn", "des", CodeUsage.R, "SYS"), Code("HL70125", "des", CodeUsage.P, "SYS"))), Some(BindingStrength.R))
    ))
  }

  val checkLegacy396Enabled99zzzR: MatchResult[List[Entry]] = {
    implicit val featureFlags: FeatureFlags = FeatureFlags(legacy0396 = true)
    check(HD_0396, ValueSetBinding(".", Some(BindingStrength.R), List("0396_R"), List(BindingLocation("4[1]", None)))) must containTheSameElementsAs(List(
      createEntry(HD_0396, VSValidationCode.RVS(Code("99zzz", "des", CodeUsage.R, "SYS"), "99abc", vs396_R), Some(BindingStrength.R)),
    ))
  }

  val checkLegacy396Enabled99zzzP: MatchResult[List[Entry]] = {
    implicit val featureFlags: FeatureFlags = FeatureFlags(legacy0396 = true)
    check(HD_0396, ValueSetBinding(".", Some(BindingStrength.R), List("0396_P"), List(BindingLocation("4[1]", None)))) must containTheSameElementsAs(List(
      createEntry(HD_0396, VSValidationCode.PVS(Code("99zzz", "des", CodeUsage.P, "SYS"), "99abc", vs396_P), Some(BindingStrength.R)),
    ))
  }

  val checkLegacy396Enabled99zzzNF: MatchResult[List[Entry]] = {
    implicit val featureFlags: FeatureFlags = FeatureFlags(legacy0396 = true)
    check(HD_0396, ValueSetBinding(".", Some(BindingStrength.R), List("0396_NF"), List(BindingLocation("4[1]", None)))) must containTheSameElementsAs(List(
      createEntry(HD_0396, VSValidationCode.CodeNotFound("99abc", None, List(vs396_NF)), Some(BindingStrength.R)),
    ))
  }

  val checkLegacy396Enabled99zzzBoth: MatchResult[List[Entry]] = {
    implicit val featureFlags: FeatureFlags = FeatureFlags(legacy0396 = true)
    check(HD_0396, ValueSetBinding(".", Some(BindingStrength.R), List("0396_BOTH"), List(BindingLocation("4[1]", None)))) must containTheSameElementsAs(List(
      createEntry(HD_0396, VSValidationCode.RVS(Code("99zzz", "des", CodeUsage.R, "SYS"), "99abc", vs396_BOTH), Some(BindingStrength.R)),
      createEntry(HD_0396, VSValidationCode.PVS(Code("99abc", "des", CodeUsage.P, "SYS"), "99abc", vs396_BOTH), Some(BindingStrength.R)),
      createEntry(HD_0396, VSValidationCode.MultipleCodesFoundInValueSet("99abc", vs396_BOTH, List(Code("99zzz", "des", CodeUsage.R, "SYS"), Code("99abc", "des", CodeUsage.P, "SYS"))), Some(BindingStrength.R))
    ))
  }


  val checkLegacy396DisabledHL7nnnnR: MatchResult[List[Entry]] = {
    implicit val featureFlags: FeatureFlags = FeatureFlags(legacy0396 = false)
    check(HD_0396, ValueSetBinding(".", Some(BindingStrength.R), List("0396_R"), List(BindingLocation("3[1]", None)))) must containTheSameElementsAs(List(
      createEntry(HD_0396, VSValidationCode.CodeNotFound("HL70125", None, List(vs396_R)), Some(BindingStrength.R)),
    ))
  }

  val checkLegacy396DisabledHL7nnnnP: MatchResult[List[Entry]] = {
    implicit val featureFlags: FeatureFlags = FeatureFlags(legacy0396 = false)
    check(HD_0396, ValueSetBinding(".", Some(BindingStrength.R), List("0396_P"), List(BindingLocation("3[1]", None)))) must containTheSameElementsAs(List(
      createEntry(HD_0396, VSValidationCode.CodeNotFound("HL70125", None, List(vs396_P)), Some(BindingStrength.R)),
    ))
  }

  val checkLegacy396DisabledHL7nnnnNF: MatchResult[List[Entry]] = {
    implicit val featureFlags: FeatureFlags = FeatureFlags(legacy0396 = false)
    check(HD_0396, ValueSetBinding(".", Some(BindingStrength.R), List("0396_NF"), List(BindingLocation("3[1]", None)))) must containTheSameElementsAs(List(
      createEntry(HD_0396, VSValidationCode.CodeNotFound("HL70125", None, List(vs396_NF)), Some(BindingStrength.R)),
    ))
  }

  val checkLegacy396DisabledHL7nnnnBOTH: MatchResult[List[Entry]] = {
    implicit val featureFlags: FeatureFlags = FeatureFlags(legacy0396 = false)
    check(HD_0396, ValueSetBinding(".", Some(BindingStrength.R), List("0396_BOTH"), List(BindingLocation("3[1]", None)))) must containTheSameElementsAs(List(
      createEntry(HD_0396, VSValidationCode.PVS(Code("HL70125", "des", CodeUsage.P, "SYS"), "HL70125", vs396_BOTH), Some(BindingStrength.R)),
    ))
  }


  val checkLegacy396Disabled99zzzR: MatchResult[List[Entry]] = {
    implicit val featureFlags: FeatureFlags = FeatureFlags(legacy0396 = false)
    check(HD_0396, ValueSetBinding(".", Some(BindingStrength.R), List("0396_R"), List(BindingLocation("4[1]", None)))) must containTheSameElementsAs(List(
      createEntry(HD_0396, VSValidationCode.CodeNotFound("99abc", None, List(vs396_R)), Some(BindingStrength.R)),
    ))
  }

  val checkLegacy396Disabled99zzzP: MatchResult[List[Entry]] = {
    implicit val featureFlags: FeatureFlags = FeatureFlags(legacy0396 = false)
    check(HD_0396, ValueSetBinding(".", Some(BindingStrength.R), List("0396_P"), List(BindingLocation("4[1]", None)))) must containTheSameElementsAs(List(
      createEntry(HD_0396, VSValidationCode.CodeNotFound("99abc", None, List(vs396_P)), Some(BindingStrength.R)),
    ))
  }

  val checkLegacy396Disabled99zzzNF: MatchResult[List[Entry]] = {
    implicit val featureFlags: FeatureFlags = FeatureFlags(legacy0396 = false)
    check(HD_0396, ValueSetBinding(".", Some(BindingStrength.R), List("0396_NF"), List(BindingLocation("4[1]", None)))) must containTheSameElementsAs(List(
      createEntry(HD_0396, VSValidationCode.CodeNotFound("99abc", None, List(vs396_NF)), Some(BindingStrength.R)),
    ))
  }

  val checkLegacy396Disabled99zzzBOTH: MatchResult[List[Entry]] = {
    implicit val featureFlags: FeatureFlags = FeatureFlags(legacy0396 = false)
    check(HD_0396, ValueSetBinding(".", Some(BindingStrength.R), List("0396_BOTH"), List(BindingLocation("4[1]", None)))) must containTheSameElementsAs(List(
      createEntry(HD_0396, VSValidationCode.PVS(Code("99abc", "des", CodeUsage.P, "SYS"), "99abc", vs396_BOTH), Some(BindingStrength.R)),
    ))
  }


  val checkLegacy396EnabledHL7nnnnExactMatch: MatchResult[List[Entry]] = {
    implicit val featureFlags: FeatureFlags = FeatureFlags(legacy0396 = true)
    check(HD_0396, ValueSetBinding(".", Some(BindingStrength.R), List("0396_R"), List(BindingLocation("1[1]", None)))) must containTheSameElementsAs(List(
      createEntry(HD_0396, VSValidationCode.CodeNotFound("HL7nnnn", None, List(vs396_R)), Some(BindingStrength.R)),
    ))
  }

  val checkLegacy396Enabled99zzzExactMatch: MatchResult[List[Entry]] = {
    implicit val featureFlags: FeatureFlags = FeatureFlags(legacy0396 = true)
    check(HD_0396, ValueSetBinding(".", Some(BindingStrength.R), List("0396_R"), List(BindingLocation("2[1]", None)))) must containTheSameElementsAs(List(
      createEntry(HD_0396, VSValidationCode.RVS(Code("99zzz", "des", CodeUsage.R, "SYS"), "99zzz", vs396_R), Some(BindingStrength.R)),
    ))
  }

  val checkLegacy396DisabledHL7nnnnExactMatch: MatchResult[List[Entry]] = {
    implicit val featureFlags: FeatureFlags = FeatureFlags(legacy0396 = false)
    check(HD_0396, ValueSetBinding(".", Some(BindingStrength.R), List("0396_R"), List(BindingLocation("1[1]", None)))) must containTheSameElementsAs(List(
      createEntry(HD_0396, VSValidationCode.RVS(Code("HL7nnnn", "des", CodeUsage.R, "SYS"), "HL7nnnn", vs396_R), Some(BindingStrength.R)),
    ))
  }

  val checkLegacy396Disabled99zzzExactMatch: MatchResult[List[Entry]] = {
    implicit val featureFlags: FeatureFlags = FeatureFlags(legacy0396 = true)
    check(HD_0396, ValueSetBinding(".", Some(BindingStrength.R), List("0396_R"), List(BindingLocation("2[1]", None)))) must containTheSameElementsAs(List(
      createEntry(HD_0396, VSValidationCode.RVS(Code("99zzz", "des", CodeUsage.R, "SYS"), "99zzz", vs396_R), Some(BindingStrength.R)),
    ))
  }

  val vsLibHL7nnnnNoPattern: MatchResult[Any] = {
    val lib = new ValueSetLibraryImpl(List(), (ValueSet("0396", None, None, List(
      Code("HL7nnnn", "des", CodeUsage.P, "SYS", None),
    )) :: Nil).map(x => { (x.id, x) }).toMap)

    lib.containsLegacy0396Codes() mustEqual(true)
  }

  val vsLibHL7nnnnPattern: MatchResult[Any] = {
    val lib = new ValueSetLibraryImpl(List(), (ValueSet("0396", None, None, List(
      Code("HL7nnnn", "des", CodeUsage.P, "SYS", Some("HL7[0-9]{4}")),
    )) :: Nil).map(x => {
      (x.id, x)
    }).toMap)

    lib.containsLegacy0396Codes() mustEqual (false)
  }

  val vsLib99zzzNoPattern: MatchResult[Any] = {
    val lib = new ValueSetLibraryImpl(List(), (ValueSet("0396", None, None, List(
      Code("99zzz", "des", CodeUsage.P, "SYS", None),
    )) :: Nil).map(x => {
      (x.id, x)
    }).toMap)

    lib.containsLegacy0396Codes() mustEqual (true)
  }

  val vsLib99zzzPattern: MatchResult[Any] = {
    val lib = new ValueSetLibraryImpl(List(), (ValueSet("0396", None, None, List(
      Code("99zzz", "des", CodeUsage.P, "SYS", Some("99[0-9A-Za-z]{4}")),
    )) :: Nil).map(x => {
      (x.id, x)
    }).toMap)

    lib.containsLegacy0396Codes() mustEqual (false)
  }

  val vsLibNo396: MatchResult[Any] = {
    val lib = new ValueSetLibraryImpl(List(), (ValueSet("0397", None, None, List(
      Code("99zzz", "des", CodeUsage.P, "SYS", None),
    )) :: Nil).map(x => {
      (x.id, x)
    }).toMap)

    lib.containsLegacy0396Codes() mustEqual (false)
  }

  val vsLib396NoCode: MatchResult[Any] = {
    val lib = new ValueSetLibraryImpl(List(), (ValueSet("0396", None, None, List(
      Code("ABC", "des", CodeUsage.P, "SYS", None),
    )) :: Nil).map(x => {
      (x.id, x)
    }).toMap)

    lib.containsLegacy0396Codes() mustEqual (false)
  }


  // ============================================ HELPERS ===========================================

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

  def makeComponent(complex: C, id: String, children: Map[Int, String]): ComplexComponent = {
    ComplexComponent(
      Composite(
        id, "name", "desc", "2", Nil
      ),
      complex.req.copy(position = complex.position), complex.location, complex.children.map(s => makeSimpleComponent(s.asInstanceOf[S], children(s.position))), complex.hasExtra
    )
  }

  def makeSimpleComponent(simple: S, id: String): SimpleComponent = {
    SimpleComponent(Primitive(id, "name", "desc", "2"), simple.req.copy(position = simple.position), simple.location, simple.value)
  }
}