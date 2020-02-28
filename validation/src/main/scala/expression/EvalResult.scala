package expression

import hl7.v2.instance.Location

/**
  * Trait representing the result of an expression evaluation
  */
sealed trait EvalResult 

object EvalResult {

  /**
    * A successful expression evaluation result
    */
  case object Pass extends EvalResult
 

  /**
    * A failed expression evaluation result
    */
  sealed trait Failure extends EvalResult {
    def stack: List[Trace]
  } 
  
  case class Fail( stack: List[Trace]) extends Failure
  case class FailPlugin( stack: List[Trace], message : List[String]) extends Failure
  case class EvalData( result : EvalResult, found : String, expected : String)
  /**
    * An inconclusive expression evaluation result
    *
    * An expression evaluation is inconclusive if an error (exception)
    * occurred during the evaluation. This usually happens when :
    *   1) Path resolution fails or returns a complex element
    *      while a simple element is expected
    *   2) Invalid conversion between 'Value' classes.
    *       Example: attempting to convert Text(X2) to a number
    *   3) An operation is not supported. Example: Date(20140326) LT Time(010100)
    */
  case class Inconclusive( trace: Trace ) extends EvalResult

  /**
    * A trace of a failed or inconclusive expression evaluation
    */
  case class Trace( expression: Expression, reasons: List[Reason] )

  /**
    * The reason of a failed or inconclusive expression evaluation
    */
  case class Reason( location: Location, message: String )

}
