package hl7.v2.validation.vs

import gov.nist.validation.report.Entry
import hl7.v2.instance.Message

import scala.concurrent.Future

/**
  * Trait defining the value set validator
  * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
  */

trait Validator {

  /**
    * The value set library used by this validator
    */
  def valueSetLibrary: ValueSetLibrary

  /**
    * Checks the message and returns the list of problems.
    */
  def checkValueSet(m: Message): Future[Seq[Entry]]

}
