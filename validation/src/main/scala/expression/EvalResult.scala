package expression

import hl7.v2.instance.Element
import hl7.v2.instance.Location

/**
  * Trait representing the result of an expression evaluation
  */
sealed trait EvalResult

/**
  * A successful expression evaluation result
  */
case object Pass extends EvalResult

/**
  * A failed expression evaluation result
  */
//case class Fail( reasons: List[Reason] ) extends EvalResult
case class Fail( stack: List[(Expression, List[Reason])] ) extends EvalResult

/**
  * An inconclusive expression evaluation result
  * 
  * An expression evaluation is usually inconclusive when :
  *   1) Path resolution fails or returns a complex element while a simple element is expected
  *   2) Invalid conversion between `Value' classes. Example: attempting to convert Text(X2) to a number
  *   3) An operation is not supported. Example: Date(20140326) LT Time(010100)
  */
case class Inconclusive(expression: Expression, details: List[String]) extends EvalResult

/**
  * The reason of an expression evaluation failure
  */
case class Reason(location: Location, msg: String)

/**
  * Evaluation Errors
  */
case class EvalError(messages: List[String]) extends Throwable {
  override def toString = s"EvalError: \n\t${messages.mkString("\n\t")}"
}
