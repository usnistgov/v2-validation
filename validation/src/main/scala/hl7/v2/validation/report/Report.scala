package hl7.v2.validation.report

/**
  * Trait representing a report entry
  */
sealed trait Entry

/**
  * Trait representing a structure error report entry
  */
trait SEntry extends Entry

/**
  * Trait representing a content error report entry
  */
trait CEntry extends Entry

/**
  * Trait representing a value set error report entry
  */
trait VSEntry extends Entry


/**
  * Class representing a validation report
  */
case class Report(structure: Seq[SEntry], content: Seq[CEntry], vs: Seq[VSEntry])


import java.util.{List => JList}

case class JReport(structure: JList[SEntry], content: JList[CEntry], vs: JList[VSEntry])
