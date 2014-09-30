/*package hl7.v2.validation.content

import hl7.v2.instance.{Component, Field, Segment, Group}

/**
  * An empty constraint manager
  */
object EmptyConstraintManager extends ConstraintManager {

  /**
   * Returns the constraints applicable to the specified group
   */
  def constraintsFor( g: Group ): List[Constraint] = Nil

  /**
   * Returns the constraints applicable to the specified segment
   */
  def constraintsFor( s: Segment ): List[Constraint] = Nil

  /**
   * Returns the constraints applicable to the specified field
   */
  def constraintsFor( f: Field ): List[Constraint] = Nil

  /**
   * Returns the constraints applicable to the specified component
   */
  def constraintsFor( c: Component ): List[Constraint] = Nil
}
*/