package hl7.v2.validation.slicing

import expression.EvalResult
import expression.EvalResult.Trace
import gov.nist.validation.report.Entry
import hl7.v2.instance.{Counter, DataElement, Element, Field, Group, Message, SegOrGroup, Separators, TimeZone, Segment => SegmentInstance}
import hl7.v2.profile.{Datatype, Segment}
import hl7.v2.validation.vs.Validator
import hl7.v2.validation.report.ConfigurableDetections

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

sealed trait SlicingCode

case class AssertionSliceMatch[T](assertionSlice: AssertionSlice[T], element: Element) extends SlicingCode

case class OccurrenceSliceMatch[T](assertionSlice: OccurrenceSlice[T], element: Element) extends SlicingCode

case class AssertionSliceInconclusive[T](
  assertionSlice: AssertionSlice[T],
  context: Element,
  trace: Trace
) extends SlicingCode

case class MatchError[T](slice: Slice[T], context: Element, message: String) extends SlicingCode

case class NoSliceMatch[T](slicing: Slicing[T], element: Element, flavorName: String) extends SlicingCode

case class SegmentSliceTargetIsGroup[T](slicing: Slicing[T], element: Group) extends SlicingCode

trait DefaultSliceEvaluator extends SliceEvaluator with expression.Evaluator {

  def applySlices(m: Message)(implicit Detections : ConfigurableDetections, VSValidator : hl7.v2.validation.vs.Validator): Future[(Message, Seq[Entry])]= Future {
    implicit val separators: Separators = m.separators
    implicit val dtz: Option[TimeZone] = m.defaultTimeZone
    implicit val context = profileSlicingContext
    val apply = applySegmentSlices(m.model.id, m.asGroup)
    (m.copy(children = apply._1.children), apply._2.map(slicingCodeToDetection))
  }

  def applySegmentSlices(messageId: String, group: Group)
    (
      implicit s: Separators,
      t: Option[TimeZone],
      VSValidator: Validator,
      Detections: ConfigurableDetections,
      profileSlicingContext: ProfileSlicingContext
    ): (Group, List[SlicingCode]) = {

    val segOrGroupsAndIssues = group.children.foldLeft((List[SegOrGroup](), List[SlicingCode]())) {
      (acc, child) => profileSlicingContext.segmentSlicingForMessageGroupAtPosition(messageId, group, child.position) match {
        case None =>  (acc._1 ::: List(child), acc._2)
        case Some(slicing) => val (parsed, issues) =  applySegmentSlices(slicing, child)
          (acc._1 ::: List(parsed), acc._2 ::: issues)
      }
    }

    val processed = segOrGroupsAndIssues._1.foldLeft((List[SegOrGroup](), List[SlicingCode]())) {
      (acc, child) => child match {
        case grp: Group => val (parsed, issues) = applySegmentSlices(messageId, grp)
          (acc._1 ::: List(parsed), acc._2 ::: issues)
        case seg: SegmentInstance => val (parsed, issues) = applyFieldSlices(seg)
          (acc._1 ::: List(parsed), acc._2 ::: issues)
      }
    }

    (group.copy(children = processed._1), processed._2 ::: segOrGroupsAndIssues._2)
  }

  def applyFieldSlices(segmentInstance: SegmentInstance)
    (
      implicit s: Separators,
      t: Option[TimeZone],
      VSValidator: Validator,
      Detections: ConfigurableDetections,
      profileSlicingContext: ProfileSlicingContext
    ): (SegmentInstance, List[SlicingCode]) = {

      val fieldsAndIssues = segmentInstance.children.foldLeft((List[Field](), List[SlicingCode]())) {
        (acc, child) =>
          profileSlicingContext.fieldSlicingForSegmentAtPosition(segmentInstance, child.position) match {
            case None => (acc._1 ::: List(child), acc._2)
            case Some(slicing) => val (parsed, issues) = applyFieldSlices(slicing, child)
              (acc._1 ::: List(parsed), acc._2 ::: issues)
          }
      }

    (segmentInstance.copy(children = fieldsAndIssues._1), fieldsAndIssues._2)
  }

  def applyFieldSlices(slicing: Slicing[Datatype], child: Field)
    (
      implicit s: Separators,
      t: Option[TimeZone],
      VSValidator: Validator,
      Detections: ConfigurableDetections
    ): (Field, List[SlicingCode]) = {

    val (matched, detections) = matchSlice(child, slicing)

    matched match {
      case Some(slice) =>
        val (appliedFieldOption, codes) = applyDatatypeToField(child, slice)
        appliedFieldOption match {
          case Some(appliedField) => (appliedField, detections ::: codes ::: Nil)
          case None => (child, detections ::: codes ::: Nil) /// TODO Is this correct? Shouldn't ever fall in this case
        }
      case None =>
        (child, NoSliceMatch(slicing, child, child.datatype.id) :: detections ::: Nil)
    }

  }

  def applySegmentSlices(slicing: Slicing[Segment], child: SegOrGroup)
    (
      implicit s: Separators,
      t: Option[TimeZone],
      VSValidator: Validator,
      Detections: ConfigurableDetections
    ): (SegOrGroup, List[SlicingCode]) = {

    implicit val ctr: Counter = Counter(scala.collection.mutable.Map[String, Int]())

    child match {
      case group: Group => (group, SegmentSliceTargetIsGroup(slicing, group) :: Nil)
      case segmentInstance: SegmentInstance =>
        val (matched, detections) = matchSlice(segmentInstance, slicing)
        matched match {
          case Some(slice) =>
            val (appliedSegmentRef, codes) = applySegmentToSegmentRef(segmentInstance, slice)
            (appliedSegmentRef, detections ::: codes ::: Nil)
          case None =>
            (segmentInstance, NoSliceMatch(slicing, segmentInstance, segmentInstance.model.ref.id) :: detections ::: Nil)
        }
    }

  }

