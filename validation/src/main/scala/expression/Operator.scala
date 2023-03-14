package expression
import hl7.v2.instance.{TimeZone, Value}
import hl7.v2.instance.util.ValueComparator.compareTo

import scala.util.Try

/**
  * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
  * @author Hossam Tamri <hossam.tamri@gmail.com>
  */

case class ComparisonMode(identical: Boolean, truncated: Boolean)

sealed trait Operator {
  def eval(v1: Value, v2: Value, comparisonMode: ComparisonMode = ComparisonMode(false, false))(implicit dtz: Option[TimeZone]): Try[Boolean]
}

object Operator {

  def equality(compareValue: Int, v1: Value, v2: Value, identical: Boolean): Boolean = {
    compareValue == 0 && ((identical && v1.raw.equals(v2.raw)) || !identical)
  }

  case object LT extends Operator {
    def eval(v1: Value, v2: Value, comparisonMode: ComparisonMode = ComparisonMode(false, false))(implicit dtz: Option[TimeZone]) =
      compareTo(v1, v2, comparisonMode.truncated) map ( _ < 0 )
    override def toString = "lower than"
  }

  case object GT extends Operator {
    def eval(v1: Value, v2: Value, comparisonMode: ComparisonMode = ComparisonMode(false, false))(implicit dtz: Option[TimeZone]) =
      compareTo(v1, v2, comparisonMode.truncated) map ( _ > 0 )
    override def toString = "greater than"
  }

  case object LE extends Operator {
    def eval(v1: Value, v2: Value, comparisonMode: ComparisonMode = ComparisonMode(false, false))(implicit dtz: Option[TimeZone]) =
      compareTo(v1, v2, comparisonMode.truncated) map (i => i < 0 || equality(i, v1, v2, comparisonMode.identical))
    override def toString = "lower or equal to"
  }

  case object GE extends Operator {
    def eval(v1: Value, v2: Value, comparisonMode: ComparisonMode = ComparisonMode(false, false))(implicit dtz: Option[TimeZone]) =
      compareTo(v1, v2, comparisonMode.truncated) map (i => i > 0 || equality(i, v1, v2, comparisonMode.identical))
    override def toString = "greater or equal to"
  }

  case object EQ extends Operator {
    def eval(v1: Value, v2: Value, comparisonMode: ComparisonMode = ComparisonMode(false, false))(implicit dtz: Option[TimeZone]) =
      compareTo(v1, v2, comparisonMode.truncated) map (equality(_, v1, v2, comparisonMode.identical))
    override def toString = "equal to"
  }

  case object NE extends Operator {
    def eval(v1: Value, v2: Value, comparisonMode: ComparisonMode = ComparisonMode(false, false))(implicit dtz: Option[TimeZone]) =
      compareTo(v1, v2, comparisonMode.truncated) map (!equality(_, v1, v2, comparisonMode.identical))
    override def toString = "different from"
  }
}
