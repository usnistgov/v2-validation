package hl7.v2.instance

trait EscapeSeqHandler {

  /**
    * Returns a new string with HL7 separators escaped
    * @param s - The string to be un-escaped
    * @param separators - The separators
    * @return A new string with HL7 separators escaped
    */
  def escape(s: String)(implicit  separators: Separators): String = {

    val( fs, cs, rs, ec, ss, otc) = Separators.unapply( separators ).get

    val r = s.replaceAllLiterally( ec.toString, s"${ec}E$ec" )
             .replaceAllLiterally( fs.toString, s"${ec}F$ec" )
             .replaceAllLiterally( cs.toString, s"${ec}S$ec" )
             .replaceAllLiterally( ss.toString, s"${ec}T$ec" )
             .replaceAllLiterally( rs.toString, s"${ec}R$ec" )

    otc match {
      case Some(x) => r.replaceAllLiterally( x.toString, s"${ec}P$ec" )
      case None    => r
    }
  }

  /**
    * Returns a new string with HL7 basic escape sequence replaced
    * @param s - The string to be un-escaped
    * @param separators - The separators
    * @return A new string with HL7 basic escape sequence replaced
    */
  def unescape(s: String)(implicit  separators: Separators): String = {
    val( fs, cs, rs, ec, ss, otc) = Separators.unapply( separators ).get

    val r = s.replaceAllLiterally( s"${ec}F$ec",  fs.toString )
             .replaceAllLiterally( s"${ec}S$ec",  cs.toString )
             .replaceAllLiterally( s"${ec}T$ec",  ss.toString )
             .replaceAllLiterally( s"${ec}R$ec",  rs.toString )
             .replaceAllLiterally( s"${ec}E$ec",  ec.toString )

    otc match {
      case Some(x) => r.replaceAllLiterally( s"${ec}P$ec",  x.toString )
      case None    => r
    }
  }

}
