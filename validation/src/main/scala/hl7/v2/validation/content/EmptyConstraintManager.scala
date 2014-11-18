package hl7.v2.validation.content

import hl7.v2.instance._

/**
  * An empty constraint manager
  */
object EmptyConstraintManager extends ConstraintManager {


  /**
    * Returns an empty list independently from the specified element.
    */
  def constraintsFor(e: Element): List[Constraint] = Nil
}
