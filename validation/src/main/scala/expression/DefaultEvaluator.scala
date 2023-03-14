package expression

import expression.EvalResult._
import hl7.v2.instance.Query._
import hl7.v2.instance.{Complex, _}
import hl7.v2.profile.Range
import hl7.v2.validation.vs.{Validator, ValueSetLibrary}
import gov.nist.validation.report.Entry

import scala.jdk.CollectionConverters.ListHasAsScala
import scala.util.{Failure, Success, Try}
import java.lang.reflect.Method

trait DefaultEvaluator extends Evaluator with EscapeSeqHandler {

  /**
   * Evaluates the expression within the specified context
   * and returns the result
   * @param e - The expression to be evaluated
   * @param c - The context node
   * @param s - The message separators
   * @param t - The default time zone
   * @return The evaluation result
   */
  def eval(e: Expression, c: Element)(implicit l: ValueSetLibrary, s: Separators,
                                      t: Option[TimeZone], VSValidator : Validator): EvalResult = e match {
    case x: Presence          => presence(x, c)
    case x: PlainText         => plainText(x, c)
    case x: Format            => format(x, c)
    case x: NumberList        => numberList(x, c)
    case x: StringList        => stringList(x, c)
    case x: SimpleValue       => simpleValue(x, c)
    case x: PathValue         => pathValue(x, c)
    case x: ComplexPathValue  => complexPathValue(x, c)
    case x: SubContext        => subContext(x, c)
    case x: AND               => and(x, c)
    case x: OR                => or(x, c)
    case x: NOT               => not(x, c)
    case x: XOR               => xor(x, c)
    case x: IMPLY             => imply(x, c)
    case x: EXIST             => exist(x, c)
    case x: FORALL            => forall(x, c)
    case x: Plugin            => plugin(x, c)
    case x: SetId             => setId(x, c)
    case x: IZSetId           => IZsetId(x, c)
    case x: ValueSet          => valueSet(x, c)
    case x: isNULL            => isNull(x, c)
    case x: StringFormat      => stringFormat(x, c)
  }

  def notFound(expression: Expression, path: String, context: Element, notPresentBehavior: String): EvalResult =
    notPresentBehavior.toUpperCase() match {
      case "FAIL" => Failures.notPresentBehaviorFail(expression, path, context)
      case "INCONCLUSIVE" => Failures.notPresentBehaviorInconclusive(expression, path,  context)
      case _ => Pass
    }

  def checkRange(atLeastOnce: Boolean, range: Option[Range], n: Int, all: Int): Boolean = {
    range match {
      case Some(r) => r.includes(n)
      case None => if(atLeastOnce) n > 0 else n == all
    }
  }

  /**
   * Evaluates the presence expression and returns the result
   * @param p       - The presence expression
   * @param context - The context
   * @return The evaluation result
   */
  def presence(p: Presence, context: Element): EvalResult =
    query(context, p.path) match {
      case Success(Nil) => Failures.presence(context, p)
      case Success(_)   => Pass
      case Failure(e)   => inconclusive(p, context.location, e)
    }

  /**
   * Evaluates the plain text expression and returns the result
   * @param p       - The plain text expression
   * @param context - The context
   * @return The evaluation result
   */
  def plainText(p: PlainText, context: Element)(implicit s: Separators): EvalResult =
    queryAsSimple(context, p.path) match {
      case Success(ls) =>
        ls match {
          case Nil => notFound(p, p.path, context, p.notPresentBehavior)
          case list => list filter (x => notEqual(x, p.text, p.ignoreCase)) match {
            case xs  => if (checkRange(p.atLeastOnce, p.range,  list.size - xs.size, list.size)) Pass else Failures.plainText(p, xs)
          }
        }
      case Failure(e) => inconclusive(p, context.location, e)
    }

  /**
   * Evaluates the format expression and returns the result
   * @param f       - The format expression
   * @param context - The context
   * @return The evaluation result
   */
  def format(f: Format, context: Element)(implicit s: Separators): EvalResult =
    queryAsSimple(context, f.path) match {
      case Success(list) =>
        list match {
          case Nil => notFound(f, f.path, context, f.notPresentBehavior)
          case ls => ls filter (x => notMatch(x, f.pattern)) match {
            case xs  => if (checkRange(f.atLeastOnce, f.range,  ls.size - xs.size, ls.size)) Pass else Failures.format(f, xs)
          }
        }
      case Failure(e) => inconclusive(f, context.location, e)
    }

