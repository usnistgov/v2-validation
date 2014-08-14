package hl7.v2.profile

import scala.util.Success
import scala.util.Failure
import scala.xml.PrettyPrinter

object Main extends App {

  val xml = getClass.getResourceAsStream("/Profile.xml")
  val xsd = getClass.getResourceAsStream("/Profile.xsd")
  XMLDeserializer.deserialize(xml, xsd) match {
    case Success(s) => 
      val pp = new PrettyPrinter(200, 4)
      println( pp.format( XMLSerializer.serialize( s ) ) )
    case Failure(f) => println(f.getStackTraceString)
  }
}