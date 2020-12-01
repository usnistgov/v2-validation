package expression

import expression.EvalResult.Pass
import hl7.v2.instance.Query._
import hl7.v2.instance.{Number, Text}
import org.specs2.Specification

import scala.util.Success

trait NumberListSpec extends Specification with Evaluator with Mocks  {

  /*
  NumberListSpec
      NumberList evaluation should succeed if the path is not populated            $numberListPathNotPopulated
      NumberList evaluation should be inconclusive if the path is complex          $numberListPathComplex
      NumberList evaluation should be inconclusive if the path is invalid          $numberListPathInvalid
      NumberList evaluation should be inconclusive if the path is unreachable      $numberListPathUnreachable
      NumberList should be inconclusive if at least one value is not a number      $numberListNaN
      NumberList should pass if the values are in the list                         $numberListValueInList
      NumberList should fail if the values are not in the list                         $numberListValueNotInList
      If the path is valued to multiple elements
        NumberList should pass if one of the elements is in the list and AtLeastOnce = True           $numberListAtLeastOnceT
        NumberList should fail if one of the elements is not in the list and AtLeastOnce = False           $numberListAtLeastOnceF
      NumberList evaluation should fail If not present behavior is FAIL and no element is found  $numberListNoElmFAIL
      NumberList evaluation should be inconclusive If not present behavior is INCONCLUSIVE and no element is found $numberListNoElmINC
      NumberList evaluation should pass If not present behavior is PASS and no element is found $numberListNoElmPASS
  */

  //c1.4[1] is not populated
  assert( queryAsSimple(c1, "4[1]") == Success(Nil) )
  def numberListPathNotPopulated = eval( NumberList("4[1]", Nil), c1 ) === Pass

  def numberListNoElmFAIL = {
    val f = NumberList("4[1]", Nil, false, "FAIL")
    eval(f, c1) === Failures.notPresentBehaviorFail(f, f.path, c1)
  }
  def numberListNoElmINC = {
    val f = NumberList("4[1]", Nil, false, "INCONCLUSIVE")
    eval(f, c1) === Failures.notPresentBehaviorInconclusive(f, f.path, c1)
  }
  def numberListNoElmPASS = {
    val f = NumberList("4[1]", Nil, false, "PASS")
    eval(f, c1) === Pass
  }

  // c1.2[3] is complex
  def numberListPathComplex = {
    val p = NumberList("2[3]", Nil)
    eval( p, c2 ) ===
      inconclusive(p, c2.location, "Path resolution returned at least one complex element")
  }

  // 4 is an invalid path
  def numberListPathInvalid = Seq(true, false) map { b =>
    val p = NumberList("4", Nil)
    eval( p, c0 ) === inconclusive(p, c0.location, s"Invalid Path '${p.path}'")
  }

  // s0 is a simple element, querying it will fail
  def numberListPathUnreachable = {
    val p = NumberList("4[1]", Nil)
    eval( p, s0 ) === inconclusive(p, s0.location, s"Unreachable Path '${p.path}'")
  }

  // The following value will be used in the next tests
  private val `c1.3[1]`  = queryAsSimple(c1, "3[1]").get.head
  assert( `c1.3[1]`.value == Text("S3") )

  private val `c2.5[1]` = queryAsSimple(c2, "5[1]").get.head
  assert( `c2.5[1]`.value == Number("51") )

  def numberListNaN = {
    val p = NumberList("3[1]", List(1))
    eval(p, c1) === Failures.numberListNaN(p, `c1.3[1]`::Nil)
  }

  def numberListValueInList = eval( NumberList("5[1]", List(51)), c2 ) === Pass

  def numberListValueNotInList = {
    val p = NumberList("5[1]", List(52, 53))
    eval(p, c2 ) === Failures.numberList(p, `c2.5[1]`::Nil)
  }
  
  assert( queryAsSimple(c1, "2[*]").isSuccess &&  queryAsSimple(c1, "2[*]").get.size > 1)
  def numberListAtLeastOnceT = {
    val p = NumberList("2[*]", List(52, 22),true)
    eval(p, c1 ) === Pass
  }
  
  def numberListAtLeastOnceF = {
    val p = NumberList("2[*]", List(21, 22),false)
    val `c1.2[3]`  = queryAsSimple(c1, "2[3]").get.head
    assert( `c1.2[3]`.value == Number("23") )
    eval(p, c1 ) === Failures.numberList(p, `c1.2[3]`::Nil)
  }

}
