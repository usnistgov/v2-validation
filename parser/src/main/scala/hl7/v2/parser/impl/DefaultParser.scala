/*package hl7.v2.parser.impl

import scala.util.Try

import hl7.v2.instance.Group
import hl7.v2.old.Message
import hl7.v2.instance.Segment
import hl7.v2.parser.Parser

/**
  * Default implementation of the parser
  * 
  * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
  */

trait DefaultParser extends Parser {

  // Type Aliases
  type Stack = List[Line]
  type LS    = List[Segment]
  type LG    = List[Group]
  type LESG  = List[Either[List[Segment], List[Group]]]

  def parse( message: String, model: Message ): Try[Message] =
    PreProcessor.process(message) map { t =>
      val(valid, invalid) = t 
      val(children, unexpected) = processChildren( model.children , valid)
      Message( model, children.reverse, invalid, unexpected )
    }

  /**
    * Creates the children of a message or a group. Returns a triplet consisting of the 
    * list of children elements, the list of unexpected lines and the remaining stack.
    */
  private def processChildren(models: List[Either[SegmentRef, Group]], stack: Stack): (LESG, Stack) =
    models.foldLeft( (List[Either[List[Segment], List[Group]]](), stack) ) { (acc, x) =>
      x match {
        case Left(sm)  => val (ls, s) = processSegment(sm, acc._2); ( Left(ls) :: acc._1, s)
        case Right(gm) => val (lg, s) = processGroup(gm, acc._2);  ( Right(lg) :: acc._1, s)
      }
    }

  private def processGroup(gm: Group, stack: Stack): (List[Group], Stack) = {
    def loop(acc: List[Group], s: Stack, i: Int): (List[Group], Stack) = s match {
      case x::xs if( isExpected(x, gm) ) => 
        val(children, ss) = processChildren( gm.children, s)
        val g = Group( gm, children.reverse, i )
        loop( g::acc, ss , i +1)
      case _ => (acc.reverse, s)
    }
    loop(Nil, stack, 1)
  }

  private def processSegment(sm: SegmentRef, stack: Stack): (List[Segment], Stack) = {
    val(x, remainingStack) = stack span (l => isExpected(l, sm))
    val ls = x.zipWithIndex map { t => SegmentBuilder(sm, t._1, t._2 +1) }
    (ls, remainingStack)
  }

  /**
    * Returns true if the line is expected at this position
    */
  private def isExpected( l: Line, m: Group ) = l._2 startsWith headName(m)

  private def isExpected( l: Line, m: SegmentRef ) = l._2 startsWith m.ref.name

  private def headName(m: Group): String = m.children.head match {
    case  Left(s) => s.ref.name
    case Right(g) => headName(g)
  }
}
*/