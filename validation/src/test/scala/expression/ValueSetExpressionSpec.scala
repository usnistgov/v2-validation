package expression

import expression.EvalResult.Pass
import hl7.v2.instance.{ComplexField, EType, Field, Location, SimpleComponent, SimpleField, Text}
import hl7.v2.validation.vs.{Code, CodeUsage, EmptyValueSetSpecification, InternalValueSet, ValueSetLibraryImpl, ValueSetSpecification, VsEntry, VsValidator}
import hl7.v2.validation.vs.factory.impl.java.{DefaultValueSetFactory, DefaultValueSetFactoryConfiguration}
import hl7.v2.profile.{BindingLocation, Component, Composite, Primitive, Req, Usage, ValueSetSpec}
import hl7.v2.validation.vs.VSValidationCode.{CodeNotFound, EVS, InvalidCodeBindingLocation}
import org.specs2.Specification


trait ValueSetExpressionSpec extends Specification with Evaluator with Mocks {
  /* ValueSetSpec
        ValueSet expression fails when code is not found in value set
          Simple Element  $failCodeNotFoundSimple
          Complex Element $failCodeNotFoundComplex
          Coded Element
            Single binding location $failCodeNotFoundCE1
            Multiple binding location $failCodeNotFoundCE2
        ValueSet expression fails when code is found in value set with usage E
          Simple Element  $failECodeNotFoundSimple
          Complex Element $failECodeNotFoundComplex
          Coded Element
            Single binding location $failECodeNotFoundCE1
            Multiple binding location $failECodeNotFoundCE2
        ValueSet expression passes when code is found in value set R
          Simple Element  $passSimple
          Complex Element $passComplex
          Coded Element
            Single binding location $passCE1
            Multiple binding location $passCE2
        ValueSet expression inconclusive when there is a spec error
          Simple Element with BL
          Complex Element with no BL
          Complex Element with not existing BL
        NotPresentBehaviour = FAIL
          ValueSet expression fail when path is empty $npbFailEmpty
          ValueSet expression fail when binding location's path is empty (single) $npbFailBLEmpty
          ValueSet expression fail when both binding location's path is empty (multiple) $npbFail2BLEmpty
        NotPresentBehaviour = PASS
          ValueSet expression pass when path is empty $npbPassEmpty
          ValueSet expression pass when binding location's path is empty (single) $npbPassBLEmpty
          ValueSet expression pass when both binding location's path is empty (multiple) $npbPass2BLEmpty
        NotPresentBehaviour = INCONCLUSIVE
          ValueSet expression inconclusive when path is empty $npbIncEmpty
          ValueSet expression inconclusive when binding location's path is empty (single) $npbIncBLEmpty
          ValueSet expression inconclusive when both binding location's path is empty (multiple) $npbInc2BLEmpty
   */

  def vsSpecification: ValueSetSpecification = EmptyValueSetSpecification
  val vs1: InternalValueSet = InternalValueSet("vs1", None, None, List(Code("A", "A", CodeUsage.R, "SYS"),Code("B", "B", CodeUsage.P, "SYS"), Code("C", "C", CodeUsage.E, "SYS")))
  val vs2: InternalValueSet = InternalValueSet("vs2", None, None, List(Code("X", "X", CodeUsage.R, "SYS1"),Code("Y", "Y", CodeUsage.P, "SYS1"), Code("Z", "Z", CodeUsage.E, "SYS1")))
  val valueSetLibrary = new ValueSetLibraryImpl(List("excluded"), (vs1 :: vs2 :: Nil).map(x => {
    (x.id, x)
  }).toMap)
  implicit val valueSetFactory: hl7.v2.validation.vs.factory.ValueSetFactory = new DefaultValueSetFactory(valueSetLibrary, new DefaultValueSetFactoryConfiguration(true, false))

/*  ValueSet expression fails when code is not found in value set
      Simple Element $failCodeNotFoundSimple
      Complex Element $failCodeNotFoundComplex
      Coded Element
        Single binding location $failCodeNotFoundCE1
        Multiple binding location $failCodeNotFoundCE2 */

  def failCodeNotFoundSimple = {
    val s_not_found = makeSimpleField("NOT_FOUND")
    val exp = ValueSet(".", ValueSetSpec("vs1", None, None))
    eval(exp, s_not_found) === Failures.valueSet(exp, s_not_found, VsEntry(s_not_found, CodeNotFound("NOT_FOUND", None, vs1 :: Nil), None) :: Nil)
  }

