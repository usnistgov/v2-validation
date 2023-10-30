package hl7.v2.parser.impl

import hl7.v2.instance.{Counter, EType, ElementProfilePath, Line, SegOrGroup, Segment, Separators, Group => GI, Location => L}
import hl7.v2.profile.SegRefOrGroup
import hl7.v2.profile.{Group => GM}
import org.specs2.{ScalaCheck, Specification}

trait ParserSpecHelper extends Specification with ScalaCheck with DefaultParser {

  def check(tc: (String, List[SegOrGroup]), profile: List[SegRefOrGroup]) = {
    val stack = PreProcessor.splitOnMSH(tc._1)._2
    implicit val s = Separators( '|', '^', '~', '\\', '&', '#')
    implicit val ctr = Counter(scala.collection.mutable.Map[String, Int]())
    val result = processChildren(profile, None, stack.map(x => Line(x._1, x._2)))._1.reverse
    result must containTheSameElementsAs(tc._2)
  }

  def Location(eType: EType,
               desc: String,
               path: String,
               line: Int,
               column: Int,
               uidPath: String) = L(eType, desc, path, line, column, uidPath, None)

  def GroupInstance(model: GM,
                    instance: Int,
                    children: List[SegOrGroup]): GI = {
    GI(model, instance, None, children)
  }

  def make(list: List[SegOrGroup]): List[SegOrGroup] = {
    def inner(list: List[SegOrGroup], parent: Option[ElementProfilePath]): List[SegOrGroup] = {
      list.map {
        case group: GI =>
          val node = ElementProfilePath(
            Some(group.model.name),
            EType.Group,
            group.req.position,
            Some(group.instance),
            None
          )
          val elementProfilePath = parent.map(p => p.append(node)).getOrElse(node)
          val children = inner(group.children, Some(elementProfilePath))
          group.copy( profilePath = Some(elementProfilePath), children = children)
        case segment: Segment =>
          val node = ElementProfilePath(
            Some(segment.model.ref.name),
            EType.Segment,
            segment.req.position,
            Some(segment.instance),
            None
          )
          val elementProfilePath = parent.map(p => p.append(node)).getOrElse(node)
          segment.copy(location = segment.location.copy(elementProfilePath = Some(elementProfilePath)))
      }
    }

    inner(list, None)
  }


}
