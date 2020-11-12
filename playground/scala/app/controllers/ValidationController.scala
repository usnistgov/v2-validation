package controllers

import java.io.{ByteArrayInputStream, StringReader}
import java.util.stream.Collectors

import com.typesafe.config.{ConfigException, ConfigFactory}
import hl7.v2.profile.XMLDeserializer
import hl7.v2.validation.{HL7Validator, content}
import hl7.v2.validation.vs.{EmptyValueSetSpecification, ValueSetLibraryImpl}
import javax.inject._
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.SchemaFactory
import org.xml.sax.ErrorHandler
import play.api.mvc._
import play.api.libs.json.{Json, Reads, Writes}
import java.net.URL

import controllers.ResourceType.{Constraints, Profile, VsLib}

import scala.util.{Failure, Success, Try}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import collection.JavaConverters._
import scala.collection.mutable
import scala.xml.SAXParseException

object ResourceType extends Enumeration {
  type ResourceType = Value
  val Profile, Constraints, VsLib, VsSpec, Configuration = Value
}

import ResourceType._

case class ValidationQuery(profile: String, constraints: String, vsLib: String, configuration: String, id: String, message: String)

case class CheckResource(content: String, rType: ResourceType)

case class ValidationResult(profile: ResourceCheckResult, constraints: ResourceCheckResult, vsLib: ResourceCheckResult, configuration: ResourceCheckResult, message: List[Entry])

case class Entry(category: String, classification: String, description: String, path: String, line: Int, column: Int)

case class ResourceCheckResult(status: Boolean, issues: List[Entry])

