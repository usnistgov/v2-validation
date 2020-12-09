package hl7.v2.validation

import hl7.v2.profile.XMLDeserializer
import hl7.v2.validation.vs.{EmptyValueSetSpecification, ValueSetLibraryImpl}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object MainDebug extends App {

  def time[R](block: => R): R = {
    val t0 = System.nanoTime()
    val result = block    // call-by-name
    val t1 = System.nanoTime()
    println("Elapsed time: " + (t1 - t0) + "ns")
    result
  }

  override def main(args: Array[String]): Unit = {
    val _profile = getClass.getResourceAsStream("/tmp/Profile.xml")
    val profile = XMLDeserializer.deserialize( _profile ) match {
      case Success(p) => p
      case Failure(e) => throw e
    }

    val _vsLib = getClass.getResourceAsStream("/tmp/ValueSets.xml")
    val vsLib = ValueSetLibraryImpl(_vsLib).get

    val _constraints = getClass.getResourceAsStream("/tmp/Constraints.xml")
    val constraints = content.DefaultConformanceContext(_constraints).get

    val msg = scala.io.Source.fromInputStream(getClass.getResourceAsStream("/tmp/msg.er7")).mkString


    val validator = new HL7Validator(profile, vsLib, constraints, EmptyValueSetSpecification)

    1 to 1 foreach { i =>
      time {
        validator.validate(msg, "5e62985a08bb3a000648b90c" ) onComplete {
          case Success( report ) =>
            println( report.toText )
            println( s"\n\n ${ report.toJson } \n\n" )
          case Failure( e )      =>
            println(s"\n\n[Error] An error occurred while validating the message ... \n\t${e.getMessage}")
        }
      }
    }
  }


}
