package expression

import expression.EvalResult.{Fail, Inconclusive, Pass}
import hl7.v2.instance.Query.queryAsSimple
import hl7.v2.instance.{ Element, Text}
import org.specs2.Specification
import org.specs2.matcher.MatchResult

import scala.util.Success

trait ComplexPathValueSpec extends Specification with Evaluator with Mocks {

  /*
  ComplexPathValueSpec
      ComplexPathValue should pass if both paths are not populated                        $complexPathValueBothPathNotPopulated
      ComplexPathValue evaluation should be inconclusive if the path is invalid           $complexPathValuePathInvalid
      ComplexPathValue evaluation should be inconclusive if the path is unreachable       $complexPathValuePathUnreachable
      ComplexPathValue evaluation should be inconclusive if applied to incompatible elements (Complex/Simple)        $complexPathNotFieldOrComponent
      ComplexPathValue evaluation should fail if one of the children in one path does not satisfy operator constraint applied to its counterpart (eg : A^B^C vs A^X^C, L1^A&B&C vs L1^A&X&C)   $complexPathChildNoMatch
      ComplexPathValue evaluation should fail if one of the occurrences in one path does not satisfy operator constraint applied to its counterpart (eg: A^B^C~X^Y^Z vs A^B^C~X^Y^T)    $complexPathOccurrenceFail
      ComplexPathValue should pass if all children are equally populated and satisfy operator constraint (eg: A&B&C vs A&B&C, L1^A&B&C vs L1^A&B&C)         $complexPathValueEqPopulated
      ComplexPathValue should pass if all occurrences are equally populated and satisfy operator constraint (eg: A^B^C~X^Y^Z vs A^B^C~X^Y^Z)         $complexPathValueEqPopulatedOccurrence
      Strict Mode
        ComplexPathValue should fail if only one path is populated                                                                $complexPathValueOnePathPopulatedS
        ComplexPathValue should fail if one of the children within one path is populated and not in the other (eg: A^^C vs A^B^C, L1^A&B&C vs L1^A&&C)   $complexPathValueOnePathChildPopulatedS
        ComplexPathValue should fail if one of the occurrences has no match (all others satisfy operator constraint) (e.g A^B^C~X^Y^Z vs A^B^C)          $complexPathNotSameOccurrenceNumberStrict
      Non-Strict Mode
        ComplexPathValue should pass if only one path is populated                                                                  $complexPathValueOnePathPopulated
        ComplexPathValue should pass if one of the children within one path is populated and not in the other (eg: A^^C vs A^B^C, L1^A&B&C vs L1^A&&C)
        ComplexPathValue should pass if one of the occurrences has no match (all others satisfy operator constraint) (e.g A^B^C~X^Y^Z vs A^B^C)     $complexPathNotSameOccurrenceNumberNotStrict

      ComplexPathValue evaluation should fail If not present behavior is FAIL and both paths not found                      $complexPathValueNoElmFAIL
      ComplexPathValue evaluation should be inconclusive If not present behavior is INCONCLUSIVE and both paths not found   $complexPathValueNoElmINC
      ComplexPathValue evaluation should pass If not present behavior is PASS and both paths not found                      $complexPathValueNoElmPASS
  */

  //c1.4[1] is not populated
  assert( queryAsSimple(c1, "4[1]") == Success(Nil) )
  def complexPathValueBothPathNotPopulated: MatchResult[EvalResult] =
    eval( ComplexPathValue("4[1]", Operator.LT, "4[1]"), c1 ) === Pass

  def complexPathValueNoElmFAIL = {
    val f = ComplexPathValue("4[1]", Operator.LT, "4[1]", false, ComparisonMode(false, false), "FAIL")
    eval(f, c1) === Failures.notPresentBehaviorFail(f, s"[ ${f.path1}, ${f.path2} ]", c1)
  }
  def complexPathValueNoElmINC = {
    val f = ComplexPathValue("4[1]", Operator.LT, "4[1]", false, ComparisonMode(false, false), "INCONCLUSIVE")
    eval(f, c1) === Failures.notPresentBehaviorInconclusive(f, s"[ ${f.path1}, ${f.path2} ]", c1)
  }
  def complexPathValueNoElmPASS = {
    val f = ComplexPathValue("4[1]", Operator.LT, "4[1]", false, ComparisonMode(false, false), "PASS")
    eval(f, c1) === Pass
  }