  /**
   * Evaluates the string list expression and returns the result
   * @param sl      - The string list expression
   * @param context - The context
   * @return The evaluation result
   */
  def stringList(sl: StringList, context: Element)(implicit s: Separators): EvalResult =
    queryAsSimple(context, sl.path) match {
      case Success(list) =>
        list match {
          case Nil => notFound(sl, sl.path, context, sl.notPresentBehavior)
          case ls => ls filter (x => notInList(x.value.raw, sl.csv)) match {
            case xs  => if (checkRange(sl.atLeastOnce, sl.range,  ls.size - xs.size, ls.size)) Pass else Failures.stringList(sl, xs)
          }
        }
      case Failure(e) => inconclusive(sl, context.location, e)
    }

  /**
   * Evaluates the number list expression and returns the result
   * @param nl      - The number list expression
   * @param context - The context
   * @return The evaluation result
   */
  def numberList(nl: NumberList, context: Element)(implicit s: Separators): EvalResult =
    queryAsSimple(context, nl.path) match {
      case Success(list) =>
        list match {
          case Nil => notFound(nl, nl.path, context, nl.notPresentBehavior)
          case ls =>
            val (l1, l2) = ls partition (x => convertibleToDouble(x.value.raw))
            l2 match {
              case Nil =>
                l1 filter (x => notInList(x.value.raw.toDouble, nl.csv)) match {
                  case xs  => if (checkRange(nl.atLeastOnce, nl.range,  ls.size - xs.size, ls.size)) Pass else Failures.numberList(nl, xs)
                }
              case xs => Failures.numberListNaN(nl, xs)
            }
        }
      case Failure(e) => inconclusive(nl, context.location, e)
    }

  /**
   * Evaluates the simple value expression and returns the result
   * @param sv      - The simple value expression
   * @param context - The context
   * @return The evaluation result
   */
  def simpleValue(sv: SimpleValue, context: Element)(implicit dtz: Option[TimeZone]): EvalResult =
    queryAsSimple(context, sv.path) match {
      case Success(list) =>
        list match {
          case Nil => notFound(sv, sv.path, context, sv.notPresentBehavior)
          case ls =>
            val evs = ls map { s => s -> sv.operator.eval(s.value, sv.value, sv.comparisonMode) }
            evs partition {
              _._2.isFailure
            } match {
              case (Nil, xs) =>
                xs filter {
                  _._2 == Success(false)
                } match {
                  case ys => if (checkRange(sv.atLeastOnce, sv.range,  xs.size - ys.size, xs.size)) Pass else Failures.simpleValue(sv, ys map { x => x._1 })
                }
              case (xs, _) => inconclusive(sv, xs)
            }
        }
      case Failure(e) => inconclusive(sv, context.location, e)
    }

  /**
   * Evaluates the path value expression and returns the result
   * @param pv      - The path value expression
   * @param context - The context
   * @return The evaluation result
   */
  def pathValue(pv: PathValue, context: Element)(implicit dtz: Option[TimeZone]): EvalResult =
    (queryAsSimple(context, pv.path1), queryAsSimple(context, pv.path2)) match {
      case (Success(Nil), Success(Nil))      => notFound(pv, s"[ ${pv.path1}, ${pv.path2} ]", context, pv.notPresentBehavior)
      case (Success(x :: Nil), Success(Nil)) => Failures.pathValue(pv, x, pv.path2)
      case (Success(Nil), Success(x :: Nil)) => Failures.pathValue(pv, x, pv.path1)
//      case (Success(x1 :: Nil), Success(x2 :: Nil)) =>
//        pv.operator.eval(x1.value, x2.value, pv.comparisonMode) match {
//          case Success(true)  => Pass
//          case Success(false) => Failures.pathValue(pv, x1, x2)
//          case Failure(e)     => inconclusive(pv, context.location, e)
//        }
      case (Success(xs1), Success(xs2)) => multiElementsCompare(pv, xs1, xs2)
//        inconclusive(pv, context, xs1, xs2)
      case (Failure(e), _)              => inconclusive(pv, context.location, e)
      case (_, Failure(e))              => inconclusive(pv, context.location, e)
    }

