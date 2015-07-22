package expression

import expression.EvalResult.{Inconclusive, Fail, Pass}
import org.specs2.Specification

/**
 * Created by hossam.tamri on 7/16/15.
 */
trait EXISTSpec extends Specification
with Evaluator
with Mocks {

  /*
    EXIST expression evaluation specifications
       EXIST should be inconclusive if one of the expressions is inconclusive                    $existInconclusive
       EXIST should fail if all the expressions fails                                            $existAllFail
       EXIST should pass if one of the expressions passes                                        $existOnePasses
       EXIST should pass if many of the expressions pass                                         $existManyPass
*/

  private val exp1 = Presence("2[1]")
  private val exp2 = Presence("2[2]")
  private val exp3 = Presence("1")
  private val exp22 = Presence("5[5]")

  assert(eval(exp1, c2) == Pass)
  assert(eval(exp22, c2).isInstanceOf[Fail])
  assert(eval(exp2, c2).isInstanceOf[Fail])
  assert(eval(exp3, c2).isInstanceOf[Inconclusive])

  def existInconclusive = Seq(exp1, exp2, exp3) map { e =>
    eval(EXIST(exp3, e, exp1), c2) === inconclusive(exp3, c2.location, "Invalid Path '1'")
  }

  def existOnePasses =  eval(EXIST(exp1, exp2, exp2), c2) === Pass and
    eval(EXIST(exp2, exp1, exp2), c2) === Pass and
    eval(EXIST(exp2, exp2, exp1), c2) === Pass

  def existAllFail = {
    val f1 = eval(exp2, c2).asInstanceOf[Fail]
    val f2 = eval(exp22, c2).asInstanceOf[Fail]
    eval(EXIST(exp2, exp2, exp2), c2) === Failures.exist(EXIST(exp2, exp2, exp2),c2,Fail(f1.stack:::f1.stack:::f1.stack)) and
      eval(EXIST(exp22, exp22, exp22), c2) === Failures.exist(EXIST(exp22, exp22, exp22),c2,Fail(f2.stack:::f2.stack:::f2.stack)) and
      eval(EXIST(exp2, exp22, exp22), c2) === Failures.exist(EXIST(exp2, exp22, exp22),c2,Fail(f1.stack:::f2.stack:::f2.stack)) and
      eval(EXIST(exp22, exp2, exp2), c2) === Failures.exist(EXIST(exp22, exp2, exp2),c2,Fail(f2.stack:::f1.stack:::f1.stack))
  }

  def existManyPass = eval(EXIST(exp2, exp1, exp1), c2) === Pass and
    eval(EXIST(exp1, exp2, exp1), c2) === Pass and
    eval(EXIST(exp1, exp1, exp2), c2) === Pass

}