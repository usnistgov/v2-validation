package expression

import hl7.v2.instance.Element

trait Evaluator {

  /**
    * The plugin map to be used by this expression evaluator
    */
  def pluginMap: Map[String, Seq[String] => EvalResult]

  /**
    * Evaluates the expression within the specified context
    * and returns the result
    * @param e       - The expression to be evaluated
    * @param context - The context node
    * @return The evaluation result
    */
  def eval(e: Expression, context: Element): EvalResult
}