  def failCodeNotFoundComplex = {
    val c_not_found = makeComplexField("NOT_FOUND", 1)
    val exp = ValueSet(".", ValueSetSpec("vs1", None, Some(BindingLocation.Position(1))))
    eval(exp, c_not_found) === Failures.valueSet(exp, c_not_found, VsEntry(c_not_found, CodeNotFound("NOT_FOUND", None, vs1 :: Nil), None) :: Nil)
  }

  def failCodeNotFoundCE1 = {
    val ce_not_found = makeCodedField(("NOT_FOUND", Some("NOT_FOUND")) :: Nil)
    val code_1 = ce_not_found.children(0)
    val exp = ValueSet(".", ValueSetSpec("vs1", None, Some(BindingLocation.Position(1))))
    eval(exp, ce_not_found) === Failures.valueSet(exp, ce_not_found, VsEntry(code_1, CodeNotFound("NOT_FOUND", Some("NOT_FOUND"), vs1 :: Nil), None) :: Nil)
  }

  def failCodeNotFoundCE2 = {
    val ce_not_found = makeCodedField(("NOT_FOUND", Some("NOT_FOUND")) :: ("NOT_FOUND_1", Some("NOT_FOUND_1")) :: Nil)
    val code_1 = ce_not_found.children(0)
    val code_2 = ce_not_found.children(2)
    val exp = ValueSet(".", ValueSetSpec("vs1", None, Some(BindingLocation.XOR(1, 4))))
    eval(exp, ce_not_found) === Failures.valueSet(exp, ce_not_found, VsEntry(code_1, CodeNotFound("NOT_FOUND", None, vs1 :: Nil), None) :: VsEntry(code_2, CodeNotFound("NOT_FOUND_1", None, vs1 :: Nil), None) :: Nil)
  }

  /*
  ValueSet expression fails when code is found in value set with usage E
    Simple Element  $failECodeNotFoundSimple
    Complex Element $failECodeNotFoundComplex
    Coded Element
      Single binding location $failECodeNotFoundCE1
      Multiple binding location $failECodeNotFoundCE2
   */

  def failECodeNotFoundSimple = {
    val s_code_e = makeSimpleField("C")
    val exp = ValueSet(".", ValueSetSpec("vs1", None, None))
    eval(exp, s_code_e) === Failures.valueSet(exp, s_code_e, VsEntry(s_code_e, EVS(Code("C", "C", CodeUsage.E, "SYS"), "C", vs1), None) :: Nil)
  }

  def failECodeNotFoundComplex = {
    val c_code_e = makeComplexField("C", 1)
    val exp = ValueSet(".", ValueSetSpec("vs1", None, Some(BindingLocation.Position(1))))
    eval(exp, c_code_e) === Failures.valueSet(exp, c_code_e, VsEntry(c_code_e, EVS(Code("C", "C", CodeUsage.E, "SYS"), "C", vs1), None) :: Nil)
  }

  def failECodeNotFoundCE1 = {
    val c_code_e = makeCodedField(("C", Some("SYS")) :: Nil)
    val code_1 = c_code_e.children(0)
    val exp = ValueSet(".", ValueSetSpec("vs1", None, Some(BindingLocation.Position(1))))
    eval(exp, c_code_e) === Failures.valueSet(exp, c_code_e, VsEntry(code_1, EVS(Code("C", "C", CodeUsage.E, "SYS"), "C", vs1), None) :: Nil)
  }

  def failECodeNotFoundCE2 = {
    val ce_not_found = makeCodedField(("NOT_FOUND", Some("NOT_FOUND")) :: ("C", Some("SYS")) :: Nil)
    val code_2 = ce_not_found.children(2)
    val exp = ValueSet(".", ValueSetSpec("vs1", None, Some(BindingLocation.XOR(1, 4))))
    eval(exp, ce_not_found) === Failures.valueSet(exp, ce_not_found, VsEntry(code_2, EVS(Code("C", "C", CodeUsage.E, "SYS"), "C", vs1), None) :: Nil)
  }

  /*

  ValueSet expression passes when code is found in value set (R, P)
    Simple Element  $passCodeNotFoundSimple
    Complex Element $passCodeNotFoundComplex
    Coded Element
      Single binding location $passCodeNotFoundCE1
      Multiple binding location $passCodeNotFoundCE2
   */

  def passSimple = {
    val s_code_r = makeSimpleField("A")
    val exp = ValueSet(".", ValueSetSpec("vs1", None, None))
    eval(exp, s_code_r) === Pass
  }

  def passComplex = {
    val c_code_r = makeComplexField("A", 1)
    val exp = ValueSet(".", ValueSetSpec("vs1", None, Some(BindingLocation.Position(1))))
    eval(exp, c_code_r) === Pass
  }

