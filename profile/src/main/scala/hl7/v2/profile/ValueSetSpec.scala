package hl7.v2.profile

import scala.util.{Failure, Success, Try}

/**
  * Class representing a value set specification
  */
case class ValueSetSpec(
    valueSetId: String,
    bindingStrength: Option[BindingStrength],
    bindingLocation: Option[BindingLocation]
)

/**
  * Trait defining a value set binding strength
  */
sealed trait BindingStrength

/**
  * Value set binding strength companion object
  */
object BindingStrength {
  case object R extends BindingStrength { val description = "Required"     }
  case object S extends BindingStrength { val description = "Suggested"    }
  case object U extends BindingStrength { val description = "Undetermined" }

  def apply(s: String): Try[BindingStrength] =
    s match {
      case "R"  => Success( BindingStrength.R )
      case "S"  => Success( BindingStrength.S )
      case "U"  => Success( BindingStrength.U )
      case _    => Failure( new Exception( s"Invalid BindingStrength '$s'") )
    }
}

/**
  * Trait defining a value set binding location
  */
sealed trait BindingLocation

/**
  * Value set binding location companion object
  */
object BindingLocation {

  case class Position(value: Int) extends BindingLocation
  case class XOR(position1: Int, position2: Int) extends  BindingLocation

  val pos = """\s*(\d+)\s*""".r
  val xor = """\s*(\d+)\s*:\s*(\d+)\s*""".r

  def apply(s: String): Try[BindingLocation] = s match {
    case pos(p)      => Success( Position(p.toInt) )
    case xor(p1, p2) => Success( XOR(p1.toInt, p2.toInt) )
    case _ => Failure( new Exception(s"Invalid Binding Location '$s'") )
  }
}
