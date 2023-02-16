package expression

import org.specs2.Specification
import Operator.EQ
import Operator.GE
import Operator.GT
import Operator.LE
import Operator.LT
import Operator.NE
import hl7.v2.instance.{Date, DateTime, Number, Text, Time, TimeZone, Value}

class OperatorSpec extends Specification { def is = s2"""
  Operator specification

  DEFAULT (Equivalent - Non Truncated) 
  
  NUMBER
    Number( "1" ) LT Number( "2" ) should be true  $e1
    Number( "2" ) GT Number( "1" ) should be true  $e2
    Number( "1" ) LE Number( "1" ) should be true  $e3
    Number( "1" ) GE Number( "1" ) should be true  $e4
    Number( "1" ) EQ Number( "1" ) should be true  $e5
    Number( "1" ) NE Number( "2" ) should be true  $e6
                                                       
  TEXT                                                     
    Text( "BC" ) LT Text( "AB" ) should be true  $et1
    Text( "AB" ) GT Text( "BC" ) should be true  $et2
    Text( "AB" ) LE Text( "AB" ) should be true  $et3
    Text( "AB" ) GE Text( "AB" ) should be true  $et4
    Text( "AB" ) EQ Text( "AB" ) should be true  $et5
    Text( "AB" ) NE Text( "BC" ) should be true  $et6

  DATE                                                       
    Date( "20200110" ) LT Date( "20200111" ) should be true  $ed1
    Date( "20200111" ) GT Date( "20200110" ) should be true  $ed2
    Date( "20200110" ) LE Date( "20200110" ) should be true  $ed3
    Date( "20200110" ) GE Date( "20200110" ) should be true  $ed4
    Date( "20200110" ) EQ Date( "20200110" ) should be true  $ed5
    Date( "20200110" ) NE Date( "20200111" ) should be true  $ed6

  DATETIME                                                       
    DateTime( "20200110100110+0000" ) LT DateTime( "20200110101010+0000" ) should be true  $edt1
    DateTime( "20200110101010+0000" ) GT DateTime( "20200110100110+0000" ) should be true  $edt2
    DateTime( "20200110100110+0000" ) LE DateTime( "20200110100110+0000" ) should be true  $edt3
    DateTime( "20200110100110+0000" ) GE DateTime( "20200110100110+0000" ) should be true  $edt4
    DateTime( "20200110100110+0000" ) EQ DateTime( "20200110100110+0000" ) should be true  $edt5
    DateTime( "20200110100110+0000" ) NE DateTime( "20200110101010+0000" ) should be true  $edt6
    
  TIME
    Time( "100110+0000" ) LT Time( "101010+0000" ) should be true  $ett1
    Time( "101010+0000" ) GT Time( "100110+0000" ) should be true  $ett2
    Time( "100110+0000" ) LE Time( "100110+0000" ) should be true  $ett3
    Time( "100110+0000" ) GE Time( "100110+0000" ) should be true  $ett4
    Time( "100110+0000" ) EQ Time( "100110+0000" ) should be true  $ett5
    Time( "100110+0000" ) NE Time( "101010+0000" ) should be true  $ett6      
                                                       
  EQUALITY 
                                                       
  Text
    Equivalent
      "ABC" EQ "AbC" should be true ${te1()}
      "ABC" EQ "ABC" should be true ${te2()}
    Identical
      "ABC" EQ "AbC" should be false ${ti1()}
      "ABC" EQ "ABC" should be true  ${ti2()}
    Truncated
      "ABCD" EQ "ABC" should be true ${tt1()}

  Number
    Equivalent
      "1.0" EQ "+001" should be true ${ne1()}
      "1.0" EQ "1.01" should be false ${ne2()}
    Identical
      "1.0" EQ "+001" should be false ${ni1()}
      "1.0" EQ "1.01" should be false ${ni2()}
      "1.0" EQ "1.0" should be true ${ni3()}
    Truncated
      "1.0" EQ "+001" should be true ${nt1()}
      "1.0" EQ "1.01" should be true ${nt2()}
      "1.0" EQ "1.0" should be true ${nt3()}
  Date
    Equivalent
      "20200125" EQ "20200125" should be true ${de1()}
      "20200125" EQ "20200126" should be false ${de2()}
    Identical
      "20200125" EQ "20200125" should be true ${di1()}
      "20200125" EQ "20200126" should be false ${di2()}
    Truncated
      "202001" EQ "20200125" should be true ${dt1()}
      "2020" EQ "20200126" should be true ${dt2()}

  DateTime
    Equivalent
      "20200125102412+0000" EQ "20200125062412-0400" should be true ${dte1()}
      "20200125102412+0000" EQ "20200125062412+0000" should be false ${dte2()}
      "20200125102412" EQ "20200125062412-0400" should be true (default TZ +0000) ${dte3()}
      "20200125+0000" EQ "20200125-0400" should be true ${dte4()}
      "20200125102412" EQ "20200125102412" should be true (no default TZ) ${dte5()}
      "20200125102412" EQ "20200125102413" should be false (no default TZ) ${dte6()}
      "20200125102412" EQ "20200125102412+0000" should fail stating No TimeZone defined (no default TZ) ${dte7()}
      "20200127130000-0600" EQ "20200127140000-0500" should be true ${dte8()}
    Identical
      "20200125102412+0000" EQ "20200125102412+0000" should be true ${dti1()}
      "20200125102412+0000" EQ "20200125062412-0400" should be false ${dti2()}
      "20200125102412" EQ "20200125062412-0400" should be false (default TZ +0000) ${dti3()}
      "20200125+0000" EQ "20200125-0400" should be false ${dti4()}
      "20200125102412" EQ "20200125102412" should be true (no default TZ) ${dti5()}
      "20200125102412" EQ "20200125102413" should be false (no default TZ) ${dti6()}
      "20200125102412" EQ "20200125102412+0000" should fail stating No TimeZone defined (no default TZ) ${dti7()}
    Truncated
      "20200125062412+0000" EQ "20200125062412+0000" should be true ${dtt1()}
      "20200125062412-0400" EQ "20200125062412+0000" should be false ${dtt2()}
      "20200125062412" EQ "20200125062412+0000" should be true ${dtt3()}
      "202001250624" EQ "20200125062412+0000" should be true ${dtt4()}
      "2020012506" EQ "20200125062412+0000" should be true ${dtt5()}
      "20200125" EQ "20200125062412+0000" should be true ${dtt6()}
      "202001" EQ "20200125062412+0000" should be true ${dtt7()}
      "2020" EQ "20200125062412+0000" should be true ${dtt8()}
      "20200125102412+0000" EQ "202001250624-0400" should be true ${dtt9()}
      "20200125102412" EQ "202001250624-0400" should be true (default TZ +0000) ${dtt10()}
      "20200125+0000" EQ "20200125-0400" should be true ${dtt11()}
      "20200125+0000" EQ "20200125062412-0400" should be true ${dtt12()}
      "20200125102412" EQ "20200125102412" should be true (no default TZ) ${dtt13()}
      "2020012510" EQ "20200125102412" should be true (no default TZ) ${dtt14()}
      "20200125102412" EQ "20200125102413" should be false (no default TZ) ${dtt15()}
      "20200125102412" EQ "20200125102412+0000" should be true (no default TZ) ${dtt16()}
                                                       
  Time
    Equivalent
      "102412+0000" EQ "062412-0400" should be true ${tte1()}
      "102412+0000" EQ "062412+0000" should be false ${tte2()}
      "102412" EQ "062412-0400" should be true (default TZ +0000) ${tte3()}
      "102412" EQ "102412" should be true (no default TZ) ${tte5()}
      "102412" EQ "102413" should be false (no default TZ) ${tte6()}
      "102412" EQ "102412+0000" should fail stating No TimeZone defined (no default TZ) ${tte7()}
      "130000-0600" EQ "140000-0500" should be true ${tte8()}
    Identical
      "102412+0000" EQ "102412+0000" should be true ${{tti1()}}
      "102412+0000" EQ "062412-0400" should be false ${tti2()}
      "102412" EQ "062412-0400" should be false (default TZ +0000) ${tti3()}
      "102412" EQ "102412" should be true (no default TZ) ${tti5()}
      "102412" EQ "102413" should be false (no default TZ) ${tti6()}
      "102412" EQ "102412+0000" should fail stating No TimeZone defined (no default TZ) ${tti7()}
    Truncated
      "062412+0000" EQ "062412+0000" should be true ${ttt1()}
      "062412-0400" EQ "062412+0000" should be false ${ttt2()}
      "062412" EQ "062412+0000" should be true ${ttt3()}
      "0624" EQ "062412+0000" should be true ${ttt4()}
      "06" EQ "062412+0000" should be true ${ttt5()}
      "102412+0000" EQ "0624-0400" should be true ${ttt9()}
      "102412" EQ "0624-0400" should be true (default TZ +0000) ${ttt10()}
      "102412" EQ "102412" should be true (no default TZ) ${ttt13()}
      "10" EQ "102412" should be true (no default TZ) ${ttt14()}
      "102412" EQ "102413" should be false (no default TZ) ${ttt15()}
      "102412" EQ "102412+0000" should be true (no default TZ) ${ttt16()}
  """

