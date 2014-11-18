package expression

import hl7.v2.instance.Value
import scala.util.Try

/**
 * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
 */

sealed trait Operator {
  def eval(v1: Value, v2: Value): Try[Boolean]
}

object Operator {

  case object LT extends Operator {
    def eval( v1: Value, v2: Value ) = v1 compareTo v2 map ( _ < 0 )
    override def toString = "lower than"
  }

  case object GT extends Operator {
    def eval( v1: Value, v2: Value ) = v1 compareTo v2 map ( _ > 0 )
    override def toString = "greater than"
  }

  case object LE extends Operator {
    def eval( v1: Value, v2: Value ) = v1 compareTo v2 map ( _ <= 0 )
    override def toString = "lower or equal to"
  }

  case object GE extends Operator {
    def eval( v1: Value, v2: Value ) = v1 compareTo v2 map ( _ >= 0 )
    override def toString = "greater or equal to"
  }

  case object EQ extends Operator {
    def eval( v1: Value, v2: Value ) = v1 compareTo v2 map ( _ == 0 )
    override def toString = "equal to"
  }

  case object NE extends Operator {
    def eval( v1: Value, v2: Value ) = v1 compareTo v2 map ( _ != 0 )
    override def toString = "different from"
  }
}
