package hl7.v2.instance.util

import hl7.v2.instance.util.ValueConversionHelpers._
import org.scalacheck.{Prop, Gen}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

import scala.util.Success

class ValueConversionHelpersSpec extends Specification with ScalaCheck {

  def sGen  = Gen.oneOf("+", "-")
  def hGen  = Gen.oneOf( 1 to 23 )
  def mGen  = Gen.oneOf( 1 to 59 )
  def ssGen = Gen.oneOf( 1 to 9999)

  "timZoneToMilliSeconds" should {

    "Return a failure if the format is invalid" in {
      Seq("0000", "+00", "-0") map { s =>
        timeZoneToMilliSeconds(s) must beFailedTry
      }
    }

    s"Returns valid result if the time zone is valid" in {
      Prop.forAll(sGen, hGen, mGen) { (s: String, h: Int, m: Int) =>
        val tz = f"$s$h%02d$m%02d"
        val r = 1000 * (3600 * h + 60 * m )
        timeZoneToMilliSeconds(tz) === Success( if( s == "-") -r else r )
      }
    }

  }

  "timToMilliSeconds" should {

    "Return a failure if the format is invalid" in {
      Seq("0", "24") map { s =>
        timeToMilliSeconds(s) must beFailedTry
      }
    }

    s"Returns valid result if the time is valid" in {
      Prop.forAll(hGen, mGen, mGen, ssGen, sGen, hGen, mGen) {
        (hh: Int, mm: Int, ss: Int, ssss: Int, s: String, th: Int, tm: Int ) =>
        val t = f"$hh%02d$mm%02d$ss%02d.$ssss%04d$s$th%02d$tm%02d"
        val tz = 1000 * (3600 * th + 60 * tm )
        val r =  ssss + 1000 * (ss + 60 * mm + 3600 * hh)
        timeToMilliSeconds(t) === Success( if( s == "-") r - tz else r + tz )
      }
    }

  }

}
