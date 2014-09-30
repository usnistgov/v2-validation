package hl7.v2.profile

import java.io.InputStream

import hl7.v2.profile.XMLDeserializerHelper.profile
import nist.xml.util.XOMDocumentBuilder

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * Module to deserialize an HL7 profile from XML
  *
  * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
  */

object XMLDeserializer {

  /**
    * Returns the profile XSD schema as InputStream
    */
  def xsd: InputStream = getClass.getResourceAsStream("/Profile.xsd")

  /**
    * Create a profile from XML
    * @param xml - The XML file
    * @return A future containing the profile object or a failure
    */
  def deserialize( xml: InputStream ): Future[Profile] =
    XOMDocumentBuilder.build(xml, xsd) match {
      case Success(doc) => profile( doc.getRootElement )
      case Failure(e)   => Future.failed(e)
    }
}
