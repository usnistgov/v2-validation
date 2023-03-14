package hl7.v2.instance

import hl7.v2.profile.{Group => GM}

/**
  * Class representing a group
  */
case class Group (
    model: GM,
    instance: Int,
    children: List[SegOrGroup],
) extends SegOrGroup {

  // The group should contain an element with position = 1 and instance = 1
//  require( children exists {c => c.position == 1 && c.instance == 1} )

  val hasExtra = false //FIXME: A group cannot have extra unless it is the root right ?

//  lazy val head: Segment =
//    children find { c => c.position == 1 && c.instance == 1 } match {
//      case Some(s: Segment) => s
//      case Some(g: Group)   => g.head
//      case None => throw new Error(s"The group head is missing. $this")
//      case Some(x) => throw new Error(s"Unknown Group element '$x'. $this")
//    }
//
//  lazy val location = head.location
//    .copy(EType.Group, desc=model.name, path = model.name, uidPath = s"${model.name}[$instance]")
  
    lazy val head: Segment =
    children headOption match {
      case Some(s: Segment) => s
      case Some(g: Group)   => g.head
      case None => throw new Error(s"The group head is missing. $this")
      case Some(x) => throw new Error(s"Unknown Group element '$x'. $this")
    }

  lazy val location = head.location
    .copy(EType.Group, desc=model.name, path = model.name, uidPath = s"${model.name}[$instance]")
}
