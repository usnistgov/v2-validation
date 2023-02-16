package hl7.v2.validation.slicing

import expression.EvalResult.Pass
import expression.{EvalResult, PlainText}
import hl7.v2.instance.Query.query
import hl7.v2.validation.structure.Helpers
import org.specs2.Specification
import hl7.v2.instance.{ComplexField, Counter, Element, Field, Group, Line, NULLComplexField, SegOrGroup, Segment, SimpleField, Message => MI}
import hl7.v2.parser.impl.DefaultParser
import hl7.v2.profile.{Composite, Datatype, Message, Req, Segment => SM}
import org.specs2.matcher.MatchResult

trait SlicingEvalSpec extends Specification
  with hl7.v2.validation.slicing.DefaultSliceEvaluator
  with expression.DefaultEvaluator
  with SlicingEvalMocks
  with Helpers
  with DefaultParser
{

  // =================== FIELD ====================

  def fieldAssertionSlicingTest(SLICING: FieldAssertionSlicing, expected: (List[Field], Element, List[SlicingCode]) => List[MatchResult[Any]]) = {
    def SEGMENT_A = mkSegment("SGA||A^B^C^D", SEGMENT_REF_A)
    val (fields, codes) = SEGMENT_A.children.map((child) => applyFieldSlices(SLICING, child)).foldLeft(List[Field](), List[SlicingCode]()){
      (acc, x) => (acc._1 ::: List(x._1), acc._2 ::: x._2)
    }
    val TARGET_FIELD = assertGet(SEGMENT_A, s"${SLICING.position}[*]")
    expected(fields, TARGET_FIELD, codes)
  }


  def assertionSliceMatchFieldFailPass(): Seq[MatchResult[Any]] = {
    def MATCH : AssertionSlice[Datatype] = AssertionSlice("If SGA-1 is valued 'A' (PASS)", PlainText("1[*]", "A", ignoreCase = true), CWE_FLV1)
    def SLICING = FieldAssertionSlicing(
      position = 2,
      List(
        AssertionSlice("If SGA-1 is valued 'X' (FAIL)", PlainText("1[*]", "X", ignoreCase = true), CWE_FLV2),
        MATCH,
      )
    )

    fieldAssertionSlicingTest(
      SLICING,
      (fields, TARGET_FIELD, codes) => {
        val containsASliceMatch = codes must containTheSameElementsAs(
          List(AssertionSliceMatch(MATCH, TARGET_FIELD))
        )
        containsASliceMatch :: fields.filter(_.position == 2)
          .map(
            hasProfile(
              _,
              MATCH.use,
              mustHaveExtra(
                TARGET_FIELD.asInstanceOf[ComplexField].children,
                MATCH.use.asInstanceOf[Composite].reqs
              )
            )
          )
      }
    )
  }

  def assertionSliceMatchFieldPassPass(): Seq[MatchResult[Any]] = {
    def MATCH : AssertionSlice[Datatype] = AssertionSlice("If SGA-1 is valued 'A' (PASS)", PlainText("1[*]", "A", ignoreCase = true), CWE_FLV1)
    def PASS : AssertionSlice[Datatype] =  AssertionSlice("If SGA-2 is valued 'B' (PASS)", PlainText("2[*]", "B", ignoreCase = true), CWE_FLV2)
    def SLICING = FieldAssertionSlicing(
      position = 2,
      List(
        MATCH,
        PASS
      )
    )
    fieldAssertionSlicingTest(
      SLICING,
      (fields, TARGET_FIELD, codes) => {
        val containsASliceMatch = codes must containTheSameElementsAs(
          List(AssertionSliceMatch(MATCH, TARGET_FIELD))
        )

        val secondPass = eval(PASS.assertion, TARGET_FIELD) === Pass
        containsASliceMatch :: secondPass :: fields.filter(_.position == 2)
          .map(
            hasProfile(
              _,
              MATCH.use,
              mustHaveExtra(
                TARGET_FIELD.asInstanceOf[ComplexField].children,
                MATCH.use.asInstanceOf[Composite].reqs
              )
            )
          )
      }
    )
  }

  def assertionSliceNoMatchField(SLICING: FieldAssertionSlicing): Seq[MatchResult[Any]] = {
    def FIELD_DATATYPE = SEGMENT_REF_A.ref.fields.find(_.req.position == 2).get.datatype
    fieldAssertionSlicingTest(
      SLICING,
      (fields, TARGET_FIELD, codes) => {
        val containsANoSliceMatch = codes must containTheSameElementsAs(List(
          NoSliceMatch(SLICING, TARGET_FIELD, TARGET_FIELD.asInstanceOf[Field].datatype.id)
        ))

        containsANoSliceMatch :: fields.filter(_.position == 2)
          .map(
            hasProfile(
              _,
              FIELD_DATATYPE,
              mustHaveExtra(
                TARGET_FIELD.asInstanceOf[ComplexField].children,
                FIELD_DATATYPE.asInstanceOf[Composite].reqs
              )
            )
          )
      }
    )
  }

  def assertionSliceNoMatchFieldFailFail() =  assertionSliceNoMatchField(
    FieldAssertionSlicing(
      position = 2,
      List(
        AssertionSlice("If SGA-1 is valued 'X' (PASS)", PlainText("1[*]", "X", ignoreCase = true), CWE_FLV1),
        AssertionSlice("If SGA-2 is valued 'Y' (PASS)", PlainText("2[*]", "Y", ignoreCase = true), CWE_FLV2)
      )
    )
  )

  def assertionSliceNoMatchFieldEmpty() =  assertionSliceNoMatchField(
    FieldAssertionSlicing(
      position = 2,
      List()
    )
  )

  def assertionSliceFieldFailInconclusive() = {
    def FAIL : AssertionSlice[Datatype] = AssertionSlice("If SGA-1 is valued 'X' (PASS)", PlainText("1[*]", "X", ignoreCase = true), CWE_FLV1)
    def INCONCLUSIVE : AssertionSlice[Datatype] = AssertionSlice("If SGA-1.1 is valued 'A' (PASS)", PlainText("1[*].1[*]", "A", ignoreCase = true), CWE_FLV2)
    def FIELD_DATATYPE = SEGMENT_REF_A.ref.fields.find(_.req.position == 2).get.datatype

    def SLICING = FieldAssertionSlicing(
      position = 2,
      List(
        FAIL,
        INCONCLUSIVE,
      )
    )

    fieldAssertionSlicingTest(
      SLICING,
      (fields, TARGET_FIELD, codes) => {
        val INC_RESULT = eval(INCONCLUSIVE.assertion, TARGET_FIELD).asInstanceOf[EvalResult.Inconclusive]

        val containsNoSliceMatchAndInconclusive = codes must containTheSameElementsAs(
          List(
            NoSliceMatch(SLICING, TARGET_FIELD, TARGET_FIELD.asInstanceOf[Field].datatype.id),
            AssertionSliceInconclusive(INCONCLUSIVE, TARGET_FIELD, INC_RESULT.trace)
          )
        )
        containsNoSliceMatchAndInconclusive :: fields.filter(_.position == 2)
          .map(
            hasProfile(
              _,
              FIELD_DATATYPE,
              mustHaveExtra(
                TARGET_FIELD.asInstanceOf[ComplexField].children,
                FIELD_DATATYPE.asInstanceOf[Composite].reqs
              )
            )
          )
      }
    )
  }

  def assertionSliceFieldInconclusivePass() = {
    def PASS : AssertionSlice[Datatype] = AssertionSlice("If SGA-1 is valued 'A' (PASS)", PlainText("1[*]", "A", ignoreCase = true), CWE_FLV1)
    def INCONCLUSIVE : AssertionSlice[Datatype] = AssertionSlice("If SGA-1.1 is valued 'A' (INCONCLUSIVE)", PlainText("1[*].1[*]", "A", ignoreCase = true), CWE_FLV2)

    def SLICING = FieldAssertionSlicing(
      position = 2,
      List(
        INCONCLUSIVE,
        PASS,
      )
    )

    fieldAssertionSlicingTest(
      SLICING,
      (fields, TARGET_FIELD, codes) => {
        val INC_RESULT = eval(INCONCLUSIVE.assertion, TARGET_FIELD).asInstanceOf[EvalResult.Inconclusive]

        val containsNoSliceMatchAndInconclusive = codes must containTheSameElementsAs(
          List(
            AssertionSliceMatch(PASS, TARGET_FIELD),
            AssertionSliceInconclusive(INCONCLUSIVE, TARGET_FIELD, INC_RESULT.trace)
          )
        )
        containsNoSliceMatchAndInconclusive :: fields.filter(_.position == 2)
          .map(
            hasProfile(
              _,
              CWE_FLV1,
              mustHaveExtra(
                TARGET_FIELD.asInstanceOf[ComplexField].children,
                CWE_FLV1.reqs
              )
            )
          )
      }
    )
  }

  def assertionSliceFieldPassInconclusive() = {
    def PASS : AssertionSlice[Datatype] = AssertionSlice("If SGA-1 is valued 'A' (PASS)", PlainText("1[*]", "A", ignoreCase = true), CWE_FLV1)
    def INCONCLUSIVE : AssertionSlice[Datatype] = AssertionSlice("If SGA-1.1 is valued 'A' (INCONCLUSIVE)", PlainText("1[*].1[*]", "A", ignoreCase = true), CWE_FLV2)

    def SLICING = FieldAssertionSlicing(
      position = 2,
      List(
        PASS,
        INCONCLUSIVE,
      )
    )

    fieldAssertionSlicingTest(
      SLICING,
      (fields, TARGET_FIELD, codes) => {

        val containsNoSliceMatchAndInconclusive = codes must containTheSameElementsAs(
          List(
            AssertionSliceMatch(PASS, TARGET_FIELD),
          )
        )
        containsNoSliceMatchAndInconclusive :: fields.filter(_.position == 2)
          .map(
            hasProfile(
              _,
              CWE_FLV1,
              mustHaveExtra(
                TARGET_FIELD.asInstanceOf[ComplexField].children,
                CWE_FLV1.asInstanceOf[Composite].reqs
              )
            )
          )
      }
    )
  }

  def fieldOccurrenceSlicingTest(v: String, SLICING: FieldOccurrenceSlicing, expected: (List[Field], Map[Int, Element], List[SlicingCode]) => List[MatchResult[Any]]) = {
    def SEGMENT_A = mkSegment(v, SEGMENT_REF_A)
    val (fields, codes) = SEGMENT_A.children.map((child) => applyFieldSlices(SLICING, child)).foldLeft(List[Field](), List[SlicingCode]()){
      (acc, x) => (acc._1 ::: List(x._1), acc._2 ::: x._2)
    }
    val TARGET_FIELD = assertGetList(SEGMENT_A, s"${SLICING.position}[*]")
    expected(fields, TARGET_FIELD, codes)
  }


  def occurrenceSliceMatchFieldEmptyPop(): Seq[MatchResult[Any]] = {
    def O1 : OccurrenceSlice[Datatype] = OccurrenceSlice(1, CWE_FLV1)
    def O2 : OccurrenceSlice[Datatype] = OccurrenceSlice(2, CWE_FLV2)

    def SLICING = FieldOccurrenceSlicing(
      position = 2,
      List(
        O1,
        O2
      )
    )

    fieldOccurrenceSlicingTest(
      "SGA||~A^B^C^D",
      SLICING,
      (fields, TARGET_FIELD, codes) => {
        val containsASliceMatch = codes must containTheSameElementsAs(
          List(OccurrenceSliceMatch(O2, TARGET_FIELD(2)))
        )
        containsASliceMatch :: fields.filter(f => f.position == 2 && f.instance == 2)
          .map(
            hasProfile(
              _,
              O2.use,
              mustHaveExtra(
                TARGET_FIELD(2).asInstanceOf[ComplexField].children,
                O2.use.asInstanceOf[Composite].reqs
              )
            )
          )
      }
    )
  }

  def occurrenceSliceMatchFieldPopEmpty(): Seq[MatchResult[Any]] = {

    def O1 : OccurrenceSlice[Datatype] = OccurrenceSlice(1, CWE_FLV1)
    def O2 : OccurrenceSlice[Datatype] = OccurrenceSlice(2, CWE_FLV2)

    def SLICING = FieldOccurrenceSlicing(
      position = 2,
      List(
        O1,
        O2
      )
    )

    fieldOccurrenceSlicingTest(
      "SGA||A^B^C^D",
      SLICING,
      (fields, TARGET_FIELD, codes) => {
        val containsASliceMatch = codes must containTheSameElementsAs(
          List(OccurrenceSliceMatch(O1, TARGET_FIELD(1)))
        )
        containsASliceMatch :: fields.filter(f => f.position == 2 && f.instance == 1)
          .map(
            hasProfile(
              _,
              O1.use,
              mustHaveExtra(
                TARGET_FIELD(1).asInstanceOf[ComplexField].children,
                O1.use.asInstanceOf[Composite].reqs
              )
            )
          )
      }
    )
  }

  def occurrenceSliceMatchFieldPopPop(): Seq[MatchResult[Any]] = {

    def O1 : OccurrenceSlice[Datatype] = OccurrenceSlice(1, CWE_FLV1)
    def O2 : OccurrenceSlice[Datatype] = OccurrenceSlice(2, CWE_FLV2)

    def SLICING = FieldOccurrenceSlicing(
      position = 2,
      List(
        O1,
        O2
      )
    )

    fieldOccurrenceSlicingTest(
      "SGA||A^B^C^D~A^B^C^D",
      SLICING,
      (fields, TARGET_FIELD, codes) => {
        val containsASliceMatch = codes must containTheSameElementsAs(
          List(
            OccurrenceSliceMatch(O1, TARGET_FIELD(1)),
            OccurrenceSliceMatch(O2, TARGET_FIELD(2))
          )
        )
        containsASliceMatch ::
          fields.filter(f => f.position == 2 && f.instance == 1)
          .map(
            hasProfile(
              _,
              O1.use,
              mustHaveExtra(
                TARGET_FIELD(1).asInstanceOf[ComplexField].children,
                O1.use.asInstanceOf[Composite].reqs
              )
            )
          ) :::
          fields.filter(f => f.position == 2 && f.instance == 2)
            .map(
              hasProfile(
                _,
                O2.use,
                mustHaveExtra(
                  TARGET_FIELD(2).asInstanceOf[ComplexField].children,
                  O2.use.asInstanceOf[Composite].reqs
                )
              )
            )
      }
    )
  }

  def occurrenceSliceMatchFieldPopXPop(): Seq[MatchResult[Any]] = {
    def FIELD_DATATYPE = SEGMENT_REF_A.ref.fields.find(_.req.position == 2).get.datatype
    def O2 : OccurrenceSlice[Datatype] = OccurrenceSlice(2, CWE_FLV2)

    def SLICING = FieldOccurrenceSlicing(
      position = 2,
      List(
        O2
      )
    )

    fieldOccurrenceSlicingTest(
      "SGA||A^B^C^D~A^B^C^D",
      SLICING,
      (fields, TARGET_FIELD, codes) => {
        val containsASliceMatch = codes must containTheSameElementsAs(
          List(
            NoSliceMatch(SLICING, TARGET_FIELD(1), TARGET_FIELD(1).asInstanceOf[Field].datatype.id),
            OccurrenceSliceMatch(O2, TARGET_FIELD(2))
          )
        )
        containsASliceMatch ::
          fields.filter(f => f.position == 2 && f.instance == 1)
            .map(
              hasProfile(
                _,
                FIELD_DATATYPE,
                mustHaveExtra(
                  TARGET_FIELD(1).asInstanceOf[ComplexField].children,
                  FIELD_DATATYPE.asInstanceOf[Composite].reqs
                )
              )
            ) :::
          fields.filter(f => f.position == 2 && f.instance == 2)
            .map(
              hasProfile(
                _,
                O2.use,
                mustHaveExtra(
                  TARGET_FIELD(2).asInstanceOf[ComplexField].children,
                  O2.use.asInstanceOf[Composite].reqs
                )
              )
            )
      }
    )
  }

  def occurrenceSliceMatchFieldPopPopNoSlice(): Seq[MatchResult[Any]] = {
    def FIELD_DATATYPE = SEGMENT_REF_A.ref.fields.find(_.req.position == 2).get.datatype
    def O3 : OccurrenceSlice[Datatype] = OccurrenceSlice(3, CWE_FLV2)

    def SLICING = FieldOccurrenceSlicing(
      position = 2,
      List(
        O3
      )
    )

    fieldOccurrenceSlicingTest(
      "SGA||A^B^C^D~A^B^C^D",
      SLICING,
      (fields, TARGET_FIELD, codes) => {
        val containsASliceMatch = codes must containTheSameElementsAs(
          List(
            NoSliceMatch(SLICING, TARGET_FIELD(1), TARGET_FIELD(1).asInstanceOf[Field].datatype.id),
            NoSliceMatch(SLICING, TARGET_FIELD(2), TARGET_FIELD(2).asInstanceOf[Field].datatype.id),
          )
        )
        containsASliceMatch ::
          fields.filter(f => f.position == 2 && f.instance == 1)
            .map(
              hasProfile(
                _,
                FIELD_DATATYPE,
                mustHaveExtra(
                  TARGET_FIELD(1).asInstanceOf[ComplexField].children,
                  FIELD_DATATYPE.asInstanceOf[Composite].reqs
                )
              )
            ) :::
          fields.filter(f => f.position == 2 && f.instance == 2)
            .map(
              hasProfile(
                _,
                FIELD_DATATYPE,
                mustHaveExtra(
                  TARGET_FIELD(2).asInstanceOf[ComplexField].children,
                  FIELD_DATATYPE.asInstanceOf[Composite].reqs
                )
              )
            )
      }
    )
  }

  def occurrenceSliceMatchFieldNoSlice(): Seq[MatchResult[Any]] = {
    def FIELD_DATATYPE = SEGMENT_REF_A.ref.fields.find(_.req.position == 2).get.datatype
    def O3 : OccurrenceSlice[Datatype] = OccurrenceSlice(3, CWE_FLV2)

    def SLICING = FieldOccurrenceSlicing(
      position = 2,
      Nil
    )

    fieldOccurrenceSlicingTest(
      "SGA||A^B^C^D~A^B^C^D",
      SLICING,
      (fields, TARGET_FIELD, codes) => {
        val containsASliceMatch = codes must containTheSameElementsAs(
          List(
            NoSliceMatch(SLICING, TARGET_FIELD(1), TARGET_FIELD(1).asInstanceOf[Field].datatype.id),
            NoSliceMatch(SLICING, TARGET_FIELD(2), TARGET_FIELD(2).asInstanceOf[Field].datatype.id),
          )
        )
        containsASliceMatch ::
          fields.filter(f => f.position == 2 && f.instance == 1)
            .map(
              hasProfile(
                _,
                FIELD_DATATYPE,
                mustHaveExtra(
                  TARGET_FIELD(1).asInstanceOf[ComplexField].children,
                  FIELD_DATATYPE.asInstanceOf[Composite].reqs
                )
              )
            ) :::
          fields.filter(f => f.position == 2 && f.instance == 2)
            .map(
              hasProfile(
                _,
                FIELD_DATATYPE,
                mustHaveExtra(
                  TARGET_FIELD(2).asInstanceOf[ComplexField].children,
                  FIELD_DATATYPE.asInstanceOf[Composite].reqs
                )
              )
            )
      }
    )
  }


  // ====================== Segment ========================
  def segmentAssertionSlicingTest(SLICING: SegmentAssertionSlicing, expected: (List[Segment], Element, List[SlicingCode]) => List[MatchResult[Any]]) = {
    val LSGS = mkMessage("SGA||A^B^C^D", MESSAGE)
    val (segOrGroups, codes) = LSGS.filter(_.position == SLICING.position).map((child) => applySegmentSlices(SLICING, child)).foldLeft(List[SegOrGroup](), List[SlicingCode]()){
      (acc, x) => (acc._1 ::: List(x._1), acc._2 ::: x._2)
    }
    val TARGET_SEGMENT = LSGS.filter(_.position == SLICING.position).head
    expected(segOrGroups.map(_.asInstanceOf[Segment]), TARGET_SEGMENT, codes)
  }

  def segmentOccurrenceSlicingTest(v: List[String], SLICING: SegmentOccurrenceSlicing, expected: (List[Segment], Map[Int, Element], List[SlicingCode]) => List[MatchResult[Any]]) = {
    val LSGS = mkMessage(v, MESSAGE)
    val (segOrGroups, codes) = LSGS.filter(_.position == SLICING.position).map((child) => applySegmentSlices(SLICING, child)).foldLeft(List[SegOrGroup](), List[SlicingCode]()){
      (acc, x) => (acc._1 ::: List(x._1), acc._2 ::: x._2)
    }
    val TARGET_SEGMENT = LSGS.filter(_.position == SLICING.position).groupBy(_.instance).view.mapValues(_.head).toMap
    expected(segOrGroups.map(_.asInstanceOf[Segment]), TARGET_SEGMENT, codes)
  }

  def assertionSliceMatchSegmentFailPass(): Seq[MatchResult[Any]] = {
    def MATCH : AssertionSlice[SM] = AssertionSlice("If SGA-1 is valued 'A' (PASS)", PlainText("2[*].1[*]", "A", ignoreCase = true), SEGMENT_A_FLV1)
    def SLICING = SegmentAssertionSlicing(
      position = 1,
      List(
        AssertionSlice("If SGA-1 is valued 'X' (FAIL)", PlainText("2[*].1[*]", "X", ignoreCase = true), SEGMENT_A_FLV2),
        MATCH,
      )
    )

    segmentAssertionSlicingTest(
      SLICING,
      (segments, TARGET_SEGMENT, codes) => {
        val containsASliceMatch = codes must containTheSameElementsAs(
          List(AssertionSliceMatch(MATCH, TARGET_SEGMENT))
        )
        containsASliceMatch :: segments.filter(_.position == 1)
          .map(
            hasProfile(
              _,
              MATCH.use,
              mustHaveExtra(
                TARGET_SEGMENT.asInstanceOf[Segment].children,
                MATCH.use.reqs
              )
            )
          )
      }
    )
  }

  def assertionSliceMatchSegmentPassPass(): Seq[MatchResult[Any]] = {
    def MATCH : AssertionSlice[SM] = AssertionSlice("If SGA-1 is valued 'A' (PASS)", PlainText("2[*].1[*]", "A", ignoreCase = true), SEGMENT_A_FLV1)
    def PASS : AssertionSlice[SM] =  AssertionSlice("If SGA-2 is valued 'B' (PASS)", PlainText("2[*].2[*]", "B", ignoreCase = true), SEGMENT_A_FLV2)
    def SLICING = SegmentAssertionSlicing(
      position = 1,
      List(
        MATCH,
        PASS
      )
    )
    segmentAssertionSlicingTest(
      SLICING,
      (segments, TARGET_SEGMENT, codes) => {
        val containsASliceMatch = codes must containTheSameElementsAs(
          List(AssertionSliceMatch(MATCH, TARGET_SEGMENT))
        )

        val secondPass = eval(PASS.assertion, TARGET_SEGMENT) === Pass
        containsASliceMatch :: secondPass :: segments.filter(_.position == 1)
          .map(
            hasProfile(
              _,
              MATCH.use,
              mustHaveExtra(
                TARGET_SEGMENT.asInstanceOf[Segment].children,
                MATCH.use.reqs
              )
            )
          )
      }
    )
  }

  def assertionSliceNoMatchSegment(SLICING: SegmentAssertionSlicing): Seq[MatchResult[Any]] = {
    segmentAssertionSlicingTest(
      SLICING,
      (segments, TARGET_SEGMENT, codes) => {
        val containsANoSliceMatch = codes must containTheSameElementsAs(List(
          NoSliceMatch(SLICING, TARGET_SEGMENT, TARGET_SEGMENT.asInstanceOf[Segment].model.ref.id)
        ))

        containsANoSliceMatch :: segments.filter(_.position == 1)
          .map(
            hasProfile(
              _,
              SEGMENT_REF_A.ref,
              mustHaveExtra(
                TARGET_SEGMENT.asInstanceOf[Segment].children,
                SEGMENT_REF_A.ref.reqs
              )
            )
          )
      }
    )
  }

  def assertionSliceNoMatchSegmentFailFail() =  assertionSliceNoMatchSegment(
    SegmentAssertionSlicing(
      position = 1,
      List(
        AssertionSlice("If SGA-1 is valued 'X' (PASS)", PlainText("2[*].1[*]", "X", ignoreCase = true), SEGMENT_A_FLV1),
        AssertionSlice("If SGA-2 is valued 'Y' (PASS)", PlainText("2[*].2[*]", "Y", ignoreCase = true), SEGMENT_A_FLV2)
      )
    )
  )

  def assertionSliceNoMatchSegmentEmpty() =  assertionSliceNoMatchSegment(
    SegmentAssertionSlicing(
      position = 1,
      List()
    )
  )

  def assertionSliceSegmentFailInconclusive() = {
    def FAIL : AssertionSlice[SM] = AssertionSlice("If SGA-1 is valued 'X' (PASS)", PlainText("2[*].1[*]", "X", ignoreCase = true), SEGMENT_A_FLV1)
    def INCONCLUSIVE : AssertionSlice[SM] = AssertionSlice("If SGA-1 is valued 'X' (PASS)", PlainText("2[*].1[*].1[*]", "X", ignoreCase = true), SEGMENT_A_FLV2)

    def SLICING = SegmentAssertionSlicing(
      position = 1,
      List(
        FAIL,
        INCONCLUSIVE,
      )
    )

    segmentAssertionSlicingTest(
      SLICING,
      (segments, TARGET_SEGMENT, codes) => {
        val INC_RESULT = eval(INCONCLUSIVE.assertion, TARGET_SEGMENT).asInstanceOf[EvalResult.Inconclusive]

        val containsNoSliceMatchAndInconclusive = codes must containTheSameElementsAs(
          List(
            NoSliceMatch(SLICING, TARGET_SEGMENT, TARGET_SEGMENT.asInstanceOf[Segment].model.ref.id),
            AssertionSliceInconclusive(INCONCLUSIVE, TARGET_SEGMENT, INC_RESULT.trace)
          )
        )
        containsNoSliceMatchAndInconclusive :: segments.filter(_.position == 1)
          .map(
            hasProfile(
              _,
              SEGMENT_REF_A.ref,
              mustHaveExtra(
                TARGET_SEGMENT.asInstanceOf[Segment].children,
                SEGMENT_REF_A.ref.reqs
              )
            )
          )
      }
    )
  }

  def assertionSliceSegmentInconclusivePass()= {
    def PASS : AssertionSlice[SM] = AssertionSlice("If SGA-1 is valued 'A' (PASS)", PlainText("2[*].1[*]", "A", ignoreCase = true), SEGMENT_A_FLV1)
    def INCONCLUSIVE : AssertionSlice[SM] = AssertionSlice("If SGA-1 is valued 'X' (PASS)", PlainText("2[*].1[*].1[*]", "X", ignoreCase = true), SEGMENT_A_FLV2)

    def SLICING = SegmentAssertionSlicing(
      position = 1,
      List(
        INCONCLUSIVE,
        PASS,
      )
    )

    segmentAssertionSlicingTest(
      SLICING,
      (segments, TARGET_SEGMENT, codes) => {
        val INC_RESULT = eval(INCONCLUSIVE.assertion, TARGET_SEGMENT).asInstanceOf[EvalResult.Inconclusive]

        val containsNoSliceMatchAndInconclusive = codes must containTheSameElementsAs(
          List(
            AssertionSliceMatch(PASS, TARGET_SEGMENT),
            AssertionSliceInconclusive(INCONCLUSIVE, TARGET_SEGMENT, INC_RESULT.trace)
          )
        )
        containsNoSliceMatchAndInconclusive :: segments.filter(_.position == 1)
          .map(
            hasProfile(
              _,
              SEGMENT_A_FLV1,
              mustHaveExtra(
                TARGET_SEGMENT.asInstanceOf[Segment].children,
                SEGMENT_A_FLV1.reqs
              )
            )
          )
      }
    )
  }


  def assertionSliceSegmentPassInconclusive() = {
    def PASS : AssertionSlice[SM] = AssertionSlice("If SGA-1 is valued 'A' (PASS)", PlainText("2[*].1[*]", "A", ignoreCase = true), SEGMENT_A_FLV1)
    def INCONCLUSIVE : AssertionSlice[SM] = AssertionSlice("If SGA-1 is valued 'X' (PASS)", PlainText("2[*].1[*].1[*]", "X", ignoreCase = true), SEGMENT_A_FLV2)

    def SLICING = SegmentAssertionSlicing(
      position = 1,
      List(
        PASS,
        INCONCLUSIVE
      )
    )

    segmentAssertionSlicingTest(
      SLICING,
      (segments, TARGET_SEGMENT, codes) => {
        val containsNoSliceMatchAndInconclusive = codes must containTheSameElementsAs(
          List(
            AssertionSliceMatch(PASS, TARGET_SEGMENT),
          )
        )
        containsNoSliceMatchAndInconclusive :: segments.filter(_.position == 1)
          .map(
            hasProfile(
              _,
              SEGMENT_A_FLV1,
              mustHaveExtra(
                TARGET_SEGMENT.asInstanceOf[Segment].children,
                SEGMENT_A_FLV1.reqs
              )
            )
          )
      }
    )
  }

  def occurrenceSliceMatchSegmentPopEmpty(): Seq[MatchResult[Any]] = {
    def O1 : OccurrenceSlice[SM] = OccurrenceSlice(1, SEGMENT_A_FLV1)
    def O2 : OccurrenceSlice[SM] = OccurrenceSlice(2, SEGMENT_A_FLV2)

    def SLICING = SegmentOccurrenceSlicing(
      position = 1,
      List(
        O1,
        O2
      )
    )

    segmentOccurrenceSlicingTest(
      "SGA||~A^B^C^D" :: Nil,
      SLICING,
      (segments, TARGET_SEGMENT, codes) => {
        val containsASliceMatch = codes must containTheSameElementsAs(
          List(OccurrenceSliceMatch(O1, TARGET_SEGMENT(1)))
        )
        containsASliceMatch :: segments.filter(f => f.position == 1 && f.instance == 1)
          .map(
            hasProfile(
              _,
              O1.use,
              mustHaveExtra(
                TARGET_SEGMENT(1).asInstanceOf[Segment].children,
                O1.use.reqs
              )
            )
          )
      }
    )
  }

  def occurrenceSliceMatchSegmentPopPop(): Seq[MatchResult[Any]] = {
    def O1 : OccurrenceSlice[SM] = OccurrenceSlice(1, SEGMENT_A_FLV1)
    def O2 : OccurrenceSlice[SM] = OccurrenceSlice(2, SEGMENT_A_FLV2)

    def SLICING = SegmentOccurrenceSlicing(
      position = 1,
      List(
        O1,
        O2
      )
    )

    segmentOccurrenceSlicingTest(
      "SGA||~A^B^C^D" :: "SGA||~A^B^C^D" :: Nil,
      SLICING,
      (segments, TARGET_SEGMENT, codes) => {
        val containsASliceMatch = codes must containTheSameElementsAs(
          List(
            OccurrenceSliceMatch(O1, TARGET_SEGMENT(1)),
            OccurrenceSliceMatch(O2, TARGET_SEGMENT(2))
          )
        )
        containsASliceMatch ::
          segments.filter(f => f.position == 1 && f.instance == 1)
            .map(
              hasProfile(
                _,
                O1.use,
                mustHaveExtra(
                  TARGET_SEGMENT(1).asInstanceOf[Segment].children,
                  O1.use.reqs
                )
              )
            ) :::
          segments.filter(f => f.position == 1 && f.instance == 2)
            .map(
              hasProfile(
                _,
                O2.use,
                mustHaveExtra(
                  TARGET_SEGMENT(2).asInstanceOf[Segment].children,
                  O2.use.reqs
                )
              )
            )
      }
    )
  }


  def occurrenceSliceMatchSegmentPopXPop(): Seq[MatchResult[Any]] = {
    def O2 : OccurrenceSlice[SM] = OccurrenceSlice(2, SEGMENT_A_FLV2)

    def SLICING = SegmentOccurrenceSlicing(
      position = 1,
      List(
        O2
      )
    )

    segmentOccurrenceSlicingTest(
      "SGA||~A^B^C^D" :: "SGA||~A^B^C^D" :: Nil,
      SLICING,
      (segments, TARGET_SEGMENT, codes) => {
        val containsASliceMatch = codes must containTheSameElementsAs(
          List(
            NoSliceMatch(SLICING, TARGET_SEGMENT(1), TARGET_SEGMENT(1).asInstanceOf[Segment].model.ref.id),
            OccurrenceSliceMatch(O2, TARGET_SEGMENT(2))
          )
        )
        containsASliceMatch ::
          segments.filter(f => f.position == 1 && f.instance == 1)
            .map(
              hasProfile(
                _,
                SEGMENT_A_BASE,
                mustHaveExtra(
                  TARGET_SEGMENT(1).asInstanceOf[Segment].children,
                  SEGMENT_A_BASE.reqs
                )
              )
            ) :::
          segments.filter(f => f.position == 1 && f.instance == 2)
            .map(
              hasProfile(
                _,
                O2.use,
                mustHaveExtra(
                  TARGET_SEGMENT(2).asInstanceOf[Segment].children,
                  O2.use.reqs
                )
              )
            )
      }
    )
  }

  def occurrenceSliceMatchSegmentPopPopNoSlice(): Seq[MatchResult[Any]] = {
    def O3 : OccurrenceSlice[SM] = OccurrenceSlice(3, SEGMENT_A_FLV2)

    def SLICING = SegmentOccurrenceSlicing(
      position = 1,
      List(
        O3
      )
    )

    segmentOccurrenceSlicingTest(
      "SGA||~A^B^C^D" :: "SGA||~A^B^C^D" :: Nil,
      SLICING,
      (segments, TARGET_SEGMENT, codes) => {
        val containsASliceMatch = codes must containTheSameElementsAs(
          List(
            NoSliceMatch(SLICING, TARGET_SEGMENT(1), TARGET_SEGMENT(1).asInstanceOf[Segment].model.ref.id),
            NoSliceMatch(SLICING, TARGET_SEGMENT(2), TARGET_SEGMENT(2).asInstanceOf[Segment].model.ref.id),
          )
        )
        containsASliceMatch ::
          segments.filter(f => f.position == 1 && f.instance == 1)
            .map(
              hasProfile(
                _,
                SEGMENT_A_BASE,
                mustHaveExtra(
                  TARGET_SEGMENT(1).asInstanceOf[Segment].children,
                  SEGMENT_A_BASE.reqs
                )
              )
            ) :::
          segments.filter(f => f.position == 1 && f.instance == 2)
            .map(
              hasProfile(
                _,
                SEGMENT_A_BASE,
                mustHaveExtra(
                  TARGET_SEGMENT(2).asInstanceOf[Segment].children,
                  SEGMENT_A_BASE.reqs
                )
              )
            )
      }
    )
  }

  def occurrenceSliceMatchSegmentNoSlice(): Seq[MatchResult[Any]] = {
    def SLICING = SegmentOccurrenceSlicing(
      position = 1,
      Nil
    )

    segmentOccurrenceSlicingTest(
      "SGA||~A^B^C^D" :: "SGA||~A^B^C^D" :: Nil,
      SLICING,
      (segments, TARGET_SEGMENT, codes) => {
        val containsASliceMatch = codes must containTheSameElementsAs(
          List(
            NoSliceMatch(SLICING, TARGET_SEGMENT(1), TARGET_SEGMENT(1).asInstanceOf[Segment].model.ref.id),
            NoSliceMatch(SLICING, TARGET_SEGMENT(2), TARGET_SEGMENT(2).asInstanceOf[Segment].model.ref.id),
          )
        )
        containsASliceMatch ::
          segments.filter(f => f.position == 1 && f.instance == 1)
            .map(
              hasProfile(
                _,
                SEGMENT_A_BASE,
                mustHaveExtra(
                  TARGET_SEGMENT(1).asInstanceOf[Segment].children,
                  SEGMENT_A_BASE.reqs
                )
              )
            ) :::
          segments.filter(f => f.position == 1 && f.instance == 2)
            .map(
              hasProfile(
                _,
                SEGMENT_A_BASE,
                mustHaveExtra(
                  TARGET_SEGMENT(2).asInstanceOf[Segment].children,
                  SEGMENT_A_BASE.reqs
                )
              )
            )
      }
    )
  }

  def targetIsGroup() = {
    def SLICING = SegmentOccurrenceSlicing(
      position = 2,
      List(
        OccurrenceSlice(1, SEGMENT_B_FLV1)
      )
    )

    val LSGS = mkMessage("SGA||~A^B^C^D" :: "SGB||~A^B^C^D" :: Nil, MESSAGE)
    val (_, codes) = LSGS.filter(_.position == SLICING.position).map((child) => applySegmentSlices(SLICING, child)).foldLeft(List[SegOrGroup](), List[SlicingCode]()){
      (acc, x) => (acc._1 ::: List(x._1), acc._2 ::: x._2)
    }
    val TARGET = LSGS.filter(_.position == SLICING.position).groupBy(_.instance).view.mapValues(_.head).toMap

    codes must containTheSameElementsAs(
      List(
        SegmentSliceTargetIsGroup(SLICING, TARGET(1).asInstanceOf[Group]),
      )
    )
  }

  def matchError() = {
    val SLICE = OccurrenceSlice(1, SEGMENT_B_FLV1)
    val SLICING = SegmentOccurrenceSlicing(
      position = 1,
      List(
        SLICE
      )
    )

    val LSGS = mkMessage("SGA||~A^B^C^D" :: Nil, MESSAGE)
    val (_, codes) = LSGS.filter(_.position == SLICING.position).map((child) => applySegmentSlices(SLICING, child)).foldLeft(List[SegOrGroup](), List[SlicingCode]()){
      (acc, x) => (acc._1 ::: List(x._1), acc._2 ::: x._2)
    }
    val TARGET = LSGS.filter(_.position == SLICING.position).groupBy(_.instance).view.mapValues(_.head).toMap

    codes must containTheSameElementsAs(
      List(
        OccurrenceSliceMatch(SLICE, TARGET(1).asInstanceOf[Segment]),
        MatchError(SLICE, TARGET(1).asInstanceOf[Segment], "requirement failed: Invalid segment name. Expected: 'SGB', Found: 'SGA||~A^B^C^D'"),
      )
    )
  }

  def endToEndNested() = {
    val ASSERTION: AssertionSlice[SM] = AssertionSlice("If SGB-1 is valued 'A' (PASS)", PlainText("2[*].1[*]", "A", ignoreCase = true), SEGMENT_B_FLV2)
    val OCCURRENCE: OccurrenceSlice[Datatype] = OccurrenceSlice(1, CWE_FLV1)

    implicit val profileSlicingContext: ProfileSlicingContext = new DefaultProfileSlicingContext(
      Map(MESSAGE.id -> Map("GROUP_ID" ->
        Map(1 ->
          SegmentAssertionSlicing(
            position = 1,
            List(
              ASSERTION
            )
          )
        )
      )),
      Map("SEGMENT_B_FLV2" ->
        Map(2 ->
          FieldOccurrenceSlicing(
            position = 2,
            List(
              OCCURRENCE
            )
          )
        )
      ),
    )

    val LSGS = mkMessage("SGA||A^B^C^D" :: "SGB||A^B^C^D" :: Nil, MESSAGE)
    val GRP = Group(MESSAGE.asGroup, 1, LSGS)
    val (pGrp, codes) = applySegmentSlices(MESSAGE.id, GRP)
    val TARGET_SEGMENT = assertGet(GRP, "2[*].1[*]")
    val TARGET_FIELD = assertGet(TARGET_SEGMENT, "2[*]")

    val containsMatches = codes must contain(beLike[SlicingCode] {
      case AssertionSliceMatch(assertion, sgm) =>
        (assertion mustEqual(ASSERTION))  and (sgm mustEqual(TARGET_SEGMENT))
      case OccurrenceSliceMatch(occurrence, _) =>
        (occurrence mustEqual(OCCURRENCE))
      case _ => true mustEqual(false)
    }).forall

    val P_SEGMENT = assertGet(pGrp, "2[*].1[*]").asInstanceOf[Segment]
    val P_FIELD = assertGet(P_SEGMENT, "2[*]").asInstanceOf[ComplexField]

    val segmentProfile = hasProfile(
      P_SEGMENT,
      SEGMENT_B_FLV2,
      mustHaveExtra(
        P_SEGMENT.children,
        SEGMENT_B_FLV2.reqs
      )
    )

    val fieldProfile = hasProfile(
      P_FIELD,
      CWE_FLV1,
      mustHaveExtra(
        TARGET_FIELD.asInstanceOf[ComplexField].children,
        CWE_FLV1.reqs
      )
    )

    containsMatches :: List(segmentProfile, fieldProfile)
  }

  def noSlicingDefined() = {

    val LSGS = mkMessage("SGA||A^B^C^D" :: "SGB||A^B^C^D" :: Nil, MESSAGE)
    val GRP = Group(MESSAGE.asGroup, 1, LSGS)
    implicit val profileSlicingContext: ProfileSlicingContext = EmptyProfileSlicingContext
    val (pGrp, _) = applySegmentSlices(MESSAGE.id, GRP)

    GRP mustEqual(pGrp)
  }

  def noSliceMatched() = {
    val ASSERTION: AssertionSlice[SM] = AssertionSlice("If SGB-1 is valued 'X' (FAIL)", PlainText("2[*].1[*]", "X", ignoreCase = true), SEGMENT_B_FLV2)
    val OCCURRENCE: OccurrenceSlice[Datatype] = OccurrenceSlice(2, CWE_FLV1)

    val SEG_SLICING = SegmentAssertionSlicing(
      position = 1,
      List(
        ASSERTION
      )
    )
    val FIELD_SLICING = FieldOccurrenceSlicing(
      position = 2,
      List(
        OCCURRENCE
      )
    )

    implicit val profileSlicingContext: ProfileSlicingContext = new DefaultProfileSlicingContext(
      Map("MSG_ID" -> Map("GROUP_ID" ->
        Map(1 -> SEG_SLICING)
      )),
      Map("SEGMENT_B_BASE" ->
        Map(2 -> FIELD_SLICING)
      ),
    )
    val LSGS = mkMessage("SGA||A^B^C^D" :: "SGB||A^B^C^D" :: Nil, MESSAGE)
    val GRP = Group(MESSAGE.asGroup, 1, LSGS)
    val (pGrp, codes) = applySegmentSlices(MESSAGE.id, GRP)
    val TARGET_SEGMENT = assertGet(GRP, "2[*].1[*]")
    val TARGET_FIELD = assertGet(TARGET_SEGMENT, "2[*]")

    GRP mustEqual(pGrp) and (codes must containTheSameElementsAs(
      List(
        NoSliceMatch(SEG_SLICING, TARGET_SEGMENT, TARGET_SEGMENT.asInstanceOf[Segment].model.ref.id),
        NoSliceMatch(FIELD_SLICING, TARGET_FIELD, TARGET_FIELD.asInstanceOf[Field].datatype.id),
      )
    ))
  }


  def mustHaveExtra(elements: List[Element], reqs: List[Req]): Boolean = {
    elements.map(_.position).max > reqs.map(_.position).max
  }

  def hasProfile(instance: Segment, model: SM, mustHaveExtra: Boolean) = {
    val extra = instance.hasExtra mustEqual mustHaveExtra
    val eqModel =  instance.model.ref mustEqual model
    extra and eqModel
  }

  def hasProfile(field: Field, model: Datatype, mustHaveExtra: Boolean) = {
    val x = field match {
      case cf: ComplexField => cf.hasExtra mustEqual(mustHaveExtra)
      case pf: SimpleField => mustHaveExtra mustEqual(false)
      case ncf: NULLComplexField => true mustEqual(true)
      case _ => true mustEqual(false)
    }

    x and field.datatype.mustEqual(model)
  }

  def mkMessage(value: String, profile: Message): LSG = {
    implicit val ambiguous: Boolean = false
    implicit val counter: Counter = Counter(scala.collection.mutable.Map())
    processChildren(profile.structure, value.split("\n").zipWithIndex.map(x => Line(x._2, x._1)).toList, false)._1
  }

  def mkMessage(value: List[String], profile: Message): LSG = {
    implicit val ambiguous: Boolean = false
    implicit val counter: Counter = Counter(scala.collection.mutable.Map())
    processChildren(profile.structure, value.zipWithIndex.map(x => Line(x._2, x._1)), false)._1
  }

  def assertGet(e: Element, path: String): Element = {
    val element = query(e, path).toOption.map(_.head).orNull
    assert(element != null)
    element
  }

  def assertGetList(e: Element, path: String): Map[Int, Element] = {
    val element = query(e, path).get.groupBy(_.instance).view.mapValues(_.head).toMap
    assert(element != null && element.nonEmpty)
    element
  }

}
