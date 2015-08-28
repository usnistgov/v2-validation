package hl7.v2.validation.structure

import hl7.v2.instance._
import hl7.v2.profile.Range
import hl7.v2.validation.report.Detections
import hl7.v2.validation.structure.ValueValidation._
import org.specs2.Specification

class ValueValidationSpec extends Specification { def is = s2"""

  Value Validation Specification (Length, Format and Separator in Value )

    Value validation should not report any error if the value is Null and the location is a Field  $e1
    Value validation should check and report the format errors         $e2
    Value validation should check the use of unescaped separators      $e3
    Value validation should check the length constraint                $e4
    Value validation length check should be done on an unsecaped value $e5
    Value validation length check should take into account trailing whitespaces $e6
"""

  implicit val separators = Separators( '|', '^', '~', '\\', '&', Some('#') )
  val loc = Location(EType.Field, "The description", "The path", 1, 1)
  val lcs = Some( Range(2, "3") )
  val lcn = None

  def e1 = checkValue( Number("\"\""), lcs, loc) === Nil

  def e2 = {
    val m = "1E5 is not a valid Number. The format should be: [+|-]digits[.digits]"
    checkValue( Number("1E5"), lcs, loc) ===  Detections.format(loc, m) :: Nil
  }

  def e3 = Seq("|x", "x^s", "x&q") map { s =>
    checkValue( Text(s), lcs, loc) ===   Detections.unescaped(loc) :: Nil
  }

  def e4 = Seq("x", "xxxx") map { s =>
    checkValue(Text(s), lcs, loc) ===  Detections.length(loc, lcs.get, s) :: Nil
  }

  def e5 = Seq("""x\F\y""", """q\S\""", """\T\w""") map { s =>
    checkValue( Text(s), lcs, loc) ===  Nil
  }

  def e6 = Seq (
      ("  ", Nil),
      (" x  ", Detections.length(loc, lcs.get, " x  ") :: Nil)
  ) map { t =>
    checkValue(Text(t._1), lcs, loc) === t._2
  }

}
