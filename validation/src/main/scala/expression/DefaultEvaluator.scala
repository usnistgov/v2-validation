package expression

import hl7.v2.instance.Element
import hl7.v2.instance.Simple
import hl7.v2.instance.Query._
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
  }

  def presence(p: Presence, context: Element): EvalResult = query(context, p.path) match {
    case Success(Nil) => Fail( Reason( context.location, s"${path(context, p.path)} is missing") :: Nil)
    case Success(_)   => Pass
    case Failure(e)   => Inconclusive(context, p, e.getMessage :: Nil)
  }

  def plainText(p: PlainText, context: Element): EvalResult = queryAsSimple(context, p.path) match {
    case Success(ls)  => 
      ls filter( s => !eq(s, p.text, p.ignoreCase) ) match {
        case Nil => Pass
        case xs  => plainTextFailure(p, xs)
      }
    case Failure(e) => Inconclusive(context, p, e.getMessage :: Nil)
  }

  def format(f: Format, context: Element): EvalResult = ???

  def numberList(nl: NumberList, context: Element): EvalResult = ???

  def stringList(nl: StringList, context: Element): EvalResult = ???

  def simpleValue(sv: SimpleValue, context: Element): EvalResult = ???

  def pathValue(pv: PathValue, context: Element): EvalResult = ???

  def and(and: AND, context: Element): EvalResult = ???

  def or(or: OR, context: Element): EvalResult = ???

  def not(not: NOT, context: Element): EvalResult = ???

  def xor(xor: XOR, context: Element): EvalResult = ???

  def imply(e: IMPLY, context: Element): EvalResult = ???

  def exist(e: EXIST, context: Element): EvalResult = ???

  def forall(e: FORALL, context: Element): EvalResult = ???

  //Presence evaluation helper
  private def path(c: Element, path: String) = s"${c.location.path}.${path}"

  //Plain text evaluation helpers
  private def eq(s: Simple, text: String, cs: Boolean): Boolean = 
    if( cs ) s.value.asString.equalsIgnoreCase( text ) else s.value.asString == text

  private def plainTextFailure(p: PlainText, xs: Seq[Simple]) = {
    val cs = if( p.ignoreCase ) "case insensitive" else "case sensitive"
    Fail( xs map { s => Reason(s.location, s"'${s.value.asString}' is different from '${p.text}' ($cs)") } )
  }
}