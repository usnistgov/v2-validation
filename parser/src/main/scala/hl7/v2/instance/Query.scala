package hl7.v2.instance

import scala.util.Failure
import scala.util.Try

/**
  * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
  */

object Query {

  /**
    * Query the context for the specified path and attempt
    * to cast the result as a sequence of simple.
    */
  def queryAsSimple(context: Element, path: String): Try[Seq[Simple]] = 
    query( context, path ) flatMap ( asSimple _ )

  /**
    * Query the context for the specified path.
    */
  def query( context: Element, path: String ): Try[Seq[Element]] = {
    def h(scontext: Element, spath: String): Seq[Element] = spath match {
      case Path.extractor(position, instance, subPath) => 
        scontext match {
          case s: Simple  => throw new Error(s"Unreachable Path '${path}'")
          case c: Complex =>
            val list = children( c, position, instance )
            if( subPath == null ) list
            else list.foldLeft(Seq[Element]())( (acc, child) => acc ++ h(child, subPath) )
        }
      case _ => throw new Error(s"Invalid Path '${path}'")
    }
    if( Path.isValid(path) ) Try( h(context, path) )
    else Failure( new Error(s"Invalid Path '${path}'") )
  }

  /**
    * Convert a sequence of `Element' to a sequence of `Simple' element
    */
  private def asSimple ( l: Seq[Element] ): Try[Seq[Simple]] = Try { 
    l.map { case s: Simple => s; case _ => throw new MatchError() } 
  } orElse Failure( new Error("Path resolution returned at least one complex element") )

  /**
    * Returns the children at the specified position and instance 
    */
  private def children(c: Complex, position: String, instance: String): Seq[Element] = 
    if( "*" == instance ) c.get(position.toInt ) else c.get(position.toInt, instance.toInt )
}
