package hl7.v2.validation.vs

import hl7.v2.instance.Element
import hl7.v2.instance.Component
import hl7.v2.instance.Field
import hl7.v2.instance.Segment
import hl7.v2.instance.Group

class DefaultValueSetSpecification(
  val specifications : Map[String, List[ValueSetBinding]]    
) extends ValueSetSpecification {
  
  def vsSpecificationFor(e: Element) = e match {
    case c: Component => specifications.get(c.datatype.id)
    case f: Field => specifications.get(f.datatype.id)
    case s: Segment => specifications.get(s.model.ref.id)
    case g: Group => specifications.get(g.model.id)
  }

}