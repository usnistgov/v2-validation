package expression

import hl7.v2.instance.Element

object AsString {

  def expression(e: Expression, context: Element): String = e match {
    case e: Presence => presence(e, context)
    case e: PlainText => plainText(e, context)
    case e: Format => format(e, context)
    case e: NumberList => numberList(e, context)
    case e: StringList => stringList(e, context)
    case e: SimpleValue => simpleValue(e, context)
    case e: PathValue => pathValue(e, context)
    case e: AND => and(e, context)
    case e: OR => or(e, context)
    case e: NOT => not(e, context)
    case e: XOR => xor(e, context)
    case e: IMPLY => imply(e, context)
    case e: EXIST => exist(e, context)
    case e: FORALL => forall(e, context)
    case e: Plugin => plugin(e, context)
    case e: SetId => setId(e, context)
    case e: IZSetId => IZsetId(e, context)
    case e: ValueSet => valueSet(e, context)
    case e: isNULL => isNull(e, context)
    case e: StringFormat => stringFormat(e, context)
  }

  def condition(e: Expression, context: Element): String = e match {
    case e: Presence => presenceC(e, context)
    case e: PlainText => plainTextC(e, context)
    case e: Format => formatC(e, context)
    case e: NumberList => numberListC(e, context)
    case e: StringList => stringListC(e, context)
    case e: SimpleValue => simpleValueC(e, context)
    case e: PathValue => pathValueC(e, context)
    case e: AND => andC(e, context)
    case e: OR => orC(e, context)
    case e: NOT => notC(e, context)
    case e: XOR => xorC(e, context)
    case e: IMPLY => implyC(e, context)
    case e: EXIST => existC(e, context)
    case e: FORALL => forallC(e, context)
    case e: Plugin => plugin(e, context)
    case e: SetId => setId(e, context)
    case e: IZSetId => IZsetId(e, context)
    case e: ValueSet => valueSetC(e, context)
    case e: isNULL => isNullC(e, context)
    case e: StringFormat => stringFormatC(e, context)
  }

  private def path(c: Element, p: String) = s"${c.location.path}.$p"

  private def presence(e: Presence, c: Element) =
    s"${path(c, e.path)} SHALL be present"

  private def presenceC(e: Presence, c: Element) =
    s"${path(c, e.path)} is present"

  private def isNull(e: isNULL, c: Element) =
    s"${path(c, e.path)} SHALL be NULL"

  private def isNullC(e: isNULL, c: Element) =
    s"${path(c, e.path)} is NULL"

  private def plainText(e: PlainText, c: Element) = {
    val cs = if (e.ignoreCase) "(case insensitive)" else ""
    val at = if (e.atLeastOnce) "At least one element from " else ""
    s"$at${path(c, e.path)} SHALL be equal to '${e.text}' $cs"
  }

  private def plainTextC(e: PlainText, c: Element) = {
    val cs = if (e.ignoreCase) "(case insensitive)" else ""
    val at = if (e.atLeastOnce) "At least one element from " else ""
    s"$at${path(c, e.path)} is equal to '${e.text}' $cs"
  }

  private def format(e: Format, c: Element) = {
    val at = if (e.atLeastOnce) "At least one element from "
    s"$at${path(c, e.path)} SHALL match '${e.pattern}' regular expression"
  }

  private def formatC(e: Format, c: Element) = {
    val at = if (e.atLeastOnce) "At least one element from "
    s"$at${path(c, e.path)} matches '${e.pattern}' regular expression"
  }

  private def numberList(e: NumberList, c: Element) = {
    val at = if (e.atLeastOnce) "At least one element from "
    s"$at${path(c, e.path)} SHALL be one of ${e.csv.mkString("{", ", ", "}")}"
  }

  private def numberListC(e: NumberList, c: Element) = {
    val at = if (e.atLeastOnce) "At least one element from "
    s"$at${path(c, e.path)} is one of ${e.csv.mkString("{", ", ", "}")}"
  }

  private def stringList(e: StringList, c: Element) = {
    val at = if (e.atLeastOnce) "At least one element from "
    s"$at${path(c, e.path)} SHALL be one of ${e.csv.mkString("{", ", ", "}")}"
  }

  private def stringListC(e: StringList, c: Element) = {
    val at = if (e.atLeastOnce) "At least one element from "
    s"$at${path(c, e.path)} is one of ${e.csv.mkString("{", ", ", "}")}"
  }

  private def simpleValue(e: SimpleValue, c: Element) =
    s"${path(c, e.path)} SHALL be ${e.operator} '${e.value}'"

  private def simpleValueC(e: SimpleValue, c: Element) =
    s"${path(c, e.path)} is ${e.operator} '${e.value}'"

  private def pathValue(e: PathValue, c: Element) =
    s"${path(c, e.path1)} SHALL be ${e.operator} ${path(c, e.path2)}"

  private def pathValueC(e: PathValue, c: Element) =
    s"${path(c, e.path1)} is ${e.operator} ${path(c, e.path2)}"
    
