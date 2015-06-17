package hl7.v2.validation
package content

import java.util.{Arrays => JArrays, List => JList}

import expression.EvalResult.{Fail, Inconclusive, Pass, Trace}
import gov.nist.validation.report.{Entry, Trace => GTrace}
import hl7.v2.instance._
import hl7.v2.validation.content.PredicateUsage.{R, X}
import hl7.v2.validation.report.Detections

import scala.collection.JavaConversions.seqAsJavaList
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait DefaultValidator extends Validator with expression.Evaluator {

  /**
    * Check the message against the constraints defined
    * in the constraint manager and returns the report.
    * @param m - The message to be checked
    * @return The report
    */
  def checkContent(m: Message): Future[Seq[Entry]] = Future {
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
                   (implicit s: Separators, dtz: Option[TimeZone]): List[Entry] = {
    val cl: List[Constraint] = conformanceContext.constraintsFor(e)
    val pl: List[Predicate]  = conformanceContext.predicatesFor(e)

    val r = pl.foldLeft( cl map { check(e, _) } ){ (acc, p) =>
      check(e, p) ::: acc
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
                   (implicit s: Separators, dtz: Option[TimeZone]): Entry =
    eval(c.assertion, e) match {
      case Pass                => Detections.csSuccess(e, c)
      case Fail(stack)         =>
        val errLoc = approximativeErrorLocation(e, stack)
        Detections.csFailure(errLoc, e, c, stackTrace(e, stack))
      case Inconclusive(trace) => Detections.csSpecError(e, c, stackTrace(e, trace::Nil))
    }

  private def check(e: Element, p: Predicate)
                   (implicit s: Separators, dtz: Option[TimeZone]): List[Entry] =
    eval(p.condition, e) match {
      case Pass            => checkUsage(e, p, p.trueUsage)
      case Fail(stack)     => checkUsage(e, p, p.falseUsage)
      case Inconclusive(t) => Detections.predicateSpecErr(e, p, stackTrace(e, t::Nil))::Nil
    }

  /**
    * Checks if the target path of the predicate satisfy the usage 'u'
    */
  private def checkUsage(e: Element, p: Predicate, u: PredicateUsage): List[Entry] =
    try {
      val(contexts, path) = reducePath(e, p.target)
      if( contexts.isEmpty )
        Detections.predicateSuccess(e, p) :: Nil //Nothing to do the parent is missing
      else {
        lazy val l = contexts flatMap { c => Query.query(c, path).get }
        u match {
          case R if l.isEmpty  =>
            contexts map { c =>
              val dl = Utils.defaultLocation(c.asInstanceOf[Complex], path).getOrElse(c.location)
              val usageErr = Detections.rusage(dl).getDescription
              Detections.predicateFailure(dl, usageErr, "required", p.description)
            }
          case X if l.nonEmpty => l map { x =>
            val usageErr = Detections.xusage(x.location).getDescription
            Detections.predicateFailure(x.location, usageErr, "not supported", p.description)
          }
          case _ => Detections.predicateSuccess(e, p) :: Nil
        }
      }
    } catch { case f: Throwable =>
      val trace = new GTrace("Path resolution failed.", JArrays.asList(f.getMessage))
      Detections.predicateSpecErr(e, p, JArrays.asList(trace)) :: Nil
    }

  /*
   * The target path needs to be reduced since no verification should
   * be done if the direct parent of the target is missing
   */
  @throws
  private
  def reducePath(context: Element, target: String) : (List[Element], String) =
    target.lastIndexOf('.') match {
      case -1 => (context :: Nil, target)
      case i  =>
        val parentPath = target.take( i )
        val path = target.drop( i + 1)
        (Query.query(context, parentPath).get, path )
    }

  private def stackTrace(context: Element, stack: List[Trace]): JList[GTrace] =
    seqAsJavaList( stack map { t =>
      val assertion = expression.AsString.expression(t.expression, context)
      val reasons = t.reasons map { r =>
        s"[${r.location.line}, ${r.location.column}] ${r.message}"
      }
      new GTrace(assertion, reasons)
    })

  private def approximativeErrorLocation(context: Element, stack: List[Trace]) =
    stack match {
      case Nil => context.location
      case _   =>
        stack.last.reasons match {
          case Nil => context.location
          case xs  => xs.last.location
        }
    }
}
