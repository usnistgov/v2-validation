package hl7.v2.validation.content

import scala.concurrent.Future

import hl7.v2.instance.Message
import hl7.v2.validation.report.CEntry

/**
  * The message content validator
  * 
  * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
  */

trait Validator {

  /**
    * The constraint manager used by this validator.
    */
  val constraintManager: ConstraintManager

  /**
    * Check the message against the constraints defined
    * in the constraint manager and returns the report.
    * @param m - The message to be checked
    * @return The report
    */
  def checkContent(m: Message): Future[Seq[CEntry]]
}