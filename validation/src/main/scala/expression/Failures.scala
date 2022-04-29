package expression

import expression.EvalResult.{Fail, Reason, Trace}
import hl7.v2.instance._
import hl7.v2.profile.Range
import gov.nist.validation.report.Entry
import hl7.v2.instance.Query._

import scala.util.{Failure, Success}
import expression.EvalResult.Inconclusive
object Failures extends EscapeSeqHandler {

  /**
    * Creates and returns a presence failure stack traces
    */
  def presence(c: Element, e: Presence): Fail = {
    val nar = narrowLocation(c,e.path)
    val path    = s"${c.location.path}.${e.path}"
    val reasons = Reason( nar.location, s"$path is missing"):: Nil
    Fail( Trace( e, reasons ) :: Nil )
  }
  
  def isNull(c: Element, e: isNULL, isField : Boolean): Fail = {
    val path    = s"${c.location.path}.${e.path}"
    val r = if(isField) " is not NULL" else " is not a Field"
    val reasons = Reason( c.location, s"$path "+r):: Nil
    Fail( Trace( e, reasons ) :: Nil )
  }
  
  def isNull(c: Element, e: isNULL): Fail = {
    val path    = s"${c.location.path}.${e.path}"
    val reasons = Reason( c.location, s"$path is missing"):: Nil
    Fail( Trace( e, reasons ) :: Nil )
  }
  
  def formatCheck(c: Element, e: StringFormat): Fail = {
    val path    = s"${c.location.path}.${e.path}"
    val reasons = Reason( c.location, s"$path is not conform to ${e.format} string format"):: Nil
    Fail( Trace( e, reasons ) :: Nil )
  }

  /**
    * Creates and returns a plain text failure stack traces
    */
  def plainText(e: PlainText, xs: List[Simple])(implicit s: Separators): Fail = {
    val cs = if( e.ignoreCase ) "case insensitive" else "case sensitive"
    val reasons = xs map { x =>
      Reason(x.location, s"'${unescape(x.value.raw)}' is different from '${e.text}' ($cs)")
    }
    Fail( Trace(e, reasons) :: Nil )
  }
  
  def notPresentBehaviorFail(p: Expression, path: String, context: Element) : Fail = {
    val msg = s"Path ${path} resolution from ${context.location.prettyString} returned no element"
    Fail( Trace(p, Reason(context.location, msg) :: Nil) :: Nil )
  }
  
  def notPresentBehaviorInconclusive(p: Expression, path: String, context: Element) : Inconclusive = {
     val msg = s"Path ${path} resolution from ${context.location.prettyString} returned no element"
     Inconclusive( Trace(p, Reason(context.location, msg) :: Nil) )
  }

    /**
    * Creates and returns a plain text failure stack traces
    */
  def stringFromat(e: StringFormat, xs: List[Simple])(implicit s: Separators): Fail = {
    val reasons = xs map { x =>
      Reason(x.location, s"'${unescape(x.value.raw)}' does not match '${e.toString}'")
    }
    Fail( Trace(e, reasons) :: Nil )
  }
  
  /**
    * Creates and returns a format failure stack traces
    */
  def format(e: Format, xs: List[Simple])(implicit s: Separators): Fail = {
    val reasons = xs map { x =>
      Reason(x.location, s"'${unescape(x.value.raw)}' doesn't match '${e.pattern}'")
    }
    Fail( Trace(e, reasons) :: Nil )
  }

  /**
    * Creates and returns a number list failure stack traces
    * This is used when NaN is found
    */
  def numberListNaN(e: NumberList, xs: List[Simple]): Fail = {
    val reasons = xs map { x =>
      Reason(x.location, s"${x.value} cannot be treated as a number")
    }
    Fail( Trace(e, reasons) :: Nil )
  }

  /**
    * Creates and returns a number list failure stack traces
    */
  def numberList(e: NumberList, xs: List[Simple]): Fail = list( e, xs, e.csv )

  /**
    * Creates and returns a string list failure stack traces
    */
  def stringList(e: StringList, xs: List[Simple]): Fail = list( e, xs, e.csv )

  /**
    * Creates and returns a list failure stack traces
    */
  private def list[T](e: Expression, xs: List[Simple], l: List[T]): Fail = {
    val ls = l.mkString("{ '", "', '", "' }")
    val reasons = xs map { s =>
      Reason(s.location, s"'${s.value.raw}' is not in the list $ls")
    }
    Fail( Trace(e, reasons) :: Nil )
  }

