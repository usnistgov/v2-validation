package hl7.v2.instance

import scala.annotation.tailrec

trait EscapeSeqHandler {

  /**
    * Returns a new string with HL7 separators escaped
    * @param s - The string to be un-escaped
    * @param separators - The separators
    * @return A new string with HL7 separators escaped
    */
  def escape(s: String)(implicit  separators: Separators): String = {

    val( fs, cs, rs, ec, ss, otc, dn, ts) = Separators.unapply( separators ).get

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

    val( fs, cs, rs, ec, ss, otc, dn, ts) = Separators.unapply( separators ).get

    val escapeTruncation = otc match { case None => false case _ => true }
    val efs = s"${ec}F$ec"
    val ecs = s"${ec}S$ec"
    val ess = s"${ec}T$ec"
    val ers = s"${ec}R$ec"
    val eec = s"${ec}E$ec"
    val etc = s"${ec}P$ec"

    @tailrec
    def f(sb: StringBuilder, s: String): String =
      s span ( _ != ec ) match {
        case (x, "") => sb.append(x).toString()
        case (x, y) if y.take(3) == efs => f( sb.append(x).append(fs), y drop 3 )
        case (x, y) if y.take(3) == ecs => f( sb.append(x).append(cs), y drop 3 )
        case (x, y) if y.take(3) == ess => f( sb.append(x).append(ss), y drop 3 )
        case (x, y) if y.take(3) == ers => f( sb.append(x).append(rs), y drop 3 )
        case (x, y) if y.take(3) == eec => f( sb.append(x).append(ec), y drop 3 )
        case (x, y) if escapeTruncation && y.take(3) == etc =>
          f( sb.append(x).append( otc.get ), y drop 3 )
        case (x, y) => f( sb.append(x).append(ec), y drop 1 )
      }

    f( new StringBuilder, s )
  }

}

//This code is a bogus: \S\F\F\ will unescaped as \S|F\ instead of ^F|
/*def unescape(s: String)(implicit  separators: Separators): String = {
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
}*/
