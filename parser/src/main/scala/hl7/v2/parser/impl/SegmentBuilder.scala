/*package hl7.v2.parser.impl


import hl7.v2.instance.Field
import hl7.v2.old.Location
import hl7.v2.instance.Segment

/**
  * Segment builder
  * 
  * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
  */

object SegmentBuilder {

  /**
    * Creates and returns a segment instance from the string
    * @param model - The segment model
    * @param value - The string value
    * @param instance - The instance number
    * @param line - The line number
    * @return The segment instance
    */
  def apply(model: SegmentRef, value: String, instance: Int, line: Int ): Segment = {
    require( isValid( value ), s"Invalid segment instance '$value'" )
    val name = model.ref.name
    require( name == value.take(3), 
        s"Segment model and instance name mismatch. Expected: '$name', Found: '$value'" )
    val l   = Location(s"$name[$instance]", line, 1)
    val fml = model.ref.fields
    val vs  = split( fs, value drop 4 , 5)
    val _fields = if( value.startsWith("MSH") ) mshFields(fml, vs, l) else fields( fml, vs, l )
    Segment( model, _fields, instance, l )
  }

  def apply(model: SegmentRef, line: (Int, String), instance: Int): Segment =
    apply(model, line._2, instance, line._1)

  private def mshFields( fml: List[Field], vs: Array[(Int, String)], l: Location ) = {
    val separators = vs(0)._2
    require( separators matches( """\Q^\E~\Q\\E&(?:#.*)?""" ) , 
        s"Invalid HL7 separators. Expected '^~\\&' or '^~\\&#'. Found: '$separators'" )
    val _fields = fields(fml.tail.tail, vs drop 1 , l)
    val `MSH.1` = DataElementBuilder(fml.head, s"$fs", 1, location(l, 1, 4))
    val `MSH.2` = DataElementBuilder(fml.tail.head, s"$separators", 1, location(l, 2, 5))
    ( `MSH.1`.toList ) :: ( `MSH.2`.toList ) :: _fields
  }

  private def fields( fml: List[Field], vs: Array[(Int, String)], l: Location) =
    (fml zip pad(vs, (-1, ""), fml.size) ) map { t => repetitions( t._1, t._2, l ) }

  private def repetitions( m:Field, t: (Int, String), l: Location): List[Field] =
    ( split(rs, t._2, t._1).toList.zipWithIndex map { tt => 
      val( (col, v), i ) = tt
      DataElementBuilder( m, v, i + 1, location(l, m.position, col, i + 1) )
    }).flatten

  private val segFormat = s"[A-Z]{2}[A-Z0-9](?:\\Q$fs\\E.*)*".r

  private def isValid( s: String ) = segFormat.pattern.matcher( s ).matches
}
*/