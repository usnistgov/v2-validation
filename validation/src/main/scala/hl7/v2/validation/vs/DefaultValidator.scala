/*package hl7.v2.validation.vs

import hl7.v2.instance.Message
import hl7.v2.validation.report.VSEntry

import scala.concurrent.Future

trait DefaultValidator extends Validator{

  /**
    * Checks the message and returns the list of problems.
    */
  def checkValueSet(m: Message): Future[Seq[VSEntry]] = ???

}
*/