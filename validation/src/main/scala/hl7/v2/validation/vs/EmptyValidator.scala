package hl7.v2.validation.vs

import gov.nist.validation.report.Entry
import hl7.v2.instance.Message

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait EmptyValidator extends Validator {

  /**
    * Checks the message and returns the list of problems.
    */
  override def checkValueSet(m: Message) = Future { List[Entry]() }

}