  // 4 is an invalid path
  def complexPathValuePathInvalid = {
    val p = ComplexPathValue("4", Operator.LT, "4")
    eval( p, c0 ) === inconclusive(p, c0.location, s"Invalid Path '${p.path1}'")
  }

  // s0 is a simple element, querying it will fail
  def complexPathValuePathUnreachable = Seq(true, false) map { b =>
    val p = ComplexPathValue("4[1]", Operator.LT, "4[1]")
    eval( p, s0 ) === inconclusive(p, s0.location, s"Unreachable Path '${p.path1}'")
  }

  //  ComplexPathValue evaluation should be inconclusive if applied to elements other than Field/Component        $complexPathNotFieldOrComponent
  def complexPathNotFieldOrComponent = {
    val e = ComplexPathValue("4[*]", Operator.EQ, "2[*]")
    eval(e, cx) === inconclusiveList(e, c0, Failures.inconclusivePathComparison(e, c0, s0)::Nil)
  }

  //-------------------------------------------------------------------------
  //------------------------ complexPathChildNoMatch (strict/noStrict) ------
  //-------------------------------------------------------------------------

  def complexPathChildNoMatchLvl1 = {
    // A&B&C vs A&X&C
    val c1s = (S(1,1, Text("A")), S(2,1, Text("B")), S(3,1, Text("C")))
    val c2s = (S(1,1, Text("A")), S(2,1, Text("X")), S(3,1, Text("C")))
    val C1 = C(1,1, List(c1s._1, c1s._2, c1s._3))
    val C2 = C(2,1, List(c2s._1, c2s._2, c2s._3))
    val context = C(1,1, List(C1, C2))

    Seq(true, false).map(
      strict => {
        val e = ComplexPathValue("1[*]", Operator.EQ, "2[*]", strict)
        eval(e, context) === failureList(e, context, List(
          // B vs X
          Failures.complexPathValue(e, c1s._2, c2s._2),
        ))
      }
    )

  }

  def complexPathChildNoMatchLvl2 = {
    // Level 2 A&B&C vs A&X&C
    val c1s = (S(1,1, Text("A")), S(2,1, Text("B")), S(3,1, Text("C")))
    val c2s = (S(1,1, Text("A")), S(2,1, Text("X")), S(3,1, Text("C")))
    val C1 = C(2,1, List(c1s._1, c1s._2, c1s._3))
    val C2 = C(2,1, List(c2s._1, c2s._2, c2s._3))

    // Level 1 L1^A&B&C vs L1^A&X&C
    val c11s = (S(1, 1, Text("L1")), C1)
    val c22s = (S(1, 1, Text("L1")), C2)
    val C11 = C(1,1, List(c11s._1, c11s._2))
    val C22 = C(2,1, List(c22s._1, c22s._2))
    val context = C(1,1, List(C11, C22))

    Seq(true, false).map(
      strict => {
        val e = ComplexPathValue("1[*]", Operator.EQ, "2[*]", strict)
        eval(e, context) === failureList(e, context, List(
          // B vs X
          Failures.complexPathValue(e, c1s._2, c2s._2),
        ))
      }
    )
  }

  def complexPathChildNoMatch = Seq(complexPathChildNoMatchLvl1, complexPathChildNoMatchLvl2).flatten

  //-------------------------------------------------------------------------
  //------------------------ complexPathOccurrenceFail (strict/noStrict) ----
  //-------------------------------------------------------------------------

  def complexPathOccurrenceFail = {
    // A^B^C~X^Y^Z vs A^B^C~X^Y^T
    val c1_1s = (S(1,1, Text("A")), S(2,1, Text("B")), S(3,1, Text("C")))
    val c1_2s = (S(1,1, Text("X")), S(2,1, Text("Y")), S(3,1, Text("Z")))

    val c2_1s = (S(1,1, Text("A")), S(2,1, Text("B")), S(3,1, Text("C")))
    val c2_2s = (S(1,1, Text("X")), S(2,1, Text("Y")), S(3,1, Text("T")))

    val C11 = C(1,1, List(c1_1s._1, c1_1s._2, c1_1s._3))
    val C12 = C(1,2, List(c1_2s._1, c1_2s._2, c1_2s._3))

    val C21 = C(2,1, List(c2_1s._1, c2_1s._2, c2_1s._3))
    val C22 = C(2,2, List(c2_2s._1, c2_2s._2, c2_2s._3))

    val context = C(1,1, List(C11, C12, C21, C22))

    Seq(true, false).map(
      strict => {
        val e = ComplexPathValue("1[*]", Operator.EQ, "2[*]", strict)
        eval(e, context) === failureList(e, context, List(
          // Z vs T
          Failures.complexPathValue(e, c1_2s._3, c2_2s._3),
        ))
      }
    )
  }

