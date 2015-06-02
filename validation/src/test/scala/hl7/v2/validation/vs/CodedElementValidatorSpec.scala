/*package hl7.v2.validation.vs

import org.specs2.Specification
import org.specs2.ScalaCheck

class CodedElementValidatorSpec
    extends Specification
    with ScalaCheck
    with CodedElementValidator { def is = s2"""

  CodedElementValidation.isCodedElement should:
    Returns true when the data type is CE, CWE and CNE        $f1
    Returns false in all other cases                          $f2
  """

  private val codedElems = Seq("CE", "CWE", "CNE")

  def f1 = codedElems map { s => isCodedElement(s) === true }
  def f2 = prop { (s: String) => isCodedElement(s) === (codedElems contains s) }
}
*/