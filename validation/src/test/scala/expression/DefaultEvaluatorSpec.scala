package expression

import expression.EvalResult._
import hl7.v2.instance.{Separators, Element}

object DefaultEvaluatorSpec extends EvaluatorSpec with DefaultEvaluator {

  val f1 = (p: Plugin, c: Element, s: Separators) => Pass

  def f2(p: Plugin, c: Element, s: Separators): Fail = {
    val reasons = Reason(c.location, s"$p execution failed") :: Nil
    Fail( Trace(p, reasons) :: Nil )
  }

  def f3 = (p: Plugin, c: Element, s: Separators) => Inconclusive( Trace(p, Nil) )

  val pluginMap = Map[String, (Plugin, Element, Separators) => EvalResult](
    "P1" -> f1,
    "P2" -> f2,
    "P3" -> f3
  )
}