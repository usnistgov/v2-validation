package hl7.v2.validation.report

import expression.{Expression, Reason}
import hl7.v2.instance.{Element, Location}
import hl7.v2.profile.Range
import hl7.v2.validation.content.Constraint

/**
  * Trait representing a report entry
  */
sealed trait Entry

/**
  * Trait representing a structure problem report entry
  */
sealed trait SEntry extends Entry

/**
  * Trait representing a content problem report entry
  */
sealed trait CEntry extends Entry

/**
  * Trait representing a value set problem report entry
  */
sealed trait VSEntry extends Entry


//==============================================================================
//    Class representing the report
//==============================================================================

/**
  * Class representing the validation report
  */
case class Report(structure: Seq[SEntry], content: Seq[CEntry], vs: Seq[VSEntry])

//==============================================================================
//    Structure problem report entries
//==============================================================================

case class RUsage(location: Location) extends SEntry

case class REUsage(location: Location) extends SEntry

case class XUsage(location: Location) extends SEntry

case class WUsage(location: Location) extends SEntry

case class MinCard(location: Location, instance: Int, range: Range) extends SEntry

case class MaxCard(location: Location, instance: Int, range: Range) extends SEntry

case class Length(location: Location, value: String, range: Range) extends SEntry

case class UnexpectedLines( list: List[(Int, String)] ) extends SEntry

case class InvalidLines( list: List[(Int, String)] ) extends SEntry

case class Extra( location: Location ) extends SEntry

case class UnescapedSeparators( location: Location ) extends SEntry

case class Format(location: Location, details: String) extends SEntry

//==============================================================================
//    Content problem report entries
//==============================================================================

/**
  * Class representing a successful constraint checking result
  */
case class Success(context: Element, constraint: Constraint) extends CEntry

/**
  * Class representing a failed constraint checking result
  */
case class Failure(
    context   : Element,
    constraint: Constraint,
    stack     : List[(Expression, List[Reason])]
) extends CEntry

/**
  * Class representing an inconclusive constraint checking result
  */
case class SpecError(
    context   : Element,
    constraint: Constraint,
    expression: Expression,  // The expression that can't be evaluated
    details   : List[String] // The list of problems found
) extends CEntry

//==============================================================================
//    Value Set problem report entries
//==============================================================================
