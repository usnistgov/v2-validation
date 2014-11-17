package expression

import hl7.v2.instance.{Element, Query}
import Query.query
import org.specs2.Specification

import scala.util.{Failure, Success}

trait PresenceSpec extends Specification with Evaluator with Mocks {

  /*
  PresenceSpec
      Presence evaluation should be inconclusive if the path is invalid          $presencePathInvalid
      Presence evaluation should be inconclusive if the path is unreachable      $presencePathUnreachable
      Presence should pass if the path is populated                              $presencePathPopulated
      Presence should fail if the path is not populated                          $presencePathNotPopulated
  */

  def presencePathInvalid = {
    val p = Presence("1")
    eval( p, c0) === Inconclusive( p, "Invalid Path '1'" :: Nil )
  }

  def presencePathUnreachable = {
    val p = Presence("2[2]")
    eval( p, s0 ) === Inconclusive( p, "Unreachable Path '2[2]'" :: Nil )
  }

  def presencePathPopulated = {
    assert(isPopulated(c2, "2[1]"))
    eval(Presence("2[1]"), c2) === Pass
  }

  def presencePathNotPopulated = {
    assert(!isPopulated(c2, "2[2]"))
    eval(Presence("2[2]"), c2) === Failures.presenceFailure(Presence("2[2]"), c2)
  }

  private def isPopulated(c: Element, path: String): Boolean =
    query(c, path) match {
      case Success(l) => l.nonEmpty
      case Failure(f) => false
    }
}

