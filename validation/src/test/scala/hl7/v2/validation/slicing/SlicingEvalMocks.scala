package hl7.v2.validation.slicing
import hl7.v2.instance.{Counter, Separators, TimeZone, Segment => SI}
import hl7.v2.profile.{Component, Composite, Field, Message, Primitive, Req, Segment, SegmentRef, Usage, Group => GM}

import hl7.v2.validation.vs.{EmptyValueSetSpecification, ValueSetLibraryImpl, ValueSetSpecification}


trait SlicingEvalMocks {

  var profileSlicingContext: ProfileSlicingContext = EmptyProfileSlicingContext
  val vsSpecification: ValueSetSpecification = EmptyValueSetSpecification
  implicit val valueSetLibrary: ValueSetLibraryImpl = new ValueSetLibraryImpl(List("excluded"), Map())
  implicit val separators: Separators = Separators( '|', '^', '~', '\\', '&', Some('#') )
  implicit val time: Option[TimeZone] = None

  def req(p: Int, desc: String, usage: Usage): Req = Req(p, desc, usage, None, None, None, Nil, None, hide = false, None)

  def MESSAGE: Message = Message("MSG_ID", "STRUCT_ID", "EVENT_ID", "TYPE_ID", "DESC", List(
    SEGMENT_REF_A,
    GROUP,
  ))

  def GROUP: GM = GM("GROUP_ID", "GROUP", List(SEGMENT_REF_B), req(2, "Group", Usage.O))

  // -------- SGA (Base, FLV1, FLV2)
  def SEGMENT_REF_A: SegmentRef = SegmentRef(req(1, "Segment A", Usage.R), SEGMENT_A_BASE)
  def SEGMENT_A_BASE: Segment = Segment("SEGMENT_A_BASE", "SGA","Segment A Base", SEGMENT_A_BASE_FIELDS.values.toList.sortBy(_.req.position), Nil)
  def SEGMENT_A_BASE_FIELDS = Map(
    "A" -> Field("A_BASE", ST, req(1, "Field A Base", Usage.R)),
    "B" -> Field("B_BASE", CWE, req(2, "Field B Base", Usage.O)),
    "C" -> Field("C_BASE", ST, req(3, "Field B Base", Usage.O)) ,
  )

  def SEGMENT_A_FLV1: Segment = Segment("SEGMENT_A_FLV1", "SGA","Segment A FLV1", SEGMENT_A_FLV1_FIELDS.values.toList.sortBy(_.req.position), Nil)
  def SEGMENT_A_FLV1_FIELDS = Map(
    "A" -> Field("A_FLV1", ST, req(1, "Field A FLV1", Usage.O)),
    "B" -> Field("B_FLV1", CWE, req(2, "Field B FLV1", Usage.R)),
  )

  def SEGMENT_A_FLV2: Segment = Segment("SEGMENT_A_FLV2", "SGA","Segment A FLV2", SEGMENT_A_FLV2_FIELDS.values.toList.sortBy(_.req.position), Nil)
  def SEGMENT_A_FLV2_FIELDS = Map(
    "A" -> Field("A_FLV2", ST, req(1, "Field A FLV2", Usage.R)),
    "B" -> Field("B_FLV2", CWE, req(2, "Field B FLV2", Usage.R)),
    "C" -> Field("C_FLV2", ST, req(3, "Field C FLV2", Usage.O)) ,
    "D" -> Field("D_FLV2", ST, req(4, "Field D FLV2", Usage.O)) ,
  )

  // -------- SGB (Base, FLV1, FLV2)
  def SEGMENT_REF_B: SegmentRef = SegmentRef(req(1, "Segment B", Usage.R), SEGMENT_B_BASE)
  def SEGMENT_B_BASE: Segment = Segment("SEGMENT_B_BASE", "SGB","Segment B Base", SEGMENT_B_BASE_FIELDS.values.toList.sortBy(_.req.position), Nil)
  def SEGMENT_B_BASE_FIELDS = Map(
    "A" -> Field("A_BASE", ST, req(1, "Field A Base", Usage.R)),
    "B" -> Field("B_BASE", CWE, req(2, "Field B Base", Usage.O)),
    "C" -> Field("C_BASE", ST, req(3, "Field B Base", Usage.O)) ,
  )

