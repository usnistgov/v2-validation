package hl7.v2.validation.content

import expression.Expression

case class Reference( chapter: String, section: String, page: Int, url: String )

/**
  * A constraint describes an assertion that shall always be verified
  */
case class Constraint( 
    id: Option[String],
    reference: Option[Reference],
    description: Option[String],
    assertion: Expression
)
