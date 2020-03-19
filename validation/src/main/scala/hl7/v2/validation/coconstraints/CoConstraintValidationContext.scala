package hl7.v2.validation.coconstraints

import hl7.v2.instance.Group

trait CoConstraintValidationContext {

  def coConstraintBindingsFor(e: Group): List[CoConstraintBinding]

}
