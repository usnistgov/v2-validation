package expression

import expression.EvalResult.Pass
import hl7.v2.instance.{Element, Query}
import Query.query
import org.specs2.Specification

import scala.util.{Failure, Success}

trait IsNULLSpec extends Specification with Evaluator with Mocks {

  /*
    IsNULL evaluation should be inconclusive if the path is invalid                               $nullPathInvalid
    IsNuLL evaluation should be inconclusive if the path is unreachable                           $nullPathUnreachable
    IsNull evaluation should fail if the path is not populated                                    $nullPathNotPopulated
    If the path is populated
        IsNull evaluation should pass if the path points to a field and the field is Null         $nullPathNullField
        IsNull evaluation should fail if the path points to a field and the field is NOT Null     $nullPathNotNullField
        IsNull evaluation should fail if the path DOES NOT point to a field.                      $nullPathNotField
  */

  
  def nullPathInvalid = {
    val p = Presence("1")
    eval( p, c0) === inconclusive( p, c0.location, "Invalid Path '1'")
  }

  def presencePathUnreachable = {
    val p = Presence("2[2]")
    eval( p, s0 ) === inconclusive(p, s0.location, "Unreachable Path '2[2]'")
  }

  def presencePathPopulated = {
    assert(isPopulated(c2, "2[1]"))
    eval(Presence("2[1]"), c2) === Pass
  }

  def presencePathNotPopulated = {
    assert(!isPopulated(c2, "2[2]"))
    eval(Presence("2[2]"), c2) === Failures.presence(c2, Presence("2[2]"))
  }

  private def isPopulated(c: Element, path: String): Boolean =
    query(c, path) match {
      case Success(l) => l.nonEmpty
      case Failure(f) => false
    }
}

