package hl7.v2.validation.coconstraints

import gov.nist.validation.report.Entry
import hl7.v2.instance.Message
import hl7.v2.validation.report.ConfigurableDetections

import scala.concurrent.Future

trait CoConstraintValidator {
  val coConstraintValidationContext: CoConstraintValidationContext
  def checkCoConstraint(m: Message)(implicit Detections : ConfigurableDetections): Future[Seq[Entry]]
}
