package edi.ncpdp.script.parser

package object impl {

  type Line = (Int, String)

  val lineBreak = """\r?\n|\r\n?|\r\n""".r

  def validLinesRegex(implicit fs: Char, cs: String) = s"^([A-Z]{2}[A-Z0-9](${quote(fs)}.*)|UNA(${quoteS(cs)}.*))*".r

  def quote(c: Char) = java.util.regex.Pattern.quote( c.toString )

  def quoteS(c: String) = java.util.regex.Pattern.quote( c )

  /**
    * Trim the space on right of the string
    */
  def trimRight(s: String) = s.replaceAll("\\s+$", "")

  /**
    * Trim the line breaks on left of the string
    */
  def trimLineBreakLeft(s: String) = s.replaceAll("^\r?\n|\r\n?|\r\n","")

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
