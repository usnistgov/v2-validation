package hl7.v2.validation.report
package extension

import expression.{AsString, Expression}
import expression.EvalResult.{Reason, Trace}
import hl7.v2.instance.Element
import hl7.v2.validation.content.Constraint

/**
  * Provides functions to convert a content report entry (CEntry) to Json
  */
object CEntryAsJson {

  /**
    * Creates and returns a Json string from a content report entry (CEntry)
    * @param c - The content report entry
    * @return The Json string
    */
  def toJson(c: CEntry): String = c match {
    case x: Success => toJson(x)
    case x: Failure => toJson(x)
    case x: SpecError => toJson(x)
  }

  /**
    * Creates a JSON string from a successful constraint check result
    */
  private def toJson(x: Success): String =
    s"""{"Success":{${toJson(x.context)},${toJson(x.constraint, x.context)}}}"""

  /**
    * Creates a JSON string from a failed constraint check result
    */
  private def toJson(x: Failure): String = {
    val context    = toJson(x.context)
    val constraint = toJson(x.constraint, x.context)
    val stack      = toJson(x.context, x.stack)
    s"""{"Failure":{$context,$constraint,$stack}}"""
  }

  /**
    * Creates a JSON string from an inconclusive constraint check result
    */
  private def toJson(x: SpecError): String = {
    val context    = toJson(x.context)
    val constraint = toJson(x.constraint, x.context)
    val trace = s""""trace":${toJson(x.context, x.trace)}"""
    s"""{"SpecError":{$context,$constraint,$trace}}"""
  }

  /**
    * Creates a JSON string from an element
    */
  private def toJson(e: Element): String = s""""context":{${extension.toJson(e.location)}}"""

  /**
    * Creates a JSON string from an expression
    */
  private def toJson(c: Constraint, ctx: Element): String = {
    val l = List (
      c.id map ( x => s""""id":"${escape(x)}"""" ),
      c.description match {
        case Some(x) => Some(s""""description":"${escape(x)}"""")
        case None    => Some(s""""description":"${expAsString(c.assertion, ctx)}"""")
      }
    )
    s""""constraint":{${ l.flatten.mkString(",") }}"""
  }

  /**
    * Creates a JSON string from a failure stack traces
    */
  private def toJson(c: Element, stack: List[Trace]): String = {
    val s = stack.map { toJson(c, _) }.mkString("[", ",", "]")
    s""""stack":$s"""
  }

  /**
    * Creates a JSON string from a stack trace
    */
  private def toJson(c: Element, t: Trace): String = {
    val exp = expAsString(t.expression, c)
    val reasons = t.reasons map toJson mkString("[", ",", "]" )
    s"""{"expression":"$exp", "reasons":$reasons}"""
  }

  /**
    * Creates a JSON string from a failure reason
    */
  private def toJson(r: Reason): String =
    s"""{${extension.toJson(r.location)},"msg":"${escape(r.message)}"}"""

  private def expAsString(e: Expression, c: Element): String =
    escape( AsString.expression(e, c) )

}
