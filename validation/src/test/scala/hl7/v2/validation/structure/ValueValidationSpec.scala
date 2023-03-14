package hl7.v2.validation.structure

import hl7.v2.instance._
import hl7.v2.profile.Range
import hl7.v2.validation.structure.ValueValidation.{checkValue, _}
import org.specs2.Specification
import hl7.v2.validation.report.ConfigurableDetections
import com.typesafe.config.ConfigFactory

class ValueValidationSpec extends Specification { def is = s2"""

  Value Validation Specification (Length, Format and Separator in Value )

    Value validation should not report any error if the value is Null and the location is a Field  $e1
    Value validation should check and report the format errors         $e2
    Value validation should check the use of unescaped separators      $e3
    Length
      Length constraint should fail if value size is outside MinLength, MaxLength range             $e4
      Length constraint should fail if value size is outside ConfLength Range                       $e42
      Length constraint should pass if value size is in ConfLength Range                       $e43
      Length constraint should pass if value size is in MinLength, MaxLength Range                       $e44
      Length validation should fail with spec error if both MinLength, MaxLength and ConfLength are specified    $e41
      Length validation should fail with spec error if none of MinLength, MaxLength and ConfLength are specified    $e45
      Value validation length check should be done on an unescaped value $e5
      Value validation length check should take into account trailing whitespaces $e6
    Constant Value
      Value validation should pass if value equals defined ConstantValue (case sensitive) $e7
      Value validation should fail if value is not equal to defined ConstantValue (case sensitive) $e8
      Value validation should be evaluated on unescaped value $e9

"""

  implicit val separators = Separators( '|', '^', '~', '\\', '&', Some('#') )
  implicit val Detections = new ConfigurableDetections(ConfigFactory.load());
  val loc = Location(EType.Field, "The description", "The path", 1, 1)
  val lcs = Some( Range(2, "3") )
  val rng = Some( Range(2, "3") )
  val lcn = None

  def e1 = checkValue( Number("\"\""), lcs, None, None, loc) === Nil

  def e2 = {
    val m = "1E5 is not a valid Number. The format should be: [+|-]digits[.digits]"
    checkValue( Number("1E5"), lcs, None, None, loc) ===  Detections.format(loc, m) :: Nil
  }

  def e3 = Seq("|x", "x^s", "x&q") map { s =>
    checkValue( Text(s), lcs, None, None, loc) ===   Detections.unescaped(loc) :: Nil
  }

  def e4 = Seq("x", "xxxx") map { s =>
    checkValue(Text(s), lcs, None, None, loc) ===  Detections.length(loc, lcs.get, s) :: Nil
  }
  
  def e41 = Seq("x", "xxxx") map { s =>
    checkValue(Text(s), lcs, rng, None, loc) ===  Detections.lengthSpecErrorXOR(loc) :: Nil
  }
  
  def e45 = Seq("x", "xxxx") map { s =>
    checkValue(Text(s), None, None, None, loc) ===  Detections.lengthSpecErrorNF(loc) :: Nil
  }
  
  def e42 = Seq("x", "xxxx") map { s =>
    checkValue(Text(s), None, rng, None, loc) ===  Detections.length(loc, lcs.get, s) :: Nil
  }
  
  def e43 = Seq("xx", "xxx") map { s =>
    checkValue(Text(s), None, rng, None, loc) === Nil
  }
  
  def e44 = Seq("xx", "xxx") map { s =>
    checkValue(Text(s), lcs, None, None, loc) === Nil
  }

  def e5 = Seq("""x\F\y""", """q\S\""", """\T\w""") map { s =>
    checkValue( Text(s), lcs, None, None, loc) ===  Nil
  }

  def e6 = Seq (
      ("  ", Nil),
      (" x  ", Detections.length(loc, lcs.get, " x  ") :: Nil)
  ) map { t =>
    checkValue(Text(t._1), lcs, None, None, loc) === t._2
  }

  def e7 = Seq (
    checkValue(Text("AbC"), lcs, None, Some("AbC"), loc) === Nil,
    checkValue(Number("123"), lcs, None, Some("123"), loc) === Nil,
  )

  def e8 = Seq (
    checkValue(Text("AbC"), lcs, None, Some("ABC"), loc) === Detections.constantValue(loc, "ABC", "AbC") :: Nil,
    checkValue(Text("abc"), lcs, None, Some("def"), loc) === Detections.constantValue(loc, "def", "abc") :: Nil,
    checkValue(Number("123"), lcs, None, Some("SomeValue"), loc) === Detections.constantValue(loc, "SomeValue", "123") :: Nil,
    checkValue(Number("123"), lcs, None, Some("245"), loc) === Detections.constantValue(loc, "245", "123") :: Nil
  )

  def e9 = Seq (
    checkValue(Text("""x\F\y"""), lcs, None, Some("""x\F\y"""), loc) === Detections.constantValue(loc, """x\F\y""", s"x${separators.fs}y") :: Nil,
    checkValue(Text("""x\F\y"""), lcs, None, Some(s"x${separators.fs}y"), loc) === Nil
  )

}
