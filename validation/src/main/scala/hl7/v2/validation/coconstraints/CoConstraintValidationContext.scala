package hl7.v2.validation.coconstraints

import hl7.v2.instance.Message

trait CoConstraintValidationContext {

  def coConstraintBindingsFor(e: Message): List[CoConstraintBindingContext]

}