  implicit val dtz: Option[TimeZone] = Some(TimeZone("+0000"))

  def eval[T <: Value](truncated: Boolean, identical: Boolean, creator: String => T, ops: List[Operator])(v1: String, v2: String, expectation: Boolean) = {
    ops.map(OP => {
      OP.eval(creator(v1), creator(v2), ComparisonMode(identical, truncated))  must beSuccessfulTry.withValue(expectation)
    })
  }
  
  def evalDefault(v1: Value, op: Operator, v2: Value, expectation: Boolean) = {
    op.eval(v1, v2, ComparisonMode(false, false))  must beSuccessfulTry.withValue(expectation)
  }
  
  val eqOp = List(EQ)
  def textEq = eval[Text](truncated = false, identical = false, Text.apply, eqOp) _
  def textId = eval[Text](truncated = false, identical = true, Text.apply, eqOp) _
  def textTrunc = eval[Text](truncated = true, identical = false, Text.apply, eqOp) _
  def numberEq = eval[Number](truncated = false, identical = false, Number.apply, eqOp) _
  def numberId = eval[Number](truncated = false, identical = true, Number.apply, eqOp) _
  def numberTrunc = eval[Number](truncated = true, identical = false, Number.apply, eqOp) _
  def dateEq = eval[Date](truncated = false, identical = false, Date.apply, eqOp) _
  def dateId = eval[Date](truncated = false, identical = true, Date.apply, eqOp) _
  def dateTrunc = eval[Date](truncated = true, identical = false, Date.apply, eqOp) _
  def dateTimeEq = eval[DateTime](truncated = false, identical = false, DateTime.apply, eqOp) _
  def dateTimeId = eval[DateTime](truncated = false, identical = true, DateTime.apply, eqOp) _
  def dateTimeTrunc = eval[DateTime](truncated = true, identical = false, DateTime.apply, eqOp) _
  def timeEq = eval[Time](truncated = false, identical = false, Time.apply, eqOp) _
  def timeId = eval[Time](truncated = false, identical = true, Time.apply, eqOp) _
  def timeTrunc = eval[Time](truncated = true, identical = false, Time.apply, eqOp) _
  
