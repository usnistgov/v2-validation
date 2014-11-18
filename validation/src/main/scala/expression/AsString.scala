package expression

import hl7.v2.instance.Element

object AsString {

  def path(c: Element, p: String) = s"${c.location.path}.$p"

  def expression(e: Expression, context: Element): String = e match {
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
    case e: Plugin      => ???
  }

  def presence(e: Presence, c: Element) = s"${ path(c, e.path) } SHALL be present"

  def plainText(e: PlainText, c: Element) = 
      s"${ path(c, e.path) } SHALL be equal to '${e.text}' ${ if(e.ignoreCase) "(case insensitive)" }"

  def format(e: Format, c: Element) = s"${ path(c, e.path) } SHALL match '${e.pattern}'"

  def numberList(e: NumberList, c: Element) = 
      s"${ path(c, e.path) } SHALL be one of '${e.csv.mkString("[", ", ", "]")}'" 

  def stringList(e: StringList, c: Element) = 
      s"${ path(c, e.path) } SHALL be one of '${e.csv.mkString("[", ", ", "]")}'"

  def simpleValue(e: SimpleValue, c: Element) =
    s"${ path(c, e.path) } SHALL be ${e.operator} '${e.value}'"

  def pathValue(e: PathValue, c: Element) =
    s"${ path(c, e.path1) } SHALL be ${e.operator} ${ path(c, e.path2) }"

  def and(e: AND, c: Element) =
    s"( ${ expression(e.exp1, c) } and ${ expression(e.exp2, c) } )"

  def or(e: OR, c: Element) =
    s"( ${ expression(e.exp1, c) } or ${ expression(e.exp2, c) } )"

  def not(e: NOT, c: Element) = s"not ( ${ expression(e.exp, c) } )"

  def xor(e: XOR, c: Element) =
    s"either ${ expression(e.exp1, c) } or ${ expression(e.exp2, c) } but not both"

  def imply(e: IMPLY, c: Element) =
    s"if ${ expression(e.exp1, c) } then ${ expression(e.exp2, c) }"

  def exist(e: EXIST, c: Element) = ??? //FIXME //s"one of ${ e.list map { ee => expression(ee, c) }   } must be true)"

  def forall(e: FORALL, c: Element) = ??? //FIXME

  def plugin(e: Plugin, c: Element) = s"Plugin '${e.id}'"
}