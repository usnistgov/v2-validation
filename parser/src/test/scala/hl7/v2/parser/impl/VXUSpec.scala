package hl7.v2.parser.impl

import hl7.v2.instance.{EType, Location, SegOrGroup, Segment => SegmentInstance, Group => GroupInstance}
import hl7.v2.profile.{Group, Range, Req, SegRefOrGroup, Segment, SegmentRef, Usage}

trait VXUSpec extends ParserSpecHelper {


	// Test Definitions
	def CONTEXT_FREE_EXAMPLE_VXU_TEST = check(CONTEXT_FREE_EXAMPLE_VXU, VXU_profile)
	// Resources
	val VXU_MSH = SegmentRef(Req(1, "MSH", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("MSH", "MSH", "MSH", Nil, Nil))
	val VXU_SFT = SegmentRef(Req(2, "SFT", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None), Segment("SFT", "SFT", "SFT", Nil, Nil))
	val VXU_PID = SegmentRef(Req(3, "PID", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("PID", "PID", "PID", Nil, Nil))
	val VXU_PD1 = SegmentRef(Req(4, "PD1", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("PD1", "PD1", "PD1", Nil, Nil))
	val VXU_NK1 = SegmentRef(Req(5, "NK1", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None), Segment("NK1", "NK1", "NK1", Nil, Nil))
	val VXU_PATIENT_PV1 = SegmentRef(Req(1, "PV1", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("PV1", "PV1", "PV1", Nil, Nil))
	val VXU_PATIENT_PV2 = SegmentRef(Req(2, "PV2", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("PV2", "PV2", "PV2", Nil, Nil))
	val VXU_PATIENT = Group("PATIENT", "PATIENT", List(VXU_PATIENT_PV1,
	VXU_PATIENT_PV2), Req(6, "PATIENT", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None))
	val VXU_GT1 = SegmentRef(Req(7, "GT1", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None), Segment("GT1", "GT1", "GT1", Nil, Nil))
	val VXU_INSURANCE_IN1 = SegmentRef(Req(1, "IN1", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("IN1", "IN1", "IN1", Nil, Nil))
	val VXU_INSURANCE_IN2 = SegmentRef(Req(2, "IN2", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("IN2", "IN2", "IN2", Nil, Nil))
	val VXU_INSURANCE_IN3 = SegmentRef(Req(3, "IN3", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("IN3", "IN3", "IN3", Nil, Nil))
	val VXU_INSURANCE = Group("INSURANCE", "INSURANCE", List(VXU_INSURANCE_IN1,
	VXU_INSURANCE_IN2,
	VXU_INSURANCE_IN3), Req(8, "INSURANCE", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None))
	val VXU_ORDER_ORC = SegmentRef(Req(1, "ORC", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("ORC", "ORC", "ORC", Nil, Nil))
	val VXU_ORDER_TIMING_TQ1 = SegmentRef(Req(1, "TQ1", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("TQ1", "TQ1", "TQ1", Nil, Nil))
	val VXU_ORDER_TIMING_TQ2 = SegmentRef(Req(2, "TQ2", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("TQ2", "TQ2", "TQ2", Nil, Nil))
	val VXU_ORDER_TIMING = Group("TIMING", "TIMING", List(VXU_ORDER_TIMING_TQ1,
	VXU_ORDER_TIMING_TQ2), Req(2, "TIMING", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None))
	val VXU_ORDER_RXA = SegmentRef(Req(3, "RXA", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("RXA", "RXA", "RXA", Nil, Nil))
	val VXU_ORDER_RXR = SegmentRef(Req(4, "RXR", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("RXR", "RXR", "RXR", Nil, Nil))
	val VXU_ORDER_OBSERVATION_OBX = SegmentRef(Req(1, "OBX", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("OBX", "OBX", "OBX", Nil, Nil))
	val VXU_ORDER_OBSERVATION_NTE = SegmentRef(Req(2, "NTE", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("NTE", "NTE", "NTE", Nil, Nil))
	val VXU_ORDER_OBSERVATION = Group("OBSERVATION", "OBSERVATION", List(VXU_ORDER_OBSERVATION_OBX,
	VXU_ORDER_OBSERVATION_NTE), Req(5, "OBSERVATION", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None))
	val VXU_ORDER = Group("ORDER", "ORDER", List(VXU_ORDER_ORC,
	VXU_ORDER_TIMING,
	VXU_ORDER_RXA,
	VXU_ORDER_RXR,
	VXU_ORDER_OBSERVATION), Req(9, "ORDER", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None))
	// Profile
	val VXU_profile: List[SegRefOrGroup] = List(VXU_MSH,
	VXU_SFT,
	VXU_PID,
	VXU_PD1,
	VXU_NK1,
	VXU_PATIENT,
	VXU_GT1,
	VXU_INSURANCE,
	VXU_ORDER)
	// Mocks
	val CONTEXT_FREE_EXAMPLE_VXU = ("""/MSH
	/PID
	/PD1
	/NK1
	/ORC
	/RXA
	/RXR
	/OBX
	/OBX
	/OBX
	/OBX""".stripMargin('/'), List(SegmentInstance(VXU_MSH, Location(EType.Segment, "MSH", "MSH", 1, 1, "MSH[1]"), 1, Nil, hasExtra = true, "MSH"),
	SegmentInstance(VXU_PID, Location(EType.Segment, "PID", "PID", 2, 1, "PID[1]"), 1, Nil, hasExtra = true, "PID"),
	SegmentInstance(VXU_PD1, Location(EType.Segment, "PD1", "PD1", 3, 1, "PD1[1]"), 1, Nil, hasExtra = true, "PD1"),
	SegmentInstance(VXU_NK1, Location(EType.Segment, "NK1", "NK1", 4, 1, "NK1[1]"), 1, Nil, hasExtra = true, "NK1"),
	GroupInstance(VXU_ORDER, 1, List(SegmentInstance(VXU_ORDER_ORC, Location(EType.Segment, "ORC", "ORC", 5, 1, "ORC[1]"), 1, Nil, hasExtra = true, "ORC"),
	SegmentInstance(VXU_ORDER_RXA, Location(EType.Segment, "RXA", "RXA", 6, 1, "RXA[1]"), 1, Nil, hasExtra = true, "RXA"),
	SegmentInstance(VXU_ORDER_RXR, Location(EType.Segment, "RXR", "RXR", 7, 1, "RXR[1]"), 1, Nil, hasExtra = true, "RXR"),
	GroupInstance(VXU_ORDER_OBSERVATION, 1, List(SegmentInstance(VXU_ORDER_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 8, 1, "OBX[1]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(VXU_ORDER_OBSERVATION, 2, List(SegmentInstance(VXU_ORDER_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 9, 1, "OBX[2]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(VXU_ORDER_OBSERVATION, 3, List(SegmentInstance(VXU_ORDER_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 10, 1, "OBX[3]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(VXU_ORDER_OBSERVATION, 4, List(SegmentInstance(VXU_ORDER_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 11, 1, "OBX[4]"), 1, Nil, hasExtra = true, "OBX")))))))
	

}
