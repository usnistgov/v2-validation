import hl7.v2.instance.Element
import hl7.v2.instance.Simple

package object expression {
  def inconclusive(c: Element, e: Expression, m: String) = Inconclusive( c, e, m :: Nil )

  def fail( reasons: Seq[Reason] ) = Fail( reasons )

  def fail( c: Element, m: String ) = Fail( Reason( c.location, m)::Nil )

  def fail( l: Seq[Simple], f : Simple => Reason ) = Fail( l.toList map f )
}