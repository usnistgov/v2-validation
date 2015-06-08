package expression

import expression.EvalResult.{Fail, Reason, Trace}
import hl7.v2.instance.{Element, EscapeSeqHandler, Separators, Simple}

object Failures extends EscapeSeqHandler {

  /**
    * Creates and returns a presence failure stack traces
    */
  def presence(c: Element, e: Presence): Fail = {
    val path    = s"${c.location.path}.${e.path}"
    val reasons = Reason( c.location, s"$path is missing"):: Nil
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
    * Creates and returns an AND expression failure stack traces
    */
  def and(e: AND, c: Element, f: Fail): Fail = Fail( Trace( e, Nil) :: f.stack )

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

}
