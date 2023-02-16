package hl7.v2.parser.impl

import hl7.v2.instance._
import hl7.v2.instance.util.ValueConversionHelpers.splitOnTZ
import hl7.v2.instance.util.ValueFormatCheckers.isValidDateTimeFormat
import hl7.v2.parser.Parser
import hl7.v2.profile.{Group => GM, Message => MM, SegRefOrGroup => SGM, SegmentRef => SM}

import scala.util.Try

/**
 * Default implementation of the parser
 *
 * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
 */

trait DefaultParser extends Parser {

  /**
   * Parses the message and returns the message instance model
   * @param message - The message to be parsed
   * @param model   - The message model (profile)
   * @return The message instance model
   */
  def parse(message: String, model: MM): Try[Message] =
    PreProcessor.process(message, model) map { t =>
      val PPR(valid, invalid, preUnexpected, separators) = t
      implicit val s = separators
      implicit val ctr = Counter(scala.collection.mutable.Map[String, Int]())
      val (children, unexpected) = processChildren(model.structure, valid.map(x => Line(x._1, x._2)))
      val tz: Option[TimeZone] = resolveTimeZone(children)
      val ils = invalid map (x => Line(x._1, x._2))
      val uls = (preUnexpected.map(x => Line(x._1, x._2)) ::: unexpected)
      Message(model, children.reverse, ils, uls, tz, s)
    }

  def resolveTimeZone(children: LSG): Option[TimeZone] = {
    children.find(c => c.position == 1).collect({
      case segment: Segment if segment.model.ref.name.equals("MSH") =>
        segment.children.find(f => f.position == 7).collect({
          case sf: SimpleField if isValidDateTimeFormat(sf.value.raw) =>
            Option(splitOnTZ(sf.value.raw)._2).filter(_.nonEmpty).map(TimeZone)
        }).flatten
    }).flatten
  }

  // Type Aliases
  type Stack = List[Line] // List[(Int, String)]
  type LS = List[Segment]
  type LG = List[Group]
  type LSG = List[SegOrGroup]

  /**
   * Creates the children of a message or a group. Returns a pair consisting
   * of the list of children elements and the remaining stack.
   */
  protected def processChildren(models: List[SGM], stack: Stack, head: Boolean = true)(implicit separators: Separators, ctr: Counter): (LSG, Stack) = {
    var isHead = head
    models.foldLeft((List[SegOrGroup](), stack)) { (acc, x) =>
      x match {
        case sm: SM =>
          val (ls, s) = processSegment(sm, acc._2, isHead)
          isHead = false
          (ls ::: acc._1, s)
        case gm: GM =>
          val (lg, s) = processGroup(gm, acc._2)
          isHead = false
          (lg ::: acc._1, s)
      }
    }
  }

  private def processGroup(gm: GM, stack: Stack)(implicit separators: Separators, ctr: Counter): (List[Group], Stack) = {
    def loop(acc: List[Group], s: Stack, i: Int): (List[Group], Stack) = {
      if (s.isEmpty) (acc, s)
      else if (!isExpected(s.head, gm)) (acc, s)
      else
        lookFor(gm, s.head) match {
          case Some(index) => {
            val (children, ss) = processChildren(gm.structure.takeRight(gm.structure.size - index), s, true)
            val g = Group(gm, i, children.reverse)
            loop(g :: acc, ss, i + 1)
          }
          case None => (acc, s)
        }
    }

    loop(Nil, stack, 1)
  }

  private def lookFor(gm: GM, l: Line)(implicit separators: Separators, ctr: Counter) =
    (gm.structure.zipWithIndex) find
      { x => isExpected(l, x._1) } match {
        case Some((m, i)) => Some(i)
        case None => None
      }

  private def processSegment(sm: SM, stack: Stack, isHead: Boolean)(implicit s: Separators, ctr: Counter): (List[Segment], Stack) =
    if (isHead) (segment(sm, stack.head, 1) :: Nil, stack.tail)
    else {
      val (x, remainingStack) = stack span (l => isExpected(l, sm))
      val ls = x.zipWithIndex map { t => segment(sm, t._1, t._2 + 1) }
      (ls.reverse, remainingStack)
    }

  /**
   * Creates and returns a segment instance
   * @param m - The segment model
   * @param l - The line number and the segment value as string
   * @param i - The instance number
   * @param s - The separators
   * @return A segment instance
   */
  private def segment(m: SM, l: Line, i: Int)(implicit s: Separators, ctr: Counter) =
    Segment(m, l.content, i, l.number)

  private def isExpected(l: Line, m: GM): Boolean = m.structure.span(_.isInstanceOf[SM]) match {
    case (Nil, grpAndOthers) => grpAndOthers.exists(isExpected(l, _))
    case (headSegments, _) => headSegments.exists(isExpected(l, _))
  }
  private def isExpected(l: Line, m: SM ) = l.content startsWith m.ref.name
  private def isExpected(l: Line, m: SGM): Boolean = m match {
    case s : SM => isExpected(l, s)
    case g : GM => isExpected(l, g)
  }

  private def isHead(l : Line, m : GM) = l.content startsWith headName(m)

  /**
   * Returns the group head name
   * @param m - The group model
   * @return The group head name
   */
  private def headName(m: GM): String = m.structure.head match {
    case s: SM => s.ref.name
    case g: GM => headName(g)
  }
}
