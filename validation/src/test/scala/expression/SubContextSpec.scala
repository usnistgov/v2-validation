package expression

import org.specs2.Specification
import expression.EvalResult.{Fail, Inconclusive, Pass}
import hl7.v2.instance.Query.query
import hl7.v2.instance.Text
import hl7.v2.profile.Range
import org.specs2.matcher.MatchResult

import scala.util.Success

trait SubContextSpec extends Specification with Evaluator with Mocks {

  /*
    SubContextSpec
        SubContext evaluation should succeed if the path is not populated                                           $subContextPathNotPopulated
        SubContext evaluation should be inconclusive if the path is invalid                                         $subContextPathInvalid
        SubContext evaluation should be inconclusive if the path is unreachable                                     $subContextPathUnreachable
        SubContext evaluation should be inconclusive if both Min/Max cardinality and AtLeastOnce are specified      $subContextMinMaxALO
        SubContext evaluation should be inconclusive if assertion is inconclusive for one or more children (regardless of failures and success) $subContextInconclusive
        If there is no range specified (Cardinality, AtLeastOnce is going to be false by default)
          SubContext evaluation should succeed if assertion succeeds for all children                               $subContextNoRangeSuccess
          SubContext evaluation should fail if assertion fails for one or more children                             $subContextNoRangeFailure
        If there is a Min/Max cardinality
          SubContext evaluation should succeed if assertion succeeds for number of children between [Min, Max] cardinality $subContextMinMaxSuccess
          SubContext evaluation should fail if assertion succeeds for number of children outside [Min, Max] cardinality    $subContextMinMaxFailure
        If there AtLeastOnce is true
          SubContext evaluation should succeed if assertion succeeds for one or more children                       $subContextALOSuccess
          SubContext evaluation should fail if assertion fails for all children                                     $subContextALOFailure
        SubContext evaluation should fail If not present behavior is FAIL and no element is found  $subContextNoElmFAIL
        SubContext evaluation should be inconclusive If not present behavior is INCONCLUSIVE and no element is found $subContextNoElmINC
        SubContext evaluation should pass If not present behavior is PASS and no element is found $subContextNoElmPASS
    */


  val c_1_1: C = C(1, 1, List(
    S(1, 1, Text("A")),
    S(2, 1, Text("B")),
  ))
  val c_1_2: C = C(1, 2, List(
    S(1, 1, Text("A")),
    S(2, 1, Text("B")),
  ))
  val c_1_3: C = C(1, 3, List(
    S(1, 1, Text("A")),
    S(2, 1, Text("C")),
  ))

  val c4: C = C(1, 1, List(
    c_1_1,
    c_1_2,
    c_1_3
  ))

  val plainText_1_A: PlainText = PlainText("1[1]", "A", ignoreCase = true, atLeastOnce = false, "FAIL")
  val plainText_1_X: PlainText = PlainText("1[1]", "X", ignoreCase = true, atLeastOnce = false, "FAIL")
  val plainText_2_B: PlainText = PlainText("2[1]", "B", ignoreCase = true, atLeastOnce = false, "FAIL")
  val plainText_1_A_AND_2_B: AND = AND(plainText_1_A, plainText_2_B)
  val plainTextExpInconclusive: PlainText = PlainText("1", "S12", ignoreCase = true, atLeastOnce = false, "FAIL")

  // All instances have 1[1] = A
  assert(eval(plainText_1_A, c_1_1) == Pass)
  assert(eval(plainText_1_A, c_1_2) == Pass)
  assert(eval(plainText_1_A, c_1_3) == Pass)

  // instance 1,2 have (1[1] = A, 2[1] = B) and 3 does not
  assert(eval(plainText_1_A_AND_2_B, c_1_1) == Pass)
  assert(eval(plainText_1_A_AND_2_B, c_1_2) == Pass)
  assert(eval(plainText_1_A_AND_2_B, c_1_3).isInstanceOf[Fail])

  // instance 1,2 have 2[1] = B and 3 has 2[1] != B
  assert(eval(plainText_2_B, c_1_1) == Pass)
  assert(eval(plainText_2_B, c_1_2) == Pass)
  assert(eval(plainText_2_B, c_1_3).isInstanceOf[Fail])

  assert(eval(plainTextExpInconclusive, c1).isInstanceOf[Inconclusive])

  assert( query(c2, "3[*]") == Success(Nil) )
  def subContextPathNotPopulated: MatchResult[EvalResult] = eval(SubContext(plainText_1_A, "3[*]", None, None), c2) === Pass

