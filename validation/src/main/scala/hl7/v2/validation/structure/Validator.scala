package hl7.v2.validation.structure

import hl7.v2.instance.Message

import scala.concurrent.Future
import hl7.v2.validation.report.SEntry

/**
  * The message structure validator
  * 
  * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
  */

trait Validator { 

  /**
    * Checks the message against the basic constraints (usage,
    * cardinality and length ) defined in the message profile
    * 
    * @param m - The message to be checked
    * @return  - The list of violations
    */
  def checkStructure(m: Message): Future[Seq[SEntry]]
}
