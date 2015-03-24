package hl7.v2.validation.vs

import hl7.v2.instance.Complex
import hl7.v2.profile.Datatype
import hl7.v2.validation.report.VSEntry


trait CodedElementValidator {

  /**
    * Returns true if the data type is an HL7 coded element
    */
  def isCodedElement(d: Datatype) = d.name.matches("C(W|N)?E")

  /**
    * Checks the coded element and return the result
    */
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
  def checkCodedElement(c: Complex): List[VSEntry] = ???

}
