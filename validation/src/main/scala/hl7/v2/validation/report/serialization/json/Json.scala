package hl7.v2.validation.report.serialization.json

import com.typesafe.config.ConfigFactory
import hl7.v2.validation.report._

object Json {

  val conf = ConfigFactory.load

  def toJson(x: RUsage): String = {
    val d = s"The required ${x.location.eType} ${x.location.path} (${x.location.desc}) is missing"
    val cat = conf.getString("r-usage.category")
    val cla = conf.getString("r-usage.classification")
    template(x.location.path, d, x.location.line, x.location.column, "", cat, cla)
  }

  def toJson(x: XUsage): String = {
    val d = s"The ${x.location.eType} ${x.location.path} (${x.location.desc}) is present whereas it is an unsupported element; Usage = X"
    val cat = conf.getString("x-usage.category")
    val cla = conf.getString("x-usage.classification")
    template(x.location.path, d, x.location.line, x.location.column, "", cat, cla)
  }

  def toJson(x: WUsage): String = {
    val d = s"The ${x.location.eType} ${x.location.path} (${x.location.desc}) is present whereas it is a withdrawn element; Usage=W"
    val cat = conf.getString("w-usage.category")
    val cla = conf.getString("w-usage.classification")
    template(x.location.path, d, x.location.line, x.location.column, "", cat, cla)
  }

  def toJson(x: REUsage): String = {
    val d = s"Element ${x.location.path} (${x.location.desc}) is missing. Depending on the use case and data availability it may be appropriate to value this element"
    val cat = conf.getString("re-usage.category")
    val cla = conf.getString("re-usage.classification")
    template(x.location.path, d, x.location.line, x.location.column, "", cat, cla)
  }

}
