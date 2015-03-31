package hl7.v2.validation.vs

import hl7.v2.instance.Message
import hl7.v2.validation.report.VSEntry

import scala.concurrent.Future

/**
  * Trait defining the value set validator
  * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
  */

trait Validator {

  /**
    * The value set library used by this validator
    */
  def valueSetLibrary: Map[String, ValueSet]

  /**
    * Checks the message and returns the list of problems.
    */
  def checkValueSet(m: Message): Future[Seq[VSEntry]]

}
