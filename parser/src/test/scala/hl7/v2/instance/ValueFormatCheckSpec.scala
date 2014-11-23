package hl7.v2.instance

import org.specs2.mutable.Specification

import FormatChecker._

class ValueFormatCheckSpec extends Specification {

  "Value.checkNumberFormat( x: String ) " should {

    val validNM  = Seq("1", "+1", "-1", "1.", "+1.", "-1.", "1.0", "+1.0", "-1.0")
    s"return None  for ${validNM.mkString("{ ", ", ", " }")}" in {
      validNM map ( checkNumberFormat( _ ) === None )
    }

    // Beside +, - and . No other characters (including whitespaces) is allowed
    val invalidNM = Seq(" 1", "1 ", "1e7" )
    s"return Some(...) for ${invalidNM.mkString("{ ", ", ", " }")}" in {
      invalidNM map { s => checkNumberFormat( s ) ===
        Some( s"Number($s) is invalid. The format should be: [+|-]digits[.digits]" )
      }
    }

    def m(s: String) =
      s"Number($s) is invalid. The format should be: [+|-]digits[.digits]"
  }

  // Date format
  "Value.checkDateFormat( x: String ) " should {

    val valids = Seq("0000", "000001", "00000112")
    s"return None for ${valids.mkString("{", ", ", "}" )}" in {
      valids map ( checkDateFormat(_) === None )
    }

    val invalids = Seq("12", " 2012", "2012 ", "000013", "00001232")
    s"return Some(...) for ${invalids.mkString("{", ", ", "}" )}" in {
      invalids map ( checkDateFormat(_) must beSome )
    }

    s"return Some(February cannot have 30 days) for 20140230" in {
      checkDateFormat("20140230") ===
        Some("Date(20140230) is invalid. February cannot have 30 days")
    }

    s"return Some(February cannot have 31 days) for 20140231" in {
      checkDateFormat("20140231") ===
        Some("Date(20140231) is invalid. February cannot have 31 days")
    }

    "return Some(February cannot have 29 days since 2015 is not a leap year) for 20150229" in {
      checkDateFormat("20150229") ===
        Some("Date(20150229) is invalid. February cannot have 29 days since 2015 is not a leap year")
    }

    "return None for 20160229 since 2016 is a leap year" in {
      checkDateFormat("20160229") === None
    }

    val ms = Seq("04", "06", "09", "11")
    s"return Some(...) for 2016{04, 06, 09, 11}31" in {
      ms map { m =>
        checkDateFormat(s"2016${m}31") ===
          Some(s"Date(2016${m}31) is invalid. The month $m cannot have 31 days")
      }
    }
  }

  "Value.checkTime( x: String ) " should {

    val valids = Seq("00", "0000", "000000.0", "000000.0000", "00+0000", "00-0000")
    s"return None for ${valids.mkString("{", ", ", "}")}" in {
      valids map (checkTimeFormat(_) === None)
    }

    val invalids = Seq("0", "0060", "005960", " 00", "00 ", "00-123", "00+12345", "000000.", "+1200")
    s"return Some(...) for ${invalids.mkString("{", ", ", "}")}" in {
      invalids map { s => checkTimeFormat(s) ===
        Some(s"Time($s) is invalid. The format should be: HH[MM[SS[.S[S[S[S]]]]]][+/-ZZZZ]")
      }
    }
  }

  "Value.checkTimeZone( x: String ) " should {

    val valids = Seq("+0000", "-0000", "+1234")
    s"return None for ${valids.mkString("{", ", ", "}")}" in {
      valids map (checkTimeZoneFormat(_) === None)
    }

    val invalids = Seq(" +0000", "+ 0000", "+00", "+2400", " +0060")
    s"return Some(...) for ${invalids.mkString("{", ", ", "}")}" in {
      invalids map { s => checkTimeZoneFormat(s) ===
        Some(s"TimeZone($s) is invalid. The format should be: [+|-]HHMM")
      }
    }
  }

  "Value.checkDateTime( x: String ) " should {

    val valids = Seq("0000", "0000+1234", "0000010100", "0000010100+1234")
    s"return None for ${valids.mkString("{", ", ", "}")}" in {
      valids map (checkDateTimeFormat(_) === None)
    }

    s"return Some(DateTime(0000+2400) is invalid. TimeZone(+2400) is invalid... )" in {
      checkDateTimeFormat("0000+2400") ===
        Some(s"DateTime(0000+2400) is invalid. TimeZone(+2400) is invalid. The format should be: [+|-]HHMM")
    }

    val invalids = Seq("00001301", "0000+2400", "0000120124")
    s"return Some(...) for ${invalids.mkString("{", ", ", "}")}" in {
      invalids map (checkDateTimeFormat(_) must beSome)
    }
  }

}
