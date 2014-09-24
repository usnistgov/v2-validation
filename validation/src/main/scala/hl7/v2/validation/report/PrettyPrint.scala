package hl7.v2.validation.report

import expression.AsString.{expression => exp}
import expression.{Expression, Reason}
import hl7.v2.instance.{Element, Location}


object PrettyPrint {

  def prettyPrint(r: Report): Unit = {
    println("########  Structure problems")
    r.structure foreach { e => println(asString(e)) }

    println("########  Content problems")
    r.content foreach { e => println(asString(e)) }

    println("########  Value set problems")
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
    case _ => ???
  }

  private def loc(l: Location) = s"[${l.line}, ${l.column}][${l.path}]"

  private def rusage(e: RUsage) =  s"${loc(e.location)}[RUsage]"

  private def xusage(e: XUsage) = s"${loc(e.location)}[XUsage]"

  private def wusage(e: WUsage) = s"${loc(e.location)}[WUsage]"

  private def mincard(e: MinCard) = s"${loc(e.location)}[Min Cardinality] ${e.instance} not in ${e.range}"

  private def maxcard(e: MaxCard) = s"${loc(e.location)}[Max Cardinality] ${e.instance} not in ${e.range}"

  private def length(e: Length) = s"${loc(e.location)}[Length] length of '${e.value}' (${e.value.length}}) not in ${e.range}"

  private def invalid(e: InvalidLines) = e.list.map( l => s"[${l._1}, 1][Invalid Line] ${l._2}" ).mkString("\n")

  private def unexpected(e: UnexpectedLines) = e.list.map( l => s"[${l._1}, 1][Unexpected] ${l._2}" ).mkString("\n")

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