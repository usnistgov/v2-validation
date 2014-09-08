package expression

import org.specs2.Specification

import scala.util.Random

trait ORSpec
  extends Specification
  with Evaluator
  with Mocks  {

  /*
  OR expression evaluation specifications
      OR should be inconclusive if the first expression is inconclusive
          or the first expression failed and the second is inconclusive     $orInconclusive
      OR should pass if the first passes or the first fails and the second passes $orPass
      OR should fail if both expressions fail                                   $orFail
  */

  private val exp1 = Presence("2[1]")
  private val exp2 = Presence("2[2]")
  private val exp3 = Presence("1")

  assert( eval(exp1, c2) == Pass )
  assert( eval(exp2, c2).isInstanceOf[Fail] )
  assert( eval(exp3, c2).isInstanceOf[Inconclusive] )

  val random = new Random

  def randomExp = {
    val i = random.nextInt.abs % 3 + 1
    if(i == 1) exp1 else if(i == 2) exp2 else if(i == 3) exp3 else ???
  }

  def orInconclusive1 = (1 to 10).toSeq map { i => eval( OR(exp3, randomExp), c2 ) === inconclusive(c2, exp3, "Invalid Path '1'")  }

  def orInconclusive = eval( OR(exp3, randomExp), c2 ) === inconclusive(c2, exp3, "Invalid Path '1'")

  def orPass = todo

  def orFail = todo
}
