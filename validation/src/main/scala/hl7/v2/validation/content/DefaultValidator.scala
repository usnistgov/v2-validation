package hl7.v2.validation.content

import expression.{Inconclusive, Fail, Pass}
import hl7.v2.instance.{Complex, Element, Message}
import hl7.v2.validation.report.{CEntry, Failure, SpecError, Success}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait DefaultValidator extends Validator with expression.Evaluator {

  val cm = constraintManager

  /**
    * Check the message against the constraints defined
    * in the constraint manager and returns the report.
    * @param m - The message to be checked
    * @return The report
    */
  def checkContent(m: Message): Future[Seq[CEntry]] = Future { check(m.asGroup) }

  /**
    * Checks the element and its descendants against the constraints
    * defined in the constraint manager and returns the report.
    * @param e - The element to be checked
    * @return The report
    */
  private def check(e: Element): List[CEntry] = {
    val cl: List[Constraint] = constraintManager.constraintsFor(e)
    val r = cl map { check(e, _)  }
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
  private def check(e: Element, c: Constraint): CEntry =
    eval(c.assertion, e) match {
      case Pass        => Success(e, c)
      case Fail(stack) => Failure(e, c, stack)
      case Inconclusive(exp, details) => SpecError(e, c, exp, details)
    }
}
