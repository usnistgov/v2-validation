package hl7.v2.instance

import hl7.v2.parser.impl.DefaultParser
import hl7.v2.profile.XMLDeserializer
import org.specs2.{ScalaCheck, Specification}
import hl7.v2.instance.Query._

class ElementPathSpec extends Specification with ScalaCheck with DefaultParser {
  def is = s2"""
    Segment at position N in the message (no group) should have valid path values for each repetition $sft
    Group at position N in the message (one level) should have valid path values for each repetition $pt
    Segment at position N in a group should have valid path values for each repetition of the group $pid
    Segment at position N in a group should have valid path values for each repetition of the segment $uac
  """
  val profile = {
    val xml = getClass.getResourceAsStream("/ORU_R01_Profile.xml")
    val r = XMLDeserializer.deserialize(xml)
    assert(r.isSuccess, "[Error] An error occurred while creating the profile.")
    r.get
  }
  val messageProfile = profile.messages.getOrElse("ORU_R01", throw new Error("Unable to find the message model"))

  val message =
    """/MSH|^~\&#
       /SFT|A
       /SFT|B
       /SFT|C
       /PID|11||~^^^&3.4.2
       /UAC
       /UAC
       /PID|11||~^^^&3.4.2
       /UAC
       /UAC""".stripMargin('/')

  val parsed = parse(message, messageProfile).getOrElse(throw new Error("Unable to parse message"))


  def sft1 = checkElement(
    "2[1]", "SFT", "SFT[1]", "SFT[1]", "2[1]", "SFT", "2"
  )
  def sft2 = checkElement(
    "2[2]", "SFT", "SFT[2]", "SFT[2]", "2[2]", "SFT", "2"
  )
  def sft3 = checkElement(
    "2[3]", "SFT", "SFT[3]", "SFT[3]", "2[3]", "SFT", "2"
  )
  def sft = sft1 and sft2 and sft3
  def pt1 = checkElement(
    "5[1]", "PATIENT", "PATIENT[1]", "PATIENT[1]", "5[1]", "PATIENT", "5"
  )
  def pt2 = checkElement(
    "5[2]", "PATIENT", "PATIENT[2]", "PATIENT[2]", "5[2]", "PATIENT", "5"
  )
  def pt = pt1 and pt2
  def pid1 = checkElement(
    "5[1].1[1].1[1]", "PID-1", "PID[1]-1[1]", "PATIENT[1].PID[1]-1[1]", "5[1].1[1].1[1]", "PATIENT.PID-1", "5.1.1"
  )
  def pid2 = checkElement(
    "5[2].1[1].1[1]", "PID-1", "PID[2]-1[1]", "PATIENT[2].PID[1]-1[1]", "5[2].1[1].1[1]", "PATIENT.PID-1", "5.1.1"
  )
  def pid = pid1 and pid2
  def uac11 = checkElement(
    "5[1].2[1]", "UAC", "UAC[1]", "PATIENT[1].UAC[1]", "5[1].2[1]", "PATIENT.UAC", "5.2"
  )
  def uac12 = checkElement(
    "5[1].2[2]", "UAC", "UAC[2]", "PATIENT[1].UAC[2]", "5[1].2[2]", "PATIENT.UAC", "5.2"
  )
  def uac21 = checkElement(
    "5[2].2[1]", "UAC", "UAC[3]", "PATIENT[2].UAC[1]", "5[2].2[1]", "PATIENT.UAC", "5.2"
  )
  def uac22 = checkElement(
    "5[2].2[2]", "UAC", "UAC[4]", "PATIENT[2].UAC[2]", "5[2].2[2]", "PATIENT.UAC", "5.2"
  )
  def uac = uac11 and uac12 and uac22 and uac21

  def checkElement(
                    q: String,
                    path: String,
                    uidPath: String,
                    messageInstancePathName: String,
                    messageInstancePositionalPath: String,
                    messageProfilePath: String,
                    messageProfilePositionalPath: String
  ) = {
    val elements = query(parsed.asGroup, q).getOrElse(throw new Error("Error querying path " + q))
    val element = if(elements.size != 1) throw new Error("Error querying path " + q + " returned " + elements.size + " elements") else elements.head
    val location = element.location
    location.path must beEqualTo(path)
    location.uidPath must beEqualTo(uidPath)
    location.instancePath must beEqualTo(messageInstancePathName)
    location.instancePositionPath must beEqualTo(messageInstancePositionalPath)
    location.profilePath must beEqualTo(messageProfilePath)
    location.profilePositionPath must beEqualTo(messageProfilePositionalPath)
  }
}
