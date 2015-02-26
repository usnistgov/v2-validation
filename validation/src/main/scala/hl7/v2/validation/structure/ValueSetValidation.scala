package hl7.v2.validation.structure

import hl7.v2.instance.{Value, Location}
import hl7.v2.validation.report._
import hl7.v2.validation.vs.CodeUsage.{P, E}
import hl7.v2.validation.vs.{Code, BindingStrength, CodeUsage, ValueSet}

object ValueSetValidation {

  /**
    * Checks if the value 'v' against the table identified by 'table'
    */
  def checkValueSet(l: Location, v: Value, table: Option[String])
                   (implicit library: Map[String, ValueSet]) : Option[SEntry] =
    table match {
      case None       => None
      case Some(spec) =>
        val(id, bsp) = spec span ( _ != '#' )
        val bs       = bsp drop 1
        library get id match {
          case None     => Some( VSNotFound(l, v, id, bs) )
          case Some(vs) => checkValueSet(l, v, vs, bs)
        }
    }

  /**
    * Checks if the value 'v' against the value set 'vs'
    */
  def checkValueSet(l: Location, v: Value, vs: ValueSet, bs: String): Option[SEntry] =
    vs.codes filter ( c => c.value == v.raw ) match {
      case Nil      => Some( CodeNotFound(l, v, vs, bs) )
      case x :: Nil => checkCodeUsage(x.usage, l, v, vs, bs)
      case x :: xs  => vsSpecError(l, x, vs)
    }

  /**
    * Returns a detection if the code usage is E or P
    */
  def checkCodeUsage(usage: CodeUsage, l: Location, v: Value, vs: ValueSet,
                     bs: BindingStrength): Option[SEntry] = usage match {
    case E => Some( EVS(l, v, vs, bs) )
    case P => Some( PVS(l, v, vs, bs) )
    case _ => None
  }

  private def vsSpecError(l: Location, code: Code, vs: ValueSet) = {
    val m = s"More than one code '${code.value}' found in the value set '${vs.id}'"
    Some( VSSpecError(l, vs.id, m) )
  }

  implicit private def value(v: Value): String = v.raw

  implicit private def bindingStrength(s: String): BindingStrength =
    s match {
      case "R" => BindingStrength.R
      case "S" => BindingStrength.S
      case "U" => BindingStrength.U
      case _   => throw new Exception(s"Invalid Binding Strength '$s'")
    }
}
