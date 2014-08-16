package hl7.v2.instance

/**
  * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
  */

object Path {

  val format = """[1-9][0-9]*\Q[\E(\Q*\E|([1-9][0-9]*))\Q]\E(\Q.\E[1-9][0-9]*\Q[\E(\Q*\E|([1-9][0-9]*))\Q]\E)*""".r

  val extractor = """([1-9][0-9]*)\Q[\E(\Q*\E|[1-9][0-9]*)\Q]\E(?:\Q.\E(.+))?""".r

  /**
    * Returns true if the path is valid
    */
  def isValid( path: String ) = format.pattern.matcher( path ).matches
}

