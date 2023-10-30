package hl7.v2.validation

import hl7.v2.instance.{ElementProfilePath, _}
import hl7.v2.profile
import hl7.v2.profile.{Composite, Req}

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
    (e match {
      case m: Message =>
        (m.model.structure.filter(_.req.position == r.position) match {
          case (group: profile.Group) :: _ => Some(getGroupElementProfilePath(group))
          case (segRef: profile.SegmentRef) :: _ => Some(getSegmentElementProfilePath(segRef))
          case _ => None
        }).map(child => {
          e.location.copy(
            child.eType,
            desc = r.description,
            path = s"${child.name.getOrElse("")}",
            uidPath = s"${child.name.getOrElse("")}[1]",
            elementProfilePath = Some(child)
          )
        })
      case g: Group   =>
        (g.model.structure.filter(_.req.position == r.position) match {
          case (group: profile.Group) :: _ => Some(getGroupElementProfilePath(group))
          case (segRef: profile.SegmentRef) :: _ => Some(getSegmentElementProfilePath(segRef))
          case _ => None
        }).map(child => {
          e.location.copy(
            child.eType,
            desc = r.description,
            path = s"${e.location.elementProfilePath.map(_ => s"${e.location.path}.").getOrElse("")}${child.name.getOrElse("")}",
            uidPath = s"${e.location.elementProfilePath.map(_ => s"${e.location.path}.").getOrElse("")}${child.name.getOrElse("")}[1]",
            elementProfilePath = Some(
              e.location.elementProfilePath.map(p => p.append(child)).getOrElse(child)
            )
          )
        })
      case s: Segment =>
        (s.model.ref.fields.filter(_.req.position == r.position) match {
          case (field: profile.Field) :: _ => Some(getFieldElementProfilePath(field))
          case _ => None
        }).map(child => {
          e.location.copy(
            child.eType,
            desc = r.description,
            path = s"${e.location.path}-${r.position}",
            uidPath = s"${e.location.uidPath}-${r.position}[1]",
            elementProfilePath = Some(
              e.location.elementProfilePath.map(p => p.append(child)).getOrElse(child)
            )
          )
        })
      case f: Field =>
        f.datatype match {
          case Composite(_, _, _, _, components) =>
            (components.filter(_.req.position == r.position) match {
              case (component: profile.Component) :: _ => Some(getComponentElementProfilePath(component, EType.Component))
              case _ => None
            }).map(child => {
              e.location.copy(
                child.eType,
                desc = r.description,
                path = s"${e.location.path}.${r.position}",
                uidPath = s"${e.location.uidPath}.${r.position}",
                elementProfilePath = Some(
                  e.location.elementProfilePath.map(p => p.append(child)).getOrElse(child)
                )
              )
            })
          case _ => None
        }
      case c: Component =>
        c.datatype match {
          case Composite(_, _, _, _, components) =>
            (components.filter(_.req.position == r.position) match {
              case (component: profile.Component) :: _ => Some(getComponentElementProfilePath(component, EType.SubComponent))
              case _ => None
            }).map(child => {
              e.location.copy(
                child.eType,
                desc = r.description,
                path = s"${e.location.path}.${r.position}",
                uidPath = s"${e.location.uidPath}.${r.position}",
                elementProfilePath = Some(
                  e.location.elementProfilePath.map(p => p.append(child)).getOrElse(child)
                )
              )
            })
          case _ => None
        }
    }).getOrElse(e.location)


  private def getGroupElementProfilePath(element: profile.Group): ElementProfilePath = {
    ElementProfilePath(
      Some(element.name),
      EType.Group,
      element.req.position,
      Some(1),
      None
    )
  }

  private def getSegmentElementProfilePath(element: profile.SegmentRef): ElementProfilePath = {
    ElementProfilePath(
      Some(element.ref.name),
      EType.Segment,
      element.req.position,
      Some(1),
      None
    )
  }

  private def getFieldElementProfilePath(element: profile.Field): ElementProfilePath = {
    ElementProfilePath(
      None,
      EType.Field,
      element.req.position,
      Some(1),
      None
    )
  }

  private def getComponentElementProfilePath(element: profile.Component, kind: EType): ElementProfilePath = {
    ElementProfilePath(
      None,
      kind,
      element.req.position,
      None,
      None
    )
  }
}