  //-------------------------------------------------------------------------
  //------------------------ complexPathValueEqPopulated (strict/noStrict) --
  //-------------------------------------------------------------------------

  def complexPathValueEqPopulatedLvl1 = {
    // A&B&C vs A&B&C
    val c1s = (S(1,1, Text("A")), S(2,1, Text("B")), S(3,1, Text("C")))
    val c2s = (S(1,1, Text("A")), S(2,1, Text("B")), S(3,1, Text("C")))
    val C1 = C(1,1, List(c1s._1, c1s._2, c1s._3))
    val C2 = C(2,1, List(c2s._1, c2s._2, c2s._3))
    val context = C(1,1, List(C1, C2))

    Seq(true, false).map(
      strict => {
        val e = ComplexPathValue("1[*]", Operator.EQ, "2[*]", strict)
        eval(e, context) === Pass
      }
    )
  }

  def complexPathValueEqPopulatedLvl2 = {
    // Level 2 A&B&C vs A&X&C
    val c1s = (S(1,1, Text("A")), S(2,1, Text("B")), S(3,1, Text("C")))
    val c2s = (S(1,1, Text("A")), S(2,1, Text("B")), S(3,1, Text("C")))
    val C1 = C(2,1, List(c1s._1, c1s._2, c1s._3))
    val C2 = C(2,1, List(c2s._1, c2s._2, c2s._3))

    // Level 1 L1^A&B&C vs L1^A&X&C
    val c11s = (S(1, 1, Text("L1")), C1)
    val c22s = (S(1, 1, Text("L1")), C2)
    val C11 = C(1,1, List(c11s._1, c11s._2))
    val C22 = C(2,1, List(c22s._1, c22s._2))
    val context = C(1,1, List(C11, C22))

    Seq(true, false).map(
      strict => {
        val e = ComplexPathValue("1[*]", Operator.EQ, "2[*]", strict)
        eval(e, context) === Pass
      }
    )
  }

  def complexPathValueEqPopulated = Seq(complexPathValueEqPopulatedLvl1, complexPathValueEqPopulatedLvl2).flatten

  //------------------------------------------------------------------------------------
  //------------------------ $complexPathValueEqPopulatedOccurrence (strict/noStrict) --
  //------------------------------------------------------------------------------------

  def complexPathValueEqPopulatedOccurrence = {
    // A^B^C~X^Y^Z vs A^B^C~X^Y^T
    val c1_1s = (S(1,1, Text("A")), S(2,1, Text("B")), S(3,1, Text("C")))
    val c1_2s = (S(1,1, Text("X")), S(2,1, Text("Y")), S(3,1, Text("Z")))

    val c2_1s = (S(1,1, Text("A")), S(2,1, Text("B")), S(3,1, Text("C")))
    val c2_2s = (S(1,1, Text("X")), S(2,1, Text("Y")), S(3,1, Text("Z")))

    val C11 = C(1,1, List(c1_1s._1, c1_1s._2, c1_1s._3))
    val C12 = C(1,2, List(c1_2s._1, c1_2s._2, c1_2s._3))

    val C21 = C(2,1, List(c2_1s._1, c2_1s._2, c2_1s._3))
    val C22 = C(2,2, List(c2_2s._1, c2_2s._2, c2_2s._3))

    val context = C(1,1, List(C11, C12, C21, C22))

    Seq(true, false).map(
      strict => {
        val e = ComplexPathValue("1[*]", Operator.EQ, "2[*]", strict)
        eval(e, context) === Pass
      }
    )
  }

