package expression

import hl7.v2.instance.Element

trait Evaluator {

  def eval(e: Expression, context: Element): EvalResult
}