  def SEGMENT_B_FLV1: Segment = Segment("SEGMENT_B_FLV1", "SGB","Segment B FLV1", SEGMENT_B_FLV1_FIELDS.values.toList.sortBy(_.req.position), Nil)
  def SEGMENT_B_FLV1_FIELDS = Map(
    "A" -> Field("A_FLV1", ST, req(1, "Field A FLV1", Usage.O)),
    "B" -> Field("B_FLV1", CWE, req(2, "Field B FLV1", Usage.R)),
  )

  def SEGMENT_B_FLV2: Segment = Segment("SEGMENT_B_FLV2", "SGB","Segment B FLV2", SEGMENT_B_FLV2_FIELDS.values.toList.sortBy(_.req.position), Nil)
  def SEGMENT_B_FLV2_FIELDS = Map(
    "A" -> Field("A_FLV2", ST, req(1, "Field A FLV2", Usage.R)),
    "B" -> Field("B_FLV2", CWE, req(2, "Field B FLV2", Usage.R)),
    "C" -> Field("C_FLV2", ST, req(3, "Field C FLV2", Usage.O)) ,
    "D" -> Field("D_FLV2", ST, req(4, "Field D FLV2", Usage.O)) ,
  )

  // ------ CWE (Base, FLV1, FLV2)
  def CWE: Composite = Composite("CWE", "CWE", "Coded Element", "2.5", CWE_COMPS.values.toList.sortBy(_.req.position))
  def CWE_COMPS = Map(
    "C1" -> Component("C1", ST, req(1, "Component C1", Usage.R)),
    "C2" -> Component("C2", ST, req(2, "Component C2", Usage.R)),
    "C3" -> Component("C3", ST, req(3, "Component C3", Usage.R)),
    "C4" -> Component("C4", ST, req(4, "Component C4", Usage.R)),
    "C5" -> Component("C5", ST, req(5, "Component C5", Usage.R)),
    "C6" -> Component("C6", ST, req(6, "Component C6", Usage.R)),
  )

  def CWE_FLV1: Composite = Composite("CWE_FLV1", "CWE_FLV1", "Coded Element", "2.5", CWE_FLV1_COMPS.values.toList.sortBy(_.req.position))
  def CWE_FLV1_COMPS = Map(
    "C1" -> Component("C1", ST, req(1, "Component C1", Usage.R)),
    "C2" -> Component("C2", ST, req(2, "Component C2", Usage.O)),
    "C3" -> Component("C3", ST, req(3, "Component C3", Usage.R)),
  )

  def CWE_FLV2: Composite = Composite("CWE_FLV2", "CWE_FLV2", "Coded Element", "2.5", CWE_FLV2_COMPS.values.toList.sortBy(_.req.position))
  def CWE_FLV2_COMPS = Map(
    "C1" -> Component("C1", ST, req(1, "Component C1", Usage.O)),
    "C2" -> Component("C2", ST, req(2, "Component C2", Usage.O)),
    "C3" -> Component("C3", ST, req(3, "Component C3", Usage.O)),
    "C4" -> Component("C4", ST, req(4, "Component C4", Usage.R)),
    "C5" -> Component("C5", ST, req(5, "Component C5", Usage.R)),
    "C6" -> Component("C6", ST, req(6, "Component C6", Usage.R)),
  )



  def ST: Primitive = Primitive("ST", "ST", "String", "2.5")
  def mkSegment(value: String, segment: SegmentRef): SI = {
    implicit val ctr: Counter = Counter(scala.collection.mutable.Map[String, Int]())
    SI(segment, value, 1, -1)
  }

}
