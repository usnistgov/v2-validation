package hl7.v2.validation.content

import expression.Expression

case class Pattern (
  trigger : Trigger,
  constraints : List[Constraint],
  contexts  : List[Context]
)

case class Context (
  contextPath : String,
  Patterns    : List[Pattern]
)

case class Trigger (
  errorMessage : String,
  expression   : Expression
)