  // Text Eq
  def te1() = textEq("ABC", "AbC", true)
  def te2() = textEq("ABC", "ABC", true)
  // Text Id
  def ti1() = textId("ABC", "AbC", false)
  def ti2() = textId("ABC", "ABC", true)
  // Text Trunc
  def tt1() = textTrunc("ABCD", "ABC", true)

  // Number Eq
  def ne1() = numberEq("1.0", "+001", true)
  def ne2() = numberEq("1.0", "1.01", false)
  // Number Id
  def ni1() = numberId("1.0", "+001", false)
  def ni2() = numberId("1.0", "1.01", false)
  def ni3() = numberId("1.0", "1.0", true)
  // Number Trunc
  def nt1() = numberTrunc("1.0", "+001", true)
  def nt2() = numberTrunc("1.0", "1.01", true)
  def nt3() = numberTrunc("1.0", "1.0", true)

  // Date Eq
  def de1() = dateEq("20200125", "20200125", true)
  def de2() = dateEq("20200125", "20200126", false)
  // Date Id
  def di1() = dateId("20200125", "20200125", true)
  def di2() = dateId("20200125", "20200126", false)
  // Date Trunc
  def dt1() = dateTrunc("202001", "20200125", true)
  def dt2() = dateTrunc("2020", "20200125", true)

  // DateTime Eq
  def dte1() = dateTimeEq("20200125102412+0000", "20200125062412-0400", true)
  def dte2() = dateTimeEq("20200125102412+0000", "20200125062412+0000", false)
  def dte3() = dateTimeEq("20200125102412", "20200125062412-0400", true)
  def dte4() = dateTimeEq("20200125+0000", "20200125-0400", true)
  def dte5() = {
    implicit val dtz: Option[TimeZone] = None
    dateTimeEq("20200125102412", "20200125102412", true)
  }
  def dte6() = {
    implicit val dtz: Option[TimeZone] = None
    dateTimeEq("20200125102412", "20200125102413", false)
  }
  def dte7() = {
    implicit val dtz: Option[TimeZone] = None
    eqOp.map(OP => {
      OP.eval(DateTime("20200125102412"), DateTime("20200125102412+0000"))  must beFailedTry.like { case e: Exception => e.getMessage must beEqualTo("Time Zone is missing and no default is set.") }
    })
  }
  def dte8() = dateTimeEq("20200127130000-0600", "20200127140000-0500", true)

