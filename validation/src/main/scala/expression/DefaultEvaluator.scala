package expression

import hl7.v2.instance.Query._
import hl7.v2.instance.{Separators, Element, EscapeSeqHandler, Simple}

import scala.util.{Failure, Success}

trait DefaultEvaluator extends Evaluator with EscapeSeqHandler {

  /**
    * Evaluates the expression within the specified context
    * and returns the result
    * @param e - The expression to be evaluated
    * @param c - The context node
    * @param s - The message separators
    * @return The evaluation result
    */
  def eval(e: Expression, c: Element)
          (implicit  s: Separators): EvalResult = e match {
    case x: Presence    => presence(x, c)
    case x: PlainText   => plainText(x, c)
    case x: Format      => format(x, c)
    case x: NumberList  => numberList(x, c)
    case x: StringList  => stringList(x, c)
    case x: SimpleValue => simpleValue(x, c)
    case x: PathValue   => pathValue(x, c)
    case x: AND         => and(x, c)
    case x: OR          => or(x, c)
    case x: NOT         => not(x, c)
    case x: XOR         => xor(x, c)
    case x: IMPLY       => imply(x, c)
    case x: EXIST       => exist(x, c)
    case x: FORALL      => forall(x, c)
    case x: Plugin      => plugin(x, c)
  }

  /**
    * Evaluates the presence expression and returns the result
    * @param p       - The presence expression
    * @param context - The context
    * @return The evaluation result
    */
  def presence(p: Presence, context: Element): EvalResult =
    query(context, p.path) match {
      case Success(Nil) => Failures.presenceFailure(p, context)
      case Success(_)   => Pass
      case Failure(e)   => Inconclusive(p, e.getMessage :: Nil)
    }

  /**
    * Evaluates the plain text expression and returns the result
    * @param p       - The plain text expression
    * @param context - The context
    * @return The evaluation result
    */
  def plainText(p: PlainText, context: Element)
               (implicit s: Separators): EvalResult =
    queryAsSimple(context, p.path) match {
      case Success(ls)  =>
        ls filter( x => notEqual(x, p.text, p.ignoreCase) ) match {
          case Nil => Pass
          case xs  => Failures.plainTextFailure(p, xs)
        }
      case Failure(e) => Inconclusive(p, e.getMessage :: Nil)
    }

  /**
    * Evaluates the format expression and returns the result
    * @param f       - The format expression
    * @param context - The context
    * @return The evaluation result
    */
  def format(f: Format, context: Element)(implicit s: Separators): EvalResult =
    queryAsSimple(context, f.path) match {
      case Success(ls)  =>
        ls filter( x => notMatch(x, f.pattern) ) match {
          case Nil => Pass
          case xs  => Failures.formatFailure(f, xs)
        }
      case Failure(e) => Inconclusive(f, e.getMessage :: Nil)
    }

  /**
    * Evaluates the string list expression and returns the result
    * @param sl      - The string list expression
    * @param context - The context
    * @return The evaluation result
    */
  def stringList(sl: StringList, context: Element)
                (implicit s: Separators): EvalResult =
    queryAsSimple(context, sl.path) match {
      case Success(ls)  =>
        ls filter( x => notInList(x.value.raw, sl.csv, true) ) match {
          case Nil => Pass
          case xs  => Failures.stringListFailure(sl, xs)
        }
      case Failure(e) => Inconclusive(sl, e.getMessage :: Nil)
    }

  /**
    * Evaluates the number list expression and returns the result
    * @param nl      - The number list expression
    * @param context - The context
    * @return The evaluation result
    */
  def numberList(nl: NumberList, context: Element)
                (implicit s: Separators): EvalResult =
    queryAsSimple(context, nl.path) match {
      case Success(ls)  =>
        val(l1, l2) = ls partition( x => convertibleToDouble( x.value.raw ))
        l2 match {
          case Nil =>
            l1 filter( x => notInList(x.value.raw.toDouble, nl.csv) ) match {
              case Nil => Pass
              case xs  => Failures.numberListFailure(nl, xs)
            }
          case xs  => Inconclusive(nl, xs map Failures.NaNErrMsg)
        }
      case Failure(e) => Inconclusive(nl, e.getMessage :: Nil)
    }

