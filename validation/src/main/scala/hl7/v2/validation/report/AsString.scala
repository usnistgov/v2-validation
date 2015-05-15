package hl7.v2.validation.report

import com.typesafe.config.ConfigFactory

object AsString {

  val conf = ConfigFactory.load

  /**
    * Extends RUsage by adding the asString method
    */
  class RUsageExt(val x: RUsage) extends AnyVal {
    def category: String       = conf.getString("r-usage.category")
    def classification: String = conf.getString("r-usage.classification")
    def asString: String = s"The required ${x.location.prettyString} is missing"
  }

  /**
    * Extends XUsage by adding the asString method
    */
  class XUsageExt(val x: XUsage) extends AnyVal {
    def asString: String = s"The ${x.location.prettyString} is present whereas it is an unsupported element; Usage = X"
  }

  /**
    * Extends WUsage by adding the asString method
    */
  class WUsageExt(val x: WUsage) extends AnyVal {
    def asString: String = s"The ${x.location.prettyString} is present whereas it is a withdrawn element; Usage=W"
  }

  /**
    * Extends REUsage by adding the asString method
    */
  class REUsageExt(val x: REUsage) extends AnyVal {
    def asString: String = s"${x.location.prettyString} is missing. Depending on the use case and data availability it may be appropriate to value this element"
  }

  class MinCardAsString(val x: MinCard) extends AnyVal {
    def asString: String = s"Invalid Minimum Cardinality: ${x.location.prettyString} must be in the cardinality range of [%min%,%max%]; %occurrence% occurrences found."
  }

  /**
    * Escapes json special characters from the string
    */
  private def jsonEscape(s: String) = s.replaceAllLiterally("\\", "\\\\")
    .replaceAllLiterally("\"", "\\\"")

  /**
    * Report entry JSON template
    */
  private def jsonTemplate (
      path: String,
      msg: String,
      line: Int,
      column: Int,
      details: String,
      category: String,
      classification: String
    ) = s"""{"path":"$path","message":"${jsonEscape(msg)}","line":$line,"column":$column,"details":"${jsonEscape(details)}","category":"$category","classification":"$classification"}"""

}
