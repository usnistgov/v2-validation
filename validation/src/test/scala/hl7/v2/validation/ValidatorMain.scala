package hl7.v2.validation

import gov.nist.validation.report.Entry
import hl7.v2.parser.impl.DefaultParser
import hl7.v2.profile.XMLDeserializer
import hl7.v2.validation.coconstraints.{DefaultCoConstraintValidationContext, EmptyCoConstraintValidationContext}
import hl7.v2.validation.slicing.{DefaultProfileSlicingContext, EmptyProfileSlicingContext}
import hl7.v2.validation.vs.factory.impl.java.{DefaultValueSetFactory, DefaultValueSetFactoryConfiguration}
import hl7.v2.validation.vs.{DefaultValueSetSpecification, EmptyValueSetSpecification, ValueSetLibraryImpl}

import java.io.InputStream
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import scala.jdk.CollectionConverters.{CollectionHasAsScala, ListHasAsScala}


object Main extends App with DefaultParser with structure.DefaultValidator {

  def time[R](block: => R): R = {
    val t0 = System.nanoTime()
    val result = block    // call-by-name
    val t1 = System.nanoTime()
    println("Elapsed time: " + (t1 - t0) + "ns")
    result
  }

  def validateFromResources(folder: String, messageId: String) = validate(
    getClass.getResourceAsStream(s"${folder}/Profile.xml"),
    getClass.getResourceAsStream(s"${folder}/Constraints.xml") :: Nil,
    getClass.getResourceAsStream(s"${folder}/Valuesets.xml"),
    Option(getClass.getResourceAsStream(s"${folder}/ValueSetBindings.xml")),
    Option(getClass.getResourceAsStream(s"${folder}/Slicings.xml")),
    Option(getClass.getResourceAsStream(s"${folder}/CoConstraints.xml")),
    getClass.getResourceAsStream(s"${folder}/Message.txt"),
    messageId
  )

  def validate(profileInput: InputStream, constraints: List[InputStream], valueSetLibraryInput: InputStream, vsBindings:  Option[InputStream], slicings: Option[InputStream], coConstraints: Option[InputStream], messageInput: InputStream, messageId: String) = {
    val profile = XMLDeserializer.deserialize(profileInput) match {
      case Success(p) => p
      case Failure(e) => throw e
    }
    val message = scala.io.Source.fromInputStream(messageInput).mkString
    val conformanceContext = content.DefaultConformanceContext(constraints:_*).get
    val valueSetLibrary = ValueSetLibraryImpl(valueSetLibraryInput).get
    val factory = new DefaultValueSetFactory(valueSetLibrary, new DefaultValueSetFactoryConfiguration(
      true,
      false
    ))
    val bindings = vsBindings.map(DefaultValueSetSpecification(_) match {
      case Success(o) => o
      case Failure(e) => throw e
    }).getOrElse(EmptyValueSetSpecification)
    val slicing = slicings.map(DefaultProfileSlicingContext(_, profile) match {
      case Success(o) => o
      case Failure(e) => throw e
    }).getOrElse(EmptyProfileSlicingContext)
    val coConstraintsSpec = coConstraints.map(DefaultCoConstraintValidationContext(_) match {
      case Success(o) => o
      case Failure(e) => throw e
    }).getOrElse(EmptyCoConstraintValidationContext)
    val context = ValidationContext(
      profile,
      factory,
      conformanceContext,
      bindings,
      coConstraintsSpec,
      slicing
    )
    val validator = new HL7Validator(context)
    time {
      validator.validate(message, messageId) onComplete {
        case Success(report) =>
          val entries = scala.collection.mutable.Map[String, scala.collection.mutable.ListBuffer[Entry]]()
          for(category <- report.getEntries.values().asScala) {
            for (entry <- category.asScala) {
              entries.getOrElseUpdate(entry.getClassification, scala.collection.mutable.ListBuffer[Entry]()).addOne(entry)
            }
          }
          for(classification <- entries.keys) {
            val classEntries = entries.getOrElse(classification, Nil)
            println("\n")
            println(s"[Classification] ${classification} (${classEntries.size})")
            println("\n")
            for(entry <- classEntries) {
              println(s"${entry.toText}")
            }
          }
        case Failure(e) =>
          println(s"\n\n[Error] An error occurred while validating the message ... \n\t${e.getMessage}")
      }
    }
  }
}
