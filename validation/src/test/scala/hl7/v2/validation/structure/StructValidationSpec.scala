package hl7.v2.validation.structure

import hl7.v2.instance.Location
import hl7.v2.parser.impl.DefaultParser
import hl7.v2.profile.{XMLDeserializer, Range}
import hl7.v2.validation.report.{InvalidLines, Length, MaxCard, MinCard, RUsage, SEntry, UnexpectedLines, WUsage, XUsage}
import org.specs2.Specification

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

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
    The structure validation should correctly report length errors                     $todo
    The structure validation should correctly report invalid lines                     $todo
    The structure validation should correctly report unexpected segments               $todo
  """

  //TODO: Implements invalid lines and unexpected segments

  val profile = {
    val xml = getClass.getResourceAsStream("/ORU_R01_Profile.xml")
    val r = XMLDeserializer.deserialize( xml )
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
   * [7, 1] ORDER is X but present
   */


  /*val m2 = """/MSH|^~\&#
              /SFT
              /UAC
              /PID||x|~1^^^&3.4.2
              /SFT""".stripMargin('/')*/

  val m2 = """/MSH|^~\&#
              /SFT
              /UAC
              /PID||x|~1^^^&3.4.2
              /UAC
              /UAC
              /SFT""".stripMargin('/')

  def e2 = {
    val expected =
      List(
          W("Software Segment", "SFT[1]", 2, 1),
          X("User Authentication Credential Segment", "UAC[1]",3 , 1),
          R("...", "PID[1].1[1]", 4,1),
          W("Patient ID", "PID[1].2[1]", 4, 6),
          X("ID Number", "PID[1].3[2].1[1]", 4, 9),
          X("Group ORDER", "ORDER[1]", 7, 1)
      )

    validate(m2) must containTheSameElementsAs( expected )
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
    val expected = MaxC("Group PATIENT", "PATIENT[3]", 7, 1, 3, Range(1, "2")) ::
                   MaxC("Patient Identifier List", "PID[1].3[4]", 5, 12, 4, Range(2, "3")) ::
                   MinC("User Authentication Credential Segment","UAC[1]", 6, 1, 1, Range(2, "2")) :: Nil
    validate(m3) must containTheSameElementsAs( expected )
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
  /*def e4 = {
    val expected = Len("PID[1].1", 2, 5, "1", Range(2, "3"))::Len("PID[1].1", 5, 5, "3333", Range(2, "3"))::Nil
    validate(m4) ===  expected 
  }*/

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

  private def validate(m: String): Seq[SEntry] = parse(m, mm) match {
    case Success(msg) => Await.result( checkStructure(msg) , Duration(2, "seconds"))
    case Failure(e) => throw e
  }

  private def R(d: String, p: String, l: Int, c: Int) = RUsage( Location(d, p, l, c) )
  private def X(d: String, p: String, l: Int, c: Int) = XUsage( Location(d, p, l, c) )
  private def W(d: String, p: String, l: Int, c: Int) = WUsage( Location(d, p, l, c) )

  private def MaxC(d: String, p: String, l: Int, c: Int, i: Int, r: Range) =
    MaxCard(Location(d, p, l, c), i, r)

  private def MinC(d: String, p: String, l: Int, c: Int, i: Int, r: Range) =
    MinCard(Location(d, p, l, c), i, r)

  private def Len(d: String, p: String, l: Int, c: Int, v: String, r: Range) =
    Length(Location(d, p, l, c), v, r)

}
