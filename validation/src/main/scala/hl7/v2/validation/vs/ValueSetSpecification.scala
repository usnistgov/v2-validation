package hl7.v2.validation.vs

import hl7.v2.instance.{Element, Path, Simple}
import hl7.v2.profile.BindingStrength

import scala.util.{Failure, Success, Try}


trait ValueSetSpecification {
    def vsSpecificationFor(e: Element): Option[List[ValueSetBinding]]
    def singleCodeSpecificationFor(e: Element): Option[List[SingleCodeBinding]]
}

case class BindingLocation(codeLocation : String, codeSystemLocation : Option[String])
object BindingLocationReader {

    def apply(code: String, codeSys: Option[String]): Try[BindingLocation] = Path.isValid(code) match {
        case true => codeSys match {
            case Some(cs) => if(Path.isValid(cs)) Success(BindingLocation(code, Some(cs))) else Failure( new Exception(s"Invalid Binding Location '$cs'") )
            case None => Success(BindingLocation(code, None))
        }
        case false => Failure( new Exception(s"Invalid Binding Location '$code'") )
    }
}


case class BindingMetadata(strength : Option[BindingStrength], stability : Option[Stability], extensibility : Option[Extensibility])

case class ValueSetBinding(target: String, strength: Option[BindingStrength], bindings: List[String], bindingLocations: List[BindingLocation])

case class SingleCodeBinding(target: String, code: String, codeSystem: String, bindingLocations: List[BindingLocation])

case class CodeHolder(code : Simple, codeSystem : Option[Simple], location : BindingLocation)

case class ValueSetValidationPayload(target: Element, values: List[CodeHolder], valueSets: List[ValueSet], strength: Option[BindingStrength], detections: List[VSValidationCode])

case class SingleCodeValidationPayload(target: Element, values: List[CodeHolder], spec: SingleCodeBinding, detections: List[VSValidationCode])