package expression

import expression.Failures._
import hl7.v2.instance.Query._
import hl7.v2.instance._

import scala.util.{Failure, Success, Try}

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
        ls filter( x => notInList(x.value.raw, sl.csv) ) match {
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
          case xs => Failures.numberListNaNFailure(nl, xs)
        }
      case Failure(e) => Inconclusive(nl, e.getMessage :: Nil)
    }

  /**
    * Evaluates the simple value expression and returns the result
    * @param sv      - The simple value expression
    * @param context - The context
    * @return The evaluation result
    */
  def simpleValue(sv: SimpleValue, context: Element): EvalResult =
    queryAsSimple(context, sv.path) match {
      case Success(ls) =>
        val evs = ls map { s => s -> sv.operator.eval( s.value, sv.value ) }
        evs partition { _._2.isFailure  } match {
          case (Nil, xs) =>
            xs filter { _._2 == Success(false) } match {
              case Nil => Pass
              case ys  => simpleValueFailure( sv, ys map { x => x._1 } )
            }
          case (xs, _) => Inconclusive(sv, valueComparisonErrors(xs, sv))
        }
      case Failure(e) => Inconclusive(sv, e.getMessage :: Nil)
    }

  /**
    * Evaluates the path value expression and returns the result
    * @param pv      - The path value expression
    * @param context - The context
    * @return The evaluation result
    */
  def pathValue(pv: PathValue, context: Element): EvalResult =
    (queryAsSimple(context, pv.path1), queryAsSimple(context, pv.path2)) match {
      case (Success(  Nil  ), Success(  Nil  )) => Pass
      case (Success(x::Nil), Success(Nil)) => pathValueFailure(pv, x, pv.path2)
      case (Success(Nil), Success(x::Nil)) => pathValueFailure(pv, x, pv.path1)
      case (Success(x1::Nil), Success(x2::Nil)) =>
        pv.operator.eval( x1.value, x2.value ) match {
          case Success(true)  => Pass
          case Success(false) => pathValueFailure(pv, x1, x2)
          case Failure(e)     => Inconclusive(pv, e.getMessage::Nil)
        }
      case (Success(xs1), Success(xs2)) => Inconclusive(pv, pvErrMsgs(pv, xs1, xs2))
      case ( Failure(e), _ )            => Inconclusive(pv, e.getMessage :: Nil)
      case ( _, Failure(e) )            => Inconclusive(pv, e.getMessage :: Nil)
    }

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
    */
  private def notInList(s: String, list: List[String])
                          (implicit separators: Separators): Boolean =
    !list.contains(unescape(s))

  /**
    * Returns true if the list does not contain 'd'
    */
  private def notInList(d: Double, list: List[Double]): Boolean = !list.contains(d)

  /**
    * Returns true if s can be converted to a Double
    */
  private def convertibleToDouble(s: String): Boolean =
    try { s.toDouble; true } catch { case e: Throwable => false }

  private def valueComparisonErrors(xs: List[(Simple, Try[Boolean])], sv: SimpleValue) =
    xs map {
      case(s, Failure(e)) =>
        val m = e.getMessage
        s"${loc(s.location)} ${s.value} ${sv.operator} ${sv.value} failed. Reason: $m"
      case _ => ???
    }

  private def pvErrMsgs(pv: PathValue, xs1: List[Simple], xs2: List[Simple]) =
    s"path1(${pv.path1}) and path2(${pv.path2
    }) resolution returned respectively ${xs1.length} and ${xs1.length} elements." :: Nil
}