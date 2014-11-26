package hl7.v2.instance
package value

import org.specs2.Specification

class FormatCheckerSpec extends Specification with FormatChecker { def is = s2"""

    Number format checking should succeed for valid numbers                         $n1
    Number format checking should fail for invalid numbers                          $n2

    Date format checking should succeed for valid dates                             $d1
    Date format checking should fail for invalid dates                              $d2
    Date format checking should fail if the number of days in the month is invalid  $d3
    Date format checking should fail if february has 30 or 31 days                  $d4
    Date format checking should fail if february has 29 the year is not a leap year $d5

    Time format checking should succeed for valid times                             $t1
    Time format checking should fail for invalid times                              $t2

    DateTime format checking should succeed if only the date is present and it is valid    $dtm1
    DateTime format checking should succeed if the date and/or time portions are valid     $dtm2
    DateTime format checking should succeed if only a valid date and time zone are present $dtm3
    DateTime format checking should fail if the date is invalid                            $dtm4
    DateTime format checking should fail if the date is valid and the time is invalid      $dtm5
    DateTime format checking should fail for a valid date, missing time and invalid time zone $dtm6
"""

  // Number checks
  val validNM = Seq("1", "+1", "-1", "1.", "+1.", "-1.", "1.0", "+1.0", "-1.0")
  def n1 = validNM map { s => checkFormat( Number(s) ) === None }

  val invalidNM = Seq(" 1", "1 ", "1e7")
  def n2 = invalidNM map { s => checkFormat( Number(s) ) === nmErr(s) }

  // Date checks
  val validDT = Seq("0000", "000001", "00000112", "20160229")
  def d1 = validDT map { s => checkFormat( Date(s) ) === None }

  val invalidDT = Seq("12", " 2012", "2012 ", "000013", "00001232")
  def d2 = invalidDT map { s => checkFormat( Date(s) ) === dtErr(s) }

  val invalidDaysInMonth = Seq("20140431", "20140631", "20140931", "20141131")
  def d3 = invalidDaysInMonth map { s => checkFormat(Date(s)) === daysInMonthErr(s) }

  val invalidDaysInFebruary = Seq("20140230", "20140231")
  def d4 = invalidDaysInFebruary map { s =>
    checkFormat(Date(s)) === dtErr(s, s"February cannot have ${s drop 6} days.")
  }

  def d5 = checkFormat(Date("20150229")) === dtErr("20150229",
    s"February cannot have 29 days since 2015 is not a leap year.")

  val dtz = None

  // Time checks
  val validTM = Seq("00", "0000", "000000.0", "000000.0000", "00+0000", "00-0000")
  def t1 = validTM map (s => checkFormat( Time(s, dtz) ) === None)

  val invalidTM = Seq("0", "0060", "005960", " 00", "00 ", "00-123", "00+12345", "000000.", "+1200")
  def t2 = invalidTM map (s => checkFormat( Time(s, dtz) ) === tmErr(s))

  // DateTime checks
  def dtm1 = validDT map { s => checkFormat( DateTime(s, dtz) ) === None }

  def dtm2 = validTM map { s => checkFormat(DateTime(s"20160229$s", dtz)) === None }

  def dtm3 = checkFormat(DateTime(s"2016+1234", dtz)) === None

  def dtm4 = checkFormat(DateTime(s"201400", dtz)) ===
    Some(s"201400 is not a valid DateTime(DTM). ${dtErr("201400").get}")

  def dtm5 = checkFormat(DateTime(s"201601010+12", dtz)) ===
    Some( s"201601010+12 is not a valid DateTime(DTM). ${tmErr("0+12").get}" )

  def dtm6 = checkFormat(DateTime(s"2016+12", dtz)) === Some {
    "2016+12 is not a valid DateTime(DTM). TimeZone(+12) is invalid." +
      " The format should be: +/-HHMM"
  }

  // Helpers

  private def nmErr(s: String) = Some(s"$s is not a valid Number(NM). " +
    s"The format should be: [+|-]digits[.digits]")

  private def dtErr(s: String) = Some(s"$s is not a valid Date(DT). " +
    s"The format should be: YYYY[MM[DD]]")

  private def dtErr(s: String, m: String) =
    Some(s"$s is not a valid Date(DT). $m")

  private def daysInMonthErr(s: String) =
    dtErr(s, s"The month ${s drop 4 take 2} cannot have 31 days.")

  private def tmErr(s: String) = Some(s"$s is not a valid Time(TM). " +
    s"The format should be: HH[MM[SS[.S[S[S[S]]]]]][+/-ZZZZ]")

}
