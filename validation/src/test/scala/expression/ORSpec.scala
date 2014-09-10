package expression

import org.specs2.Specification

import scala.util.Random

trait ORSpec
  extends Specification
  with Evaluator
  with Mocks  {

  /*
  OR expression evaluation specifications
      OR should be inconclusive if the first expression is inconclusive                      $orInconclusive1 
      OR should be inconclusive if the first expression fails and the second is inconclusive $orInconclusive2
      OR should pass if the first expression passes                                          $orPass1
      OR should pass if the first expression fails and the second passes                     $orPass2
      OR should fail if both expressions fail                                                $orFail
  */

  private val exp1 = Presence("2[1]")
  private val exp2 = Presence("2[2]")
  private val exp3 = Presence("1")

  assert( eval(exp1, c2) == Pass )
  assert( eval(exp2, c2).isInstanceOf[Fail] )
  assert( eval(exp3, c2).isInstanceOf[Inconclusive] )

  def orInconclusive1 = Seq(exp1, exp2, exp3) map { e => 
    eval( OR(exp3, e), c2 ) === inconclusive(c2, exp3, "Invalid Path '1'")
  }

  def orInconclusive2 = eval( OR(exp2, exp3), c2 ) === inconclusive(c2, exp3, "Invalid Path '1'")

  def orPass1 = Seq(exp1, exp2, exp3) map { e => eval( OR(exp1, e), c2 ) === Pass }

  def orPass2 = eval( OR(exp2, exp1), c2 ) === Pass 

  def orFail = {
    val r = eval(exp2, c2).asInstanceOf[Fail]
    val expected = Fail( Reason(c2.location, s"Both ${exp2} and ${exp2} failed") :: r.reasons ::: r.reasons )
    eval( OR(exp2, exp2), c2 ) === expected
  }
}
