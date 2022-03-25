package expression

import expression.EvalResult.Pass
import hl7.v2.instance.{Query, Text}
import Query.queryAsSimple
import org.specs2.Specification

import scala.util.Success

trait PlainTextSpec extends Specification with Evaluator with Mocks {

  /*
  PlainTextSpec
      PlainText evaluation should succeed if the path is not populated              $plainTextPathNotPopulated
      PlainText evaluation should be inconclusive if the path is complex            $plainTextPathComplex
      PlainText evaluation should be inconclusive if the path is invalid            $plainTextPathInvalid
      PlainText evaluation should be inconclusive if the path is unreachable        $plainTextPathUnreachable
      PlainText evaluation should pass if the values are the same                              $plainTextSameValue
      PlainText evaluation should pass if the values are the same by ignoring the case         $plainTextSameValueIC
      PlainText evaluation should fail if the values are different                             $plainTextDifferentValue
      PlainText evaluation should fail for same values in different case when case not ignored $plainTextSameValueCNI
      If the path is valued to multiple elements
        PlainText evaluation should fail if one of the elements value is different than the expected value with AtLeastOnce = False $plainTextAtLeastOnceF
        PlainText evaluation should pass if one of the elements value is equal to the expected value with AtLeastOnce = True $plainTextAtLeastOnceT
        Min = 1 & Max = *
          PlainText evaluation should fail if 0 of the elements value is equal to the expected $plainTextMinMax1s0
          PlainText evaluation should pass if 1 of the elements value is equal to the expected $plainTextMinMax1s1
          PlainText evaluation should pass if 2 of the elements value is equal to the expected $plainTextMinMax1s2
          PlainText evaluation should pass if 3 (all) of the elements value is equal to the expected $plainTextMinMax1s3
        Min = 2 & Max = 2
          PlainText evaluation should fail if 0 of the elements value is equal to the expected $plainTextMinMax220
          PlainText evaluation should fail if 1 of the elements value is equal to the expected $plainTextMinMax221
          PlainText evaluation should pass if 2 of the elements value is equal to the expected $plainTextMinMax222
          PlainText evaluation should fail if 3 (all) of the elements value is equal to the expected $plainTextMinMax223
        Min = 0 & Max = *
          PlainText evaluation should pass if 0 of the elements value is equal to the expected $plainTextMinMax0s0
          PlainText evaluation should pass if 1 of the elements value is equal to the expected $plainTextMinMax0s1
          PlainText evaluation should pass if 3 (all) of the elements value is equal to the expected $plainTextMinMax0s3
      PlainText evaluation should fail If not present behavior is FAIL and no element is found  $plainTextNoElmFAIL
      PlainText evaluation should be inconclusive If not present behavior is INCONCLUSIVE and no element is found $plainTextNoElmINC
      PlainText evaluation should pass If not present behavior is PASS and no element is found $plainTextNoElmPASS
  */
  
  //c1.4[1] is not populated
  assert( queryAsSimple(c1, "4[1]") == Success(Nil) )
  
  def plainTextPathNotPopulated = Seq(true, false) map { b =>
    eval( PlainText("4[1]", "", b), c1 ) === Pass
  }
  
  def plainTextNoElmFAIL = Seq(true, false) map { b =>
    val p = PlainText("4[1]", "", b, b, "FAIL")
    eval(p , c1 ) === Failures.notPresentBehaviorFail(p, p.path, c1)
  }
 
  def plainTextNoElmINC = Seq(true, false) map { b =>
    val p = PlainText("4[1]", "", b, b, "INCONCLUSIVE")
    eval(p , c1 ) === Failures.notPresentBehaviorInconclusive(p, p.path, c1)
  }
  
  def plainTextNoElmPASS = Seq(true, false) map { b =>
    eval( PlainText("4[1]", "", b, b, "PASS"), c1 ) === Pass
  }

  // c1.2[3] is complex
  def plainTextPathComplex = Seq(true, false) map { b =>
    val p2 = PlainText("2[3]", "", b)
    eval( p2, c2 ) === inconclusive(p2, c2.location, "Path resolution returned at least one complex element")
  }

  // 4 is an invalid path
  def plainTextPathInvalid = Seq(true, false) map { b =>
    val p3 = PlainText("4", "", b)
    eval( p3, c0 ) === inconclusive(p3, c0.location, s"Invalid Path '${p3.path}'")
  }

  // s0 is a simple element, querying it will fail
  def plainTextPathUnreachable = Seq(true, false) map { b =>
    val p4 = PlainText("4[1]", "", b)
    eval( p4, s0 ) === inconclusive(p4, s0.location, s"Unreachable Path '${p4.path}'")
  }

