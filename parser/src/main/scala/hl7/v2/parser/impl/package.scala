package hl7.v2.parser

package object impl {

  val rs = '~'  // Field repetition separator
  val fs = '|'  // Field separator
  val cs = '^'  // Component separator
  val ss = '&'  // Sub component separator
  val ec = '\\' // Escape character
  val tc = '#'  // Truncation character

  type Line = (Int, String)

  val lineBreak = """\r?\n|\r\n?|\r\n""".r

  /**
    * Regular expression representing a valid segment instance
    */
  val validLinesRegex = s"^[A-Z]{2}[A-Z0-9](${quote(fs)}.*)*".r

  def quote(c: Char) = java.util.regex.Pattern.quote( c.toString )

  case class Separators( fs: Char, cs: Char, rs: Char, ec: Char, ss: Char, tc: Option[Char] ) {

    /**
      * Returns true if the separators are the ones recommended by HL7
      */
    def areRecommended = fs == '|' && cs == '^' && rs == '~' &&
                         ec == '\\' && ss == '&' && tc.getOrElse('#') =='#'
    
    /**
      * Returns the list of separators used more than once
      */
    def getDuplicates = { val l = toList; l.diff( l.distinct ).distinct }
    
    /**
      * Returns a list containing the separators
      */
    def toList = if( tc.isDefined ) List( fs, cs, rs, ec, ss, tc.get )
                 else List( fs, cs, rs, ec, ss)
  }

  /**
    * Trim the space on right of the string
    */
  def trimRight(s: String) = s.replaceAll("\\s+$", "")

  /**
    * Splits the string `s' on the character `sep' and returns a tuple of
    * (Int, String) where the Int represent the index of the sub string + `col'
    */
  def split(sep: Char, s: String, col: Int): Array[(Int, String)] = {
    var column = col
    val trimmed = trimRight( s )

    trimmed.split(sep) map { ss =>
      val r = column -> ss
      column += ss.length + 1;
      r
    }
  }

}
