package hl7.v2.instance

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


object Separators {

  def apply(fs: Char, cs: Char, rs: Char, ec: Char, ss: Char): Separators =
    Separators(fs, cs, rs, ec, ss, None)

  def apply(fs: Char, cs: Char, rs: Char, ec: Char, ss: Char,
            tc: Char): Separators = Separators(fs, cs, rs, ec, ss, Some(tc))
}
