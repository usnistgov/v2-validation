package hl7.v2.validation.slicing

import gov.nist.hit.hl7.v2.schemas.utils.{HL7v2Schema, HL7v2SchemaResourceResolver}

import java.io.InputStream
import hl7.v2.instance
import hl7.v2.instance.Group
import hl7.v2.profile.{Datatype, Profile, Segment}
import nist.xml.util.{ClassPathResourceResolver, XOMDocumentBuilder}

import scala.util.{Failure, Try}

class DefaultProfileSlicingContext(
  val segmentSlicing : Map[String, Map[String, Map[Int, Slicing[Segment]]]],
  val fieldSlicing: Map[String, Map[Int, Slicing[Datatype]]]
) extends ProfileSlicingContext {
  /**
    * Returns the list of possible slices defined for this Segment Reference at Position (position) in Group (group)
    */
  override def segmentSlicingForMessageGroupAtPosition[T](messageId: String, group: Group, position: Int): Option[Slicing[Segment]] = {
    segmentSlicing.get(messageId) match {
      case Some(message) => message.get(group.model.id) match {
        case Some(group) => group.get(position)
        case None => None
      }
      case None => None
    }
  }

  /**
    * Returns the list of possible slices defined for this Field at Position (position) in Segment (segment)
    */
  override def fieldSlicingForSegmentAtPosition[T](segment: instance.Segment, position: Int): Option[Slicing[Datatype]] = {
    fieldSlicing.get(segment.model.ref.id) match {
      case Some(byContext) => byContext.get(position)
      case None => None
    }
  }
}

object DefaultProfileSlicingContext {
  /**
   * The resource resolver
   */
  private val resourceResolver = Some(new HL7v2SchemaResourceResolver())

  def apply(context: InputStream, profile: Profile): Try[DefaultProfileSlicingContext] = {
    try {
      XOMDocumentBuilder.build(context, HL7v2Schema.getSlicing, resourceResolver) map { doc =>
        implicit val p: Profile = profile;
        val (segments, fields) = SlicingXMLDeserializer.deserialize(doc)
        new DefaultProfileSlicingContext(
          segments.getOrElse(Map[String, Map[String, Map[Int, Slicing[Segment]]]]()),
          fields.getOrElse(Map[String, Map[Int, Slicing[Datatype]]]())
        )
      }
    } catch { case e: Throwable => Failure(e) }
  }
}