package hl7.v2.validation.vs

import hl7.v2.instance._
import scala.concurrent.Future
import hl7.v2.validation.report.ConfigurableDetections
import gov.nist.validation.report.Entry


trait ValueSetValidator {

  implicit def valueSetLibrary: ValueSetLibrary
  def vsSpecification : ValueSetSpecification;
  /**
    * Checks the message structure and returns the list of problems.
    * 
    * @param m - The message to be checked
    * @return  - The list of problems
    */
  def checkValueSets(m: Message)(implicit Detections : ConfigurableDetections): Future[Seq[Entry]]
}
