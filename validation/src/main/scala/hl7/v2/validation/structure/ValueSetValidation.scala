/*package hl7.v2.validation.structure

import hl7.v2.instance.{Simple, Value, Location}
import hl7.v2.profile.{ValueSetSpec, BindingStrength}
import hl7.v2.validation.report._
import hl7.v2.validation.vs.CodeUsage.{P, E}
import hl7.v2.validation.vs.{Code, CodeUsage, ValueSet}

object ValueSetValidation {


  def check(s: Simple, table: Option[String])
           (implicit library: Map[String, ValueSet]) : List[SEntry] = ???

  /**
    * Checks if the value 'v' against the table identified by 'table'
    */
  def checkValueSet(l: Location, v: Value, vso: List[ValueSetSpec])
                   (implicit library: Map[String, ValueSet]) : List[SEntry] =
    vso match {
      case Nil       => Nil
      case x :: Nil =>
        val id = x.valueSetId
        val bs = x.bindingStrength
        library get id match {
          case None     => VSNotFound(l, v, id, bs) :: Nil
          case Some(vs) => checkValueSet(l, v, vs, bs)
        }
      case xs => ??? //TODO
    }

  /**
    * Checks if the value 'v' against the value set 'vs'
    */
  def checkValueSet(l: Location, v: Value, vs: ValueSet,
                    bs: Option[BindingStrength]): List[SEntry] =
    vs.codes filter ( c => c.value == v.raw ) match {
      case Nil      => CodeNotFound(l, v, vs, bs) :: Nil
      case x :: Nil => checkCodeUsage(x.usage, l, v, vs, bs)
      case x :: xs  => vsSpecError(l, x, vs)
    }

  /**
    * Returns a detection if the code usage is E or P
    */
  def checkCodeUsage(usage: CodeUsage, l: Location, v: Value, vs: ValueSet,
                    bs: Option[BindingStrength]): List[SEntry] =
    usage match {
      case E => EVS(l, v, vs, bs) :: Nil
      case P => PVS(l, v, vs, bs) :: Nil
      case _ => Nil
    }

  /**
    * Returns a detection if the code usage is E or P
    */
  /*def checkCode(c: Code, l: Location, v: Value, vs: ValueSet,
                bs: Option[BindingStrength])( f ) : List[SEntry] =
    c usage match {
      case E => EVS(l, v, vs, bs) :: Nil
      case P => PVS(l, v, vs, bs) :: Nil
      case _ => Nil
    }*/

  private def vsSpecError(l: Location, code: Code, vs: ValueSet) = {
    val m = s"More than one code '${code.value}' found in the value set '${vs.id}'"
    VSSpecError(l, vs.id, m) :: Nil
  }

  implicit private def value(v: Value): String = v.raw
}
*/