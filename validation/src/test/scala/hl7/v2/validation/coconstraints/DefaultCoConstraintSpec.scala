package hl7.v2.validation.coconstraints

import org.specs2.Specification

object DefaultCoConstraintSpec extends Specification with CellCoConstraintSpec with CoConstraintTableSpec {
  def is = s2"""
      Co-Constraint Cell Validation
        InconclusiveAssertion should be considered [NO_MATCH] ${inconclusiveIsNoMatch()}
        PlainText
          Should return CoConstraintPlainTextSuccess if one of path elements text is equal to data element value (case sensitive) ${plainTextSuccess()}
          if no path element succeeded or was inconclusive for any data element
            Should return CoConstraintPlainTextFailure for each failed element if all path elements text is not equal to data element value (case sensitive) ${plainTextFailure()}
          Should return InconclusiveAssertion for each inconclusive element if one or many of the path elements evaluation was inconclusive ${plainTextInconclusive()}
          CoConstraintPlainTextSuccess should be considered as [MATCH] ${plainTextSuccessIsMatch()}
          CoConstraintPlainTextFailure should be considered as [NO_MATCH] ${plainTextFailureIsNoMatch()}
        Code
          Should return CoConstraintCodeSuccess if one of path elements matches code and code system for one of the binding locations ${codeSuccess()}
          if no path element succeeded or was inconclusive for any location
            Should return CoConstraintCodeFailureNotFound for each failed element where code did not match ${codeFailureCodeNotFound()}
            Should return CoConstraintCodeFailureInvalidCs for each failed element where code system did not match ${codeFailureCodeSysInvalid()}
            Should return CoConstraintCodeFailureNotFoundCs for each failed element where code system was not found ${codeFailureCodeSysNotFound()}
          Should return InconclusiveAssertion if code path or code system path returned multiple elements ${codeInconclusiveMultiple()}
          Should return InconclusiveAssertion if code path or code system path evaluation was inconclusive ${codeInconclusivePath()}
          CoConstraintCodeSuccess should be considered as [MATCH] ${codeSuccessIsMatch()}
          CoConstraintCodeFailureNotFound should be considered as [NO_MATCH] ${codeNotFoundNoMatch()}
          CoConstraintCodeFailureInvalidCs should be considered as [NO_MATCH] ${codeInvalidCsNoMatch()}
          CoConstraintCodeFailureNotFoundCs should be considered as [NO_MATCH] ${codeNotFoundCsNoMatch()}
        ValueSet Binding
          Should return CoConstraintVsBindingSuccess if one of path elements binding validation for one of the bindings was considered "code found" ${vsBindingSuccess()}
          if no path element succeeded or was inconclusive for any location
            Should return CoConstraintVsBindingFailure for each failed element where code did not match ${vsBindingFailure()}
          Should return InconclusiveVsBinding for each vs binding spec error ${vsBindingVsSpecErr()}
          Should return InconclusiveAssertion for each inconclusive element if one or many of the path elements evaluation was inconclusive ${vsBindingInconclusive()}
          CoConstraintVsBindingSuccess should be considered as [MATCH] ${vsBindingSuccessIsMatch}
          CoConstraintVsBindingFailure should be considered as [NO_MATCH] ${vsBindingFailureNoMatch}

      Co-Constraint Validation
        Requirement
          Should return a RequiredCoConstraint if co-constraint usage is R but was not found ${requiredCc()}
          Should return a CardinalityCoConstraint if co-constraint cardinality min is violated ${minCardinality()}
          Should return a CardinalityCoConstraint if co-constraint cardinality max is violated ${maxCardinality()}
        Matching
          Should [MATCH] co-constraint if all selectors are matched ${ccMatch()}
          Should [NO_MATCH] co-constraint if one or many selectors evaluation
            Failed ${ccNoMatchFailed()}
            Was Inconclusive ${ccNoMatchInconclusive()}
            Was not evaluated because an element was not populated ${ccNoMatchNotPopulated()}
        Group ID
          Should return a NonDistinctGroupId If individual co-constraint has a non-unique groupId ${ccNoDistinctGroupId()}
        Constraint Failure
          Should check constraints of each in a matched co-constraint
            PlainText ${ccMatchPlainText()}
            Code ${ccMatchFailCode()}
            VS Binding ${ccMatchFailVs()}

      Co-Constraint Group Validation
        Requirement
          Should return a RequiredGroup if group usage is R but was not matched ${requiredGrp()}
          Should return a CardinalityGroup if co-constraint group cardinality min is violated ${minCardinalityGrp()}
          Should return a CardinalityGroup if co-constraint group cardinality max is violated ${maxCardinalityGrp()}
        Matching
          Should [MATCH] group if primary co-constraint is matched ${grpMatch()}
          Should [NO_MATCH] co-constraint group if primary co-constraint is not matched
            Failed ${grpNoMatchFailed()}
            Was Inconclusive $grpNoMatchInconclusive
            Was not evaluated because an element was not populated ${grpNoMatchNotPopulated()}
        Group ID
          If segment has a different group Id than the matched primary
            Should not [MATCH] a co-constraint from the group to the segment ${groupIdNoMatch()}
          If segment has the same group Id as the matched primary
            Should [MATCH] a co-constraint from the group to the segment ${groupIdMatch()}
        Constraint Failure
          Should return a failure for each failed co-constraint in the matched group ${grpMatchEval()}

      Co-Constraint Conditional
        If condition is PASS
          Co-Constraint Table should be evaluated ${conditionalPass()}
        If condition is FAIL
          Co-Constraint Table should not be evaluated ${conditionalFail()}
        If condition is INCONCLUSIVE
          Co-Constraint Spec Error should be returned ${conditionalInc()}

      Specification Errors
        Should return a specification error
          If co-constraint binding target path contains elements that are not segments ${specErrTargetPathNoSeg()}
          If co-constraint binding target path can't be reached ${specErrTargetPathInvalid()}
          If conditional co-constraint table's condition can't be evaluated ${specErrCondInconclusive()}
          If co-constraint group ID evaluation is inconclusive ${specErrGroupIdInconclusive()}

"""
}
