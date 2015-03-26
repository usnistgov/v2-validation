package hl7.v2.validation.vs

import hl7.v2.instance.{Location, Value, Simple}
import hl7.v2.profile.BindingStrength
import hl7.v2.validation.report._
import hl7.v2.validation.vs.CodeUsage.{E, P}


trait DefaultSimpleElemValidator extends SimpleElementValidator {

  // Alias
  type OBS = Option[BindingStrength]

  //FIXME: HANDLE HL70396, 99ZZZ etc.

  /**
    * Checks the simple element against the value specifications
    * and returns the list of problems detected.
    */
  /*
    Preconditions:
      1) The simple element is valued and not null
      2) There is a value set spec

    Checks:
      1) Return a detection if the value set is not in the library
      2) Return a detection if the value set is empty
      3) Return a detection if the value is not in the value set
      4) If the value is in the value set then returns EVS or PVS if the
         the usage is either E or P
   */
  def check(s: Simple, library: Map[String, ValueSet]): List[VSEntry] =
    s.req.vsSpec match {
      case Nil      => Nil
      case z :: Nil =>
        val id = z.valueSetId
        val bs = z.bindingStrength
        library get id match {
          case None    => VSNotFound(s.location, s.value.raw, id, bs) :: Nil
          case Some(x) => x.codes.isEmpty match {
            case true if !s.value.isNull => EmptyVS(s.location, x) :: Nil
            case _                       => check(s.location, s.value, x, bs)
          }
        }
      case xs => ??? //FIXME
    }

  def check(l: Location, v: Value, vs: ValueSet, obs: OBS): List[VSEntry] =
    if( v.isNull ) Nil
    else vs.codes filter ( c => c.value == v.raw ) match {
      case Nil      => CodeNotFound(l, v.raw, vs, obs ) :: Nil
      case x :: Nil => checkCode(l, v, x, vs, obs)
      case x :: xs  => VSSpecError(l, moreThanOneCodeInVS(x, vs)) :: Nil
    }

  /**
    * Returns a detection if the code usage is E or P
    */
  private
  def checkCode(l: Location, v: Value, c: Code, vs: ValueSet, obs: OBS): List[VSEntry] =
    c.usage match {
      case E => EVS(l, v.raw, vs, obs) :: Nil
      case P => PVS(l, v.raw, vs, obs) :: Nil
      case _ => Nil
    }

  /**
    * Returns true is the simple element is valued and
    * not null  and there is a value set specification
    */
  //private
  //def canCheck(s: Simple): Boolean = !s.value.isNull && s.req.vsSpec.nonEmpty

  /**
    * Returns the error message when more than one oce is found
    */
  private def moreThanOneCodeInVS(c: Code, vs: ValueSet) =
    s"Multiple code '${c.value}' found in the value set '${vs.id}'"

}
