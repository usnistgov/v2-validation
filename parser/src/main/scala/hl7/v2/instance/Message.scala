package hl7.v2.instance

import hl7.v2.profile.{QProps, Req}

/**
 * Class representing a message
 */
case class Message (
    id: String,
    structId: String,
    event: String,
    typ: String,
    desc: String,
    children: List[SegOrGroup],
    reqs: List[Req]
) {

  private lazy val qProps = QProps(hl7.v2.profile.MSG, id, structId)

  lazy val asGroup = Group(qProps, -1, 1, children, reqs)

  lazy val location = asGroup.location
}
