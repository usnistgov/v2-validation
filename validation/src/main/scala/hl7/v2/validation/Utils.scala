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
        val (et, pp)  = g.model.structure.head match {
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

  /*private def getReq(l: List[SegRefOrGroup], pathPart: List[Int]): Option[Req] =
    pathPart match {
      case Nil      => None
      case x :: Nil => l find { y => y.req.position == x} map { z => z.req }
      case x :: xs  =>
        (l find { y => y.req.position == x }) flatMap {
          case SegmentRef(req, ref)             => getReq(ref, xs)
          case profile.Group(_, structure, req) => getReq(structure, xs)
        }
    }

  def getReq(m: profile.Message, pathPart: List[Int]): Option[Req] =
    getReq(m.structure, pathPart)

  def getReq(g: profile.Group, pathPart: List[Int]): Option[Req] =
    getReq(g.structure, pathPart)

  def getReq(s: profile.Segment, pathPart: List[Int]): Option[Req] =
    pathPart match {
      case Nil      => None
      case x :: Nil => s.fields find { _.req.position == x } map { _.req }
      case x :: xs  =>
        (s.fields find { f => f.req.position == x }) flatMap { f =>
          getReq(f.datatype, xs)
        }
    }

  def getReq(d: profile.Datatype, pathPart: List[Int]): Option[Req] =
    d match {
      case p: profile.Primitive => None
      case c: profile.Composite =>
        pathPart match {
          case Nil      => None
          case x :: Nil => c.components find { _.req.position == x } map { _.req }
          case x :: xs  =>
            (c.components find { _.req.position == x }) flatMap { cc =>
              getReq(cc.datatype, xs)
            }
        }
    }*/

}
