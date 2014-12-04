package expression

import hl7.v2.instance.{Location, Element, Simple}

object Failures {

  def loc(l: Location) = f"[line=${l.line}%03d, column=${l.column}%03d]"

  def NaNErrMsg(s: Simple): String =
    s"${loc(s.location)} '${s.value.raw}' is not a valid number."

  def presenceFailure(e: Presence, c: Element): Fail = {
    val path = s"${c.location.path}.${e.path}"
    val reasons = Reason( c.location, s"$path is missing") :: Nil
    Fail( e -> reasons :: Nil )
  }

  def plainTextFailure(e: PlainText, xs: Seq[Simple]): Fail = {
    val cs = if( e.ignoreCase ) "case insensitive" else "case sensitive"
    val reasons = xs.toList map { s =>
      Reason(s.location, s"'${s.value.raw}' is different from '${e.text}' ($cs)") //FIXME escape values?
    }
    Fail( e -> reasons :: Nil )
  }

  def formatFailure(e: Format, xs: Seq[Simple]): Fail = {
    val reasons = xs.toList map { s =>
      Reason(s.location, s"'${s.value.raw}' doesn't match '${e.pattern}'") //FIXME escape values?
    }
    Fail( e -> reasons :: Nil )
  }

  def stringListFailure(e: StringList, xs: Seq[Simple]): Fail =
    listFailure(e, xs, e.csv)

  def numberListFailure(e: NumberList, xs: Seq[Simple]): Fail =
    listFailure(e, xs, e.csv)

  def simpleValueFailure( sv: SimpleValue, xs: List[Simple] ) = {
    val reasons = xs map { x =>
      Reason(x.location, s"${x.value} is not ${sv.operator} ${sv.value}")
    }
    Fail( sv -> reasons :: Nil )
  }

  def andFailure(e: AND, c: Element, f: Fail): Fail = 
    Fail( e -> List() :: f.stack )

  def orFailure(e: OR, c: Element, f1: Fail, f2: Fail): Fail =
    Fail( e -> List() :: f1.stack ::: f2.stack )

  def notFailure(e: NOT, c: Element): Fail = {
    val reasons = List(Reason(c.location, "The inner expression evaluation returned 'true'"))
    Fail( e -> reasons :: Nil )
  }

  private def listFailure[T](e: Expression, xs: Seq[Simple], l: List[T]): Fail = {
    val ls = l.mkString("{ '", "', '", "' }")
    val reasons = xs.toList map { s =>
      Reason(s.location, s"'${s.value.raw}' is not in the list $ls")
    }
    Fail( e -> reasons :: Nil )
  }

}
