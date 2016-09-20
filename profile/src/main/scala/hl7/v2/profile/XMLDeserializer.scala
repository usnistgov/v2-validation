package hl7.v2.profile

import java.io.InputStream

import hl7.v2.profile.XMLDeserializerHelper.profile
import nist.xml.util.XOMDocumentBuilder

import scala.util.Try

/**
  * Module to deserialize an HL7 profile from XML
  *
  * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
  */

object XMLDeserializer {

  /**
    * Returns the profile XSD schema as InputStream
    */
//  def xsd: InputStream = getClass.getResourceAsStream("/Profile.xsd")
  def xsd_name = "/Profile.xsd"

  /**
    * Create a profile from XML
    * @param xml - The XML file
    * @return A future containing the profile object or a failure
    */
  def deserialize( xml: InputStream ): Try[Profile] = {
    XOMDocumentBuilder.build(xml, getClass.getResourceAsStream("/Profile.xsd")) map { doc => profile( doc.getRootElement ) }
  }
    
}
