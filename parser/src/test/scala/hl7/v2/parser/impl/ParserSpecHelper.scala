package hl7.v2.parser.impl

import hl7.v2.instance.{Counter, SegOrGroup, Separators}
import hl7.v2.profile.{Req => Requeriement, SegRefOrGroup, Usage, Range, ValueSetSpec}
import org.specs2.{ScalaCheck, Specification}

trait ParserSpecHelper extends Specification with ScalaCheck with DefaultParser {

  def check(tc: (String, List[SegOrGroup]), profile: List[SegRefOrGroup]) = {
    val stack = PreProcessor.splitOnMSH(tc._1)._2
    implicit val s = Separators( '|', '^', '~', '\\', '&', '#')
    implicit val ctr = Counter(scala.collection.mutable.Map[String, Int]())
    val result = processChildren(profile, stack)._1.reverse
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
     csValueBackwardsCompatible: Option[String]): Requeriement = Requeriement(position, description, usage, cardinality, length, confLength, vsSpec, confRange, hide)

}
