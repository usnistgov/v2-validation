package hl7.v2.validation.report

import hl7.v2.instance.Location
import hl7.v2.validation.report

/**
  * Trait representing a report entry
  */
trait Entry {

  def location: Location
  def path: String = location.path
  def line: Int    = location.line
  def column: Int  = location.column

  def msg: String
  def evaluationTrace: String = ""
  def category: String
  def classification: String

  def toJson: String =
    jsonTemplate(path, msg, line, column, evaluationTrace, category, classification)

  override def toString: String =
    s"[$classification][$line, $column] $category : $msg \n\t$evaluationTrace".trim
}


//==============================================================================
//    Class representing the report
//==============================================================================

/**
  * Class representing the validation report
  */
case class Report(structure: Seq[SEntry], content: Seq[CEntry], vs: Seq[VSEntry]){

  def prettyPrint() {
    println(s"\n\n########  Structure check: ${structure.size} problem(s) detected.")
    structure.reverse foreach { e => println( e.toString ) }

    println(s"\n\n########  Content check: ${content.size} problem(s) detected.")
    content foreach {
      case x: report.Failure          => println( x.toString )
      case x: report.PredicateFailure => println( x.toString )
      case _                          => "" //FIXME
    }

    println(s"\n\n########  Value set check: ${vs.size} problem(s) detected.")
    vs foreach { e => println( e.toString ) }
    println("\n")
  }

  def asText: String = {
    val sb = new StringBuilder
    sb.append(s"\n########  Structure check: ${structure.size} problem(s) detected.\n\n")
    structure.reverse foreach { e => sb.append( e.toString ); sb.append("\n") }
    sb.append(s"\n\n########  Content check: ${content.size} problem(s) detected.\n\n")
    content foreach {
      case x: report.Failure          => sb.append( x.toString ); sb.append("\n")
      case x: report.PredicateFailure => sb.append( x.toString ); sb.append("\n")
      case _                          => //FIXME
    }
    sb.append(s"\n\n########  Value set check: ${vs.size} problem(s) detected.\n\n")
    vs foreach { e => sb.append( e.toString ); sb.append("\n") }
    sb.append("\n")
    sb.toString
  }

  def toJson: String = {
    val cc = content filter { x =>
      x.isInstanceOf[report.Failure] || x.isInstanceOf[report.PredicateFailure]
    }
    s""" { "Report" : [ ${structure.view ++ cc.view ++ vs.view map { _.toJson } mkString ","} ] }"""
  }

}
