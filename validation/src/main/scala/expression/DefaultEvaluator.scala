package expression

import hl7.v2.instance.{Simple, Query, Element}
import Query._
import scala.util.Success
import scala.util.Failure

trait DefaultEvaluator extends Evaluator {

  def eval(exp: Expression, context: Element): EvalResult = exp match {
    case e: Presence    => presence(e, context)
    case e: PlainText   => plainText(e, context)
    case e: Format      => format(e, context)
    case e: NumberList  => numberList(e, context)
    case e: StringList  => stringList(e, context)
    case e: SimpleValue => simpleValue(e, context)
    case e: PathValue   => pathValue(e, context)
    case e: AND         => and(e, context)
    case e: OR          => or(e, context)
    case e: NOT         => not(e, context)
    case e: XOR         => xor(e, context)
    case e: IMPLY       => imply(e, context)
    case e: EXIST       => exist(e, context)
    case e: FORALL      => forall(e, context)
    case e: Plugin      => plugin(e, context)
  }

  def presence(p: Presence, context: Element): EvalResult =
    query(context, p.path) match {
      case Success(Nil) => Failures.presenceFailure(p, context)
      case Success(_)   => Pass
      case Failure(e)   => Inconclusive(p, e.getMessage :: Nil)
    }

  def plainText(p: PlainText, context: Element): EvalResult =
    queryAsSimple(context, p.path) match {
      case Success(ls)  =>
        ls filter( s => notEqual(s, p.text, p.ignoreCase) ) match {
          case Nil => Pass
          case xs  => Failures.plainTextFailure(p, xs)
        }
      case Failure(e) => Inconclusive(p, e.getMessage :: Nil)
    }

  def format(f: Format, context: Element): EvalResult = ??? //FIXME

  def numberList(nl: NumberList, context: Element): EvalResult = ??? //FIXME

  def stringList(nl: StringList, context: Element): EvalResult = ??? //FIXME

  def simpleValue(sv: SimpleValue, context: Element): EvalResult = ??? //FIXME

  def pathValue(pv: PathValue, context: Element): EvalResult = ??? //FIXME

  def and(and: AND, context: Element): EvalResult =
    eval(and.exp1, context) match {
      case i: Inconclusive => i
      case f: Fail         => Failures.andFailure(and, context, f)
      case Pass            =>
        eval( and.exp2, context ) match {
          case f: Fail => Failures.andFailure(and, context, f)
          case x       => x
        }
    }

  def or(or: OR, context: Element): EvalResult =
    eval( or.exp1, context ) match {
      case f1: Fail =>
        eval(or.exp2, context) match {
          case f2: Fail => Failures.orFailure(or, context, f1, f2)
          case x        => x
        }
      case x => x
    }

  def not(not: NOT, context: Element): EvalResult =
    eval( not.exp, context ) match {
      case Pass    => Failures.notFailure( not, context)
      case f: Fail => Pass
      case i: Inconclusive => i
    }

  def xor(xor: XOR, context: Element): EvalResult = ??? //FIXME

  def imply(e: IMPLY, context: Element): EvalResult = ??? //FIXME

  def exist(e: EXIST, context: Element): EvalResult = ??? //FIXME

  def forall(e: FORALL, context: Element): EvalResult = ??? //FIXME

  def plugin(e: Plugin, context: Element): EvalResult =
    pluginMap.get( e.id ) match {
      case Some( f ) => f( e.params )
      case None => Inconclusive(e, s"Plugin '${e.id}' not found" :: Nil)
    }

  //Plain text evaluation helpers
  // Returns true if the value of `s' is not equal to `text'
  private def notEqual(s: Simple, text: String, cs: Boolean): Boolean =
    if( cs ) !s.value.asString.equalsIgnoreCase( text )
    else !(s.value.asString == text)
}