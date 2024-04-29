package hl7.v2.validation.vs

import java.util.{List => JList}
import scala.jdk.CollectionConverters.SeqHasAsJava
/**
  * Trait representing a code usage
  */
sealed trait CodeUsage { def description: String }
object CodeUsage {
  case object R extends CodeUsage { val description = "Required"  }
  case object P extends CodeUsage { val description = "Permitted" }
  case object E extends CodeUsage { val description = "Excluded"  }

  def fromString(s: String): CodeUsage = s match {
    case "R" => R
    case "P" => P
    case "E" => E
    case "" => R
    case _ => throw new Error(s"Invalid code usage '${s}'")
  }
}

/**
  * Class representing a code
  */
case class Code(value: String, description: String, usage: CodeUsage, codeSys: String, pattern: Option[String] = None)

/**
  * Trait representing a value set extensibility
  */
sealed trait Extensibility
object Extensibility {
  case object Open  extends Extensibility
  case object Closed extends Extensibility
}

/**
  * Trait representing a value set stability
  */
sealed trait Stability
object Stability {
  case object Static  extends Stability
  case object Dynamic extends Stability
}

/**
  * Class representing a value set
  */
trait ValueSet{
  val id: String
  val extensibility: Option[Extensibility]
  val stability: Option[Stability]
  def isEmpty: Boolean
}

case class InternalValueSet(
    id: String,
    extensibility: Option[Extensibility],
    stability: Option[Stability],
    codes: List[Code]
) extends ValueSet {
  def codesAsJava(): JList[Code] = codes.asJava
  def isEmpty: Boolean = codes.isEmpty
}

case class ExternalValueSet(
    id: String,
    url: String,
    extensibility: Option[Extensibility],
    stability: Option[Stability]
 ) extends ValueSet {
  def isEmpty: Boolean = false
}
