package expression

trait EvaluatorSpec
  extends org.specs2.Specification
  with Evaluator
  with PresenceSpec
  with PlainTextSpec
  with ANDSpec
  with ORSpec
  with NOTSpec
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

    AND expression evaluation specifications
      AND should be inconclusive if at least one expression is inconclusive         $andInconclusive
      AND should pass if both expressions pass                                      $andPass
      AND should fail if at least one expression fail and none is inconclusive      $andFail

    OR expression evaluation specifications
      OR should be inconclusive if the first expression is inconclusive             $orInconclusive1 
      OR should be inconclusive if the first expression fails and the second is inconclusive $orInconclusive2
      OR should pass if the first expression passes                                 $orPass1
      OR should pass if the first expression fails and the second passes            $orPass2
      OR should fail if both expressions fail                                       $orFail

    NOT expression evaluation specifications
      NOT should be inconclusive if the underlining expression is inconclusive      $notInconclusive
      NOT should pass if the underlining expression fail                            $notPass
      NOT should fail if the underlining expression pass                            $notFail
  """
}