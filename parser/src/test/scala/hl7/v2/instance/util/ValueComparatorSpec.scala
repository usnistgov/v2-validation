package hl7.v2.instance.util

import org.specs2.{ScalaCheck, Specification}

import ValueComparator._
import hl7.v2.instance._


class ValueComparatorSpec extends Specification with ScalaCheck { def is = s2"""

  Number Comparison Specification
    Comparing number with a Date should fail                                    $n1
    Comparing number with a Time should fail                                    $n2
    Comparing number with a DateTime should fail                                $n3
    Comparing number with another comparable should fail if one format is invalid $n4
    Comparing number with another comparable should succeed if both formats are valid $n5


"""

  val dtz = Some( TimeZone("+0000") )

  def n1 = compareTo(Number("1"), Date("2014")) must
    beFailedTry.withThrowable[Exception]{
      "\\QNumber(1) is not comparable to Date(2014).\\E"
    }

  def n2 = compareTo(Number("1"), Time("00", dtz)) must
    beFailedTry.withThrowable[Exception]{
      s"\\QNumber(1) is not comparable to ${Time("00", dtz)}.\\E"
    }

  def n3 = compareTo(Number("1"), DateTime("2014", dtz)) must
    beFailedTry.withThrowable[Exception]{
      s"\\QNumber(1) is not comparable to ${DateTime("2014", dtz)}.\\E"
    }

  def n4 = compareTo(Number("1"), Number("1.0E2")) must
    beFailedTry.withThrowable[Exception] {
      "\\Q1.0E2 is not a valid Number. The format should be: [+|-]digits[.digits]\\E"
    }

  def n5 = prop { (i1: Int, i2: Int) =>
    (i1 compareTo i2) === compareTo(Number(s"$i1"), Number(s"$i2")).get and
    compareTo(Number(s"$i1"), Number(s"$i2")) === compareTo(Number(s"$i1"), Text(s"$i2"))
  }

  //TODO: TO BE COMPLETED

}
