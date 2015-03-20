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
  * ValueSetSpec companion object
  */
object ValueSetSpec {

  //ValueSetSpec format
  private val format = """([^#]+)(?:#([^#]+)(?:#([^#]+))?)?""".r

  /**
    * Create a ValueSetSpec from a string
    */
  def apply(s: String): Try[ValueSetSpec] = s match {
    case format(id, null, null) => Success( ValueSetSpec(id, None, None) )
    case format(id, bs, null  ) =>
      BindingStrength(bs) map { x => ValueSetSpec(id, Some(x), None)}
    case format(id, bs, bl) =>
      for {
        x <- BindingStrength(bs)
        y <- BindingLocation(bl)
      } yield ValueSetSpec(id, Some(x), Some(y) )
    case _ => Failure( new Exception(s"Invalid value set specification: $s") )
  }
}

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
  case class  OR(position1: Int, position2: Int) extends  BindingLocation
  case class AND(position1: Int, position2: Int) extends  BindingLocation
  case class XOR(position1: Int, position2: Int) extends  BindingLocation
  case class NBL(position: Int, bl: BindingLocation) extends BindingLocation {
    assert(
      bl match {case NBL(_, _) => false case _ => true},
      s"NBL cannot contain another NBL: $this"
    )
  }

  val pos = """\s*(\d+)\s*""".r
  val or  = """\s*(\d+)\s*or\s*(\d+)\s*""".r
  val and = """\s*(\d+)\s*and\s*(\d+)\s*""".r
  val xor = """\s*(\d+)\s*xor\s*(\d+)\s*""".r
  val nbl = """\s*(\d+)\s*:\s*((?:\d+)|(?:(?:\d+)\s*or\s*(?:\d+))|(?:(?:\d+)\s*and\s*(?:\d+))|(?:(?:\d+)\s*xor\s*(?:\d+)))\s*""".r

  def apply(s: String): Try[BindingLocation] = s match {
    case pos(p)      => Success( Position(p.toInt) )
    case or(p1, p2)  => Success( OR(p1.toInt, p2.toInt) )
    case and(p1, p2) => Success( AND(p1.toInt, p2.toInt) )
    case xor(p1, p2) => Success( XOR(p1.toInt, p2.toInt) )
    case nbl(p, bl ) =>
      apply(bl) flatMap { x => Success( NBL(p.toInt, x) ) } orElse {
        Failure( new Exception(s"Invalid Binding Location '$s'") )
      }
    case _ => Failure( new Exception(s"Invalid Binding Location '$s'") )
  }
}
