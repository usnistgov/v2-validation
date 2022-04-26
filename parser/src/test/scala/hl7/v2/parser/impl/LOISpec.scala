package hl7.v2.parser.impl

import hl7.v2.instance.{EType, Location, SegOrGroup, Segment => SegmentInstance, Group => GroupInstance}
import hl7.v2.profile.{Group, Range, Req, SegRefOrGroup, Segment, SegmentRef, Usage}

trait LOISpec extends ParserSpecHelper {


	// Test Definitions
	def JAIMIE_VALID_MESSAGE_LOI_TEST = check(JAIMIE_VALID_MESSAGE_LOI, LOI_profile)
	def JAIMIE_VALID_MESSAGE_WITH_ORDER_PRIOR_LOI_TEST = check(JAIMIE_VALID_MESSAGE_WITH_ORDER_PRIOR_LOI, LOI_profile)
	// Resources
	val LOI_MSH = SegmentRef(Req(1, "MSH", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("MSH", "MSH", "MSH", Nil, Nil))
	val LOI_ARV = SegmentRef(Req(2, "ARV", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None), Segment("ARV", "ARV", "ARV", Nil, Nil))
	val LOI_SFT = SegmentRef(Req(3, "SFT", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None), Segment("SFT", "SFT", "SFT", Nil, Nil))
	val LOI_NTE = SegmentRef(Req(4, "NTE", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None), Segment("NTE", "NTE", "NTE", Nil, Nil))
	val LOI_PATIENT_PID = SegmentRef(Req(1, "PID", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("PID", "PID", "PID", Nil, Nil))
	val LOI_PATIENT_PD1 = SegmentRef(Req(2, "PD1", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("PD1", "PD1", "PD1", Nil, Nil))
	val LOI_PATIENT_NTE = SegmentRef(Req(3, "NTE", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None), Segment("NTE", "NTE", "NTE", Nil, Nil))
	val LOI_PATIENT_NK1 = SegmentRef(Req(4, "NK1", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None), Segment("NK1", "NK1", "NK1", Nil, Nil))
	val LOI_PATIENT_PATIENT_VISIT_PV1 = SegmentRef(Req(1, "PV1", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("PV1", "PV1", "PV1", Nil, Nil))
	val LOI_PATIENT_PATIENT_VISIT_PV2 = SegmentRef(Req(2, "PV2", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("PV2", "PV2", "PV2", Nil, Nil))
	val LOI_PATIENT_PATIENT_VISIT = Group("PATIENT_VISIT", "PATIENT_VISIT", List(LOI_PATIENT_PATIENT_VISIT_PV1,
	LOI_PATIENT_PATIENT_VISIT_PV2), Req(5, "PATIENT_VISIT", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None))
	val LOI_PATIENT_INSURANCE_IN1 = SegmentRef(Req(1, "IN1", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("IN1", "IN1", "IN1", Nil, Nil))
	val LOI_PATIENT_INSURANCE_IN2 = SegmentRef(Req(2, "IN2", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("IN2", "IN2", "IN2", Nil, Nil))
	val LOI_PATIENT_INSURANCE_IN3 = SegmentRef(Req(3, "IN3", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("IN3", "IN3", "IN3", Nil, Nil))
	val LOI_PATIENT_INSURANCE = Group("INSURANCE", "INSURANCE", List(LOI_PATIENT_INSURANCE_IN1,
	LOI_PATIENT_INSURANCE_IN2,
	LOI_PATIENT_INSURANCE_IN3), Req(6, "INSURANCE", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None))
	val LOI_PATIENT_GT1 = SegmentRef(Req(7, "GT1", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("GT1", "GT1", "GT1", Nil, Nil))
	val LOI_PATIENT_AL1 = SegmentRef(Req(8, "AL1", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None), Segment("AL1", "AL1", "AL1", Nil, Nil))
	val LOI_PATIENT = Group("PATIENT", "PATIENT", List(LOI_PATIENT_PID,
	LOI_PATIENT_PD1,
	LOI_PATIENT_NTE,
	LOI_PATIENT_NK1,
	LOI_PATIENT_PATIENT_VISIT,
	LOI_PATIENT_INSURANCE,
	LOI_PATIENT_GT1,
	LOI_PATIENT_AL1), Req(5, "PATIENT", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None))
	val LOI_ORDER_ORC = SegmentRef(Req(1, "ORC", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("ORC", "ORC", "ORC", Nil, Nil))
	val LOI_ORDER_TIMING_TQ1 = SegmentRef(Req(1, "TQ1", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("TQ1", "TQ1", "TQ1", Nil, Nil))
	val LOI_ORDER_TIMING_TQ2 = SegmentRef(Req(2, "TQ2", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None), Segment("TQ2", "TQ2", "TQ2", Nil, Nil))
	val LOI_ORDER_TIMING = Group("TIMING", "TIMING", List(LOI_ORDER_TIMING_TQ1,
	LOI_ORDER_TIMING_TQ2), Req(2, "TIMING", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None))
	val LOI_ORDER_OBSERVATION_REQUEST_OBR = SegmentRef(Req(1, "OBR", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("OBR", "OBR", "OBR", Nil, Nil))
	val LOI_ORDER_OBSERVATION_REQUEST_TCD = SegmentRef(Req(2, "TCD", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("TCD", "TCD", "TCD", Nil, Nil))
	val LOI_ORDER_OBSERVATION_REQUEST_NTE = SegmentRef(Req(3, "NTE", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None), Segment("NTE", "NTE", "NTE", Nil, Nil))
	val LOI_ORDER_OBSERVATION_REQUEST_PRT = SegmentRef(Req(4, "PRT", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None), Segment("PRT", "PRT", "PRT", Nil, Nil))
	val LOI_ORDER_OBSERVATION_REQUEST_CTD = SegmentRef(Req(5, "CTD", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("CTD", "CTD", "CTD", Nil, Nil))
	val LOI_ORDER_OBSERVATION_REQUEST_DG1 = SegmentRef(Req(6, "DG1", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None), Segment("DG1", "DG1", "DG1", Nil, Nil))
	val LOI_ORDER_OBSERVATION_REQUEST_OBSERVATION_OBX = SegmentRef(Req(1, "OBX", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("OBX", "OBX", "OBX", Nil, Nil))
	val LOI_ORDER_OBSERVATION_REQUEST_OBSERVATION_TCD = SegmentRef(Req(2, "TCD", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("TCD", "TCD", "TCD", Nil, Nil))
	val LOI_ORDER_OBSERVATION_REQUEST_OBSERVATION_NTE = SegmentRef(Req(3, "NTE", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None), Segment("NTE", "NTE", "NTE", Nil, Nil))
	val LOI_ORDER_OBSERVATION_REQUEST_OBSERVATION = Group("OBSERVATION", "OBSERVATION", List(LOI_ORDER_OBSERVATION_REQUEST_OBSERVATION_OBX,
	LOI_ORDER_OBSERVATION_REQUEST_OBSERVATION_TCD,
	LOI_ORDER_OBSERVATION_REQUEST_OBSERVATION_NTE), Req(7, "OBSERVATION", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None))
	val LOI_ORDER_OBSERVATION_REQUEST_SPECIMEN_SPM = SegmentRef(Req(1, "SPM", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("SPM", "SPM", "SPM", Nil, Nil))
	val LOI_ORDER_OBSERVATION_REQUEST_SPECIMEN_OBX = SegmentRef(Req(2, "OBX", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None), Segment("OBX", "OBX", "OBX", Nil, Nil))
	val LOI_ORDER_OBSERVATION_REQUEST_SPECIMEN_CONTAINER_SAC = SegmentRef(Req(1, "SAC", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("SAC", "SAC", "SAC", Nil, Nil))
	val LOI_ORDER_OBSERVATION_REQUEST_SPECIMEN_CONTAINER_OBX = SegmentRef(Req(2, "OBX", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("OBX", "OBX", "OBX", Nil, Nil))
	val LOI_ORDER_OBSERVATION_REQUEST_SPECIMEN_CONTAINER = Group("CONTAINER", "CONTAINER", List(LOI_ORDER_OBSERVATION_REQUEST_SPECIMEN_CONTAINER_SAC,
	LOI_ORDER_OBSERVATION_REQUEST_SPECIMEN_CONTAINER_OBX), Req(3, "CONTAINER", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None))
	val LOI_ORDER_OBSERVATION_REQUEST_SPECIMEN = Group("SPECIMEN", "SPECIMEN", List(LOI_ORDER_OBSERVATION_REQUEST_SPECIMEN_SPM,
	LOI_ORDER_OBSERVATION_REQUEST_SPECIMEN_OBX,
	LOI_ORDER_OBSERVATION_REQUEST_SPECIMEN_CONTAINER), Req(8, "SPECIMEN", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None))
	val LOI_ORDER_OBSERVATION_REQUEST_SGH = SegmentRef(Req(9, "SGH", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("SGH", "SGH", "SGH", Nil, Nil))
	val LOI_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_PATIENT_PRIOR_PID = SegmentRef(Req(1, "PID", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("PID", "PID", "PID", Nil, Nil))
	val LOI_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_PATIENT_PRIOR_PD1 = SegmentRef(Req(2, "PD1", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("PD1", "PD1", "PD1", Nil, Nil))
	val LOI_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_PATIENT_PRIOR = Group("PATIENT_PRIOR", "PATIENT_PRIOR", List(LOI_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_PATIENT_PRIOR_PID,
	LOI_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_PATIENT_PRIOR_PD1), Req(1, "PATIENT_PRIOR", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None))
	val LOI_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_PATIENT_VISIT_PRIOR_PV1 = SegmentRef(Req(1, "PV1", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("PV1", "PV1", "PV1", Nil, Nil))
	val LOI_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_PATIENT_VISIT_PRIOR_PV2 = SegmentRef(Req(2, "PV2", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("PV2", "PV2", "PV2", Nil, Nil))
	val LOI_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_PATIENT_VISIT_PRIOR = Group("PATIENT_VISIT_PRIOR", "PATIENT_VISIT_PRIOR", List(LOI_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_PATIENT_VISIT_PRIOR_PV1,
	LOI_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_PATIENT_VISIT_PRIOR_PV2), Req(2, "PATIENT_VISIT_PRIOR", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None))
	val LOI_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_AL1 = SegmentRef(Req(3, "AL1", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None), Segment("AL1", "AL1", "AL1", Nil, Nil))
	val LOI_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_ORC = SegmentRef(Req(1, "ORC", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("ORC", "ORC", "ORC", Nil, Nil))
	val LOI_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_OBR = SegmentRef(Req(2, "OBR", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("OBR", "OBR", "OBR", Nil, Nil))
	val LOI_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_NTE = SegmentRef(Req(3, "NTE", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None), Segment("NTE", "NTE", "NTE", Nil, Nil))
	val LOI_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_TIMING_PRIOR_TQ1 = SegmentRef(Req(1, "TQ1", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("TQ1", "TQ1", "TQ1", Nil, Nil))
	val LOI_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_TIMING_PRIOR_TQ2 = SegmentRef(Req(2, "TQ2", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None), Segment("TQ2", "TQ2", "TQ2", Nil, Nil))
	val LOI_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_TIMING_PRIOR = Group("TIMING_PRIOR", "TIMING_PRIOR", List(LOI_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_TIMING_PRIOR_TQ1,
	LOI_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_TIMING_PRIOR_TQ2), Req(4, "TIMING_PRIOR", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None))
	val LOI_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_OBSERVATION_PRIOR_OBX = SegmentRef(Req(1, "OBX", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("OBX", "OBX", "OBX", Nil, Nil))
	val LOI_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_OBSERVATION_PRIOR_NTE = SegmentRef(Req(2, "NTE", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None), Segment("NTE", "NTE", "NTE", Nil, Nil))
	val LOI_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_OBSERVATION_PRIOR = Group("OBSERVATION_PRIOR", "OBSERVATION_PRIOR", List(LOI_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_OBSERVATION_PRIOR_OBX,
	LOI_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_OBSERVATION_PRIOR_NTE), Req(5, "OBSERVATION_PRIOR", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None))
	val LOI_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR = Group("ORDER_PRIOR", "ORDER_PRIOR", List(LOI_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_ORC,
	LOI_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_OBR,
	LOI_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_NTE,
	LOI_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_TIMING_PRIOR,
	LOI_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_OBSERVATION_PRIOR), Req(4, "ORDER_PRIOR", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None))
	val LOI_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT = Group("PRIOR_RESULT", "PRIOR_RESULT", List(LOI_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_PATIENT_PRIOR,
	LOI_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_PATIENT_VISIT_PRIOR,
	LOI_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_AL1,
	LOI_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR), Req(10, "PRIOR_RESULT", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None))
	val LOI_ORDER_OBSERVATION_REQUEST_SGT = SegmentRef(Req(11, "SGT", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("SGT", "SGT", "SGT", Nil, Nil))
	val LOI_ORDER_OBSERVATION_REQUEST = Group("OBSERVATION_REQUEST", "OBSERVATION_REQUEST", List(LOI_ORDER_OBSERVATION_REQUEST_OBR,
	LOI_ORDER_OBSERVATION_REQUEST_TCD,
	LOI_ORDER_OBSERVATION_REQUEST_NTE,
	LOI_ORDER_OBSERVATION_REQUEST_PRT,
	LOI_ORDER_OBSERVATION_REQUEST_CTD,
	LOI_ORDER_OBSERVATION_REQUEST_DG1,
	LOI_ORDER_OBSERVATION_REQUEST_OBSERVATION,
	LOI_ORDER_OBSERVATION_REQUEST_SPECIMEN,
	LOI_ORDER_OBSERVATION_REQUEST_SGH,
	LOI_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT,
	LOI_ORDER_OBSERVATION_REQUEST_SGT), Req(3, "OBSERVATION_REQUEST", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None))
	val LOI_ORDER_FT1 = SegmentRef(Req(4, "FT1", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None), Segment("FT1", "FT1", "FT1", Nil, Nil))
	val LOI_ORDER_CTI = SegmentRef(Req(5, "CTI", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None), Segment("CTI", "CTI", "CTI", Nil, Nil))
	val LOI_ORDER_BLG = SegmentRef(Req(6, "BLG", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("BLG", "BLG", "BLG", Nil, Nil))
	val LOI_ORDER = Group("ORDER", "ORDER", List(LOI_ORDER_ORC,
	LOI_ORDER_TIMING,
	LOI_ORDER_OBSERVATION_REQUEST,
	LOI_ORDER_FT1,
	LOI_ORDER_CTI,
	LOI_ORDER_BLG), Req(6, "ORDER", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None))
	// Profile
	val LOI_profile: List[SegRefOrGroup] = List(LOI_MSH,
	LOI_ARV,
	LOI_SFT,
	LOI_NTE,
	LOI_PATIENT,
	LOI_ORDER)
	// Mocks
	val JAIMIE_VALID_MESSAGE_LOI = ("""/MSH
	/PID
	/ORC
	/OBR
	/SPM
	/SGT
	/ORC
	/OBR
	/SPM
	/SGT""".stripMargin('/'), List(SegmentInstance(LOI_MSH, Location(EType.Segment, "MSH", "MSH", 1, 1, "MSH[1]"), 1, Nil, hasExtra = true, "MSH"),
	GroupInstance(LOI_PATIENT, 1, List(SegmentInstance(LOI_PATIENT_PID, Location(EType.Segment, "PID", "PID", 2, 1, "PID[1]"), 1, Nil, hasExtra = true, "PID"))),
	GroupInstance(LOI_ORDER, 1, List(SegmentInstance(LOI_ORDER_ORC, Location(EType.Segment, "ORC", "ORC", 3, 1, "ORC[1]"), 1, Nil, hasExtra = true, "ORC"),
	GroupInstance(LOI_ORDER_OBSERVATION_REQUEST, 1, List(SegmentInstance(LOI_ORDER_OBSERVATION_REQUEST_OBR, Location(EType.Segment, "OBR", "OBR", 4, 1, "OBR[1]"), 1, Nil, hasExtra = true, "OBR"),
	GroupInstance(LOI_ORDER_OBSERVATION_REQUEST_SPECIMEN, 1, List(SegmentInstance(LOI_ORDER_OBSERVATION_REQUEST_SPECIMEN_SPM, Location(EType.Segment, "SPM", "SPM", 5, 1, "SPM[1]"), 1, Nil, hasExtra = true, "SPM"))),
	SegmentInstance(LOI_ORDER_OBSERVATION_REQUEST_SGT, Location(EType.Segment, "SGT", "SGT", 6, 1, "SGT[1]"), 1, Nil, hasExtra = true, "SGT"))))),
	GroupInstance(LOI_ORDER, 2, List(SegmentInstance(LOI_ORDER_ORC, Location(EType.Segment, "ORC", "ORC", 7, 1, "ORC[2]"), 1, Nil, hasExtra = true, "ORC"),
	GroupInstance(LOI_ORDER_OBSERVATION_REQUEST, 1, List(SegmentInstance(LOI_ORDER_OBSERVATION_REQUEST_OBR, Location(EType.Segment, "OBR", "OBR", 8, 1, "OBR[2]"), 1, Nil, hasExtra = true, "OBR"),
	GroupInstance(LOI_ORDER_OBSERVATION_REQUEST_SPECIMEN, 1, List(SegmentInstance(LOI_ORDER_OBSERVATION_REQUEST_SPECIMEN_SPM, Location(EType.Segment, "SPM", "SPM", 9, 1, "SPM[2]"), 1, Nil, hasExtra = true, "SPM"))),
	SegmentInstance(LOI_ORDER_OBSERVATION_REQUEST_SGT, Location(EType.Segment, "SGT", "SGT", 10, 1, "SGT[2]"), 1, Nil, hasExtra = true, "SGT")))))))
	val JAIMIE_VALID_MESSAGE_WITH_ORDER_PRIOR_LOI = ("""/MSH
	/PID
	/ORC
	/OBR
	/SPM
	/ORC
	/OBR
	/SGT
	/ORC
	/OBR
	/SPM
	/SGT""".stripMargin('/'), List(SegmentInstance(LOI_MSH, Location(EType.Segment, "MSH", "MSH", 1, 1, "MSH[1]"), 1, Nil, hasExtra = true, "MSH"),
	GroupInstance(LOI_PATIENT, 1, List(SegmentInstance(LOI_PATIENT_PID, Location(EType.Segment, "PID", "PID", 2, 1, "PID[1]"), 1, Nil, hasExtra = true, "PID"))),
	GroupInstance(LOI_ORDER, 1, List(SegmentInstance(LOI_ORDER_ORC, Location(EType.Segment, "ORC", "ORC", 3, 1, "ORC[1]"), 1, Nil, hasExtra = true, "ORC"),
	GroupInstance(LOI_ORDER_OBSERVATION_REQUEST, 1, List(SegmentInstance(LOI_ORDER_OBSERVATION_REQUEST_OBR, Location(EType.Segment, "OBR", "OBR", 4, 1, "OBR[1]"), 1, Nil, hasExtra = true, "OBR"),
	GroupInstance(LOI_ORDER_OBSERVATION_REQUEST_SPECIMEN, 1, List(SegmentInstance(LOI_ORDER_OBSERVATION_REQUEST_SPECIMEN_SPM, Location(EType.Segment, "SPM", "SPM", 5, 1, "SPM[1]"), 1, Nil, hasExtra = true, "SPM"))),
	GroupInstance(LOI_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT, 1, List(GroupInstance(LOI_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR, 1, List(SegmentInstance(LOI_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_ORC, Location(EType.Segment, "ORC", "ORC", 6, 1, "ORC[2]"), 1, Nil, hasExtra = true, "ORC"),
	SegmentInstance(LOI_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_OBR, Location(EType.Segment, "OBR", "OBR", 7, 1, "OBR[2]"), 1, Nil, hasExtra = true, "OBR"))))),
	SegmentInstance(LOI_ORDER_OBSERVATION_REQUEST_SGT, Location(EType.Segment, "SGT", "SGT", 8, 1, "SGT[1]"), 1, Nil, hasExtra = true, "SGT"))))),
	GroupInstance(LOI_ORDER, 2, List(SegmentInstance(LOI_ORDER_ORC, Location(EType.Segment, "ORC", "ORC", 9, 1, "ORC[3]"), 1, Nil, hasExtra = true, "ORC"),
	GroupInstance(LOI_ORDER_OBSERVATION_REQUEST, 1, List(SegmentInstance(LOI_ORDER_OBSERVATION_REQUEST_OBR, Location(EType.Segment, "OBR", "OBR", 10, 1, "OBR[3]"), 1, Nil, hasExtra = true, "OBR"),
	GroupInstance(LOI_ORDER_OBSERVATION_REQUEST_SPECIMEN, 1, List(SegmentInstance(LOI_ORDER_OBSERVATION_REQUEST_SPECIMEN_SPM, Location(EType.Segment, "SPM", "SPM", 11, 1, "SPM[2]"), 1, Nil, hasExtra = true, "SPM"))),
	SegmentInstance(LOI_ORDER_OBSERVATION_REQUEST_SGT, Location(EType.Segment, "SGT", "SGT", 12, 1, "SGT[2]"), 1, Nil, hasExtra = true, "SGT")))))))
	

}
