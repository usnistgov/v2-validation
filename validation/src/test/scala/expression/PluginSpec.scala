package expression

import expression.EvalResult._
import org.specs2.Specification

trait PluginSpec extends Specification with Evaluator with Mocks {

  /*
  PluginSpec
      Plugin execution should be inconclusive if an exception is raised when evaluation the assertion $pluginNoFunction
      Plugin execution should pass if the assertion evaluation is true                             $pluginPass
      Plugin execution should fail if the assertion evaluation is false                            $pluginFail
      Plugin execution should pass if the assertion evaluation returns a NULL list                            $pluginCustomPassNull
      Plugin execution should pass if the assertion evaluation returns an empty list                            $pluginCustomPassEmpty
      Plugin execution should fail if the assertion evaluation returns non-empty list                            $pluginCustomPassEmpty
      Plugin execution should be inconclusive if the implementation contains multiple matching methods                            $pluginMulti
  */

  def pluginPass = eval( Plugin( "expression.PluginSuccess" ), c1 ) === Pass

  def pluginFail = eval( Plugin( "expression.PluginFailure" ), c1 ) === Fail(Nil)
  
  def pluginCustomPassNull = eval( Plugin( "expression.PluginCustomSuccessNull" ), c1 ) === Pass
  
  def pluginCustomPassEmpty = eval( Plugin( "expression.PluginCustomSuccessEmpty" ), c1 ) === Pass

  def pluginCustomFail = eval( Plugin( "expression.PluginCustomFailure" ), c1 ) === FailPlugin(Nil, List("FAIL"))

  def pluginInconclusive = eval( Plugin( "xxx" ), c1 ) must beLike { case Inconclusive(_) => ok }
  
  def pluginMulti = eval( Plugin( "expression.PluginFailureMulti" ), c1 ) must beLike { case Inconclusive(_) => ok }

}
