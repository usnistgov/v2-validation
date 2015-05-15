package hl7.v2.validation.report

import expression.EvalResult.{Reason, Trace}
import hl7.v2.instance.{Element, Location}
import hl7.v2.validation.content.{Predicate, Constraint}
import hl7.v2.validation.report.Configurations.{Templates, Classifications, Categories}

/**
  * Trait defining a content report entry
  *
  * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
  */
trait CEntry extends Entry {

  override def toString: String =
    s"[$classification][$line, $column] $category : $msg \n\t$evaluationTrace\n"
}


/**
  * Class representing a failed constraint checking result
  */
case class Failure(context: Element, constraint: Constraint, stack: List[Trace])
      extends CEntry {
  override lazy val location = context.location //FIXME Refactor to get a more precise location if possible.
  override lazy val category: String = Categories.Failure
  override lazy val classification: String = Classifications.Failure
  override lazy val msg = String.format(Templates.Failure, constraint.id, constraint.description)
  override lazy val evaluationTrace = stackTrace(context, stack)
}

/**
  * Class representing a failed constraint checking result
  */
case class PredicateFailure(predicate: Predicate, violation: UsageEntry)
  extends CEntry {
  override lazy val location = violation.location
  override lazy val category: String = Categories.PredicateFailure
  override lazy val classification: String = Classifications.PredicateFailure
  override lazy val msg =
    String.format(Templates.PredicateFailure, violation.msg, usage, predicate.description)

  private def usage = violation match {
    case RUsage(_) => "required"
    case XUsage(_) => "not supported"
    case _         => throw new IllegalStateException
  }
}

//TODO Handle the classes below. The will be filtered out from the report for now
/**
  * Class representing a successful constraint checking result
  */
case class Success(context: Element, constraint: Constraint) extends CEntry {
  override def location: Location = ???
  override def msg: String = ???
  override def category: String = ???
  override def classification: String = ???
}

/**
  * Class representing an inconclusive constraint checking result
  */
case class SpecError(context: Element, constraint: Constraint, trace: Trace) extends CEntry {
  override def location: Location = ???
  override def msg: String = ???
  override def category: String = ???
  override def classification: String = "" //Doesn't apply
}


/**
  * Class representing a successful predicate checking result
  */
case class PredicateSuccess(predicate: Predicate) extends CEntry {
  override def location: Location = ???
  override def msg: String = ???
  override def category: String = ???
  override def classification: String = ???
}

/**
  * Class representing an inconclusive predicate checking result
  */
case class PredicateSpecError(predicate: Predicate, reasons: List[Reason]) extends CEntry {
  override def location: Location = ???
  override def msg: String = ???
  override def category: String = ???
  override def classification: String = ???
}

/*
  ################################################################################
  # This detection is issued for a successful constraint verification
  #
  #    The template takes two parameter:
  #       $1 The id of the constraint
  #       $2 The description of the constraint
  ################################################################################

  success {
    template = "%s - %s"
    category = ${report.category.constraint}
    classification = ${report.classification.informational}
  }


  ################################################################################
  # This detection is issued for a successful predicate verification
  #
  #    The template takes four parameters:
  #       $1 The description
  #       $2 The true usage
  #       $3 The false usage
  #       $4 The target
  ################################################################################
  predicate-success {
    template = "%s - Usage = C(%s/%s)  - Target = %s"
    category = ${report.category.predicate}
    classification = ${report.classification.informational}
  }


 */