package hl7.v2.instance

import hl7.v2.profile.{Group => GM}

/**
  * Class representing a group
  */
case class Group (
    model: GM,
    instance: Int,
    profilePath: Option[ElementProfilePath],
    children: List[SegOrGroup],
) extends SegOrGroup {

  val hasExtra = false //FIXME: A group cannot have extra unless it is the root right ?
  lazy val head: Segment =
    children.headOption match {
      case Some(s: Segment) => s
      case Some(g: Group)   => g.head
      case None => throw new Error(s"The group head is missing. $this")
      case Some(x) => throw new Error(s"Unknown Group element '$x'. $this")
    }

  lazy val location = head.location
    .copy(EType.Group, desc=model.name, path = model.name, uidPath = s"${model.name}[$instance]", elementProfilePath = profilePath)
}
