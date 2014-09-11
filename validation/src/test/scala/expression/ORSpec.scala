package expression

import org.specs2.Specification

import scala.util.Random

trait ORSpec
  extends Specification
  with Evaluator
  with Mocks  {

  /*
  OR expression evaluation specifications
      OR should be inconclusive if the first expression is inconclusive                      $orFirstInconclusive
      OR should pass if the first expression passes                                          $orFirstPass
      If the first expression fails
          OR should be inconclusive if the second is inconclusive                            $orFirstFailsSecondInconclusive
          OR should pass if the second passes                                                $orFirstFailsSecondPasses
          OR should fail if the second fails                                                 $orFirstFailsSecondFails
 */

  private val exp1 = Presence("2[1]")
  private val exp2 = Presence("2[2]")
  private val exp3 = Presence("1")

  assert( eval(exp1, c2) == Pass )
  assert( eval(exp2, c2).isInstanceOf[Fail] )
  assert( eval(exp3, c2).isInstanceOf[Inconclusive] )

  def orFirstInconclusive = Seq(exp1, exp2, exp3) map { e => 
    eval( OR(exp3, e), c2 ) === inconclusive(c2, exp3, "Invalid Path '1'")
  }

  def orFirstPasses = Seq(exp1, exp2, exp3) map { e => 
    eval( OR(exp1, e), c2 ) === Pass
  }

  def orFirstFailsSecondInconclusive = eval( OR(exp2, exp3), c2 ) === inconclusive(c2, exp3, "Invalid Path '1'")

  def orFirstFailsSecondPasses = eval( OR(exp2, exp1), c2 ) === Pass

  def orFirstFailsSecondFails = eval( OR(exp2, exp2), c2 ) === {
    val f = eval(exp2, c2).asInstanceOf[Fail]
    Failures.orFailure(OR(exp2, exp2), c2, f, f)
  }
}
