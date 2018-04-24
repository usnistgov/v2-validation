package hl7.v2.parser.impl

import hl7.v2.instance._
import hl7.v2.parser.Parser
import hl7.v2.profile.{ Group => GM, Message => MM, SegRefOrGroup => SGM, SegmentRef => SM }

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
      val PPR(valid, invalid, preUnexpected, separators, ambiguous) = t
      implicit val s = separators
      implicit val ctr = Counter(scala.collection.mutable.Map[String, Int]())
      implicit val a = ambiguous
      val (children, unexpected) = processChildren(model.structure, valid)
      val tz: Option[TimeZone] = None //FIXME Get TZ from MSH.7
      val ils = invalid map (x => Line(x._1, x._2)) //FIXME Update PreProcessor to use Line
      val uls = (preUnexpected ::: unexpected) map (x => Line(x._1, x._2)) //FIXME Update PreProcessor to use Line
      Message(model, children.reverse, ils, uls, tz, s)
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
  private def processChildren(models: List[SGM], stack: Stack)(implicit separators: Separators, ctr: Counter, ambiguous : Boolean): (LSG, Stack) = {
    var isHead = true
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

  private def processGroup(gm: GM, stack: Stack)(implicit separators: Separators, ctr: Counter, ambiguous : Boolean): (List[Group], Stack) = {

    def loop(acc: List[Group], s: Stack, i: Int): (List[Group], Stack) = {
      if (s isEmpty) (acc, s) else
        lookFor(gm, s.head, ambiguous) match {
          case Some(index) => {
            val (children, ss) = processChildren(gm.structure.takeRight(gm.structure.size - index), s)
            val g = Group(gm, i, children.reverse)
            loop(g :: acc, ss, i + 1)
          }
          case None => (acc, s)
        }
    }

    loop(Nil, stack, 1)
  }

  private def forwardLooking(gm: GM, l: Line)(implicit separators: Separators, ctr: Counter) =
    (gm.structure zipWithIndex) find
      { x => isExpected(l, x._1) } match {
        case Some((m, i)) => Some(i)
        case None => None
      }

  private def lookFor(gm: GM, l: Line, ambiguous: Boolean)(implicit separators: Separators, ctr: Counter): Option[Int] = {
    if (ambiguous)
      if (isHead(l, gm)) Some(0)
      else None
    else forwardLooking(gm, l)
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
    Segment(m, l._2, i, l._1)

  private def isExpected(l: Line, m: GM): Boolean = !(m.structure find(isExpected(l,_))).isEmpty
  private def isExpected( l: Line, m: SM ) = l._2 startsWith m.ref.name
  private def isExpected( l: Line, m: SGM): Boolean = m match {
    case s : SM => isExpected(l, s)
    case g : GM => isExpected(l, g)
  }
  private def isHead(l : Line, m : GM) = l._2 startsWith headName(m)

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
