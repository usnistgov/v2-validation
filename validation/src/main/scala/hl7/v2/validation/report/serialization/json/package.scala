package hl7.v2.validation.report.serialization


/**
  * Module containing helper functions for JSON serialization
  */
package object json {

  /**
    * Escapes json special characters from the string
    */
  def escape(s: String) = s.replaceAllLiterally("\\", "\\\\")
    .replaceAllLiterally("\"", "\\\"")

  /**
    * Report entry JSON template
    */
  def template (
      path: String,
      desc: String,
      line: Int,
      column: Int,
      details: String,
      category: String,
      classification: String
    ) = s"""{"path":"$path","description":"${escape(desc)
          }","line":$line,"column":$column,"details":"${escape(details)
          }","category":"$category","classification":"$classification"}"""

}
