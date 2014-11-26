package hl7.v2.instance
package value

trait FormatChecker {

  def checkFormat[T](t: T)(implicit x: Format[T]): Option[String] =
    x.check(t)
}

trait Format[T] {

  def check(t: T): Option[String]
}

object Format {

  /*****************************************************************************
   **************    Number format checking
   *****************************************************************************/
  private val NM = """(\+|\-)?\d+(\.\d*)?"""

  /**
    * Checks the format of a Number and returns the error if any
    * @param s - The number as string
    * @return The error message if any None otherwise
    */
  def checkNumberFormat(s: String): Option[String] =
    if( s matches NM ) None
    else Some {
      s"$s is not a valid Number(NM). The format should be: [+|-]digits[.digits]"
    }

  /**
    * Implicit object for checking the format of a Number
    */
  implicit object NumberFormatChecker extends Format[Number] {
    def check(n: Number): Option[String] = checkNumberFormat(n.raw)
  }

  /*****************************************************************************
   **************    Date format checking
   *****************************************************************************/

  private val YY = "\\d{4}"
  private val MM = "(?:0[1-9]|1[0-2])"
  private val DD = "(?:0[1-9]|[1-2][0-9]|3[0-1])"
  private val DT = s"($YY)(?:($MM)($DD)?)?".r

  /**
    * Checks the format of a Date and returns the error if any
    * @param s - The date as string
    * @return The error message if any None otherwise
    */
  def checkDateFormat(s: String): Option[String] = s match {
    case DT(y, m, d) =>
      checkDaysInMonth(y, m, d) map (msg => s"$s is not a valid Date(DT). $msg")
    case _ =>
      Some(s"$s is not a valid Date(DT). The format should be: YYYY[MM[DD]]")
  }

  /**
    * Implicit object for checking the format of a Date
    */
  implicit object DateFormatChecker extends Format[Date] {
    def check(d: Date): Option[String] = checkDateFormat(d.raw)
  }

  /**
    * Checks if the number of days in the month is valid
    * and returns the error message if any None otherwise.
    */
  private def checkDaysInMonth(y: String, m: String, d: String): Option[String] =
    if ("02" == m && (d == "30" || d == "31"))
      Some(s"February cannot have $d days.")
    else if ( "02" == m && !( y.toInt % 4 == 0) && d == "29")
      Some(s"February cannot have 29 days since $y is not a leap year.")
    else if (d == "31" && ("04" == m || "06" == m || "09" == m || "11" == m))
      Some(s"The month $m cannot have 31 days.")
    else None

  /*****************************************************************************
   **************    Time format checking
   *****************************************************************************/
  private val SS, mm = "[0-5][0-9]"
  private val HH = "(?:[0-1][0-9]|2[0-3])"
  private val TM = s"$HH($mm($SS(\\.\\d{1,4})?)?)?((\\+|\\-)?$HH$mm)?"

  /**
    * Checks the format of a Time and returns the error if any
    * @param s - The Time as string
    * @return The error message if any None otherwise
    */
  def checkTimeFormat(s: String): Option[String] =
    if(s matches TM) None
    else Some( s"$s is not a valid Time(TM). " +
      s"The format should be: HH[MM[SS[.S[S[S[S]]]]]][+/-ZZZZ]")

  /**
    * Implicit object for checking the format of a time
    */
  implicit object TimeFormatChecker extends Format[Time] {
    def check(t: Time): Option[String] = checkTimeFormat(t.raw)
  }

  /*****************************************************************************
   **************    DateTime format checking
   *****************************************************************************/
  private val DTM = s"(\\d{4,8})([^\\+\\-]*)((?:\\+|\\-)?.*)".r

  /**
    * Checks the format of a DateTime and returns the error if any
    * @param s - The DateTime as string
    * @return The error message if any None otherwise
    */
  def checkDateTimeFormat(s: String): Option[String] = s match {
    case DTM(ds, ts, tzs) =>
      checkDateFormat(ds) match {
        case Some(m)           => Some( dtmErr(s, m) )
        case None if ts != ""  => checkTimeFormat(s"$ts$tzs") map { dtmErr(s, _) }
        case None if tzs != "" =>    checkTimeZoneFormat(tzs) map { dtmErr(s, _) }
        case None              => None
      }
    case _ => Some( dtmErr(s, "The format should be: YYYY[MM[DD[HH[MM[SS[.S[S[S[S]]]]]]]][+/-ZZZZ]") )
  }

  /**
    * Implicit object for checking the format of a DateTime.
    */
  implicit object DateTimeFormatChecker extends Format[DateTime] {
    def check(d: DateTime): Option[String] = checkDateTimeFormat(d.raw)
  }

  private def dtmErr(s: String, m: String) = s"$s is not a valid DateTime(DTM). $m"

  /*****************************************************************************
   **************    TimeZone format checking
   *****************************************************************************/
  private val TZ = s"(\\+|\\-)?(${Time.HH})(${Time.MM})".r

  /**
    * Checks the format of a TimeZone and returns the error if any
    * @param s - The TimeZone as string
    * @return The error message if any None otherwise
    */
  def checkTimeZoneFormat(s: String): Option[String] =
    if( TZ.pattern.matcher(s).matches ) None
    else Some(s"TimeZone($s) is invalid. The format should be: +/-HHMM")

}
