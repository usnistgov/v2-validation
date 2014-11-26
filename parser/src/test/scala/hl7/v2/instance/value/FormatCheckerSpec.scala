package hl7.v2.instance
package value

import org.specs2.Specification

class FormatCheckerSpec extends Specification with FormatChecker { def is = s2"""

    Number format checking should succeed for valid numbers $n1
    Number format checking should fail for invalid numbers  $n2

    Date format checking should succeed for valid dates $d1
    Date format checking should fail for invalid dates $d2
    Date format checking should fail if the number of days in the month is invalid $d3
    Date format checking should fail if february has 30 or 31 days $d4
    Date format checking should fail if february has 29 the year is not a leap year $d5

    Time format checking should succeed for valid times $t1
    Time format checking should fail for invalid times $t2

    DateTime format checking should succeed if only the date is present and it is valid
    DateTime format checking should succeed if the date and/or time portions are valid
    DateTime format checking should succeed if only a valid date and time zone are present
    DateTime format checking should fail if one of the date or the time is invalid
    DateTime format checking should succeed if only a valid date and an invalid time zone are present
"""

  val validNM = Seq("1", "+1", "-1", "1.", "+1.", "-1.", "1.0", "+1.0", "-1.0")
  def n1 = validNM map { s => checkFormat( Number(s) ) === None }

  val invalidNM = Seq(" 1", "1 ", "1e7")
  def n2 = invalidNM map { s => checkFormat( Number(s) ) === Some( nmErr(s)) }

  val validDT = Seq("0000", "000001", "00000112", "20160229")
  def d1 = validDT map { s => checkFormat( Date(s) ) === None }

  val invalidDT = Seq("12", " 2012", "2012 ", "000013", "00001232")
  def d2 = invalidDT map { s => checkFormat( Date(s) ) === dtErr(s) }

  def d3 = Seq("04", "06", "09", "11") map { m =>
    val d = s"2014${m}31"
    checkFormat(Date(d)) === dtErr(d, s"The month $m cannot have 31 days.")
  }

  def d4 = Seq("30", "31") map { x =>
    val d = s"201402$x"
    checkFormat(Date(d)) === dtErr(d, s"February cannot have $x days.")
  }
  def d5 = checkFormat(Date("20150229")) === dtErr("20150229",
    s"February cannot have 29 days since 2015 is not a leap year.")

  val dtz = None

  val validTM = Seq("00", "0000", "000000.0", "000000.0000", "00+0000", "00-0000")
  def t1 = validTM map (s => checkFormat( Time(s, dtz) ) === None)

  val invalidTM = Seq("0", "0060", "005960", " 00", "00 ", "00-123", "00+12345", "000000.", "+1200")
  def t2 = invalidTM map (s => checkFormat( Time(s, dtz) ) === tmErr(s))

  private def nmErr(s: String) = s"$s is not a valid Number(NM). The format should be: [+|-]digits[.digits]"

  private def dtErr(s: String) =
    Some(s"$s is not a valid Date(DT). The format should be: YYYY[MM[DD]]")
  private def dtErr(s: String, m: String) = Some(s"$s is not a valid Date(DT). $m")

  private def tmErr(s: String) =
    Some(s"$s is not a valid Time(TM). The format should be: HH[MM[SS[.S[S[S[S]]]]]][+/-ZZZZ]")

}
