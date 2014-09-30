package hl7.v2.profile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import scala.xml.PrettyPrinter

object Main extends App {

  val xml = getClass.getResourceAsStream("/Profile.xml")
  XMLDeserializer.deserialize(xml).onComplete {
    case Success(s) => 
      val pp = new PrettyPrinter(200, 4)
      println( pp.format( XMLSerializer.serialize( s ) ) )
    case Failure(f) => println(f.getStackTraceString)
  }
}