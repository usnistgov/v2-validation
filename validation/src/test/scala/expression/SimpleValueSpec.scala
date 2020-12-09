package expression

import expression.EvalResult.{Trace, Reason, Inconclusive, Pass}
import hl7.v2.instance.Query._
import hl7.v2.instance.{Number, Text, Time}
import org.specs2.Specification

import scala.util.Success

trait SimpleValueSpec extends Specification with Evaluator with Mocks  {

  /*
  SimpleValueSpec
      SimpleValue evaluation should succeed if the path is not populated            $simpleValuePathNotPopulated
      SimpleValue evaluation should be inconclusive if the path is complex          $simpleValuePathComplex
      SimpleValue evaluation should be inconclusive if the path is invalid          $simpleValuePathInvalid
      SimpleValue evaluation should be inconclusive if the path is unreachable      $simpleValuePathUnreachable
      SimpleValue should be inconclusive if the values are not comparable           $simpleValueNotComparable
      SimpleValue should be inconclusive if at least one value is invalid           $simpleValueInvalidValue
      SimpleValue should pass if operator = < and path.value < value                $simpleValuePass
      SimpleValue should fail if operator = < and path.value > value                $simpleValueFail
      If the path is valued to multiple elements
        SimpleValue should pass if one of the elements is in the list and AtLeastOnce = True           $simpleValueAtLeastOnceT
        SimpleValue should fail if one of the elements is not in the list and AtLeastOnce = False           $simpleValueAtLeastOnceF
      SimpleValue evaluation should fail If not present behavior is FAIL and no element is found  $simpleValueNoElmFAIL
      SimpleValue evaluation should be inconclusive If not present behavior is INCONCLUSIVE and no element is found $simpleValueNoElmINC
      SimpleValue evaluation should pass If not present behavior is PASS and no element is found $simpleValueNoElmPASS
  */

  //c1.4[1] is not populated
  assert( queryAsSimple(c1, "4[1]") == Success(Nil) )
  def simpleValuePathNotPopulated = eval( SimpleValue("4[1]", Operator.LT, Text("xx")), c1 ) === Pass

  def simpleValueNoElmFAIL = {
    val f = SimpleValue("4[1]", Operator.LT, Text("xx"), false, "FAIL")
    eval(f, c1) === Failures.notPresentBehaviorFail(f, f.path, c1)
  }
  def simpleValueNoElmINC = {
    val f = SimpleValue("4[1]", Operator.LT, Text("xx"), false, "INCONCLUSIVE")
    eval(f, c1) === Failures.notPresentBehaviorInconclusive(f, f.path, c1)
  }
  def simpleValueNoElmPASS = {
    val f = SimpleValue("4[1]", Operator.LT, Text("xx"), false, "PASS")
    eval(f, c1) === Pass
  }

  // c1.2[3] is complex
  def simpleValuePathComplex = {
    val p = SimpleValue("2[3]", Operator.LT, Text("xx"))
    eval( p, c2 ) ===
      inconclusive(p, c2.location, "Path resolution returned at least one complex element")
  }

  // 4 is an invalid path
  def simpleValuePathInvalid = Seq(true, false) map { b =>
    val p = SimpleValue("4", Operator.LT, Text("xx"))
    eval( p, c0 ) === inconclusive(p, c0.location, s"Invalid Path '${p.path}'")
  }

  // s0 is a simple element, querying it will fail
  def simpleValuePathUnreachable = {
    val p = SimpleValue("4[1]", Operator.LT, Text("xx"))
    eval( p, s0 ) === inconclusive(p, s0.location, s"Unreachable Path '${p.path}'")
  }

  // The following value will be used in the next tests
  private val `c1.3[1]`  = queryAsSimple(c1, "3[1]").get.head
  assert( `c1.3[1]`.value == Text("S3") )

  private val `c2.5[1]` = queryAsSimple(c2, "5[1]").get.head
  assert( `c2.5[1]`.value == Number("51") )

  def simpleValuePass = eval( SimpleValue("5[1]", Operator.LT, Number("52")), c2 ) === Pass

  def simpleValueFail = {
    val p = SimpleValue("5[1]", Operator.LT, Number("50"))
    eval( p, c2 ) === Failures.simpleValue(p, `c2.5[1]`::Nil)
  }

  def simpleValueNotComparable = {
    val o = Operator.LT
    val v = Time("00")
    val p = SimpleValue("5[1]", o, v)
    val s = `c2.5[1]`
    val reasons = Reason( s.location, s"${s.value} is not comparable to $v." ) :: Nil
    //val error  = s"${loc(s.location)} ${s.value} $o $v failed. Reason: $reason"
    eval( p, c2 ) === Inconclusive( Trace(p, reasons) )
  }

  def simpleValueInvalidValue = {
    val o = Operator.LT
    val v = Number("xx")
    val p = SimpleValue("5[1]", o, v)
    val s = `c2.5[1]`
    val reason = Reason(s.location, s"${v.raw} is not a valid Number. The format should be: [+|-]digits[.digits]")
    eval( p, c2 ) === Inconclusive( Trace(p, reason:: Nil) )
  }

  assert( queryAsSimple(c1, "1[*]").isSuccess &&  queryAsSimple(c1, "1[*]").get.size > 1)
  def simpleValueAtLeastOnceT = {
    val p = SimpleValue("1[*]", Operator.EQ, Text("S12"), true)
    eval( p, c1 ) === Pass
  }

  def simpleValueAtLeastOnceF = {
    val p = SimpleValue("1[*]", Operator.EQ, Text("S12"), false)

    val `c1.1[1]`  = queryAsSimple(c1, "1[1]").get.head
    assert( `c1.1[1]`.value == Text("S11") )
    val `c1.1[3]`  = queryAsSimple(c1, "1[3]").get.head
    assert( `c1.1[3]`.value == Text("S13") )
    eval( p, c1 ) === Failures.simpleValue(p, `c1.1[1]`::`c1.1[3]`::Nil)
  }

}
