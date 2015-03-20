package hl7.v2.validation.structure

import hl7.v2.instance.{ComplexComponent, ComplexField, Complex}


/*

Preconditions
  1) There is a table spec
  2) The complex element is a coded element

 Checks:

 For each valued binding location (If not valued usage will trigger)
    1) If the value set is not in the library then VSNotFound
    2) If the code is not in the value set then CodeNotFound
    3) If the code is in the value set
        a) If the usage is E then EVS
        b) If the usage is P then PVS
        c) If the code system is different from the value
           at position + 2 then InvalidCodeSystem
 */

object CodedElementValidation {


  /**
    * Returns true if the complex element is a coded element
    */
  private def isCodedElement(c: Complex): Boolean =
    c match {
      case ComplexField (dt, _, _, _, _, _) => isCodedElement(dt.name)
      case ComplexComponent(dt, _, _, _, _) => isCodedElement(dt.name)
      case _                                => false
    }

  private def isCodedElement(dtName: String) = dtName.matches("C(W|N)?E")

}
