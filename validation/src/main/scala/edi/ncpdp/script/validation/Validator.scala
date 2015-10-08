package edi.ncpdp.script.validation

import gov.nist.validation.report.Report
import hl7.v2.profile.Profile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * An NCPDP message validator which uses the default implementation of the parser, structure validator,
  * content validator and expression evaluator.
  */
class NCPDPValidator(
    val profile: Profile,
    val valueSetLibrary: hl7.v2.validation.vs.ValueSetLibrary,
    val conformanceContext: hl7.v2.validation.content.ConformanceContext
  ) extends hl7.v2.validation.Validator
    with edi.ncpdp.script.parser.impl.DefaultNCPDPParser
    with hl7.v2.validation.structure.DefaultValidator
    with hl7.v2.validation.content.DefaultValidator
    with expression.DefaultEvaluator


/**
  * A synchronous NCPDP message validator which uses the default implementation of the  parser,
  * structure validator, content validator and expression evaluator.
  */
class SyncNCPDPValidator(
    val profile: Profile,
    val valueSetLibrary: hl7.v2.validation.vs.ValueSetLibrary,
    val conformanceContext: hl7.v2.validation.content.ConformanceContext
  ) extends hl7.v2.validation.Validator
    with edi.ncpdp.script.parser.impl.DefaultNCPDPParser
    with hl7.v2.validation.structure.DefaultValidator
    with hl7.v2.validation.content.DefaultValidator
    with expression.DefaultEvaluator {

  import scala.concurrent.Await
  import scala.concurrent.duration._

  @throws[Exception]
  def check(message: String, id: String): Report =
    Await.result(validate(message, id), 10.second)

}
