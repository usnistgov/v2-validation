package expression

import expression.EvalResult.Pass
import hl7.v2.instance.Query._
import hl7.v2.instance.Text
import org.specs2.Specification

import scala.util.Success

trait FormatSpec extends Specification with Evaluator with Mocks {

  /*
  FormatSpec
      Format evaluation should succeed if the path is not populated       $formatPathNotPopulated
      Format should pass if the value match the pattern                   $formatMatch
      Format should fail if the value doesn't match the pattern           $formatNoMatch
      If the path is valued to multiple elements
        Format should pass if one of the elements matches the pattern and AtLeastOnce = True           $formatAtLeastOnceT
        Format should fail if one of the elements doesn't match the pattern and AtLeastOnce = False           $formatAtLeastOnceF
        Min = 1 & Max = *
          Format evaluation should fail if 0 of the elements value matches the pattern $formatMinMax1s0
          Format evaluation should pass if 1 of the elements value matches the pattern $formatMinMax1s1
          Format evaluation should pass if 2 of the elements value matches the pattern $formatMinMax1s2
          Format evaluation should pass if 3 (all) of the elements value matches the pattern $formatMinMax1s3
        Min = 2 & Max = 2
          Format evaluation should fail if 0 of the elements value matches the pattern $formatMinMax220
          Format evaluation should fail if 1 of the elements value matches the pattern $formatMinMax221
          Format evaluation should pass if 2 of the elements value matches the pattern $formatMinMax222
          Format evaluation should fail if 3 (all) of the elements value matches the pattern $formatMinMax223
        Min = 0 & Max = *
          Format evaluation should pass if 0 of the elements value matches the pattern $formatMinMax0s0
          Format evaluation should pass if 1 of the elements value matches the pattern $formatMinMax0s1
          Format evaluation should pass if 3 (all) of the elements value matches the pattern $formatMinMax0s3
      Format evaluation should fail If not present behavior is FAIL and no element is found  $formatNoElmFAIL
      Format evaluation should be inconclusive If not present behavior is INCONCLUSIVE and no element is found $formatNoElmINC
      Format evaluation should pass If not present behavior is PASS and no element is found $formatNoElmPASS
  */

  //c1.4[1] is not populated
  assert( queryAsSimple(c1, "4[1]") == Success(Nil) )
  def formatPathNotPopulated = eval( Format("4[1]", "xx"), c1 ) === Pass

  def formatNoElmFAIL = {
    val f = Format("4[1]", "xx", false, "FAIL")
    eval(f, c1) === Failures.notPresentBehaviorFail(f, f.path, c1)
  }
  def formatNoElmINC = {
    val f = Format("4[1]", "xx", false, "INCONCLUSIVE")
    eval(f, c1) === Failures.notPresentBehaviorInconclusive(f, f.path, c1)
  }
  def formatNoElmPASS = {
    val f = Format("4[1]", "xx", false, "PASS")
    eval(f, c1) === Pass
  }

  // The following value will be used in the next tests
  private val `c1.3[1]`  = queryAsSimple(c1, "3[1]").get.head
  assert( `c1.3[1]`.value == Text("S3") )

  def formatMatch = eval( Format("3[1]", "[A-Z0-9]+"), c1 ) === Pass

  def formatNoMatch = {
    val e = Format("3[1]", "[a-z0-9]+")
    eval( e, c1 ) === Failures.format(e, `c1.3[1]` :: Nil )
  }
  
  assert( queryAsSimple(c1, "1[*]").isSuccess &&  queryAsSimple(c1, "1[*]").get.size > 1)
  def formatAtLeastOnceF = {
    val e = Format("1[*]", "[A-Z0-9]+2", false)
    val `c1.1[3]` = queryAsSimple(c1, "1[3]").get.head
    assert( `c1.1[3]`.value == Text("S13") )
    val `c1.1[1]` = queryAsSimple(c1, "1[1]").get.head
    assert( `c1.1[1]`.value == Text("S11") )
    eval( e, c1 ) === Failures.format(e, `c1.1[1]`::`c1.1[3]`::Nil )
  }
  
  def formatAtLeastOnceT = {
    val e = Format("1[*]", "[A-Z0-9]+2", true)
    eval( e, c1 ) === Pass
  }

  def formatMinMax1s0 = {
    val p = Format("1[*]", "NONE", false, "PASS", Some(hl7.v2.profile.Range(1, "*")))
    eval(p, cMinMax) === Failures.format(p, getValues(cMinMax, "1[1]"::"1[2]"::"1[3]"::Nil))
  }

  def formatMinMax1s1 = {
    val p = Format("1[*]", "S11", false, "PASS", Some(hl7.v2.profile.Range(1, "*")))
    eval(p, cMinMax) === Pass
  }

  def formatMinMax1s2 = {
    val p = Format("4[*]", "S4A", false, "PASS", Some(hl7.v2.profile.Range(1, "*")))
    eval(p, cMinMax) === Pass
  }

  def formatMinMax1s3 = {
    val p = Format("3[*]", "S3", false, "PASS", Some(hl7.v2.profile.Range(1, "*")))
    eval(p, cMinMax) === Pass
  }

  def formatMinMax220 = {
    val p = Format("1[*]", "NONE", false, "PASS", Some(hl7.v2.profile.Range(2, "2")))
    eval(p, cMinMax) === Failures.format(p, getValues(cMinMax, "1[1]"::"1[2]"::"1[3]"::Nil))
  }

  def formatMinMax221 = {
    val p = Format("1[*]", "S11", false, "PASS", Some(hl7.v2.profile.Range(2, "2")))
    eval(p, cMinMax) === Failures.format(p, getValues(cMinMax, "1[2]"::"1[3]"::Nil))
  }

  def formatMinMax222 = {
    val p = Format("4[*]", "S4A", false, "PASS", Some(hl7.v2.profile.Range(2, "2")))
    eval(p, cMinMax) === Pass
  }

  def formatMinMax223 = {
    val p = Format("3[*]", "S3", false, "PASS", Some(hl7.v2.profile.Range(2, "2")))
    eval(p, cMinMax) === Failures.format(p, Nil)
  }

  def formatMinMax0s0 = {
    val p = Format("1[*]", "NONE", false, "PASS", Some(hl7.v2.profile.Range(0, "*")))
    eval(p, cMinMax) === Pass
  }

  def formatMinMax0s1 = {
    val p = Format("1[*]", "S11", false, "PASS", Some(hl7.v2.profile.Range(0, "*")))
    eval(p, cMinMax) === Pass
  }

  def formatMinMax0s3 = {
    val p = Format("3[*]", "S3", false, "PASS", Some(hl7.v2.profile.Range(0, "*")))
    eval(p, cMinMax) === Pass
  }
}