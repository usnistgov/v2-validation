package hl7.v2.parser.impl

import hl7.v2.instance.{EType, Location, SegOrGroup, Segment => SegmentInstance, Group => GroupInstance}
import hl7.v2.profile.{Group, Range, Req, SegRefOrGroup, Segment, SegmentRef, Usage}

trait LOIv1Spec extends ParserSpecHelper {


	// Test Definitions
	def PT_LOIV1_TEST = check(PT_LOIV1, LOIv1_profile)
	def SED_RATE_LOIV1_TEST = check(SED_RATE_LOIV1, LOIv1_profile)
	def CBC_LOIV1_TEST = check(CBC_LOIV1, LOIv1_profile)
	def LIPID_PANEL_LOIV1_TEST = check(LIPID_PANEL_LOIV1, LOIv1_profile)
	def LIPID_PANEL_FI_LOIV1_TEST = check(LIPID_PANEL_FI_LOIV1, LOIv1_profile)
	def CULTURE_AND_SUSCEP_LOIV1_TEST = check(CULTURE_AND_SUSCEP_LOIV1, LOIv1_profile)
	def REFLEX_HEPATITIS_LOIV1_TEST = check(REFLEX_HEPATITIS_LOIV1, LOIv1_profile)
	def PAP_SMEAR_LOIV1_TEST = check(PAP_SMEAR_LOIV1, LOIv1_profile)
	def GHP_LOIV1_TEST = check(GHP_LOIV1, LOIv1_profile)
	def CREATININE_CLEARANCE_LOIV1_TEST = check(CREATININE_CLEARANCE_LOIV1, LOIv1_profile)
	def PROSTATE_BIOPSY_LOIV1_TEST = check(PROSTATE_BIOPSY_LOIV1, LOIv1_profile)
	def ALL_SEGMENTS__NO_REPS_LOIV1_TEST = check(ALL_SEGMENTS__NO_REPS_LOIV1, LOIv1_profile)
	def ALL_SEGMENTS__SEGMENT_REPETITIONS_LOIV1_TEST = check(ALL_SEGMENTS__SEGMENT_REPETITIONS_LOIV1, LOIv1_profile)
	def MULTIPLE_GROUPS_REPS_LOIV1_TEST = check(MULTIPLE_GROUPS_REPS_LOIV1, LOIv1_profile)
	def ONE_ORDER_GROUP_WITH_PRIOR_RESULTS_LOIV1_TEST = check(ONE_ORDER_GROUP_WITH_PRIOR_RESULTS_LOIV1, LOIv1_profile)
	def ONE_ORDER_GROUP_WITH_MULTIPLE_PRIOR_RESULTS_LOIV1_TEST = check(ONE_ORDER_GROUP_WITH_MULTIPLE_PRIOR_RESULTS_LOIV1, LOIv1_profile)
	def ONE_ORDER__ONE_PRIOR_RESULT__MULTIPLE_ORDER_PRIOR_LOIV1_TEST = check(ONE_ORDER__ONE_PRIOR_RESULT__MULTIPLE_ORDER_PRIOR_LOIV1, LOIv1_profile)
	// Resources
	val LOIv1_MSH = SegmentRef(Req(1, "MSH", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("MSH", "MSH", "MSH", Nil, Nil))
	val LOIv1_SFT = SegmentRef(Req(2, "SFT", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None), Segment("SFT", "SFT", "SFT", Nil, Nil))
	val LOIv1_NTE = SegmentRef(Req(3, "NTE", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None), Segment("NTE", "NTE", "NTE", Nil, Nil))
	val LOIv1_PATIENT_PID = SegmentRef(Req(1, "PID", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("PID", "PID", "PID", Nil, Nil))
	val LOIv1_PATIENT_PD1 = SegmentRef(Req(2, "PD1", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("PD1", "PD1", "PD1", Nil, Nil))
	val LOIv1_PATIENT_NTE = SegmentRef(Req(3, "NTE", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None), Segment("NTE", "NTE", "NTE", Nil, Nil))
	val LOIv1_PATIENT_NK1 = SegmentRef(Req(4, "NK1", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None), Segment("NK1", "NK1", "NK1", Nil, Nil))
	val LOIv1_PATIENT_PATIENT_VISIT_PV1 = SegmentRef(Req(1, "PV1", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("PV1", "PV1", "PV1", Nil, Nil))
	val LOIv1_PATIENT_PATIENT_VISIT_PV2 = SegmentRef(Req(2, "PV2", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("PV2", "PV2", "PV2", Nil, Nil))
	val LOIv1_PATIENT_PATIENT_VISIT = Group("PATIENT_VISIT", "PATIENT_VISIT", List(LOIv1_PATIENT_PATIENT_VISIT_PV1,
	LOIv1_PATIENT_PATIENT_VISIT_PV2), Req(5, "PATIENT_VISIT", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None))
	val LOIv1_PATIENT_INSURANCE_IN1 = SegmentRef(Req(1, "IN1", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("IN1", "IN1", "IN1", Nil, Nil))
	val LOIv1_PATIENT_INSURANCE_IN2 = SegmentRef(Req(2, "IN2", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("IN2", "IN2", "IN2", Nil, Nil))
	val LOIv1_PATIENT_INSURANCE_IN3 = SegmentRef(Req(3, "IN3", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("IN3", "IN3", "IN3", Nil, Nil))
	val LOIv1_PATIENT_INSURANCE = Group("INSURANCE", "INSURANCE", List(LOIv1_PATIENT_INSURANCE_IN1,
	LOIv1_PATIENT_INSURANCE_IN2,
	LOIv1_PATIENT_INSURANCE_IN3), Req(6, "INSURANCE", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None))
	val LOIv1_PATIENT_GT1 = SegmentRef(Req(7, "GT1", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("GT1", "GT1", "GT1", Nil, Nil))
	val LOIv1_PATIENT_AL1 = SegmentRef(Req(8, "AL1", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None), Segment("AL1", "AL1", "AL1", Nil, Nil))
	val LOIv1_PATIENT = Group("PATIENT", "PATIENT", List(LOIv1_PATIENT_PID,
	LOIv1_PATIENT_PD1,
	LOIv1_PATIENT_NTE,
	LOIv1_PATIENT_NK1,
	LOIv1_PATIENT_PATIENT_VISIT,
	LOIv1_PATIENT_INSURANCE,
	LOIv1_PATIENT_GT1,
	LOIv1_PATIENT_AL1), Req(4, "PATIENT", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None))
	val LOIv1_ORDER_ORC = SegmentRef(Req(1, "ORC", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("ORC", "ORC", "ORC", Nil, Nil))
	val LOIv1_ORDER_TIMING_TQ1 = SegmentRef(Req(1, "TQ1", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("TQ1", "TQ1", "TQ1", Nil, Nil))
	val LOIv1_ORDER_TIMING_TQ2 = SegmentRef(Req(2, "TQ2", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None), Segment("TQ2", "TQ2", "TQ2", Nil, Nil))
	val LOIv1_ORDER_TIMING = Group("TIMING", "TIMING", List(LOIv1_ORDER_TIMING_TQ1,
	LOIv1_ORDER_TIMING_TQ2), Req(2, "TIMING", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None))
	val LOIv1_ORDER_OBSERVATION_REQUEST_OBR = SegmentRef(Req(1, "OBR", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("OBR", "OBR", "OBR", Nil, Nil))
	val LOIv1_ORDER_OBSERVATION_REQUEST_TCD = SegmentRef(Req(2, "TCD", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("TCD", "TCD", "TCD", Nil, Nil))
	val LOIv1_ORDER_OBSERVATION_REQUEST_NTE = SegmentRef(Req(3, "NTE", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None), Segment("NTE", "NTE", "NTE", Nil, Nil))
	val LOIv1_ORDER_OBSERVATION_REQUEST_PRT = SegmentRef(Req(4, "PRT", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None), Segment("PRT", "PRT", "PRT", Nil, Nil))
	val LOIv1_ORDER_OBSERVATION_REQUEST_CTD = SegmentRef(Req(5, "CTD", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("CTD", "CTD", "CTD", Nil, Nil))
	val LOIv1_ORDER_OBSERVATION_REQUEST_DG1 = SegmentRef(Req(6, "DG1", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None), Segment("DG1", "DG1", "DG1", Nil, Nil))
	val LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION_OBX = SegmentRef(Req(1, "OBX", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("OBX", "OBX", "OBX", Nil, Nil))
	val LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION_TCD = SegmentRef(Req(2, "TCD", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("TCD", "TCD", "TCD", Nil, Nil))
	val LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION_NTE = SegmentRef(Req(3, "NTE", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None), Segment("NTE", "NTE", "NTE", Nil, Nil))
	val LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION = Group("OBSERVATION", "OBSERVATION", List(LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION_OBX,
	LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION_TCD,
	LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION_NTE), Req(7, "OBSERVATION", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None))
	val LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN_SPM = SegmentRef(Req(1, "SPM", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("SPM", "SPM", "SPM", Nil, Nil))
	val LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN_OBX = SegmentRef(Req(2, "OBX", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None), Segment("OBX", "OBX", "OBX", Nil, Nil))
	val LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN_CONTAINER_SAC = SegmentRef(Req(1, "SAC", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("SAC", "SAC", "SAC", Nil, Nil))
	val LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN_CONTAINER_OBX = SegmentRef(Req(2, "OBX", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("OBX", "OBX", "OBX", Nil, Nil))
	val LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN_CONTAINER = Group("CONTAINER", "CONTAINER", List(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN_CONTAINER_SAC,
	LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN_CONTAINER_OBX), Req(3, "CONTAINER", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None))
	val LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN = Group("SPECIMEN", "SPECIMEN", List(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN_SPM,
	LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN_OBX,
	LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN_CONTAINER), Req(8, "SPECIMEN", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None))
	val LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_SGH = SegmentRef(Req(1, "SGH", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("SGH", "SGH", "SGH", Nil, Nil))
	val LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_PATIENT_PRIOR_PID = SegmentRef(Req(1, "PID", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("PID", "PID", "PID", Nil, Nil))
	val LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_PATIENT_PRIOR_PD1 = SegmentRef(Req(2, "PD1", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("PD1", "PD1", "PD1", Nil, Nil))
	val LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_PATIENT_PRIOR = Group("PATIENT_PRIOR", "PATIENT_PRIOR", List(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_PATIENT_PRIOR_PID,
	LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_PATIENT_PRIOR_PD1), Req(2, "PATIENT_PRIOR", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None))
	val LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_PATIENT_VISIT_PRIOR_PV1 = SegmentRef(Req(1, "PV1", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("PV1", "PV1", "PV1", Nil, Nil))
	val LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_PATIENT_VISIT_PRIOR_PV2 = SegmentRef(Req(2, "PV2", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("PV2", "PV2", "PV2", Nil, Nil))
	val LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_PATIENT_VISIT_PRIOR = Group("PATIENT_VISIT_PRIOR", "PATIENT_VISIT_PRIOR", List(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_PATIENT_VISIT_PRIOR_PV1,
	LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_PATIENT_VISIT_PRIOR_PV2), Req(3, "PATIENT_VISIT_PRIOR", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None))
	val LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_AL1 = SegmentRef(Req(4, "AL1", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None), Segment("AL1", "AL1", "AL1", Nil, Nil))
	val LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_ORC = SegmentRef(Req(1, "ORC", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("ORC", "ORC", "ORC", Nil, Nil))
	val LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_OBR = SegmentRef(Req(2, "OBR", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("OBR", "OBR", "OBR", Nil, Nil))
	val LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_NTE = SegmentRef(Req(3, "NTE", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None), Segment("NTE", "NTE", "NTE", Nil, Nil))
	val LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_TIMING_PRIOR_TQ1 = SegmentRef(Req(1, "TQ1", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("TQ1", "TQ1", "TQ1", Nil, Nil))
	val LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_TIMING_PRIOR_TQ2 = SegmentRef(Req(2, "TQ2", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None), Segment("TQ2", "TQ2", "TQ2", Nil, Nil))
	val LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_TIMING_PRIOR = Group("TIMING_PRIOR", "TIMING_PRIOR", List(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_TIMING_PRIOR_TQ1,
	LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_TIMING_PRIOR_TQ2), Req(4, "TIMING_PRIOR", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None))
	val LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_OBSERVATION_PRIOR_OBX = SegmentRef(Req(1, "OBX", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("OBX", "OBX", "OBX", Nil, Nil))
	val LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_OBSERVATION_PRIOR_NTE = SegmentRef(Req(2, "NTE", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None), Segment("NTE", "NTE", "NTE", Nil, Nil))
	val LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_OBSERVATION_PRIOR = Group("OBSERVATION_PRIOR", "OBSERVATION_PRIOR", List(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_OBSERVATION_PRIOR_OBX,
	LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_OBSERVATION_PRIOR_NTE), Req(5, "OBSERVATION_PRIOR", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None))
	val LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR = Group("ORDER_PRIOR", "ORDER_PRIOR", List(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_ORC,
	LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_OBR,
	LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_NTE,
	LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_TIMING_PRIOR,
	LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_OBSERVATION_PRIOR), Req(5, "ORDER_PRIOR", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None))
	val LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_SGT = SegmentRef(Req(6, "SGT", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("SGT", "SGT", "SGT", Nil, Nil))
	val LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT = Group("PRIOR_RESULT", "PRIOR_RESULT", List(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_SGH,
	LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_PATIENT_PRIOR,
	LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_PATIENT_VISIT_PRIOR,
	LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_AL1,
	LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR,
	LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_SGT), Req(9, "PRIOR_RESULT", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None))
	val LOIv1_ORDER_OBSERVATION_REQUEST = Group("OBSERVATION_REQUEST", "OBSERVATION_REQUEST", List(LOIv1_ORDER_OBSERVATION_REQUEST_OBR,
	LOIv1_ORDER_OBSERVATION_REQUEST_TCD,
	LOIv1_ORDER_OBSERVATION_REQUEST_NTE,
	LOIv1_ORDER_OBSERVATION_REQUEST_PRT,
	LOIv1_ORDER_OBSERVATION_REQUEST_CTD,
	LOIv1_ORDER_OBSERVATION_REQUEST_DG1,
	LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION,
	LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN,
	LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT), Req(3, "OBSERVATION_REQUEST", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None))
	val LOIv1_ORDER_FT1 = SegmentRef(Req(4, "FT1", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None), Segment("FT1", "FT1", "FT1", Nil, Nil))
	val LOIv1_ORDER_CTI = SegmentRef(Req(5, "CTI", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None), Segment("CTI", "CTI", "CTI", Nil, Nil))
	val LOIv1_ORDER_BLG = SegmentRef(Req(6, "BLG", Usage.O, Some(Range(1, "1")), None, None, Nil, None, hide = false, None), Segment("BLG", "BLG", "BLG", Nil, Nil))
	val LOIv1_ORDER = Group("ORDER", "ORDER", List(LOIv1_ORDER_ORC,
	LOIv1_ORDER_TIMING,
	LOIv1_ORDER_OBSERVATION_REQUEST,
	LOIv1_ORDER_FT1,
	LOIv1_ORDER_CTI,
	LOIv1_ORDER_BLG), Req(5, "ORDER", Usage.O, Some(Range(1, "*")), None, None, Nil, None, hide = false, None))
	// Profile
	val LOIv1_profile: List[SegRefOrGroup] = List(LOIv1_MSH,
	LOIv1_SFT,
	LOIv1_NTE,
	LOIv1_PATIENT,
	LOIv1_ORDER)
	// Mocks
	val PT_LOIV1 = ("""/MSH
	/PID
	/ORC
	/OBR
	/DG1""".stripMargin('/'), List(SegmentInstance(LOIv1_MSH, Location(EType.Segment, "MSH", "MSH", 1, 1, "MSH[1]"), 1, Nil, hasExtra = true, "MSH"),
	GroupInstance(LOIv1_PATIENT, 1, List(SegmentInstance(LOIv1_PATIENT_PID, Location(EType.Segment, "PID", "PID", 2, 1, "PID[1]"), 1, Nil, hasExtra = true, "PID"))),
	GroupInstance(LOIv1_ORDER, 1, List(SegmentInstance(LOIv1_ORDER_ORC, Location(EType.Segment, "ORC", "ORC", 3, 1, "ORC[1]"), 1, Nil, hasExtra = true, "ORC"),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBR, Location(EType.Segment, "OBR", "OBR", 4, 1, "OBR[1]"), 1, Nil, hasExtra = true, "OBR"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_DG1, Location(EType.Segment, "DG1", "DG1", 5, 1, "DG1[1]"), 1, Nil, hasExtra = true, "DG1")))))))
	val SED_RATE_LOIV1 = ("""/MSH
	/PID
	/ORC
	/TQ1
	/OBR
	/NTE
	/NTE
	/PRT
	/PRT
	/DG1""".stripMargin('/'), List(SegmentInstance(LOIv1_MSH, Location(EType.Segment, "MSH", "MSH", 1, 1, "MSH[1]"), 1, Nil, hasExtra = true, "MSH"),
	GroupInstance(LOIv1_PATIENT, 1, List(SegmentInstance(LOIv1_PATIENT_PID, Location(EType.Segment, "PID", "PID", 2, 1, "PID[1]"), 1, Nil, hasExtra = true, "PID"))),
	GroupInstance(LOIv1_ORDER, 1, List(SegmentInstance(LOIv1_ORDER_ORC, Location(EType.Segment, "ORC", "ORC", 3, 1, "ORC[1]"), 1, Nil, hasExtra = true, "ORC"),
	GroupInstance(LOIv1_ORDER_TIMING, 1, List(SegmentInstance(LOIv1_ORDER_TIMING_TQ1, Location(EType.Segment, "TQ1", "TQ1", 4, 1, "TQ1[1]"), 1, Nil, hasExtra = true, "TQ1"))),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBR, Location(EType.Segment, "OBR", "OBR", 5, 1, "OBR[1]"), 1, Nil, hasExtra = true, "OBR"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_NTE, Location(EType.Segment, "NTE", "NTE", 6, 1, "NTE[1]"), 1, Nil, hasExtra = true, "NTE"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_NTE, Location(EType.Segment, "NTE", "NTE", 7, 1, "NTE[2]"), 2, Nil, hasExtra = true, "NTE"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRT, Location(EType.Segment, "PRT", "PRT", 8, 1, "PRT[1]"), 1, Nil, hasExtra = true, "PRT"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRT, Location(EType.Segment, "PRT", "PRT", 9, 1, "PRT[2]"), 2, Nil, hasExtra = true, "PRT"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_DG1, Location(EType.Segment, "DG1", "DG1", 10, 1, "DG1[1]"), 1, Nil, hasExtra = true, "DG1")))))))
	val CBC_LOIV1 = ("""/MSH
	/PID
	/ORC
	/OBR
	/PRT
	/DG1
	/SPM""".stripMargin('/'), List(SegmentInstance(LOIv1_MSH, Location(EType.Segment, "MSH", "MSH", 1, 1, "MSH[1]"), 1, Nil, hasExtra = true, "MSH"),
	GroupInstance(LOIv1_PATIENT, 1, List(SegmentInstance(LOIv1_PATIENT_PID, Location(EType.Segment, "PID", "PID", 2, 1, "PID[1]"), 1, Nil, hasExtra = true, "PID"))),
	GroupInstance(LOIv1_ORDER, 1, List(SegmentInstance(LOIv1_ORDER_ORC, Location(EType.Segment, "ORC", "ORC", 3, 1, "ORC[1]"), 1, Nil, hasExtra = true, "ORC"),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBR, Location(EType.Segment, "OBR", "OBR", 4, 1, "OBR[1]"), 1, Nil, hasExtra = true, "OBR"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRT, Location(EType.Segment, "PRT", "PRT", 5, 1, "PRT[1]"), 1, Nil, hasExtra = true, "PRT"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_DG1, Location(EType.Segment, "DG1", "DG1", 6, 1, "DG1[1]"), 1, Nil, hasExtra = true, "DG1"),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN_SPM, Location(EType.Segment, "SPM", "SPM", 7, 1, "SPM[1]"), 1, Nil, hasExtra = true, "SPM")))))))))
	val LIPID_PANEL_LOIV1 = ("""/MSH
	/PID
	/ORC
	/OBR
	/PRT
	/DG1
	/DG1
	/OBX
	/SPM""".stripMargin('/'), List(SegmentInstance(LOIv1_MSH, Location(EType.Segment, "MSH", "MSH", 1, 1, "MSH[1]"), 1, Nil, hasExtra = true, "MSH"),
	GroupInstance(LOIv1_PATIENT, 1, List(SegmentInstance(LOIv1_PATIENT_PID, Location(EType.Segment, "PID", "PID", 2, 1, "PID[1]"), 1, Nil, hasExtra = true, "PID"))),
	GroupInstance(LOIv1_ORDER, 1, List(SegmentInstance(LOIv1_ORDER_ORC, Location(EType.Segment, "ORC", "ORC", 3, 1, "ORC[1]"), 1, Nil, hasExtra = true, "ORC"),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBR, Location(EType.Segment, "OBR", "OBR", 4, 1, "OBR[1]"), 1, Nil, hasExtra = true, "OBR"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRT, Location(EType.Segment, "PRT", "PRT", 5, 1, "PRT[1]"), 1, Nil, hasExtra = true, "PRT"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_DG1, Location(EType.Segment, "DG1", "DG1", 6, 1, "DG1[1]"), 1, Nil, hasExtra = true, "DG1"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_DG1, Location(EType.Segment, "DG1", "DG1", 7, 1, "DG1[2]"), 2, Nil, hasExtra = true, "DG1"),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 8, 1, "OBX[1]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN_SPM, Location(EType.Segment, "SPM", "SPM", 9, 1, "SPM[1]"), 1, Nil, hasExtra = true, "SPM")))))))))
	val LIPID_PANEL_FI_LOIV1 = ("""/MSH
	/PID
	/PV1
	/IN1
	/GT1
	/ORC
	/OBR
	/PRT
	/DG1
	/DG1
	/OBX
	/SPM""".stripMargin('/'), List(SegmentInstance(LOIv1_MSH, Location(EType.Segment, "MSH", "MSH", 1, 1, "MSH[1]"), 1, Nil, hasExtra = true, "MSH"),
	GroupInstance(LOIv1_PATIENT, 1, List(SegmentInstance(LOIv1_PATIENT_PID, Location(EType.Segment, "PID", "PID", 2, 1, "PID[1]"), 1, Nil, hasExtra = true, "PID"),
	GroupInstance(LOIv1_PATIENT_PATIENT_VISIT, 1, List(SegmentInstance(LOIv1_PATIENT_PATIENT_VISIT_PV1, Location(EType.Segment, "PV1", "PV1", 3, 1, "PV1[1]"), 1, Nil, hasExtra = true, "PV1"))),
	GroupInstance(LOIv1_PATIENT_INSURANCE, 1, List(SegmentInstance(LOIv1_PATIENT_INSURANCE_IN1, Location(EType.Segment, "IN1", "IN1", 4, 1, "IN1[1]"), 1, Nil, hasExtra = true, "IN1"))),
	SegmentInstance(LOIv1_PATIENT_GT1, Location(EType.Segment, "GT1", "GT1", 5, 1, "GT1[1]"), 1, Nil, hasExtra = true, "GT1"))),
	GroupInstance(LOIv1_ORDER, 1, List(SegmentInstance(LOIv1_ORDER_ORC, Location(EType.Segment, "ORC", "ORC", 6, 1, "ORC[1]"), 1, Nil, hasExtra = true, "ORC"),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBR, Location(EType.Segment, "OBR", "OBR", 7, 1, "OBR[1]"), 1, Nil, hasExtra = true, "OBR"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRT, Location(EType.Segment, "PRT", "PRT", 8, 1, "PRT[1]"), 1, Nil, hasExtra = true, "PRT"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_DG1, Location(EType.Segment, "DG1", "DG1", 9, 1, "DG1[1]"), 1, Nil, hasExtra = true, "DG1"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_DG1, Location(EType.Segment, "DG1", "DG1", 10, 1, "DG1[2]"), 2, Nil, hasExtra = true, "DG1"),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 11, 1, "OBX[1]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN_SPM, Location(EType.Segment, "SPM", "SPM", 12, 1, "SPM[1]"), 1, Nil, hasExtra = true, "SPM")))))))))
	val CULTURE_AND_SUSCEP_LOIV1 = ("""/MSH
	/PID
	/ORC
	/OBR
	/PRT
	/DG1
	/SPM""".stripMargin('/'), List(SegmentInstance(LOIv1_MSH, Location(EType.Segment, "MSH", "MSH", 1, 1, "MSH[1]"), 1, Nil, hasExtra = true, "MSH"),
	GroupInstance(LOIv1_PATIENT, 1, List(SegmentInstance(LOIv1_PATIENT_PID, Location(EType.Segment, "PID", "PID", 2, 1, "PID[1]"), 1, Nil, hasExtra = true, "PID"))),
	GroupInstance(LOIv1_ORDER, 1, List(SegmentInstance(LOIv1_ORDER_ORC, Location(EType.Segment, "ORC", "ORC", 3, 1, "ORC[1]"), 1, Nil, hasExtra = true, "ORC"),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBR, Location(EType.Segment, "OBR", "OBR", 4, 1, "OBR[1]"), 1, Nil, hasExtra = true, "OBR"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRT, Location(EType.Segment, "PRT", "PRT", 5, 1, "PRT[1]"), 1, Nil, hasExtra = true, "PRT"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_DG1, Location(EType.Segment, "DG1", "DG1", 6, 1, "DG1[1]"), 1, Nil, hasExtra = true, "DG1"),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN_SPM, Location(EType.Segment, "SPM", "SPM", 7, 1, "SPM[1]"), 1, Nil, hasExtra = true, "SPM")))))))))
	val REFLEX_HEPATITIS_LOIV1 = ("""/MSH
	/PID
	/NK1
	/ORC
	/OBR
	/DG1
	/OBX
	/SPM""".stripMargin('/'), List(SegmentInstance(LOIv1_MSH, Location(EType.Segment, "MSH", "MSH", 1, 1, "MSH[1]"), 1, Nil, hasExtra = true, "MSH"),
	GroupInstance(LOIv1_PATIENT, 1, List(SegmentInstance(LOIv1_PATIENT_PID, Location(EType.Segment, "PID", "PID", 2, 1, "PID[1]"), 1, Nil, hasExtra = true, "PID"),
	SegmentInstance(LOIv1_PATIENT_NK1, Location(EType.Segment, "NK1", "NK1", 3, 1, "NK1[1]"), 1, Nil, hasExtra = true, "NK1"))),
	GroupInstance(LOIv1_ORDER, 1, List(SegmentInstance(LOIv1_ORDER_ORC, Location(EType.Segment, "ORC", "ORC", 4, 1, "ORC[1]"), 1, Nil, hasExtra = true, "ORC"),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBR, Location(EType.Segment, "OBR", "OBR", 5, 1, "OBR[1]"), 1, Nil, hasExtra = true, "OBR"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_DG1, Location(EType.Segment, "DG1", "DG1", 6, 1, "DG1[1]"), 1, Nil, hasExtra = true, "DG1"),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 7, 1, "OBX[1]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN_SPM, Location(EType.Segment, "SPM", "SPM", 8, 1, "SPM[1]"), 1, Nil, hasExtra = true, "SPM")))))))))
	val PAP_SMEAR_LOIV1 = ("""/MSH
	/PID
	/ORC
	/OBR
	/DG1
	/OBX
	/OBX
	/SPM""".stripMargin('/'), List(SegmentInstance(LOIv1_MSH, Location(EType.Segment, "MSH", "MSH", 1, 1, "MSH[1]"), 1, Nil, hasExtra = true, "MSH"),
	GroupInstance(LOIv1_PATIENT, 1, List(SegmentInstance(LOIv1_PATIENT_PID, Location(EType.Segment, "PID", "PID", 2, 1, "PID[1]"), 1, Nil, hasExtra = true, "PID"))),
	GroupInstance(LOIv1_ORDER, 1, List(SegmentInstance(LOIv1_ORDER_ORC, Location(EType.Segment, "ORC", "ORC", 3, 1, "ORC[1]"), 1, Nil, hasExtra = true, "ORC"),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBR, Location(EType.Segment, "OBR", "OBR", 4, 1, "OBR[1]"), 1, Nil, hasExtra = true, "OBR"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_DG1, Location(EType.Segment, "DG1", "DG1", 5, 1, "DG1[1]"), 1, Nil, hasExtra = true, "DG1"),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 6, 1, "OBX[1]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION, 2, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 7, 1, "OBX[2]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN_SPM, Location(EType.Segment, "SPM", "SPM", 8, 1, "SPM[1]"), 1, Nil, hasExtra = true, "SPM")))))))))
	val GHP_LOIV1 = ("""/MSH
	/PID
	/NK1
	/NK1
	/ORC
	/TQ1
	/OBR
	/DG1
	/DG1
	/DG1
	/ORC
	/TQ1
	/OBR
	/DG1""".stripMargin('/'), List(SegmentInstance(LOIv1_MSH, Location(EType.Segment, "MSH", "MSH", 1, 1, "MSH[1]"), 1, Nil, hasExtra = true, "MSH"),
	GroupInstance(LOIv1_PATIENT, 1, List(SegmentInstance(LOIv1_PATIENT_PID, Location(EType.Segment, "PID", "PID", 2, 1, "PID[1]"), 1, Nil, hasExtra = true, "PID"),
	SegmentInstance(LOIv1_PATIENT_NK1, Location(EType.Segment, "NK1", "NK1", 3, 1, "NK1[1]"), 1, Nil, hasExtra = true, "NK1"),
	SegmentInstance(LOIv1_PATIENT_NK1, Location(EType.Segment, "NK1", "NK1", 4, 1, "NK1[2]"), 2, Nil, hasExtra = true, "NK1"))),
	GroupInstance(LOIv1_ORDER, 1, List(SegmentInstance(LOIv1_ORDER_ORC, Location(EType.Segment, "ORC", "ORC", 5, 1, "ORC[1]"), 1, Nil, hasExtra = true, "ORC"),
	GroupInstance(LOIv1_ORDER_TIMING, 1, List(SegmentInstance(LOIv1_ORDER_TIMING_TQ1, Location(EType.Segment, "TQ1", "TQ1", 6, 1, "TQ1[1]"), 1, Nil, hasExtra = true, "TQ1"))),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBR, Location(EType.Segment, "OBR", "OBR", 7, 1, "OBR[1]"), 1, Nil, hasExtra = true, "OBR"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_DG1, Location(EType.Segment, "DG1", "DG1", 8, 1, "DG1[1]"), 1, Nil, hasExtra = true, "DG1"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_DG1, Location(EType.Segment, "DG1", "DG1", 9, 1, "DG1[2]"), 2, Nil, hasExtra = true, "DG1"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_DG1, Location(EType.Segment, "DG1", "DG1", 10, 1, "DG1[3]"), 3, Nil, hasExtra = true, "DG1"))))),
	GroupInstance(LOIv1_ORDER, 2, List(SegmentInstance(LOIv1_ORDER_ORC, Location(EType.Segment, "ORC", "ORC", 11, 1, "ORC[2]"), 1, Nil, hasExtra = true, "ORC"),
	GroupInstance(LOIv1_ORDER_TIMING, 1, List(SegmentInstance(LOIv1_ORDER_TIMING_TQ1, Location(EType.Segment, "TQ1", "TQ1", 12, 1, "TQ1[2]"), 1, Nil, hasExtra = true, "TQ1"))),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBR, Location(EType.Segment, "OBR", "OBR", 13, 1, "OBR[2]"), 1, Nil, hasExtra = true, "OBR"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_DG1, Location(EType.Segment, "DG1", "DG1", 14, 1, "DG1[4]"), 1, Nil, hasExtra = true, "DG1")))))))
	val CREATININE_CLEARANCE_LOIV1 = ("""/MSH
	/PID
	/ORC
	/OBR
	/DG1
	/DG1
	/OBX
	/OBX
	/OBX
	/SPM
	/SPM
	/ORC
	/OBR
	/DG1
	/DG1
	/OBX
	/OBX
	/SPM""".stripMargin('/'), List(SegmentInstance(LOIv1_MSH, Location(EType.Segment, "MSH", "MSH", 1, 1, "MSH[1]"), 1, Nil, hasExtra = true, "MSH"),
	GroupInstance(LOIv1_PATIENT, 1, List(SegmentInstance(LOIv1_PATIENT_PID, Location(EType.Segment, "PID", "PID", 2, 1, "PID[1]"), 1, Nil, hasExtra = true, "PID"))),
	GroupInstance(LOIv1_ORDER, 1, List(SegmentInstance(LOIv1_ORDER_ORC, Location(EType.Segment, "ORC", "ORC", 3, 1, "ORC[1]"), 1, Nil, hasExtra = true, "ORC"),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBR, Location(EType.Segment, "OBR", "OBR", 4, 1, "OBR[1]"), 1, Nil, hasExtra = true, "OBR"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_DG1, Location(EType.Segment, "DG1", "DG1", 5, 1, "DG1[1]"), 1, Nil, hasExtra = true, "DG1"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_DG1, Location(EType.Segment, "DG1", "DG1", 6, 1, "DG1[2]"), 2, Nil, hasExtra = true, "DG1"),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 7, 1, "OBX[1]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION, 2, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 8, 1, "OBX[2]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION, 3, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 9, 1, "OBX[3]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN_SPM, Location(EType.Segment, "SPM", "SPM", 10, 1, "SPM[1]"), 1, Nil, hasExtra = true, "SPM"))),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN, 2, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN_SPM, Location(EType.Segment, "SPM", "SPM", 11, 1, "SPM[2]"), 1, Nil, hasExtra = true, "SPM"))))))),
	GroupInstance(LOIv1_ORDER, 2, List(SegmentInstance(LOIv1_ORDER_ORC, Location(EType.Segment, "ORC", "ORC", 12, 1, "ORC[2]"), 1, Nil, hasExtra = true, "ORC"),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBR, Location(EType.Segment, "OBR", "OBR", 13, 1, "OBR[2]"), 1, Nil, hasExtra = true, "OBR"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_DG1, Location(EType.Segment, "DG1", "DG1", 14, 1, "DG1[3]"), 1, Nil, hasExtra = true, "DG1"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_DG1, Location(EType.Segment, "DG1", "DG1", 15, 1, "DG1[4]"), 2, Nil, hasExtra = true, "DG1"),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 16, 1, "OBX[4]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION, 2, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 17, 1, "OBX[5]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN_SPM, Location(EType.Segment, "SPM", "SPM", 18, 1, "SPM[3]"), 1, Nil, hasExtra = true, "SPM")))))))))
	val PROSTATE_BIOPSY_LOIV1 = ("""/MSH
	/PID
	/ORC
	/OBR
	/SPM
	/SPM
	/SPM
	/SPM
	/SPM
	/SPM
	/SPM
	/SPM
	/SPM
	/SPM""".stripMargin('/'), List(SegmentInstance(LOIv1_MSH, Location(EType.Segment, "MSH", "MSH", 1, 1, "MSH[1]"), 1, Nil, hasExtra = true, "MSH"),
	GroupInstance(LOIv1_PATIENT, 1, List(SegmentInstance(LOIv1_PATIENT_PID, Location(EType.Segment, "PID", "PID", 2, 1, "PID[1]"), 1, Nil, hasExtra = true, "PID"))),
	GroupInstance(LOIv1_ORDER, 1, List(SegmentInstance(LOIv1_ORDER_ORC, Location(EType.Segment, "ORC", "ORC", 3, 1, "ORC[1]"), 1, Nil, hasExtra = true, "ORC"),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBR, Location(EType.Segment, "OBR", "OBR", 4, 1, "OBR[1]"), 1, Nil, hasExtra = true, "OBR"),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN_SPM, Location(EType.Segment, "SPM", "SPM", 5, 1, "SPM[1]"), 1, Nil, hasExtra = true, "SPM"))),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN, 2, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN_SPM, Location(EType.Segment, "SPM", "SPM", 6, 1, "SPM[2]"), 1, Nil, hasExtra = true, "SPM"))),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN, 3, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN_SPM, Location(EType.Segment, "SPM", "SPM", 7, 1, "SPM[3]"), 1, Nil, hasExtra = true, "SPM"))),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN, 4, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN_SPM, Location(EType.Segment, "SPM", "SPM", 8, 1, "SPM[4]"), 1, Nil, hasExtra = true, "SPM"))),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN, 5, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN_SPM, Location(EType.Segment, "SPM", "SPM", 9, 1, "SPM[5]"), 1, Nil, hasExtra = true, "SPM"))),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN, 6, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN_SPM, Location(EType.Segment, "SPM", "SPM", 10, 1, "SPM[6]"), 1, Nil, hasExtra = true, "SPM"))),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN, 7, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN_SPM, Location(EType.Segment, "SPM", "SPM", 11, 1, "SPM[7]"), 1, Nil, hasExtra = true, "SPM"))),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN, 8, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN_SPM, Location(EType.Segment, "SPM", "SPM", 12, 1, "SPM[8]"), 1, Nil, hasExtra = true, "SPM"))),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN, 9, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN_SPM, Location(EType.Segment, "SPM", "SPM", 13, 1, "SPM[9]"), 1, Nil, hasExtra = true, "SPM"))),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN, 10, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN_SPM, Location(EType.Segment, "SPM", "SPM", 14, 1, "SPM[10]"), 1, Nil, hasExtra = true, "SPM")))))))))
	val ALL_SEGMENTS__NO_REPS_LOIV1 = ("""/MSH
	/SFT
	/NTE
	/PID
	/PD1
	/NTE
	/NK1
	/PV1
	/PV2
	/IN1
	/IN2
	/IN3
	/GT1
	/AL1
	/ORC
	/TQ1
	/TQ2
	/OBR
	/TCD
	/NTE
	/PRT
	/CTD
	/DG1
	/OBX
	/TCD
	/NTE
	/SPM
	/OBX
	/SAC
	/OBX
	/SGH
	/PID
	/PD1
	/PV1
	/PV2
	/AL1
	/ORC
	/OBR
	/NTE
	/TQ1
	/TQ2
	/OBX
	/NTE
	/SGT
	/FT1
	/CTI
	/BLG""".stripMargin('/'), List(SegmentInstance(LOIv1_MSH, Location(EType.Segment, "MSH", "MSH", 1, 1, "MSH[1]"), 1, Nil, hasExtra = true, "MSH"),
	SegmentInstance(LOIv1_SFT, Location(EType.Segment, "SFT", "SFT", 2, 1, "SFT[1]"), 1, Nil, hasExtra = true, "SFT"),
	SegmentInstance(LOIv1_NTE, Location(EType.Segment, "NTE", "NTE", 3, 1, "NTE[1]"), 1, Nil, hasExtra = true, "NTE"),
	GroupInstance(LOIv1_PATIENT, 1, List(SegmentInstance(LOIv1_PATIENT_PID, Location(EType.Segment, "PID", "PID", 4, 1, "PID[1]"), 1, Nil, hasExtra = true, "PID"),
	SegmentInstance(LOIv1_PATIENT_PD1, Location(EType.Segment, "PD1", "PD1", 5, 1, "PD1[1]"), 1, Nil, hasExtra = true, "PD1"),
	SegmentInstance(LOIv1_PATIENT_NTE, Location(EType.Segment, "NTE", "NTE", 6, 1, "NTE[2]"), 1, Nil, hasExtra = true, "NTE"),
	SegmentInstance(LOIv1_PATIENT_NK1, Location(EType.Segment, "NK1", "NK1", 7, 1, "NK1[1]"), 1, Nil, hasExtra = true, "NK1"),
	GroupInstance(LOIv1_PATIENT_PATIENT_VISIT, 1, List(SegmentInstance(LOIv1_PATIENT_PATIENT_VISIT_PV1, Location(EType.Segment, "PV1", "PV1", 8, 1, "PV1[1]"), 1, Nil, hasExtra = true, "PV1"),
	SegmentInstance(LOIv1_PATIENT_PATIENT_VISIT_PV2, Location(EType.Segment, "PV2", "PV2", 9, 1, "PV2[1]"), 1, Nil, hasExtra = true, "PV2"))),
	GroupInstance(LOIv1_PATIENT_INSURANCE, 1, List(SegmentInstance(LOIv1_PATIENT_INSURANCE_IN1, Location(EType.Segment, "IN1", "IN1", 10, 1, "IN1[1]"), 1, Nil, hasExtra = true, "IN1"),
	SegmentInstance(LOIv1_PATIENT_INSURANCE_IN2, Location(EType.Segment, "IN2", "IN2", 11, 1, "IN2[1]"), 1, Nil, hasExtra = true, "IN2"),
	SegmentInstance(LOIv1_PATIENT_INSURANCE_IN3, Location(EType.Segment, "IN3", "IN3", 12, 1, "IN3[1]"), 1, Nil, hasExtra = true, "IN3"))),
	SegmentInstance(LOIv1_PATIENT_GT1, Location(EType.Segment, "GT1", "GT1", 13, 1, "GT1[1]"), 1, Nil, hasExtra = true, "GT1"),
	SegmentInstance(LOIv1_PATIENT_AL1, Location(EType.Segment, "AL1", "AL1", 14, 1, "AL1[1]"), 1, Nil, hasExtra = true, "AL1"))),
	GroupInstance(LOIv1_ORDER, 1, List(SegmentInstance(LOIv1_ORDER_ORC, Location(EType.Segment, "ORC", "ORC", 15, 1, "ORC[1]"), 1, Nil, hasExtra = true, "ORC"),
	GroupInstance(LOIv1_ORDER_TIMING, 1, List(SegmentInstance(LOIv1_ORDER_TIMING_TQ1, Location(EType.Segment, "TQ1", "TQ1", 16, 1, "TQ1[1]"), 1, Nil, hasExtra = true, "TQ1"),
	SegmentInstance(LOIv1_ORDER_TIMING_TQ2, Location(EType.Segment, "TQ2", "TQ2", 17, 1, "TQ2[1]"), 1, Nil, hasExtra = true, "TQ2"))),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBR, Location(EType.Segment, "OBR", "OBR", 18, 1, "OBR[1]"), 1, Nil, hasExtra = true, "OBR"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_TCD, Location(EType.Segment, "TCD", "TCD", 19, 1, "TCD[1]"), 1, Nil, hasExtra = true, "TCD"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_NTE, Location(EType.Segment, "NTE", "NTE", 20, 1, "NTE[3]"), 1, Nil, hasExtra = true, "NTE"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRT, Location(EType.Segment, "PRT", "PRT", 21, 1, "PRT[1]"), 1, Nil, hasExtra = true, "PRT"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_CTD, Location(EType.Segment, "CTD", "CTD", 22, 1, "CTD[1]"), 1, Nil, hasExtra = true, "CTD"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_DG1, Location(EType.Segment, "DG1", "DG1", 23, 1, "DG1[1]"), 1, Nil, hasExtra = true, "DG1"),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 24, 1, "OBX[1]"), 1, Nil, hasExtra = true, "OBX"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION_TCD, Location(EType.Segment, "TCD", "TCD", 25, 1, "TCD[2]"), 1, Nil, hasExtra = true, "TCD"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION_NTE, Location(EType.Segment, "NTE", "NTE", 26, 1, "NTE[4]"), 1, Nil, hasExtra = true, "NTE"))),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN_SPM, Location(EType.Segment, "SPM", "SPM", 27, 1, "SPM[1]"), 1, Nil, hasExtra = true, "SPM"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN_OBX, Location(EType.Segment, "OBX", "OBX", 28, 1, "OBX[2]"), 1, Nil, hasExtra = true, "OBX"),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN_CONTAINER, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN_CONTAINER_SAC, Location(EType.Segment, "SAC", "SAC", 29, 1, "SAC[1]"), 1, Nil, hasExtra = true, "SAC"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN_CONTAINER_OBX, Location(EType.Segment, "OBX", "OBX", 30, 1, "OBX[3]"), 1, Nil, hasExtra = true, "OBX"))))),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_SGH, Location(EType.Segment, "SGH", "SGH", 31, 1, "SGH[1]"), 1, Nil, hasExtra = true, "SGH"),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_PATIENT_PRIOR, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_PATIENT_PRIOR_PID, Location(EType.Segment, "PID", "PID", 32, 1, "PID[2]"), 1, Nil, hasExtra = true, "PID"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_PATIENT_PRIOR_PD1, Location(EType.Segment, "PD1", "PD1", 33, 1, "PD1[2]"), 1, Nil, hasExtra = true, "PD1"))),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_PATIENT_VISIT_PRIOR, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_PATIENT_VISIT_PRIOR_PV1, Location(EType.Segment, "PV1", "PV1", 34, 1, "PV1[2]"), 1, Nil, hasExtra = true, "PV1"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_PATIENT_VISIT_PRIOR_PV2, Location(EType.Segment, "PV2", "PV2", 35, 1, "PV2[2]"), 1, Nil, hasExtra = true, "PV2"))),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_AL1, Location(EType.Segment, "AL1", "AL1", 36, 1, "AL1[2]"), 1, Nil, hasExtra = true, "AL1"),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_ORC, Location(EType.Segment, "ORC", "ORC", 37, 1, "ORC[2]"), 1, Nil, hasExtra = true, "ORC"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_OBR, Location(EType.Segment, "OBR", "OBR", 38, 1, "OBR[2]"), 1, Nil, hasExtra = true, "OBR"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_NTE, Location(EType.Segment, "NTE", "NTE", 39, 1, "NTE[5]"), 1, Nil, hasExtra = true, "NTE"),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_TIMING_PRIOR, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_TIMING_PRIOR_TQ1, Location(EType.Segment, "TQ1", "TQ1", 40, 1, "TQ1[2]"), 1, Nil, hasExtra = true, "TQ1"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_TIMING_PRIOR_TQ2, Location(EType.Segment, "TQ2", "TQ2", 41, 1, "TQ2[2]"), 1, Nil, hasExtra = true, "TQ2"))),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_OBSERVATION_PRIOR, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_OBSERVATION_PRIOR_OBX, Location(EType.Segment, "OBX", "OBX", 42, 1, "OBX[4]"), 1, Nil, hasExtra = true, "OBX"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_OBSERVATION_PRIOR_NTE, Location(EType.Segment, "NTE", "NTE", 43, 1, "NTE[6]"), 1, Nil, hasExtra = true, "NTE"))))),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_SGT, Location(EType.Segment, "SGT", "SGT", 44, 1, "SGT[1]"), 1, Nil, hasExtra = true, "SGT"))))),
	SegmentInstance(LOIv1_ORDER_FT1, Location(EType.Segment, "FT1", "FT1", 45, 1, "FT1[1]"), 1, Nil, hasExtra = true, "FT1"),
	SegmentInstance(LOIv1_ORDER_CTI, Location(EType.Segment, "CTI", "CTI", 46, 1, "CTI[1]"), 1, Nil, hasExtra = true, "CTI"),
	SegmentInstance(LOIv1_ORDER_BLG, Location(EType.Segment, "BLG", "BLG", 47, 1, "BLG[1]"), 1, Nil, hasExtra = true, "BLG")))))
	val ALL_SEGMENTS__SEGMENT_REPETITIONS_LOIV1 = ("""/MSH
	/SFT
	/SFT
	/NTE
	/NTE
	/PID
	/PD1
	/NTE
	/NTE
	/NK1
	/NK1
	/PV1
	/PV2
	/IN1
	/IN2
	/IN3
	/GT1
	/AL1
	/AL1
	/ORC
	/TQ1
	/TQ2
	/TQ2
	/OBR
	/TCD
	/NTE
	/NTE
	/PRT
	/PRT
	/CTD
	/DG1
	/DG1
	/OBX
	/TCD
	/NTE
	/NTE
	/SPM
	/OBX
	/OBX
	/SAC
	/OBX
	/SGH
	/PID
	/PD1
	/PV1
	/PV2
	/AL1
	/AL1
	/ORC
	/OBR
	/NTE
	/NTE
	/TQ1
	/TQ2
	/TQ2
	/OBX
	/NTE
	/NTE
	/SGT
	/FT1
	/FT1
	/CTI
	/CTI
	/BLG""".stripMargin('/'), List(SegmentInstance(LOIv1_MSH, Location(EType.Segment, "MSH", "MSH", 1, 1, "MSH[1]"), 1, Nil, hasExtra = true, "MSH"),
	SegmentInstance(LOIv1_SFT, Location(EType.Segment, "SFT", "SFT", 2, 1, "SFT[1]"), 1, Nil, hasExtra = true, "SFT"),
	SegmentInstance(LOIv1_SFT, Location(EType.Segment, "SFT", "SFT", 3, 1, "SFT[2]"), 2, Nil, hasExtra = true, "SFT"),
	SegmentInstance(LOIv1_NTE, Location(EType.Segment, "NTE", "NTE", 4, 1, "NTE[1]"), 1, Nil, hasExtra = true, "NTE"),
	SegmentInstance(LOIv1_NTE, Location(EType.Segment, "NTE", "NTE", 5, 1, "NTE[2]"), 2, Nil, hasExtra = true, "NTE"),
	GroupInstance(LOIv1_PATIENT, 1, List(SegmentInstance(LOIv1_PATIENT_PID, Location(EType.Segment, "PID", "PID", 6, 1, "PID[1]"), 1, Nil, hasExtra = true, "PID"),
	SegmentInstance(LOIv1_PATIENT_PD1, Location(EType.Segment, "PD1", "PD1", 7, 1, "PD1[1]"), 1, Nil, hasExtra = true, "PD1"),
	SegmentInstance(LOIv1_PATIENT_NTE, Location(EType.Segment, "NTE", "NTE", 8, 1, "NTE[3]"), 1, Nil, hasExtra = true, "NTE"),
	SegmentInstance(LOIv1_PATIENT_NTE, Location(EType.Segment, "NTE", "NTE", 9, 1, "NTE[4]"), 2, Nil, hasExtra = true, "NTE"),
	SegmentInstance(LOIv1_PATIENT_NK1, Location(EType.Segment, "NK1", "NK1", 10, 1, "NK1[1]"), 1, Nil, hasExtra = true, "NK1"),
	SegmentInstance(LOIv1_PATIENT_NK1, Location(EType.Segment, "NK1", "NK1", 11, 1, "NK1[2]"), 2, Nil, hasExtra = true, "NK1"),
	GroupInstance(LOIv1_PATIENT_PATIENT_VISIT, 1, List(SegmentInstance(LOIv1_PATIENT_PATIENT_VISIT_PV1, Location(EType.Segment, "PV1", "PV1", 12, 1, "PV1[1]"), 1, Nil, hasExtra = true, "PV1"),
	SegmentInstance(LOIv1_PATIENT_PATIENT_VISIT_PV2, Location(EType.Segment, "PV2", "PV2", 13, 1, "PV2[1]"), 1, Nil, hasExtra = true, "PV2"))),
	GroupInstance(LOIv1_PATIENT_INSURANCE, 1, List(SegmentInstance(LOIv1_PATIENT_INSURANCE_IN1, Location(EType.Segment, "IN1", "IN1", 14, 1, "IN1[1]"), 1, Nil, hasExtra = true, "IN1"),
	SegmentInstance(LOIv1_PATIENT_INSURANCE_IN2, Location(EType.Segment, "IN2", "IN2", 15, 1, "IN2[1]"), 1, Nil, hasExtra = true, "IN2"),
	SegmentInstance(LOIv1_PATIENT_INSURANCE_IN3, Location(EType.Segment, "IN3", "IN3", 16, 1, "IN3[1]"), 1, Nil, hasExtra = true, "IN3"))),
	SegmentInstance(LOIv1_PATIENT_GT1, Location(EType.Segment, "GT1", "GT1", 17, 1, "GT1[1]"), 1, Nil, hasExtra = true, "GT1"),
	SegmentInstance(LOIv1_PATIENT_AL1, Location(EType.Segment, "AL1", "AL1", 18, 1, "AL1[1]"), 1, Nil, hasExtra = true, "AL1"),
	SegmentInstance(LOIv1_PATIENT_AL1, Location(EType.Segment, "AL1", "AL1", 19, 1, "AL1[2]"), 2, Nil, hasExtra = true, "AL1"))),
	GroupInstance(LOIv1_ORDER, 1, List(SegmentInstance(LOIv1_ORDER_ORC, Location(EType.Segment, "ORC", "ORC", 20, 1, "ORC[1]"), 1, Nil, hasExtra = true, "ORC"),
	GroupInstance(LOIv1_ORDER_TIMING, 1, List(SegmentInstance(LOIv1_ORDER_TIMING_TQ1, Location(EType.Segment, "TQ1", "TQ1", 21, 1, "TQ1[1]"), 1, Nil, hasExtra = true, "TQ1"),
	SegmentInstance(LOIv1_ORDER_TIMING_TQ2, Location(EType.Segment, "TQ2", "TQ2", 22, 1, "TQ2[1]"), 1, Nil, hasExtra = true, "TQ2"),
	SegmentInstance(LOIv1_ORDER_TIMING_TQ2, Location(EType.Segment, "TQ2", "TQ2", 23, 1, "TQ2[2]"), 2, Nil, hasExtra = true, "TQ2"))),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBR, Location(EType.Segment, "OBR", "OBR", 24, 1, "OBR[1]"), 1, Nil, hasExtra = true, "OBR"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_TCD, Location(EType.Segment, "TCD", "TCD", 25, 1, "TCD[1]"), 1, Nil, hasExtra = true, "TCD"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_NTE, Location(EType.Segment, "NTE", "NTE", 26, 1, "NTE[5]"), 1, Nil, hasExtra = true, "NTE"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_NTE, Location(EType.Segment, "NTE", "NTE", 27, 1, "NTE[6]"), 2, Nil, hasExtra = true, "NTE"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRT, Location(EType.Segment, "PRT", "PRT", 28, 1, "PRT[1]"), 1, Nil, hasExtra = true, "PRT"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRT, Location(EType.Segment, "PRT", "PRT", 29, 1, "PRT[2]"), 2, Nil, hasExtra = true, "PRT"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_CTD, Location(EType.Segment, "CTD", "CTD", 30, 1, "CTD[1]"), 1, Nil, hasExtra = true, "CTD"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_DG1, Location(EType.Segment, "DG1", "DG1", 31, 1, "DG1[1]"), 1, Nil, hasExtra = true, "DG1"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_DG1, Location(EType.Segment, "DG1", "DG1", 32, 1, "DG1[2]"), 2, Nil, hasExtra = true, "DG1"),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 33, 1, "OBX[1]"), 1, Nil, hasExtra = true, "OBX"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION_TCD, Location(EType.Segment, "TCD", "TCD", 34, 1, "TCD[2]"), 1, Nil, hasExtra = true, "TCD"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION_NTE, Location(EType.Segment, "NTE", "NTE", 35, 1, "NTE[7]"), 1, Nil, hasExtra = true, "NTE"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION_NTE, Location(EType.Segment, "NTE", "NTE", 36, 1, "NTE[8]"), 2, Nil, hasExtra = true, "NTE"))),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN_SPM, Location(EType.Segment, "SPM", "SPM", 37, 1, "SPM[1]"), 1, Nil, hasExtra = true, "SPM"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN_OBX, Location(EType.Segment, "OBX", "OBX", 38, 1, "OBX[2]"), 1, Nil, hasExtra = true, "OBX"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN_OBX, Location(EType.Segment, "OBX", "OBX", 39, 1, "OBX[3]"), 2, Nil, hasExtra = true, "OBX"),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN_CONTAINER, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN_CONTAINER_SAC, Location(EType.Segment, "SAC", "SAC", 40, 1, "SAC[1]"), 1, Nil, hasExtra = true, "SAC"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN_CONTAINER_OBX, Location(EType.Segment, "OBX", "OBX", 41, 1, "OBX[4]"), 1, Nil, hasExtra = true, "OBX"))))),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_SGH, Location(EType.Segment, "SGH", "SGH", 42, 1, "SGH[1]"), 1, Nil, hasExtra = true, "SGH"),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_PATIENT_PRIOR, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_PATIENT_PRIOR_PID, Location(EType.Segment, "PID", "PID", 43, 1, "PID[2]"), 1, Nil, hasExtra = true, "PID"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_PATIENT_PRIOR_PD1, Location(EType.Segment, "PD1", "PD1", 44, 1, "PD1[2]"), 1, Nil, hasExtra = true, "PD1"))),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_PATIENT_VISIT_PRIOR, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_PATIENT_VISIT_PRIOR_PV1, Location(EType.Segment, "PV1", "PV1", 45, 1, "PV1[2]"), 1, Nil, hasExtra = true, "PV1"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_PATIENT_VISIT_PRIOR_PV2, Location(EType.Segment, "PV2", "PV2", 46, 1, "PV2[2]"), 1, Nil, hasExtra = true, "PV2"))),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_AL1, Location(EType.Segment, "AL1", "AL1", 47, 1, "AL1[3]"), 1, Nil, hasExtra = true, "AL1"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_AL1, Location(EType.Segment, "AL1", "AL1", 48, 1, "AL1[4]"), 2, Nil, hasExtra = true, "AL1"),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_ORC, Location(EType.Segment, "ORC", "ORC", 49, 1, "ORC[2]"), 1, Nil, hasExtra = true, "ORC"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_OBR, Location(EType.Segment, "OBR", "OBR", 50, 1, "OBR[2]"), 1, Nil, hasExtra = true, "OBR"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_NTE, Location(EType.Segment, "NTE", "NTE", 51, 1, "NTE[9]"), 1, Nil, hasExtra = true, "NTE"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_NTE, Location(EType.Segment, "NTE", "NTE", 52, 1, "NTE[10]"), 2, Nil, hasExtra = true, "NTE"),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_TIMING_PRIOR, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_TIMING_PRIOR_TQ1, Location(EType.Segment, "TQ1", "TQ1", 53, 1, "TQ1[2]"), 1, Nil, hasExtra = true, "TQ1"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_TIMING_PRIOR_TQ2, Location(EType.Segment, "TQ2", "TQ2", 54, 1, "TQ2[3]"), 1, Nil, hasExtra = true, "TQ2"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_TIMING_PRIOR_TQ2, Location(EType.Segment, "TQ2", "TQ2", 55, 1, "TQ2[4]"), 2, Nil, hasExtra = true, "TQ2"))),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_OBSERVATION_PRIOR, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_OBSERVATION_PRIOR_OBX, Location(EType.Segment, "OBX", "OBX", 56, 1, "OBX[5]"), 1, Nil, hasExtra = true, "OBX"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_OBSERVATION_PRIOR_NTE, Location(EType.Segment, "NTE", "NTE", 57, 1, "NTE[11]"), 1, Nil, hasExtra = true, "NTE"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_OBSERVATION_PRIOR_NTE, Location(EType.Segment, "NTE", "NTE", 58, 1, "NTE[12]"), 2, Nil, hasExtra = true, "NTE"))))),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_SGT, Location(EType.Segment, "SGT", "SGT", 59, 1, "SGT[1]"), 1, Nil, hasExtra = true, "SGT"))))),
	SegmentInstance(LOIv1_ORDER_FT1, Location(EType.Segment, "FT1", "FT1", 60, 1, "FT1[1]"), 1, Nil, hasExtra = true, "FT1"),
	SegmentInstance(LOIv1_ORDER_FT1, Location(EType.Segment, "FT1", "FT1", 61, 1, "FT1[2]"), 2, Nil, hasExtra = true, "FT1"),
	SegmentInstance(LOIv1_ORDER_CTI, Location(EType.Segment, "CTI", "CTI", 62, 1, "CTI[1]"), 1, Nil, hasExtra = true, "CTI"),
	SegmentInstance(LOIv1_ORDER_CTI, Location(EType.Segment, "CTI", "CTI", 63, 1, "CTI[2]"), 2, Nil, hasExtra = true, "CTI"),
	SegmentInstance(LOIv1_ORDER_BLG, Location(EType.Segment, "BLG", "BLG", 64, 1, "BLG[1]"), 1, Nil, hasExtra = true, "BLG")))))
	val MULTIPLE_GROUPS_REPS_LOIV1 = ("""/MSH
	/PID
	/IN1
	/IN1
	/ORC
	/OBR
	/OBX
	/NTE
	/OBX
	/NTE
	/OBX
	/OBX
	/SPM
	/SPM
	/SPM
	/OBX
	/SPM
	/OBX""".stripMargin('/'), List(SegmentInstance(LOIv1_MSH, Location(EType.Segment, "MSH", "MSH", 1, 1, "MSH[1]"), 1, Nil, hasExtra = true, "MSH"),
	GroupInstance(LOIv1_PATIENT, 1, List(SegmentInstance(LOIv1_PATIENT_PID, Location(EType.Segment, "PID", "PID", 2, 1, "PID[1]"), 1, Nil, hasExtra = true, "PID"),
	GroupInstance(LOIv1_PATIENT_INSURANCE, 1, List(SegmentInstance(LOIv1_PATIENT_INSURANCE_IN1, Location(EType.Segment, "IN1", "IN1", 3, 1, "IN1[1]"), 1, Nil, hasExtra = true, "IN1"))),
	GroupInstance(LOIv1_PATIENT_INSURANCE, 2, List(SegmentInstance(LOIv1_PATIENT_INSURANCE_IN1, Location(EType.Segment, "IN1", "IN1", 4, 1, "IN1[2]"), 1, Nil, hasExtra = true, "IN1"))))),
	GroupInstance(LOIv1_ORDER, 1, List(SegmentInstance(LOIv1_ORDER_ORC, Location(EType.Segment, "ORC", "ORC", 5, 1, "ORC[1]"), 1, Nil, hasExtra = true, "ORC"),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBR, Location(EType.Segment, "OBR", "OBR", 6, 1, "OBR[1]"), 1, Nil, hasExtra = true, "OBR"),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 7, 1, "OBX[1]"), 1, Nil, hasExtra = true, "OBX"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION_NTE, Location(EType.Segment, "NTE", "NTE", 8, 1, "NTE[1]"), 1, Nil, hasExtra = true, "NTE"))),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION, 2, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 9, 1, "OBX[2]"), 1, Nil, hasExtra = true, "OBX"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION_NTE, Location(EType.Segment, "NTE", "NTE", 10, 1, "NTE[2]"), 1, Nil, hasExtra = true, "NTE"))),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION, 3, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 11, 1, "OBX[3]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION, 4, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 12, 1, "OBX[4]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN_SPM, Location(EType.Segment, "SPM", "SPM", 13, 1, "SPM[1]"), 1, Nil, hasExtra = true, "SPM"))),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN, 2, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN_SPM, Location(EType.Segment, "SPM", "SPM", 14, 1, "SPM[2]"), 1, Nil, hasExtra = true, "SPM"))),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN, 3, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN_SPM, Location(EType.Segment, "SPM", "SPM", 15, 1, "SPM[3]"), 1, Nil, hasExtra = true, "SPM"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN_OBX, Location(EType.Segment, "OBX", "OBX", 16, 1, "OBX[5]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN, 4, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN_SPM, Location(EType.Segment, "SPM", "SPM", 17, 1, "SPM[4]"), 1, Nil, hasExtra = true, "SPM"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_SPECIMEN_OBX, Location(EType.Segment, "OBX", "OBX", 18, 1, "OBX[6]"), 1, Nil, hasExtra = true, "OBX")))))))))
	val ONE_ORDER_GROUP_WITH_PRIOR_RESULTS_LOIV1 = ("""/MSH
	/PID
	/ORC
	/OBR
	/DG1
	/OBX
	/OBX
	/OBX
	/SGH
	/ORC
	/OBR
	/OBX
	/OBX
	/OBX
	/SGT""".stripMargin('/'), List(SegmentInstance(LOIv1_MSH, Location(EType.Segment, "MSH", "MSH", 1, 1, "MSH[1]"), 1, Nil, hasExtra = true, "MSH"),
	GroupInstance(LOIv1_PATIENT, 1, List(SegmentInstance(LOIv1_PATIENT_PID, Location(EType.Segment, "PID", "PID", 2, 1, "PID[1]"), 1, Nil, hasExtra = true, "PID"))),
	GroupInstance(LOIv1_ORDER, 1, List(SegmentInstance(LOIv1_ORDER_ORC, Location(EType.Segment, "ORC", "ORC", 3, 1, "ORC[1]"), 1, Nil, hasExtra = true, "ORC"),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBR, Location(EType.Segment, "OBR", "OBR", 4, 1, "OBR[1]"), 1, Nil, hasExtra = true, "OBR"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_DG1, Location(EType.Segment, "DG1", "DG1", 5, 1, "DG1[1]"), 1, Nil, hasExtra = true, "DG1"),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 6, 1, "OBX[1]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION, 2, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 7, 1, "OBX[2]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION, 3, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 8, 1, "OBX[3]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_SGH, Location(EType.Segment, "SGH", "SGH", 9, 1, "SGH[1]"), 1, Nil, hasExtra = true, "SGH"),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_ORC, Location(EType.Segment, "ORC", "ORC", 10, 1, "ORC[2]"), 1, Nil, hasExtra = true, "ORC"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_OBR, Location(EType.Segment, "OBR", "OBR", 11, 1, "OBR[2]"), 1, Nil, hasExtra = true, "OBR"),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_OBSERVATION_PRIOR, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_OBSERVATION_PRIOR_OBX, Location(EType.Segment, "OBX", "OBX", 12, 1, "OBX[4]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_OBSERVATION_PRIOR, 2, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_OBSERVATION_PRIOR_OBX, Location(EType.Segment, "OBX", "OBX", 13, 1, "OBX[5]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_OBSERVATION_PRIOR, 3, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_OBSERVATION_PRIOR_OBX, Location(EType.Segment, "OBX", "OBX", 14, 1, "OBX[6]"), 1, Nil, hasExtra = true, "OBX"))))),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_SGT, Location(EType.Segment, "SGT", "SGT", 15, 1, "SGT[1]"), 1, Nil, hasExtra = true, "SGT")))))))))
	val ONE_ORDER_GROUP_WITH_MULTIPLE_PRIOR_RESULTS_LOIV1 = ("""/MSH
	/PID
	/ORC
	/OBR
	/DG1
	/OBX
	/OBX
	/OBX
	/SGH
	/ORC
	/OBR
	/OBX
	/OBX
	/OBX
	/SGT
	/SGH
	/ORC
	/OBR
	/OBX
	/OBX
	/OBX
	/SGT""".stripMargin('/'), List(SegmentInstance(LOIv1_MSH, Location(EType.Segment, "MSH", "MSH", 1, 1, "MSH[1]"), 1, Nil, hasExtra = true, "MSH"),
	GroupInstance(LOIv1_PATIENT, 1, List(SegmentInstance(LOIv1_PATIENT_PID, Location(EType.Segment, "PID", "PID", 2, 1, "PID[1]"), 1, Nil, hasExtra = true, "PID"))),
	GroupInstance(LOIv1_ORDER, 1, List(SegmentInstance(LOIv1_ORDER_ORC, Location(EType.Segment, "ORC", "ORC", 3, 1, "ORC[1]"), 1, Nil, hasExtra = true, "ORC"),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBR, Location(EType.Segment, "OBR", "OBR", 4, 1, "OBR[1]"), 1, Nil, hasExtra = true, "OBR"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_DG1, Location(EType.Segment, "DG1", "DG1", 5, 1, "DG1[1]"), 1, Nil, hasExtra = true, "DG1"),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 6, 1, "OBX[1]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION, 2, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 7, 1, "OBX[2]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION, 3, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 8, 1, "OBX[3]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_SGH, Location(EType.Segment, "SGH", "SGH", 9, 1, "SGH[1]"), 1, Nil, hasExtra = true, "SGH"),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_ORC, Location(EType.Segment, "ORC", "ORC", 10, 1, "ORC[2]"), 1, Nil, hasExtra = true, "ORC"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_OBR, Location(EType.Segment, "OBR", "OBR", 11, 1, "OBR[2]"), 1, Nil, hasExtra = true, "OBR"),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_OBSERVATION_PRIOR, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_OBSERVATION_PRIOR_OBX, Location(EType.Segment, "OBX", "OBX", 12, 1, "OBX[4]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_OBSERVATION_PRIOR, 2, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_OBSERVATION_PRIOR_OBX, Location(EType.Segment, "OBX", "OBX", 13, 1, "OBX[5]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_OBSERVATION_PRIOR, 3, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_OBSERVATION_PRIOR_OBX, Location(EType.Segment, "OBX", "OBX", 14, 1, "OBX[6]"), 1, Nil, hasExtra = true, "OBX"))))),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_SGT, Location(EType.Segment, "SGT", "SGT", 15, 1, "SGT[1]"), 1, Nil, hasExtra = true, "SGT"))),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT, 2, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_SGH, Location(EType.Segment, "SGH", "SGH", 16, 1, "SGH[2]"), 1, Nil, hasExtra = true, "SGH"),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_ORC, Location(EType.Segment, "ORC", "ORC", 17, 1, "ORC[3]"), 1, Nil, hasExtra = true, "ORC"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_OBR, Location(EType.Segment, "OBR", "OBR", 18, 1, "OBR[3]"), 1, Nil, hasExtra = true, "OBR"),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_OBSERVATION_PRIOR, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_OBSERVATION_PRIOR_OBX, Location(EType.Segment, "OBX", "OBX", 19, 1, "OBX[7]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_OBSERVATION_PRIOR, 2, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_OBSERVATION_PRIOR_OBX, Location(EType.Segment, "OBX", "OBX", 20, 1, "OBX[8]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_OBSERVATION_PRIOR, 3, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_OBSERVATION_PRIOR_OBX, Location(EType.Segment, "OBX", "OBX", 21, 1, "OBX[9]"), 1, Nil, hasExtra = true, "OBX"))))),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_SGT, Location(EType.Segment, "SGT", "SGT", 22, 1, "SGT[2]"), 1, Nil, hasExtra = true, "SGT")))))))))
	val ONE_ORDER__ONE_PRIOR_RESULT__MULTIPLE_ORDER_PRIOR_LOIV1 = ("""/MSH
	/PID
	/ORC
	/OBR
	/DG1
	/OBX
	/OBX
	/OBX
	/SGH
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
	/SGT""".stripMargin('/'), List(SegmentInstance(LOIv1_MSH, Location(EType.Segment, "MSH", "MSH", 1, 1, "MSH[1]"), 1, Nil, hasExtra = true, "MSH"),
	GroupInstance(LOIv1_PATIENT, 1, List(SegmentInstance(LOIv1_PATIENT_PID, Location(EType.Segment, "PID", "PID", 2, 1, "PID[1]"), 1, Nil, hasExtra = true, "PID"))),
	GroupInstance(LOIv1_ORDER, 1, List(SegmentInstance(LOIv1_ORDER_ORC, Location(EType.Segment, "ORC", "ORC", 3, 1, "ORC[1]"), 1, Nil, hasExtra = true, "ORC"),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBR, Location(EType.Segment, "OBR", "OBR", 4, 1, "OBR[1]"), 1, Nil, hasExtra = true, "OBR"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_DG1, Location(EType.Segment, "DG1", "DG1", 5, 1, "DG1[1]"), 1, Nil, hasExtra = true, "DG1"),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 6, 1, "OBX[1]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION, 2, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 7, 1, "OBX[2]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION, 3, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_OBSERVATION_OBX, Location(EType.Segment, "OBX", "OBX", 8, 1, "OBX[3]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_SGH, Location(EType.Segment, "SGH", "SGH", 9, 1, "SGH[1]"), 1, Nil, hasExtra = true, "SGH"),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_ORC, Location(EType.Segment, "ORC", "ORC", 10, 1, "ORC[2]"), 1, Nil, hasExtra = true, "ORC"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_OBR, Location(EType.Segment, "OBR", "OBR", 11, 1, "OBR[2]"), 1, Nil, hasExtra = true, "OBR"),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_OBSERVATION_PRIOR, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_OBSERVATION_PRIOR_OBX, Location(EType.Segment, "OBX", "OBX", 12, 1, "OBX[4]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_OBSERVATION_PRIOR, 2, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_OBSERVATION_PRIOR_OBX, Location(EType.Segment, "OBX", "OBX", 13, 1, "OBX[5]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_OBSERVATION_PRIOR, 3, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_OBSERVATION_PRIOR_OBX, Location(EType.Segment, "OBX", "OBX", 14, 1, "OBX[6]"), 1, Nil, hasExtra = true, "OBX"))))),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR, 2, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_ORC, Location(EType.Segment, "ORC", "ORC", 15, 1, "ORC[3]"), 1, Nil, hasExtra = true, "ORC"),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_OBR, Location(EType.Segment, "OBR", "OBR", 16, 1, "OBR[3]"), 1, Nil, hasExtra = true, "OBR"),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_OBSERVATION_PRIOR, 1, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_OBSERVATION_PRIOR_OBX, Location(EType.Segment, "OBX", "OBX", 17, 1, "OBX[7]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_OBSERVATION_PRIOR, 2, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_OBSERVATION_PRIOR_OBX, Location(EType.Segment, "OBX", "OBX", 18, 1, "OBX[8]"), 1, Nil, hasExtra = true, "OBX"))),
	GroupInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_OBSERVATION_PRIOR, 3, List(SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_ORDER_PRIOR_OBSERVATION_PRIOR_OBX, Location(EType.Segment, "OBX", "OBX", 19, 1, "OBX[9]"), 1, Nil, hasExtra = true, "OBX"))))),
	SegmentInstance(LOIv1_ORDER_OBSERVATION_REQUEST_PRIOR_RESULT_SGT, Location(EType.Segment, "SGT", "SGT", 20, 1, "SGT[1]"), 1, Nil, hasExtra = true, "SGT")))))))))
	

}
