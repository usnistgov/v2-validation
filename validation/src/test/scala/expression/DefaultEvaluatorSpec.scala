package expression

object DefaultEvaluatorSpec extends EvaluatorSpec with DefaultEvaluator {

  val pluginMap = Map[String, Seq[String] => EvalResult]()
}