  def matchSlice[T](element: Element, slicing: Slicing[T])
    (
      implicit s: Separators,
      t: Option[TimeZone],
      VSValidator: Validator,
      Detections: ConfigurableDetections
    ): (Option[Slice[T]], List[SlicingCode]) = {
    slicing.slices.foldLeft[(Option[Slice[T]], List[SlicingCode])]((None, Nil)) {
      (matchAcc, slice) =>
        // If already found just return already found result
        if (matchAcc._1.isDefined) {
          matchAcc
        }
        // If not found yet
        else {
          // Evaluate current slice
          val sliceEvalResult = evaluateSlice(element, slice)
          (if (sliceEvalResult._1) Some(slice) else None, sliceEvalResult._2 ::: matchAcc._2)
        }
    }
  }


  def evaluateSlice[T](element: Element, slice: Slice[T])
    (
      implicit s: Separators,
      t: Option[TimeZone],
      VSValidator: Validator,
      Detections: ConfigurableDetections
    ): (Boolean, List[SlicingCode]) = {
    slice match {
      case os: OccurrenceSlice[T] => evaluateOccurrenceSlice(os, element)
      case as: AssertionSlice[T] => evaluateAssertionSlice(as, element)
    }
  }

  def evaluateOccurrenceSlice[T](slice: OccurrenceSlice[T], element: Element): (Boolean, List[SlicingCode]) = {
    if (element.instance == slice.occurrence) {
      (true, OccurrenceSliceMatch(slice, element) :: Nil)
    } else {
      (false, Nil)
    }
  }

  def evaluateAssertionSlice[T](slice: AssertionSlice[T], element: Element)
    (
      implicit s: Separators,
      t: Option[TimeZone],
      VSValidator: Validator,
      Detections: ConfigurableDetections
    ): (Boolean, List[SlicingCode]) = {
    eval(slice.assertion, element) match {
      case EvalResult.Pass => (true, AssertionSliceMatch(slice, element) :: Nil)
      case EvalResult.Fail(_) => (false, Nil)
      case EvalResult.FailPlugin(_, _) => (false, Nil)
      case EvalResult.Inconclusive(trace) => (false, AssertionSliceInconclusive(slice, element, trace) :: Nil)
    }
  }


  def applyDatatypeToField(f: Field, slice: Slice[Datatype])
    (implicit s: Separators): (Option[Field], List[SlicingCode]) = {
    try {
      (DataElement.field(slice.use, f.req, f.location, f.rawMessageValue, f.instance), Nil)
    } catch {
      case e: Exception => (None, MatchError(slice, f, e.getMessage) :: Nil)
    }
  }

  def applySegmentToSegmentRef(instance: SegmentInstance, slice: Slice[Segment])
    (implicit s: Separators, ctr: Counter): (SegmentInstance, List[SlicingCode]) = {
    try {
      val parsed = SegmentInstance.apply(
        instance.model.copy(ref = slice.use),
        instance.rawMessageValue,
        instance.instance,
        instance.location.line
      ).copy(location = instance.location)
      (parsed, Nil)
    } catch {
      case e: Exception => (instance, MatchError(slice, instance, e.getMessage) :: Nil)
    }
  }

  def ordinal(position: Int, instance: Int): Int = {
    (position.toString + instance.toString).toInt
  }

  def slicingCodeToDetection(slicingCode: SlicingCode)(implicit Detections: ConfigurableDetections): Entry = slicingCode match {
    case AssertionSliceMatch(assertionSlice, element) =>  assertionSlice.use match {
      case d: Datatype => Detections.assertionSlicingMatch(element, assertionSlice.description, "datatype", d.id)
      case s: Segment => Detections.assertionSlicingMatch(element, assertionSlice.description, "segment", s.id)
    }
    case OccurrenceSliceMatch(occurrenceSlice, element) => occurrenceSlice.use match {
      case d: Datatype => Detections.occurrenceSlicingMatch(element, occurrenceSlice.occurrence, "datatype", d.id)
      case s: Segment => Detections.occurrenceSlicingMatch(element, occurrenceSlice.occurrence, "segment", s.id)
    }
    case AssertionSliceInconclusive(assertionSlice, context, trace) => assertionSlice.use match {
      case d: Datatype => Detections.assertionSlicingInconclusive(context, assertionSlice.description, "datatype", d.id, trace.reasons.head.message)
      case s: Segment => Detections.assertionSlicingInconclusive(context, assertionSlice.description, "segment", s.id, trace.reasons.head.message)
    }
    case MatchError(slice, context, message) => slice.use match {
      case d: Datatype => Detections.slicingMatchError(context, "datatype", d.id, message)
      case s: Segment => Detections.slicingMatchError(context, "segment", s.id, message)
    }
    case NoSliceMatch(_, element, flavorName) =>  Detections.slicingNoMatch(element, flavorName)
    case SegmentSliceTargetIsGroup(_, element) => Detections.slicingTargetGroup(element)
  }

}
