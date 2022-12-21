package hl7.v2.parser.impl

import hl7.v2.instance.{EType, Location, SegOrGroup, Segment => SegmentInstance, Group => GroupInstance}
import hl7.v2.profile.{Group, Range, Req, SegRefOrGroup, Segment, SegmentRef, Usage}

trait ORUSpec extends ParserSpecHelper {


	// Test Definitions
	def SED_RATE_1_ORU_TEST = check(SED_RATE_1_ORU, ORU_profile)
	def SED_RATE_1__NO_ORC_ORU_TEST = check(SED_RATE_1__NO_ORC_ORU, ORU_profile)
	def SED_RATE_2_ORU_TEST = check(SED_RATE_2_ORU, ORU_profile)
	def SED_RATE_3_ORU_TEST = check(SED_RATE_3_ORU, ORU_profile)
	def CBC_ORU_TEST = check(CBC_ORU, ORU_profile)
	def CULTURE_AND_SUSCEPTIBILITY_2_ORU_TEST = check(CULTURE_AND_SUSCEPTIBILITY_2_ORU, ORU_profile)
	def REFLEX_1_ORU_TEST = check(REFLEX_1_ORU, ORU_profile)
	def REFLEX_1__NO_ORC_ORU_TEST = check(REFLEX_1__NO_ORC_ORU, ORU_profile)
	def REFLEX_1__THE_FIRST_ORC_ORU_TEST = check(REFLEX_1__THE_FIRST_ORC_ORU, ORU_profile)
	def REFLEX_1__THE_SECOND_ORC_PRESENT_ORU_TEST = check(REFLEX_1__THE_SECOND_ORC_PRESENT_ORU, ORU_profile)
	def CCHD_EXAMPLE_MESSAGE_ORU_TEST = check(CCHD_EXAMPLE_MESSAGE_ORU, ORU_profile)
	// Resources
	val ORU_MSH = SegmentRef(Req(1, "MSH", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("MSH", "MSH", "MSH", Nil, Nil))
	val ORU_SFT = SegmentRef(Req(2, "SFT", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None), Segment("SFT", "SFT", "SFT", Nil, Nil))
	val ORU_PATIENT_RESULT_PATIENT_PID = SegmentRef(Req(1, "PID", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("PID", "PID", "PID", Nil, Nil))
	val ORU_PATIENT_RESULT_PATIENT_PD1 = SegmentRef(Req(2, "PD1", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("PD1", "PD1", "PD1", Nil, Nil))
	val ORU_PATIENT_RESULT_PATIENT_NTE = SegmentRef(Req(3, "NTE", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None), Segment("NTE", "NTE", "NTE", Nil, Nil))
	val ORU_PATIENT_RESULT_PATIENT_NK1 = SegmentRef(Req(4, "NK1", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None), Segment("NK1", "NK1", "NK1", Nil, Nil))
	val ORU_PATIENT_RESULT_PATIENT_VISIT_PV1 = SegmentRef(Req(1, "PV1", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("PV1", "PV1", "PV1", Nil, Nil))
	val ORU_PATIENT_RESULT_PATIENT_VISIT_PV2 = SegmentRef(Req(2, "PV2", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("PV2", "PV2", "PV2", Nil, Nil))
	val ORU_PATIENT_RESULT_PATIENT_VISIT = Group("VISIT", "VISIT", List(ORU_PATIENT_RESULT_PATIENT_VISIT_PV1,
	ORU_PATIENT_RESULT_PATIENT_VISIT_PV2), Req(5, "VISIT", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None))
	val ORU_PATIENT_RESULT_PATIENT = Group("PATIENT", "PATIENT", List(ORU_PATIENT_RESULT_PATIENT_PID,
	ORU_PATIENT_RESULT_PATIENT_PD1,
	ORU_PATIENT_RESULT_PATIENT_NTE,
	ORU_PATIENT_RESULT_PATIENT_NK1,
	ORU_PATIENT_RESULT_PATIENT_VISIT), Req(1, "PATIENT", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None))
	val ORU_PATIENT_RESULT_ORDER_OBSERVATION_ORC = SegmentRef(Req(1, "ORC", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("ORC", "ORC", "ORC", Nil, Nil))
	val ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBR = SegmentRef(Req(2, "OBR", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("OBR", "OBR", "OBR", Nil, Nil))
	val ORU_PATIENT_RESULT_ORDER_OBSERVATION_NTE = SegmentRef(Req(3, "NTE", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None), Segment("NTE", "NTE", "NTE", Nil, Nil))
	val ORU_PATIENT_RESULT_ORDER_OBSERVATION_TIMING_QTY_TQ1 = SegmentRef(Req(1, "TQ1", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("TQ1", "TQ1", "TQ1", Nil, Nil))
	val ORU_PATIENT_RESULT_ORDER_OBSERVATION_TIMING_QTY_TQ2 = SegmentRef(Req(2, "TQ2", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None), Segment("TQ2", "TQ2", "TQ2", Nil, Nil))
	val ORU_PATIENT_RESULT_ORDER_OBSERVATION_TIMING_QTY = Group("TIMING_QTY", "TIMING_QTY", List(ORU_PATIENT_RESULT_ORDER_OBSERVATION_TIMING_QTY_TQ1,
	ORU_PATIENT_RESULT_ORDER_OBSERVATION_TIMING_QTY_TQ2), Req(4, "TIMING_QTY", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None))
	val ORU_PATIENT_RESULT_ORDER_OBSERVATION_CTD = SegmentRef(Req(5, "CTD", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("CTD", "CTD", "CTD", Nil, Nil))
	val ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX = SegmentRef(Req(1, "OBX", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("OBX", "OBX", "OBX", Nil, Nil))
	val ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_NTE = SegmentRef(Req(2, "NTE", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None), Segment("NTE", "NTE", "NTE", Nil, Nil))
	val ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION = Group("OBSERVATION", "OBSERVATION", List(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX,
	ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_NTE), Req(6, "OBSERVATION", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None))
	val ORU_PATIENT_RESULT_ORDER_OBSERVATION_FT1 = SegmentRef(Req(7, "FT1", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None), Segment("FT1", "FT1", "FT1", Nil, Nil))
	val ORU_PATIENT_RESULT_ORDER_OBSERVATION_CTI = SegmentRef(Req(8, "CTI", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None), Segment("CTI", "CTI", "CTI", Nil, Nil))
	val ORU_PATIENT_RESULT_ORDER_OBSERVATION_SPECIMEN_SPM = SegmentRef(Req(1, "SPM", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("SPM", "SPM", "SPM", Nil, Nil))
	val ORU_PATIENT_RESULT_ORDER_OBSERVATION_SPECIMEN_OBX = SegmentRef(Req(2, "OBX", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None), Segment("OBX", "OBX", "OBX", Nil, Nil))
	val ORU_PATIENT_RESULT_ORDER_OBSERVATION_SPECIMEN = Group("SPECIMEN", "SPECIMEN", List(ORU_PATIENT_RESULT_ORDER_OBSERVATION_SPECIMEN_SPM,
	ORU_PATIENT_RESULT_ORDER_OBSERVATION_SPECIMEN_OBX), Req(9, "SPECIMEN", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None))
	val ORU_PATIENT_RESULT_ORDER_OBSERVATION = Group("ORDER_OBSERVATION", "ORDER_OBSERVATION", List(ORU_PATIENT_RESULT_ORDER_OBSERVATION_ORC,
	ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBR,
	ORU_PATIENT_RESULT_ORDER_OBSERVATION_NTE,
	ORU_PATIENT_RESULT_ORDER_OBSERVATION_TIMING_QTY,
	ORU_PATIENT_RESULT_ORDER_OBSERVATION_CTD,
	ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION,
	ORU_PATIENT_RESULT_ORDER_OBSERVATION_FT1,
	ORU_PATIENT_RESULT_ORDER_OBSERVATION_CTI,
	ORU_PATIENT_RESULT_ORDER_OBSERVATION_SPECIMEN), Req(2, "ORDER_OBSERVATION", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None))
	val ORU_PATIENT_RESULT = Group("PATIENT_RESULT", "PATIENT_RESULT", List(ORU_PATIENT_RESULT_PATIENT,
	ORU_PATIENT_RESULT_ORDER_OBSERVATION), Req(3, "PATIENT_RESULT", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None))
	val ORU_DSC = SegmentRef(Req(4, "DSC", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("DSC", "DSC", "DSC", Nil, Nil))
	// Profile
	val ORU_profile: List[SegRefOrGroup] = List(ORU_MSH,
	ORU_SFT,
	ORU_PATIENT_RESULT,
	ORU_DSC)
	// Mocks
	val SED_RATE_1_ORU = ("""/MSH
	/PID
	/ORC
	/OBR
	/NTE
	/TQ1
	/OBX
	/SPM""".stripMargin('/'), List(SegmentInstance(ORU_MSH, Location(EType.Segment, "MSH", "MSH", 1, 1, "MSH[1]"), 1, Nil, hasExtra = true, "MSH"),
	GroupInstance(ORU_PATIENT_RESULT, 1, List(GroupInstance(ORU_PATIENT_RESULT_PATIENT, 1, List(SegmentInstance(ORU_PATIENT_RESULT_PATIENT_PID, Location(EType.Segment, "PID", "PID", 2, 1, "PID[1]"), 1, Nil, hasExtra = true, "PID"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION, 1, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_ORC, Location(EType.Segment, "ORC", "ORC", 3, 1, "ORC[1]"), 1, Nil, hasExtra = true, "ORC"),
	SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBR, Location(EType.Segment, "OBR", "OBR", 4, 1, "OBR[1]"), 1, Nil, hasExtra = true, "OBR"),
	SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_NTE, Location(EType.Segment, "NTE", "NTE", 5, 1, "NTE[1]"), 1, Nil, hasExtra = true, "NTE"),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_TIMING_QTY, 1, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_TIMING_QTY_TQ1, Location(EType.Segment, "TQ1", "TQ1", 6, 1, "TQ1[1]"), 1, Nil, hasExtra = true, "TQ1"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 1, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 7, 1, "OBX[1]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_SPECIMEN, 1, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_SPECIMEN_SPM, Location(EType.Segment, "SPM", "SPM", 8, 1, "SPM[1]"), 1, Nil, hasExtra = true, "SPM")))))))))
	val SED_RATE_1__NO_ORC_ORU = ("""/MSH
	/PID
	/OBR
	/NTE
	/TQ1
	/OBX
	/SPM""".stripMargin('/'), List(SegmentInstance(ORU_MSH, Location(EType.Segment, "MSH", "MSH", 1, 1, "MSH[1]"), 1, Nil, hasExtra = true, "MSH"),
	GroupInstance(ORU_PATIENT_RESULT, 1, List(GroupInstance(ORU_PATIENT_RESULT_PATIENT, 1, List(SegmentInstance(ORU_PATIENT_RESULT_PATIENT_PID, Location(EType.Segment, "PID", "PID", 2, 1, "PID[1]"), 1, Nil, hasExtra = true, "PID"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION, 1, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBR, Location(EType.Segment, "OBR", "OBR", 3, 1, "OBR[1]"), 1, Nil, hasExtra = true, "OBR"),
	SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_NTE, Location(EType.Segment, "NTE", "NTE", 4, 1, "NTE[1]"), 1, Nil, hasExtra = true, "NTE"),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_TIMING_QTY, 1, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_TIMING_QTY_TQ1, Location(EType.Segment, "TQ1", "TQ1", 5, 1, "TQ1[1]"), 1, Nil, hasExtra = true, "TQ1"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 1, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 6, 1, "OBX[1]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_SPECIMEN, 1, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_SPECIMEN_SPM, Location(EType.Segment, "SPM", "SPM", 7, 1, "SPM[1]"), 1, Nil, hasExtra = true, "SPM")))))))))
	val SED_RATE_2_ORU = ("""/MSH
	/PID
	/ORC
	/OBR
	/NTE
	/TQ1
	/OBX
	/NTE
	/SPM""".stripMargin('/'), List(SegmentInstance(ORU_MSH, Location(EType.Segment, "MSH", "MSH", 1, 1, "MSH[1]"), 1, Nil, hasExtra = true, "MSH"),
	GroupInstance(ORU_PATIENT_RESULT, 1, List(GroupInstance(ORU_PATIENT_RESULT_PATIENT, 1, List(SegmentInstance(ORU_PATIENT_RESULT_PATIENT_PID, Location(EType.Segment, "PID", "PID", 2, 1, "PID[1]"), 1, Nil, hasExtra = true, "PID"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION, 1, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_ORC, Location(EType.Segment, "ORC", "ORC", 3, 1, "ORC[1]"), 1, Nil, hasExtra = true, "ORC"),
	SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBR, Location(EType.Segment, "OBR", "OBR", 4, 1, "OBR[1]"), 1, Nil, hasExtra = true, "OBR"),
	SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_NTE, Location(EType.Segment, "NTE", "NTE", 5, 1, "NTE[1]"), 1, Nil, hasExtra = true, "NTE"),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_TIMING_QTY, 1, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_TIMING_QTY_TQ1, Location(EType.Segment, "TQ1", "TQ1", 6, 1, "TQ1[1]"), 1, Nil, hasExtra = true, "TQ1"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 1, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 7, 1, "OBX[1]"), 1, Nil, hasExtra = true, "OBX"),
	SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_NTE, Location(EType.Segment, "NTE", "NTE", 8, 1, "NTE[2]"), 1, Nil, hasExtra = true, "NTE"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_SPECIMEN, 1, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_SPECIMEN_SPM, Location(EType.Segment, "SPM", "SPM", 9, 1, "SPM[1]"), 1, Nil, hasExtra = true, "SPM")))))))))
	val SED_RATE_3_ORU = ("""/MSH
	/PID
	/ORC
	/OBR
	/SPM""".stripMargin('/'), List(SegmentInstance(ORU_MSH, Location(EType.Segment, "MSH", "MSH", 1, 1, "MSH[1]"), 1, Nil, hasExtra = true, "MSH"),
	GroupInstance(ORU_PATIENT_RESULT, 1, List(GroupInstance(ORU_PATIENT_RESULT_PATIENT, 1, List(SegmentInstance(ORU_PATIENT_RESULT_PATIENT_PID, Location(EType.Segment, "PID", "PID", 2, 1, "PID[1]"), 1, Nil, hasExtra = true, "PID"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION, 1, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_ORC, Location(EType.Segment, "ORC", "ORC", 3, 1, "ORC[1]"), 1, Nil, hasExtra = true, "ORC"),
	SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBR, Location(EType.Segment, "OBR", "OBR", 4, 1, "OBR[1]"), 1, Nil, hasExtra = true, "OBR"),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_SPECIMEN, 1, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_SPECIMEN_SPM, Location(EType.Segment, "SPM", "SPM", 5, 1, "SPM[1]"), 1, Nil, hasExtra = true, "SPM")))))))))
	val CBC_ORU = ("""/MSH
	/PID
	/ORC
	/OBR
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/SPM""".stripMargin('/'), List(SegmentInstance(ORU_MSH, Location(EType.Segment, "MSH", "MSH", 1, 1, "MSH[1]"), 1, Nil, hasExtra = true, "MSH"),
	GroupInstance(ORU_PATIENT_RESULT, 1, List(GroupInstance(ORU_PATIENT_RESULT_PATIENT, 1, List(SegmentInstance(ORU_PATIENT_RESULT_PATIENT_PID, Location(EType.Segment, "PID", "PID", 2, 1, "PID[1]"), 1, Nil, hasExtra = true, "PID"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION, 1, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_ORC, Location(EType.Segment, "ORC", "ORC", 3, 1, "ORC[1]"), 1, Nil, hasExtra = true, "ORC"),
	SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBR, Location(EType.Segment, "OBR", "OBR", 4, 1, "OBR[1]"), 1, Nil, hasExtra = true, "OBR"),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 1, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 5, 1, "OBX[1]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 2, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 6, 1, "OBX[2]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 3, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 7, 1, "OBX[3]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 4, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 8, 1, "OBX[4]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 5, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 9, 1, "OBX[5]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 6, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 10, 1, "OBX[6]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 7, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 11, 1, "OBX[7]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 8, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 12, 1, "OBX[8]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 9, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 13, 1, "OBX[9]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 10, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 14, 1, "OBX[10]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 11, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 15, 1, "OBX[11]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 12, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 16, 1, "OBX[12]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 13, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 17, 1, "OBX[13]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 14, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 18, 1, "OBX[14]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 15, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 19, 1, "OBX[15]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 16, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 20, 1, "OBX[16]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 17, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 21, 1, "OBX[17]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 18, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 22, 1, "OBX[18]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 19, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 23, 1, "OBX[19]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 20, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 24, 1, "OBX[20]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 21, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 25, 1, "OBX[21]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 22, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 26, 1, "OBX[22]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 23, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 27, 1, "OBX[23]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 24, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 28, 1, "OBX[24]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 25, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 29, 1, "OBX[25]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 26, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 30, 1, "OBX[26]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 27, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 31, 1, "OBX[27]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 28, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 32, 1, "OBX[28]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_SPECIMEN, 1, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_SPECIMEN_SPM, Location(EType.Segment, "SPM", "SPM", 33, 1, "SPM[1]"), 1, Nil, hasExtra = true, "SPM")))))))))
	val CULTURE_AND_SUSCEPTIBILITY_2_ORU = ("""/MSH
	/PID
	/ORC
	/OBR
	/OBX
	/OBX
	/OBX
	/SPM
	/ORC
	/OBR
	/OBX
	/OBX
	/OBX
	/ORC
	/OBR
	/OBX
	/OBX
	/OBX
	/ORC
	/OBR
	/OBX
	/OBX
	/OBX""".stripMargin('/'), List(SegmentInstance(ORU_MSH, Location(EType.Segment, "MSH", "MSH", 1, 1, "MSH[1]"), 1, Nil, hasExtra = true, "MSH"),
	GroupInstance(ORU_PATIENT_RESULT, 1, List(GroupInstance(ORU_PATIENT_RESULT_PATIENT, 1, List(SegmentInstance(ORU_PATIENT_RESULT_PATIENT_PID, Location(EType.Segment, "PID", "PID", 2, 1, "PID[1]"), 1, Nil, hasExtra = true, "PID"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION, 1, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_ORC, Location(EType.Segment, "ORC", "ORC", 3, 1, "ORC[1]"), 1, Nil, hasExtra = true, "ORC"),
	SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBR, Location(EType.Segment, "OBR", "OBR", 4, 1, "OBR[1]"), 1, Nil, hasExtra = true, "OBR"),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 1, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 5, 1, "OBX[1]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 2, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 6, 1, "OBX[2]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 3, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 7, 1, "OBX[3]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_SPECIMEN, 1, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_SPECIMEN_SPM, Location(EType.Segment, "SPM", "SPM", 8, 1, "SPM[1]"), 1, Nil, hasExtra = true, "SPM"))))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION, 2, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_ORC, Location(EType.Segment, "ORC", "ORC", 9, 1, "ORC[2]"), 1, Nil, hasExtra = true, "ORC"),
	SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBR, Location(EType.Segment, "OBR", "OBR", 10, 1, "OBR[2]"), 1, Nil, hasExtra = true, "OBR"),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 1, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 11, 1, "OBX[4]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 2, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 12, 1, "OBX[5]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 3, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 13, 1, "OBX[6]"), 1, Nil, hasExtra = true, "OBX"))))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION, 3, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_ORC, Location(EType.Segment, "ORC", "ORC", 14, 1, "ORC[3]"), 1, Nil, hasExtra = true, "ORC"),
	SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBR, Location(EType.Segment, "OBR", "OBR", 15, 1, "OBR[3]"), 1, Nil, hasExtra = true, "OBR"),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 1, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 16, 1, "OBX[7]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 2, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 17, 1, "OBX[8]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 3, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 18, 1, "OBX[9]"), 1, Nil, hasExtra = true, "OBX"))))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION, 4, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_ORC, Location(EType.Segment, "ORC", "ORC", 19, 1, "ORC[4]"), 1, Nil, hasExtra = true, "ORC"),
	SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBR, Location(EType.Segment, "OBR", "OBR", 20, 1, "OBR[4]"), 1, Nil, hasExtra = true, "OBR"),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 1, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 21, 1, "OBX[10]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 2, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 22, 1, "OBX[11]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 3, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 23, 1, "OBX[12]"), 1, Nil, hasExtra = true, "OBX")))))))))
	val REFLEX_1_ORU = ("""/MSH
	/PID
	/ORC
	/OBR
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/NTE
	/SPM
	/ORC
	/OBR
	/OBX""".stripMargin('/'), List(SegmentInstance(ORU_MSH, Location(EType.Segment, "MSH", "MSH", 1, 1, "MSH[1]"), 1, Nil, hasExtra = true, "MSH"),
	GroupInstance(ORU_PATIENT_RESULT, 1, List(GroupInstance(ORU_PATIENT_RESULT_PATIENT, 1, List(SegmentInstance(ORU_PATIENT_RESULT_PATIENT_PID, Location(EType.Segment, "PID", "PID", 2, 1, "PID[1]"), 1, Nil, hasExtra = true, "PID"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION, 1, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_ORC, Location(EType.Segment, "ORC", "ORC", 3, 1, "ORC[1]"), 1, Nil, hasExtra = true, "ORC"),
	SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBR, Location(EType.Segment, "OBR", "OBR", 4, 1, "OBR[1]"), 1, Nil, hasExtra = true, "OBR"),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 1, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 5, 1, "OBX[1]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 2, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 6, 1, "OBX[2]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 3, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 7, 1, "OBX[3]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 4, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 8, 1, "OBX[4]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 5, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 9, 1, "OBX[5]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 6, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 10, 1, "OBX[6]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 7, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 11, 1, "OBX[7]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 8, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 12, 1, "OBX[8]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 9, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 13, 1, "OBX[9]"), 1, Nil, hasExtra = true, "OBX"),
	SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_NTE, Location(EType.Segment, "NTE", "NTE", 14, 1, "NTE[1]"), 1, Nil, hasExtra = true, "NTE"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_SPECIMEN, 1, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_SPECIMEN_SPM, Location(EType.Segment, "SPM", "SPM", 15, 1, "SPM[1]"), 1, Nil, hasExtra = true, "SPM"))))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION, 2, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_ORC, Location(EType.Segment, "ORC", "ORC", 16, 1, "ORC[2]"), 1, Nil, hasExtra = true, "ORC"),
	SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBR, Location(EType.Segment, "OBR", "OBR", 17, 1, "OBR[2]"), 1, Nil, hasExtra = true, "OBR"),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 1, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 18, 1, "OBX[10]"), 1, Nil, hasExtra = true, "OBX")))))))))
	val REFLEX_1__NO_ORC_ORU = ("""/MSH
	/PID
	/OBR
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/NTE
	/SPM
	/OBR
	/OBX""".stripMargin('/'), List(SegmentInstance(ORU_MSH, Location(EType.Segment, "MSH", "MSH", 1, 1, "MSH[1]"), 1, Nil, hasExtra = true, "MSH"),
	GroupInstance(ORU_PATIENT_RESULT, 1, List(GroupInstance(ORU_PATIENT_RESULT_PATIENT, 1, List(SegmentInstance(ORU_PATIENT_RESULT_PATIENT_PID, Location(EType.Segment, "PID", "PID", 2, 1, "PID[1]"), 1, Nil, hasExtra = true, "PID"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION, 1, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBR, Location(EType.Segment, "OBR", "OBR", 3, 1, "OBR[1]"), 1, Nil, hasExtra = true, "OBR"),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 1, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 4, 1, "OBX[1]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 2, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 5, 1, "OBX[2]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 3, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 6, 1, "OBX[3]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 4, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 7, 1, "OBX[4]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 5, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 8, 1, "OBX[5]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 6, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 9, 1, "OBX[6]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 7, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 10, 1, "OBX[7]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 8, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 11, 1, "OBX[8]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 9, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 12, 1, "OBX[9]"), 1, Nil, hasExtra = true, "OBX"),
	SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_NTE, Location(EType.Segment, "NTE", "NTE", 13, 1, "NTE[1]"), 1, Nil, hasExtra = true, "NTE"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_SPECIMEN, 1, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_SPECIMEN_SPM, Location(EType.Segment, "SPM", "SPM", 14, 1, "SPM[1]"), 1, Nil, hasExtra = true, "SPM"))))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION, 2, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBR, Location(EType.Segment, "OBR", "OBR", 15, 1, "OBR[2]"), 1, Nil, hasExtra = true, "OBR"),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 1, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 16, 1, "OBX[10]"), 1, Nil, hasExtra = true, "OBX")))))))))
	val REFLEX_1__THE_FIRST_ORC_ORU = ("""/MSH
	/PID
	/ORC
	/OBR
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/NTE
	/SPM
	/OBR
	/OBX""".stripMargin('/'), List(SegmentInstance(ORU_MSH, Location(EType.Segment, "MSH", "MSH", 1, 1, "MSH[1]"), 1, Nil, hasExtra = true, "MSH"),
	GroupInstance(ORU_PATIENT_RESULT, 1, List(GroupInstance(ORU_PATIENT_RESULT_PATIENT, 1, List(SegmentInstance(ORU_PATIENT_RESULT_PATIENT_PID, Location(EType.Segment, "PID", "PID", 2, 1, "PID[1]"), 1, Nil, hasExtra = true, "PID"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION, 1, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_ORC, Location(EType.Segment, "ORC", "ORC", 3, 1, "ORC[1]"), 1, Nil, hasExtra = true, "ORC"),
	SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBR, Location(EType.Segment, "OBR", "OBR", 4, 1, "OBR[1]"), 1, Nil, hasExtra = true, "OBR"),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 1, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 5, 1, "OBX[1]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 2, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 6, 1, "OBX[2]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 3, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 7, 1, "OBX[3]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 4, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 8, 1, "OBX[4]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 5, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 9, 1, "OBX[5]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 6, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 10, 1, "OBX[6]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 7, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 11, 1, "OBX[7]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 8, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 12, 1, "OBX[8]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 9, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 13, 1, "OBX[9]"), 1, Nil, hasExtra = true, "OBX"),
	SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_NTE, Location(EType.Segment, "NTE", "NTE", 14, 1, "NTE[1]"), 1, Nil, hasExtra = true, "NTE"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_SPECIMEN, 1, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_SPECIMEN_SPM, Location(EType.Segment, "SPM", "SPM", 15, 1, "SPM[1]"), 1, Nil, hasExtra = true, "SPM"))))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION, 2, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBR, Location(EType.Segment, "OBR", "OBR", 16, 1, "OBR[2]"), 1, Nil, hasExtra = true, "OBR"),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 1, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 17, 1, "OBX[10]"), 1, Nil, hasExtra = true, "OBX")))))))))
	val REFLEX_1__THE_SECOND_ORC_PRESENT_ORU = ("""/MSH
	/PID
	/OBR
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/NTE
	/SPM
	/ORC
	/OBR
	/OBX""".stripMargin('/'), List(SegmentInstance(ORU_MSH, Location(EType.Segment, "MSH", "MSH", 1, 1, "MSH[1]"), 1, Nil, hasExtra = true, "MSH"),
	GroupInstance(ORU_PATIENT_RESULT, 1, List(GroupInstance(ORU_PATIENT_RESULT_PATIENT, 1, List(SegmentInstance(ORU_PATIENT_RESULT_PATIENT_PID, Location(EType.Segment, "PID", "PID", 2, 1, "PID[1]"), 1, Nil, hasExtra = true, "PID"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION, 1, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBR, Location(EType.Segment, "OBR", "OBR", 3, 1, "OBR[1]"), 1, Nil, hasExtra = true, "OBR"),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 1, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 4, 1, "OBX[1]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 2, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 5, 1, "OBX[2]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 3, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 6, 1, "OBX[3]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 4, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 7, 1, "OBX[4]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 5, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 8, 1, "OBX[5]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 6, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 9, 1, "OBX[6]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 7, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 10, 1, "OBX[7]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 8, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 11, 1, "OBX[8]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 9, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 12, 1, "OBX[9]"), 1, Nil, hasExtra = true, "OBX"),
	SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_NTE, Location(EType.Segment, "NTE", "NTE", 13, 1, "NTE[1]"), 1, Nil, hasExtra = true, "NTE"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_SPECIMEN, 1, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_SPECIMEN_SPM, Location(EType.Segment, "SPM", "SPM", 14, 1, "SPM[1]"), 1, Nil, hasExtra = true, "SPM"))))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION, 2, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_ORC, Location(EType.Segment, "ORC", "ORC", 15, 1, "ORC[1]"), 1, Nil, hasExtra = true, "ORC"),
	SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBR, Location(EType.Segment, "OBR", "OBR", 16, 1, "OBR[2]"), 1, Nil, hasExtra = true, "OBR"),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 1, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 17, 1, "OBX[10]"), 1, Nil, hasExtra = true, "OBX")))))))))
	val CCHD_EXAMPLE_MESSAGE_ORU = ("""/MSH
	/PID
	/NK1
	/PV1
	/OBR
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX
	/OBX""".stripMargin('/'), List(SegmentInstance(ORU_MSH, Location(EType.Segment, "MSH", "MSH", 1, 1, "MSH[1]"), 1, Nil, hasExtra = true, "MSH"),
	GroupInstance(ORU_PATIENT_RESULT, 1, List(GroupInstance(ORU_PATIENT_RESULT_PATIENT, 1, List(SegmentInstance(ORU_PATIENT_RESULT_PATIENT_PID, Location(EType.Segment, "PID", "PID", 2, 1, "PID[1]"), 1, Nil, hasExtra = true, "PID"),
	SegmentInstance(ORU_PATIENT_RESULT_PATIENT_NK1, Location(EType.Segment, "NK1", "NK1", 3, 1, "NK1[1]"), 1, Nil, hasExtra = true, "NK1"),
	GroupInstance(ORU_PATIENT_RESULT_PATIENT_VISIT, 1, List(SegmentInstance(ORU_PATIENT_RESULT_PATIENT_VISIT_PV1, Location(EType.Segment, "PV1", "PV1", 4, 1, "PV1[1]"), 1, Nil, hasExtra = true, "PV1"))))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION, 1, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBR, Location(EType.Segment, "OBR", "OBR", 5, 1, "OBR[1]"), 1, Nil, hasExtra = true, "OBR"),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 1, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 6, 1, "OBX[1]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 2, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 7, 1, "OBX[2]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 3, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 8, 1, "OBX[3]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 4, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 9, 1, "OBX[4]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 5, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 10, 1, "OBX[5]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 6, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 11, 1, "OBX[6]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 7, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 12, 1, "OBX[7]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 8, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 13, 1, "OBX[8]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 9, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 14, 1, "OBX[9]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 10, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 15, 1, "OBX[10]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 11, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 16, 1, "OBX[11]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 12, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 17, 1, "OBX[12]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 13, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 18, 1, "OBX[13]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 14, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 19, 1, "OBX[14]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 15, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 20, 1, "OBX[15]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 16, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 21, 1, "OBX[16]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 17, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 22, 1, "OBX[17]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 18, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 23, 1, "OBX[18]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 19, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 24, 1, "OBX[19]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 20, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 25, 1, "OBX[20]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 21, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 26, 1, "OBX[21]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 22, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 27, 1, "OBX[22]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 23, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 28, 1, "OBX[23]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 24, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 29, 1, "OBX[24]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 25, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 30, 1, "OBX[25]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 26, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 31, 1, "OBX[26]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 27, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 32, 1, "OBX[27]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION, 28, List(SegmentInstance(ORU_PATIENT_RESULT_ORDER_OBSERVATION_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 33, 1, "OBX[28]"), 1, Nil, hasExtra = true, "OBX")))))))))
	

}
