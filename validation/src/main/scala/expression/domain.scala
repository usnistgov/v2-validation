package expression

import hl7.v2.instance.Value
import hl7.v2.profile.ValueSetSpec
import hl7.v2.profile.Range

sealed trait Expression

case class Presence( path: String ) extends Expression

case class PlainText( path: String, text: String, ignoreCase: Boolean, atLeastOnce : Boolean = false, notPresentBehavior : String = "PASS", range: Option[Range] = None) extends Expression

case class Format( path: String, pattern: String, atLeastOnce : Boolean = false, notPresentBehavior : String = "PASS", range: Option[Range] = None) extends Expression

case class NumberList( path: String, csv: List[Double], atLeastOnce : Boolean = false, notPresentBehavior : String = "PASS", range: Option[Range] = None) extends Expression

case class StringList( path: String, csv: List[String], atLeastOnce : Boolean = false, notPresentBehavior : String = "PASS", range: Option[Range] = None) extends Expression

case class SimpleValue( path: String, operator: Operator, value: Value, comparisonMode: ComparisonMode = ComparisonMode(false, false), atLeastOnce : Boolean = false, notPresentBehavior : String = "PASS", range: Option[Range] = None) extends Expression

case class PathValue( path1: String, operator: Operator, path2: String, comparisonMode: ComparisonMode = ComparisonMode(false, false), notPresentBehavior : String = "PASS", path1Mode: MultiCompareMode = MultiCompareMode.All(), path2Mode: MultiCompareMode = MultiCompareMode.All()) extends Expression

case class ComplexPathValue( path1: String, operator: Operator, path2: String, strict: Boolean = true, comparisonMode: ComparisonMode = ComparisonMode(false, false), notPresentBehavior : String = "PASS" ) extends Expression

case class isNULL(path: String) extends Expression

// Combination expressions
case class SubContext(assertion: Expression, path: String, cardinality: Option[Range], atLeastOnce : Option[Boolean], notPresentBehavior: String = "PASS") extends Expression

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

case class ValueSet(path: String, spec: ValueSetSpec, notPresentBehavior : String = "PASS") extends Expression

case class StringFormat(path: String, format: String, atLeastOnce : Boolean = false, notPresentBehavior : String = "PASS", range: Option[Range] = None) extends Expression

// Helper
sealed trait MultiCompareMode
object MultiCompareMode {
  case class AtLeastOne() extends MultiCompareMode
  case class All() extends MultiCompareMode
  case class Count(n: Int) extends MultiCompareMode

  def toString(mcm: MultiCompareMode): String = {
    mcm match {
      case MultiCompareMode.AtLeastOne() => "At Least One"
      case MultiCompareMode.All() => "All"
      case MultiCompareMode.Count(n) => s"${n}"
    }
  }
}