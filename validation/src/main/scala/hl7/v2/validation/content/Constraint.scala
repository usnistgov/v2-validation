package hl7.v2.validation.content

import expression.Expression

import scala.beans.BeanProperty

case class Reference(
    @BeanProperty chapter: String,
    @BeanProperty section: String,
    @BeanProperty page: String,
    @BeanProperty url: String,
    @BeanProperty source: String,
    @BeanProperty generatedBy: String,
    @BeanProperty referencePath: String,
    @BeanProperty testDataCategorization: String
)

/**
  * A constraint describes an assertion that shall always be verified
  */
case class Constraint( 
    id: String,
    reference: Option[Reference],
    description: String,
    assertion: Expression
)

/**
  * Class representing a predicate.
  */
case class Predicate(
    target: String,
    trueUsage: PredicateUsage,
    falseUsage: PredicateUsage,
    reference: Option[Reference],
    description: String,
    condition: Expression
)

/**
  * Trait representing allowed values for predicate true and false attributes
  */
sealed trait PredicateUsage
object PredicateUsage {
  case object R  extends PredicateUsage
  case object RE extends PredicateUsage
  case object X  extends PredicateUsage
  case object O  extends PredicateUsage
}