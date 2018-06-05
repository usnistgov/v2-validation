package hl7.v2.validation.vs

import hl7.v2.instance.Element
import hl7.v2.profile.BindingStrength
import hl7.v2.profile.BindingLocation


trait ValueSetSpecification {
  
    def vsSpecificationFor(e: Element): List[ValueSetBinding]
    
}

trait ValueSetBinding {
    def target : String;
    def strength : BindingStrength;
    def bindings : List[String];
}

case class SimpleValueSetBinding(
  target : String,
  strength : BindingStrength,
  bindings : List[String]
) extends ValueSetBinding;

case class ComplexValueSetBinding(
  target : String,
  strength : BindingStrength,
  bindings : List[String],
  locations : List[BindingLocation]
) extends ValueSetBinding;