package hl7.v2.validation.coconstraints

import expression.Expression
import hl7.v2.validation.vs.ValueSetBinding

case class CoConstraintBindingContext(name: String, path: String, segments: List[CoConstraintBindingSegment])
case class CoConstraintBindingSegment(name: String, path: String, bindings: List[CoConstraintTable])

trait CoConstraintCollection {
  val coConstraints: List[CoConstraint]
}

trait CoConstraintTable extends CoConstraintCollection {
  val grouper: List[GroupId]
  val coConstraintGroups: List[CoConstraintGroup]
}

case class GroupId(priority: Int, path: String, name: String)

case class CoConstraintRequirement(usage: CoConstraintUsage, cardinality: hl7.v2.profile.Range)

case class CoConstraintGroup(name: String, requirement: CoConstraintRequirement, primary: CoConstraint, coConstraints: List[CoConstraint]) extends CoConstraintCollection

case class Condition(description: String, assertion: Expression)

case class ConditionalCoConstraintTable(condition: Condition, grouper: List[GroupId], coConstraints: List[CoConstraint], coConstraintGroups: List[CoConstraintGroup]) extends CoConstraintTable

case class SimpleCoConstraintTable(grouper: List[GroupId], coConstraints: List[CoConstraint], coConstraintGroups: List[CoConstraintGroup]) extends CoConstraintTable

case class CoConstraint(requirement: CoConstraintRequirement, selectors: List[CoConstraintCell], constraints: List[CoConstraintCell])

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

  def fromString(s: String): CoConstraintUsage = {
    s match {
      case "R" => R
      case "S" => S
      case "O" => O
      case _ => throw new Error(s"Invalid usage '${s}'")
    }
  }
}
