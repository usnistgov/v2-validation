package hl7.v2.validation

import expression.EvalResult.{Reason, Trace}
import hl7.v2.instance.{Element, Location}

/**
  * Module containing helpers functions for serializing report entries
  *
  * @author Salifou Sidi M. Malick <salifou.sidi@nist.gov>
  */
package object report {

  /**
    * Escapes json special characters from the string
    */
  def jsonEscape(s: String) = s.replaceAllLiterally("\\", "\\\\")
                               .replaceAllLiterally("\"", "\\\"")
                               .replaceAll("\n", "\\\\n")
                               .replaceAll("\t", "\\\\t")

  /**
    * Report entry JSON template
    */
  def jsonTemplate (
      path: String,
      desc: String,
      line: Int,
      column: Int,
      details: String,
      category: String,
      classification: String
    ) = s""" "Entry":{"path":"$path","description":"${jsonEscape(desc)
            }","line":$line,"column":$column,"details":"${jsonEscape(details)
            }","category":"$category","classification":"$classification"}"""



  def stackTrace(c: Element, l: List[Trace], tab: String = ""): String =
    Stream.from(1).zip(l).map( t => s"${trace(c, t._2, "\t" * t._1)}").mkString("\n")

  private
  def trace(c: Element, t: Trace, tab: String): String = {
    val rs = t.reasons.map( r => s"\n$tab\t${reason(r)}" ).mkString("\n")
    s"$tab[Failed] ${ expression.AsString.expression(t.expression, c)}$rs"
  }

  private
  def reason(r: Reason) = s"Reason: [${r.location.line}, ${r.location.column}] ${r.message}"

}
