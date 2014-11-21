package hl7.v2.instance

object FormatChecker {

  /**
    * Returns the format error for the object t if any
    * @param t - The object to be checked
    * @param e - The format checker for t
    * @return The format error  if any
    */
  def checkFormat[T](t: T)(implicit  e: Format[T]): Option[String] = e.check(t)
}


trait Format[T] {

  /**
    * Returns the format error for the object t if any
    * @param t - The object to be checked
    * @return The format error  if any
    */
  def check(t: T): Option[String]
}

object Format {

  /**
    * Number format checker
    */
  implicit object NumberFormat extends Format[Number] {
    val NMFormat = "(\\+|\\-)\\d+(\\.\\d*)?"

    override def check(v: Number): Option[String] =
      if(v.raw matches NMFormat) None else Some(s"The format of $v is invalid.")
  }

  /**
    * Date format checker
    */
  implicit object DateFormat extends Format[Date] {
    val DTFormat = """\d{4}((0[1-9]|1[0-2])(0[1-9]|[1-2][0-9]|3[0-1])?)?"""

    /**
      * Checks the date format and allowed values
      * and returns the error message if any
      * @param s - The date as string
      * @return None if valid
      */
    private def checkDate(s: String): Option[String] =
      if( s matches DTFormat ) {
        val y = s take 4
        val m = s drop 4 take 2
        val d = s drop 6 take 2
        if( "02" == m ) {
          if( d == "30" || d == "31" ) Some(s"February cannot have $d days")
          else if( d == "29" && y.toInt % 4 != 0 )
            Some(s"February cannot have 29 days since $y is not a leap year")
          else None
        }
        else if( d == "31" && ("04" == m || "06" == m || "09" == m || "11" == m) )
          Some(s"The month $m cannot have 31 days") //TODO give the month name
        else None
      } else Some("Date(DT) format should be: YYYY[MM[DD]]")

    override def check(v: Date): Option[String] =
      checkDate( v.raw ) map { m => s"The format of $v is invalid. $m" }
  }
}