package hl7.v2.validation.coconstraints

import expression.Expression
import hl7.v2.validation.vs.ValueSetBinding

case class CoConstraintBinding(context: String,
                               target: String,
                               segmentName: String,
                               bindings: List[CoConstraintTable])

trait CoConstraintCollection {
  val coConstraints: List[CoConstraint]
}

trait CoConstraintTable extends CoConstraintCollection {
  val coConstraintGroups: List[CoConstraintGroup]
}

case class CoConstraintRequirement(usage: CoConstraintUsage, cardinality: hl7.v2.profile.Range)

case class CoConstraintGroup(name: String, requirement: CoConstraintRequirement, coConstraints: List[GroupCoConstraint]) extends CoConstraintCollection

case class Condition(description: String, assertion: Expression)

case class ConditionalCoConstraintTable(condition: Condition, coConstraints: List[CoConstraint], coConstraintGroups: List[CoConstraintGroup]) extends CoConstraintTable

case class SimpleCoConstraintTable(coConstraints: List[SimpleCoConstraint], coConstraintGroups: List[CoConstraintGroup]) extends CoConstraintTable

trait CoConstraint {
  val requirement: CoConstraintRequirement
  val selectors: List[CoConstraintCell]
  val constraints: List[CoConstraintCell]
}

case class GroupCoConstraint(requirement: CoConstraintRequirement, selectors: List[CoConstraintCell], constraints: List[CoConstraintCell], primary: Boolean) extends CoConstraint
case class SimpleCoConstraint(requirement: CoConstraintRequirement, selectors: List[CoConstraintCell], constraints: List[CoConstraintCell]) extends CoConstraint

sealed trait CoConstraintCell {
  val element: String
}
case class PlainText(element: String, path: String, value: String) extends CoConstraintCell
case class Code(element: String, path: String, code: String, codeSystem: String, bindingLocation: List[CoConstraintBindingLocation]) extends CoConstraintCell
case class ValueSet(element: String, path: String, bindings: List[ValueSetBinding]) extends CoConstraintCell

case class CoConstraintBindingLocation(position: Int, code: String, codeSystem: String)
sealed trait CoConstraintUsage
object CoConstraintUsage {
  case object R  extends CoConstraintUsage
  case object S extends CoConstraintUsage
  case object O  extends CoConstraintUsage
}
