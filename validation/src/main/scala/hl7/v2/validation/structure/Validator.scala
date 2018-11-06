package hl7.v2.validation.structure

import gov.nist.validation.report.Entry
import hl7.v2.instance.Message

import scala.concurrent.Future
import hl7.v2.validation.report.ConfigurableDetections

/**
  * The message structure validator
  * 
  * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
  */

trait Validator {

  /**
    * Checks the message structure and returns the list of problems.
    * 
    * @param m - The message to be checked
    * @return  - The list of problems
    */
  def checkStructure(m: Message)(implicit Detections : ConfigurableDetections): Future[Seq[Entry]]
}
