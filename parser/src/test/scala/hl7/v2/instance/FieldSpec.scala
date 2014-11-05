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

  // Empty field test
  def e1 = Seq("", "&&", "  ", " & & ") map { v =>
    Field(v, ST, loc, pos, ins) === None and Field(v, HD, loc, pos, ins) === None
  }

  // Simple field test
  def e2 = Field("xx", ST, loc, pos, ins) === Some {
    SimpleField(ST.qProps, loc, pos, ins, Value( ST.id, "xx" ))
  }

  // Complex field test
  def e3 = Seq (
    "1^& &^3&" -> Set(1 -> ("1", 1), 3 -> ("3&", 7)),
    "1^^3^   " -> Set(1 -> ("1", 1), 3 -> ("3", 4))
  ) map { t =>
    Field(t._1, HD, loc, pos, ins) === Some { complexField(loc, t._2, HD, ins) }
  }

  // Complex field extra children test
  def e4 = Field("1^& &^3^4", HD, loc, pos, ins) === Some {
    complexField(loc, Set(1 -> ("1", 1), 3 -> ("3", 7), 4 -> ("4", 9)), HD, ins)
  }

  // Complex field null value test
  def e5 = Seq (
    "\"\""   -> Set(1 -> ("\"\"", 1), 2 -> ("\"\"", 1), 3 -> ("\"\"", 1)),
    "1^\"\"" -> Set(1 -> ("1", 1), 2 -> ("\"\"", 3)),
    "\"\"^2" -> Set(1 -> ("\"\"", 1), 2 -> ("2", 4))
  ) map { t =>
    Field(t._1, HD, loc, pos, ins) === Some( complexField(loc, t._2, HD, ins) )
  }

  /**
    * Creates and returns a complex field
    * @param l  - The location
    * @param v  - The set of children positions and their corresponding value and column
    * @param dt - The data type
    * @return A complex field
    */
  private def complexField(l: Location, v: Set[(Int, (String, Int))], dt: Composite, ins: Int) = {
    require( v.forall( _._1 > 0) )
    val hasExtra = v.maxBy( _._1 )._1 > dt.components.maxBy( _.req.position ).req.position
    // Positions for which a requirement has been defined
    val validPos = v.filter( t => dt.components.exists(_.req.position == t._1) )
    val children = (validPos map { t =>
      val(cpos, (cv, ccol)) = t
      val component = dt.components( cpos - 1 )
      val cloc = l.copy( desc=component.name, path=s"${l.path}.$cpos[1]", column = ccol )
      Component(cv, map( component.datatypeId ), cloc, cpos)
    }).flatten
    ComplexField( dt.qProps, l, pos, ins, children.toList, dt.requirements, hasExtra )
  }

}
