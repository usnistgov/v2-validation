package hl7.v2.validation
package content

import java.util.{Arrays => JArrays, List => JList}

import expression.EvalResult.{EvalData, Fail, Inconclusive, Pass, Trace}
import gov.nist.validation.report.{Entry, Trace => GTrace}
import hl7.v2.instance._
import hl7.v2.validation.content.PredicateUsage.{R, X}
import hl7.v2.profile.{Message => MM}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import expression._
import hl7.v2.validation.report.ConfigurableDetections
import expression.EvalResult.FailPlugin
import scala.jdk.CollectionConverters.SeqHasAsJava

trait DefaultValidator extends Validator with expression.Evaluator with PatternFinder {

  /**
   * Check the message against the constraints defined
   * in the constraint manager and returns the report.
   * @param m - The message to be checked
   * @return The report
   */
  def checkContent(m: Message)(implicit Detections : ConfigurableDetections, VSValidator : vs.Validator): Future[Seq[Entry]] = Future {
    implicit val separators = m.separators
    implicit val dtz = m.defaultTimeZone
    implicit val model = m.model
    check(m.asGroup) ++ checkOI(m.asGroup)
  }

  /**
   * Checks the element and its descendants against the constraints
   * defined in the constraint manager and returns the report.
   * @param e - The element to be checked
   * @return The report
   */
  private def check(e: Element)(implicit s: Separators,
    dtz: Option[TimeZone], model: MM, Detections : ConfigurableDetections, VSValidator : vs.Validator): List[Entry] = {
    val cl : List[Constraint] = conformanceContext.constraintsFor(e)
    val pl : List[Predicate] = conformanceContext.predicatesFor(e)
    val ccl: List[CoConstraint] = conformanceContext.coConstraintsFor(e)

    val r = pl.foldLeft(cl flatMap {
      routeConstraint(e, _)
    }) { (acc, p) =>
      check(e, p) ::: acc
    }
    
    val rCc = ccl.foldLeft(r) { (acc, cc) =>
      checkCC(e, cc) ::: acc
    }

    e match {
      case c: Complex => c.children.foldLeft(rCc) { (acc, cc) => acc ::: check(cc) }
      case _ => rCc
    }
  }

  private def checkOI(e: Element)(implicit s: Separators,
    dtz: Option[TimeZone], model: MM, Detections : ConfigurableDetections, VSValidator : vs.Validator): List[Entry] = {
    val contexts = conformanceContext.orderIndifferentConstraints()
    checkContexts(e,contexts,routeConstraint)
  }
  
  /**
   * Checks the element against the constraint and returns a CEntry.
   * @param e - The element to be checked
   * @return A CEntry
   */
  private def mustRoute(e: Expression) = {
    e match {
      case x: PlainText => true
      case x: StringList => true
      case _ => false
    }
  }

  private def isCB(c: Constraint) = {
    c.reference match {
      case Some(ref) => if (ref.generatedBy.contains("TCAMT")) true else false
      case None => false
    }
  }

  private def format(s: String) = s.replace(" ", "-").toLowerCase()

  private def routeConstraint(e: Element, c: Constraint)(implicit s: Separators,
    dtz: Option[TimeZone], model: MM, Detections : ConfigurableDetections, VSValidator : vs.Validator): List[Entry] = {
    if (isCB(c)) {
      if (mustRoute(c.assertion))
        List(checkCB(e, c, c.reference.get))
      else
        check(e, c, true)
    } else
      check(e, c, false)
  }

  private def check(e: Element, c: Constraint, content: Boolean)(implicit s: Separators,
    dtz: Option[TimeZone], model: MM, Detections : ConfigurableDetections, VSValidator : vs.Validator): List[Entry] =
    eval(c.assertion, e) match {
      case Pass => List(Detections.csSuccess(e, c,content))
      case Fail(stack) => 
        val errLoc = approximativeErrorLocation(e, stack)
          List(Detections.csFailure(errLoc, e, c, c.description, stackTrace(e, stack),content))
      case FailPlugin(stack, messages) => 
        val errLoc = approximativeErrorLocation(e, stack)
        for(message <- messages)
        yield Detections.csFailure(errLoc, e, c, message, stackTrace(e, stack),content);  
      case Inconclusive(trace) => List(Detections.csSpecError(e, c, c.description, stackTrace(e, trace :: Nil),content))
    }
  
   private def checkCC(e: Element, c: CoConstraint)(implicit s: Separators,
    dtz: Option[TimeZone], model: MM, Detections : ConfigurableDetections, VSValidator : vs.Validator): List[Entry] =
      c.constraints.foldLeft(List[Entry]()){
       (acc, x) => checkCC(e, c.description, x.key, x.assertions) ++ acc
   }
   
