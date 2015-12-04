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
      val(children, unexpected) = processChildren( model.structure , valid,null)
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
  private def processChildren(models: List[SGM], stack: Stack,groupPrefix:String)
                             (implicit separators: Separators, ctr : Counter): (LSG, Stack) = {
    var isHead = true

    models.foldLeft( (List[SegOrGroup](), stack) ) { (acc, x) =>
      x match {
        case sm: SM =>
          val (ls, s) = processSegment(sm, acc._2, isHead,groupPrefix)
          isHead = false
          (ls ::: acc._1, s)
        case gm: GM =>
          val (lg, s) = processGroup(gm, acc._2)
          isHead = false
          (lg ::: acc._1, s)
      }
    }}

  private def getGroupPrefix(gm: GM): String = {
    gm.id.split("_").head
  }

  private def processGroup(gm: GM, stack: Stack)
                          (implicit separators: Separators, ctr : Counter): (List[Group], Stack) = {

    def loop(acc: List[Group], s: Stack, i: Int): (List[Group], Stack) = {
      val groupPrefix = getGroupPrefix(gm)
      s match {
        case x :: xs if isExpected(x, gm,groupPrefix) =>
          val (children, ss) = processChildren(gm.structure, s, groupPrefix)
          val g = Group(gm, i, children.reverse)
          loop(g :: acc, ss, i + 1)
        case _ => (acc, s)
      }
    }

    loop(Nil, stack, 1)
  }

  private def processSegment(sm: SM, stack: Stack, isHead: Boolean,groupPrefix:String)
                            (implicit s: Separators, ctr : Counter): (List[Segment], Stack) =
    if(isHead) ( segment(sm, stack.head, 1) :: Nil, stack.tail )
    else {
      val(x, remainingStack) = stack span (l => isExpected(l, sm,groupPrefix))
      val ls = x.zipWithIndex map {
        t => segment(sm, t._1, t._2 + 1)
      }
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



  private def isExpected( l: Line, m: GM ,groupPrefix: String) = {
    var isExpected = false
    val specialSegments = Map("DRU" -> 1,"SIG" -> 1,"SRC" ->1, "PVD" ->2)
    if (l._2 startsWith headName(m)) {
      var isSpecial = false
      var fieldLength = 0
      specialSegments.foreach{ specialSegment =>
        if(l._2 startsWith specialSegment._1) {
          fieldLength = specialSegment._2
          isSpecial = true
        }
      }
      if(isSpecial) {
        var id = findId(l._2, headName(m), fieldLength,null)
        if (id != "") {
          val compareId = headId(m)
          isExpected = compareId startsWith id
        }
      }
      else {
        isExpected = true
      }
    }
    isExpected
  }

  private def isExpected( l: Line, m: SM,groupPrefix:String) = {
    var isExpected = false
    val specialSegments = Map("PVD" -> 2,"DRU" -> 1,"SIG" -> 1,"SRC" ->1)
    if (l._2 startsWith m.ref.name) {
      var isSpecial = false
      var fieldLength = 0
      specialSegments.foreach{ specialSegment =>
        if(l._2 startsWith specialSegment._1) {
          fieldLength = specialSegment._2
          isSpecial = true
        }
      }
      if(isSpecial) {
        var id = findId(l._2, m.ref.name, fieldLength,groupPrefix)
        if (id != "") {
          val compareId = m.ref.id
          if(l._2 startsWith "PVD"){
            isExpected = compareId contains id
          } else {
            isExpected = compareId startsWith id
          }
        }
      } else {
        isExpected = true
      }
    }
    isExpected
    //l._2 startsWith m.ref.name
  }

  /**
    * Returns the group head name
    * @param m - The group model
    * @return The group head name
    */
  private def headName(m: GM): String = m.structure.head match {
    case s: SM => s.ref.name
    case g: GM => headName(g)
  }

  /**
   * Returns the group head name
   * @param m - The group model
   * @return The group head id
   */
  private def headId(m: GM): String = m.structure.head match {
    case s: SM => s.ref.id
    case g: GM => headId(g)
  }

  private def findId(line: String,segmentName: String,fieldLength:Int,groupPrefix:String) = {
    var segId = ""
    val segMap = Map("P" -> "Prescribed", "D" -> "Dispensed", "R" -> "Requested")
    val pvdList = List("PC","P2","SU","SK")
    if(groupPrefix!=null){
        segId = groupPrefix+"_"+segmentName;
    } else {
      segMap foreach { x =>
        val start = segmentName.length + 1
        if (line.length >= start + fieldLength) {
          val name = line.substring(start, start + fieldLength)
          if ((segmentName != "PVD") && (name equals x._1)) {
            segId = x._2 + "_" + segmentName
          }
        }
      }
    }
    if (segmentName == "PVD") {
      val pvdType = line.substring("PVD".length + 1, "PVD".length + 1 + 2)
      pvdList foreach { currentPvdType =>
        if (currentPvdType.equals(pvdType)) {
          segId = segmentName + "_" + currentPvdType
        }
      }
    }
    segId
  }

}
