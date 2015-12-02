package hl7.v2.validation

import hl7.v2.instance._
import hl7.v2.profile
import hl7.v2.profile.Req

object Utils {

  def defaultLocation(c: Complex, oneLevelPath: String): Option[Location] = {
    val position = oneLevelPath takeWhile( _ != '[' )
    c.reqs find( _.position.toString == position ) map { defaultLocation(c, _) }
  }

  /**
   * Creates and returns the location of the pathPart
   */
  // Computes the default location ... the uglyness is due to poor and
  // late requirements specification
  def defaultLocation(e: Complex, r: Req): Location =
    e match {
      case m: Message =>
        val (et, pp)  = m.model.structure.filter( _.req == r ) match {
          case profile.Group(_, name, _, _)  :: xs => (EType.Group, name)
          case profile.SegmentRef(_, ref) :: xs => (EType.Segment, ref.name)
        }
        e.location.copy(et, desc=r.description, path=pp, uidPath=s"$pp[1]")
      case g: Group   =>
        val (et, pp)  = g.model.structure.filter( _.req == r ) match {
          case profile.Group(_, name, _, _)  :: xs => (EType.Group, name)
          case profile.SegmentRef(_, ref) :: xs => (EType.Segment, ref.name)
        }
        e.location.copy(et, desc=r.description, path=pp, uidPath=s"$pp[1]")
      case s: Segment =>
        e.location.copy(EType.Field, desc=r.description,
          path=s"${e.location.path}-${r.position}",
          uidPath=s"${e.location.uidPath}-${r.position}[1]")
      case f: Field =>
        e.location.copy(EType.Component, desc=r.description,
          path=s"${e.location.path}.${r.position}",
          uidPath=s"${e.location.uidPath}.${r.position}")
      case c: Component =>
        c.location.copy(EType.SubComponent, desc=r.description,
          path=s"${c.location.path}.${r.position}",
          uidPath=s"${e.location.uidPath}.${r.position}")
    }
}
