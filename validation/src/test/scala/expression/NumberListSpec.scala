package expression

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
      NumberList should fail if the values are in the list                         $numberListValueNotInList
  */

  //c1.4[1] is not populated
  assert( queryAsSimple(c1, "4[1]") == Success(Nil) )
  def numberListPathNotPopulated = eval( NumberList("4[1]", Nil), c1 ) === Pass

  // c1.2[3] is complex
  def numberListPathComplex = {
    val p = NumberList("2[3]", Nil)
    eval( p, c2 ) ===
      Inconclusive(p, "Path resolution returned at least one complex element"::Nil)
  }

  // 4 is an invalid path
  def numberListPathInvalid = Seq(true, false) map { b =>
    val p = NumberList("4", Nil)
    eval( p, c0 ) === Inconclusive(p, s"Invalid Path '${p.path}'"::Nil)
  }

  // s0 is a simple element, querying it will fail
  def numberListPathUnreachable = {
    val p = NumberList("4[1]", Nil)
    eval( p, s0 ) === Inconclusive(p, s"Unreachable Path '${p.path}'":: Nil)
  }

  // The following value will be used in the next tests
  private val `c1.3[1]`  = queryAsSimple(c1, "3[1]").get.head
  assert( `c1.3[1]`.value == Text("S3") )

  private val `c2.5[1]` = queryAsSimple(c2, "5[1]").get.head
  assert( `c2.5[1]`.value == Number("51") )

  def numberListNaN = {
    val p = NumberList("3[1]", List(1))
    eval(p, c1) === Failures.numberListNaNFailure(p, `c1.3[1]`::Nil)
  }

  def numberListValueInList = eval( NumberList("5[1]", List(51)), c2 ) === Pass

  def numberListValueNotInList = {
    val p = NumberList("5[1]", List(52, 53))
    eval(p, c2 ) === Failures.numberListFailure(p, `c2.5[1]`::Nil)
  }

}