  // The following value will be used in the next tests
  private val `c1.3[1]`  = queryAsSimple(c1, "3[1]").get.head
  assert( `c1.3[1]`.value == Text("S3") )

  private val `c2.4[1]` = queryAsSimple(c2, "4[1]").get.head
  assert( `c2.4[1]`.value == Text("41\\F\\") )
  
  private val `c1.1[1]` = queryAsSimple(c1, "1[1]").get.head
  assert( `c1.1[1]`.value == Text("S11") )
  
  
  def plainTextSameValue = Seq(true, false) map { b =>
    eval( PlainText("3[1]", "S3", b), c1 ) === Pass and
    eval( PlainText("4[1]", "41|", b), c2  ) === Pass
  }

  def plainTextSameValueIC = eval( PlainText("3[1]", "s3", true ), c1 ) === Pass

  def plainTextDifferentValue = Seq(true, false) map { b =>
    val p1 = PlainText("3[1]", "X", b)
    val p2 = PlainText("4[1]", "41\\F\\", b)
    eval( p1, c1 ) === Failures.plainText(p1, `c1.3[1]`:: Nil) and
    eval( p2, c2 ) === Failures.plainText(p2, `c2.4[1]`:: Nil)
  }

  def plainTextSameValueCNI = {
    val p = PlainText("3[1]", "s3", false)
    eval( p, c1 ) === Failures.plainText(p, `c1.3[1]`::Nil)
  }
  
  assert( queryAsSimple(c1, "1[*]").isSuccess &&  queryAsSimple(c1, "1[*]").get.size > 1)
  def plainTextAtLeastOnceF = {
    val p = PlainText("1[*]", "S12", false, false)
    val `c1.1[3]` = queryAsSimple(c1, "1[3]").get.head
    assert( `c1.1[3]`.value == Text("S13") )
    eval(p, c1) === Failures.plainText(p, `c1.1[1]`::`c1.1[3]`::Nil)
  }
  
  def plainTextAtLeastOnceT = {
    val p = PlainText("1[*]", "S12", false, true)
    eval(p, c1) === Pass
  }

  def plainTextMinMax1s0 = {
    val p = PlainText("1[*]", "NONE", false, false, "PASS", Some(hl7.v2.profile.Range(1, "*")))
    eval(p, cMinMax) === Failures.plainText(p, getValues(cMinMax, "1[1]"::"1[2]"::"1[3]"::Nil))
  }

  def plainTextMinMax1s1 = {
    val p = PlainText("1[*]", "S11", false, false, "PASS", Some(hl7.v2.profile.Range(1, "*")))
    eval(p, cMinMax) === Pass
  }

  def plainTextMinMax1s2 = {
    val p = PlainText("4[*]", "S4A", false, false, "PASS", Some(hl7.v2.profile.Range(1, "*")))
    eval(p, cMinMax) === Pass
  }

  def plainTextMinMax1s3 = {
    val p = PlainText("3[*]", "S3", false, false, "PASS", Some(hl7.v2.profile.Range(1, "*")))
    eval(p, cMinMax) === Pass
  }

  def plainTextMinMax220 = {
    val p = PlainText("1[*]", "NONE", false, false, "PASS", Some(hl7.v2.profile.Range(2, "2")))
    eval(p, cMinMax) === Failures.plainText(p, getValues(cMinMax, "1[1]"::"1[2]"::"1[3]"::Nil))
  }

  def plainTextMinMax221 = {
    val p = PlainText("1[*]", "S11", false, false, "PASS", Some(hl7.v2.profile.Range(2, "2")))
    eval(p, cMinMax) === Failures.plainText(p, getValues(cMinMax, "1[2]"::"1[3]"::Nil))
  }

  def plainTextMinMax222 = {
    val p = PlainText("4[*]", "S4A", false, false, "PASS", Some(hl7.v2.profile.Range(2, "2")))
    eval(p, cMinMax) === Pass
  }

  def plainTextMinMax223 = {
    val p = PlainText("3[*]", "S3", false, false, "PASS", Some(hl7.v2.profile.Range(2, "2")))
    eval(p, cMinMax) === Failures.plainText(p, Nil)
  }

  def plainTextMinMax0s0 = {
    val p = PlainText("1[*]", "NONE", false, false, "PASS", Some(hl7.v2.profile.Range(0, "*")))
    eval(p, cMinMax) === Pass
  }

  def plainTextMinMax0s1 = {
    val p = PlainText("1[*]", "S11", false, false, "PASS", Some(hl7.v2.profile.Range(0, "*")))
    eval(p, cMinMax) === Pass
  }

  def plainTextMinMax0s3 = {
    val p = PlainText("3[*]", "S3", false, false, "PASS", Some(hl7.v2.profile.Range(0, "*")))
    eval(p, cMinMax) === Pass
  }
}