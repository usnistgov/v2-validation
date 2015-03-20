package hl7.v2.profile

import org.specs2.mutable.Specification

class ValueSetSpecTest extends Specification {

  "BindingStrength" should {

    val valid = Seq("R", "S", "U")
    s"be valid for $valid" in {
      valid map { x => BindingStrength(x) must beASuccessfulTry }
    }

    val invalid = Seq("", "r", "X", "Y")
    s"be invalid for $invalid" in {
      invalid map { x => BindingStrength(x) must beAFailedTry }
    }
  }

  "BindingLocation" should {

    val valid = Seq("1", " 1 or 2 ", " 1 or 2 ", "1 and 2", "1xor2",
      "1:1", "1:1or2")
    s"be valid for $valid" in {
      valid map { x => BindingLocation(x) must beASuccessfulTry }
    }

    val invalid = Seq("", "+1", "1:", "1 or ")
    s"be invalid for $invalid" in {
      invalid map { x => BindingLocation(x) must beAFailedTry }
    }
  }

  "ValueSetSpec" should {

    val valid = Seq("x", "x#R","x#R#1 or 2")
    s"be valid for $valid" in {
      valid map { x => ValueSetSpec(x) must beASuccessfulTry }
    }

    val invalid = Seq("", "x# ", "x# R", "x##1")
    s"be invalid for $invalid" in {
      invalid map { x => ValueSetSpec(x) must beAFailedTry }
    }
  }

}
