package expression

import expression.EvalResult.{Reason, Trace, Inconclusive, Pass}
import hl7.v2.instance.Query._
import hl7.v2.instance.{Number, Text}
import org.specs2.Specification

import scala.util.Success


trait PathValueSpec extends Specification with Evaluator with Mocks {

  /*
  PathValueSpec
      PathValue should pass if both paths are not populated                         $pathValueBothPathNotPopulated
      PathValue evaluation should be inconclusive if the path is complex           $pathValuePathComplex
      PathValue evaluation should be inconclusive if the path is invalid           $pathValuePathInvalid
      PathValue evaluation should be inconclusive if the path is unreachable       $pathValuePathUnreachable
      PathValue should fail if only one path is populated                          $pathValueOnePathPopulated
      Single (Mode: All, AtLeastOnce, One, Count = 1)
        PathValue should pass if operator = < and path1.value < path2.value          $pathValuePass
        PathValue should fail if operator = < and path1.value > path2.value          $pathValueFail
      Multiple
        AtLeastOnce
          AtLeastOnce
            Pass
              ([A, "B", C], [D, "B", E, ....]) $aloTalo1
              (["A", "B", C], ["A", "B", ....]) $aloTalo2
              ([A, "B", C], [D, "B", "B", ....]) $aloTalo3
              (["B", "B", "B"], ["B", "B", "B"]) $aloTalo4
            Fail
              ([A, B, C], [D, E, F]) $faloTalo1
          All
          Count
        All
          AtLeastOnce
          All
          Count
        Count
          AtLeastOnce
          All
          Count
      PathValue evaluation should fail If not present behavior is FAIL and both paths not found  $pathValueNoElmFAIL
      PathValue evaluation should be inconclusive If not present behavior is INCONCLUSIVE and both paths not found $pathValueNoElmINC
      PathValue evaluation should pass If not present behavior is PASS and both paths not found $pathValueNoElmPASS
  */

  //c1.4[1] is not populated
  assert( queryAsSimple(c1, "4[1]") == Success(Nil) )
  def pathValueBothPathNotPopulated =
    eval( PathValue("4[1]", Operator.LT, "4[1]"), c1 ) === Pass

  def pathValueNoElmFAIL = {
    val f = PathValue("4[1]", Operator.LT, "4[1]", ComparisonMode(false, false), "FAIL")
    eval(f, c1) === Failures.notPresentBehaviorFail(f, s"[ ${f.path1}, ${f.path2} ]", c1)
  }
  def pathValueNoElmINC = {
    val f = PathValue("4[1]", Operator.LT, "4[1]", ComparisonMode(false, false), "INCONCLUSIVE")
    eval(f, c1) === Failures.notPresentBehaviorInconclusive(f, s"[ ${f.path1}, ${f.path2} ]", c1)
  }
  def pathValueNoElmPASS = {
    val f = PathValue("4[1]", Operator.LT, "4[1]", ComparisonMode(false, false), "PASS")
    eval(f, c1) === Pass
  }

  // c1.2[3] is complex
  def pathValuePathComplex = {
    val p = PathValue("2[3]", Operator.LT, "2[3]")
    eval( p, c2 ) ===
      inconclusive(p, c2.location, "Path resolution returned at least one complex element")
  }

  // 4 is an invalid path
  def pathValuePathInvalid = {
    val p = PathValue("4", Operator.LT, "4")
    eval( p, c0 ) === inconclusive(p, c0.location, s"Invalid Path '${p.path1}'")
  }

  // s0 is a simple element, querying it will fail
  def pathValuePathUnreachable = Seq(true, false) map { b =>
    val p = PathValue("4[1]", Operator.LT, "4[1]")
    eval( p, s0 ) === inconclusive(p, s0.location, s"Unreachable Path '${p.path1}'")
  }

  // The following value will be used in the next tests

