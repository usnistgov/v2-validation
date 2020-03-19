package hl7.v2.validation.coconstraints

import hl7.v2.instance.Group

class DefaultCoConstraintValidationContext(
          val coConstraintsMap: Map[String, List[CoConstraintBinding]]) extends CoConstraintValidationContext {

  override def coConstraintBindingsFor(g: Group): List[CoConstraintBinding] = {
    coConstraintsMap.getOrElse(g.model.id, Nil)
  }

}