  private def stringFormat(e: StringFormat, c: Element) = {
    val at = if (e.atLeastOnce) "At least one element from "
    s"$at${path(c, e.path)} SHALL match ${e.format} format"
  }
  
  private def stringFormatC(e: StringFormat, c: Element) = {
     val at = if (e.atLeastOnce) "At least one element from "
    s"$at${path(c, e.path)} matches ${e.format}"
  }
  
  private def and(e: AND, c: Element) =
    s"${expression(e.exp1, c)} AND ${expression(e.exp2, c)}"

  private def andC(e: AND, c: Element) =
    s"${condition(e.exp1, c)} AND ${condition(e.exp2, c)}"

  private def or(e: OR, c: Element) =
    s"${expression(e.exp1, c)} OR ${expression(e.exp2, c)}"
  private def orC(e: OR, c: Element) =
    s"${condition(e.exp1, c)} OR ${condition(e.exp2, c)}"

  private def not(e: NOT, c: Element) = negate(e.exp, c)

  private def notC(e: NOT, c: Element) = negateC(e.exp, c)

  private def xor(e: XOR, c: Element) =
    s"Either ${expression(e.exp1, c)} OR ${expression(e.exp2, c)} BUT NOT BOTH"
  private def xorC(e: XOR, c: Element) =
    s"Either ${condition(e.exp1, c)} OR ${condition(e.exp2, c)} BUT NOT BOTH"

  private def imply(e: IMPLY, c: Element) =
    s"If ${expression(e.exp1, c)} Then ${expression(e.exp2, c)}"
  private def implyC(e: IMPLY, c: Element) =
    s"If ${condition(e.exp1, c)} Then ${condition(e.exp2, c)}"

  private def exist(e: EXIST, c: Element) = e.list.map(expression(_, c)).mkString(" OR ")
  private def existC(e: EXIST, c: Element) = e.list.map(condition(_, c)).mkString(" OR ")

  private def forall(e: FORALL, c: Element) = e.list.map(expression(_, c)).mkString(" AND ")
  private def forallC(e: FORALL, c: Element) = e.list.map(condition(_, c)).mkString(" AND ")

  private def plugin(e: Plugin, c: Element) = s"$e'"

  private def setId(e: SetId, c: Element) = s"$e # Context: ${c.location.prettyString}"

  private def IZsetId(e: IZSetId, c: Element) = s"$e # Context: ${c.location.prettyString}"

  private def valueSet(e: ValueSet, c: Element) =
    s"${path(c, e.path)} SHALL be valued from the value set ${
      e.spec.valueSetId
    } (Binding Strength = ${e.spec.bindingStrength.get}, Binding Location = ${e.spec.bindingLocation.get})"

  private def valueSetC(e: ValueSet, c: Element) =
    s"${path(c, e.path)} is valued from the value set ${
      e.spec.valueSetId
    } (Binding Strength = ${e.spec.bindingStrength.get}, Binding Location = ${e.spec.bindingLocation.get})"

  // De Morgan's laws
  // not (A and B) === (not A) or (not B)
  // not (A or B) === (not A) and (not B)
  private def negate(e: Expression, c: Element): String = e match {
    case x: AND => s"${expression(NOT(x.exp1), c)} OR ${expression(NOT(x.exp2), c)}"
    case x: OR => s"${expression(NOT(x.exp1), c)} AND ${expression(NOT(x.exp2), c)}"
    case x: NOT => expression(e, c)
    case x: XOR => throw new Exception("Invalid use of NOT expression")
    case x: Plugin => s"NOT ${expression(x, c)}"
    case x: IMPLY => s"${expression(x.exp1, c)} AND ${expression(NOT(x.exp2), c)}"
    case x: EXIST => x.list.map(toNOT(_, c)).mkString(" AND ")
    case x: FORALL => x.list.map(toNOT(_, c)).mkString(" OR ")
    case x: SetId => s"NOT ${expression(x, c)}"
    case x => expression(x, c).replace("SHALL", "SHALL not")
  }

  private def negateC(e: Expression, c: Element): String = e match {
    case x: AND => s"${condition(NOT(x.exp1), c)} OR ${condition(NOT(x.exp2), c)}"
    case x: OR => s"${condition(NOT(x.exp1), c)} AND ${condition(NOT(x.exp2), c)}"
    case x: NOT => condition(e, c)
    case x: XOR => throw new Exception("Invalid use of NOT expression")
    case x: Plugin => s"NOT ${condition(x, c)}"
    case x: IMPLY => s"${condition(x.exp1, c)} AND ${condition(NOT(x.exp2), c)}"
    case x: EXIST => x.list.map(toNOTC(_, c)).mkString(" AND ")
    case x: FORALL => x.list.map(toNOTC(_, c)).mkString(" OR ")
    case x: SetId => s"NOT ${condition(x, c)}"
    case x => condition(x, c).replace("is", "is not")
  }

  private def toNOT(e: Expression, c: Element) = {
    expression(NOT(e), c)
  }

  private def toNOTC(e: Expression, c: Element) = {
    condition(NOT(e), c)
  }
}