package hl7.v2.validation.report

import expression.AsString.{expression => exp}
import expression.{Expression, Reason}
import hl7.v2.instance.{Element, Location}


object PrettyPrint {

  def prettyPrint(r: Report): Unit = {
    println(s"\n\n########  Structure check: ${r.structure.size} problem(s) detected.")
    r.structure foreach { e => println(asString(e)) }

    println(s"\n\n########  Content check: ${r.content.size} problem(s) detected.")
    r.content foreach { e => println(asString(e)) }

    println(s"\n\n########  Value set check: ${r.vs.size} problem(s) detected.")
    r.vs foreach { e => println(asString(e)) }
  }

  private def asString(e: Entry): String = e match {
    case x: RUsage    => rusage(x)
    case x: XUsage    => xusage(x)
    case x: WUsage    => wusage(x)
    case x: MinCard   => mincard(x)
    case x: MaxCard   => maxcard(x)
    case x: Length    => length(x)
    case x: Success   => success(x)
    case x: Failure   => failure(x)
    case x: SpecError => specErr(x)
    case x: InvalidLines => invalid(x)
    case x: UnexpectedLines => unexpected(x)
    case x: UnescapedSeparators => unescapedSep(x)
    case _ => ???
  }

  private def loc(l: Location) = f"[${l.line}%03d, ${l.column}%03d]\t"

  private def desc(l: Location) = s"${l.path}(${l.desc})"

  private def rusage(e: RUsage) =
    s"${loc(e.location)} ${desc(e.location)} is required but is missing."

  private def xusage(e: XUsage) =
    s"${loc(e.location)} ${desc(e.location)} is not supported but is present."

  private def wusage(e: WUsage) =
    s"${loc(e.location)} ${desc(e.location)} is withdrawn but is present."

  private def mincard(e: MinCard) =
    s"${loc(e.location)} ${desc(e.location)} violated the minimum cardinality. Expected ${
      e.range}, found ${e.instance} repetitions."

  private def maxcard(e: MaxCard) =
    s"${loc(e.location)} ${desc(e.location)} violated the maximum cardinality. Expected ${
      e.range}, found ${e.instance} repetitions."

  private def length(e: Length) =
    s"${loc(e.location)} ${desc(e.location)} violated the length spec. Expected ${
      e.range}, found ${e.value.length} => (${e.value})."

  private def invalid(e: InvalidLines) =
    e.list.map( l => s"[${l._1}, 1]\t Invalid Line ${l._2}" ).mkString("\n")

  private def unexpected(e: UnexpectedLines) =
    e.list.map( l => s"[${l._1}, 1]\t Unexpected segment ${l._2}" ).mkString("\n")

  private def unescapedSep(e: UnescapedSeparators) =
    s"${loc(e.location)} ${desc(e.location)} contains unescaped separators."

  private def success(e: Success) =
    s"${loc(e.context.location)}[Success] ${e.constraint.id} - ${ exp(e.constraint.assertion, e.context) }"

  private def failure(e: Failure) =
    s"${loc(e.context.location)}[Failure] ${e.constraint.id} - ${ exp(e.constraint.assertion, e.context) } \n ${stackTrace(e.context, e.stack)} "

  private def specErr(e: SpecError) = {
    val details = s"\t${exp(e.expression, e.context)} \n ${ e.details.mkString("\n\t\t") }"
    s"${loc(e.context.location)}[Success] ${e.constraint.id} - ${exp(e.constraint.assertion, e.context)} \n $details"
  }

  //FIXME this is just for testing ... need to be reimplemented
  private def stackTrace(context: Element, stack: List[(Expression, List[Reason])]): String = {
    var i = 0
    stack.map { x =>
      i = i + 1
      val reasons = x._2.map { r =>
        s"${"\t"*(i + 1)}Reason: [${r.location.line}, ${r.location.column}] ${r.msg}"
      }.mkString("\n")

      s"${"\t" * i }[Failed] ${ exp(x._1, context) }\n${ reasons }"

    }.mkString("\n")
  }
}