  def simpleValue(sv: SimpleValue, context: Element): EvalResult = ??? //FIXME
    /*queryAsSimple(context, sv.path) match {
      case Success(ls) =>
        var errors  = List[String](); var reasons = List[Reason]()
        ls foreach { x =>
          sv.operator.eval( x.value, sv.value ) match {
            case Failure(e) => s"[line=${x.}, column=${s.column}] ${x.value} ${sv.operator} ${sv.value} failed. Reason: ${e.getMessage}" :: errors
          }
        }

        ???
      case Failure(e) => Inconclusive(sv, e.getMessage :: Nil)
    }*/

  def pathValue(pv: PathValue, context: Element): EvalResult = ??? //FIXME

  /**
    * Evaluates the AND expression and returns the result
    * @param and     - The AND expression
    * @param context - The context
    * @return The evaluation result
    */
  def and(and: AND, context: Element)(implicit s: Separators): EvalResult =
    eval(and.exp1, context) match {
      case i: Inconclusive => i
      case f: Fail         => Failures.andFailure(and, context, f)
      case Pass            =>
        eval( and.exp2, context ) match {
          case f: Fail => Failures.andFailure(and, context, f)
          case x       => x
        }
    }

  /**
    * Evaluates the OR expression and returns the result
    * @param or      - The OR expression
    * @param context - The context
    * @return The evaluation result
    */
  def or(or: OR, context: Element)(implicit s: Separators): EvalResult =
    eval( or.exp1, context ) match {
      case f1: Fail =>
        eval(or.exp2, context) match {
          case f2: Fail => Failures.orFailure(or, context, f1, f2)
          case x        => x
        }
      case x => x
    }

  /**
    * Evaluates the NOT expression and returns the result
    * @param not     - The NOT expression
    * @param context - The context
    * @return The evaluation result
    */
  def not(not: NOT, context: Element)(implicit s: Separators): EvalResult =
    eval( not.exp, context ) match {
      case Pass    => Failures.notFailure( not, context)
      case f: Fail => Pass
      case i: Inconclusive => i
    }

  def xor(xor: XOR, context: Element)(implicit s: Separators): EvalResult = ??? //FIXME

  def imply(e: IMPLY, context: Element)(implicit s: Separators): EvalResult = ??? //FIXME

  def exist(e: EXIST, context: Element)(implicit s: Separators): EvalResult = ??? //FIXME

  def forall(e: FORALL, context: Element)(implicit s: Separators): EvalResult = ??? //FIXME

  /**
    * Evaluates the plugin expression and returns the result
    * @param e       - The plugin expression
    * @param context - The context
    * @return The evaluation result
    */
  def plugin(e: Plugin, context: Element)(implicit s: Separators): EvalResult =
    pluginMap.get( e.id ) match {
      case Some( f ) => f( e, context, s )
      case None => Inconclusive(e, s"Plugin '${e.id}' not found" :: Nil)
    }

  //FIXME: Using unescape in helpers functions make expression eval tight to HL7

  /*
   * Helper functions
   *
   * Notes:
   *    1) The value of the simple element will be unescaped before the operation.
   *    2) I can't see any good rational for un-escaping text in expression
   */

  /**
    * Returns true if the unescaped value of 's' is not
    * equal to the unescaped 'text' depending on the case
    */
  private def notEqual(s: Simple, text: String, cs: Boolean)
                      (implicit separators: Separators): Boolean =
    if( cs ) ! unescape(s.value.raw).equalsIgnoreCase( text )
    else unescape(s.value.raw) != text

  /**
    * Returns true if the unescaped value of 's'
    * don't match the regular expression 'regex'
    */
  private def notMatch(s: Simple, regex: String)
                      (implicit separators: Separators): Boolean =
    !regex.r.pattern.matcher( unescape(s.value.raw) ).matches

  /**
    * Returns true if the list does not contain 's'.
    * The boolean 'b' dictates whether to un-escape the value or not.
    */
  private def notInList(s: String, list: List[String], b: Boolean)
                          (implicit separators: Separators): Boolean =
    if( b ) !list.contains(unescape(s)) else !list.contains(s)

  /**
    * Returns true if the list does not contain 'd'
    */
  private def notInList(d: Double, list: List[Double]): Boolean = !list.contains(d)

  /**
    * Returns true if s can be converted to a Double
    */
  private def convertibleToDouble(s: String): Boolean =
    try { s.toDouble; true } catch { case e: Throwable => false }
}