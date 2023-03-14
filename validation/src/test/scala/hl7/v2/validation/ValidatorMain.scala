package hl7.v2.validation

import hl7.v2.parser.impl.DefaultParser
import hl7.v2.profile.XMLDeserializer
import hl7.v2.validation.vs.{EmptyValueSetSpecification, ValueSetLibraryImpl}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object Main extends App with DefaultParser with structure.DefaultValidator {

  def time[R](block: => R): R = {
    val t0 = System.nanoTime()
    val result = block    // call-by-name
    val t1 = System.nanoTime()
    println("Elapsed time: " + (t1 - t0) + "ns")
    result
  }

  val xml = getClass.getResourceAsStream("/Profile.xml")

  val profile = XMLDeserializer.deserialize( xml ) match {
    case Success(p) => p
    case Failure(e) => throw e
  }

  val mm = profile.messages("ORU_R01")

  val m =
    """
      /asasa
      /MSH|^~\&#|^XXX^ISO^qq|NIST Lab Facility^2.16.840.1.113883.3.72.5.21^ISO||NIST EHR Facility^2.16.840.1.113883.3.72.5.23^ISO|20110531140551-2400|||NIST-LRI-GU-001.00|T|2.5.1|||AL|NE|||||LRI_Common_Component^Profile Component^2.16.840.1.113883.9.16^ISO~LRI_GU_Component^Profile Component^2.16.840.1.113883.9.12^ISO~LRI_RU_Component^Profile Component^2.16.840.1.113883.9.14^ISO
      /SFT
      /PID|11111~2~3~1~""~4||PATID1234^^^NIST MPI&2.16.840.1.113883.3.72.5.30.2&ISO^MR||Jones^William^A^JR^^^L||19610615|M||2106-3^White^HL70005^CAUC^Caucasian^L
      /ORC|1|ORD723222^NIST EHR^2.16.840.1.113883.3.72.5.24^ISO|R-783274^NIST Lab Filler^2.16.840.1.113883.3.72.5.25^ISO|GORD874211^NIST EHR^2.16.840.1.113883.3.72.5.24^ISO||||||||57422^Radon^Nicholas^M^JR^DR^^^NIST-AA-1&2.16.840.1.113883.3.72.5.30.1&ISO^L^^^NPI
      /ORC!
      /OBR|1|ORD723222^NIST EHR^2.16.840.1.113883.3.72.5.24^ISO|R-783274^NIST Lab Filler^2.16.840.1.113883.3.72.5.25^ISO|30341-2^Erythrocyte sedimentation rate^LN^815115^Erythrocyte sedimentation rate^99USI^^^Erythrocyte sedimentation rate|||20110331140551-0800||||L||7520000^fever of unknown origin^SCT^22546000^fever, origin unknown^99USI^^^Fever of unknown origin|||57422^Radon^Nicholas^M^JR^DR^^^NIST-AA-1&2.16.840.1.113883.3.72.5.30.1&ISO^L^^^NPI||||||20110331160428-0800|||F|||10092^Hamlin^Pafford^M^Sr.^Dr.^^^NIST-AA-1&2.16.840.1.113883.3.72.5.30.1&ISO^L^^^NPI|||||||||||||||||||||CC^Carbon Copy^HL70507^C^Send Copy^L^^^Copied Requested
      /NTE|1||Patient is extremely anxious about needles used for drawing blood.
      /NTE|2||Patient is extremely anxious about needles used for drawing blood.
      /NTE|3||Patient is extremely anxious about needles used for drawing blood.
      /TQ1|1||||||20110331150028-0800|20110331152028-0800
      /OBX|1|NM|1^Erythrocyte sedimentation rate^xx^815117^ESR^99USI^^^Erythrocyte sedimentation rate||10|mm/h^millimeter per hour^UCUM|0 to 17|N|||F|||20110331140551-0800|||||20110331150551-0800||||Century Hospital^^^^^NIST-AA-1&2.16.840.1.113883.3.72.5.30.1&ISO^XX^^^987|2070 Test Park^^Los Angeles^CA^90067^USA^B^^06037|2343242^Knowsalot^Phil^J.^III^Dr.^^^NIST-AA-1&2.16.840.1.113883.3.72.5.30.1&ISO^L^^^DN
      /OBX|2|CWE|XX^Erythrocyte sedimentation rate^xx^815117^ESR^99USI^^^Erythrocyte sedimentation rate||1^^CodeSyss|mm/h^millimeter per hour^UCUM|0 to 17|N|||F|||20110331140551-0800|||||20110331150551-0800||||Century Hospital^^^^^NIST-AA-1&2.16.840.1.113883.3.72.5.30.1&ISO^XX^^^987|2070 Test Park^^Los Angeles^CA^90067^USA^B^^06037|2343242^Knowsalot^Phil^J.^III^Dr.^^^NIST-AA-1&2.16.840.1.113883.3.72.5.30.1&ISO^L^^^DN
      /PDQ
      /""".stripMargin('/')

  val context1 = getClass.getResourceAsStream("/rules/CContext.xml")

  val context2 = getClass.getResourceAsStream("/rules/ConfContextSample.xml")

  val conformanceContext = content.DefaultConformanceContext().get

  val vsLibStream = getClass.getResourceAsStream("/ValueSets.xml")
  val valueSetLibrary = ValueSetLibraryImpl(vsLibStream).get

  val validator = new HL7Validator(profile, valueSetLibrary, conformanceContext, EmptyValueSetSpecification)

  1 to 1 foreach { i =>
    time {
      validator.validate( m, "ORU_R01" ) onComplete {
        case Success( report ) =>
          println( report.toText )
          println( s"\n\n ${ report.toJson } \n\n" )
        case Failure( e )      =>
          println(s"\n\n[Error] An error occurred while validating the message ... \n\t${e.getMessage}")
      }
    }
  }

  /*import scala.concurrent.duration._

  1 to 200 foreach { i =>
    time {
      val r = Await.result( validator.validate( m, "ORU_R01" ), 1.second )
      r.toJson
    }
  }*/
}
