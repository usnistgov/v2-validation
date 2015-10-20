package edi.ncpdp.script.parser.impl

import hl7.v2.instance.Separators
//import hl7.v2.instance.Line


import scala.util.Try
import scala.util.Failure
import scala.util.Success

/**
  * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
  */

case class PPR( valid: List[Line], invalid: List[Line], separators: Separators )

object NCPDPPreProcessor {

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
      splitOnUNA(message) match {
      case (Nil, s) =>
        Failure( new Exception("Issue.") )
      case (xs, s) =>
        implicit val fs = s.fs
        implicit val cs = s.cs.toString
        println("fs:^"+fs.toString+"^ | cs:^"+cs+"^")
        partition( xs ) match {
          case (Nil, invalid) =>
            println("invalid: "+invalid)
            Failure( new Exception("No valid segments in this message") )
          case (valid, invalid) =>
            println("valid: "+valid)
            println("invalid: "+invalid)
            Success(PPR(valid, invalid, s))
        }
    }  
    

  /**
    * Splits the message into lines and returns a pair of list of lines.
    * The first list will contain all lines before the MSH segment
    */
  private def splitOnUNA( message: String): (List[Line], Separators) = {
    getSeparators( message ) map { s =>
      val ts = s.ts.get.toString
      println("ts:^"+ts+"^")
      val lines = ( (Stream from 1) zip ts.r.split( message ) ).toList.map(l=>(l._1,trimLineBreakLeft(l._2)))
      (lines, s)
    } match {
      case Success(p) => p
      case Failure(e) => throw e
    }
  }

  /**
    * Partition the list of lines into list of valid and invalid lines.
    */
  private def partition(list: List[Line])
                       (implicit fs: Char, cs: String): (List[Line], List[Line]) =
    list partition (l => {
      val ml = trimLineBreakLeft(l._2)
      //println("ml about to be matched:\n^"+ml+"^")
      validLinesRegex.pattern.matcher(ml).matches
    })
      

  /**
    * Returns the separators defined in MSH.2 or a Failure
    */
  private def getSeparators( message: String ): Try[Separators] = try {

    println("getting separators for message: " + message)

    if (!message.startsWith("UNA")) {
      Failure( new Exception( s"The message doesn't start with a UNA segment"))
    }
    if (message.length() < 9){
      throw new Exception( s"The UNA segment length is less than 9 characters")
    }

    //UNA:+./*'
    val cs = message(3)
    val fs = message(4)
    val ec = message(6)
    val rs = message(7)
    
    val ss = 'º'
    val tc = None

    val dnChar = message(5)
    val tsChar = message(8)
    val dn = if( dnChar != '\0' ) Some(dnChar) else None
    val ts = if( tsChar != '\0' ) Some(tsChar) else None

    //val ss = msh(7)
    //val x  = msh(8)
    //val tc = if( x == fs ) None else Some(x)
    
    val separators = Separators(fs, cs, rs, ec, ss, tc, dn, ts)

    separators.getDuplicates match {
      case Nil => Success( separators )
      case xs  => Failure(
        new Exception( s"The following character(s) ['${xs.mkString("', '")}'] has/have been used more than once as a separator.")
      )
    }
  } catch {
    case e: Exception => Failure( e )
  }
}
