package hl7.v2.validation.structure

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.Failure
import scala.util.Success

import org.specs2.Specification

import hl7.v2.instance.Location
import hl7.v2.parser.impl.DefaultParser
import hl7.v2.profile.Range
import hl7.v2.profile.XMLDeserializer

/**
  * Integration test for the structure validation
  */

trait StructValidationSpec 
       extends Specification
       with Validator
       with DefaultParser 
     { def is = s2"""

  Structure validation specification

    The minimally populated message should pass the validation                         $e1
    The structure validation should correctly report usage errors                      $e2
    The structure validation should correctly report cardinality errors                $e3
    The structure validation should correctly report length errors                     $e4
    The structure validation should correctly report invalid lines                     $e5
    The structure validation should correctly report unexpected segments               $e6
  """

  //TODO: Implements invalid lines and unexpected segments

  val profile = {
    val xml = getClass.getResourceAsStream("/ORU_R01_Profile.xml")
    val xsd = getClass.getResourceAsStream("/Profile.xsd")
    val r = XMLDeserializer.deserialize(xml, xsd)
    assert( r.isSuccess, "[Error] An error occurred while creating the profile." )
    r.get
  }

  val mm = profile.messages.getOrElse("ORU_R01", throw new Error("Unable to find the message model") )

  // Minimally populated valid message
  val m1 = """/MSH|^~\&#
              /PID|11||~^^^&3.4.2
              /UAC
              /UAC""".stripMargin('/')
  def e1 = validate(m1) === Nil

  /*
   * Message with the following usage errors
   * [2, 1] SFT is W but Present
   * [3, 1] UAC is X but Present
   * [4, 1] PID.1 is R but is missing
   * [4, 6] PID.2 is W but present
   * [4, 9] PID.3[2].1 is X but present
   * [4, 1] PATIENT.2 (UAC) is R but is missing
   * [5, 1] ORDER is X but present
   */
  val m2 = """/MSH|^~\&#
              /SFT
              /UAC
              /PID||x|~1^^^&3.4.2
              /SFT""".stripMargin('/')
  def e2 = {
    val expected = W("SFT[1]", 2, 1) :: X("UAC[1]",3 , 1):: R("PID[1].1", 4,1)::
                   W("PID[1].2", 4, 6) :: X("PID[1].3[2].1", 4, 9):: R("PATIENT.2", 4, 1)::
                   X("ORDER", 5, 1):: Nil
    validate(m2) ===  expected 
  }

  /*
   * [7, 1] PATIENT cardinality (3) not in range [ 1..2 ]
   * [5,11] PID[1].3 (4) not in range [ 2..3 ]
   * [6, 1] UAC (1) not in range [ 2..2 ]
   */
  val m3 = """/MSH|^~\&#
              /PID|11||~^^^&3.4.2
              /UAC
              /UAC
              /PID|22||~~~^^^&3.4.2
              /UAC
              /PID|33||~~^^^&3.4.2
              /UAC
              /UAC""".stripMargin('/')
  def e3 = {
    val expected = MaxC("PATIENT", 7, 1, 3, Range(1, "2"))::MaxC("PID[1].3[4]", 5, 12, 4, Range(2, "3"))::
                   MinC("UAC[1]", 6, 1, 1, Range(2, "2")):: Nil
    validate(m3) ===  expected 
  }

  /*
   * [2, 5] PID[1].1 length (1) not in range [ 2..3 ]
   * [8, 5] PID[1].1 length (4) not in range [ 2..3 ]
   */
  val m4 = """/MSH|^~\&#
              /PID|1||~^^^&3.4.2
              /UAC
              /UAC
              /PID|3333||~^^^&3.4.2
              /UAC
              /UAC""".stripMargin('/')
  def e4 = {
    val expected = Len("PID[1].1", 2, 5, "1", Range(2, "3"))::Len("PID[1].1", 5, 5, "3333", Range(2, "3"))::Nil
    validate(m4) ===  expected 
  }

  /*
   * Invalid lines:
   *    (1, "sss")
   *    (4, "xzsas")
   */
  val m5 = """sss
              /MSH|^~\&#
              /PID|11||~^^^&3.4.2
              /xzsas
              /UAC
              /UAC""".stripMargin('/')
  def e5 = {
    validate(m5) === InvalidLines( (1, "sss") :: (4, "xzsas") :: Nil ) :: Nil
  }

  /*
   * Unexpected segments:
   *    (5, PDQ|1)
   */
  //FIXME improve test
  val m6 = """/MSH|^~\&#
              /PID|11||~^^^&3.4.2
              /UAC
              /UAC
              /PDQ|1""".stripMargin('/')
  def e6 = {
    validate(m6) === UnexpectedLines( (5, "PDQ|1") :: Nil ) :: Nil
  }

  private def validate(m: String): Seq[Entry] = parse(m, mm) match {
    case Success(msg) => Await.result( checkStructure(msg) , Duration(2, "seconds"))
    case Failure(e) => throw e
  }

  private def R(p: String, l: Int, c: Int) = RUsage( Location(p, l, c) )
  private def X(p: String, l: Int, c: Int) = XUsage( Location(p, l, c) )
  private def W(p: String, l: Int, c: Int) = WUsage( Location(p, l, c) )
  private def MaxC(p: String, l: Int, c: Int, i: Int, r: Range) = MaxCard(Location(p, l, c), i, r)
  private def MinC(p: String, l: Int, c: Int, i: Int, r: Range) = MinCard(Location(p, l, c), i, r)
  private def Len(p: String, l: Int, c: Int, v: String, r: Range) = Length(Location(p, l, c), v, r)
}
