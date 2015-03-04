package hl7.v2.validation.structure

import hl7.v2.profile.Range
import hl7.v2.instance.{Separators, Number, Text, Location}
import ValueValidation._
import hl7.v2.validation.report.{Length, UnescapedSeparators, Format}
import hl7.v2.validation.vs.ValueSet
import org.specs2.Specification


class ValueValidationSpec extends Specification { def is = s2"""

  Value Validation Specification (Length, Format and Separator in Value )

    Value validation should not report any error if the value is Null  $e1
    Value validation should check and report the format errors         $e2
    Value validation should check the use of unescaped separators      $e3
    Value validation should check the length constraint                $e4
    Value validation length check should be done on an unsecaped value $e5
    Value validation length check should take into account trailing whitespaces $e6
"""

  implicit val separators = Separators( '|', '^', '~', '\\', '&', Some('#') )
  val loc = Location("The description", "The path", 1, 1)
  val lcs = Some( Range(2, "3") )
  val tcs = None
  val lcn = None
  implicit val vsLib = Map[String, ValueSet]()

  def e1 = checkValue( Number("\"\""), lcs, tcs, loc) === Nil

  def e2 = {
    val m = "1E5 is not a valid Number. The format should be: [+|-]digits[.digits]"
    checkValue( Number("1E5"), lcs, tcs, loc) ===  Format(loc, m) :: Nil
  }

  def e3 = Seq("|x", "x^s", "x&q") map { s =>
    checkValue( Text(s), lcs, tcs, loc) ===   UnescapedSeparators(loc) :: Nil
  }

  def e4 = Seq("x", "xxxx") map { s =>
    checkValue(Text(s), lcs, tcs, loc) === Length(loc, s, lcs.get) :: Nil
  }

  def e5 = Seq("""x\F\y""", """q\S\""", """\T\w""") map { s =>
    checkValue( Text(s), lcs, tcs, loc) ===  Nil
  }

  def e6 = Seq( ("  ", Nil), (" x  ", Length(loc, " x  ", lcs.get):: Nil) ) map { t =>
    checkValue(Text(t._1), lcs, tcs, loc) === t._2
  }

}
