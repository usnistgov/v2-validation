package expression

import hl7.v2.instance.Value
import hl7.v2.profile.ValueSetSpec

sealed trait Expression

case class Presence( path: String ) extends Expression

case class PlainText( path: String, text: String, ignoreCase: Boolean, atLeastOnce : Boolean = false) extends Expression

case class Format( path: String, pattern: String, atLeastOnce : Boolean = false) extends Expression

case class NumberList( path: String, csv: List[Double], atLeastOnce : Boolean = false) extends Expression

case class StringList( path: String, csv: List[String], atLeastOnce : Boolean = false) extends Expression

case class SimpleValue( path: String, operator: Operator, value: Value ) extends Expression

case class PathValue( path1: String, operator: Operator, path2: String ) extends Expression

case class isNULL(path: String) extends Expression

// Combination expressions
case class AND( exp1: Expression, exp2: Expression ) extends Expression

case class OR( exp1: Expression, exp2: Expression ) extends Expression

case class NOT( exp: Expression ) extends Expression

//x ⊕ y   =   (x ∨ y) ∧ ¬(x ∧ y)
case class XOR( exp1: Expression, exp2: Expression ) extends Expression

//x → y   =   ¬x ∨ y
case class IMPLY( exp1: Expression, exp2: Expression ) extends Expression

case class FORALL( list: Expression* ) extends Expression

case class EXIST( list: Expression* ) extends Expression

case class Plugin( clazz: String ) extends Expression

case class SetId(path: String) extends Expression

case class IZSetId(parent: String, element : String) extends Expression

case class ValueSet(path: String, spec: ValueSetSpec) extends Expression

case class StringFormat(path: String, format: StringType, atLeastOnce : Boolean = false) extends Expression
