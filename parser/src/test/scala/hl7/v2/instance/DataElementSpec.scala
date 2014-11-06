package hl7.v2.instance

import hl7.v2.profile.{Usage, Req, Composite, Primitive}
import org.specs2.Specification

class DataElementSpec extends  Specification with Data { def is = s2"""

  Data Element creation Specification

    DataElement creation should return None if the string value is empty              $e1
    DataElement creation should return a simple element if the data type is primitive $e2
    DataElement creation should return a complex element if the data type is complex  $e3
    DataElement creation should correctly handle extra children                       $e4
    DataElement creation should correctly handle Null value                           $e5
  """

  def e11 = Seq("", "&&", "  ", " & & ") map { v =>
    Seq( `SE1.1` ) map { f => DataElement(f, v, loc, ins) === None }
  }

  def e1 = todo //e11

  def e2 = todo
  def e3 = todo
  def e4 = todo
  def e5 = todo

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

  val EI = Composite("EI", "EI", "EI data type", List (
    hl7.v2.profile.Component("EI.1", HD, Req(1, Usage.R, None, None, None, None )),
    hl7.v2.profile.Component("EI.2", ST, Req(2, Usage.R, None, None, None, None ))
  ))

  val `SE1.1` = hl7.v2.profile.Field("SE1.1", ST, Req(1, Usage.R, None, None, None, None ))
  val `SE1.2` = hl7.v2.profile.Field("SE1.2", HD, Req(2, Usage.R, None, None, None, None ))
  val `SE1.3` = hl7.v2.profile.Field("SE1.3", EI, Req(3, Usage.R, None, None, None, None ))

  val SE1 = hl7.v2.profile.Segment( "SE1", "SE1", "SE1 desc", List(`SE1.1`, `SE1.2`, `SE1.3`), Nil)

}