  /**
    * Creates and returns a simple value failure stack traces
    */
  def simpleValue( e: SimpleValue, xs: List[Simple]): Fail = {
    val reasons = xs map { x =>
      Reason(x.location, s"${x.value} is not ${e.operator} ${e.value}")
    }
    Fail( Trace(e, reasons) :: Nil )
  }

  /**
    * Creates and returns a path value failure stack traces
    */
  def pathValue( e: PathValue, s1: Simple, s2: Simple ): Fail = {
    val m = s"${s1.value} is not ${e.operator} ${s2.value}"
    val reasons = Reason(s1.location, m) :: Nil
    Fail( Trace(e, reasons) :: Nil )
  }

  def pathValue(e: PathValue, s1: List[Simple], s2: List[Simple]) = {
    val m = s"${MultiCompareMode.toString(e.path1Mode)} element(s) from ${s1.map(_.value.raw).mkString("[", ",", "]")} are not ${e.operator} ${MultiCompareMode.toString(e.path2Mode)} element(s) from ${s2.map(_.value.raw).mkString("[", ",", "]")}"
    val reasons = Reason(s1.head.location, m) :: Nil
    Fail( Trace(e, reasons) :: Nil )
  }

  /**
    * Creates and returns a subContext failure stack traces
    */
  def subContext(e: SubContext, c: Element, range: Option[Range], elms: Int, failures: List[Fail]): Fail = {
    val m = s"SubContext assertion evaluation expected [${range.map(r => s"${r.min}, ${r.max}").getOrElse("*, *")}] success, but ${failures.length} out of ${elms} failed"
    val reasons = Reason(c.location, m) :: Nil
    Fail( Trace(e, reasons) :: failures.flatMap(_.stack) )
  }

  /**
    * Creates and returns a subContext failure stack traces
    */
  def subContextInconclusive(e: SubContext, c: Element, inconclusive: List[Inconclusive]): Inconclusive = {
    val m = s"One or more expressions evaluation on a subContext path ${e.path} was inconclusive"
    val reasons = Reason(c.location, m) :: Nil
    Inconclusive(Trace(e, reasons ::: inconclusive.flatMap(_.trace.reasons)))
  }

  /**
    * Creates and returns a path value failure stack traces
    * This is used when only one path is populated
    */
  def pathValue( e: PathValue, s: Simple, p: String): Fail = {
    val m = s"${s.location.path}(${s.location.desc}) is populated but $p is not"
    val reasons = Reason(s.location, m) :: Nil
    Fail( Trace(e, reasons) :: Nil )
  }

  /**
    * Creates and returns a complex path value failure stack traces
    * This is used when only one path is populated
    */
  def complexPathValue( e: ComplexPathValue, s: Element, parent: Element, p: String): Fail = {
    val m = s"${s.location.path}(${s.location.desc}) is populated but ${parent.location.path}(${parent.location.desc}).$p is not"
    val reasons = Reason(s.location, m) :: Nil
    Fail( Trace(e, reasons) :: Nil )
  }

  /**
    * Creates and returns a complex path value failure stack traces
    * This is used when only one path is populated
    */
  def complexPathValue( e: ComplexPathValue, s: Element, p: String): Fail = {
    val m = s"${s.location.path}(${s.location.desc}) is populated but $p is not"
    val reasons = Reason(s.location, m) :: Nil
    Fail( Trace(e, reasons) :: Nil )
  }

  /**
    * This is used when only one path value is not equal to the other
    */
  def complexPathValue( e: ComplexPathValue, s1: Simple, s2: Simple): Fail = {
    val m = s"${s1.value} is not ${e.operator} ${s2.value}"
    val reasons = Reason(s1.location, m) :: Nil
    Fail( Trace(e, reasons) :: Nil )
  }

  /**
    * This is used when path value cardinality is not the same
    */
  def complexPathValue( e: ComplexPathValue, s: Element, p1: String, p1s: Int, p2: String, p2s: Int): Fail = {
    val root = s"${s.location.path}(${s.location.desc})"
    val m = s"${root}.${p1} has ${p1s} occurrences but ${root}.${p2} has ${p2s} occurrences"
    val reasons = Reason(s.location, m) :: Nil
    Fail( Trace(e, reasons) :: Nil )
  }

  /**
    * This is used when paths comparison is inconclusive
    */
  def complexPathValue( e: ComplexPathValue, c: Element, inconclusive: List[Inconclusive]): Inconclusive = {
    val root = s"${c.location.path}(${c.location.desc})"
    val m = s"One or more element comparisons within ${root}.${e.path1} and ${root}.${e.path2} were inconclusive"
    val reasons = Reason(c.location, m) :: Nil
    Inconclusive(Trace(e, reasons ::: inconclusive.flatMap(_.trace.reasons)))
  }