  //------------------------------------------------------------------------------------
  //------------------------ $complexPathValueOnePathPopulated (strict) ----------------
  //------------------------------------------------------------------------------------

  def complexPathValueOnePathPopulatedS = {
    // A&B&C vs
    val c1s = (S(1,1, Text("A")), S(2,1, Text("B")), S(3,1, Text("C")))
    val C1 = C(1,1, List(c1s._1, c1s._2, c1s._3))
    val context = C(1,1, List(C1))

    val e = ComplexPathValue("1[*]", Operator.EQ, "2[*]", true)
    // C1[1] no match in context.2[1]
    eval(e, context) === Failures.complexPathValue(e, C1, context, "2[*]")
  }

  //------------------------------------------------------------------------------------
  //------------------------ $complexPathValueOnePathChildPopulatedS (strict) ----------
  //------------------------------------------------------------------------------------

  def complexPathValueOnePathChildPopulatedSLvl1 = {
    // A&B&C vs A&&C
    val c1s = (S(1,1, Text("A")), S(2,1, Text("B")), S(3,1, Text("C")))
    val c2s = (S(1,1, Text("A")), S(3,1, Text("C")))
    val C1 = C(1,1, List(c1s._1, c1s._2, c1s._3))
    val C2 = C(2,1, List(c2s._1, c2s._2))
    val context = C(1,1, List(C1, C2))


    val e = ComplexPathValue("1[*]", Operator.EQ, "2[*]", true)
    eval(e, context) === failureList(e, context, List(
      // C1.2[1] no match in C2.2[1]
      Failures.complexPathValue(e, c1s._2, makePath(C2, 2, c1s._2.instance)),
    ))
  }

  def complexPathValueOnePathChildPopulatedSLvl2 = {
    // Level 2 A&B&C vs A&&C
    val c1s = (S(1,1, Text("A")), S(2,1, Text("B")), S(3,1, Text("C")))
    val c2s = (S(1,1, Text("A")), S(3,1, Text("C")))
    val C1 = C(2,1, List(c1s._1, c1s._2, c1s._3))
    val C2 = C(2,1, List(c2s._1, c2s._2))

    // Level 1 L1^A&B&C vs L1^A&&C
    val c11s = (S(1, 1, Text("L1")), C1)
    val c22s = (S(1, 1, Text("L1")), C2)
    val C11 = C(1,1, List(c11s._1, c11s._2))
    val C22 = C(2,1, List(c22s._1, c22s._2))
    val context = C(1,1, List(C11, C22))

    val e = ComplexPathValue("1[*]", Operator.EQ, "2[*]", true)
    eval(e, context) === failureList(e, context, List(
      // C1.2[1] no match in C2.2[1]
      Failures.complexPathValue(e, c1s._2, makePath(C2, 2, c1s._2.instance)),
    ))
  }

  def complexPathValueOnePathChildPopulatedS = Seq(complexPathValueOnePathChildPopulatedSLvl1,complexPathValueOnePathChildPopulatedSLvl2)

  //------------------------------------------------------------------------------------
  //------------------------ $complexPathNotSameOccurrenceNumberStrict (strict) --------
  //------------------------------------------------------------------------------------

  def complexPathNotSameOccurrenceNumberStrict = {
    // A&B&C~X&Y&Z vs A&B&C
    val c1_1s = (S(1,1, Text("A")), S(2,1, Text("B")), S(3,1, Text("C")))
    val c1_2s = (S(1,1, Text("X")), S(2,1, Text("Y")), S(3,1, Text("Z")))
    val c2_1s = (S(1,1, Text("A")), S(2,1, Text("B")), S(3,1, Text("C")))

    val C11 = C(1,1, List(c1_1s._1, c1_1s._2, c1_1s._3))
    val C12 = C(1,2, List(c1_2s._1, c1_2s._2, c1_2s._3))

    val C2 = C(2,1, List(c2_1s._1, c2_1s._2, c2_1s._3))
    val context = C(1,1, List(C11, C12, C2))

    val e = ComplexPathValue("1[*]", Operator.EQ, "2[*]", true)
    eval(e, context) === failureList(e, context, List(
      // C1.2[1] no match in C2.2[1]
      Failures.complexPathValue(e, C12, makePathForOccurrence(C2, "2[*]", C12.instance)),
    ))
  }