  case class PassFail(pass: Int, fail: Int)

  def multiElementsCompare(pv: PathValue, elementsLeft: List[Simple], elementsRight: List[Simple])(implicit dtz: Option[TimeZone]): EvalResult = {
    val allComparisons = elementsLeft.map(el => evaluate(el.value, elementsRight.map(_.value), pv))
    val checkRightModeResult = splitBooleanList(
      allComparisons.map(comparison => checkMode(pv.path2Mode, comparison.pass, comparison.fail))
    )
    if (checkMode(pv.path1Mode, checkRightModeResult.pass, checkRightModeResult.fail)) {
      EvalResult.Pass
    } else {
      Failures.pathValue(
        pv,
        elementsLeft,
        elementsRight,
      )
    }
  }

  def splitBooleanList(list: List[Boolean]): PassFail = {
    list partition (x => x) match {
      case (pass, fail) => PassFail(pass.size, fail.size)
    }
  }

  def evaluate(element: Value, elements: List[Value], pv: PathValue)(implicit dtz: Option[TimeZone]): PassFail = {
    splitBooleanList(
      elements.map(pv.operator.eval(element, _, pv.comparisonMode) match {
        case Success(true) => true
        case _ => false
      })
    )
  }

  def checkMode(mode: MultiCompareMode, passCount: Int, failCount: Int): Boolean = {
    mode match {
      case MultiCompareMode.AtLeastOne() => passCount >= 1
      case MultiCompareMode.All() => failCount == 0
      case MultiCompareMode.Count(n) => passCount == n
    }
  }

  /**
    * Evaluates the path value expression and returns the result
    * @param pv      - The path value expression
    * @param context - The context
    * @return The evaluation result
    */
  def complexPathValue(pv: ComplexPathValue, context: Element)(implicit dtz: Option[TimeZone]): EvalResult =
    (query(context, pv.path1), query(context, pv.path2)) match {
      case (Success(Nil), Success(Nil))      => notFound(pv, s"[ ${pv.path1}, ${pv.path2} ]", context, pv.notPresentBehavior)
      case (Success(x :: Nil), Success(Nil)) => if(pv.strict) Failures.complexPathValue(pv, x, context, pv.path2) else Pass
      case (Success(Nil), Success(x :: Nil)) => if(pv.strict) Failures.complexPathValue(pv, x, context, pv.path1) else Pass
      case (Success(x1), Success(x2)) =>
        val results = compareOccurrences(pv, context, x1, x2)
        val (failure, inconclusive) = (
          results.collect{ case f: Fail => f },
          results.collect{ case inconclusive: Inconclusive => inconclusive }
        )
        if (failure.nonEmpty) Failures.complexPathValue(pv, context, failure)
        else if(inconclusive.nonEmpty) Failures.complexPathValue(pv, context, inconclusive)
        else Pass
      case (Failure(e), _)              => inconclusive(pv, context.location, e)
      case (_, Failure(e))              => inconclusive(pv, context.location, e)
    }


  type ElementWithLocation = (String, Option[Element])
  type ComplexChildrenZip = ((Int, Int), Option[Element], Option[Element])
  case class MoreThanOne(p: Int, i: Int, ls: List[Element], first: Boolean) extends Throwable

  def childrenZipList[T <: Element](ls1: List[T], ls2: List[T]): Try[List[ComplexChildrenZip]] = {
    val ls1Map = ls1.groupBy(e => (e.position, e.instance))
    val ls2Map = ls2.groupBy(e => (e.position, e.instance))
    val idx = (ls1Map.keys.toList:::ls2Map.keys.toList).distinct
    Try(
      for(i <- idx) yield {
        val p1 = ls1Map.get(i)
        val p2 = ls2Map.get(i)
        if(p1.isDefined && p1.get.length > 1) throw MoreThanOne(i._1, i._2, p1.get, true)
        if(p2.isDefined && p2.get.length > 1) throw MoreThanOne(i._1, i._2, p2.get, false)
        (i, p1.map(_.head), p2.map(_.head))
      }
    )
  }

