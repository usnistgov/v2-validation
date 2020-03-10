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
  val CodeNotFound: structure.DetectionCodes.Value = Value("CodeNotFound")
  val PVS: structure.DetectionCodes.Value = Value("PVS")//done
  val RVS: structure.DetectionCodes.Value = Value("RVS")//done
  val EVS: structure.DetectionCodes.Value = Value("EVS")//done
  val NoUsageVS: structure.DetectionCodes.Value = Value("NoUsageVS")//done
  val InvalidCodeSys: structure.DetectionCodes.Value = Value("InvalidCodeSys")//done
  val UBS: structure.DetectionCodes.Value = Value("UBS")
  val VSNotFound: structure.DetectionCodes.Value = Value("VSNotFound")
  val VSExcluded: structure.DetectionCodes.Value = Value("VSExcluded")
  val EmptyVS: structure.DetectionCodes.Value = Value("EmptyVS")
  val DuplicateCode: structure.DetectionCodes.Value = Value("DuplicateCode")//done
  val DuplicateCodeAndCodeSystem: structure.DetectionCodes.Value = Value("DuplicateCodeAndCodeSystem")//done
  val DuplicateCodeAndCodeSystemInValueSetList: structure.DetectionCodes.Value = Value("DuplicateCodeAndCodeSystemInValueSetList")
  val UsageAndExtensibilityNotCompatible: structure.DetectionCodes.Value = Value("UsageAndExtensibilityNotCompatible")
  val InvalidBindingLocation: structure.DetectionCodes.Value = Value("InvalidBindingLocation")
  val MultiVSForPrimitive: structure.DetectionCodes.Value = Value("MultiVSForPrimitive")
}