  private val `c1.1[1]`  = queryAsSimple(c1, "1[1]").get.head
  assert( `c1.1[1]`.value == Text("S11") )

  private val `c1.3[1]`  = queryAsSimple(c1, "3[1]").get.head
  assert( `c1.3[1]`.value == Text("S3") )

  private val `c2.5[1]` = queryAsSimple(c2, "5[1]").get.head
  assert( `c2.5[1]`.value == Number("51") )


  def pathValueOnePathPopulated = {
    val p1 = PathValue("3[1]", Operator.LT, "4[1]")
    val p2 = PathValue("4[1]", Operator.LT, "3[1]")
    eval( p1, c1 ) === Failures.pathValue(p1, `c1.3[1]`, "4[1]") and
    eval( p2, c1 ) === Failures.pathValue(p2, `c1.3[1]`, "4[1]")
  }

  def pathValueManyElems = {
    val p = PathValue("2[*]", Operator.LT, "3[*]")
    val p1 = s"${c1.location.path}.${p.path1}"
    val p2 = s"${c1.location.path}.${p.path2}"
    val m = s"path1($p1) and path2($p2) resolution returned respectively 3 and 3 elements."
    eval( p, c1 ) === Inconclusive( Trace(p, Reason(c1.location, m)::Nil ) )
  }

  def pathValuePass = eval(PathValue("3[1]", Operator.EQ, "3[2]"), c1) === Pass

  def pathValueFail = {
    val p = PathValue("1[1]", Operator.EQ, "3[1]")
    eval(p, c1) === Failures.pathValue(p, List(`c1.1[1]`), List(`c1.3[1]`))
  }

  def aloTalo1() = shouldPass(
    MultiCompareMode.AtLeastOne(),
    List("A", "B", "C"),
    MultiCompareMode.AtLeastOne(),
    List("D", "B", "E", "X", "Y")
  )
  def aloTalo2() = shouldPass(
    MultiCompareMode.AtLeastOne(),
    List("A", "B", "C"),
    MultiCompareMode.AtLeastOne(),
    List("A", "B", "E", "X", "Y")
  )
  def aloTalo3() = shouldPass(
    MultiCompareMode.AtLeastOne(),
    List("A", "B", "C"),
    MultiCompareMode.AtLeastOne(),
    List("D", "B", "B", "X", "Y")
  )
  def aloTalo4() = shouldPass(
    MultiCompareMode.AtLeastOne(),
    List("B", "B", "B"),
    MultiCompareMode.AtLeastOne(),
    List("B", "B", "B")
  )
  def faloTalo1() = shouldFail(
    MultiCompareMode.AtLeastOne(),
    List("A", "B", "C"),
    MultiCompareMode.AtLeastOne(),
    List("D", "E", "F")
  )

  def shouldPass(m1: MultiCompareMode, elem1: List[String], m2: MultiCompareMode, elem2: List[String]) = {
    val p = PathValue("1[*]", Operator.EQ, "2[*]", ComparisonMode(false, false), "PASS", m1, m2)
    eval(p, makeComplex(elem1, elem2)._1) === Pass
  }

  def shouldFail(m1: MultiCompareMode, elem1: List[String], m2: MultiCompareMode, elem2: List[String]) = {
    val p = PathValue("1[*]", Operator.EQ, "2[*]", ComparisonMode(false, false), "PASS", m1, m2)
    val mock = makeComplex(elem1, elem2)
    eval(p, mock._1) === Failures.pathValue(p, mock._2, mock._3)
  }

  def makeComplex(elem1: List[String], elem2: List[String]): (C, List[S], List[S]) =  {
    val pos1 = (elem1 zipWithIndex).map(e => S(1, e._2 + 1, Text(e._1)))
    val pos2 = (elem2 zipWithIndex).map(e => S(2, e._2 + 1, Text(e._1)))

    (C(1, 1, pos1:::pos2), pos1, pos2)
  }

}