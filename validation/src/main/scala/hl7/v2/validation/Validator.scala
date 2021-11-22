package hl7.v2.validation

import gov.nist.validation.report.Report
import hl7.v2.parser.Parser
import hl7.v2.profile.Profile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}
import hl7.v2.validation.report.ConfigurableDetections
import com.typesafe.config.ConfigFactory

import scala.collection.JavaConverters._
import java.io.Reader

import hl7.v2.validation.coconstraints.{CoConstraintValidationContext, EmptyCoConstraintValidationContext}
import hl7.v2.validation.slicing.{EmptyProfileSlicingContext, ProfileSlicingContext}
import hl7.v2.validation.vs.EmptyValueSetSpecification

/**
 * Trait defining the message validation
 *
 * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
 * @author Hossam Tamri <hossam.tamri@gmail.com>
 */

trait Validator { this: Parser
    with structure.Validator
    with content.Validator
    with vs.VsValidator
    with slicing.SliceEvaluator
    with coconstraints.CoConstraintValidator =>

  val profile: Profile
  val valueSetLibrary: vs.ValueSetLibrary

  /**
   * Validates the message using the mixed in structure,
   * content and value set validators and returns the report.
   * @param message - The message to be validated
   * @param id      - The id of the message as defined in the profile
   * @return The validation report
   */
  def validate(message: String, id: String): Future[Report] = validate(message, id, null)

  /**
    * Validates the message using the mixed in structure,
    * content and value set validators and returns the report.
    *
    * @param message - The message to be validated
    * @param id      - The id of the message as defined in the profile
    * @param configuration - Custom configuration file
    * @return The validation report
    */
  def validate(message: String, id: String, configuration: Reader): Future[Report] =
    profile.messages get id match {
      case None =>
        val msg = s"No message with id '$id' is defined in the profile"
        Future failed new Exception(msg)
      case Some(model) =>
        parse(message, model) match {
          case Success(m) =>
            val defaultConfig = ConfigFactory.load();
            implicit val detections: ConfigurableDetections = if (configuration == null) new ConfigurableDetections(defaultConfig) else new ConfigurableDetections(ConfigFactory.parseReader(configuration).withFallback(defaultConfig).resolve());
            implicit val vsValidator: vs.Validator = new vs.Validator(detections)

            for {
              (sliceAppliedMessage, slicingDetections) <- applySlices(m)
              r1 <- checkStructure(sliceAppliedMessage)
              r2 <- checkContent(sliceAppliedMessage)
              r3 <- checkValueSets(sliceAppliedMessage)
              r4 <- Future { vsValidator.checkValueSet(sliceAppliedMessage, valueSetLibrary) }
              r5 <- checkCoConstraint(sliceAppliedMessage)
            } yield {
              report.Report(r1 ++ slicingDetections, r2 ++ r5, asScalaBuffer(r4) ++ r3)
            }

          case Failure(e) => Future failed e
        }
    }
}

/**
  * An HL7 message validator which uses the default implementation of the  parser,
  * and validators (structure, content, valueset, coconstraints)
 */
class HL7Validator(
  val profile: Profile,
  val valueSetLibrary: vs.ValueSetLibrary,
  val conformanceContext: content.ConformanceContext,
  val vsSpecification : vs.ValueSetSpecification,
  val coConstraintValidationContext: CoConstraintValidationContext,
  val profileSlicingContext: ProfileSlicingContext,
) extends Validator
    with hl7.v2.parser.impl.DefaultParser
    with structure.DefaultValidator
    with content.DefaultValidator
    with slicing.DefaultSliceEvaluator
    with vs.DefaultValueSetValidator
    with expression.DefaultEvaluator
    with coconstraints.DefaultValidator {

  /** v1 (Empty VS Specification, Empty Co-Constraint Specification) */
  def this(profile: Profile,
           valueSetLibrary: vs.ValueSetLibrary,
           conformanceContext: content.ConformanceContext) =
    this(
      profile,
      valueSetLibrary,
      conformanceContext,
      EmptyValueSetSpecification,
      EmptyCoConstraintValidationContext,
      EmptyProfileSlicingContext
    )

  /** v1.6.0+ vs specification (Empty Co-Constraint Specification) */
  def this(profile: Profile,
           valueSetLibrary: vs.ValueSetLibrary,
           conformanceContext: content.ConformanceContext,
           vsSpecification : vs.ValueSetSpecification) =
    this(
      profile,
      valueSetLibrary,
      conformanceContext,
      vsSpecification,
      EmptyCoConstraintValidationContext,
      EmptyProfileSlicingContext
    )

  /** v1.6.0+ co-constraints (Empty VS Specification) */
  def this(profile: Profile,
           valueSetLibrary: vs.ValueSetLibrary,
           conformanceContext: content.ConformanceContext,
           coConstraintValidationContext: CoConstraintValidationContext) =
    this(
      profile,
      valueSetLibrary,
      conformanceContext,
      EmptyValueSetSpecification,
      coConstraintValidationContext,
      EmptyProfileSlicingContext
    )

  /** v1.6.0+ Use ValidationContext (Builder) */
  def this(context: ValidationContext) =
    this(
      context.profile,
      context.valueSetLibrary,
      context.conformanceContext,
      context.vsSpecification,
      context.coConstraintValidationContext,
      context.slicingContext
    )
}

