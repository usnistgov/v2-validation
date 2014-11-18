package hl7.v2.validation.report

import expression.{Reason, Expression}
import hl7.v2.instance.Element
import hl7.v2.validation.content.Constraint

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
