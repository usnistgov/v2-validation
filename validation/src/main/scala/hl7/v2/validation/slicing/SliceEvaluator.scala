package hl7.v2.validation.slicing

import gov.nist.validation.report.Entry
import hl7.v2.instance.Message
import hl7.v2.validation.report.ConfigurableDetections
import hl7.v2.validation.vs.ValueSetLibrary

import scala.concurrent.Future

trait SliceEvaluator {

  def profileSlicingContext: ProfileSlicingContext
  implicit def valueSetLibrary: ValueSetLibrary

  def applySlices(m: Message)(implicit Detections : ConfigurableDetections, VSValidator : hl7.v2.validation.vs.Validator): Future[(Message, Seq[Entry])]

}
