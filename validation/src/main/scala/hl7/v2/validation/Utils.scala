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
        val (et, pp)  = m.model.structure.head match {
          case gg: profile.Group      => (EType.Group, gg.name)
          case ss: profile.SegmentRef => (EType.Segment, ss.ref.name)
        }
        e.location.copy(et, desc=r.description, path=pp)
      case g: Group   =>
        val (et, pp)  = g.model.structure.head match { //FIXME this is bogus
          case gg: profile.Group   => (EType.Group, gg.name)
          case ss: profile.SegmentRef => (EType.Segment, ss.ref.name)
        }
        e.location.copy(et, desc=r.description, path=pp)
      case s: Segment =>
        e.location.copy(EType.Field, desc=r.description,
          path=s"${e.location.path}-${r.position}")
      case f: Field =>
        e.location.copy(EType.Component, desc=r.description,
          path=s"${e.location.path}.${r.position}")
      case c: Component =>
        c.location.copy(EType.SubComponent, desc=r.description,
          path=s"${c.location.path}.${r.position}")
    }

}
