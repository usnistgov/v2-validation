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

  //TODO Implement TimeZone format tests

}
