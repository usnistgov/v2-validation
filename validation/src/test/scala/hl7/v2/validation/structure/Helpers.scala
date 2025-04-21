package hl7.v2.validation.structure

import hl7.v2.validation.report.ConfigurableDetections
import com.typesafe.config.ConfigFactory
import gov.nist.validation.report.Entry
import gov.nist.validation.report.impl.EntryImpl
import hl7.v2.validation.FeatureFlags

trait Helpers {
      implicit val Detections: ConfigurableDetections = new ConfigurableDetections(ConfigFactory.load());
      implicit val featureFlags: FeatureFlags = FeatureFlags(overrideLowerLevelBindings = false)

      def skipElementProfilePath(entries: Seq[Entry]): Seq[Entry] = {
            entries.map((entry) => {
                  new EntryImpl(
                        entry.getLine,
                        entry.getColumn,
                        entry.getPath,
                        "",
                        "",
                        "",
                        "",
                        entry.getDescription,
                        entry.getCategory,
                        entry.getClassification
                  )
            })
      }
}