package expression

import hl7.v2.instance.{Element, Simple}

object Failures {

  private def path(c: Element, p: String) = s"${c.location.path}.$p"

  def presenceFailure(e: Presence, c: Element): Fail = {
    val reasons = Reason( c.location, s"${path(c, e.path)} is missing") :: Nil
    Fail( e -> reasons :: Nil )
  }

  def plainTextFailure(e: PlainText, xs: Seq[Simple]): Fail = {
    val cs = if( e.ignoreCase ) "case insensitive" else "case sensitive"
    val reasons = xs.toList map { s =>
      Reason(s.location, s"'${s.value.raw}' is different from '${e.text}' ($cs)") //FIXME escape both values?
    }
    Fail( e -> reasons :: Nil )
  }

  def formatFailure(e: Format, xs: Seq[Simple]): Fail = {
    val reasons = xs.toList map { s =>
      Reason(s.location, s"'${s.value.raw}' doesn't match '${e.pattern}'") //FIXME escape both values?
    }
    Fail( e -> reasons :: Nil )
  }

  def andFailure(e: AND, c: Element, f: Fail): Fail = 
    Fail( e -> List() :: f.stack )

  def orFailure(e: OR, c: Element, f1: Fail, f2: Fail): Fail =
    Fail( e -> List() :: f1.stack ::: f2.stack )

  def notFailure(e: NOT, c: Element): Fail = {
    val reasons = List(Reason(c.location, "The inner expression evaluation returned 'true'"))
    Fail( e -> reasons :: Nil )
  }
}
