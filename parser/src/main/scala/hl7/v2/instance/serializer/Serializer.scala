/*package hl7.v2.instance
package serializer

object Serializer {

  def toXML[A: XML]( value: A ): String = {
    val pp = new scala.xml.PrettyPrinter(500, 2)
    pp.format( implicitly[XML[A]].xml(value) )
  }
}
*/