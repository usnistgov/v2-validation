package hl7.v2.validation

import hl7.v2.parser.impl.DefaultParser

import scala.concurrent.duration._
import hl7.v2.instance._
import hl7.v2.profile.{XMLDeserializer}
import gov.nist.validation.report.Entry;
import hl7.v2.validation.vs.ValueSetLibraryImpl
import scala.concurrent.Await
import scala.util.{Failure, Success}
import scala.collection.JavaConversions._

object DebugMain extends App with DefaultParser {
  import sext._
 
  val xml = getClass.getResourceAsStream("/fidelity/profile.xml")
  val ctx = getClass.getResourceAsStream("/fidelity/constraint.xml")
  val vs  = getClass.getResourceAsStream("/fidelity/vs.xml")

  val profile = XMLDeserializer.deserialize( xml ) match {
    case Success(p) => p
    case Failure(e) => throw e
  }
  val confCtx = content.DefaultConformanceContext(ctx).get;
  val vsL = ValueSetLibraryImpl(vs).get

  val msg = scala.io.Source.fromInputStream(getClass.getResourceAsStream("/fidelity/msg.er7")).mkString
  val validator = new HL7Validator(profile, vsL, confCtx);
  val future = validator.validate(msg, "aa72383a-7b48-46e5-a74a-82e019591fe7");
  val report = Await.result(future, 10.second);


  println(scala.util.parsing.json.JSONObject(serialize(orgReport(report.getEntries.values().flatten.toList))));
  
  def printG(m : Group, i : Int) : Unit = {
    val shift = "\t"*i;
    m.children map {
      x => x match {
        case s : Segment => println(shift+s.location.prettyString+" "+s.location.uidPath)
        case g : Group => println(shift+g.location.prettyString); printG(g, i+1)
      }
    }
  }
  
  def orgReport(detections : List[Entry]) = {
    detections.foldLeft( Map[String, List[String]]() ){ (acc, d) =>
      acc.get(d.getCategory()) match {
        case Some(ls) => acc + (d.getCategory() -> (d.getDescription() + "-" + d.getLine() + "-" + d.getClassification() :: ls))
        case None => acc + (d.getCategory() -> (d.getDescription() + "-" + d.getLine() + "-" + d.getClassification() :: Nil))
      }
    }
  }
  
  def serialize( report : Map[String, List[String]]) = {
    report.foldLeft( Map[String, scala.util.parsing.json.JSONArray]() ){ (acc, e) =>
      acc + (e._1 -> scala.util.parsing.json.JSONArray(e._2))
    }
  }
}
