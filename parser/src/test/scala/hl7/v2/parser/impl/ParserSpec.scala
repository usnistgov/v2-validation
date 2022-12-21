package hl7.v2.parser.impl

trait ParserSpec extends org.specs2.Specification
  with LOISpec
  with LOIv1Spec
  with VXUSpec
  with ORUSpec {
  def is = s2"""
     Segment & Group Parsing
        Simple Look ahead to match a group.
        GROUP_HEAD is the list of segments at the top of a group profile.
        CURRENT_SEGMENT is the segment in the message that the parser is currently on
        CURRENT_MODEL is the profile node that the parser is currently on
        Example Group Model:
          GROUP_1
            SEG_A
            SEG_B
            GROUP_1.1
              SEG_C
              SEG_D
        In this example the GROUP_HEAD of GROUP_1 is [SEG_A, SEG_B], GROUP_1.1 [SEG_C, SEG_D]
        Group Matching :
          If the CURRENT_MODEL is a Group Model and the CURRENT_SEGMENT is part of the GROUP_HEAD of the CURRENT_MODEL,
          the CURRENT_SEGMENT will be considered as being contained in the CURRENT_MODEL's Group Model, otherwise the CURRENT_MODEL will be considered as missing
          When no GROUP_HEAD are in the model, perform a look forward inside all the children

        Test Cases
          LOI

                  JAIMIE_VALID_MESSAGE_LOI $JAIMIE_VALID_MESSAGE_LOI_TEST
                  JAIMIE_VALID_MESSAGE_WITH_ORDER_PRIOR_LOI $JAIMIE_VALID_MESSAGE_WITH_ORDER_PRIOR_LOI_TEST

          LOIv1

                  PT_LOIV1 $PT_LOIV1_TEST
                  SED_RATE_LOIV1 $SED_RATE_LOIV1_TEST
                  CBC_LOIV1 $CBC_LOIV1_TEST
                  LIPID_PANEL_LOIV1 $LIPID_PANEL_LOIV1_TEST
                  LIPID_PANEL_FI_LOIV1 $LIPID_PANEL_FI_LOIV1_TEST
                  CULTURE_AND_SUSCEP_LOIV1 $CULTURE_AND_SUSCEP_LOIV1_TEST
                  REFLEX_HEPATITIS_LOIV1 $REFLEX_HEPATITIS_LOIV1_TEST
                  PAP_SMEAR_LOIV1 $PAP_SMEAR_LOIV1_TEST
                  GHP_LOIV1 $GHP_LOIV1_TEST
                  CREATININE_CLEARANCE_LOIV1 $CREATININE_CLEARANCE_LOIV1_TEST
                  PROSTATE_BIOPSY_LOIV1 $PROSTATE_BIOPSY_LOIV1_TEST
                  ALL_SEGMENTS__NO_REPS_LOIV1 $ALL_SEGMENTS__NO_REPS_LOIV1_TEST
                  ALL_SEGMENTS__SEGMENT_REPETITIONS_LOIV1 $ALL_SEGMENTS__SEGMENT_REPETITIONS_LOIV1_TEST
                  MULTIPLE_GROUPS_REPS_LOIV1 $MULTIPLE_GROUPS_REPS_LOIV1_TEST
                  ONE_ORDER_GROUP_WITH_PRIOR_RESULTS_LOIV1 $ONE_ORDER_GROUP_WITH_PRIOR_RESULTS_LOIV1_TEST
                  ONE_ORDER_GROUP_WITH_MULTIPLE_PRIOR_RESULTS_LOIV1 $ONE_ORDER_GROUP_WITH_MULTIPLE_PRIOR_RESULTS_LOIV1_TEST
                  ONE_ORDER__ONE_PRIOR_RESULT__MULTIPLE_ORDER_PRIOR_LOIV1 $ONE_ORDER__ONE_PRIOR_RESULT__MULTIPLE_ORDER_PRIOR_LOIV1_TEST

          ORU

                  SED_RATE_1_ORU $SED_RATE_1_ORU_TEST
                  SED_RATE_1__NO_ORC_ORU $SED_RATE_1__NO_ORC_ORU_TEST
                  SED_RATE_2_ORU $SED_RATE_2_ORU_TEST
                  SED_RATE_3_ORU $SED_RATE_3_ORU_TEST
                  CBC_ORU $CBC_ORU_TEST
                  CULTURE_AND_SUSCEPTIBILITY_2_ORU $CULTURE_AND_SUSCEPTIBILITY_2_ORU_TEST
                  REFLEX_1_ORU $REFLEX_1_ORU_TEST
                  REFLEX_1__NO_ORC_ORU $REFLEX_1__NO_ORC_ORU_TEST
                  REFLEX_1__THE_FIRST_ORC_ORU $REFLEX_1__THE_FIRST_ORC_ORU_TEST
                  REFLEX_1__THE_SECOND_ORC_PRESENT_ORU $REFLEX_1__THE_SECOND_ORC_PRESENT_ORU_TEST
                  CCHD_EXAMPLE_MESSAGE_ORU $CCHD_EXAMPLE_MESSAGE_ORU_TEST

          VXU

                  CONTEXT_FREE_EXAMPLE_VXU $CONTEXT_FREE_EXAMPLE_VXU_TEST
  """
}

