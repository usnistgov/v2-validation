package hl7.v2

package object instance {

/*
  val rs = '~'  // Field repetition separator
  val fs = '|'  // Field separator
  val cs = '^'  // Component separator
  val ss = '&'  // Sub component separator
  val ec = '\\' // Escape character
  val tc = '#'  // Truncation character
*/

  /**
   * Trim the space on right of the string
   */
  def trimRight(s: String) = s.replaceAll("\\s+$", "")

  /**
   * Splits the string `s' on the character `sep' and returns a tuple of (Int, String)
   * where the Int represent the index of the sub string + `col'
   */
  def split(sep: Char, s: String, col: Int): Array[(Int, String)] = {
    var column = col
    val trimmed = trimRight( s )
    trimmed.split( sep ) map { ss => val r = column -> ss; column += ss.length + 1; r }
  }

  def pad[T]( a: Array[T], x: T, len: Int ) = a.padTo( len, x )

  sealed trait EType
  object EType{
    //case object Message extends EType { override  def toString = "Message" }
    case object Group   extends EType { override  def toString = "Group" }
    case object Segment extends EType { override  def toString = "Segment" }
    case object Field   extends EType { override  def toString = "Field" }
    case object Component extends EType { override  def toString = "Component" }
    case object SubComponent extends EType { override def toString = "Sub-Component" }
  }
}
