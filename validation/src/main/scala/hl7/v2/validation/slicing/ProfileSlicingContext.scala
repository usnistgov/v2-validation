package hl7.v2.validation.slicing

import hl7.v2.instance.{Group, Segment => SI }
import hl7.v2.profile.{Datatype, Segment}

trait ProfileSlicingContext {

  /**
    * Returns the list of possible slices defined for this Segment Reference at Position (position) in Group (group)
    */
  def segmentSlicingForMessageGroupAtPosition[T](messageId: String, group: Group, position: Int): Option[Slicing[Segment]]
  /**
    * Returns the list of possible slices defined for this Field at Position (position) in Segment (segment)
    */
  def fieldSlicingForSegmentAtPosition[T](segment: SI, position: Int): Option[Slicing[Datatype]]

}
