package hl7.v2.validation

import hl7.v2.parser.impl.DefaultParser
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration._
import hl7.v2.instance._
import hl7.v2.profile.{SegmentRef, XMLDeserializer}
import gov.nist.validation.report.Entry;
import hl7.v2.validation.vs.ValueSetLibraryImpl
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import scala.collection.JavaConversions._
import collection.mutable._

object DebugMain extends App with DefaultParser {
  import sext._
 
//  val xml = getClass.getResourceAsStream("/debug/Profile.xml")
//  val ctx = getClass.getResourceAsStream("/debug/Constraints.xml")
//  val vs  = getClass.getResourceAsStream("/debug/ValueSets.xml")
//  
//  val profile = XMLDeserializer.deserialize( xml ) match {
//    case Success(p) => p
//    case Failure(e) => throw e
//  }
//  val confCtx = content.DefaultConformanceContext(ctx).get;
//  val vsL = ValueSetLibraryImpl(vs).get 
//
//  val msg = scala.io.Source.fromInputStream(getClass.getResourceAsStream("/debug/msg.er7")).mkString
////  val mp = profile.getMessage("57ab473284ae90ce1248081d");
//  
//  val message = parse(msg,mp).get
//  val elm = Query.query(message.asGroup, "15[3]").get
//  val x = elm.head.asInstanceOf[Segment];
//  print(x.model.ref.fields(4).treeString);
//  
//  val validator = new HL7Validator(profile, vsL, confCtx);
//  
//  val entries = validator.checkContent(message);
//  val report = Await.result(entries, 10.second).toList;
//  val imMap = collection.immutable.Map(serialize(orgReport(report)).toList : _*);
//  println(scala.util.parsing.json.JSONObject(imMap));
//  val future = validator.validate(msg, "57ab473284ae90ce1248081d");
//  val report = Await.result(future, 10.second);
//
//  println(report);

//  val cc = content.DefaultConformanceContext().get
//  val message = parse(msg,mm).get
//
//  implicit val s = message.separators
//  val ft = checkStructure(message)

//  println(scala.util.parsing.json.JSONObject(serialize(orgReport(entry))));
//  val rp = report.Report(entry,Nil,null)
//  //print(rp.toJson())
  
//  def printG(m : Group, i : Int) : Unit = {
//    val shift = "\t"*i;
//    m.children map {
//      x => x match {
//        case s : Segment => println(shift+s.location.prettyString+" "+s.location.uidPath)
//        case g : Group => println(shift+g.location.prettyString); printG(g, i+1)
//      }
//    }
//  }
  
  def orgReport(detections : List[Entry]) = {
    detections.foldLeft( Map[String, List[String]]() ){ (acc, d) =>
      acc.get(d.getCategory()) match {
        case Some(ls) => acc + (d.getCategory() -> (d.getDescription() + "-" + d.getLine() :: ls))
        case None => acc + (d.getCategory() -> (d.getDescription() + "-" + d.getLine() :: Nil))
      }
    }
  }
  
  def serialize( report : Map[String, List[String]]) = {
    report.foldLeft( Map[String, scala.util.parsing.json.JSONArray]() ){ (acc, e) =>
      acc + (e._1 -> scala.util.parsing.json.JSONArray(e._2))
    }
  }
}
