package hl7.v2.validation

import gov.nist.validation.report.Report
import hl7.v2.parser.Parser
import hl7.v2.profile.Profile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * Trait defining the message validation 
  * 
  * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
  */

trait Validator { this: Parser with structure.Validator
                               with content.Validator
                               =>
  val profile: Profile

  val valueSetLibrary: vs.ValueSetLibrary

  /**
    * Validates the message using the mixed in structure,
    * content and value set validators and returns the report.
    * @param message - The message to be validated
    * @param id      - The id of the message as defined in the profile
    * @return The validation report
    */
  def validate( message: String, id: String ): Future[Report] =
    profile.messages get id match {
      case None =>
        val msg = s"No message with id '$id' is defined in the profile"
        Future failed new Exception(msg)
      case Some( model ) => 
        parse( message, model ) match {
          case Success( m ) => 
            val structErrors   = checkStructure( m )
            println("st")
            val contentErrors  = checkContent  ( m )
            println("ct")
            val valueSetErrors = Future { vs.Validator.checkValueSet(m, valueSetLibrary) }
            println("vt")
            for {
              r1 <- structErrors
              r2 <- contentErrors
              r3 <- valueSetErrors
            } yield report.Report(r1, r2, r3)
          case Failure(e) => Future failed e
        }
    }
}

/**
  * An HL7 message validator which uses an empty value set validator
  * and the default implementation of the parser, structure validator,
  * content validator and expression evaluator.
  */
class HL7Validator(
    val profile: Profile,
    val valueSetLibrary: vs.ValueSetLibrary,
    val conformanceContext: content.ConformanceContext
  ) extends Validator
    with hl7.v2.parser.impl.DefaultParser
    with structure.DefaultValidator
    with content.DefaultValidator
    with expression.DefaultEvaluator


/**
  * A synchronous HL7 message validator which uses an empty value set
  * validator  and the default implementation of the  parser,
  * structure validator, content validator and expression evaluator.
  */
class SyncHL7Validator(
    val profile: Profile,
    val valueSetLibrary: vs.ValueSetLibrary,
    val conformanceContext: content.ConformanceContext
  ) extends Validator
    with hl7.v2.parser.impl.DefaultParser
    with structure.DefaultValidator
    with content.DefaultValidator
    with expression.DefaultEvaluator {

  import scala.concurrent.Await
  import scala.concurrent.duration._

  @throws[Exception]
  def check(message: String, id: String): Report =
    Await.result(validate(message, id), 10.second)

}
