package hl7.v2.validation.structure

import hl7.v2.instance.Message
import hl7.v2.validation.vs.ValueSet

import scala.concurrent.Future
import hl7.v2.validation.report.SEntry

/**
  * The message structure validator
  * 
  * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
  */

trait Validator {

  /**
    * Checks the message structure and returns the list of problems.
    * 
    * @param m - The message to be checked
    * @return  - The list of problems
    */
  def checkStructure(m: Message): Future[Seq[SEntry]]
}
