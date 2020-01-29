package hl7.v2.validation.structure

import gov.nist.validation.report.Entry
import hl7.v2.instance.{ EType, Location }
import hl7.v2.parser.impl.DefaultParser
import hl7.v2.profile.{ Req, Usage, Range, XMLDeserializer }
import hl7.v2.validation.report._
import hl7.v2.validation.vs.ValueSet
import org.specs2.Specification
import expression._
import hl7.v2.instance.Query._
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.{ Failure, Success }
import expression.EvalResult.{ Inconclusive, Fail, Pass }
import hl7.v2.validation.vs.EmptyValueSetLibrary
import hl7.v2.instance._
import hl7.v2.validation.structure.ValueValidation._
/**
 * Integration test for the structure validation
 */

trait NullValidationSpec
    extends Specification
    with Validator
    with DefaultParser
    with Helpers
    with DefaultEvaluator {
  def is = s2"""

  NULL validation specification

    A Null Field should be considered present             $e1
    A Null SimpleField should have "" value               $e2
    A Null ComplexField has no children                   $e5    
    An R Null Field should not raise a detection          $e3
    An X Null Field should raise a detection              $e4
    On Component and SubComponent level "" means TEXT("") $e7
    Only the Usage of a Null Field is checked             $e9       
    Cardinality                                           $e8 
        A Null Field should have one and only one instance
        Mixing Null and Valued Fields within repetitions isn't permitted    
    
  """

  implicit val defaultValueSetLibrary = EmptyValueSetLibrary.getInstance()
  implicit val separators = Separators('|', '^', '~', '\\', '&', Some('#'))
  implicit val dtz = Some(TimeZone("+0000"))

  val profile = {
    val xml = getClass.getResourceAsStream("/ORU_R01_Profile.xml")
    val r = XMLDeserializer.deserialize(xml)
    assert(r.isSuccess, "[Error] An error occurred while creating the profile.")
    r.get
  }

  val mm = profile.messages.getOrElse("ORU_R01",
    throw new Error("Unable to find the message model"))

  // A context to work with in Constraints
  val context = {
    val message = """/MSH|^~\&#
                   /PID|""||""
                   /UAC|""
                   /UAC""".stripMargin('/')
    val m = parse(message, mm)
    assert(m.isSuccess, "[Error] Cannot parse the message")
    m.get.asGroup
  }
  
  val context2 = {
    val message = """/MSH|^~\&#
               /PID|11||~^^""^&""
               /UAC
               /UAC""".stripMargin('/')
    val m = parse(message, mm)
    assert(m.isSuccess, "[Error] Cannot parse the message")
    m.get.asGroup
  }

  //A Null Field should be considered present 
  def e1 = e11 and e12

  //For SimpleField 
  def e11 = {
    val p = Presence("4[1].1[1].1[1]")
    eval(p, context) === Pass
  }

  //For ComplexField
  def e12 = {
    val p = Presence("4[1].1[1].3[1]")
    eval(p, context) === Pass
  }

  //A Null SimpleField should have "" value  
  def e2 = {
    eval(PlainText("4[1].1[1].1[1]", Value.NULL, true), context) === Pass
  }

  //An R Null Field should not raise a detection
  def e3 = {
    val message = """/MSH|^~\&#
               /PID|11||""
               /UAC
               /UAC""".stripMargin('/')

    validate(message) === Nil
  }

  //An X Null Field should raise a detection
  def e4 = {

    val message = """/MSH|^~\&#
               /PID|11||""
               /UAC|""
               /UAC""".stripMargin('/')

    val expected =
      List(
        X(EType.Field, "User Authentication Credential Type Code", "UAC-1", 3, 5, "UAC[1]-1[1]"))
    
    validate(message) must containTheSameElementsAs(expected)
  }

  //A Null ComplexField has no children
  def e5 = {
    //The parent is present
    val p = Presence("4[1].1[1].3[1]")
    assert(eval(p, context) === Pass)

    //Children not present
    val pp = Presence("4[1].1[1].3[1].1[1]")
    val a = eval(pp, context) === Failures.presence(context, pp)

    val ppp = Presence("4[1].1[1].3[1].2[1]")
    val b = eval(ppp, context) === Failures.presence(context, ppp)

    b and a
  }

  //On Component and SubComponent level "" means "" 
  def e7 = {
    val message = """/MSH|^~\&#
               /PID|11||~^^""^&""
               /UAC
               /UAC""".stripMargin('/')
    val comp = query(context2,"4[1].1[1].3[2].3[1]")
    val subcomp = query(context2,"4[1].1[1].3[2].4[1].2[1]")
    assert(comp.isSuccess)
    assert(subcomp.isSuccess)
    val expected =
      List(
        Detections.length(comp.get.head.location, comp.get.head.req.length.get, Value.NULL),
        Detections.length(subcomp.get.head.location, subcomp.get.head.req.length.get, Value.NULL))
        
    validate(message) must containTheSameElementsAs(expected)
  }

  //A Null Field should have should have only one instance, mixing Null and Valued Field isn't permitted
  def e8 = {
    val message1 = """/MSH|^~\&#
               /PID|11||""
               /UAC
               /UAC""".stripMargin('/')
               
    val message2 = """/MSH|^~\&#
               /PID|11||""~""
               /UAC
               /UAC""".stripMargin('/')
               
    val message3 = """/MSH|^~\&#
               /PID|11||^^^&3.4.2~""
               /UAC
               /UAC""".stripMargin('/')
               
    val message4 = """/MSH|^~\&#
               /PID|11||""~^^^&3.4.2
               /UAC
               /UAC""".stripMargin('/')
               
    val field = query(context2,"4[1].1[1].3[1]")
    assert(field.isSuccess)
    val expected =
      List(
        Detections.ncardinality(Location(EType.Field,"Patient Identifier List","PID-3",2,9,"PID[1]-3[1]"), 2))
        
    def e81 = {
      validate(message1) === Nil
    }
    def e82 = {
      validate(message2) must containTheSameElementsAs(expected)
    }
    def e83 = {
      validate(message3) must containTheSameElementsAs(expected)
    }
    def e84 = {
      validate(message4) must containTheSameElementsAs(expected)
    }
    e81 and e82 and e83 and e84
  }
  
  def e9 = {
    val loc = Location(EType.Field, "The description", "The path", 1, 1)
    val lcs = Some( Range(2, "3") )
    val lcn = None
    checkValue( Number("\"\""), lcs, None, loc) === Nil
  }
  private def validate(m: String): Seq[Entry] = parse(m, mm) match {
    case Success(msg) =>
      Await.result(checkStructure(msg), Duration(2, "seconds"))
    case Failure(e) => throw e
  }

  private def R(et: EType, d: String, p: String, l: Int, c: Int, uid : String) = Detections.rusage(Location(et, d, p, l, c))
  private def X(et: EType, d: String, p: String, l: Int, c: Int, uid : String) = Detections.xusage(Location(et, d, p, l, c,uid))
  private def W(et: EType, d: String, p: String, l: Int, c: Int, uid : String) = Detections.wusage(Location(et, d, p, l, c))

  private def MaxC(et: EType, d: String, p: String, l: Int, c: Int, i: Int, r: Range, uid : String) =
    Detections.cardinality(Location(et, d, p, l, c, uid), r, i)

  private def MinC(et: EType, d: String, p: String, l: Int, c: Int, i: Int, r: Range, uid : String) =
    Detections.cardinality(Location(et, d, p, l, c, uid), r, i)

  private def Len(et: EType, d: String, p: String, l: Int, c: Int, v: String, r: Range, uid : String) =
    Detections.length(Location(et, d, p, l, c, uid), r, v)
  
  private def NullCard(et: EType, d: String, p: String, l: Int, c: Int, i: Int, uid : String) =
    Detections.ncardinality(Location(et, d, p, l, c, uid), i)

}
