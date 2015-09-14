package edi.ncpdp.script.parser.impl

//import hl7.v2.parser.impl.DefaultParser
import hl7.v2.instance._
import hl7.v2.parser.Parser
//import hl7.v2.profile.{Message => MM}
import hl7.v2.profile.{Group => GM, Message => MM, SegRefOrGroup => SGM, SegmentRef => SM}

import scala.util.Try

/**
  * Default implementation of the parser
  *
  * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
  */

trait DefaultNCPDPParser extends Parser {

  /**
    * Parses the message and returns the message instance model
    * @param message - The message to be parsed
    * @param model   - The message model (profile)
    * @return The message instance model
    */
  def parse( message: String, model: MM ): Try[Message] =
    NCPDPPreProcessor.process(message) map { t =>
      val PPR(valid, invalid, separators) = t
      implicit val s = separators
      implicit val ctr = Counter(scala.collection.mutable.Map[String,Int]())
      val(children, unexpected) = processChildren( model.structure , valid)
      val tz: Option[TimeZone] = None //FIXME Get TZ from MSH.7
      val ils = invalid map ( x => Line( x._1, x._2 ) ) //FIXME Update PreProcessor to use Line
      val uls = unexpected map ( x => Line( x._1, x._2 ) ) //FIXME Update PreProcessor to use Line
      Message( model, children.reverse, ils, uls, tz, s )
    }

    /* DUPLICATE from DefaultParser*/

  // Type Aliases
  type Stack = List[Line] // List[(Int, String)]
  type LS    = List[Segment]
  type LG    = List[Group]
  type LSG   = List[SegOrGroup]

  /**
    * Creates the children of a message or a group. Returns a pair consisting
    * of the list of children elements and the remaining stack.
    */
  private def processChildren(models: List[SGM], stack: Stack)
                             (implicit separators: Separators, ctr : Counter): (LSG, Stack) = {
    var isHead = true
    models.foldLeft( (List[SegOrGroup](), stack) ) { (acc, x) =>
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
    }}

  private def processGroup(gm: GM, stack: Stack)
                          (implicit separators: Separators, ctr : Counter): (List[Group], Stack) = {

    def loop(acc: List[Group], s: Stack, i: Int): (List[Group], Stack) =
      s match {
        case x::xs if isExpected(x, gm)  =>
          val(children, ss) = processChildren( gm.structure, s)
          val g = Group( gm, i, children.reverse )
          loop( g::acc, ss , i +1)
        case _ => (acc, s)
      }

    loop(Nil, stack, 1)
  }

  private def processSegment(sm: SM, stack: Stack, isHead: Boolean)
                            (implicit s: Separators, ctr : Counter): (List[Segment], Stack) =
    if(isHead) ( segment(sm, stack.head, 1) :: Nil, stack.tail )
    else {
      val(x, remainingStack) = stack span (l => isExpected(l, sm))
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
  private def segment(m: SM, l: Line, i: Int)(implicit s: Separators, ctr : Counter) =
    Segment(m, l._2, i, l._1)

  private def isExpected( l: Line, m: GM ) = l._2 startsWith headName(m)

  private def isExpected( l: Line, m: SM ) = l._2 startsWith m.ref.name

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
