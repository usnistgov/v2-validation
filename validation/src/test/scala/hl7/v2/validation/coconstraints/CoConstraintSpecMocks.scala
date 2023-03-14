package hl7.v2.validation.coconstraints

import expression.EvalResult.Trace
import hl7.v2.instance.{Counter, Element, Group, Separators, TimeZone, Segment => SM}
import hl7.v2.instance.Query.query
import hl7.v2.profile.{Component, Composite, DynMapping, Field, Primitive, Range, Req, Segment, SegmentRef, Usage, Varies, Group => GM}
import hl7.v2.validation.vs.{CodeUsage, EmptyValueSetSpecification, InternalValueSet, ValueSetLibraryImpl, ValueSetSpecification, Code => VCode}
import java.util.{List => JList}
import gov.nist.validation.report.{Trace => GTrace}

import scala.jdk.CollectionConverters.SeqHasAsJava

trait CoConstraintSpecMocks {

  val vsSpecification: ValueSetSpecification = EmptyValueSetSpecification
  val coConstraintValidationContext: CoConstraintValidationContext = EmptyCcContext
  val vs1_cs: InternalValueSet = InternalValueSet("vs1_cs", None, None, List(VCode("A", "des", CodeUsage.R, "SYS1"),VCode("B", "des", CodeUsage.P, "SYS1"),VCode("C", "des", CodeUsage.E, "SYS1")))
  val vs1_cs_1: InternalValueSet = InternalValueSet("vs1_cs_1", None, None, List(VCode("A", "des", CodeUsage.R, "SYS2"),VCode("B", "des", CodeUsage.P, "SYS2"),VCode("C", "des", CodeUsage.E, "SYS2")))
  val vs1_1: InternalValueSet = InternalValueSet("vs1_1", None, None, List(VCode("D", "des", CodeUsage.R, "SYS"),VCode("E", "des", CodeUsage.P, "SYS"),VCode("F", "des", CodeUsage.E, "SYS")))
  val vs1_1_cs: InternalValueSet = InternalValueSet("vs1_1_cs", None, None, List(VCode("D", "des", CodeUsage.R, "SYS1"),VCode("E", "des", CodeUsage.P, "SYS1"),VCode("F", "des", CodeUsage.E, "SYS1")))
  val valueSetLibrary = new ValueSetLibraryImpl(List("excluded"), (vs1_1::vs1_cs::vs1_cs_1::vs1_1_cs::Nil).map(x => { (x.id, x) }).toMap)
  implicit val separators = Separators( '|', '^', '~', '\\', '&', Some('#') )
  implicit val time: Option[TimeZone] = None

  def req(p: Int) = Req(p, "desc", Usage.O, None, None, None, Nil, None, false, None)
  def fields = Map(
    "A" -> Field("A", ST, req(1)),
    "B" -> Field("B", CWE, req(2)),
    "C" -> Field("C", ST, req(3)) ,
    "D" -> Field("D", CWE, req(4)),
    "E" -> Field("E", VARIES, req(5))
  )
  def comps = Map(
    "C1" -> Component("C1", ST, req(1)),
    "C2" -> Component("C2", ST, req(2)),
    "C3" -> Component("C3", ST, req(3)),
    "C4" -> Component("C4", ST, req(4)),
    "C5" -> Component("C5", ST, req(5)),
    "C6" -> Component("C6", ST, req(6)),
  )
  def dynM = List(
    DynMapping(5, Some("1"), None, Map(
      (Some("ST"), None) -> ST,
      (Some("CWE"), None) -> CWE
    ))
  )
  def groupMsg = GM("MSG", "MSG", List(groupRef), req(1))
  def groupRef = GM("GRP", "GRP", List(segRef), req(1))
  def segRef = SegmentRef(req(1), segmentProfile)
  def segCondRef = SegmentRef(req(2), segmentCond)
  def groupCond = GM("GRP_CND", "GRP_CND", List(segRef, segCondRef), req(1))
  def segmentCond = Segment("CC_COND_TEST", "CND","CO-CONSTRAINT CONDITIONAL TEST", fields.values.toList.sortBy(_.req.position), dynM)
  def segmentProfile = Segment("CC_TEST", "CCT","CO-CONSTRAINT TEST", fields.values.toList.sortBy(_.req.position), dynM)
  def CWE = Composite("CWE", "CWE", "Coded Element", "2.5", comps.values.toList.sortBy(_.req.position))
  def ST = Primitive("ST", "ST", "String", "2.5")
  def VARIES = Varies("VAR", "ST", "String", "2.5", Some("1"), None)
  implicit val segmentName = "CCT"

  def assertGet(e: Element, path: String): Element = {
    val element = query(e, path).toOption.map(_.head).orNull
    assert(element != null)
    element
  }

  def simpleBinding(cc: List[CoConstraint], ccg: List[CoConstraintGroup]) = CoConstraintBindingContext("GRP", ".", List(
    CoConstraintBindingSegment("CCT", "1[*]", List(
      SimpleCoConstraintTable(Nil, cc, ccg)
    ))
  ))

  def simpleBindingGrouper(cc: List[CoConstraint], ccg: List[CoConstraintGroup], grpId: List[GroupId]) = CoConstraintBindingContext("GRP", ".", List(
    CoConstraintBindingSegment("CCT", "1[*]", List(
      SimpleCoConstraintTable(grpId, cc, ccg)
    ))
  ))

  def simpleBinding(cc: List[CoConstraint], ccg: List[CoConstraintGroup], groupId: List[GroupId]) = CoConstraintBindingContext("GRP", ".", List(
    CoConstraintBindingSegment("CCT", "1[*]", List(
      SimpleCoConstraintTable(groupId, cc, ccg)
    ))
  ))

  def conditionalBinding(condition: Condition, cc: List[CoConstraint], ccg: List[CoConstraintGroup]) = CoConstraintBindingContext("GRP", ".", List(
    CoConstraintBindingSegment("CCT", "1[*]", List(
      ConditionalCoConstraintTable(condition, Nil, cc, ccg)
    ))
  ))

  def scc(usage: CoConstraintUsage, cardinality: Range, selectors: List[CoConstraintCell], constraints: List[CoConstraintCell]) = CoConstraint(
    CoConstraintRequirement(usage, cardinality),
    selectors,
    constraints
  )

  def grp(name: String, usage: CoConstraintUsage, cardinality: Range, primary: CoConstraint, ccs: List[CoConstraint]) = CoConstraintGroup(
    name,
    CoConstraintRequirement(usage, cardinality),
    primary,
    ccs
  )

  def group(vs: List[String]) = Group(groupRef, 1, segments(vs))
  def makeGroupCond(vs: List[String], cond: String) = {
    val cct = segments(vs)
    implicit val ctr: Counter = Counter(scala.collection.mutable.Map[String, Int]())
    val cnd = SM(segCondRef, cond, 1, -1)
    Group(groupCond, 1, cct:::List(cnd))
  }

  def segments(vs: List[String]) = {
    vs.zipWithIndex.map((v) => {
      segment(v._1, v._2 + 1)
    })
  }
  def segment(v: String, i: Int) = {
    implicit val ctr = Counter(scala.collection.mutable.Map[String, Int]())
    SM(segRef, v, i, -1)
  }

  def stackTrace(context: Element, stack: List[Trace]): JList[GTrace] =
    (stack map { t =>
        val assertion = expression.AsString.expression(t.expression, context)
        val reasons = t.reasons map { r =>
          s"[${r.location.line}, ${r.location.column}] ${r.message}"
        }
        new GTrace(assertion, reasons.asJava)
      }).asJava
}
