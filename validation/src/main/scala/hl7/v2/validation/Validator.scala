package hl7.v2.validation

import expression.{EvalResult, Plugin}
import gov.nist.validation.report.impl.ReportImpl
import hl7.v2.instance.{Element, Separators}
import hl7.v2.parser.Parser
import hl7.v2.profile.Profile
import hl7.v2.validation.vs.ValueSetLibrary

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

import gov.nist.validation.report.Report
import scala.collection.JavaConversions.{seqAsJavaList, mapAsJavaMap}
import java.util.{List => JList, Map => JMap, HashMap => JHMap}

/**
  * Trait defining the message validation 
  * 
  * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
  */

trait Validator { this: Parser with structure.Validator
                               with content.Validator
                               //with vs.Validator
                               =>

  val profile: Profile

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
            val contentErrors  = checkContent  ( m )
            //val valueSetErrors = checkValueSet ( m )
            for {
              r1 <- structErrors
              r2 <- contentErrors
              //r3 <- valueSetErrors
            } yield new ReportImpl( entries(r1, r2) )  //Report(r1, r2, r3)
          case Failure(e) => Future failed e
        }
    }

  private
  def entries[T](s1: Seq[T], s2: Seq[T]/*, s3: Seq[T]*/): JMap[String, JList[T]] = {
    val entries = new JHMap[String, JList[T]]()
    entries.put("structure", seqAsJavaList(s1))
    entries.put("content", seqAsJavaList(s2))
    //entries.put("valueSet", seqAsJavaList(s3))
    entries
  }
}

/**
  * An HL7 message validator which uses an empty value set validator
  * and the default implementation of the parser, structure validator,
  * content validator and expression evaluator.
  */
class HL7Validator(
    val profile: Profile,
    //FIXME override val valueSetLibrary: ValueSetLibrary,
    val conformanceContext: content.ConformanceContext,
    val pluginMap: Map[String, (Plugin, Element, Separators) => EvalResult]
  ) extends Validator
    with hl7.v2.parser.impl.DefaultParser
    with structure.DefaultValidator
    with content.DefaultValidator
    //FIXME with vs.DefaultValidator
    with expression.DefaultEvaluator

/*
/**
  * A synchronous HL7 message validator which uses an empty value set
  * validator  and the default implementation of the  parser,
  * structure validator, content validator and expression evaluator.
  */
class SyncHL7Validator(
    val profile: Profile,
    override val valueSetLibrary: ValueSetLibrary,
    val conformanceContext: content.ConformanceContext,
    val pluginMap: Map[String, (Plugin, Element, Separators) => EvalResult]
  ) extends Validator
    with hl7.v2.parser.impl.DefaultParser
    with structure.DefaultValidator
    with content.DefaultValidator
    with vs.DefaultValidator
    with expression.DefaultEvaluator {

  import scala.concurrent.Await
  import scala.concurrent.duration._

  @throws[Exception]
  def check(message: String, id: String): Report =
    Await.result(validate(message, id), 10.second)

}
*/