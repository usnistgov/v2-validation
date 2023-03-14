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
  def getCodes(value: String): JList[Code]
  def contains(value: String): Boolean
  def codesList(): JList[Code]
  def getCodes: List[Code]
}

case class InternalValueSet(
    id: String,
    extensibility: Option[Extensibility],
    stability: Option[Stability],
    private val codes: List[Code]
) extends ValueSet {

  private def codeMatch(code: Code, value: String): Boolean = {
    code.pattern match {
      case Some(pattern) => value.matches(pattern)
      case None => code.value == value
    }
  }

  def isEmpty: Boolean = codes.isEmpty
  def getCodes(value: String): JList[Code] = (codes filter(c => codeMatch(c, value))).asJava
  def contains(value: String): Boolean = codes.exists(c => codeMatch(c, value))
  def codesList(): JList[Code] = codes.asJava
  def getCodes: List[Code] = codes
}

case class ExternalValueSet(
  id: String,
  url: String,
  source: String,
  extensibility: Option[Extensibility],
  stability: Option[Stability],
  private val codes: List[Code]) extends ValueSet {

  def isEmpty: Boolean = codes.isEmpty
  def getCodes(value: String): JList[Code] = ( codes filter(c => c.value == value) ).asJava
  def contains(value: String): Boolean = codes.exists(c => c.value == value )
  def codesList(): JList[Code] = codes.asJava
  def getCodes: List[Code] = codes
}
