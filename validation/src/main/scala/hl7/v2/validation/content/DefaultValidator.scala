package hl7.v2.validation.content

import expression.EvalResult.{Inconclusive, Fail, Pass}
import expression.{NOT, Presence}
import hl7.v2.instance._
import hl7.v2.validation.report._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait DefaultValidator extends Validator with expression.Evaluator {

  /**
    * Check the message against the constraints defined
    * in the constraint manager and returns the report.
    * @param m - The message to be checked
    * @return The report
    */
  def checkContent(m: Message): Future[Seq[CEntry]] = Future {
    implicit val separators = m.separators
    implicit val dtz = m.defaultTimeZone
    check(m.asGroup)
  }

  /**
    * Checks the element and its descendants against the constraints
    * defined in the constraint manager and returns the report.
    * @param e - The element to be checked
    * @return The report
    */
  private def check(e: Element)
                   (implicit s: Separators, dtz: Option[TimeZone]): List[CEntry] = {
    val cl: List[Constraint] = conformanceContext.constraintsFor(e)
    val pl: List[Predicate]  = conformanceContext.predicatesFor(e)

    val r = pl.foldLeft( cl map { check(e, _) } ){ (acc, p) =>
      check(e, p) :: acc
    }

    e match {
      case c: Complex => c.children.foldLeft(r){ (acc, cc) => acc ::: check(cc) }
      case _          => r
    }
  }

  /**
    * Checks the element against the constraint and returns a CEntry.
    * @param e - The element to be checked
    * @param c - The constraint
    * @return A CEntry
    */
  private def check(e: Element, c: Constraint)
                   (implicit s: Separators, dtz: Option[TimeZone]): CEntry =
    eval(c.assertion, e) match {
      case Pass                => Success(e, c)
      case Fail(stack)         => Failure(e, c, stack)
      case Inconclusive(trace) => SpecError(e, c, trace)
    }

  private def check(e: Element, p: Predicate)
                   (implicit s: Separators, dtz: Option[TimeZone]): CEntry =
    eval(p.condition, e) match {
      case Pass                => checkUsage(e, p, p.trueUsage)
      case Fail(stack)         => checkUsage(e, p, p.falseUsage)
      case Inconclusive(trace) => PredicateSpecError(e, p, trace)
    }

  private def checkUsage(e: Element, p: Predicate, u: PredicateUsage)
                        (implicit s: Separators, dtz: Option[TimeZone]): CEntry = {
    val path = p.target
    val r = u match {
      case PredicateUsage.R => eval(Presence(path), e)
      case PredicateUsage.X => eval(NOT(Presence(path)), e)
      case _ => PredicateSuccess(e, p)
    }
    r match {
      case Pass => PredicateSuccess(e, p)
      case Fail(stack) => PredicateFailure(e, p, stack)
      case Inconclusive(trace) => PredicateSpecError(e, p, trace)
    }
  }

}
