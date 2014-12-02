package hl7.v2.instance.util

import hl7.v2.instance.util.ValueFormatCheckers._
import org.specs2.mutable.Specification

class ValueFormatCheckersSpec extends Specification {

  "isValidNumberFormat" should {
    val valids = Seq("1", "+1", "-1", "1.", "+1.", "-1.", "1.0", "+1.0", "-1.0")
    s"returns true for ${valids.mkString("{'", "', '", "'}")}" in {
      valids map { s => isValidNumberFormat(s) === true }
    }
    val invalids = Seq("", " 1", "1 ", "1e7")
    s"returns false for ${invalids.mkString("{'", "', '", "'}")}" in {
      invalids map { s => isValidNumberFormat(s) === false }
    }
  }

  "isValidDateFormat" should {
    val valids = Seq("0000", "000001", "00000112", "20160229")
    s"returns true for ${valids.mkString("{'", "', '", "'}")}" in {
      valids map { s => isValidDateFormat(s) === true }
    }
    val invalids = Seq("12", " 2012", "2012 ", "000013", "00001232")
    s"returns false for ${invalids.mkString("{'", "', '", "'}")}" in {
      invalids map { s => isValidDateFormat(s) === false }
    }
  }

  "isValidTimeFormat" should {
    val valids = Seq("00", "0000", "000000.0", "000000.0000", "00+0000", "00-0000")
    s"returns true for ${valids.mkString("{'", "', '", "'}")}" in {
      valids map { s => isValidTimeFormat(s) === true }
    }
    val invalids = Seq("0", "0060", "005960", " 00", "00 ", "00-123",
              "00+12345", "000000.", "000000.00000","+1200", "00+")
    s"returns false for ${invalids.mkString("{'", "', '", "'}")}" in {
      invalids map { s => isValidTimeFormat(s) === false }
    }
  }

  "isValidDateTimeFormat" should {
    val valids = Seq("2016", "2016+1234", "201601", "20160101", "2016010100",
        "201601010000", "20160101000000", "20160101000000.0")
    s"returns true for ${valids.mkString("{'", "', '", "'}")}" in {
      valids map { s => isValidDateTimeFormat(s) === true }
    }
    val invalids = Seq("201600", "20160132", "2016010124", "201601010060",
      "20160101000060", "20160101000000.", "20160101000000.00000")
    s"returns false for ${invalids.mkString("{'", "', '", "'}")}" in {
      invalids map { s => isValidDateTimeFormat(s) === false }
    }
  }

  "isValidTimeZone" should {
    val valids = Seq("+0000", "-0000","+2300")
    s"returns true for ${valids.mkString("{'", "', '", "'}")}" in {
      valids map { s => isValidTimeZone(s) === true }
    }
    val invalids = Seq("0000", "-000", "+00000", "+2400", "+2360", " +0000", "+0000 ", "+ 0000")
    s"returns false for ${invalids.mkString("{'", "', '", "'}")}" in {
      invalids map { s => isValidTimeZone(s) === false }
    }
  }

  "Checking number of days in a Date/DateTime" should {

    val invalidDaysInMonth = Seq("20140431", "20140631", "20140931", "20141131")

    s"returns an error for ${ invalidDaysInMonth.mkString("{", ", ", "}") }" in {
      invalidDaysInMonth map { s =>
        val r = Some(s"$s is not a valid Date. The month ${
          s drop 4 take 2} cannot have 31 days.")
        checkDate(s) === r and checkDateTime(s) === r
      }
    }

    val invalidDaysInFebruary = Seq("20140230", "20140231")

    s"returns an error for ${ invalidDaysInFebruary.mkString("{", ", ", "}") }" in {
      invalidDaysInFebruary map { s =>
        val r = Some(s"$s is not a valid Date. February cannot have ${s drop 6} days.")
        checkDate(s) === r and checkDateTime(s) === r
      }
    }

    val d = "20150229"

    s"returns an error for $d. It has 29 days but it is not a leap year." in {
      val r = Some(s"$d is not a valid Date. " +
        s"February cannot have 29 days since 2015 is not a leap year.")
      checkDate(d) === r and checkDateTime(d) === r
    }

  }

}