  def compareOccurrences(pv: ComplexPathValue, context: Element, ls1: List[Element], ls2: List[Element])(implicit dtz: Option[TimeZone]): List[EvalResult] = {
    val ls1Map = ls1.groupBy(_.instance)
    val ls2Map = ls2.groupBy(_.instance)
    val idx = (ls1Map.keys.toList:::ls2Map.keys.toList).distinct
    val elements = for(i <- idx) yield {
      val p1 = ls1Map.get(i)
      val p2 = ls2Map.get(i)
      (i, p1.map(_.head), p2.map(_.head))
    }
    elements.flatMap((tuple) => {
      val elm1 =  (s"${context.location.path}.${pv.path1} (instance : ${tuple._1})", tuple._2)
      val elm2 =  (s"${context.location.path}.${pv.path2} (instance : ${tuple._1})", tuple._3)
      compareElement(pv, elm1, elm2)
    })
  }

  def compareElements(pv: ComplexPathValue, p1: Element, ls1: List[Element], p2: Element, ls2: List[Element])(implicit dtz: Option[TimeZone]): List[EvalResult] = {
    childrenZipList(ls1, ls2) match {
      case Success(ls) => ls.foldLeft(List[EvalResult]()) {
        (acc, tuple) =>
          val elm1 =  (s"${p1.location.path}(${p1.location.desc}).${tuple._1._1}[${tuple._1._2}]", tuple._2)
          val elm2 =  (s"${p2.location.path}(${p2.location.desc}).${tuple._1._1}[${tuple._1._2}]", tuple._3)
          compareElement(pv, elm1, elm2):::acc
      }
      case Failure(MoreThanOne(p, i, ls, f)) => Failures.inconclusivePathComparison(pv, if(f) p1 else p2, p, i, ls)::Nil
      case _ => Nil
    }
  }

  def compareElement(pv: ComplexPathValue, p1: ElementWithLocation, p2: ElementWithLocation)(implicit dtz: Option[TimeZone]): List[EvalResult] = {
    (p1._2, p2._2) match {
      case (Some(e1), Some(e2)) => (e1, e2) match {
        case (s1: Simple, s2: Simple) => compareSimple(pv, s1, s2)::Nil
        case (cc1: Complex, cc2: Complex) => compareElements(pv, cc1, cc1.children, cc2, cc2.children)
        case _ => Failures.inconclusivePathComparison(pv, e1, e2)::Nil
      }
      case (None, Some(e2)) => if(pv.strict) Failures.complexPathValue(pv, e2, p1._1)::Nil else Nil
      case (Some(e1), None) => if(pv.strict) Failures.complexPathValue(pv, e1, p2._1)::Nil else Nil
      case (None, None) => Nil
    }
  }

  def compareSimple(pv: ComplexPathValue, s1: Simple, s2: Simple)(implicit dtz: Option[TimeZone]): EvalResult = {
    pv.operator.eval(s1.value, s2.value, pv.comparisonMode) match {
      case Success(true)  => Pass
      case Success(false) => Failures.complexPathValue(pv, s1, s2)
      case Failure(e)     => inconclusive(pv, s1.location, e)
    }
  }


  /**
   * Evaluates the value set expression and returns the result
   * @param vs      - The value set expression
   * @param context - The context
   * @return The result of the evaluation
   */
  def valueSet(vs: ValueSet, context: Element)(implicit l: ValueSetLibrary, VSValidator : Validator): EvalResult =
    query(context, vs.path) match {
      case Failure(e)   => inconclusive(vs, context.location, e)
      case Success(Nil) => notFound(vs, vs.path, context, vs.notPresentBehavior)
      case Success(x :: Nil) =>
        val r = VSValidator.checkValueSet(x, vs.spec, l)   
        if (isVSViolated(r)) Failures.valueSet(vs, r) else Pass
      case Success(xs) =>
        val msg = "Path resolution returned more than one element"
        inconclusive(vs, context.location, msg)
    }

