package hl7.v2.validation.content

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import hl7.v2.instance.Message
import hl7.v2.validation.report.CEntry

/**
  * An empty content validator.
  * 
  * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
  */

trait EmptyValidator extends Validator {

  val constraintManager = EmptyConstraintManager

  /**
    * Returns an empty list independently from the message.
    * @param m - The message to be checked
    * @return An empty list
    */
  def checkContent(m: Message): Future[Seq[CEntry]] = Future { Nil }
}
