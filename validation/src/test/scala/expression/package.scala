import expression.EvalResult.{Reason, Trace, Inconclusive}
import hl7.v2.instance.Location

package object expression {

  /**
    * Creates an inconclusive result from a message
    */
  def inconclusive(e: Expression, l: Location, m: String): Inconclusive =
    Inconclusive( Trace( e, Reason( l, m) :: Nil ) )

  /**
    * Creates an inconclusive result from a throwable
    */
  def inconclusive(e: Expression, l: Location, t: Throwable): Inconclusive =
    inconclusive( e, l, t.getMessage )

}
