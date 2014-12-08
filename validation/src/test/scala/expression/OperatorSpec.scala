package expression

import org.specs2.Specification

import Operator.EQ
import Operator.GE
import Operator.GT
import Operator.LE
import Operator.LT
import Operator.NE

import hl7.v2.instance.{Number, TimeZone}

class OperatorSpec extends Specification { def is = s2"""
  Operator specification

    Number( "1" ) LT Number( "2" ) should be true  $e1
    Number( "2" ) GT Number( "1" ) should be true  $e2
    Number( "1" ) LE Number( "1" ) should be true  $e3
    Number( "1" ) GE Number( "1" ) should be true  $e4
    Number( "1" ) EQ Number( "1" ) should be true  $e5
    Number( "1" ) NE Number( "2" ) should be true  $e6
  """

  implicit val dtz: Option[TimeZone] = None

  def e1 = LT.eval( Number( "1" ), Number( "2" ) ) must beSuccessfulTry.withValue(true)

  def e2 = GT.eval( Number( "2" ), Number( "1" ) ) must beSuccessfulTry.withValue(true)

  def e3 = LE.eval( Number( "1" ), Number( "1" ) ) must beSuccessfulTry.withValue(true)

  def e4 = GE.eval( Number( "1" ), Number( "1" ) ) must beSuccessfulTry.withValue(true)

  def e5 = EQ.eval( Number( "1" ), Number( "1" ) ) must beSuccessfulTry.withValue(true)

  def e6 = NE.eval( Number( "1" ), Number( "2" ) ) must beSuccessfulTry.withValue(true)
}