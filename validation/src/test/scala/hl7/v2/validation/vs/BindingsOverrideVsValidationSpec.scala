package hl7.v2.validation.vs

import gov.nist.validation.report.Entry
import hl7.v2.instance.{Message => MI, Segment => SM, _}
import hl7.v2.parser.impl.DefaultParser
import hl7.v2.profile.{Component, Composite, Field, Primitive, Req, Segment, Usage, Group => GM, Message => MM, _}
import hl7.v2.validation.FeatureFlags
import hl7.v2.validation.structure.Helpers
import hl7.v2.validation.vs.factory.impl.java.{DefaultValueSetFactory, DefaultValueSetFactoryConfiguration}
import hl7.v2.validation.vs.{InternalValueSet => ValueSet}
import hl7.v2.validation.vs.BindingLocation
import hl7.v2.validation.vs.factory.ValueSetFactory
import org.specs2.Specification
import org.specs2.matcher.MatchResult

import scala.concurrent.Await
import scala.concurrent.duration.Duration

trait BindingsOverrideVsValidationSpec extends Specification
       with Helpers
       with DefaultParser {def is =
  s2"""
  Bindings Override Specification
    Value Set
      Message Level Value Set Binding Override
        Overrides binding at the component level $messageLevelBindingOverrideComponent
        Overrides binding at the field level $messageLevelBindingOverrideField
        Overrides binding at the segment level $messageLevelBindingOverrideSegment
        Overrides bindings at all the levels $messageLevelBindingOverrideAllLevels
        Multiple Message Level Bindings should all be validated $messageLevelBindingMultipleOverrideAllLevels
      Segment Level Value Set Binding Override
        Overrides binding at the component level $segmentLevelBindingOverrideComponent
        Overrides binding at the field level $segmentLevelBindingOverrideField
        Overrides bindings at all the levels $segmentLevelBindingOverrideAllLevels
      Datatype (Field) Level Value Set Binding Override
        Overrides binding at the component level $fieldLevelBindingOverrideComponent
    Single Code Binding
      Message Level Single Code Binding Override
        Overrides binding at the component level $sgMessageLevelBindingOverrideComponent
        Overrides binding at the field level $sgMessageLevelBindingOverrideField
        Overrides binding at the segment level $sgMessageLevelBindingOverrideSegment
        Overrides bindings at all the levels $sgMessageLevelBindingOverrideAllLevels
        Multiple Message Level Bindings should all be validated $sgMessageLevelBindingMultipleOverrideAllLevels
      Segment Level Single Code Binding Override
        Overrides binding at the component level $sgSegmentLevelBindingOverrideComponent
        Overrides binding at the field level $sgSegmentLevelBindingOverrideField
        Overrides bindings at all the levels $sgSegmentLevelBindingOverrideAllLevels
      Datatype (Field) Level Single Code Binding Override
        Overrides binding at the component level $sgFieldLevelBindingOverrideComponent
    Single Code & Value Set
      Higher Single Code Binding shall override lower Value Set Binding $sgOverridesVs
      Higher Value Set Binding shall override lower Single Code Binding $vsOverridesSg
  """
  implicit val separators: Separators = Separators( '|', '^', '~', '\\', '&', Some('#') )
  implicit val time: Option[TimeZone] = None
  override implicit val featureFlags: FeatureFlags = FeatureFlags()
  val vs1: ValueSet = ValueSet("vs1", None, None, List(Code("A", "des", CodeUsage.R, "SYS"),Code("B", "des", CodeUsage.P, "SYS"),Code("C", "des", CodeUsage.E, "SYS")))
  val vs2: ValueSet = ValueSet("vs2", None, None, List(Code("X", "des", CodeUsage.R, "SYS"),Code("Y", "des", CodeUsage.P, "SYS"),Code("Z", "des", CodeUsage.E, "SYS")))
  val vs3: ValueSet = ValueSet("vs3", None, None, List(Code("D", "des", CodeUsage.R, "SYS"),Code("E", "des", CodeUsage.P, "SYS"),Code("F", "des", CodeUsage.E, "SYS")))

  // ============================================ TEST CASES ===========================================
  val messageLevelBindingOverrideAllLevels: MatchResult[Seq[Entry]] = {
    checkOverridesValueSet(
      Map(
        0 -> Map("MSG" -> List(
          ValueSetBinding(
            "1[*].1[*].1[*].1[*]",
            Some(BindingStrength.R),
            List("vs1"),
            List(BindingLocation("1[1]", Some("3[1]")))
          )
        )),
        1 -> Map("SEG" -> List(
          ValueSetBinding(
            "1[*].1[*]",
            Some(BindingStrength.R),
            List("vs2"),
            List(BindingLocation("1[1]", Some("3[1]")))
          )
        )),
        2 -> Map(
          "CWE" -> List(
            ValueSetBinding(
              ".",
              Some(BindingStrength.R),
              List("vs2"),
              List(BindingLocation("1[1]", Some("3[1]")))
            )),
          "TWO" -> List(
            ValueSetBinding(
              "1[*]",
              Some(BindingStrength.R),
              List("vs1"),
              List(BindingLocation("1[1]", Some("3[1]")))
            )
          )
        )
      ),
      "SEG|A&DESC&SYS",
      ("1[*].1[*].1[*].1[*]", VSValidationCode.RVS(Code("A", "des", CodeUsage.R, "SYS"), "A", vs1))::Nil
    )
  }

  val sgMessageLevelBindingMultipleOverrideAllLevels: MatchResult[Seq[Entry]] = {
    val parsedMessage = parse("SEG|A&DESC&SYS"::Nil)
    checkOverridesSingleCode(
      Map(
        0 -> Map("MSG" -> List(
          SingleCodeBinding(
            "1[*].1[*].1[*].1[*]",
            "A", "SYS",
            List(BindingLocation("1[1]", Some("3[1]")))
          ),
          SingleCodeBinding(
            "1[*].1[*].1[*].1[*]",
            "Y", "SYS",
            List(BindingLocation("1[1]", Some("3[1]")))
          )
        )),
        1 -> Map("SEG" -> List(
          SingleCodeBinding(
            "1[*].1[*]",
            "X", "SYS",
            List(BindingLocation("1[1]", Some("3[1]")))
          )
        )),
        2 -> Map(
          "CWE" -> List(
            SingleCodeBinding(
              ".",
              "X", "SYS",
              List(BindingLocation("1[1]", Some("3[1]")))
            )),
          "TWO" -> List(
            SingleCodeBinding(
              "1[*]",
              "X", "SYS",
              List(BindingLocation("1[1]", Some("3[1]")))
            )
          )
        )
      ),
      parsedMessage,
      ("1[*].1[*].1[*].1[*]", VSValidationCode.SingleCodeNotFound(
        getOneElementByPathAsSimple(parsedMessage, "1[*].1[*].1[*].1[*].1[*]"),
        "A",
        SingleCodeBinding(
          "1[*].1[*].1[*].1[*]",
          "Y", "SYS",
          List(BindingLocation("1[1]", Some("3[1]")))
        )
      ))::
      ("1[*].1[*].1[*].1[*]", VSValidationCode.SingleCodeSuccess(
        getOneElementByPath(parsedMessage, "1[*].1[*].1[*].1[*]"),
        getOneElementByPathAsSimple(parsedMessage, "1[*].1[*].1[*].1[*].1[*]"),
        SingleCodeBinding(
          "1[*].1[*].1[*].1[*]",
          "A", "SYS",
          List(BindingLocation("1[1]", Some("3[1]")))
        )
      ))::Nil
    )
  }

  val sgMessageLevelBindingOverrideAllLevels: MatchResult[Seq[Entry]] = {
    val parsedMessage = parse("SEG|A&DESC&SYS"::Nil)
    checkOverridesSingleCode(
      Map(
        0 -> Map("MSG" -> List(
          SingleCodeBinding(
            "1[*].1[*].1[*].1[*]",
            "A", "SYS",
            List(BindingLocation("1[1]", Some("3[1]")))
          )
        )),
        1 -> Map("SEG" -> List(
          SingleCodeBinding(
            "1[*].1[*]",
            "X", "SYS",
            List(BindingLocation("1[1]", Some("3[1]")))
          )
        )),
        2 -> Map(
          "CWE" -> List(
            SingleCodeBinding(
              ".",
              "X", "SYS",
              List(BindingLocation("1[1]", Some("3[1]")))
            )),
          "TWO" -> List(
            SingleCodeBinding(
              "1[*]",
              "X", "SYS",
              List(BindingLocation("1[1]", Some("3[1]")))
            )
          )
        )
      ),
      parsedMessage,
      ("1[*].1[*].1[*].1[*]", VSValidationCode.SingleCodeSuccess(
        getOneElementByPath(parsedMessage, "1[*].1[*].1[*].1[*]"),
        getOneElementByPathAsSimple(parsedMessage, "1[*].1[*].1[*].1[*].1[*]"),
        SingleCodeBinding(
          "1[*].1[*].1[*].1[*]",
          "A", "SYS",
          List(BindingLocation("1[1]", Some("3[1]")))
        )
      ))::Nil
    )
  }

  val sgMessageLevelBindingOverrideSegment: MatchResult[Seq[Entry]] = {
    val parsedMessage = parse("SEG|A&DESC&SYS"::Nil)
    checkOverridesSingleCode(
      Map(
        0 -> Map("MSG" -> List(
          SingleCodeBinding(
            "1[*].1[*].1[*].1[*]",
            "A", "SYS",
            List(BindingLocation("1[1]", Some("3[1]")))
          )
        )),
        1 -> Map("SEG" -> List(
          SingleCodeBinding(
            "1[*].1[*]",
            "X", "SYS",
            List(BindingLocation("1[1]", Some("3[1]")))
          )
        )),
      ),
      parsedMessage,
      ("1[*].1[*].1[*].1[*]", VSValidationCode.SingleCodeSuccess(
        getOneElementByPath(parsedMessage, "1[*].1[*].1[*].1[*]"),
        getOneElementByPathAsSimple(parsedMessage, "1[*].1[*].1[*].1[*].1[*]"),
        SingleCodeBinding(
          "1[*].1[*].1[*].1[*]",
          "A", "SYS",
          List(BindingLocation("1[1]", Some("3[1]")))
        )
      ))::Nil
    )
  }

  val sgMessageLevelBindingOverrideField: MatchResult[Seq[Entry]] = {
    val parsedMessage = parse("SEG|A&DESC&SYS"::Nil)
    checkOverridesSingleCode(
      Map(
        0 -> Map("MSG" -> List(
          SingleCodeBinding(
            "1[*].1[*].1[*].1[*]",
            "A", "SYS",
            List(BindingLocation("1[1]", Some("3[1]")))
          )
        )),
        2 -> Map("TWO" -> List(
          SingleCodeBinding(
            "1[*]",
            "X", "SYS",
            List(BindingLocation("1[1]", Some("3[1]")))
          )
        )),
      ),
      parsedMessage,
      ("1[*].1[*].1[*].1[*]", VSValidationCode.SingleCodeSuccess(
        getOneElementByPath(parsedMessage, "1[*].1[*].1[*].1[*]"),
        getOneElementByPathAsSimple(parsedMessage, "1[*].1[*].1[*].1[*].1[*]"),
        SingleCodeBinding(
          "1[*].1[*].1[*].1[*]",
          "A", "SYS",
          List(BindingLocation("1[1]", Some("3[1]")))
        )
      ))::Nil
    )
  }

  val sgMessageLevelBindingOverrideComponent: MatchResult[Seq[Entry]] = {
    val parsedMessage = parse("SEG|A&DESC&SYS"::Nil)
    checkOverridesSingleCode(
      Map(
        0 -> Map("MSG" -> List(
          SingleCodeBinding(
            "1[*].1[*].1[*].1[*]",
            "A", "SYS",
            List(BindingLocation("1[1]", Some("3[1]")))
          )
        )),
        2 -> Map("CWE" -> List(
          SingleCodeBinding(
            ".",
            "X", "SYS",
            List(BindingLocation("1[1]", Some("3[1]")))
          )
        )),
      ),
      parsedMessage,
      ("1[*].1[*].1[*].1[*]", VSValidationCode.SingleCodeSuccess(
        getOneElementByPath(parsedMessage, "1[*].1[*].1[*].1[*]"),
        getOneElementByPathAsSimple(parsedMessage, "1[*].1[*].1[*].1[*].1[*]"),
        SingleCodeBinding(
          "1[*].1[*].1[*].1[*]",
          "A", "SYS",
          List(BindingLocation("1[1]", Some("3[1]")))
        )
      ))::Nil
    )
  }

  val messageLevelBindingMultipleOverrideAllLevels: MatchResult[Seq[Entry]] = {
    checkOverridesValueSet(
      Map(
        0 -> Map("MSG" -> List(
          ValueSetBinding(
            "1[*].1[*].1[*].1[*]",
            Some(BindingStrength.R),
            List("vs1"),
            List(BindingLocation("1[1]", Some("3[1]")))
          ),
          ValueSetBinding(
            "1[*].1[*].1[*].1[*]",
            Some(BindingStrength.R),
            List("vs3"),
            List(BindingLocation("1[1]", Some("3[1]")))
          )
        )),
        1 -> Map("SEG" -> List(
          ValueSetBinding(
            "1[*].1[*]",
            Some(BindingStrength.R),
            List("vs2"),
            List(BindingLocation("1[1]", Some("3[1]")))
          )
        )),
        2 -> Map(
          "CWE" -> List(
            ValueSetBinding(
              ".",
              Some(BindingStrength.R),
              List("vs2"),
              List(BindingLocation("1[1]", Some("3[1]")))
            )),
          "TWO" -> List(
            ValueSetBinding(
              "1[*]",
              Some(BindingStrength.R),
              List("vs1"),
              List(BindingLocation("1[1]", Some("3[1]")))
            )
          )
        )
      ),
      "SEG|A&DESC&SYS",
      ("1[*].1[*].1[*].1[*]", VSValidationCode.RVS(Code("A", "des", CodeUsage.R, "SYS"), "A", vs1))::
      ("1[*].1[*].1[*].1[*]", VSValidationCode.CodeNotFound("A", Some("SYS"), List(vs3)))::Nil
    )
  }

  val messageLevelBindingOverrideSegment: MatchResult[Seq[Entry]] = {
    checkOverridesValueSet(
      Map(
        0 -> Map("MSG" -> List(
          ValueSetBinding(
            "1[*].1[*].1[*].1[*]",
            Some(BindingStrength.R),
            List("vs1"),
            List(BindingLocation("1[1]", Some("3[1]")))
          )
        )),
        1 -> Map("SEG" -> List(
          ValueSetBinding(
            "1[*].1[*]",
            Some(BindingStrength.R),
            List("vs2"),
            List(BindingLocation("1[1]", Some("3[1]")))
          )
        ))
      ),
      "SEG|A&DESC&SYS",
      ("1[*].1[*].1[*].1[*]", VSValidationCode.RVS(Code("A", "des", CodeUsage.R, "SYS"), "A", vs1))::Nil
    )
  }

  val messageLevelBindingOverrideField: MatchResult[Seq[Entry]] = {
    checkOverridesValueSet(
      Map(
        0 -> Map("MSG" -> List(
          ValueSetBinding(
            "1[*].1[*].1[*].1[*]",
            Some(BindingStrength.R),
            List("vs1"),
            List(BindingLocation("1[1]", Some("3[1]")))
          )
        )),
        2 -> Map("TWO" -> List(
          ValueSetBinding(
            "1[*]",
            Some(BindingStrength.R),
            List("vs2"),
            List(BindingLocation("1[1]", Some("3[1]")))
          )
        ))
      ),
      "SEG|A&DESC&SYS",
      ("1[*].1[*].1[*].1[*]", VSValidationCode.RVS(Code("A", "des", CodeUsage.R, "SYS"), "A", vs1))::Nil
    )
  }

  val messageLevelBindingOverrideComponent: MatchResult[Seq[Entry]] = {
    checkOverridesValueSet(
      Map(
        0 -> Map("MSG" -> List(
          ValueSetBinding(
            "1[*].1[*].1[*].1[*]",
            Some(BindingStrength.R),
            List("vs1"),
            List(BindingLocation("1[1]", Some("3[1]")))
          )
        )),
        2 -> Map("CWE" -> List(
          ValueSetBinding(
            ".",
            Some(BindingStrength.R),
            List("vs2"),
            List(BindingLocation("1[1]", Some("3[1]")))
          )
        ))
      ),
      "SEG|A&DESC&SYS",
      ("1[*].1[*].1[*].1[*]", VSValidationCode.RVS(Code("A", "des", CodeUsage.R, "SYS"), "A", vs1))::Nil
    )
  }

  val segmentLevelBindingOverrideAllLevels: MatchResult[Seq[Entry]] = {
    checkOverridesValueSet(
      Map(
        1 -> Map("SEG" -> List(
          ValueSetBinding(
            "1[*].1[*]",
            Some(BindingStrength.R),
            List("vs1"),
            List(BindingLocation("1[1]", Some("3[1]")))
          )
        )),
        2 -> Map(
          "CWE" -> List(
            ValueSetBinding(
              ".",
              Some(BindingStrength.R),
              List("vs2"),
              List(BindingLocation("1[1]", Some("3[1]")))
            )),
          "TWO" -> List(
            ValueSetBinding(
              "1[*]",
              Some(BindingStrength.R),
              List("vs1"),
              List(BindingLocation("1[1]", Some("3[1]")))
            )
          )
        )
      ),
      "SEG|A&DESC&SYS",
      ("1[*].1[*].1[*].1[*]", VSValidationCode.RVS(Code("A", "des", CodeUsage.R, "SYS"), "A", vs1))::Nil
    )
  }

  val segmentLevelBindingOverrideField: MatchResult[Seq[Entry]] = {
    checkOverridesValueSet(
      Map(
        1 -> Map("SEG" -> List(
          ValueSetBinding(
            "1[*].1[*]",
            Some(BindingStrength.R),
            List("vs1"),
            List(BindingLocation("1[1]", Some("3[1]")))
          )
        )),
        2 -> Map("TWO" -> List(
          ValueSetBinding(
            "1[*]",
            Some(BindingStrength.R),
            List("vs2"),
            List(BindingLocation("1[1]", Some("3[1]")))
          )
        ))
      ),
      "SEG|A&DESC&SYS",
      ("1[*].1[*].1[*].1[*]", VSValidationCode.RVS(Code("A", "des", CodeUsage.R, "SYS"), "A", vs1))::Nil
    )
  }

  val segmentLevelBindingOverrideComponent: MatchResult[Seq[Entry]] = {
    checkOverridesValueSet(
      Map(
        1 -> Map("SEG" -> List(
          ValueSetBinding(
            "1[*].1[*]",
            Some(BindingStrength.R),
            List("vs1"),
            List(BindingLocation("1[1]", Some("3[1]")))
          )
        )),
        2 -> Map("CWE" -> List(
          ValueSetBinding(
            ".",
            Some(BindingStrength.R),
            List("vs2"),
            List(BindingLocation("1[1]", Some("3[1]")))
          )
        ))
      ),
      "SEG|A&DESC&SYS",
      ("1[*].1[*].1[*].1[*]", VSValidationCode.RVS(Code("A", "des", CodeUsage.R, "SYS"), "A", vs1))::Nil
    )
  }

  val sgSegmentLevelBindingOverrideComponent: MatchResult[Seq[Entry]] = {
    val parsedMessage = parse("SEG|A&DESC&SYS"::Nil)
    checkOverridesSingleCode(
      Map(
        1 -> Map("SEG" -> List(
          SingleCodeBinding(
            "1[*].1[*]",
            "A", "SYS",
            List(BindingLocation("1[1]", Some("3[1]")))
          )
        )),
        2 -> Map("CWE" -> List(
          SingleCodeBinding(
            ".",
            "X", "SYS",
            List(BindingLocation("1[1]", Some("3[1]")))
          )
        )),
      ),
      parsedMessage,
      ("1[*].1[*].1[*].1[*]", VSValidationCode.SingleCodeSuccess(
        getOneElementByPath(parsedMessage, "1[*].1[*].1[*].1[*]"),
        getOneElementByPathAsSimple(parsedMessage, "1[*].1[*].1[*].1[*].1[*]"),
        SingleCodeBinding(
          "1[*].1[*].1[*].1[*]",
          "A", "SYS",
          List(BindingLocation("1[1]", Some("3[1]")))
        )
      ))::Nil
    )
  }

  val sgSegmentLevelBindingOverrideField: MatchResult[Seq[Entry]] = {
    val parsedMessage = parse("SEG|A&DESC&SYS"::Nil)
    checkOverridesSingleCode(
      Map(
        1 -> Map("SEG" -> List(
          SingleCodeBinding(
            "1[*].1[*]",
            "A", "SYS",
            List(BindingLocation("1[1]", Some("3[1]")))
          )
        )),
        2 -> Map("TWO" -> List(
          SingleCodeBinding(
            "1[*]",
            "X", "SYS",
            List(BindingLocation("1[1]", Some("3[1]")))
          )
        )),
      ),
      parsedMessage,
      ("1[*].1[*].1[*].1[*]", VSValidationCode.SingleCodeSuccess(
        getOneElementByPath(parsedMessage, "1[*].1[*].1[*].1[*]"),
        getOneElementByPathAsSimple(parsedMessage, "1[*].1[*].1[*].1[*].1[*]"),
        SingleCodeBinding(
          "1[*].1[*].1[*].1[*]",
          "A", "SYS",
          List(BindingLocation("1[1]", Some("3[1]")))
        )
      ))::Nil
    )
  }

  val sgSegmentLevelBindingOverrideAllLevels: MatchResult[Seq[Entry]] = {
    val parsedMessage = parse("SEG|A&DESC&SYS"::Nil)
    checkOverridesSingleCode(
      Map(
        1 -> Map("SEG" -> List(
          SingleCodeBinding(
            "1[*].1[*]",
            "A", "SYS",
            List(BindingLocation("1[1]", Some("3[1]")))
          )
        )),
        2 -> Map("TWO" -> List(
          SingleCodeBinding(
            "1[*]",
            "X", "SYS",
            List(BindingLocation("1[1]", Some("3[1]")))
          )
        ), "CWE" -> List(
          SingleCodeBinding(
            ".",
            "X", "SYS",
            List(BindingLocation("1[1]", Some("3[1]")))
          )
        )),
      ),
      parsedMessage,
      ("1[*].1[*].1[*].1[*]", VSValidationCode.SingleCodeSuccess(
        getOneElementByPath(parsedMessage, "1[*].1[*].1[*].1[*]"),
        getOneElementByPathAsSimple(parsedMessage, "1[*].1[*].1[*].1[*].1[*]"),
        SingleCodeBinding(
          "1[*].1[*].1[*].1[*]",
          "A", "SYS",
          List(BindingLocation("1[1]", Some("3[1]")))
        )
      ))::Nil
    )
  }

  val fieldLevelBindingOverrideComponent: MatchResult[Seq[Entry]] = {
    checkOverridesValueSet(
      Map(
        2 -> Map(
          "CWE" -> List(
            ValueSetBinding(
              ".",
              Some(BindingStrength.R),
              List("vs2"),
              List(BindingLocation("1[1]", Some("3[1]")))
            )),
          "TWO" -> List(
            ValueSetBinding(
              "1[*]",
              Some(BindingStrength.R),
              List("vs1"),
              List(BindingLocation("1[1]", Some("3[1]")))
            )
          )
        )
      ),
      "SEG|A&DESC&SYS",
      ("1[*].1[*].1[*].1[*]", VSValidationCode.RVS(Code("A", "des", CodeUsage.R, "SYS"), "A", vs1))::Nil
    )
  }

  val sgFieldLevelBindingOverrideComponent: MatchResult[Seq[Entry]] = {
    val parsedMessage = parse("SEG|A&DESC&SYS"::Nil)
    checkOverridesSingleCode(
      Map(
        2 -> Map("TWO" -> List(
          SingleCodeBinding(
            "1[*]",
            "A", "SYS",
            List(BindingLocation("1[1]", Some("3[1]")))
          )
        ), "CWE" -> List(
          SingleCodeBinding(
            ".",
            "X", "SYS",
            List(BindingLocation("1[1]", Some("3[1]")))
          )
        )),
      ),
      parsedMessage,
      ("1[*].1[*].1[*].1[*]", VSValidationCode.SingleCodeSuccess(
        getOneElementByPath(parsedMessage, "1[*].1[*].1[*].1[*]"),
        getOneElementByPathAsSimple(parsedMessage, "1[*].1[*].1[*].1[*].1[*]"),
        SingleCodeBinding(
          "1[*].1[*].1[*].1[*]",
          "A", "SYS",
          List(BindingLocation("1[1]", Some("3[1]")))
        )
      ))::Nil
    )
  }

  val sgOverridesVs: MatchResult[Seq[Entry]] = {
    val specification: ValueSetSpecification = new DefaultValueSetSpecification(
      Map(
        "CWE" -> List(
          ValueSetBinding(
            ".",
            Some(BindingStrength.R),
            List("vs2"),
            List(BindingLocation("1[1]", Some("3[1]")))
          )),
        "TWO" -> List(
          ValueSetBinding(
            "1[*]",
            Some(BindingStrength.R),
            List("vs1"),
            List(BindingLocation("1[1]", Some("3[1]")))
          )
        )
      ),
      Map("SEG" -> List(
        ValueSetBinding(
          "1[*].1[*]",
          Some(BindingStrength.R),
          List("vs2"),
          List(BindingLocation("1[1]", Some("3[1]")))
        )
      )),
      Map(),
      Map(
        "CWE" -> List(
          SingleCodeBinding(
            ".",
            "X", "SYS",
            List(BindingLocation("1[1]", Some("3[1]")))
          )),
        "TWO" -> List(
          SingleCodeBinding(
            "1[*]",
            "X", "SYS",
            List(BindingLocation("1[1]", Some("3[1]")))
          )
        )
      ),
      Map("SEG" -> List(
        SingleCodeBinding(
          "1[*].1[*]",
          "X", "SYS",
          List(BindingLocation("1[1]", Some("3[1]")))
        )
      )),
      Map("MSG" -> List(
        SingleCodeBinding(
          "1[*].1[*].1[*].1[*]",
          "A", "SYS",
          List(BindingLocation("1[1]", Some("3[1]")))
        )
      ))
    )
    val parsedMessage = parse("SEG|A&DESC&SYS"::Nil)
    val validator = createValidator(specification)
    val results = Await.result(validator.checkValueSets(parsedMessage, None), Duration.Inf)
    results must containTheSameElementsAs(
      validator.createEntry(
        getOneElementByPath(parsedMessage, "1[*].1[*].1[*].1[*]"),
        VSValidationCode.SingleCodeSuccess(
          getOneElementByPath(parsedMessage, "1[*].1[*].1[*].1[*]"),
          getOneElementByPathAsSimple(parsedMessage, "1[*].1[*].1[*].1[*].1[*]"),
          SingleCodeBinding(
            "1[*].1[*].1[*].1[*]",
            "A", "SYS",
            List(BindingLocation("1[1]", Some("3[1]")))
          )
        ),
        None
      )::Nil
    )
  }

  val vsOverridesSg: MatchResult[Seq[Entry]] = {
    val specification: ValueSetSpecification = new DefaultValueSetSpecification(
      Map(
        "CWE" -> List(
          ValueSetBinding(
            ".",
            Some(BindingStrength.R),
            List("vs2"),
            List(BindingLocation("1[1]", Some("3[1]")))
          )),
        "TWO" -> List(
          ValueSetBinding(
            "1[*]",
            Some(BindingStrength.R),
            List("vs1"),
            List(BindingLocation("1[1]", Some("3[1]")))
          )
        )
      ),
      Map("SEG" -> List(
        ValueSetBinding(
          "1[*].1[*]",
          Some(BindingStrength.R),
          List("vs2"),
          List(BindingLocation("1[1]", Some("3[1]")))
        )
      )),
      Map("MSG" -> List(
        ValueSetBinding(
          "1[*].1[*].1[*].1[*]",
          Some(BindingStrength.R),
          List("vs1"),
          List(BindingLocation("1[1]", Some("3[1]")))
        )
      )),
      Map(
        "CWE" -> List(
          SingleCodeBinding(
            ".",
            "X", "SYS",
            List(BindingLocation("1[1]", Some("3[1]")))
          )),
        "TWO" -> List(
          SingleCodeBinding(
            "1[*]",
            "X", "SYS",
            List(BindingLocation("1[1]", Some("3[1]")))
          )
        )
      ),
      Map("SEG" -> List(
        SingleCodeBinding(
          "1[*].1[*]",
          "X", "SYS",
          List(BindingLocation("1[1]", Some("3[1]")))
        )
      )),
      Map()
    )
    val parsedMessage = parse("SEG|A&DESC&SYS"::Nil)
    val validator = createValidator(specification)
    val results = Await.result(validator.checkValueSets(parsedMessage, None), Duration.Inf)
    results must containTheSameElementsAs(
      validator.createEntry(
        getOneElementByPath(parsedMessage, "1[*].1[*].1[*].1[*]"),
        VSValidationCode.RVS(Code("A", "des", CodeUsage.R, "SYS"), "A", vs1),
        Some(BindingStrength.R)
      )::Nil
    )
  }

  // ============================================ MOCK ===========================================
  //  MSG / GRP / SEG (SEG) / FIELD (TWO) / COMP (CWE) / SUB-COMP (ST)
  def req(p: Int): Req = Req(p, "desc", Usage.R, None, None, None, Nil, None, hide = false, None)
  def seg_fields: Map[String, Field] = Map(
    "F1" -> Field("", TWO, req(1)),
  )
  def two_components: Map[String, Component] = Map(
    "C1" -> Component("C1", CWE, req(1))
  )
  def cwe_components: Map[String, Component] = Map(
    "C1" -> Component("C1", ST, req(1)),
    "C2" -> Component("C2", ST, req(2)),
    "C3" -> Component("C3", ST, req(3)),
  )
  def GroupRef: GM = GM("GRP", "GRP", List(SegRef), req(1))
  def MSG: MM = MM("MSG", "MSG", "MSG", "MSG", "MSG", List(GroupRef))
  def SegRef: SegmentRef = SegmentRef(req(1), SEG)
  def SEG: Segment = Segment("SEG", "SEG","", seg_fields.values.toList.sortBy(_.req.position), Nil)
  def CWE: Composite = Composite("CWE", "CWE", "Coded Element", "2.5", cwe_components.values.toList.sortBy(_.req.position))
  def TWO: Composite = Composite("TWO", "TWO", "Two Levels", "2.5", two_components.values.toList.sortBy(_.req.position))
  def ST: Primitive = Primitive("ST", "ST", "String", "2.5")
  def parse(values: List[String]): MI = {
    MI(MSG, List(Group(GroupRef, 1, None, segments(values))), Nil, Nil, time, separators)
  }
  def group(vs: List[String], group: GM): Group = Group(group, 1, None, segments(vs))
  def segments(vs: List[String]): List[SM] = {
    vs.zipWithIndex.map(v => {
      segment(v._1, v._2 + 1)
    })
  }
  def segment(v: String, i: Int): SM = {
    implicit val counter: Counter = Counter(scala.collection.mutable.Map[String, Int]())
    SM(SegRef, None, v, i, -1)
  }
  // ============================================ HELPERS ===========================================

  trait Default {
    val reqs: List[Req] = List[Req]()
    val location: Location = Location(null, "desc ...", "Path", -1, -1, "", None)
    val hasExtra = false
    val req: Req = Req(-1, "", Usage.O, None, None, None, Nil, None, hide = false, None)
    val rawMessageValue = ""
  }

  case class S(override val position: Int, instance: Int, value: Value, override val req: Req = Req(-1, "", Usage.O, None, None, None, Nil, None, hide = false, None))
    extends Simple  with Default

  case class C(override val position: Int, instance: Int, children: List[Element], override val req: Req = Req(-1, "", Usage.O, None, None, None, Nil, None, hide = false, None))
    extends Complex with Default

  def createValidator(spec: ValueSetSpecification): DefaultValueSetValidator = {
    val valueSetLibrary = new ValueSetLibraryImpl(Nil, (vs1::vs2::vs3::Nil).map(x => { (x.id, x) }).toMap)
    val factory: hl7.v2.validation.vs.factory.ValueSetFactory = new DefaultValueSetFactory(valueSetLibrary, new DefaultValueSetFactoryConfiguration(true, false))
    object validator extends DefaultValueSetValidator {
      override def vsSpecification: ValueSetSpecification = spec
      implicit val valueSetFactory: ValueSetFactory = factory
    }
    validator
  }

  def checkOverridesValueSet(bindings: Map[Int, Map[String, List[ValueSetBinding]]], message: String, expectations: List[(String, VSValidationCode)]): MatchResult[Seq[Entry]] = {
    val specification: ValueSetSpecification = new DefaultValueSetSpecification(
      bindings.getOrElse(2, Map()),
      bindings.getOrElse(1, Map()),
      bindings.getOrElse(0, Map()),
      Map(),
      Map(),
      Map(),
    )
    val validator = createValidator(specification)
    val parsedMessage = parse(message::Nil)
    val results = Await.result(validator.checkValueSets(parsedMessage, None), Duration.Inf)
    val entries = expectations.flatMap(expectation => {
      val elements = getElementByPath(parsedMessage, expectation._1)
      elements.map(element => validator.createEntry(element, expectation._2, Some(BindingStrength.R)))
    })
    results must containTheSameElementsAs(entries)
  }

  def checkOverridesSingleCode(bindings: Map[Int, Map[String, List[SingleCodeBinding]]], parsedMessage: MI, expectations: List[(String, VSValidationCode)]): MatchResult[Seq[Entry]] = {
    val specification: ValueSetSpecification = new DefaultValueSetSpecification(
      Map(),
      Map(),
      Map(),
      bindings.getOrElse(2, Map()),
      bindings.getOrElse(1, Map()),
      bindings.getOrElse(0, Map()),
    )
    val validator = createValidator(specification)
    val results = Await.result(validator.checkValueSets(parsedMessage, None), Duration.Inf)
    val entries = expectations.flatMap(expectation => {
      val elements = getElementByPath(parsedMessage, expectation._1)
      elements.map(element => validator.createEntry(element, expectation._2, Some(BindingStrength.R)))
    })
    results must containTheSameElementsAs(entries)
  }

  def getElementByPath(message: MI, path: String): List[Element] = {
    Query.query(message.asGroup, path).getOrElse(Nil)
  }

  def getOneElementByPath(message: MI, path: String): Element = {
    Query.query(message.asGroup, path).getOrElse(Nil).head
  }

  def getOneElementByPathAsSimple(message: MI, path: String): Simple = {
    Query.queryAsSimple(message.asGroup, path).getOrElse(Nil).head
  }
}