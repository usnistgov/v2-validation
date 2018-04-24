package hl7.v2.validation.structure

import hl7.v2.validation.report.ConfigurableDetections
import com.typesafe.config.ConfigFactory

trait Helpers {
      implicit val Detections = new ConfigurableDetections(ConfigFactory.load());
      implicit val vsValidation = new hl7.v2.validation.vs.Validator(Detections)
}