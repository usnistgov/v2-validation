package hl7.v2.validation.structure

import scala.concurrent.Future

import hl7.v2.instance.Message
import hl7.v2.validation.report.SEntry
import hl7.v2.validation.vs.{Validator => VSValidator}

/**
  * The message structure validator
  * 
  * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
  */

trait Validator { 
  
  /**
    * The value set validator
    */
  implicit val vsv: VSValidator

  /**
    * Checks the message against the basic constraints (usage,
    * cardinality, length, table, constant value and format )
    * defined in the message profile
    * 
    * @param m - The message to be checked
    * @return  - The list of violations
    */
  def checkStructure(m: Message): Future[Seq[SEntry]]
}
