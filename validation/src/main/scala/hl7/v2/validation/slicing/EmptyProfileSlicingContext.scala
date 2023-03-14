package hl7.v2.validation.slicing
import hl7.v2.instance.{Group, Segment => SI}
import hl7.v2.profile.{Datatype, Segment}

object EmptyProfileSlicingContext extends ProfileSlicingContext {
  /**
    * Returns the list of possible slices defined for this Segment Reference at Position (position) in Group (group)
    */
  override def segmentSlicingForMessageGroupAtPosition[T](messageId: String, group: Group, position: Int): Option[Slicing[Segment]] = None

  /**
    * Returns the list of possible slices defined for this Field at Position (position) in Segment (segment)
    */
  override def fieldSlicingForSegmentAtPosition[T](segment: SI, position: Int): Option[Slicing[Datatype]] = None
}
