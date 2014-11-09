package hl7.v2.parser

package object impl {

  type Line = (Int, String)

  val lineBreak = """\r?\n|\r\n?|\r\n""".r

  def quote(c: Char) = java.util.regex.Pattern.quote( c.toString )

  /**
    * Trim the space on right of the string
    */
  def trimRight(s: String) = s.replaceAll("\\s+$", "")

  /**
    * Splits the string `s' on the character `sep' and returns a tuple of
    * (Int, String) where the Int represent the index of the sub string + 'col'
    */
  def split(sep: Char, s: String, col: Int): Array[(Int, String)] = {
    var column = col
    val trimmed = trimRight( s )

    trimmed.split(sep) map { ss =>
      val r = column -> ss
      column += ss.length + 1
      r
    }
  }

}
