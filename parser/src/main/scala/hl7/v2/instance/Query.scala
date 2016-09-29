package hl7.v2.instance

import scala.util.{Failure, Try}

/**
 * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
 */

object Query {

  /**
   * Query the context for the specified path and attempt
   * to cast the result as a sequence of simple.
   */
  def queryAsSimple(context: Element, path: String): Try[List[Simple]] = {
    query( context, path ) flatMap asSimple
  }
    

  /**
   * Query the context for the specified path.
   */
  def query( context: Element, path: String ): Try[List[Element]] = Try {
    if(path.equals(".")) List(context)
    else if( Path.isValid(path) ) _query(context, path)
    else throw new Error(s"Invalid Path '$path'")
  }

  @throws[Error]("if the path is invalid or unreachable")
  private def _query(context: Element, path: String): List[Element] = path match {
    case Path.extractor(position, instance, subPath) =>
      context match {
        case s: Simple  => throw new Error(s"Unreachable Path '$path'")
        case c: Complex =>
          val list =  children( c, position, instance )
          if( subPath == null ) list
          else list.foldLeft( List[Element]() ) { (acc, child) =>
            acc ++ _query(child, subPath)
          }
      }
    case _ => throw new Error(s"Invalid Path '$path'")
  }

  /**
   * Convert a sequence of `Element' to a sequence of `Simple' element
   */
  private def asSimple ( l: List[Element] ): Try[List[Simple]] = Try {
    l map { case s: Simple => s; case _ => throw new MatchError() }
  } orElse Failure( new Error("Path resolution returned at least one complex element") )

  /**
   * Returns the children at the specified position and instance
   */
  private
  def children(c: Complex, position: String, instance: String): List[Element] =
    if( "*" == instance )
      c.children filter ( _.position == position.toInt )
    else c.children.filter { cc =>
      cc.position == position.toInt && cc.instance == instance.toInt
    }
}
