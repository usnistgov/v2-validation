package hl7.v2.validation.content

import gov.nist.validation.report.Entry
import hl7.v2.instance.Message

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * An empty content validator.
  * 
  * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
  */

trait EmptyValidator extends Validator {

  /**
    * The conformance context used by this validator.
    */
  val conformanceContext = EmptyConformanceContext

  /**
    * Returns an empty list independently from the message.
    * @param m - The message to be checked
    * @return An empty list
    */
  def checkContent(m: Message): Future[Seq[Entry]] = Future { Nil }
}