@Singleton
class ValidationController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  implicit val vQueryReads: Reads[ValidationQuery] = Json.reads[ValidationQuery]
  implicit val vQueryWrites: Writes[ValidationQuery] = Json.writes[ValidationQuery]
  implicit val entryWrites: Writes[Entry] = Json.writes[Entry]
  implicit val resCheckWrites: Writes[ResourceCheckResult] = Json.writes[ResourceCheckResult]
  implicit val valResultWrites: Writes[ValidationResult] = Json.writes[ValidationResult]
  implicit val genderReads: Reads[controllers.ResourceType.Value] = Reads.enumNameReads(ResourceType)
  implicit val checkResReads: Reads[CheckResource] = Json.reads[CheckResource]

  val PROFIL_XSD = "https://raw.githubusercontent.com/Jungyubw/NIST_healthcare_hl7_v2_profile_schema/master/Schema/NIST%20Validation%20Schema/Profile.xsd"
  val VSLIB_XSD = "https://raw.githubusercontent.com/Jungyubw/NIST_healthcare_hl7_v2_profile_schema/master/Schema/NIST%20Validation%20Schema/ValueSets.xsd"
  val CONSTRAINTS_XSD = "https://raw.githubusercontent.com/Jungyubw/NIST_healthcare_hl7_v2_profile_schema/master/Schema/NIST%20Validation%20Schema/ConformanceContext.xsd"


  def index() = Action {
    Ok.sendResource("public/index.html", this.getClass.getClassLoader)
  }

  def loadVxuExample() = Action {
    Ok(Json.toJson(ValidationQuery(
      scala.io.Source.fromInputStream(getClass.getResourceAsStream("/example/profile.xml")).mkString,
      scala.io.Source.fromInputStream(getClass.getResourceAsStream("/example/constraints.xml")).mkString,
      scala.io.Source.fromInputStream(getClass.getResourceAsStream("/example/vsLib.xml")).mkString,
      scala.io.Source.fromInputStream(getClass.getResourceAsStream("/reference.conf")).mkString,
      "aa72383a-7b48-46e5-a74a-82e019591fe7",
      scala.io.Source.fromInputStream(getClass.getResourceAsStream("/example/message.hl7")).mkString)))
  }

  def validateXML(xmlFile: String, xsdUrl: String, category: String): ResourceCheckResult = {
    val schemaLang = "http://www.w3.org/2001/XMLSchema"
    val factory = SchemaFactory.newInstance(schemaLang)
    val schema = factory.newSchema(new URL(xsdUrl))
    val validator = schema.newValidator()
    val issues: mutable.ArrayStack[Entry] = mutable.ArrayStack()
    validator.setErrorHandler(new ErrorHandler {
      override def warning(e: SAXParseException): Unit = {
        issues.push(Entry(category + " XML Validation", "Warning", e.getMessage, "", e.getLineNumber, e.getColumnNumber))
      }

      override def error(e: SAXParseException): Unit = {
        issues.push(Entry(category + " XML Validation", "Error", e.getMessage, "", e.getLineNumber, e.getColumnNumber))
      }

      override def fatalError(e: SAXParseException): Unit = {
        issues.push(Entry(category + " XML Validation", "Error", e.getMessage, "", e.getLineNumber, e.getColumnNumber))
      }
    })
    Try {
      validator.validate(new StreamSource(new StringReader(xmlFile)))
    } match {
      case Success(_) => ResourceCheckResult(issues.toList.isEmpty, issues.toList)
      case Failure(e: SAXParseException) => ResourceCheckResult(status = false, List {
        Entry(category + " XML Validation", "Error", e.getMessage, "", e.getLineNumber, e.getColumnNumber)
      })
      case Failure(e) => ResourceCheckResult(status = false, List {
        Entry(category + " XML Validation", "FatalError", e.getMessage, "", 0, 0)
      })
    }
  }

  def check(content: String, rType: ResourceType): ResourceCheckResult = {
    rType match {
      case Profile => validateXML(content, PROFIL_XSD, "Profile")
      case Constraints => validateXML(content, CONSTRAINTS_XSD, "Constraints")
      case VsLib => validateXML(content, VSLIB_XSD, "Value Set Library")
      case Configuration => Try {
        ConfigFactory.parseReader(new StringReader(content)).resolve()
      } match {
        case Success(_) => ResourceCheckResult(status = true, Nil)
        case Failure(ex: ConfigException) => ResourceCheckResult(status = false, List { Entry("Configuration Validation", "Error", ex.getMessage, "", ex.origin().lineNumber(), 0) })
        case Failure(ex) => ResourceCheckResult(status = false, List { Entry("Configuration Validation", "Error", ex.getMessage, "", 0, 0) })
      }
    }
  }

  def checkResource() = Action { implicit request =>
    request.body.asJson match {
      case None => BadRequest("No Argument Found")
      case Some(body) => {
        val query = body.validate[CheckResource].get
        Ok(Json.toJson(check(query.content, query.rType)))
      }
    }
  }

  def validateMessage(query: ValidationQuery):  Future[List[Entry]] = {
    val profile = XMLDeserializer.deserialize(new ByteArrayInputStream(query.profile.getBytes)) match {
      case Success(p) => p
      case Failure(e) => throw e
    }
    val vsLib = ValueSetLibraryImpl(new ByteArrayInputStream(query.vsLib.getBytes)).get
    val constraints = content.DefaultConformanceContext(new ByteArrayInputStream(query.constraints.getBytes)).get
    val validator = new HL7Validator(profile, vsLib, constraints, EmptyValueSetSpecification)


      validator.validate(query.message, query.id, new StringReader(query.configuration)) map {
        (result) =>
          result.getEntries.values()
            .stream()
            .flatMap(_.stream())
            .collect(Collectors.toList())
            .asScala.toList
            .map(v => Entry(v.getCategory(), v.getClassification(), v.getDescription(), v.getPath(), v.getLine(), v.getColumn()))
      } recover {
        case e => List { Entry("Message Validation Failure", "Error", e.getMessage, "", 0, 0) }
      }
  }

  def validation() = Action.async { implicit request =>
    request.body.asJson match {
      case None => Future {
        BadRequest("No Argument Found")
      }
      case Some(body) => {
        val query = body.validate[ValidationQuery].get

        val profileCheck = check(query.profile, Profile)
        val constraintsCheck = check(query.constraints, Constraints)
        val vsLibCheck = check(query.vsLib, VsLib)
        val configurationCheck = check(query.configuration, Configuration)

        val resourcesAreValid = !profileCheck.status || !constraintsCheck.status || !vsLibCheck.status || !configurationCheck.status
        val entries = if(resourcesAreValid) Future { List{ Entry("Resource Validation Failure", "Error", "One or more resources are invalid", "", 0, 0) } } else validateMessage(query)

        entries.map((list) => Ok(
          Json.toJson(ValidationResult(
            profileCheck,
            constraintsCheck,
            vsLibCheck,
            configurationCheck,
            list
          ))
        ))
      }
    }
  }

}