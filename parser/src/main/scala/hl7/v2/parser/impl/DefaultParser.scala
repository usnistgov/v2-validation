package hl7.v2.parser.impl

import hl7.v2.instance.{Group, Message, SegOrGroup, Segment}
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
  def parse( message: String, model: MM ): Try[Message] =
    PreProcessor.process(message) map { t =>
      val(valid, invalid) = t
      val(children, unexpected) = processChildren( model.structure , valid)
      Message( model, children.reverse, invalid, unexpected )
    }

  // Type Aliases
  type Stack = List[Line] // List[(Int, String)]
  type LS    = List[Segment]
  type LG    = List[Group]
  type LSG   = List[SegOrGroup]

  /**
    * Creates the children of a message or a group. Returns a pair consisting
    * of the list of children elements and the remaining stack.
    */
  private def processChildren(models: List[SGM], stack: Stack): (LSG, Stack) =
    models.foldLeft( (List[SegOrGroup](), stack) ) { (acc, x) =>
      x match {
        case sm: SM => val (ls, s) = processSegment(sm, acc._2); (ls ::: acc._1, s)
        case gm: GM => val (lg, s) = processGroup(gm, acc._2); (lg ::: acc._1, s)
      }
    }

  private def processGroup(gm: GM, stack: Stack): (List[Group], Stack) = {
    def loop(acc: List[Group], s: Stack, i: Int): (List[Group], Stack) = s match {
      case x::xs if isExpected(x, gm)  =>
        val(children, ss) = processChildren( gm.structure, s)
        val g = Group( gm, i, children )
        loop( g::acc, ss , i +1)
      case _ => (acc, s)
    }
    loop(Nil, stack, 1)
  }

  private def processSegment(sm: SM, stack: Stack): (List[Segment], Stack) = {
    val(x, remainingStack) = stack span (l => isExpected(l, sm))
    val ls = x.zipWithIndex map { t => segment(sm, t._1, t._2 +1) }
    (ls, remainingStack)
  }

  private def segment(m: SM, l: Line, i: Int) = Segment(m, l._2, i, l._1)

  private def isExpected( l: Line, m: GM ) = l._2 startsWith headName(m)

  private def isExpected( l: Line, m: SM ) = l._2 startsWith m.ref.name

  private def headName(m: GM): String = m.structure.head match {
    case s: SM => s.ref.name
    case g: GM => headName(g)
  }
}
