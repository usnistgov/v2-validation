package expression

import expression.EvalResult.{Inconclusive, Fail, Pass}
import org.specs2.Specification

/**
 * Created by hossam.tamri on 7/16/15.
 */
trait XORSpec extends Specification
with Evaluator
with Mocks  {

  /*
 XOR expression evaluation specifications
     XOR should be inconclusive if the first expression is inconclusive                      $xorFirstInconclusive
     If the first expression passes
         XOR should be inconclusive if the second is inconclusive                            $xorFirstPassesSecondInconclusive
         XOR should pass if the second fails                                                 $xorFirstPassesSecondFails
         XOR should fail if the second passes                                                $xorFirstPassesSecondPasses
     If the first expression fails
         XOR should be inconclusive if the second is inconclusive                            $xorFirstFailsSecondInconclusive
         XOR should pass if the second passes                                                $xorFirstFailsSecondPasses
         XOR should fail if the second fails                                                 $xorFirstFailsSecondFails
*/

  private val exp1 = Presence("2[1]")
  private val exp2 = Presence("2[2]")
  private val exp3 = Presence("1")

  assert( eval(exp1, c2) == Pass )
  assert( eval(exp2, c2).isInstanceOf[Fail] )
  assert( eval(exp3, c2).isInstanceOf[Inconclusive] )

  def xorFirstInconclusive = Seq(exp1, exp2, exp3) map { e =>
    eval( XOR(exp3, e), c2 ) === inconclusive(exp3, c2.location, "Invalid Path '1'")
  }

  // If first passes
  def xorFirstPassesSecondInconclusive = eval(XOR(exp1,exp3),c2) ===
    inconclusive(exp3, c2.location, "Invalid Path '1'")

  def xorFirstPassesSecondFails = eval(XOR(exp1,exp2),c2) === Pass

  def xorFirstPassesSecondPasses = eval( XOR(exp1, exp1), c2 ) === {
    val firstHand = OR(exp1,exp1)
    val secondHand = NOT(AND(exp1,exp1))
    val f = eval(secondHand, c2).asInstanceOf[Fail]
    Failures.and(AND(firstHand,secondHand), c2, f)
  }

  // If first fails
  def xorFirstFailsSecondInconclusive = eval( XOR(exp2, exp3), c2 ) ===
    inconclusive(exp3, c2.location, "Invalid Path '1'")

  def xorFirstFailsSecondPasses = eval( XOR(exp2, exp1), c2 ) === Pass

  def xorFirstFailsSecondFails = eval( XOR(exp2, exp2), c2 ) === {
    val firstHand = OR(exp2,exp2)
    val secondHand = NOT(AND(exp2,exp2))
    val f = eval(firstHand, c2).asInstanceOf[Fail]
    Failures.and(AND(firstHand,secondHand), c2, f)
  }




}
