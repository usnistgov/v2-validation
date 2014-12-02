package hl7.v2.instance.util

import hl7.v2.instance.util.ValueConversionHelpers._
import org.specs2.mutable.Specification

import scala.util.Success

class ValueConversionHelpersSpec extends Specification {

  //FIXME TO BE COMPLETED

  //dateToDays is boggus

  "dateToDays" should {

    "Return a failure if the format is invalid" in {
      dateToDays("20141") must beFailedTry
    }

    "Compute the days correctly" in {
      Seq( 0 -> "0000", 29 -> "000002" ) map { t =>
        dateToDays(t._2) === Success(t._1)
      }
    }

  }

}
