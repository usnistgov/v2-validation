package hl7.v2.validation.vs

import hl7.v2.instance.Simple
import hl7.v2.validation.report.VSEntry

trait SimpleElementValidator {

  /**
    * Checks the simple element against the value specifications
    * and returns the list of problems detected.
    */
  def check(s: Simple, library: Map[String, ValueSet]): List[VSEntry]
}
