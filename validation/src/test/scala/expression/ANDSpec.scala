package expression

import org.specs2.Specification

trait ANDSpec
  extends Specification
  with Evaluator
  with Mocks  {

  /*
  AND expression evaluation specifications
      AND should be inconclusive if the first expression is inconclusive     $andFirstInconclusive
      AND should fail in the first expression fails                          $andFirstFails
      If the first expression passes
          AND should be inconclusive if the second is inconclusive           $andFirstPassesSecondInconclusive
          AND should pass if the second passes                               $andFirstPassesSecondPasses
          AND should fail is the second fails                                $andFirstPassesSecondFails
  */

  private val exp1 = Presence("2[1]")
  private val exp2 = Presence("2[2]")
  private val exp3 = Presence("1")

  assert( eval(exp1, c2) == Pass )
  assert( eval(exp2, c2).isInstanceOf[Fail] )
  assert( eval(exp3, c2).isInstanceOf[Inconclusive] )

  def andFirstInconclusive = Seq(exp1, exp2, exp3) map { e => 
    eval( AND(exp3, e), c2 ) === inconclusive(c2, Presence("1"), "Invalid Path '1'")
  }

  def andFirstFails = Seq(exp1, exp2, exp3) map { e => 
    val f = eval(exp2, c2).asInstanceOf[Fail]
    eval( AND(exp2, e), c2 ) === Failures.andFailure(AND(exp2, e), c2, f)
  }

  def andFirstPassesSecondInconclusive = eval( AND(exp1, exp3), c2 ) === 
    inconclusive(c2, Presence("1"), "Invalid Path '1'")

  def andFirstPassesSecondPasses = eval( AND(exp1, exp1), c2 ) === Pass

  def andFirstPassesSecondFails = {
    val f = eval(exp2, c2).asInstanceOf[Fail]
    eval( AND(exp1, exp2), c2 ) === Failures.andFailure(AND(exp1, exp2), c2, f)
  }
}
