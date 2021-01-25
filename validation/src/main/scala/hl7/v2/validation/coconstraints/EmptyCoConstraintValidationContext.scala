package hl7.v2.validation.coconstraints
import hl7.v2.instance.Group

object EmptyCoConstraintValidationContext extends CoConstraintValidationContext {
  override def coConstraintBindingsFor(e: Group): List[CoConstraintBinding] = Nil
}
