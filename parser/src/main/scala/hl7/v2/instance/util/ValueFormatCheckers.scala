package hl7.v2.instance
package util

object ValueFormatCheckers {

  private val YY = "\\d{4}"                       // Year
  private val MM = "(?:0[1-9]|1[0-2])"            // Month
  private val DD = "(?:0[1-9]|[1-2][0-9]|3[0-1])" // Day

  private val ss, mm = "[0-5][0-9]"               // Second, Minute
  private val hh = "(?:[0-1][0-9]|2[0-3])"        // Hour
  private val TZ = s"(\\+|\\-)$hh$mm"             // Time Zone

  /**
    * Number format: [+|-]digits[.digits]
    */
  private val NM = "(\\+|\\-)?\\d+(\\.\\d*)?".r

  /**
    * Date format: YYYYMMDD
    */
  private val DT = s"$YY(?:$MM$DD?)?".r

  /**
    * Time format: HH[MM[SS[.S[S[S[S]]]]]][+/-ZZZZ]
    */
  private val TM = s"$hh(?:$mm(?:$ss(\\.\\d{1,4})?)?)?(?:$TZ)?".r

  /**
    * DateTime format: YYYY[MM[DD[HH[MM[SS[.S[S[S[S]]]]]]]]][+/-ZZZZ]
    */
  private val DTM = s"$YY(?:$MM(?:$DD(?:$hh(?:$mm(?:$ss(\\.\\d{1,4})?)?)?)?)?)?(?:$TZ)?".r

  /**
    * Returns true 's' has a valid Number format
    * @param s - The string to tested
    * @return True 's' has a valid Number format
    */
  def isValidNumberFormat(s: String): Boolean = NM.pattern.matcher(s).matches

  /**
    * Returns true 's' has a valid Date format
    * @param s - The string to tested
    * @return True 's' has a valid Date format
    */
  def isValidDateFormat(s: String): Boolean = DT.pattern.matcher(s).matches

  /**
    * Returns true 's' has a valid Time format
    * @param s - The string to tested
    * @return True 's' has a valid Time format
    */
  def isValidTimeFormat(s: String): Boolean = TM.pattern.matcher(s).matches

  /**
    * Returns true 's' has a valid DateTime format
    * @param s - The string to tested
    * @return True 's' has a valid DateTime format
    */
  def isValidDateTimeFormat(s: String): Boolean = DTM.pattern.matcher(s).matches

  /**
    * Returns true 's' has a valid TimeZone format
    * @param s - The string to tested
    * @return True 's' has a valid TimeZone format
    */
  def isValidTimeZone(s: String): Boolean = s matches TZ

  val  NMFormatErrMsg = "The format should be: [+|-]digits[.digits]"
  val  DTFormatErrMsg = "The format should be: YYYY[MM[DD]]"
  val  TMFormatErrMsg = "The format should be: HH[MM[SS[.S[S[S[S]]]]]][+/-ZZZZ]"
  val DTMFormatErrMsg = "The format should be: YYYY[MM[DD[HH[MM[SS[.S[S[S[S]]]]]]]]][+/-ZZZZ]"
  val TZFormatErrMsg  = "The format should be: +/-HHMM"

  /**
    * Checks the Number format and and returns the error message if any
    * @param s - The Number as a string
    * @return None if valid
    */
  def checkNumber(s: String): Option[String] =
    isValidNumberFormat(s) match {
      case true  => None
      case false => Some(s"$s is not a valid Number. $NMFormatErrMsg")
    }

  /**
    * Checks the Date format and and returns the error message if any
    * @param s - The Date as a string
    * @return None if valid
    */
  def checkDate(s: String): Option[String] =
    isValidDateFormat(s) match {
      case false => Some(s"$s is not a valid Date. $DTFormatErrMsg")
      case true  => checkNumberOfDaysInMonth(s)
    }

  /**
    * Checks the Time format and and returns the error message if any
    * @param s - The Time as a string
    * @return None if valid
    */
  def checkTime(s: String): Option[String] =
    isValidTimeFormat(s) match {
      case true  => None
      case false => Some(s"$s is not a valid Time. $TMFormatErrMsg")
    }

  /**
    * Checks the DateTime format and and returns the error message if any
    * @param s - The DateTime as a string
    * @return None if valid
    */
  def checkDateTime(s: String): Option[String] =
    isValidDateTimeFormat(s) match {
      case false => Some(s"$s is not a valid DateTime. $DTMFormatErrMsg")
      case true  => checkNumberOfDaysInMonth(s take 8)
    }

  def checkTimeZone(s: String): Option[String] =
    isValidTimeZone( s ) match {
      case true  => None
      case false => Some(s"$s is not a valid Time Zone. $TZFormatErrMsg")
    }

  /**
    * Checks the number of days in the month and returns the error if any
    * @param s - The Date as a string
    * @return The error message or None
    */
  private def checkNumberOfDaysInMonth(s: String): Option[String] = {
    val y = s take 4
    val m = s drop 4 take 2
    val d = s drop 6 take 2
    if ("02" == m && (d == "30" || d == "31") )
      Some(s"$s is not a valid Date. February cannot have $d days")
    else if (d == "29" && y.toInt % 4 != 0)
      Some(s"$s is not a valid Date. February cannot have 29 days since $y is not a leap year")
    else if (d == "31" && ("04" == m || "06" == m || "09" == m || "11" == m))
      Some(s"$s is not a valid Date. The month $m cannot have 31 days")
    else None
  }

}
