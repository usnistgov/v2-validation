package expression

import org.specs2.Specification

trait NOTSpec
  extends Specification
  with Evaluator
  with Mocks  {

  /*
  NOT expression evaluation specifications
      NOT should be inconclusive if the underlining expression is inconclusive $notInconclusive
      NOT should pass if the underlining expression fail                       $notPass
      NOT should fail if the underlining expression pass                       $notFail
  */

  private val exp1 = Presence("2[1]")
  private val exp2 = Presence("2[2]")
  private val exp3 = Presence("1")

  assert( eval(exp1, c2) == Pass )
  assert( eval(exp2, c2).isInstanceOf[Fail] )
  assert( eval(exp3, c2).isInstanceOf[Inconclusive] )

  def notInconclusive = eval( NOT(exp3), c2 ) === inconclusive(c2, Presence("1"), "Invalid Path '1'")

  def notPass = eval( NOT(exp2), c2 ) === Pass

  def notFail = eval( NOT(exp1), c2 ) === Failures.notFailure( NOT(exp1), c2 )
}
