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

  def notInconclusive = todo

  def notPass = todo

  def notFail = todo
}