  def passCE1 = {
    val ce_r = makeCodedField(("A", Some("SYS")) :: Nil)
    val exp = ValueSet(".", ValueSetSpec("vs1", None, Some(BindingLocation.Position(1))))
    eval(exp, ce_r) === Pass
  }

  def passCE2 = {
    val ce_r = makeCodedField(("NOT_FOUND", Some("NOT_FOUND")) :: ("A", Some("SYS")) :: Nil)
    val exp = ValueSet(".", ValueSetSpec("vs1", None, Some(BindingLocation.XOR(1, 4))))
    eval(exp, ce_r) === Pass
  }

  /*
  ValueSet expression inconclusive when there is a spec error
    Simple Element with BL $incSimpleWithBL
    Complex Element with no BL $incComplexWithoutBL
    Complex Element with not existing BL $incComplexWithNotFoundBL
   */

  def incSimpleWithBL = {
    val s_code_r = makeSimpleField("A")
    val exp = ValueSet(".", ValueSetSpec("vs1", None, Some(BindingLocation.Position(1))))
    eval(exp, s_code_r) === Failures.inconclusiveValueSet(exp, s_code_r.location, VsEntry(s_code_r, InvalidCodeBindingLocation("1[1]", false), None) :: Nil)
  }

  def incComplexWithoutBL = {
    val c_code_r = makeComplexField("A", 1)
    val exp = ValueSet(".", ValueSetSpec("vs1", None, None))
    eval(exp, c_code_r) === Failures.inconclusiveValueSet(exp, c_code_r.location, VsEntry(c_code_r, InvalidCodeBindingLocation(".", false), None) :: Nil)
  }

  def incComplexWithNotFoundBL = {
    val c_code_r = makeComplexField("A", 1)
    val exp = ValueSet(".", ValueSetSpec("vs1", None,  Some(BindingLocation.Position(10))))
    eval(exp, c_code_r) === Pass
  }

  /*
  NotPresentBehaviour = FAIL
    ValueSet expression fail when path is empty $npbFailEmpty
    ValueSet expression fail when binding location's path is empty (single) $npbFailBLEmpty
    ValueSet expression fail when both binding location's path is empty (multiple) $npbFail2BLEmpty
  NotPresentBehaviour = PASS
    ValueSet expression pass when path is empty $npbPassEmpty
    ValueSet expression pass when binding location's path is empty (single) $npbPassBLEmpty
    ValueSet expression pass when both binding location's path is empty (multiple) $npbPass2BLEmpty
  NotPresentBehaviour = INCONCLUSIVE
    ValueSet expression inconclusive when path is empty $npbIncEmpty
    ValueSet expression inconclusive when binding location's path is empty (single) $npbIncBLEmpty
    ValueSet expression inconclusive when both binding location's path is empty (multiple) $npbInc2BLEmpty
   */

  def npbFailEmpty = {
    val complex_empty_pos3 = makeComplexField("A", 1)
    val exp = ValueSet("3[1]", ValueSetSpec("vs1", None,  None), "FAIL")
    eval(exp, complex_empty_pos3) === Failures.notPresentBehaviorFail(exp, "3[1]", complex_empty_pos3)
  }

  def npbFailBLEmpty = {
    val complex_empty_pos3 = makeComplexField("A", 1)
    val exp = ValueSet(".", ValueSetSpec("vs1", None, Some(BindingLocation.Position(3))), "FAIL")
    eval(exp, complex_empty_pos3) === Failures.notPresentBehaviorFail(exp, "..3[1]", complex_empty_pos3)
  }

  def npbFail2BLEmpty = {
    val complex_empty_pos3 = makeComplexField("A", 1)
    val exp = ValueSet(".", ValueSetSpec("vs1", None, Some(BindingLocation.XOR(3, 4))), "FAIL")
    eval(exp, complex_empty_pos3) === Failures.notPresentBehaviorFail(exp, "..3[1] and ..4[1]", complex_empty_pos3)
  }

  def npbPassEmpty = {
    val complex_empty_pos3 = makeComplexField("A", 1)
    val exp = ValueSet("3[1]", ValueSetSpec("vs1", None, None), "PASS")
    eval(exp, complex_empty_pos3) === Pass
  }

  def npbPassBLEmpty = {
    val complex_empty_pos3 = makeComplexField("A", 1)
    val exp = ValueSet(".", ValueSetSpec("vs1", None, Some(BindingLocation.Position(3))), "PASS")
    eval(exp, complex_empty_pos3) === Pass
  }

