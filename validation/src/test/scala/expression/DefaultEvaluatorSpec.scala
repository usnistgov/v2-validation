package expression

import hl7.v2.instance.Element

object DefaultEvaluatorSpec extends EvaluatorSpec with DefaultEvaluator {

  val f1 = (p: Plugin, c: Element) => Pass

  def f2(p: Plugin, c: Element): Fail = {
    val reasons = Reason(c.location, s"$p execution failed") :: Nil
    Fail( p -> reasons :: Nil)
  }

  def f3 = (p: Plugin, c: Element) => Inconclusive(p, Nil)

  val pluginMap = Map[String, (Plugin, Element) => EvalResult](
    "P1" -> f1,
    "P2" -> f2,
    "P3" -> f3
  )
}