package hl7.v2.instance

import hl7.v2.profile._
import org.specs2.Specification

class ComponentSpec extends Specification with DataElementCreationHelper { def is = s2"""

  Component Specification

    Component creation should return None if the string value is empty                $e1
    Component creation should return a simple component if the data type is primitive $e2
    Component creation should return a complex component if the data type is complex  $e3
    Complex component creation should correctly handle extra children                 $e4
    Component creation should correctly handle Null value                             $e5
  """

  // Empty component test
  def e1 = Seq("", "&&", "  ", " & & ") map { v =>
    Component(v, ST, loc, pos) === None and Component(v, HD, loc, pos) === None
  }

  // Simple component test
  def e2 = Component("xx", ST, loc, pos) === Some {
    SimpleComponent (ST.qProps, loc, pos, Value( ST.id, "xx" ))
  }

  // Complex component test
  def e3 = Seq (
    "1&&3"     -> Set(1 -> ("1", 1), 3 -> ("3", 4)),
    "1&&3&   " -> Set(1 -> ("1", 1), 3 -> ("3", 4))
  ) map { t =>
    Component(t._1, HD, loc, pos) === Some { complexComp(loc, t._2, HD) }
  }

  // Complex component extra children test
  def e4 = Component("1&&3&4", HD, loc, pos) === Some {
    complexComp(loc, Set(1 -> ("1", 1), 3 -> ("3", 4), 4 -> ("4", 6)), HD)
  }

  // Complex component null value test
  def e5 = Seq (
    "\"\""   -> Set(1 -> ("\"\"", 1), 2 -> ("\"\"", 1), 3 -> ("\"\"", 1)),
    "1&\"\"" -> Set(1 -> ("1", 1), 2 -> ("\"\"", 3))
  ) map { t => Component(t._1, HD, loc, pos) === Some(complexComp(loc, t._2, HD)) }

  /**
    * Creates and returns a complex component
    * @param l  - The location
    * @param v  - The set of children positions and their corresponding value and column
    * @param dt - The data type
    * @return A complex component
    */
  private def complexComp(l: Location, v: Set[(Int, (String, Int))], dt: Composite) = {
    require( v.forall( _._1 > 0) )
    val hasExtra = v.maxBy( _._1 )._1 > dt.components.maxBy( _.req.position ).req.position
    // Positions for which a requirement has been defined
    val validPos = v.filter( t => dt.components.exists( _.req.position == t._1 ) )
    val children = validPos map { t =>
      val(cpos, (cv, ccol)) = t
      val component = dt.components( cpos - 1 )
      val cloc = l.copy( desc=component.name, path=s"${l.path}.$cpos[1]", column = ccol )
      val cdt  = map(component.datatypeId)
      SimpleComponent (cdt.qProps, cloc, cpos, Value( cdt.id, cv ))
    }
    ComplexComponent(dt.qProps, l, pos, children.toList, dt.requirements, hasExtra)
  }
}
