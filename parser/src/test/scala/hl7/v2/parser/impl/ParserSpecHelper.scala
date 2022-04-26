package hl7.v2.parser.impl

import hl7.v2.instance.{Counter, SegOrGroup, Separators}
import hl7.v2.profile.SegRefOrGroup
import org.specs2.{ScalaCheck, Specification}

trait ParserSpecHelper extends Specification with ScalaCheck with DefaultParser {

  def check(tc: (String, List[SegOrGroup]), profile: List[SegRefOrGroup]) = {
    val stack = PreProcessor.splitOnMSH(tc._1)._2
    implicit val s = Separators( '|', '^', '~', '\\', '&', '#')
    implicit val ctr = Counter(scala.collection.mutable.Map[String, Int]())
    val result = processChildren(profile, stack)._1.reverse
    result must containTheSameElementsAs(tc._2)
  }

}
