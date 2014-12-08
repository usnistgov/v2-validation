package expression

import hl7.v2.instance.{TimeZone, Separators, Element}

trait Evaluator {

  /**
    * The plugin map to be used by this expression evaluator.
    * The map will contain plugins ID as key and a function
    * which has the same signature as Evaluator.eval as value.
    */
  def pluginMap: Map[String, (Plugin, Element, Separators) => EvalResult]

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