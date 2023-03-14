package hl7.v2.validation.content

import hl7.v2.instance.Element


trait ConformanceContext {
  /**
    * Returns the list of constraints defined for the specified element.
    */
  def constraintsFor(e: Element): List[Constraint]

  /**
    * Returns the list of predicates defined for the specified element.
    */
  def predicatesFor(e: Element): List[Predicate]

  /**
    * Returns the list of order indifferent constraints defined in the conformance context.
    */
  def orderIndifferentConstraints(): List[Context]

  /**
    * Returns the list of co-constraints defined in the conformance context.
    */
  def coConstraintsFor(e: Element): List[CoConstraint]

}
