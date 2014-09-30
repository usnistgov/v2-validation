/*package hl7.v2.parser.impl

import scala.util.Try
import scala.util.Failure
import scala.util.Success

/**
  * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
  */

object PreProcessor {

  /**
    * Splits the message into lines and returns a pair of valid and invalid lines.
    * This function will standardize the lines i.e. it will replace the separators
    * with the ones recommended by HL7 if necessary.
    * 
    * @param message - The message to be pre-processed
    * @return A `Success' containing the pair of valid and invalid lines or a `Failure' if:
    *    1) No MSH segment if defined in the message
    *    2) MSH segment contains less than 9 characters
    *    3) A character is use twice as a separator
    */
  def process(message: String): Try[ Pair[List[Line], List[Line]] ] = splitOnMSH(message) match {
    case (beforeMSH, Nil) => Failure( new Error("No MSH Segment found in the message.") )
    case (beforeMSH, xs ) => getSeparators( xs.head._2 ) map { separators =>
      implicit val fs = separators.fs
      val (valid, invalid) = partition( xs )
      ( standardize(valid, separators) , beforeMSH:::invalid ) //TODO Implement standardize
    }
  }

  /**
    * Splits the message into lines and returns a pair of list of lines.
    * The first list will contain all lines before the MSH segment
    */
  private def splitOnMSH( message: String ) = 
    ((Stream from 1) zip lineBreak.split( message )).toList span ( l => !(l._2 startsWith "MSH") )

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
    val seps = Separators(fs, cs, rs, ec, ss, tc)
    if( seps.getDuplicates.nonEmpty ) Failure( new Error( s"The following character(s) ['${
        seps.getDuplicates.mkString("','") }'] has/have been used more than once as a separator." ) )
    else Success( seps )
  } catch { 
    case _: Throwable => Failure( new Error("The MSH line contains less than 9 characters.") )
  }

  /**
    * Partition the list of lines into list of valid and invalid lines.
    */
  private def partition(list: List[(Int,String)])(implicit fs: Char) = {
    //FIXME Should we allow blank after the field separator?
    val validLinesRegex = s"^[A-Z]{2}[A-Z0-9](${quote(fs)}.+)*".r 
    list partition( l => validLinesRegex.pattern.matcher( l._2 ).matches )
  }

  /**
    * Standardizes the lines of lines i.e. replace the separators with
    * the ones recommended by HL7 is necessary
    */
  private def standardize(list: List[(Int,String)], seps: Separators): List[(Int,String)] = 
    if( seps.areRecommended ) list else ??? //TODO list map ( standardize( _, seps) )

  /*
  //FIXME standardize will remove trailing field separator .... re-implement with regular expression
  //FIXME Replace Escape sequence
  private def standardize(line: Line, seps: Separators) = {
    def splitAndReplace(s: String, l: List[(Char, Char)]): String = l match {
      case Nil         => s
      case (x, y)::Nil => s.replaceAll(quote(x), y.toString)
      case (x, y)::xs  => s.split(x) map( splitAndReplace(_, xs) ) mkString (y.toString) 
    }
    val l = seps.toList zip List('|', '^', '~', '\\', '&', '#')
    if( line._2 startsWith "MSH" ) seps.tc match {
      case None    => ( line._1, s"MSH|^~\\&${ splitAndReplace( line._2.drop(8), l )}" )
      case Some(_) => ( line._1, s"MSH|^~\\&#${splitAndReplace( line._2.drop(9), l )}" )
    } else ( line._1, splitAndReplace( line._2 , l) )
  }*/

}*/