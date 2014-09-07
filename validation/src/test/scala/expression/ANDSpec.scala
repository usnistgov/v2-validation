package expression

import org.specs2.Specification

trait ANDSpec
  extends Specification
  with Evaluator
  with Mocks  {

  /*
  AND expression evaluation specifications
      AND should be inconclusive if at least one expression is inconclusive    $andInconclusive
      AND should pass if both expressions pass                                 $andPass
      AND should fail if at least one expression fail and none is inconclusive $andFail
  */

  def andInconclusive = {
    val exp1 = Presence("1")
    val exp2 = Presence("2[1]")
    assert( eval(exp2, c0).isInstanceOf[Fail] && eval(exp1, c0).isInstanceOf[Inconclusive] )
    eval( AND(exp1, exp2), c0 ) === inconclusive(c0, Presence("1"), "Invalid Path '1'") and
    eval( AND(exp2, exp1), c0 ) === inconclusive(c0, Presence("1"), "Invalid Path '1'")
  }

  def andPass = {
    val p = Presence("2[1]")
    assert( eval(p, c2) == Pass )
    eval( AND(p, p), c2) === Pass
  }

  def andFail = {
    val exp1 = Presence("2[1]")
    val exp2 = Presence("2[2]")
    val exp2EvalResult = eval(exp2, c2)
    assert( eval(exp1, c2) == Pass && exp2EvalResult.isInstanceOf[Fail] )
    val expected = Fail( Reason(c2.location, s"$exp2 failed") :: exp2EvalResult.asInstanceOf[Fail].reasons )
    eval( AND(exp1, exp2), c2 ) === expected and eval( AND(exp2, exp1), c2 ) === expected
  }
}
