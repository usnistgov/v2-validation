package controllers

import java.io.{ByteArrayInputStream, StringReader}
import com.typesafe.config.{ConfigException, ConfigFactory}
import hl7.v2.profile.XMLDeserializer
import hl7.v2.validation.{FeatureFlags, HL7Validator, ValidationContext, content}
import hl7.v2.validation.vs.{EmptyValueSetSpecification, ValueSetLibraryImpl}

import javax.inject._
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.Schema
import org.xml.sax.ErrorHandler
import play.api.mvc._
import play.api.libs.json.{Json, Reads, Writes}
import controllers.ResourceType.{Constraints, Profile, VsLib}
import gov.nist.hit.hl7.v2.schemas.utils.HL7v2Schema
import hl7.v2.parser.impl.DefaultParser
import hl7.v2.validation.coconstraints.EmptyCoConstraintValidationContext
import hl7.v2.validation.slicing.EmptyProfileSlicingContext
import hl7.v2.validation.vs.external.client.ExternalValueSetClient
import hl7.v2.validation.vs.factory.impl.java.{DefaultValueSetFactory, DefaultValueSetFactoryConfiguration}
import org.apache.http.{HttpRequest, HttpRequestInterceptor}
import org.apache.http.conn.ssl.{NoopHostnameVerifier, SSLConnectionSocketFactory, TrustAllStrategy}
import org.apache.http.impl.client.{CloseableHttpClient, HttpClients}
import org.apache.http.ssl.SSLContextBuilder
import service.FormatParsedMessage
import org.apache.http.client.config.RequestConfig
import org.apache.http.protocol.HttpContext

import scala.util.{Failure, Success, Try}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.collection.mutable
import scala.xml.SAXParseException
import scala.jdk.CollectionConverters.CollectionHasAsScala


object ResourceType extends Enumeration {
  type ResourceType = Value
  val Profile, Constraints, VsLib, ValueSetSpec, CoConstraints, Slicing, Configuration = Value
}

import ResourceType._

case class ValidationQuery(
  profile: String,
  constraints: String,
  vsLib: String,
  vsSpec: String,
  coConstraints: String,
  slicing: String,
  configuration: String,
  id: String,
  message: String,
  keys: Map[String, String],
)

case class CheckResource(content: String, rType: ResourceType)

case class ValidationResult(
  profile: ResourceCheckResult,
  constraints: ResourceCheckResult,
  vsLib: ResourceCheckResult,
  vsSpec: ResourceCheckResult,
  coConstraints: ResourceCheckResult,
  slicing: ResourceCheckResult,
  configuration: ResourceCheckResult,
  message: List[Entry]
)

case class ParseQuery(
  profile: String,
  message: String,
  id: String,
)

case class Entry(category: String, classification: String, description: String, path: String, ref: Option[ElementPath], line: Int, column: Int)
case class ElementPath(profileNamePath: String, instanceNamePath: String, profilePositionPath: String, instancePositionPath: String)

case class ResourceCheckResult(status: Boolean, issues: List[Entry])

@Singleton
class ValidationController @Inject()(cc: ControllerComponents) extends AbstractController(cc) with DefaultParser {

  implicit val vQueryReads: Reads[ValidationQuery] = Json.reads[ValidationQuery]
  implicit val vQueryWrites: Writes[ValidationQuery] = Json.writes[ValidationQuery]
  implicit val pathWrites: Writes[ElementPath] = Json.writes[ElementPath]
  implicit val entryWrites: Writes[Entry] = Json.writes[Entry]
  implicit val resCheckWrites: Writes[ResourceCheckResult] = Json.writes[ResourceCheckResult]
  implicit val valResultWrites: Writes[ValidationResult] = Json.writes[ValidationResult]
  implicit val genderReads: Reads[controllers.ResourceType.Value] = Reads.enumNameReads(ResourceType)
  implicit val checkResReads: Reads[CheckResource] = Json.reads[CheckResource]
  implicit val parseReads: Reads[ParseQuery] = Json.reads[ParseQuery]
  implicit val parseWrites: Writes[ParseQuery] = Json.writes[ParseQuery]


  def index(ignored: String) = Action {
    Ok.sendResource("public/index.html", this.getClass.getClassLoader)
  }

  def loadVxuExample() = Action {
    Ok(
      Json.toJson(
        ValidationQuery(
          scala.io.Source.fromInputStream(getClass.getResourceAsStream("/example/profile.xml")).mkString,
          scala.io.Source.fromInputStream(getClass.getResourceAsStream("/example/constraints.xml")).mkString,
          scala.io.Source.fromInputStream(getClass.getResourceAsStream("/example/vsLib.xml")).mkString,
          scala.io.Source.fromInputStream(getClass.getResourceAsStream("/example/vsBindings.xml")).mkString,
          scala.io.Source.fromInputStream(getClass.getResourceAsStream("/example/coconstraints.xml")).mkString,
          scala.io.Source.fromInputStream(getClass.getResourceAsStream("/example/slicing.xml")).mkString,
          scala.io.Source.fromInputStream(getClass.getResourceAsStream("/reference.conf")).mkString,
          "aa72383a-7b48-46e5-a74a-82e019591fe7",
          scala.io.Source.fromInputStream(getClass.getResourceAsStream("/example/message.hl7")).mkString,
          Map(),
        )
      )
    )
  }

