/*package hl7.v2.instance

import hl7.v2.profile.{Usage, Req, Composite, Primitive}


trait DataElementCreationHelper {
  // A primitive data type
  val ST = Primitive("ST", "ST", "ST data type")

  // A complex data type
  val HD = Composite("HD", "HD", "HD data type", List (
    hl7.v2.profile.Component("HD.1", "ST", Req(1, Usage.R, None, None, None, None )),
    hl7.v2.profile.Component("HD.2", "ST", Req(2, Usage.R, None, None, None, None )),
    hl7.v2.profile.Component("HD.3", "ST", Req(3, Usage.R, None, None, None, None ))
  ))

  // The implicit data type map
  implicit val map = Map("ST" -> ST, "HD" -> HD)

  // The default location
  val loc = Location("desc", "path", 1, 1)

  // The default position
  val pos = 1

  // The default instance number
  val ins = 1
}
*/