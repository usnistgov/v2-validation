package hl7.v2.profile.n

import java.io.InputStream

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
   * @param xsd - The profile XSD schema
   * @return The profile object or a failure
   */
  def deserialize(xml: InputStream, xsd: InputStream): Try[Profile] = ???


}