  private def validateXML(xmlFile: String, schema: Schema, category: String): ResourceCheckResult = {
    val validator = schema.newValidator()
    val issues: mutable.Stack[Entry] = mutable.Stack()
    validator.setErrorHandler(
      new ErrorHandler {
        override def warning(e: SAXParseException): Unit = {
          issues.push(
            Entry(
              category + " XML Validation",
              "Warning",
              e.getMessage,
              "",
              None,
              e.getLineNumber,
              e.getColumnNumber
            )
          )
        }

        override def error(e: SAXParseException): Unit = {
          issues.push(
            Entry(
              category + " XML Validation",
              "Error",
              e.getMessage,
              "",
              None,
              e.getLineNumber,
              e.getColumnNumber
            )
          )
        }

        override def fatalError(e: SAXParseException): Unit = {
          issues.push(
            Entry(
              category + " XML Validation",
              "Error",
              e.getMessage,
              "",
              None,
              e.getLineNumber,
              e.getColumnNumber
            )
          )
        }
      }
    )
    Try {
      validator.validate(new StreamSource(new StringReader(xmlFile)))
    } match {
      case Success(_) => ResourceCheckResult(issues.toList.isEmpty, issues.toList)
      case Failure(e: SAXParseException) => ResourceCheckResult(
        status = false, List {
          Entry(category + " XML Validation", "Error", e.getMessage, "", null, e.getLineNumber, e.getColumnNumber)
        }
      )
      case Failure(e) => ResourceCheckResult(
        status = false, List {
          Entry(category + " XML Validation", "FatalError", e.getMessage, "", null, 0, 0)
        }
      )
    }
  }

