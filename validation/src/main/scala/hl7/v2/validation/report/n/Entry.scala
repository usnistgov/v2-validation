/*package hl7.v2.validation.report.n

import java.util.{List => JList, Map => JMap}

import com.fasterxml.jackson.core.JsonProcessingException
import gov.nist.validation.report.Trace
import gov.nist.validation.report.impl.Util
import hl7.v2.instance.Location

import Configurations._

trait Entry extends gov.nist.validation.report.Entry {

  val location: Location

  override val getLine: Int    = location.line
  override val getColumn: Int  = location.column
  override val getPath: String = location.path
  override def getStackTrace: JList[Trace]       = null
  override def getMetaData: JMap[String, Object] = null
  @throws[JsonProcessingException]
  override def toJson: String = Util.mapper.writeValueAsString(this)
}

case class RUsage(location: Location) extends Entry {
  val getDescription = String.format(template(this), location.prettyString)
  val getCategory = category(this)
  val getClassification = classification(this)
}

case class XUsage(location: Location) extends Entry {
  val getDescription = String.format(template(this), location.prettyString)
  val getCategory = category(this)
  val getClassification = classification(this)
}

case class WUsage(location: Location) extends Entry {
  val getDescription = String.format(template(this), location.prettyString)
  val getCategory = category(this)
  val getClassification = classification(this)
}

case class REUsage(location: Location) extends Entry {
  val getDescription = String.format(template(this), location.prettyString)
  val getCategory = category(this)
  val getClassification = classification(this)
}

case class Length(location: Location) extends Entry {
  val getDescription = String.format(template(this), location.prettyString)
  val getCategory = category(this)
  val getClassification = classification(this)
}

case class Format(location: Location) extends Entry {
  val getDescription = String.format(template(this), location.prettyString)
  val getCategory = category(this)
  val getClassification = classification(this)
}

case class Extra(location: Location) extends Entry {
  val getDescription = String.format(template(this), location.prettyString)
  val getCategory = category(this)
  val getClassification = classification(this)
}
*/


/*
import java.util.{Collection => JCollection}

import com.fasterxml.jackson.core.JsonProcessingException
import gov.nist.validation.report.impl.Util
import gov.nist.validation.report.{Entry => GEntry, Report => GReport}

import scala.collection.JavaConversions.asJavaCollection

/**
 *
 */
case class Report(entries: List[GEntry]) extends GReport {

  override def getEntriesByCategory(category: String): JCollection[GEntry] =
    entries filter ( e => e.getCategory == category )

  override def getEntriesByClassification(classification: String): JCollection[GEntry] =
    entries filter( e => e.getClassification == classification)

  override def getEntries: JCollection[GEntry] = entries

  @throws[JsonProcessingException]
  override def toJson: String = Util.mapper.writeValueAsString(this)

  //override def toText: String = super.toString = ???
}

*/