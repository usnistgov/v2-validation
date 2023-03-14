package expression

import expression.EvalResult._
import hl7.v2.instance.Query._
import hl7.v2.instance._
import hl7.v2.validation.vs.{ Validator, ValueSetLibrary }
import gov.nist.validation.report.Entry

import scala.util.{ Failure, Success, Try }

class ContextBasedEvaluator extends EscapeSeqHandler {

  def eval(e: Expression, c: Element)(implicit l: ValueSetLibrary, s: Separators,
    t: Option[TimeZone]): EvalData = {
    e match {
      case x: PlainText => plainText(x, c)
      case x: StringList => stringList(x, c)
      case _ => EvalData(Pass, "", "")
    }
  }

  def plainText(p: PlainText, context: Element)(implicit s: Separators): EvalData = {
    queryAsSimple(context, p.path) match {
      case Success(ls) =>
        ls filter (x => notEqual(x, p.text, p.ignoreCase)) match {
          case Nil => EvalData(Pass, "", "")
          case xs => if (p.atLeastOnce && (xs.size != ls.size)) EvalData(Pass, "", "") else EvalData(Failures.plainText(p, xs), xs.head.value.raw, p.text)
        }
      case Failure(e) => EvalData(inconclusive(p, context.location, e), "", "")
    }
  }

  def stringList(sl: StringList, context: Element)(implicit s: Separators): EvalData =
    queryAsSimple(context, sl.path) match {
      case Success(ls) =>
        ls filter (x => notInList(x.value.raw, sl.csv)) match {
          case Nil => EvalData(Pass, "", "")
          case xs => if (sl.atLeastOnce && (xs.size != ls.size)) EvalData(Pass, "", "") else EvalData(Failures.stringList(sl, xs), xs.head.value.raw, sl.csv.toString())
        }
      case Failure(e) => EvalData(inconclusive(sl, context.location, e), "", "")
    }

  private def notEqual(s: Simple, text: String, cs: Boolean)(implicit separators: Separators): Boolean =
    if (cs) !unescape(s.value.raw).equalsIgnoreCase(text)
    else unescape(s.value.raw) != text

  private def notInList(s: String, list: List[String])(implicit separators: Separators): Boolean =
    !list.contains(unescape(s))
  /**
   * Creates an inconclusive result from a message
   */
  private def inconclusive(e: Expression, l: Location, m: String): Inconclusive =
    Inconclusive(Trace(e, Reason(l, m) :: Nil))

  /**
   * Creates an inconclusive result from a throwable
   */
  private def inconclusive(e: Expression, l: Location, t: Throwable): Inconclusive =
    inconclusive(e, l, t.getMessage)

  private def inconclusive(sv: SimpleValue, xs: List[(Simple, Try[Boolean])]) = {
    val reasons = xs map {
      case (s, Failure(e)) => Reason(s.location, e.getMessage)
      case _ => ??? //Not gonna happens
    }
    Inconclusive(Trace(sv, reasons))
  }

  private def inconclusive(pv: PathValue, c: Element, xs1: List[Simple], xs2: List[Simple]) = {
    val p1 = s"${c.location.path}.${pv.path1}"
    val p2 = s"${c.location.path}.${pv.path2}"
    val m = s"path1($p1) and path2($p2) resolution returned respectively ${xs1.length} and ${xs1.length} elements."
    val reasons = Reason(c.location, m) :: Nil
    Inconclusive(Trace(pv, reasons))
  }
}