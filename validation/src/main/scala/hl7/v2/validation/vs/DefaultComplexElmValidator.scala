package hl7.v2.validation.vs

import hl7.v2.instance.{Complex, ComplexComponent, ComplexField}
import hl7.v2.profile.Datatype
import hl7.v2.validation.report.VSEntry

trait DefaultComplexElmValidator
    extends ComplexElementValidator
    with    CodedElementValidator {

  /**
    * Checks the complex element against the value specifications
    * and returns the list of problems detected.
    */
  def check(e: Complex, library: Map[String, ValueSet]): List[VSEntry] =
    e match {
      case ComplexField (dt, _, _, _, _, _) => check(e, dt)
      case ComplexComponent(dt, _, _, _, _) => check(e, dt)
      case  _                               => Nil
    }

  private def check(c: Complex, d: Datatype): List[VSEntry] =
    if( isCodedElement(d) ) checkCodedElement(c)
    else Nil // No Op
}
