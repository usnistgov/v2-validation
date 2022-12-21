package hl7.v2.parser.impl

import hl7.v2.instance.{Counter, Field, Line, Location, SegOrGroup, Segment, Separators}
import hl7.v2.profile.{Range, SegRefOrGroup, Usage, ValueSetSpec, Req => Requirement, SegmentRef => SM}
import org.specs2.{ScalaCheck, Specification}

trait ParserSpecHelper extends Specification with ScalaCheck with DefaultParser {

  def check(tc: (String, List[SegOrGroup]), profile: List[SegRefOrGroup]) = {
    val stack = PreProcessor.splitOnMSH(tc._1)._2
    implicit val s = Separators( '|', '^', '~', '\\', '&', '#')
    implicit val ctr = Counter(scala.collection.mutable.Map[String, Int]())
    val result = processChildren(profile, stack.map(x => Line(x._1, x._2)))._1.reverse
    result must containTheSameElementsAs(tc._2)
  }

  def Req(
     position: Int,
     description: String,
     usage: Usage,
     cardinality: Option[Range],
     length: Option[Range],
     confLength: Option[String],
     vsSpec: List[ValueSetSpec],
     confRange: Option[Range],
     hide: Boolean,
     csValueBackwardsCompatible: Option[String]): Requirement = Requirement(position, description, usage, cardinality, length, confLength, vsSpec, confRange, hide)

  def SegmentInstance(
   model: SM,
   location: Location,
   instance: Int,
   children: List[Field],
   hasExtra: Boolean,
   rawMessageValue: String
  ): Segment = Segment(model, location, instance, children, hasExtra)

}
