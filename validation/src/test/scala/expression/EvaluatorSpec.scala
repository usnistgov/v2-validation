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
      SimpleValue evaluation should fail If not present behavior is FAIL and no element is found  $simpleValueNoElmFAIL
      SimpleValue evaluation should be inconclusive If not present behavior is INCONCLUSIVE and no element is found $simpleValueNoElmINC
      SimpleValue evaluation should pass If not present behavior is PASS and no element is found $simpleValueNoElmPASS

    PathValueSpec
      PathValue should pass if both pass are not populated                         $pathValueBothPathNotPopulated
      PathValue evaluation should be inconclusive if the path is complex           $pathValuePathComplex
      PathValue evaluation should be inconclusive if the path is invalid           $pathValuePathInvalid
      PathValue evaluation should be inconclusive if the path is unreachable       $pathValuePathUnreachable
      PathValue should fail if only one path is populated                          $pathValueOnePathPopulated
      PathValue should be inconclusive if path1 and path2 resolve to many elements $pathValueManyElems
      PathValue should pass if operator = < and path1.value < path2.value          $pathValuePass
      PathValue should fail if operator = < and path1.value > path2.value          $pathValueFail
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
      StringFormat evaluation should fail If not present behavior is FAIL and no element is found  $stringFormatNoElmFAIL
      StringFormat evaluation should be inconclusive If not present behavior is INCONCLUSIVE and no element is found $stringFormatNoElmINC
      StringFormat evaluation should pass If not present behavior is PASS and no element is found $stringFormatNoElmPASS
  """
}