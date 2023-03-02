package hl7.v2.profile

import gov.nist.hit.hl7.v2.schemas.utils.{HL7v2Schema, HL7v2SchemaResourceResolver}

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
    * Create a profile from XML
    * @param xml - The XML file
    * @return A future containing the profile object or a failure
    */
  def deserialize( xml: InputStream ): Try[Profile] = {
    XOMDocumentBuilder.build(xml, HL7v2Schema.getProfile) map { doc => profile( doc.getRootElement ) }
  }
    
}
