package hl7.v2.parser

import scala.util.Try

import hl7.v2.instance.Message
import hl7.v2.profile.{Message => Model}

/**
  * Default implementation of the parser
  * 
  * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
  */

trait DefaultParser extends Parser {

  def parse( message: String, model: Model ): Try[Message] = ???
}