  def isNull(n: isNULL, context: Element): EvalResult = {
    def loop(l: List[Element]): EvalResult = {
      l match {
        case Nil => Pass
        case x :: ls => x.location.eType match {
          case EType.Field => x match {
            case c: Complex => if (!c.isInstanceOf[NULLComplexField]) Failures.isNull(context, n, true) else loop(ls)
            case s: Simple  => if (!s.value.isNull) Failures.isNull(context, n, true) else loop(ls)
          }
          case _ => Failures.isNull(context, n, false)
        }
      }
    }
    query(context, n.path) match {
      case Failure(e)   => inconclusive(n, context.location, e)
      case Success(Nil) => Failures.isNull(context, n)
      case Success(xs)  => loop(xs)
    }
  }



  def contextRange(range: Option[Range], atLeastOnce: Option[Boolean]): Try[Option[Range]] = {
    (range, atLeastOnce) match {
      case (Some(r), Some(b)) => Failure(new Throwable("Min/Max Cardinality and AtLeastOnce were both specified, only one should be used in SubContext expression"))
      case (None, Some(b)) => if(b) Success(Some(Range(1, "*"))) else Success(None)
      case (Some(r), None) => Success(Some(r))
      case (None, None) => Success(None)
    }
  }

  /**
    * Evaluates the Context expression which provides a sub-context to the assertion to run
    * @param subContext - The Context expression
    * @param context - The context
    * @return The evaluation result
    */
  def subContext(subContext: SubContext, context: Element)(implicit l: ValueSetLibrary, s: Separators, dtz: Option[TimeZone], VSValidator : Validator): EvalResult =
    contextRange(subContext.cardinality, subContext.atLeastOnce) match {
      case Success(range) => query(context, subContext.path) match {
        case Failure(e)   => inconclusive(subContext, context.location, e)
        case Success(Nil) => {
          notFound(subContext, subContext.path, context, subContext.notPresentBehavior)
        }
        case Success(xs)  =>
          val evalResult = xs.map(eval(subContext.assertion, _))
          val (success, failure, inconclusive) = (
            evalResult.collect{ case Pass => Pass },
            evalResult.collect{ case f: Fail => f },
            evalResult.collect{ case inconclusive: Inconclusive => inconclusive }
          )

          if(inconclusive.nonEmpty) Failures.subContextInconclusive(subContext, context, inconclusive)
          else if (range.map(r => r.includes(success.length)).getOrElse(failure.isEmpty))
            Pass
          else Failures.subContext(subContext, context, range, xs.length, failure)
      }
      case Failure(e) => inconclusive(subContext, context.location, e)
    }

  /**
   * Evaluates the AND expression and returns the result
   * @param and     - The AND expression
   * @param context - The context
   * @return The evaluation result
   */
  def and(and: AND, context: Element)(implicit l: ValueSetLibrary, s: Separators,
                                      dtz: Option[TimeZone], VSValidator : Validator): EvalResult =
    eval(and.exp1, context) match {
      case i: Inconclusive => i
      case f: Fail         => Failures.and(and, context, f)
      case f: FailPlugin         => Failures.and(and, context, Fail(f.stack))
      case Pass =>
        eval(and.exp2, context) match {
          case f: FailPlugin         => Failures.and(and, context, Fail(f.stack))
          case f: Fail => Failures.and(and, context, f)
          case x       => x
        }
    }

