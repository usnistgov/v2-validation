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
  
  def orderIndifferentConstraints(): List[Context]
}
