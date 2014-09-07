package expression

import org.specs2.Specification

trait ANDSpec
  extends Specification
  with Evaluator
  with Mocks  {

  /*
  AND expression evaluation specifications
      AND should be inconclusive if at least one expression is inconclusive  $andInconclusive
      AND should pass if both expressions pass                               $andPass
      AND should fail if at least one expression fail                        $andFail
  */

  def andInconclusive = todo

  def andPass = todo

  def andFail = todo
}
