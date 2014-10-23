package hl7.v2.instance

import hl7.v2.profile.{Req, QProps}

/**
  * Class representing a segment
  */
case class Segment (
    qProps: QProps,
    location: Location,
    position: Int,
    instance: Int,
    children: List[Field with Element], //FIXME: can we add any element here?
    reqs: List[Req],
    hasExtra: Boolean
) extends SegOrGroup
