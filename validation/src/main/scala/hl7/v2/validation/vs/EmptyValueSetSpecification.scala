package hl7.v2.validation.vs
import hl7.v2.instance.Element

object EmptyValueSetSpecification extends ValueSetSpecification {
  override def vsSpecificationFor(e: Element): Option[List[ValueSetBinding]] = None
  override def singleCodeSpecificationFor(e: Element): Option[List[SingleCodeBinding]] = None
}
