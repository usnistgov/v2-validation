package hl7.v2.profile

import java.io.ByteArrayInputStream

import org.specs2.Specification

import nist.xml.util.XOMDocumentBuilder

class XMLSerializerSpec extends Specification { def is = s2"""

  This the spec of XML Serializer and Deserializer

    Deserializing a valid XML should be successful           $e1
    Serializing a valid model should yield a valid XML       $e2

  """


  def e1 = {
    val xml = getClass.getResourceAsStream("/Profile.xml")
    val xsd = getClass.getResourceAsStream("/Profile.xsd")
    val deserialized = XMLDeserializer.deserialize(xml, xsd)
    deserialized must beSuccessfulTry
  }

  def e2 = {
    val xml = getClass.getResourceAsStream("/Profile.xml")
    val xsd = getClass.getResourceAsStream("/Profile.xsd")
    val deserialized = XMLDeserializer.deserialize(xml, xsd)
    assert( deserialized.isSuccess, "The deserialzed must be a success" )
    val serialized   = deserialized map ( XMLSerializer.serialize _ )
    assert( serialized.isSuccess, "The serialzed must be a success" )
    val result = {
      val stream = new ByteArrayInputStream( serialized.get.toString.getBytes("UTF-8"))
      XOMDocumentBuilder.build(stream, getClass.getResourceAsStream("/Profile.xsd"))
    }
    result must beSuccessfulTry
  }
}