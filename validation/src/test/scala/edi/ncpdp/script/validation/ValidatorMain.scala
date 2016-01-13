package edi.ncpdp.script.validation

import edi.ncpdp.script.parser.impl.DefaultNCPDPParser
import ncpdp.script.profile.XMLDeserializer
import hl7.v2.validation.vs.ValueSetLibraryImpl

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import scala.io.Source


object Main extends App with DefaultNCPDPParser with hl7.v2.validation.structure.DefaultValidator {

  def time[R](block: => R): R = {
    val t0 = System.nanoTime()
    val result = block    // call-by-name
    val t1 = System.nanoTime()
    println("Elapsed time: " + (t1 - t0) + "ns")
    result
  }

  val newrxProfile = XMLDeserializer.deserialize( getClass.getResourceAsStream("/integration-test/rxchg_profile_20151113.xml") ) match {
    case Success(p) => p
    case Failure(e) => throw e
  }  

  val context0 = getClass.getResourceAsStream("/integration-test/empty_conformance_context.xml")
  val context1 = getClass.getResourceAsStream("/integration-test/Constraints.xml")
  val context2 = getClass.getResourceAsStream("/integration-test/constraints-lite.xml")
  val contextRxchg = getClass.getResourceAsStream("/integration-test/Constraints_rxchg.xml")

  val conformanceContext = hl7.v2.validation.content.DefaultConformanceContext(contextRxchg).get

  val newrxVsLibStream = getClass.getResourceAsStream("/integration-test/rxchg_valueset_20151112.xml")
  val newrxValueSetLibrary = ValueSetLibraryImpl(newrxVsLibStream).get

  val newrxMessage = Source.fromInputStream(getClass.getResourceAsStream("/integration-test/rxchg.txt")).mkString

  // SyncNCPDPValidator
  val validator = new SyncNCPDPValidator(newrxProfile, newrxValueSetLibrary, conformanceContext)

  1 to 1 foreach { i =>
    time {
      val rep = validator.check( newrxMessage, "RXCHG" )
      println( rep.toText )
      println( s"\n\n ${ rep.toJson } \n\n" )  
    }
  }


}