  // 3 is an invalid path
  def subContextPathInvalid: MatchResult[EvalResult] = {
    val exp = SubContext(plainText_1_A, "3", None, None)
    eval(exp, c2) === inconclusive(exp, c2.location, s"Invalid Path '3'")
  }

  // s0 is a simple element, querying it will fail
  def subContextPathUnreachable: MatchResult[EvalResult] = {
    val exp = SubContext(plainText_1_A, "1[*]", None, None)
    eval(exp, s0) === inconclusive(exp, s0.location, s"Unreachable Path '1[*]'")
  }

  def subContextMinMaxALO: MatchResult[EvalResult] = {
    val exp = SubContext(plainText_1_A, "2[*]", Some(Range(0, "*")), Some(true))
    eval(exp, c2) === inconclusive(exp, c2.location, "Min/Max Cardinality and AtLeastOnce were both specified, only one should be used in SubContext expression")
  }

  def subContextInconclusive: MatchResult[EvalResult] = {
    val subContext = SubContext(plainTextExpInconclusive, "2[*]", None, None)
    eval(subContext, c2) === Failures.subContextInconclusive(subContext, c2, List(inconclusive(plainTextExpInconclusive, c0.location, s"Invalid Path '1'"), inconclusive(plainTextExpInconclusive, c1.location, s"Invalid Path '1'")))
  }

  def subContextNoRangeSuccess: MatchResult[EvalResult] = {
    val subContext = SubContext(plainText_1_A, "1[*]", None, None)
    eval(subContext, c4) === Pass
  }

  def subContextNoRangeFailure: MatchResult[EvalResult] = {
    val subContext = SubContext(plainText_2_B, "1[*]", None, None)
    eval(subContext, c4) === Failures.subContext(subContext, c4, None, 3, List(
      Failures.plainText(plainText_2_B, List(S(1, 1, Text("C"))))))
  }

  def subContextMinMaxSuccess: MatchResult[EvalResult] = {
    val subContext = SubContext(plainText_1_A_AND_2_B, "1[*]", Some(Range(1, "2")), None)
    eval(subContext, c4) === Pass
  }

  def subContextMinMaxFailure: Seq[MatchResult[EvalResult]] = {
    // Success 3 outside Max Range
    val subContextMax = SubContext(plainText_1_A, "1[*]", Some(Range(1, "2")), None)
    val min = eval(subContextMax, c4) === Failures.subContext(subContextMax, c4, Some(Range(1, "2")), 3, List())

    // Success 2 outside Min Range
    val subContextMin = SubContext(plainText_1_A_AND_2_B, "1[*]", Some(Range(3, "*")), None)
    val max = eval(subContextMin, c4) === Failures.subContext(subContextMin, c4, Some(Range(3, "*")), 3, List(
      Failures.and(plainText_1_A_AND_2_B, c_1_3, Failures.plainText(plainText_2_B, List(S(1, 2, Text("C")))))
    ))

    Seq(min, max)
  }

  def subContextALOSuccess: MatchResult[EvalResult] = {
    // Success 2/3
    val subContextMin = SubContext(plainText_1_A_AND_2_B, "1[*]", None, Some(true))
    eval(subContextMin, c4) === Pass
  }

  def subContextALOFailure: MatchResult[EvalResult] = {
    val subContext = SubContext(plainText_1_X, "1[*]", None, None)
    eval(subContext, c4) === Failures.subContext(subContext, c4, None, 3, List(
      Failures.plainText(plainText_1_X, List(S(1, 1, Text("A")))),
      Failures.plainText(plainText_1_X, List(S(1, 1, Text("A")))),
      Failures.plainText(plainText_1_X, List(S(1, 1, Text("A"))))))
  }

  def subContextNoElmFAIL: MatchResult[EvalResult] = {
    val subContext = SubContext(plainText_1_A, "3[*]", None, None, "FAIL")
    eval(subContext, c2) === Failures.notPresentBehaviorFail(subContext, subContext.path, c2)
  }

  def subContextNoElmINC: MatchResult[EvalResult] = {
    val subContext = SubContext(plainText_1_A, "3[*]", None, None, "INCONCLUSIVE")
    eval(subContext, c2)  === Failures.notPresentBehaviorInconclusive(subContext, subContext.path, c2)
  }

  def subContextNoElmPASS: MatchResult[EvalResult] = {
    eval(SubContext(plainText_1_A, "3[*]", None, None), c2) === Pass
  }
}
