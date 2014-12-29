package hl7.v2.validation.structure

import hl7.v2.instance.{Line, Location}
import hl7.v2.parser.impl.DefaultParser
import hl7.v2.profile.{Range, XMLDeserializer}
import hl7.v2.validation.report._
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

    The minimally populated message should pass the validation             $e1
    The structure validation should correctly report usage errors          $e2
    The structure validation should correctly report cardinality errors    $e3
    The structure validation should correctly report length errors         $e4
    The structure validation should correctly report invalid lines         $e5
    The structure validation should correctly report unexpected segments   $e6
    The structure validation should correctly report extra elements        $e7
    The structure validation should correctly report separators in a value $e8
  """

  //TODO: Implements invalid lines and unexpected segments

  val profile = {
    val xml = getClass.getResourceAsStream("/ORU_R01_Profile.xml")
    val r = XMLDeserializer.deserialize( xml )
    assert(r.isSuccess, "[Error] An error occurred while creating the profile.")
    r.get
  }

  val mm = profile.messages.getOrElse("ORU_R01",
                           throw new Error("Unable to find the message model") )

  /**
    * Valid message test
    */
  def e1 = {
    // Minimally populated valid message
    val m = """/MSH|^~\&#
               /PID|11||~^^^&3.4.2
               /UAC
               /UAC""".stripMargin('/')

    validate(m) === Nil
  }


  /**
    * Usage test
    */
  def e2 = {
    /*
     * [2, 1] SFT is W but Present
     * [3, 1] UAC is X but Present
     * [4, 1] PID.1 is R but is missing
     * [4, 6] PID.2 is W but present
     * [4, 9] PID.3[2].1 is X but present
     * [7, 1] ORDER is X but present
     */
    val m = """/MSH|^~\&#
               /SFT
               /UAC
               /PID||x|~1^^^&3.4.2
               /UAC
               /UAC
               /SFT""".stripMargin('/')
    val expected =
      List(
          W("Software Segment", "SFT[1]", 2, 1),
          X("User Authentication Credential Segment", "UAC[1]",3 , 1),
          R("...", "PID[1].1[1]", 4,1),
          W("Patient ID", "PID[1].2[1]", 4, 6),
          X("ID Number", "PID[1].3[2].1[1]", 4, 9),
          X("Group ORDER", "ORDER[1]", 7, 1)
      )

    validate(m) must containTheSameElementsAs( expected )
  }

  /**
    * Cardinality test
    */
  def e3 = {
    /*
     * [7, 1] PATIENT[3] cardinality (3) not in range [ 1..2 ]
     * [5,11] PID[1].3 cardinality (4) not in range [ 2..3 ]
     * [6, 1] UAC cardinality (1) not in range [ 2..2 ]
     */
    val m = """/MSH|^~\&#
               /PID|11||~^^^&3.4.2
               /UAC
               /UAC
               /PID|22||~~~^^^&3.4.2
               /UAC
               /PID|33||~~^^^&3.4.2
               /UAC
               /UAC""".stripMargin('/')
    val expected =
      List(
        MaxC("Group PATIENT", "PATIENT[3]", 7, 1, 3, Range(1, "2")),
        MaxC("Patient Identifier List", "PID[1].3[4]", 5, 12, 4, Range(2, "3")),
        MinC("User Authentication Credential Segment","UAC[1]",6,1,1,Range(2,"2"))
      )

    validate(m) must containTheSameElementsAs( expected )
  }

  /**
    * Length test
    */
  def e4 = {
    /*
     * [2, 5] PID[1].1 length (1) not in range [ 2..3 ]
     * [5, 5] PID[1].1 length (4) not in range [ 2..3 ]
     */
    val m = """/MSH|^~\&#
               /PID|1||~^^^&3.4.2
               /UAC
               /UAC
               /PID|333\F\||~^^^&3.4.2
               /UAC
               /UAC""".stripMargin('/')
    val expected = List(
            Len("Set ID - PID", "PID[1].1[1]", 2, 5, "1", Range(2, "3")),
            Len("Set ID - PID", "PID[1].1[1]", 5, 5, "333|", Range(2, "3"))
    )

    validate(m) must containTheSameElementsAs( expected )
  }

  /**
    * Invalid lines test
    */
  def e5 = {
    /*
     *    (1, "sss")
     *    (4, "xzsas")
     *    (6, "PID!")
     */
    val m = """sss
              /MSH|^~\&#
              /PID|11||~^^^&3.4.2
              /xzsas
              /UAC
              /PID!
              /UAC""".stripMargin('/')
    val expected = List( InvalidLines( List( Line(1, "sss"), Line(4, "xzsas"), Line(6,"PID!")) ) )

    validate(m) must containTheSameElementsAs( expected )
  }

  /**
    * Unexpected lines test
    */
  def e6 = {
    /*
     * (5, PDQ|1)
     */
    //FIXME improve test
    val m = """/MSH|^~\&#
               /PID|11||~^^^&3.4.2
               /UAC
               /UAC
               /PDQ|1""".stripMargin('/')
    val expected = List( UnexpectedLines( Line(5, "PDQ|1") :: Nil ) )//UnexpectedLine(5, "PDQ|1") :: Nil

    validate(m) must containTheSameElementsAs( expected )
  }

  /**
    * Extra children test
    */
  def e7 = {
    /*
     * [2, 10] PID[1].3[2]
     * [2, 13] PID[1].3[2].4[1]
     */
    val m = """/MSH|^~\&#
               /PID|11||~^^^&3.4.2&&HD.4^CX.5
               /UAC
               /UAC""".stripMargin('/')
    val expected =
      List(
        Extra( Location("Patient Identifier List", "PID[1].3[2]", 2, 10) ),
        Extra( Location("Assigning Authority", "PID[1].3[2].4[1]", 2, 13) )
      )

    validate(m) must containTheSameElementsAs( expected )
  }

  /**
    * Unescaped separators in value test
    */
  def e8 = {
    /*
     * [2, 5] PID[1].1
     */
    val m = """/MSH|^~\&#
                /PID|1^&||~^^^&3.4.2
                /UAC
                /UAC""".stripMargin('/')
    val expected =
      List(
        UnescapedSeparators( Location("Set ID - PID", "PID[1].1[1]", 2, 5) )
      )

    validate(m) must containTheSameElementsAs( expected )
  }

  private def validate(m: String): Seq[SEntry] = parse(m, mm) match {
    case Success(msg) =>
      Await.result( checkStructure(msg) , Duration(2, "seconds"))
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
