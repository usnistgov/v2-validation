package hl7.v2.validation.structure

import com.typesafe.config.{Config, ConfigFactory}
import hl7.v2.instance.{Complex, Element, Location, Simple, Text, Value}
import hl7.v2.profile.{BindingStrength, Req, Usage}
import hl7.v2.validation.vs.{BindingLocation, Code, CodeUsage, EmptyValueSetSpecification, Extensibility, InternalValueSet, Stability, VSValidationCode, ValueSetBinding, ValueSetLibraryImpl, ValueSetSpecification}
import org.specs2.Specification
import org.specs2.matcher.MatchResult

trait ValueSetValidationDefaultConfigurationSpec2 extends Specification
  with hl7.v2.validation.vs.DefaultValueSetValidator
  with Helpers {
  def is =s2"""
      MultiVSForPrimitive Detection when a value set is Closed/ Dynamic and Binding Strength is Required should be SPEC_ERROR $multi_vs_closed_dynamic_required
      MultiVSForPrimitive Detection when a value set is Open/ Dynamic and Binding Strength is Required should be SPEC_ERROR $multi_vs_open_dynamic_required
      MultiVSForPrimitive Detection when a value set is Closed/ Static and Binding Strength is Required should be SPEC_ERROR $multi_vs_closed_static_required
      MultiVSForPrimitive Detection when a value set is Open/ Static and Binding Strength is Required should be SPEC_ERROR $multi_vs_open_static_required

      MultiVSForPrimitive Detection when a value set is Closed/ Dynamic and Binding Strength is Suggested should be SPEC_ERROR $multi_vs_closed_dynamic_suggested
      MultiVSForPrimitive Detection when a value set is Open/ Dynamic and Binding Strength is Suggested should be SPEC_ERROR $multi_vs_open_dynamic_suggested
      MultiVSForPrimitive Detection when a value set is Closed/ Static and Binding Strength is Suggested should be SPEC_ERROR $multi_vs_closed_static_suggested
      MultiVSForPrimitive Detection when a value set is Open/ Static and Binding Strength is Suggested should be SPEC_ERROR $multi_vs_open_static_suggested

      VSNotFound Detection when  Binding Strength is Required should be SPEC_ERROR $vs_not_found_required
      VSNotFound Detection when  Binding Strength is Suggested should be SPEC_ERROR $vs_not_found_suggested

      EmptyVS Detection when a value set is Closed/ Dynamic and Binding Strength is Required should be ALERT $empty_vs_closed_dynamic_required
      EmptyVS Detection when a value set is Open/ Dynamic and Binding Strength is Required should be ALERT $empty_vs_open_dynamic_required
      EmptyVS Detection when a value set is Closed/ Static and Binding Strength is Required should be ALERT $empty_vs_closed_static_required
      EmptyVS Detection when a value set is Open/ Static and Binding Strength is Required should be ALERT $empty_vs_open_static_required

      EmptyVS Detection when a value set is Closed/ Dynamic and Binding Strength is Suggested should be ALERT $empty_vs_closed_dynamic_suggested
      EmptyVS Detection when a value set is Open/ Dynamic and Binding Strength is Suggested should be ALERT $empty_vs_open_dynamic_suggested
      EmptyVS Detection when a value set is Closed/ Static and Binding Strength is Suggested should be ALERT $empty_vs_closed_static_suggested
      EmptyVS Detection when a value set is Open/ Static and Binding Strength is Suggested should be ALERT $empty_vs_open_static_suggested

      """
  //------- MOCKS
  val valueSetLibrary = new ValueSetLibraryImpl(Nil, Map())
  val vsSpecification: ValueSetSpecification = EmptyValueSetSpecification
  val elm: S = S(1, 1, Text("A"))
  val _default: Config = ConfigFactory.load()

  val ERROR: String = _default.getString("report.classification.error")
  val WARNING: String = _default.getString("report.classification.warning")
  val ALERT: String = _default.getString("report.classification.alert")
  val SPEC_ERROR: String = _default.getString("report.classification.spec-error")
  val AFFIRMATIVE: String = _default.getString("report.classification.affirmative")

  val PCode: Code = Code("A", "B", CodeUsage.P, "C")
  val RCode: Code = Code("A", "B", CodeUsage.R, "C")
  val ECode: Code = Code("A", "B", CodeUsage.E, "C")
  val NUCode: Code = Code("A", "B", null, "C")

  //------ TEST CASES

  //--MultiVSForPrimitive : only for primitive datatypes that have multiple VS
  val multi_vs_closed_dynamic_required: MatchResult[String] = expectationSingleVs(DetectionCodes.MultiVSForPrimitive, valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed)), Some(BindingStrength.R), SPEC_ERROR)
  val multi_vs_open_dynamic_required: MatchResult[String] = expectationSingleVs(DetectionCodes.MultiVSForPrimitive, valueSet(Some(Stability.Dynamic), Some(Extensibility.Open)), Some(BindingStrength.R), SPEC_ERROR)
  val multi_vs_closed_static_required: MatchResult[String] = expectationSingleVs(DetectionCodes.MultiVSForPrimitive, valueSet(Some(Stability.Static), Some(Extensibility.Closed)), Some(BindingStrength.R), SPEC_ERROR)
  val multi_vs_open_static_required: MatchResult[String] = expectationSingleVs(DetectionCodes.MultiVSForPrimitive, valueSet(Some(Stability.Static), Some(Extensibility.Open)), Some(BindingStrength.R), SPEC_ERROR)

  val multi_vs_closed_dynamic_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.MultiVSForPrimitive, valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed)), Some(BindingStrength.S), SPEC_ERROR)
  val multi_vs_open_dynamic_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.MultiVSForPrimitive, valueSet(Some(Stability.Dynamic), Some(Extensibility.Open)), Some(BindingStrength.S), SPEC_ERROR)
  val multi_vs_closed_static_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.MultiVSForPrimitive, valueSet(Some(Stability.Static), Some(Extensibility.Closed)), Some(BindingStrength.S), SPEC_ERROR)
  val multi_vs_open_static_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.MultiVSForPrimitive, valueSet(Some(Stability.Static), Some(Extensibility.Open)), Some(BindingStrength.S), SPEC_ERROR)

  //--VSNotFound
  val vs_not_found_required: MatchResult[String] = expectationNoVs(DetectionCodes.VSNotFound, Some(BindingStrength.R), SPEC_ERROR)
  val vs_not_found_suggested: MatchResult[String] = expectationNoVs(DetectionCodes.VSNotFound, Some(BindingStrength.S), SPEC_ERROR)

  //--EmptyVS
  val empty_vs_closed_dynamic_required: MatchResult[String] = expectationSingleVs(DetectionCodes.EmptyVS, valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed)), Some(BindingStrength.R), ALERT)
  val empty_vs_open_dynamic_required: MatchResult[String] = expectationSingleVs(DetectionCodes.EmptyVS, valueSet(Some(Stability.Dynamic), Some(Extensibility.Open)), Some(BindingStrength.R), ALERT)
  val empty_vs_closed_static_required: MatchResult[String] = expectationSingleVs(DetectionCodes.EmptyVS, valueSet(Some(Stability.Static), Some(Extensibility.Closed)), Some(BindingStrength.R), ALERT)
  val empty_vs_open_static_required: MatchResult[String] = expectationSingleVs(DetectionCodes.EmptyVS, valueSet(Some(Stability.Static), Some(Extensibility.Open)), Some(BindingStrength.R), ALERT)

  val empty_vs_closed_dynamic_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.EmptyVS, valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed)), Some(BindingStrength.S), ALERT)
  val empty_vs_open_dynamic_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.EmptyVS, valueSet(Some(Stability.Dynamic), Some(Extensibility.Open)), Some(BindingStrength.S), ALERT)
  val empty_vs_closed_static_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.EmptyVS, valueSet(Some(Stability.Static), Some(Extensibility.Closed)), Some(BindingStrength.S), ALERT)
  val empty_vs_open_static_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.EmptyVS, valueSet(Some(Stability.Static), Some(Extensibility.Open)), Some(BindingStrength.S), ALERT)


  //------ HELPERS
  def classificationIs(entry: VSValidationCode, strength: Option[BindingStrength], expected: String): MatchResult[String] = createEntry(elm, entry, strength).getClassification === expected
  def valueSet(stability: Option[Stability], extensibility: Option[Extensibility]): InternalValueSet = InternalValueSet("VS", extensibility, stability, Nil)


  def expectationVsList(detectionCode: DetectionCodes.Value, vsList: List[InternalValueSet], strength: Option[BindingStrength], expected: String): MatchResult[String] = {
    classificationIs(detectionToCaseClass(detectionCode, vsList), strength, expected)
  }

  def expectationSingleVs(detectionCode: DetectionCodes.Value, vs: InternalValueSet, strength: Option[BindingStrength], expected: String): MatchResult[String] = {
    classificationIs(detectionToCaseClass(detectionCode, vs), strength, expected)
  }

  def expectationNoVs(detectionCode: DetectionCodes.Value, strength: Option[BindingStrength], expected: String): MatchResult[String] = {
    classificationIs(detectionToCaseClass(detectionCode), strength, expected)
  }

  def detectionToCaseClass(detectionCode: DetectionCodes.Value, vsList: List[InternalValueSet]): VSValidationCode = {
    detectionCode match {
      case DetectionCodes.CodeNotFound => VSValidationCode.CodeNotFound("A", None, vsList)
      case DetectionCodes.DuplicateCodeAndCodeSystemInValueSetList => VSValidationCode.FoundInMultipleValueSets("A", None, vsList)
    }
  }

  def detectionToCaseClass(detectionCode: DetectionCodes.Value, vs: InternalValueSet): VSValidationCode = {
    val vsb = ValueSetBinding("1[*]", None, List(vs.id), List(BindingLocation(".", None)))
    detectionCode match {
      case DetectionCodes.DynamicVS => VSValidationCode.DynamicValueSet(vs)
      case DetectionCodes.XOR => VSValidationCode.CodedElementXOR(List(), vs)
      case DetectionCodes.PVS => VSValidationCode.PVS(PCode, PCode.value, vs)
      case DetectionCodes.EVS => VSValidationCode.EVS(ECode, ECode.value, vs)
      case DetectionCodes.RVS => VSValidationCode.RVS(RCode, RCode.value, vs)
      case DetectionCodes.NoUsageVS => VSValidationCode.NoUsage(NUCode, NUCode.value, vs)
      case DetectionCodes.InvalidCodeSys => VSValidationCode.SimpleCodeFoundInvalidCodeSystem("A", "B", "C", vs)
      case DetectionCodes.UBS => VSValidationCode.UBS(vsb)
      case DetectionCodes.VSNotFound => VSValidationCode.VSNotFound("")
      case DetectionCodes.VSExcluded => VSValidationCode.ExcludedVS("")
      case DetectionCodes.EmptyVS => VSValidationCode.EmptyVS(vs)
      case DetectionCodes.DuplicateCode => VSValidationCode.MultipleCodesFoundInValueSet("A", vs, List())
      case DetectionCodes.DuplicateCodeAndCodeSystem => VSValidationCode.MultipleCodeAndCodeSystemFound("A", "B", vs, List())
      case DetectionCodes.UsageAndExtensibilityNotCompatible => VSValidationCode.UsageAndExtensibilityNotCompatible(PCode, vs)
      case DetectionCodes.InvalidBindingLocation => VSValidationCode.InvalidCodeBindingLocation("1", multiple = false)
      case DetectionCodes.MultiVSForPrimitive => VSValidationCode.MultipleVSForPrimitive(vsb)
      case _ => throw new Exception("Detection Code Not Found, ValueSet Required")
    }
  }

  def detectionToCaseClass(detectionCode: DetectionCodes.Value): VSValidationCode = {
    detectionCode match {
      case DetectionCodes.VSNotFound => VSValidationCode.VSNotFound("")
      case DetectionCodes.VSExcluded => VSValidationCode.ExcludedVS("")
      case _ => throw new Exception("Detection Code Not Found, ValueSet Not Required")
    }
  }

  trait Default {
    val reqs: List[Req] = List[Req]()
    val location: Location = Location(null, "desc ...", "Path", -1, -1)
    val hasExtra = false
    val req: Req = Req(-1, "", Usage.O, None, None, None, Nil, None, false, None)
    val rawMessageValue = "";
  }

  case class S(override val position: Int, instance: Int, value: Value)
    extends Simple  with Default

  case class C(override val position: Int, instance: Int, children: List[Element])
    extends Complex with Default
}
