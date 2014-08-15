package hl7.v2.validation.vs

/**
  * Trait defining the value set validator
  */

/**
  * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
  */

trait Validator {

  /**
    * Checks if the code is present in the specified code set
    * @param code - The code
    * @param codeSet - The code set
    * @return The result which is:
    *    a) Pass if the code is in the code set
    *    b) CodeSetNotFound if the code set cannot be found
    *    c) Fail if the code is not in the code set
    */
  def checkCodeSet( code: String, codeSet: String ): Validator.Result
}

object Validator {
  sealed trait Result
  case object Pass extends Result
  case object Fail extends Result
  case object CodeSetNotFound extends Result
}
