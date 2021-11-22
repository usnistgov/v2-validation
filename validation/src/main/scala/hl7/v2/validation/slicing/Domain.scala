package hl7.v2.validation.slicing
import expression.Expression
import hl7.v2.profile.{Datatype, Segment}

sealed trait Slicing[T] {
  def position: Int
  def slices: List[Slice[T]]
}

sealed trait SegmentSlicing extends Slicing[Segment] {}
sealed trait FieldSlicing extends Slicing[Datatype] {}

sealed trait Slice[T] {
  def use: T
}

case class OccurrenceSlice[T] (
   occurrence: Int,
   use: T
) extends Slice[T]

case class AssertionSlice[T] (
   description: String,
   assertion: Expression,
   use: T
) extends Slice[T]

case class SegmentOccurrenceSlicing(
  position: Int,
  slices: List[OccurrenceSlice[Segment]]
) extends SegmentSlicing

case class SegmentAssertionSlicing(
  position: Int,
  slices: List[AssertionSlice[Segment]]
) extends SegmentSlicing

case class FieldOccurrenceSlicing(
 position: Int,
 slices: List[OccurrenceSlice[Datatype]]
) extends FieldSlicing

case class FieldAssertionSlicing(
  position: Int,
  slices: List[AssertionSlice[Datatype]]
) extends FieldSlicing
