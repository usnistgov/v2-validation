package hl7.v2.instance

import hl7.v2.profile.{Primitive, Composite, Req, Usage, QProps, QType}
import org.specs2.Specification

trait DataElementCreationHelper {

  val ST = Primitive("ST", "ST", "ST data type")

  val HD = Composite("HD", "HD", "HD data type", List (
    hl7.v2.profile.Component("HD.1", "ST", Req(1, Usage.R, None, None, None, None )),
    hl7.v2.profile.Component("HD.2", "ST", Req(2, Usage.R, None, None, None, None )),
    hl7.v2.profile.Component("HD.3", "ST", Req(3, Usage.R, None, None, None, None ))
  ))

  implicit val map = Map("ST" -> ST, "HD" -> HD)

  val loc = Location("desc", "path", 1, 1)

  val pos = 1

  val scQProps = QProps(QType.DT, "ST", "ST")

  def scValue(v: String) = Value( scQProps.id, v )

  def sc(v: String): Option[Component] = Component.apply(v, ST, loc, pos)

  def cc(v: String): Option[Component] = Component.apply(v, HD, loc, pos)

}

class ComponentSpec extends Specification with DataElementCreationHelper { def is = s2"""
  Component Specification
    Component creation should return None if the string value is empty $e1
    Component creation should return a simple component if the data type is primitive $e2
    Component creation should return a complex component if the data type is complex
    Component creation should correctly handle Null value
  """

  def e1 = Seq("", "&&", "  ", " & & ") map { v => sc(v) === None and cc(v) === None }

  def e2 = sc("xx") === Some( SimpleComponent (scQProps, loc, pos, scValue("xx")) )
}