  //------------------------------------------------------------------------------------
  //------------------------ $complexPathValueOnePathPopulated (noStrict) --------------
  //------------------------------------------------------------------------------------

  def complexPathValueOnePathPopulated = {
    // A&B&C vs
    val c1s = (S(1,1, Text("A")), S(2,1, Text("B")), S(3,1, Text("C")))
    val C1 = C(1,1, List(c1s._1, c1s._2, c1s._3))
    val context = C(1,1, List(C1))


    val e = ComplexPathValue("1[*]", Operator.EQ, "2[*]", false)
    eval(e, context) === Pass
  }

  //------------------------------------------------------------------------------------
  //------------------------ $complexPathValueOnePathChildPopulated (noStrict) ---------
  //------------------------------------------------------------------------------------

  def complexPathValueOnePathChildPopulatedLvl1 = {
    // A&B&C vs A&&C
    val c1s = (S(1,1, Text("A")), S(2,1, Text("B")), S(3,1, Text("C")))
    val c2s = (S(1,1, Text("A")), S(3,1, Text("C")))
    val C1 = C(1,1, List(c1s._1, c1s._2, c1s._3))
    val C2 = C(2,1, List(c2s._1, c2s._2))
    val context = C(1,1, List(C1, C2))


    val e = ComplexPathValue("1[*]", Operator.EQ, "2[*]", false)
    eval(e, context) === Pass
  }

  def complexPathValueOnePathChildPopulatedLvl2 = {
    // Level 2 A&B&C vs A&&C
    val c1s = (S(1,1, Text("A")), S(2,1, Text("B")), S(3,1, Text("C")))
    val c2s = (S(1,1, Text("A")), S(3,1, Text("C")))
    val C1 = C(2,1, List(c1s._1, c1s._2, c1s._3))
    val C2 = C(2,1, List(c2s._1, c2s._2))

    // Level 1 L1^A&B&C vs L1^A&&C
    val c11s = (S(1, 1, Text("L1")), C1)
    val c22s = (S(1, 1, Text("L1")), C2)
    val C11 = C(1,1, List(c11s._1, c11s._2))
    val C22 = C(2,1, List(c22s._1, c22s._2))
    val context = C(1,1, List(C11, C22))

    val e = ComplexPathValue("1[*]", Operator.EQ, "2[*]", false)
    eval(e, context) === Pass
  }

  def complexPathValueOnePathChildPopulated = Seq(complexPathValueOnePathChildPopulatedLvl1, complexPathValueOnePathChildPopulatedLvl2)

  //------------------------------------------------------------------------------------
  //------------------------ $complexPathNotSameOccurrenceNumberStrict (noStrict) ------
  //------------------------------------------------------------------------------------

  def complexPathNotSameOccurrenceNumberNotStrict = {
    // A&B&C~X&Y&Z vs A&B&C
    val c1_1s = (S(1,1, Text("A")), S(2,1, Text("B")), S(3,1, Text("C")))
    val c1_2s = (S(1,1, Text("X")), S(2,1, Text("Y")), S(3,1, Text("Z")))
    val c2_1s = (S(1,1, Text("A")), S(2,1, Text("B")), S(3,1, Text("C")))

    val C11 = C(1,1, List(c1_1s._1, c1_1s._2, c1_1s._3))
    val C12 = C(1,2, List(c1_2s._1, c1_2s._2, c1_2s._3))

    val C2 = C(2,1, List(c2_1s._1, c2_1s._2, c2_1s._3))
    val context = C(1,1, List(C11, C12, C2))

    val e = ComplexPathValue("1[*]", Operator.EQ, "2[*]", false)
    eval(e, context) === Pass
  }

  // HELPERS

  def inconclusiveList(pv: ComplexPathValue, context: Element, inconclusive: List[Inconclusive]) = {
    Failures.complexPathValue(pv, context, inconclusive)
  }

  def failureList(pv: ComplexPathValue, context: Element, failures: List[Fail]) = {
    Failures.complexPathValue(pv, context, failures)
  }

  def makePath(e: Element, p: Int, i: Int) = s"${e.location.path}(${e.location.desc}).${p}[${i}]"
  def makePathForOccurrence(context: Element, path: String, i: Int) = s"${context.location.path}.${path} (instance : ${i})"

}