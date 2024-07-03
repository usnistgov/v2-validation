package expression

import hl7.v2.validation.vs.{DefaultValueSetValidator, VsValidator}

object DefaultEvaluatorSpec extends EvaluatorSpec with DefaultEvaluator with DefaultValueSetValidator {
  implicit def legacyExpressionValueSetValidator: VsValidator = this
}
