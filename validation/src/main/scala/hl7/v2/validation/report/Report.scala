package hl7.v2.validation.report

import expression.EvalResult.{Reason, Trace}
import hl7.v2.instance.{Element, Line, Location}
import hl7.v2.profile.Range
import hl7.v2.validation.content.{Constraint, Predicate}
import hl7.v2.validation.vs.{BindingStrength, ValueSet}

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
case class Report(structure: Seq[SEntry], content: Seq[CEntry], vs: Seq[VSEntry]){

  /**
    * Returns the report as Json string
    */
  def toJson: String = extension.ReportAsJson.toJson(this)
}

//==============================================================================
//    Structure problem report entries
//==============================================================================

sealed trait UsageEntry extends SEntry { def location: Location }

case class RUsage(location: Location) extends UsageEntry

case class REUsage(location: Location) extends UsageEntry

case class XUsage(location: Location) extends UsageEntry

case class WUsage(location: Location) extends UsageEntry

case class MinCard(location: Location, instance: Int, range: Range) extends SEntry

case class MaxCard(location: Location, instance: Int, range: Range) extends SEntry

case class Length(location: Location, value: String, range: Range) extends SEntry

case class Extra( location: Location ) extends SEntry

case class UnescapedSeparators( location: Location ) extends SEntry

case class Format(location: Location, details: String) extends SEntry

case class UnexpectedLines( list: List[Line] ) extends SEntry

case class InvalidLines( list: List[Line]  ) extends SEntry

case class EVS (
    location: Location,
    value: String,
    valueSet: ValueSet,
    bindingStrength: Option[BindingStrength]
) extends SEntry

case class PVS (
    location: Location,
    value: String,
    valueSet: ValueSet,
    bindingStrength: Option[BindingStrength]
) extends SEntry

case class CodeNotFound(
    location: Location,
    value: String,
    valueSet: ValueSet,
    bindingStrength: Option[BindingStrength]
) extends SEntry

case class VSNotFound(
    location: Location,
    value: String,
    valueSetId: String,
    bindingStrength: Option[BindingStrength]
) extends SEntry

case class VSSpecError(
    location: Location,
    valueSetId: String,
    msg: String
) extends SEntry

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
case class Failure(context: Element, constraint: Constraint, stack: List[Trace]) extends CEntry

/**
  * Class representing an inconclusive constraint checking result
  */
case class SpecError(context: Element, constraint: Constraint, trace: Trace) extends CEntry

/**
  * Class representing a successful predicate checking result
  */
case class PredicateSuccess(predicate: Predicate) extends CEntry

/**
  * Class representing a failed predicate checking result
  */
case class PredicateFailure(predicate: Predicate, violations: List[UsageEntry]) extends CEntry

/**
  * Class representing an inconclusive predicate checking result
  */
case class PredicateSpecError(predicate: Predicate, reasons: List[Reason]) extends CEntry

//==============================================================================
//    Value Set problem report entries
//==============================================================================


