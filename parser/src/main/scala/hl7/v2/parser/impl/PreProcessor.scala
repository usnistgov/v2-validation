package hl7.v2.parser.impl

import hl7.v2.instance.Separators
import hl7.v2.profile.{Group => GM, Message => MM, SegRefOrGroup => SGM, SegmentRef => SM}
import scala.util.Try
import scala.util.Failure
import scala.util.Success
import hl7.v2.profile.Usage

/**
  * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
  */

case class PPR( valid: List[Line], invalid: List[Line], unexpected : List[Line], separators: Separators)

object PreProcessor {

  /**
    * Splits the message into lines and returns a PPR.
    * This function will standardize the lines i.e. it will replace the separators
    * with the ones recommended by HL7 if necessary.
    *
    * @param message - The message to be pre-processed
    * @return A `Success' containing  the PPR or a `Failure' if:
    *    1) No MSH segment if defined in the message
    *    2) MSH segment contains less than 9 characters
    *    3) A character is use twice as a separator
    */
  def process(message: String, model : MM): Try[PPR] =
    splitOnMSH(message) match {
      case (beforeMSH, Nil) =>
        Failure( new Exception("No MSH Segment found in the message.") )
      case (beforeMSH, xs ) =>
        getSeparators( xs.head._2 ) map { separators =>
          implicit val fs = separators.fs
          val segNames = messageSegNames(model.structure)
          val (correct, invalid) = partition( xs )
          val (unexpected, valid) = correct partition {
            seg => segNames.filter (seg._2 startsWith _) isEmpty
          }
          PPR(valid, beforeMSH:::invalid, unexpected, separators)
        }
    }
 
  def messageSegNames(models: List[SGM]) : List[String] = {
    def loop(l : List[SGM], names : List[String]) : List[String] = {
      l match {
        case head::list => head match { 
          case s : SM => loop(list, s.ref.name::names)
          case g : GM => loop(list, loop(g.structure, names))
        }
        case Nil => names
      }
    }
    loop(models, Nil)
  }

  /**
    * Splits the message into lines and returns a pair of list of lines.
    * The first list will contain all lines before the MSH segment
    */
  def splitOnMSH( message: String ): (List[Line], List[Line]) =
    ( (Stream from 1) zip lineBreak.split( message ) ).toList span { l =>
      !(l._2 startsWith "MSH")
    }

  /**
    * Partition the list of lines into list of valid and invalid lines.
    */
  private def partition(list: List[Line])
                       (implicit fs: Char): (List[Line], List[Line]) =
    list partition (l => validLinesRegex.pattern.matcher(l._2).matches)

  /**
    * Returns the separators defined in MSH.2 or a Failure
    */
  private def getSeparators( msh: String ): Try[Separators] = try {
    val fs = msh(3)
    val cs = msh(4)
    val rs = msh(5)
    val ec = msh(6)
    val ss = msh(7)
    val x  = msh(8)
    val tc = if( x == fs ) None else Some(x)
    val separators = Separators(fs, cs, rs, ec, ss, tc)
    separators.getDuplicates match {
      case Nil => Success( separators )
      case xs  => Failure(
        new Exception( s"The following character(s) ['${xs.mkString("', '")
        }'] has/have been used more than once as a separator.")
      )
    }
  } catch {
    case _: Throwable => Failure( new Exception("The MSH line contains less than 9 characters.") )
  }
}
