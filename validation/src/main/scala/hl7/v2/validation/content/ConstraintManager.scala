package hl7.v2.validation.content

import hl7.v2.instance.Element


trait ConstraintManager {

  /**
    * Returns the list of constraints defined for
    * the specified element.
    */
  def constraintsFor(e: Element): List[Constraint]
}
