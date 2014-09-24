package hl7.v2.validation

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success
import scala.util.Try

import hl7.v2.parser.Parser
import hl7.v2.profile.Profile
import hl7.v2.validation.report.Report

/**
  * Trait defining the message validation 
  * 
  * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
  */

trait Validator { this: Parser with structure.Validator
                               with content.Validator
                               with vs.Validator =>

  val profile: Profile

  /**
    * Validates the message against the structure and content constraints
    * @param message - The message to be validated
    * @param id      - The id of the message with the profile
    * @return The report
    */
  def validate( message: String, id: String ): Future[Try[Report]] = 
    profile.messages get( id ) match {
      case None =>
        val msg = s"No message with id '$id' is found in the profile"
        Future{ Failure( new Error(msg) ) }
      case Some( model ) => 
        parse( message, model ) match {
          case Success( m ) => 
            val structErrors   = checkStructure( m )
            val contentErrors  = checkContent  ( m )
            val valueSetErrors = checkValueSet ( m )
            for {
              r1 <- structErrors
              r2 <- contentErrors
              r3 <- valueSetErrors
            } yield Success( Report(r1, r2, r3) )
          case Failure(e) => Future{ Failure(e) }
        }
    }
}

class HL7Validator(
    val profile: Profile,
    val constraintManager: content.ConstraintManager
  ) extends Validator 
    with hl7.v2.parser.impl.DefaultParser
    with structure.DefaultValidator
    with content.DefaultValidator
    with vs.EmptyValidator
    with expression.DefaultEvaluator
    
