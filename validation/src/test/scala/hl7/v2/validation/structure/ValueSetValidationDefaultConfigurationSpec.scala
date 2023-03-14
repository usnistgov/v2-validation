package hl7.v2.validation.structure

import com.typesafe.config.{Config, ConfigFactory}
import hl7.v2.instance.{Complex, Element, Location, Simple, Text, Value}
import hl7.v2.profile.{BindingStrength, Req, Usage}
import hl7.v2.validation.structure
import hl7.v2.validation.vs.{BindingLocation, Code, CodeUsage, EmptyValueSetSpecification, Extensibility, InternalValueSet, Stability, VSValidationCode, ValueSetBinding, ValueSetLibraryImpl, ValueSetSpecification}
import org.specs2.Specification
import org.specs2.matcher.MatchResult

object DetectionCodes extends Enumeration {
  type DetectionCodes = Value

  val DynamicVS: structure.DetectionCodes.Value = Value("DynamicVS")//done
  val XOR: structure.DetectionCodes.Value = Value("XOR")//done
  val CodeNotFound: structure.DetectionCodes.Value = Value("CodeNotFound")//done
  val PVS: structure.DetectionCodes.Value = Value("PVS")//done
  val RVS: structure.DetectionCodes.Value = Value("RVS")//done
  val EVS: structure.DetectionCodes.Value = Value("EVS")//done
  val NoUsageVS: structure.DetectionCodes.Value = Value("NoUsageVS")//done
  val InvalidCodeSys: structure.DetectionCodes.Value = Value("InvalidCodeSys")//done
  val UBS: structure.DetectionCodes.Value = Value("UBS")//done
  val VSExcluded: structure.DetectionCodes.Value = Value("VSExcluded")//done
  val DuplicateCode: structure.DetectionCodes.Value = Value("DuplicateCode")//done
  val DuplicateCodeAndCodeSystem: structure.DetectionCodes.Value = Value("DuplicateCodeAndCodeSystem")//done
  val DuplicateCodeAndCodeSystemInValueSetList: structure.DetectionCodes.Value = Value("DuplicateCodeAndCodeSystemInValueSetList")//done

  //those are "spec error" detections
  val UsageAndExtensibilityNotCompatible: structure.DetectionCodes.Value = Value("UsageAndExtensibilityNotCompatible")//done
  val InvalidBindingLocation: structure.DetectionCodes.Value = Value("InvalidBindingLocation")//done
  val MultiVSForPrimitive: structure.DetectionCodes.Value = Value("MultiVSForPrimitive")
  val VSNotFound: structure.DetectionCodes.Value = Value("VSNotFound")
  val EmptyVS: structure.DetectionCodes.Value = Value("EmptyVS")

}