  def npbPass2BLEmpty = {
    val complex_empty_pos3 = makeComplexField("A", 1)
    val exp = ValueSet(".", ValueSetSpec("vs1", None, Some(BindingLocation.XOR(3, 4))), "PASS")
    eval(exp, complex_empty_pos3) === Pass
  }

  def npbIncEmpty = {
    val complex_empty_pos3 = makeComplexField("A", 1)
    val exp = ValueSet("3[1]", ValueSetSpec("vs1", None, None), "INCONCLUSIVE")
    eval(exp, complex_empty_pos3) === Failures.notPresentBehaviorInconclusive(exp, "3[1]", complex_empty_pos3)
  }

  def npbIncBLEmpty = {
    val complex_empty_pos3 = makeComplexField("A", 1)
    val exp = ValueSet(".", ValueSetSpec("vs1", None, Some(BindingLocation.Position(3))), "INCONCLUSIVE")
    eval(exp, complex_empty_pos3) === Failures.notPresentBehaviorInconclusive(exp, "..3[1]", complex_empty_pos3)
  }

  def npbInc2BLEmpty = {
    val complex_empty_pos3 = makeComplexField("A", 1)
    val exp = ValueSet(".", ValueSetSpec("vs1", None, Some(BindingLocation.XOR(3, 4))), "INCONCLUSIVE")
    eval(exp, complex_empty_pos3) === Failures.notPresentBehaviorInconclusive(exp, "..3[1] and ..4[1]", complex_empty_pos3)
  }


  def makeSimpleField(code: String) = SimpleField(
    Primitive("ID", "ID", "ID", "2.5"),
    Req(1, "", Usage.R, None, None, None, Nil, None, false, None),
    Location(EType.Field, "", "", 1, 1, "", None),
    1,
    Text(code),
    code
  )

  def makeCodedField(codes: List[(String, Option[String])]) = {
    var i = 1
    val datatype = Composite(
      "CWE",
      "CWE",
      "CWE",
      "2.5",
      codes.flatMap((code) => {
        val cc = Component(
          "Component " + i,
          Primitive("ID", "ID", "ID", "2.5"),
          Req(i, "", Usage.R, None, None, None, Nil, None, false, None),
        )
        val cs = if (code._2.isDefined) Component(
          "Component CS " + i + 2,
          Primitive("ID", "ID", "ID", "2.5"),
          Req(i + 2, "", Usage.R, None, None, None, Nil, None, false, None),
        ) :: Nil else Nil

        i = i + 3
        cc :: cs
      })
    )

    i = 1
    ComplexField(
      datatype,
      Req(1, "", Usage.R, None, None, None, Nil, None, false, None),
      Location(EType.Field, "", "", 1, 1, "", None),
      1,
      codes.flatMap((code) => {
        val cc = SimpleComponent(
          Primitive("ID", "ID", "ID", "2.5"),
          Req(i, "", Usage.R, None, None, None, Nil, None, false, None),
          Location(EType.Field, "", "", 1, 1, "", None),
          Text(code._1),
        )
        val cs = if (code._2.isDefined) SimpleComponent(
          Primitive("ID", "ID", "ID", "2.5"),
          Req(i + 2, "", Usage.R, None, None, None, Nil, None, false, None),
          Location(EType.Field, "", "", 1, 1, "", None),
          Text(code._2.get),
        ) :: Nil else Nil
        i = i + 3
        cc :: cs
      }),
      false,
      ""
    )
  }

  def makeComplexField(code: String, bl: Int) = {
    val datatype = Composite(
      "HD",
      "HD",
      "HD",
      "2.5",
      Component(
        "Component " + bl,
        Primitive("ID", "ID", "ID", "2.5"),
        Req(bl, "", Usage.R, None, None, None, Nil, None, false, None),
      ) :: Component(
        "Component " + (bl + 1),
        Primitive("ID", "ID", "ID", "2.5"),
        Req(bl + 1, "", Usage.R, None, None, None, Nil, None, false, None),
      ) :: Nil
    )

    ComplexField(
      datatype,
      Req(1, "", Usage.R, None, None, None, Nil, None, false, None),
      Location(EType.Field, "", "", 1, 1, "", None),
      1,
      SimpleComponent(
        Primitive("ID", "ID", "ID", "2.5"),
        Req(bl, "", Usage.R, None, None, None, Nil, None, false, None),
        Location(EType.Field, "", "", 1, 1, "", None),
        Text(code),
      ) :: Nil,
      false,
      ""
    )
  }

}