/*package hl7.v2.instance

import hl7.v2.profile._
import org.scalacheck.{Prop, Gen}
import org.specs2.{ScalaCheck, Specification}

class DataElementSpec extends  Specification with ScalaCheck with Data { def is = s2"""

  Data Element creation Specification

    DataElement creation should return None if the string value is empty              $e1
    DataElement creation should return a simple element if the data type is primitive $e2
    DataElement creation should return a complex element if the data type is complex  $e3
    DataElement creation should correctly handle extra children                       $e4
    DataElement creation should correctly handle Null value                           $e5
  """

  def emptyCompGen = for {
    a <- Gen.oneOf("", "&&", "  ", " & & ")
    b <- Gen.oneOf(`EI.1`, `HD.1`)
  } yield (a, b)

  def emptyFieldGen = for {
    a <- Gen.oneOf("", "^^", "^ ^", " ^ & & ^ ")
    b <- Gen.oneOf(`SE1.1`, `SE1.2`, `SE1.3`)
  } yield (a, b)

  def e11 = Prop.forAll( emptyCompGen )  { t => DataElement(t._2, t._1, loc) === None }
  def e12 = Prop.forAll( emptyFieldGen ) { t => DataElement(t._2, t._1, loc, ins) === None }
  def e1 = e11 and e12

  def e21 = DataElement(`HD.1`, "xx", loc) === Some( SimpleComponent( ST, loc, pos, Value( ST, "xx" )) )
  def e22 = DataElement(`SE1.1`, "xx", loc, ins) === Some( SimpleField( ST, loc, pos, ins, Value( ST, "xx" )) )
  def e2 = e21 and e22


  def e31 = Seq (
    "1&&3"     -> Set(1 -> ("1", 1), 3 -> ("3", 4)),
    "1&&3&   " -> Set(1 -> ("1", 1), 3 -> ("3", 4))
  ) map { t =>
    DataElement( `EI.1`, t._1, loc) === Some { complexComp(`EI.1`.req.position, HD, t._2) }
  }

  def e32 = Seq ( "1^& &^3&", "1^^3^   " ) map { v =>
    //DataElement( `SE1.2`, v, loc, ins) === Some { complexField(`SE1.2`.req.position, HD, v) }
    DataElement( `SE1.3`, v, loc, ins) === Some { complexField(`SE1.3`.req.position, EI, v) }
  }

  def e3 = e32

  def e4 = todo

  def complexComp(pos: Int, dt: Composite, v: Set[(Int, (String, Int))]) = {
    require( v.forall( _._1 > 0) )
    val hasExtra = v.maxBy( _._1 )._1 > dt.components.maxBy( _.req.position ).req.position
    // Positions for which a requirement has been defined
    val validPos = v.filter( t => dt.components.exists( _.req.position == t._1 ) )
    val children = validPos map { t =>
      val(cpos, (cv, ccol)) = t
      val component = dt.components( cpos - 1 )
      val cloc = genLoc(loc, component.name, cpos, 1, ccol)
      val cdt  = component.datatype.asInstanceOf[Primitive]
      SimpleComponent (cdt, cloc, cpos, Value( cdt, cv ))
    }
    ComplexComponent ( dt, loc, pos, children.toList, hasExtra)
  }

  def complexField(pos: Int, dt: Composite, v: String) = {
    val vs = split(cs, v, 1)
    val hasExtra = vs.size > dt.components.size
    val children = dt.components zip vs map { t =>
      val(cm, (ccol, cv)) = t
      val cloc = genLoc(loc, cm.name, cm.req.position, 1, ccol)
      DataElement(cm, cv, cloc)
    }
    ComplexField ( dt, loc, pos, ins, children.flatten, hasExtra)
  }



  def e5 = todo

  /**
    * Generates a location from the parent location
    * @param pl - The parent location
    * @param d  - The current location description
    * @param p  - The current position
    * @param i  - The current instance number
    * @param c  - The current column
    * @return A location
    */
  private def genLoc(pl: Location, d: String, p: Int, i: Int, c: Int) =
    pl.copy( desc = d, path = s"${pl.path}.$p[$i]", column = c )

}

trait Data {

  val pos = 1
  val ins = 1
  val loc = Location("Unknown description", "Unknown path", 1, 1)

  // A primitive data type
  val ST = Primitive("ST", "ST", "ST data type")

  val `HD.1` = hl7.v2.profile.Component("HD.1", ST, Req(1, Usage.R, None, None, None, None ))
  val `HD.2` = hl7.v2.profile.Component("HD.2", ST, Req(2, Usage.R, None, None, None, None ))
  val `HD.3` = hl7.v2.profile.Component("HD.3", ST, Req(3, Usage.R, None, None, None, None ))

  // A complex data type
  val HD = Composite("HD", "HD", "HD data type", List ( `HD.1`, `HD.2`, `HD.3`))

  val `EI.1` = hl7.v2.profile.Component("EI.1", HD, Req(1, Usage.R, None, None, None, None ))
  val `EI.2` = hl7.v2.profile.Component("EI.2", ST, Req(2, Usage.R, None, None, None, None ))

  val EI = Composite("EI", "EI", "EI data type", List ( `EI.1`, `EI.2`))

  val `SE1.1` = hl7.v2.profile.Field("SE1.1", ST, Req(1, Usage.R, None, None, None, None ))
  val `SE1.2` = hl7.v2.profile.Field("SE1.2", HD, Req(2, Usage.R, None, None, None, None ))
  val `SE1.3` = hl7.v2.profile.Field("SE1.3", EI, Req(3, Usage.R, None, None, None, None ))

  val SE1 = hl7.v2.profile.Segment( "SE1", "SE1", "SE1 desc", List(`SE1.1`, `SE1.2`, `SE1.3`), Nil)

}
*/