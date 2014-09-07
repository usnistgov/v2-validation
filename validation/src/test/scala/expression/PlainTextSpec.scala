package expression

import hl7.v2.instance.Query.queryAsSimple
import hl7.v2.instance.{Simple, Text}
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
  def plainTextPathNotPopulated = Seq(true, false) map { b => eval( PlainText("4[1]", "", b), c1 ) === Pass }

  // c1.2[3] is complex
  def plainTextPathComplex = Seq(true, false) map { b =>
    val p2 = PlainText("2[3]", "", b)
    eval( p2, c2 ) === inconclusive(c2, p2, "Path resolution returned at least one complex element")
  }

  // 4 is an invalid path
  def plainTextPathInvalid = Seq(true, false) map { b =>
    val p3 = PlainText("4", "", b)
    eval( p3, c0 ) === inconclusive(c0, p3, s"Invalid Path '${p3.path}'")
  }

  // s0 is a simple element, querying it will fail
  def plainTextPathUnreachable = Seq(true, false) map { b =>
    val p4 = PlainText("4[1]", "", b)
    eval( p4, s0 ) === inconclusive(s0, p4, s"Unreachable Path '${p4.path}'")
  }

  // The following value will be used in the next tests
  private val `c1.3[1]`  = queryAsSimple(c1, "3[1]").get.head
  assert( `c1.3[1]`.value == Text("S3") )

  def plainTextSameValue = Seq(true, false) map { b => eval( PlainText("3[1]", "S3", b), c1 ) === Pass }

  def plainTextSameValueIC = eval( PlainText("3[1]", "s3", true ), c1 ) === Pass

  def plainTextDifferentValue = Seq(true, false) map { b =>
    val p = PlainText("3[1]", "X", b)
    eval( p, c1 ) === fail(p, `c1.3[1]`)
  }

  def plainTextSameValueCNI = { val p = PlainText("3[1]", "s3", false); eval( p, c1 ) === fail(p, `c1.3[1]`) }

  private def fail( e: PlainText, s: Simple ) = {
    val cs = if( e.ignoreCase ) "(case insensitive)" else "(case sensitive)"
    Fail( Reason(s.location, s"'${s.value.asString}' is different from '${e.text}' $cs")::Nil )
  }
}