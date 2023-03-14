package hl7.v2.validation.coconstraints

import expression.EvalResult.{Reason, Trace}
import hl7.v2.instance.{Group, Simple}
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
          checkBindingContext(grp, sBinding) must containTheSameElementsAs(
            createEntry(RequiredCoConstraint(None, cc))
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
          checkBindingContext(grp, sBinding) must containTheSameElementsAs(
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
          checkBindingContext(grp, sBinding) must containTheSameElementsAs(
            createEntry(CardinalityCoConstraint(None, cc, 1))
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
          checkBindingContext(grp, sBinding) must containTheSameElementsAs(
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
          checkBindingContext(grp, sBinding) must containTheSameElementsAs(
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
          checkBindingContext(grp, sBinding) must containTheSameElementsAs(
            createEntry(CardinalityCoConstraint(None, cc, segments._2))
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
      checkBindingContext(grp, sBinding) must containTheSameElementsAs(Nil)
    }
    def code = {
      implicit val grp: Group = group(List("CCT|CWE|x^^y"))
      val sBinding = binding(List(Code("CCT-1 (A)", "2[*]", "x", "y", List(
        CoConstraintBindingLocation(1, "1[*]", "3[*]")
      ))))
      checkBindingContext(grp, sBinding) must containTheSameElementsAs(Nil)
    }
    def vsBinding = {
      implicit val grp: Group = group(List("CCT|CWE|A^^SYS1"))
      val sBinding = binding(List(ValueSet("CCT-1 (A)", "2[*]", List(
        ValueSetBinding(".", Some(BindingStrength.R), List("vs1_cs"), List(
          BindingLocation("1[1]", Some("3[1]"))
        ))
      ))))
      checkBindingContext(grp, sBinding) must containTheSameElementsAs(Nil)
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
      checkBindingContext(grp, sBinding) must containTheSameElementsAs(Nil)
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
      checkBindingContext(grp, sBinding) must containTheSameElementsAs(
        createEntry(RequiredCoConstraint(None, sBinding.segments.head.bindings.head.coConstraints.head))
      )
    }
    def code = {
      implicit val grp: Group = group(List("CCT|CWE|x^^y"))
      val sBinding = binding(List(Code("CCT-1 (A)", "2[*]", "z", "y", List(
        CoConstraintBindingLocation(1, "1[*]", "3[*]")
      ))))
      checkBindingContext(grp, sBinding) must containTheSameElementsAs(
        createEntry(RequiredCoConstraint(None, sBinding.segments.head.bindings.head.coConstraints.head))
      )
    }
    def vsBinding = {
      implicit val grp: Group = group(List("CCT|CWE|E^^SYS1"))
      val sBinding = binding(List(ValueSet("CCT-1 (A)", "2[*]", List(
        ValueSetBinding(".", Some(BindingStrength.R), List("vs1_cs"), List(
          BindingLocation("1[1]", Some("3[1]"))
        ))
      ))))
      checkBindingContext(grp, sBinding) must containTheSameElementsAs(
        createEntry(RequiredCoConstraint(None, sBinding.segments.head.bindings.head.coConstraints.head))
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
          checkBindingContext(grp, sBinding) must containTheSameElementsAs(
            createEntry(RequiredCoConstraint(None, sBinding.segments.head.bindings.head.coConstraints.head))
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
      checkBindingContext(grp, sBinding) must containTheSameElementsAs(
        createEntry(RequiredCoConstraint(None, sBinding.segments.head.bindings.head.coConstraints.head))
      )
    }
    def code = {
      implicit val grp: Group = group(List("CCT|CWE|x^^y"))
      val sBinding = binding(List(Code("CCT-1 (A)", "2[*]", "z", "y", List(
        CoConstraintBindingLocation(1, "1[*].1[1]", "3[*]")
      ))))
      checkBindingContext(grp, sBinding) must containTheSameElementsAs(
        createEntry(RequiredCoConstraint(None, sBinding.segments.head.bindings.head.coConstraints.head))
      )
    }
    def vsBinding = {
      implicit val grp: Group = group(List("CCT|CWE|E^^SYS1"))
      val sBinding = binding(List(ValueSet("CCT-1 (A)", "2[*]", List(
        ValueSetBinding(".", Some(BindingStrength.R), List("vs1_cs"), List(
          BindingLocation("1[1].1[1]", Some("3[1]"))
        ))
      ))))
      checkBindingContext(grp, sBinding) must containTheSameElementsAs(
        createEntry(RequiredCoConstraint(None, sBinding.segments.head.bindings.head.coConstraints.head))
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
          checkBindingContext(grp, sBinding) must containTheSameElementsAs(
            createEntry(RequiredCoConstraint(None, sBinding.segments.head.bindings.head.coConstraints.head))
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
      checkBindingContext(grp, sBinding) must containTheSameElementsAs(
        createEntry(RequiredCoConstraint(None, sBinding.segments.head.bindings.head.coConstraints.head))
      )
    }
    def code = {
      implicit val grp: Group = group(List("CCT|CWE|"))
      val sBinding = binding(List(Code("CCT-1 (A)", "2[*]", "x", "y", List(
        CoConstraintBindingLocation(1, "1[*]", "3[*]")
      ))))
      checkBindingContext(grp, sBinding) must containTheSameElementsAs(
        createEntry(RequiredCoConstraint(None, sBinding.segments.head.bindings.head.coConstraints.head))
      )
    }
    def vsBinding = {
      implicit val grp: Group = group(List("CCT|CWE|"))
      val sBinding = binding(List(ValueSet("CCT-1 (A)", "2[*]", List(
        ValueSetBinding(".", Some(BindingStrength.R), List("vs1_cs"), List(
          BindingLocation("1[1]", Some("3[1]"))
        ))
      ))))
      checkBindingContext(grp, sBinding) must containTheSameElementsAs(
        createEntry(RequiredCoConstraint(None, sBinding.segments.head.bindings.head.coConstraints.head))
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
          checkBindingContext(grp, sBinding) must containTheSameElementsAs(
            createEntry(RequiredCoConstraint(None, sBinding.segments.head.bindings.head.coConstraints.head))
          )
        }
        )
    }

    Seq(plaint, code, vsBinding) ++ combine
  }

  def ccNoDistinctGroupId() = {
    def binding(selectors: List[CoConstraintCell], gId: GroupId) = {
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), selectors, List())
      (simpleBindingGrouper(List(cc), List(), List(gId)), cc)
    }

    implicit val grp: Group = group(List("CCT|CWE|x^^y|1", "CCT|ST|x^^y|1"))
    val gid = GroupId(1, "3[1]", "CCT-3")
    val (sBinding, cc) = binding(List(PlainText("CCT-1 (A)", "1[*]", "CWE")), gid)
    checkBindingContext(grp, sBinding) must containTheSameElementsAs(
      createEntry(NotDistinctGrouper(cc, CCGroupId(gid, assertGet(grp, "1[1].3[1]"), "1")))
    )
  }

  def ccMatchPlainText() = {
    def binding(selectors: List[CoConstraintCell], cnstr: List[CoConstraintCell]) = {
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), selectors, cnstr)
      simpleBinding(List(cc), List())
    }
    val cell = PlainText("CCT-1 (A)", "3[*]", "A")
    val sBinding = binding(List(PlainText("CCT-1 (A)", "1[*]", "CWE")), List(cell))

    def success() = {
      implicit val grp: Group = group(List("CCT|CWE|x^^y|A"))
      checkBindingContext(grp, sBinding) must containTheSameElementsAs(
        createEntry(CoConstraintPlainTextSuccess(None, sBinding.segments.head.bindings.head.coConstraints.head, assertGet(grp, "1[1].3[1]"), cell))
      )
    }

    def failure() = {
      implicit val grp: Group = group(List("CCT|CWE|x^^y|B"))
      checkBindingContext(grp, sBinding) must containTheSameElementsAs(
        createEntry(CoConstraintPlainTextFailure(None, sBinding.segments.head.bindings.head.coConstraints.head, assertGet(grp, "1[1].3[1]"), cell, "B"))
      )
    }
    success() and failure()
  }

  def ccMatchFailCode() = {
    def binding(selectors: List[CoConstraintCell], cnstr: List[CoConstraintCell]) = {
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), selectors, cnstr)
      simpleBinding(List(cc), List())
    }
    val cell = Code("CCT-1 (A)", "2[*]", "x", "y", List(
      CoConstraintBindingLocation(1, "1[*]", "3[*]")
    ))
    val sBinding = binding(List(PlainText("CCT-1 (A)", "1[*]", "CWE")), List(cell))

    def success() = {
      implicit val grp: Group = group(List("CCT|CWE|x^^y|A"))
      checkBindingContext(grp, sBinding) must containTheSameElementsAs(
        createEntry(CoConstraintCodeSuccess(None, sBinding.segments.head.bindings.head.coConstraints.head, assertGet(grp, "1[1].2[1]"), cell))
      )
    }

    def failureCode() = {
      implicit val grp: Group = group(List("CCT|CWE|z^^y|B"))
      checkBindingContext(grp, sBinding) must containTheSameElementsAs(
        createEntry(CoConstraintCodeFailureNotFound(None, sBinding.segments.head.bindings.head.coConstraints.head, assertGet(grp, "1[1].2[1].1[1]").asInstanceOf[Simple], cell))
      )
    }

    def failureCodeSys() = {
      implicit val grp: Group = group(List("CCT|CWE|x^^z|B"))
      checkBindingContext(grp, sBinding) must containTheSameElementsAs(
        createEntry(CoConstraintCodeFailureInvalidCs(None, sBinding.segments.head.bindings.head.coConstraints.head, assertGet(grp, "1[1].2[1].3[1]"), cell, "z"))
      )
    }

    def failureCodeSysNf() = {
      implicit val grp: Group = group(List("CCT|CWE|x^^|B"))
      checkBindingContext(grp, sBinding) must containTheSameElementsAs(
        createEntry(CoConstraintCodeFailureNotFoundCs(None, sBinding.segments.head.bindings.head.coConstraints.head, assertGet(grp, "1[1].2[1]"), cell, CoConstraintBindingLocation(1, "1[*]", "3[*]")))
      )
    }

    success() and failureCode() and failureCodeSys() and failureCodeSysNf()
  }

  def ccMatchFailVs() = {
    def binding(selectors: List[CoConstraintCell], cnstr: List[CoConstraintCell]) = {
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), selectors, cnstr)
      simpleBinding(List(cc), List())
    }
    val vb = ValueSetBinding(".", Some(BindingStrength.R), List("vs1_cs"), List(
      BindingLocation("1[1]", Some("3[1]")),
    ))
    val cell = ValueSet("CCT-2 (B)", "2[*]", List(vb))
    val sBinding = binding(List(PlainText("CCT-1 (A)", "1[*]", "CWE")), List(cell))

    def success() = {
      implicit val grp: Group = group(List("CCT|CWE|A^^SYS1|A"))
      checkBindingContext(grp, sBinding) must containTheSameElementsAs(
        createEntry(
          CoConstraintVsBindingSuccess(None, sBinding.segments.head.bindings.head.coConstraints.head, assertGet(grp, "1[1].2[1]"), cell, vb,
            VsEntry(assertGet(grp, "1[1].2[1]"), VSValidationCode.RVS(VCode("A", "des", CodeUsage.R, "SYS1"), "A", vs1_cs), Some(BindingStrength.R))
          )
        )
      )
    }

    def failure() = {
      implicit val grp: Group = group(List("CCT|CWE|X^^SYS1|A"))
      checkBindingContext(grp, sBinding) must containTheSameElementsAs(
        createEntry(
          CoConstraintVsBindingFailure(None, sBinding.segments.head.bindings.head.coConstraints.head, assertGet(grp, "1[1]"), cell,
            VsEntry(assertGet(grp, "1[1].2[1]"), VSValidationCode.CodeNotFound("X", Some("SYS1"), List(vs1_cs)), Some(BindingStrength.R)),
            vb
          )
        )
      )
    }

    success() and failure()
  }

  def requiredGrp() = {
    val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(
      PlainText("CCT-1 (A)", "1[*]", "CWE")
    ), List())
    val ccg = grp("GRP", CoConstraintUsage.R, Range(1, "1"), cc, List())
    val sBinding = simpleBinding(List(), List(ccg), List(GroupId(1, "3[1]", "CCT-3")))

    def failure = {
      Seq(
        // One Segment
        List("CCT|ST|x^^y|1"),
        // Multi Segment
        List("CCT|ST|x^^y|1", "CCT|ST|x^^y|1")
      ) map (
        segments => {
          implicit val grp: Group = group(segments)
          checkBindingContext(grp, sBinding) must containTheSameElementsAs(
            createEntry(RequiredGroup(ccg))
          )
        })
    }

    def success = {
      Seq(
        // One Segment
        List("CCT|CWE|x^^y|1"),
        // Multi Segment
        List("CCT|ST|x^^y|1", "CCT|CWE|x^^y|1")
      ) map (
        segments => {
          implicit val grp: Group = group(segments)
          checkBindingContext(grp, sBinding) must containTheSameElementsAs(
            Nil
          )
        })
    }
    failure ++ success
  }

  def minCardinalityGrp() = {
    val cc = scc(CoConstraintUsage.R, Range(1, "*"), List(
      PlainText("CCT-1 (A)", "1[*]", "CWE")
    ), List())
    val ccg = grp("GRP", CoConstraintUsage.R, Range(2, "*"), cc, List())
    val sBinding = simpleBinding(List(), List(ccg), List(GroupId(1, "3[1]", "CCT-3")))

    def failure = {
      Seq(
        // One Segment, One match
        List("CCT|CWE|x^^y|1"),
        // Multi Segment, One match
        List("CCT|ST|x^^y|1", "CCT|CWE|x^^y|1"),
        // Multi Segment, Multi match, Same GroupId
        List("CCT|CWE|x^^y|1", "CCT|CWE|x^^y|1")
      ) map (
        segments => {
          implicit val grp: Group = group(segments)
          checkBindingContext(grp, sBinding) must containTheSameElementsAs(
            createEntry(CardinalityGroup(ccg, 1, "CCT-3", List("1").mkString("[", ",", "]")))
          )
        })
    }

    def success = {
      Seq(
        // Two Segment, Two match
        List("CCT|CWE|x^^y|1", "CCT|CWE|z^^t|2"),
        // Multi Segment, Two match
        List("CCT|ST|x^^y|1", "CCT|CWE|x^^y|1", "CCT|CWE|z^^t|2"),
        // X Segment, X match
        List("CCT|CWE|x^^y|1", "CCT|CWE|z^^t|2", "CCT|CWE|e^^f|3"),
        // Multi Segment, Multi match
        List("CCT|ST|x^^y|1", "CCT|CWE|x^^y|1", "CCT|CWE|z^^t|2", "CCT|CWE|e^^f|3")
      ) map (
        segments => {
          implicit val grp: Group = group(segments)
          checkBindingContext(grp, sBinding) must containTheSameElementsAs(
            Nil
          )
        })
    }
    failure ++ success
  }

  def maxCardinalityGrp() = {
    val cc = scc(CoConstraintUsage.R, Range(1, "*"), List(
      PlainText("CCT-1 (A)", "1[*]", "CWE")
    ), List())
    val ccg = grp("GRP", CoConstraintUsage.R, Range(1, "1"), cc, List())
    val sBinding = simpleBinding(List(), List(ccg), List(GroupId(1, "3[1]", "CCT-3")))

    def success = {
      Seq(
        // One Segment, One match
        List("CCT|CWE|x^^y|1"),
        // Multi Segment, One match
        List("CCT|ST|x^^y|1", "CCT|CWE|x^^y|2"),
        // Multi Segment, Multi match, Same group Id
        List("CCT|CWE|x^^y|1", "CCT|CWE|x^^y|1")
      ) map (
        segments => {
          implicit val grp: Group = group(segments)
          checkBindingContext(grp, sBinding) must containTheSameElementsAs(
            Nil
          )
        })
    }

    def failure = {
      Seq(
        // Two Segment, Two match
        (List("CCT|CWE|x^^y|1", "CCT|CWE|z^^t|2"), 2, List("1", "2")),
        // Multi Segment, Two match
        (List("CCT|ST|x^^y|1", "CCT|CWE|x^^y|1", "CCT|CWE|z^^t|2"), 2, List("1", "2")),
        // X Segment, X match
        (List("CCT|CWE|x^^y|1", "CCT|CWE|z^^t|2", "CCT|CWE|e^^f|3"), 3, List("1", "2", "3")),
        // Multi Segment, Multi match
        (List("CCT|ST|x^^y|1", "CCT|CWE|x^^y|1", "CCT|CWE|z^^t|2", "CCT|CWE|e^^f|3"), 3, List("1", "2", "3"))
      ) map (
        segments => {
          implicit val grp: Group = group(segments._1)
          checkBindingContext(grp, sBinding) must containTheSameElementsAs(
            createEntry(CardinalityGroup(ccg, segments._2, "CCT-3", segments._3.sorted.mkString("[", ",", "]")))
          )
        })
    }
    failure ++ success
  }

  def grpMatch() = {
    def binding(selectors: List[CoConstraintCell]) = {
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), selectors, List())
      val ccg = grp("GRP", CoConstraintUsage.R, Range(1, "1"), cc, List())
      val sBinding = simpleBinding(List(), List(ccg), List(GroupId(1, "3[1]", "CCT-3")))
      (sBinding, ccg, cc)
    }

    def plaint = {
      implicit val grp: Group = group(List("CCT|CWE|x^^y|1"))
      val sBinding = binding(List(PlainText("CCT-1 (A)", "1[*]", "CWE")))
      checkBindingContext(grp, sBinding._1) must containTheSameElementsAs(Nil)
    }
    def code = {
      implicit val grp: Group = group(List("CCT|CWE|x^^y|1"))
      val sBinding = binding(List(Code("CCT-1 (A)", "2[*]", "x", "y", List(
        CoConstraintBindingLocation(1, "1[*]", "3[*]")
      ))))
      checkBindingContext(grp, sBinding._1) must containTheSameElementsAs(Nil)
    }
    def vsBinding = {
      implicit val grp: Group = group(List("CCT|CWE|A^^SYS1|1"))
      val sBinding = binding(List(ValueSet("CCT-1 (A)", "2[*]", List(
        ValueSetBinding(".", Some(BindingStrength.R), List("vs1_cs"), List(
          BindingLocation("1[1]", Some("3[1]"))
        ))
      ))))
      checkBindingContext(grp, sBinding._1) must containTheSameElementsAs(Nil)
    }
    def combine = {
      implicit val grp: Group = group(List("CCT|CWE|x^^y|1|A^^SYS1"))
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
      checkBindingContext(grp, sBinding._1) must containTheSameElementsAs(Nil)
    }

    plaint and code and vsBinding and combine
  }

  def grpNoMatchFailed() = {

    def binding(selectors: List[CoConstraintCell]) = {
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), selectors, List())
      val ccg = grp("GRP", CoConstraintUsage.R, Range(1, "1"), cc, List())
      val sBinding = simpleBinding(List(), List(ccg), List(GroupId(1, "3[1]", "CCT-3")))
      (sBinding, ccg, cc)
    }

    def plaint = {
      implicit val grp: Group = group(List("CCT|CWE|x^^y|1"))
      val sBinding = binding(List(PlainText("CCT-1 (A)", "1[*]", "ST")))
      checkBindingContext(grp, sBinding._1) must containTheSameElementsAs(
        createEntry(RequiredGroup(sBinding._2))
      )
    }
    def code = {
      implicit val grp: Group = group(List("CCT|CWE|x^^y|1"))
      val sBinding = binding(List(Code("CCT-1 (A)", "2[*]", "z", "y", List(
        CoConstraintBindingLocation(1, "1[*]", "3[*]")
      ))))
      checkBindingContext(grp, sBinding._1) must containTheSameElementsAs(
        createEntry(RequiredGroup(sBinding._2))
      )
    }
    def vsBinding = {
      implicit val grp: Group = group(List("CCT|CWE|E^^SYS1|1"))
      val sBinding = binding(List(ValueSet("CCT-1 (A)", "2[*]", List(
        ValueSetBinding(".", Some(BindingStrength.R), List("vs1_cs"), List(
          BindingLocation("1[1]", Some("3[1]"))
        ))
      ))))
      checkBindingContext(grp, sBinding._1) must containTheSameElementsAs(
        createEntry(RequiredGroup(sBinding._2))
      )
    }
    def combine = {
      Seq(
        //PlainText Fail
        List("CCT|ST|x^^y|1|A^^SYS1"),
        //Code Fail
        List("CCT|CWE|z^^y|1|A^^SYS1"),
        //Vs Binding Fail
        List("CCT|CWE|x^^y|1|E^^SYS1"),
        //All Fail
        List("CCT|ST|z^^y|1|E^^SYS1")
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
          checkBindingContext(grp, sBinding._1) must containTheSameElementsAs(
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
      val sBinding = simpleBinding(List(), List(ccg), List(GroupId(1, "3[1]", "CCT-3")))
      (sBinding, ccg, cc)
    }

    def plaint = {
      implicit val grp: Group = group(List("CCT|CWE|x^^y|1"))
      val sBinding = binding(List(PlainText("CCT-1 (A)", "1", "ST")))
      checkBindingContext(grp, sBinding._1) must containTheSameElementsAs(
        createEntry(RequiredGroup(sBinding._2))
      )
    }
    def code = {
      implicit val grp: Group = group(List("CCT|CWE|x^^y|1"))
      val sBinding = binding(List(Code("CCT-1 (A)", "2[*]", "z", "y", List(
        CoConstraintBindingLocation(1, "1[*].1[1]", "3[*]")
      ))))
      checkBindingContext(grp, sBinding._1) must containTheSameElementsAs(
        createEntry(RequiredGroup(sBinding._2))
      )
    }
    def vsBinding = {
      implicit val grp: Group = group(List("CCT|CWE|E^^SYS1|1"))
      val sBinding = binding(List(ValueSet("CCT-1 (A)", "2[*]", List(
        ValueSetBinding(".", Some(BindingStrength.R), List("vs1_cs"), List(
          BindingLocation("1[1].1[1]", Some("3[1]"))
        ))
      ))))
      checkBindingContext(grp, sBinding._1) must containTheSameElementsAs(
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
        (List("CCT|CWE|x^^y|1|A^^SYS1"),
          Map("PT" -> PlainText("CCT-1 (A)", "1", "ST"))),
        //Code Fail
        (List("CCT|CWE|x^^y|1|A^^SYS1"),
          Map("CD" -> Code("CCT-1 (A)", "2[*]", "z", "y", List(
            CoConstraintBindingLocation(1, "1[*].1[1]", "3[*]")
          )))),
        //Vs Binding Fail
        (List("CCT|CWE|x^^y|1|A^^SYS1"),
          Map("VS" -> ValueSet("CCT-3 (C)", "4[*]", List(
            ValueSetBinding(".", Some(BindingStrength.R), List("vs1_cs"), List(
              BindingLocation("1[1].1[1]", Some("3[1]"))
            ))
          )))),
        //All Fail
        (List("CCT|CWE|x^^y|1|A^^SYS1"),
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
          checkBindingContext(grp, sBinding._1) must containTheSameElementsAs(
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
      val sBinding = simpleBinding(List(), List(ccg), List(GroupId(1, "3[1]", "CCT-3")))
      (sBinding, ccg, cc)
    }

    def plaint = {
      implicit val grp: Group = group(List("CCT||x^^y|1"))
      val sBinding = binding(List(PlainText("CCT-1 (A)", "1[*]", "CWE")))
      checkBindingContext(grp, sBinding._1) must containTheSameElementsAs(
        createEntry(RequiredGroup(sBinding._2))
      )
    }
    def code = {
      implicit val grp: Group = group(List("CCT|CWE||1"))
      val sBinding = binding(List(Code("CCT-1 (A)", "2[*]", "x", "y", List(
        CoConstraintBindingLocation(1, "1[*]", "3[*]")
      ))))
      checkBindingContext(grp, sBinding._1) must containTheSameElementsAs(
        createEntry(RequiredGroup(sBinding._2))
      )
    }
    def vsBinding = {
      implicit val grp: Group = group(List("CCT|CWE||1"))
      val sBinding = binding(List(ValueSet("CCT-1 (A)", "2[*]", List(
        ValueSetBinding(".", Some(BindingStrength.R), List("vs1_cs"), List(
          BindingLocation("1[1]", Some("3[1]"))
        ))
      ))))
      checkBindingContext(grp, sBinding._1) must containTheSameElementsAs(
        createEntry(RequiredGroup(sBinding._2))
      )
    }
    def combine = {
      Seq(
        //PlainText Fail
        List("CCT||x^^y|1|A^^SYS1"),
        //Code Fail
        List("CCT|CWE||1|A^^SYS1"),
        //Vs Binding Fail
        List("CCT|CWE|x^^y|1|"),
        //All Fail
        List("CCT|||1|")
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
          checkBindingContext(grp, sBinding._1) must containTheSameElementsAs(
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
      val sBinding = simpleBinding(List(), List(ccg), List(GroupId(1, "3[1]", "CCT-3")))
      (sBinding, ccg, cc)
    }

    def groupInstance(id: String): Option[GroupInstance] = {
      Some(GroupInstance("GRPIE", "CCT-3",id))
    }

    def plainSuccess = {
      implicit val grp: Group = group(List("CCT|CWE|x^^y|1", "CCT|ST|a^^b|1", "CCT|TST||1"))
      val cc =  scc(CoConstraintUsage.R, Range(1, "1"), List(
        PlainText("CCT-1 (B)", "1[*]", "ST")
      ), List(
        PlainText("CCT-2.1 (B)", "2[*].1[*]", "a")
      ))
      val sBinding = binding(List(PlainText("CCT-1 (A)", "1[*]", "CWE")), List(cc))
      checkBindingContext(grp, sBinding._1) must containTheSameElementsAs(
        createEntry(CoConstraintPlainTextSuccess(groupInstance("1"), cc, assertGet(grp, "1[2].2[1].1[1]"), cc.constraints.head.asInstanceOf[PlainText]))
      )
    }

    def plainFailure = {
      implicit val grp: Group = group(List("CCT|CWE|x^^y|1", "CCT|ST|a^^b|1", "CCT|TST||1"))
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(
        PlainText("CCT-1 (B)", "1[*]", "ST")
      ), List(
        PlainText("CCT-2.1 (B)", "2[*].1[*]", "x")
      ))
      val sBinding = binding(List(PlainText("CCT-1 (A)", "1[*]", "CWE")), List(cc))
      checkBindingContext(grp, sBinding._1) must containTheSameElementsAs(
        createEntry(CoConstraintPlainTextFailure(groupInstance("1"), cc, assertGet(grp, "1[2].2[1].1[1]"), cc.constraints.head.asInstanceOf[PlainText], "a"))
      )
    }

    def codeSuccess = {
      implicit val grp: Group = group(List("CCT|CWE|x^^y|1", "CCT|ST|a^^b|1", "CCT|TST||1"))
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(
        PlainText("CCT-1 (A)", "1[*]", "CWE")
      ), List(
        Code("CCT-2 (B)", "2[*]", "x", "y", List(
          CoConstraintBindingLocation(1, "1[*]", "3[*]")
        ))
      ))
      val sBinding = binding(List(PlainText("CCT-1 (A)", "1[*]", "ST")), List(cc))
      checkBindingContext(grp, sBinding._1) must containTheSameElementsAs(
        createEntry(CoConstraintCodeSuccess(groupInstance("1"), cc, assertGet(grp, "1[1].2[1]"), cc.constraints.head.asInstanceOf[Code]))
      )
    }

    def codeFailure = {
      implicit val grp: Group = group(List("CCT|CWE|z^^y|1", "CCT|ST|a^^b|1", "CCT|TST||1"))
      val cc =  scc(CoConstraintUsage.R, Range(1, "1"), List(
        PlainText("CCT-1 (A)", "1[*]", "CWE")
      ), List(
        Code("CCT-2 (B)", "2[*]", "x", "y", List(
          CoConstraintBindingLocation(1, "1[*]", "3[*]")
        ))
      ))
      val sBinding = binding(List(PlainText("CCT-1 (A)", "1[*]", "ST")), List(cc))
      checkBindingContext(grp, sBinding._1) must containTheSameElementsAs(
        createEntry(CoConstraintCodeFailureNotFound(groupInstance("1"), cc, assertGet(grp, "1[1].2[1].1[1]").asInstanceOf[Simple], cc.constraints.head.asInstanceOf[Code]))
      )
    }

    def vsBindingSuccess = {
      implicit val grp: Group = group(List("CCT|CWE|A^^SYS1|1", "CCT|ST|a^^b|1", "CCT|TST||1"))
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
      checkBindingContext(grp, sBinding._1) must containTheSameElementsAs(
        createEntry(CoConstraintVsBindingSuccess(groupInstance("1"), cc, assertGet(grp, "1[1].2[1]"), vs, vsB,
          VsEntry(assertGet(grp, "1[1].2[1]"), VSValidationCode.RVS(VCode("A", "des", CodeUsage.R, "SYS1"), "A", vs1_cs), Some(BindingStrength.R))
        ))
      )
    }

    def vsBindingFailure = {
      implicit val grp: Group = group(List("CCT|CWE|X^^SYS1|1", "CCT|ST|a^^b|1", "CCT|TST||1"))
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
      checkBindingContext(grp, sBinding._1) must containTheSameElementsAs(
        createEntry(CoConstraintVsBindingFailure(groupInstance("1"), cc, assertGet(grp, "1[1]"), vs,
          VsEntry(assertGet(grp, "1[1].2[1]"), VSValidationCode.CodeNotFound("X", Some("SYS1"), List(vs1_cs)), Some(BindingStrength.R)),
          vsB
        ))
      )
    }

    def combineSuccess = {
      implicit val grp: Group = group(List("CCT|CWE|A^^SYS1|1", "CCT|ST|a^^b|1", "CCT|TST||1"))
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
      checkBindingContext(grp, sBinding._1) must containTheSameElementsAs(
        createEntry(CoConstraintPlainTextSuccess(groupInstance("1"), cc, assertGet(grp, "1[1].2[1].1[1]"), cc.constraints.head.asInstanceOf[PlainText])) :::
        createEntry(CoConstraintCodeSuccess(groupInstance("1"), cc, assertGet(grp, "1[1].2[1]"), cc.constraints(1).asInstanceOf[Code])) :::
        createEntry(CoConstraintVsBindingSuccess(groupInstance("1"), cc, assertGet(grp, "1[1].2[1]"), vs, vsB,
          VsEntry(assertGet(grp, "1[1].2[1]"), VSValidationCode.RVS(VCode("A", "des", CodeUsage.R, "SYS1"), "A", vs1_cs), Some(BindingStrength.R))
        ))
      )
    }

    def combineFailure = {
      implicit val grp: Group = group(List("CCT|CWE|x^^y|1", "CCT|ST|a^^b|1", "CCT|TST||1"))
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
      checkBindingContext(grp, sBinding._1) must containTheSameElementsAs(
        createEntry(CoConstraintPlainTextFailure(groupInstance("1"), cc, assertGet(grp, "1[1].2[1].1[1]"), cc.constraints.head.asInstanceOf[PlainText], "x")) :::
        createEntry(CoConstraintCodeFailureNotFound(groupInstance("1"), cc, assertGet(grp, "1[1].2[1].1[1]").asInstanceOf[Simple], cc.constraints(1).asInstanceOf[Code])) :::
        createEntry(CoConstraintVsBindingFailure(groupInstance("1"), cc, assertGet(grp, "1[1]"), vs,
          VsEntry(assertGet(grp, "1[1].2[1]"), VSValidationCode.CodeNotFound("x", Some("y"), List(vs1_cs)), Some(BindingStrength.R)),
          vsB
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
      checkBindingContext(grp, binding) must containTheSameElementsAs(
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
      checkBindingContext(grp, binding) must containTheSameElementsAs(
        createEntry(RequiredCoConstraint(None, cc))
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
      checkBindingContext(grp, binding) must containTheSameElementsAs(
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
      checkBindingContext(grp, binding) must containTheSameElementsAs(
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
      checkBindingContext(grp, binding) must containTheSameElementsAs(
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

      checkBindingContext(grp, binding) must containTheSameElementsAs(
        createEntry(InconclusiveCondition(grp, "CONDITION", stackTrace(grp, Trace(exp, Reason(grp.location, s"Invalid Path '2[1].1'") :: Nil)::Nil)))
      )
    }

    success and failure
  }

  def groupIdMatch() = {
    def binding(primary: List[CoConstraintCell], secondary: List[CoConstraintCell]) = {
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), primary, List())
      val ccs = scc(CoConstraintUsage.R, Range(1, "1"), secondary, List())
      val ccg = grp("GRP", CoConstraintUsage.R, Range(1, "1"), cc, List(ccs))
      val sBinding = simpleBinding(List(), List(ccg), List(GroupId(1, "3[1]", "CCT-3")))
      (sBinding, ccg, cc)
    }

    implicit val g: Group = group(List("CCT|CWE|x^^y|1", "CCT|ST|x^^y|1"))
    val sBinding = binding(List(PlainText("CCT-1 (A)", "1[*]", "CWE")), List(PlainText("CCT-1 (A)", "1[*]", "ST")))
    checkBindingContext(g, sBinding._1) must containTheSameElementsAs(Nil)
  }

  def groupIdNoMatch() = {
    def binding(primary: List[CoConstraintCell], secondary: List[CoConstraintCell]) = {
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), primary, List())
      val ccs = scc(CoConstraintUsage.R, Range(1, "1"), secondary, List())
      val ccg = grp("GRP", CoConstraintUsage.R, Range(1, "1"), cc, List(ccs))
      val sBinding = simpleBinding(List(), List(ccg), List(GroupId(1, "3[1]", "CCT-3")))
      (sBinding, ccg, cc, ccs)
    }

    def groupInstance(id: String): Option[GroupInstance] = {
      Some(GroupInstance("GRP", "CCT-3",id))
    }

    implicit val g: Group = group(List("CCT|CWE|x^^y|1", "CCT|ST|x^^y|2"))
    val sBinding = binding(List(PlainText("CCT-1 (A)", "1[*]", "CWE")), List(PlainText("CCT-1 (A)", "1[*]", "ST")))
    checkBindingContext(g, sBinding._1) must containTheSameElementsAs(
      createEntry(RequiredCoConstraint(groupInstance("1"), sBinding._4))
    )
  }

  def specErrTargetPathNoSeg() = {
    val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(
      PlainText("CCT-1 (A)", "1[*]", "CWE")
    ), List())

    def binding = CoConstraintBindingContext("GRP", ".", List(
      CoConstraintBindingSegment("CCT", "1[*].1[*]", List(
        SimpleCoConstraintTable(Nil, List(cc), List())
      ))
    ))


    implicit val grp: Group = group(List("CCT|ST~CWE|x^^y|"))
    checkBindingContext(grp, binding) must containTheSameElementsAs(
        createEntry(ElementIsNotSegment("1[*].1[*]", assertGet(grp, "1[*].1[1]"))) :::
        createEntry(ElementIsNotSegment("1[*].1[*]", assertGet(grp, "1[*].1[2]"))) :::
        createEntry(RequiredCoConstraint(None, cc))
    )
  }

  def specErrTargetPathInvalid() = {
    val cc = scc(CoConstraintUsage.R, Range(1, "1"), List(
      PlainText("CCT-1 (A)", "1[*]", "CWE")
    ), List())

    def binding = CoConstraintBindingContext("GRP", ".", List(
      CoConstraintBindingSegment("CCT", "1", List(
        SimpleCoConstraintTable(Nil, List(cc), List())
      ))
    ))

    implicit val grp: Group = group(List("CCT|ST~CWE|x^^y|"))
    checkBindingContext(grp, binding) must containTheSameElementsAs(
      createEntry(InconclusiveTarget(grp, "1", s"Invalid Path '1'"))
    )
  }

  def specErrCondInconclusive() = {
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
    checkBindingContext(grp, binding) must containTheSameElementsAs(
      createEntry(InconclusiveCondition(grp, "CONDITION", stackTrace(grp, Trace(exp, Reason(grp.location, s"Invalid Path '1'") :: Nil ) :: Nil)))
    )
  }

  def specErrGroupIdInconclusive() = {
    def binding(primary: List[CoConstraintCell], secondary: List[CoConstraintCell]) = {
      val cc = scc(CoConstraintUsage.R, Range(1, "1"), primary, List())
      val ccs = scc(CoConstraintUsage.R, Range(1, "1"), secondary, List())
      val ccg = grp("GRP", CoConstraintUsage.R, Range(1, "1"), cc, List(ccs))
      val sBinding = simpleBinding(List(), List(ccg), List(GroupId(1, "3", "CCT-3")))
      (sBinding, ccg, cc)
    }

    implicit val g: Group = group(List("CCT|CWE|x^^y|1", "CCT|ST|x^^y|1"))
    val sBinding = binding(List(PlainText("CCT-1 (A)", "1[*]", "CWE")), List(PlainText("CCT-1 (A)", "1[*]", "ST")))
    checkBindingContext(g, sBinding._1) must containTheSameElementsAs(
      createEntry(InconclusiveGrouper(assertGet(g, "1[1]"), "3", s"Invalid Path '3'")) :::
      createEntry(InconclusiveGrouper(assertGet(g, "1[2]"), "3", s"Invalid Path '3'")) :::
      createEntry(RequiredGroup(sBinding._2))
    )
  }
}
