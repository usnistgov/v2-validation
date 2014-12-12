package hl7.v2.validation.report

import hl7.v2.instance.Location
import hl7.v2.profile.Range

/**
  * The package object contains various functions useful for extension
  */
package object extension {

  /**
    * Creates and returns a Json string from a Range
    */
  def toJson(r: Range): String = s""""range":{"min":"${r.min}","max":"${r.max}"}"""

  /**
    * Creates and returns a Json string from a Location
    */
  def toJson(l: Location): String = s""""location":{"desc":"${escape(l.desc)
  }","path":"${l.path}","line":"${l.line}","column":"${l.column}"}"""

  /**
    * Escapes json special characters from the string
    */
  def escape(s: String) = s.replaceAllLiterally("\\", "\\\\")
                           .replaceAllLiterally("\"", "\\\"")
}
