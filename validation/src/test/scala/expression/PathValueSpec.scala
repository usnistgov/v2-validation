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
            Pass
              (["A", B, C], ["A", "A", "A", As....]) $aloTall1
              (["B", "B", C], ["B", "B", Bs....]) $aloTall2
              (["C", "C", "C"], ["C", "C", "C", Cs....]) $aloTall3
            Fail
              ([A, B, C], [A, B, C]) $faloTall1
              ([A, B, C], [A, A, C]) $faloTall2
          Count
            Pass
              (Count = 1)
              ([A, "B", C], [D, "B", E, ...]) $aloToc11
              ([A, "B", "C"], [D, "B", "C"]) $aloToc12
              (["B", "B", "B"], [A, "B", C]) $aloToc13
              (["A", "B", "C"], ["A", "B", "C"]) $aloToc14
              ([A, "B", "C"], ["B", "B", "C"]) $aloToc15
              (Count = 2)
              ([A, "B", C], [D, "B", "B", ...]) $aloToc21
              ([A, "B", "C"], [D, "B", "B", "C", "C", ....]) $aloToc22
            Fail
              (Count = 1)
              ([A, B, C], [D, E, F]) $faloToc11
              ([B, B, B], [B, B, B]) $faloToc12
              ([A, B, C], [B, B, D]) $faloToc13
              (Count = 2)
              ([A, "B", C], [D, "B", E, ...]) $faloToc21
              ([A, "B", "C"], [D, "B", "C"]) $faloToc22
        All
          AtLeastOnce
            Pass
              (["A", "B", "C"], ["A", "B", "C", ....]) $allToalo1
              (["A", "B", "C"], ["A", "A", "B", "C", ....]) $allToalo2
              (["A", "B", "C"], ["A", "A", "B", "B", "C", "C", ....]) $allToalo3
            Fail
              (["A", "B", "C"], ["A", "B", ....]) $fallToalo1
              (["A", "B", "C"], ["A",  .....]) $fallToalo2
              (["A", "B", "C"], [X, Y, Z]) $fallToalo3
          All
            Pass
              (["A", "A", "A"], ["A", "A", As....]) $allToall1
            Fail
              (["A", "B", "C"], ["A", "B", "C"]) $fallToall1
              (["A", "A"], ["A", "A", "C"]) $fallToall2
          Count
            Pass
              (Count = 1)
              (["A", "B", "C"], ["A", "B", "C", ....]) $allToc11
              (Count = 2)
              (["A", "B", "C"], ["A", "A", "B", "B", "C", "C", ....]) $allToc12
            Fail
              (Count = 1)
              (["A", "B", "C"], ["A", "A", "B", "C", ....]) $fallToc11
              (["A", "B", "C"], ["A", "A", "B", "B", "C", "C", ....]) $fallToc12
              (["A", "B", "C"], ["A", Y, Z]) $fallToc13
              (Count = 2)
              (["A", "B", "C"], ["A", "A", "A", "B", "B", "C", "C", ....]) $fallToc21
              (["A", "B", "C"], ["A", "B", "C", ....]) $fallToc22
        Count
          AtLeastOnce
            Pass
              (Count = 1)
              (["A", "B", "C"], ["A", "X", "Y",....]) $cTalo11
              (["A", "B", "C"], ["A", "A", "X", "Y", ....]) $cTalo12
              (Count = 2)
              (["A", "B", "C"], ["A", "B", "X", "Y",....]) $cTalo21
              (["A", "B", "C"], ["A", "A", "C", "X", "Y", ....]) $cTalo22
            Fail
              (Count = 1)
              (["A", "B", "C"], ["X", "Y", "Z", ....]) $fcTalo11
              (["A", "B", "C"], ["A", "A", "B", "X", "Y", ....]) $fcTalo12
              (Count = 2)
              (["A", "B", "C"], ["A", "X", "Y",....]) $fcTalo21
              (["A", "B", "C"], ["A", "B", "C", "X", "Y", ....]) $fcTalo22
          All
            Pass
              (Count = 1)
              (["A", "B", "C"], ["A", "A", "A", As....]) $cTall11
              (["A", "B", "C"], ["B", "B", "B", Bs....]) $cTall12
              (Count = 2)
              (["A", "A", "C"], ["A", "A", "A", As....]) $cTall21
              (["A", "B", "B"], ["B", "B", "B", Bs....]) $cTall22
            Fail
              (Count = 1)
              (["A", "B", "C"], ["A", "B", "C",....]) $fcTall11
              (["A", "A", "C"], ["A", "A", "A", As....]) $fcTall12
              (Count = 2)
              (["A", "B", "C"], ["A", "A", "A", As....]) $fcTall21
          Count
            Pass
              (Count = 1 / Count = 1)
              (["A", "B", "C"], ["A", "X", "Y", ....]) $cTc111
              (["A", "B", "C"], ["A", "B", "B", "X", "Y", ....]) $cTc112
              (Count = 1 / Count = 2)
              (["A", "B", "C"], ["A", "A", "B", "C", ....]) $cTc121
              (Count = 2 / Count = 1)
              (["A", "B", "C"], ["A", "B", "C", "C", "X", "Y", ....]) $cTc211
              (Count = 2 / Count = 2)
              (["A", "B", "C"], ["A", "B", "B", "C", "C", "X", "Y", ....]) $cTc221
            Fail
              (Count = 1 / Count = 1)
              (["A", "B", "C"], ["X", "Y", "Z"]) $fcTc111
              (["A", "B", "C"], ["A", "B", "Y", ....]) $fcTc112
              (Count = 1 / Count = 2)
              (["A", "B", "C"], ["A", "A", "B", "B", "C", ....]) $fcTc121
              (Count = 2 / Count = 1)
              (["A", "B", "C"], ["A", "B", "B", "C", "C", "X", "Y", ....]) $fcTc211
              (Count = 2 / Count = 2)
              (["A", "B", "C"], ["A", "A", "B", "B", "C", "C", "X", "Y", ....]) $fcTc221
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

  def aloTall1 = shouldPass(
    MultiCompareMode.AtLeastOne(),
    List("A", "B", "C"),
    MultiCompareMode.All(),
    List("A", "A", "A", "A")
  )

  def aloTall2 = shouldPass(
    MultiCompareMode.AtLeastOne(),
    List("B", "B", "C"),
    MultiCompareMode.All(),
    List("B", "B", "B", "B")
  )

  def aloTall3 = shouldPass(
    MultiCompareMode.AtLeastOne(),
    List("C", "C", "C"),
    MultiCompareMode.All(),
    List("C", "C", "C", "C")
  )

  def faloTall1 = shouldFail(
    MultiCompareMode.AtLeastOne(),
    List("A", "B", "C"),
    MultiCompareMode.All(),
    List("A", "B", "C"),
  )

  def faloTall2 = shouldFail(
    MultiCompareMode.AtLeastOne(),
    List("A", "B", "C"),
    MultiCompareMode.All(),
    List("A", "A", "C"),
  )

  def aloToc11 = shouldPass(
    MultiCompareMode.AtLeastOne(),
    List("A", "B", "C"),
    MultiCompareMode.Count(1),
    List("D", "B", "E", "X")
  )

  def aloToc12 = shouldPass(
    MultiCompareMode.AtLeastOne(),
    List("A", "B", "C"),
    MultiCompareMode.Count(1),
    List("B", "D", "C", "X")
  )

  def aloToc13 = shouldPass(
    MultiCompareMode.AtLeastOne(),
    List("B", "B", "B"),
    MultiCompareMode.Count(1),
    List("A", "B", "C", "D")
  )

  def aloToc14 = shouldPass(
    MultiCompareMode.AtLeastOne(),
    List("A", "B", "C"),
    MultiCompareMode.Count(1),
    List("A", "B", "C")
  )

  def aloToc15 = shouldPass(
    MultiCompareMode.AtLeastOne(),
    List("A", "B", "C"),
    MultiCompareMode.Count(1),
    List("B", "B", "C")
  )

  def aloToc21 = shouldPass(
    MultiCompareMode.AtLeastOne(),
    List("A", "B", "C"),
    MultiCompareMode.Count(1),
    List("B", "B", "C")
  )

  def aloToc22 = shouldPass(
    MultiCompareMode.AtLeastOne(),
    List("A", "B", "C"),
    MultiCompareMode.Count(2),
    List("B", "B", "C")
  )

  def faloToc11 = shouldFail(
    MultiCompareMode.AtLeastOne(),
    List("A", "B", "C"),
    MultiCompareMode.Count(1),
    List("D", "E", "F")
  )

  def faloToc12 = shouldFail(
    MultiCompareMode.AtLeastOne(),
    List("B", "B", "B"),
    MultiCompareMode.Count(1),
    List("B", "B", "B")
  )

  def faloToc13 = shouldFail(
    MultiCompareMode.AtLeastOne(),
    List("A", "B", "C"),
    MultiCompareMode.Count(1),
    List("B", "B", "D")
  )

  def faloToc21 = shouldFail(
    MultiCompareMode.AtLeastOne(),
    List("A", "B", "C"),
    MultiCompareMode.Count(2),
    List("D", "B", "E")
  )

  def faloToc22 = shouldFail(
    MultiCompareMode.AtLeastOne(),
    List("A", "B", "C"),
    MultiCompareMode.Count(2),
    List("D", "B", "C")
  )

  def allToalo1 = shouldPass(
    MultiCompareMode.All(),
    List("A", "B", "C"),
    MultiCompareMode.AtLeastOne(),
    List("A", "B", "C", "X", "Y")
  )

  def allToalo2 = shouldPass(
    MultiCompareMode.All(),
    List("A", "B", "C"),
    MultiCompareMode.AtLeastOne(),
    List("A", "A", "B", "C", "X", "Y")
  )

  def allToalo3 = shouldPass(
    MultiCompareMode.All(),
    List("A", "B", "C"),
    MultiCompareMode.AtLeastOne(),
    List("A", "A", "B", "B", "C", "C", "X", "Y")
  )

  def fallToalo1 = shouldFail(
    MultiCompareMode.All(),
    List("A", "B", "C"),
    MultiCompareMode.AtLeastOne(),
    List("A", "B", "X", "Y")
  )

  def fallToalo2 = shouldFail(
    MultiCompareMode.All(),
    List("A", "B", "C"),
    MultiCompareMode.AtLeastOne(),
    List("A", "X", "Y")
  )

  def fallToalo3 = shouldFail(
    MultiCompareMode.All(),
    List("A", "B", "C"),
    MultiCompareMode.AtLeastOne(),
    List("X", "Y", "Z")
  )

  def allToall1 = shouldPass(
    MultiCompareMode.All(),
    List("A", "A", "A"),
    MultiCompareMode.All(),
    List("A", "A", "A", "A")
  )

  def fallToall1 = shouldFail(
    MultiCompareMode.All(),
    List("A", "B", "C"),
    MultiCompareMode.All(),
    List("A", "B", "C")
  )

  def fallToall2 = shouldFail(
    MultiCompareMode.All(),
    List("A", "A"),
    MultiCompareMode.All(),
    List("A", "A", "C")
  )

  def allToc11 = shouldPass(
    MultiCompareMode.All(),
    List("A", "B", "C"),
    MultiCompareMode.Count(1),
    List("A", "B", "C", "X", "Y")
  )

  def allToc12 = shouldPass(
    MultiCompareMode.All(),
    List("A", "B", "C"),
    MultiCompareMode.Count(2),
    List("A", "A", "B", "B", "C", "C", "X", "Y")
  )

  def fallToc11 = shouldFail(
    MultiCompareMode.All(),
    List("A", "B", "C"),
    MultiCompareMode.Count(1),
    List("A", "A", "B", "C", "X", "Y")
  )

  def fallToc12 = shouldFail(
    MultiCompareMode.All(),
    List("A", "B", "C"),
    MultiCompareMode.Count(1),
    List("A", "A", "B", "B", "C", "C", "X", "Y")
  )

  def fallToc13 = shouldFail(
    MultiCompareMode.All(),
    List("A", "B", "C"),
    MultiCompareMode.Count(1),
    List("A", "X", "Y")
  )

  def fallToc21 = shouldFail(
    MultiCompareMode.All(),
    List("A", "B", "C"),
    MultiCompareMode.Count(2),
    List("A", "A", "A", "B", "B", "C", "C", "X", "Y")
  )

  def fallToc22 = shouldFail(
    MultiCompareMode.All(),
    List("A", "B", "C"),
    MultiCompareMode.Count(2),
    List("A", "B", "C", "X", "Y")
  )

  def cTalo11 = shouldPass(
    MultiCompareMode.Count(1),
    List("A", "B", "C"),
    MultiCompareMode.AtLeastOne(),
    List("A", "X", "Y", "Z")
  )

  def cTalo12 = shouldPass(
    MultiCompareMode.Count(1),
    List("A", "B", "C"),
    MultiCompareMode.AtLeastOne(),
    List("A", "A", "X", "Y", "Z")
  )

  def cTalo21 = shouldPass(
    MultiCompareMode.Count(2),
    List("A", "B", "C"),
    MultiCompareMode.AtLeastOne(),
    List("A", "B", "X", "Y", "Z")
  )

  def cTalo22 = shouldPass(
    MultiCompareMode.Count(2),
    List("A", "B", "C"),
    MultiCompareMode.AtLeastOne(),
    List("A", "A", "C", "X", "Y", "Z")
  )

  def fcTalo11 = shouldFail(
    MultiCompareMode.Count(1),
    List("A", "B", "C"),
    MultiCompareMode.AtLeastOne(),
    List("X", "Y", "Z", "D")
  )

  def fcTalo12 = shouldFail(
    MultiCompareMode.Count(1),
    List("A", "B", "C"),
    MultiCompareMode.AtLeastOne(),
    List("A", "A", "B", "X", "Y", "Z", "D")
  )

  def fcTalo21 = shouldFail(
    MultiCompareMode.Count(2),
    List("A", "B", "C"),
    MultiCompareMode.AtLeastOne(),
    List("A", "X", "Y", "Z", "D")
  )

  def fcTalo22 = shouldFail(
    MultiCompareMode.Count(2),
    List("A", "B", "C"),
    MultiCompareMode.AtLeastOne(),
    List("A", "B", "C", "X", "Y", "Z", "D")
  )

  def cTall11 = shouldPass(
    MultiCompareMode.Count(1),
    List("A", "B", "C"),
    MultiCompareMode.All(),
    List("A", "A", "A", "A", "A")
  )

  def cTall12 = shouldPass(
    MultiCompareMode.Count(1),
    List("A", "B", "C"),
    MultiCompareMode.All(),
    List("B", "B", "B", "B", "B")
  )

  def cTall21 = shouldPass(
    MultiCompareMode.Count(2),
    List("A", "A", "C"),
    MultiCompareMode.All(),
    List("A", "A", "A", "A", "A")
  )

  def cTall22 = shouldPass(
    MultiCompareMode.Count(2),
    List("A", "B", "B"),
    MultiCompareMode.All(),
    List("B", "B", "B", "B", "B")
  )

  def fcTall11 = shouldFail(
    MultiCompareMode.Count(1),
    List("A", "B", "C"),
    MultiCompareMode.All(),
    List("A", "B", "C", "X", "Y")
  )

  def fcTall12 = shouldFail(
    MultiCompareMode.Count(1),
    List("A", "A", "C"),
    MultiCompareMode.All(),
    List("A", "A", "A", "A", "A")
  )

  def fcTall21 = shouldFail(
    MultiCompareMode.Count(2),
    List("A", "B", "C"),
    MultiCompareMode.All(),
    List("A", "A", "A", "A", "A")
  )

  def cTc111 = shouldPass(
    MultiCompareMode.Count(1),
    List("A", "B", "C"),
    MultiCompareMode.Count(1),
    List("A", "X", "Y", "Z")
  )

  def cTc112 = shouldPass(
    MultiCompareMode.Count(1),
    List("A", "B", "C"),
    MultiCompareMode.Count(1),
    List("A", "B", "B", "X", "Y", "D")
  )

  def cTc121 = shouldPass(
    MultiCompareMode.Count(1),
    List("A", "B", "C"),
    MultiCompareMode.Count(2),
    List("A", "A", "B", "C", "X", "Y", "D")
  )

  def cTc211 = shouldPass(
    MultiCompareMode.Count(2),
    List("A", "B", "C"),
    MultiCompareMode.Count(1),
    List("A", "B", "C", "C", "X", "Y", "D")
  )

  def cTc221 = shouldPass(
    MultiCompareMode.Count(2),
    List("A", "B", "C"),
    MultiCompareMode.Count(2),
    List("A", "B", "B", "C", "C", "X", "Y", "D")
  )

  def fcTc111 = shouldFail(
    MultiCompareMode.Count(1),
    List("A", "B", "C"),
    MultiCompareMode.Count(1),
    List("X", "Y", "Z")
  )

  def fcTc112 = shouldFail(
    MultiCompareMode.Count(1),
    List("A", "B", "C"),
    MultiCompareMode.Count(1),
    List("A", "B", "X", "Y", "Z")
  )

  def fcTc121 = shouldFail(
    MultiCompareMode.Count(1),
    List("A", "B", "C"),
    MultiCompareMode.Count(2),
    List("A", "A", "B", "B", "C", "X", "Y", "Z")
  )

  def fcTc211 = shouldFail(
    MultiCompareMode.Count(2),
    List("A", "B", "C"),
    MultiCompareMode.Count(1),
    List("A", "B", "B", "C", "C", "X", "Y", "X", "Y", "Z")
  )

  def fcTc221 = shouldFail(
    MultiCompareMode.Count(2),
    List("A", "B", "C"),
    MultiCompareMode.Count(2),
    List("A", "A", "B", "B", "C", "C", "X", "Y", "X", "Y", "Z")
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