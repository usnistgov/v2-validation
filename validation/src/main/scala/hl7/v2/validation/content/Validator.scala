package hl7.v2.validation.content

import gov.nist.validation.report.Entry
import hl7.v2.instance.Message
import hl7.v2.validation.vs.ValueSetLibrary

import scala.concurrent.Future

/**
  * The message content validator
  * 
  * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
  */

trait Validator {

  /**
    * The conformance context used by this validator.
    */
  def conformanceContext: ConformanceContext

  implicit def valueSetLibrary: ValueSetLibrary

  /**
    * Check the message against the constraints defined
    * in the constraint manager and returns the report.
    * @param m - The message to be checked
    * @return The report
    */
  def checkContent(m: Message): Future[Seq[Entry]]
}