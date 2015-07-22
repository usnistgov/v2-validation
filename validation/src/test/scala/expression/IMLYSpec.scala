package expression

import org.specs2.Specification
import expression.EvalResult.{Inconclusive, Fail, Pass}
/**
 * Created by hossam.tamri on 7/16/15.
 */
trait IMPLYSpec extends Specification
with Evaluator
with Mocks {

  /*
    IMPLY expression evaluation specifications
       IMPLY should be inconclusive if the first expression is inconclusive                      $implyFirstInconclusive
       IMPLY should pass if first expression fails                                               $implyFirstFails
       If the first expression passes
           IMPLY should be inconclusive if the second is inconclusive                            $implyFirstPassesSecondInconclusive
           IMPLY should fail if the second fails                                                 $implyFirstPassesSecondFails
           IMPLY should pass if the second passes                                                $implyFirstPassesSecondPasses

*/

  private val exp1 = Presence("2[1]")
  private val exp2 = Presence("2[2]")
  private val exp3 = Presence("1")

  assert(eval(exp1, c2) == Pass)
  assert(eval(exp2, c2).isInstanceOf[Fail])
  assert(eval(exp3, c2).isInstanceOf[Inconclusive])

  def implyFirstInconclusive = Seq(exp1, exp2, exp3) map { e =>
    eval(IMPLY(exp3, e), c2) === inconclusive(exp3, c2.location, "Invalid Path '1'")
  }

  // If first passes
  def implyFirstPassesSecondInconclusive = eval(IMPLY(exp1, exp3), c2) ===
    inconclusive(exp3, c2.location, "Invalid Path '1'")

  def implyFirstPassesSecondFails = eval(IMPLY(exp1, exp2), c2) === {
    val f1 = eval(NOT(exp1), c2).asInstanceOf[Fail]
    val f2 = eval(exp2, c2).asInstanceOf[Fail]
    Failures.or(OR(NOT(exp1), exp2), c2, f1,f2)
  }

  def implyFirstPassesSecondPasses = eval(IMPLY(exp1, exp1), c2) === Pass

  // If first fails
  def implyFirstFails = Seq(exp1, exp2, exp3) map { e =>
    eval( IMPLY(exp2, e), c2 ) === Pass
  }

}