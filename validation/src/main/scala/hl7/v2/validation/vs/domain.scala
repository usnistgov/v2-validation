package hl7.v2.validation.vs

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
case class Code(value: String, description: String, usage: CodeUsage, codeSys: String)

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
case class ValueSet(
    id: String,
    extensibility: Option[Extensibility],
    stability: Option[Stability],
    codes: List[Code]
)
