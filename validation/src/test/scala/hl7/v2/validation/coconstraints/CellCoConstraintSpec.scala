package hl7.v2.validation.coconstraints

import hl7.v2.instance.Group
import hl7.v2.parser.impl.DefaultParser
import hl7.v2.profile.{BindingStrength, Range }
import hl7.v2.validation.structure.Helpers
import hl7.v2.validation.vs.{BindingLocation, CodeUsage, VSValidationCode, ValueSetBinding, VsEntry, Code => VCode}
import org.specs2.Specification

object EmptyCcContext extends CoConstraintValidationContext {
  override def coConstraintBindingsFor(e: Group): List[CoConstraintBinding] = Nil
}
trait CellCoConstraintSpec extends Specification
  with hl7.v2.validation.coconstraints.DefaultValidator
  with expression.DefaultEvaluator
  with hl7.v2.validation.vs.DefaultValueSetValidator
  with CoConstraintSpecMocks
  with Helpers
  with DefaultParser {

  // ======== Co-Constraint Cell Validation

  def inconclusiveIsNoMatch() = Seq(InconclusiveAssertion(null, null, null), InconclusiveVsBinding(null, null, null, null)).map(
    isMatch(_) === false
  )

  // ---- PlainText

  def plainTextSuccess() = {
    def one = {
      val s = segment("CCT|ST|x^^y|A||||", 1)
      val pt = PlainText("CCT-2 (A)", "1[*]", "ST")
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(pt), List())
      validatePlainText(s, pt, cc) === Some(List(
        CoConstraintPlainTextSuccess(cc, assertGet(s, "1[1]"), pt)
      ))
    }

    def manyOne = {
      val s = segment("CCT|A~ST|x^^y|A||||", 1)
      val pt = PlainText("CCT-2 (A)", "1[*]", "ST")
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(pt), List())
      validatePlainText(s, pt, cc) === Some(List(
        CoConstraintPlainTextSuccess(cc, assertGet(s, "1[2]"), pt)
      ))
    }

    def manyMany = {
      val s = segment("CCT|ST~ST|x^^y|A||||", 1)
      val pt = PlainText("CCT-2 (A)", "1[*]", "ST")
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(pt), List())
      validatePlainText(s, pt, cc) === Some(List(
        CoConstraintPlainTextSuccess(cc, assertGet(s, "1[1]"), pt),
        CoConstraintPlainTextSuccess(cc, assertGet(s, "1[2]"), pt)
      ))
    }

    one and manyOne and manyMany
  }

  def plainTextFailure() = {
    def oneDiff = {
      val s = segment("CCT|ST|x^^y|A||||", 1)
      val pt = PlainText("CCT-2 (A)", "1[*]", "ab")
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(pt), List())
      validatePlainText(s, pt, cc) === Some(List(
        CoConstraintPlainTextFailure(cc, assertGet(s, "1[1]"), pt, "ST")
      ))
    }

    def oneCase = {
      val s = segment("CCT|ST|x^^y|A||||", 1)
      val pt = PlainText("CCT-2 (A)", "1[*]", "st")
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(pt), List())
      validatePlainText(s, pt, cc) === Some(List(
        CoConstraintPlainTextFailure(cc, assertGet(s, "1[1]"), pt, "ST")
      ))
    }

    def many = {
      val s = segment("CCT|ab~st|x^^y|A||||", 1)
      val pt = PlainText("CCT-2 (A)", "1[*]", "ST")
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(pt), List())
      validatePlainText(s, pt, cc) === Some(List(
        CoConstraintPlainTextFailure(cc, assertGet(s, "1[1]"), pt, "ab"),
        CoConstraintPlainTextFailure(cc, assertGet(s, "1[2]"), pt, "st")
      ))
    }

    oneDiff and oneCase and many
  }

  def plainTextInconclusive() = {
    def invalidPath = {
      val s = segment("CCT|ST|x^^y|A||||", 1)
      val pt = PlainText("CCT-2 (A)", "1", "ab")
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(pt), List())
      validatePlainText(s, pt, cc) === Some(List(
        InconclusiveAssertion(pt, s.location, s"Invalid Path '${pt.path}'")
      ))
    }

    def noReach = {
      val s = segment("CCT|ST|x^^y|A||||", 1)
      val pt = PlainText("CCT-2 (A)", "1[*].1[1]", "st")
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(pt), List())
      validatePlainText(s, pt, cc) === Some(List(
        InconclusiveAssertion(pt, s.location, s"Unreachable Path '1[1]'")
      ))
    }

    def complex = {
      val s = segment("CCT|ST|x^^y|A||||", 1)
      val pt = PlainText("CCT-2 (A)", "2[*]", "st")
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(pt), List())
      validatePlainText(s, pt, cc) === Some(List(
        InconclusiveAssertion(pt, s.location, "Path resolution returned at least one complex element")
      ))
    }

    invalidPath and noReach and complex
  }

  def plainTextSuccessIsMatch() = isMatch(CoConstraintPlainTextSuccess(null, null, null)) === true

  def plainTextFailureIsNoMatch() = isMatch(CoConstraintPlainTextFailure(null, null, null, null)) === false

  //---- Code

  def codeSuccess() = {
    def one = {
      val s = segment("CCT|ST|x^^y|A||||", 1)
      val cd = Code("CCT-2 (B)", "2[*]", "x", "y", List(CoConstraintBindingLocation(1, "1[1]", "3[1]")))
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(cd), List())
      validateCode(s, cd, cc) === Some(List(
        CoConstraintCodeSuccess(cc, assertGet(s, "2[1]"), cd)
      ))
    }

    def oneManyBl = {
      val s = segment("CCT|ST|x^^y^z^^t|A||||", 1)
      val cd = Code("CCT-2 (B)", "2[*]", "z", "t", List(
        CoConstraintBindingLocation(1, "1[1]", "3[1]"),
        CoConstraintBindingLocation(4, "4[1]", "6[1]"),
      ))
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(cd), List())
      validateCode(s, cd, cc) === Some(List(
        CoConstraintCodeSuccess(cc, assertGet(s, "2[1]"), cd)
      ))
    }

    def manyOne = {
      val s = segment("CCT||a^^b~x^^y|A||||", 1)
      val cd = Code("CCT-2 (B)", "2[*]", "x", "y", List(CoConstraintBindingLocation(1, "1[1]", "3[1]")))
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(cd), List())
      validateCode(s, cd, cc) === Some(List(
        CoConstraintCodeSuccess(cc, assertGet(s, "2[2]"), cd)
      ))
    }

    def manyOneManyBl = {
      val s = segment("CCT||a^^b~x^^y^z^^t|A||||", 1)
      val cd = Code("CCT-2 (B)", "2[*]", "z", "t", List(
        CoConstraintBindingLocation(1, "1[1]", "3[1]"),
        CoConstraintBindingLocation(4, "4[1]", "6[1]"),
      ))
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(cd), List())
      validateCode(s, cd, cc) === Some(List(
        CoConstraintCodeSuccess(cc, assertGet(s, "2[2]"), cd)
      ))
    }

    def manyMany = {
      val s = segment("CCT||x^^y~x^^y|A||||", 1)
      val cd = Code("CCT-2 (B)", "2[*]", "x", "y", List(CoConstraintBindingLocation(1, "1[1]", "3[1]")))
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(cd), List())
      validateCode(s, cd, cc) === Some(List(
        CoConstraintCodeSuccess(cc, assertGet(s, "2[1]"), cd),
        CoConstraintCodeSuccess(cc, assertGet(s, "2[2]"), cd)
      ))
    }

    def manyManyManyBl = {
      val s = segment("CCT||x^^y^z^^t~x^^y^z^^t|A||||", 1)
      val cd = Code("CCT-2 (B)", "2[*]", "z", "t", List(
        CoConstraintBindingLocation(1, "1[1]", "3[1]"),
        CoConstraintBindingLocation(4, "4[1]", "6[1]"),
      ))
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(cd), List())
      validateCode(s, cd, cc) === Some(List(
        CoConstraintCodeSuccess(cc, assertGet(s, "2[1]"), cd),
        CoConstraintCodeSuccess(cc, assertGet(s, "2[2]"), cd)
      ))
    }

    one and oneManyBl and manyOne and manyOneManyBl and manyMany and manyManyManyBl
  }

  def codeFailureCodeNotFound() = {
    def one = {
      val s = segment("CCT|ST|xp^^y|A||||", 1)
      val cd = Code("CCT-2 (B)", "2[*]", "x", "y", List(CoConstraintBindingLocation(1, "1[1]", "3[1]")))
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(cd), List())
      validateCode(s, cd, cc) === Some(List(
        CoConstraintCodeFailureNotFound(cc, assertGet(s, "2[1].1[1]"), cd)
      ))
    }

    def oneManyBl = {
      val s = segment("CCT|ST|x^^y^zp^^t|A||||", 1)
      val cd = Code("CCT-2 (B)", "2[*]", "z", "t", List(
        CoConstraintBindingLocation(1, "1[1]", "3[1]"),
        CoConstraintBindingLocation(4, "4[1]", "6[1]"),
      ))
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(cd), List())
      validateCode(s, cd, cc) === Some(List(
        CoConstraintCodeFailureNotFound(cc, assertGet(s, "2[1].1[1]"), cd),
        CoConstraintCodeFailureNotFound(cc, assertGet(s, "2[1].4[1]"), cd),
      ))
    }

    def manyOne = {
      val s = segment("CCT||a^^b~xp^^y|A||||", 1)
      val cd = Code("CCT-2 (B)", "2[*]", "x", "y", List(CoConstraintBindingLocation(1, "1[1]", "3[1]")))
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(cd), List())
      validateCode(s, cd, cc) === Some(List(
        CoConstraintCodeFailureNotFound(cc, assertGet(s, "2[1].1[1]"), cd),
        CoConstraintCodeFailureNotFound(cc, assertGet(s, "2[2].1[1]"), cd),
      ))
    }

    def manyOneManyBl = {
      val s = segment("CCT||a^^b~x^^y^zp^^t|A||||", 1)
      val cd = Code("CCT-2 (B)", "2[*]", "z", "t", List(
        CoConstraintBindingLocation(1, "1[1]", "3[1]"),
        CoConstraintBindingLocation(4, "4[1]", "6[1]"),
      ))
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(cd), List())
      validateCode(s, cd, cc) === Some(List(
        CoConstraintCodeFailureNotFound(cc, assertGet(s, "2[1].1[1]"), cd),
        CoConstraintCodeFailureNotFound(cc, assertGet(s, "2[2].1[1]"), cd),
        CoConstraintCodeFailureNotFound(cc, assertGet(s, "2[2].4[1]"), cd),
      ))
    }

    def manyMany = {
      val s = segment("CCT||xp^^y~xp^^y|A||||", 1)
      val cd = Code("CCT-2 (B)", "2[*]", "x", "y", List(CoConstraintBindingLocation(1, "1[1]", "3[1]")))
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(cd), List())
      validateCode(s, cd, cc) === Some(List(
        CoConstraintCodeFailureNotFound(cc, assertGet(s, "2[1].1[1]"), cd),
        CoConstraintCodeFailureNotFound(cc, assertGet(s, "2[2].1[1]"), cd),
      ))
    }

    def manyManyManyBl = {
      val s = segment("CCT||x^^y^zp^^t~x^^y^zp^^t|A||||", 1)
      val cd = Code("CCT-2 (B)", "2[*]", "z", "t", List(
        CoConstraintBindingLocation(1, "1[1]", "3[1]"),
        CoConstraintBindingLocation(4, "4[1]", "6[1]"),
      ))
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(cd), List())
      validateCode(s, cd, cc) === Some(List(
        CoConstraintCodeFailureNotFound(cc, assertGet(s, "2[1].1[1]"), cd),
        CoConstraintCodeFailureNotFound(cc, assertGet(s, "2[1].4[1]"), cd),
        CoConstraintCodeFailureNotFound(cc, assertGet(s, "2[2].1[1]"), cd),
        CoConstraintCodeFailureNotFound(cc, assertGet(s, "2[2].4[1]"), cd),
      ))
    }

    one and oneManyBl and manyOne and manyOneManyBl and manyMany and manyManyManyBl
  }

  def codeFailureCodeSysInvalid() = {
    def one = {
      val s = segment("CCT|ST|x^^yp|A||||", 1)
      val cd = Code("CCT-2 (B)", "2[*]", "x", "y", List(CoConstraintBindingLocation(1, "1[1]", "3[1]")))
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(cd), List())
      validateCode(s, cd, cc) === Some(List(
        CoConstraintCodeFailureInvalidCs(cc, assertGet(s, "2[1].3[1]"), cd, "yp")
      ))
    }

    def oneManyBl = {
      val s = segment("CCT|ST|x^^y^z^^tp|A||||", 1)
      val cd = Code("CCT-2 (B)", "2[*]", "z", "t", List(
        CoConstraintBindingLocation(1, "1[1]", "3[1]"),
        CoConstraintBindingLocation(4, "4[1]", "6[1]"),
      ))
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(cd), List())
      unwrapOptionAndMatchList(validateCode(s, cd, cc), List(
        CoConstraintCodeFailureNotFound(cc, assertGet(s, "2[1].1[1]"), cd),
        CoConstraintCodeFailureInvalidCs(cc, assertGet(s, "2[1].6[1]"), cd, "tp"),
      ))
    }

    def manyOne = {
      val s = segment("CCT||a^^b~x^^yp|A||||", 1)
      val cd = Code("CCT-2 (B)", "2[*]", "x", "y", List(CoConstraintBindingLocation(1, "1[1]", "3[1]")))
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(cd), List())
      unwrapOptionAndMatchList(validateCode(s, cd, cc), List(
        CoConstraintCodeFailureNotFound(cc, assertGet(s, "2[1].1[1]"), cd),
        CoConstraintCodeFailureInvalidCs(cc, assertGet(s, "2[2].3[1]"), cd, "yp"),
      ))
    }

    def manyOneManyBl = {
      val s = segment("CCT||a^^b~x^^y^z^^tp|A||||", 1)
      val cd = Code("CCT-2 (B)", "2[*]", "z", "t", List(
        CoConstraintBindingLocation(1, "1[1]", "3[1]"),
        CoConstraintBindingLocation(4, "4[1]", "6[1]"),
      ))
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(cd), List())
      unwrapOptionAndMatchList(validateCode(s, cd, cc), List(
        CoConstraintCodeFailureNotFound(cc, assertGet(s, "2[1].1[1]"), cd),
        CoConstraintCodeFailureNotFound(cc, assertGet(s, "2[2].1[1]"), cd),
        CoConstraintCodeFailureInvalidCs(cc, assertGet(s, "2[2].6[1]"), cd, "tp"),
      ))
    }

    def manyMany = {
      val s = segment("CCT||x^^yp~x^^yp|A||||", 1)
      val cd = Code("CCT-2 (B)", "2[*]", "x", "y", List(CoConstraintBindingLocation(1, "1[1]", "3[1]")))
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(cd), List())
      unwrapOptionAndMatchList(validateCode(s, cd, cc), List(
        CoConstraintCodeFailureInvalidCs(cc, assertGet(s, "2[1].3[1]"), cd, "yp"),
        CoConstraintCodeFailureInvalidCs(cc, assertGet(s, "2[2].3[1]"), cd, "yp"),
      ))
    }

    def manyManyManyBl = {
      val s = segment("CCT||x^^y^z^^tp~x^^y^z^^tp|A||||", 1)
      val cd = Code("CCT-2 (B)", "2[*]", "z", "t", List(
        CoConstraintBindingLocation(1, "1[1]", "3[1]"),
        CoConstraintBindingLocation(4, "4[1]", "6[1]"),
      ))
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(cd), List())
      unwrapOptionAndMatchList(validateCode(s, cd, cc), List(
        CoConstraintCodeFailureNotFound(cc, assertGet(s, "2[1].1[1]"), cd),
        CoConstraintCodeFailureInvalidCs(cc, assertGet(s, "2[1].6[1]"), cd, "tp"),
        CoConstraintCodeFailureNotFound(cc, assertGet(s, "2[2].1[1]"), cd),
        CoConstraintCodeFailureInvalidCs(cc, assertGet(s, "2[2].6[1]"), cd, "tp"),
      ))
    }

    one and oneManyBl and manyOne and manyOneManyBl and manyMany and manyManyManyBl
  }

  def codeFailureCodeSysNotFound() = {
    def one = {
      val s = segment("CCT|ST|x^^|A||||", 1)
      val cd = Code("CCT-2 (B)", "2[*]", "x", "y", List(CoConstraintBindingLocation(1, "1[1]", "3[1]")))
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(cd), List())
      validateCode(s, cd, cc) === Some(List(
        CoConstraintCodeFailureNotFoundCs(cc, assertGet(s, "2[1]"), cd)
      ))
    }

    def oneManyBl = {
      val s = segment("CCT|ST|x^^y^z^^|A||||", 1)
      val cd = Code("CCT-2 (B)", "2[*]", "z", "t", List(
        CoConstraintBindingLocation(1, "1[1]", "3[1]"),
        CoConstraintBindingLocation(4, "4[1]", "6[1]"),
      ))
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(cd), List())
      unwrapOptionAndMatchList(validateCode(s, cd, cc), List(
        CoConstraintCodeFailureNotFound(cc, assertGet(s, "2[1].1[1]"), cd),
        CoConstraintCodeFailureNotFoundCs(cc, assertGet(s, "2[1]"), cd),
      ))
    }

    def manyOne = {
      val s = segment("CCT||a^^b~x^^|A||||", 1)
      val cd = Code("CCT-2 (B)", "2[*]", "x", "y", List(CoConstraintBindingLocation(1, "1[1]", "3[1]")))
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(cd), List())
      unwrapOptionAndMatchList(validateCode(s, cd, cc), List(
        CoConstraintCodeFailureNotFound(cc, assertGet(s, "2[1].1[1]"), cd),
        CoConstraintCodeFailureNotFoundCs(cc, assertGet(s, "2[2]"), cd),
      ))
    }

    def manyOneManyBl = {
      val s = segment("CCT||a^^b~x^^y^z^^|A||||", 1)
      val cd = Code("CCT-2 (B)", "2[*]", "z", "t", List(
        CoConstraintBindingLocation(1, "1[1]", "3[1]"),
        CoConstraintBindingLocation(4, "4[1]", "6[1]"),
      ))
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(cd), List())
      unwrapOptionAndMatchList(validateCode(s, cd, cc), List(
        CoConstraintCodeFailureNotFound(cc, assertGet(s, "2[1].1[1]"), cd),
        CoConstraintCodeFailureNotFound(cc, assertGet(s, "2[2].1[1]"), cd),
        CoConstraintCodeFailureNotFoundCs(cc, assertGet(s, "2[2]"), cd),
      ))
    }

    def manyMany = {
      val s = segment("CCT||x^^~x^^|A||||", 1)
      val cd = Code("CCT-2 (B)", "2[*]", "x", "y", List(CoConstraintBindingLocation(1, "1[1]", "3[1]")))
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(cd), List())
      unwrapOptionAndMatchList(validateCode(s, cd, cc), List(
        CoConstraintCodeFailureNotFoundCs(cc, assertGet(s, "2[1]"), cd),
        CoConstraintCodeFailureNotFoundCs(cc, assertGet(s, "2[2]"), cd),
      ))
    }

    def manyManyManyBl = {
      val s = segment("CCT||x^^y^z^^~x^^y^z^^|A||||", 1)
      val cd = Code("CCT-2 (B)", "2[*]", "z", "t", List(
        CoConstraintBindingLocation(1, "1[1]", "3[1]"),
        CoConstraintBindingLocation(4, "4[1]", "6[1]"),
      ))
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(cd), List())
      unwrapOptionAndMatchList(validateCode(s, cd, cc), List(
        CoConstraintCodeFailureNotFound(cc, assertGet(s, "2[1].1[1]"), cd),
        CoConstraintCodeFailureNotFoundCs(cc, assertGet(s, "2[1]"), cd),
        CoConstraintCodeFailureNotFound(cc, assertGet(s, "2[2].1[1]"), cd),
        CoConstraintCodeFailureNotFoundCs(cc, assertGet(s, "2[2]"), cd),
      ))
    }

    one and oneManyBl and manyOne and manyOneManyBl and manyMany and manyManyManyBl
  }

  def codeInconclusiveMultiple() = {
    def codeMultiple = {
      val s = segment("CCT|ST~ST||A||||", 1)
      val cd = Code("CCT-2 (B)", ".", "ST", "y", List(CoConstraintBindingLocation(1, "1[*]", "3[*]")))
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(cd), List())
      validateCode(s, cd, cc) === Some(List(
        InconclusiveAssertion(cd, s.location, s"code path returned multiple elements")
      ))
    }

    def codeSysMultiple = {
      val s = segment("CCT|ST||A~C||||", 1)
      val cd = Code("CCT-2 (B)", ".", "ST", "y", List(CoConstraintBindingLocation(1, "1[*]", "3[*]")))
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(cd), List())
      validateCode(s, cd, cc) === Some(List(
        InconclusiveAssertion(cd, s.location, s"codeSystem path returned multiple elements")
      ))
    }

    codeMultiple and codeSysMultiple
  }

  def codeInconclusivePath() = {
    def codeInvalid = {
      val s = segment("CCT|ST~ST||A||||", 1)
      val cd = Code("CCT-2 (B)", ".", "x", "y", List(CoConstraintBindingLocation(1, "1", "3[*]")))
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(cd), List())
      validateCode(s, cd, cc) === Some(List(
        InconclusiveAssertion(cd, s.location, s"Invalid Path '1'")
      ))
    }

    def codeSysInvalid = {
      val s = segment("CCT|ST||A~C||||", 1)
      val cd = Code("CCT-2 (B)", ".", "ST", "y", List(CoConstraintBindingLocation(1, "1[*]", "3")))
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(cd), List())
      validateCode(s, cd, cc) === Some(List(
        InconclusiveAssertion(cd, s.location, s"Invalid Path '3'")
      ))
    }

    codeInvalid and codeSysInvalid
  }

  def codeSuccessIsMatch() = isMatch(CoConstraintCodeSuccess(null, null, null)) === true

  def codeNotFoundNoMatch() = isMatch(CoConstraintCodeFailureNotFound(null, null, null)) === false

  def codeInvalidCsNoMatch() = isMatch(CoConstraintCodeFailureInvalidCs(null, null, null, null)) === false

  def codeNotFoundCsNoMatch() = isMatch(CoConstraintCodeFailureNotFoundCs(null, null, null)) === false

  //---- Vs Binding Cell

  def vsBindingSuccess() = {
    def one = {
      Seq(
        ("CCT|ST|A^^SYS1|A||||", VSValidationCode.RVS(VCode("A", "des", CodeUsage.R, "SYS1"), vs1_cs)),
        ("CCT|ST|B^^SYS1|A||||", VSValidationCode.PVS(VCode("B", "des", CodeUsage.P, "SYS1"), vs1_cs)),
        ("CCT|ST|C^^SYS1|A||||", VSValidationCode.EVS(VCode("C", "des", CodeUsage.E, "SYS1"), vs1_cs)),
      ) map(tuple => {
        val s = segment(tuple._1, 1)
        val vbL = Seq("vs1_cs", "vs1_1") map (vs =>
          ValueSetBinding(".", Some(BindingStrength.R), List(vs), List(
            BindingLocation("1[1]", Some("3[1]")),
          ))) toList
        val vsB = ValueSet("CCT-2 (B)", "2[*]", vbL)
        val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(vsB), List())
        validateValueSetBinding(s, vsB, cc) === Some(List(
          CoConstraintVsBindingSuccess(cc, assertGet(s, "2[1]"), vsB, vbL.head,
            VsEntry(assertGet(s, "2[1]"), tuple._2, Some(BindingStrength.R))
          )
        ))
      })
    }

    def oneManyBl = {
      Seq(
        ("CCT|ST|x^^y^A^^SYS1|A||||", VSValidationCode.RVS(VCode("A", "des", CodeUsage.R, "SYS1"), vs1_cs)),
        ("CCT|ST|x^^y^B^^SYS1|A||||", VSValidationCode.PVS(VCode("B", "des", CodeUsage.P, "SYS1"), vs1_cs)),
        ("CCT|ST|x^^y^C^^SYS1|A||||", VSValidationCode.EVS(VCode("C", "des", CodeUsage.E, "SYS1"), vs1_cs)),
      ) map(tuple => {
        val s = segment(tuple._1, 1)
          val vbL = Seq("vs1_cs", "vs1_1") map (vs =>
            ValueSetBinding(".", Some(BindingStrength.R), List(vs), List(
              BindingLocation("1[1]", Some("3[1]")),
              BindingLocation("4[1]", Some("6[1]"))
            ))
          ) toList
          val vsB = ValueSet("CCT-2 (B)", "2[*]", vbL)
          val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(vsB), List())
          validateValueSetBinding(s, vsB, cc) === Some(List(
            CoConstraintVsBindingSuccess(cc, assertGet(s, "2[1]"), vsB, vbL.head,
              VsEntry(assertGet(s, "2[1]"), tuple._2, Some(BindingStrength.R))
            )
          ))

      })
    }

    def manyOne = {
      Seq(
        ("CCT|ST|x^^y~A^^SYS1|A||||", VSValidationCode.RVS(VCode("A", "des", CodeUsage.R, "SYS1"), vs1_cs)),
        ("CCT|ST|x^^y~B^^SYS1|A||||", VSValidationCode.PVS(VCode("B", "des", CodeUsage.P, "SYS1"), vs1_cs)),
        ("CCT|ST|x^^y~C^^SYS1|A||||", VSValidationCode.EVS(VCode("C", "des", CodeUsage.E, "SYS1"), vs1_cs)),
      ) map(tuple => {
        val s = segment(tuple._1, 1)
        val vbL = Seq("vs1_cs", "vs1_1") map (vs =>
          ValueSetBinding(".", Some(BindingStrength.R), List(vs), List(
            BindingLocation("1[1]", Some("3[1]")),
          ))) toList
        val vsB = ValueSet("CCT-2 (B)", "2[*]", vbL)
        val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(vsB), List())
        validateValueSetBinding(s, vsB, cc) === Some(List(
          CoConstraintVsBindingSuccess(cc, assertGet(s, "2[2]"), vsB, vbL.head,
            VsEntry(assertGet(s, "2[2]"), tuple._2, Some(BindingStrength.R))
          )
        ))
      })
    }

    def manyOneManyBl = {
      Seq(
        ("CCT|ST|s^^t~x^^y^A^^SYS1|A||||", VSValidationCode.RVS(VCode("A", "des", CodeUsage.R, "SYS1"), vs1_cs)),
        ("CCT|ST|s^^t~x^^y^B^^SYS1|A||||", VSValidationCode.PVS(VCode("B", "des", CodeUsage.P, "SYS1"), vs1_cs)),
        ("CCT|ST|s^^t~x^^y^C^^SYS1|A||||", VSValidationCode.EVS(VCode("C", "des", CodeUsage.E, "SYS1"), vs1_cs)),
      ) map(tuple => {
        val s = segment(tuple._1, 1)
        val vbL = Seq("vs1_cs", "vs1_1") map (vs =>
          ValueSetBinding(".", Some(BindingStrength.R), List(vs), List(
            BindingLocation("1[1]", Some("3[1]")),
            BindingLocation("4[1]", Some("6[1]"))
          ))) toList
        val vsB = ValueSet("CCT-2 (B)", "2[*]", vbL)
        val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(vsB), List())
        validateValueSetBinding(s, vsB, cc) === Some(List(
          CoConstraintVsBindingSuccess(cc, assertGet(s, "2[2]"), vsB, vbL.head,
            VsEntry(assertGet(s, "2[2]"), tuple._2, Some(BindingStrength.R))
          )
        ))
      })
    }

    def manyMany = {
      Seq(
        ("CCT|ST|A^^SYS1~A^^SYS1|A||||", VSValidationCode.RVS(VCode("A", "des", CodeUsage.R, "SYS1"), vs1_cs)),
        ("CCT|ST|B^^SYS1~B^^SYS1|A||||", VSValidationCode.PVS(VCode("B", "des", CodeUsage.P, "SYS1"), vs1_cs)),
        ("CCT|ST|C^^SYS1~C^^SYS1|A||||", VSValidationCode.EVS(VCode("C", "des", CodeUsage.E, "SYS1"), vs1_cs)),
      ) map(tuple => {
        val s = segment(tuple._1, 1)
        val vbL = Seq("vs1_cs", "vs1_1") map (vs =>
          ValueSetBinding(".", Some(BindingStrength.R), List(vs), List(
            BindingLocation("1[1]", Some("3[1]")),
          ))) toList
        val vsB = ValueSet("CCT-2 (B)", "2[*]", vbL)
        val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(vsB), List())
        validateValueSetBinding(s, vsB, cc) === Some(List(
          CoConstraintVsBindingSuccess(cc, assertGet(s, "2[1]"), vsB, vbL.head,
            VsEntry(assertGet(s, "2[1]"), tuple._2, Some(BindingStrength.R))
          ),
          CoConstraintVsBindingSuccess(cc, assertGet(s, "2[2]"), vsB, vbL.head,
            VsEntry(assertGet(s, "2[2]"), tuple._2, Some(BindingStrength.R))
          )
        ))
      })
    }

    def manyManyManyBl = {
      Seq(
        ("CCT|ST|x^^y^A^^SYS1~x^^y^A^^SYS1|A||||", VSValidationCode.RVS(VCode("A", "des", CodeUsage.R, "SYS1"), vs1_cs)),
        ("CCT|ST|x^^y^B^^SYS1~x^^y^B^^SYS1|A||||", VSValidationCode.PVS(VCode("B", "des", CodeUsage.P, "SYS1"), vs1_cs)),
        ("CCT|ST|x^^y^C^^SYS1~x^^y^C^^SYS1|A||||", VSValidationCode.EVS(VCode("C", "des", CodeUsage.E, "SYS1"), vs1_cs)),
      ) map(tuple => {
        val s = segment(tuple._1, 1)
        val vbL = Seq("vs1_cs", "vs1_1") map (vs =>
          ValueSetBinding(".", Some(BindingStrength.R), List(vs), List(
            BindingLocation("1[1]", Some("3[1]")),
            BindingLocation("4[1]", Some("6[1]"))
          ))) toList
        val vsB = ValueSet("CCT-2 (B)", "2[*]", vbL)
        val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(vsB), List())
        validateValueSetBinding(s, vsB, cc) === Some(List(
          CoConstraintVsBindingSuccess(cc, assertGet(s, "2[1]"), vsB, vbL.head,
            VsEntry(assertGet(s, "2[1]"), tuple._2, Some(BindingStrength.R))
          ),
          CoConstraintVsBindingSuccess(cc, assertGet(s, "2[2]"), vsB, vbL.head,
            VsEntry(assertGet(s, "2[2]"), tuple._2, Some(BindingStrength.R))
          )
        ))
      })
    }

    one ++ oneManyBl ++ manyOne ++ manyOneManyBl ++ manyMany ++ manyManyManyBl
  }

  def vsBindingFailure() = {

    def one = {
      Seq(
        ("CCT|ST|X^^SYS1|A||||", List(
          VSValidationCode.CodeNotFound("X", Some("SYS1"), List(vs1_cs)),
          VSValidationCode.CodeNotFound("X", Some("SYS1"), List(vs1_1))
        )),
        ("CCT|ST|A^^SYS2|A||||", List(
          VSValidationCode.SimpleCodeFoundInvalidCodeSystem("A", "SYS1", "SYS2", vs1_cs),
          VSValidationCode.CodeNotFound("A", Some("SYS2"), List(vs1_cs)),
          VSValidationCode.CodeNotFound("A", Some("SYS2"), List(vs1_1))
        ))
      ) map(tuple => {
        val s = segment(tuple._1, 1)
        val vbL = Seq("vs1_cs", "vs1_1") map (vs =>
          ValueSetBinding(".", Some(BindingStrength.R), List(vs), List(
            BindingLocation("1[1]", Some("3[1]")),
          ))) toList
        val vsB = ValueSet("CCT-2 (B)", "2[*]", vbL)
        val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(vsB), List())

        unwrapOptionAndMatchList(validateValueSetBinding(s, vsB, cc),
          tuple._2 map (vsDetCode => {
            CoConstraintVsBindingFailure(cc, s, vsB,
              VsEntry(assertGet(s, "2[1]"), vsDetCode, Some(BindingStrength.R))
            )
          })
        )
      })
    }

    def oneManyBl = {
      Seq(
        ("CCT|ST|Y^^SYS1^X^^SYS1|A||||", List(
          VSValidationCode.CodeNotFound("X", Some("SYS1"), List(vs1_cs)),
          VSValidationCode.CodeNotFound("X", Some("SYS1"), List(vs1_1)),
          VSValidationCode.CodeNotFound("Y", Some("SYS1"), List(vs1_cs)),
          VSValidationCode.CodeNotFound("Y", Some("SYS1"), List(vs1_1))
        )),
        ("CCT|ST|B^^SYS2^A^^SYS2|A||||", List(
          VSValidationCode.SimpleCodeFoundInvalidCodeSystem("A", "SYS1", "SYS2", vs1_cs),
          VSValidationCode.CodeNotFound("A", Some("SYS2"), List(vs1_cs)),
          VSValidationCode.CodeNotFound("A", Some("SYS2"), List(vs1_1)),
          VSValidationCode.SimpleCodeFoundInvalidCodeSystem("B", "SYS1", "SYS2", vs1_cs),
          VSValidationCode.CodeNotFound("B", Some("SYS2"), List(vs1_cs)),
          VSValidationCode.CodeNotFound("B", Some("SYS2"), List(vs1_1))
        ))
      ) map(tuple => {
        val s = segment(tuple._1, 1)
        val vbL = Seq("vs1_cs", "vs1_1") map (vs =>
          ValueSetBinding(".", Some(BindingStrength.R), List(vs), List(
            BindingLocation("1[1]", Some("3[1]")),
            BindingLocation("4[1]", Some("6[1]")),
          ))) toList
        val vsB = ValueSet("CCT-2 (B)", "2[*]", vbL)
        val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(vsB), List())

        unwrapOptionAndMatchList(validateValueSetBinding(s, vsB, cc),
          tuple._2 map (vsDetCode => {
            CoConstraintVsBindingFailure(cc, s, vsB,
              VsEntry(assertGet(s, "2[1]"), vsDetCode, Some(BindingStrength.R))
            )
          })
        )
      })
    }

    def oneManyRep = {
      Seq(
        ("CCT|ST|Y^^SYS1~X^^SYS1|A||||", List(
          (VSValidationCode.CodeNotFound("X", Some("SYS1"), List(vs1_cs)), "2[2]"),
          (VSValidationCode.CodeNotFound("X", Some("SYS1"), List(vs1_1)), "2[2]"),
          (VSValidationCode.CodeNotFound("Y", Some("SYS1"), List(vs1_cs)), "2[1]"),
          (VSValidationCode.CodeNotFound("Y", Some("SYS1"), List(vs1_1)), "2[1]"),
        )),
        ("CCT|ST|B^^SYS2~A^^SYS2|A||||", List(
          (VSValidationCode.SimpleCodeFoundInvalidCodeSystem("A", "SYS1", "SYS2", vs1_cs), "2[2]"),
          (VSValidationCode.CodeNotFound("A", Some("SYS2"), List(vs1_cs)), "2[2]"),
          (VSValidationCode.CodeNotFound("A", Some("SYS2"), List(vs1_1)), "2[2]"),
          (VSValidationCode.SimpleCodeFoundInvalidCodeSystem("B", "SYS1", "SYS2", vs1_cs), "2[1]"),
          (VSValidationCode.CodeNotFound("B", Some("SYS2"), List(vs1_cs)), "2[1]"),
          (VSValidationCode.CodeNotFound("B", Some("SYS2"), List(vs1_1)), "2[1]"),
        ))
      ) map(tuple => {
        val s = segment(tuple._1, 1)
        val vbL = Seq("vs1_cs", "vs1_1") map (vs =>
          ValueSetBinding(".", Some(BindingStrength.R), List(vs), List(
            BindingLocation("1[1]", Some("3[1]")),
          ))) toList
        val vsB = ValueSet("CCT-2 (B)", "2[*]", vbL)
        val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(vsB), List())

        unwrapOptionAndMatchList(validateValueSetBinding(s, vsB, cc),
          tuple._2 map (vsDetCode => {
            CoConstraintVsBindingFailure(cc, s, vsB,
              VsEntry(assertGet(s, vsDetCode._2), vsDetCode._1, Some(BindingStrength.R))
            )
          })
        )
      })
    }

    def oneManyRepManyBl = {
      Seq(
        ("CCT|ST|T^^SYS1^Y^^SYS1~Z^^SYS1^X^^SYS1|A||||", List(
          (VSValidationCode.CodeNotFound("X", Some("SYS1"), List(vs1_cs)), "2[2]"),
          (VSValidationCode.CodeNotFound("X", Some("SYS1"), List(vs1_1)), "2[2]"),
          (VSValidationCode.CodeNotFound("Y", Some("SYS1"), List(vs1_cs)), "2[1]"),
          (VSValidationCode.CodeNotFound("Y", Some("SYS1"), List(vs1_1)), "2[1]"),
          (VSValidationCode.CodeNotFound("Z", Some("SYS1"), List(vs1_cs)), "2[2]"),
          (VSValidationCode.CodeNotFound("Z", Some("SYS1"), List(vs1_1)), "2[2]"),
          (VSValidationCode.CodeNotFound("T", Some("SYS1"), List(vs1_cs)), "2[1]"),
          (VSValidationCode.CodeNotFound("T", Some("SYS1"), List(vs1_1)), "2[1]"),
        )),
        ("CCT|ST|C^^SYS2^B^^SYS2~C^^SYS2^A^^SYS2|A||||", List(
          (VSValidationCode.SimpleCodeFoundInvalidCodeSystem("A", "SYS1", "SYS2", vs1_cs), "2[2]"),
          (VSValidationCode.CodeNotFound("A", Some("SYS2"), List(vs1_cs)), "2[2]"),
          (VSValidationCode.CodeNotFound("A", Some("SYS2"), List(vs1_1)), "2[2]"),
          (VSValidationCode.SimpleCodeFoundInvalidCodeSystem("B", "SYS1", "SYS2", vs1_cs), "2[1]"),
          (VSValidationCode.CodeNotFound("B", Some("SYS2"), List(vs1_cs)), "2[1]"),
          (VSValidationCode.CodeNotFound("B", Some("SYS2"), List(vs1_1)), "2[1]"),
          (VSValidationCode.SimpleCodeFoundInvalidCodeSystem("C", "SYS1", "SYS2", vs1_cs), "2[2]"),
          (VSValidationCode.CodeNotFound("C", Some("SYS2"), List(vs1_cs)), "2[2]"),
          (VSValidationCode.CodeNotFound("C", Some("SYS2"), List(vs1_1)), "2[2]"),
          (VSValidationCode.SimpleCodeFoundInvalidCodeSystem("C", "SYS1", "SYS2", vs1_cs), "2[1]"),
          (VSValidationCode.CodeNotFound("C", Some("SYS2"), List(vs1_cs)), "2[1]"),
          (VSValidationCode.CodeNotFound("C", Some("SYS2"), List(vs1_1)), "2[1]"),
        ))
      ) map(tuple => {
        val s = segment(tuple._1, 1)
        val vbL = Seq("vs1_cs", "vs1_1") map (vs =>
          ValueSetBinding(".", Some(BindingStrength.R), List(vs), List(
            BindingLocation("1[1]", Some("3[1]")),
            BindingLocation("4[1]", Some("6[1]")),
          ))) toList
        val vsB = ValueSet("CCT-2 (B)", "2[*]", vbL)
        val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(vsB), List())

        unwrapOptionAndMatchList(validateValueSetBinding(s, vsB, cc),
          tuple._2 map (vsDetCode => {
            CoConstraintVsBindingFailure(cc, s, vsB,
              VsEntry(assertGet(s, vsDetCode._2), vsDetCode._1, Some(BindingStrength.R))
            )
          })
        )
      })
    }

    one ++ oneManyBl ++ oneManyRep ++ oneManyRepManyBl
  }

  def vsBindingVsSpecErr() = {

    def one = {
      Seq(
        ("CCT|ST|X^^SYS1|A||||", VSValidationCode.InvalidCodeBindingLocation("1", false)),
      ) map(tuple => {
        val s = segment(tuple._1, 1)
        val vbL = Seq("vs1_cs", "vs1_1") map (vs =>
          ValueSetBinding(".", Some(BindingStrength.R), List(vs), List(
            BindingLocation("1", Some("3[1]")),
          ))) toList
        val vsB = ValueSet("CCT-2 (B)", "2[*]", vbL)
        val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(vsB), List())

        unwrapOptionAndMatchList(validateValueSetBinding(s, vsB, cc), List(
          InconclusiveVsBinding(vsB, vbL(0), s.location,
            VsEntry(assertGet(s, "2[1]"), tuple._2, Some(BindingStrength.R))
          ),
          InconclusiveVsBinding(vsB, vbL(1), s.location,
            VsEntry(assertGet(s, "2[1]"), tuple._2, Some(BindingStrength.R))
          )
        ))
    })
    }
    one
  }

  def vsBindingInconclusive() = {
    def one = {
      Seq(
        ("CCT|ST|X^^SYS1|A||||", VSValidationCode.InvalidCodeBindingLocation("1", false)),
      ) map(tuple => {
        val s = segment(tuple._1, 1)
        val vbL = Seq("vs1_cs", "vs1_1") map (vs =>
          ValueSetBinding(".", Some(BindingStrength.R), List(vs), List(
            BindingLocation("1[1]", Some("3[1]")),
          ))) toList
        val vsB = ValueSet("CCT-2 (B)", "2", vbL)
        val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(vsB), List())

        unwrapOptionAndMatchList(validateValueSetBinding(s, vsB, cc), List(
          InconclusiveAssertion(vsB, s.location, s"Invalid Path '2'")
        ))
      })
    }
    one
  }

  def vsBindingSuccessIsMatch = isMatch(CoConstraintVsBindingSuccess(null, null, null, null, null)) === true

  def vsBindingFailureNoMatch = isMatch(CoConstraintVsBindingFailure(null, null, null, null)) === false

  def unwrapOptionAndMatchList[T](o: Option[List[T]], expected: List[T]) = o.map(ls => ls must containTheSameElementsAs(expected)).getOrElse(1 mustEqual(2))


}
