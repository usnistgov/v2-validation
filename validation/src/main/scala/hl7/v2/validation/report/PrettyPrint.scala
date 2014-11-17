package hl7.v2.validation.report

import expression.AsString.{expression => exp}
import expression.{Expression, Reason}
import hl7.v2.instance.{Element, Location}


object PrettyPrint {

  def prettyPrint(r: Report) {
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

  private def loc(l: Location) = f"[${l.line}%03d, ${l.column}%03d]\t${l.path}(${l.desc})"

  //private def desc(l: Location) = s"${l.path}(${l.desc})"

  // Usage problems
  private def rusage(e: RUsage) = s"${loc(e.location)} is required but is missing."

  private def xusage(e: XUsage) = s"${loc(e.location)} is not supported but is present."

  private def wusage(e: WUsage) = s"${loc(e.location)} is withdrawn but is present."


  // Cardinality problems
  private def mincard(e: MinCard) = {
    val expectation = s"Expected ${e.range}, found ${e.instance} repetitions."
    s"${loc(e.location)} violated the minimum cardinality. $expectation"
  }

  private def maxcard(e: MaxCard) = {
    val expectation = s"Expected ${e.range}, found ${e.instance} repetitions."
    s"${loc(e.location)} violated the maximum cardinality. $expectation."
  }

  private def length(e: Length) = {
    val expectation = s"Expected ${e.range}, found ${e.value.length} => (${e.value})"
    s"${loc(e.location)} violated the length spec. $expectation."
  }

  private def invalid(e: InvalidLines) =
    e.list.map( l => s"[${l._1}, 1]\t Invalid Line ${l._2}" ).mkString("\n")

  private def unexpected(e: UnexpectedLines) =
    e.list.map( l => s"[${l._1}, 1]\t Unexpected segment ${l._2}" ).mkString("\n")

  private def unescapedSep(e: UnescapedSeparators) =
    s"${loc(e.location)} contains unescaped separators."

  private def success(e: Success) = s"[Success] ${loc(e.context.location)} ${
    e.constraint.id} - ${ exp(e.constraint.assertion, e.context) }"

  private def failure(e: Failure) = s"[Failure] ${loc(e.context.location)} ${
    e.constraint.id} - ${ exp(e.constraint.assertion, e.context) } \n ${
    stackTrace(e.context, e.stack)} "

  private def specErr(e: SpecError) = {
    val details = s"\t${exp(e.expression, e.context)} \n ${ e.details.mkString("\n\t\t") }"
    s"[Spec Error] ${loc(e.context.location)} ${e.constraint.id} - ${exp(e.constraint.assertion, e.context)} \n $details"
  }

  type EStack = List[(Expression, List[Reason])]
  //FIXME this is just for testing ... need to be reimplemented
  private def stackTrace(context: Element, stack: EStack): String = {
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