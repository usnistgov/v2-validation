package expression

trait EvaluatorSpec
  extends org.specs2.Specification
  with PresenceSpec
  with PlainTextSpec
  with FormatSpec
  with StringListSpec
  with NumberListSpec
  with SimpleValueSpec
  with PathValueSpec
  with ANDSpec
  with ORSpec
  with NOTSpec
  with PluginSpec
  with XORSpec
  with IMPLYSpec
  with FORALLSpec
  with EXISTSpec
  with StringFormatSpec
  with SubContextSpec
  with ComplexPathValueSpec
  with Mocks { def is = s2"""

  Expression evaluator specifications

    Given the following elements:

    ${ elementsDescription /* See ElementsMocks for details */ }

    Presence expression evaluation specifications
      Presence evaluation should be inconclusive if the path is invalid             $presencePathInvalid
      Presence evaluation should be inconclusive if the path is unreachable         $presencePathUnreachable
      Presence should pass if the path is populated                                 $presencePathPopulated
      Presence should fail if the path is not populated                             $presencePathNotPopulated

    PlainTextSpec
      PlainText evaluation should succeed if the path is not populated              $plainTextPathNotPopulated
      PlainText evaluation should be inconclusive if the path is complex            $plainTextPathComplex
      PlainText evaluation should be inconclusive if the path is invalid            $plainTextPathInvalid
      PlainText evaluation should be inconclusive if the path is unreachable        $plainTextPathUnreachable
      PlainText evaluation should pass if the values are the same                              $plainTextSameValue
      PlainText evaluation should pass if the values are the same by ignoring the case         $plainTextSameValueIC
      PlainText evaluation should fail if the values are different                             $plainTextDifferentValue
      PlainText evaluation should fail for same values in different case when case not ignored $plainTextSameValueCNI
      If the path is valued to multiple elements
        PlainText evaluation should fail if one of the elements value is different than the expected value with AtLeastOnce = False $plainTextAtLeastOnceF
        PlainText evaluation should pass if one of the elements value is equal to the expected value with AtLeastOnce = True $plainTextAtLeastOnceT
        Min = 1 & Max = *
          PlainText evaluation should fail if 0 of the elements value is equal to the expected $plainTextMinMax1s0
          PlainText evaluation should pass if 1 of the elements value is equal to the expected $plainTextMinMax1s1
          PlainText evaluation should pass if 2 of the elements value is equal to the expected $plainTextMinMax1s2
          PlainText evaluation should pass if 3 (all) of the elements value is equal to the expected $plainTextMinMax1s3
        Min = 2 & Max = 2
          PlainText evaluation should fail if 0 of the elements value is equal to the expected $plainTextMinMax220
          PlainText evaluation should fail if 1 of the elements value is equal to the expected $plainTextMinMax221
          PlainText evaluation should pass if 2 of the elements value is equal to the expected $plainTextMinMax222
          PlainText evaluation should fail if 3 (all) of the elements value is equal to the expected $plainTextMinMax223
        Min = 0 & Max = *
          PlainText evaluation should pass if 0 of the elements value is equal to the expected $plainTextMinMax0s0
          PlainText evaluation should pass if 1 of the elements value is equal to the expected $plainTextMinMax0s1
          PlainText evaluation should pass if 3 (all) of the elements value is equal to the expected $plainTextMinMax0s3
      PlainText evaluation should fail If not present behavior is FAIL and no element is found  $plainTextNoElmFAIL
      PlainText evaluation should be inconclusive If not present behavior is INCONCLUSIVE and no element is found $plainTextNoElmINC
      PlainText evaluation should pass If not present behavior is PASS and no element is found $plainTextNoElmPASS
        
    FormatSpec
      Format evaluation should succeed if the path is not populated                 $formatPathNotPopulated
      Format should pass if the value match the pattern                             $formatMatch
      Format should fail if the value doesn't match the pattern                     $formatNoMatch
      If the path is valued to multiple elements
        Format should pass if one of the elements matches the pattern and AtLeastOnce = True           $formatAtLeastOnceT
        Format should fail if one of the elements doesn't match the pattern and AtLeastOnce = False           $formatAtLeastOnceF
        Min = 1 & Max = *
          Format evaluation should fail if 0 of the elements value matches the pattern $formatMinMax1s0
          Format evaluation should pass if 1 of the elements value matches the pattern $formatMinMax1s1
          Format evaluation should pass if 2 of the elements value matches the pattern $formatMinMax1s2
          Format evaluation should pass if 3 (all) of the elements value matches the pattern $formatMinMax1s3
        Min = 2 & Max = 2
          Format evaluation should fail if 0 of the elements value matches the pattern $formatMinMax220
          Format evaluation should fail if 1 of the elements value matches the pattern $formatMinMax221
          Format evaluation should pass if 2 of the elements value matches the pattern $formatMinMax222
          Format evaluation should fail if 3 (all) of the elements value matches the pattern $formatMinMax223
        Min = 0 & Max = *
          Format evaluation should pass if 0 of the elements value matches the pattern $formatMinMax0s0
          Format evaluation should pass if 1 of the elements value matches the pattern $formatMinMax0s1
          Format evaluation should pass if 3 (all) of the elements value matches the pattern $formatMinMax0s3
      Format evaluation should fail If not present behavior is FAIL and no element is found  $formatNoElmFAIL
      Format evaluation should be inconclusive If not present behavior is INCONCLUSIVE and no element is found $formatNoElmINC
      Format evaluation should pass If not present behavior is PASS and no element is found $formatNoElmPASS

    StringListSpec
      StringList evaluation should succeed if the path is not populated            $stringListPathNotPopulated
      StringList evaluation should be inconclusive if the path is complex          $stringListPathComplex
      StringList evaluation should be inconclusive if the path is invalid          $stringListPathInvalid
      StringList evaluation should be inconclusive if the path is unreachable      $stringListPathUnreachable
      StringList should pass if the values are in the list                         $stringListValueInList
      StringList should fail if the values are in the list                         $stringListValueNotInList
      If the path is valued to multiple elements
        StringList should pass if one of the elements is in the list and AtLeastOnce = True           $stringListAtLeastOnceT
        StringList should fail if one of the elements is not in the list and AtLeastOnce = False           $stringListAtLeastOnceF
        Min = 1 & Max = *
          StringList evaluation should fail if 0 of the elements value is in the list $stringListMinMax1s0
          StringList evaluation should pass if 1 of the elements value is in the list $stringListMinMax1s1
          StringList evaluation should pass if 2 of the elements value is in the list $stringListMinMax1s2
          StringList evaluation should pass if 3 (all) of the elements value is in the list $stringListMinMax1s3
        Min = 2 & Max = 2
          StringList evaluation should fail if 0 of the elements value is in the list $stringListMinMax220
          StringList evaluation should fail if 1 of the elements value is in the list $stringListMinMax221
          StringList evaluation should pass if 2 of the elements value is in the list $stringListMinMax222
          StringList evaluation should fail if 3 (all) of the elements value is in the list $stringListMinMax223
        Min = 0 & Max = *
          StringList evaluation should pass if 0 of the elements value is in the list $stringListMinMax0s0
          StringList evaluation should pass if 1 of the elements value is in the list $stringListMinMax0s1
          StringList evaluation should pass if 3 (all) of the elements value is in the list $stringListMinMax0s3
      StringList evaluation should fail If not present behavior is FAIL and no element is found  $stringListNoElmFAIL
      StringList evaluation should be inconclusive If not present behavior is INCONCLUSIVE and no element is found $stringListNoElmINC
      StringList evaluation should pass If not present behavior is PASS and no element is found $stringListNoElmPASS

    NumberListSpec
      NumberList evaluation should succeed if the path is not populated            $numberListPathNotPopulated
      NumberList evaluation should be inconclusive if the path is complex          $numberListPathComplex
      NumberList evaluation should be inconclusive if the path is invalid          $numberListPathInvalid
      NumberList evaluation should be inconclusive if the path is unreachable      $numberListPathUnreachable
      NumberList should be inconclusive if at least one value is not a number      $numberListNaN
      NumberList should pass if the values are in the list                         $numberListValueInList
      NumberList should fail if the values are in the list                         $numberListValueNotInList
      If the path is valued to multiple elements
        NumberList should pass if one of the elements is in the list and AtLeastOnce = True           $numberListAtLeastOnceT
        NumberList should fail if one of the elements is not in the list and AtLeastOnce = False           $numberListAtLeastOnceF
        Min = 1 & Max = *
          NumberList evaluation should fail if 0 of the elements value is in the list $numberListMinMax1s0
          NumberList evaluation should pass if 1 of the elements value is in the list $numberListMinMax1s1
          NumberList evaluation should pass if 2 of the elements value is in the list $numberListMinMax1s2
          NumberList evaluation should pass if 3 (all) of the elements value is in the list $numberListMinMax1s3
        Min = 2 & Max = 2
          NumberList evaluation should fail if 0 of the elements value is in the list $numberListMinMax220
          NumberList evaluation should fail if 1 of the elements value is in the list $numberListMinMax221
          NumberList evaluation should pass if 2 of the elements value is in the list $numberListMinMax222
          NumberList evaluation should fail if 3 (all) of the elements value is in the list $numberListMinMax223
        Min = 0 & Max = *
          NumberList evaluation should pass if 0 of the elements value is in the list $numberListMinMax0s0
          NumberList evaluation should pass if 1 of the elements value is in the list $numberListMinMax0s1
          NumberList evaluation should pass if 3 (all) of the elements value is in the list $numberListMinMax0s3
      NumberList evaluation should fail If not present behavior is FAIL and no element is found  $numberListNoElmFAIL
      NumberList evaluation should be inconclusive If not present behavior is INCONCLUSIVE and no element is found $numberListNoElmINC
      NumberList evaluation should pass If not present behavior is PASS and no element is found $numberListNoElmPASS

    SimpleValueSpec
      SimpleValue evaluation should succeed if the path is not populated            $simpleValuePathNotPopulated
      SimpleValue evaluation should be inconclusive if the path is complex          $simpleValuePathComplex
      SimpleValue evaluation should be inconclusive if the path is invalid          $simpleValuePathInvalid
      SimpleValue evaluation should be inconclusive if the path is unreachable      $simpleValuePathUnreachable
      SimpleValue should be inconclusive if the values are not comparable           $simpleValueNotComparable
      SimpleValue should be inconclusive if at least one value is invalid           $simpleValueInvalidValue
      SimpleValue should pass if operator = < and path.value < value                $simpleValuePass
      SimpleValue should fail if operator = < and path.value > value                $simpleValueFail
      If the path is valued to multiple elements
        SimpleValue should pass if one of the elements is in the list and AtLeastOnce = True           $simpleValueAtLeastOnceT
        SimpleValue should fail if one of the elements is not in the list and AtLeastOnce = False           $simpleValueAtLeastOnceF
        Min = 1 & Max = *
          SimpleValue evaluation should fail if 0 of the elements value eq expected value $simpleValueMinMax1s0
          SimpleValue evaluation should pass if 1 of the elements value eq expected value $simpleValueMinMax1s1
          SimpleValue evaluation should pass if 2 of the elements value eq expected value $simpleValueMinMax1s2
          SimpleValue evaluation should pass if 3 (all) of the elements value eq expected value $simpleValueMinMax1s3
        Min = 2 & Max = 2
          SimpleValue evaluation should fail if 0 of the elements value eq expected value $simpleValueMinMax220
          SimpleValue evaluation should fail if 1 of the elements value eq expected value $simpleValueMinMax221
          SimpleValue evaluation should pass if 2 of the elements value eq expected value $simpleValueMinMax222
          SimpleValue evaluation should fail if 3 (all) of the elements value eq expected value $simpleValueMinMax223
        Min = 0 & Max = *
          SimpleValue evaluation should pass if 0 of the elements value eq expected value $simpleValueMinMax0s0
          SimpleValue evaluation should pass if 1 of the elements value eq expected value $simpleValueMinMax0s1
          SimpleValue evaluation should pass if 3 (all) of the elements value eq expected value $simpleValueMinMax0s3
      SimpleValue evaluation should fail If not present behavior is FAIL and no element is found  $simpleValueNoElmFAIL
      SimpleValue evaluation should be inconclusive If not present behavior is INCONCLUSIVE and no element is found $simpleValueNoElmINC
      SimpleValue evaluation should pass If not present behavior is PASS and no element is found $simpleValueNoElmPASS

    PathValueSpec
      PathValue should pass if both pass are not populated                         $pathValueBothPathNotPopulated
      PathValue evaluation should be inconclusive if the path is complex           $pathValuePathComplex
      PathValue evaluation should be inconclusive if the path is invalid           $pathValuePathInvalid
      PathValue evaluation should be inconclusive if the path is unreachable       $pathValuePathUnreachable
      PathValue should fail if only one path is populated                          $pathValueOnePathPopulated
      Single (Mode: All, AtLeastOnce, One, Count = 1)
        PathValue should pass if operator = < and path1.value < path2.value          $pathValuePass
        PathValue should fail if operator = < and path1.value > path2.value          $pathValueFail
      Multiple
        AtLeastOnce
          AtLeastOnce
            Pass
              ([A, "B", C], [D, "B", E, ....]) ${aloTalo1()}
              (["A", "B", C], ["A", "B", ....]) ${aloTalo2()}
              ([A, "B", C], [D, "B", "B", ....]) ${aloTalo3()}
              (["B", "B", "B"], ["B", "B", "B"]) ${aloTalo4()}
            Fail
              ([A, B, C], [D, E, F]) ${faloTalo1()}
          All
            Pass
              (["A", B, C], ["A", "A", "A", As....]) $aloTall1
              (["B", "B", C], ["B", "B", Bs....]) $aloTall2
              (["C", "C", "C"], ["C", "C", "C", Cs....]) $aloTall3
            Fail
              ([A, B, C], [A, B, C]) $faloTall1
              ([A, B, C], [A, A, C]) $faloTall2
          Count
            Pass
              (Count = 1)
                ([A, "B", C], [D, "B", E, ...]) $aloToc11
                ([A, "B", "C"], [D, "B", "C"]) $aloToc12
                (["B", "B", "B"], [A, "B", C]) $aloToc13
                (["A", "B", "C"], ["A", "B", "C"]) $aloToc14
                ([A, "B", "C"], ["B", "B", "C"]) $aloToc15
              (Count = 2)
                ([A, "B", C], [D, "B", "B", ...]) $aloToc21
                ([A, "B", "C"], [D, "B", "B", "C", "C", ....]) $aloToc22
            Fail
              (Count = 1)
                ([A, B, C], [D, E, F]) $faloToc11
                ([B, B, B], [B, B, B]) $faloToc12
                ([A, B, C], [B, B, D]) $faloToc13
              (Count = 2)
                ([A, "B", C], [D, "B", E, ...]) $faloToc21
                ([A, "B", "C"], [D, "B", "C"]) $faloToc22
        All
          AtLeastOnce
            Pass
              (["A", "B", "C"], ["A", "B", "C", ....]) $allToalo1
              (["A", "B", "C"], ["A", "A", "B", "C", ....]) $allToalo2
              (["A", "B", "C"], ["A", "A", "B", "B", "C", "C", ....]) $allToalo3
            Fail
              (["A", "B", "C"], ["A", "B", ....]) $fallToalo1
              (["A", "B", "C"], ["A",  .....]) $fallToalo2
              (["A", "B", "C"], [X, Y, Z]) $fallToalo3
          All
            Pass
              (["A", "A", "A"], ["A", "A", As....]) $allToall1
            Fail
              (["A", "B", "C"], ["A", "B", "C"]) $fallToall1
              (["A", "A"], ["A", "A", "C"]) $fallToall2
          Count
            Pass
              (Count = 1)
                (["A", "B", "C"], ["A", "B", "C", ....]) $allToc11
              (Count = 2)
                (["A", "B", "C"], ["A", "A", "B", "B", "C", "C", ....]) $allToc12
            Fail
              (Count = 1)
                (["A", "B", "C"], ["A", "A", "B", "C", ....]) $fallToc11
                (["A", "B", "C"], ["A", "A", "B", "B", "C", "C", ....]) $fallToc12
                (["A", "B", "C"], ["A", Y, Z]) $fallToc13
              (Count = 2)
                (["A", "B", "C"], ["A", "A", "A", "B", "B", "C", "C", ....]) $fallToc21
                (["A", "B", "C"], ["A", "B", "C", ....]) $fallToc22
        Count
          AtLeastOnce
            Pass
              (Count = 1)
                (["A", "B", "C"], ["A", "X", "Y",....]) $cTalo11
                (["A", "B", "C"], ["A", "A", "X", "Y", ....]) $cTalo12
              (Count = 2)
                (["A", "B", "C"], ["A", "B", "X", "Y",....]) $cTalo21
                (["A", "B", "C"], ["A", "A", "C", "X", "Y", ....]) $cTalo22
            Fail
              (Count = 1)
                (["A", "B", "C"], ["X", "Y", "Z", ....]) $fcTalo11
                (["A", "B", "C"], ["A", "A", "B", "X", "Y", ....]) $fcTalo12
              (Count = 2)
                (["A", "B", "C"], ["A", "X", "Y",....]) $fcTalo21
                (["A", "B", "C"], ["A", "B", "C", "X", "Y", ....]) $fcTalo22
          All
            Pass
              (Count = 1)
                (["A", "B", "C"], ["A", "A", "A", As....]) $cTall11
                (["A", "B", "C"], ["B", "B", "B", Bs....]) $cTall12
              (Count = 2)
                (["A", "A", "C"], ["A", "A", "A", As....]) $cTall21
                (["A", "B", "B"], ["B", "B", "B", Bs....]) $cTall22
            Fail
              (Count = 1)
                (["A", "B", "C"], ["A", "B", "C",....]) $fcTall11
                (["A", "A", "C"], ["A", "A", "A", As....]) $fcTall12
              (Count = 2)
                (["A", "B", "C"], ["A", "A", "A", As....]) $fcTall21
          Count
            Pass
              (Count = 1 / Count = 1)
                (["A", "B", "C"], ["A", "X", "Y", ....]) $cTc111
                (["A", "B", "C"], ["A", "B", "B", "X", "Y", ....]) $cTc112
              (Count = 1 / Count = 2)
                (["A", "B", "C"], ["A", "A", "B", "C", ....]) $cTc121
              (Count = 2 / Count = 1)
                (["A", "B", "C"], ["A", "B", "C", "C", "X", "Y", ....]) $cTc211
              (Count = 2 / Count = 2)
                (["A", "B", "C"], ["A", "B", "B", "C", "C", "X", "Y", ....]) $cTc221
            Fail
              (Count = 1 / Count = 1)
                (["A", "B", "C"], ["X", "Y", "Z"]) $fcTc111
                (["A", "B", "C"], ["A", "B", "Y", ....]) $fcTc112
              (Count = 1 / Count = 2)
                (["A", "B", "C"], ["A", "A", "B", "B", "C", ....]) $fcTc121
              (Count = 2 / Count = 1)
                (["A", "B", "C"], ["A", "B", "B", "C", "C", "X", "Y", ....]) $fcTc211
              (Count = 2 / Count = 2)
                (["A", "B", "C"], ["A", "A", "B", "B", "C", "C", "X", "Y", ....]) $fcTc221
      PathValue evaluation should fail If not present behavior is FAIL and both paths not found  $pathValueNoElmFAIL
      PathValue evaluation should be inconclusive If not present behavior is INCONCLUSIVE and both paths not found $pathValueNoElmINC
      PathValue evaluation should pass If not present behavior is PASS and both paths not found $pathValueNoElmPASS

    FORALL expression evaluation specifications
           FORALL should be inconclusive if one of the expressions is inconclusive $forallInconclusive
           FORALL should fail if one of the expressions fails                      $forallOneFails
           FORALL should fail if many of the expressions fails                     $forallManyFail
           FORALL should pass if all the expressions pass                          $forallAllPass

    EXIST expression evaluation specifications
           EXIST should be inconclusive if one of the expressions is inconclusive  $existInconclusive
           EXIST should fail if all the expressions fails                          $existAllFail
           EXIST should pass if one of the expressions passes                      $existOnePasses
           EXIST should pass if many of the expressions pass                       $existManyPass

    AND expression evaluation specifications
      AND should be inconclusive if the first expression is inconclusive            $andFirstInconclusive
      AND should fail in the first expression fails                                 $andFirstFails
      If the first expression passes
          AND should be inconclusive if the second is inconclusive                  $andFirstPassesSecondInconclusive
          AND should pass if the second passes                                      $andFirstPassesSecondPasses
          AND should fail if the second fails                                       $andFirstPassesSecondFails

    OR expression evaluation specifications
      OR should be inconclusive if the first expression is inconclusive             $orFirstInconclusive
      OR should pass if the first expression passes                                 $orFirstPasses
      If the first expression fails
          OR should be inconclusive if the second is inconclusive                   $orFirstFailsSecondInconclusive
          OR should pass if the second passes                                       $orFirstFailsSecondPasses
          OR should fail if the second fails                                        $orFirstFailsSecondFails

    IMPLY expression evaluation specifications
      IMPLY should be inconclusive if the first expression is inconclusive          $implyFirstInconclusive
      IMPLY should pass if first expression fails                                   $implyFirstFails
      If the first expression passes
        IMPLY should be inconclusive if the second is inconclusive                  $implyFirstPassesSecondInconclusive
        IMPLY should fail if the second fails                                       $implyFirstPassesSecondFails
        IMPLY should pass if the second passes                                      $implyFirstPassesSecondPasses

    XOR expression evaluation specifications
         XOR should be inconclusive if the first expression is inconclusive         $xorFirstInconclusive
         If the first expression passes
             XOR should be inconclusive if the second is inconclusive               $xorFirstPassesSecondInconclusive
             XOR should pass if the second fails                                    $xorFirstPassesSecondFails
             XOR should fail if the second passes                                   $xorFirstPassesSecondPasses
         If the first expression fails
             XOR should be inconclusive if the second is inconclusive               $xorFirstFailsSecondInconclusive
             XOR should pass if the second passes                                   $xorFirstFailsSecondPasses
             XOR should fail if the second fails                                    $xorFirstFailsSecondFails

    NOT expression evaluation specifications
      NOT should be inconclusive if the underlining expression is inconclusive      $notInconclusive
      NOT should pass if the underlining expression fail                            $notPass
      NOT should fail if the underlining expression pass                            $notFail

    PluginSpec
      Plugin execution should be inconclusive if an exception is raised when evaluation the assertion   $pluginInconclusive
      Plugin execution should pass if the assertion evaluation is true                                  $pluginPass
      Plugin execution should fail if the assertion evaluation is false                                 $pluginFail
      Plugin execution should pass if the assertion evaluation returns a NULL list                      $pluginCustomPassNull
      Plugin execution should pass if the assertion evaluation returns an empty list                    $pluginCustomPassEmpty
      Plugin execution should fail if the assertion evaluation returns non-empty list                   $pluginCustomPassEmpty
      Plugin execution should be inconclusive if the implementation contains multiple matching methods  $pluginMulti
    
    StringFormatSpec
      StringFormat should succeed if the path is not populated $stringFormatPathNotPopulated
      StringFormat should fail if a LOINC string is invalid $stringFormatLOINCInvalid
      StringFormat should pass if a LOINC string is valid $stringFormatLOINCvalid
      StringFormat should fail if a SNOMED string is invalid $stringFormatSNOMEDInvalid
      StringFormat should pass if a SNOMED string is valid $stringFormatSNOMEDvalid
      StringFormat should be inconclusive if a string format is unrecognized $stringFormatUnknown
      If the path is valued to multiple elements
        StringFormat should pass if one of the elements is in the list and AtLeastOnce = True           $stringFormatAtLeastOnceT
        StringFormat should fail if one of the elements is not in the list and AtLeastOnce = False           $stringFormatAtLeastOnceF
        Min = 1 & Max = *
          StringFormat evaluation should fail if 0 of the elements value matches expected format $stringFormatMinMax1s0
          StringFormat evaluation should pass if 1 of the elements value matches expected format $stringFormatMinMax1s1
          StringFormat evaluation should pass if 2 of the elements value matches expected format $stringFormatMinMax1s2
          StringFormat evaluation should pass if 3 (all) of the elements value matches expected format $stringFormatMinMax1s3
        Min = 2 & Max = 2
          StringFormat evaluation should fail if 0 of the elements value matches expected format $stringFormatMinMax220
          StringFormat evaluation should fail if 1 of the elements value matches expected format $stringFormatMinMax221
          StringFormat evaluation should pass if 2 of the elements value matches expected format $stringFormatMinMax222
          StringFormat evaluation should fail if 3 (all) of the elements value matches expected format $stringFormatMinMax223
        Min = 0 & Max = *
          StringFormat evaluation should pass if 0 of the elements value matches expected format $stringFormatMinMax0s0
          StringFormat evaluation should pass if 1 of the elements value matches expected format $stringFormatMinMax0s1
          StringFormat evaluation should pass if 3 (all) of the elements value matches expected format $stringFormatMinMax0s3
      StringFormat evaluation should fail If not present behavior is FAIL and no element is found  $stringFormatNoElmFAIL
      StringFormat evaluation should be inconclusive If not present behavior is INCONCLUSIVE and no element is found $stringFormatNoElmINC
      StringFormat evaluation should pass If not present behavior is PASS and no element is found $stringFormatNoElmPASS

    SubContextSpec
      SubContext evaluation should succeed if the path is not populated                                           $subContextPathNotPopulated
      SubContext evaluation should be inconclusive if the path is invalid                                         $subContextPathInvalid
      SubContext evaluation should be inconclusive if the path is unreachable                                     $subContextPathUnreachable
      SubContext evaluation should be inconclusive if both Min/Max cardinality and AtLeastOnce are specified      $subContextMinMaxALO
      SubContext evaluation should be inconclusive if assertion is inconclusive for one or more children (regardless of failures and success) $subContextInconclusive
      If there is no range specified (Cardinality, AtLeastOnce is going to be false by default)
        SubContext evaluation should succeed if assertion succeeds for all children                               $subContextNoRangeSuccess
        SubContext evaluation should fail if assertion fails for one or more children                             $subContextNoRangeFailure
      If there is a Min/Max cardinality
        SubContext evaluation should succeed if assertion succeeds for number of children between [Min, Max] cardinality $subContextMinMaxSuccess
        SubContext evaluation should fail if assertion succeeds for number of children outside [Min, Max] cardinality    $subContextMinMaxFailure
      If there AtLeastOnce is true
        SubContext evaluation should succeed if assertion succeeds for one or more children                       $subContextALOSuccess
        SubContext evaluation should fail if assertion fails for all children                                     $subContextALOFailure
      SubContext evaluation should fail If not present behavior is FAIL and no element is found  $subContextNoElmFAIL
      SubContext evaluation should be inconclusive If not present behavior is INCONCLUSIVE and no element is found $subContextNoElmINC
      SubContext evaluation should pass If not present behavior is PASS and no element is found $subContextNoElmPASS

     ComplexPathValueSpec
      ComplexPathValue should pass if both paths are not populated                        $complexPathValueBothPathNotPopulated
      ComplexPathValue evaluation should be inconclusive if the path is invalid           $complexPathValuePathInvalid
      ComplexPathValue evaluation should be inconclusive if the path is unreachable       $complexPathValuePathUnreachable
      ComplexPathValue evaluation should be inconclusive if applied to incompatible elements (Complex/Simple)        $complexPathNotFieldOrComponent
      ComplexPathValue evaluation should fail if one of the children in one path does not satisfy operator constraint applied to its counterpart (eg : A^B^C vs A^X^C, L1^A&B&C vs L1^A&X&C)   $complexPathChildNoMatch
      ComplexPathValue evaluation should fail if one of the occurrences in one path does not satisfy operator constraint applied to its counterpart (eg: A^B^C~X^Y^Z vs A^B^C~X^Y^T)    $complexPathOccurrenceFail
      ComplexPathValue should pass if all children are equally populated and satisfy operator constraint (eg: A&B&C vs A&B&C, L1^A&B&C vs L1^A&B&C)         $complexPathValueEqPopulated
      ComplexPathValue should pass if all occurrences are equally populated and satisfy operator constraint (eg: A^B^C~X^Y^Z vs A^B^C~X^Y^Z)         $complexPathValueEqPopulatedOccurrence
      Strict Mode
        ComplexPathValue should fail if only one path is populated                                                                $complexPathValueOnePathPopulatedS
        ComplexPathValue should fail if one of the children within one path is populated and not in the other (eg: A^^C vs A^B^C, L1^A&B&C vs L1^A&&C)   $complexPathValueOnePathChildPopulatedS
        ComplexPathValue should fail if one of the occurrences has no match (all others satisfy operator constraint) (e.g A^B^C~X^Y^Z vs A^B^C)          $complexPathNotSameOccurrenceNumberStrict
      Non-Strict Mode
        ComplexPathValue should pass if only one path is populated                                                                  $complexPathValueOnePathPopulated
        ComplexPathValue should pass if one of the children within one path is populated and not in the other (eg: A^^C vs A^B^C, L1^A&B&C vs L1^A&&C)
        ComplexPathValue should pass if one of the occurrences has no match (all others satisfy operator constraint) (e.g A^B^C~X^Y^Z vs A^B^C)     $complexPathNotSameOccurrenceNumberNotStrict
      ComplexPathValue evaluation should fail If not present behavior is FAIL and both paths not found                      $complexPathValueNoElmFAIL
      ComplexPathValue evaluation should be inconclusive If not present behavior is INCONCLUSIVE and both paths not found   $complexPathValueNoElmINC
      ComplexPathValue evaluation should pass If not present behavior is PASS and both paths not found                      $complexPathValueNoElmPASS
  """
}