  /**
   * Evaluates the OR expression and returns the result
   * @param or      - The OR expression
   * @param context - The context
   * @return The evaluation result
   */
  def or(or: OR, context: Element)(implicit l: ValueSetLibrary, s: Separators,
                                   dtz: Option[TimeZone], VSValidator : Validator): EvalResult =
    eval(or.exp1, context) match {
      case f1: Fail =>
        eval(or.exp2, context) match {
          case f2: Fail => Failures.or(or, context, f1, f2)
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
  def not(not: NOT, context: Element)(implicit l: ValueSetLibrary, s: Separators,
                                      dtz: Option[TimeZone], VSValidator : Validator): EvalResult =
    eval(not.exp, context) match {
      case Pass            => not.exp match {
        case Presence(p)   => Failures.not(not, query(context,p).get.head)
        case _             => Failures.not(not, context)
      }
      case Fail(_) | FailPlugin(_,_)        => Pass
      case i: Inconclusive => i
    }
  //x ⊕ y   =   (x ∨ y) ∧ ¬(x ∧ y)
  def xor(xor: XOR, context: Element)(implicit l: ValueSetLibrary, s: Separators,
                                      dtz: Option[TimeZone], VSValidator : Validator): EvalResult =
    eval(AND(OR(xor.exp1, xor.exp2), NOT(AND(xor.exp1, xor.exp2))), context)

  def imply(e: IMPLY, context: Element)(implicit l: ValueSetLibrary, s: Separators,
                                        dtz: Option[TimeZone], VSValidator : Validator): EvalResult =
    eval(OR(NOT(e.exp1), e.exp2), context)

  def exist(e: EXIST, context: Element)(implicit l: ValueSetLibrary, s: Separators,
                                        dtz: Option[TimeZone], VSValidator : Validator): EvalResult = {
    def loop(expressions: List[Expression]): EvalResult = {
      expressions match {
        case x :: Nil => eval(x, context)
        case x :: y => eval(x, context) match {
          case Pass => Pass
          case Fail(tr1) => loop(y) match {
            case Pass      => Pass
            case Fail(tr2) => Fail(tr1 ::: tr2)
            case x         => x
          }
          case x => x
        }
        case Nil => Inconclusive(Trace(e, Reason(null, "No assertion to test") :: Nil))
      }
    }
    loop(e.list toList) match {
      case Fail(tr) => Failures.exist(e, context, Fail(tr))
      case Pass     => Pass
      case x        => x
    }
  }

  def forall(e: FORALL, context: Element)(implicit l: ValueSetLibrary, s: Separators,
                                          dtz: Option[TimeZone], VSValidator : Validator): EvalResult = {

    def loop(expressions: List[Expression]): EvalResult = {
      expressions match {
        case x :: Nil => eval(x, context)
        case x :: y => eval(x, context) match {
          case Pass     => loop(y)
          case Fail(tr) => Fail(tr)
          case x        => x
        }
        case Nil => Inconclusive(Trace(e, Reason(null, "No assertion to test") :: Nil))
      }
    }
    loop(e.list toList) match {
      case Fail(tr) => Failures.forall(e, context, Fail(tr))
      case Pass     => Pass
      case x        => x
    }
  }

  def setId(e: SetId, context: Element) =
    queryAsSimple(context, e.path) match {
      case Success(x :: Nil) =>
        if (context.instance.toString == x.value.raw) Pass
        else Failures.seqId(e, context, x)
      case Success(xs) =>
        val m = s"$e Path resolution returned more than one element"
        inconclusive(e, context.location, m)
      case Failure(f) => inconclusive(e, context.location, f)
    }

  def IZsetId(e: IZSetId, context: Element) =
    query(context, e.parent) match {
      case Failure(f)   => inconclusive(e, context.location, f)
      case Success(Nil) => Pass
      case Success(xs) => {
        parent(xs, e.element) match {
          case Failure(f)   => inconclusive(e, context.location, f)
          case Success(Nil) => Pass
          case Success(ys) => {
            
            val L = ys.foldLeft(List[(Simple, Int)]())({ (acc, l) =>
              acc ++ (l zip (LazyList from 1))
            }) zip (LazyList from 1)
            
            
            val a = L.foldLeft(List[(Simple, Int)]()) { (acc, x) =>
              toInt(x._1._1.value.raw) match {
                case None    => (x._1._1, -1) :: acc
                case Some(i) => if (i == x._1._2) acc else (x._1._1, x._1._2) :: acc
              }
            }

            val b = L.foldLeft(List[(Simple, Int)]()) { (acc, x) =>
              toInt(x._1._1.value.raw) match {
                case None    => (x._1._1, -1) :: acc
                case Some(i) => if (i == x._2) acc else (x._1._1, x._2) :: acc
              }
            }
            
            if((a.size*b.size) == 0) Pass
            else Failures.IZseqId(e, context, (if (b.size < a.size) b else a))
          }
        }
      }
    }

 
  def stringFormat(e : StringFormat, context: Element)(implicit s: Separators) = {
    try {
      val validator = StringType.fromString(e.format)
      queryAsSimple(context, e.path) match {
        case Success(ls) =>
          ls match {
            case Nil => notFound(e, e.path, context, e.notPresentBehavior)
            case xs => xs.filter { elm => !validator.validate(elm.value.raw) } match {
              case ys => if (checkRange(e.atLeastOnce, e.range,  xs.size - ys.size, xs.size)) Pass else Failures.stringFromat(e, ys)
            }
          }
        case Failure(f) => inconclusive(e, context.location, f)
      }
    }
    catch {
      case u : UnknownStringFormatException => inconclusive(e, context.location, u.getMessage)
    }    
  }
  
  def toInt(s: String): Option[Int] = {
    try {
      Some(s.toInt)
    } catch {
      case e: Exception => None
    }
  }
  
  def debug(l : List[((Simple,Int),Int)]) = {
      l.map { x => ((x._1._1.value.raw,x._1._2),x._2) }
  }
  
  def parent(l: List[Element], path: String): Try[List[List[Simple]]] = {
    l match {
      case Nil => Success(Nil);
      case x => {
        queryAsSimple(x.head, path) match {
          case Failure(f) => Failure(f)
          case Success(l) => parent(x.tail, path) match {
            case Failure(f)   => Failure(f)
            case Success(lss) => Success(childs(l) :: lss)
          }
        }
      }
    }
  }

  def childs(l: List[Simple]): List[Simple] = {
    l match {
      case Nil => Nil
      case x   => x.head :: childs(x.tail);
    }
  }

  /**
   * Evaluates the plugin expression and returns the result
   * @param e       - The plugin expression
   * @param context - The context
   * @return The evaluation result
   */
  def plugin(e: Plugin, context: Element)(implicit s: Separators): EvalResult =
    try {
      val clazz = Class.forName(e.clazz)
      
      var methods = scala.collection.mutable.Map[String, Method]();
      for(x <- clazz.getDeclaredMethods) {
        if( x.getName.equals("assertion") ||  x.getName.equals("assertionWithCustomMessages")) 
          methods += x.getName -> x
      }
      
      if(methods.size == 0) throw new Exception("No method defined for plugin");
      else if(methods.size > 1) throw new Exception("More than one method defined for plugin");
      else {
        if(methods.head._1.equals("assertion")){
          methods.head._2.invoke(clazz.newInstance(), context).asInstanceOf[Boolean] match {
            case true  => Pass
            case false => Fail(Nil)
          }
        }
        else {
          methods.head._2.invoke(clazz.newInstance(), context).asInstanceOf[java.util.List[String]] match {
            case null => Pass
            case str : java.util.List[String] => if(str != null && !str.isEmpty) FailPlugin(Nil, str.asScala.toList) else Pass
          }
        }
      }
    } catch { case f: Throwable => inconclusive(e, context.location, f) }

  /**
   * Returns true if the unescaped value of 's' is not
   * equal to the unescaped 'text' depending on the case
   */
  private def notEqual(s: Simple, text: String, cs: Boolean)(implicit separators: Separators): Boolean =
    if (cs) !unescape(s.value.raw).equalsIgnoreCase(text)
    else unescape(s.value.raw) != text

  /**
   * Returns true if the unescaped value of 's'
   * don't match the regular expression 'regex'
   */
  private def notMatch(s: Simple, regex: String)(implicit separators: Separators): Boolean =
    !regex.r.pattern.matcher(unescape(s.value.raw)).matches

  /**
   * Returns true if the list does not contain 's'.
   */
  private def notInList(s: String, list: List[String])(implicit separators: Separators): Boolean =
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
      case _               => ??? //Not gonna happens
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

  //FIXME This needs to be updated according to Rob's feedback
  //FIXME For now every non null entry is considered as violation
  private def isVSViolated(e: Entry): Boolean = e != null

}
