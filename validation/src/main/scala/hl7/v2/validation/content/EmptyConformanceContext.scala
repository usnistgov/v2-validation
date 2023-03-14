package hl7.v2.validation.content

import hl7.v2.instance.Element

object EmptyConformanceContext extends ConformanceContext {

  /**
    * Returns the list of constraints defined for the specified element.
    */
  override def constraintsFor(e: Element): List[Constraint] = Nil
  /**
    * Returns the list of predicates defined for the specified element.
    */
  override def predicatesFor(e: Element): List[Predicate] = Nil
  
  override def orderIndifferentConstraints(): List[Context] = Nil
  
  override def coConstraintsFor(e: Element): List[CoConstraint] = Nil
}
