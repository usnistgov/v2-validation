package expression

trait EvaluatorSpec
  extends org.specs2.Specification
  with PresenceSpec
  with PlainTextSpec
  with FormatSpec
  with StringListSpec
  with NumberListSpec
  with ANDSpec
  with ORSpec
  with NOTSpec
  with PluginSpec
  with Mocks { def is = s2"""

  Expression evaluator specifications

    Given the following elements:

    ${ elementsDescription /* See ElementsMocks for details */ }

    Presence expression evaluation specifications
      Presence evaluation should be inconclusive if the path is invalid             $presencePathInvalid
      Presence evaluation should be inconclusive if the path is unreachable         $presencePathUnreachable
      Presence should pass if the path is populated                                 $presencePathPopulated
      Presence should fail if the path is not populated                             $presencePathNotPopulated

    PlainText  expression evaluation specifications
      PlainText evaluation should succeed if the path is not populated              $plainTextPathNotPopulated
      PlainText evaluation should be inconclusive if the path is complex            $plainTextPathComplex
      PlainText evaluation should be inconclusive if the path is invalid            $plainTextPathInvalid
      PlainText evaluation should be inconclusive if the path is unreachable        $plainTextPathUnreachable
      PlainText should pass if the values are the same                              $plainTextSameValue
      PlainText should pass if the values are the same by ignoring the case         $plainTextSameValueIC
      PlainText should fail if the values are different                             $plainTextDifferentValue
      PlainText should fail for same values in different case when case not ignored $plainTextSameValueCNI

    FormatSpec
      Format evaluation should succeed if the path is not populated                 $formatPathNotPopulated
      Format should pass if the value match the pattern                             $formatMatch
      Format should fail if the value doesn't match the pattern                     $formatNoMatch

    StringListSpec
      StringList evaluation should succeed if the path is not populated            $stringListPathNotPopulated
      StringList evaluation should be inconclusive if the path is complex          $stringListPathComplex
      StringList evaluation should be inconclusive if the path is invalid          $stringListPathInvalid
      StringList evaluation should be inconclusive if the path is unreachable      $stringListPathUnreachable
      StringList should pass if the values are in the list                         $stringListValueInList
      StringList should fail if the values are in the list                         $stringListValueNotInList

    NumberListSpec
      NumberList evaluation should succeed if the path is not populated            $numberListPathNotPopulated
      NumberList evaluation should be inconclusive if the path is complex          $numberListPathComplex
      NumberList evaluation should be inconclusive if the path is invalid          $numberListPathInvalid
      NumberList evaluation should be inconclusive if the path is unreachable      $numberListPathUnreachable
      NumberList should be inconclusive if at least one value is not a number      $numberListNaN
      NumberList should pass if the values are in the list                         $numberListValueInList
      NumberList should fail if the values are in the list                         $numberListValueNotInList

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

    NOT expression evaluation specifications
      NOT should be inconclusive if the underlining expression is inconclusive      $notInconclusive
      NOT should pass if the underlining expression fail                            $notPass
      NOT should fail if the underlining expression pass                            $notFail

    PluginSpec
      Plugin execution should fail if there is no associated function in the map      $pluginNoFunction
      Plugin execution should pass if the function returns Pass                       $pluginPass
      Plugin execution should fail if the function returns Fail                       $pluginFail
      Plugin execution should be inconclusive if the function returns is inconclusive $pluginInconclusive

  """
}