package hl7.v2.validation.content

import hl7.v2.instance.Group
import hl7.v2.instance.Segment
import hl7.v2.instance.Field
import hl7.v2.instance.Component

trait ConstraintManager {

  /**
    * Returns the constraints applicable to the specified group
    */
  def constraintsFor( g: Group ): List[Constraint]

  /**
    * Returns the constraints applicable to the specified segment
    */
  def constraintsFor( s: Segment ): List[Constraint] 

  /**
    * Returns the constraints applicable to the specified field
    */
  def constraintsFor( f: Field ): List[Constraint]

  /**
    * Returns the constraints applicable to the specified component
    */
  def constraintsFor( c: Component ): List[Constraint]
}
