package expression

import hl7.v2.instance.Element

object AsString {

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
    case e: Plugin      => plugin(e, context)
    case e: SetId       => setId(e, context)
    case e: IZSetId       => IZsetId(e, context)
    case e: ValueSet    => valueSet(e, context)
    case e: isNULL      => isNull(e, context)
  }

  private def path(c: Element, p: String) = s"${c.location.path}.$p"

  private def presence(e: Presence, c: Element) =
    s"${ path(c, e.path) } SHALL be present"

  private def isNull(e: isNULL, c: Element) =
    s"${ path(c, e.path) } SHALL be NULL"
  
  private def plainText(e: PlainText, c: Element) = {
    val cs = if (e.ignoreCase) "(case insensitive)"
    val at = if(e.atLeastOnce) "At least one element from "
    s"$at${path(c, e.path)} SHALL be equal to '${e.text}' $cs"
  }

  private def format(e: Format, c: Element) = {
    val at = if(e.atLeastOnce) "At least one element from " 
    s"$at${ path(c, e.path) } SHALL match '${e.pattern}' regular expression"  
  }
  private def numberList(e: NumberList, c: Element) = {
    val at = if(e.atLeastOnce) "At least one element from " 
    s"$at${ path(c, e.path) } SHALL be one of ${e.csv.mkString("{", ", ", "}")}"
  }

  private def stringList(e: StringList, c: Element) = {
    val at = if(e.atLeastOnce) "At least one element from " 
    s"$at${ path(c, e.path) } SHALL be one of ${e.csv.mkString("{", ", ", "}")}"
  }
    

  private def simpleValue(e: SimpleValue, c: Element) =
    s"${ path(c, e.path) } SHALL be ${e.operator} '${e.value}'"

  private def pathValue(e: PathValue, c: Element) =
    s"${ path(c, e.path1) } SHALL be ${e.operator} ${ path(c, e.path2) }"

  private def and(e: AND, c: Element) =
    s"${ expression(e.exp1, c) } AND ${ expression(e.exp2, c) }"

  private def or(e: OR, c: Element) =
    s"${ expression(e.exp1, c) } OR ${ expression(e.exp2, c) }"

  private def not(e: NOT, c: Element) = negate(e.exp, c)

  private def xor(e: XOR, c: Element) =
    s"Either ${ expression(e.exp1, c) } OR ${ expression(e.exp2, c) } BUT NOT BOTH"

  private def imply(e: IMPLY, c: Element) =
    s"If ${ expression(e.exp1, c) } Then ${ expression(e.exp2, c) }"

  private def exist(e: EXIST, c: Element) = e.list.map( expression(_, c) ).mkString(" OR ")

  private def forall(e: FORALL, c: Element) = e.list.map( expression(_, c) ).mkString(" AND ")

  private def plugin(e: Plugin, c: Element) = s"$e'"

  private def setId(e: SetId, c: Element) = s"$e # Context: ${c.location.prettyString}"
  
  private def IZsetId(e: IZSetId, c: Element) = s"$e # Context: ${c.location.prettyString}"

  private def valueSet(e: ValueSet, c: Element) =
    s"${ path(c, e.path) } SHALL be valued from the value set ${e.spec.valueSetId
    } (Binding Strength = ${e.spec.bindingStrength}, Binding Location = ${e.spec.bindingLocation})"

  // De Morgan's laws
  // not (A and B) === (not A) or (not B)
  // not (A or B) === (not A) and (not B)
  private def negate(e: Expression, c: Element): String = e match {
    case x: AND => s"${expression(NOT(x.exp1), c)} OR ${expression(NOT(x.exp2), c)}"
    case x: OR  => s"${expression(NOT(x.exp1), c)} AND ${expression(NOT(x.exp2), c)}"
    case x: NOT => expression(e, c)
    case x: XOR    => throw new Exception("Invalid use of NOT expression")
    case x: Plugin => s"NOT ${expression(x, c)}"
    case x: IMPLY  => s"${expression(x.exp1, c)} AND ${expression(NOT(x.exp2), c)}"
    case x: EXIST  => x.list.map( toNOT(_, c) ).mkString(" AND ")
    case x: FORALL => x.list.map( toNOT(_, c) ).mkString(" OR ")
    case x: SetId  => s"NOT ${expression(x, c)}"
    case x  => expression(x, c).replaceAllLiterally("SHALL", "SHALL not")
  }
  
  private def toNOT(e: Expression, c: Element) = {
    expression(NOT(e),c)
  }
}
