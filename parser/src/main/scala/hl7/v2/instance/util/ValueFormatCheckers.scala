package hl7.v2.instance.util

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

}
