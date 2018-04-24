package expression

import hl7.v2.instance.{TimeZone, Separators, Element}
import hl7.v2.validation.vs.ValueSetLibrary
import hl7.v2.validation.report.ConfigurableDetections
import hl7.v2.validation.vs.Validator

trait Evaluator {

  /**
    * Evaluates the expression within the specified context
    * and returns the result
    * @param e - The expression to be evaluated
    * @param c - The context node
    * @param l - The value set library
    * @param s - The message separators
    * @param t - The default Time Zone
    * @return The evaluation result
    */
  def eval(e: Expression, c: Element)(implicit l: ValueSetLibrary, s: Separators,
                                      t: Option[TimeZone], VSValidator : Validator): EvalResult
}