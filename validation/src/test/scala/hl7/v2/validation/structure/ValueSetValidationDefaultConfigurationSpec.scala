package hl7.v2.validation.structure

import com.typesafe.config.{Config, ConfigFactory}
import hl7.v2.instance.{Complex, Element, Location, Simple, Text, Value}
import hl7.v2.profile.{BindingStrength, Req, Usage}
import hl7.v2.validation.vs.{Code, CodeUsage, EmptyValueSetSpecification, Extensibility, InternalValueSet, Stability, VSValidationCode, ValueSetLibraryImpl, ValueSetSpecification}
import org.specs2.Specification
import org.specs2.matcher.MatchResult

trait ValueSetValidationDefaultConfigurationSpec extends Specification
  with hl7.v2.validation.vs.DefaultValueSetValidator
  with Helpers {
  def is =s2"""
      PVS Detection when a value set is Closed/ Static and Binding Strength is Required should be ERROR $pvs_closed_static_required
    """

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

  //------ EXAMPLE
  val pvs_closed_static_required: MatchResult[String] = {
    val vs = valueSet(Some(Stability.Static), Some(Extensibility.Closed))
    val detection = VSValidationCode.PVS(PCode, vs)
    classificationIs(detection, Some(BindingStrength.R), ERROR)
  }

  def classificationIs(entry: VSValidationCode, strength: Option[BindingStrength], expected: String): MatchResult[String] = createEntry(elm, entry, strength).getClassification === expected
  def valueSet(stability: Option[Stability], extensibility: Option[Extensibility]): InternalValueSet = InternalValueSet("VS", extensibility, stability, Nil)

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
