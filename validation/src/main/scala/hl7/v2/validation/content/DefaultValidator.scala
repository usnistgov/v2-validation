package hl7.v2.validation.content

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import expression._
import hl7.v2.instance._

trait DefaultValidator extends Validator with expression.Evaluator {

  val cm = constraintManager

  /**
   * Check the message against the constraints defined
   * in the constraint manager and returns the report
   */
  def checkContent(m: Message): Future[Seq[Entry]] = Future {
    val g = m.asGroup
    check( g, cm.constraintsFor(g) )
  }

  private def check(g: Group): List[Entry] = {
    val r = check(g, constraintManager.constraintsFor( g ))
    g.structure.foldLeft( r ){ (acc, x) =>
      x match {
        case Left (ls) => acc ::: (ls map check).flatten
        case Right(lg) => acc ::: (lg map check).flatten
      }
    }
  }

  private def check(s: Segment): List[Entry] = {
    val r = check(s, constraintManager.constraintsFor( s ))
    s.fields.flatten.foldLeft(r){ (acc, x) => acc ::: check(x) }
  }

  private def check(f: Field): List[Entry] = {
    val r = check(f, constraintManager.constraintsFor( f ))
    f match {
      case sc: SimpleField  => r
      case cc: ComplexField =>
        cc.components.flatten.foldLeft(r){ (acc, x) => acc ::: check(x) }
    }
  }

  private def check(c: Component): List[Entry] = {
    val r = check(c, constraintManager.constraintsFor( c ))
    c match {
      case sc: SimpleComponent  => r
      case cc: ComplexComponent =>
        cc.components.flatten.foldLeft(r){ (acc, x) => acc ::: check(x) }
    }
  }

  private def check(e: Element, cl: List[Constraint]): List[Entry] =
    cl map { constraint => check(e, constraint)  }

  private def check(e: Element, c: Constraint): Entry =
    eval( c.assertion, e ) match {
      case Pass                      => Success(e, c)
      case Fail(stack)               => Failure(e, c, stack)
      case Inconclusive(exp,details) => SpecError(e, c, exp, details)
    }
}
