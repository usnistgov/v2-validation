package hl7.v2.validation.content

import expression.{Expression, Reason}
import hl7.v2.instance.Element

/**
  * Trait representing a content check report entry
  */
trait Entry

/**
  * Class representing a successful constraint checking result
  */
case class Success(
    context   : Element,
    constraint: Constraint
) extends Entry

/**
  * Class representing a failed constraint checking result
  */
case class Failure(
    context   : Element,
    constraint: Constraint,
    stack     : List[(Expression, List[Reason])]
) extends Entry

/**
  * Class representing an inconclusive constraint checking result
  */
case class SpecError(
    context   : Element,
    constraint: Constraint,
    expression: Expression,  // The expression that can't be evaluated
    details   : List[String] // The list of problems found
) extends Entry
