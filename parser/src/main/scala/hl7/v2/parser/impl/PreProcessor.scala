package hl7.v2.parser.impl

import hl7.v2.instance.Separators

import scala.util.Try
import scala.util.Failure
import scala.util.Success

/**
  * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
  */

case class PPR( valid: List[Line], invalid: List[Line], separators: Separators )

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
  def process(message: String): Try[PPR] =
    splitOnMSH(message) match {
      case (beforeMSH, Nil) =>
        Failure( new Exception("No MSH Segment found in the message.") )
      case (beforeMSH, xs ) =>
        getSeparators( xs.head._2 ) map { separators =>
          implicit val fs = separators.fs
          val (valid, invalid) = partition( xs )
          PPR(valid, beforeMSH:::invalid, separators)
        }
    }

  /**
    * Splits the message into lines and returns a pair of list of lines.
    * The first list will contain all lines before the MSH segment
    */
  private def splitOnMSH( message: String ): (List[Line], List[Line]) =
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