  def check(content: String, rType: ResourceType): ResourceCheckResult = {
    rType match {
      case Profile => validateXML(content, HL7v2Schema.getProfileSchema, "Profile")
      case Constraints => validateXML(content, HL7v2Schema.getConformanceContextSchema, "Constraints")
      case VsLib => validateXML(content, HL7v2Schema.getValueSetLibrarySchema, "Value Set Library")
      case ValueSetSpec => validateXML(content, HL7v2Schema.getValueSetBindingsSchema, "Value Set Bindings")
      case CoConstraints => validateXML(content, HL7v2Schema.getCoConstraintsSchema, "Co-Constraints")
      case Slicing => validateXML(content, HL7v2Schema.getSlicingSchema, "Slicing")
      case Configuration => Try {
        ConfigFactory.parseReader(new StringReader(content)).resolve()
      } match {
        case Success(_) => ResourceCheckResult(status = true, Nil)
        case Failure(ex: ConfigException) => ResourceCheckResult(
          status = false, List {
            Entry("Configuration Validation", "Error", ex.getMessage, "", None, ex.origin().lineNumber(), 0)
          }
        )
        case Failure(ex) => ResourceCheckResult(
          status = false, List {
            Entry("Configuration Validation", "Error", ex.getMessage, "", None, 0, 0)
          }
        )
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

  private def getValidationContext(query: ValidationQuery): ValidationContext = {
    val profile = XMLDeserializer.deserialize(new ByteArrayInputStream(query.profile.getBytes)) match {
      case Success(p) => p
      case Failure(e) => throw e
    }
    val vsLib = ValueSetLibraryImpl(new ByteArrayInputStream(query.vsLib.getBytes)).get
    val constraints = content.DefaultConformanceContext(new ByteArrayInputStream(query.constraints.getBytes)).get
    val vsSpec = if(query.vsSpec.isEmpty) EmptyValueSetSpecification else hl7.v2.validation.vs.DefaultValueSetSpecification(new ByteArrayInputStream(query.vsSpec.getBytes)).get
    val coConstraints = if(query.coConstraints.isEmpty) EmptyCoConstraintValidationContext else hl7.v2.validation.coconstraints.DefaultCoConstraintValidationContext(
      new ByteArrayInputStream(
        query.coConstraints.getBytes
      )
    ).get
    val slicing = if(query.slicing.isEmpty) EmptyProfileSlicingContext else hl7.v2.validation.slicing.DefaultProfileSlicingContext(
      new ByteArrayInputStream(
        query.slicing.getBytes
      ),
      profile
    ).get
    val vsFactory = new DefaultValueSetFactory(getHTTPClient(query.keys), vsLib, new DefaultValueSetFactoryConfiguration(true, true))
    ValidationContext(profile, vsFactory, constraints, vsSpec, coConstraints, slicing, FeatureFlags(vsLib.containsLegacy0396Codes()))
  }

  private def getHTTPClient(keys: Map[String, String]): CloseableHttpClient = {
      val builder = new SSLContextBuilder();
      builder.loadTrustMaterial(null, new TrustAllStrategy());
      val socketFactory = new SSLConnectionSocketFactory(builder.build(), NoopHostnameVerifier.INSTANCE)
      val requestConfig = RequestConfig.custom.setConnectionRequestTimeout(2 * 1000).setConnectTimeout(2 * 1000).setSocketTimeout(2 * 1000).build
      HttpClients
          .custom()
          .setDefaultRequestConfig(requestConfig)
          .disableCookieManagement()
          .setSSLSocketFactory(socketFactory)
          .addInterceptorFirst(new HttpRequestInterceptor {
            override def process(request: HttpRequest, context: HttpContext): Unit = {
              val vsBinding = context.getAttribute(ExternalValueSetClient.HTTP_CONTEXT_VS_BINDING_IDENTIFIER).toString
              request.addHeader("X-API-KEY", keys.getOrElse(context.getAttribute(ExternalValueSetClient.HTTP_CONTEXT_VS_BINDING_IDENTIFIER).toString, ""))
            }
          })
          .build();
  }

  private def getParsedTree(query: ParseQuery) = {
    val profile = XMLDeserializer.deserialize(new ByteArrayInputStream(query.profile.getBytes)) match {
      case Success(p) => p
      case Failure(e) => throw e
    }
    val messageParsed = parse(query.message, profile.getMessage(query.id)).get
    FormatParsedMessage.format(messageParsed, false)
  }


  def validateMessage(query: ValidationQuery): Future[List[Entry]] = {
    val context = getValidationContext(query)
    val validator = new HL7Validator(context)

    validator.validate(query.message, query.id, new StringReader(query.configuration)) map {
      (result) =>
        result.getEntries.values()
          .asScala
          .toList
          .flatMap(_.asScala)
          .map(
            v => Entry(
              v.getCategory,
              v.getClassification,
              v.getDescription,
              v.getPath,
              Some(ElementPath(
                v.getMessageProfilePath,
                v.getMessageInstancePathName,
                v.getMessageProfilePositionPath,
                v.getMessageInstancePositionPath
              )),
              v.getLine,
              v.getColumn
            )
          )
    } recover {
      case e => {
        e.printStackTrace();
        List {
          Entry("Message Validation Failure", "Error", e.getMessage, "", null, 0, 0)
        }
      }
    }
  }

  def checkValidationQueryAndDo[T](query: ValidationQuery, exec: (ValidationQuery, ValidationResult) => T): T = {
    val profileCheck = check(query.profile, Profile)
    val constraintsCheck = check(query.constraints, Constraints)
    val vsLibCheck = check(query.vsLib, VsLib)
    val vsSpecCheck = check(query.vsSpec, ValueSetSpec)
    val coConstraintCheck = check(query.coConstraints, CoConstraints)
    val slicingCheck = check(query.slicing, Slicing)
    val configurationCheck = check(query.configuration, Configuration)

    exec(
      query,
      ValidationResult(
        profileCheck,
        constraintsCheck,
        vsLibCheck,
        vsSpecCheck,
        coConstraintCheck,
        slicingCheck,
        configurationCheck,
        List()
      )
    )
  }


  def validation() = Action.async { implicit request =>
    request.body.asJson match {
      case None => Future {
        BadRequest("No Argument Found")
      }
      case Some(body) => {
        checkValidationQueryAndDo(
          body.validate[ValidationQuery].get,
          (query, validationResult) => {
            val resourcesAreValid =
              !validationResult.profile.status ||
              !validationResult.constraints.status ||
              !validationResult.vsLib.status ||
              !validationResult.configuration.status
            val entries = if (resourcesAreValid) Future {
              List {
                Entry("Resource Validation Failure", "Error", "One or more resources are invalid", "", null, 0, 0)
              }
            } else validateMessage(query)

            entries.map(
              (list) => Ok(
                Json.toJson(
                  validationResult.copy(message = list)
                )
              )
            )

          }
        )
      }
    }
  }

  def parseMessage() = Action { implicit request =>
    request.body.asJson match {
      case None => BadRequest("No Argument Found")
      case Some(body) => {
        val query = body.validate[ParseQuery].get
        val profileCheck = check(query.profile, Profile)
        if(profileCheck.status) Ok(Json.toJson(FormatParsedMessage.toJson(getParsedTree(query))))
        else BadRequest("Invalid Profile")
      }
    }
  }

}