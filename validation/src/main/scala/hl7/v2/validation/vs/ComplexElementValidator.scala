package hl7.v2.validation.vs

import hl7.v2.instance.Complex
import hl7.v2.validation.report.VSEntry

trait ComplexElementValidator {

  /**
    * Checks the complex element against the value specifications
    * and returns the list of problems detected.
    */
  //FIXME: Update the instance model to differentiate between a data element ...
  // and complex element.
  def check(e: Complex, library: Map[String, ValueSet]): List[VSEntry]
}
