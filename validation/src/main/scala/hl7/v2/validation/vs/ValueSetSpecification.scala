package hl7.v2.validation.vs

import hl7.v2.instance.Element
import hl7.v2.profile.BindingStrength
import hl7.v2.instance.Simple


trait ValueSetSpecification {
    def vsSpecificationFor(e: Element): Option[List[ValueSetBinding]]    
}
case class BindingLocation(codeLocation : String, codeSystemLocation : Option[String])

case class BindingMetadata(strength : Option[BindingStrength], stability : Option[Stability], extensibility : Option[Extensibility])

case class ValueSetBinding(
  target : String,
  strength : BindingStrength,
  bindings : List[String],
  bindingLocations : List[BindingLocation],
  uniqCodeSys : Boolean
)

case class CodeHolder(code : Simple, codeSystem : Option[Simple], location : BindingLocation)
case class ValueSetValidationPayload(
  target : Element,
  values : List[CodeHolder],
  valueSets : List[ValueSet],
  xor : Boolean,
  strength : BindingStrength
)