trait ValueSetValidationDefaultConfigurationSpec extends Specification
  with hl7.v2.validation.vs.DefaultValueSetValidator
  with Helpers {
  def is =s2"""
      DynamicVS Detection when a value set is Closed/ Dynamic and Binding Strength is Required should be ALERT $dynamicVS_closed_dynamic_required
      DynamicVS Detection when a value set is Open/ Dynamic and Binding Strength is Required should be ALERT $dynamicVS_open_dynamic_required
      DynamicVS Detection when a value set is Closed/ Dynamic and Binding Strength is Suggested should be ALERT $dynamicVS_closed_dynamic_suggested
      DynamicVS Detection when a value set is Open/ Dynamic and Binding Strength is Suggested should be ALERT $dynamicVS_open_dynamic_suggested

      XOR Detection when a value set is Closed/ Dynamic and Binding Strength is Required should be ERROR $xor_closed_dynamic_required
      XOR Detection when a value set is Open/ Dynamic and Binding Strength is Required should be ERROR $xor_open_dynamic_required
      XOR Detection when a value set is Closed/ Static and Binding Strength is Required should be ERROR $xor_closed_static_required
      XOR Detection when a value set is Open/ Static and Binding Strength is Required should be ERROR $xor_open_static_required

      XOR Detection when a value set is Closed/ Dynamic and Binding Strength is Suggested should be ALERT $xor_closed_dynamic_suggested
      XOR Detection when a value set is Open/ Dynamic and Binding Strength is Suggested should be ALERT $xor_open_dynamic_suggested
      XOR Detection when a value set is Closed/ Static and Binding Strength is Suggested should be ALERT $xor_closed_static_suggested
      XOR Detection when a value set is Open/ Static and Binding Strength is Suggested should be ALERT $xor_open_static_suggested

      NoUsageVS Detection when a value set is Closed/ Dynamic and Binding Strength is Required should be ALERT $no_usage_closed_dynamic_required
      NoUsageVS Detection when a value set is Open/ Dynamic and Binding Strength is Required should be ALERT $no_usage_open_dynamic_required
      NoUsageVS Detection when a value set is Closed/ Static and Binding Strength is Required should be ALERT $no_usage_closed_static_required
      NoUsageVS Detection when a value set is Open/ Static and Binding Strength is Required should be ALERT $no_usage_open_static_required

      NoUsageVS Detection when a value set is Closed/ Dynamic and Binding Strength is Suggested should be ALERT $no_usage_closed_dynamic_suggested
      NoUsageVS Detection when a value set is Open/ Dynamic and Binding Strength is Suggested should be ALERT $no_usage_open_dynamic_suggested
      NoUsageVS Detection when a value set is Closed/ Static and Binding Strength is Suggested should be ALERT $no_usage_closed_static_suggested
      NoUsageVS Detection when a value set is Open/ Static and Binding Strength is Suggested should be ALERT $no_usage_open_static_suggested

      EVS Detection when a value set is Closed/ Dynamic and Binding Strength is Required should be ERROR $evs_closed_dynamic_required
      EVS Detection when a value set is Open/ Dynamic and Binding Strength is Required should be ERROR $evs_open_dynamic_required
      EVS Detection when a value set is Closed/ Static and Binding Strength is Required should be ERROR $evs_closed_static_required
      EVS Detection when a value set is Open/ Static and Binding Strength is Required should be ERROR $evs_open_static_required

      EVS Detection when a value set is Closed/ Dynamic and Binding Strength is Suggested should be ALERT $evs_closed_dynamic_suggested
      EVS Detection when a value set is Open/ Dynamic and Binding Strength is Suggested should be ALERT $evs_open_dynamic_suggested
      EVS Detection when a value set is Closed/ Static and Binding Strength is Suggested should be ALERT $evs_closed_static_suggested
      EVS Detection when a value set is Open/ Static and Binding Strength is Suggested should be ALERT $evs_open_static_suggested

      PVS Detection when a value set is Closed/ Dynamic and Binding Strength is Required should be ALERT $pvs_closed_dynamic_required
      PVS Detection when a value set is Open/ Dynamic and Binding Strength is Required should be ALERT $pvs_open_dynamic_required
      PVS Detection when a value set is Closed/ Static and Binding Strength is Required should be ALERT $pvs_closed_static_required
      PVS Detection when a value set is Open/ Static and Binding Strength is Required should be ALERT $pvs_open_static_required

      PVS Detection when a value set is Closed/ Dynamic and Binding Strength is Suggested should be ALERT $pvs_closed_dynamic_suggested
      PVS Detection when a value set is Open/ Dynamic and Binding Strength is Suggested should be ALERT $pvs_open_dynamic_suggested
      PVS Detection when a value set is Closed/ Static and Binding Strength is Suggested should be ALERT $pvs_closed_static_suggested
      PVS Detection when a value set is Open/ Static and Binding Strength is Suggested should be ALERT $pvs_open_static_suggested

      RVS Detection when a value set is Closed/ Dynamic and Binding Strength is Required should be AFFIRMATIVE $rvs_closed_dynamic_required
      RVS Detection when a value set is Open/ Dynamic and Binding Strength is Required should be AFFIRMATIVE $rvs_open_dynamic_required
      RVS Detection when a value set is Closed/ Static and Binding Strength is Required should be AFFIRMATIVE $rvs_closed_static_required
      RVS Detection when a value set is Open/ Static and Binding Strength is Required should be AFFIRMATIVE $rvs_open_static_required

      RVS Detection when a value set is Closed/ Dynamic and Binding Strength is Suggested should be ALERT $rvs_closed_dynamic_suggested
      RVS Detection when a value set is Open/ Dynamic and Binding Strength is Suggested should be ALERT $rvs_open_dynamic_suggested
      RVS Detection when a value set is Closed/ Static and Binding Strength is Suggested should be ALERT $rvs_closed_static_suggested
      RVS Detection when a value set is Open/ Static and Binding Strength is Suggested should be ALERT $rvs_open_static_suggested

      DuplicateCodeAndCodeSystem Detection when a value set is Closed/ Dynamic and Binding Strength is Required should be SPEC_ERROR $dup_C_CS_closed_dynamic_required
      DuplicateCodeAndCodeSystem Detection when a value set is Open/ Dynamic and Binding Strength is Required should be SPEC_ERROR $dup_C_CS_open_dynamic_required
      DuplicateCodeAndCodeSystem Detection when a value set is Closed/ Static and Binding Strength is Required should be SPEC_ERROR $dup_C_CS_closed_static_required
      DuplicateCodeAndCodeSystem Detection when a value set is Open/ Static and Binding Strength is Required should be SPEC_ERROR $dup_C_CS_open_static_required

      DuplicateCodeAndCodeSystem Detection when a value set is Closed/ Dynamic and Binding Strength is Suggested should be SPEC_ERROR $dup_C_CS_closed_dynamic_suggested
      DuplicateCodeAndCodeSystem Detection when a value set is Open/ Dynamic and Binding Strength is Suggested should be SPEC_ERROR $dup_C_CS_open_dynamic_suggested
      DuplicateCodeAndCodeSystem Detection when a value set is Closed/ Static and Binding Strength is Suggested should be SPEC_ERROR $dup_C_CS_closed_static_suggested
      DuplicateCodeAndCodeSystem Detection when a value set is Open/ Static and Binding Strength is Suggested should be SPEC_ERROR $dup_C_CS_open_static_suggested

      DuplicateCode Detection when a value set is Closed/ Dynamic and Binding Strength is Required should be SPEC_ERROR $dup_C_closed_dynamic_required
      DuplicateCode Detection when a value set is Open/ Dynamic and Binding Strength is Required should be SPEC_ERROR $dup_C_open_dynamic_required
      DuplicateCode Detection when a value set is Closed/ Static and Binding Strength is Required should be SPEC_ERROR $dup_C_closed_static_required
      DuplicateCode Detection when a value set is Open/ Static and Binding Strength is Required should be SPEC_ERROR $dup_C_open_static_required

      DuplicateCode Detection when a value set is Closed/ Dynamic and Binding Strength is Suggested should be SPEC_ERROR $dup_C_closed_dynamic_suggested
      DuplicateCode Detection when a value set is Open/ Dynamic and Binding Strength is Suggested should be SPEC_ERROR $dup_C_open_dynamic_suggested
      DuplicateCode Detection when a value set is Closed/ Static and Binding Strength is Suggested should be SPEC_ERROR $dup_C_closed_static_suggested
      DuplicateCode Detection when a value set is Open/ Static and Binding Strength is Suggested should be SPEC_ERROR $dup_C_open_static_suggested

      InvalidCodeSys Detection when a value set is Closed/ Dynamic and Binding Strength is Required should be HIGH ALERT $invalid_CS_closed_dynamic_required
      InvalidCodeSys Detection when a value set is Open/ Dynamic and Binding Strength is Required should be HIGH ALERT $invalid_CS_open_dynamic_required
      InvalidCodeSys Detection when a value set is Closed/ Static and Binding Strength is Required should be ERROR $invalid_CS_closed_static_required
      InvalidCodeSys Detection when a value set is Open/ Static and Binding Strength is Required should be HIGH ALERT $invalid_CS_open_static_required

      InvalidCodeSys Detection when a value set is Closed/ Dynamic and Binding Strength is Suggested should be ALERT $invalid_CS_closed_dynamic_suggested
      InvalidCodeSys Detection when a value set is Open/ Dynamic and Binding Strength is Suggested should be ALERT $invalid_CS_open_dynamic_suggested
      InvalidCodeSys Detection when a value set is Closed/ Static and Binding Strength is Suggested should be ALERT $invalid_CS_closed_static_suggested
      InvalidCodeSys Detection when a value set is Open/ Static and Binding Strength is Suggested should be ALERT $invalid_CS_open_static_suggested

      CodeNotFound Detection when a value set is Closed/ Dynamic and Binding Strength is Required should be HIGH ALERT $codeNotFound_closed_dynamic_required
      CodeNotFound Detection when a value set is Closed/ Static and Binding Strength is Required should be ERROR $codeNotFound_closed_static_required
      CodeNotFound Detection when a value set is Open/ Dynamic and Binding Strength is Required should be HIGH ALERT $codeNotFound_open_dynamic_required
      CodeNotFound Detection when a value set is Open/ Static and Binding Strength is Required should be HIGH ALERT $codeNotFound_open_static_required

      CodeNotFound Detection when a value set is Closed/ Dynamic and Binding Strength is Suggested should be ALERT $codeNotFound_closed_dynamic_suggested
      CodeNotFound Detection when a value set is Closed/ Static and Binding Strength is Suggested should be ALERT $codeNotFound_closed_static_suggested
      CodeNotFound Detection when a value set is Open/ Dynamic and Binding Strength is Suggested should be ALERT $codeNotFound_open_dynamic_suggested
      CodeNotFound Detection when a value set is Open/ Static and Binding Strength is Suggested should be ALERT $codeNotFound_open_static_suggested

      CodeNotFound Detection when a value set in the value set list is Closed/ Static and Binding Strength is Required should be ERROR $codeNotFound_closed_static_closed_static_required
      CodeNotFound Detection when a value set in the value set list is Closed/ Static and Binding Strength is Suggested should be ALERT $codeNotFound_closed_static_closed_static_suggested
      CodeNotFound Detection when a value set in the value set list is Dynamic and Binding Strength is Required should be ERROR $codeNotFound_closed_dynamic_closed_dynamic_required
      CodeNotFound Detection when a value set in the value set list is Dynamic and Binding Strength is Suggested should be ALERT $codeNotFound_closed_dynamic_closed_dynamic_suggested
      CodeNotFound Detection when a value set in the value set list is Dynamic and Binding Strength is Required should be ERROR $codeNotFound_closed_dynamic_closed_static_required
      CodeNotFound Detection when a value set in the value set list is Dynamic and Binding Strength is Suggested should be ALERT $codeNotFound_closed_dynamic_closed_static_suggested
      CodeNotFound Detection when a value set in the value set list is Open and Binding Strength is Required should be ERROR $codeNotFound_open_static_open_static_required
      CodeNotFound Detection when a value set in the value set list is Open and Binding Strength is Suggested should be ALERT $codeNotFound_open_static_open_static_suggested
      CodeNotFound Detection when a value set in the value set list is Open or Dynamic and Binding Strength is Required should be ERROR $codeNotFound_open_dynamic_open_dynamic_required
      CodeNotFound Detection when a value set in the value set list is Open or Dynamic and Binding Strength is Suggested should be ALERT $codeNotFound_open_dynamic_open_dynamic_suggested
      CodeNotFound Detection when a value set in the value set list is Open or Dynamic and Binding Strength is Required should be ERROR $codeNotFound_open_dynamic_open_static_required
      CodeNotFound Detection when a value set in the value set list is Open or Dynamic and Binding Strength is Suggested should be ALERT $codeNotFound_open_dynamic_open_static_suggested
      CodeNotFound Detection when a value set in the value set list is Open and Binding Strength is Required should be ERROR $codeNotFound_open_static_closed_static_required
      CodeNotFound Detection when a value set in the value set list is Open and Binding Strength is Suggested should be ALERT $codeNotFound_open_static_closed_static_suggested
      CodeNotFound Detection when a value set in the value set list is Open or Dynamic and Binding Strength is Required should be ERROR $codeNotFound_open_dynamic_closed_dynamic_required
      CodeNotFound Detection when a value set in the value set list is Open or Dynamic and Binding Strength is Suggested should be ALERT $codeNotFound_open_dynamic_closed_dynamic_suggested
      CodeNotFound Detection when a value set in the value set list is Open or Dynamic and Binding Strength is Required should be ERROR $codeNotFound_open_static_closed_dynamic_required
      CodeNotFound Detection when a value set in the value set list is Open or Dynamic and Binding Strength is Suggested should be ALERT $codeNotFound_open_static_closed_dynamic_suggested
      CodeNotFound Detection when a value set in the value set list is Open or Dynamic and Binding Strength is Required should be ERROR $codeNotFound_open_dynamic_closed_static_required
      CodeNotFound Detection when a value set in the value set list is Open or Dynamic and Binding Strength is Suggested should be ALERT $codeNotFound_open_dynamic_closed_static_suggested

      DuplicateCodeAndCodeSystemInValueSetList Detection when all value set in the value set list are Closed/ Static and Binding Strength is Required should be SPEC_ERROR $dup_C_CS_list_closed_static_closed_static_required
      DuplicateCodeAndCodeSystemInValueSetList Detection when all value set in the value set list are Closed/ Static and Binding Strength is Suggested should be SPEC_ERROR $dup_C_CS_list_closed_static_closed_static_suggested
      DuplicateCodeAndCodeSystemInValueSetList Detection when all value set in the value set list are Closed/ Dynamic and Binding Strength is Required should be SPEC_ERROR $dup_C_CS_list_closed_dynamic_closed_dynamic_required
      DuplicateCodeAndCodeSystemInValueSetList Detection when all value set in the value set list are Closed/ Dynamic and Binding Strength is Suggested should be SPEC_ERROR $dup_C_CS_list_closed_dynamic_closed_dynamic_suggested
      DuplicateCodeAndCodeSystemInValueSetList Detection when all value set in the value set list are Closed/ Static or Dynamic and Binding Strength is Required should be SPEC_ERROR $dup_C_CS_list_closed_dynamic_closed_static_required
      DuplicateCodeAndCodeSystemInValueSetList Detection when all value set in the value set list are Closed/ Static or Dynamic and Binding Strength is Suggested should be SPEC_ERROR $dup_C_CS_list_closed_dynamic_closed_static_suggested
      DuplicateCodeAndCodeSystemInValueSetList Detection when all value set in the value set list are Open/ Static and Binding Strength is Required should be SPEC_ERROR $dup_C_CS_list_open_static_open_static_required
      DuplicateCodeAndCodeSystemInValueSetList Detection when all value set in the value set list are Open/ Static and Binding Strength is Suggested should be SPEC_ERROR $dup_C_CS_list_open_static_open_static_suggested
      DuplicateCodeAndCodeSystemInValueSetList Detection when all value set in the value set list are Open/ Dynamic and Binding Strength is Required should be SPEC_ERROR $dup_C_CS_list_open_dynamic_open_dynamic_required
      DuplicateCodeAndCodeSystemInValueSetList Detection when all value set in the value set list are Open/ Dynamic and Binding Strength is Suggested should be SPEC_ERROR $dup_C_CS_list_open_dynamic_open_dynamic_suggested
      DuplicateCodeAndCodeSystemInValueSetList Detection when all value set in the value set list are Open/ Static or Dynamic and Binding Strength is Required should be SPEC_ERROR $dup_C_CS_list_open_dynamic_open_static_required
      DuplicateCodeAndCodeSystemInValueSetList Detection when all value set in the value set list are Open/ Static or Dynamic and Binding Strength is Suggested should be SPEC_ERROR $dup_C_CS_list_open_dynamic_open_static_suggested
      DuplicateCodeAndCodeSystemInValueSetList Detection when all value set in the value set list are Open or Closed/ Static and Binding Strength is Required should be SPEC_ERROR $dup_C_CS_list_open_static_closed_static_required
      DuplicateCodeAndCodeSystemInValueSetList Detection when all value set in the value set list are Open or Closed/ Static and Binding Strength is Suggested should be SPEC_ERROR $dup_C_CS_list_open_static_closed_static_suggested
      DuplicateCodeAndCodeSystemInValueSetList Detection when all value set in the value set list are Open or Closed/ Dynamic and Binding Strength is Required should be SPEC_ERROR $dup_C_CS_list_open_dynamic_closed_dynamic_required
      DuplicateCodeAndCodeSystemInValueSetList Detection when all value set in the value set list are Open or Closed/ Dynamic and Binding Strength is Suggested should be SPEC_ERROR $dup_C_CS_list_open_dynamic_closed_dynamic_suggested
      DuplicateCodeAndCodeSystemInValueSetList Detection when all value set in the value set list are Open or Closed/ Static or Dynamic and Binding Strength is Required should be SPEC_ERROR $dup_C_CS_list_open_static_closed_dynamic_required
      DuplicateCodeAndCodeSystemInValueSetList Detection when all value set in the value set list are Open or Closed/ Static or Dynamic and Binding Strength is Suggested should be SPEC_ERROR $dup_C_CS_list_open_static_closed_dynamic_suggested
      DuplicateCodeAndCodeSystemInValueSetList Detection when all value set in the value set list are Open or Closed/ Static or Dynamic and Binding Strength is Required should be SPEC_ERROR $dup_C_CS_list_open_dynamic_closed_static_required
      DuplicateCodeAndCodeSystemInValueSetList Detection when all value set in the value set list are Open or Closed/ Static or Dynamic and Binding Strength is Suggested should be SPEC_ERROR $dup_C_CS_list_open_dynamic_closed_static_suggested

      UBS Detection when a value set is Closed/ Dynamic and Binding Strength is Required should be ALERT $ubs_closed_dynamic_required
      UBS Detection when a value set is Open/ Dynamic and Binding Strength is Required should be ALERT $ubs_open_dynamic_required
      UBS Detection when a value set is Closed/ Static and Binding Strength is Required should be ALERT $ubs_closed_static_required
      UBS Detection when a value set is Open/ Static and Binding Strength is Required should be ALERT $ubs_open_static_required

      UBS Detection when a value set is Closed/ Dynamic and Binding Strength is Suggested should be ALERT $ubs_closed_dynamic_suggested
      UBS Detection when a value set is Open/ Dynamic and Binding Strength is Suggested should be ALERT $ubs_open_dynamic_suggested
      UBS Detection when a value set is Closed/ Static and Binding Strength is Suggested should be ALERT $ubs_closed_static_suggested
      UBS Detection when a value set is Open/ Static and Binding Strength is Suggested should be ALERT $ubs_open_static_suggested

      UsageAndExtensibilityNotCompatible Detection when a value set is Closed/ Dynamic and Binding Strength is Required should be ALERT $usage_extensibility_closed_dynamic_required
      UsageAndExtensibilityNotCompatible Detection when a value set is Closed/ Static and Binding Strength is Required should be ALERT $usage_extensibility_closed_static_required

      UsageAndExtensibilityNotCompatible Detection when a value set is Closed/ Dynamic and Binding Strength is Suggested should be ALERT $usage_extensibility_closed_dynamic_suggested
      UsageAndExtensibilityNotCompatible Detection when a value set is Closed/ Static and Binding Strength is Suggested should be ALERT $usage_extensibility_closed_static_suggested

      InvalidBindingLocation Detection when a value set is Closed/ Dynamic and Binding Strength is Required should be SPEC_ERROR $invalid_bl_closed_dynamic_required
      InvalidBindingLocation Detection when a value set is Open/ Dynamic and Binding Strength is Required should be SPEC_ERROR $invalid_bl_open_dynamic_required
      InvalidBindingLocation Detection when a value set is Closed/ Static and Binding Strength is Required should be SPEC_ERROR $invalid_bl_closed_static_required
      InvalidBindingLocation Detection when a value set is Open/ Static and Binding Strength is Required should be SPEC_ERROR $invalid_bl_open_static_required

      InvalidBindingLocation Detection when a value set is Closed/ Dynamic and Binding Strength is Suggested should be SPEC_ERROR $invalid_bl_closed_dynamic_suggested
      InvalidBindingLocation Detection when a value set is Open/ Dynamic and Binding Strength is Suggested should be SPEC_ERROR $invalid_bl_open_dynamic_suggested
      InvalidBindingLocation Detection when a value set is Closed/ Static and Binding Strength is Suggested should be SPEC_ERROR $invalid_bl_closed_static_suggested
      InvalidBindingLocation Detection when a value set is Open/ Static and Binding Strength is Suggested should be SPEC_ERROR $invalid_bl_open_static_suggested

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
  val AFFIRMATIVE: String =  _default.getString("report.classification.affirmative")

  val PCode: Code = Code("A", "B", CodeUsage.P, "C")
  val RCode: Code = Code("A", "B", CodeUsage.R, "C")
  val ECode: Code = Code("A", "B", CodeUsage.E, "C")
  val NUCode: Code = Code("A", "B", null, "C")

  //------ TEST CASES

  //-- DynamicVS
  val dynamicVS_closed_dynamic_required: MatchResult[String] = expectationSingleVs(DetectionCodes.DynamicVS, valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed)), Some(BindingStrength.R), ALERT)
  val dynamicVS_open_dynamic_required: MatchResult[String] = expectationSingleVs(DetectionCodes.DynamicVS, valueSet(Some(Stability.Dynamic), Some(Extensibility.Open)), Some(BindingStrength.R), ALERT)
  val dynamicVS_closed_dynamic_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.DynamicVS, valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed)), Some(BindingStrength.S), ALERT)
  val dynamicVS_open_dynamic_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.DynamicVS, valueSet(Some(Stability.Dynamic), Some(Extensibility.Open)), Some(BindingStrength.S), ALERT)
  // DynamicVS should not be triggered for a static value set
  // DynamicVS should not be triggered for a binding strength of U

  //-- XOR
  //TODO : double check with Rob if XOR is HIGH ALERT or ERROR
  val xor_closed_dynamic_required: MatchResult[String] = expectationSingleVs(DetectionCodes.XOR, valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed)), Some(BindingStrength.R), ERROR)
  val xor_open_dynamic_required: MatchResult[String] = expectationSingleVs(DetectionCodes.XOR, valueSet(Some(Stability.Dynamic), Some(Extensibility.Open)), Some(BindingStrength.R), ERROR)
  val xor_closed_static_required: MatchResult[String] = expectationSingleVs(DetectionCodes.XOR, valueSet(Some(Stability.Static), Some(Extensibility.Closed)), Some(BindingStrength.R), ERROR)
  val xor_open_static_required: MatchResult[String] = expectationSingleVs(DetectionCodes.XOR, valueSet(Some(Stability.Static), Some(Extensibility.Open)), Some(BindingStrength.R), ERROR)

  val xor_closed_dynamic_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.XOR, valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed)), Some(BindingStrength.S), ALERT)
  val xor_open_dynamic_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.XOR, valueSet(Some(Stability.Dynamic), Some(Extensibility.Open)), Some(BindingStrength.S), ALERT)
  val xor_closed_static_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.XOR, valueSet(Some(Stability.Static), Some(Extensibility.Closed)), Some(BindingStrength.S), ALERT)
  val xor_open_static_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.XOR, valueSet(Some(Stability.Static), Some(Extensibility.Open)), Some(BindingStrength.S), ALERT)
  // XOR should not be triggered for a binding strength of U

  //--NoUsageVS
  val no_usage_closed_dynamic_required: MatchResult[String] = expectationSingleVs(DetectionCodes.NoUsageVS, valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed)), Some(BindingStrength.R), ALERT)
  val no_usage_open_dynamic_required: MatchResult[String] = expectationSingleVs(DetectionCodes.NoUsageVS, valueSet(Some(Stability.Dynamic), Some(Extensibility.Open)), Some(BindingStrength.R), ALERT)
  val no_usage_closed_static_required: MatchResult[String] = expectationSingleVs(DetectionCodes.NoUsageVS, valueSet(Some(Stability.Static), Some(Extensibility.Closed)), Some(BindingStrength.R), ALERT)
  val no_usage_open_static_required: MatchResult[String] = expectationSingleVs(DetectionCodes.NoUsageVS, valueSet(Some(Stability.Static), Some(Extensibility.Open)), Some(BindingStrength.R), ALERT)

  val no_usage_closed_dynamic_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.NoUsageVS, valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed)), Some(BindingStrength.S), ALERT)
  val no_usage_open_dynamic_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.NoUsageVS, valueSet(Some(Stability.Dynamic), Some(Extensibility.Open)), Some(BindingStrength.S), ALERT)
  val no_usage_closed_static_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.NoUsageVS, valueSet(Some(Stability.Static), Some(Extensibility.Closed)), Some(BindingStrength.S), ALERT)
  val no_usage_open_static_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.NoUsageVS, valueSet(Some(Stability.Static), Some(Extensibility.Open)), Some(BindingStrength.S), ALERT)

  //--EVS
  val evs_closed_dynamic_required: MatchResult[String] = expectationSingleVs(DetectionCodes.EVS, valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed)), Some(BindingStrength.R), ERROR)
  val evs_open_dynamic_required: MatchResult[String] = expectationSingleVs(DetectionCodes.EVS, valueSet(Some(Stability.Dynamic), Some(Extensibility.Open)), Some(BindingStrength.R), ERROR)
  val evs_closed_static_required: MatchResult[String] = expectationSingleVs(DetectionCodes.EVS, valueSet(Some(Stability.Static), Some(Extensibility.Closed)), Some(BindingStrength.R), ERROR)
  val evs_open_static_required: MatchResult[String] = expectationSingleVs(DetectionCodes.EVS, valueSet(Some(Stability.Static), Some(Extensibility.Open)), Some(BindingStrength.R), ERROR)

  val evs_closed_dynamic_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.EVS, valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed)), Some(BindingStrength.S), ALERT)
  val evs_open_dynamic_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.EVS, valueSet(Some(Stability.Dynamic), Some(Extensibility.Open)), Some(BindingStrength.S), ALERT)
  val evs_closed_static_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.EVS, valueSet(Some(Stability.Static), Some(Extensibility.Closed)), Some(BindingStrength.S), ALERT)
  val evs_open_static_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.EVS, valueSet(Some(Stability.Static), Some(Extensibility.Open)), Some(BindingStrength.S), ALERT)

  //--PVS
  val pvs_closed_dynamic_required: MatchResult[String] = expectationSingleVs(DetectionCodes.PVS, valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed)), Some(BindingStrength.R), ALERT)
  val pvs_open_dynamic_required: MatchResult[String] = expectationSingleVs(DetectionCodes.PVS, valueSet(Some(Stability.Dynamic), Some(Extensibility.Open)), Some(BindingStrength.R), ALERT)
  val pvs_closed_static_required: MatchResult[String] = expectationSingleVs(DetectionCodes.PVS, valueSet(Some(Stability.Static), Some(Extensibility.Closed)), Some(BindingStrength.R), ALERT)
  val pvs_open_static_required: MatchResult[String] = expectationSingleVs(DetectionCodes.PVS, valueSet(Some(Stability.Static), Some(Extensibility.Open)), Some(BindingStrength.R), ALERT)

  val pvs_closed_dynamic_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.PVS, valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed)), Some(BindingStrength.S), ALERT)
  val pvs_open_dynamic_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.PVS, valueSet(Some(Stability.Dynamic), Some(Extensibility.Open)), Some(BindingStrength.S), ALERT)
  val pvs_closed_static_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.PVS, valueSet(Some(Stability.Static), Some(Extensibility.Closed)), Some(BindingStrength.S), ALERT)
  val pvs_open_static_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.PVS, valueSet(Some(Stability.Static), Some(Extensibility.Open)), Some(BindingStrength.S), ALERT)

  //--RVS
  val rvs_closed_dynamic_required: MatchResult[String] = expectationSingleVs(DetectionCodes.RVS, valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed)), Some(BindingStrength.R), AFFIRMATIVE)
  val rvs_open_dynamic_required: MatchResult[String] = expectationSingleVs(DetectionCodes.RVS, valueSet(Some(Stability.Dynamic), Some(Extensibility.Open)), Some(BindingStrength.R), AFFIRMATIVE)
  val rvs_closed_static_required: MatchResult[String] = expectationSingleVs(DetectionCodes.RVS, valueSet(Some(Stability.Static), Some(Extensibility.Closed)), Some(BindingStrength.R), AFFIRMATIVE)
  val rvs_open_static_required: MatchResult[String] = expectationSingleVs(DetectionCodes.RVS, valueSet(Some(Stability.Static), Some(Extensibility.Open)), Some(BindingStrength.R), AFFIRMATIVE)

  val rvs_closed_dynamic_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.RVS, valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed)), Some(BindingStrength.S), ALERT)
  val rvs_open_dynamic_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.RVS, valueSet(Some(Stability.Dynamic), Some(Extensibility.Open)), Some(BindingStrength.S), ALERT)
  val rvs_closed_static_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.RVS, valueSet(Some(Stability.Static), Some(Extensibility.Closed)), Some(BindingStrength.S), ALERT)
  val rvs_open_static_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.RVS, valueSet(Some(Stability.Static), Some(Extensibility.Open)), Some(BindingStrength.S), ALERT)

  //--DuplicateCodeAndCodeSystem
  val dup_C_CS_closed_dynamic_required: MatchResult[String] = expectationSingleVs(DetectionCodes.DuplicateCodeAndCodeSystem, valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed)), Some(BindingStrength.R), SPEC_ERROR)
  val dup_C_CS_open_dynamic_required: MatchResult[String] = expectationSingleVs(DetectionCodes.DuplicateCodeAndCodeSystem, valueSet(Some(Stability.Dynamic), Some(Extensibility.Open)), Some(BindingStrength.R), SPEC_ERROR)
  val dup_C_CS_closed_static_required: MatchResult[String] = expectationSingleVs(DetectionCodes.DuplicateCodeAndCodeSystem, valueSet(Some(Stability.Static), Some(Extensibility.Closed)), Some(BindingStrength.R), SPEC_ERROR)
  val dup_C_CS_open_static_required: MatchResult[String] = expectationSingleVs(DetectionCodes.DuplicateCodeAndCodeSystem, valueSet(Some(Stability.Static), Some(Extensibility.Open)), Some(BindingStrength.R), SPEC_ERROR)

  val dup_C_CS_closed_dynamic_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.DuplicateCodeAndCodeSystem, valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed)), Some(BindingStrength.S), SPEC_ERROR)
  val dup_C_CS_open_dynamic_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.DuplicateCodeAndCodeSystem, valueSet(Some(Stability.Dynamic), Some(Extensibility.Open)), Some(BindingStrength.S), SPEC_ERROR)
  val dup_C_CS_closed_static_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.DuplicateCodeAndCodeSystem, valueSet(Some(Stability.Static), Some(Extensibility.Closed)), Some(BindingStrength.S), SPEC_ERROR)
  val dup_C_CS_open_static_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.DuplicateCodeAndCodeSystem, valueSet(Some(Stability.Static), Some(Extensibility.Open)), Some(BindingStrength.S), SPEC_ERROR)

  //--DuplicateCode
  val dup_C_closed_dynamic_required: MatchResult[String] = expectationSingleVs(DetectionCodes.DuplicateCode, valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed)), Some(BindingStrength.R), SPEC_ERROR)
  val dup_C_open_dynamic_required: MatchResult[String] = expectationSingleVs(DetectionCodes.DuplicateCode, valueSet(Some(Stability.Dynamic), Some(Extensibility.Open)), Some(BindingStrength.R), SPEC_ERROR)
  val dup_C_closed_static_required: MatchResult[String] = expectationSingleVs(DetectionCodes.DuplicateCode, valueSet(Some(Stability.Static), Some(Extensibility.Closed)), Some(BindingStrength.R), SPEC_ERROR)
  val dup_C_open_static_required: MatchResult[String] = expectationSingleVs(DetectionCodes.DuplicateCode, valueSet(Some(Stability.Static), Some(Extensibility.Open)), Some(BindingStrength.R), SPEC_ERROR)

  val dup_C_closed_dynamic_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.DuplicateCode, valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed)), Some(BindingStrength.S), SPEC_ERROR)
  val dup_C_open_dynamic_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.DuplicateCode, valueSet(Some(Stability.Dynamic), Some(Extensibility.Open)), Some(BindingStrength.S), SPEC_ERROR)
  val dup_C_closed_static_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.DuplicateCode, valueSet(Some(Stability.Static), Some(Extensibility.Closed)), Some(BindingStrength.S), SPEC_ERROR)
  val dup_C_open_static_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.DuplicateCode, valueSet(Some(Stability.Static), Some(Extensibility.Open)), Some(BindingStrength.S), SPEC_ERROR)

  //--InvalidCodeSys
  val invalid_CS_closed_dynamic_required: MatchResult[String] = expectationSingleVs(DetectionCodes.InvalidCodeSys, valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed)), Some(BindingStrength.R), ERROR)
  val invalid_CS_open_dynamic_required: MatchResult[String] = expectationSingleVs(DetectionCodes.InvalidCodeSys, valueSet(Some(Stability.Dynamic), Some(Extensibility.Open)), Some(BindingStrength.R), ERROR)
  val invalid_CS_closed_static_required: MatchResult[String] = expectationSingleVs(DetectionCodes.InvalidCodeSys, valueSet(Some(Stability.Static), Some(Extensibility.Closed)), Some(BindingStrength.R), ERROR)
  val invalid_CS_open_static_required: MatchResult[String] = expectationSingleVs(DetectionCodes.InvalidCodeSys, valueSet(Some(Stability.Static), Some(Extensibility.Open)), Some(BindingStrength.R), ERROR)

  val invalid_CS_closed_dynamic_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.InvalidCodeSys, valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed)), Some(BindingStrength.S), ALERT)
  val invalid_CS_open_dynamic_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.InvalidCodeSys, valueSet(Some(Stability.Dynamic), Some(Extensibility.Open)), Some(BindingStrength.S), ALERT)
  val invalid_CS_closed_static_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.InvalidCodeSys, valueSet(Some(Stability.Static), Some(Extensibility.Closed)), Some(BindingStrength.S), ALERT)
  val invalid_CS_open_static_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.InvalidCodeSys, valueSet(Some(Stability.Static), Some(Extensibility.Open)), Some(BindingStrength.S), ALERT)

  //--CodeNotFound - single value set
  val codeNotFound_closed_dynamic_required = expectationVsList(DetectionCodes.CodeNotFound, List(valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed))), Some(BindingStrength.R), ERROR)
  val codeNotFound_closed_static_required = expectationVsList(DetectionCodes.CodeNotFound, List(valueSet(Some(Stability.Static), Some(Extensibility.Closed))), Some(BindingStrength.R), ERROR)
  val codeNotFound_open_dynamic_required = expectationVsList(DetectionCodes.CodeNotFound, List(valueSet(Some(Stability.Dynamic), Some(Extensibility.Open))), Some(BindingStrength.R), ERROR)
  val codeNotFound_open_static_required = expectationVsList(DetectionCodes.CodeNotFound, List(valueSet(Some(Stability.Static), Some(Extensibility.Open))), Some(BindingStrength.R), ERROR)

  val codeNotFound_closed_dynamic_suggested = expectationVsList(DetectionCodes.CodeNotFound, List(valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed))), Some(BindingStrength.S), ALERT)
  val codeNotFound_closed_static_suggested = expectationVsList(DetectionCodes.CodeNotFound, List(valueSet(Some(Stability.Static), Some(Extensibility.Closed))), Some(BindingStrength.S), ALERT)
  val codeNotFound_open_dynamic_suggested = expectationVsList(DetectionCodes.CodeNotFound, List(valueSet(Some(Stability.Dynamic), Some(Extensibility.Open))), Some(BindingStrength.S), ALERT)
  val codeNotFound_open_static_suggested = expectationVsList(DetectionCodes.CodeNotFound, List(valueSet(Some(Stability.Static), Some(Extensibility.Open))), Some(BindingStrength.S), ALERT)

  //-- CodeNotFound - multiple value sets
  //-- use case one : all closed, all static
  val codeNotFound_closed_static_closed_static_required = expectationVsList(DetectionCodes.CodeNotFound, List(valueSet(Some(Stability.Static), Some(Extensibility.Closed)),valueSet(Some(Stability.Static), Some(Extensibility.Closed))), Some(BindingStrength.R), ERROR)
  val codeNotFound_closed_static_closed_static_suggested = expectationVsList(DetectionCodes.CodeNotFound, List(valueSet(Some(Stability.Static), Some(Extensibility.Closed)),valueSet(Some(Stability.Static), Some(Extensibility.Closed))), Some(BindingStrength.S), ALERT)

  //-- use case two : all closed, all dynamic
  val codeNotFound_closed_dynamic_closed_dynamic_required = expectationVsList(DetectionCodes.CodeNotFound, List(valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed)),valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed))), Some(BindingStrength.R), ERROR)
  val codeNotFound_closed_dynamic_closed_dynamic_suggested = expectationVsList(DetectionCodes.CodeNotFound, List(valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed)),valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed))), Some(BindingStrength.S), ALERT)

  //-- use case three : all closed, static/dynamic mix
  val codeNotFound_closed_dynamic_closed_static_required = expectationVsList(DetectionCodes.CodeNotFound, List(valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed)),valueSet(Some(Stability.Static), Some(Extensibility.Closed))), Some(BindingStrength.R), ERROR)
  val codeNotFound_closed_dynamic_closed_static_suggested = expectationVsList(DetectionCodes.CodeNotFound, List(valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed)),valueSet(Some(Stability.Static), Some(Extensibility.Closed))), Some(BindingStrength.S), ALERT)

  //-- use case four : all open, all static
  val codeNotFound_open_static_open_static_required = expectationVsList(DetectionCodes.CodeNotFound, List(valueSet(Some(Stability.Static), Some(Extensibility.Open)),valueSet(Some(Stability.Static), Some(Extensibility.Open))), Some(BindingStrength.R), ERROR)
  val codeNotFound_open_static_open_static_suggested = expectationVsList(DetectionCodes.CodeNotFound, List(valueSet(Some(Stability.Static), Some(Extensibility.Open)),valueSet(Some(Stability.Static), Some(Extensibility.Open))), Some(BindingStrength.S), ALERT)

  //-- use case five : all open, all dynamic
  val codeNotFound_open_dynamic_open_dynamic_required = expectationVsList(DetectionCodes.CodeNotFound, List(valueSet(Some(Stability.Dynamic), Some(Extensibility.Open)),valueSet(Some(Stability.Dynamic), Some(Extensibility.Open))), Some(BindingStrength.R), ERROR)
  val codeNotFound_open_dynamic_open_dynamic_suggested = expectationVsList(DetectionCodes.CodeNotFound, List(valueSet(Some(Stability.Dynamic), Some(Extensibility.Open)),valueSet(Some(Stability.Dynamic), Some(Extensibility.Open))), Some(BindingStrength.S), ALERT)

  //-- use case six : all open, static/dynamic mix
  val codeNotFound_open_dynamic_open_static_required = expectationVsList(DetectionCodes.CodeNotFound, List(valueSet(Some(Stability.Dynamic), Some(Extensibility.Open)),valueSet(Some(Stability.Static), Some(Extensibility.Open))), Some(BindingStrength.R), ERROR)
  val codeNotFound_open_dynamic_open_static_suggested = expectationVsList(DetectionCodes.CodeNotFound, List(valueSet(Some(Stability.Dynamic), Some(Extensibility.Open)),valueSet(Some(Stability.Static), Some(Extensibility.Open))), Some(BindingStrength.S), ALERT)

  //-- use case seven : open/closed mix, all static
  val codeNotFound_open_static_closed_static_required = expectationVsList(DetectionCodes.CodeNotFound, List(valueSet(Some(Stability.Static), Some(Extensibility.Open)),valueSet(Some(Stability.Static), Some(Extensibility.Closed))), Some(BindingStrength.R), ERROR)
  val codeNotFound_open_static_closed_static_suggested = expectationVsList(DetectionCodes.CodeNotFound, List(valueSet(Some(Stability.Static), Some(Extensibility.Open)),valueSet(Some(Stability.Static), Some(Extensibility.Closed))), Some(BindingStrength.S), ALERT)

  //-- use case eight : open/closed mix, all dynamic
  val codeNotFound_open_dynamic_closed_dynamic_required = expectationVsList(DetectionCodes.CodeNotFound, List(valueSet(Some(Stability.Dynamic), Some(Extensibility.Open)),valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed))), Some(BindingStrength.R), ERROR)
  val codeNotFound_open_dynamic_closed_dynamic_suggested = expectationVsList(DetectionCodes.CodeNotFound, List(valueSet(Some(Stability.Dynamic), Some(Extensibility.Open)),valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed))), Some(BindingStrength.S), ALERT)

  //-- use case nine : open/closed mix, static/dynamic mix
  val codeNotFound_open_static_closed_dynamic_required = expectationVsList(DetectionCodes.CodeNotFound, List(valueSet(Some(Stability.Static), Some(Extensibility.Open)),valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed))), Some(BindingStrength.R), ERROR)
  val codeNotFound_open_static_closed_dynamic_suggested = expectationVsList(DetectionCodes.CodeNotFound, List(valueSet(Some(Stability.Static), Some(Extensibility.Open)),valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed))), Some(BindingStrength.S), ALERT)
  val codeNotFound_open_dynamic_closed_static_required = expectationVsList(DetectionCodes.CodeNotFound, List(valueSet(Some(Stability.Dynamic), Some(Extensibility.Open)),valueSet(Some(Stability.Static), Some(Extensibility.Closed))), Some(BindingStrength.R), ERROR)
  val codeNotFound_open_dynamic_closed_static_suggested = expectationVsList(DetectionCodes.CodeNotFound, List(valueSet(Some(Stability.Dynamic), Some(Extensibility.Open)),valueSet(Some(Stability.Static), Some(Extensibility.Closed))), Some(BindingStrength.S), ALERT)

  //-- DuplicateCodeAndCodeSystemInValueSetList - multiple value sets
  //-- use case one : all closed, all static
  val dup_C_CS_list_closed_static_closed_static_required = expectationVsList(DetectionCodes.DuplicateCodeAndCodeSystemInValueSetList, List(valueSet(Some(Stability.Static), Some(Extensibility.Closed)),valueSet(Some(Stability.Static), Some(Extensibility.Closed))), Some(BindingStrength.R), SPEC_ERROR)
  val dup_C_CS_list_closed_static_closed_static_suggested = expectationVsList(DetectionCodes.DuplicateCodeAndCodeSystemInValueSetList, List(valueSet(Some(Stability.Static), Some(Extensibility.Closed)),valueSet(Some(Stability.Static), Some(Extensibility.Closed))), Some(BindingStrength.S), SPEC_ERROR)

  //-- use case two : all closed, all dynamic
  val dup_C_CS_list_closed_dynamic_closed_dynamic_required = expectationVsList(DetectionCodes.DuplicateCodeAndCodeSystemInValueSetList, List(valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed)),valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed))), Some(BindingStrength.R), SPEC_ERROR)
  val dup_C_CS_list_closed_dynamic_closed_dynamic_suggested = expectationVsList(DetectionCodes.DuplicateCodeAndCodeSystemInValueSetList, List(valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed)),valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed))), Some(BindingStrength.S), SPEC_ERROR)

  //-- use case three : all closed, static/dynamic mix
  val dup_C_CS_list_closed_dynamic_closed_static_required = expectationVsList(DetectionCodes.DuplicateCodeAndCodeSystemInValueSetList, List(valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed)),valueSet(Some(Stability.Static), Some(Extensibility.Closed))), Some(BindingStrength.R), SPEC_ERROR)
  val dup_C_CS_list_closed_dynamic_closed_static_suggested = expectationVsList(DetectionCodes.DuplicateCodeAndCodeSystemInValueSetList, List(valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed)),valueSet(Some(Stability.Static), Some(Extensibility.Closed))), Some(BindingStrength.S), SPEC_ERROR)

  //-- use case four : all open, all static
  val dup_C_CS_list_open_static_open_static_required = expectationVsList(DetectionCodes.DuplicateCodeAndCodeSystemInValueSetList, List(valueSet(Some(Stability.Static), Some(Extensibility.Open)),valueSet(Some(Stability.Static), Some(Extensibility.Open))), Some(BindingStrength.R), SPEC_ERROR)
  val dup_C_CS_list_open_static_open_static_suggested = expectationVsList(DetectionCodes.DuplicateCodeAndCodeSystemInValueSetList, List(valueSet(Some(Stability.Static), Some(Extensibility.Open)),valueSet(Some(Stability.Static), Some(Extensibility.Open))), Some(BindingStrength.S), SPEC_ERROR)

  //-- use case five : all open, all dynamic
  val dup_C_CS_list_open_dynamic_open_dynamic_required = expectationVsList(DetectionCodes.DuplicateCodeAndCodeSystemInValueSetList, List(valueSet(Some(Stability.Dynamic), Some(Extensibility.Open)),valueSet(Some(Stability.Dynamic), Some(Extensibility.Open))), Some(BindingStrength.R), SPEC_ERROR)
  val dup_C_CS_list_open_dynamic_open_dynamic_suggested = expectationVsList(DetectionCodes.DuplicateCodeAndCodeSystemInValueSetList, List(valueSet(Some(Stability.Dynamic), Some(Extensibility.Open)),valueSet(Some(Stability.Dynamic), Some(Extensibility.Open))), Some(BindingStrength.S), SPEC_ERROR)

  //-- use case six : all open, static/dynamic mix
  val dup_C_CS_list_open_dynamic_open_static_required = expectationVsList(DetectionCodes.DuplicateCodeAndCodeSystemInValueSetList, List(valueSet(Some(Stability.Dynamic), Some(Extensibility.Open)),valueSet(Some(Stability.Static), Some(Extensibility.Open))), Some(BindingStrength.R), SPEC_ERROR)
  val dup_C_CS_list_open_dynamic_open_static_suggested = expectationVsList(DetectionCodes.DuplicateCodeAndCodeSystemInValueSetList, List(valueSet(Some(Stability.Dynamic), Some(Extensibility.Open)),valueSet(Some(Stability.Static), Some(Extensibility.Open))), Some(BindingStrength.S), SPEC_ERROR)

  //-- use case seven : open/closed mix, all static
  val dup_C_CS_list_open_static_closed_static_required = expectationVsList(DetectionCodes.DuplicateCodeAndCodeSystemInValueSetList, List(valueSet(Some(Stability.Static), Some(Extensibility.Open)),valueSet(Some(Stability.Static), Some(Extensibility.Closed))), Some(BindingStrength.R), SPEC_ERROR)
  val dup_C_CS_list_open_static_closed_static_suggested = expectationVsList(DetectionCodes.DuplicateCodeAndCodeSystemInValueSetList, List(valueSet(Some(Stability.Static), Some(Extensibility.Open)),valueSet(Some(Stability.Static), Some(Extensibility.Closed))), Some(BindingStrength.S), SPEC_ERROR)

  //-- use case eight : open/closed mix, all dynamic
  val dup_C_CS_list_open_dynamic_closed_dynamic_required = expectationVsList(DetectionCodes.DuplicateCodeAndCodeSystemInValueSetList, List(valueSet(Some(Stability.Dynamic), Some(Extensibility.Open)),valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed))), Some(BindingStrength.R), SPEC_ERROR)
  val dup_C_CS_list_open_dynamic_closed_dynamic_suggested = expectationVsList(DetectionCodes.DuplicateCodeAndCodeSystemInValueSetList, List(valueSet(Some(Stability.Dynamic), Some(Extensibility.Open)),valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed))), Some(BindingStrength.S), SPEC_ERROR)

  //-- use case nine : open/closed mix, static/dynamic mix
  val dup_C_CS_list_open_static_closed_dynamic_required = expectationVsList(DetectionCodes.DuplicateCodeAndCodeSystemInValueSetList, List(valueSet(Some(Stability.Static), Some(Extensibility.Open)),valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed))), Some(BindingStrength.R), SPEC_ERROR)
  val dup_C_CS_list_open_static_closed_dynamic_suggested = expectationVsList(DetectionCodes.DuplicateCodeAndCodeSystemInValueSetList, List(valueSet(Some(Stability.Static), Some(Extensibility.Open)),valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed))), Some(BindingStrength.S), SPEC_ERROR)
  val dup_C_CS_list_open_dynamic_closed_static_required = expectationVsList(DetectionCodes.DuplicateCodeAndCodeSystemInValueSetList, List(valueSet(Some(Stability.Dynamic), Some(Extensibility.Open)),valueSet(Some(Stability.Static), Some(Extensibility.Closed))), Some(BindingStrength.R), SPEC_ERROR)
  val dup_C_CS_list_open_dynamic_closed_static_suggested = expectationVsList(DetectionCodes.DuplicateCodeAndCodeSystemInValueSetList, List(valueSet(Some(Stability.Dynamic), Some(Extensibility.Open)),valueSet(Some(Stability.Static), Some(Extensibility.Closed))), Some(BindingStrength.S), SPEC_ERROR)

  //--UBS : should be independent from value set metadata - always ALERT
  val ubs_closed_dynamic_required: MatchResult[String] = expectationSingleVs(DetectionCodes.UBS, valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed)), Some(BindingStrength.R), ALERT)
  val ubs_open_dynamic_required: MatchResult[String] = expectationSingleVs(DetectionCodes.UBS, valueSet(Some(Stability.Dynamic), Some(Extensibility.Open)), Some(BindingStrength.R), ALERT)
  val ubs_closed_static_required: MatchResult[String] = expectationSingleVs(DetectionCodes.UBS, valueSet(Some(Stability.Static), Some(Extensibility.Closed)), Some(BindingStrength.R), ALERT)
  val ubs_open_static_required: MatchResult[String] = expectationSingleVs(DetectionCodes.UBS, valueSet(Some(Stability.Static), Some(Extensibility.Open)), Some(BindingStrength.R), ALERT)

  val ubs_closed_dynamic_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.UBS, valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed)), Some(BindingStrength.S), ALERT)
  val ubs_open_dynamic_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.UBS, valueSet(Some(Stability.Dynamic), Some(Extensibility.Open)), Some(BindingStrength.S), ALERT)
  val ubs_closed_static_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.UBS, valueSet(Some(Stability.Static), Some(Extensibility.Closed)), Some(BindingStrength.S), ALERT)
  val ubs_open_static_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.UBS, valueSet(Some(Stability.Static), Some(Extensibility.Open)), Some(BindingStrength.S), ALERT)

  //--VSExcluded : should be independent from value set metadata - always ALERT
  val vs_excluded_closed_dynamic_required: MatchResult[String] = expectationSingleVs(DetectionCodes.VSExcluded, valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed)), Some(BindingStrength.R), ALERT)
  val vs_excluded_open_dynamic_required: MatchResult[String] = expectationSingleVs(DetectionCodes.VSExcluded, valueSet(Some(Stability.Dynamic), Some(Extensibility.Open)), Some(BindingStrength.R), ALERT)
  val vs_excluded_closed_static_required: MatchResult[String] = expectationSingleVs(DetectionCodes.VSExcluded, valueSet(Some(Stability.Static), Some(Extensibility.Closed)), Some(BindingStrength.R), ALERT)
  val vs_excluded_open_static_required: MatchResult[String] = expectationSingleVs(DetectionCodes.VSExcluded, valueSet(Some(Stability.Static), Some(Extensibility.Open)), Some(BindingStrength.R), ALERT)

  val vs_excluded_closed_dynamic_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.VSExcluded, valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed)), Some(BindingStrength.S), ALERT)
  val vs_excluded_open_dynamic_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.VSExcluded, valueSet(Some(Stability.Dynamic), Some(Extensibility.Open)), Some(BindingStrength.S), ALERT)
  val vs_excluded_closed_static_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.VSExcluded, valueSet(Some(Stability.Static), Some(Extensibility.Closed)), Some(BindingStrength.S), ALERT)
  val vs_excluded_open_static_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.VSExcluded, valueSet(Some(Stability.Static), Some(Extensibility.Open)), Some(BindingStrength.S), ALERT)

  //--UsageAndExtensibilityNotCompatible : only for P usage codes in CLOSED value sets
  val usage_extensibility_closed_dynamic_required: MatchResult[String] = expectationSingleVs(DetectionCodes.UsageAndExtensibilityNotCompatible, valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed)), Some(BindingStrength.R), ALERT)
  val usage_extensibility_closed_static_required: MatchResult[String] = expectationSingleVs(DetectionCodes.UsageAndExtensibilityNotCompatible, valueSet(Some(Stability.Static), Some(Extensibility.Closed)), Some(BindingStrength.R), ALERT)

  val usage_extensibility_closed_dynamic_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.UsageAndExtensibilityNotCompatible, valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed)), Some(BindingStrength.S), ALERT)
  val usage_extensibility_closed_static_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.UsageAndExtensibilityNotCompatible, valueSet(Some(Stability.Static), Some(Extensibility.Closed)), Some(BindingStrength.S), ALERT)

  //--InvalidBindingLocation
  val invalid_bl_closed_dynamic_required: MatchResult[String] = expectationSingleVs(DetectionCodes.InvalidBindingLocation, valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed)), Some(BindingStrength.R), SPEC_ERROR)
  val invalid_bl_open_dynamic_required: MatchResult[String] = expectationSingleVs(DetectionCodes.InvalidBindingLocation, valueSet(Some(Stability.Dynamic), Some(Extensibility.Open)), Some(BindingStrength.R), SPEC_ERROR)
  val invalid_bl_closed_static_required: MatchResult[String] = expectationSingleVs(DetectionCodes.InvalidBindingLocation, valueSet(Some(Stability.Static), Some(Extensibility.Closed)), Some(BindingStrength.R), SPEC_ERROR)
  val invalid_bl_open_static_required: MatchResult[String] = expectationSingleVs(DetectionCodes.InvalidBindingLocation, valueSet(Some(Stability.Static), Some(Extensibility.Open)), Some(BindingStrength.R), SPEC_ERROR)

  val invalid_bl_closed_dynamic_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.InvalidBindingLocation, valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed)), Some(BindingStrength.S), SPEC_ERROR)
  val invalid_bl_open_dynamic_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.InvalidBindingLocation, valueSet(Some(Stability.Dynamic), Some(Extensibility.Open)), Some(BindingStrength.S), SPEC_ERROR)
  val invalid_bl_closed_static_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.InvalidBindingLocation, valueSet(Some(Stability.Static), Some(Extensibility.Closed)), Some(BindingStrength.S), SPEC_ERROR)
  val invalid_bl_open_static_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.InvalidBindingLocation, valueSet(Some(Stability.Static), Some(Extensibility.Open)), Some(BindingStrength.S), SPEC_ERROR)

  //--MultiVSForPrimitive : only for primitive datatypes that have multiple VS
  val multi_vs_closed_dynamic_required: MatchResult[String] = expectationSingleVs(DetectionCodes.MultiVSForPrimitive, valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed)), Some(BindingStrength.R), ALERT)
  val multi_vs_open_dynamic_required: MatchResult[String] = expectationSingleVs(DetectionCodes.MultiVSForPrimitive, valueSet(Some(Stability.Dynamic), Some(Extensibility.Open)), Some(BindingStrength.R), ALERT)
  val multi_vs_closed_static_required: MatchResult[String] = expectationSingleVs(DetectionCodes.MultiVSForPrimitive, valueSet(Some(Stability.Static), Some(Extensibility.Closed)), Some(BindingStrength.R), ALERT)
  val multi_vs_open_static_required: MatchResult[String] = expectationSingleVs(DetectionCodes.MultiVSForPrimitive, valueSet(Some(Stability.Static), Some(Extensibility.Open)), Some(BindingStrength.R), ALERT)

  val multi_vs_closed_dynamic_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.MultiVSForPrimitive, valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed)), Some(BindingStrength.S), ALERT)
  val multi_vs_open_dynamic_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.MultiVSForPrimitive, valueSet(Some(Stability.Dynamic), Some(Extensibility.Open)), Some(BindingStrength.S), ALERT)
  val multi_vs_closed_static_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.MultiVSForPrimitive, valueSet(Some(Stability.Static), Some(Extensibility.Closed)), Some(BindingStrength.S), ALERT)
  val multi_vs_open_static_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.MultiVSForPrimitive, valueSet(Some(Stability.Static), Some(Extensibility.Open)), Some(BindingStrength.S), ALERT)

  //--VSNotFound
  val vs_not_found_required: MatchResult[String] = expectationNoVs(DetectionCodes.VSNotFound, Some(BindingStrength.R), ALERT)
  val vs_not_found_suggested: MatchResult[String] = expectationNoVs(DetectionCodes.VSNotFound, Some(BindingStrength.S), ALERT)

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
