package expression

import org.specs2.Specification

trait ORSpec
  extends Specification
  with Evaluator
  with Mocks  {

  /*
  OR expression evaluation specifications
      OR should be inconclusive if the first expression is inconclusive
          or the first expression failed and the second is inconclusive     $orInconclusive
      OR should pass if at least one expressions pass                       $orPass
      OR should fail if both expressions fail                               $orFail
  */

  def orInconclusive = todo

  def orPass = todo

  def orFail = todo
}
