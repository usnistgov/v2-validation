package expression

import expression.EvalResult._
import org.specs2.Specification

trait PluginSpec extends Specification with Evaluator with Mocks {

  /*
  PluginSpec
      Plugin execution should be inconclusive if an exception is raised when evaluation the assertion $pluginNoFunction
      Plugin execution should pass if the assertion evaluation is true                             $pluginPass
      Plugin execution should fail if the assertion evaluation is false                            $pluginFail
  */

  def pluginPass = eval( Plugin( "expression.PluginSuccess" ), c1 ) === Pass

  def pluginFail = eval( Plugin( "expression.PluginFailure" ), c1 ) === Fail(Nil)

  def pluginInconclusive = eval( Plugin( "xxx" ), c1 ) must beLike { case Inconclusive(_) => ok }

}
