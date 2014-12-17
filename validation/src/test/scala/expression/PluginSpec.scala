package expression

import expression.EvalResult._
import org.specs2.Specification

trait PluginSpec extends Specification with Evaluator with Mocks {

  /*
  PluginSpec
      Plugin execution should fail if there is no associated function in the map      $pluginNoFunction
      Plugin execution should pass if the function returns Pass                       $pluginPass
      Plugin execution should fail if the function returns Fail                       $pluginFail
      Plugin execution should be inconclusive if the function returns is inconclusive $pluginInconclusive
  */

  def pluginNoFunction = {
    assert( !(pluginMap contains "xxx") )
    val e = Plugin( "xxx", Map[String, String]() )
    eval( e, c1 ) === inconclusive(e, c1.location, s"Plugin '${e.id}' not found")
  }

  def pluginPass = {
    assert( pluginMap contains "P1" )
    val p = Plugin( "P1", Map[String, String]() )
    val expected = Pass
    assert( pluginMap("P1")(p, c1, separators) === expected)
    eval( p, c1 ) === expected
  }

  def pluginFail = {
    assert( pluginMap contains "P2" )
    val p = Plugin( "P2", Map[String, String]() )
    val reasons = Reason(c1.location, s"$p execution failed") :: Nil
    val expected = Fail( Trace(p, reasons) :: Nil )
    assert( pluginMap("P2")(p, c1, separators) === expected)
    eval( p, c1 ) === expected
  }

  def pluginInconclusive = {
    assert( pluginMap contains "P3" )
    val p = Plugin( "P3", Map[String, String]() )
    val expected = Inconclusive( Trace(p, Nil) )
    assert( pluginMap("P3")(p, c1, separators) === expected)
    eval( p, c1 ) === expected
  }

}
