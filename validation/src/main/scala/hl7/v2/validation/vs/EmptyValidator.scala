package hl7.v2.validation.vs

/**
  * An empty value set validator. It will returns CodeSetNotFound for each call.
  */
trait EmptyValidator extends Validator {

  def checkCodeSet( code: String, codeSet: String ) = Validator.CodeSetNotFound
}
