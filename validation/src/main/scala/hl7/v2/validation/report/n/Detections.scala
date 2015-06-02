package hl7.v2.validation.report.n

import com.typesafe.config.ConfigFactory
import hl7.v2.instance.Location
import gov.nist.validation.report.Entry
import gov.nist.validation.report.impl.EntryImpl

object Detections {

  val conf = ConfigFactory.load

  def rusage(l: Location): Entry = {
    val category       = conf.getString("report.r-usage.category")
    val classification = conf.getString("report.r-usage.classification")
    val template       = conf.getString("report.r-usage.template")
    val description    = String.format(template, l.prettyString)
    new EntryImpl(l.line, l.column, l.path, description, category, classification)
  }

}
