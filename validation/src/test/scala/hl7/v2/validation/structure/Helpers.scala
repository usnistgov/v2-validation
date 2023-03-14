package hl7.v2.validation.structure

import hl7.v2.validation.report.ConfigurableDetections
import com.typesafe.config.ConfigFactory
import hl7.v2.validation.{FeatureFlags, vs}

trait Helpers {
      implicit val Detections: ConfigurableDetections = new ConfigurableDetections(ConfigFactory.load());
      implicit val vsValidation: vs.Validator = new hl7.v2.validation.vs.Validator(Detections)
      implicit val featureFlags: FeatureFlags = FeatureFlags()
}