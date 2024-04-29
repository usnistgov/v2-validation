package hl7.v2.validation.slicing

import gov.nist.validation.report.Entry
import hl7.v2.instance.Message
import hl7.v2.validation.report.ConfigurableDetections
import scala.concurrent.Future

trait SliceEvaluator {

  def profileSlicingContext: ProfileSlicingContext
  def applySlices(m: Message)(implicit Detections : ConfigurableDetections): Future[(Message, Seq[Entry])]

}
