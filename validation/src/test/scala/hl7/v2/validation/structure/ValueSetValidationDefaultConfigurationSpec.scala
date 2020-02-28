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

  val DynamicVS: structure.DetectionCodes.Value = Value("DynamicVS")
  val XOR: structure.DetectionCodes.Value = Value("XOR")
  val CodeNotFound: structure.DetectionCodes.Value = Value("CodeNotFound")
  val PVS: structure.DetectionCodes.Value = Value("PVS")
  val RVS: structure.DetectionCodes.Value = Value("RVS")
  val EVS: structure.DetectionCodes.Value = Value("EVS")
  val NoUsageVS: structure.DetectionCodes.Value = Value("NoUsageVS")
  val InvalidCodeSys: structure.DetectionCodes.Value = Value("InvalidCodeSys")
  val UBS: structure.DetectionCodes.Value = Value("UBS")
  val VSNotFound: structure.DetectionCodes.Value = Value("VSNotFound")
  val VSExcluded: structure.DetectionCodes.Value = Value("VSExcluded")
  val EmptyVS: structure.DetectionCodes.Value = Value("EmptyVS")
  val DuplicateCode: structure.DetectionCodes.Value = Value("DuplicateCode")
  val DuplicateCodeAndCodeSystem: structure.DetectionCodes.Value = Value("DuplicateCodeAndCodeSystem")
  val DuplicateCodeAndCodeSystemInValueSetList: structure.DetectionCodes.Value = Value("DuplicateCodeAndCodeSystemInValueSetList")
  val UsageAndExtensibilityNotCompatible: structure.DetectionCodes.Value = Value("UsageAndExtensibilityNotCompatible")
  val InvalidBindingLocation: structure.DetectionCodes.Value = Value("InvalidBindingLocation")
  val MultiVSForPrimitive: structure.DetectionCodes.Value = Value("MultiVSForPrimitive")
}

trait ValueSetValidationDefaultConfigurationSpec extends Specification
  with hl7.v2.validation.vs.DefaultValueSetValidator
  with Helpers {
  def is =s2"""
      PVS Detection when a value set is Closed/ Static and Binding Strength is Required should be ERROR $pvs_closed_static_required
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
  val pvs_closed_static_required: MatchResult[String] = expectationSingleVs(DetectionCodes.PVS, valueSet(Some(Stability.Static), Some(Extensibility.Closed)), Some(BindingStrength.R), ERROR)




















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
