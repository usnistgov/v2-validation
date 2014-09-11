import hl7.v2.instance.{Element, Simple}

package object expression {
  def inconclusive(c: Element, e: Expression, m: String) = Inconclusive( c, e, m :: Nil )
}