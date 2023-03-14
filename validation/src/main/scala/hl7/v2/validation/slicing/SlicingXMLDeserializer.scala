package hl7.v2.validation.slicing

import hl7.v2.profile.{Datatype, Profile, Segment}

object SlicingXMLDeserializer {
  import nist.xml.util.XOMExtensions._


  type SegmentSlicingDefs = Map[String, Map[String, Map[Int, Slicing[Segment]]]]
  type FieldSlicingDefs = Map[String, Map[Int, Slicing[Datatype]]]
  type PickTarget[T] = (Profile, String) => Option[T]
  type SlicingParser[E, T>: Slicing[E]] = (Int, List[Slice[E]]) => T

  def deserialize(doc: nu.xom.Document)(implicit profile: Profile): (Option[SegmentSlicingDefs], Option[FieldSlicingDefs]) = {
    val slicing = doc.getRootElement
    if(slicing != null) {
      (deserializeSegmentSlicingByMessage(slicing), deserializeFieldSlicing(slicing))
    } else {
      (None, None)
    }
  }

  def deserializeSegmentSlicingByMessage(elm: nu.xom.Element)(implicit profile: Profile): Option[SegmentSlicingDefs] = {
    val segmentSlicing = elm.getFirstChildElement("SegmentSlicing")
    if(segmentSlicing != null) {
      val contexts = segmentSlicing.getChildElements("Message")
      val byMessage = for(context <- contexts) yield {
        context.attribute("ID") -> deserializeSlicingContext[Segment](
          context,
          "GroupContext",
          (p, ref) => p.segments.get(ref),
          (i, slices) => SegmentAssertionSlicing(i, slices),
          (i, slices) => SegmentOccurrenceSlicing(i, slices)
        )
      }
      Some(byMessage.toMap)
    } else {
      None
    }
  }

  def deserializeFieldSlicing(elm: nu.xom.Element)(implicit profile: Profile): Option[FieldSlicingDefs] = {
    val fieldSlicing = elm.getFirstChildElement("FieldSlicing")
    if(fieldSlicing != null) {
      Some(deserializeSlicingContext(
        fieldSlicing,
        "SegmentContext",
        (p, ref) => p.datatypes.get(ref),
        (i, slices) => FieldAssertionSlicing(i, slices),
        (i, slices) => FieldOccurrenceSlicing(i, slices)
      ))
    } else {
      None
    }
  }

  def deserializeSlicingContext[T](elm: nu.xom.Element, elmName: String, pickTarget: PickTarget[T], assertionBuilder: (Int, List[AssertionSlice[T]]) => Slicing[T], occurrenceBuilder: (Int, List[OccurrenceSlice[T]]) => Slicing[T])(implicit profile: Profile): Map[String, Map[Int, Slicing[T]]] = {
    val contexts = elm.getChildElements(elmName)
    contexts.map(elm => elm).groupBy({
      ctx => ctx.attribute("ID")
    }).map({
      pair => {
        if(pair._2.size > 1)
          throw new Exception(s"Context ${pair._1} is present multiple times in slicing")
        val top = pair._2.head
        pair._1 -> deserializeSlicingPosition(top, pickTarget, assertionBuilder, occurrenceBuilder)
      }
    })
  }

  def deserializeSlicingPosition[T](context: nu.xom.Element, pickTarget: PickTarget[T], assertionBuilder: (Int, List[AssertionSlice[T]]) => Slicing[T], occurrenceBuilder: (Int, List[OccurrenceSlice[T]]) => Slicing[T])(implicit profile: Profile): Map[Int, Slicing[T]] = {
    context.getChildElements().map(x => x).groupBy({
      slice => slice.attribute("Position").toInt
    }).map({
      pair => {
        if(pair._2.size > 1)
          throw new Exception(s"Position ${pair._1} is present multiple times in slicing for context ${context.attribute("ID")}")

        val top = pair._2.head
        top.getLocalName match {
          case "OccurrenceSlicing" => pair._1 -> occurrenceBuilder(pair._1, occurrenceSlicing(top, pickTarget))
          case "AssertionSlicing" => pair._1 -> assertionBuilder(pair._1, assertionSlicing(top, pickTarget))
          case _ => throw new Exception(s"Unrecognized slicing type : ${top.getLocalName}")
        }
      }
    })
  }

  def occurrenceSlicing[T](slicing: nu.xom.Element, pickTarget: PickTarget[T])(implicit profile: Profile): List[OccurrenceSlice[T]] = {
    val slices = slicing.getChildElements("Slice")
    slices.map({
      slice =>
        val occurrence = attribute(slice.attribute("Occurrence"), "Instance number for Occurrence Slicing must be valued").toInt
        val reference = attribute(slice.attribute("Ref"), "Target Reference (Ref Attribute) for Occurrence Slicing must be valued")
        pickTarget(profile, reference) match {
          case Some(value) => OccurrenceSlice(occurrence, value)
          case None => throw new Exception("Not found slice reference " + reference)
        }
    }).toList
  }

  def assertionSlicing[T](slicing: nu.xom.Element, pickTarget: PickTarget[T])(implicit profile: Profile): List[AssertionSlice[T]] = {
    val slices = slicing.getChildElements("Slice")
    slices.map({
      slice =>
        val description = textElement(slice.getFirstChildElement("Description"), "Description for Assertion Slicing must be valued")
        val reference = attribute(slice.attribute("Ref"), "Target Reference (Ref Attribute) for Assertion Slicing must be valued")
        val assertion = slice.getFirstChildElement("Assertion")
        if(assertion == null || assertion.getChildElements.size() != 1) {
          throw new Exception("Assertion in AssertionSlicing must be valued with an expression")
        }
        pickTarget(profile, reference) match {
          case Some(value) => AssertionSlice(description, expression.XMLDeserializer.expression(assertion.getChildElements.get(0)), value)
          case None => throw new Exception("Not found slice reference " + reference)
        }
    }).toList
  }


  def attribute(value: String, message: String): String = if(value != null && !value.isEmpty) value else throw new Exception(message)
  def textElement(elm: nu.xom.Element, message: String): String = if(elm != null && !elm.getValue.isEmpty) elm.getValue else throw new Exception(message)


}
