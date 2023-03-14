package expression

import expression.EvalResult.Pass
import hl7.v2.instance.Text
import hl7.v2.instance.Query._
import org.specs2.Specification

import scala.util.Success

trait StringListSpec extends Specification with Evaluator with Mocks  {

  /*
  StringListSpec
      StringList evaluation should succeed if the path is not populated            $stringListPathNotPopulated
      StringList evaluation should be inconclusive if the path is complex          $stringListPathComplex
      StringList evaluation should be inconclusive if the path is invalid          $stringListPathInvalid
      StringList evaluation should be inconclusive if the path is unreachable      $stringListPathUnreachable
      StringList should pass if the values are in the list                         $stringListValueInList
      StringList should fail if the values are not in the list                         $stringListValueNotInList
      If the path is valued to multiple elements
        StringList should pass if one of the elements is in the list and AtLeastOnce = True           $stringListAtLeastOnceT
        StringList should fail if one of the elements is not in the list and AtLeastOnce = False           $stringListAtLeastOnceF
        Min = 1 & Max = *
          StringList evaluation should fail if 0 of the elements value is in the list $stringListMinMax1s0
          StringList evaluation should pass if 1 of the elements value is in the list $stringListMinMax1s1
          StringList evaluation should pass if 2 of the elements value is in the list $stringListMinMax1s2
          StringList evaluation should pass if 3 (all) of the elements value is in the list $stringListMinMax1s3
        Min = 2 & Max = 2
          StringList evaluation should fail if 0 of the elements value is in the list $stringListMinMax220
          StringList evaluation should fail if 1 of the elements value is in the list $stringListMinMax221
          StringList evaluation should pass if 2 of the elements value is in the list $stringListMinMax222
          StringList evaluation should fail if 3 (all) of the elements value is in the list $stringListMinMax223
        Min = 0 & Max = *
          StringList evaluation should pass if 0 of the elements value is in the list $stringListMinMax0s0
          StringList evaluation should pass if 1 of the elements value is in the list $stringListMinMax0s1
          StringList evaluation should pass if 3 (all) of the elements value is in the list $stringListMinMax0s3
      StringList evaluation should fail If not present behavior is FAIL and no element is found  $stringListNoElmFAIL
      StringList evaluation should be inconclusive If not present behavior is INCONCLUSIVE and no element is found $stringListNoElmINC
      StringList evaluation should pass If not present behavior is PASS and no element is found $stringListNoElmPASS
  */

  //c1.4[1] is not populated
  assert( queryAsSimple(c1, "4[1]") == Success(Nil) )
  def stringListPathNotPopulated = eval( StringList("4[1]", Nil), c1 ) === Pass

  def stringListNoElmFAIL = {
    val f = StringList("4[1]", Nil, false, "FAIL")
    eval(f, c1) === Failures.notPresentBehaviorFail(f, f.path, c1)
  }
  def stringListNoElmINC = {
    val f = StringList("4[1]", Nil, false, "INCONCLUSIVE")
    eval(f, c1) === Failures.notPresentBehaviorInconclusive(f, f.path, c1)
  }
  def stringListNoElmPASS = {
    val f = StringList("4[1]", Nil, false, "PASS")
    eval(f, c1) === Pass
  }

  // c1.2[3] is complex
  def stringListPathComplex = {
    val p = StringList("2[3]", Nil)
    eval( p, c2 ) ===
      inconclusive(p, c2.location, "Path resolution returned at least one complex element")
  }

  // 4 is an invalid path
  def stringListPathInvalid = Seq(true, false) map { b =>
    val p = StringList("4", Nil)
    eval( p, c0 ) === inconclusive(p, c0.location, s"Invalid Path '${p.path}'")
  }

  // s0 is a simple element, querying it will fail
  def stringListPathUnreachable = {
    val p = StringList("4[1]", Nil)
    eval( p, s0 ) === inconclusive(p, s0.location, s"Unreachable Path '${p.path}'")
  }

  // The following value will be used in the next tests
  private val `c1.3[1]`  = queryAsSimple(c1, "3[1]").get.head
  assert( `c1.3[1]`.value == Text("S3") )

