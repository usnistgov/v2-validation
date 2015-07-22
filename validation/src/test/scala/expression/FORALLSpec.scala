package expression

import expression.EvalResult.{Inconclusive, Fail, Pass}
import org.specs2.Specification

/**
 * Created by hossam.tamri on 7/16/15.
 */
trait FORALLSpec extends Specification
with Evaluator
with Mocks {

  /*
    FORALL expression evaluation specifications
       FORALL should be inconclusive if one of the expressions is inconclusive                    $forallInconclusive
       FORALL should fail if one of the expressions fails                                         $forallOneFails
       FORALL should fail if many of the expressions fails                                        $forallManyFail
       FORALL should pass if all the expressions pass                                             $forallAllPass
*/

  private val exp1 = Presence("2[1]")
  private val exp2 = Presence("2[2]")
  private val exp3 = Presence("1")
  private val exp22 = Presence("5[5]")

  assert(eval(exp1, c2) == Pass)
  assert(eval(exp22, c2).isInstanceOf[Fail])
  assert(eval(exp2, c2).isInstanceOf[Fail])
  assert(eval(exp3, c2).isInstanceOf[Inconclusive])

  def forallInconclusive = Seq(exp1, exp2, exp3) map { e =>
        eval(FORALL(exp3, e, exp1), c2) === inconclusive(exp3, c2.location, "Invalid Path '1'")
  }

  def forallOneFails = {
    val f = eval(exp2,c2).asInstanceOf[Fail]
    eval(FORALL(exp2, exp1,exp1), c2) === Failures.forall(FORALL(exp2, exp1,exp1),c2,f) and
      eval(FORALL(exp1, exp2,exp1), c2) === Failures.forall(FORALL(exp1, exp2,exp1),c2,f) and
      eval(FORALL(exp1, exp1,exp2), c2) === Failures.forall(FORALL(exp1, exp1,exp2),c2,f)
  }

  def forallManyFail = {
    val f1 = eval(exp2, c2).asInstanceOf[Fail]
    val f2 = eval(exp22, c2).asInstanceOf[Fail]
    eval(FORALL(exp2, exp2, exp2), c2) === Failures.forall(FORALL(exp2, exp2,exp2),c2,f1) and
      eval(FORALL(exp1, exp2, exp22), c2) === Failures.forall(FORALL(exp1, exp2,exp22),c2,f1) and
      eval(FORALL(exp1, exp22, exp2), c2) === Failures.forall(FORALL(exp1, exp22,exp2),c2,f2) and
      eval(FORALL(exp22, exp22, exp22), c2) === Failures.forall(FORALL(exp22, exp22,exp22),c2,f2)
  }

  def forallAllPass = eval(FORALL(exp1, exp1, exp1), c2) === Pass

}