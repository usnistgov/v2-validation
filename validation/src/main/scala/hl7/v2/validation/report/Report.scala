package hl7.v2.validation.report

import java.util.{HashMap => JHMap, List => JList, Map => JMap}

import gov.nist.validation.report.Entry

import scala.jdk.CollectionConverters.SeqHasAsJava
import scala.jdk.CollectionConverters.ListHasAsScala


case class Report(
    structure: Seq[Entry],
    content: Seq[Entry],
    vs: JList[Entry]
  ) extends gov.nist.validation.report.Report {

  override def getEntries: JMap[String, JList[Entry]] = {
    val map = new JHMap[String, JList[Entry]]()
    map.put("structure", structure.asJava)
    map.put("content", content.asJava)
    map.put("value-set", vs)
    map
  }

  override def toJson: String =
    gov.nist.validation.report.impl.JsonObjectMapper.mapper.writeValueAsString(this)

  override def toText: String =
    s"""
      |\n\n########  structure check: ${structure.size} problems detected.\n
      |${ structure map (e => e.toString) mkString "\n" }
      |\n\n########  content check: ${content.size} problems detected.\n
      |${ content map (e => e.toString) mkString "\n" }
      \n\n|########  value set check: ${vs.size} problems detected.\n
      |${ vs.asScala map (e => e.toString) mkString "\n" }
     """.stripMargin

}
