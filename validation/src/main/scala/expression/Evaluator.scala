package expression

import hl7.v2.instance.{TimeZone, Separators, Element}

trait Evaluator {

  /**
    * Evaluates the expression within the specified context
    * and returns the result
    * @param e - The expression to be evaluated
    * @param c - The context node
    * @param s - The message separators
    * @param t - The default Time Zone
    * @return The evaluation result
    */
  def eval(e: Expression, c: Element)
          (implicit s: Separators, t: Option[TimeZone]): EvalResult
}