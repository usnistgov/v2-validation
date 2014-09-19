package hl7.v2.validation.content

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import hl7.v2.instance.Message

/**
  * An empty validator. It will return no report entry
  * 
  * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
  */

trait EmptyValidator extends Validator {

  implicit val constraintManager = EmptyConstraintManager

  def checkContent(m: Message): Future[Seq[Entry]] = Future { Nil }
}
