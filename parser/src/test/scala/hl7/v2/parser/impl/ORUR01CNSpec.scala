package hl7.v2.parser.impl

import hl7.v2.instance.{EType, Location, Segment => SegmentInstance, Group => GroupInstance}
import hl7.v2.profile.{Group, Range, Req, SegRefOrGroup, Segment, SegmentRef, Usage}

trait ORUR01CNSpec extends ParserSpecHelper {

	// Test Definitions
	def ORUR01_CN = check(CONTEXT_FREE, PROFILE)
	// Resources
	val MSH = SegmentRef(Req(1, "MSH", Usage.R, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("MSH", "MSH", "MSH", Nil, Nil))
	val SFT = SegmentRef(Req(2, "SFT", Usage.X, Some(Range(0, "0")), None, None, Nil, None, hide = false, None), Segment("SFT", "SFT", "SFT", Nil, Nil))
	val PATIENT_RESULT_PATIENT_PID = SegmentRef(Req(1, "PID", Usage.R, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("PID", "PID", "PID", Nil, Nil))
	val PATIENT_RESULT_PATIENT_PD1 = SegmentRef(Req(2, "PD1", Usage.X, Some(Range(0, "0")), None, None, Nil, None, hide = false, None), Segment("PD1", "PD1", "PD1", Nil, Nil))
	val PATIENT_RESULT_PATIENT_NTE = SegmentRef(Req(3, "NTE", Usage.X, Some(Range(0, "0")), None, None, Nil, None, hide = false, None), Segment("NTE", "NTE", "NTE", Nil, Nil))
	val PATIENT_RESULT_PATIENT_NK1 = SegmentRef(Req(4, "NK1", Usage.X, Some(Range(0, "0")), None, None, Nil, None, hide = false, None), Segment("NK1", "NK1", "NK1", Nil, Nil))

	val PATIENT_RESULT_PATIENT_VISIT_PV1 = SegmentRef(Req(1, "PV1", Usage.X, Some(Range(0, "0")), None, None, Nil, None, hide = false, None), Segment("PV1", "PV1", "PV1", Nil, Nil))
	val PATIENT_RESULT_PATIENT_VISIT_PV2 = SegmentRef(Req(2, "PV2", Usage.X, Some(Range(0, "0")), None, None, Nil, None, hide = false, None), Segment("PV2", "PV2", "PV2", Nil, Nil))
	val PATIENT_RESULT_PATIENT_VISIT = Group("VISIT", "VISIT",
		List(
			PATIENT_RESULT_PATIENT_VISIT_PV1,
			PATIENT_RESULT_PATIENT_VISIT_PV2
		), Req(5, "VISIT", Usage.X, Some(Range(0, "0")), None, None, Nil, None, hide = false, None)
	)

	val PATIENT_RESULT_PATIENT = Group("PATIENT", "PATIENT",
		List(
			PATIENT_RESULT_PATIENT_PID,
			PATIENT_RESULT_PATIENT_PD1,
			PATIENT_RESULT_PATIENT_NTE,
			PATIENT_RESULT_PATIENT_NK1,
			PATIENT_RESULT_PATIENT_VISIT
		), Req(1, "PATIENT", Usage.RE, Some(Range(0, "1")), None, None, Nil, None, hide = false, None)
	)

	val PATIENT_RESULT_ORDER_OBSERVATION_ORC = SegmentRef(Req(1, "ORC", Usage.X, Some(Range(0, "0")), None, None, Nil, None, hide = false, None), Segment("ORC", "ORC", "ORC", Nil, Nil))
	val PATIENT_RESULT_ORDER_OBSERVATION_OBR = SegmentRef(Req(2, "OBR", Usage.R, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("OBR", "OBR", "OBR", Nil, Nil))
	val PATIENT_RESULT_ORDER_OBSERVATION_NTE = SegmentRef(Req(3, "NTE", Usage.X, Some(Range(0, "0")), None, None, Nil, None, hide = false, None), Segment("NTE", "NTE", "NTE", Nil, Nil))

	val PATIENT_RESULT_ORDER_OBSERVATION_TIMING_QTY_TQ1 = SegmentRef(Req(1, "TQ1", Usage.X, Some(Range(0, "0")), None, None, Nil, None, hide = false, None), Segment("TQ1", "TQ1", "TQ1", Nil, Nil))
	val PATIENT_RESULT_ORDER_OBSERVATION_TIMING_QTY_TQ2 = SegmentRef(Req(2, "TQ2", Usage.X, Some(Range(0, "0")), None, None, Nil, None, hide = false, None), Segment("TQ2", "TQ2", "TQ2", Nil, Nil))
	val PATIENT_RESULT_ORDER_OBSERVATION_TIMING_QTY = Group("TIMING_QTY", "TIMING_QTY",
		List(
			PATIENT_RESULT_ORDER_OBSERVATION_TIMING_QTY_TQ1,
			PATIENT_RESULT_ORDER_OBSERVATION_TIMING_QTY_TQ2
		), Req(4, "TIMING_QTY", Usage.X, Some(Range(0, "0")), None, None, Nil, None, hide = false, None)
	)

	val PATIENT_RESULT_ORDER_OBSERVATION_CDT = SegmentRef(Req(5, "CDT", Usage.X, Some(Range(0, "0")), None, None, Nil, None, hide = false, None), Segment("CDT", "CDT", "CDT", Nil, Nil))

	val PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX = SegmentRef(Req(1, "OBX", Usage.O, Some(Range(0, "1")), None, None, Nil, None, hide = false, None), Segment("OBX", "OBX", "OBX", Nil, Nil))
	val PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_NTE = SegmentRef(Req(2, "NTE", Usage.X, Some(Range(0, "0")), None, None, Nil, None, hide = false, None), Segment("NTE", "NTE", "NTE", Nil, Nil))
	val PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION = Group("OBSERVATION", "OBSERVATION",
		List(
			PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX,
			PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_NTE
		), Req(6, "OBSERVATION", Usage.O, Some(Range(0, "*")), None, None, Nil, None, hide = false, None)
	)

	val PATIENT_RESULT_ORDER_OBSERVATION_FT1 = SegmentRef(Req(7, "FT1", Usage.X, Some(Range(0, "0")), None, None, Nil, None, hide = false, None), Segment("FT1", "FT1", "FT1", Nil, Nil))
	val PATIENT_RESULT_ORDER_OBSERVATION_CTI = SegmentRef(Req(8, "CTI", Usage.X, Some(Range(0, "0")), None, None, Nil, None, hide = false, None), Segment("CTI", "CTI", "CTI", Nil, Nil))

	val PATIENT_RESULT_ORDER_OBSERVATION_SPECIMEN_SPM = SegmentRef(Req(1, "SPM", Usage.X, Some(Range(0, "0")), None, None, Nil, None, hide = false, None), Segment("SPM", "SPM", "SPM", Nil, Nil))
	val PATIENT_RESULT_ORDER_OBSERVATION_SPECIMEN_OBX = SegmentRef(Req(2, "OBX", Usage.X, Some(Range(0, "0")), None, None, Nil, None, hide = false, None), Segment("OBX", "OBX", "OBX", Nil, Nil))
	val PATIENT_RESULT_ORDER_OBSERVATION_SPECIMEN = Group("SPECIMEN", "SPECIMEN",
		List(
			PATIENT_RESULT_ORDER_OBSERVATION_SPECIMEN_SPM,
			PATIENT_RESULT_ORDER_OBSERVATION_SPECIMEN_OBX
		), Req(9, "TIMING", Usage.X, Some(Range(0, "0")), None, None, Nil, None, hide = false, None)
	)

	val PATIENT_RESULT_ORDER_OBSERVATION = Group("ORDER_OBSERVATION", "ORDER_OBSERVATION",
		List(
			PATIENT_RESULT_ORDER_OBSERVATION_ORC,
			PATIENT_RESULT_ORDER_OBSERVATION_OBR,
			PATIENT_RESULT_ORDER_OBSERVATION_NTE,
			PATIENT_RESULT_ORDER_OBSERVATION_TIMING_QTY,
			PATIENT_RESULT_ORDER_OBSERVATION_CDT,
			PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION,
			PATIENT_RESULT_ORDER_OBSERVATION_FT1,
			PATIENT_RESULT_ORDER_OBSERVATION_CTI,
			PATIENT_RESULT_ORDER_OBSERVATION_SPECIMEN
		), Req(2, "ORDER_OBSERVATION", Usage.R, Some(Range(1, "*")), None, None, Nil, None, hide = false, None)
	)

	val PATIENT_RESULT = Group("PATIENT_RESULT", "PATIENT_RESULT",
		List(
			PATIENT_RESULT_PATIENT,
			PATIENT_RESULT_ORDER_OBSERVATION,
		), Req(3, "PATIENT_RESULT", Usage.R, Some(Range(1, "1")), None, None, Nil, None, hide = false, None)
	)

	val DSC = SegmentRef(Req(4, "DSC", Usage.X, Some(Range(0, "0")), None, None, Nil, None, hide = false, None), Segment("DSC", "DSC", "DSC", Nil, Nil))

	val PROFILE: List[SegRefOrGroup] = List(
		MSH,
		SFT,
		PATIENT_RESULT,
		DSC,
	)

	val CONTEXT_FREE = (
		"""/MSH
												/PID
												/OBR
												/OBR
												/OBX
												/OBX""".stripMargin('/'), List(
		SegmentInstance(MSH, Location(EType.Segment, "MSH", "MSH", 1, 1, "MSH[1]"), 1, Nil, hasExtra = true, "MSH"),
		GroupInstance(PATIENT_RESULT, 1, List(
			GroupInstance(PATIENT_RESULT_PATIENT, 1, List(
				SegmentInstance(PATIENT_RESULT_PATIENT_PID, Location(EType.Segment, "PID", "PID", 2, 1, "PID[1]"), 1, Nil, hasExtra = true, "PID"),
			)),
			GroupInstance(PATIENT_RESULT_ORDER_OBSERVATION, 1, List(
				SegmentInstance(PATIENT_RESULT_ORDER_OBSERVATION_OBR, Location(EType.Segment, "OBR", "OBR", 3, 1, "OBR[1]"), 1, Nil, hasExtra = true, "OBR"),
			)),
			GroupInstance(PATIENT_RESULT_ORDER_OBSERVATION, 2, List(
				SegmentInstance(PATIENT_RESULT_ORDER_OBSERVATION_OBR, Location(EType.Segment, "OBR", "OBR", 4, 1, "OBR[2]"), 1, Nil, hasExtra = true, "OBR"),
				GroupInstance(PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 1, List(
					SegmentInstance(PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 5, 1, "OBX[1]"), 1, Nil, hasExtra = true, "OBX"),
				)),
				GroupInstance(PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 2, List(
					SegmentInstance(PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 6, 1, "OBX[2]"), 1, Nil, hasExtra = true, "OBX"),
				))
			)),
		))))

}