  private val `c2.4[1]` = queryAsSimple(c2, "4[1]").get.head
  assert( `c2.4[1]`.value == Text("41\\F\\") )

  def stringListValueInList =
    eval( StringList("3[1]", List("S3")), c1 ) === Pass and
    eval( StringList("4[1]", List("41|")), c2 ) === Pass

  def stringListValueNotInList = {
    val p = StringList("3[1]", List("s3"))
    eval( p, c1 ) === Failures.stringList(p, `c1.3[1]`::Nil)
  }
  
  assert( queryAsSimple(c1, "1[*]").isSuccess &&  queryAsSimple(c1, "1[*]").get.size > 1)
  def stringListAtLeastOnceT = {
    val p = StringList("1[*]", List("S12","XX"), true)
    eval( p, c1 ) === Pass
  }
  
  def stringListAtLeastOnceF = {
    val p = StringList("1[*]", List("S12","XX"), false)
    
    val `c1.1[1]`  = queryAsSimple(c1, "1[1]").get.head
    assert( `c1.1[1]`.value == Text("S11") )
    val `c1.1[3]`  = queryAsSimple(c1, "1[3]").get.head
    assert( `c1.1[3]`.value == Text("S13") )
    eval( p, c1 ) === Failures.stringList(p, `c1.1[1]`::`c1.1[3]`::Nil)
  }

  def stringListMinMax1s0 = {
    val p = StringList("1[*]", List("NONE"), false, "PASS", Some(hl7.v2.profile.Range(1, "*")))
    eval(p, cMinMax) === Failures.stringList(p, getValues(cMinMax, "1[1]"::"1[2]"::"1[3]"::Nil))
  }

  def stringListMinMax1s1 = {
    val p = StringList("1[*]", List("S11"), false, "PASS", Some(hl7.v2.profile.Range(1, "*")))
    eval(p, cMinMax) === Pass
  }

  def stringListMinMax1s2 = {
    val p = StringList("4[*]", List("S4A"), false, "PASS", Some(hl7.v2.profile.Range(1, "*")))
    eval(p, cMinMax) === Pass
  }

  def stringListMinMax1s3 = {
    val p = StringList("3[*]", List("S3"), false, "PASS", Some(hl7.v2.profile.Range(1, "*")))
    eval(p, cMinMax) === Pass
  }

  def stringListMinMax220 = {
    val p = StringList("1[*]", List("NONE"), false, "PASS", Some(hl7.v2.profile.Range(2, "2")))
    eval(p, cMinMax) === Failures.stringList(p, getValues(cMinMax, "1[1]"::"1[2]"::"1[3]"::Nil))
  }

  def stringListMinMax221 = {
    val p = StringList("1[*]", List("S11"), false, "PASS", Some(hl7.v2.profile.Range(2, "2")))
    eval(p, cMinMax) === Failures.stringList(p, getValues(cMinMax, "1[2]"::"1[3]"::Nil))
  }

  def stringListMinMax222 = {
    val p = StringList("4[*]", List("S4A"), false, "PASS", Some(hl7.v2.profile.Range(2, "2")))
    eval(p, cMinMax) === Pass
  }

  def stringListMinMax223 = {
    val p = StringList("3[*]", List("S3"), false, "PASS", Some(hl7.v2.profile.Range(2, "2")))
    eval(p, cMinMax) === Failures.stringList(p, Nil)
  }

  def stringListMinMax0s0 = {
    val p = StringList("1[*]", List("NONE"), false, "PASS", Some(hl7.v2.profile.Range(0, "*")))
    eval(p, cMinMax) === Pass
  }

  def stringListMinMax0s1 = {
    val p = StringList("1[*]", List("S11"), false, "PASS", Some(hl7.v2.profile.Range(0, "*")))
    eval(p, cMinMax) === Pass
  }

  def stringListMinMax0s3 = {
    val p = StringList("3[*]", List("S3"), false, "PASS", Some(hl7.v2.profile.Range(0, "*")))
    eval(p, cMinMax) === Pass
  }

}
