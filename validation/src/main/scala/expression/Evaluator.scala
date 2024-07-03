package expression

import hl7.v2.instance.{Element, Separators, TimeZone}
import hl7.v2.validation.FeatureFlags
import hl7.v2.validation.report.ConfigurableDetections
import hl7.v2.validation.vs.VsValidator

trait Evaluator {

  /**
    Used to support legacy ValueSet "expression"
  */
  implicit def legacyExpressionValueSetValidator: VsValidator

  /**
    * Evaluates the expression within the specified context
    * and returns the result
    * @param e - The expression to be evaluated
    * @param c - The context node
    * @param s - The message separators
    * @param t - The default Time Zone
    * @return The evaluation result
    */
  def eval(e: Expression, c: Element)(implicit s: Separators, t: Option[TimeZone], detections: ConfigurableDetections, featureFlags: FeatureFlags): EvalResult
}