/**
 * A synchronous HL7 message validator which uses the default implementation of the  parser,
 * and validators (structure, content, valueset, coconstraints)
 */
class SyncHL7Validator(
        override val profile: Profile,
        override val valueSetLibrary: vs.ValueSetLibrary,
        override val conformanceContext: content.ConformanceContext,
        override val vsSpecification : vs.ValueSetSpecification,
        override val coConstraintValidationContext: CoConstraintValidationContext,
        override val profileSlicingContext: ProfileSlicingContext
)
  extends HL7Validator(profile, valueSetLibrary, conformanceContext, vsSpecification, coConstraintValidationContext, profileSlicingContext) {

  /** v1 (Empty VS Specification, Empty Co-Constraint Specification) */
  def this(profile: Profile,
           valueSetLibrary: vs.ValueSetLibrary,
           conformanceContext: content.ConformanceContext) =
    this(
      profile,
      valueSetLibrary,
      conformanceContext,
      EmptyValueSetSpecification,
      EmptyCoConstraintValidationContext,
      EmptyProfileSlicingContext
    )

  /** v1.6.0+ vs specification (Empty Co-Constraint Specification) */
  def this(profile: Profile,
           valueSetLibrary: vs.ValueSetLibrary,
           conformanceContext: content.ConformanceContext,
           vsSpecification : vs.ValueSetSpecification) =
    this(
      profile,
      valueSetLibrary,
      conformanceContext,
      vsSpecification,
      EmptyCoConstraintValidationContext,
      EmptyProfileSlicingContext
    )

  /** v1.6.0+ co-constraints (Empty VS Specification) */
  def this(profile: Profile,
           valueSetLibrary: vs.ValueSetLibrary,
           conformanceContext: content.ConformanceContext,
           coConstraintValidationContext: CoConstraintValidationContext) =
    this(
      profile,
      valueSetLibrary,
      conformanceContext,
      EmptyValueSetSpecification,
      coConstraintValidationContext,
      EmptyProfileSlicingContext
    )

  /** v1.6.0+ Use ValidationContext (Builder) */
  def this(context: ValidationContext) =
    this(
      context.profile,
      context.valueSetLibrary,
      context.conformanceContext,
      context.vsSpecification,
      context.coConstraintValidationContext,
      context.slicingContext
    )


  import scala.concurrent.Await
  import scala.concurrent.duration._

  /**
    * Validates the message using the mixed in structure,
    * content and value set validators and returns the report.
    * @param message - The message to be validated
    * @param id      - The id of the message as defined in the profile
    * @return The validation report
    */
  @throws[Exception]
  def check(message: String, id: String): Report =
    Await.result(validate(message, id), 10.second)

  /**
    * Validates the message using the mixed in structure,
    * content and value set validators and returns the report.
    *
    * @param message - The message to be validated
    * @param id      - The id of the message as defined in the profile
    * @param configuration - Custom configuration file
    * @return The validation report
    */
  @throws[Exception]
  def checkUsingConfiguration(message: String, id: String, configuration: Reader): Report =
    Await.result(validate(message, id, configuration), 10.second)

}

