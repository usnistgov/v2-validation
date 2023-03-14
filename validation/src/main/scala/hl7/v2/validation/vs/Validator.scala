package hl7.v2.validation.vs

import hl7.v2.instance._

import scala.concurrent.Future
import hl7.v2.validation.report.ConfigurableDetections
import gov.nist.validation.report.Entry
import hl7.v2.profile.BindingStrength
import hl7.v2.validation.FeatureFlags


trait VsValidator {

  implicit def valueSetLibrary: ValueSetLibrary

  def vsSpecification: ValueSetSpecification;

  /**
    * Checks the message structure and returns the list of problems.
    *
    * @param m - The message to be checked
    * @return - The list of problems
    */
  def checkValueSets(m: Message, additionalVsBindings: Option[ValueSetSpecification] = None)
                    (implicit Detections: ConfigurableDetections, featureFlags: FeatureFlags): Future[Seq[Entry]]

  def check(elements: List[Element], spec: ValueSetBinding)
           (implicit Detections: ConfigurableDetections, featureFlags: FeatureFlags): List[VsEntry]

  def check(elements: List[Element], spec: SingleCodeBinding)
           (implicit Detections: ConfigurableDetections, featureFlags: FeatureFlags): List[VsEntry]

  def createEntry(element: Element, issue: VSValidationCode, bindingStrength: Option[BindingStrength])
                 (implicit Detections: ConfigurableDetections): Entry

}
