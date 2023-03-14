package hl7.v2.validation.coconstraints
import hl7.v2.instance.{Group, Message}

object EmptyCoConstraintValidationContext extends CoConstraintValidationContext {
  override def coConstraintBindingsFor(e: Message): List[CoConstraintBindingContext] = Nil
}
