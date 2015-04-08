package hl7.v2.instance
package util

import scala.util.{Failure, Success, Try}

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
  //private val NM = "(\\+|\\-)?\\d+(\\.\\d*)?".r
  private val NM = "(\\+|\\-)?((\\d+(\\.\\d*)?)|(\\d*\\.\\d+))".r

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

  /**
    * Checks the Number format and returns the error message if any
    * @param s - The Number as a string
    * @return None if valid
    */
  def checkNumber(s: String): Option[String] =
    if( isValidNumberFormat(s) ) None else Some( numberFormatErr(s) )

  /**
    * Checks the Date format and returns the error message if any
    * @param s - The Date as a string
    * @return None if valid
    */
  def checkDate(s: String): Option[String] =
    if( isValidDateFormat(s) ) checkNumberOfDaysInMonth(s)
    else Some( dateFormatErr(s))

  /**
    * Checks the Time format and returns the error message if any
    * @param s - The Time as a string
    * @return None if valid
    */
  def checkTime(s: String): Option[String] =
    if( isValidTimeFormat(s) ) None else  Some( timeFormatErr(s) )

  /**
    * Checks the DateTime format and returns the error message if any
    * @param s - The DateTime as a string
    * @return None if valid
    */
  def checkDateTime(s: String): Option[String] =
    if( isValidDateTimeFormat(s) ) checkNumberOfDaysInMonth(s take 8)
    else Some( dateTimeFormatErr(s) )

  /**
    * Checks the TimeZone anr returns the error message if any
    * @param s - The TimeZone as string
    * @return None if valid
    */
  def checkTimeZone(s: String): Option[String] =
    if( isValidTimeZone(s) ) None else Some( timeFormatErr(s) )

  /**
    * Checks the Number and returns the raw value
    * @param s - The Number as string
    * @return A failure if the format is invalid
    */
  def checkNumberFormat(s: String): Try[String] =
    if( isValidNumberFormat(s) )  Success(s)
    else  Failure( new Exception(numberFormatErr(s)) )

  /**
    * Checks the Date and returns the raw value
    * @param s - The Date as string
    * @return A failure if the format is invalid
    */
  def checkDateFormat(s: String): Try[String] = checkDate(s) match {
    case None    => Success(s)
    case Some(m) => Failure( new Exception(m) )
  }

  /**
    * Checks the DateTime and returns the raw value
    * @param s - The DateTime as string
    * @return A failure if the format is invalid
    */
  def checkDateTimeFormat(s: String) = checkDateTime(s) match {
    case None    => Success(s)
    case Some(m) => Failure( new Exception(m) )
  }

  /**
    * Checks the Time and returns the raw value
    * @param s - The Time as string
    * @return A failure if the format is invalid
    */
  def checkTimeFormat(s: String): Try[String] =
    if( isValidTimeFormat(s) )  Success(s)
    else Failure( new Exception(timeFormatErr(s)) )

  /**
    * Checks the TimeZone and returns the raw value
    * @param s - The TimeZone as string
    * @return A failure if the format is invalid
    */
  def checkTimeZoneFormat(s: String): Try[String] =
    if( isValidTimeZone(s) ) Success(s)
    else Failure( new Exception(timeZoneFormatErr(s)) )

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
      Some(s"$s is not a valid Date. February cannot have $d days.")
    else if (d == "29" && y.toInt % 4 != 0)
      Some(s"$s is not a valid Date. February cannot have 29 days since $y is not a leap year.")
    else if (d == "31" && ("04" == m || "06" == m || "09" == m || "11" == m))
      Some(s"$s is not a valid Date. The month $m cannot have 31 days.")
    else None
  }

  private def numberFormatErr(s: String): String =
    s"$s is not a valid Number. The format should be: [+|-]digits[.digits]"

  private def dateFormatErr(s: String): String =
    s"$s is not a valid Date. The format should be: YYYY[MM[DD]]"

  private def timeFormatErr(s: String): String =
    s"$s is not a valid Time. The format should be: HH[MM[SS[.S[S[S[S]]]]]][+/-ZZZZ]"

  private def dateTimeFormatErr(s: String): String =
    s"$s is not a valid DateTime. The format should be: YYYY[MM[DD[HH[MM[SS[.S[S[S[S]]]]]]]]][+/-ZZZZ]"

  private def timeZoneFormatErr(s: String): String =
    s"$s is not a valid TimeZone. The format should be: +/-HHMM"

}
