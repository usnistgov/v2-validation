package hl7.v2.instance

import org.scalacheck.{Prop, Gen}
import org.specs2.ScalaCheck
import org.specs2.Specification

class EscapeSeqHandlerSpec
  extends Specification
  with ScalaCheck
  with EscapeSeqHandler { def is =s2"""
  Escape sequence specification

    Escaping and un-escaping a string should return the same string $e1

  """

  implicit val separators = Separators( '|', '^', '~', '\\', '&', Some('#') )

  // A separator generator
  private def SepGen = Gen.oneOf("|", "^", "~", "\\", "&", "#")
  // The test string generator
  def gen = Gen.listOfN( 10, Gen.oneOf( SepGen, Gen.alphaStr ) )

  def e1 = Prop.forAll( gen ) { (l: List[String]) =>
    val s = l.mkString
    unescape( escape( s ) ) === s
  }
}