  // DateTime Id
  def dti1() = dateTimeId("20200125102412+0000", "20200125102412+0000", true)
  def dti2() = dateTimeId("20200125102412+0000", "20200125062412-0400", false)
  def dti3() = dateTimeId("20200125102412", "20200125062412-0400", false)
  def dti4() = dateTimeId("20200125+0000", "20200125-0400", false)
  def dti5() = {
    implicit val dtz: Option[TimeZone] = None
    dateTimeId("20200125102412", "20200125102412", true)
  }
  def dti6() = {
    implicit val dtz: Option[TimeZone] = None
    dateTimeId("20200125102412", "20200125102413", false)
  }
  def dti7() = {
    implicit val dtz: Option[TimeZone] = None
    eqOp.map(OP => {
      OP.eval(DateTime("20200125102412"), DateTime("20200125102412+0000"), new ComparisonMode(true, false))  must beFailedTry.like { case e: Exception => e.getMessage must beEqualTo("Time Zone is missing and no default is set.") }
    })
  }
  // DateTime Trunc
  def dtt1() = dateTimeTrunc("20200125062412+0000", "20200125062412+0000", true)
  def dtt2() = dateTimeTrunc("20200125062412-0400", "20200125062412+0000", false)
  def dtt3() = dateTimeTrunc("20200125062412", "20200125062412+0000", true)
  def dtt4() = dateTimeTrunc("202001250624", "20200125062412-0000", true)
  def dtt5() = dateTimeTrunc("2020012506", "20200125062412-0000", true)
  def dtt6() = dateTimeTrunc("20200125", "20200125062412-0000", true)
  def dtt7() = dateTimeTrunc("202001", "20200125062412-0000", true)
  def dtt8() = dateTimeTrunc("2020", "20200125062412-0000", true)
  def dtt9() = dateTimeTrunc("20200125102412+0000", "202001250624-0400", true)
  def dtt10() = dateTimeTrunc("20200125102412", "202001250624-0400", true)
  def dtt11() = dateTimeTrunc("20200125+0000", "20200125-0400", true)
  def dtt12() = dateTimeTrunc("20200125+0000", "20200125062412-0400", true)
  def dtt13() = {
    implicit val dtz: Option[TimeZone] = None
    dateTimeTrunc("20200125102412", "20200125102412", true)
  }
  def dtt14() = {
    implicit val dtz: Option[TimeZone] = None
    dateTimeTrunc("2020012510", "20200125102412", true)
  }
  def dtt15() = {
    implicit val dtz: Option[TimeZone] = None
    dateTimeTrunc("20200125102412", "20200125102413", false)
  }
  def dtt16() = {
    implicit val dtz: Option[TimeZone] = None
    dateTimeTrunc("20200125102412", "20200125102412+0000", true)
  }

  // Time Eq
  def tte1() = timeEq("102412+0000", "062412-0400", true)
  def tte2() = timeEq("102412+0000", "062412+0000", false)
  def tte3() = timeEq("102412", "062412-0400", true)
  def tte5() = {
    implicit val dtz: Option[TimeZone] = None
    timeEq("102412", "102412", true)
  }
  def tte6() = {
    implicit val dtz: Option[TimeZone] = None
    timeEq("102412", "102413", false)
  }
  def tte7() = {
    implicit val dtz: Option[TimeZone] = None
    eqOp.map(OP => {
      OP.eval(Time("102412"), Time("102412+0000"))  must beFailedTry.like { case e: Exception => e.getMessage must beEqualTo("Time Zone is missing and no default is set.") }
    })
  }
  def tte8() = timeEq("130000-0600", "140000-0500", true)

