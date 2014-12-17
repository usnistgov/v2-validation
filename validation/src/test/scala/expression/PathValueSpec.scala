package expression

import expression.EvalResult.{Reason, Trace, Inconclusive, Pass}
import hl7.v2.instance.Query._
import hl7.v2.instance.{Number, Text}
import org.specs2.Specification

import scala.util.Success


trait PathValueSpec extends Specification with Evaluator with Mocks {

  /*
  PathValueSpec
      PathValue should pass if both pass are not populated                         $pathValueBothPathNotPopulated
      PathValue evaluation should be inconclusive if the path is complex           $pathValuePathComplex
      PathValue evaluation should be inconclusive if the path is invalid           $pathValuePathInvalid
      PathValue evaluation should be inconclusive if the path is unreachable       $pathValuePathUnreachable
      PathValue should fail if only one path is populated                          $pathValueOnePathPopulated
      PathValue should be inconclusive if path1 and path2 resolve to many elements $pathValueManyElems
      PathValue should pass if operator = < and path1.value < path2.value          $pathValuePass
      PathValue should fail if operator = < and path1.value > path2.value          $pathValueFail

  */

  //c1.4[1] is not populated
  assert( queryAsSimple(c1, "4[1]") == Success(Nil) )
  def pathValueBothPathNotPopulated =
    eval( PathValue("4[1]", Operator.LT, "4[1]"), c1 ) === Pass

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
    eval(p, c1) === Failures.pathValue(p, `c1.1[1]`, `c1.3[1]`)
  }

}