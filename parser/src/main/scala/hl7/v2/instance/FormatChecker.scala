package hl7.v2.instance

object FormatChecker {

  val NMFormat = """(\+|\-)?\d+(\.\d*)?"""

  private val SS = "[0-5][0-9]"
  private val MM = SS
  private val HH = "([0-1][0-9]|2[0-3])"
  //FIXME It seems like valid TZ offsets are -12 +14 see: http://en.wikipedia.org/wiki/List_of_UTC_time_offsets
  private val TZ = s"(\\+|\\-)$HH$MM"
  private val TMFormat = s"$HH($MM($SS(\\.\\d{1,4})?)?)?($TZ)?"

  private val YYYY = "\\d{4}"
  private val mm   = "(0[1-9]|1[0-2])"
  private val DD   = "(0[1-9]|[1-2][0-9]|3[0-1])"

  private val DTFormat = s"$YYYY($mm($DD)?)?"

  /**
    * Checks the number format and returns the error message if any
    * @param s - The number as string
    * @return None if valid
    */
  def checkNumberFormat(s: String): Option[String] =
    if (s matches NMFormat) None
    else Some(s"Number($s) is invalid. The format should be: [+|-]digits[.digits]")

  /**
    * Checks the date format and allowed values
    * and returns the error message if any
    * @param s - The date as string
    * @return None if valid
    */
  def checkDateFormat(s: String): Option[String] =
    if (s matches DTFormat) {
      val y = s take 4
      val m = s drop 4 take 2
      val d = s drop 6 take 2
      if ("02" == m) {
        if (d == "30" || d == "31")
          Some(s"Date($s) is invalid. February cannot have $d days")
        else if (d == "29" && y.toInt % 4 != 0)
          Some(s"Date($s) is invalid. February cannot have 29 days since $y is not a leap year")
        else
          None
      }
      else if (d == "31" && ("04" == m || "06" == m || "09" == m || "11" == m))
        Some(s"Date($s) is invalid. The month $m cannot have 31 days") //TODO give the month name
      else None
    } else Some(s"Date($s) is invalid. The format should be: YYYY[MM[DD]]")

  /**
    * Checks the time format and returns the error message if any
    * @param s - The time as string
    * @return None if valid
    */
  def checkTimeFormat(s: String): Option[String] =
    if (s matches TMFormat) None
    else Some(s"Time($s) is invalid. The format should be: HH[MM[SS[.S[S[S[S]]]]]][+/-ZZZZ]")

  /**
    * Checks the date-time format and returns the error message if any
    * @param s - The date-time as string
    * @return None if valid
    */
  def checkDateTimeFormat(s: String): Option[String] = {
    val(dtm, tz) = splitOnTZ( s )
    checkDateFormat(dtm take 8) match {
      case Some(m) => Some(s"DateTime($s) is invalid. $m")
      case None    =>
        val tzErr = if(tz.nonEmpty) checkTimeZoneFormat(tz) else None
        tzErr match {
          case Some(m) => Some(s"DateTime($s) is invalid. $m")
          case None    =>
            val tm = dtm drop 8
            if( tm.isEmpty ) None
            else checkTimeFormat(tm) map (m => s"DateTime($s) is invalid. $m")
        }
    }
  }

  def checkTimeZoneFormat(s: String): Option[String] =
    if( s matches TZ ) None
    else Some(s"TimeZone($s) is invalid. The format should be: [+|-]HHMM")

  private
  def splitOnTZ(s: String): (String, String) = s span( c => c != '+' && c != '-' )
}