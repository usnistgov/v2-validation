package hl7.v2.instance

case class Separators( fs: Char, cs: Char, rs: Char, ec: Char, ss: Char, tc: Option[Char], dn: Option[Char], ts: Option[Char] ) {

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
  def toList = {
    var l = List( fs, cs, rs, ec, ss)
    //println("separators list size: "+l.size+" | tc.isDefined? "+tc.isDefined+" | dn.isDefined? "+dn.isDefined+" "+dn.get.toString+" | ts.isDefined? "+ts.isDefined+" "+ts.get.toString)
    if( tc.isDefined ) {
      l = l ::: List(tc.get)
    }
    if( dn.isDefined ) {
      //println("dn.isDefined --> "+dn.get.toString)
      l = l ::: List(dn.get)
    }
    if( ts.isDefined ) {
      //println("ts.isDefined --> "+ts.get.toString)
      l = l ::: List(ts.get)
    }
    //println("separators list size after tc, dn, ts: "+l.size)
    l
  }

}


object Separators {

  def apply(fs: Char, cs: Char, rs: Char, ec: Char, ss: Char): Separators =
    Separators(fs, cs, rs, ec, ss, None, None, None)

  def apply(fs: Char, cs: Char, rs: Char, ec: Char, ss: Char, tc: Option[Char]): Separators =
    Separators(fs, cs, rs, ec, ss, tc, None, None)

  def apply(fs: Char, cs: Char, rs: Char, ec: Char, ss: Char, tc: Char): Separators =
    Separators(fs, cs, rs, ec, ss, Some(tc), None, None)

  def apply(fs: Char, cs: Char, rs: Char, ec: Char, ss: Char,
            tc: Char, dn: Char, ts: Char): Separators = Separators(fs, cs, rs, ec, ss, Some(tc), Some(dn), Some(ts))
}
