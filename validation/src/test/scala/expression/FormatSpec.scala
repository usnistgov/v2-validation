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
  */

  //c1.4[1] is not populated
  assert( queryAsSimple(c1, "4[1]") == Success(Nil) )
  def formatPathNotPopulated = eval( Format("4[1]", "xx"), c1 ) === Pass

  // The following value will be used in the next tests
  private val `c1.3[1]`  = queryAsSimple(c1, "3[1]").get.head
  assert( `c1.3[1]`.value == Text("S3") )

  def formatMatch = eval( Format("3[1]", "[A-Z0-9]+"), c1 ) === Pass

  def formatNoMatch = {
    val e = Format("3[1]", "[a-z0-9]+")
    eval( e, c1 ) === Failures.format(e, `c1.3[1]` :: Nil )
  }
}