  // Time Id
  def tti1() = timeId("102412+0000", "102412+0000", true)
  def tti2() = timeId("102412+0000", "062412-0400", false)
  def tti3() = timeId("102412", "062412-0400", false)
  def tti5() = {
    implicit val dtz: Option[TimeZone] = None
    timeId("102412", "102412", true)
  }
  def tti6() = {
    implicit val dtz: Option[TimeZone] = None
    timeId("102412", "102413", false)
  }
  def tti7() = {
    implicit val dtz: Option[TimeZone] = None
    eqOp.map(OP => {
      OP.eval(Time("102412"), Time("102412+0000"), new ComparisonMode(true, false))  must beFailedTry.like { case e: Exception => e.getMessage must beEqualTo("Time Zone is missing and no default is set.") }
    })
  }
  // Time Trunc
  def ttt1() = timeTrunc("062412+0000", "062412+0000", true)
  def ttt2() = timeTrunc("062412-0400", "062412+0000", false)
  def ttt3() = timeTrunc("062412", "062412+0000", true)
  def ttt4() = timeTrunc("0624", "062412-0000", true)
  def ttt5() = timeTrunc("06", "062412-0000", true)
  def ttt9() = timeTrunc("102412+0000", "0624-0400", true)
  def ttt10() = timeTrunc("102412", "0624-0400", true)
  def ttt13() = {
    implicit val dtz: Option[TimeZone] = None
    timeTrunc("102412", "102412", true)
  }
  def ttt14() = {
    implicit val dtz: Option[TimeZone] = None
    timeTrunc("10", "102412", true)
  }
  def ttt15() = {
    implicit val dtz: Option[TimeZone] = None
    timeTrunc("102412", "102413", false)
  }
  def ttt16() = {
    implicit val dtz: Option[TimeZone] = None
    timeTrunc("102412", "102412+0000", true)
  }

  def e1 = LT.eval( Number( "1" ), Number( "2" ) ) must beSuccessfulTry.withValue(true)

  def e2 = GT.eval( Number( "2" ), Number( "1" ) ) must beSuccessfulTry.withValue(true)

  def e3 = LE.eval( Number( "1" ), Number( "1" ) ) must beSuccessfulTry.withValue(true)

  def e4 = GE.eval( Number( "1" ), Number( "1" ) ) must beSuccessfulTry.withValue(true)

  def e5 = EQ.eval( Number( "1" ), Number( "1" ) ) must beSuccessfulTry.withValue(true)

  def e6 = NE.eval( Number( "1" ), Number( "2" ) ) must beSuccessfulTry.withValue(true)

  def et1 = evalDefault(Text( "BC" ), LT, Text( "AB" ), true)  
  def et2 = evalDefault(Text( "AB" ), GT, Text( "BC" ), true)  
  def et3 = evalDefault(Text( "AB" ), LE, Text( "AB" ), true)  
  def et4 = evalDefault(Text( "AB" ), GE, Text( "AB" ), true)  
  def et5 = evalDefault(Text( "AB" ), EQ, Text( "AB" ), true)  
  def et6 = evalDefault(Text( "AB" ), NE, Text( "BC" ), true)

  def ed1 = evalDefault(Date( "20200110" ), LT, Date( "20200111" ), true)
  def ed2 = evalDefault(Date( "20200111" ), GT, Date( "20200110" ), true)
  def ed3 = evalDefault(Date( "20200110" ), LE, Date( "20200110" ), true)
  def ed4 = evalDefault(Date( "20200110" ), GE, Date( "20200110" ), true)
  def ed5 = evalDefault(Date( "20200110" ), EQ, Date( "20200110" ), true)
  def ed6 = evalDefault(Date( "20200110" ), NE, Date( "20200111" ), true)

  def edt1 = evalDefault(DateTime( "20200110100110+0000" ), LT, DateTime( "20200110101010+0000" ), true)
  def edt2 = evalDefault(DateTime( "20200110101010+0000" ), GT, DateTime( "20200110100110+0000" ), true)
  def edt3 = evalDefault(DateTime( "20200110100110+0000" ), LE, DateTime( "20200110100110+0000" ), true)
  def edt4 = evalDefault(DateTime( "20200110100110+0000" ), GE, DateTime( "20200110100110+0000" ), true)
  def edt5 = evalDefault(DateTime( "20200110100110+0000" ), EQ, DateTime( "20200110100110+0000" ), true)
  def edt6 = evalDefault(DateTime( "20200110100110+0000" ), NE, DateTime( "20200110101010+0000" ), true)


  def ett1 = evalDefault(Time( "100110+0000" ), LT, Time( "101010+0000" ), true)
  def ett2 = evalDefault(Time( "101010+0000" ), GT, Time( "100110+0000" ), true)
  def ett3 = evalDefault(Time( "100110+0000" ), LE, Time( "100110+0000" ), true)
  def ett4 = evalDefault(Time( "100110+0000" ), GE, Time( "100110+0000" ), true)
  def ett5 = evalDefault(Time( "100110+0000" ), EQ, Time( "100110+0000" ), true)
  def ett6 = evalDefault(Time( "100110+0000" ), NE, Time( "101010+0000" ), true)
  
}