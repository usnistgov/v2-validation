package hl7.v2.validation.vs

import gov.nist.validation.report.Entry
import hl7.v2.instance.{Location, Simple, Value}
import hl7.v2.profile.ValueSetSpec
import hl7.v2.validation.report.Detections
import hl7.v2.validation.vs.CodeUsage.{E, P}

trait DefaultSimpleElemValidator {

  /**
    * Checks the simple element against the value specifications
    * and returns the list of problems detected.
    */
  /*
    Preconditions:
      1) The simple element is valued and not null
      2) There is a value set spec

    Checks:
      0) Return NoVal detection if the value set is excluded from the validation
      1) Return a detection if the value set is not in the library
      2) Return a detection if the value set is empty
      3) Return a detection if the value is not in the value set
      4) If the value is in the value set then returns EVS or PVS if the
         the usage is either E or P
   */
  def check(s: Simple, library: ValueSetLibrary): List[Entry] =
    canCheck(s) match {
      case false => Nil
      case true  =>
        val spec = s.req.vsSpec.head //FIXME We only take one spec for now ...
        val id = spec.valueSetId
        if( library skipValidation id )
          Detections.vsNoVal(s.location, id) :: Nil
        else library get id match {
          case None    => Detections.vsNotFound(s.location, s.value.raw, spec) :: Nil
          case Some(x) => x.codes.isEmpty match {
            case true => Detections.emptyVS(s.location, x, spec) :: Nil
            case _    => check(s.location, s.value, x, spec)
          }
        }
    }

  def check(l: Location, v: Value, vs: ValueSet, vsSpec: ValueSetSpec): List[Entry] =
    if( v.isNull ) Nil
    else if( skipCodeCheck(v, vs) ) Nil
    else vs.codes filter ( c => c.value == v.raw ) match {
      case Nil      => Detections.codeNotFound(l, v.raw, vs, vsSpec) :: Nil
      case x :: Nil => checkCode(l, v, x, vs, vsSpec)
      case x :: xs  =>
        val msg = s"Multiple occurrences of the code '${x.value}' found."
        Detections.vsError(l, msg, vs, vsSpec) :: Nil
    }

  /**
    * Returns a detection if the code usage is E or P
    */
  private
  def checkCode(l: Location, v: Value, c: Code, vs: ValueSet,
                spec: ValueSetSpec): List[Entry] =
    c.usage match {
      case E => Detections.evs(l, v.raw, vs, spec) :: Nil
      case P => Detections.pvs(l, v.raw, vs, spec) :: Nil
      case _ => Nil
    }

  /**
    * Returns true is the simple element is valued and
    * not null  and there is a value set specification
    */
  private
  def canCheck(s: Simple): Boolean = !s.value.isNull && s.req.vsSpec.nonEmpty

  private def skipCodeCheck(v: Value, vs: ValueSet): Boolean =
    if( vs.id matches "(HL7)?0396" )
      (v.raw matches "HL7[0-9]{4}") || (v.raw matches "99[a-zA-Z0-9]{3}")
    else false

}
