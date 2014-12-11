package hl7.v2.validation.report
package extension

import hl7.v2.profile.Range
import hl7.v2.instance.{Element, Location}
import hl7.v2.validation.content.Constraint

object JsonSerialization {

  private def toJson(r: Range): String = s""""range":{"min":"${r.min
  }","max":"${r.max}"}"""

  private def toJson(l: Location): String = s""""location":{"desc":"${escape(l.desc)
    }","path":"${l.path}","line":"${l.line}","column":"${l.column}"}"""

  private def toJson(x: RUsage): String = s"""{"RUsage":{${toJson(x.location)}}}"""

  private def toJson(x: XUsage): String = s"""{"XUsage":{${toJson(x.location)}}}"""

  private def toJson(x: WUsage): String = s"""{"WUsage":{${toJson(x.location)}}}"""

  private def toJson(x: MinCard): String =
    s"""{"MinCard":{${toJson(x.location)},${toJson(x.range)},"instance":"${x.instance}"}}"""

  private def toJson(x: MaxCard): String =
    s"""{"MaxCard":{${toJson(x.location)},${toJson(x.range)},"instance":"${x.instance}"}}"""

  private def toJson(x: Length): String =
    s"""{"Length":{${toJson(x.location)},${toJson(x.range)},"value":"${escape(x.value)}"}}"""

  private def toJson(x: Format): String =
    s"""{"Format":{${toJson(x.location)},"details":"${escape(x.details)}"}}"""

  private def toJson(x: Extra): String = s"""{"Extra":{${toJson(x.location)}}}"""

  private def toJson(x: UnescapedSeparators): String =
    s"""{"UnescapedSeparators":{${toJson(x.location)}}}"""

  private def toJson(x: InvalidLines): String =
    s"""{"InvalidLines":{"list":${x.list.map( l => s"""{"line":"${l._1}","value":"${l._2}"}""" ).mkString("[", ",", "]")}}}"""

  private def toJson(x: UnexpectedLines): String =
    s"""{"UnexpectedLines":{"list":${x.list.map( l => s"""{"line":"${l._1}","value":"${l._2}"}""" ).mkString("[", ",", "]")}}}"""

  private def toJson(s: SEntry): String = s match {
    case x: RUsage  => toJson(x)
    case x: XUsage  => toJson(x)
    case x: WUsage  => toJson(x)
    case x: MinCard => toJson(x)
    case x: MaxCard => toJson(x)
    case x: Length  => toJson(x)
    case x: Format  => toJson(x)
    case x: Extra   => toJson(x)
    case x: UnexpectedLines => toJson(x)
    case x: InvalidLines    => toJson(x)
    case x: UnescapedSeparators => toJson(x)
  }

  import expression.AsString.expression

  private def toJson(c: Constraint, ctx: Element): String = {
    val l = List (
      c.id map ( x => s""""id":"$x""" ),
      c.tag map ( x => s""""tag":"$x""" ),
      c.description match {
        case Some(x) => Some(s""""description":"$x""")
        case None    => Some(s""""description":"${expression(c.assertion, ctx)}""")
      }
    )
    s""""constraint":{${ l.flatten.mkString(",") }}"""
  }

  

  def toJson(r: Report): String = s"""{"structure":${ r.structure.map( s => toJson(s) ).mkString("[", ",", "]") }}"""

  private def escape(s: String) =
    s.replaceAllLiterally("\\", "\\\\").replaceAllLiterally("\"", "\\\"")
}

/*

case class Success(context: Element, constraint: Constraint) extends CEntry

case class Failure(
                    context   : Element,
                    constraint: Constraint,
                    stack     : List[(Expression, List[Reason])]
                    ) extends CEntry

case class SpecError(
                      context   : Element,
                      constraint: Constraint,
                      expression: Expression,  // The expression that can't be evaluated
                      details   : List[String] // The list of problems found
                      ) extends CEntry
*/
