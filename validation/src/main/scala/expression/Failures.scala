package expression

import hl7.v2.instance.{Location, Element, Simple}

object Failures {

  def loc(l: Location) = f"[line=${l.line}%03d, column=${l.column}%03d]"

  def presenceFailure(e: Presence, c: Element): Fail = {
    val path = s"${c.location.path}.${e.path}"
    val reasons = Reason( c.location, s"$path is missing") :: Nil
    Fail( e -> reasons :: Nil )
  }

  def plainTextFailure(e: PlainText, xs: List[Simple]): Fail = {
    val cs = if( e.ignoreCase ) "case insensitive" else "case sensitive"
    val reasons = xs map { s =>
      Reason(s.location, s"'${s.value.raw}' is different from '${e.text}' ($cs)") //FIXME escape values?
    }
    Fail( e -> reasons :: Nil )
  }

  def formatFailure(e: Format, xs: List[Simple]): Fail = {
    val reasons = xs map { s =>
      Reason(s.location, s"'${s.value.raw}' doesn't match '${e.pattern}'") //FIXME escape values?
    }
    Fail( e -> reasons :: Nil )
  }

  def stringListFailure(e: StringList, xs: List[Simple]): Fail =
    listFailure(e, xs, e.csv)

  def numberListNaNFailure(e: NumberList, xs: List[Simple]): Fail = {
    val reasons = xs map { x =>
      Reason(x.location, s"${x.value} cannot be treated as a number")
    }
    Fail( e -> reasons :: Nil )
  }

  def numberListFailure(e: NumberList, xs: List[Simple]): Fail =
    listFailure(e, xs, e.csv)

  def simpleValueFailure( sv: SimpleValue, xs: List[Simple] ) = {
    val reasons = xs map { x =>
      Reason(x.location, s"${x.value} is not ${sv.operator} ${sv.value}")
    }
    Fail( sv -> reasons :: Nil )
  }

  def pathValueFailure( pv: PathValue, s1: Simple, s2: Simple ) = {
    val r = Reason(s1.location, s"${s1.value} is not ${pv.operator} ${s2.value}") :: Nil
    Fail( pv -> r :: Nil )
  }

  def pathValueFailure( pv: PathValue, s: Simple, p: String) = {
    val r = Reason(s.location, s"${s.location.path}(${s.location.desc
                    }) is populated but not $p is missing") :: Nil
    Fail( pv -> r :: Nil )
  }

  def andFailure(e: AND, c: Element, f: Fail): Fail = 
    Fail( e -> List() :: f.stack )

  def orFailure(e: OR, c: Element, f1: Fail, f2: Fail): Fail =
    Fail( e -> List() :: f1.stack ::: f2.stack )

  def notFailure(e: NOT, c: Element): Fail = {
    val reasons = List(Reason(c.location, "The inner expression evaluation returned 'true'"))
    Fail( e -> reasons :: Nil )
  }

  private def listFailure[T](e: Expression, xs: List[Simple], l: List[T]): Fail = {
    val ls = l.mkString("{ '", "', '", "' }")
    val reasons = xs map { s =>
      Reason(s.location, s"'${s.value.raw}' is not in the list $ls")
    }
    Fail( e -> reasons :: Nil )
  }

}