   private def checkCC(e: Element, str : String, k: Expression, a: List[Expression])(implicit s: Separators,
    dtz: Option[TimeZone], model: MM, Detections : ConfigurableDetections, VSValidator : vs.Validator): List[Entry] =
     eval(k, e) match {
      case Pass => checkCCList(e, str, a, k)
      case Fail(_) => Nil
      case FailPlugin(_,_) => Nil
      case Inconclusive(trace) => Nil
    }
    
   private def checkCCList(e: Element, str : String, a: List[Expression], k: Expression)(implicit s: Separators,
    dtz: Option[TimeZone], model: MM, Detections : ConfigurableDetections, VSValidator : vs.Validator): List[Entry] = 
    a.foldLeft(List[Entry]()){
       (acc, x) => eval(x, e) match {
         case Pass => Detections.coConstraintSuccess(e, str, k, x) :: acc
         case Fail(_) | FailPlugin(_,_) => Detections.coConstraintFailure(e, str, k, x) :: acc
         case Inconclusive(trace) => Detections.coConstraintFailure(e, str, k, x) :: acc
       }
     }
   

  private def checkCB(e: Element, c: Constraint, ref: Reference)(implicit s: Separators,
    dtz: Option[TimeZone], model: MM, Detections : ConfigurableDetections): Entry = {

    //-- Custom Evaluation for context-based constraints
    val evaluator = new ContextBasedEvaluator

    evaluator.eval(c.assertion, e) match {
      case EvalData(a, found, expected) => a match {
        case Pass => Detections.cntSuccess(e, c)
        case Fail(stack) =>
          val errLoc = approximativeErrorLocation(e, stack)
          Detections.cntFailureCustom(errLoc, e, c, stackTrace(e, stack), format(ref.testDataCategorization), found, expected)
        case FailPlugin(stack,_) =>
          val errLoc = approximativeErrorLocation(e, stack)
          Detections.cntFailureCustom(errLoc, e, c, stackTrace(e, stack), format(ref.testDataCategorization), found, expected)
        case Inconclusive(trace) => Detections.csSpecError(e, c, c.description, stackTrace(e, trace :: Nil))
      }
    }

  }

  private def check(e: Element, p: Predicate)(implicit s: Separators, dtz: Option[TimeZone], Detections : ConfigurableDetections, VSValidator : vs.Validator): List[Entry] =
    eval(p.condition, e) match {
      case Pass => checkUsage(e, p, p.trueUsage, predicatePass = true)
      case Fail(_) | FailPlugin(_,_) => checkUsage(e, p, p.falseUsage, predicatePass = false)
      case Inconclusive(t) => Detections.predicateSpecErr(e, p, stackTrace(e, t :: Nil)) :: Nil
    }

  private def cleanFromNull(ls: List[Element]): List[Element] = {
    ls match {
      case Nil => Nil
      case x :: xs => x match {
        case n: NULLComplexField => cleanFromNull(xs)
        case _ => x :: cleanFromNull(xs)
      }
    }
  }
  /**
   * Checks if the target path of the predicate satisfy the usage 'u'
   */
  private def checkUsage(e: Element, p: Predicate, u: PredicateUsage, predicatePass: Boolean)(implicit Detections : ConfigurableDetections): List[Entry] =
    try {
      val (ccontexts, path) = reducePath(e, p.target)
      val contexts = cleanFromNull(ccontexts)
      if (contexts.isEmpty)
        Detections.predicateSuccess(e, p) :: Nil //Nothing to do the parent is missing
      else {
        lazy val l = contexts flatMap { c => Query.query(c, path).get }
        val predicateErrors = u match {
          case R if l.isEmpty =>
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
        val notes = l.map(elm => Detections.predicateUsageNote(elm.location, u.toString, p.trueUsage.toString, p.falseUsage.toString, p.description, predicatePass))
        predicateErrors ::: notes
      }
    } catch {
      case f: Throwable =>
        val trace = new GTrace("Path resolution failed.", JArrays.asList(f.getMessage))
        Detections.predicateSpecErr(e, p, JArrays.asList(trace)) :: Nil
    }

  /*
   * The target path needs to be reduced since no verification should
   * be done if the direct parent of the target is missing
   */
  @throws
  private def reducePath(context: Element, target: String): (List[Element], String) =
    target.lastIndexOf('.') match {
      case -1 => (context :: Nil, target)
      case i =>
        val parentPath = target.take(i)
        val path = target.drop(i + 1)
        (Query.query(context, parentPath).get, path)
    }

  private def stackTrace(context: Element, stack: List[Trace]): JList[GTrace] =
    (stack map { t =>
      val assertion = expression.AsString.expression(t.expression, context)
      val reasons = t.reasons map { r =>
        s"[${r.location.line}, ${r.location.column}] ${r.message}"
      }
      new GTrace(assertion, reasons.asJava)
    }).asJava

  private def approximativeErrorLocation(context: Element, stack: List[Trace]) =
    stack match {
      case Nil => context.location
      case _ =>
        stack.last.reasons match {
          case Nil => context.location
          case xs => xs.last.location
        }
    }
}
