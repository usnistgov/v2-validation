package hl7.v2.validation.report

import expression.AsString.{expression => exp}
import expression.{Expression, Reason}
import hl7.v2.instance.{Element, Location}


object PrettyPrint {

  def prettyPrint(r: Report) {
    println(s"\n\n########  Structure check: ${r.structure.size} problem(s) detected.")
    r.structure.reverse foreach { e => println(asString(e)) }

    println(s"\n\n########  Content check: ${r.content.size} problem(s) detected.")
    r.content foreach { e => println(asString(e)) }

    println(s"\n\n########  Value set check: ${r.vs.size} problem(s) detected.")
    r.vs foreach { e => println(asString(e)) }

    println("\n")
  }

  private def asString(e: Entry): String = e match {
    case x: RUsage   => usage(x)
    case x: REUsage  => usage(x)
    case x: XUsage   => usage(x)
    case x: WUsage   => usage(x)
    case x: MinCard  => cardinality(x)
    case x: MaxCard  => cardinality(x)
    case x: Length   => length(x)
    case x: Format   => format(x)
    case x: Extra    => extra(x)
    case x: UnescapedSeparators => unescapedSep(x)
    case x: Success   => success(x)
    case x: Failure   => failure(x)
    case x: SpecError => specErr(x)
    case x: InvalidLines => invalid(x)
    case x: UnexpectedLines => unexpected(x)
  }

  private def loc(l: Location) = f"[${l.line}%03d, ${l.column}%03d]\t${l.path}(${l.desc})"

  //private def desc(l: Location) = s"${l.path}(${l.desc})"

  // Usage problems
  private def usage(e: RUsage) = s"${loc(e.location)}\tR-Usage (required but missing)."

  private def usage(e: REUsage) = s"${loc(e.location)}\tRE-Usage"

  private def usage(e: XUsage) = s"${loc(e.location)}\tX-Usage (not supported but present)."

  private def usage(e: WUsage) = s"${loc(e.location)}\tW-Usage (withdrawn but present)."


  // Cardinality problems
  private def cardinality(e: MinCard) = {
    val expectation = s"Expected ${e.range}, found ${e.instance} repetitions"
    s"${loc(e.location)}\tMinimum cardinality violated. $expectation"
  }

  private def cardinality(e: MaxCard) = {
    val expectation = s"Expected ${e.range}, found ${e.instance} repetitions"
    s"${loc(e.location)}\tMaximum cardinality violated. $expectation"
  }

  private def length(e: Length) = {
    val expectation = s"Expected ${e.range}, found ${e.value.length} => (${e.value})"
    s"${loc(e.location)}\tLength violated. $expectation"
  }

  private def invalid(e: InvalidLines) = {
    println("\n>>>>> Invalid line(s)")
    e.list.map(l => s"\t[${l._1}, 1]\t'${l._2.take(70)}'").mkString("\n")
  }

  private def unexpected(e: UnexpectedLines) = {
    println("\n>>>>> Unexpected segment(s)")
    e.list.map(l => s"\t[${l._1}, 1]\t'${l._2.take(70)}'").mkString("\n")
  }

  private def format(e: Format) = s"${loc(e.location)}\t${e.details}"

  private def extra(e: Extra) = s"${loc(e.location)}"

  private def unescapedSep(e: UnescapedSeparators) =
    s"${loc(e.location)}\tUnescaped separators in primitive element."

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