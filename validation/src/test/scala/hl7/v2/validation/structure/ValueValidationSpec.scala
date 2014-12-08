package hl7.v2.validation.structure

import org.specs2.Specification


class ValueValidationSpec extends Specification { def is = s2"""

  Value Validation Specification (Length, Format and Separator in Value )

    Value validation should not report any error if the value is Null $todo
    Value validation should check the format                          $todo
    Value validation should check the use of unescaped separators     $todo
    Value validation should check the length constraint               $todo

    Length computation should be performed on unescaped value         $todo
    Length computation should take in account whitespaces in value    $todo

"""

  //TODO Usage and Cardinality check on Null 

}
