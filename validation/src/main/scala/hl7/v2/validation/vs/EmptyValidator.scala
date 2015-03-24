/*package hl7.v2.validation.vs

//FIXME: What is an empty value set validator?

import hl7.v2.instance.Message
import hl7.v2.validation.report.VSEntry

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * An empty value set validator.
  */
trait EmptyValidator extends Validator {

  def checkValueSet(m: Message): Future[Seq[VSEntry]] = Future { Nil }
}
*/