trait ValueSetValidationDefaultConfigurationSpec extends Specification
  with hl7.v2.validation.vs.DefaultValueSetValidator
  with Helpers {
  def is =s2"""
      
      DynamicVS Detection when a value set is Closed/ Dynamic and Binding Strength is Required should be ALERT $dynamicVS_closed_dynamic_required
      DynamicVS Detection when a value set is Open/ Dynamic and Binding Strength is Required should be ALERT $dynamicVS_open_dynamic_required
      DynamicVS Detection when a value set is Closed/ Dynamic and Binding Strength is Suggested should be ALERT $dynamicVS_closed_dynamic_suggested
      DynamicVS Detection when a value set is Open/ Dynamic and Binding Strength is Suggested should be ALERT $dynamicVS_open_dynamic_suggested     

      XOR Detection when a value set is Closed/ Dynamic and Binding Strength is Required should be HIGH_ALERT $xor_closed_dynamic_required
      XOR Detection when a value set is Open/ Dynamic and Binding Strength is Required should be HIGH_ALERT $xor_open_dynamic_required
      XOR Detection when a value set is Closed/ Static and Binding Strength is Required should be HIGH_ALERT $xor_closed_static_required
      XOR Detection when a value set is Open/ Static and Binding Strength is Required should be HIGH_ALERT $xor_open_static_required

      XOR Detection when a value set is Closed/ Dynamic and Binding Strength is Suggested should be ALERT $xor_closed_dynamic_suggested
      XOR Detection when a value set is Open/ Dynamic and Binding Strength is Suggested should be ALERT $xor_open_dynamic_suggested
      XOR Detection when a value set is Closed/ Static and Binding Strength is Suggested should be ALERT $xor_closed_static_suggested
      XOR Detection when a value set is Open/ Static and Binding Strength is Suggested should be ALERT $xor_open_static_suggested
      
      NoUsageVS Detection when a value set is Closed/ Dynamic and Binding Strength is Required should be AFFIRMATIVE $no_usage_closed_dynamic_required
      NoUsageVS Detection when a value set is Open/ Dynamic and Binding Strength is Required should be AFFIRMATIVE $no_usage_open_dynamic_required
      NoUsageVS Detection when a value set is Closed/ Static and Binding Strength is Required should be AFFIRMATIVE $no_usage_closed_static_required
      NoUsageVS Detection when a value set is Open/ Static and Binding Strength is Required should be AFFIRMATIVE $no_usage_open_static_required

      NoUsageVS Detection when a value set is Closed/ Dynamic and Binding Strength is Suggested should be AFFIRMATIVE $no_usage_closed_dynamic_suggested
      NoUsageVS Detection when a value set is Open/ Dynamic and Binding Strength is Suggested should be AFFIRMATIVE $no_usage_open_dynamic_suggested
      NoUsageVS Detection when a value set is Closed/ Static and Binding Strength is Suggested should be AFFIRMATIVE $no_usage_closed_static_suggested
      NoUsageVS Detection when a value set is Open/ Static and Binding Strength is Suggested should be AFFIRMATIVE $no_usage_open_static_suggested
      
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
      
      DuplicateCodeAndCodeSystem Detection when a value set is Closed/ Dynamic and Binding Strength is Required should be ALERT $dup_C_CS_closed_dynamic_required
      DuplicateCodeAndCodeSystem Detection when a value set is Open/ Dynamic and Binding Strength is Required should be ALERT $dup_C_CS_open_dynamic_required
      DuplicateCodeAndCodeSystem Detection when a value set is Closed/ Static and Binding Strength is Required should be ALERT $dup_C_CS_closed_static_required
      DuplicateCodeAndCodeSystem Detection when a value set is Open/ Static and Binding Strength is Required should be ALERT $dup_C_CS_open_static_required
      
      DuplicateCodeAndCodeSystem Detection when a value set is Closed/ Dynamic and Binding Strength is Suggested should be ALERT $dup_C_CS_closed_dynamic_suggested
      DuplicateCodeAndCodeSystem Detection when a value set is Open/ Dynamic and Binding Strength is Suggested should be ALERT $dup_C_CS_open_dynamic_suggested
      DuplicateCodeAndCodeSystem Detection when a value set is Closed/ Static and Binding Strength is Suggested should be ALERT $dup_C_CS_closed_static_suggested
      DuplicateCodeAndCodeSystem Detection when a value set is Open/ Static and Binding Strength is Suggested should be ALERT $dup_C_CS_open_static_suggested
      
      DuplicateCode Detection when a value set is Closed/ Dynamic and Binding Strength is Required should be ALERT $dup_C_closed_dynamic_required
      DuplicateCode Detection when a value set is Open/ Dynamic and Binding Strength is Required should be ALERT $dup_C_open_dynamic_required
      DuplicateCode Detection when a value set is Closed/ Static and Binding Strength is Required should be ALERT $dup_C_closed_static_required
      DuplicateCode Detection when a value set is Open/ Static and Binding Strength is Required should be ALERT $dup_C_open_static_required
      
      DuplicateCode Detection when a value set is Closed/ Dynamic and Binding Strength is Suggested should be ALERT $dup_C_closed_dynamic_suggested
      DuplicateCode Detection when a value set is Open/ Dynamic and Binding Strength is Suggested should be ALERT $dup_C_open_dynamic_suggested
      DuplicateCode Detection when a value set is Closed/ Static and Binding Strength is Suggested should be ALERT $dup_C_closed_static_suggested
      DuplicateCode Detection when a value set is Open/ Static and Binding Strength is Suggested should be ALERT $dup_C_open_static_suggested
      
      InvalidCodeSys Detection when a value set is Closed/ Dynamic and Binding Strength is Required should be ALERT $invalid_CS_closed_dynamic_required
      InvalidCodeSys Detection when a value set is Open/ Dynamic and Binding Strength is Required should be ALERT $invalid_CS_open_dynamic_required
      InvalidCodeSys Detection when a value set is Closed/ Static and Binding Strength is Required should be ALERT $invalid_CS_closed_static_required
      InvalidCodeSys Detection when a value set is Open/ Static and Binding Strength is Required should be ALERT $invalid_CS_open_static_required
      
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
     
    """

  //------- MOCKS
  val valueSetLibrary = new ValueSetLibraryImpl(Nil, Map())
  val vsSpecification: ValueSetSpecification = EmptyValueSetSpecification
  val elm: S = S(1, 1, Text("A"))
  val _default: Config = ConfigFactory.load()

  val ERROR: String = _default.getString("report.classification.error")
  val WARNING: String = _default.getString("report.classification.warning")
  val ALERT: String = _default.getString("report.classification.alert")
  val HIGH_ALERT: String = _default.getString("report.classification.high-alert")
  val INFO: String = _default.getString("report.classification.informational")
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
  val xor_closed_dynamic_required: MatchResult[String] = expectationSingleVs(DetectionCodes.XOR, valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed)), Some(BindingStrength.R), HIGH_ALERT)
  val xor_open_dynamic_required: MatchResult[String] = expectationSingleVs(DetectionCodes.XOR, valueSet(Some(Stability.Dynamic), Some(Extensibility.Open)), Some(BindingStrength.R), HIGH_ALERT)
  val xor_closed_static_required: MatchResult[String] = expectationSingleVs(DetectionCodes.XOR, valueSet(Some(Stability.Static), Some(Extensibility.Closed)), Some(BindingStrength.R), HIGH_ALERT)
  val xor_open_static_required: MatchResult[String] = expectationSingleVs(DetectionCodes.XOR, valueSet(Some(Stability.Static), Some(Extensibility.Open)), Some(BindingStrength.R), HIGH_ALERT)
  
  val xor_closed_dynamic_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.XOR, valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed)), Some(BindingStrength.S), ALERT)
  val xor_open_dynamic_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.XOR, valueSet(Some(Stability.Dynamic), Some(Extensibility.Open)), Some(BindingStrength.S), ALERT)
  val xor_closed_static_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.XOR, valueSet(Some(Stability.Static), Some(Extensibility.Closed)), Some(BindingStrength.S), ALERT)
  val xor_open_static_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.XOR, valueSet(Some(Stability.Static), Some(Extensibility.Open)), Some(BindingStrength.S), ALERT)
  // XOR should not be triggered for a binding strength of U
  
  //--NoUsageVS
  val no_usage_closed_dynamic_required: MatchResult[String] = expectationSingleVs(DetectionCodes.NoUsageVS, valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed)), Some(BindingStrength.R), AFFIRMATIVE)
  val no_usage_open_dynamic_required: MatchResult[String] = expectationSingleVs(DetectionCodes.NoUsageVS, valueSet(Some(Stability.Dynamic), Some(Extensibility.Open)), Some(BindingStrength.R), AFFIRMATIVE)
  val no_usage_closed_static_required: MatchResult[String] = expectationSingleVs(DetectionCodes.NoUsageVS, valueSet(Some(Stability.Static), Some(Extensibility.Closed)), Some(BindingStrength.R), AFFIRMATIVE)
  val no_usage_open_static_required: MatchResult[String] = expectationSingleVs(DetectionCodes.NoUsageVS, valueSet(Some(Stability.Static), Some(Extensibility.Open)), Some(BindingStrength.R), AFFIRMATIVE)
  
  val no_usage_closed_dynamic_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.NoUsageVS, valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed)), Some(BindingStrength.S), AFFIRMATIVE)
  val no_usage_open_dynamic_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.NoUsageVS, valueSet(Some(Stability.Dynamic), Some(Extensibility.Open)), Some(BindingStrength.S), AFFIRMATIVE)
  val no_usage_closed_static_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.NoUsageVS, valueSet(Some(Stability.Static), Some(Extensibility.Closed)), Some(BindingStrength.S), AFFIRMATIVE)
  val no_usage_open_static_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.NoUsageVS, valueSet(Some(Stability.Static), Some(Extensibility.Open)), Some(BindingStrength.S), AFFIRMATIVE)
  
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
  
  val pvs_closed_dynamic_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.PVS, valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed)), Some(BindingStrength.R), ALERT)
  val pvs_open_dynamic_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.PVS, valueSet(Some(Stability.Dynamic), Some(Extensibility.Open)), Some(BindingStrength.R), ALERT)
  val pvs_closed_static_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.PVS, valueSet(Some(Stability.Static), Some(Extensibility.Closed)), Some(BindingStrength.R), ALERT)
  val pvs_open_static_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.PVS, valueSet(Some(Stability.Static), Some(Extensibility.Open)), Some(BindingStrength.R), ALERT)
  
  //--RVS
  val rvs_closed_dynamic_required: MatchResult[String] = expectationSingleVs(DetectionCodes.RVS, valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed)), Some(BindingStrength.R), AFFIRMATIVE)
  val rvs_open_dynamic_required: MatchResult[String] = expectationSingleVs(DetectionCodes.RVS, valueSet(Some(Stability.Dynamic), Some(Extensibility.Open)), Some(BindingStrength.R), AFFIRMATIVE)
  val rvs_closed_static_required: MatchResult[String] = expectationSingleVs(DetectionCodes.RVS, valueSet(Some(Stability.Static), Some(Extensibility.Closed)), Some(BindingStrength.R), AFFIRMATIVE)
  val rvs_open_static_required: MatchResult[String] = expectationSingleVs(DetectionCodes.RVS, valueSet(Some(Stability.Static), Some(Extensibility.Open)), Some(BindingStrength.R), AFFIRMATIVE)
  
  val rvs_closed_dynamic_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.RVS, valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed)), Some(BindingStrength.R), ALERT)
  val rvs_open_dynamic_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.RVS, valueSet(Some(Stability.Dynamic), Some(Extensibility.Open)), Some(BindingStrength.R), ALERT)
  val rvs_closed_static_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.RVS, valueSet(Some(Stability.Static), Some(Extensibility.Closed)), Some(BindingStrength.R), ALERT)
  val rvs_open_static_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.RVS, valueSet(Some(Stability.Static), Some(Extensibility.Open)), Some(BindingStrength.R), ALERT)
  
  //--DuplicateCodeAndCodeSystem
  val dup_C_CS_closed_dynamic_required: MatchResult[String] = expectationSingleVs(DetectionCodes.DuplicateCodeAndCodeSystem, valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed)), Some(BindingStrength.R), ALERT)
  val dup_C_CS_open_dynamic_required: MatchResult[String] = expectationSingleVs(DetectionCodes.DuplicateCodeAndCodeSystem, valueSet(Some(Stability.Dynamic), Some(Extensibility.Open)), Some(BindingStrength.R), ALERT)
  val dup_C_CS_closed_static_required: MatchResult[String] = expectationSingleVs(DetectionCodes.DuplicateCodeAndCodeSystem, valueSet(Some(Stability.Static), Some(Extensibility.Closed)), Some(BindingStrength.R), ALERT)
  val dup_C_CS_open_static_required: MatchResult[String] = expectationSingleVs(DetectionCodes.DuplicateCodeAndCodeSystem, valueSet(Some(Stability.Static), Some(Extensibility.Open)), Some(BindingStrength.R), ALERT)
  
  val dup_C_CS_closed_dynamic_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.DuplicateCodeAndCodeSystem, valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed)), Some(BindingStrength.R), ALERT)
  val dup_C_CS_open_dynamic_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.DuplicateCodeAndCodeSystem, valueSet(Some(Stability.Dynamic), Some(Extensibility.Open)), Some(BindingStrength.R), ALERT)
  val dup_C_CS_closed_static_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.DuplicateCodeAndCodeSystem, valueSet(Some(Stability.Static), Some(Extensibility.Closed)), Some(BindingStrength.R), ALERT)
  val dup_C_CS_open_static_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.DuplicateCodeAndCodeSystem, valueSet(Some(Stability.Static), Some(Extensibility.Open)), Some(BindingStrength.R), ALERT)
  
  //--DuplicateCode
  val dup_C_closed_dynamic_required: MatchResult[String] = expectationSingleVs(DetectionCodes.DuplicateCode, valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed)), Some(BindingStrength.R), ALERT)
  val dup_C_open_dynamic_required: MatchResult[String] = expectationSingleVs(DetectionCodes.DuplicateCode, valueSet(Some(Stability.Dynamic), Some(Extensibility.Open)), Some(BindingStrength.R), ALERT)
  val dup_C_closed_static_required: MatchResult[String] = expectationSingleVs(DetectionCodes.DuplicateCode, valueSet(Some(Stability.Static), Some(Extensibility.Closed)), Some(BindingStrength.R), ALERT)
  val dup_C_open_static_required: MatchResult[String] = expectationSingleVs(DetectionCodes.DuplicateCode, valueSet(Some(Stability.Static), Some(Extensibility.Open)), Some(BindingStrength.R), ALERT)
  
  val dup_C_closed_dynamic_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.DuplicateCode, valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed)), Some(BindingStrength.R), ALERT)
  val dup_C_open_dynamic_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.DuplicateCode, valueSet(Some(Stability.Dynamic), Some(Extensibility.Open)), Some(BindingStrength.R), ALERT)
  val dup_C_closed_static_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.DuplicateCode, valueSet(Some(Stability.Static), Some(Extensibility.Closed)), Some(BindingStrength.R), ALERT)
  val dup_C_open_static_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.DuplicateCode, valueSet(Some(Stability.Static), Some(Extensibility.Open)), Some(BindingStrength.R), ALERT)  

  //--InvalidCodeSys
  val invalid_CS_closed_dynamic_required: MatchResult[String] = expectationSingleVs(DetectionCodes.InvalidCodeSys, valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed)), Some(BindingStrength.R), HIGH_ALERT)
  val invalid_CS_open_dynamic_required: MatchResult[String] = expectationSingleVs(DetectionCodes.InvalidCodeSys, valueSet(Some(Stability.Dynamic), Some(Extensibility.Open)), Some(BindingStrength.R), HIGH_ALERT)
  val invalid_CS_closed_static_required: MatchResult[String] = expectationSingleVs(DetectionCodes.InvalidCodeSys, valueSet(Some(Stability.Static), Some(Extensibility.Closed)), Some(BindingStrength.R), ERROR)
  val invalid_CS_open_static_required: MatchResult[String] = expectationSingleVs(DetectionCodes.InvalidCodeSys, valueSet(Some(Stability.Static), Some(Extensibility.Open)), Some(BindingStrength.R), HIGH_ALERT)
  
  val invalid_CS_closed_dynamic_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.InvalidCodeSys, valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed)), Some(BindingStrength.R), ALERT)
  val invalid_CS_open_dynamic_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.InvalidCodeSys, valueSet(Some(Stability.Dynamic), Some(Extensibility.Open)), Some(BindingStrength.R), ALERT)
  val invalid_CS_closed_static_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.InvalidCodeSys, valueSet(Some(Stability.Static), Some(Extensibility.Closed)), Some(BindingStrength.R), ALERT)
  val invalid_CS_open_static_suggested: MatchResult[String] = expectationSingleVs(DetectionCodes.InvalidCodeSys, valueSet(Some(Stability.Static), Some(Extensibility.Open)), Some(BindingStrength.R), ALERT)
  
  //--CodeNotFound - single value set
  val codeNotFound_closed_dynamic_required = expectationVsList(DetectionCodes.CodeNotFound, List(valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed))), Some(BindingStrength.R), HIGH_ALERT)
  val codeNotFound_closed_static_required = expectationVsList(DetectionCodes.CodeNotFound, List(valueSet(Some(Stability.Static), Some(Extensibility.Closed))), Some(BindingStrength.R), ERROR)
  val codeNotFound_open_dynamic_required = expectationVsList(DetectionCodes.CodeNotFound, List(valueSet(Some(Stability.Dynamic), Some(Extensibility.Open))), Some(BindingStrength.R), HIGH_ALERT)
  val codeNotFound_open_static_required = expectationVsList(DetectionCodes.CodeNotFound, List(valueSet(Some(Stability.Static), Some(Extensibility.Open))), Some(BindingStrength.R), HIGH_ALERT)
  
  val codeNotFound_closed_dynamic_suggested = expectationVsList(DetectionCodes.CodeNotFound, List(valueSet(Some(Stability.Dynamic), Some(Extensibility.Closed))), Some(BindingStrength.S), ALERT)
  val codeNotFound_closed_static_suggested = expectationVsList(DetectionCodes.CodeNotFound, List(valueSet(Some(Stability.Static), Some(Extensibility.Closed))), Some(BindingStrength.S), ALERT)
  val codeNotFound_open_dynamic_suggested = expectationVsList(DetectionCodes.CodeNotFound, List(valueSet(Some(Stability.Dynamic), Some(Extensibility.Open))), Some(BindingStrength.S), ALERT)
  val codeNotFound_open_static_suggested = expectationVsList(DetectionCodes.CodeNotFound, List(valueSet(Some(Stability.Static), Some(Extensibility.Open))), Some(BindingStrength.S), ALERT)
    
  //-- CodeNotFound - multiple value sets
  
 



















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
      case DetectionCodes.PVS => VSValidationCode.PVS(PCode, vs)
      case DetectionCodes.EVS => VSValidationCode.EVS(ECode, vs)
      case DetectionCodes.RVS => VSValidationCode.RVS(RCode, vs)
      case DetectionCodes.NoUsageVS => VSValidationCode.NoUsage(NUCode, vs)
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
    val req: Req = Req(-1, "", Usage.O, None, None, None, Nil)
  }

  case class S(override val position: Int, instance: Int, value: Value)
    extends Simple  with Default

  case class C(override val position: Int, instance: Int, children: List[Element])
    extends Complex with Default
}
