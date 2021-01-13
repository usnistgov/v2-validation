package hl7.v2.validation.coconstraints

import expression.EvalResult.{Reason, Trace}
import hl7.v2.instance.{Element, Group}
import hl7.v2.parser.impl.DefaultParser
import hl7.v2.profile.{BindingStrength, Range}
import hl7.v2.validation.structure.Helpers
import hl7.v2.validation.vs.{BindingLocation, CodeUsage, VSValidationCode, ValueSetBinding, VsEntry, Code => VCode}
import org.specs2.Specification

trait CoConstraintTableSpec  extends Specification
  with hl7.v2.validation.coconstraints.DefaultValidator
  with expression.DefaultEvaluator
  with hl7.v2.validation.vs.DefaultValueSetValidator
  with CoConstraintSpecMocks
  with Helpers
  with DefaultParser {

  def requiredCc() = {
    val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(
      PlainText("CCT-1 (A)", "1[*]", "CWE")
    ), List())
    val sBinding = simpleBinding(List(cc), List())

    def failure = {
      Seq(
        // One Segment
        List("CCT|ST|x^^y"),
        // Multi Segment
        List("CCT|ST|x^^y", "CCT|ST|x^^y")
      ) map (
        segments => {
          implicit val grp: Group = group(segments)
          checkGroupAndBindings(grp, List(sBinding)) must containTheSameElementsAs(
            createEntry(RequiredCoConstraint(cc))
          )
        })
    }

    def success = {
      Seq(
        // One Segment
        List("CCT|CWE|x^^y"),
        // Multi Segment
        List("CCT|ST|x^^y", "CCT|CWE|x^^y")
      ) map (
        segments => {
          implicit val grp: Group = group(segments)
          checkGroupAndBindings(grp, List(sBinding)) must containTheSameElementsAs(
            Nil
          )
        })
    }
    failure ++ success
  }

  def minCardinality() = {
    val cc = scc(CoConstraintUsage.R, Range(2, "*"), List(
      PlainText("CCT-1 (A)", "1[*]", "CWE")
    ), List())
    val sBinding = simpleBinding(List(cc), List())

    def failure = {
      Seq(
        // One Segment, One match
        List("CCT|CWE|x^^y"),
        // Multi Segment, One match
        List("CCT|ST|x^^y", "CCT|CWE|x^^y")
      ) map (
        segments => {
          implicit val grp: Group = group(segments)
          checkGroupAndBindings(grp, List(sBinding)) must containTheSameElementsAs(
            createEntry(CardinalityCoConstraint(cc, 1))
          )
        })
    }

    def success = {
      Seq(
        // Two Segment, Two match
        List("CCT|CWE|x^^y", "CCT|CWE|z^^t"),
        // Multi Segment, Two match
        List("CCT|ST|x^^y", "CCT|CWE|x^^y", "CCT|CWE|z^^t"),
        // X Segment, X match
        List("CCT|CWE|x^^y", "CCT|CWE|z^^t", "CCT|CWE|e^^f"),
        // Multi Segment, Multi match
        List("CCT|ST|x^^y", "CCT|CWE|x^^y", "CCT|CWE|z^^t", "CCT|CWE|e^^f")
      ) map (
        segments => {
          implicit val grp: Group = group(segments)
          checkGroupAndBindings(grp, List(sBinding)) must containTheSameElementsAs(
            Nil
          )
        })
    }
    failure ++ success
  }

  def maxCardinality() = {
    val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(
      PlainText("CCT-1 (A)", "1[*]", "CWE")
    ), List())
    val sBinding = simpleBinding(List(cc), List())

    def success = {
      Seq(
        // One Segment, One match
        List("CCT|CWE|x^^y"),
        // Multi Segment, One match
        List("CCT|ST|x^^y", "CCT|CWE|x^^y")
      ) map (
        segments => {
          implicit val grp: Group = group(segments)
          checkGroupAndBindings(grp, List(sBinding)) must containTheSameElementsAs(
            Nil
          )
        })
    }

    def failure = {
      Seq(
        // Two Segment, Two match
        (List("CCT|CWE|x^^y", "CCT|CWE|z^^t"), 2),
        // Multi Segment, Two match
        (List("CCT|ST|x^^y", "CCT|CWE|x^^y", "CCT|CWE|z^^t"), 2),
        // X Segment, X match
        (List("CCT|CWE|x^^y", "CCT|CWE|z^^t", "CCT|CWE|e^^f"), 3),
        // Multi Segment, Multi match
        (List("CCT|ST|x^^y", "CCT|CWE|x^^y", "CCT|CWE|z^^t", "CCT|CWE|e^^f"), 3)
      ) map (
        segments => {
          implicit val grp: Group = group(segments._1)
          checkGroupAndBindings(grp, List(sBinding)) must containTheSameElementsAs(
            createEntry(CardinalityCoConstraint(cc, segments._2))
          )
        })
    }
    failure ++ success
  }

  def ccMatch() = {
    def binding(selectors: List[CoConstraintCell]) = {
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), selectors, List())
      simpleBinding(List(cc), List())
    }

    def plaint = {
      implicit val grp: Group = group(List("CCT|CWE|x^^y"))
      val sBinding = binding(List(PlainText("CCT-1 (A)", "1[*]", "CWE")))
      checkGroupAndBindings(grp, List(sBinding)) must containTheSameElementsAs(Nil)
    }
    def code = {
      implicit val grp: Group = group(List("CCT|CWE|x^^y"))
      val sBinding = binding(List(Code("CCT-1 (A)", "2[*]", "x", "y", List(
        CoConstraintBindingLocation(1, "1[*]", "3[*]")
      ))))
      checkGroupAndBindings(grp, List(sBinding)) must containTheSameElementsAs(Nil)
    }
    def vsBinding = {
      implicit val grp: Group = group(List("CCT|CWE|A^^SYS1"))
      val sBinding = binding(List(ValueSet("CCT-1 (A)", "2[*]", List(
        ValueSetBinding(".", Some(BindingStrength.R), List("vs1_cs"), List(
          BindingLocation("1[1]", Some("3[1]"))
        ))
      ))))
      checkGroupAndBindings(grp, List(sBinding)) must containTheSameElementsAs(Nil)
    }
    def combine = {
      implicit val grp: Group = group(List("CCT|CWE|x^^y||A^^SYS1"))
      val sBinding = binding(List(
        PlainText("CCT-1 (A)", "1[*]", "CWE"),
        ValueSet("CCT-1 (A)", "4[*]", List(
          ValueSetBinding(".", Some(BindingStrength.R), List("vs1_cs"), List(
            BindingLocation("1[1]", Some("3[1]"))
          ))
        )),
        Code("CCT-1 (A)", "2[*]", "x", "y", List(
          CoConstraintBindingLocation(1, "1[*]", "3[*]")
        ))
      ))
      checkGroupAndBindings(grp, List(sBinding)) must containTheSameElementsAs(Nil)
    }

    plaint and code and vsBinding and combine
  }

  def ccNoMatchFailed() = {
    def binding(selectors: List[CoConstraintCell]) = {
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), selectors, List())
      simpleBinding(List(cc), List())
    }

    def plaint = {
      implicit val grp: Group = group(List("CCT|CWE|x^^y"))
      val sBinding = binding(List(PlainText("CCT-1 (A)", "1[*]", "ST")))
      checkGroupAndBindings(grp, List(sBinding)) must containTheSameElementsAs(
        createEntry(RequiredCoConstraint(sBinding.bindings.head.coConstraints.head))
      )
    }
    def code = {
      implicit val grp: Group = group(List("CCT|CWE|x^^y"))
      val sBinding = binding(List(Code("CCT-1 (A)", "2[*]", "z", "y", List(
        CoConstraintBindingLocation(1, "1[*]", "3[*]")
      ))))
      checkGroupAndBindings(grp, List(sBinding)) must containTheSameElementsAs(
        createEntry(RequiredCoConstraint(sBinding.bindings.head.coConstraints.head))
      )
    }
    def vsBinding = {
      implicit val grp: Group = group(List("CCT|CWE|E^^SYS1"))
      val sBinding = binding(List(ValueSet("CCT-1 (A)", "2[*]", List(
        ValueSetBinding(".", Some(BindingStrength.R), List("vs1_cs"), List(
          BindingLocation("1[1]", Some("3[1]"))
        ))
      ))))
      checkGroupAndBindings(grp, List(sBinding)) must containTheSameElementsAs(
        createEntry(RequiredCoConstraint(sBinding.bindings.head.coConstraints.head))
      )
    }
    def combine = {
      Seq(
        //PlainText Fail
        List("CCT|ST|x^^y||A^^SYS1"),
        //Code Fail
        List("CCT|CWE|z^^y||A^^SYS1"),
        //Vs Binding Fail
        List("CCT|CWE|x^^y||E^^SYS1"),
        //All Fail
        List("CCT|ST|z^^y||E^^SYS1")
      ) map (
        segments => {
          implicit val grp: Group = group(segments)
          val sBinding = binding(List(
            PlainText("CCT-1 (A)", "1[*]", "CWE"),
            ValueSet("CCT-3 (C)", "4[*]", List(
              ValueSetBinding(".", Some(BindingStrength.R), List("vs1_cs"), List(
                BindingLocation("1[1]", Some("3[1]"))
              ))
            )),
            Code("CCT-2 (B)", "2[*]", "x", "y", List(
              CoConstraintBindingLocation(1, "1[*]", "3[*]")
            ))
          ))
          checkGroupAndBindings(grp, List(sBinding)) must containTheSameElementsAs(
            createEntry(RequiredCoConstraint(sBinding.bindings.head.coConstraints.head))
          )
        }
      )
    }

    Seq(plaint, code, vsBinding) ++ combine
  }

  def ccNoMatchInconclusive() = {
    def binding(selectors: List[CoConstraintCell]) = {
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), selectors, List())
      simpleBinding(List(cc), List())
    }

    def plaint = {
      implicit val grp: Group = group(List("CCT|CWE|x^^y"))
      val sBinding = binding(List(PlainText("CCT-1 (A)", "1", "ST")))
      checkGroupAndBindings(grp, List(sBinding)) must containTheSameElementsAs(
        createEntry(RequiredCoConstraint(sBinding.bindings.head.coConstraints.head))
      )
    }
    def code = {
      implicit val grp: Group = group(List("CCT|CWE|x^^y"))
      val sBinding = binding(List(Code("CCT-1 (A)", "2[*]", "z", "y", List(
        CoConstraintBindingLocation(1, "1[*].1[1]", "3[*]")
      ))))
      checkGroupAndBindings(grp, List(sBinding)) must containTheSameElementsAs(
        createEntry(RequiredCoConstraint(sBinding.bindings.head.coConstraints.head))
      )
    }
    def vsBinding = {
      implicit val grp: Group = group(List("CCT|CWE|E^^SYS1"))
      val sBinding = binding(List(ValueSet("CCT-1 (A)", "2[*]", List(
        ValueSetBinding(".", Some(BindingStrength.R), List("vs1_cs"), List(
          BindingLocation("1[1].1[1]", Some("3[1]"))
        ))
      ))))
      checkGroupAndBindings(grp, List(sBinding)) must containTheSameElementsAs(
        createEntry(RequiredCoConstraint(sBinding.bindings.head.coConstraints.head))
      )
    }
    def combine = {
      val selectors = Map(
        "PT" -> PlainText("CCT-1 (A)", "1[*]", "CWE"),
        "VS" -> ValueSet("CCT-3 (C)", "4[*]", List(
          ValueSetBinding(".", Some(BindingStrength.R), List("vs1_cs"), List(
            BindingLocation("1[1]", Some("3[1]"))
          ))
        )),
        "CD" -> Code("CCT-2 (B)", "2[*]", "x", "y", List(
          CoConstraintBindingLocation(1, "1[*]", "3[*]")
        ))
      )
      Seq(
        //PlainText Fail
        (List("CCT|CWE|x^^y||A^^SYS1"),
          Map("PT" -> PlainText("CCT-1 (A)", "1", "ST"))),
        //Code Fail
        (List("CCT|CWE|x^^y||A^^SYS1"),
          Map("CD" -> Code("CCT-1 (A)", "2[*]", "z", "y", List(
            CoConstraintBindingLocation(1, "1[*].1[1]", "3[*]")
          )))),
        //Vs Binding Fail
        (List("CCT|CWE|x^^y||A^^SYS1"),
          Map("VS" -> ValueSet("CCT-3 (C)", "4[*]", List(
            ValueSetBinding(".", Some(BindingStrength.R), List("vs1_cs"), List(
              BindingLocation("1[1].1[1]", Some("3[1]"))
            ))
        )))),
        //All Fail
        (List("CCT|CWE|x^^y||A^^SYS1"),
          Map("PT" -> PlainText("CCT-1 (A)", "1", "ST"),
          "CD" -> Code("CCT-1 (A)", "2[*]", "z", "y", List(
            CoConstraintBindingLocation(1, "1[*].1[1]", "3[*]")
          )),
          "VS" -> ValueSet("CCT-3 (C)", "4[*]", List(
            ValueSetBinding(".", Some(BindingStrength.R), List("vs1_cs"), List(
              BindingLocation("1[1].1[1]", Some("3[1]"))
            ))
          )))
        )
      ) map (
        segments => {
          implicit val grp: Group = group(segments._1)
          val sBinding = binding((selectors ++ segments._2).values.toList)
          checkGroupAndBindings(grp, List(sBinding)) must containTheSameElementsAs(
            createEntry(RequiredCoConstraint(sBinding.bindings.head.coConstraints.head))
          )
        }
        )
    }

    Seq(plaint, code, vsBinding) ++ combine
  }

  def ccNoMatchNotPopulated() = {
    def binding(selectors: List[CoConstraintCell]) = {
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), selectors, List())
      simpleBinding(List(cc), List())
    }

    def plaint = {
      implicit val grp: Group = group(List("CCT||x^^y"))
      val sBinding = binding(List(PlainText("CCT-1 (A)", "1[*]", "CWE")))
      checkGroupAndBindings(grp, List(sBinding)) must containTheSameElementsAs(
        createEntry(RequiredCoConstraint(sBinding.bindings.head.coConstraints.head))
      )
    }
    def code = {
      implicit val grp: Group = group(List("CCT|CWE|"))
      val sBinding = binding(List(Code("CCT-1 (A)", "2[*]", "x", "y", List(
        CoConstraintBindingLocation(1, "1[*]", "3[*]")
      ))))
      checkGroupAndBindings(grp, List(sBinding)) must containTheSameElementsAs(
        createEntry(RequiredCoConstraint(sBinding.bindings.head.coConstraints.head))
      )
    }
    def vsBinding = {
      implicit val grp: Group = group(List("CCT|CWE|"))
      val sBinding = binding(List(ValueSet("CCT-1 (A)", "2[*]", List(
        ValueSetBinding(".", Some(BindingStrength.R), List("vs1_cs"), List(
          BindingLocation("1[1]", Some("3[1]"))
        ))
      ))))
      checkGroupAndBindings(grp, List(sBinding)) must containTheSameElementsAs(
        createEntry(RequiredCoConstraint(sBinding.bindings.head.coConstraints.head))
      )
    }
    def combine = {
      Seq(
        //PlainText Fail
        List("CCT||x^^y||A^^SYS1"),
        //Code Fail
        List("CCT|CWE|||A^^SYS1"),
        //Vs Binding Fail
        List("CCT|CWE|x^^y||"),
        //All Fail
        List("CCT||||")
      ) map (
        segments => {
          implicit val grp: Group = group(segments)
          val sBinding = binding(List(
            PlainText("CCT-1 (A)", "1[*]", "CWE"),
            ValueSet("CCT-3 (C)", "4[*]", List(
              ValueSetBinding(".", Some(BindingStrength.R), List("vs1_cs"), List(
                BindingLocation("1[1]", Some("3[1]"))
              ))
            )),
            Code("CCT-2 (B)", "2[*]", "x", "y", List(
              CoConstraintBindingLocation(1, "1[*]", "3[*]")
            ))
          ))
          checkGroupAndBindings(grp, List(sBinding)) must containTheSameElementsAs(
            createEntry(RequiredCoConstraint(sBinding.bindings.head.coConstraints.head))
          )
        }
        )
    }

    Seq(plaint, code, vsBinding) ++ combine
  }

  def requiredGrp() = {
    val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(
      PlainText("CCT-1 (A)", "1[*]", "CWE")
    ), List())
    val ccg = grp("GRP", CoConstraintUsage.R, Range(1, "1"), cc, List())
    val sBinding = simpleBinding(List(), List(ccg))

    def failure = {
      Seq(
        // One Segment
        List("CCT|ST|x^^y"),
        // Multi Segment
        List("CCT|ST|x^^y", "CCT|ST|x^^y")
      ) map (
        segments => {
          implicit val grp: Group = group(segments)
          checkGroupAndBindings(grp, List(sBinding)) must containTheSameElementsAs(
            createEntry(RequiredGroup(ccg))
          )
        })
    }

    def success = {
      Seq(
        // One Segment
        List("CCT|CWE|x^^y"),
        // Multi Segment
        List("CCT|ST|x^^y", "CCT|CWE|x^^y")
      ) map (
        segments => {
          implicit val grp: Group = group(segments)
          checkGroupAndBindings(grp, List(sBinding)) must containTheSameElementsAs(
            Nil
          )
        })
    }
    failure ++ success
  }

  def minCardinalityGrp() = {
    val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(
      PlainText("CCT-1 (A)", "1[*]", "CWE")
    ), List())
    val ccg = grp("GRP", CoConstraintUsage.R, Range(2, "*"), cc, List())
    val sBinding = simpleBinding(List(), List(ccg))

    def failure = {
      Seq(
        // One Segment, One match
        List("CCT|CWE|x^^y"),
        // Multi Segment, One match
        List("CCT|ST|x^^y", "CCT|CWE|x^^y")
      ) map (
        segments => {
          implicit val grp: Group = group(segments)
          checkGroupAndBindings(grp, List(sBinding)) must containTheSameElementsAs(
            createEntry(CardinalityGroup(ccg, 1))
          )
        })
    }

    def success = {
      Seq(
        // Two Segment, Two match
        List("CCT|CWE|x^^y", "CCT|CWE|z^^t"),
        // Multi Segment, Two match
        List("CCT|ST|x^^y", "CCT|CWE|x^^y", "CCT|CWE|z^^t"),
        // X Segment, X match
        List("CCT|CWE|x^^y", "CCT|CWE|z^^t", "CCT|CWE|e^^f"),
        // Multi Segment, Multi match
        List("CCT|ST|x^^y", "CCT|CWE|x^^y", "CCT|CWE|z^^t", "CCT|CWE|e^^f")
      ) map (
        segments => {
          implicit val grp: Group = group(segments)
          checkGroupAndBindings(grp, List(sBinding)) must containTheSameElementsAs(
            Nil
          )
        })
    }
    failure ++ success
  }

  def maxCardinalityGrp() = {
    val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(
      PlainText("CCT-1 (A)", "1[*]", "CWE")
    ), List())
    val ccg = grp("GRP", CoConstraintUsage.R, Range(1, "1"), cc, List())
    val sBinding = simpleBinding(List(), List(ccg))

    def success = {
      Seq(
        // One Segment, One match
        List("CCT|CWE|x^^y"),
        // Multi Segment, One match
        List("CCT|ST|x^^y", "CCT|CWE|x^^y")
      ) map (
        segments => {
          implicit val grp: Group = group(segments)
          checkGroupAndBindings(grp, List(sBinding)) must containTheSameElementsAs(
            Nil
          )
        })
    }

    def failure = {
      Seq(
        // Two Segment, Two match
        (List("CCT|CWE|x^^y", "CCT|CWE|z^^t"), 2),
        // Multi Segment, Two match
        (List("CCT|ST|x^^y", "CCT|CWE|x^^y", "CCT|CWE|z^^t"), 2),
        // X Segment, X match
        (List("CCT|CWE|x^^y", "CCT|CWE|z^^t", "CCT|CWE|e^^f"), 3),
        // Multi Segment, Multi match
        (List("CCT|ST|x^^y", "CCT|CWE|x^^y", "CCT|CWE|z^^t", "CCT|CWE|e^^f"), 3)
      ) map (
        segments => {
          implicit val grp: Group = group(segments._1)
          checkGroupAndBindings(grp, List(sBinding)) must containTheSameElementsAs(
            createEntry(CardinalityGroup(ccg, segments._2))
          )
        })
    }
    failure ++ success
  }

  def grpMatch() = {
    def binding(selectors: List[CoConstraintCell]) = {
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), selectors, List())
      val ccg = grp("GRP", CoConstraintUsage.R, Range(1, "1"), cc, List())
      val sBinding = simpleBinding(List(), List(ccg))
      (sBinding, ccg, cc)
    }

    def plaint = {
      implicit val grp: Group = group(List("CCT|CWE|x^^y"))
      val sBinding = binding(List(PlainText("CCT-1 (A)", "1[*]", "CWE")))
      checkGroupAndBindings(grp, List(sBinding._1)) must containTheSameElementsAs(Nil)
    }
    def code = {
      implicit val grp: Group = group(List("CCT|CWE|x^^y"))
      val sBinding = binding(List(Code("CCT-1 (A)", "2[*]", "x", "y", List(
        CoConstraintBindingLocation(1, "1[*]", "3[*]")
      ))))
      checkGroupAndBindings(grp, List(sBinding._1)) must containTheSameElementsAs(Nil)
    }
    def vsBinding = {
      implicit val grp: Group = group(List("CCT|CWE|A^^SYS1"))
      val sBinding = binding(List(ValueSet("CCT-1 (A)", "2[*]", List(
        ValueSetBinding(".", Some(BindingStrength.R), List("vs1_cs"), List(
          BindingLocation("1[1]", Some("3[1]"))
        ))
      ))))
      checkGroupAndBindings(grp, List(sBinding._1)) must containTheSameElementsAs(Nil)
    }
    def combine = {
      implicit val grp: Group = group(List("CCT|CWE|x^^y||A^^SYS1"))
      val sBinding = binding(List(
        PlainText("CCT-1 (A)", "1[*]", "CWE"),
        ValueSet("CCT-1 (A)", "4[*]", List(
          ValueSetBinding(".", Some(BindingStrength.R), List("vs1_cs"), List(
            BindingLocation("1[1]", Some("3[1]"))
          ))
        )),
        Code("CCT-1 (A)", "2[*]", "x", "y", List(
          CoConstraintBindingLocation(1, "1[*]", "3[*]")
        ))
      ))
      checkGroupAndBindings(grp, List(sBinding._1)) must containTheSameElementsAs(Nil)
    }

    plaint and code and vsBinding and combine
  }

  def grpNoMatchFailed() = {

    def binding(selectors: List[CoConstraintCell]) = {
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), selectors, List())
      val ccg = grp("GRP", CoConstraintUsage.R, Range(1, "1"), cc, List())
      val sBinding = simpleBinding(List(), List(ccg))
      (sBinding, ccg, cc)
    }

    def plaint = {
      implicit val grp: Group = group(List("CCT|CWE|x^^y"))
      val sBinding = binding(List(PlainText("CCT-1 (A)", "1[*]", "ST")))
      checkGroupAndBindings(grp, List(sBinding._1)) must containTheSameElementsAs(
        createEntry(RequiredGroup(sBinding._2))
      )
    }
    def code = {
      implicit val grp: Group = group(List("CCT|CWE|x^^y"))
      val sBinding = binding(List(Code("CCT-1 (A)", "2[*]", "z", "y", List(
        CoConstraintBindingLocation(1, "1[*]", "3[*]")
      ))))
      checkGroupAndBindings(grp, List(sBinding._1)) must containTheSameElementsAs(
        createEntry(RequiredGroup(sBinding._2))
      )
    }
    def vsBinding = {
      implicit val grp: Group = group(List("CCT|CWE|E^^SYS1"))
      val sBinding = binding(List(ValueSet("CCT-1 (A)", "2[*]", List(
        ValueSetBinding(".", Some(BindingStrength.R), List("vs1_cs"), List(
          BindingLocation("1[1]", Some("3[1]"))
        ))
      ))))
      checkGroupAndBindings(grp, List(sBinding._1)) must containTheSameElementsAs(
        createEntry(RequiredGroup(sBinding._2))
      )
    }
    def combine = {
      Seq(
        //PlainText Fail
        List("CCT|ST|x^^y||A^^SYS1"),
        //Code Fail
        List("CCT|CWE|z^^y||A^^SYS1"),
        //Vs Binding Fail
        List("CCT|CWE|x^^y||E^^SYS1"),
        //All Fail
        List("CCT|ST|z^^y||E^^SYS1")
      ) map (
        segments => {
          implicit val grp: Group = group(segments)
          val sBinding = binding(List(
            PlainText("CCT-1 (A)", "1[*]", "CWE"),
            ValueSet("CCT-3 (C)", "4[*]", List(
              ValueSetBinding(".", Some(BindingStrength.R), List("vs1_cs"), List(
                BindingLocation("1[1]", Some("3[1]"))
              ))
            )),
            Code("CCT-2 (B)", "2[*]", "x", "y", List(
              CoConstraintBindingLocation(1, "1[*]", "3[*]")
            ))
          ))
          checkGroupAndBindings(grp, List(sBinding._1)) must containTheSameElementsAs(
            createEntry(RequiredGroup(sBinding._2))
          )
        }
        )
    }

    Seq(plaint, code, vsBinding) ++ combine
  }

  def grpNoMatchInconclusive = {

    def binding(selectors: List[CoConstraintCell]) = {
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), selectors, List())
      val ccg = grp("GRP", CoConstraintUsage.R, Range(1, "1"), cc, List())
      val sBinding = simpleBinding(List(), List(ccg))
      (sBinding, ccg, cc)
    }

    def plaint = {
      implicit val grp: Group = group(List("CCT|CWE|x^^y"))
      val sBinding = binding(List(PlainText("CCT-1 (A)", "1", "ST")))
      checkGroupAndBindings(grp, List(sBinding._1)) must containTheSameElementsAs(
        createEntry(RequiredGroup(sBinding._2))
      )
    }
    def code = {
      implicit val grp: Group = group(List("CCT|CWE|x^^y"))
      val sBinding = binding(List(Code("CCT-1 (A)", "2[*]", "z", "y", List(
        CoConstraintBindingLocation(1, "1[*].1[1]", "3[*]")
      ))))
      checkGroupAndBindings(grp, List(sBinding._1)) must containTheSameElementsAs(
        createEntry(RequiredGroup(sBinding._2))
      )
    }
    def vsBinding = {
      implicit val grp: Group = group(List("CCT|CWE|E^^SYS1"))
      val sBinding = binding(List(ValueSet("CCT-1 (A)", "2[*]", List(
        ValueSetBinding(".", Some(BindingStrength.R), List("vs1_cs"), List(
          BindingLocation("1[1].1[1]", Some("3[1]"))
        ))
      ))))
      checkGroupAndBindings(grp, List(sBinding._1)) must containTheSameElementsAs(
        createEntry(RequiredGroup(sBinding._2))
      )
    }
    def combine = {
      val selectors = Map(
        "PT" -> PlainText("CCT-1 (A)", "1[*]", "CWE"),
        "VS" -> ValueSet("CCT-3 (C)", "4[*]", List(
          ValueSetBinding(".", Some(BindingStrength.R), List("vs1_cs"), List(
            BindingLocation("1[1]", Some("3[1]"))
          ))
        )),
        "CD" -> Code("CCT-2 (B)", "2[*]", "x", "y", List(
          CoConstraintBindingLocation(1, "1[*]", "3[*]")
        ))
      )
      Seq(
        //PlainText Fail
        (List("CCT|CWE|x^^y||A^^SYS1"),
          Map("PT" -> PlainText("CCT-1 (A)", "1", "ST"))),
        //Code Fail
        (List("CCT|CWE|x^^y||A^^SYS1"),
          Map("CD" -> Code("CCT-1 (A)", "2[*]", "z", "y", List(
            CoConstraintBindingLocation(1, "1[*].1[1]", "3[*]")
          )))),
        //Vs Binding Fail
        (List("CCT|CWE|x^^y||A^^SYS1"),
          Map("VS" -> ValueSet("CCT-3 (C)", "4[*]", List(
            ValueSetBinding(".", Some(BindingStrength.R), List("vs1_cs"), List(
              BindingLocation("1[1].1[1]", Some("3[1]"))
            ))
          )))),
        //All Fail
        (List("CCT|CWE|x^^y||A^^SYS1"),
          Map("PT" -> PlainText("CCT-1 (A)", "1", "ST"),
            "CD" -> Code("CCT-1 (A)", "2[*]", "z", "y", List(
              CoConstraintBindingLocation(1, "1[*].1[1]", "3[*]")
            )),
            "VS" -> ValueSet("CCT-3 (C)", "4[*]", List(
              ValueSetBinding(".", Some(BindingStrength.R), List("vs1_cs"), List(
                BindingLocation("1[1].1[1]", Some("3[1]"))
              ))
            )))
        )
      ) map (
        segments => {
          implicit val grp: Group = group(segments._1)
          val sBinding = binding((selectors ++ segments._2).values.toList)
          checkGroupAndBindings(grp, List(sBinding._1)) must containTheSameElementsAs(
            createEntry(RequiredGroup(sBinding._2))
          )
        })
    }

    Seq(plaint, code, vsBinding) ++ combine
  }

  def grpNoMatchNotPopulated() = {
    def binding(selectors: List[CoConstraintCell]) = {
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), selectors, List())
      val ccg = grp("GRP", CoConstraintUsage.R, Range(1, "1"), cc, List())
      val sBinding = simpleBinding(List(), List(ccg))
      (sBinding, ccg, cc)
    }

    def plaint = {
      implicit val grp: Group = group(List("CCT||x^^y"))
      val sBinding = binding(List(PlainText("CCT-1 (A)", "1[*]", "CWE")))
      checkGroupAndBindings(grp, List(sBinding._1)) must containTheSameElementsAs(
        createEntry(RequiredGroup(sBinding._2))
      )
    }
    def code = {
      implicit val grp: Group = group(List("CCT|CWE|"))
      val sBinding = binding(List(Code("CCT-1 (A)", "2[*]", "x", "y", List(
        CoConstraintBindingLocation(1, "1[*]", "3[*]")
      ))))
      checkGroupAndBindings(grp, List(sBinding._1)) must containTheSameElementsAs(
        createEntry(RequiredGroup(sBinding._2))
      )
    }
    def vsBinding = {
      implicit val grp: Group = group(List("CCT|CWE|"))
      val sBinding = binding(List(ValueSet("CCT-1 (A)", "2[*]", List(
        ValueSetBinding(".", Some(BindingStrength.R), List("vs1_cs"), List(
          BindingLocation("1[1]", Some("3[1]"))
        ))
      ))))
      checkGroupAndBindings(grp, List(sBinding._1)) must containTheSameElementsAs(
        createEntry(RequiredGroup(sBinding._2))
      )
    }
    def combine = {
      Seq(
        //PlainText Fail
        List("CCT||x^^y||A^^SYS1"),
        //Code Fail
        List("CCT|CWE|||A^^SYS1"),
        //Vs Binding Fail
        List("CCT|CWE|x^^y||"),
        //All Fail
        List("CCT||||")
      ) map (
        segments => {
          implicit val grp: Group = group(segments)
          val sBinding = binding(List(
            PlainText("CCT-1 (A)", "1[*]", "CWE"),
            ValueSet("CCT-3 (C)", "4[*]", List(
              ValueSetBinding(".", Some(BindingStrength.R), List("vs1_cs"), List(
                BindingLocation("1[1]", Some("3[1]"))
              ))
            )),
            Code("CCT-2 (B)", "2[*]", "x", "y", List(
              CoConstraintBindingLocation(1, "1[*]", "3[*]")
            ))
          ))
          checkGroupAndBindings(grp, List(sBinding._1)) must containTheSameElementsAs(
            createEntry(RequiredGroup(sBinding._2))
          )
        })
    }

    Seq(plaint, code, vsBinding) ++ combine
  }

  def grpMatchEval() = {
    def binding(primary: List[CoConstraintCell], ccList: List[CoConstraint]) = {
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), primary, List())
      val ccg = grp("GRPIE", CoConstraintUsage.R, Range(1, "1"), cc, ccList)
      val sBinding = simpleBinding(List(), List(ccg))
      (sBinding, ccg, cc)
    }

    def plainSuccess = {
      implicit val grp: Group = group(List("CCT|CWE|x^^y", "CCT|ST|a^^b", "CCT|TST"))
      val cc =  scc(CoConstraintUsage.R, Range(1, "1"), List(
        PlainText("CCT-1 (B)", "1[*]", "ST")
      ), List(
        PlainText("CCT-2.1 (B)", "2[*].1[*]", "a")
      ))
      val sBinding = binding(List(PlainText("CCT-1 (A)", "1[*]", "CWE")), List(cc))
      checkGroupAndBindings(grp, List(sBinding._1)) must containTheSameElementsAs(
        createEntry(CoConstraintPlainTextSuccess(cc, assertGet(grp, "1[2].2[1].1[1]"), cc.constraints.head.asInstanceOf[PlainText]))
      )
    }

    def plainFailure = {
      implicit val grp: Group = group(List("CCT|CWE|x^^y", "CCT|ST|a^^b", "CCT|TST"))
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(
        PlainText("CCT-1 (B)", "1[*]", "ST")
      ), List(
        PlainText("CCT-2.1 (B)", "2[*].1[*]", "x")
      ))
      val sBinding = binding(List(PlainText("CCT-1 (A)", "1[*]", "CWE")), List(cc))
      checkGroupAndBindings(grp, List(sBinding._1)) must containTheSameElementsAs(
        createEntry(CoConstraintPlainTextFailure(cc, assertGet(grp, "1[2].2[1].1[1]"), cc.constraints.head.asInstanceOf[PlainText], "a"))
      )
    }

    def codeSuccess = {
      implicit val grp: Group = group(List("CCT|CWE|x^^y", "CCT|ST|a^^b", "CCT|TST"))
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(
        PlainText("CCT-1 (A)", "1[*]", "CWE")
      ), List(
        Code("CCT-2 (B)", "2[*]", "x", "y", List(
          CoConstraintBindingLocation(1, "1[*]", "3[*]")
        ))
      ))
      val sBinding = binding(List(PlainText("CCT-1 (A)", "1[*]", "ST")), List(cc))
      checkGroupAndBindings(grp, List(sBinding._1)) must containTheSameElementsAs(
        createEntry(CoConstraintCodeSuccess(cc, assertGet(grp, "1[1].2[1]"), cc.constraints.head.asInstanceOf[Code]))
      )
    }

    def codeFailure = {
      implicit val grp: Group = group(List("CCT|CWE|z^^y", "CCT|ST|a^^b", "CCT|TST"))
      val cc =  scc(CoConstraintUsage.R, Range(1, "1"), List(
        PlainText("CCT-1 (A)", "1[*]", "CWE")
      ), List(
        Code("CCT-2 (B)", "2[*]", "x", "y", List(
          CoConstraintBindingLocation(1, "1[*]", "3[*]")
        ))
      ))
      val sBinding = binding(List(PlainText("CCT-1 (A)", "1[*]", "ST")), List(cc))
      checkGroupAndBindings(grp, List(sBinding._1)) must containTheSameElementsAs(
        createEntry(CoConstraintCodeFailureNotFound(cc, assertGet(grp, "1[1].2[1].1[1]"), cc.constraints.head.asInstanceOf[Code]))
      )
    }

    def vsBindingSuccess = {
      implicit val grp: Group = group(List("CCT|CWE|A^^SYS1", "CCT|ST|a^^b", "CCT|TST"))
      val vsB = ValueSetBinding(".", Some(BindingStrength.R), List("vs1_cs"), List(
        BindingLocation("1[1]", Some("3[1]"))
      ))
      val vs = ValueSet("CCT-1 (A)", "2[*]", List(
        vsB
      ))
      val cc =  scc(CoConstraintUsage.R, Range(1, "1"), List(
        PlainText("CCT-1 (A)", "1[*]", "CWE")
      ), List(
        vs
      ))
      val sBinding = binding(List(PlainText("CCT-1 (A)", "1[*]", "ST")), List(cc))
      checkGroupAndBindings(grp, List(sBinding._1)) must containTheSameElementsAs(
        createEntry(CoConstraintVsBindingSuccess(cc, assertGet(grp, "1[1].2[1]"), vs, vsB,
          VsEntry(assertGet(grp, "1[1].2[1]"), VSValidationCode.RVS(VCode("A", "des", CodeUsage.R, "SYS1"), vs1_cs), Some(BindingStrength.R))
        ))
      )
    }

    def vsBindingFailure = {
      implicit val grp: Group = group(List("CCT|CWE|X^^SYS1", "CCT|ST|a^^b", "CCT|TST"))
      val vsB = ValueSetBinding(".", Some(BindingStrength.R), List("vs1_cs"), List(
        BindingLocation("1[1]", Some("3[1]"))
      ))
      val vs = ValueSet("CCT-1 (A)", "2[*]", List(
        vsB
      ))
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(
        PlainText("CCT-1 (A)", "1[*]", "CWE")
      ), List(
        vs
      ))
      val sBinding = binding(List(PlainText("CCT-1 (A)", "1[*]", "ST")), List(cc))
      checkGroupAndBindings(grp, List(sBinding._1)) must containTheSameElementsAs(
        createEntry(CoConstraintVsBindingFailure(cc, assertGet(grp, "1[1]"), vs,
          VsEntry(assertGet(grp, "1[1].2[1]"), VSValidationCode.CodeNotFound("X", Some("SYS1"), List(vs1_cs)), Some(BindingStrength.R))
        ))
      )
    }

    def combineSuccess = {
      implicit val grp: Group = group(List("CCT|CWE|A^^SYS1", "CCT|ST|a^^b", "CCT|TST"))
      val vsB = ValueSetBinding(".", Some(BindingStrength.R), List("vs1_cs"), List(
        BindingLocation("1[1]", Some("3[1]"))
      ))
      val vs = ValueSet("CCT-1 (A)", "2[*]", List(
        vsB
      ))

      val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(
        PlainText("CCT-1 (B)", "1[*]", "CWE")
      ), List(
        PlainText("CCT-2.1 (B)", "2[*].1[*]", "A"),
        Code("CCT-2 (B)", "2[*]", "A", "SYS1", List(
          CoConstraintBindingLocation(1, "1[*]", "3[*]")
        )),
        vs
      ))
      val sBinding = binding(List(PlainText("CCT-1 (A)", "1[*]", "ST")), List(cc))
      checkGroupAndBindings(grp, List(sBinding._1)) must containTheSameElementsAs(
        createEntry(CoConstraintPlainTextSuccess(cc, assertGet(grp, "1[1].2[1].1[1]"), cc.constraints.head.asInstanceOf[PlainText])) :::
        createEntry(CoConstraintCodeSuccess(cc, assertGet(grp, "1[1].2[1]"), cc.constraints(1).asInstanceOf[Code])) :::
        createEntry(CoConstraintVsBindingSuccess(cc, assertGet(grp, "1[1].2[1]"), vs, vsB,
          VsEntry(assertGet(grp, "1[1].2[1]"), VSValidationCode.RVS(VCode("A", "des", CodeUsage.R, "SYS1"), vs1_cs), Some(BindingStrength.R))
        ))
      )
    }

    def combineFailure = {
      implicit val grp: Group = group(List("CCT|CWE|x^^y", "CCT|ST|a^^b", "CCT|TST"))
      val vsB = ValueSetBinding(".", Some(BindingStrength.R), List("vs1_cs"), List(
        BindingLocation("1[1]", Some("3[1]"))
      ))
      val vs = ValueSet("CCT-1 (A)", "2[*]", List(
        vsB
      ))

      val cc =  scc(CoConstraintUsage.R, Range(1, "1"), List(
        PlainText("CCT-1 (B)", "1[*]", "CWE")
      ), List(
        PlainText("CCT-2.1 (B)", "2[*].1[*]", "A"),
        Code("CCT-2 (B)", "2[*]", "A", "SYS1", List(
          CoConstraintBindingLocation(1, "1[*]", "3[*]")
        )),
        vs
      ))
      val sBinding = binding(List(PlainText("CCT-1 (A)", "1[*]", "ST")), List(cc))
      checkGroupAndBindings(grp, List(sBinding._1)) must containTheSameElementsAs(
        createEntry(CoConstraintPlainTextFailure(cc, assertGet(grp, "1[1].2[1].1[1]"), cc.constraints.head.asInstanceOf[PlainText], "x")) :::
        createEntry(CoConstraintCodeFailureNotFound(cc, assertGet(grp, "1[1].2[1].1[1]"), cc.constraints(1).asInstanceOf[Code])) :::
        createEntry(CoConstraintVsBindingFailure(cc, assertGet(grp, "1[1]"), vs,
          VsEntry(assertGet(grp, "1[1].2[1]"), VSValidationCode.CodeNotFound("x", Some("y"), List(vs1_cs)), Some(BindingStrength.R))
        ))
      )
    }

    plainSuccess and plainFailure and codeSuccess and codeFailure and vsBindingSuccess and vsBindingFailure and combineSuccess and combineFailure
  }

  def conditionalPass() = {
    def success = {
      implicit val grp: Group = makeGroupCond(
        List("CCT|CWE|x^^y", "CCT|ST|a^^b", "CCT|TST"),
        "CND|CWE"
      )
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(
        PlainText("CCT-1 (A)", "1[*]", "CWE")
      ), List())
      val binding = conditionalBinding(
        Condition("CONDITION", expression.PlainText("2[1].1[1]", "CWE", true)),
        List(cc),
        Nil
      )
      checkGroupAndBindings(grp, List(binding)) must containTheSameElementsAs(
        Nil
      )
    }

    def failure = {
      implicit val grp: Group = makeGroupCond(
        List("CCT|ST|a^^b", "CCT|TST"),
        "CND|CWE"
      )
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(
        PlainText("CCT-1 (A)", "1[*]", "CWE")
      ), List())
      val binding = conditionalBinding(
        Condition("CONDITION", expression.PlainText("2[1].1[1]", "CWE", true)),
        List(cc),
        Nil
      )
      checkGroupAndBindings(grp, List(binding)) must containTheSameElementsAs(
        createEntry(RequiredCoConstraint(cc))
      )
    }

    success and failure
  }

  def conditionalFail() = {
    def success = {
      implicit val grp: Group = makeGroupCond(
        List("CCT|CWE|x^^y", "CCT|ST|a^^b", "CCT|TST"),
        "CND|ST"
      )
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(
        PlainText("CCT-1 (A)", "1[*]", "CWE")
      ), List())
      val binding = conditionalBinding(
        Condition("CONDITION", expression.PlainText("2[1].1[1]", "CWE", true)),
        List(cc),
        Nil
      )
      checkGroupAndBindings(grp, List(binding)) must containTheSameElementsAs(
        Nil
      )
    }

    def failure = {
      implicit val grp: Group = makeGroupCond(
        List("CCT|ST|a^^b", "CCT|TST"),
        "CND|ST"
      )
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(
        PlainText("CCT-1 (A)", "1[*]", "CWE")
      ), List())
      val binding = conditionalBinding(
        Condition("CONDITION", expression.PlainText("2[1].1[1]", "CWE", true)),
        List(cc),
        Nil
      )
      checkGroupAndBindings(grp, List(binding)) must containTheSameElementsAs(
        Nil
      )
    }

    success and failure
  }

  def conditionalInc() = {
    def success = {
      implicit val grp: Group = makeGroupCond(
        List("CCT|CWE|x^^y", "CCT|ST|a^^b", "CCT|TST"),
        "CND|CWE"
      )
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(
        PlainText("CCT-1 (A)", "1[*]", "CWE")
      ), List())
      val exp = expression.PlainText("2[1].1", "CWE", true)
      val binding = conditionalBinding(
        Condition("CONDITION", exp),
        List(cc),
        Nil
      )
      checkGroupAndBindings(grp, List(binding)) must containTheSameElementsAs(
        createEntry(InconclusiveCondition(grp, "CONDITION", stackTrace(grp, Trace(exp, Reason(grp.location, s"Invalid Path '2[1].1'") :: Nil)::Nil)))
      )
    }

    def failure = {
      implicit val grp: Group = makeGroupCond(
        List("CCT|ST|a^^b", "CCT|TST"),
        "CND|CWE"
      )
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(
        PlainText("CCT-1 (A)", "1[*]", "CWE")
      ), List())
      val exp = expression.PlainText("2[1].1", "CWE", true)
      val binding = conditionalBinding(
        Condition("CONDITION", exp),
        List(cc),
        Nil
      )

      checkGroupAndBindings(grp, List(binding)) must containTheSameElementsAs(
        createEntry(InconclusiveCondition(grp, "CONDITION", stackTrace(grp, Trace(exp, Reason(grp.location, s"Invalid Path '2[1].1'") :: Nil)::Nil)))
      )
    }

    success and failure
  }
}
