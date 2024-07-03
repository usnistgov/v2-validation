package hl7.v2.validation

import expression.DefaultEvaluator
import hl7.v2.validation.vs.factory.impl.java.{DefaultValueSetFactory, DefaultValueSetFactoryConfiguration}
import hl7.v2.validation.vs.{DefaultValueSetValidator, EmptyValueSetLibrary, EmptyValueSetSpecification, ValueSetSpecification, VsValidator}

trait DefaultEvaluatorWithEmptyValueSetValidation extends DefaultEvaluator with DefaultValueSetValidator {
  implicit def legacyExpressionValueSetValidator: VsValidator = this
  implicit val valueSetFactory: hl7.v2.validation.vs.factory.ValueSetFactory = new DefaultValueSetFactory(EmptyValueSetLibrary.getInstance(), new DefaultValueSetFactoryConfiguration(true, false))
  def vsSpecification: ValueSetSpecification = EmptyValueSetSpecification
}
