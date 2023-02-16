package hl7.v2.validation.slicing
import org.specs2.Specification

object DefaultSlicingEvalSpec extends Specification with SlicingEvalSpec {
  def is =
    s2"""
      Assertion Slice Matching
        Evaluator should use the first slice that matches by assertion
          - Slice #1 assertion => FAIL, Slice #2 assertion => PASS use Slice #2
            - For field ${assertionSliceMatchFieldFailPass()}
            - For Segment ${assertionSliceMatchSegmentFailPass()}
          - Slice #1 assertion => PASS, Slice #2 assertion => PASS use Slice #1
            - For field ${assertionSliceMatchFieldPassPass()}
            - For Segment ${assertionSliceMatchSegmentPassPass()}
        Evaluation should return a No Match if no slice matches
          - Slice #1 assertion => FAIL, Slice #2 assertion => FAIL
            - For Field ${assertionSliceNoMatchFieldFailFail()}
            - For Segment ${assertionSliceNoMatchSegmentFailFail()}
          - Empty Slicing (No Slice Defined)
            - For Field ${assertionSliceNoMatchFieldEmpty()}
            - For Segment ${assertionSliceNoMatchSegmentEmpty()}
        Evaluator should return a spec error if an slice assertion is inconclusive before a match is found
          - Slice #1 assertion => FAIL, Slice #2 assertion => INCONCLUSIVE : Inconclusive Slicing & No Match
            - For Field ${assertionSliceFieldFailInconclusive()}
            - For Segment ${assertionSliceSegmentFailInconclusive()}
          - Slice #1 assertion => INCONCLUSIVE, Slice #2 assertion => PASS : Inconclusive Slicing & Match
            - For Field ${assertionSliceFieldInconclusivePass()}
            - For Segment ${assertionSliceSegmentInconclusivePass()}
          - Slice #1 assertion => PASS, Slice #2 assertion => INCONCLUSIVE : Match
            - For Field ${assertionSliceFieldPassInconclusive()}
            - For Segment ${assertionSliceSegmentPassInconclusive()}

      Occurrence Slice Matching
        Evaluator should use the slice that matches by occurrence
          - Slice #1 occurrence (1) => NOT_POPULATED, Slice #2 occurrence (2) => POPULATED : MATCH Slice #2
            - For Field ${occurrenceSliceMatchFieldEmptyPop()}
            - Not Applicable for Segment (Cannot populate 2 and not 1)
          - Slice #1 occurrence (1) => POPULATED, Slice #2 occurrence (2) => NOT_POPULATED : MATCH Slice #1
            - For Field ${occurrenceSliceMatchFieldPopEmpty()}
            - For Segment ${occurrenceSliceMatchSegmentPopEmpty()}
          - Slice #1 occurrence (1) => POPULATED, Slice #2 occurrence (2) => POPULATED : MATCH Slice #1, MATCH Slice #2
            - For Field ${occurrenceSliceMatchFieldPopPop()}
            - For Segment ${occurrenceSliceMatchSegmentPopPop()}
          - occurrence (1) => POPULATED and No Slice, Slice #2 occurrence (2) => POPULATED : NO_MATCH Slice #1, MATCH Slice #2
            - For Field ${occurrenceSliceMatchFieldPopXPop()}
            - For Segment ${occurrenceSliceMatchSegmentPopXPop()}
        Evaluation should return a No Match if no slice matches
          - occurrence (1) => POPULATED and No Slice, occurrence (2) => POPULATED and No Slice : NO_MATCH Slice #1, NO_MATCH Slice #2
            - For Field ${occurrenceSliceMatchFieldPopPopNoSlice()}
            - For Segment ${occurrenceSliceMatchSegmentPopPopNoSlice()}
          - Empty Slicing (No Slice Defined) and occurrence (1) => POPULATED, occurrence (2) => POPULATED : => NO_MATCH Slice #1, NO_MATCH Slice #2
            - For Field ${occurrenceSliceMatchFieldNoSlice()}
            - For Segment ${occurrenceSliceMatchSegmentNoSlice()}

      Target Of Slicing is Group (Segment Slicing)
        Evaluator should return a Spec Error if target at position is not a SegmentRef ${targetIsGroup()}

      Matched Slice Error
        Evaluator should return a Spec Error if parsing the message content in slice fails
          Slice Segment Name does not match Segment Name in message ${matchError()}

      Nested
        * Assertion Segment Slicing
          * Field Occurrence Slicing
            => MATCH Segment Assertion Slicing + MATCH Field Occurrence Slicing ${endToEndNested()}

        * If no slice is matched, message input should equal message output
          - No Slicing Defined ${noSlicingDefined()}
          - No Slice Matched ${noSliceMatched()}
      """
}
