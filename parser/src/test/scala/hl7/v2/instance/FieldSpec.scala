package hl7.v2.instance

import hl7.v2.profile.Composite
import org.specs2.Specification

class FieldSpec extends Specification with DataElementCreationHelper { def is = s2"""
  Field Specification

    Field creation should return None if the string value is empty            $e1
    Field creation should return a simple field if the data type is primitive $e2
    Field creation should return a complex field if the data type is complex  $e3
    Complex field creation should correctly handle extra children             $e4
    Field creation should correctly handle Null value                         $e5

"""

  def e1 = Seq("", "&&", "  ", " & & ") map { v =>
    Field(v, ST, loc, pos, ins) === None and Field(v, HD, loc, pos, ins) === None
  }

  def e2 = Field("xx", ST, loc, pos, ins) === Some {
    SimpleField(ST.qProps, loc, pos, ins, Value( ST.id, "xx" ))
  }

  def e3 = todo
  def e4 = todo
  def e5 = todo

  /**
   * Creates and returns a simple component
   * @param pl - The parent location
   * @param v  - The value as string
   * @param c  - The component model
   * @param col- The column
   * @return A simple component
   */
  private def simpleComp(pl: Location, v: String, c: hl7.v2.profile.Component, col: Int = 1) = {
    val dt  = map(c.datatypeId)
    val pos = c.req.position
    val loc = pl.copy( desc=c.name, path=s"${pl.path}.$pos[1]", column = col )
    SimpleComponent (dt.qProps, loc, pos, Value( dt.id, v ))
  }

  /**
   * Creates and returns a complex component
   * @param l - The location
   * @param v - The set of children positions and their corresponding value and column
   * @param c - The data type
   * @return A complex component
   */
  private def complexComp(l: Location, v: Set[(Int, (String, Int))], c: Composite) = {
    require( v.forall( _._1 > 0) )
    val hasExtra = v.maxBy( _._1 )._1 > c.components.maxBy( _.req.position ).req.position
    // Positions for which a requirement has been defined
    val validPos = v.filter( t => c.components.exists( _.req.position == t._1 ) )
    val children = validPos map { t =>
      simpleComp( l, t._2._1, c.components(t._1 - 1), t._2._2 )
    }
    ComplexComponent (c.qProps, l, pos, children.toList, c.requirements, hasExtra)
  }

}
