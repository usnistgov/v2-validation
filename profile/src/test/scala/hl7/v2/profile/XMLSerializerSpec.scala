package hl7.v2.profile

import java.io.ByteArrayInputStream

import nist.xml.util.XOMDocumentBuilder
import org.specs2.Specification

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

class XMLSerializerSpec extends Specification { def is = s2"""

  This the spec of XML Serializer and Deserializer

    Deserializing a valid XML should be successful           $e1
    Serializing a valid model should yield a valid XML       $e2

  """

  def e1 = {
    val xml = getClass.getResourceAsStream("/Profile.xml")
    val p = profile( XMLDeserializer.deserialize(xml) )
    p mustNotEqual null
  }

  def e2 = {
    val xml = getClass.getResourceAsStream("/Profile.xml")

    val p = profile( XMLDeserializer.deserialize(xml) )
    p mustNotEqual null

    val serialized = XMLSerializer.serialize(p)

    val result = {
      val stream = new ByteArrayInputStream( serialized.toString.getBytes("UTF-8"))
      XOMDocumentBuilder.build(stream, getClass.getResourceAsStream("/Profile.xsd"))
    }
    result must beSuccessfulTry
  }

  private def profile(f: Future[Profile]) = Await.result(f, Duration(300, "millis"))
}