  /**
    * This is used when paths comparison is inconclusive
    */
  def complexPathValue( e: ComplexPathValue, c: Element, failures: List[Fail]): Fail = {
    val root = s"${c.location.path}(${c.location.desc})"
    val m = s"One or more element comparisons within ${root}.${e.path1} and ${root}.${e.path2} failed"
    val reasons = Reason(c.location, m) :: Nil
    Fail( Trace(e, reasons) :: failures.flatMap(_.stack) )
  }

  /**
    * This is used when paths types are different than component or field
    */
  def inconclusivePathComparison(e: ComplexPathValue, c1: Element, c2: Element): Inconclusive = {
    val c1p = s"${c1.location.path}(${c1.location.desc})"
    val c2p = s"${c2.location.path}(${c2.location.desc})"
    val m = s"Comparing ${c1p} and ${c2p} of incompatible types (Complex/Simple)"
    Inconclusive(Trace(e, Reason(c1.location, m) :: Nil))
  }

  /**
    * This is used when path has duplicate elements with same instance and position
    */
  def inconclusivePathComparison(e: ComplexPathValue, c1: Element, p: Int, i: Int, elms: List[Element]): Inconclusive = {
    val c1p = s"${c1.location.path}(${c1.location.desc})"
    val m = s"Invalid parsing of message ${c1p}.${p}[${i}] returned ${elms.length} elements"
    Inconclusive(Trace(e, Reason(c1.location, m) :: Nil))
  }

  /**
    * Creates and returns a value set failure stack traces
    */
  def valueSet(e: ValueSet, x: Entry): Fail = {
    val location = Location(null, "...", x.getPath, x.getLine, x.getColumn)
    var reasons = Reason(location, x.getDescription) :: Nil

    if( x.getStackTrace != null )
      (0 until x.getStackTrace.size()) foreach { i =>
        val gtrace = x.getStackTrace.get(i)
        (0 until gtrace.getReasons.size()) foreach { n =>
          reasons = Reason(location, gtrace.getReasons.get(n)) :: reasons
        }
      }
    Fail( Trace(e, reasons) :: Nil )
  }
  /*def valueSet(e: ValueSet, xs: List[Entry]): Fail = {
    val reasons = xs map { x =>
      val location = Location(null, "...", x.getPath, x.getLine, x.getColumn)
      Reason(location, s"Value Set assertion is violated. Details: ${x.getDescription}")
    }
    Fail( Trace(e, reasons) :: Nil )
  }*/

  /**
    * Creates and returns an AND expression failure stack traces
    */
  def and(e: AND, c: Element, f: Fail): Fail = Fail( Trace( e, Nil) :: f.stack )

  /**
   * Creates and returns an FORALL expression failure stack traces
   */
  def forall(e: FORALL, c: Element, f: Fail): Fail = Fail( Trace( e, Nil) :: f.stack )

  /**
   * Creates and returns an EXIST expression failure stack traces
   */
  def exist(e: EXIST, c: Element, f: Fail) : Fail = Fail( Trace( e, Nil) :: f.stack )


  /**
    * Creates and returns an OR expression failure stack traces
    */
  def or(e: OR, c: Element, f1: Fail, f2: Fail): Fail =
    Fail( Trace( e, Nil) :: f1.stack ::: f2.stack )

  /**
    * Creates and returns a NOT expression failure stack traces
    */
  def not(e: NOT, c: Element): Fail =
    Fail( Trace( e, List(Reason(c.location, "The inner expression is true")) ) :: Nil )

  def seqId(e: SetId, c: Element, s: Simple) = {
    val reason = Reason(s.location, s"Expected ${c.instance}, Found ${s.value.raw}")
    Fail( Trace(e, reason :: Nil) :: Nil )
  }
  
  def IZseqId(e: IZSetId, c: Element, s: List[(Simple, Int)]) = {
    val reasons = s.foldLeft(List[Reason]())({ (acc,l) =>
      if(l._2 == -1) Reason(l._1.location, s"Value ${l._1.value.raw} is not a number")::acc
      else Reason(l._1.location, s"Expected ${l._2}, Found ${l._1.value.raw}")::acc
    })
    Fail( Trace(e, reasons) :: Nil )
  }
  
  def narrowLocation(c : Element, path : String) : Element = {
    path.split("\\.").foldLeft(c)( (acc,p) =>
        query(acc,p) match {
          case Success(Nil) => return acc
          case Success(ls) => ls.head
          case Failure(_) => return acc
        }
    )
  }

}
