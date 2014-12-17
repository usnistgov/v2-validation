package expression

import expression.EvalResult.Pass
import hl7.v2.instance.Query
import Query.queryAsSimple
import hl7.v2.instance.Text
import org.specs2.Specification

import scala.util.Success

trait PlainTextSpec extends Specification with Evaluator with Mocks {

  /*
  PlainTextSpec
      PlainText evaluation should succeed if the path is not populated              $plainTextPathNotPopulated
      PlainText evaluation should be inconclusive if the path is complex            $plainTextPathComplex
      PlainText evaluation should be inconclusive if the path is invalid            $plainTextPathInvalid
      PlainText evaluation should be inconclusive if the path is unreachable        $plainTextPathUnreachable
      PlainText should pass if the values are the same                              $plainTextSameValue
      PlainText should pass if the values are the same by ignoring the case         $plainTextSameValueIC
      PlainText should fail if the values are different                             $plainTextDifferentValue
      PlainText should fail for same values in different case when case not ignored $plainTextSameValueCNI
  */
  
  //c1.4[1] is not populated
  assert( queryAsSimple(c1, "4[1]") == Success(Nil) )
  def plainTextPathNotPopulated = Seq(true, false) map { b =>
    eval( PlainText("4[1]", "", b), c1 ) === Pass
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
}