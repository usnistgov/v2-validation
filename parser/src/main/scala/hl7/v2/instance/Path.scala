package hl7.v2.instance

import scala.util.matching.Regex

/**
 * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
 */

object Path {

  val format: Regex = """[1-9][0-9]*\Q[\E(\Q*\E|([1-9][0-9]*))\Q]\E(\Q.\E[1-9][0-9]*\Q[\E(\Q*\E|([1-9][0-9]*))\Q]\E)*|\Q.\E""".r

  val extractor: Regex = """([1-9][0-9]*)\Q[\E(\Q*\E|[1-9][0-9]*)\Q]\E(?:\Q.\E(.+))?""".r

  /**
   * Returns true if the path is valid
   */
  def isValid( path: String ): Boolean = format.pattern.matcher( path ).matches
}
