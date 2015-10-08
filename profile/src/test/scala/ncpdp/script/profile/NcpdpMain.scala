package ncpdp.script.profile

import scala.util.{Failure, Success}
import scala.xml.PrettyPrinter

import hl7.v2.profile.XMLSerializer

object Main extends App {

  println("NCPDP Main: " + (args mkString ", "))

  val profilePath = if (args.length == 0) null else args(0)
	println("profilePath: " + profilePath)

  val xml = if (profilePath != null) getClass.getResourceAsStream("/"+profilePath) else getClass.getResourceAsStream("/newrx_profile_20150916-fv.xml")
  XMLDeserializer.deserialize(xml) match {
    case Success(s) => 
      val pp = new PrettyPrinter(200, 4)
      println( pp.format( XMLSerializer.serialize( s ) ) )
    case Failure(f) => println("Error Message:\n"+f)
  }

}